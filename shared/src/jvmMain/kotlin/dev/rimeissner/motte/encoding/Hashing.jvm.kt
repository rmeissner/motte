package dev.rimeissner.motte.encoding

import org.bouncycastle.crypto.digests.KeccakDigest

private const val DEFAULT_SIZE = 256

actual fun ByteArray.keccak(): ByteArray =
    doDigest(this, KeccakDigest(DEFAULT_SIZE))

private fun doDigest(message: ByteArray, digest: KeccakDigest): ByteArray {
    val hash = ByteArray(digest.digestSize)

    if (message.isNotEmpty()) {
        digest.update(message, 0, message.size)
    }
    digest.doFinal(hash, 0)
    return hash
}