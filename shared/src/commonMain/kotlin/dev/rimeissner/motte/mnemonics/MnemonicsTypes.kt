package dev.rimeissner.motte.mnemonics

data class WordList(val separator: String, val words: List<String>)

interface MnemonicsFactory {
    fun generate(strength: Int = MIN_ENTROPY_BITS): String
    fun toSeed(mnemonic: String, password: String? = null): ByteArray
    fun validate(mnemonic: String): String
}

sealed class Bip39ValidationResult : IllegalArgumentException()
data class InvalidEntropy(val mnemonic: String, val entropyBits: Int) : Bip39ValidationResult()
data class InvalidChecksum(val mnemonic: String, val checksum: CharSequence, val generatedChecksum: CharSequence) : Bip39ValidationResult()
data class MnemonicNotInWordlist(val mnemonic: String) : Bip39ValidationResult()
data class EmptyMnemonic(val mnemonic: String) : Bip39ValidationResult()

const val MIN_ENTROPY_BITS = 128
const val MAX_ENTROPY_BITS = 256
const val ENTROPY_MULTIPLE = 32
const val WORD_LIST_SIZE = 2048