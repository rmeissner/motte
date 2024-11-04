package dev.rimeissner.motte.solidity

import dev.rimeissner.motte.math.BigNumber


data class UInt8(
    val value: BigNumber
) : UIntBase(value, 8) {
    companion object {
        val DECODER: Decoder<UInt8> =
            Decoder { UInt8(it) }
    }
}

data class UInt16(
    val value: BigNumber
) : UIntBase(value, 16) {
    companion object {
        val DECODER: Decoder<UInt16> =
            Decoder { UInt16(it) }
    }
}

data class UInt24(
    val value: BigNumber
) : UIntBase(value, 24) {
    companion object {
        val DECODER: Decoder<UInt24> =
            Decoder { UInt24(it) }
    }
}

data class UInt32(
    val value: BigNumber
) : UIntBase(value, 32) {
    companion object {
        val DECODER: Decoder<UInt32> =
            Decoder { UInt32(it) }
    }
}

data class UInt40(
    val value: BigNumber
) : UIntBase(value, 40) {
    companion object {
        val DECODER: Decoder<UInt40> =
            Decoder { UInt40(it) }
    }
}

data class UInt48(
    val value: BigNumber
) : UIntBase(value, 48) {
    companion object {
        val DECODER: Decoder<UInt48> =
            Decoder { UInt48(it) }
    }
}

data class UInt56(
    val value: BigNumber
) : UIntBase(value, 56) {
    companion object {
        val DECODER: Decoder<UInt56> =
            Decoder { UInt56(it) }
    }
}

data class UInt64(
    val value: BigNumber
) : UIntBase(value, 64) {
    companion object {
        val DECODER: Decoder<UInt64> =
            Decoder { UInt64(it) }
    }
}

data class UInt72(
    val value: BigNumber
) : UIntBase(value, 72) {
    companion object {
        val DECODER: Decoder<UInt72> =
            Decoder { UInt72(it) }
    }
}

data class UInt80(
    val value: BigNumber
) : UIntBase(value, 80) {
    companion object {
        val DECODER: Decoder<UInt80> =
            Decoder { UInt80(it) }
    }
}

data class UInt88(
    val value: BigNumber
) : UIntBase(value, 88) {
    companion object {
        val DECODER: Decoder<UInt88> =
            Decoder { UInt88(it) }
    }
}

data class UInt96(
    val value: BigNumber
) : UIntBase(value, 96) {
    companion object {
        val DECODER: Decoder<UInt96> =
            Decoder { UInt96(it) }
    }
}

data class UInt104(
    val value: BigNumber
) : UIntBase(value, 104) {
    companion object {
        val DECODER: Decoder<UInt104> =
            Decoder { UInt104(it) }
    }
}

data class UInt112(
    val value: BigNumber
) : UIntBase(value, 112) {
    companion object {
        val DECODER: Decoder<UInt112> =
            Decoder { UInt112(it) }
    }
}

data class UInt120(
    val value: BigNumber
) : UIntBase(value, 120) {
    companion object {
        val DECODER: Decoder<UInt120> =
            Decoder { UInt120(it) }
    }
}

data class UInt128(
    val value: BigNumber
) : UIntBase(value, 128) {
    companion object {
        val DECODER: Decoder<UInt128> =
            Decoder { UInt128(it) }
    }
}

data class UInt136(
    val value: BigNumber
) : UIntBase(value, 136) {
    companion object {
        val DECODER: Decoder<UInt136> =
            Decoder { UInt136(it) }
    }
}

data class UInt144(
    val value: BigNumber
) : UIntBase(value, 144) {
    companion object {
        val DECODER: Decoder<UInt144> =
            Decoder { UInt144(it) }
    }
}

data class UInt152(
    val value: BigNumber
) : UIntBase(value, 152) {
    companion object {
        val DECODER: Decoder<UInt152> =
            Decoder { UInt152(it) }
    }
}

data class UInt160(
    val value: BigNumber
) : UIntBase(value, 160) {
    companion object {
        val DECODER: Decoder<UInt160> =
            Decoder { UInt160(it) }
    }
}

data class UInt168(
    val value: BigNumber
) : UIntBase(value, 168) {
    companion object {
        val DECODER: Decoder<UInt168> =
            Decoder { UInt168(it) }
    }
}

data class UInt176(
    val value: BigNumber
) : UIntBase(value, 176) {
    companion object {
        val DECODER: Decoder<UInt176> =
            Decoder { UInt176(it) }
    }
}

data class UInt184(
    val value: BigNumber
) : UIntBase(value, 184) {
    companion object {
        val DECODER: Decoder<UInt184> =
            Decoder { UInt184(it) }
    }
}

data class UInt192(
    val value: BigNumber
) : UIntBase(value, 192) {
    companion object {
        val DECODER: Decoder<UInt192> =
            Decoder { UInt192(it) }
    }
}

data class UInt200(
    val value: BigNumber
) : UIntBase(value, 200) {
    companion object {
        val DECODER: Decoder<UInt200> =
            Decoder { UInt200(it) }
    }
}

data class UInt208(
    val value: BigNumber
) : UIntBase(value, 208) {
    companion object {
        val DECODER: Decoder<UInt208> =
            Decoder { UInt208(it) }
    }
}

data class UInt216(
    val value: BigNumber
) : UIntBase(value, 216) {
    companion object {
        val DECODER: Decoder<UInt216> =
            Decoder { UInt216(it) }
    }
}

data class UInt224(
    val value: BigNumber
) : UIntBase(value, 224) {
    companion object {
        val DECODER: Decoder<UInt224> =
            Decoder { UInt224(it) }
    }
}

data class UInt232(
    val value: BigNumber
) : UIntBase(value, 232) {
    companion object {
        val DECODER: Decoder<UInt232> =
            Decoder { UInt232(it) }
    }
}

data class UInt240(
    val value: BigNumber
) : UIntBase(value, 240) {
    companion object {
        val DECODER: Decoder<UInt240> =
            Decoder { UInt240(it) }
    }
}

data class UInt248(
    val value: BigNumber
) : UIntBase(value, 248) {
    companion object {
        val DECODER: Decoder<UInt248> =
            Decoder { UInt248(it) }
    }
}

data class UInt256(
    val value: BigNumber
) : UIntBase(value, 256) {
    companion object {
        val DECODER: Decoder<UInt256> =
            Decoder { UInt256(it) }
    }
}