@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package dev.rimeissner.motte.math

actual class BigNumber {
    actual companion object {
        actual fun from(
            encoded: String,
            base: Int
        ): BigNumber {
            TODO("Not yet implemented")
        }

        actual fun from(array: ByteArray): BigNumber {
            TODO("Not yet implemented")
        }

        actual fun from(
            signum: Int,
            magnitude: ByteArray
        ): BigNumber {
            TODO("Not yet implemented")
        }

        actual fun from(number: Long): BigNumber {
            TODO("Not yet implemented")
        }

        actual fun random(bits: Int): BigNumber {
            TODO("Not yet implemented")
        }

        actual val ONE: BigNumber
            get() = TODO("Not yet implemented")
        actual val ZERO: BigNumber
            get() = TODO("Not yet implemented")

    }

    actual infix fun and(other: BigNumber): BigNumber {
        TODO("Not yet implemented")
    }

    actual infix fun shr(n: Int): BigNumber {
        TODO("Not yet implemented")
    }

    actual infix fun shl(n: Int): BigNumber {
        TODO("Not yet implemented")
    }

    actual operator fun rem(other: BigNumber): BigNumber {
        TODO("Not yet implemented")
    }

    actual operator fun plus(other: BigNumber): BigNumber {
        TODO("Not yet implemented")
    }

    actual operator fun minus(other: BigNumber): BigNumber {
        TODO("Not yet implemented")
    }

    actual operator fun times(other: BigNumber): BigNumber {
        TODO("Not yet implemented")
    }

    actual operator fun div(other: BigNumber): BigNumber {
        TODO("Not yet implemented")
    }

    actual operator fun compareTo(other: BigNumber): Int {
        TODO("Not yet implemented")
    }

    actual override fun equals(other: Any?): Boolean {
        TODO("Not yet implemented")
    }

    actual fun divRem(other: BigNumber): List<BigNumber> {
        TODO("Not yet implemented")
    }

    actual fun pow(exponent: Int): BigNumber {
        TODO("Not yet implemented")
    }

    actual fun modPow(
        exponent: BigNumber,
        m: BigNumber
    ): BigNumber {
        TODO("Not yet implemented")
    }

    actual fun abs(): BigNumber {
        TODO("Not yet implemented")
    }

    actual fun remInverse(other: BigNumber): BigNumber {
        TODO("Not yet implemented")
    }

    actual fun negate(): BigNumber {
        TODO("Not yet implemented")
    }

    actual fun bitLength(): Int {
        TODO("Not yet implemented")
    }

    actual fun signum(): Int {
        TODO("Not yet implemented")
    }

    actual fun testBit(bit: Int): Boolean {
        TODO("Not yet implemented")
    }

    actual fun toBytes(): ByteArray {
        TODO("Not yet implemented")
    }

    actual fun toString(radix: Int): String {
        TODO("Not yet implemented")
    }

    actual fun toInt(): Int {
        TODO("Not yet implemented")
    }

    actual fun toExactInt(): Int {
        TODO("Not yet implemented")
    }

}

actual class BigDecimal {
    actual companion object {
        actual fun from(encoded: String): BigDecimal {
            TODO("Not yet implemented")
        }

        actual fun from(number: Long): BigDecimal {
            TODO("Not yet implemented")
        }

        actual fun from(number: BigNumber): BigDecimal {
            TODO("Not yet implemented")
        }
    }

    actual fun add(other: BigDecimal): BigDecimal {
        TODO("Not yet implemented")
    }

    actual fun sub(other: BigDecimal): BigDecimal {
        TODO("Not yet implemented")
    }

    actual fun mul(other: BigDecimal): BigDecimal {
        TODO("Not yet implemented")
    }

    actual fun div(other: BigDecimal): BigDecimal {
        TODO("Not yet implemented")
    }

    actual fun pow(exponent: Int): BigDecimal {
        TODO("Not yet implemented")
    }

}