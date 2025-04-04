package dev.rimeissner.motte.safe.actions

import dev.rimeissner.motte.safe.api.txs.MultisigTxTS
import dev.rimeissner.motte.safe.api.txs.PageTS
import dev.rimeissner.motte.safe.api.txs.SafeTransactionServiceApi
import dev.rimeissner.motte.safe.api.txs.TransferTS
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed class Action(
    open val date: String,
    open val direction: Direction,
    open val ethereumTxHash: String?
) {
    enum class Direction {
        INCOMING, OUTGOING, UNKNOWN
    }

    data class Transfer(
        override val date: String,
        override val direction: Direction,
        override val ethereumTxHash: String?
    ) : Action(
        date,
        direction,
        ethereumTxHash
    )

    data class MutlisigTx(
        override val date: String,
        override val direction: Direction,
        override val ethereumTxHash: String?
    ) : Action(
        date,
        direction,
        ethereumTxHash
    )

    data class Group(
        override val date: String,
        override val direction: Direction,
        override val ethereumTxHash: String?,
        val actions: List<Action>
    ) : Action(
        date,
        direction,
        ethereumTxHash
    )
}

interface ActionIterator {
    suspend fun next(advance: Boolean = true): Action?
}

abstract class TxServiceIterator : ActionIterator {

    private val mutex = Mutex()
    private val cache: MutableList<Action> = mutableListOf()
    private var index = -1
    private var cursor: String? = null

    internal abstract suspend fun initialLoad(): ActionsPage
    internal abstract suspend fun loadMore(cursor: String?): ActionsPage

    private fun cache(check: String?, page: ActionsPage) {
        // We use the check to ensure that we are not caching the same page twice
        if (check != cursor) return
        cache.addAll(page.action)
        cursor = page.cursor
    }

    private suspend fun checkInitialLoad() {
        if (index < 0) {
            cache(cursor, initialLoad())
            index = 0
        }
    }

    private suspend fun checkMoreData() {
        val currentCursor = cursor
        if (index >= cache.size && currentCursor != null)
            cache(currentCursor, loadMore(currentCursor))
    }

    override suspend fun next(advance: Boolean): Action? {
        mutex.withLock {
            checkInitialLoad()
            checkMoreData()
            val nextAction = cache.getOrNull(index)
            // We only increment the index if we could return an action
            if (advance && nextAction != null) index++
            return nextAction
        }
    }
}


class TxServiceTransferIterator(
    private val api: SafeTransactionServiceApi,
    private val endpoint: String,
    private val safe: String,
) : TxServiceIterator() {

    private fun TransferTS.mapDirection(): Action.Direction =
        when {
            from == safe -> Action.Direction.OUTGOING
            to == safe -> Action.Direction.INCOMING
            else -> Action.Direction.UNKNOWN
        }

    private fun PageTS<TransferTS>.mapPage(): ActionsPage =
        ActionsPage(
            action = results.map { transfer ->
                Action.Transfer(
                    date = transfer.executionDate,
                    direction = transfer.mapDirection(),
                    ethereumTxHash = transfer.transactionHash
                )
            },
            cursor = next
        )

    override suspend fun initialLoad(): ActionsPage =
        api.transfers(endpoint, safe).mapPage()

    override suspend fun loadMore(cursor: String?): ActionsPage =
        cursor?.let { api.moreTransfers(it).mapPage() } ?: ActionsPage(emptyList(), null)
}

data class ActionsPage(
    val action: List<Action>,
    val cursor: String?,
)

class TxServiceMultisigTxIterator(
    private val api: SafeTransactionServiceApi,
    private val endpoint: String,
    private val safe: String,
) : TxServiceIterator() {

    private fun PageTS<MultisigTxTS>.mapPage(): ActionsPage =
        ActionsPage(
            action = results.map { tx ->
                Action.MutlisigTx(
                    date = tx.executionDate ?: tx.submissionDate,
                    direction = Action.Direction.OUTGOING,
                    ethereumTxHash = tx.transactionHash
                )
            },
            cursor = next
        )

    override suspend fun initialLoad(): ActionsPage =
        api.multisigTxs(endpoint, safe).mapPage()

    override suspend fun loadMore(cursor: String?): ActionsPage =
        cursor?.let { api.moreMultisigTxs(it).mapPage() } ?: ActionsPage(emptyList(), null)
}

class CombinedActionIterator(
    private val iterators: List<ActionIterator>
) : ActionIterator {
    override suspend fun next(advance: Boolean): Action? {
        var next: ActionIterator? = null
        for (iterator in iterators) {
            val nextVal = next?.next(false)
            val peekVal = iterator.next(false)
            // If we don't have a next or if the value is smaller then the next we set the iter
            if (nextVal == null || peekVal != null && peekVal.date > nextVal.date) next = iterator
        }
        return next?.next(advance)
    }

}