package motte.crypto.signing

import dev.rimeissner.motte.encoding.keccak
import dev.rimeissner.motte.keys.BouncyHDNodeFactory
import dev.rimeissner.motte.keys.BouncyKeyEngine
import dev.rimeissner.motte.mnemonics.BouncyMnemonicsFactory
import dev.rimeissner.motte.mnemonics.wordlists.ENGLISH_WORD_LIST
import dev.rimeissner.motte.signing.AppSignerManager
import dev.rimeissner.motte.signing.GeneralStorage
import dev.rimeissner.motte.signing.PasswordAuthorization
import dev.rimeissner.motte.signing.SecureDeviceStorage
import kotlinx.coroutines.runBlocking
import motte.crypto.mocks.MockStorage
import motte.crypto.mocks.PassThroughEncryptionEngine
import org.bouncycastle.crypto.InvalidCipherTextException
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.security.SecureRandom
import kotlin.random.asKotlinRandom
import kotlin.test.assertNull


class AppSignerManagerTest {
    private lateinit var generalStorage: GeneralStorage
    private lateinit var secureStorage: SecureDeviceStorage
    private lateinit var appSigner: AppSignerManager

    @Before
    fun setup() {
        generalStorage = MockStorage()
        secureStorage = SecureDeviceStorage(
            deviceEncryption = PassThroughEncryptionEngine(),
            generalStorage = generalStorage
        )
        appSigner = AppSignerManager(
            generalStorage = generalStorage,
            secureStorage = secureStorage,
            keyEngine = BouncyKeyEngine(),
            nodeFactory = BouncyHDNodeFactory(),
            mnemonicsFactory = BouncyMnemonicsFactory(ENGLISH_WORD_LIST),
            random = SecureRandom().asKotlinRandom()
        )
    }

    @Test
    fun addMultipleAuthorizations() = runBlocking {
        val auth1 = PasswordAuthorization("pw1", generalStorage)
        assertNull(generalStorage.getString(ID_APP_SIGNER_KEY_CHECKSUM))
        appSigner.addAuth(auth1)
        assertNotNull(generalStorage.getString(ID_APP_SIGNER_KEY_CHECKSUM))
        val auth2 = PasswordAuthorization("pw2", generalStorage)
        appSigner.addAuth(auth2, auth1)

        val auth3 = PasswordAuthorization("pw3", generalStorage)
        // Check that initial password authorization was invalidated
        assertThrows(InvalidCipherTextException::class.java) {
            runBlocking { appSigner.addAuth(auth3, auth1) }
        }
        appSigner.addAuth(auth3, auth2)
    }

    @Test
    fun addMultipleAuthorizationsDifferentIds() = runBlocking {
        val auth1 = PasswordAuthorization("pw1", generalStorage)
        assertNull(generalStorage.getString(ID_APP_SIGNER_KEY_CHECKSUM))
        appSigner.addAuth(auth1)
        assertNotNull(generalStorage.getString(ID_APP_SIGNER_KEY_CHECKSUM))
        val auth2 = PasswordAuthorization("pw2", generalStorage, "auth2")
        appSigner.addAuth(auth2, auth1)

        val auth3 = PasswordAuthorization("pw3", generalStorage)
        appSigner.addAuth(auth3, auth1)

        // Check that initial password authorization was invalidated by auth3
        val auth4 = PasswordAuthorization("pw4", generalStorage, "auth2")
        assertThrows(InvalidCipherTextException::class.java) {
            runBlocking { appSigner.addAuth(auth4, auth1) }
        }
        appSigner.addAuth(auth4, auth2)
    }

    companion object {
        private const val ID_APP_SIGNER = "key_manager.string.app_signer"
        private const val ID_APP_SIGNER_KEY_CHECKSUM = "key_manager.string.app_signer_key_checksum"
    }
}