@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package dev.rimeissner.motte.math

expect class BigNumber {

    companion object {
        fun from(encoded: String, base: Int = 10): BigNumber
        fun from(array: ByteArray): BigNumber
        fun from(signum: Int, magnitude: ByteArray): BigNumber
        fun from(number: Long): BigNumber
        fun random(bits: Int): BigNumber

        val ONE: BigNumber
        val ZERO: BigNumber
    }

    infix fun and(other: BigNumber): BigNumber
    infix fun shr(n: Int): BigNumber
    infix fun shl(n: Int): BigNumber
    operator fun rem(other: BigNumber): BigNumber
    operator fun plus(other: BigNumber): BigNumber
    operator fun minus(other: BigNumber): BigNumber
    operator fun times(other: BigNumber): BigNumber
    operator fun div(other: BigNumber): BigNumber
    operator fun compareTo(other: BigNumber): Int
    override operator fun equals(other: Any?): Boolean
    fun divRem(other: BigNumber): List<BigNumber>
    fun pow(exponent: Int): BigNumber
    fun modPow(exponent: BigNumber, m: BigNumber): BigNumber
    fun abs(): BigNumber
    fun remInverse(other: BigNumber): BigNumber
    fun negate(): BigNumber
    fun bitLength(): Int
    fun signum(): Int
    fun testBit(bit: Int): Boolean
    fun toBytes(): ByteArray
    fun toString(radix: Int): String
    fun toInt(): Int
    fun toExactInt(): Int
}

expect class BigDecimal {

    companion object {
        fun from(encoded: String): BigDecimal
        fun from(number: Long): BigDecimal
        fun from(number: BigNumber): BigDecimal
    }

    operator fun compareTo(other: BigDecimal): Int

    fun add(other: BigDecimal): BigDecimal
    fun sub(other: BigDecimal): BigDecimal
    fun mul(other: BigDecimal): BigDecimal
    fun div(other: BigDecimal): BigDecimal
    fun pow(exponent: Int): BigDecimal

    fun toFloat(): Float
    fun format(format: String): String
}