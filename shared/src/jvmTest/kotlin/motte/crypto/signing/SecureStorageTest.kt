package motte.crypto.signing

import dev.rimeissner.motte.encoding.keccak
import dev.rimeissner.motte.encryption.BouncyAESEngine
import dev.rimeissner.motte.encryption.CryptoData
import dev.rimeissner.motte.signing.GeneralStorage
import dev.rimeissner.motte.signing.SecureDeviceStorage
import motte.crypto.mocks.MockStorage
import motte.crypto.mocks.PassThroughEncryptionEngine
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SecureStorageTest {
    private val password = "test_password".toByteArray().keccak()
    private lateinit var generalStorage: GeneralStorage
    private lateinit var secureStorage: SecureDeviceStorage

    @Before
    fun setup() {
        generalStorage = MockStorage()
        secureStorage = SecureDeviceStorage(
            deviceEncryption = PassThroughEncryptionEngine(),
            generalStorage = generalStorage
        )
    }

    @Test
    fun storeAndRetrieve() {
        secureStorage.store("test_key", "test_data") { password }
        // Check that we don't store plain data
        assertNotEquals("test_data", generalStorage.getString("test_key"))
        val data = secureStorage.retrieve("test_key") { password }
        assertEquals("test_data", data)
    }
}