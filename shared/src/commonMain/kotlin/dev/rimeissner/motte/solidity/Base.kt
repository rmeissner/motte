package dev.rimeissner.motte.solidity

import dev.rimeissner.motte.math.BigNumber
import utils.hexToByteArray
import utils.padEndMultiple
import utils.padStartMultiple
import utils.toHex
import kotlin.String as kString

class InvalidBitLengthException(message: kString?) : Exception(message) {
    companion object {
        val NOT_MULTIPLE_OF_EIGHT =
            InvalidBitLengthException("The bit length of the value is not a multiple of 8")
        val BIG_VALUE = InvalidBitLengthException("The bit length of the value is too big")
    }
}
const val BYTES_PAD = 32
const val PADDED_HEX_LENGTH = BYTES_PAD * 2

val dynamicTypes: List<kString> = listOf("bytes", "string")

interface Type {
    fun encode(): kString
    fun encodePacked(): kString
}

interface StaticType : Type

interface DynamicType : Type {
    data class Parts(val static: kString, val dynamic: kString)
}

abstract class Collection<out T : Type>(val items: List<T>) : Type {

    override fun encodePacked(): kString {
        if (items.any { isDynamic(it) }) throw IllegalArgumentException("Cannot encode dynamic items packed!")
        return encodeTuple(items)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Collection<*>) return false
        if (items.size != other.items.size) return false
        items.forEachIndexed { index, t ->
            if (other.items[index] != t) return false
        }
        return true
    }

    override fun toString(): kotlin.String {
        return "Collection[${items.joinToString(",")}]"
    }

    override fun hashCode(): Int {
        return items.hashCode()
    }

    abstract fun isDynamic(): Boolean
}

open class UIntBase(private val value: BigNumber, private val bitLength: Int) : StaticType {
    init {
        when {
            bitLength % 8 != 0 -> throw InvalidBitLengthException.NOT_MULTIPLE_OF_EIGHT
            value.bitLength() > bitLength -> throw InvalidBitLengthException.BIG_VALUE
            value.signum() == -1 -> throw IllegalArgumentException("UInt doesn't allow negative numbers")
        }
    }

    override fun encode(): kString =
        value.toString(16).padStartMultiple(PADDED_HEX_LENGTH, '0')

    override fun encodePacked(): kString =
        value.toString(16).padStart(bitLength / 8 * 2, '0')

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UIntBase) return false
        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): kotlin.String = "UInt$bitLength(${value.toString(10)})"

    open class Decoder<out T : UIntBase>(private val factory: (BigNumber) -> T) :
        TypeDecoder<T> {
        override fun isDynamic(): Boolean {
            return false
        }

        override fun decode(source: PartitionData): T {
            return factory.invoke(decodeUInt(source.consume()))
        }
    }

}

open class IntBase(private val value: BigNumber, private val bitLength: Int) : StaticType {
    init {
        if (bitLength % 8 != 0) throw InvalidBitLengthException.NOT_MULTIPLE_OF_EIGHT
        val min = BigNumber.from(2).pow(bitLength - 1).negate()
        val max = BigNumber.from(2).pow(bitLength - 1) - BigNumber.ONE

        if (value < min || value > max) throw IllegalArgumentException("Value is not within bit range [$min, $max]")
    }

    private fun encodeWithPadding(paddingLength: Int): kString {
        return if (value.signum() == -1) {
            val bits = value.toString(2).removePrefix("-").padStart(bitLength, '0')
            val x = bits.map { if (it == '0') '1' else '0' }.joinToString("")
            BigNumber.from(x, 2).plus(BigNumber.ONE).toString(16)
                .padStartMultiple(paddingLength, 'f')
        } else {
            value.toString(16).padStartMultiple(paddingLength, '0')
        }

    }

    override fun encode(): kString = encodeWithPadding(PADDED_HEX_LENGTH)

    override fun encodePacked(): kString = encodeWithPadding(bitLength / 8 * 2)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IntBase) return false

        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): kotlin.String = "Int$bitLength(${value.toString(10)})"

    open class Decoder<out T : IntBase>(private val factory: (BigNumber) -> T) :
        TypeDecoder<T> {
        override fun isDynamic(): Boolean {
            return false
        }

        override fun decode(source: PartitionData): T {
            return factory.invoke(decodeInt(source.consume()))
        }
    }
}

open class StaticBytes(val byteArray: ByteArray, nBytes: Int) : StaticType {
    init {
        if (byteArray.size > nBytes) throw IllegalArgumentException("Byte array has ${byteArray.size} bytes. It should have no more than $nBytes bytes.")
    }

    override fun encode(): kString {
        return byteArray.toHex().padEnd(PADDED_HEX_LENGTH, '0')
    }

    override fun encodePacked(): kString {
        return byteArray.toHex()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StaticBytes) return false

        return byteArray.contentEquals(other.byteArray)
    }

    override fun hashCode(): Int {
        return byteArray.hashCode()
    }

    open class Decoder<out T : StaticBytes>(
        private val factory: (ByteArray) -> T,
        val nBytes: Int
    ) : TypeDecoder<T> {
        override fun isDynamic(): Boolean {
            return false
        }

        override fun decode(source: PartitionData): T {
            return factory.invoke(decodeStaticBytes(source.consume(), nBytes))
        }
    }
}

class PartitionData(data: kString, private val offset: Int = 0) {
    private val cleanData = data.removePrefix("0x")
    private var index: Int = 0

    fun consume(): kString {
        return cleanData.substring(offset + index * 64, offset + (index + 1) * 64).apply {
            index++
        }
    }

    fun reset() {
        index = 0
    }

    fun subData() = PartitionData(cleanData, offset + index * 64)

    fun subData(bytesOffset: Int) = PartitionData(cleanData, offset + bytesOffset * 2)

    companion object {
        fun of(data: kString): PartitionData {
            return PartitionData(data)
        }
    }
}

interface TypeDecoder<out T : Type> {

    fun isDynamic(): Boolean

    fun decode(source: PartitionData): T
}

private fun <T : Type> decodeList(
    source: PartitionData,
    capacity: Int,
    itemDecoder: TypeDecoder<T>
): List<T> {
    return (0 until capacity).map {
        if (itemDecoder.isDynamic()) {
            // Get offset
            val offset = BigNumber.from(source.consume(), 16).toExactInt()
            // Decode dynamic data at offset
            itemDecoder.decode(source.subData(offset))
        } else {
            // If static type then the source might be consumed by the item decoder
            itemDecoder.decode(source)
        }
    }
}

open class Array<out T : Type>(items: List<T>, val capacity: Int) :
    Collection<T>(checkCapacity(items, capacity)) {
    override fun encode(): kString {
        if (items.size != capacity) {
            throw IllegalStateException("Capacity mismatch!")
        }
        // Encode the fixed array as a tuple where all parts are of the same type
        return encodeTuple(items)
    }

    override fun isDynamic(): Boolean {
        if (capacity == 0) {
            return false
        }
        return items.any { isDynamic(it) }
    }

    class Decoder<out T : Type>(private val itemDecoder: TypeDecoder<T>, private val capacity: Int) :
        TypeDecoder<Array<T>> {
        override fun isDynamic(): Boolean {
            return itemDecoder.isDynamic()
        }

        override fun decode(source: PartitionData): Array<T> {
            return Array(decodeList(source, capacity, itemDecoder), capacity)
        }
    }

    companion object {
        private fun <T : Type> checkCapacity(items: List<T>, capacity: Int): List<T> {
            if (items.size != capacity) {
                throw IllegalStateException("Array is of wrong capacity!")
            }
            return ArrayList(items)
        }

    }
}

class Vector<out T : Type>(items: List<T>) : Collection<T>(items), DynamicType {

    override fun encode(): kString {
        val parts = encodeParts()
        return parts.static + parts.dynamic
    }

    private fun encodeParts(): DynamicType.Parts {
        val length = items.size.toString(16).padStart(PADDED_HEX_LENGTH, '0')
        // Encode the dynamic array as the length and a tuple where all parts are of the same type
        return DynamicType.Parts(length, encodeTuple(items))
    }

    override fun isDynamic(): Boolean {
        return true
    }

    class Decoder<out T : Type>(private val itemDecoder: TypeDecoder<T>) :
        TypeDecoder<Vector<T>> {
        override fun isDynamic(): Boolean {
            return true
        }

        override fun decode(source: PartitionData): Vector<T> {
            val capacity = decodeUInt(source.consume()).toInt()
            return Vector(decodeList(source.subData(), capacity, itemDecoder))
        }
    }
}

class Tuple(items: List<Type>) : Collection<Type>(items) {

    override fun encode() = encodeTuple(items)

    override fun isDynamic() = items.any { isDynamic(it) }

    class Decoder(private val itemDecoders: List<TypeDecoder<Type>>) :
        TypeDecoder<Tuple> {
        override fun isDynamic() = itemDecoders.any { it.isDynamic() }

        override fun toString(): kotlin.String =
            "Tuple(${itemDecoders.joinToString(",")})"

        override fun decode(source: PartitionData): Tuple {
            return Tuple(itemDecoders.map {
                if (it.isDynamic()) {
                    // Get offset
                    val offset = BigNumber.from(source.consume(), 16).toExactInt()
                    // Decode dynamic data at offset
                    it.decode(source.subData(offset))
                } else {
                    it.decode(source)
                }
            })
        }
    }
}

@Suppress("unused")
fun encodeFunctionArguments(vararg args: Type): kString {
    return encodeTuple(args.toList())
}

private fun encodeTuple(parts: List<Type>): kString {
    val encodedParts = ArrayList<Pair<kString, Boolean>>()

    var sizeOfStaticBlock = 0
    parts.forEach {
        val encoded = it.encode()
        if (isDynamic(it)) {
            encodedParts += Pair(encoded, true)
            // Add length of an address to static block size
            sizeOfStaticBlock += BYTES_PAD
        } else {
            encodedParts += Pair(encoded, false)
            sizeOfStaticBlock += encoded.length / 2
        }
    }

    val staticArgsBuilder = StringBuilder()
    val dynamicArgsBuilder = StringBuilder()
    encodedParts.forEach { (encoded, dynamic) ->
        if (dynamic) {
            val location = sizeOfStaticBlock + dynamicArgsBuilder.length / 2
            staticArgsBuilder.append(location.toString(16).padStart(PADDED_HEX_LENGTH, '0'))
            dynamicArgsBuilder.append(encoded)
        } else {
            staticArgsBuilder.append(encoded)
        }
    }

    return staticArgsBuilder.toString() + dynamicArgsBuilder.toString()
}

private fun isDynamic(type: Type): Boolean {
    if (type is DynamicType) {
        return true
    }
    return (type as? Collection<*>)?.isDynamic() ?: false || (type is Vector<*>)
}

private fun decodeUInt(data: kString): BigNumber {
    return BigNumber.from(data, 16)
}

private fun decodeBool(data: kString): Boolean {
    val value = BigNumber.from(data)
    return when (value) {
        BigNumber.ZERO -> false
        BigNumber.ONE -> true
        else -> throw IllegalArgumentException("${value.toString(10)} is not a valid boolean representation. It should either be 0 (false) or 1 (true)")
    }
}

private fun decodeInt(data: kString): BigNumber {
    val value = BigNumber.from(data, 16)
    if (data.startsWith("8") ||
        data.startsWith("9") ||
        data.startsWith("A", true) ||
        data.startsWith("B", true) ||
        data.startsWith("C", true) ||
        data.startsWith("D", true) ||
        data.startsWith("E", true) ||
        data.startsWith("F", true)
    ) {
        val x = value.toString(2).map { if (it == '0') '1' else '0' }.joinToString("")
        return BigNumber.from(x, 2).plus(BigNumber.ONE).negate()
    }
    return value
}

private fun decodeStaticBytes(data: kString, nBytes: kotlin.Int): ByteArray {
    return data.substring(0, nBytes * 2).hexToByteArray()
}

private fun decodeBytes(source: PartitionData): ByteArray {
    val contentSize = BigNumber.from(source.consume(), 16).toExactInt() * 2
    if (contentSize == 0) return ByteArray(0)
    val sb = StringBuilder()
    while (sb.length < contentSize) {
        sb.append(source.consume())
    }
    return sb.substring(0, contentSize).hexToByteArray()
}

private fun decodeString(source: PartitionData) =
    decodeBytes(source).decodeToString()

data class Address(
    val value: BigNumber
) : UIntBase(value, 160) {
    companion object {
        val DECODER: Decoder<Address> =
            Decoder { Address(it) }
    }
}

data class Bool(
    val value: Boolean
) : UIntBase(if (value) BigNumber.ONE else BigNumber.ZERO, 8) {
    class Decoder : TypeDecoder<Bool> {
        override fun isDynamic(): Boolean = false
        override fun decode(source: PartitionData): Bool =
            Bool(decodeBool(source.consume()))
    }

    companion object {
        val DECODER: Decoder = Decoder()
    }
}

open class Bytes(
    val items: ByteArray
) : DynamicType {
    init {
        if (BigNumber.from(items.size.toString(10)) > BigNumber.from(2)
                .pow(256)
        ) throw Exception()
    }

    override fun encode(): kString {
        val parts = encodeParts()
        return parts.static + parts.dynamic
    }

    private fun encodeParts(): DynamicType.Parts {
        val length = items.size.toString(16).padStart(64, '0')
        val contents = items.toHex().padEndMultiple(64, '0')
        return DynamicType.Parts(length, contents)
    }

    override fun encodePacked(): kString = items.toHex()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is Bytes) return false
        return items.contentEquals(other.items)
    }

    override fun hashCode(): Int {
        return items.contentHashCode()
    }

    class Decoder : TypeDecoder<Bytes> {
        override fun isDynamic(): Boolean = true
        override fun decode(source: PartitionData): Bytes =
            Bytes(decodeBytes(source))
    }

    companion object {
        val DECODER: Decoder = Decoder()
    }
}

data class String(
    val value: kString
) : Bytes(value.encodeToByteArray()) {
    class Decoder : TypeDecoder<String> {
        override fun isDynamic(): Boolean = true
        override fun decode(source: PartitionData): String =
            String(decodeString(source))
    }

    companion object {
        val DECODER: Decoder = Decoder()
    }
}