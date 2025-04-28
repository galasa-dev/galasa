/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.mocks.MockCPSStore;
import dev.galasa.framework.mocks.MockDSSStore;
import dev.galasa.framework.mocks.MockFramework;
import dev.galasa.framework.mocks.MockRun;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.RunRasAction;
import dev.galasa.framework.spi.IFrameworkRuns.SharedEnvironmentPhase;
import dev.galasa.framework.spi.utils.GalasaGson;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

public class FrameworkRunsTest {

    private static final GalasaGson gson = new GalasaGson();

    @BeforeClass
    public static void setUp() {
        gson.setGsonBuilder(new GalasaGsonBuilder(false));
    }

    private String getExpectedOverridesJson(Properties properties) {
        JsonArray overridesArray = new JsonArray();

        for (Entry<Object, Object> entry : properties.entrySet()) {
            JsonObject propertyJson = new JsonObject();
            propertyJson.addProperty("key", (String) entry.getKey());
            propertyJson.addProperty("value", (String) entry.getValue());
            overridesArray.add(propertyJson);
        }
        return gson.toJson(overridesArray);
    }
   
    @Test
    public void testSubmitRunReturnsSubmittedRun() throws Exception {
        // Given...
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        MockFramework mockFramework = new MockFramework(mockCps, mockDss);

        FrameworkRuns frameworkRuns = new FrameworkRuns(mockFramework);

        String submissionId = "submission1";
        String runType = "unknown";
        String requestor = "me";
        String bundleName = "mybundle";
        String testName = "mytest";
        String groupName = "my.group";
        String mavenRepo = "https://my.maven.repo";
        String obr = "mvn:my.group/my.group.obr/0.38.0/obr";
        String stream = "a-test-stream";
        boolean local = true;
        boolean trace = true;
        Set<String> tags = null ;

        Properties overrides = new Properties();
        String override1Key = "override1";
        String override1Value = "this-is-an-override";
        String override2Key = "override2";
        String override2Value = "this-is-another-override";
        overrides.setProperty(override1Key, override1Value);
        overrides.setProperty(override2Key, override2Value);

        SharedEnvironmentPhase sharedEnvironmentPhase = null;
        String sharedEnvironmentRunName = null;
        String language = "java";

        // When...
        IRun run = frameworkRuns.submitRun(
            runType,
            requestor,
            bundleName,
            testName,
            groupName,
            mavenRepo,
            obr,
            stream,
            local,
            trace,
            tags,
            overrides,
            sharedEnvironmentPhase,
            sharedEnvironmentRunName,
            language,
            submissionId
        );

        // Then...
        assertThat(run).isNotNull();
        assertThat(run.getName()).isEqualTo("U1");
        assertThat(run.getTest()).isEqualTo(bundleName + "/" + testName);

        // Check that the DSS has been populated with the correct run-related properties
        assertThat(mockDss.get("request.prefix.U.lastused")).isEqualTo("1");
        assertThat(mockDss.get("run.U1.obr")).isEqualTo(obr);
        assertThat(mockDss.get("run.U1.group")).isEqualTo(groupName);
        assertThat(mockDss.get("run.U1.requestor")).isEqualTo(requestor);
        assertThat(mockDss.get("run.U1.testbundle")).isEqualTo(bundleName);
        assertThat(mockDss.get("run.U1.repository")).isEqualTo(mavenRepo);
        assertThat(mockDss.get("run.U1.stream")).isEqualTo(stream);
        assertThat(mockDss.get("run.U1.local")).isEqualTo(Boolean.toString(local));
        assertThat(mockDss.get("run.U1.testclass")).isEqualTo(testName);
        assertThat(mockDss.get("run.U1.trace")).isEqualTo(Boolean.toString(trace));
        assertThat(mockDss.get("run.U1.request.type")).isEqualTo(runType.toUpperCase());
        assertThat(mockDss.get("run.U1.status")).isEqualTo("queued");
        assertThat(mockDss.get("run.U1.overrides")).isEqualTo(getExpectedOverridesJson(overrides));
    }
   
    @Test
    public void testSubmitRunWithMaxRunNumberReachedReturnsSubmittedRunOk() throws Exception {
        // Given...
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        mockDss.put("request.prefix.U.lastused", "10");
        
        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        mockCps.setProperty("request.prefix.U.maximum", "10");
        
        MockFramework mockFramework = new MockFramework(mockCps, mockDss);

        FrameworkRuns frameworkRuns = new FrameworkRuns(mockFramework);

        String submissionId = "submission1";
        String runType = "unknown";
        String requestor = "me";
        String bundleName = "mybundle";
        String testName = "mytest";
        String groupName = "my.group";
        String mavenRepo = "https://my.maven.repo";
        String obr = "mvn:my.group/my.group.obr/0.38.0/obr";
        String stream = "a-test-stream";
        boolean local = true;
        boolean trace = true;
        Set<String> tags = null ;

        Properties overrides = new Properties();
        String override1Key = "override1";
        String override1Value = "this-is-an-override";
        String override2Key = "override2";
        String override2Value = "this-is-another-override";
        overrides.setProperty(override1Key, override1Value);
        overrides.setProperty(override2Key, override2Value);

        SharedEnvironmentPhase sharedEnvironmentPhase = null;
        String sharedEnvironmentRunName = null;
        String language = "java";

        // When...
        IRun run = frameworkRuns.submitRun(
            runType,
            requestor,
            bundleName,
            testName,
            groupName,
            mavenRepo,
            obr,
            stream,
            local,
            trace,
            tags,
            overrides,
            sharedEnvironmentPhase,
            sharedEnvironmentRunName,
            language,
            submissionId
        );

        // Then...
        assertThat(run).isNotNull();
        assertThat(run.getName()).isEqualTo("U1");
        assertThat(run.getTest()).isEqualTo(bundleName + "/" + testName);

        // Check that the DSS has been populated with the correct run-related properties
        assertThat(mockDss.get("request.prefix.U.lastused")).isEqualTo("1");
        assertThat(mockDss.get("run.U1.obr")).isEqualTo(obr);
        assertThat(mockDss.get("run.U1.group")).isEqualTo(groupName);
        assertThat(mockDss.get("run.U1.requestor")).isEqualTo(requestor);
        assertThat(mockDss.get("run.U1.testbundle")).isEqualTo(bundleName);
        assertThat(mockDss.get("run.U1.repository")).isEqualTo(mavenRepo);
        assertThat(mockDss.get("run.U1.stream")).isEqualTo(stream);
        assertThat(mockDss.get("run.U1.local")).isEqualTo(Boolean.toString(local));
        assertThat(mockDss.get("run.U1.testclass")).isEqualTo(testName);
        assertThat(mockDss.get("run.U1.trace")).isEqualTo(Boolean.toString(trace));
        assertThat(mockDss.get("run.U1.request.type")).isEqualTo(runType.toUpperCase());
        assertThat(mockDss.get("run.U1.status")).isEqualTo("queued");
        assertThat(mockDss.get("run.U1.overrides")).isEqualTo(getExpectedOverridesJson(overrides));
    }
   
    @Test
    public void testSubmitRunWithMaxRunNumberReachedTwiceThrowsError() throws Exception {
        // Given...
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        mockDss.put("request.prefix.U.lastused", "10");

        mockDss.setSwapSetToFail(true);
        
        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        mockCps.setProperty("request.prefix.U.maximum", "0");
        
        MockFramework mockFramework = new MockFramework(mockCps, mockDss);

        FrameworkRuns frameworkRuns = new FrameworkRuns(mockFramework);

        String submissionId = "submission1";
        String runType = "unknown";
        String requestor = "me";
        String bundleName = "mybundle";
        String testName = "mytest";
        String groupName = "my.group";
        String mavenRepo = "https://my.maven.repo";
        String obr = "mvn:my.group/my.group.obr/0.38.0/obr";
        String stream = "a-test-stream";
        boolean local = true;
        boolean trace = true;
        Set<String> tags = null ;

        Properties overrides = new Properties();
        String override1Key = "override1";
        String override1Value = "this-is-an-override";
        String override2Key = "override2";
        String override2Value = "this-is-another-override";
        overrides.setProperty(override1Key, override1Value);
        overrides.setProperty(override2Key, override2Value);

        SharedEnvironmentPhase sharedEnvironmentPhase = null;
        String sharedEnvironmentRunName = null;
        String language = "java";

        // When...
        FrameworkException thrown = catchThrowableOfType(() -> {
            frameworkRuns.submitRun(
                runType,
                requestor,
                bundleName,
                testName,
                groupName,
                mavenRepo,
                obr,
                stream,
                local,
                trace,
                tags,
                overrides,
                sharedEnvironmentPhase,
                sharedEnvironmentRunName,
                language,
                submissionId
            );
        }, FrameworkException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("Not enough request type numbers available, looped twice");
    }

    @Test
    public void testSubmitRunWithNoTestNameThrowsCorrectError() throws Exception {
        // Given...
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        MockFramework mockFramework = new MockFramework(mockCps, mockDss);

        FrameworkRuns frameworkRuns = new FrameworkRuns(mockFramework);

        String testName = null;

        String submissionId = "submission1";
        String runType = "unknown";
        String requestor = "me";
        String bundleName = "mybundle";
        String groupName = "my.group";
        String mavenRepo = "https://my.maven.repo";
        String obr = "mvn:my.group/my.group.obr/0.38.0/obr";
        String stream = "a-test-stream";
        boolean local = true;
        boolean trace = true;
        Set<String> tags = null ;

        Properties overrides = new Properties();

        SharedEnvironmentPhase sharedEnvironmentPhase = null;
        String sharedEnvironmentRunName = null;
        String language = "java";

        // When...
        FrameworkException thrown = catchThrowableOfType(() -> {
            frameworkRuns.submitRun(
                runType,
                requestor,
                bundleName,
                testName,
                groupName,
                mavenRepo,
                obr,
                stream,
                local,
                trace,
                tags,
                overrides,
                sharedEnvironmentPhase,
                sharedEnvironmentRunName,
                language,
                submissionId
            );
        }, FrameworkException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Missing test name");
    }

    @Test
    public void testSubmitRunWithNoBundleNameThrowsCorrectError() throws Exception {
        // Given...
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        MockFramework mockFramework = new MockFramework(mockCps, mockDss);

        FrameworkRuns frameworkRuns = new FrameworkRuns(mockFramework);

        String bundleName = null;
        
        String testName = "mytest";
        String submissionId = "submission1";
        String runType = "unknown";
        String requestor = "me";
        String groupName = "my.group";
        String mavenRepo = "https://my.maven.repo";
        String obr = "mvn:my.group/my.group.obr/0.38.0/obr";
        String stream = "a-test-stream";
        boolean local = true;
        boolean trace = true;
        Set<String> tags = null ;

        Properties overrides = new Properties();

        SharedEnvironmentPhase sharedEnvironmentPhase = null;
        String sharedEnvironmentRunName = null;
        String language = "java";

        // When...
        FrameworkException thrown = catchThrowableOfType(() -> {
            frameworkRuns.submitRun(
                runType,
                requestor,
                bundleName,
                testName,
                groupName,
                mavenRepo,
                obr,
                stream,
                local,
                trace,
                tags,
                overrides,
                sharedEnvironmentPhase,
                sharedEnvironmentRunName,
                language,
                submissionId
            );
        }, FrameworkException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Missing bundle name");
    }

    @Test
    public void testSubmitRunWithNoLanguageDefaultsToJavaBundleFormat() throws Exception {
        // Given...
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        MockFramework mockFramework = new MockFramework(mockCps, mockDss);

        FrameworkRuns frameworkRuns = new FrameworkRuns(mockFramework);

        String language = null;

        String bundleName = "mybundle";
        String testName = "mytest";
        String submissionId = "submission1";
        String runType = "unknown";
        String requestor = "me";
        String groupName = "my.group";
        String mavenRepo = "https://my.maven.repo";
        String obr = "mvn:my.group/my.group.obr/0.38.0/obr";
        String stream = "a-test-stream";
        boolean local = true;
        boolean trace = true;
        Set<String> tags = null ;

        Properties overrides = new Properties();

        SharedEnvironmentPhase sharedEnvironmentPhase = null;
        String sharedEnvironmentRunName = null;

        // When...
        IRun run = frameworkRuns.submitRun(
            runType,
            requestor,
            bundleName,
            testName,
            groupName,
            mavenRepo,
            obr,
            stream,
            local,
            trace,
            tags,
            overrides,
            sharedEnvironmentPhase,
            sharedEnvironmentRunName,
            language,
            submissionId
        );

        // Then...
        assertThat(run).isNotNull();
        assertThat(run.getName()).isEqualTo("U1");
        assertThat(run.getTest()).isEqualTo(bundleName + "/" + testName);
    }

    @Test
    public void testSubmitRunWithLocalRunTypeSetsRunPrefixCorrectly() throws Exception {
        // Given...
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        MockFramework mockFramework = new MockFramework(mockCps, mockDss);

        FrameworkRuns frameworkRuns = new FrameworkRuns(mockFramework);

        String runType = "local";
        
        String submissionId = "submission1";
        String language = "java";
        String bundleName = "mybundle";
        String testName = "mytest";
        String requestor = "me";
        String groupName = "my.group";
        String mavenRepo = "https://my.maven.repo";
        String obr = "mvn:my.group/my.group.obr/0.38.0/obr";
        String stream = "a-test-stream";
        boolean local = true;
        boolean trace = true;
        Set<String> tags = null ;

        Properties overrides = new Properties();

        SharedEnvironmentPhase sharedEnvironmentPhase = null;
        String sharedEnvironmentRunName = null;

        // When...
        IRun run = frameworkRuns.submitRun(
            runType,
            requestor,
            bundleName,
            testName,
            groupName,
            mavenRepo,
            obr,
            stream,
            local,
            trace,
            tags,
            overrides,
            sharedEnvironmentPhase,
            sharedEnvironmentRunName,
            language,
            submissionId
        );

        // Then...
        assertThat(run).isNotNull();
        assertThat(run.getType()).isEqualTo(runType.toUpperCase());
        assertThat(run.getName()).isEqualTo("L1");
    }

    @Test
    public void testSubmitRunWithGherkinReturnsRunCorrectly() throws Exception {
        // Given...
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        MockFramework mockFramework = new MockFramework(mockCps, mockDss);

        FrameworkRuns frameworkRuns = new FrameworkRuns(mockFramework);

        String language = "gherkin";
        
        String submissionId = "submission1";
        String runType = "local";
        String bundleName = "mybundle";
        String testName = "mygherkintest";
        String requestor = "me";
        String groupName = "my.group";
        String mavenRepo = "https://my.maven.repo";
        String obr = "mvn:my.group/my.group.obr/0.38.0/obr";
        String stream = "a-test-stream";
        boolean local = true;
        boolean trace = true;
        Set<String> tags = null ;

        Properties overrides = new Properties();

        SharedEnvironmentPhase sharedEnvironmentPhase = null;
        String sharedEnvironmentRunName = null;

        // When...
        IRun run = frameworkRuns.submitRun(
            runType,
            requestor,
            bundleName,
            testName,
            groupName,
            mavenRepo,
            obr,
            stream,
            local,
            trace,
            tags,
            overrides,
            sharedEnvironmentPhase,
            sharedEnvironmentRunName,
            language,
            submissionId
        );

        // Then...
        assertThat(run).isNotNull();
        assertThat(run.getName()).isEqualTo("L1");
        assertThat(run.getGherkin()).isEqualTo(testName);
        assertThat(run.getTestBundleName()).isEqualTo(null);

        // Check that the DSS has been populated with the correct run-related properties
        assertThat(mockDss.get("run.L1.gherkin")).isEqualTo(testName);
        assertThat(mockDss.get("run.L1.testclass")).isEqualTo(testName);
        assertThat(mockDss.get("run.L1.testbundle")).isEqualTo("none");
        assertThat(mockDss.get("request.prefix.L.lastused")).isEqualTo("1");
        assertThat(mockDss.get("run.L1.obr")).isEqualTo(obr);
        assertThat(mockDss.get("run.L1.group")).isEqualTo(groupName);
        assertThat(mockDss.get("run.L1.requestor")).isEqualTo(requestor);
        assertThat(mockDss.get("run.L1.repository")).isEqualTo(mavenRepo);
        assertThat(mockDss.get("run.L1.stream")).isEqualTo(stream);
        assertThat(mockDss.get("run.L1.local")).isEqualTo(Boolean.toString(local));
        assertThat(mockDss.get("run.L1.trace")).isEqualTo(Boolean.toString(trace));
        assertThat(mockDss.get("run.L1.request.type")).isEqualTo(runType.toUpperCase());
        assertThat(mockDss.get("run.L1.status")).isEqualTo("queued");
    }

    @Test
    public void testSubmitRunWithSharedEnvironmentBuildPhaseReturnsRunCorrectly() throws Exception {
        // Given...
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        MockFramework mockFramework = new MockFramework(mockCps, mockDss);

        FrameworkRuns frameworkRuns = new FrameworkRuns(mockFramework);

        SharedEnvironmentPhase sharedEnvironmentPhase = SharedEnvironmentPhase.BUILD;
        String sharedEnvironmentRunName = "SHARED-RUN1";

        String submissionId = "submission1";
        String language = "java";
        String runType = "local";
        String bundleName = "mybundle";
        String testName = "mysharedenvtest";
        String requestor = "me";
        String groupName = "my.group";
        String mavenRepo = "https://my.maven.repo";
        String obr = "mvn:my.group/my.group.obr/0.38.0/obr";
        String stream = "a-test-stream";
        boolean local = true;
        boolean trace = true;
        Set<String> tags = null ;

        Properties overrides = new Properties();

        // When...
        IRun run = frameworkRuns.submitRun(
            runType,
            requestor,
            bundleName,
            testName,
            groupName,
            mavenRepo,
            obr,
            stream,
            local,
            trace,
            tags,
            overrides,
            sharedEnvironmentPhase,
            sharedEnvironmentRunName,
            language,
            submissionId
        );

        // Then...
        assertThat(run).isNotNull();
        assertThat(run.getName()).isEqualTo(sharedEnvironmentRunName);

        Properties expectedOverrides = new Properties();
        expectedOverrides.put("framework.run.shared.environment.phase", "BUILD");

        // Check that the DSS has been populated with the correct run-related properties
        assertThat(mockDss.get("run.SHARED-RUN1.overrides")).isEqualTo(getExpectedOverridesJson(expectedOverrides));
        assertThat(mockDss.get("run.SHARED-RUN1.shared.environment")).isEqualTo("true");
        assertThat(mockDss.get("run.SHARED-RUN1.obr")).isEqualTo(obr);
        assertThat(mockDss.get("run.SHARED-RUN1.group")).isEqualTo(groupName);
        assertThat(mockDss.get("run.SHARED-RUN1.requestor")).isEqualTo(requestor);
        assertThat(mockDss.get("run.SHARED-RUN1.testbundle")).isEqualTo(bundleName);
        assertThat(mockDss.get("run.SHARED-RUN1.repository")).isEqualTo(mavenRepo);
        assertThat(mockDss.get("run.SHARED-RUN1.stream")).isEqualTo(stream);
        assertThat(mockDss.get("run.SHARED-RUN1.local")).isEqualTo(Boolean.toString(local));
        assertThat(mockDss.get("run.SHARED-RUN1.testclass")).isEqualTo(testName);
        assertThat(mockDss.get("run.SHARED-RUN1.trace")).isEqualTo(Boolean.toString(trace));
        assertThat(mockDss.get("run.SHARED-RUN1.request.type")).isEqualTo(runType.toUpperCase());
        assertThat(mockDss.get("run.SHARED-RUN1.status")).isEqualTo("queued");
    }

    @Test
    public void testSubmitRunWithSharedEnvironmentBuildPhaseAndNoRunNameThrowsError() throws Exception {
        // Given...
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        MockFramework mockFramework = new MockFramework(mockCps, mockDss);

        FrameworkRuns frameworkRuns = new FrameworkRuns(mockFramework);

        SharedEnvironmentPhase sharedEnvironmentPhase = SharedEnvironmentPhase.BUILD;
        String sharedEnvironmentRunName = null;

        String submissionId = "submission1";
        String language = "java";
        String runType = "local";
        String bundleName = "mybundle";
        String testName = "mysharedenvtest";
        String requestor = "me";
        String groupName = "my.group";
        String mavenRepo = "https://my.maven.repo";
        String obr = "mvn:my.group/my.group.obr/0.38.0/obr";
        String stream = "a-test-stream";
        boolean local = true;
        boolean trace = true;
        Set<String> tags = null ;

        Properties overrides = new Properties();

        // When...
        FrameworkException thrown = catchThrowableOfType(() -> {
            frameworkRuns.submitRun(
                runType,
                requestor,
                bundleName,
                testName,
                groupName,
                mavenRepo,
                obr,
                stream,
                local,
                trace,
                tags,
                overrides,
                sharedEnvironmentPhase,
                sharedEnvironmentRunName,
                language,
                submissionId
            );
        }, FrameworkException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("Missing run name for shared environment");
    }

    @Test
    public void testSubmitRunWithSharedEnvironmentDiscardPhaseReturnsRunCorrectly() throws Exception {
        // Given...
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        String sharedEnvironmentRunName = "SHARED-RUN1";

        mockDss.put("run." + sharedEnvironmentRunName + ".shared.environment", "true");
        mockDss.put("run." + sharedEnvironmentRunName + ".status", "up");

        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        MockFramework mockFramework = new MockFramework(mockCps, mockDss);

        FrameworkRuns frameworkRuns = new FrameworkRuns(mockFramework);

        SharedEnvironmentPhase sharedEnvironmentPhase = SharedEnvironmentPhase.DISCARD;

        String submissionId = "submission1";
        String language = "java";
        String runType = "local";
        String bundleName = "mybundle";
        String testName = "mysharedenvtest";
        String requestor = "me";
        String groupName = "my.group";
        String mavenRepo = "https://my.maven.repo";
        String obr = "mvn:my.group/my.group.obr/0.38.0/obr";
        String stream = "a-test-stream";
        boolean local = true;
        boolean trace = true;
        Set<String> tags = null ;

        Properties overrides = new Properties();

        // When...
        IRun run = frameworkRuns.submitRun(
            runType,
            requestor,
            bundleName,
            testName,
            groupName,
            mavenRepo,
            obr,
            stream,
            local,
            trace,
            tags,
            overrides,
            sharedEnvironmentPhase,
            sharedEnvironmentRunName,
            language,
            submissionId
        );

        // Then...
        assertThat(run).isNotNull();
        assertThat(run.getName()).isEqualTo(sharedEnvironmentRunName);

        // Check that the DSS has been populated with the correct run-related properties
        assertThat(mockDss.get("run.SHARED-RUN1.overrides")).isEqualTo("framework.run.shared.environment.phase=DISCARD");
        assertThat(mockDss.get("run.SHARED-RUN1.shared.environment")).isEqualTo("true");
        assertThat(mockDss.get("run.SHARED-RUN1.group")).isEqualTo(groupName);
        assertThat(mockDss.get("run.SHARED-RUN1.status")).isEqualTo("queued");
    }

    @Test
    public void testSubmitRunWithDuplicateSharedEnvironmentBuildPhaseRunThrowsError() throws Exception {
        // Given...
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        mockDss.setSwapSetToFail(true);

        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        String sharedEnvironmentRunName = "SHARED-RUN1";

        mockDss.put("run." + sharedEnvironmentRunName + ".test", "existing/test");
        MockFramework mockFramework = new MockFramework(mockCps, mockDss);

        FrameworkRuns frameworkRuns = new FrameworkRuns(mockFramework);

        SharedEnvironmentPhase sharedEnvironmentPhase = SharedEnvironmentPhase.BUILD;

        String submissionId = "submission1";
        String language = "java";
        String runType = "local";
        String bundleName = "mybundle";
        String testName = "mysharedenvtest";
        String requestor = "me";
        String groupName = "my.group";
        String mavenRepo = "https://my.maven.repo";
        String obr = "mvn:my.group/my.group.obr/0.38.0/obr";
        String stream = "a-test-stream";
        boolean local = true;
        boolean trace = true;
        Set<String> tags = null ;

        Properties overrides = new Properties();

        // When...
        FrameworkException thrown = catchThrowableOfType(() -> {
            frameworkRuns.submitRun(
                runType,
                requestor,
                bundleName,
                testName,
                groupName,
                mavenRepo,
                obr,
                stream,
                local,
                trace,
                tags,
                overrides,
                sharedEnvironmentPhase,
                sharedEnvironmentRunName,
                language,
                submissionId
            );
        }, FrameworkException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Unable to submit shared environment run", sharedEnvironmentRunName, "is there a duplicate runname?");
    }

    @Test
    public void testCancelRunSetsInterruptReasonAndDeferredRasActionOk() throws Exception {
        // Given...
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        MockFramework mockFramework = new MockFramework(mockCps, mockDss);

        FrameworkRuns frameworkRuns = new FrameworkRuns(mockFramework);

        String runName = "mytestrun1";
        String rasRunId = "my-run-document-id";

        // Put a run-related property into the DSS to show that the run with the given name exists in the DSS
        mockDss.put("run." + runName + ".rasrunid", rasRunId);

        // When...
        boolean isRunMarkedCancelled = frameworkRuns.markRunInterrupted(runName, Result.CANCELLED);

        // Then...
        assertThat(isRunMarkedCancelled).isTrue();
        assertThat(mockDss.get("run." + runName + ".interruptReason")).isEqualTo(Result.CANCELLED);

        // We expect the 'rasActions' property to be populated with a base64-encoded JSON structure
        List<RunRasAction> expectedRasActions = new ArrayList<>();
        RunRasAction rasAction = new RunRasAction(rasRunId, TestRunLifecycleStatus.FINISHED.toString(), Result.CANCELLED);
        expectedRasActions.add(rasAction);
        String expectedJsonStr = gson.toJson(expectedRasActions);
        String expectedEncodedStr = Base64.getEncoder().encodeToString(expectedJsonStr.getBytes(StandardCharsets.UTF_8));

        assertThat(mockDss.get("run." + runName + ".rasActions")).isEqualTo(expectedEncodedStr);
    }

    @Test
    public void testCancelNonExistantRunReturnsFalse() throws Exception {
        // Given...
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        MockFramework mockFramework = new MockFramework(mockCps, mockDss);

        FrameworkRuns frameworkRuns = new FrameworkRuns(mockFramework);

        String runName = "mytestrun1";

        // When...
        boolean isRunMarkedCancelled = frameworkRuns.markRunInterrupted(runName, Result.CANCELLED);

        // Then...
        assertThat(isRunMarkedCancelled).isFalse();
    }

    @Test
    public void testCancelRunOnAlreadyCancelledRunDoesNotUpdateDssAgain() throws Exception {
        // Given...
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        MockFramework mockFramework = new MockFramework(mockCps, mockDss);

        FrameworkRuns frameworkRuns = new FrameworkRuns(mockFramework);

        String runName = "mytestrun1";
        String rasRunId = "my-run-document-id";

        List<RunRasAction> existingRasActions = new ArrayList<>();
        RunRasAction rasAction = new RunRasAction(rasRunId, TestRunLifecycleStatus.FINISHED.toString(), Result.CANCELLED);
        existingRasActions.add(rasAction);

        String rasActionJsonStr = gson.toJson(existingRasActions);
        String encodedRasActionStr = Base64.getEncoder().encodeToString(rasActionJsonStr.getBytes(StandardCharsets.UTF_8));

        // Mark the run as cancelled already
        mockDss.put("run." + runName + ".rasrunid", rasRunId);
        mockDss.put("run." + runName + ".status", TestRunLifecycleStatus.FINISHED.toString());
        mockDss.put("run." + runName + ".result", Result.CANCELLED);
        mockDss.put("run." + runName + ".rasActions", encodedRasActionStr);

        // When...
        boolean isRunMarkedCancelled = frameworkRuns.markRunInterrupted(runName, Result.CANCELLED);

        // Then...
        assertThat(isRunMarkedCancelled).isTrue();

        // We don't want the 'rasActions' property to have changed
        assertThat(mockDss.get("run." + runName + ".rasActions")).isEqualTo(encodedRasActionStr);
    }

    @Test
    public void testCancelRunOnAlreadyMarkedRunDoesNotUpdateDssAgain() throws Exception {
        // Given...
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        MockFramework mockFramework = new MockFramework(mockCps, mockDss);

        FrameworkRuns frameworkRuns = new FrameworkRuns(mockFramework);

        String runName = "mytestrun1";
        String rasRunId = "my-run-document-id";

        List<RunRasAction> existingRasActions = new ArrayList<>();
        RunRasAction rasAction = new RunRasAction(rasRunId, TestRunLifecycleStatus.FINISHED.toString(), Result.CANCELLED);
        existingRasActions.add(rasAction);

        String rasActionJsonStr = gson.toJson(existingRasActions);
        String encodedRasActionStr = Base64.getEncoder().encodeToString(rasActionJsonStr.getBytes(StandardCharsets.UTF_8));

        // Mark the run as cancelled already
        mockDss.put("run." + runName + ".rasrunid", rasRunId);
        mockDss.put("run." + runName + ".interruptReason", Result.CANCELLED);
        mockDss.put("run." + runName + ".rasActions", encodedRasActionStr);

        // When...
        boolean isRunMarkedCancelled = frameworkRuns.markRunInterrupted(runName, Result.CANCELLED);

        // Then...
        assertThat(isRunMarkedCancelled).isTrue();

        // We don't want the 'rasActions' property to have changed
        assertThat(mockDss.get("run." + runName + ".rasActions")).isEqualTo(encodedRasActionStr);
    }

    @Test
    public void testAddRasActionAddsRasActionToDss() throws Exception {
        // Given...
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        MockFramework mockFramework = new MockFramework(mockCps, mockDss);

        FrameworkRuns frameworkRuns = new FrameworkRuns(mockFramework);

        String runName = "mytestrun1";
        
        MockRun mockRun = new MockRun(null, null, runName, null, null, null, null, false);
        
        String rasRunId = "my-run-document-id";
        RunRasAction rasAction = new RunRasAction(rasRunId, TestRunLifecycleStatus.FINISHED.toString(), Result.CANCELLED);
        
        List<RunRasAction> rasActions = new ArrayList<>();
        rasActions.add(rasAction);
        String rasActionJsonStr = gson.toJson(rasActions);
        String encodedRasActionStr = Base64.getEncoder().encodeToString(rasActionJsonStr.getBytes(StandardCharsets.UTF_8));

        // When...
        frameworkRuns.addRunRasAction(mockRun, rasAction);

        // Then...
        assertThat(mockDss.get("run." + runName + ".rasActions")).isEqualTo(encodedRasActionStr);
    }

    @Test
    public void testmarkRunInterruptedSetsInterruptReasonAndRasActionOk() throws Exception {
        // Given...
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        MockFramework mockFramework = new MockFramework(mockCps, mockDss);

        FrameworkRuns frameworkRuns = new FrameworkRuns(mockFramework);

        String runName = "mytestrun1";
        String rasRunId = "my-run-document-id";

        // Put a run-related property into the DSS to show that the run with the given name exists in the DSS
        mockDss.put("run." + runName + ".rasrunid", rasRunId);

        // When...
        boolean isRunMarkedRequeued = frameworkRuns.markRunInterrupted(runName, Result.REQUEUED);

        // Then...
        assertThat(isRunMarkedRequeued).isTrue();
        assertThat(mockDss.get("run." + runName + ".interruptReason")).isEqualTo(Result.REQUEUED);

        // We expect the 'rasActions' property to be populated with a base64-encoded JSON structure
        List<RunRasAction> expectedRasActions = new ArrayList<>();
        RunRasAction rasAction = new RunRasAction(rasRunId, TestRunLifecycleStatus.FINISHED.toString(), Result.REQUEUED);
        expectedRasActions.add(rasAction);
        String expectedJsonStr = gson.toJson(expectedRasActions);
        String expectedEncodedStr = Base64.getEncoder().encodeToString(expectedJsonStr.getBytes(StandardCharsets.UTF_8));

        assertThat(mockDss.get("run." + runName + ".rasActions")).isEqualTo(expectedEncodedStr);
    }

    @Test
    public void testRequeueRunOnAlreadyMarkedRunDoesNotUpdateDssAgain() throws Exception {
        // Given...
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        MockFramework mockFramework = new MockFramework(mockCps, mockDss);

        FrameworkRuns frameworkRuns = new FrameworkRuns(mockFramework);

        String runName = "mytestrun1";
        String rasRunId = "my-run-document-id";

        List<RunRasAction> existingRasActions = new ArrayList<>();
        RunRasAction rasAction = new RunRasAction(rasRunId, TestRunLifecycleStatus.FINISHED.toString(), Result.REQUEUED);
        existingRasActions.add(rasAction);

        String rasActionJsonStr = gson.toJson(existingRasActions);
        String encodedRasActionStr = Base64.getEncoder().encodeToString(rasActionJsonStr.getBytes(StandardCharsets.UTF_8));

        // Mark the run as requeued already
        mockDss.put("run." + runName + ".rasrunid", rasRunId);
        mockDss.put("run." + runName + ".interruptReason", Result.REQUEUED);
        mockDss.put("run." + runName + ".rasActions", encodedRasActionStr);

        // When...
        boolean isRunMarkedRequeued = frameworkRuns.markRunInterrupted(runName, Result.REQUEUED);

        // Then...
        assertThat(isRunMarkedRequeued).isTrue();

        // We don't want the 'rasActions' property to have changed
        assertThat(mockDss.get("run." + runName + ".rasActions")).isEqualTo(encodedRasActionStr);
    }

    @Test
    public void testRequeueRunOnNonExistantRunDoesNotUpdateDss() throws Exception {
        // Given...
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        MockFramework mockFramework = new MockFramework(mockCps, mockDss);

        FrameworkRuns frameworkRuns = new FrameworkRuns(mockFramework);

        // Don't set any DSS properties for the run
        String runName = "mytestrun1";

        // When...
        boolean isRunMarkedRequeued = frameworkRuns.markRunInterrupted(runName, Result.REQUEUED);

        // Then...
        assertThat(isRunMarkedRequeued).isFalse();
    }

    @Test
    public void testResetRunOnLocalRunDoesNotUpdateDss() throws Exception {
        // Given...
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        MockFramework mockFramework = new MockFramework(mockCps, mockDss);

        FrameworkRuns frameworkRuns = new FrameworkRuns(mockFramework);

        String runName = "mytestrun1";

        // Mark the run as local
        mockDss.put("run." + runName + ".local", "true");

        // When...
        boolean isRunReset = frameworkRuns.reset(runName);

        // Then...
        assertThat(isRunReset).isFalse();
    }

    @Test
    public void testResetRunOnNonExistantRunDoesNotUpdateDss() throws Exception {
        // Given...
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        MockFramework mockFramework = new MockFramework(mockCps, mockDss);

        FrameworkRuns frameworkRuns = new FrameworkRuns(mockFramework);

        // Don't set any DSS properties for the run
        String runName = "mytestrun1";


        // When...
        boolean isRunReset = frameworkRuns.reset(runName);

        // Then...
        assertThat(isRunReset).isFalse();
    }

    @Test
    public void testResetRunUpdatesStatusAndRemovesInterruptReasonAndHeartbeat() throws Exception {
        // Given...
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        MockFramework mockFramework = new MockFramework(mockCps, mockDss);

        FrameworkRuns frameworkRuns = new FrameworkRuns(mockFramework);

        String runName = "mytestrun1";
        String rasRunId = "my-run-document-id";

        mockDss.put("run." + runName + ".rasrunid", rasRunId);
        mockDss.put("run." + runName + ".interruptReason", Result.REQUEUED);
        mockDss.put("run." + runName + ".heartbeat", Instant.now().toString());

        // When...
        boolean isRunReset = frameworkRuns.reset(runName);

        // Then...
        assertThat(isRunReset).isTrue();
        assertThat(mockDss.get("run." + runName + ".interruptReason")).isNull();
        assertThat(mockDss.get("run." + runName + ".heartbeat")).isNull();
        assertThat(mockDss.get("run." + runName + ".status")).isEqualTo(TestRunLifecycleStatus.QUEUED.toString());
    }
}
