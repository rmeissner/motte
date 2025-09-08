package dev.rimeissner.motte.signing

/**
 * Authorization are used to store key bytes and allow retrieval at an later point.
 * Examples for Authorizations are PasswordAuthorization and BiometricsAuthorization.
 * These Authorizations can be used with the AppSigner to gain access to the stored signer material.
 */
interface Authorization {
    fun keyBytes(keyChecksum: ByteArray): ByteArray
    fun setup(key: ByteArray)
    fun verify(): Boolean
}