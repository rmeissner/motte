package dev.rimeissner.motte.safe.api.txs

import dev.rimeissner.motte.math.BigNumber
import dev.rimeissner.motte.solidity.Address
import dev.rimeissner.motte.utils.toHexString
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.appendPathSegments


interface TransactionServiceApi {
    suspend fun transfers(
        base: String,
        address: String,
        token: String? = null,
        beforeDate: String? = null
    ): PageTS<TransferTS>

    suspend fun moreTransfers(
        url: String
    ): PageTS<TransferTS>

    suspend fun multisigTxs(
        base: String,
        address: String,
        nonce: Long? = null,
        beforeDate: String? = null
    ): PageTS<MultisigTxTS>

    suspend fun moreMultisigTxs(
        url: String
    ): PageTS<MultisigTxTS>
}

class SafeTransactionServiceApi(
    private val httpClient: HttpClient
) : TransactionServiceApi {

    override suspend fun transfers(
        base: String,
        address: String,
        token: String?,
        beforeDate: String?
    ): PageTS<TransferTS> =
        httpClient.get(base) {
            url {
                appendPathSegments(
                    "v1",
                    "safes",
                    address,
                    "transfers"
                )
                token?.let {
                    if (it == Address(BigNumber.ZERO).toHexString())
                        parameter("ether", "true")
                    else
                        parameter("token_address", it)
                }
                beforeDate?.let {
                    parameter("execution_date__lte", it)
                }
            }
        }.body()

    override suspend fun moreTransfers(url: String): PageTS<TransferTS> =
        httpClient.get(url).body()

    override suspend fun multisigTxs(
        base: String,
        address: String,
        nonce: Long?,
        beforeDate: String?
    ): PageTS<MultisigTxTS> =
        httpClient.get(base) {
            url {
                appendPathSegments(
                    "v1",
                    "safes",
                    address,
                    "multisig-transactions"
                )
                nonce?.let {
                    parameter("nonce__gte", it)
                }
                beforeDate?.let {
                    parameter("submission_date__lte", it)
                }
            }
            println(url.build())
        }.body()

    override suspend fun moreMultisigTxs(url: String): PageTS<MultisigTxTS> =
        httpClient.get(url).body()
}