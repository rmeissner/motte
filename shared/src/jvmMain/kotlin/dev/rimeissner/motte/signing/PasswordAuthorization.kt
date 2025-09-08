package dev.rimeissner.motte.signing

import dev.rimeissner.motte.encoding.checksum
import dev.rimeissner.motte.encoding.keccak
import dev.rimeissner.motte.encryption.BouncyAESEngine
import dev.rimeissner.motte.encryption.CryptoData
import org.bouncycastle.crypto.generators.SCrypt

class PasswordAuthorization(
    password: String,
    private val generalStorage: GeneralStorage,
    private val passwordId: String = "default"
) : Authorization {

    private val passwordBytes = preparePassword(password)

    private fun preparePassword(password: String): ByteArray =
        password.toByteArray().run {
            SCrypt.generate(
                this,
                this.keccak(),
                SCRYPT_ITERATIONS,
                SCRYPT_BLOCK_SIZE,
                SCRYPT_PARALLELIZATION,
                SCRYPT_KEY_LENGTH
            )
        }

    private fun withPasswordId(key: String) =
        "${key}::${passwordId}"

    override fun setup(key: ByteArray) {
        val checksum = checksum(passwordBytes)
        val checksumData = BouncyAESEngine({ passwordBytes }).encrypt(checksum)
        generalStorage.putString(
            withPasswordId(ID_ENCRYPTED_CHECKSUM),
            checksumData.toString()
        )
        val appSignerKeyData = BouncyAESEngine({ passwordBytes }).encrypt(key)
        generalStorage.putString(
            withPasswordId(ID_ENCRYPTED_KEY),
            appSignerKeyData.toString()
        )
    }

    private fun getAndVerifyPasswordBytes(): ByteArray {
        val checksumData =
            generalStorage.getString(withPasswordId(ID_ENCRYPTED_CHECKSUM))
                ?: throw IllegalStateException("No password set")
        val passwordChecksum =
            BouncyAESEngine({ passwordBytes }).decrypt(CryptoData.fromString(checksumData))
        if (!checksum(passwordBytes).contentEquals(passwordChecksum))
            throw IllegalArgumentException("Invalid password")
        return passwordBytes
    }

    override fun verify(): Boolean {
        try {
            getAndVerifyPasswordBytes()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    override fun keyBytes(keyChecksum: ByteArray): ByteArray {
        val keyData =
            generalStorage.getString(withPasswordId(ID_ENCRYPTED_KEY))
                ?: throw IllegalStateException("No password set")
        val key = BouncyAESEngine({ getAndVerifyPasswordBytes() })
            .decrypt(CryptoData.fromString(keyData))
        if (!checksum(key).contentEquals(keyChecksum))
            throw IllegalStateException("Unexpected key")
        return key
    }

    companion object {
        private const val SCRYPT_ITERATIONS = 16384
        private const val SCRYPT_BLOCK_SIZE = 8
        private const val SCRYPT_PARALLELIZATION = 1
        private const val SCRYPT_KEY_LENGTH = 32

        private const val ID_ENCRYPTED_KEY =
            "password_authorization.string.encrypted_key"
        private const val ID_ENCRYPTED_CHECKSUM =
            "password_authorization.string.encrypted_checksum"

        fun isConfigured(
            generalStorage: GeneralStorage,
            passwordId: String = "default"
        ) =
            generalStorage.hasKey("${ID_ENCRYPTED_KEY}::${passwordId}") &&
                    generalStorage.hasKey("${ID_ENCRYPTED_CHECKSUM}::${passwordId}")
    }
}