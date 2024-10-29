/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.etcd.internal.mocks;

import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.IEncryptionService;

public class MockEncryptionService implements IEncryptionService {

    private int encryptCount = 0;
    private int decryptCount = 0;

    public int getEncryptCount() {
        return encryptCount;
    }

    public int getDecryptCount() {
        return decryptCount;
    }

    @Override
    public String encrypt(String plainText) throws CredentialsException {
        encryptCount++;
        return plainText;
    }

    @Override
    public String decrypt(String encryptedText) throws CredentialsException {
        decryptCount++;
        return encryptedText;
    }
}
