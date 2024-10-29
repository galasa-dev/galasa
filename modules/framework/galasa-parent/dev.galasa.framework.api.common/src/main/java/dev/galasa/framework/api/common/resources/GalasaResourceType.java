 /*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;
 
public enum GalasaResourceType {
    GALASA_PROPERTY("GalasaProperty"),
    GALASA_SECRET("GalasaSecret");

    private String name;

    private GalasaResourceType(String name) {
        this.name = name;
    }

    public static GalasaResourceType getFromString(String resourceAsString) {
        GalasaResourceType match = null;
        for (GalasaResourceType resource : values()) {
            if (resource.toString().equalsIgnoreCase(resourceAsString.trim())) {
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