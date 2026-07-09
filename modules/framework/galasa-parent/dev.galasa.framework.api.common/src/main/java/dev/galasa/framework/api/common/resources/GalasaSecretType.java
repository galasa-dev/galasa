 /*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

public enum GalasaSecretType {
    USERNAME_PASSWORD("UsernamePassword", new String[]{ "username", "password" }, null),
    USERNAME_TOKEN("UsernameToken", new String[]{ "username", "token" }, null),
    USERNAME("Username", new String[]{ "username" }, null),
    TOKEN("Token", new String[]{ "token" }, null),
    KEYSTORE("KeyStore", new String[]{ "keystore", "keystoreType" }, new String[]{ "keystorePassword", "password"});

    private String name;
    private String[] requiredDataFields;
    private String[] optionalDataFields;

    private GalasaSecretType(String type, String[] requiredDataFields, String[] optionalDataFields) {
        this.name = type;
        this.requiredDataFields = requiredDataFields;
        this.optionalDataFields = optionalDataFields;
    }

    public static GalasaSecretType getFromString(String typeAsString) {
        GalasaSecretType match = null;
        for (GalasaSecretType resource : values()) {
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

    public String[] getRequiredDataFields() {
        return requiredDataFields;
    }

    public String[] getOptionalDataFields() {
        return optionalDataFields;
    }
}