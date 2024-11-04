package dev.rimeissner.merlon.crypto.graphics

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class Blockies(
    val primaryColor: Int,
    val backgroundColor: Int,
    val spotColor: Int,
    val useDarkText: Boolean,
    val data: DoubleArray
) {
    companion object {
        const val SIZE = 8

        fun fromAddress(address: String): Blockies {
            val seed = seedFromAddress(address.lowercase())
            // colorFromSeed() and dataFromSeed() change the seed
            // thus order is important
            val primaryColor = colorFromSeed(seed).toRgb()
            val backgroundHSL = colorFromSeed(seed)
            val useDarkText = backgroundHSL.l > 50.0
            val backgroundColor = backgroundHSL.toRgb()
            val spotColor = colorFromSeed(seed).toRgb()
            val imageData = dataFromSeed(seed)
            return Blockies(primaryColor, backgroundColor, spotColor, useDarkText, imageData)
        }

        private fun seedFromAddress(address: String): LongArray {
            val seed = LongArray(4)
            address.indices.forEach {
                var test = seed[it % 4] shl 5
                if (test > Int.MAX_VALUE shl 1 || test < Int.MIN_VALUE shl 1) test =
                    test.toInt().toLong()

                val test2 = test - seed[it % 4]
                seed[it % 4] = test2 + address[it].code
            }

            seed.indices.forEach { seed[it] = seed[it].toInt().toLong() }
            return seed
        }

        private fun colorFromSeed(seed: LongArray): HSL {
            val h = floor(nextFromSeed(seed) * 360.0)
            val s = nextFromSeed(seed) * 60.0 + 40.0
            val l =
                (nextFromSeed(seed) + nextFromSeed(seed) + nextFromSeed(seed) + nextFromSeed(seed)) * 25.0
            return HSL(h, s, l)
        }

        private fun nextFromSeed(seed: LongArray): Double {
            val t = (seed[0] xor (seed[0] shl 11)).toInt()
            seed[0] = seed[1]
            seed[1] = seed[2]
            seed[2] = seed[3]
            seed[3] = seed[3] xor (seed[3] shr 19) xor t.toLong() xor (t shr 8).toLong()
            val t1 = abs(seed[3]).toDouble()

            return t1 / Int.MAX_VALUE
        }

        private fun dataFromSeed(seed: LongArray) = DoubleArray(64).apply {
            (0 until SIZE).forEach { row ->
                (0 until SIZE / 2).forEach { column ->
                    val value = floor(nextFromSeed(seed) * 2.3)
                    this[row * SIZE + column] = value
                    this[(row + 1) * SIZE - column - 1] = value
                }
            }
        }
    }

    private data class HSL(val h: Double, val s: Double, val l: Double) {
        fun toRgb(): Int {
            var h = h.toFloat()
            var s = s.toFloat()
            var l = l.toFloat()
            h %= 360.0f
            h /= 360f
            s /= 100f
            l /= 100f

            val q = if (l < 0.5) l * (1 + s) else l + s - s * l
            val p = 2 * l - q

            var r = max(0f, hueToRGB(p, q, h + 1.0f / 3.0f))
            var g = max(0f, hueToRGB(p, q, h))
            var b = max(0f, hueToRGB(p, q, h - 1.0f / 3.0f))

            r = min(r, 1.0f)
            g = min(g, 1.0f)
            b = min(b, 1.0f)

            val red = (r * 255).toInt()
            val green = (g * 255).toInt()
            val blue = (b * 255).toInt()
            return 0xFF shl 24 or (red shl 16) or (green shl 8) or blue
        }
    }
}