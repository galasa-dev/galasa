/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import dev.galasa.framework.spi.utils.GalasaGson;

public class GalasaNamespace {

    private String name;
    private String propertiesUrl;
    private Visibility type;

    private static final GalasaGson gson = new GalasaGson();

    public GalasaNamespace(CPSNamespace namespace){
        this.name = namespace.getName();
        this.propertiesUrl = namespace.getPropertiesUrl();
        this.type = namespace.getVisibility();
    }

    public GalasaNamespace(String namespace){
        GalasaNamespace newNamespace = gson.fromJson(namespace, this.getClass());
        this.name = newNamespace.getName();
        this.propertiesUrl = newNamespace.getUrl();
        this.type = newNamespace.getVisibility();
    }
    
    public String getName(){
        return name;
    }

    public String getUrl(){
        return propertiesUrl;
    }

    public Visibility getVisibility(){
        return type;
    }

    public String toJson(){
        return gson.toJson(this);
    }
}
