package dev.rimeissner.motte.keys

class KeyPair(val publicKey: ByteArray, val privateKey: ByteArray? = null)

class Signature(val r: ByteArray, val s: ByteArray, val v: Int)

class HDNode(val keyPair: KeyPair, val chainCode: ByteArray, val depth: Int, val index: Long, val parentFingerprint: ByteArray)

interface KeyEngine {
    fun sign(keyPair: KeyPair, messageHash: ByteArray): Signature
}

interface KeyPairFactory {
    fun fromPrivateKey(privateKey: ByteArray): KeyPair
}

interface HDNodeFactory {
    fun masterNode(seed: ByteArray): HDNode
    fun derive(node: HDNode, path: String): HDNode
}

const val BIP44_PATH_ETHEREUM = "m/44'/60'/0'/0"

const val MASTER_SECRET = "Bitcoin seed"