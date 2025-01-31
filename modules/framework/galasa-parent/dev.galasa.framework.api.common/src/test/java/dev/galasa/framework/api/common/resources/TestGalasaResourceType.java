/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class TestGalasaResourceType {

    @Test
    public void testResourceTypeGalasaPropertyReturnGalasaProperty(){
        //Given...
        GalasaResourceType resourceType = GalasaResourceType.GALASA_PROPERTY;
        //When...
        String typeValue = resourceType.toString();
        //Then...
        assertThat(typeValue).isEqualTo("GalasaProperty");
    }

    @Test
    public void testResourceTypeGalasaPropertyLowerCaseFromStringReturnGalasaProperty(){
        //Given...
        GalasaResourceType resourceType = GalasaResourceType.getFromString("galasaproperty");
        //When...
        String typeValue = resourceType.toString();
        //Then...
        assertThat(typeValue).isEqualTo("GalasaProperty");
    }

    @Test
    public void testResourceTypeGalasaPropertyUpperCaseFromStringReturnGalasaProperty(){
        //Given...
        GalasaResourceType resourceType = GalasaResourceType.getFromString("GALASAPROPERTY");
        //When...
        String typeValue = resourceType.toString();
        //Then...
        assertThat(typeValue).isEqualTo("GalasaProperty");
    }

    @Test
    public void testResourceTypeGalasaPropertyMixedCaseFromStringReturnGalasaProperty(){
        //Given...
        GalasaResourceType resourceType = GalasaResourceType.getFromString("GaLaSaPrOpErTy");
        //When...
        String typeValue = resourceType.toString();
        //Then...
        assertThat(typeValue).isEqualTo("GalasaProperty");
    }
}
