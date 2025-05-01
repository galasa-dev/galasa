/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.runner;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonArray;

import dev.galasa.Tags;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.framework.spi.utils.GalasaGson;

/**
 * Allows a test runner to harvest tags from a java class.
 */
public class TagHarvester {

    Log logger = LogFactory.getLog(TagHarvester.class);

    private IDynamicStatusStoreService dss;
    private IResultArchiveStore ras;
    private TestStructure testStructure;
    private GalasaGson gson ;
    public TagHarvester(IDynamicStatusStoreService dss, IResultArchiveStore ras, TestStructure testStructure, GalasaGson gson) {
        this.dss = dss ;
        this.ras = ras ;
        this.testStructure = testStructure;
        this.gson = gson;
    }

    /**
     * We have a test class with possible annotations inside. 
     * Harvest these and add them to the collection of tags we have from when the test was submitted.
     * Then update the dss and ras accordingly.
     * 
     * @param testClass
     */
    public void harvestTagsFromTestClass( Class<?> testClass, String runName) {
        Set<String> classAnnotationTags = extractTagsFromClass(testClass);
        if( ! classAnnotationTags.isEmpty() ) {
            // There were test class annotations which added tags.
            updateTestStructureWithTags(classAnnotationTags);
            updateTagsInDss(runName);
            updateTagsInRas();
        }
    }

    private Set<String> extractTagsFromClass(Class<?> clazz) {
        HashSet<String> tags = new HashSet<String>();

        Tags[] annotations = clazz.getAnnotationsByType(Tags.class);
        if( annotations != null) {
            for( Tags annotation : annotations ){
                String[] values = annotation.value();
                if( values != null) {
                    for( String value : values) {
                        if( value != null) {
                            String trimmedValue = value.trim();
                            if(!trimmedValue.isBlank() ){
                                tags.add(trimmedValue);
                            }
                        }
                    }
                }
            }
        }
        return tags;
    }

    private void updateTestStructureWithTags(Set<String> tagsToAdd) {
        // Update the test structure with tags.
        for(String tag: tagsToAdd ) {
            this.testStructure.addTag(tag);
        }
    }

    private void updateTagsInDss(String runName) {

        // Update the dss with the tags we have collected in the test structure.
        Set<String> tags = this.testStructure.getTags();
        String key = "run." + runName + "." + "tags" ;

        // The tags are stored as a json array of strings... so 
        // turn the tags into json.
        JsonArray tagsJsonArray = new JsonArray();
        for( String tag: tags) {
            tagsJsonArray.add(tag);
        }
        String tagsJson = gson.toJson(tagsJsonArray);

        try {
            this.dss.put(key, tagsJson);
        } catch (DynamicStatusStoreException ex) {
            logger.error("Failed to write tags to dss. Ignoring and running the test anyway.",ex);
        }
    }

    private void updateTagsInRas() {
        try {
            this.ras.updateTestStructure(testStructure);
        } catch( ResultArchiveStoreException rasEx) {
            logger.error("Failed to update the tags in test structure in the RAS. Ignoring and running the test anyway.",rasEx);
        }
    }


}