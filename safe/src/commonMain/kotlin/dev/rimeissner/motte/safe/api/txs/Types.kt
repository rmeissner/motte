package dev.rimeissner.motte.safe.api.txs
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement


@Serializable
data class PageTS<T>(
    val count: Int,
    val next: String? = null,
    val previous: String? = null,
    val results: List<T>
)

@Serializable
data class TokenInfoTS(
    val type: String,
    val address: String,
    val name: String? = null,
    val symbol: String? = null,
    val decimals: Int? = null,
    val logoUri: String? = null,
    val trusted: Boolean
)

@Serializable
sealed class TransferTS {

    abstract val from: String
    abstract val to: String
    abstract val executionDate: String
    abstract val transactionHash: String

    @Serializable
    @SerialName("ERC20_TRANSFER")
    data class Erc20(
        val value: String,
        val tokenInfo: TokenInfoTS,
        override val from: String,
        override val to: String,
        override val executionDate: String,
        override val transactionHash: String,
    ) : TransferTS()

    @Serializable
    @SerialName("ERC721_TRANSFER")
    data class Erc721(
        val tokenId: String,
        val tokenInfo: TokenInfoTS,
        override val from: String,
        override val to: String,
        override val executionDate: String,
        override val transactionHash: String
    ) : TransferTS()

    @Serializable
    @SerialName("ETHER_TRANSFER")
    data class Native(
        val value: String,
        override val from: String,
        override val to: String,
        override val executionDate: String,
        override val transactionHash: String
    ) : TransferTS()
}

@Serializable
data class DataDecodedTS(
    val method: String,
    val parameters: List<DataParameterTS>
)

@Serializable
data class DataParameterTS(
    val name: String,
    val type: String,
    val value: JsonElement,
    val valueDecoded: JsonElement? = null
)

@Serializable
data class SafeActionTS(
    val to: String,
    val value: String,
    val data: String?,
    val operation: Int,
    val dataDecoded: DataDecodedTS? = null
)

@Serializable
data class MultisigTxTS (
    val to: String,
    val value: String,
    val data: String?,
    val operation: Int,
    val gasToken: String,
    val safeTxGas: Int,
    val baseGas: Int,
    val gasPrice: String,
    val refundReceiver: String,
    val nonce: Int,
    val safeTxHash: String,
    val transactionHash: String?,
    val isSuccessful: Boolean?,
    val origin: String,
    val submissionDate: String,
    val executionDate: String?,
    val dataDecoded: DataDecodedTS? = null
)