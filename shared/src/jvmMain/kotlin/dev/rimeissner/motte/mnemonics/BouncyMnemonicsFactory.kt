package dev.rimeissner.motte.mnemonics

import dev.rimeissner.motte.utils.getIndexes
import dev.rimeissner.motte.utils.toBinaryString
import dev.rimeissner.motte.utils.words
import okio.ByteString.Companion.toByteString
import org.bouncycastle.jcajce.provider.symmetric.PBEPBKDF2
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.text.Normalizer
import javax.crypto.SecretKey
import javax.crypto.spec.PBEKeySpec
import kotlin.random.Random
import kotlin.random.asKotlinRandom

class BouncyMnemonicsFactory(
    private val wordList: WordList,
    private val random: Random = SecureRandom().asKotlinRandom()
) : MnemonicsFactory {
    private class Hasher : PBEPBKDF2.PBKDF2withSHA512() {
        fun generateSecret(keySpec: KeySpec): SecretKey {
            return engineGenerateSecret(keySpec)
        }
    }

    private fun pbkdf2(
        password: CharArray,
        salt: ByteArray
    ): ByteArray {
        val spec = PBEKeySpec(password, salt, 2048, 64 * 8)
        val skf = Hasher()
        return skf.generateSecret(spec).encoded
    }

    override fun toSeed(mnemonic: String, password: String?): ByteArray {
        val mnemonicBuffer = normalize(mnemonic).toCharArray()
        val saltBuffer = salt(normalize(password ?: "")).toByteArray()
        return pbkdf2(mnemonicBuffer, saltBuffer)
    }

    private fun normalize(phrase: String) = Normalizer.normalize(phrase, Normalizer.Form.NFKD)

    private fun salt(password: String?) = "mnemonic" + (password ?: "")

    override fun generate(strength: Int): String {
        if (strength < MIN_ENTROPY_BITS || strength > MAX_ENTROPY_BITS || strength % ENTROPY_MULTIPLE != 0) {
            throw IllegalArgumentException("Entropy length should be between $MIN_ENTROPY_BITS and $MAX_ENTROPY_BITS and be a multiple of $ENTROPY_MULTIPLE")
        }

        if (wordList.words.size != WORD_LIST_SIZE) throw IllegalArgumentException("Wordlist needs to have $WORD_LIST_SIZE (it has ${wordList.words.size})")

        val bytes = ByteArray(strength / 8)
        random.nextBytes(bytes)

        val sha256 = bytes.toByteString().sha256().toByteArray()
        val checksumLength = strength / 32

        val checksum = sha256.toBinaryString().subSequence(0, checksumLength)

        val concatenated = bytes.toBinaryString() + checksum

        val wordIndexes =
            (concatenated.indices step 11).map { concatenated.subSequence(it, it + 11) }
                .map { Integer.parseInt(it.toString(), 2) }.toList()

        return wordIndexes.joinToString(wordList.separator) { wordList.words[it] }
    }

    override fun validate(mnemonic: String): String {
        val words = mnemonic.words()
        if (words.isEmpty() || words[0].isEmpty()) throw EmptyMnemonic(mnemonic)

        val checksumNBits = (words.size * 11) / (ENTROPY_MULTIPLE + 1)
        val entropyNBits = checksumNBits * 32
        if (entropyNBits < MIN_ENTROPY_BITS || entropyNBits > MAX_ENTROPY_BITS) {
            throw InvalidEntropy(mnemonic, entropyNBits)
        }

        val binaryIndexes = try {
            wordList.words.getIndexes(words)
                .joinToString("") { Integer.toBinaryString(it).padStart(11, '0') }
        } catch (e: IllegalArgumentException) {
            throw MnemonicNotInWordlist(mnemonic)
        }

        val checksum = binaryIndexes.subSequence(entropyNBits, binaryIndexes.length)
        val originalEntropy = binaryIndexes.subSequence(0, binaryIndexes.length - checksumNBits)
        val originalBytes = (originalEntropy.indices step 8).map {
            (Integer.valueOf(
                (originalEntropy.subSequence(it, it + 8).toString()),
                2
            ) and 0xFF).toByte()
        }.toByteArray()

        val sha256 = originalBytes.toByteString().sha256().toByteArray()
        val generatedChecksum = sha256.toBinaryString().subSequence(0, checksumNBits)
        if (checksum != generatedChecksum) throw InvalidChecksum(
            mnemonic,
            checksum,
            generatedChecksum
        )

        return mnemonic
    }
}