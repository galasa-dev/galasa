/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.*;

import org.junit.Test;

import dev.galasa.framework.mocks.*;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.framework.spi.utils.GalasaGson;

public class TestRunImpl {

    @Test
    public void testCanCreateARunImplWithNothingInDss() throws Exception  {
        String name = "U1234";
        Map<String,String> properties = new HashMap<String,String>();
        IDynamicStatusStoreService dss = new MockDSSStore(properties);
        new RunImpl(name,dss);
    }

    @Test
    public void testCanCreateARunImplWithARunInDss() throws Exception  {
        String name = "U1234";
        Map<String,String> dssProps = new HashMap<String,String>();

        Set<String> tagsValueGoingIn = new HashSet<String>();
        tagsValueGoingIn.add("tag1");
        tagsValueGoingIn.add("tag2");
        GalasaGson gson = new GalasaGson();
        String tagsAsJsonString = gson.toJson(tagsValueGoingIn);
        dssProps.put("run.U1234"+".tags", tagsAsJsonString);
        IDynamicStatusStoreService dss = new MockDSSStore(dssProps);

        RunImpl run = new RunImpl(name,dss);
        Set<String> tagsGotBack = run.getTags();

        assertThat( tagsGotBack).containsExactly("tag1","tag2");
    }

    @Test
    public void testCanConvertARunImplToATestStructure() throws Exception  {
        // Given...
        String name = "U1234";
        Map<String,String> dssProps = new HashMap<String,String>();

        Set<String> tagsValueGoingIn = new HashSet<String>();
        tagsValueGoingIn.add("tag1");
        tagsValueGoingIn.add("tag2");
        GalasaGson gson = new GalasaGson();

        String queuedTimeAsString = Instant.now().toString();

        String tagsAsJsonString = gson.toJson(tagsValueGoingIn);
        dssProps.put("run.U1234"+".tags", tagsAsJsonString);

        dssProps.put("run.U1234"+".test", "bundle/package.testclass");
        dssProps.put("run.U1234"+".group", "my-test-group");
        dssProps.put("run.U1234"+".submissionId", "my-submission-id");
        dssProps.put("run.U1234"+".queued", queuedTimeAsString);
        dssProps.put("run.U1234"+".requestor", "duck");

        IDynamicStatusStoreService dss = new MockDSSStore(dssProps);
        RunImpl run = new RunImpl(name,dss);

        // When...
        TestStructure testStructureGotBack = run.toTestStructure();

        // Then...
        assertThat(testStructureGotBack.getRunName()).isEqualTo(name);
        assertThat(testStructureGotBack.getBundle()).isEqualTo("bundle");
        assertThat(testStructureGotBack.getTestName()).isEqualTo("package.testclass");
        assertThat(testStructureGotBack.getTestShortName()).isEqualTo("testclass");
        assertThat(testStructureGotBack.getQueued()).isEqualTo(queuedTimeAsString);
        assertThat(testStructureGotBack.getGroup()).isEqualTo("my-test-group");
        assertThat(testStructureGotBack.getRequestor()).isEqualTo("duck");
        assertThat(testStructureGotBack.getSubmissionId()).isEqualTo("my-submission-id");
        assertThat(testStructureGotBack.getTags()).containsExactly("tag1", "tag2");
    }

    @Test
    public void testCanConvertARunImplWithAShortTestNameToATestStructure() throws Exception  {
        // Given...
        String name = "U1234";
        Map<String,String> dssProps = new HashMap<String,String>();

        Set<String> tagsValueGoingIn = new HashSet<String>();
        tagsValueGoingIn.add("tag1");
        tagsValueGoingIn.add("tag2");
        GalasaGson gson = new GalasaGson();

        String queuedTimeAsString = Instant.now().toString();

        String tagsAsJsonString = gson.toJson(tagsValueGoingIn);
        dssProps.put("run.U1234"+".tags", tagsAsJsonString);

        dssProps.put("run.U1234"+".test", "bundle/class");
        dssProps.put("run.U1234"+".group", "my-test-group");
        dssProps.put("run.U1234"+".submissionId", "my-submission-id");
        dssProps.put("run.U1234"+".queued", queuedTimeAsString);
        dssProps.put("run.U1234"+".requestor", "duck");

        IDynamicStatusStoreService dss = new MockDSSStore(dssProps);
        RunImpl run = new RunImpl(name,dss);

        // When...
        TestStructure testStructureGotBack = run.toTestStructure();

        // Then...
        assertThat(testStructureGotBack.getRunName()).isEqualTo(name);
        assertThat(testStructureGotBack.getBundle()).isEqualTo("bundle");
        assertThat(testStructureGotBack.getTestName()).isEqualTo("class");
        assertThat(testStructureGotBack.getTestShortName()).isNull();
        assertThat(testStructureGotBack.getQueued()).isEqualTo(queuedTimeAsString);
        assertThat(testStructureGotBack.getGroup()).isEqualTo("my-test-group");
        assertThat(testStructureGotBack.getRequestor()).isEqualTo("duck");
        assertThat(testStructureGotBack.getSubmissionId()).isEqualTo("my-submission-id");
        assertThat(testStructureGotBack.getTags()).containsExactly("tag1", "tag2");
    }
}