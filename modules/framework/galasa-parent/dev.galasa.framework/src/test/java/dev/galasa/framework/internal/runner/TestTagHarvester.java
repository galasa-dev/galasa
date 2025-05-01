/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.runner;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import dev.galasa.Tags;
import dev.galasa.framework.mocks.*;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.framework.spi.utils.GalasaGson;

public class TestTagHarvester {

    @Test
    public void testCanCreateAHarvester() {
        IDynamicStatusStoreService dss = null;
        IResultArchiveStore ras = null ;
        TestStructure testStructure = null ;
        GalasaGson gson = null;
        new TagHarvester(dss,ras,testStructure,gson);
    }

    @Test
    public void testCanHarvestTagsFromAClassWhichHasSomeTagsButLaunchDidntSupplyAny() {
        IDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        MockFileSystem fileSystem = new MockFileSystem();
        IResultArchiveStore ras = new MockIResultArchiveStore("U1234", fileSystem);
        TestStructure testStructure = new TestStructure() ;
        GalasaGson gson = new GalasaGson();
        TagHarvester harvester = new TagHarvester(dss,ras,testStructure,gson);

        @Tags({"hello","world"})
        class TestClass {
        }

        TestClass testClassInstance = new TestClass();
        Class<?> testClass = testClassInstance.getClass();

        harvester.harvestTagsFromTestClass(testClass, "U1234");

        assertThat(testStructure.getTags()).hasSize(2);
        assertThat(testStructure.getTags()).contains("hello");
        assertThat(testStructure.getTags()).contains("world");
    }

    @Test
    public void testCanHarvestTagsFromEmptyTagAnnotation() {
        IDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        MockFileSystem fileSystem = new MockFileSystem();
        IResultArchiveStore ras = new MockIResultArchiveStore("U1234", fileSystem);
        TestStructure testStructure = new TestStructure() ;
        GalasaGson gson = new GalasaGson();
        TagHarvester harvester = new TagHarvester(dss,ras,testStructure,gson);

        @Tags({})
        class TestClass {
        }

        TestClass testClassInstance = new TestClass();
        Class<?> testClass = testClassInstance.getClass();

        harvester.harvestTagsFromTestClass(testClass, "U1234");

        assertThat(testStructure.getTags()).hasSize(0);
    }

    @Test
    public void testCanHarvestTagsFromSpacesOnlyTagAnnotation() {
        IDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        MockFileSystem fileSystem = new MockFileSystem();
        IResultArchiveStore ras = new MockIResultArchiveStore("U1234", fileSystem);
        TestStructure testStructure = new TestStructure() ;
        GalasaGson gson = new GalasaGson();
        TagHarvester harvester = new TagHarvester(dss,ras,testStructure,gson);

        @Tags({"   "})
        class TestClass {
        }

        TestClass testClassInstance = new TestClass();
        Class<?> testClass = testClassInstance.getClass();

        harvester.harvestTagsFromTestClass(testClass, "U1234");

        assertThat(testStructure.getTags()).hasSize(0);
    }

    @Test
    public void testCanHarvestTagsFromAClassWhichHasSomeTagsButLaunchSuupliedSomeAlso() {
        IDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        MockFileSystem fileSystem = new MockFileSystem();
        IResultArchiveStore ras = new MockIResultArchiveStore("U1234", fileSystem);

        // Simulate tags being available because they were supplied in the test structure before we
        // harvest tags from the class code.
        TestStructure testStructure = new TestStructure() ;
        testStructure.addTag("one");
        testStructure.addTag("two");

        GalasaGson gson = new GalasaGson();
        TagHarvester harvester = new TagHarvester(dss,ras,testStructure,gson);

        @Tags({"hello","world"})
        class TestClass {
        }

        TestClass testClassInstance = new TestClass();
        Class<?> testClass = testClassInstance.getClass();

        harvester.harvestTagsFromTestClass(testClass, "U1234");

        assertThat(testStructure.getTags()).hasSize(4).contains("hello","world","one","two");
    }

    @Test
    public void testCanHarvestTagsFromAClassWhichAreDuplicatedByLaunchTags() {
        IDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        MockFileSystem fileSystem = new MockFileSystem();
        IResultArchiveStore ras = new MockIResultArchiveStore("U1234", fileSystem);

        // Simulate tags being available because they were supplied in the test structure before we
        // harvest tags from the class code.
        TestStructure testStructure = new TestStructure() ;
        testStructure.addTag("one");
        testStructure.addTag("two");

        GalasaGson gson = new GalasaGson();
        TagHarvester harvester = new TagHarvester(dss,ras,testStructure,gson);

        @Tags({"one","world"})
        class TestClass {
        }

        TestClass testClassInstance = new TestClass();
        Class<?> testClass = testClassInstance.getClass();

        harvester.harvestTagsFromTestClass(testClass, "U1234");

        assertThat(testStructure.getTags()).hasSize(3).contains("world","one","two");
    }


    @Test
    public void testCanHarvestTagsIntoDss() throws Exception {
        IDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        MockFileSystem fileSystem = new MockFileSystem();
        IResultArchiveStore ras = new MockIResultArchiveStore("U1234", fileSystem);
        TestStructure testStructure = new TestStructure() ;
        GalasaGson gson = new GalasaGson();
        TagHarvester harvester = new TagHarvester(dss,ras,testStructure,gson);

        @Tags({"hello"})
        class TestClass {
        }

        TestClass testClassInstance = new TestClass();
        Class<?> testClass = testClassInstance.getClass();

        harvester.harvestTagsFromTestClass(testClass, "U1234");

        // Then ...
        String tagsAsString = dss.get("run.U1234.tags");

        assertThat(tagsAsString).isNotNull().isNotEmpty().isNotBlank();

        HashSet<?> tagSetOfObj = gson.fromJson(tagsAsString, HashSet.class);
        HashSet<String> tags = new HashSet<String>();
        for( Object entry : tagSetOfObj) {
            // Tags are always going to be strings, so we can safely cast as a string,
            // but do an instanceof check to keep the compiler happy.
            if( entry instanceof String) {
                tags.add((String)entry);
            }
        }

        assertThat(tags).hasSize(1).contains("hello");
    }

    @Test
    public void testCanHarvestTagsAndAddToExistingDssTagsOverwritingWhatsAlreadyThere() throws Exception {

        String dssKey = "run.U1234.tags";
        IDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        dss.put(dssKey,"{\"world\"}");

        MockFileSystem fileSystem = new MockFileSystem();
        IResultArchiveStore ras = new MockIResultArchiveStore("U1234", fileSystem);
        TestStructure testStructure = new TestStructure() ;

        GalasaGson gson = new GalasaGson();
        TagHarvester harvester = new TagHarvester(dss,ras,testStructure,gson);

        @Tags({"hello"})
        class TestClass {
        }

        TestClass testClassInstance = new TestClass();
        Class<?> testClass = testClassInstance.getClass();

        harvester.harvestTagsFromTestClass(testClass, "U1234");

        // Then ...
        String tagsAsString = dss.get(dssKey);

        assertThat(tagsAsString).isNotNull().isNotEmpty().isNotBlank();

        HashSet<?> tagSetOfObj = gson.fromJson(tagsAsString, HashSet.class);
        HashSet<String> tags = new HashSet<String>();
        for( Object entry : tagSetOfObj) {
            // Tags are always going to be strings, so we can safely cast as a string,
            // but do an instanceof check to keep the compiler happy.
            if( entry instanceof String) {
                tags.add((String)entry);
            }
        }

        assertThat(tags).hasSize(1).contains("hello");
    }

    @Test
    public void testCanHarvestTagsIntoRas() throws Exception {
        IDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        MockFileSystem rasFileSystem = new MockFileSystem();
        MockIResultArchiveStore ras = new MockIResultArchiveStore("U1234", rasFileSystem);
        TestStructure testStructure = new TestStructure() ;
        GalasaGson gson = new GalasaGson();
        TagHarvester harvester = new TagHarvester(dss,ras,testStructure,gson);

        @Tags({"hello"})
        class TestClass {
        }

        TestClass testClassInstance = new TestClass();
        Class<?> testClass = testClassInstance.getClass();

        harvester.harvestTagsFromTestClass(testClass, "U1234");

        // Then ...
        List<TestStructure> testStructuresSaved = ras.getTestStructureHistory();
        assertThat(testStructuresSaved).isNotNull().hasSize(1);
        TestStructure testStructureSaved = testStructuresSaved.get(0);
        assertThat(testStructureSaved.getTags()).hasSize(1).contains("hello");
    }
}
