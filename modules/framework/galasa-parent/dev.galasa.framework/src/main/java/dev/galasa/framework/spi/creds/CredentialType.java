 /*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.creds;

public enum CredentialType {
    USERNAME_PASSWORD("UsernamePassword"),
    USERNAME_TOKEN("UsernameToken"),
    USERNAME("Username"),
    TOKEN("Token"),
    KEYSTORE("KeyStore");

    private String name;

    private CredentialType(String type) {
        this.name = type;
    }

    public static CredentialType getFromString(String typeAsString) {
        CredentialType match = null;
        for (CredentialType resource : values()) {
            if (resource.toString().equalsIgnoreCase(typeAsString.trim())) {
                match = resource;
                break;
            }
        }
        return match;
    }

    @Override
    public String toString() {
        return name;
    }
}
