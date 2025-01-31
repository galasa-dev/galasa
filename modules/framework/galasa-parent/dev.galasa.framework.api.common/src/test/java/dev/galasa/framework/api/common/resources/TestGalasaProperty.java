/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import org.junit.Test;

import dev.galasa.framework.api.beans.GalasaProperty;
import dev.galasa.framework.api.common.InternalServletException;

import static org.assertj.core.api.Assertions.*;

public class TestGalasaProperty {
    
    private boolean isPropertyNameValid(GalasaProperty property) {
        boolean valid;
        valid = property.getName() != null && !property.getName().isBlank();
        if(valid){
            valid = property.getName().split("[.]").length >=2;
        }
        return valid;
    }

    private boolean isPropertyNameSpaceValid(GalasaProperty property) {
        return property.getNamespace() != null && !property.getNamespace().isBlank();
    }

    private boolean isPropertyValueValid(GalasaProperty property) {
        return property.getValue() != null && !property.getValue().isBlank();
    }

    private boolean isPropertyApiVersionValid(GalasaProperty property) {
        return property.getApiVersion() != null && !property.getApiVersion().isBlank();
    }

    private boolean isPropertyValid(GalasaProperty property) throws InternalServletException {
        return isPropertyApiVersionValid(property) && isPropertyNameSpaceValid(property) 
                && isPropertyNameValid(property) && isPropertyValueValid(property);
    }

    
    @Test
    public void testGalasaPropertyDefaultApiVersion() throws InternalServletException{
        //Given...
        String namespace = "mynamespace";
        String propertyName = "new.property.name";
        String propertyValue = "randomValue123";
        
        //When...
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue);
        
        //Then...
        assertThat(property.getKind()).isEqualTo("GalasaProperty");
        assertThat(property.getApiVersion()).isEqualTo("galasa-dev/v1alpha1");
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        assertThat(isPropertyValid(property)).isTrue();
    }

    @Test
    public void testGalasaPropertyCustomApiVersion() throws InternalServletException{
        //Given...
        String apiVersion = "randomApi";
        String namespace = "randomnamespace";
        String propertyName = "random.property.name";
        String propertyValue = "randomValue123";
        
        //When...
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue, apiVersion);
        
        //Then...
        assertThat(property.getKind()).isEqualTo("GalasaProperty");
        assertThat(property.getApiVersion()).isEqualTo(apiVersion);
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        assertThat(isPropertyValid(property)).isTrue();
    }

    @Test
    public void testGalasaPropertyNoDataIsInvalid() throws InternalServletException{
        //Given...
        String apiVersion = null;
        String namespace = null;
        String propertyName = null;
        String propertyValue = null;
        
        //When...
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue, apiVersion);
        

        //Then...
        assertThat(property.getKind()).isEqualTo("GalasaProperty");
        assertThat(property.getApiVersion()).isEqualTo(null);
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        assertThat(isPropertyValid(property)).isFalse();
    }

    @Test
    public void testGalasaPropertyNoDataDefaultApiVersionIsInvalid() throws InternalServletException{
        //Given...
        String namespace = null;
        String propertyName = null;
        String propertyValue = null;
        
        //When...
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.getKind()).isEqualTo("GalasaProperty");
        assertThat(property.getApiVersion()).isEqualTo("galasa-dev/v1alpha1");
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        assertThat(isPropertyValid(property)).isFalse();
    }

    @Test
    public void testGalasaPropertyNamespaceOnlyIsInvalid() throws InternalServletException{
        //Given...
        String namespace = "framework";
        String propertyName = null;
        String propertyValue = null;
        
        //When...
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.getKind()).isEqualTo("GalasaProperty");
        assertThat(property.getApiVersion()).isEqualTo("galasa-dev/v1alpha1");
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        assertThat(isPropertyValid(property)).isFalse();
    }

    @Test
    public void testGalasaPropertyPartialDataIsInvalid() throws InternalServletException{
        //Given...
        String namespace = "framework";
        String propertyName = "property";
        String propertyValue = null;
        
        //When...
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.getKind()).isEqualTo("GalasaProperty");
        assertThat(property.getApiVersion()).isEqualTo("galasa-dev/v1alpha1");
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        assertThat(isPropertyValid(property)).isFalse();
    }

    @Test
    public void testGalasaPropertyNoNamespaceIsInvalid() throws InternalServletException{
        //Given...
        String namespace = null;
        String propertyName = "property";
        String propertyValue = "value";
        
        //When...
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue);
        
        //Then...
        assertThat(property.getKind()).isEqualTo("GalasaProperty");
        assertThat(property.getApiVersion()).isEqualTo("galasa-dev/v1alpha1");
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        assertThat(isPropertyValid(property)).isFalse();
    }

    @Test
    public void testGalasaPropertyNoNameIsInvalid() throws InternalServletException{
        //Given...
        String namespace = "framework";
        String propertyName = "";
        String propertyValue = "value";
        
        //When...
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue);
        
        //Then...
        assertThat(property.getKind()).isEqualTo("GalasaProperty");
        assertThat(property.getApiVersion()).isEqualTo("galasa-dev/v1alpha1");
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        assertThat(isPropertyValid(property)).isFalse();
    }

    @Test
    public void testGalasaPropertyNoValueIsInvalid() throws InternalServletException{
        //Given...
        String namespace = "framework";
        String propertyName = "property";
        String propertyValue = "";
        
        //When...
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue);     

        //Then...
        assertThat(property.getKind()).isEqualTo("GalasaProperty");
        assertThat(property.getApiVersion()).isEqualTo("galasa-dev/v1alpha1");
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        assertThat(isPropertyValid(property)).isFalse();
    }
}
