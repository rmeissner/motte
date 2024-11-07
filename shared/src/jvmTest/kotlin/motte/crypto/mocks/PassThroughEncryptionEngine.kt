package motte.crypto.mocks

import dev.rimeissner.motte.encryption.CryptoData
import dev.rimeissner.motte.encryption.EncryptionEngine

class PassThroughEncryptionEngine: EncryptionEngine {
    override fun encrypt(data: ByteArray): CryptoData =
        CryptoData(data, byteArrayOf(0x1))

    override fun decrypt(data: CryptoData): ByteArray =
        data.data
}