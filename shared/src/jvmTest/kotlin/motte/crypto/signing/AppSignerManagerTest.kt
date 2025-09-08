package motte.crypto.signing

import dev.rimeissner.motte.encoding.checksum
import dev.rimeissner.motte.keys.BouncyHDNodeFactory
import dev.rimeissner.motte.keys.BouncyKeyEngine
import dev.rimeissner.motte.mnemonics.BouncyMnemonicsFactory
import dev.rimeissner.motte.mnemonics.wordlists.ENGLISH_WORD_LIST
import dev.rimeissner.motte.signing.AppSignerManager
import dev.rimeissner.motte.signing.GeneralStorage
import dev.rimeissner.motte.signing.PasswordAuthorization
import dev.rimeissner.motte.signing.SecureDeviceStorage
import kotlinx.coroutines.runBlocking
import motte.crypto.mocks.MockAuthorization
import motte.crypto.mocks.MockSecureStorage
import motte.crypto.mocks.MockStorage
import motte.crypto.mocks.PassThroughEncryptionEngine
import org.bouncycastle.crypto.InvalidCipherTextException
import org.junit.Assert.assertThrows
import utils.toHex
import java.security.SecureRandom
import kotlin.random.asKotlinRandom
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppSignerManagerTest {
    private lateinit var generalStorage: GeneralStorage
    private lateinit var appSigner: AppSignerManager

    @BeforeTest
    fun setup() {
        generalStorage = MockStorage()
        appSigner = AppSignerManager(
            generalStorage = generalStorage,
            secureStorage = MockSecureStorage(generalStorage),
            keyEngine = BouncyKeyEngine(),
            nodeFactory = BouncyHDNodeFactory(),
            mnemonicsFactory = BouncyMnemonicsFactory(ENGLISH_WORD_LIST),
            random = SecureRandom().asKotlinRandom()
        )
    }

    @Test
    fun addInitialAuthorization() = runBlocking {
        val auth = MockAuthorization()
        assertNull(generalStorage.getString(ID_ENCRYPTION_KEY_CHECKSUM))
        // Add initial authorization that will setup the app signer
        appSigner.addAuth(auth)
        assertNotNull(generalStorage.getString(ID_ENCRYPTION_KEY_CHECKSUM))
        assertEquals(
            checksum(auth.keyBytes!!).toHex(),
            generalStorage.getString(ID_ENCRYPTION_KEY_CHECKSUM)
        )
        val encryptedMnemonic = generalStorage.getString(ID_SIGNER_MNEMONIC)
        assertNotNull(encryptedMnemonic)
        // Mock storage appends the password with a :: separator to indicate encryption
        assertTrue(encryptedMnemonic.startsWith("${auth.keyBytes!!.toHex()}::"))
        val mnemonic = encryptedMnemonic.substringAfter("::")
        // Check that a 24 word mnemonic was generated
        // TODO verify this by passing in a mock mnemonicsFactory
        assertTrue(mnemonic.split(" ").size == 24)
    }

    @Test
    fun addMultipleAuthorizations() = runBlocking {
        val auth1 = PasswordAuthorization("pw1", generalStorage)
        assertNull(generalStorage.getString(ID_ENCRYPTION_KEY_CHECKSUM))
        // Add initial authorization that will setup the app signer
        appSigner.addAuth(auth1)
        assertNotNull(generalStorage.getString(ID_ENCRYPTION_KEY_CHECKSUM))
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
        assertNull(generalStorage.getString(ID_ENCRYPTION_KEY_CHECKSUM))
        appSigner.addAuth(auth1)
        assertNotNull(generalStorage.getString(ID_ENCRYPTION_KEY_CHECKSUM))
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
        private const val ID_SIGNER_MNEMONIC = "app_signer.string.signer_mnemonic::default"
        private const val ID_ENCRYPTION_KEY_CHECKSUM =
            "app_signer.string.encryption_key_checksum::default"
    }
}