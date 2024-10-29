/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.creds;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.crypto.spec.SecretKeySpec;

import dev.galasa.ICredentialsUsername;

public class CredentialsUsername extends Credentials implements ICredentialsUsername {
    private String username;

    public CredentialsUsername(String plainTextUsername) {
        this.username = plainTextUsername;
    }

    public CredentialsUsername(SecretKeySpec key, String username) throws CredentialsException {
        super(key);

        this.username = decryptToString(username);

        if (this.username == null) {
            this.username = new String(decode(username), StandardCharsets.UTF_8);
        }
    }

    public String getUsername() {
        return username;
    }

    @Override
    public Properties toProperties(String credentialsId) {
        Properties credsProperties = new Properties();
        credsProperties.setProperty("secure.credentials." + credentialsId + ".username" , this.username);
        return credsProperties;
    }

}
