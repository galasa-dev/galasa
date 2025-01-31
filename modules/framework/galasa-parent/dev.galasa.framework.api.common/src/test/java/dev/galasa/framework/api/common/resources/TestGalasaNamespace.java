/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import org.junit.Test;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

import static org.assertj.core.api.Assertions.*;
import dev.galasa.framework.api.common.mocks.*;

public class TestGalasaNamespace {

    private String getExpectedJson(String expectedName,String expectedUrl,String visibility){
        return "{\n  \"name\": \""+expectedName+"\",\n  \"propertiesUrl\": \""+expectedUrl+"\",\n  \"type\": \""+visibility+"\"\n}";
    }

    @Test
    public void testGalasaNamespaceCreateNormalNamespaceFromCPSNamespace() throws ConfigurationPropertyStoreException{
        //Given...
        String expectedName = "NameSpace1";
        String expectedUrl = "/NameSpace1/properties";
        IConfigurationPropertyStoreService mockCPS = new MockIConfigurationPropertyStoreService();
        MockFramework mockFramework = new MockFramework(mockCPS);
        CPSNamespace cpsNamespace = new CPSNamespace(expectedName, Visibility.NORMAL, mockFramework);
        //When...
        GalasaNamespace namespace = new GalasaNamespace(cpsNamespace);
        //Then...
        String returnedJson = namespace.toJson();
        String expectedJson = getExpectedJson(expectedName, expectedUrl, "NORMAL");
        assertThat(returnedJson).isEqualTo(expectedJson);
        assertThat(namespace.getName()).isEqualTo(expectedName);
        assertThat(namespace.getUrl()).isEqualTo(expectedUrl);
        assertThat(namespace.getVisibility()).isEqualTo(Visibility.NORMAL);
    }

    @Test
    public void testGalasaNamespaceCreateNormalNamespaceFromJSONString() throws ConfigurationPropertyStoreException{
        //Given...
        String expectedName = "NameSpace1";
        String expectedUrl = "/NameSpace1/properties";
        String visibility = "NORMAL";
        String json = getExpectedJson(expectedName, expectedUrl, visibility);
        //When...
        GalasaNamespace namespace = new GalasaNamespace(json);
        //Then...
        String returnedJson = namespace.toJson();
        String expectedJson = getExpectedJson(expectedName, expectedUrl, "NORMAL");
        assertThat(returnedJson).isEqualTo(expectedJson);
        assertThat(namespace.getName()).isEqualTo(expectedName);
        assertThat(namespace.getUrl()).isEqualTo(expectedUrl);
        assertThat(namespace.getVisibility()).isEqualTo(Visibility.NORMAL);
    }

    @Test
    public void testGalasaNamespaceCreateSecureNamespaceFromCPSNamespace() throws ConfigurationPropertyStoreException{
        //Given...
        String expectedName = "NameSpace1";
        String expectedUrl = "/NameSpace1/properties";
        IConfigurationPropertyStoreService mockCPS = new MockIConfigurationPropertyStoreService();
        MockFramework mockFramework = new MockFramework(mockCPS);
        CPSNamespace cpsNamespace = new CPSNamespace(expectedName, Visibility.SECURE, mockFramework);
        //When...
        GalasaNamespace namespace = new GalasaNamespace(cpsNamespace);
        //Then...
        String returnedJson = namespace.toJson();
        String expectedJson = getExpectedJson(expectedName, expectedUrl, "SECURE");
        assertThat(returnedJson).isEqualTo(expectedJson);
        assertThat(namespace.getName()).isEqualTo(expectedName);
        assertThat(namespace.getUrl()).isEqualTo(expectedUrl);
        assertThat(namespace.getVisibility()).isEqualTo(Visibility.SECURE);
    }

    @Test
    public void testGalasaNamespaceCreateSecureNamespaceFromJSONString() throws ConfigurationPropertyStoreException{
        //Given...
        String expectedName = "NameSpace1";
        String expectedUrl = "/NameSpace1/properties";
        String visibility = "SECURE";
        String json = getExpectedJson(expectedName, expectedUrl, visibility);
        //When...
        GalasaNamespace namespace = new GalasaNamespace(json);
        //Then...
        String returnedJson = namespace.toJson();
        String expectedJson = getExpectedJson(expectedName, expectedUrl, "SECURE");
        assertThat(returnedJson).isEqualTo(expectedJson);
        assertThat(namespace.getName()).isEqualTo(expectedName);
        assertThat(namespace.getUrl()).isEqualTo(expectedUrl);
        assertThat(namespace.getVisibility()).isEqualTo(Visibility.SECURE);
    }
    
}
