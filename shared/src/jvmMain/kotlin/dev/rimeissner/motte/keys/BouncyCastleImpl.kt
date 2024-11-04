package dev.rimeissner.motte.keys

import okio.Buffer
import okio.ByteString.Companion.encodeUtf8
import okio.ByteString.Companion.toByteString
import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.asn1.x9.X9ECParametersHolder
import org.bouncycastle.asn1.x9.X9ECPoint
import org.bouncycastle.asn1.x9.X9IntegerConverter
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.crypto.signers.HMacDSAKCalculator
import org.bouncycastle.math.ec.ECAlgorithms
import org.bouncycastle.math.ec.ECConstants
import org.bouncycastle.math.ec.ECCurve
import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.math.ec.WNafUtil
import org.bouncycastle.math.ec.endo.GLVTypeBEndomorphism
import org.bouncycastle.math.ec.endo.GLVTypeBParameters
import org.bouncycastle.math.ec.endo.ScalarSplitParameters
import org.bouncycastle.util.encoders.Hex
import java.math.BigInteger

class BouncyKeyEngine : KeyEngine {

    private fun canonicalise(s: BigInteger) =
        if (s > SECP256K1_HALF_CURVE_ORDER) SECP256K1.n.subtract(s) else s

    private fun buildSignature(
        r: BigInteger,
        s: BigInteger,
        messageHash: ByteArray,
        keyPair: KeyPair
    ): Signature {
        // Now we have to work backwards to figure out the recId needed to recover the signature.
        var recId = -1
        for (i in 0..3) {
            try {
                val pubKey = recoverPubKey(r, s, i, messageHash)
                if (pubKey.contentEquals(keyPair.publicKey)) {
                    recId = i
                    break
                }
            } catch (e: IllegalStateException) {
                // Noop as wee iterate through all possible recIds
            }
        }
        if (recId == -1)
            throw RuntimeException("Could not construct a recoverable key. This should never happen.")
        val v = (recId + 27)
        return Signature(r.toByteArray(), s.toByteArray(), v)
    }

    override fun sign(keyPair: KeyPair, messageHash: ByteArray): Signature {
        if (messageHash.size != 32) throw IllegalArgumentException("messageHash is not 32 bytes")
        val pk = keyPair.privateKey ?: throw IllegalStateException("No private key available")
        val signer = ECDSASigner(HMacDSAKCalculator(SHA256Digest()))
        signer.init(true, ECPrivateKeyParameters(BigInteger(1, pk), SECP256K1))
        val (r, s) = signer.generateSignature(messageHash)
        return buildSignature(r, canonicalise(s), messageHash, keyPair)
    }

    private fun recoverPubKey(
        r: BigInteger,
        s: BigInteger,
        v: Int,
        messageHash: ByteArray
    ): ByteArray {
        if (v < 0) throw IllegalArgumentException("recId must be positive")
        if (r.signum() < 0) throw IllegalArgumentException("r must be positive")
        if (s.signum() < 0) throw IllegalArgumentException("s must be positive")
        // 1.0 For j from 0 to h   (h == recId here and the loop is outside this function)
        //   1.1 Let x = r + jn
        val n = SECP256K1.n  // Curve order.
        val i = BigInteger.valueOf(v.toLong() / 2)
        val x = r.add(i.multiply(n))
        //   1.2. Convert the integer x to an octet string X of length mlen using the conversion routine
        //        specified in Section 2.3.7, where mlen = ⌈(log2 p)/8⌉ or mlen = ⌈m/8⌉.
        //   1.3. Convert the octet string (16 set binary digits)||X to an elliptic curve point R using the
        //        conversion routine specified in Section 2.3.4. If this conversion routine outputs “invalid”, then
        //        do another iteration of Step 1.
        //
        // More concisely, what these points mean is to use X as a compressed public key.
        val curve = SECP256K1.curve as ECCurve.Fp
        val prime =
            curve.q  // Bouncy Castle is not consistent about the letter it uses for the prime.
        if (x >= prime) {
            // Cannot have point co-ordinates larger than this as everything takes place modulo Q.
            throw IllegalStateException("Point too large")
        }
        // Compressed keys require you to know an extra bit of data about the y-coord as there are two possibilities.
        // So it's encoded in the recId.
        val R = decompressKey(x, v and 1 == 1)
        //   1.4. If nR != point at infinity, then do another iteration of Step 1 (callers responsibility).
        if (!R.multiply(n).isInfinity) {
            throw IllegalStateException("nR is not point at infinity")
        }
        //   1.5. Compute e from M using Steps 2 and 3 of ECDSA signature verification.
        val e = BigInteger(1, messageHash)
        //   1.6. For k from 1 to 2 do the following.   (loop is outside this function via iterating recId)
        //   1.6.1. Compute a candidate public key as:
        //               Q = mi(r) * (sR - eG)
        //
        // Where mi(x) is the modular multiplicative inverse. We transform this into the following:
        //               Q = (mi(r) * s ** R) + (mi(r) * -e ** G)
        // Where -e is the modular additive inverse of e, that is z such that z + e = 0 (mod n). In the above equation
        // ** is point multiplication and + is point addition (the EC group operator).
        //
        // We can find the additive inverse by subtracting e from zero then taking the mod. For example the additive
        // inverse of 3 modulo 11 is 8 because 3 + 8 mod 11 = 0, and -3 mod 11 = 8.
        val eInv = BigInteger.ZERO.subtract(e).mod(n)
        val rInv = r.modInverse(n)
        val srInv = rInv.multiply(s).mod(n)
        val eInvrInv = rInv.multiply(eInv).mod(n)
        val q = ECAlgorithms.sumOfTwoMultiplies(SECP256K1.g, eInvrInv, R, srInv) as ECPoint.Fp
        return q.getEncoded(true)
    }

    /** Decompress a compressed public key (x co-ord and low-bit of y-coord).  */
    private fun decompressKey(xBN: BigInteger, yBit: Boolean): ECPoint {
        val x9 = X9IntegerConverter()
        val compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(SECP256K1.curve))
        compEnc[0] = (if (yBit) 0x03 else 0x02).toByte()
        return SECP256K1.curve.decodePoint(compEnc)
    }
}

class BouncyKeyPairFactoryImpl : KeyPairFactory {
    override fun fromPrivateKey(privateKey: ByteArray): KeyPair {
        val pkNum = BigInteger(1, privateKey)
        val pubKey = SECP256K1.g.multiply(pkNum).getEncoded(true)
        return KeyPair(pubKey, privateKey)
    }
}

class BouncyHDNodeFactory(
    private val keyPairFactory: KeyPairFactory = BouncyKeyPairFactoryImpl()
) : HDNodeFactory {

    fun masterNode(seed: ByteArray): HDNode {
        val hash = seed.toByteString().hmacSha512(MASTER_SECRET.encodeUtf8())
        val rootKeyPair = keyPairFactory.fromPrivateKey(hash.substring(0, 32).toByteArray())
        val chainCode = hash.substring(32).toByteArray()
        return HDNode(rootKeyPair, chainCode, 0, 0, byteArrayOf(0, 0, 0, 0))
    }

    override fun derive(node: HDNode, path: String): HDNode {
        if (path == "m" || path == "M" || path == "m'" || path == "M'") {
            return node
        }

        val parts = path.split("/")

        var dNode = node
        parts.forEachIndexed { index, s ->
            if (index == 0) {
                if (s != "m") {
                    throw IllegalArgumentException("Invalid path")
                } else {
                    return@forEachIndexed
                }
            }

            val hardened = s.length > 1 && s[s.length - 1] == '\''
            val cleanS = s.replace("'", "")
            var childIndex = cleanS.toLong()

            if (childIndex >= HARDENED_OFFSET) {
                throw IllegalArgumentException("Invalid index")
            }
            if (hardened) {
                childIndex += HARDENED_OFFSET
            }

            dNode = deriveChild(dNode, childIndex)
        }
        return dNode
    }

    private fun deriveChild(node: HDNode, index: Long): HDNode {
        node.keyPair.privateKey ?: throw IllegalStateException("No private key")
        val isHardened = index >= HARDENED_OFFSET
        val dataBuffer = Buffer()
        if (isHardened) {
            dataBuffer.writeByte(0)
            dataBuffer.write(node.keyPair.privateKey)
        } else {
            dataBuffer.write(node.keyPair.publicKey) // Use public key
        }
        dataBuffer.writeInt(index.toInt())
        val bytes = dataBuffer.hmacSha512(node.chainCode.toByteString())

        return try {
            val newChainCode = bytes.substring(32).toByteArray()
            val privateKey = bytes.substring(0, 32).toByteArray()
            HDNode(
                generateKeyPair(privateKey, node.keyPair.privateKey),
                newChainCode,
                node.depth + 1,
                index,
                node.fingerprint()
            )
        } catch (e: Exception) {
            deriveChild(node, index + 1)
        }
    }

    private fun generateKeyPair(privateKey: ByteArray, chainCode: ByteArray): KeyPair {
        val pkn = BigInteger(1, privateKey)
        val ccn = BigInteger(1, chainCode)
        if (pkn.signum() <= 0) {
            throw IllegalArgumentException("Private key must be greater than 0")
        }
        if (pkn >= SECP256K1.n) {
            throw IllegalArgumentException("Private key must be less than the curve order")
        }

        val an = pkn.add(ccn).mod(SECP256K1.n)
        if (an.signum() <= 0) {
            throw IllegalArgumentException("Private key must be greater than 0")
        }
        val adjusted = an.toBytes(32)
        return keyPairFactory.fromPrivateKey(adjusted)
    }

    companion object {
        private const val HARDENED_OFFSET = 0x80000000
    }
}

/**
 * The parameters of the secp256k1 curve that Ethereum uses.
 * Inlined from BouncyCastle to avoid looking up cureves by name
 */
private val SECP256K1_PARAMS = object : X9ECParametersHolder() {
    override fun createCurve(): ECCurve {
        // p = 2^256 - 2^32 - 2^9 - 2^8 - 2^7 - 2^6 - 2^4 - 1
        val p =
            BigInteger(
                1,
                Hex.decodeStrict("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F")
            )
        val a = ECConstants.ZERO
        val b = BigInteger.valueOf(7)
        val n =
            BigInteger(
                1,
                Hex.decodeStrict("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141")
            )
        val h = BigInteger.valueOf(1)

        val glv = GLVTypeBParameters(
            BigInteger("7ae96a2b657c07106e64479eac3434e99cf0497512f58995c1396c28719501ee", 16),
            BigInteger("5363ad4cc05c30e0a5261c028812645a122e22ea20816678df02967c1b23bd72", 16),
            ScalarSplitParameters(
                arrayOf(
                    BigInteger("3086d221a7d46bcde86c90e49284eb15", 16),
                    BigInteger("-e4437ed6010e88286f547fa90abfe4c3", 16)
                ),
                arrayOf(
                    BigInteger("114ca50f7a8e2f3f657c1108d9d44cfd8", 16),
                    BigInteger("3086d221a7d46bcde86c90e49284eb15", 16)
                ),
                BigInteger("3086d221a7d46bcde86c90e49284eb153dab", 16),
                BigInteger("e4437ed6010e88286f547fa90abfe4c42212", 16),
                272
            )
        )
        val c = ECCurve.Fp(p, a, b, n, h, true)
        return c.configure().setEndomorphism(GLVTypeBEndomorphism(c, glv)).create()
    }

    override fun createParameters(): X9ECParameters {
        val S: ByteArray? = null
        val curve = curve

        val G = X9ECPoint(
            curve, Hex.decodeStrict(
                "0479BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8"
            )
        )
        WNafUtil.configureBasepoint(G.point)

        return X9ECParameters(curve, G, curve.order, curve.cofactor, S)
    }
}.parameters

private val SECP256K1 = ECDomainParameters(
    SECP256K1_PARAMS.curve,
    SECP256K1_PARAMS.g,
    SECP256K1_PARAMS.n,
    SECP256K1_PARAMS.h
)

/**
 * Equal to CURVE.getN().shiftRight(1), used for canonicalising the S value of a signature. If you aren't
 * sure what this is about, you can ignore it.
 */
private val SECP256K1_HALF_CURVE_ORDER: BigInteger = SECP256K1.n.shiftRight(1)
