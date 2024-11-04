package motte.crypto.keys

import dev.rimeissner.motte.keys.KeyPair
import dev.rimeissner.motte.keys.Signature
import dev.rimeissner.motte.keys.BouncyKeyEngineImpl
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalStdlibApi::class)
class BouncyKeyEngineTest {
    @Test
    fun sign_isCorrect() {
        val engine = BouncyKeyEngineImpl()
        val keyPair = KeyPair(
            publicKey = "0314bcddd11fc0b4c6202574c82583c2bafc1f0639aec7017fd43a72b9407a1dd9".hexToByteArray(),
            privateKey = "8678adf78db8d1c8a40028795077b3463ca06a743ca37dfd28a5b4442c27b457".hexToByteArray()
        )
        val data = "9b8bc77908c0b0ebe93e897e43f594b811f5d7130d86a5708403ddb417dc111b".hexToByteArray()

        val expectedSignature = Signature(
            "6c65af8fabdf55b026300ccb4cf1c19f27592a81c78aba86abe83409563d9c13".hexToByteArray(),
            "256a9a9e87604e89f083983f7449f58a456ac7929265f7114d585538fe226e1f".hexToByteArray(),
            27
        )
        val actualSignature = engine.sign(keyPair, data)
        assertArrayEquals("Unexpected r", expectedSignature.r, actualSignature.r)
        assertArrayEquals("Unexpected s", expectedSignature.s, actualSignature.s)
        assertEquals("Unexpected v", expectedSignature.v, actualSignature.v)
    }
}