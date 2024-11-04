package dev.rimeissner.motte.encoding

import dev.rimeissner.motte.math.BigNumber
import okio.ByteString
import okio.ByteString.Companion.toByteString

// TODO: Replace with Komputing version: https://github.com/komputing/KBase58/tree/master
object Base58Utils {
    fun encode(source: ByteString): String {
        val sourceSize = source.size
        if (sourceSize == 0) {
            return ""
        }

        var intData: BigNumber

        try {
            intData = BigNumber.from(1, source.toByteArray())
        } catch (e: NumberFormatException) {
            return ""
        }


        val result = StringBuilder()

        while (intData.compareTo(BigNumber.ZERO) == 1) {
            val quotientAndRemainder = intData.divRem(BASE)

            val quotient = quotientAndRemainder[0]
            val remainder = quotientAndRemainder[1]

            intData = quotient

            result.append(ALPHABET[remainder.toInt()])
        }

        var i = 0
        while (i < sourceSize && source[i] == ZERO_BYTE) {
            result.append(LEADER)
            i++
        }

        return result.reverse().toString()
    }

    private fun addChecksum(data: ByteString): ByteString {
        val checksum = data.sha256().sha256()

        val resultSize = data.size + 4
        val result = ByteArray(resultSize)

        data.toByteArray().copyInto(result)
        checksum.toByteArray().copyInto(result, endIndex = 4, destinationOffset = data.size)

        return result.toByteString(0, resultSize)
    }

    fun encodeChecked(data: ByteString): String {
        return encode(addChecksum(data))
    }

    private const val ZERO_BYTE = 0.toByte()
    private const val ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
    private const val LEADER = ALPHABET.get(0)

    private val BASE = BigNumber.from(ALPHABET.length.toLong())
}