package dev.rimeissner.motte.safe.api

import dev.rimeissner.motte.safe.api.txs.SafeTransactionServiceApi
import dev.rimeissner.motte.safe.api.txs.TransactionServiceApi
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test


class BouncyMnemonicsFactoryTest {
    private lateinit var client: HttpClient
    private lateinit var api: TransactionServiceApi

    @BeforeTest
    fun setup() {
        client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        api = SafeTransactionServiceApi(client)
    }

    @Test
    fun loadTxs() = runBlocking {
        var transfers = api.transfers(
            "https://safe-transaction-gnosis-chain.safe.global/api/",
            "0xE3A676FdD7bD6f784C5DCA39A2ac30795B302fe4"
        )

        println(transfers.count)
        println(transfers.results.size)

        while (transfers.next != null) {
            transfers = api.moreTransfers(transfers.next!!)
            println(transfers.results.size)
        }
    }
}