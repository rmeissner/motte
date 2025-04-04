package dev.rimeissner.motte.solidity

import kotlin.String as kString

fun findTupleEnd(tupleData: kString, index: Int): Int {
    var depth = 0
    var currentIndex = index
    while (currentIndex < tupleData.length) {
        val currentChar = tupleData[currentIndex]
        when (currentChar) {
            '(' -> {
                depth++
            }
            ')' -> {
                depth--
                if (depth <= 0) return currentIndex
            }
        }
        currentIndex++
    }
    throw IllegalStateException("Unclosed tuple")
}

private fun kString.parseTupleDecoder(): Tuple.Decoder {
    val itemDecoders = mutableListOf<TypeDecoder<Type>>()
    var index = 0
    while (index < length) {
        // Store current start index before checking for tuple bounds
        val startIndex = index
        if (get(index) == '(') {
            // Adjust index that use used to search parameter separators (,)
            index = findTupleEnd(this, startIndex)
        }
        // Search for parameter separator (,)
        val itemEnd = indexOf(',', index).let { if (it < 0) length else it }
        // Add TypeDecoder for parameter to list
        itemDecoders.add(substring(startIndex, itemEnd).trim().typeDecoder())
        index = itemEnd + 1
    }
    return Tuple.Decoder(itemDecoders)
}


// "(bytes[][4][], int256[1], bool[], string)[5][]"
fun kString.typeDecoder(): TypeDecoder<Type> {
    if (startsWith("(") && endsWith(")")) {
        // Create tuple data (without parentheses)
        val tupleData = substring(1, length - 1).trim()
        return tupleData.parseTupleDecoder()
    }
    val arrayOpening = indexOfLast { it == '[' }
    if (arrayOpening >= 0) {
        val itemDecoder = substring(0, arrayOpening).trim().typeDecoder()
        val arrayClosing = indexOfLast { it == ']' }
        return if (arrayClosing - arrayOpening == 1) {
            Vector.Decoder(itemDecoder)
        } else {
            Array.Decoder(itemDecoder, substring(arrayOpening + 1, arrayClosing).toInt())
        }
    }
    when {
        this == "address" -> {
            return Address.DECODER
        }
        this == "bool" -> {
            return Bool.DECODER
        }
        this == "string" -> {
            return String.DECODER
        }
        startsWith("bytes", ignoreCase = true) -> {
            if (length == 5) return Bytes.DECODER
            val width = substring(5).toInt()
            return StaticBytes.Decoder({ StaticBytes(it, width) }, width)
        }
        startsWith("int", ignoreCase = true) -> {
            val cardinal = if (length == 3) 256 else substring(3).toInt()
            return IntBase.Decoder { IntBase(it, cardinal) }
        }
        startsWith("uint", ignoreCase = true) -> {
            val cardinal = if (length == 4) 256 else substring(4).toInt()
            return UIntBase.Decoder { UIntBase(it, cardinal) }
        }
    }
    throw IllegalArgumentException("Unknown type: $this")
}