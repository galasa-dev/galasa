/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.creds;

import java.nio.file.Paths;
import java.util.List;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.spi.Environment;

public class EncryptionKeys {
    private String encryptionKey;
    private List<String> fallbackDecryptionKeys;

    public EncryptionKeys() {
        // No-op constructor
    }

    public EncryptionKeys(IFileSystem fileSystem, Environment environment) throws CredentialsException {
        EncryptionKeys parsedEncryptionKeys = parseEncryptionKeysFile(fileSystem, environment);
        this.encryptionKey = parsedEncryptionKeys.getEncryptionKey();
        this.fallbackDecryptionKeys = parsedEncryptionKeys.getFallbackDecryptionKeys();
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public List<String> getFallbackDecryptionKeys() {
        return fallbackDecryptionKeys;
    }

    public void setFallbackDecryptionKeys(List<String> fallbackDecryptionKeys) {
        this.fallbackDecryptionKeys = fallbackDecryptionKeys;
    }

    /**
     * Parses a YAML file containing a primary encryption key and a list of fallback decryption keys, and returns
     * the keys parsed into an EncryptionKeys bean.
     *
     * All keys in the YAML file must be base64-encoded in order for the keys to be properly decoded.
     * The YAML format for the encryption keys is as follows:
     *
     * encryptionKey: <current-base64-encoded-key>
     * fallbackDecryptionKeys:
     * - <encoded-fallback-key-1>
     * - <encoded-fallback-key-2>
     *
     * @return a bean object representing the parsed encryption keys
     * @throws CredentialsException if there was an error parsing the encryption keys YAML file
     */
    private EncryptionKeys parseEncryptionKeysFile(IFileSystem fileSystem, Environment environment) throws CredentialsException {
        EncryptionKeys encryptionKeys = this;
        String encryptionKeysLocation = environment.getenv(FrameworkEncryptionService.ENCRYPTION_KEYS_PATH_ENV);
        if (encryptionKeysLocation != null) {
            try {
                String encryptionKeysYamlStr = fileSystem.readString(Paths.get(encryptionKeysLocation));
                if (encryptionKeysYamlStr != null && !encryptionKeysYamlStr.isBlank()) {
                    Yaml yamlParser = new Yaml(new Constructor(EncryptionKeys.class, new LoaderOptions()));
                    encryptionKeys = yamlParser.load(encryptionKeysYamlStr);
                }
            } catch (Exception e) {
                throw new CredentialsException("Failed to read encryption keys file", e);
            }
        }

        return encryptionKeys;
    }
}
