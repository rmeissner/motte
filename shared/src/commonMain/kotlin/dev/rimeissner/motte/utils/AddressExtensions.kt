package dev.rimeissner.motte.utils

import dev.rimeissner.motte.encoding.keccak
import dev.rimeissner.motte.math.BigNumber
import dev.rimeissner.motte.solidity.Address

fun String.removeHexPrefix() = removePrefix("0x").removePrefix("0X")

fun String.toAddress(checkCorrectness: Boolean = true) =
    Address(BigNumber.from(removeHexPrefix(), 16)).also {
        if (checkCorrectness && it.toStringWithChecksum() != this)
            throw IllegalStateException("Invalid Checksum")
    }

fun String.toAddressOrNull() =
    try {
        toAddress()
    } catch (e: Exception) {
        null
    }

fun Address.toHexString() = "0x" + value.toString(16).padStart(40, '0')

fun Address.toStringWithChecksum() =
    toHexString().removeHexPrefix().run {
        val checksum = encodeToByteArray().keccak()
        foldIndexed(StringBuilder("0x")) { index, stringBuilder, char ->
            stringBuilder.append(
                when {
                    char in '0'..'9' -> char
                    checksum.hexCharValue(index) >= 8 -> char.uppercaseChar()
                    else -> char.lowercaseChar()
                }
            )
        }.toString()
    }

private fun ByteArray.hexCharValue(position: Int) = (get(position / 2).toInt() ushr (4 * ((position + 1) % 2))) and 0x0F