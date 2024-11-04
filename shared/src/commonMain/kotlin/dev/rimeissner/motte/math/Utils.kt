package utils

import dev.rimeissner.motte.math.BigNumber
import okio.ByteString
import kotlin.experimental.and

import kotlin.math.min

fun String.padStartMultiple(multiple: Int, padChar: Char = ' ') =
    this.padStart(
        if (this.length % multiple != 0) this.length + multiple - this.length % multiple else 0,
        padChar
    )

fun String.padEndMultiple(multiple: Int, padChar: Char = ' ') =
    this.padEnd(
        if (this.length % multiple != 0) this.length + multiple - this.length % multiple else 0,
        padChar
    )

fun ByteString.bigNum() = BigNumber.from(1, toByteArray())

fun String.hexBigNum() = BigNumber.from(this, 16)

fun BigNumber.toBytes(numBytes: Int): ByteArray {
    val bytes = ByteArray(numBytes)
    val biBytes = toBytes()
    // We want to cut off the bytes on the left if there is too much
    val start = if (biBytes.size > numBytes) biBytes.size - numBytes else 0
    val length = min(biBytes.size, numBytes)
    biBytes.copyInto(
        bytes,
        // If there is less bytes than specified we offset to pad with 0 bytes
        destinationOffset = numBytes - length,
        startIndex = start,
        endIndex = length + start
    )
    return bytes
}

private val hexArray = "0123456789abcdef".toCharArray()

fun ByteArray.toHex(): String {
    val hexChars = CharArray(this.size * 2)
    for (j in this.indices) {
        val v = ((this[j] and 0xFF.toByte()).toInt() + 256) % 256
        hexChars[j * 2] = hexArray[v ushr 4]
        hexChars[j * 2 + 1] = hexArray[v and 0x0F]
    }
    return hexChars.concatToString()
}

fun String.hexToByteArray(): ByteArray {
    val s = this.removePrefix("0x")
    val len = s.length
    val data = ByteArray(len / 2)
    var i = 0
    while (i < len) {
        data[i / 2] =
            ((s[i].digitToInt(radix = 16) shl 4) + s[i + 1].digitToInt(radix = 16)).toByte()
        i += 2
    }
    return data
}