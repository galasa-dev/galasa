/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openapi2beans.example;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.galasa.openapi2beans.example.generated.BeanWithEnumPropertyAnEnumProperty;


public class TestAnEnumProperty {
    
    
    @Test
    public void testCanSerialiseTheEnumWithValue1() throws Exception {
        BeanWithEnumPropertyAnEnumProperty enumUnderTest = BeanWithEnumPropertyAnEnumProperty.STRING_1;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String serialisedForm = gson.toJson(enumUnderTest);
        assertThat(serialisedForm).contains("\"string1\"");
    }

    @Test
    public void testCanSerialiseTheEnumWithValue2() throws Exception {
        BeanWithEnumPropertyAnEnumProperty enumUnderTest = BeanWithEnumPropertyAnEnumProperty.STRING_2;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String serialisedForm = gson.toJson(enumUnderTest);
        assertThat(serialisedForm).contains("\"string2\"");
    }
}
