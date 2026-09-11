/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import dev.galasa.framework.api.common.resources.Visibility;

public enum BuiltInCPSNamespace {
    
    DEX("dex", Visibility.HIDDEN),
    DSS("dss", Visibility.HIDDEN),
    FRAMEWORK("framework", Visibility.NORMAL),
    SECURE("secure", Visibility.SECURE),
    SERVICE("service", Visibility.NORMAL);

    private String name;
    private Visibility visibility;

    private BuiltInCPSNamespace(String name, Visibility visibility) {
        this.name = name;
        this.visibility = visibility;
    }

    public static BuiltInCPSNamespace getFromString(String namespaceStr) {
        BuiltInCPSNamespace match = null;
        for (BuiltInCPSNamespace namespace : values()) {
            if (namespace.toString().equalsIgnoreCase(namespaceStr.trim())) {
                match = namespace;
                break;
            }
        }
        return match;
    }

    public Visibility getVisibility() {
        return this.visibility;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
