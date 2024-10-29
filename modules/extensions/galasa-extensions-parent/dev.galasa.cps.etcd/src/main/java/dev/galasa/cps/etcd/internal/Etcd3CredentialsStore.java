/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cps.etcd.internal;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import javax.crypto.spec.SecretKeySpec;

import dev.galasa.ICredentials;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.CredentialsToken;
import dev.galasa.framework.spi.creds.CredentialsUsername;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.framework.spi.creds.CredentialsUsernameToken;
import dev.galasa.framework.spi.creds.FrameworkEncryptionService;
import dev.galasa.framework.spi.creds.ICredentialsStore;
import dev.galasa.framework.spi.creds.IEncryptionService;
import io.etcd.jetcd.Client;

/**
 * This class implements the credential store in a etcd store. Usernames,
 * Passwords and tokens can be retrieved from etc using the correct key format:
 * 
 * "secure.credentials.{SomeCredentialId};.username"
 */
public class Etcd3CredentialsStore extends Etcd3Store implements ICredentialsStore {
    private final SecretKeySpec key;
    private final IEncryptionService encryptionService;

    /**
     * This constructor instantiates the Key value client that can retrieve values
     * from the etcd store.
     * @param framework - The framework used by this credential store.
     * @param etcd - URI location of ETCD store.
     * @throws CredentialsException A failure occurred.
     */
    public Etcd3CredentialsStore(IFramework framework, URI etcd) throws CredentialsException {
        super(etcd);
        try {
            IConfigurationPropertyStoreService cpsService = framework.getConfigurationPropertyService("secure");
            String encryptionKey = cpsService.getProperty("credentials.file", "encryption.key");
            if (encryptionKey != null) {
                key = createKey(encryptionKey);
            } else {
                key = null;
            }

            this.encryptionService = new FrameworkEncryptionService(key);
        } catch (Exception e) {
            throw new CredentialsException("Unable to initialise the etcd credentials store", e);
        }
    }

    public Etcd3CredentialsStore(SecretKeySpec key, IEncryptionService encryptionService, Client etcdClient) throws CredentialsException {
        super(etcdClient);
        this.key = key;
        this.encryptionService = encryptionService;
    }

    /**
     * This method checks for the three available credential types in the
     * credentials stores and returns the appropiate response.
     * 
     * A token will be returned on its own as a toekn object, a username can be
     * passed back on its own, or with a passowrd if there is one available.
     * 
     * @param credentialsId a key which contains the credential id.
     * @throws CredentialsException A failure occurred.
     */
    public ICredentials getCredentials(String credentialsId) throws CredentialsException {
        try {
            ICredentials credentials = null;
            String token = get("secure.credentials." + credentialsId + ".token");
            String username = get("secure.credentials." + credentialsId + ".username");

            // Check if the credentials are UsernameToken or Token
            if (token != null && username != null) {
                credentials = new CredentialsUsernameToken(key, username, token);
            } else if (token != null) {
                credentials = new CredentialsToken(key, token);
            } else if (username != null) {
                // We have a username, so check if the credentials are UsernamePassword or Username
                String password = get("secure.credentials." + credentialsId + ".password");
                if (password != null) {
                   credentials = new CredentialsUsernamePassword(key, username, password); 
                } else {
                    credentials = new CredentialsUsername(key, username);
                }
            }
    
            return credentials;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new CredentialsException("Failed to get credentials", e);
        }
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
        super.shutdownStore();
    }

    @Override
    public void setCredentials(String credentialsId, ICredentials credentials) throws CredentialsException {
        Properties credentialProperties = credentials.toProperties(credentialsId);

        try {
            for (Entry<Object, Object> property : credentialProperties.entrySet()) {
                put((String) property.getKey(), encryptionService.encrypt((String) property.getValue()));
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new CredentialsException("Failed to set credentials", e);
        }
    }

    @Override
    public void deleteCredentials(String credentialsId) throws CredentialsException {
        try {
            deletePrefix("secure.credentials." + credentialsId);
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new CredentialsException("Failed to delete credentials", e);
        }
    }
}
