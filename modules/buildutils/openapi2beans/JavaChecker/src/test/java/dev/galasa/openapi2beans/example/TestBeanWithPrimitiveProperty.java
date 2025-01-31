/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openapi2beans.example;


import org.junit.*;

import static org.assertj.core.api.Assertions.*;
import dev.galasa.openapi2beans.example.generated.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TestBeanWithPrimitiveProperty {
    
    @Test
    public void testCanSerialiseTheBean() throws Exception {
        BeanWithPrimitiveProperty beanUnderTest = new BeanWithPrimitiveProperty();
        beanUnderTest.setAStringVariable("hello");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String serialisedForm = gson.toJson(beanUnderTest);
        assertThat(serialisedForm).contains("\"aStringVariable\": \"hello\"");
    }
}