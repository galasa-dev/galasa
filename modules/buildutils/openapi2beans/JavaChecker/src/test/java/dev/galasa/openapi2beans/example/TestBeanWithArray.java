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

import dev.galasa.openapi2beans.example.generated.BeanWithArray;


public class TestBeanWithArray {
    
    @Test
    public void testCanSerialiseTheBean() throws Exception {
        BeanWithArray beanUnderTest = new BeanWithArray();
        beanUnderTest.setAnArrayVariable(new String[]{"string0", "string1"});
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String serialisedForm = gson.toJson(beanUnderTest);
        assertThat(serialisedForm).contains("\"anArrayVariable\": [\n" +
                        "    \"string0\",\n" +
                        "    \"string1\"\n" +
                        "  ]");
    }
}
