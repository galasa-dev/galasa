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
import java.time.Instant;
import java.util.Properties;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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

    private static final String CREDS_NAMESPACE = "secure";
    private static final String CREDS_PROPERTY_PREFIX = CREDS_NAMESPACE + ".credentials.";

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
            IConfigurationPropertyStoreService cpsService = framework.getConfigurationPropertyService(CREDS_NAMESPACE);
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
        ICredentials credentials = null;
        try {
            Map<String, String> credentialsProperties = getPropertiesWithPrefix(CREDS_PROPERTY_PREFIX + credentialsId);
            credentials = convertPropertiesIntoCredentials(credentialsProperties, credentialsId);

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new CredentialsException("Failed to get credentials", e);
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
        super.shutdownStore();
    }

    @Override
    public void setCredentials(String credentialsId, ICredentials credentials) throws CredentialsException {
        Properties credentialProperties = credentials.toProperties(credentialsId);
        Properties metadataProperties = credentials.getMetadataProperties(credentialsId);

        try {
            // Clear any existing properties with the same credentials ID
            deleteCredentials(credentialsId);

            putAllProperties(credentialProperties, true);
            putAllProperties(metadataProperties, false);
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new CredentialsException("Failed to set credentials", e);
        }
    }

    @Override
    public void deleteCredentials(String credentialsId) throws CredentialsException {
        try {
            deletePropertiesWithPrefix(CREDS_PROPERTY_PREFIX + credentialsId);
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new CredentialsException("Failed to delete credentials", e);
        }
    }

    @Override
    public Map<String, ICredentials> getAllCredentials() throws CredentialsException {
        Map<String, ICredentials> credentials = new HashMap<>();
        try {
            Map<String, String> credentialsKeyValues = getPropertiesWithPrefix(CREDS_PROPERTY_PREFIX);

            // Build a set of all credential IDs stored in etcd
            Set<Entry<String, String>> credentialsEntries = credentialsKeyValues.entrySet();
            Set<String> credentialIds = new HashSet<>();
            for (Entry<String, String> entry : credentialsEntries) {
                String credsId = getCredentialsIdFromKey(entry.getKey());
                if (credsId != null) {
                    credentialIds.add(credsId);
                }
            }

            // For each credential ID, convert its properties into a credentials object for use by the framework
            for (String id : credentialIds) {
                Map<String, String> idProperties = credentialsEntries.stream()
                    .filter(entry -> entry.getKey().contains("." + id + "."))
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

                ICredentials convertedCredentials = convertPropertiesIntoCredentials(idProperties, id);
                if (convertedCredentials != null) {
                    credentials.put(id, convertedCredentials);
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new CredentialsException("Failed to get credentials", e);
        }
        return credentials;
    }

    private ICredentials convertPropertiesIntoCredentials(Map<String, String> credProperties, String credentialsId) throws CredentialsException {
        String token = credProperties.get(CREDS_PROPERTY_PREFIX + credentialsId + ".token");
        String username = credProperties.get(CREDS_PROPERTY_PREFIX + credentialsId + ".username");
        String password = credProperties.get(CREDS_PROPERTY_PREFIX + credentialsId + ".password");

        ICredentials credentials = null;

        // Check if the credentials are UsernameToken or Token
        if (token != null && username != null) {
            credentials = new CredentialsUsernameToken(key, username, token);
        } else if (token != null) {
            credentials = new CredentialsToken(key, token);
        } else if (username != null) {
            // We have a username, so check if the credentials are UsernamePassword or Username
            if (password != null) {
                credentials = new CredentialsUsernamePassword(key, username, password); 
            } else {
                credentials = new CredentialsUsername(key, username);
            }
        }

        if (credentials != null) {
            String description = credProperties.get(CREDS_PROPERTY_PREFIX + credentialsId + ".description");
            String lastUpdatedTime = credProperties.get(CREDS_PROPERTY_PREFIX + credentialsId + ".lastUpdated.time");
            String lastUpdatedUser = credProperties.get(CREDS_PROPERTY_PREFIX + credentialsId + ".lastUpdated.user");

            credentials.setDescription(description);
            credentials.setLastUpdatedByUser(lastUpdatedUser);
            if (lastUpdatedTime != null) {
                credentials.setLastUpdatedTime(Instant.parse(lastUpdatedTime));
            }
        }
        return credentials;
    }

    private String getCredentialsIdFromKey(String key) {
        // Keys for credentials should be in the form:
        //     secure.credentials.CRED_ID.suffix
        // so let's split on "." and grab the third part
        String credentialsId = null;
        String[] keyParts = key.split("\\.");
        if (keyParts.length >= 3) {
            credentialsId = keyParts[2];
        }
        return credentialsId;
    }

    private void putAllProperties(Properties properties, boolean encryptValues) throws CredentialsException, InterruptedException, ExecutionException {
        for (Entry<Object, Object> property : properties.entrySet()) {
            String key = (String) property.getKey();
            String value = (String) property.getValue();
            if (encryptValues) {
                value = encryptionService.encrypt(value);
            }
            putProperty(key, value);
        }
    }
}
