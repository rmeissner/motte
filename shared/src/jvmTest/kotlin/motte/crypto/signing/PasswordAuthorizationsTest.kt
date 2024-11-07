package motte.crypto.signing

import dev.rimeissner.motte.encoding.checksum
import dev.rimeissner.motte.encoding.keccak
import dev.rimeissner.motte.signing.GeneralStorage
import dev.rimeissner.motte.signing.PasswordAuthorization
import motte.crypto.mocks.MockStorage
import org.bouncycastle.crypto.InvalidCipherTextException
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class PasswordAuthorizationsTest {
    private val password = "test_password"
    private val key = "test_key".toByteArray().keccak()
    private lateinit var generalStorage: GeneralStorage
    private lateinit var authorization: PasswordAuthorization

    @Before
    fun setup() {
        generalStorage = MockStorage()
        authorization = PasswordAuthorization(password, generalStorage)
    }

    @Test
    fun setupAuthAndLoad() {
        authorization.setup(key)
        assertNotNull(generalStorage.getString(ID_ENCRYPTED_APP_SIGNER_KEY))
        assertNotEquals(String(key), generalStorage.getString(ID_ENCRYPTED_APP_SIGNER_KEY))
        assertNotNull(generalStorage.getString(ID_ENCRYPTED_CHECKSUM))
        assertNotEquals(String(key), generalStorage.getString(ID_ENCRYPTED_APP_SIGNER_KEY))

        assertArrayEquals(
            key,
            authorization.keyBytes(checksum(key))
        )
    }

    @Test
    fun setupAuthAndLoadNewInstance() {
        authorization.setup(key)

        val auth = PasswordAuthorization(password, generalStorage)
        assertArrayEquals(
            key,
            auth.keyBytes(checksum(key))
        )
    }

    @Test
    fun setupAuthAndLoadInvalidChecksum() {
        authorization.setup(key)
        val error = assertThrows(IllegalStateException::class.java) {
            authorization.keyBytes(byteArrayOf())
        }
        assertEquals("Unexpected key", error.message)
    }

    @Test
    fun loadNoSetup() {
        val error = assertThrows(IllegalStateException::class.java) {
            authorization.keyBytes(byteArrayOf())
        }
        assertEquals("No password set", error.message)
    }

    @Test
    fun setupAuthAndLoadNoAppId() {
        authorization.setup(key)
        generalStorage.remove(ID_ENCRYPTED_APP_SIGNER_KEY)
        val error = assertThrows(IllegalStateException::class.java) {
            authorization.keyBytes(byteArrayOf())
        }
        assertEquals("No password set", error.message)
    }

    @Test
    fun setupAuthAndLoadWrongPassword() {
        authorization.setup(key)
        generalStorage.remove(ID_ENCRYPTED_APP_SIGNER_KEY)
        // Use different auth with invalid password
        val auth = PasswordAuthorization("wrong", generalStorage)
        assertThrows(InvalidCipherTextException::class.java) {
            auth.keyBytes(byteArrayOf())
        }
    }

    companion object {
        private const val ID_ENCRYPTED_APP_SIGNER_KEY =
            "password_authorization.string.encrypted_app_signer_key"
        private const val ID_ENCRYPTED_CHECKSUM =
            "password_authorization.string.encrypted_checksum"
    }
}