package motte.crypto.keys

import dev.rimeissner.motte.keys.BouncyKeyPairFactory
import org.junit.Assert.assertArrayEquals
import org.junit.Test

@OptIn(ExperimentalStdlibApi::class)
class BouncyKeyPairFactoryTest {
    @Test
    fun derivePublicKey_isCorrect() {
        val factory = BouncyKeyPairFactory()
        val keyPair = factory.fromPrivateKey("75205699e5fab1e0fa5c3d41898d00f8d251dc38f09ab85b9f5f035f72725310".hexToByteArray())

        assertArrayEquals("02e3c64e2b1d86045297da6167c1b6f26bbdb0ab572728bf3d6722f87e5f0a4838".hexToByteArray(), keyPair.publicKey)
        assertArrayEquals("75205699e5fab1e0fa5c3d41898d00f8d251dc38f09ab85b9f5f035f72725310".hexToByteArray(), keyPair.privateKey)
    }
}