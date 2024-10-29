/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.creds;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.crypto.spec.SecretKeySpec;

import dev.galasa.ICredentialsUsernamePassword;

public class CredentialsUsernamePassword extends Credentials implements ICredentialsUsernamePassword {
    private String username;
    private String password;

    public CredentialsUsernamePassword(String plainTextUsername, String plainTextPassword) {
        this.username = plainTextUsername;
        this.password = plainTextPassword;
    }

    public CredentialsUsernamePassword(SecretKeySpec key, String username, String password)
            throws CredentialsException {
        super(key);

        this.username = decryptToString(username);
        this.password = decryptToString(password);

        if (this.username == null) {
            this.username = new String(decode(username), StandardCharsets.UTF_8);
        }

        if (this.password == null) {
            this.password = new String(decode(password), StandardCharsets.UTF_8);
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public Properties toProperties(String credentialsId) {
        Properties credsProperties = new Properties();
        credsProperties.setProperty("secure.credentials." + credentialsId + ".username" , this.username);
        credsProperties.setProperty("secure.credentials." + credentialsId + ".password" , this.password);
        return credsProperties;
    }
}
