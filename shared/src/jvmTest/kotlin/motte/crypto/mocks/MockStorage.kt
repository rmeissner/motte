package motte.crypto.mocks

import dev.rimeissner.motte.signing.GeneralStorage
import dev.rimeissner.motte.signing.SecureStorage
import utils.toHex

class MockStorage : GeneralStorage {

    private val map = HashMap<String, String>()

    override fun hasKey(key: String): Boolean =
        map.containsKey(key)

    override fun getString(key: String): String? =
        map[key]

    override fun putString(key: String, data: String) {
        map[key] = data
    }

    override fun remove(key: String) {
        map.remove(key)
    }

}

class MockSecureStorage(
    val internalStorage: GeneralStorage = MockStorage()
) : SecureStorage {
    override fun retrieve(
        key: String,
        passwordProvider: () -> ByteArray
    ): String {
        val pw = passwordProvider().toHex()
        val data =  internalStorage.getString(key)
            ?: throw IllegalArgumentException("No data for key $key")
        val split = data.split("::")
        if (split[0] != pw) {
            throw IllegalArgumentException("Invalid password")
        }
        return split[1]
    }

    override fun store(
        key: String,
        data: String,
        passwordProvider: () -> ByteArray
    ) {
        val pw = passwordProvider().toHex()
        internalStorage.putString(key, "$pw::$data")
    }

    override fun hasKey(key: String): Boolean {
        return internalStorage.hasKey(key)
    }

}