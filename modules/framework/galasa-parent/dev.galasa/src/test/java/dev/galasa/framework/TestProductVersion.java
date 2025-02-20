/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import dev.galasa.ManagerException;
import dev.galasa.ProductVersion;


public class TestProductVersion {

    @Test
    public void testTwoVersionsHaveSameHashCode() throws Exception {
        ProductVersion a = ProductVersion.v(12).r(3).m(45);
        ProductVersion b = ProductVersion.v(12).r(3).m(45);
        ProductVersion c = new ProductVersion(12,3,45);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a.hashCode()).isEqualTo(c.hashCode());
    }



    @Test
    public void testTwoDifferentVersionsHaveDifferentHashCode() {
        ProductVersion a = ProductVersion.v(12).r(3).m(45);
        ProductVersion b = ProductVersion.v(12).r(3).m(46);
        ProductVersion c = ProductVersion.v(12).r(4).m(45);
        ProductVersion d = ProductVersion.v(13).r(3).m(45);

        assertThat(a.hashCode()).isNotEqualTo(b.hashCode());
        assertThat(a.hashCode()).isNotEqualTo(c.hashCode());
        assertThat(a.hashCode()).isNotEqualTo(d.hashCode());
    }

    @Test
    public void testTwoVersionsAreEqual() throws Exception {
        ProductVersion a = ProductVersion.v(12).r(3).m(45);
        ProductVersion b = ProductVersion.v(12).r(3).m(45);
        ProductVersion c = new ProductVersion(12,3,45);

        assertThat(a).isEqualTo(b);
        assertThat(a.equals(b)).isTrue();
        assertThat(a).isEqualTo(c);

        assertThat(b).isEqualTo(a);
        assertThat(b.equals(a)).isTrue();
        assertThat(c).isEqualTo(a);
    }

    @Test
    public void testTwoDifferentVersionsAreNotEqual() {
        ProductVersion a = ProductVersion.v(12).r(3).m(45);
        ProductVersion b = ProductVersion.v(12).r(3).m(46);
        ProductVersion c = ProductVersion.v(12).r(4).m(45);
        ProductVersion d = ProductVersion.v(13).r(3).m(45);
        assertThat(a).isNotEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(d);
        assertThat(a.equals(b)).isFalse();
        assertThat(a.equals(c)).isFalse();
        assertThat(a.equals(d)).isFalse();
    }

    @Test
    public void testVersionsCanBeGreaterThanEachOther() {
        ProductVersion a = ProductVersion.v(12).r(3).m(45);
        ProductVersion b = ProductVersion.v(12).r(3).m(46);
        ProductVersion c = ProductVersion.v(12).r(4).m(45);
        ProductVersion d = ProductVersion.v(13).r(3).m(45);

        assertThat(b).isGreaterThan(a);
        assertThat(c).isGreaterThan(a);
        assertThat(d).isGreaterThan(a);
    }

    @Test
    public void testVersionsCanBeLessThanEachOther() {
        ProductVersion a = ProductVersion.v(12).r(3).m(45);
        ProductVersion b = ProductVersion.v(12).r(3).m(46);
        ProductVersion c = ProductVersion.v(12).r(4).m(45);
        ProductVersion d = ProductVersion.v(13).r(3).m(45);
        assertThat(a).isLessThan(b);
        assertThat(a).isLessThan(c);
        assertThat(a).isLessThan(d);
    }
    
    @Test
    public void testCanParseValues() throws Exception {
        ProductVersion a = ProductVersion.v(12).r(3).m(45);
        String rendered = a.toString();
        ProductVersion b = ProductVersion.parse(rendered);
        assertThat(a).isEqualTo(b);
    }

    @Test
    public void testNegativeNumbersCantBeParsed() {
        ManagerException ex = catchThrowableOfType( ()->ProductVersion.parse("0.-1.2"), ManagerException.class);
        assertThat(ex).hasMessageContaining("Invalid product version string");
    }

    @Test
    public void testNegativeNumbersCantBeUsedInConstructor() {

        ManagerException ex = catchThrowableOfType( ()->new ProductVersion(-1,0,2), ManagerException.class);
        assertThat(ex).hasMessageContaining("Invalid product version string");

        ex = catchThrowableOfType( ()->new ProductVersion(0,-1,2), ManagerException.class);
        assertThat(ex).hasMessageContaining("Invalid product version string");

        ex = catchThrowableOfType( ()->new ProductVersion(0,1,-2), ManagerException.class);
        assertThat(ex).hasMessageContaining("Invalid product version string");
    }

    @Test
    public void testNegativeNumbersWithFluidMethodsAreTreatedAsZeros() {
        ProductVersion a = ProductVersion.v(0).r(0).m(0);
        ProductVersion b = ProductVersion.v(0).r(0).m(-1);
        ProductVersion c = ProductVersion.v(0).r(-1).m(0);
        ProductVersion d = ProductVersion.v(-1).r(0).m(0);

        assertThat(a).isEqualTo(b);
        assertThat(a).isEqualTo(c);
        assertThat(a).isEqualTo(d);
    }
}
