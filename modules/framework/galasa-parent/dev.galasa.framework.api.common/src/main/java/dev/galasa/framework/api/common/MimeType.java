/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import static dev.galasa.framework.api.common.WildcardMimeType.*;

public enum MimeType {
    APPLICATION_JSON("application/json"),
    APPLICATION_YAML("application/yaml"),
    TEXT_PLAIN("text/plain"),
    ;

    private final String type;
    private final String wildcardType;
    
    private MimeType(String type) {
        this(type, null);
    }
    
    private MimeType(String type, MimeType defaultType) {
        this.type = type;
        this.wildcardType = type.split("/")[0] + "/*";
    }

    public boolean matchesType(String acceptHeaderValue) {
        String type = acceptHeaderValue.trim();
        return type.equalsIgnoreCase(this.type) || type.equalsIgnoreCase(wildcardType) || type.equalsIgnoreCase(WILDCARD.toString());
    }

    @Override
    public String toString() {
        return type;
    }
}
