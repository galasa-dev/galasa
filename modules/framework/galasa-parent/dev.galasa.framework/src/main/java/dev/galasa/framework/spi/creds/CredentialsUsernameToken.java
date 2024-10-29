/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.creds;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.crypto.spec.SecretKeySpec;

import dev.galasa.ICredentialsUsernameToken;

public class CredentialsUsernameToken extends Credentials implements ICredentialsUsernameToken {
    private String username;
    private byte[] token;

    public CredentialsUsernameToken(String plainTextUsername, String encryptedToken) {
        this.username = plainTextUsername;
        this.token = encryptedToken.getBytes();
    }

    public CredentialsUsernameToken(SecretKeySpec key, String username, String token) throws CredentialsException {
        super(key);

        this.username = decryptToString(username);
        if (this.username == null) {
            this.username = new String(decode(username), StandardCharsets.UTF_8);
        }

        String decryptedToken = decryptToString(token);
        if (decryptedToken == null) {
            this.token = decode(token);
        } else {
            this.token = decryptedToken.getBytes();
        }
    }

    public String getUsername() {
        return username;
    }

    public byte[] getToken() {
        return token;
    }

    @Override
    public Properties toProperties(String credentialsId) {
        Properties credsProperties = new Properties();
        credsProperties.setProperty("secure.credentials." + credentialsId + ".username" , this.username);
        credsProperties.setProperty("secure.credentials." + credentialsId + ".token" , new String(this.token));
        return credsProperties;
    }
}
