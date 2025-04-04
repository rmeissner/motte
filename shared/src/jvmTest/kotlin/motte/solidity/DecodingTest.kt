package motte.solidity

import dev.rimeissner.motte.math.BigNumber
import dev.rimeissner.motte.solidity.Array
import dev.rimeissner.motte.solidity.Bool
import dev.rimeissner.motte.solidity.Bytes
import dev.rimeissner.motte.solidity.IntBase
import dev.rimeissner.motte.solidity.PartitionData
import dev.rimeissner.motte.solidity.StaticBytes
import dev.rimeissner.motte.solidity.String
import dev.rimeissner.motte.solidity.Tuple
import dev.rimeissner.motte.solidity.UIntBase
import dev.rimeissner.motte.solidity.Vector
import dev.rimeissner.motte.solidity.typeDecoder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DecodingTest {
    @Test
    fun buildComplexType() {
        "(bytes[][4][], int256[1], bool[], string)[5][]".typeDecoder()
    }

    @Test
    fun decodeBoolean() {
        assertTrue(
            ("bool".typeDecoder().decode(PartitionData.of(Bool(true).encode())) as Bool).value
        )
        assertFalse(
            ("bool".typeDecoder().decode(PartitionData.of(Bool(false).encode())) as Bool).value
        )
    }

    @Test
    fun decodeBooleanVector() {
        val data = Vector(
            listOf(
                Bool(true),
                Bool(true),
                Bool(false),
                Bool(true)
            )
        )
        assertEquals(
            data,
            "bool[]".typeDecoder().decode(PartitionData.of(data.encode()))
        )
    }

    @Test
    fun decodeBooleanVectorTuple() {
        val data = Tuple(
            listOf(
                Vector(
                    listOf(
                        Bool(true),
                        Bool(true),
                        Bool(false),
                        Bool(true)
                    )
                )
            )
        )
        assertEquals(
            data,
            "(bool[])".typeDecoder().decode(PartitionData.of(data.encode()))
        )
    }

    @Test
    fun decodeIntArrayBooleanVectorTuple() {
        val data = Tuple(
            listOf(
                Array(listOf(IntBase(BigNumber.from("123", 16), 256)), 1),
                Vector(
                    listOf(
                        Bool(true),
                        Bool(true),
                        Bool(false),
                        Bool(true)
                    )
                )
            )
        )
        assertEquals(
            "0000000000000000000000000000000000000000000000000000000000000123000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001",
            data.encode()
        )
        assertEquals(
            data,
            "(int256[1],bool[])".typeDecoder().decode(PartitionData.of(data.encode()))
        )
    }

    @Test
    fun decodeIntArray() {
        val data = Array(listOf(IntBase(BigNumber.from("123", 16), 256)), 1)
        assertEquals(
            data,
            "int256[1]".typeDecoder().decode(PartitionData.of(data.encode()))
        )
    }

    @Test
    fun decodeIntArrayTuple() {
        val data = Tuple(listOf(Array(listOf(IntBase(BigNumber.from("123", 16), 256)), 1)))
        assertEquals(
            data,
            "(int256[1])".typeDecoder().decode(PartitionData.of(data.encode()))
        )
    }

    @Test
    fun decodeTuple() {
        val data =
            Tuple(
                listOf(
                    IntBase(BigNumber.from("123", 16), 256),
                    UIntBase(BigNumber.from("245", 16), 256),
                    Bool(false)
                )
            )
        assertEquals(
            data,
            "(int256,uint, bool)".typeDecoder().decode(PartitionData.of(data.encode()))
        )
    }

    @Test
    fun decodeNestedTuple() {
        val data =
            Tuple(
                listOf(
                    Tuple(
                        listOf(
                            IntBase(BigNumber.from("123", 16), 256),
                            String("Hello")
                        )
                    ),
                    Bool(true)
                )
            )
        assertEquals(
            "0000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000001230000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000000000000548656c6c6f000000000000000000000000000000000000000000000000000000",
            data.encode()
        )
        assertEquals(
            data,
            "((int256,string),bool)".typeDecoder().decode(PartitionData.of(data.encode()))
        )
    }

    @Test
    fun decodeDoubleNestedTuple() {
        val data =
            Tuple(
                listOf(
                    Tuple(
                        listOf(
                            IntBase(BigNumber.from("123", 16), 256),
                            Tuple(
                                listOf(
                                    UIntBase(BigNumber.from("456", 16), 256),
                                    String("Hello")
                                )
                            )
                        )
                    ),
                    Bool(true)
                )
            )
        assertEquals(
            "000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000123000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000004560000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000000000000548656c6c6f000000000000000000000000000000000000000000000000000000",
            data.encode()
        )
        assertEquals(
            data,
            "((int256,(uint256,string)),bool)".typeDecoder().decode(PartitionData.of(data.encode()))
        )
    }

    @Test
    fun decodeDoubleMultiNestedTuple() {
        val data =
            Tuple(
                listOf(
                    Tuple(
                        listOf(
                            IntBase(BigNumber.from("123", 16), 256),
                            Tuple(
                                listOf(
                                    UIntBase(BigNumber.from("456", 16), 256),
                                    String("Hello")
                                )
                            ),
                            Tuple(
                                listOf(
                                    Bool(false)
                                )
                            )
                        )
                    ),
                    Bool(true)
                )
            )
        assertEquals(
            "0000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000001230000000000000000000000000000000000000000000000000000000000000060000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000004560000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000000000000548656c6c6f000000000000000000000000000000000000000000000000000000",
            data.encode()
        )
        assertEquals(
            data,
            "((int256,(uint256,string),(bool)),bool)".typeDecoder()
                .decode(PartitionData.of(data.encode()))
        )
    }

    @Test
    fun decodeTupleVector() {
        val data = Vector(
            listOf(
                Tuple(
                    listOf(
                        IntBase(BigNumber.from("123", 16), 256),
                        UIntBase(BigNumber.from("245", 16), 256),
                        Bool(false)
                    )
                )
            )
        )
        assertEquals(
            data,
            "(int256,uint, bool)[]".typeDecoder().decode(PartitionData.of(data.encode()))
        )
    }

    @Test
    fun encodeDecodeTest() {
        val typeDef = "(bytes[][4][], int256[1], bool[], string)[]"
        val data =
            Vector(
                listOf(
                    Tuple(
                        listOf(
                            Vector(
                                listOf(
                                    Array(
                                        listOf(
                                            Vector(
                                                listOf(
                                                    Bytes("Entry 1 1 1".toByteArray()),
                                                    Bytes("Entry 1 2 1".toByteArray()),
                                                )
                                            ),
                                            Vector(
                                                listOf(
                                                    Bytes("Entry 1 2 1".toByteArray()),
                                                    Bytes("Entry 1 2 1".toByteArray()),
                                                )
                                            ),
                                            Vector(
                                                listOf(
                                                    Bytes("Entry 1 3 1".toByteArray()),
                                                    Bytes("Entry 1 3 2".toByteArray()),
                                                    Bytes("Entry 1 3 3".toByteArray()),
                                                )
                                            ),
                                            Vector(
                                                listOf(
                                                    Bytes("Entry 1 4".toByteArray()),
                                                )
                                            ),
                                        ), 4
                                    ),
                                    Array(
                                        listOf(
                                            Vector(
                                                listOf(
                                                    Bytes("Entry 2 1 1".toByteArray()),
                                                )
                                            ),
                                            Vector(
                                                listOf(
                                                    Bytes("Entry 2 2 1".toByteArray()),
                                                )
                                            ),
                                            Vector(
                                                listOf(
                                                    Bytes("Entry 2 3 1".toByteArray()),
                                                    Bytes("Entry 2 3 2".toByteArray()),
                                                    Bytes("Entry 2 3 3".toByteArray()),
                                                )
                                            ),
                                            Vector(listOf()),
                                        ), 4
                                    )
                                )
                            ),
                            Array(listOf(IntBase(BigNumber.from("123", 10), 256)), 1),
                            Vector(
                                listOf(
                                    Bool(true),
                                    Bool(true),
                                    Bool(false),
                                    Bool(true)
                                )
                            ),
                            String("Hello, world!")
                        )
                    )
                )
            )
        val decoder = typeDef.typeDecoder()
        val encode = data.encode()
        val decoded = decoder.decode(PartitionData.of(encode))
        assertTrue(decoded is Vector<*>)
        assertEquals(data.items.size, decoded.items.size)
        data.items.forEachIndexed { vectorIndex, tuple ->
            val decodedTuple = decoded.items[vectorIndex]
            assertTrue(decodedTuple is Tuple)
            tuple.items.forEachIndexed { tupleIndex, item ->
                assertEquals(item, decodedTuple.items[tupleIndex])
            }
        }
    }

    @Test
    fun solidityDocsDynamicTypesDecoder() {
        val decoder = "(uint256,uint32[],bytes10,bytes)".typeDecoder()
        val encode =
            "00000000000000000000000000000000000000000000000000000000000001230000000000000000000000000000000000000000000000000000000000000080313233343536373839300000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000e0000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000004560000000000000000000000000000000000000000000000000000000000000789000000000000000000000000000000000000000000000000000000000000000d48656c6c6f2c20776f726c642100000000000000000000000000000000000000"
        val decoded = decoder.decode(PartitionData.of(encode))
        // 0x123, [0x456, 0x789], "1234567890", "Hello, world!"
        val expected = Tuple(
            listOf(
                UIntBase(BigNumber.from("123", 16), 256),
                Vector(
                    listOf(
                        UIntBase(BigNumber.from("456", 16), 32),
                        UIntBase(BigNumber.from("789", 16), 32)

                    )
                ),
                StaticBytes("1234567890".toByteArray(), 10),
                Bytes("Hello, world!".toByteArray())
            )
        )
        assertTrue(decoded is Tuple)
        assertEquals(expected.items.size, decoded.items.size)
        expected.items.forEachIndexed { index, item ->
            assertEquals(item, decoded.items[index])
        }
    }
}