/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import org.junit.Test;

import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import javax.validation.constraints.NotNull;

public class TestCPSProperty extends BaseServletTest {
    
    private class MockICPSServiceWithError extends MockIConfigurationPropertyStoreService {
        protected MockICPSServiceWithError(String namespace){
            super.namespaceInput= namespace;
        }
        
        @Override
        public void deleteProperty(@NotNull String name) throws ConfigurationPropertyStoreException {
            throw new ConfigurationPropertyStoreException("Could not Delete Key");
        }
    }

    @Test
    public void TestCPSPropertyDefaultApiVersion() throws InternalServletException{
        //Given...
        String namespace = "mynamespace";
        String propertyName = "new.property.name";
        String propertyValue = "randomValue123";
        
        //When...
        CPSProperty property = new CPSProperty(namespace, propertyName, propertyValue);
        
        //Then...
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        assertThat(property.isPropertyValid()).isTrue();
    }

    @Test
    public void TestCPSPropertyFromString() throws InternalServletException{
        //Given...
        String namespace = "mynamespace";
        String propertyName = "new.property.name";
        String propertyValue = "randomValue123";
        String fullPropertyName = namespace+"."+propertyName;
        
        //When...
        CPSProperty property = new CPSProperty(fullPropertyName, propertyValue);
        
        //Then...
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        assertThat(property.isPropertyValid()).isTrue();
    }

    @Test
    public void TestCPSPropertyFromMapEntry() throws InternalServletException{
        //Given...
        String namespace = "mynamespace";
        String propertyName = "new.property.name";
        String propertyValue = "randomValue123";
        String fullPropertyName = namespace+"."+propertyName;
        Map.Entry<String, String> entry = Map.entry(fullPropertyName, propertyValue);
        
        //When...
        CPSProperty property = new CPSProperty(entry);
        
        //Then...
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        assertThat(property.isPropertyValid()).isTrue();
    }

    @Test
    public void TestCPSPropertyCustomApiVersion() throws InternalServletException{
        //Given...
        String namespace = "randomnamespace";
        String propertyName = "random.property.name";
        String propertyValue = "randomValue123";
        
        //When...
        CPSProperty property = new CPSProperty(namespace, propertyName, propertyValue);
        
        //Then...
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        assertThat(property.isPropertyValid()).isTrue();
    }

    @Test
    public void TestCPSPropertyNoDataIsInvalid() throws InternalServletException{
        //Given...
        String namespace = null;
        String propertyName = null;
        String propertyValue = null;
        
        //When...
        CPSProperty property = new CPSProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        Throwable thrown = catchThrowable( () -> {
            property.isPropertyValid();
        });

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5024","value");
    }

    @Test
    public void TestCPSPropertyNoDataDefaultApiVersionIsInvalid() throws InternalServletException{
        //Given...
        String namespace = null;
        String propertyName = null;
        String propertyValue = null;
        
        //When...
        CPSProperty property = new CPSProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        Throwable thrown = catchThrowable( () -> {
            property.isPropertyValid();
        });

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5024","value");
    }

    @Test
    public void TestCPSPropertyNamespaceOnlyIsInvalid() throws InternalServletException{
        //Given...
        String namespace = "framework";
        String propertyName = null;
        String propertyValue = null;
        
        //When...
        CPSProperty property = new CPSProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        Throwable thrown = catchThrowable( () -> {
            property.isPropertyValid();
        });

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5024","value");
    }

    @Test
    public void TestCPSPropertyPartialDataIsInvalid() throws InternalServletException{
        //Given...
        String namespace = "framework";
        String propertyName = "property";
        String propertyValue = null;
        
        //When...
        CPSProperty property = new CPSProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        Throwable thrown = catchThrowable( () -> {
            property.isPropertyValid();
        });

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5024","value");
    }

    @Test
    public void TestCPSPropertyNoNamespaceIsInvalid() throws InternalServletException{
        //Given...
        String namespace = null;
        String propertyName = "property";
        String propertyValue = "value";
        
        //When...
        CPSProperty property = new CPSProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        Throwable thrown = catchThrowable( () -> {
            property.isPropertyValid();
        });

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5024","namespace");
    }

    @Test
    public void TestCPSPropertyNoNameIsInvalid() throws InternalServletException{
        //Given...
        String namespace = "framework";
        String propertyName = "";
        String propertyValue = "value";
        
        //When...
        CPSProperty property = new CPSProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        Throwable thrown = catchThrowable( () -> {
            property.isPropertyValid();
        });

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5024","name");
    }

    @Test
    public void TestCPSPropertyNoValueIsInvalid() throws InternalServletException{
        //Given...
        String namespace = "framework";
        String propertyName = "property";
        String propertyValue = "";
        
        //When...
        CPSProperty property = new CPSProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        Throwable thrown = catchThrowable( () -> {
            property.isPropertyValid();
        });

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5024","value");
    }

    @Test
    public void TestGetOutputValueFromNormalNamespaceReturnsNormalValue() throws ConfigurationPropertyStoreException{
        //Given...
        String propertyNamespace = "random";
        String propertyName = "property.name";
        String propertyValue = "randomValue123";
        IConfigurationPropertyStoreService mockCPS = new MockIConfigurationPropertyStoreService(propertyNamespace);
        MockFramework mockFramework = new MockFramework(mockCPS);
        mockCPS.setProperty(propertyNamespace+"."+propertyName, propertyValue);
        //check that the property has been set
        assertThat(mockCPS.getProperty("property","name")).isNotNull();
        GalasaPropertyName galasaPropertyName = new GalasaPropertyName(propertyNamespace, propertyName);
        CPSNamespace namespace = new CPSNamespace(propertyNamespace, Visibility.NORMAL, mockFramework);
        CPSProperty property = new CPSProperty(mockCPS, namespace, galasaPropertyName, propertyValue);

        //When...
        String outputValue = property.getPossiblyRedactedValue();

        //Then...
        assertThat(outputValue.equals(propertyValue)).isTrue();
    }

    @Test
    public void TestGetOutputValueFromSecureNamespaceReturnsRedactedValue() throws ConfigurationPropertyStoreException{
        //Given...
        String propertyNamespace = "secure";
        String propertyName = "property.name";
        String propertyValue = "randomValue123";
        IConfigurationPropertyStoreService mockCPS = new MockIConfigurationPropertyStoreService(propertyNamespace);
        MockFramework mockFramework = new MockFramework(mockCPS);
        mockCPS.setProperty(propertyNamespace+"."+propertyName, propertyValue);
        //check that the property has been set
        assertThat(mockCPS.getProperty("property","name")).isNotNull();
        GalasaPropertyName galasaPropertyName = new GalasaPropertyName(propertyNamespace, propertyName);
        CPSNamespace namespace = new CPSNamespace(propertyNamespace, Visibility.SECURE, mockFramework);
        CPSProperty property = new CPSProperty(mockCPS, namespace, galasaPropertyName, propertyValue);

        //When...
        String outputValue = property.getPossiblyRedactedValue();

        //Then...
        assertThat(outputValue.equals("********")).isTrue();
    }

    @Test
    public void TestDeletePropertyFromStoreReturnsOk() throws InternalServletException, ConfigurationPropertyStoreException{
        //Given...
        String propertyNamespace = "random";
        String propertyName = "property.name";
        String propertyValue = "randomValue123";
        IConfigurationPropertyStoreService mockCPS = new MockIConfigurationPropertyStoreService(propertyNamespace);
        MockFramework mockFramework = new MockFramework(mockCPS);
        mockCPS.setProperty(propertyNamespace+"."+propertyName, propertyValue);
        //check that the property has been set
        assertThat(mockCPS.getProperty("property","name")).isNotNull();
        GalasaPropertyName galasaPropertyName = new GalasaPropertyName(propertyNamespace, propertyName);
        CPSNamespace namespace = new CPSNamespace(propertyNamespace, Visibility.NORMAL, mockFramework);
        CPSProperty property = new CPSProperty(mockCPS, namespace, galasaPropertyName, propertyValue);

        //When...
        property.deletePropertyFromStore();

        //Then...
        assertThat(mockCPS.getProperty("random.property","property.name")).isNull();
    }

    @Test
    public void TestDeletePropertyFromStoreInvalidNameReturnsError() throws Exception{
        //Given...
        String propertyNamespace = "random";
        String propertyName = "property.name";
        String propertyValue = "randomValue123";
        IConfigurationPropertyStoreService mockCPS = new MockICPSServiceWithError(propertyNamespace);
        MockFramework mockFramework = new MockFramework(mockCPS);
        mockCPS.setProperty(propertyNamespace+"."+propertyName, propertyValue);
        //check that the property has been set
        assertThat(mockCPS.getProperty("property","name")).isNotNull();
        GalasaPropertyName galasaPropertyName = new GalasaPropertyName(propertyNamespace, "properly.name");
        CPSNamespace namespace = new CPSNamespace(propertyNamespace, Visibility.NORMAL, mockFramework);
        CPSProperty property = new CPSProperty(mockCPS, namespace, galasaPropertyName, propertyValue);

        //When...
        Throwable thrown = catchThrowable( () -> {
            property.deletePropertyFromStore();
        });

        //Then...
        assertThat(thrown).isNotNull();
        checkErrorStructure(thrown.getMessage(),5030,
            "GAL5030E: Error occurred when trying to delete Property 'properly.name'.",
            "Report the problem to your Galasa Ecosystem owner.");        
    }

    @Test
    public void TestDeletePropertyFromStoreEmptyValueReturnsOk() throws InternalServletException, ConfigurationPropertyStoreException{
        //Given...
        String propertyNamespace = "random";
        String propertyName = "property.name";
        String propertyValue = "";
        IConfigurationPropertyStoreService mockCPS = new MockIConfigurationPropertyStoreService(propertyNamespace);
        MockFramework mockFramework = new MockFramework(mockCPS);
        mockCPS.setProperty(propertyNamespace+"."+propertyName, propertyValue);
        //check that the property has been set
        assertThat(mockCPS.getProperty("property","name")).isNotNull();
        GalasaPropertyName galasaPropertyName = new GalasaPropertyName(propertyNamespace, propertyName);
        CPSNamespace namespace = new CPSNamespace(propertyNamespace, Visibility.NORMAL, mockFramework);
        CPSProperty property = new CPSProperty(mockCPS, namespace, galasaPropertyName, propertyValue);

        //When...
        property.deletePropertyFromStore();

        //Then...
        assertThat(mockCPS.getProperty("random.property","property.name")).isNull();
    }

    @Test
    public void TestDeletePropertyFromStoreInvalidNamespaceReturnsError() throws Exception{
        //Given...
        String invalidNamespace = "random";
        String propertyName = "property.name";
        String propertyValue = "randomValue123";
        IConfigurationPropertyStoreService mockCPS = new MockICPSServiceWithError(invalidNamespace);
        MockFramework mockFramework = new MockFramework(mockCPS);
        mockCPS.setProperty("validNamespace."+propertyName, propertyValue);
        //check that the property has been set
        assertThat(mockCPS.getProperty("property","name")).isNotNull();
        GalasaPropertyName galasaPropertyName = new GalasaPropertyName(invalidNamespace, propertyName);
        CPSNamespace namespace = new CPSNamespace(invalidNamespace, Visibility.NORMAL, mockFramework);
        CPSProperty property = new CPSProperty(mockCPS, namespace, galasaPropertyName, propertyValue);

        //When...
        Throwable thrown = catchThrowable( () -> {
            property.deletePropertyFromStore();
        });

        //Then...
        assertThat(thrown).isNotNull();
        checkErrorStructure(thrown.getMessage(),5030,
            "GAL5030E: Error occurred when trying to delete Property 'property.name'.",
            "Report the problem to your Galasa Ecosystem owner.");        
    }


    @Test
    public void TestDeletePropertyFromStoreSecureNamespaceReturnsOk() throws Exception{
        //Given...
        String propertyNamespace = "secure";
        String propertyName = "property.name";
        String propertyValue = "randomValue123";
        IConfigurationPropertyStoreService mockCPS = new MockIConfigurationPropertyStoreService(propertyNamespace);
        MockFramework mockFramework = new MockFramework(mockCPS);
        mockCPS.setProperty(propertyNamespace+"."+propertyName, propertyValue);
        //check that the property has been set
        assertThat(mockCPS.getProperty("property","name")).isNotNull();
        GalasaPropertyName galasaPropertyName = new GalasaPropertyName(propertyNamespace, propertyName);
        CPSNamespace namespace = new CPSNamespace(propertyNamespace, Visibility.SECURE, mockFramework);
        CPSProperty property = new CPSProperty(mockCPS, namespace, galasaPropertyName, propertyValue);

        //When...
        property.deletePropertyFromStore();

        //Then...
        assertThat(mockCPS.getProperty("secure.property","name")).isNull();
    }
}
