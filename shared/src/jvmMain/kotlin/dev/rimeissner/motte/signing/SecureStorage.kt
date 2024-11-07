package dev.rimeissner.motte.signing

import dev.rimeissner.motte.encryption.BouncyAESEngine
import dev.rimeissner.motte.encryption.CryptoData
import dev.rimeissner.motte.encryption.EncryptionEngine


class SecureDeviceStorage(
    private val deviceEncryption: EncryptionEngine,
    private val generalStorage: GeneralStorage
): SecureStorage {

    override fun hasKey(key: String): Boolean = generalStorage.hasKey(key)

    private fun chainDecrypt(data: String, vararg engines: EncryptionEngine): String {
        var decrypted = data
        engines.forEach {
            decrypted = String(it.decrypt(CryptoData.fromString(decrypted)))
        }
        return decrypted
    }

    override fun retrieve(key: String, passwordProvider: () -> ByteArray): String {
        val storedData =
            generalStorage.getString(key) ?: throw IllegalArgumentException("No data for key $key")
        return chainDecrypt(storedData, BouncyAESEngine(passwordProvider), deviceEncryption)
    }

    private fun chainEncrypt(data: String, vararg engines: EncryptionEngine): String {
        var encrypted = data
        engines.forEach {
            encrypted = it.encrypt(encrypted.toByteArray()).toString()
        }
        return encrypted
    }

    override fun store(key: String, data: String, passwordProvider: () -> ByteArray) {
        generalStorage.putString(
            key,
            chainEncrypt(data, deviceEncryption, BouncyAESEngine(passwordProvider))
        )
    }
}