/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.plugin.common.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonFactory {
    public Gson getGson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson ;
    }
}
