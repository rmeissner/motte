@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package dev.rimeissner.motte.math

import java.math.BigInteger
import java.security.SecureRandom
import java.math.BigDecimal as JavaBigDecimal

actual class BigNumber(private val num: BigInteger) {

    actual infix fun and(other: BigNumber): BigNumber =
        BigNumber(num.and(other.num))

    actual infix fun shr(n: Int): BigNumber =
        BigNumber(num.shr(n))

    actual infix fun shl(n: Int): BigNumber =
        BigNumber(num.shl(n))

    actual operator fun plus(other: BigNumber) =
        BigNumber(num.add(other.num))

    actual operator fun minus(other: BigNumber) =
        BigNumber(num.subtract(other.num))

    actual operator fun times(other: BigNumber) =
        BigNumber(num.multiply(other.num))

    actual operator fun div(other: BigNumber) =
        BigNumber(num.divide(other.num))

    actual operator fun rem(other: BigNumber) =
        BigNumber(num.mod(other.num))

    actual fun divRem(other: BigNumber) =
        num.divideAndRemainder(other.num).map { BigNumber(it) }

    actual fun pow(exponent: Int) =
        BigNumber(num.pow(exponent))

    actual fun modPow(exponent: BigNumber, m: BigNumber) =
        BigNumber(num.modPow(exponent.num, m.num))

    actual fun remInverse(other: BigNumber) =
        BigNumber(num.modInverse(other.num))

    actual fun toBytes(): ByteArray =
        num.toByteArray()

    actual fun negate(): BigNumber =
        BigNumber(num.negate())

    actual operator fun compareTo(other: BigNumber) =
        num.compareTo(other.num)

    actual override operator fun equals(other: Any?) =
        if (other is BigNumber) num == other.num
        else super.equals(other)

    actual fun bitLength() =
        num.bitLength()

    actual fun signum() =
        num.signum()

    actual fun testBit(bit: Int) =
        num.testBit(bit)

    actual fun abs() =
        BigNumber(num.abs())

    actual fun toString(radix: Int): String =
        num.toString(radix)

    actual fun toInt(): Int =
        num.toInt()

    actual fun toExactInt(): Int =
        JavaBigDecimal(num).intValueExact()

    fun decimal(): BigDecimal =
        BigDecimal(JavaBigDecimal(num))

    actual companion object {
        actual fun from(encoded: String, base: Int): BigNumber =
            BigNumber(BigInteger(encoded, base))

        actual fun from(number: Long): BigNumber =
            BigNumber(BigInteger.valueOf(number))

        actual fun from(array: ByteArray): BigNumber =
            BigNumber(BigInteger(array))

        actual fun from(signum: Int, magnitude: ByteArray): BigNumber =
            BigNumber(BigInteger(signum, magnitude))

        actual fun random(bits: Int): BigNumber =
            BigNumber(BigInteger(bits, SecureRandom()))

        actual val ZERO = BigNumber(BigInteger.ZERO)
        actual val ONE = BigNumber(BigInteger.ONE)
    }
}

actual class BigDecimal(private val num: JavaBigDecimal) {

    actual fun add(other: BigDecimal) =
        BigDecimal(num.add(other.num))

    actual fun sub(other: BigDecimal) =
        BigDecimal(num.subtract(other.num))

    actual fun mul(other: BigDecimal) =
        BigDecimal(num.multiply(other.num))

    actual fun div(other: BigDecimal) =
        BigDecimal(num.divide(other.num))

    actual fun pow(exponent: Int) =
        BigDecimal(num.pow(exponent))

    actual companion object {
        actual fun from(encoded: String): BigDecimal =
            BigDecimal(JavaBigDecimal(encoded))

        actual fun from(number: Long): BigDecimal =
            BigDecimal(JavaBigDecimal.valueOf(number))

        actual fun from(number: BigNumber): BigDecimal =
            number.decimal()
    }
}