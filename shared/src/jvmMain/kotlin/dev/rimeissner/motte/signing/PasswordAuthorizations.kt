package dev.rimeissner.motte.signing

import dev.rimeissner.motte.encoding.checksum
import dev.rimeissner.motte.encoding.keccak
import dev.rimeissner.motte.encryption.BouncyAESEngine
import dev.rimeissner.motte.encryption.CryptoData
import org.bouncycastle.crypto.generators.SCrypt

class PasswordAuthorization(
    private val password: String,
    private val generalStorage: GeneralStorage,
    private val passwordId: String = "default"
) : Authorization {

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
        val passwordBytes = preparePassword(password)
        val checksum = checksum(passwordBytes)
        val checksumData = BouncyAESEngine({ passwordBytes }).encrypt(checksum)
        generalStorage.putString(
            withPasswordId(ID_ENCRYPTED_CHECKSUM),
            checksumData.toString()
        )
        val appSignerKeyData = BouncyAESEngine({ passwordBytes }).encrypt(key)
        generalStorage.putString(
            withPasswordId(ID_ENCRYPTED_APP_SIGNER_KEY),
            appSignerKeyData.toString()
        )
    }

    override fun keyBytes(keyChecksum: ByteArray): ByteArray {
        val checksumData =
            generalStorage.getString(withPasswordId(ID_ENCRYPTED_CHECKSUM))
                ?: throw IllegalStateException("No password set")
        val passwordBytes = preparePassword(password)
        val passwordChecksum =
            BouncyAESEngine({ passwordBytes }).decrypt(CryptoData.fromString(checksumData))
        if (!checksum(passwordBytes).contentEquals(passwordChecksum))
            throw IllegalArgumentException("Invalid password")
        val keyData =
            generalStorage.getString(withPasswordId(ID_ENCRYPTED_APP_SIGNER_KEY))
                ?: throw IllegalStateException("No password set")
        val key = BouncyAESEngine({ passwordBytes })
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

        private const val ID_ENCRYPTED_APP_SIGNER_KEY =
            "password_authorization.string.encrypted_app_signer_key"
        private const val ID_ENCRYPTED_CHECKSUM =
            "password_authorization.string.encrypted_checksum"
    }
}