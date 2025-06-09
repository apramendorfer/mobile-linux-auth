package at.apramendorfer.authenticator.service

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import java.io.StringWriter
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStore.PrivateKeyEntry
import java.security.Signature

class CryptoManagerService {
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    fun getPublicKeyAsPem(): String {
        val publicKey = getKeyPair().public;

        val stringWriter = StringWriter()
        JcaPEMWriter(stringWriter).use { pemWriter ->
            pemWriter.writeObject(publicKey)
        }
        return stringWriter.toString()
    }

    fun decryptWithDefaultKey(value: String): ByteArray {
        val keypair = getKeyPair()

        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(keypair.private)
        signature.update(value.toByteArray())
        return signature.sign()
    }

    private fun getKeyPair(): KeyPair {
        val existingKey = keyStore.getEntry(KEY_ALIAS_PRIVATE, null) as? PrivateKeyEntry;
        return if(existingKey != null) {
            KeyPair(existingKey.certificate.publicKey, existingKey.privateKey)
        } else {
            createKey()
        }
    }

    private fun createKey(): KeyPair {
        //We are creating a RSA key pair and store it in the Android Keystore
        val keyPairGenerator: KeyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM, ANDROID_KEY_STORE)

        //We are creating the key pair with sign and verify purposes
        val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS_PRIVATE,
            KeyProperties.PURPOSE_SIGN or
                    KeyProperties.PURPOSE_VERIFY or
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).run {
            setBlockModes(KeyProperties.BLOCK_MODE_ECB)
            setDigests(KeyProperties.DIGEST_SHA256)
            setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            setKeySize(3072)
            build()
        }

        //Initialization of key generator with the parameters we have specified above
        keyPairGenerator.initialize(parameterSpec)
        //Generates the key pair
        return keyPairGenerator.genKeyPair()
    }

    companion object {
        private  const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private  const val ALGORITHM = KeyProperties.KEY_ALGORITHM_RSA;

        private const val KEY_ALIAS_PRIVATE = "at.apramendorfer.authenticator.private.13"
    }
}