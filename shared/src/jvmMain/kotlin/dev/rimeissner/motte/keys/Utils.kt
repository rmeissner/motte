package dev.rimeissner.motte.keys

import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.bouncycastle.jcajce.provider.digest.RIPEMD160
import java.math.BigInteger
import java.security.NoSuchAlgorithmException

/**
 * The regular {@link java.math.BigInteger#toByteArray()} method isn't quite what we often need:
 * it appends a leading zero to indicate that the number is positive and may need padding.
 *
 * @param numBytes the desired size of the resulting byte array
 * @return numBytes byte long array.
 */
fun BigInteger.toBytes(numBytes: Int): ByteArray {
    val bytes = ByteArray(numBytes)
    val biBytes = toByteArray()
    val start = if (biBytes.size == numBytes + 1) 1 else 0
    val length = kotlin.math.min(biBytes.size, numBytes)
    System.arraycopy(biBytes, start, bytes, numBytes - length, length)
    return bytes
}

fun ByteString.hash160(): ByteString {
    try {
        val digest = RIPEMD160.Digest().digest(sha256().toByteArray())
        return digest.toByteString(0, digest.size)
    } catch (e: NoSuchAlgorithmException) {
        throw AssertionError(e)
    }
}

fun HDNode.fingerprint() =
    keyPair.publicKey
        .let { it.toByteString(0, it.size) }
        .hash160()
        .substring(0, 4)
        .toByteArray()