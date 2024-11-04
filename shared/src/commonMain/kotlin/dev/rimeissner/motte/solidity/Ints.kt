package dev.rimeissner.motte.solidity

import dev.rimeissner.motte.math.BigNumber


data class Int8(
    val value: BigNumber
) : IntBase(value, 8) {
    companion object {
        val DECODER: Decoder<Int8> = Decoder {
            Int8(it)
        }
    }
}

data class Int16(
    val value: BigNumber
) : IntBase(value, 16) {
    companion object {
        val DECODER: Decoder<Int16> = Decoder {
            Int16(it)
        }
    }
}

data class Int24(
    val value: BigNumber
) : IntBase(value, 24) {
    companion object {
        val DECODER: Decoder<Int24> = Decoder {
            Int24(it)
        }
    }
}

data class Int32(
    val value: BigNumber
) : IntBase(value, 32) {
    companion object {
        val DECODER: Decoder<Int32> = Decoder {
            Int32(it)
        }
    }
}

data class Int40(
    val value: BigNumber
) : IntBase(value, 40) {
    companion object {
        val DECODER: Decoder<Int40> = Decoder {
            Int40(it)
        }
    }
}

data class Int48(
    val value: BigNumber
) : IntBase(value, 48) {
    companion object {
        val DECODER: Decoder<Int48> = Decoder {
            Int48(it)
        }
    }
}

data class Int56(
    val value: BigNumber
) : IntBase(value, 56) {
    companion object {
        val DECODER: Decoder<Int56> = Decoder {
            Int56(it)
        }
    }
}

data class Int64(
    val value: BigNumber
) : IntBase(value, 64) {
    companion object {
        val DECODER: Decoder<Int64> = Decoder {
            Int64(it)
        }
    }
}

data class Int72(
    val value: BigNumber
) : IntBase(value, 72) {
    companion object {
        val DECODER: Decoder<Int72> = Decoder {
            Int72(it)
        }
    }
}

data class Int80(
    val value: BigNumber
) : IntBase(value, 80) {
    companion object {
        val DECODER: Decoder<Int80> = Decoder {
            Int80(it)
        }
    }
}

data class Int88(
    val value: BigNumber
) : IntBase(value, 88) {
    companion object {
        val DECODER: Decoder<Int88> = Decoder {
            Int88(it)
        }
    }
}

data class Int96(
    val value: BigNumber
) : IntBase(value, 96) {
    companion object {
        val DECODER: Decoder<Int96> = Decoder {
            Int96(it)
        }
    }
}

data class Int104(
    val value: BigNumber
) : IntBase(value, 104) {
    companion object {
        val DECODER: Decoder<Int104> =
            Decoder { Int104(it) }
    }
}

data class Int112(
    val value: BigNumber
) : IntBase(value, 112) {
    companion object {
        val DECODER: Decoder<Int112> =
            Decoder { Int112(it) }
    }
}

data class Int120(
    val value: BigNumber
) : IntBase(value, 120) {
    companion object {
        val DECODER: Decoder<Int120> =
            Decoder { Int120(it) }
    }
}

data class Int128(
    val value: BigNumber
) : IntBase(value, 128) {
    companion object {
        val DECODER: Decoder<Int128> =
            Decoder { Int128(it) }
    }
}

data class Int136(
    val value: BigNumber
) : IntBase(value, 136) {
    companion object {
        val DECODER: Decoder<Int136> =
            Decoder { Int136(it) }
    }
}

data class Int144(
    val value: BigNumber
) : IntBase(value, 144) {
    companion object {
        val DECODER: Decoder<Int144> =
            Decoder { Int144(it) }
    }
}

data class Int152(
    val value: BigNumber
) : IntBase(value, 152) {
    companion object {
        val DECODER: Decoder<Int152> =
            Decoder { Int152(it) }
    }
}

data class Int160(
    val value: BigNumber
) : IntBase(value, 160) {
    companion object {
        val DECODER: Decoder<Int160> =
            Decoder { Int160(it) }
    }
}

data class Int168(
    val value: BigNumber
) : IntBase(value, 168) {
    companion object {
        val DECODER: Decoder<Int168> =
            Decoder { Int168(it) }
    }
}

data class Int176(
    val value: BigNumber
) : IntBase(value, 176) {
    companion object {
        val DECODER: Decoder<Int176> =
            Decoder { Int176(it) }
    }
}

data class Int184(
    val value: BigNumber
) : IntBase(value, 184) {
    companion object {
        val DECODER: Decoder<Int184> =
            Decoder { Int184(it) }
    }
}

data class Int192(
    val value: BigNumber
) : IntBase(value, 192) {
    companion object {
        val DECODER: Decoder<Int192> =
            Decoder { Int192(it) }
    }
}

data class Int200(
    val value: BigNumber
) : IntBase(value, 200) {
    companion object {
        val DECODER: Decoder<Int200> =
            Decoder { Int200(it) }
    }
}

data class Int208(
    val value: BigNumber
) : IntBase(value, 208) {
    companion object {
        val DECODER: Decoder<Int208> =
            Decoder { Int208(it) }
    }
}

data class Int216(
    val value: BigNumber
) : IntBase(value, 216) {
    companion object {
        val DECODER: Decoder<Int216> =
            Decoder { Int216(it) }
    }
}

data class Int224(
    val value: BigNumber
) : IntBase(value, 224) {
    companion object {
        val DECODER: Decoder<Int224> =
            Decoder { Int224(it) }
    }
}

data class Int232(
    val value: BigNumber
) : IntBase(value, 232) {
    companion object {
        val DECODER: Decoder<Int232> =
            Decoder { Int232(it) }
    }
}

data class Int240(
    val value: BigNumber
) : IntBase(value, 240) {
    companion object {
        val DECODER: Decoder<Int240> =
            Decoder { Int240(it) }
    }
}

data class Int248(
    val value: BigNumber
) : IntBase(value, 248) {
    companion object {
        val DECODER: Decoder<Int248> =
            Decoder { Int248(it) }
    }
}

data class Int256(
    val value: BigNumber
) : IntBase(value, 256) {
    companion object {
        val DECODER: Decoder<Int256> =
            Decoder { Int256(it) }
    }
}