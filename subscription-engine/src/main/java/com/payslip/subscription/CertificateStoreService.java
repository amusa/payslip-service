// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.payslip.subscription;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Objects;

import jakarta.annotation.Nonnull;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
//import org.apache.tomcat.util.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.lang.NonNull;
// import org.springframework.stereotype.Service;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Service responsible for certificate operations: - getting the certificate -
 * validating signatures
 * - decrypting content
 */
@ApplicationScoped
public class CertificateStoreService {

    @ConfigProperty(name = "cert.storename")
    private String storeName;

    @ConfigProperty(name = "cert.storepass")
    private String storePassword;

    @ConfigProperty(name = "cert.alias")
    private String alias;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public CertificateStoreService() {
        // Add the BouncyCastle provider for
        // RSA/None/OAEPWithSHA1AndMGF1Padding cipher support
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * @return the KeyStore specified in application.yml
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws IOException
     */
    private KeyStore getCertificateStore()
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        var keystore = KeyStore.getInstance("JKS");

        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(storeName);

        // keystore.load(new FileInputStream(storeName), storePassword.toCharArray());
        keystore.load(inputStream, storePassword.toCharArray());
        log.info("***Keystore loaded successfully***");
        return keystore;
    }

    /**
     * @return the certificate specified in application.yml encoded in base64
     */
    public String getBase64EncodedCertificate() {
        try {
            var keystore = getCertificateStore();
            var certificate = keystore.getCertificate(alias);
            return new String(Base64.encodeBase64String(certificate.getEncoded()));
        } catch (final Exception e) {
            log.error("Error getting Base64 encoded certificate", e);
            return null;
        }
    }

    /**
     * @return the certificate ID or alias specified in application.yml
     */
    public String getCertificateId() {
        return alias;
    }

    /**
     * @param base64encodedSymmetricKey the base64-encoded symmetric key to be
     *                                  decrypted
     * @return the decrypted symmetric key
     */
    public byte[] getEncryptionKey(@Nonnull final String base64encodedSymmetricKey) {
        Objects.requireNonNull(base64encodedSymmetricKey);
        try {
            var encryptedSymmetricKey = Base64.decodeBase64(base64encodedSymmetricKey);
            var keystore = getCertificateStore();
            var asymmetricKey = keystore.getKey(alias, storePassword.toCharArray());
            var cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, asymmetricKey);
            return cipher.doFinal(encryptedSymmetricKey);
        } catch (final Exception e) {
            log.error("Error getting encryption key", e);
            return new byte[0];
        }
    }

    /**
     * @param encryptionKey       the symmetric key that was used to sign the
     *                            encrypted data
     * @param encryptedData       the signed encrypted data to validate
     * @param comparisonSignature the expected signature
     * @return true if the signature is valid, false if not
     */
    public boolean isDataSignatureValid(@Nonnull final byte[] encryptionKey,
            @Nonnull final String encryptedData, @Nonnull final String comparisonSignature) {
        Objects.requireNonNull(encryptionKey);
        Objects.requireNonNull(comparisonSignature);
        Objects.requireNonNull(encryptedData);
        try {
            var decodedEncryptedData = Base64.decodeBase64(encryptedData);
            var mac = Mac.getInstance("HMACSHA256");
            var secretKey = new SecretKeySpec(encryptionKey, "HMACSHA256");
            mac.init(secretKey);
            var hashedData = mac.doFinal(decodedEncryptedData);
            var encodedHashedData = new String(Base64.encodeBase64String(hashedData));
            return comparisonSignature.equals(encodedHashedData);
        } catch (final Exception e) {
            log.error("Error validating signature", e);
            return false;
        }
    }

    /**
     * @param encryptionKey the encryption key to use to decrypt the data
     * @param encryptedData the encrypted data
     * @return the decrypted data
     */
    public String getDecryptedData(@Nonnull final byte[] encryptionKey,
            @Nonnull final String encryptedData) {
        Objects.requireNonNull(encryptedData);
        Objects.requireNonNull(encryptionKey);
        try {
            var secretKey = new SecretKeySpec(encryptionKey, "AES");
            var ivBytes = Arrays.copyOf(encryptionKey, 16);
            @SuppressWarnings("java:S3329")
            // Sonar warns that a random IV should be used for encryption
            // but we are decrypting here.
            var ivSpec = new IvParameterSpec(ivBytes);

            var cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            return new String(cipher.doFinal(Base64.decodeBase64(encryptedData)));
        } catch (final Exception e) {
            log.error("Error decrypting data", e);
            return null;
        }
    }
}
