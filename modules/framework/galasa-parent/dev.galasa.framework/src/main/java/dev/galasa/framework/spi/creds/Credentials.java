/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.creds;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import dev.galasa.framework.FileSystem;
import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.spi.Environment;
import dev.galasa.framework.spi.SystemEnvironment;

public abstract class Credentials {

    private IEncryptionService encryptionService;
    private final SecretKeySpec key;

    public Credentials() {
        this.key = null;
    }

    public Credentials(SecretKeySpec key) throws CredentialsException {
        this(key, new FileSystem(), new SystemEnvironment());
    }

    public Credentials(SecretKeySpec key, IFileSystem fileSystem, Environment environment) throws CredentialsException {
        this.key = key;
        this.encryptionService = new FrameworkEncryptionService(key, fileSystem, environment);
    }

    protected String decryptToString(String encryptedText) throws CredentialsException {
        return encryptionService.decrypt(encryptedText);
    }

    protected byte[] decode(String text) throws CredentialsException {
        if (text.startsWith("aes:")) {
            return decrypt(text.substring(4));
        } else if (text.startsWith("base64:")) {
            return base64(text.substring(7));
        } else {
            return text.getBytes();
        }
    }

    protected byte[] base64(String base64) {
        return Base64.getDecoder().decode(base64);
    }

    protected byte[] decrypt(String encrypted) throws CredentialsException {
        if (this.key == null) {
            throw new CredentialsException("Unable to decrypt credentials as no decrypt key has been provided");
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(new byte[16]));
            return cipher.doFinal(Base64.getDecoder().decode(encrypted));
        } catch (Exception e) {
            throw new CredentialsException("Unable to decrypt credentials", e);
        }
    }

}
