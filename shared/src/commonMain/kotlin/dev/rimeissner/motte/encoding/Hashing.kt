package dev.rimeissner.motte.encoding

expect fun ByteArray.keccak(): ByteArray

fun checksum(key: ByteArray): ByteArray =
    key.keccak().sliceArray(0..5)