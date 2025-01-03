/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.beans.generated;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TestRBACRole {
    
    @Test
    public void testCanSerialiseBeanOk() {
        RBACRole bean = new RBACRole();
        bean.setApiVersion("myApiVersion");
        bean.setkind("GalasaRole");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String serialisedForm = gson.toJson(bean);

        // We expect to see this sort of thing:
        // {
        // "apiVersion": "myApiVersion",
        // "kind": "GalasaRole"
        // }
        // BUT we can't guarantee the order, so using contains to spot the bits we care about.
        assertThat(serialisedForm)
            .contains("\"apiVersion\": \"myApiVersion\"")
            .contains("\"kind\": \"GalasaRole\"");
    }
}
