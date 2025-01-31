/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class TestVisibility {

    @Test
    public void testNamespaceTypeNormalReturnNormal(){
        //Given...
        Visibility namespaceType = Visibility.NORMAL;
        //When...
        String typeValue = namespaceType.toString();
        //Then...
        assertThat(typeValue).isEqualTo("normal");
    }

    @Test
    public void testNamespaceTypeSecureReturnSecure(){
        //Given...
        Visibility namespaceType = Visibility.SECURE;
        //When...
        String typeValue = namespaceType.toString();
        //Then...
        assertThat(typeValue).isEqualTo("secure");
    }

    @Test
    public void testNamespaceTypeGetFromStringNormalReturnNormal(){
        //Given...
        Visibility namespaceType = Visibility.getfromString("NoRmal");
        //When...
        String typeValue = namespaceType.toString();
        //Then...
        assertThat(typeValue).isEqualTo("normal");
    }

    @Test
    public void testNamespaceTypeGetFromStringSecureReturnSecure(){
        //Given...
        Visibility namespaceType = Visibility.getfromString("SecUre");
        //When...
        String typeValue = namespaceType.toString();
        //Then...
        assertThat(typeValue).isEqualTo("secure");
    }
}
