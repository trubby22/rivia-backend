package me.rivia.api.encryption

import org.apache.tomcat.util.codec.binary.Base64
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.io.IOException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.Security
import java.security.cert.CertificateException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Service responsible for certificate operations: - getting the certificate - validating signatures
 * - decrypting content
 */
class CertificateStoreService {
    private val storeName = "JKSkeystore.jks"
    private val storePassword = "123456"

    /**
     * @return the certificate ID or alias specified in application.yml
     */
    val certificateId = "selfsignedjks"
    private val log = LoggerFactory.getLogger(this.javaClass)

    init {
        // Add the BouncyCastle provider for
        // RSA/None/OAEPWithSHA1AndMGF1Padding cipher support
        Security.addProvider(BouncyCastleProvider())
    }

    /**
     * @return the KeyStore specified in application.yml
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws IOException
     */
    @get:Throws(
        KeyStoreException::class,
        NoSuchAlgorithmException::class,
        CertificateException::class,
        IOException::class
    )
    private val certificateStore: KeyStore
        get() {
            val keystore = KeyStore.getInstance("JKS")
            keystore.load(
                FileInputStream(storeName),
                storePassword.toCharArray()
            )
            return keystore
        }

    /**
     * @return the certificate specified in application.yml encoded in base64
     */
    val base64EncodedCertificate: String?
        get() = try {
            val keystore = certificateStore
            val certificate = keystore.getCertificate(
                certificateId
            )
            String(Base64.encodeBase64(certificate.encoded))
        } catch (e: Exception) {
            log.error("Error getting Base64 encoded certificate", e)
            null
        }

    /**
     * @param base64encodedSymmetricKey the base64-encoded symmetric key to be decrypted
     * @return the decrypted symmetric key
     */
    fun getEncryptionKey(base64encodedSymmetricKey: String): ByteArray {
        Objects.requireNonNull(base64encodedSymmetricKey)
        return try {
            val encryptedSymmetricKey =
                Base64.decodeBase64(base64encodedSymmetricKey)
            val keystore = certificateStore
            val asymmetricKey =
                keystore.getKey(certificateId, storePassword.toCharArray())
            val cipher =
                Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding")
            cipher.init(Cipher.DECRYPT_MODE, asymmetricKey)
            cipher.doFinal(encryptedSymmetricKey)
        } catch (e: Exception) {
            log.error("Error getting encryption key", e)
            ByteArray(0)
        }
    }

    /**
     * @param encryptionKey the symmetric key that was used to sign the encrypted data
     * @param encryptedData the signed encrypted data to validate
     * @param comparisonSignature the expected signature
     * @return true if the signature is valid, false if not
     */
    fun isDataSignatureValid(
        encryptionKey: ByteArray,
        encryptedData: String, comparisonSignature: String
    ): Boolean {
        Objects.requireNonNull(encryptionKey)
        Objects.requireNonNull(comparisonSignature)
        Objects.requireNonNull(encryptedData)
        return try {
            val decodedEncryptedData = Base64.decodeBase64(encryptedData)
            val mac = Mac.getInstance("HMACSHA256")
            val secretKey = SecretKeySpec(encryptionKey, "HMACSHA256")
            mac.init(secretKey)
            val hashedData = mac.doFinal(decodedEncryptedData)
            val encodedHashedData = String(Base64.encodeBase64(hashedData))
            comparisonSignature == encodedHashedData
        } catch (e: Exception) {
            log.error("Error validating signature", e)
            false
        }
    }

    /**
     * @param encryptionKey the encryption key to use to decrypt the data
     * @param encryptedData the encrypted data
     * @return the decrypted data
     */
    fun getDecryptedData(
        encryptionKey: ByteArray,
        encryptedData: String
    ): String? {
        Objects.requireNonNull(encryptedData)
        Objects.requireNonNull(encryptionKey)
        return try {
            val secretKey = SecretKeySpec(encryptionKey, "AES")
            val ivBytes = encryptionKey.copyOf(16)
            val ivSpec = IvParameterSpec(ivBytes)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            String(cipher.doFinal(Base64.decodeBase64(encryptedData)))
        } catch (e: Exception) {
            log.error("Error decrypting data", e)
            null
        }
    }
}