package dev.rimeissner.motte.safe.actions

import dev.rimeissner.motte.safe.api.txs.SafeTransactionServiceApi
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test


class CombinedActionIteratorTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val unconfinedDispatcher = UnconfinedTestDispatcher()
    private lateinit var client: HttpClient
    private lateinit var iterator: CombinedActionIterator

    @BeforeTest
    fun setup() {
        client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val api = SafeTransactionServiceApi(client)
        iterator = CombinedActionIterator(
            listOf(
                TxServiceTransferIterator(
                    api,
                    "https://safe-transaction-gnosis-chain.safe.global/api/",
                    "0xF3076139089aBf36b15BFbe34D23246BD439a50B"
                ),
                TxServiceMultisigTxIterator(
                    api,
                    "https://safe-transaction-gnosis-chain.safe.global/api/",
                    "0xF3076139089aBf36b15BFbe34D23246BD439a50B"
                )
            )
        )
    }

    @Test
    fun loadTxs() = runTest(unconfinedDispatcher) {
        var next: Action? = null
        val group = mutableListOf<Action>()
        do {
            val prev = next
            prev?.let { group.add(it) }
            next = iterator.next()
            if (prev != null && prev.ethereumTxHash != next?.ethereumTxHash) {
                println(
                    if (group.size > 1)
                        Action.Group(
                            prev.date,
                            Action.Direction.UNKNOWN,
                            prev.ethereumTxHash,
                            group
                        )
                    else prev
                )
                group.clear()
            }
        } while (next != null)
    }
}