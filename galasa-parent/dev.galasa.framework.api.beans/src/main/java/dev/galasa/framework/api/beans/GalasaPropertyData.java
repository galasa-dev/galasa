/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.beans;

public class GalasaPropertyData {
    private String value;

    public GalasaPropertyData (String propertyValue){
        this.value = propertyValue;
    }

    public String getValue() {
        return this.value;
    }
}