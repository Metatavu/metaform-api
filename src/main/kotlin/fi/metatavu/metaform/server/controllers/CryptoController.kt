package fi.metatavu.metaform.server.controllers

import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import java.nio.ByteBuffer
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for cryptography related operations
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class CryptoController {
    @Inject
    lateinit var logger: Logger

    /**
     * Generates new RSA key pair
     *
     * @return generated key pair
     */
    fun generateRsaKeyPair(): KeyPair? {
        return try {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(KEY_SIZE)
            keyPairGenerator.generateKeyPair()
        } catch (e: NoSuchAlgorithmException) {
            logger.error("Failed to initialize key pair generator", e)
            null
        }
    }

    /**
     * Returns public part of key pair as base64 encoded string
     *
     * @param publicKey public key
     * @return public part of key pair as base64 encoded string
     */
    fun getPublicKeyBase64(publicKey: PublicKey?): String? {
        return publicKey?.let { Base64.encodeBase64URLSafeString(it.encoded) }
    }

    /**
     * Returns private part of key pair as base64 encoded string
     *
     * @param privateKey private key
     * @return private part of key pair as base64 encoded string
     */
    fun getPrivateKeyBase64(privateKey: PrivateKey?): String? {
        return privateKey?.let { Base64.encodeBase64URLSafeString(it.encoded) }
    }

    /**
     * Loads public key from base64 encoded string
     *
     * @param base64String encoded key
     * @return public key
     */
    fun loadPublicKeyBase64(base64String: String?): PublicKey? {
        if (StringUtils.isBlank(base64String)) {
            return null
        }
        val key = Base64.decodeBase64(base64String) ?: return null

        return try {
            val keyFactory = KeyFactory.getInstance("RSA")
            val keySpec = X509EncodedKeySpec(key)
            keyFactory.generatePublic(keySpec)
        } catch (e: NoSuchAlgorithmException) {
            null
        } catch (e: InvalidKeySpecException) {
            null
        }
    }

    /**
     * Loads private key from base64 encoded string
     *
     * @param base64String encoded key
     * @return private key
     */
    fun loadPrivateKeyBase64(base64String: String?): PrivateKey? {
        if (StringUtils.isBlank(base64String)) {
            return null
        }

        val key = Base64.decodeBase64(base64String) ?: return null
        return loadPrivateKey(key)
    }

    /**
     * Loads private key
     *
     * @param key key
     * @return private key
     */
    fun loadPrivateKey(key: ByteArray?): PrivateKey? {
        return try {
            val keyFactory = KeyFactory.getInstance("RSA")
            val keySpec = PKCS8EncodedKeySpec(key)
            keyFactory.generatePrivate(keySpec)
        } catch (e: NoSuchAlgorithmException) {
            null
        } catch (e: InvalidKeySpecException) {
            null
        }
    }

    /**
     * Signs UUID with given private key
     *
     * @param privateKey private key
     * @param id id
     * @return signature
     */
    fun signUUID(privateKey: PrivateKey, id: UUID): ByteArray? {
        return sign(privateKey, getUUIDBytes(id))
    }

    /**
     * Verifys that given signature matches given UUID with given public key
     *
     * @param publicKey public key
     * @param signature signature
     * @param id iod
     * @return whether signature matches UUID with public key
     */
    fun verifyUUID(publicKey: PublicKey, signature: ByteArray, id: UUID): Boolean {
        return verify(publicKey, signature, getUUIDBytes(id))
    }

    /**
     * Signs data with given private key
     *
     * @param privateKey private key
     * @param data data
     * @return signature
     */
    private fun sign(privateKey: PrivateKey, data: ByteArray): ByteArray? {
        return try {
            val signature = Signature.getInstance("SHA256withRSA")
            signature.initSign(privateKey)
            signature.update(data)
            signature.sign()
        } catch (e: NoSuchAlgorithmException) {
            logger.error("Failed to sign data", e)
            null
        } catch (e: InvalidKeyException) {
            logger.error("Failed to sign data", e)
            null
        } catch (e: SignatureException) {
            logger.error("Failed to sign data", e)
            null
        }
    }

    /**
     * Verifys that given signature matches given data with given public key
     *
     * @param publicKey public key
     * @param signature signature
     * @param data data
     * @return whether signature matches data with public key
     */
    private fun verify(publicKey: PublicKey, signature: ByteArray, data: ByteArray): Boolean {
        return try {
            val signInstance = Signature.getInstance("SHA256withRSA")
            signInstance.initVerify(publicKey)
            signInstance.update(data)
            signInstance.verify(signature)
        } catch (e: SignatureException) {
            logger.error("Failed to verífy data", e)
            false
        } catch (e: InvalidKeyException) {
            logger.error("Failed to verífy data", e)
            false
        } catch (e: NoSuchAlgorithmException) {
            logger.error("Failed to verífy data", e)
            false
        }
    }

    /**
     * Returns UUID as byte array
     *
     * @param id id
     * @return UUID as byte array
     */
    private fun getUUIDBytes(id: UUID): ByteArray {
        val result = ByteBuffer.wrap(ByteArray(16))
        result.putLong(id.mostSignificantBits)
        result.putLong(id.leastSignificantBits)
        return result.array()
    }

    companion object {
        private const val KEY_SIZE = 2048
    }
}