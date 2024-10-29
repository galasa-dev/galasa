/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;
import java.util.Map.Entry;


public class BaseServletTest {

    // This JWT contains the following claims:
    // {
    //   "preferred_username": "testRequestor",
    //   "sub": "requestorId",
    //   "name": "Jack Skellington",
    //   "iat": 1516239022
    // }
    public final static String DUMMY_JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwcmVmZXJyZWRfdXNlcm5hbWUiOiJ0ZXN0UmVxdWVzdG9yIiwic3ViIjoicmVxdWVzdG9ySWQiLCJuYW1lIjoiSmFjayBTa2VsbGluZ3RvbiIsImlhdCI6MTUxNjIzOTAyMn0.kW1arFknbywrtRrxsLjB2MiXcM6oSgnUrOpuAlE5dhk"; //Dummy JWT

	protected void checkErrorStructure(String jsonString , int expectedErrorCode , String... expectedErrorMessageParts ) throws Exception {

		JsonElement jsonElement = JsonParser.parseString(jsonString);
		assertThat(jsonElement).isNotNull().as("Failed to parse the body to a json object.");

		JsonObject jsonObject = jsonElement.getAsJsonObject();
		assertThat(jsonObject).isNotNull().as("Json parsed is not a json object.");

		// Check the error code
		JsonElement errorCodeField = jsonObject.get("error_code");
		assertThat(errorCodeField).isNotNull().as("Returned structure didn't contain the error_code field!");

		int actualErrorCode = jsonObject.get("error_code").getAsInt();
		assertThat(actualErrorCode).isEqualTo(expectedErrorCode);

		// Check the error message...
		String msg = jsonObject.get("error_message").toString();
		for ( String expectedMessagePart : expectedErrorMessageParts ) {
			assertThat(msg).contains(expectedMessagePart);
		}
	}

	protected void checkJsonArrayStructure(String jsonString, Map<String, String> jsonFieldsToCheck) throws Exception {

		JsonElement jsonElement = JsonParser.parseString(jsonString);
		assertThat(jsonElement).isNotNull().as("Failed to parse the body to a json object.");

		JsonArray jsonArray = jsonElement.getAsJsonArray();
		assertThat(jsonArray).isNotNull().as("Json parsed is not a json array.");

        // Go through the map of provided fields and check if any of the objects in the JSON array
        // contain a matching key-value entry.
        for (Entry<String, String> entry : jsonFieldsToCheck.entrySet()) {
            boolean fieldMatches = false;

            for (JsonElement element : jsonArray) {
                JsonObject jsonObject = element.getAsJsonObject();
                if (jsonObject.get(entry.getKey()).getAsString().equals(entry.getValue())) {
                    fieldMatches = true;
                }
            }
            assertThat(fieldMatches).isTrue();
        }
    }

    protected JsonArray getJsonArrayFromJson(String jsonString, String jsonArrayKey) throws Exception {
        JsonElement jsonElement = JsonParser.parseString(jsonString);
        assertThat(jsonElement).isNotNull().as("Failed to parse the body to a json object.");

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        assertThat(jsonObject.has(jsonArrayKey)).isTrue();

        JsonArray jsonArray = jsonObject.get(jsonArrayKey).getAsJsonArray();
        assertThat(jsonArray).isNotNull().as("Json parsed is not a json array.");

        return jsonArray;
    }
}