package fi.metatavu.metaform.server.crypto;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 * Controller for cryptography related operations
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class CryptoController {

  private static final int KEY_SIZE = 2048;

  @Inject
  private Logger logger;

  /**
   * Generates new RSA key pair
   * 
   * @return generated key pair
   */
  public KeyPair generateRsaKeyPair() {
    try {
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
      keyPairGenerator.initialize(KEY_SIZE);
      return keyPairGenerator.generateKeyPair();
    } catch (NoSuchAlgorithmException e) {
      logger.error("Failed to initialize key pair generator", e);
      return null;
    }
  }

  /**
   * Returns public part of key pair as base64 encoded string
   * 
   * @param publicKey public key
   * @return public part of key pair as base64 encoded string
   */
  public String getPublicKeyBase64(PublicKey publicKey) {
    if (publicKey == null) {
      return null;
    }

    return Base64.encodeBase64URLSafeString(publicKey.getEncoded());
  }

  /**
   * Returns private part of key pair as base64 encoded string
   * 
   * @param privateKey private key
   * @return private part of key pair as base64 encoded string
   */
  public String getPrivateKeyBase64(PrivateKey privateKey) {
    if (privateKey == null) {
      return null;
    }
    
    return Base64.encodeBase64URLSafeString(privateKey.getEncoded());
  }

  /**
   * Loads public key from base64 encoded string
   * 
   * @param base64String encoded key
   * @return public key
   */
  public PublicKey loadPublicKeyBase64(String base64String) {
    if (StringUtils.isBlank(base64String)) {
      return null;
    }

    byte[] key = Base64.decodeBase64(base64String);
    if (key == null) {
      return null;
    }

    try {
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
      return keyFactory.generatePublic(keySpec);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      return null;
    }
  }

  /**
   * Loads private key from base64 encoded string
   * 
   * @param base64String encoded key
   * @return private key
   */
  public PrivateKey loadPrivateKeyBase64(String base64String) {
    if (StringUtils.isBlank(base64String)) {
      return null;
    }

    byte[] key = Base64.decodeBase64(base64String);
    if (key == null) {
      return null;
    }

    return loadPrivateKey(key);
  }

  /**
   * Loads private key
   * 
   * @param key key
   * @return private key
   */
  public PrivateKey loadPrivateKey(byte[] key) {
    try {
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
      return keyFactory.generatePrivate(keySpec);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      return null;
    }
  }

  /**
   * Signs UUID with given private key
   * 
   * @param privateKey private key
   * @param id id
   * @return signature
   */
  public byte[] signUUID(PrivateKey privateKey, UUID id) {
    return sign(privateKey, getUUIDBytes(id));
  }

  /**
   * Verifys that given signature matches given UUID with given public key
   * 
   * @param publicKey public key
   * @param signature signature
   * @param id iod
   * @return whether signature matches UUID with public key
   */
  public boolean verifyUUID(PublicKey publicKey, byte[] signature, UUID id) {
    return verify(publicKey, signature, getUUIDBytes(id));
  }

  /**
   * Signs data with given private key
   * 
   * @param privateKey private key
   * @param data data
   * @return signature
   */
  private byte[] sign(PrivateKey privateKey, byte[] data) {
    try {
      Signature signature = Signature.getInstance("SHA256withRSA");
      signature.initSign(privateKey);
      signature.update(data);
      return signature.sign();
    } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
      logger.error("Failed to  sign data", e);
      return null;
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
  private boolean verify(PublicKey publicKey, byte[] signature, byte[] data) {
    try {
      Signature signInstance = Signature.getInstance("SHA256withRSA");
      signInstance.initVerify(publicKey);
      signInstance.update(data);
      return signInstance.verify(signature);
    } catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException e) {
      logger.error("Failed to  verífy data", e);
      return false;
    }
  }

  /**
   * Returns UUID as byte array 
   * 
   * @param id id
   * @return UUID as byte array
   */
  private byte[] getUUIDBytes(UUID id) {
    ByteBuffer result = ByteBuffer.wrap(new byte[16]);
    result.putLong(id.getMostSignificantBits());
    result.putLong(id.getLeastSignificantBits());
    return result.array();
  }

}
