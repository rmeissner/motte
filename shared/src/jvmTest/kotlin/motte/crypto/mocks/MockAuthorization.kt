package motte.crypto.mocks

import dev.rimeissner.motte.signing.Authorization

class MockAuthorization: Authorization {
    var keyBytes: ByteArray? = null

    override fun keyBytes(keyChecksum: ByteArray): ByteArray {
        return keyBytes ?: throw IllegalStateException("No key set")
    }

    override fun setup(key: ByteArray) {
        keyBytes = key
    }

    override fun verify(): Boolean {
        TODO("Not yet implemented")
    }
}