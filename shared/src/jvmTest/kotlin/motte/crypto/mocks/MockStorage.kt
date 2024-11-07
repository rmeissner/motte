package motte.crypto.mocks

import dev.rimeissner.motte.signing.GeneralStorage

class MockStorage: GeneralStorage {

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