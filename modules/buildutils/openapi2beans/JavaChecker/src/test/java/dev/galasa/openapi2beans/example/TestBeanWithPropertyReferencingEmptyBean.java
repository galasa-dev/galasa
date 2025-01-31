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

import dev.galasa.openapi2beans.example.generated.BeanWithPropertyReferencingEmptyBean;
import dev.galasa.openapi2beans.example.generated.EmptyBean;

public class TestBeanWithPropertyReferencingEmptyBean {
    
    @Test
    public void testCanSerialiseTheBean() throws Exception {
        BeanWithPropertyReferencingEmptyBean beanUnderTest = new BeanWithPropertyReferencingEmptyBean();
        EmptyBean emptyBean = new EmptyBean();
        beanUnderTest.setReferencingProperty(emptyBean);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String serialisedForm = gson.toJson(beanUnderTest);
        assertThat(serialisedForm).contains("\"referencingProperty\": {}");
    }
}
