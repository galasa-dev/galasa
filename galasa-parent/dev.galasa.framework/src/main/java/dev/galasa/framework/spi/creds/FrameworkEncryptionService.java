/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.creds;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import dev.galasa.framework.FileSystem;
import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.spi.Environment;
import dev.galasa.framework.spi.SystemEnvironment;

public class FrameworkEncryptionService implements IEncryptionService {
    public static final String ENCRYPTION_KEYS_PATH_ENV = "GALASA_ENCRYPTION_KEYS_PATH";

    private static final String KEY_ALGORITHM = "AES";
    private static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";

    private static final int GCM_AUTH_TAG_LENGTH_BITS = 128;
    private static final int GCM_IV_BYTES_LENGTH = 12;

    private SecretKeySpec encryptionKey;
    private List<SecretKeySpec> decryptionKeys = new ArrayList<>();

    private SecureRandom secureRandom;

    public FrameworkEncryptionService(SecretKeySpec encryptionKey) throws CredentialsException {
        this(encryptionKey, new FileSystem(), new SystemEnvironment(), new SecureRandom());
    }

    public FrameworkEncryptionService(SecretKeySpec encryptionKey, IFileSystem fileSystem, Environment environment) throws CredentialsException {
        this(encryptionKey, fileSystem, environment, new SecureRandom());
    }

    public FrameworkEncryptionService(
        SecretKeySpec encryptionKey,
        IFileSystem fileSystem,
        Environment environment,
        SecureRandom secureRandom
    ) throws CredentialsException {
        this.encryptionKey = encryptionKey;
        this.secureRandom = secureRandom;

        if (encryptionKey == null) {
            EncryptionKeys encryptionKeys = new EncryptionKeys(fileSystem, environment);
            String parsedEncryptionKey = encryptionKeys.getEncryptionKey();
            List<String> parsedFallbackKeys = encryptionKeys.getFallbackDecryptionKeys();
            if (parsedEncryptionKey != null && parsedFallbackKeys != null) {
                this.encryptionKey = loadPrimaryEncryptionKey(parsedEncryptionKey);
                this.decryptionKeys = loadDecryptionKeys(this.encryptionKey, parsedFallbackKeys);
            }
        } else {
            this.decryptionKeys = new ArrayList<>();
            this.decryptionKeys.add(encryptionKey);
        }
    }

    private SecretKeySpec loadPrimaryEncryptionKey(String encodedKeyStr) throws CredentialsException {
        byte[] decodedPrimaryKeyBytes = Base64.getDecoder().decode(encodedKeyStr);
        return new SecretKeySpec(decodedPrimaryKeyBytes, KEY_ALGORITHM);
    }

    private List<SecretKeySpec> loadDecryptionKeys(SecretKeySpec primaryEncryptionKey, List<String> encodedFallbackKeys) throws CredentialsException {
        List<SecretKeySpec> decryptionKeys = new ArrayList<>();
        decryptionKeys.add(primaryEncryptionKey);

        for (String fallbackKey : encodedFallbackKeys) {
            byte[] decodedFallbackKeyBytes = Base64.getDecoder().decode(fallbackKey);
            SecretKeySpec key = new SecretKeySpec(decodedFallbackKeyBytes, KEY_ALGORITHM);
            decryptionKeys.add(key);
        }
        return decryptionKeys;
    }

    @Override
    public String encrypt(String plainText) throws CredentialsException {
        if (this.encryptionKey == null) {
            throw new CredentialsException("Unable to encrypt the provided data. No encryption key has been set");
        }

        // Generate a random initialization vector (IV)
        byte[] initVector = new byte[GCM_IV_BYTES_LENGTH];
        secureRandom.nextBytes(initVector);

        byte[] encryptedIvAndText;
        try {
            // Initialise the GCM cipher in encrypt mode
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_AUTH_TAG_LENGTH_BITS, initVector);
            cipher.init(Cipher.ENCRYPT_MODE, this.encryptionKey, gcmParameterSpec);

            // Encrypt the plaintext
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Concatenate the IV and the encrypted text
            encryptedIvAndText = new byte[initVector.length + encryptedBytes.length];
            System.arraycopy(initVector, 0, encryptedIvAndText, 0, initVector.length);
            System.arraycopy(encryptedBytes, 0, encryptedIvAndText, initVector.length, encryptedBytes.length);

        } catch (Exception e) {
            throw new CredentialsException("Failed to encrypt the provided data", e);
        }
        return Base64.getEncoder().encodeToString(encryptedIvAndText);
    }

    @Override
    public String decrypt(String encryptedText) throws CredentialsException {
        String decryptedText = null;
        for (SecretKeySpec key : decryptionKeys) {
            try {
                decryptedText = decrypt(encryptedText, key);
            } catch (CredentialsException e) {
                // Decryption failed, so let's try the next key...
            }
        }
        return decryptedText;
    }

    private String decrypt(String encryptedText, SecretKeySpec decryptionKey) throws CredentialsException {
        String decryptedText = null;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);

            // Get the IV from the encrypted text
            byte[] initVector = new byte[GCM_IV_BYTES_LENGTH];
            System.arraycopy(decodedBytes, 0, initVector, 0, initVector.length);

            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_AUTH_TAG_LENGTH_BITS, initVector);
            cipher.init(Cipher.DECRYPT_MODE, decryptionKey, gcmParameterSpec);

            // Strip off the IV and decrypt the text
            byte[] decryptedBytes = cipher.doFinal(decodedBytes, initVector.length, decodedBytes.length - initVector.length);
            decryptedText = new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CredentialsException("Failed to decrypt the provided data", e);
        }
        return decryptedText;
    }
}
