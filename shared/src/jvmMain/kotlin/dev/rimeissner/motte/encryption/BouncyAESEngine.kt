package dev.rimeissner.motte.encryption

import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.modes.CBCBlockCipher
import org.bouncycastle.crypto.paddings.PKCS7Padding
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import java.security.SecureRandom
import kotlin.random.Random
import kotlin.random.asKotlinRandom

class BouncyAESEngine(
    private val keyProvider: () -> ByteArray,
    private val random: Random = SecureRandom().asKotlinRandom()
): EncryptionEngine {

    override fun encrypt(data: ByteArray): CryptoData {
        return useCipher(true, keyProvider(), CryptoData(data, randomIv()))
    }

    override fun decrypt(data: CryptoData): ByteArray {
        return useCipher(false, keyProvider(), data).data
    }

    private fun randomIv() = random.nextBytes(ByteArray(16))

    private fun useCipher(encrypt: Boolean, key: ByteArray, wrapper: CryptoData): CryptoData {
        val padding = PKCS7Padding()
        val cipher =
            PaddedBufferedBlockCipher(CBCBlockCipher.newInstance(AESEngine.newInstance()), padding)
        cipher.reset()

        val keyParam = KeyParameter(key)
        val params = ParametersWithIV(keyParam, wrapper.iv)
        cipher.init(encrypt, params)

        // create a temporary buffer to decode into (it'll include padding)
        val buf = ByteArray(cipher.getOutputSize(wrapper.data.size))
        var len = cipher.processBytes(wrapper.data, 0, wrapper.data.size, buf, 0)
        len += cipher.doFinal(buf, len)

        // remove padding
        val out = ByteArray(len)
        System.arraycopy(buf, 0, out, 0, len)

        return CryptoData(out, wrapper.iv)
    }
}