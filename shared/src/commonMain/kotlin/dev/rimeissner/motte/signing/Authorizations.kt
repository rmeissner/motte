package dev.rimeissner.motte.signing

interface Authorization {
    fun keyBytes(keyChecksum: ByteArray): ByteArray
    fun setup(key: ByteArray)
}