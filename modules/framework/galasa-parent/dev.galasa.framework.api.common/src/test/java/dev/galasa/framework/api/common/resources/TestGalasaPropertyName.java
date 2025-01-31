/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;


public class TestGalasaPropertyName {

    @Test
    public void testCanCreatePropNameOk() {
        new GalasaPropertyName("mydomain.mypropgroup.myprop");
    }

    @Test
    public void testCreatingWithQualifiedNameSplitsOutNamespaceNameOk() {
        GalasaPropertyName name = new GalasaPropertyName("mydomain.mypropgroup.myprop");
        assertThat(name.getNamespaceName()).isEqualTo("mydomain");
    }

    @Test
    public void testCreatingWithQualifiedNameSplitsOuPropertySimpleNameOk() {
        GalasaPropertyName name = new GalasaPropertyName("mydomain.mypropgroup.myprop");
        assertThat(name.getSimpleName()).isEqualTo("mypropgroup.myprop");
    }

    @Test
    public void testTwoPropertyNamesAreEqual() {
        GalasaPropertyName name1 = new GalasaPropertyName("mydomain.mypropgroup.myprop");
        GalasaPropertyName name2 = new GalasaPropertyName("mydomain.mypropgroup.myprop");
        assertThat(name1).isEqualTo(name2);
        assertThat(name2).isEqualTo(name1);
    }

    @Test
    public void testTwoPropertyNamesHaveSameHashcode() {
        GalasaPropertyName name1 = new GalasaPropertyName("mydomain.mypropgroup.myprop");
        GalasaPropertyName name2 = new GalasaPropertyName("mydomain.mypropgroup.myprop");
        assertThat(name1.hashCode()).isEqualTo(name2.hashCode());
    }

    @Test
    public void testPropertyNameHashCodesWithNullNamespaceAreSame() {
        String namespace = null;
        String simpleName = "my.prop.name";
        GalasaPropertyName name1 = new GalasaPropertyName(namespace,simpleName);
        GalasaPropertyName name2 = new GalasaPropertyName(namespace,simpleName);
        assertThat(name1.hashCode()).isEqualTo(name2.hashCode());
    }

    @Test
    public void testPropertyNameHashCodesWithNullSimpleNameAreSame() {
        String namespace = "myNamespace";
        String simpleName = null;
        GalasaPropertyName name1 = new GalasaPropertyName(namespace,simpleName);
        GalasaPropertyName name2 = new GalasaPropertyName(namespace,simpleName);
        assertThat(name1.hashCode()).isEqualTo(name2.hashCode());
    }

    @Test
    public void testPropertyNameWithNullSimpleNameAreSame() {
        String namespace = "myNamespace";
        String simpleName = null;
        GalasaPropertyName name1 = new GalasaPropertyName(namespace,simpleName);
        GalasaPropertyName name2 = new GalasaPropertyName(namespace,simpleName);
        assertThat(name1).isEqualTo(name2);
        assertThat(name2).isEqualTo(name1);
    }

    @Test
    public void testPropertyNameWithNullSimpleNamespaceAreSame() {
        String namespace = null;
        String simpleName = "my.property";
        GalasaPropertyName name1 = new GalasaPropertyName(namespace,simpleName);
        GalasaPropertyName name2 = new GalasaPropertyName(namespace,simpleName);
        assertThat(name1).isEqualTo(name2);
        assertThat(name2).isEqualTo(name1);
    }

    @Test
    public void testPropertyNameWithNullNamespaceAreNotSame() {
        GalasaPropertyName name1 = new GalasaPropertyName(null,"my.property");
        GalasaPropertyName name2 = new GalasaPropertyName("myNamespace","my.property");
        assertThat(name1).isNotEqualTo(name2);
        assertThat(name2).isNotEqualTo(name1);
    }

    @Test
    public void testPropertyNameWithNullSimpleNameAreNotSame() {
        GalasaPropertyName name1 = new GalasaPropertyName("myNamespace",null);
        GalasaPropertyName name2 = new GalasaPropertyName("myNamespace","my.property");
        assertThat(name1).isNotEqualTo(name2);
        assertThat(name2).isNotEqualTo(name1);
    }
}