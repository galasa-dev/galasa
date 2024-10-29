/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.beans;

public class GalasaProperty {

    private final String kind = "GalasaProperty";
    private GalasaPropertyMetadata metadata ;
    private GalasaPropertyData data;

    public static final String DEFAULTAPIVERSION = "galasa-dev/v1alpha1";
    private String apiVersion = DEFAULTAPIVERSION;


    public GalasaProperty( GalasaPropertyMetadata metadata, GalasaPropertyData data ) {
        this.metadata = metadata ;
        this.data = data ;
    }

    public GalasaProperty (String namespace, String propertyName, String propertyValue) {
        this.metadata = new GalasaPropertyMetadata(namespace, propertyName);
        this.data = new GalasaPropertyData(propertyValue);
    }

    public GalasaProperty (String namespace, String propertyName, String propertyValue, String apiVersion) {
        this(namespace, propertyName, propertyValue);
        this.apiVersion = apiVersion;
    }

    public GalasaPropertyMetadata getMetadata() {
        return this.metadata;
    }

    public String getKind() {
        return this.kind;
    }

    public String getApiVersion() {
        return this.apiVersion;
    }

    public String getNamespace() {
        return this.getMetadata().getNamespace();
    }

    public String getName() {
        return this.getMetadata().getName();
    }

    public GalasaPropertyData getData() {
        return this.data;
    }

    public String getValue() {
        return this.getData().getValue();
    }

}
