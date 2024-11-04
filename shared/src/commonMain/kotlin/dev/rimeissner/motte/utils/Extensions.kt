package dev.rimeissner.motte.utils

import dev.rimeissner.motte.encoding.Base58Utils
import dev.rimeissner.motte.keys.HDNode
import okio.Buffer

fun String.trimWhitespace() = trim().replace("\\s+".toRegex(), " ")

fun String.words() = trimWhitespace().split(" ")
    .let { if (it.size == 1 && it[0].isBlank()) emptyList() else it }

fun ByteArray.toBinaryString(): String {
    val sb = StringBuilder(this.size * Byte.SIZE_BITS)
    for (i in 0 until Byte.SIZE_BITS * this.size) {
        sb.append(if (this[i / Byte.SIZE_BITS].toInt() shl i % Byte.SIZE_BITS and 0x80 == 0) '0' else '1')
    }
    return sb.toString()
}

/**
 * @return array of indexes for the specified collection.
 */
fun <T> Collection<T>.getIndexes(items: List<T>): Array<Int> {
    if (items.isEmpty()) return emptyArray()

    return items.map {
        val index = this.indexOf(it)
        if (index == -1) throw IllegalArgumentException("$it is not present on the list")
        index
    }.toTypedArray()
}

private const val VERSION = 0x0488ade4

fun HDNode.toBase58(): String {
    return Base58Utils.encodeChecked(
        Buffer()
            // var network = this.keyPair.network
            // var version = (!this.isNeutered()) ? network.bip32.private : network.bip32.public
            // 4 bytes: version bytes
            .writeInt(VERSION)
            // 1 byte: depth: 0x00 for master nodes, 0x01 for level-1 descendants, ....
            .writeByte(depth)
            // 4 bytes: the parentFingerprint of the parent's key (0x00000000 if master key)
            .write(parentFingerprint)
            // 4 bytes: child number. This is the number i in xi = xpar/i, with xi the key being serialized.
            .writeInt(index.toInt())
            // 32 bytes: the chain code
            .write(chainCode)
            // 33 bytes: private key data (0 + 32 key)
            .writeByte(0)
            .write(keyPair.privateKey ?: throw IllegalStateException("No private key"))
            .readByteString()
    )
}