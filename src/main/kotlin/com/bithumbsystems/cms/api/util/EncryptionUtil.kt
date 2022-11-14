package com.bithumbsystems.cms.api.util

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.KeySpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class EncryptionUtil {

    private val logger by Logger()

    companion object {
        private const val AES_ALGORITHM = "AES/GCM/NoPadding"
        private const val TAG_LENGTH_BIT = 128 // must be one of {128, 120, 112, 104, 96}

        private const val IV_LENGTH_BYTE = 12
        private const val SALT_LENGTH_BYTE = 16
        private val UTF_8 = StandardCharsets.UTF_8

        private const val RSA_ALGORITHM = "RSA"
        private const val DEFAULT_VALUE = ""
    }

    // string a base64 encoded AES encrypted text
    fun String.encryptAES(password: String, plainMessage: String): String =
        runCatching {
            // 16 bytes salt
            val salt = getRandomNonce(SALT_LENGTH_BYTE)
            // GCM recommends 12 bytes iv
            val iv = getRandomNonce(IV_LENGTH_BYTE)
            encrypt(password, salt, iv, plainMessage)
        }.onFailure {
            logger.error(it.message)
        }.getOrDefault(DEFAULT_VALUE)

    fun String.encryptAES(password: String, plainMessage: String, saltKey: String, ivKey: String): String =
        runCatching {
            val decoder = Base64.getDecoder()
            val salt: ByteArray = decoder.decode(saltKey.toByteArray(UTF_8))
            val iv: ByteArray = decoder.decode(ivKey.toByteArray(UTF_8))
            encrypt(password, salt, iv, plainMessage)
        }.onFailure {
            logger.error(it.message)
        }.getOrDefault(DEFAULT_VALUE)

    // we need the same password, salt and iv to decrypt it
    fun String.decryptAES(password: String, cipherMessage: String): String =
        runCatching {
            val decode = Base64.getDecoder().decode(cipherMessage.toByteArray(UTF_8))

            // get back the iv and salt from the cipher text
            val bb = ByteBuffer.wrap(decode)
            val iv = ByteArray(IV_LENGTH_BYTE)
            bb[iv]
            val salt = ByteArray(SALT_LENGTH_BYTE)
            bb[salt]
            val cipherText = ByteArray(bb.remaining())
            bb[cipherText]

            // get back the aes key from the same password and salt
            val aesKeyFromPassword = getAESKeyFromPassword(password.toCharArray(), salt)
            val cipher = Cipher.getInstance(AES_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, aesKeyFromPassword, GCMParameterSpec(TAG_LENGTH_BIT, iv))
            val plainText = cipher.doFinal(cipherText)
            String(plainText, UTF_8)
        }.onFailure {
            logger.error(it.message)
        }.getOrDefault(DEFAULT_VALUE)

    fun String.encryptRSA(plainText: String, publicKey: String): String =
        runCatching {
            val cipher = Cipher.getInstance(RSA_ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, getPublicKeyFromBase64Encrypted(publicKey))
            val bytePlain = cipher.doFinal(plainText.toByteArray())
            Base64.getEncoder().encodeToString(bytePlain)
        }.onFailure {
            logger.error(it.message)
        }.getOrDefault(DEFAULT_VALUE)

    fun String.decryptRSA(encryptedText: String, privateKey: String): String =
        runCatching {
            val cipher = Cipher.getInstance(RSA_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, getPrivateKeyFromBase64Encrypted(privateKey))
            val byteEncrypted = Base64.getDecoder().decode(encryptedText.toByteArray())
            val bytePlain = cipher.doFinal(byteEncrypted)
            String(bytePlain, StandardCharsets.UTF_8)
        }.onFailure {
            logger.error(it.message)
        }.getOrDefault(DEFAULT_VALUE)

    private fun getRandomNonce(length: Int): ByteArray {
        val nonce = ByteArray(length)
        SecureRandom().nextBytes(nonce)
        return nonce
    }

    private fun encrypt(
        password: String,
        salt: ByteArray,
        iv: ByteArray,
        plainMessage: String
    ): String {
        // secret key from password
        val aesKeyFromPassword = getAESKeyFromPassword(password.toCharArray(), salt)
        val cipher = Cipher.getInstance(AES_ALGORITHM)

        // ASE-GCM needs GCMParameterSpec
        cipher.init(Cipher.ENCRYPT_MODE, aesKeyFromPassword, GCMParameterSpec(TAG_LENGTH_BIT, iv))
        val cipherText = cipher.doFinal(plainMessage.toByteArray(UTF_8))

        // prefix IV and Salt to cipher text
        val cipherTextWithIvSalt = ByteBuffer.allocate(iv.size + salt.size + cipherText.size)
            .put(iv)
            .put(salt)
            .put(cipherText)
            .array()

        return Base64.getEncoder().encodeToString(cipherTextWithIvSalt)
    }

    // AES 128 bits secret key derived from a password
    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    private fun getAESKeyFromPassword(password: CharArray?, salt: ByteArray?): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        // iterationCount = 65536
        // keyLength = 128
        val spec: KeySpec = PBEKeySpec(password, salt, 1024, 256)
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }

    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    private fun getPublicKeyFromBase64Encrypted(base64PublicKey: String): PublicKey? {
        val decodedBase64PubKey = Base64.getDecoder().decode(base64PublicKey)
        return KeyFactory.getInstance(RSA_ALGORITHM)
            .generatePublic(X509EncodedKeySpec(decodedBase64PubKey))
    }

    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    private fun getPrivateKeyFromBase64Encrypted(base64PrivateKey: String): PrivateKey? {
        val decodedBase64PrivateKey = Base64.getDecoder().decode(base64PrivateKey)
        return KeyFactory.getInstance(RSA_ALGORITHM)
            .generatePrivate(PKCS8EncodedKeySpec(decodedBase64PrivateKey))
    }
}
