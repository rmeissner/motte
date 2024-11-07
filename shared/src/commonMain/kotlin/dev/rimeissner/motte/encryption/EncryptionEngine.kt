package dev.rimeissner.motte.encryption

import utils.hexToByteArray
import utils.toHex

interface EncryptionEngine {
    fun encrypt(data: ByteArray): CryptoData
    fun decrypt(data: CryptoData): ByteArray
}

data class CryptoData(val data: ByteArray, val iv: ByteArray) {
    override fun toString(): String {
        return "${data.toHex()}$SEPARATOR${iv.toHex()}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CryptoData

        if (!data.contentEquals(other.data)) return false
        if (!iv.contentEquals(other.iv)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }

    companion object {
        const val SEPARATOR = "####"
        fun fromString(encoded: String) =
            encoded.split(SEPARATOR).let {
                if (it.size != 2) throw IllegalArgumentException("Not correctly encoded!")
                val data = it[0].hexToByteArray() ?: throw IllegalArgumentException("Could not decode data!")
                val iv = it[1].hexToByteArray() ?: throw IllegalArgumentException("Could not decode iv!")
                CryptoData(data, iv)
            }
    }
}