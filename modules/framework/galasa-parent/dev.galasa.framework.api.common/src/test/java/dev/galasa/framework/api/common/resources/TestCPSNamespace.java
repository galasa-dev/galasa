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

import java.util.Map;

public class TestCPSNamespace {

    @Test
    public void testNamespaceTypeNormalReturnOkNoURL() throws Exception {
        //Given...
        String expectedName = "NameSpace1";
        String expectedUrl = "/NameSpace1/properties";
        IConfigurationPropertyStoreService mockCPS = new MockIConfigurationPropertyStoreService();
        MockFramework mockFramework = new MockFramework(mockCPS);
        //When...
        CPSNamespace namespace = new CPSNamespace(expectedName, Visibility.NORMAL, mockFramework);
        //Then...
        String name = namespace.getName();
        Visibility visibility = namespace.getVisibility();
        String propertiesurl = namespace.getPropertiesUrl();
        assertThat(name).isEqualTo(expectedName);
        assertThat(visibility).isEqualTo(Visibility.NORMAL);
        assertThat(propertiesurl).isEqualTo(expectedUrl);
    }

    @Test
    public void testNamespaceTypeNormalReturnOkWithURL() throws ConfigurationPropertyStoreException{
        //Given...
        String expectedName = "NameSpace1";
        String expectedUrl = "/NameSpace1/properties";
        IConfigurationPropertyStoreService mockCPS = new MockIConfigurationPropertyStoreService();
        MockFramework mockFramework = new MockFramework(mockCPS);
        //When...
        CPSNamespace namespace = new CPSNamespace(expectedName, Visibility.NORMAL, mockFramework);
        //Then...
        String name = namespace.getName();
        Visibility visibility = namespace.getVisibility();
        String propertiesurl = namespace.getPropertiesUrl();
        assertThat(name).isEqualTo(expectedName);
        assertThat(visibility).isEqualTo(Visibility.NORMAL);
        assertThat(propertiesurl).isEqualTo(expectedUrl);
    }

    @Test
    public void testNamespaceTypeSecureReturnOkNoURL() throws Exception {
        //Given...
        String expectedName = "secure";
        String expectedUrl = "/secure/properties";
        IConfigurationPropertyStoreService mockCPS = new MockIConfigurationPropertyStoreService();
        MockFramework mockFramework = new MockFramework(mockCPS);
    
        //When...
        CPSNamespace namespace = new CPSNamespace(expectedName, Visibility.SECURE, mockFramework);
        //Then...
        String name = namespace.getName();
        Visibility visibility = namespace.getVisibility();
        String propertiesurl = namespace.getPropertiesUrl();
        assertThat(name).isEqualTo(expectedName);
        assertThat(visibility).isEqualTo(Visibility.SECURE);
        assertThat(propertiesurl).isEqualTo(expectedUrl);
    }

    @Test
    public void testNamespaceTypeSecureReturnOkWithURL() throws Exception {
        //Given...
        String expectedName = "secure";
        String expectedUrl = "/secure/properties";
        IConfigurationPropertyStoreService mockCPS = new MockIConfigurationPropertyStoreService();
        MockFramework mockFramework = new MockFramework(mockCPS);
        //When...
        CPSNamespace namespace = new CPSNamespace(expectedName, Visibility.SECURE, mockFramework);
        //Then...
        String name = namespace.getName();
        Visibility visibility = namespace.getVisibility();
        String propertiesurl = namespace.getPropertiesUrl();
        assertThat(name).isEqualTo(expectedName);
        assertThat(visibility).isEqualTo(Visibility.SECURE);
        assertThat(propertiesurl).isEqualTo(expectedUrl);
    }

    @Test
    public void testSecureNamespaceSaysItsSecure() throws Exception {
        IConfigurationPropertyStoreService mockCPS = new MockIConfigurationPropertyStoreService();
        MockFramework mockFramework = new MockFramework(mockCPS);
        CPSNamespace namespace = new CPSNamespace("secure", Visibility.SECURE, mockFramework);
        assertThat(namespace.isSecure()).isTrue();
    }


    @Test
    public void testSecureNamespaceSaysItsNotHidden() throws Exception {
        IConfigurationPropertyStoreService mockCPS = new MockIConfigurationPropertyStoreService();
        MockFramework mockFramework = new MockFramework(mockCPS);
        CPSNamespace namespace = new CPSNamespace("secure", Visibility.SECURE, mockFramework);
        assertThat(namespace.isHidden()).isFalse();
    }

    @Test
    public void testDssNamespaceSaysItsHidden() throws Exception {
        IConfigurationPropertyStoreService mockCPS = new MockIConfigurationPropertyStoreService();
        MockFramework mockFramework = new MockFramework(mockCPS);
        CPSNamespace namespace = new CPSNamespace("dss", Visibility.HIDDEN, mockFramework);
        assertThat(namespace.isHidden()).isTrue();
    }

    @Test
    public void testDssNamespaceSaysItsNotSecure() throws Exception {
        IConfigurationPropertyStoreService mockCPS = new MockIConfigurationPropertyStoreService();
        MockFramework mockFramework = new MockFramework(mockCPS);
        CPSNamespace namespace = new CPSNamespace("dss", Visibility.HIDDEN, mockFramework);
        assertThat(namespace.isSecure()).isFalse();
    }

    @Test
    public void testNamespaceGivesPropertiesFromCPSServiceNormalNamespaceReturnsValue() throws Exception {
        //Given...
        IConfigurationPropertyStoreService mockCPS = new MockIConfigurationPropertyStoreService("myNamespace");
        String propertyName = "myNamespace.my.property.a";
        String propertyValue = "myValue";
        mockCPS.setProperty(propertyName, propertyValue);
        MockFramework mockFramework = new MockFramework(mockCPS);
        CPSNamespace namespace = new CPSNamespace("myNamespace", Visibility.NORMAL,mockFramework);

        //When...
        Map<GalasaPropertyName,CPSProperty> properties = namespace.getProperties();

        //Then...
        Visibility visibility = namespace.getVisibility();
        assertThat(properties.size()).isEqualTo(6);
        assertThat(visibility).isEqualTo(Visibility.NORMAL);
    }

    @Test
    public void testNamespaceGivesPropertyFromCPSServiceNormalNamespaceReturnsValue() throws Exception {
        //Given...
        IConfigurationPropertyStoreService mockCPS = new MockIConfigurationPropertyStoreService("myNamespace");
        String fullPropertyName = "myNamespace.my.property.a";
        String propertyName = "my.property.a";
        String propertyValue = "myValue";
        mockCPS.setProperty(fullPropertyName, propertyValue);
        MockFramework mockFramework = new MockFramework(mockCPS);
        CPSNamespace namespace = new CPSNamespace("myNamespace", Visibility.NORMAL,mockFramework);

        //When...
        CPSProperty property = namespace.getProperty(propertyName);

        //Then...
        Visibility visibility = namespace.getVisibility();
        assertThat(property.getNamespace()).isEqualTo("myNamespace");
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        assertThat(visibility).isEqualTo(Visibility.NORMAL);
    }

    @Test
    public void testNamespaceGivesPropertiesFromCPSServiceSecureNamespaceReturnsRedactedValue() throws Exception {
        //Given...
        IConfigurationPropertyStoreService mockCPS = new MockIConfigurationPropertyStoreService();
        MockFramework mockFramework = new MockFramework(mockCPS);
        String propertyName = "myNamespace.my.property.a";
        String propertyValue = "myValue";
        mockCPS.setProperty(propertyName, propertyValue);
        CPSNamespace namespace = new CPSNamespace("myNamespace", Visibility.SECURE,mockFramework);

        //When...
        Map<GalasaPropertyName,CPSProperty> properties = namespace.getProperties();

        //Then...

        Visibility visibility = namespace.getVisibility();
        assertThat(properties.size()).isEqualTo(6);
        assertThat(visibility).isEqualTo(Visibility.SECURE);
    }

    @Test
    public void testNamespaceGivesPropertiesFromCPSServiceSecureNamespaceReturnsNull() throws Exception {
        //Given...
        IConfigurationPropertyStoreService mockCPS = new MockIConfigurationPropertyStoreService();
        String propertyName = "myNamespace.my.property.a";
        String propertyValue = "myValue";
        mockCPS.setProperty(propertyName, propertyValue);
        GalasaPropertyName propName = new GalasaPropertyName(propertyName);
        MockFramework mockFramework = new MockFramework(mockCPS);
        CPSNamespace namespace = new CPSNamespace("myNamespace", Visibility.HIDDEN,mockFramework);

        //When...
        Map<GalasaPropertyName,CPSProperty> properties = namespace.getProperties();

        //Then...
        CPSProperty returnedProperty = properties.get(propName);
        Visibility visibility = namespace.getVisibility();
        assertThat(returnedProperty).isNull();
        assertThat(visibility).isEqualTo(Visibility.HIDDEN);
    }
}
