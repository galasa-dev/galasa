/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.Reader;

import org.apache.commons.io.output.StringBuilderWriter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

public class GalasaGson {

    private GalasaGsonBuilder galasaGsonBuilder;
    private Gson gson;

    public GalasaGson(){
        galasaGsonBuilder =new GalasaGsonBuilder();
        gson = galasaGsonBuilder.getGson();
    }
    public Gson getGson(){
        return gson;
    }

    public GalasaGsonBuilder getGsonBuilder(){
        return galasaGsonBuilder;
    }

    public void setGsonBuilder(GalasaGsonBuilder builder){
        galasaGsonBuilder = builder;
        gson = galasaGsonBuilder.getGson();
    }

    public String toJson(Object obj){
        return gson.toJson(obj);
    }

    public <T> T fromJson(String json, Class<T> classOfT)  {
        return gson.fromJson(json,  classOfT);   
    }
    
    public <T> T fromJson(InputStreamReader inputStreamReader, Class<T> classOfT) throws IOException  {
        return fromJson(readerToString(inputStreamReader),  classOfT);  
    }
    
    public <T> T fromJson(JsonObject jsonObject, Class<T> classOfT)  {
        return gson.fromJson(jsonObject,  classOfT);   
    }
    
    public <T> T fromJson(JsonReader jsonReader, Class<T> classOfT)  {
        return gson.fromJson(jsonReader,  classOfT);   
    }
    
    public JsonObject fromJson(BufferedReader bufferedReader, Class<JsonObject> classOfT) throws IOException  {
        return fromJson(readerToString(bufferedReader),  classOfT);   
    }

    public JsonElement toJsonTree(Object obj){
        return gson.toJsonTree(obj);
    }

    public JsonReader newJsonReader(StringReader stringReader) {
        return gson.newJsonReader(stringReader);
    }

    private String readerToString(Reader reader) throws IOException {
        StringBuilderWriter writer = new StringBuilderWriter();
        reader.transferTo(writer);
        return writer.toString();
    }
}
