/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.common;

import java.util.*;

import org.junit.Test;

import dev.galasa.framework.api.common.resources.CPSProperty;
import dev.galasa.framework.api.common.resources.GalasaPropertyName;

import static org.assertj.core.api.Assertions.*;


public class TestPropertyComparator{
    
    private Map<GalasaPropertyName, CPSProperty> sort(Collection<GalasaPropertyName> unsorted ) {
        PropertyComparator comparator = new PropertyComparator();
        Map<GalasaPropertyName, CPSProperty> sorted = new TreeMap<GalasaPropertyName, CPSProperty>(comparator);
    
        for( GalasaPropertyName key : unsorted ) {
            sorted.put(key,new CPSProperty("1.1", "1.1")); // All properties have value 1. We don't care for testing.
        }
    
        return sorted;
    }

    @Test
    public void testTwoEntriesInCorrectOrder() {

        List<GalasaPropertyName> keys = new ArrayList<>();
        keys.add(new GalasaPropertyName("a.b.c.d"));
        keys.add(new GalasaPropertyName("a.b.cdefg"));

        Map<GalasaPropertyName, CPSProperty> results = sort(keys);
        assertThat(results.size()).isEqualTo(2);
        Iterator<GalasaPropertyName> walker = results.keySet().iterator();
        assertThat(walker.next()).isEqualTo(new GalasaPropertyName("a.b.c.d"));
        assertThat(walker.next()).isEqualTo(new GalasaPropertyName("a.b.cdefg"));
    }

    @Test
    public void testTwoEntriesInWrongOrder() {

       List<GalasaPropertyName> keys = new ArrayList<>();
        keys.add(new GalasaPropertyName("a.b.cdefg"));
        keys.add(new GalasaPropertyName("a.b.c.d"));


        Map<GalasaPropertyName, CPSProperty>results = sort(keys);
        assertThat(results.size()).isEqualTo(2);
        Iterator<GalasaPropertyName> walker = results.keySet().iterator();
        assertThat(walker.next()).isEqualTo(new GalasaPropertyName("a.b.c.d"));
        assertThat(walker.next()).isEqualTo(new GalasaPropertyName("a.b.cdefg"));
    }

    @Test
    public void testTwoEntriesLongestLaterAlphabetically() {

       List<GalasaPropertyName> keys = new ArrayList<>();
        keys.add(new GalasaPropertyName("a.b.adefg"));
        keys.add(new GalasaPropertyName("a.b.c.d"));

        Map<GalasaPropertyName, CPSProperty>results = sort(keys);
        assertThat(results.size()).isEqualTo(2);
        Iterator<GalasaPropertyName> walker = results.keySet().iterator();
        assertThat(walker.next()).isEqualTo(new GalasaPropertyName("a.b.c.d"));
        assertThat(walker.next()).isEqualTo(new GalasaPropertyName("a.b.adefg"));
    }

    @Test
    public void testTwoEntriesAlphabeticallySortedThirdPart() {

       List<GalasaPropertyName> keys = new ArrayList<>();
        keys.add(new GalasaPropertyName("a.b.a.d"));
        keys.add(new GalasaPropertyName("a.b.c.d"));

        Map<GalasaPropertyName, CPSProperty>results = sort(keys);
        assertThat(results.size()).isEqualTo(2);
        Iterator<GalasaPropertyName> walker = results.keySet().iterator();
        assertThat(walker.next()).isEqualTo(new GalasaPropertyName("a.b.a.d"));
        assertThat(walker.next()).isEqualTo(new GalasaPropertyName("a.b.c.d"));
    }

    @Test
    public void testComparatorInDetail() {

        PropertyComparator comparator = new PropertyComparator();

        assertThat( comparator.compare("a", "b")).isLessThan(0);
        assertThat( comparator.compare("b", "a")).isGreaterThan(0);
        assertThat( comparator.compare("a.a", "a.b")).isLessThan(0);
        assertThat( comparator.compare("a.b", "a.a")).isGreaterThan(0);
        assertThat( comparator.compare("a.a", "a")).isLessThan(0);
        assertThat( comparator.compare("a", "a.a")).isGreaterThan(0);

        assertThat( comparator.compare("a", "a")).isEqualTo(0);
    }

    @Test
    public void testComparatorwithNull() {

        PropertyComparator comparator = new PropertyComparator();
        assertThat( comparator.compare("a", null)).isLessThan(0);
        assertThat( comparator.compare(null, "a")).isGreaterThan(0);
        assertThat( comparator.compare(new GalasaPropertyName(null,null), new GalasaPropertyName(null,null))).isEqualTo(0);
    }

}