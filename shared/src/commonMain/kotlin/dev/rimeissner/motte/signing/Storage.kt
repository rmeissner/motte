package dev.rimeissner.motte.signing

interface SecureStorage {
    fun retrieve(key: String, passwordProvider: () -> ByteArray): String
    fun store(key: String, data: String, passwordProvider: () -> ByteArray)
    fun hasKey(key: String): Boolean
}

interface GeneralStorage {
    fun hasKey(key: String): Boolean
    fun getString(key: String): String?
    fun putString(key: String, data: String)
    fun remove(key: String)
}