/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.common.screens.json;


import java.util.ArrayList;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import dev.galasa.framework.spi.utils.GalasaGsonBuilder;
import dev.galasa.zos3270.common.screens.Terminal;

/**
 * Handles the writing of a terminal into a json string or json object.
 */
public class TerminalJsonTransform {

    private Gson gson ;

    public TerminalJsonTransform() {
        this(true);
    }

    public TerminalJsonTransform( boolean isPrettyPrinting ) {
        this.gson = new GalasaGsonBuilder(isPrettyPrinting).getGson();
    }
    
    public JsonObject toJsonObject(Terminal terminal) {
        return (JsonObject) gson.toJsonTree(terminal);
    }
    
    public String toJsonString(Terminal terminal) {
        JsonObject jsonObj = toJsonObject(terminal);
        stripFalseBooleans(jsonObj);
        return gson.toJson(jsonObj);
    }

    public Terminal toTerminal(String tempJson) {
        return gson.fromJson(tempJson, Terminal.class);
    }

    private void stripFalseBooleans(JsonObject json) {
        ArrayList<Entry<String, JsonElement>> entries = new ArrayList<>();
        entries.addAll(json.entrySet());

        for (Entry<String, JsonElement> entry : entries) {
            JsonElement element = entry.getValue();

            if (element.isJsonPrimitive() && ((JsonPrimitive) element).isBoolean()
                    && !((JsonPrimitive) element).getAsBoolean()) {
                json.remove(entry.getKey());
            } else if (element.isJsonObject()) {
                stripFalseBooleans((JsonObject) element);
            } else if (element.isJsonArray()) {
                JsonArray array = (JsonArray) element;
                for (int i = 0; i < array.size(); i++) {
                    if (array.get(i).isJsonObject()) {
                        stripFalseBooleans((JsonObject) array.get(i));
                    }
                }
            }
        }
    }
}
