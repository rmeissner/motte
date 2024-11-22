package dev.rimeissner.motte.signing

import dev.rimeissner.motte.encoding.checksum
import dev.rimeissner.motte.keys.HDNodeFactory
import dev.rimeissner.motte.keys.KeyEngine
import dev.rimeissner.motte.keys.KeyPair
import dev.rimeissner.motte.keys.Signature
import dev.rimeissner.motte.mnemonics.MnemonicsFactory
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import utils.hexToByteArray
import utils.toHex
import kotlin.random.Random

class AppSignerManager(
    private val generalStorage: GeneralStorage,
    private val secureStorage: SecureStorage,
    private val keyEngine: KeyEngine,
    private val nodeFactory: HDNodeFactory,
    private val mnemonicsFactory: MnemonicsFactory,
    private val random: Random
) {
    private val keyLock = Mutex()

    private fun setupAppSignerKey(): ByteArray {
        val appKey = random.nextBytes(ByteArray(32))
        generalStorage.putString(ID_APP_SIGNER_KEY_CHECKSUM, checksum(appKey).toHex())
        return appKey
    }

    private fun getAppSignerKeyChecksum(): ByteArray? =
        generalStorage.getString(ID_APP_SIGNER_KEY_CHECKSUM)?.hexToByteArray()

    private fun deriveFromMnemonic(mnemonic: String, path: String): KeyPair {
        val seed = mnemonicsFactory.toSeed(mnemonic)
        val masterNode = nodeFactory.masterNode(seed)
        return nodeFactory.derive(masterNode, path).keyPair
    }

    suspend fun isSetup(): Boolean {
        keyLock.withLock {
            return getAppSignerKeyChecksum() != null
        }
    }

    suspend fun addAuth(newAuth: Authorization, existingAuth: Authorization? = null) {
        keyLock.withLock {
            val appKey = getAppSignerKeyChecksum()?.let {
                existingAuth?.keyBytes(it)
                    ?: throw IllegalArgumentException("Missing previous password")
            } ?: setupAppSignerKey()

            newAuth.setup(appKey)
        }
    }

    suspend fun sign(data: ByteArray, authorization: Authorization): Signature {
        keyLock.withLock {
            if (!secureStorage.hasKey(ID_APP_SIGNER)) throw IllegalStateException("Signer not setup")
            val mnemonic = secureStorage.retrieve(ID_APP_SIGNER) {
                authorization.keyBytes(
                    getAppSignerKeyChecksum() ?: throw IllegalStateException("App Signer not setup")
                )
            }
            val keyPair = deriveFromMnemonic(mnemonic, "m/44'/60'/0'/0/0")
            return keyEngine.sign(keyPair, data)
        }
    }

    companion object {
        private const val ID_APP_SIGNER = "key_manager.string.app_signer"
        private const val ID_APP_SIGNER_KEY_CHECKSUM = "key_manager.string.app_signer_key_checksum"
    }
}