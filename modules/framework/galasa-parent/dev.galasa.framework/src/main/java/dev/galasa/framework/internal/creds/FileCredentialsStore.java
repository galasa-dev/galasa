/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.creds;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import dev.galasa.framework.spi.FrameworkPropertyFile;
import dev.galasa.framework.spi.FrameworkPropertyFileException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.CredentialsKeyStore;
import dev.galasa.framework.spi.creds.CredentialsToken;
import dev.galasa.framework.spi.creds.CredentialsUsername;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.framework.spi.creds.CredentialsUsernameToken;
import dev.galasa.framework.spi.creds.ICredentialsStore;
import dev.galasa.ICredentials;

/**
 * <p>
 * This class is used to retrieve credentials stored locally, whether they are
 * encrypted or not
 * </p>
 * 
 *  
 */

public class FileCredentialsStore implements ICredentialsStore {
    private final FrameworkPropertyFile              fpf;
    private final SecretKeySpec                      key;
    private final IFramework                         framework;
    private final IConfigurationPropertyStoreService cpsService;

    public FileCredentialsStore(URI file, IFramework framework) throws CredentialsException {
        try {
            this.framework = framework;
            cpsService = this.framework.getConfigurationPropertyService("secure");
            fpf = new FrameworkPropertyFile(file);
            String encryptionKey = cpsService.getProperty("credentials.file", "encryption.key");
            if (encryptionKey != null) {
                key = createKey(encryptionKey);
            } else {
                key = null;
            }
        } catch (Exception e) {
            throw new CredentialsException("Unable to initialise the credentials store", e);
        }
    }

    /**
     * <p>
     * This method is used to retrieve credentials as an appropriate object
     * </p>
     *
     * @param credentialsId
     * @throws CredentialsException
     */
    @Override
    public ICredentials getCredentials(String credentialsId) throws CredentialsException {
        final String keyPrefix = "secure.credentials." + credentialsId;
        ICredentials credentials = null;

        String keystore = fpf.get(keyPrefix + ".keystore");
        if (keystore != null) {
            String keystorePassword = fpf.get(keyPrefix + ".password");
            String type = fpf.get(keyPrefix + ".type");
            
            credentials = new CredentialsKeyStore(key, keystore, keystorePassword, type);
        } else {
            String token = fpf.get(keyPrefix + ".token");
            String username = fpf.get(keyPrefix + ".username");
            String password = fpf.get(keyPrefix + ".password");

            boolean hasToken = (token != null);
            boolean hasUsername = (username != null);
            boolean hasPassword = (password != null);

            if (hasToken && hasUsername) {
                credentials = new CredentialsUsernameToken(key, username, token);
            } else if (hasToken) {
                credentials = new CredentialsToken(key, token);
            } else if (hasUsername && hasPassword) {
                credentials = new CredentialsUsernamePassword(key, username, password);
            } else if (hasUsername) {
                credentials = new CredentialsUsername(key, username);
            }    
        }

        return credentials;
    }

    private static SecretKeySpec createKey(String secret)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte[] key = secret.getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        key = sha.digest(key);
        return new SecretKeySpec(key, "AES");
    }

    @Override
    public void shutdown() throws CredentialsException {
        try {
            this.fpf.shutdown();
        } catch (FrameworkPropertyFileException e) {
            throw new CredentialsException("Problem shutting down the Credentials File", e);
        }
    }

    @Override
    public void setCredentials(String credsId, ICredentials credentials) throws CredentialsException {
        // Not implemented for local credentials...
    }

    @Override
    public void deleteCredentials(String credsId) throws CredentialsException {
        // Not implemented for local credentials...
    }

    @Override
    public Map<String, ICredentials> getAllCredentials() throws CredentialsException {
        // Not implemented for local credentials...
        throw new UnsupportedOperationException("Unimplemented method 'getAllCredentials'");
    }
}
