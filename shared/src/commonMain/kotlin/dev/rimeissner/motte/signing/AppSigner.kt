package dev.rimeissner.motte.signing

import dev.rimeissner.motte.encoding.checksum
import dev.rimeissner.motte.keys.HDNodeFactory
import dev.rimeissner.motte.keys.KeyEngine
import dev.rimeissner.motte.keys.KeyPair
import dev.rimeissner.motte.keys.Signature
import dev.rimeissner.motte.mnemonics.MAX_ENTROPY_BITS
import dev.rimeissner.motte.mnemonics.MnemonicsFactory
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import utils.hexToByteArray
import utils.toHex
import kotlin.random.Random

/**
 * The App Signer Manager uses a mnemonic to allow signing of messages.
 * The mnemonic is stored in a secure storage combining multiple encryption mechanisms.
 * As part of this a password is required to access the stored mnemonic, the "App Key".
 * The App Key is not stored in the signer manager, rather in Authorizations that can be used with the App Signer Manager.
 */
class AppSignerManager(
    private val generalStorage: GeneralStorage,
    private val secureStorage: SecureStorage,
    private val keyEngine: KeyEngine,
    private val nodeFactory: HDNodeFactory,
    private val mnemonicsFactory: MnemonicsFactory,
    private val random: Random,
    private val appSignerId: String = "default"
) {
    private val keyLock = Mutex()

    private fun withId(key: String) = "${key}::${appSignerId}"

    private fun setupAppSignerAndGetKey(): ByteArray {
        // Generate a random app key that is used to encrypt the app signer mnemonic
        val appKey = random.nextBytes(ByteArray(32))
        // Store the checksum of the app key
        generalStorage.putString(
            withId(ID_ENCRYPTION_KEY_CHECKSUM), checksum(appKey).toHex()
        )
        // Generate a random app signer mnemonic
        val mnemonic = mnemonicsFactory.generate(MAX_ENTROPY_BITS)
        // Store the app signer mnemonic using the app key
        secureStorage.store(withId(ID_SIGNER_MNEMONIC), mnemonic) { appKey }
        return appKey
    }

    private fun getEncryptionKeyChecksum(): ByteArray? =
        generalStorage.getString(withId(ID_ENCRYPTION_KEY_CHECKSUM))?.hexToByteArray()

    private fun deriveFromMnemonic(mnemonic: String, path: String): KeyPair {
        val seed = mnemonicsFactory.toSeed(mnemonic)
        val masterNode = nodeFactory.masterNode(seed)
        return nodeFactory.derive(masterNode, path).keyPair
    }

    suspend fun isSetup(): Boolean {
        keyLock.withLock {
            return getEncryptionKeyChecksum() != null
        }
    }

    suspend fun addAuth(newAuth: Authorization, existingAuth: Authorization? = null) {
        keyLock.withLock {
            // Check if we have already an encryption key by checking the presence of a checksum
            val appKey = getEncryptionKeyChecksum()?.let {
                // If there is a checksum present, use an existing Authorization to get the key
                existingAuth?.keyBytes(it)
                    ?: throw IllegalArgumentException("Missing previous password")
            } ?: setupAppSignerAndGetKey()

            newAuth.setup(appKey)
        }
    }

    private fun getKeyPair(
        authorization: Authorization, path: String
    ): KeyPair {
        if (!secureStorage.hasKey(withId(ID_SIGNER_MNEMONIC))) throw IllegalStateException("Signer not setup")
        val mnemonic = secureStorage.retrieve(withId(ID_SIGNER_MNEMONIC)) {
            authorization.keyBytes(
                getEncryptionKeyChecksum() ?: throw IllegalStateException("Signer not setup")
            )
        }
        return deriveFromMnemonic(mnemonic, path)
    }

    suspend fun publicKey(
        authorization: Authorization, path: String = DEFAULT_PATH
    ): ByteArray {
        keyLock.withLock {
            return getKeyPair(authorization, path).publicKey
        }
    }

    suspend fun sign(
        data: ByteArray, authorization: Authorization, path: String = DEFAULT_PATH
    ): Signature {
        keyLock.withLock {
            val keyPair = getKeyPair(authorization, path)
            return keyEngine.sign(keyPair, data)
        }
    }

    companion object {
        private const val DEFAULT_PATH = "m/44'/60'/0'/0/0"
        private const val ID_SIGNER_MNEMONIC = "app_signer.string.signer_mnemonic"
        private const val ID_ENCRYPTION_KEY_CHECKSUM = "app_signer.string.encryption_key_checksum"
    }
}