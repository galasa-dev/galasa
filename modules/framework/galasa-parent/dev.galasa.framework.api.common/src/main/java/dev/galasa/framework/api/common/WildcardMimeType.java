/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

public enum WildcardMimeType {
    APPLICATION_WILDCARD("application/*", MimeType.APPLICATION_JSON),
    TEXT_WILDCARD("text/*", MimeType.TEXT_PLAIN),
    WILDCARD("*/*", MimeType.APPLICATION_JSON),
    ;

    private final String type;
    private final MimeType defaultSubtype;

    private WildcardMimeType(String type, MimeType defaultSubtype) {
        this.type = type;
        this.defaultSubtype = defaultSubtype;
    }

    public static WildcardMimeType getFromString(String typeAsString) {
        WildcardMimeType match = null;
        for (WildcardMimeType type : values()) {
            if (type.toString().equalsIgnoreCase(typeAsString.trim())) {
                match = type;
                break;
            }
        }
        return match;
    }

    public String getDefaultSubtype() {
        return defaultSubtype.toString();
    }

    @Override
    public String toString() {
        return type;
    }
}
