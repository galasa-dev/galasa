/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.spi.ras.RasSearchCriteriaBundle;
import dev.galasa.framework.spi.ras.RasSearchCriteriaQueuedFrom;
import dev.galasa.framework.spi.ras.RasSearchCriteriaQueuedTo;
import dev.galasa.framework.spi.ras.RasSearchCriteriaRequestor;
import dev.galasa.framework.spi.ras.RasSearchCriteriaResult;
import dev.galasa.framework.spi.ras.RasSearchCriteriaRunName;
import dev.galasa.framework.spi.ras.RasSearchCriteriaStatus;
import dev.galasa.framework.spi.ras.RasSearchCriteriaTags;
import dev.galasa.framework.spi.ras.RasSearchCriteriaTestName;
import dev.galasa.framework.spi.ras.RasSearchCriteriaUser;

public class CouchdbRasQueryBuilderTest {

    @Test
    public void testCanBuildQueryWithNoSearchCriteria() throws Exception {
        // Given...
        CouchdbRasQueryBuilder builder = new CouchdbRasQueryBuilder();

        // When...
        JsonObject query = builder.buildGetRunsQuery();

        // Then...
        assertThat(query).isNotNull();
        assertThat(query.has("$and")).isTrue();
        JsonArray andArray = query.getAsJsonArray("$and");
        assertThat(andArray).isEmpty();
    }

    @Test
    public void testCanBuildQueryWithSingleRunName() throws Exception {
        // Given...
        CouchdbRasQueryBuilder builder = new CouchdbRasQueryBuilder();
        RasSearchCriteriaRunName runNameCriteria = new RasSearchCriteriaRunName("L10");

        // When...
        JsonObject query = builder.buildGetRunsQuery(runNameCriteria);

        // Then...
        assertThat(query).isNotNull();
        assertThat(query.has("$and")).isTrue();
        JsonArray andArray = query.getAsJsonArray("$and");
        assertThat(andArray).hasSize(1);

        JsonObject condition = andArray.get(0).getAsJsonObject();
        assertThat(condition.has("runName")).isTrue();
        assertThat(condition.get("runName").getAsString()).isEqualTo("L10");
    }

    @Test
    public void testCanBuildQueryWithMultipleRunNames() throws Exception {
        // Given...
        CouchdbRasQueryBuilder builder = new CouchdbRasQueryBuilder();
        RasSearchCriteriaRunName runNameCriteria = new RasSearchCriteriaRunName("L10", "L20", "L30");

        // When...
        JsonObject query = builder.buildGetRunsQuery(runNameCriteria);

        // Then...
        assertThat(query).isNotNull();
        JsonArray andArray = query.getAsJsonArray("$and");
        assertThat(andArray).hasSize(1);

        JsonObject condition = andArray.get(0).getAsJsonObject();
        JsonArray inArray = condition.getAsJsonObject("runName").getAsJsonArray("$in");
        assertThat(inArray).hasSize(3);
        assertThat(inArray.get(0).getAsString()).isEqualTo("L10");
        assertThat(inArray.get(1).getAsString()).isEqualTo("L20");
        assertThat(inArray.get(2).getAsString()).isEqualTo("L30");
    }

    @Test
    public void testCanBuildQueryWithMultipleTags() throws Exception {
        // Given...
        CouchdbRasQueryBuilder builder = new CouchdbRasQueryBuilder();
        RasSearchCriteriaTags tagsCriteria = new RasSearchCriteriaTags("tag1", "tag2", "tag3");

        // When...
        JsonObject query = builder.buildGetRunsQuery(tagsCriteria);

        // Then...
        assertThat(query).isNotNull();
        JsonArray andArray = query.getAsJsonArray("$and");
        assertThat(andArray).hasSize(1);

        JsonObject condition = andArray.get(0).getAsJsonObject();
        JsonArray inArray = condition.getAsJsonObject("tags").getAsJsonArray("$in");
        assertThat(inArray).hasSize(3);
        assertThat(inArray.get(0).getAsString()).isEqualTo("tag1");
        assertThat(inArray.get(1).getAsString()).isEqualTo("tag2");
        assertThat(inArray.get(2).getAsString()).isEqualTo("tag3");
    }

    @Test
    public void testCanBuildQueryWithQueuedFromCriteria() throws Exception {
        // Given...
        CouchdbRasQueryBuilder builder = new CouchdbRasQueryBuilder();
        Instant fromTime = Instant.parse("2024-01-01T10:00:00Z");
        RasSearchCriteriaQueuedFrom queuedFromCriteria = new RasSearchCriteriaQueuedFrom(fromTime);

        // When...
        JsonObject query = builder.buildGetRunsQuery(queuedFromCriteria);

        // Then...
        assertThat(query).isNotNull();
        JsonArray andArray = query.getAsJsonArray("$and");
        assertThat(andArray).hasSize(1);

        JsonObject condition = andArray.get(0).getAsJsonObject();
        assertThat(condition.has("queued")).isTrue();
        JsonObject queuedCondition = condition.getAsJsonObject("queued");
        assertThat(queuedCondition.has("$gte")).isTrue();
        assertThat(queuedCondition.get("$gte").getAsString()).isEqualTo(fromTime.toString());
    }

    @Test
    public void testCanBuildQueryWithQueuedToCriteria() throws Exception {
        // Given...
        CouchdbRasQueryBuilder builder = new CouchdbRasQueryBuilder();
        Instant toTime = Instant.parse("2024-12-31T23:59:59Z");
        RasSearchCriteriaQueuedTo queuedToCriteria = new RasSearchCriteriaQueuedTo(toTime);

        // When...
        JsonObject query = builder.buildGetRunsQuery(queuedToCriteria);

        // Then...
        assertThat(query).isNotNull();
        JsonArray andArray = query.getAsJsonArray("$and");
        assertThat(andArray).hasSize(1);

        JsonObject condition = andArray.get(0).getAsJsonObject();
        assertThat(condition.has("queued")).isTrue();
        JsonObject queuedCondition = condition.getAsJsonObject("queued");
        assertThat(queuedCondition.has("$lt")).isTrue();
        assertThat(queuedCondition.get("$lt").getAsString()).isEqualTo(toTime.toString());
    }

    @Test
    public void testCanBuildQueryWithQueuedFromAndToCriteria() throws Exception {
        // Given...
        CouchdbRasQueryBuilder builder = new CouchdbRasQueryBuilder();
        Instant fromTime = Instant.parse("2024-01-01T00:00:00Z");
        Instant toTime = Instant.parse("2024-12-31T23:59:59Z");
        RasSearchCriteriaQueuedFrom queuedFromCriteria = new RasSearchCriteriaQueuedFrom(fromTime);
        RasSearchCriteriaQueuedTo queuedToCriteria = new RasSearchCriteriaQueuedTo(toTime);

        // When...
        JsonObject query = builder.buildGetRunsQuery(queuedFromCriteria, queuedToCriteria);

        // Then...
        assertThat(query).isNotNull();
        JsonArray andArray = query.getAsJsonArray("$and");
        assertThat(andArray).hasSize(2);

        // Check first condition (queued from)
        JsonObject fromCondition = andArray.get(0).getAsJsonObject();
        assertThat(fromCondition.getAsJsonObject("queued").get("$gte").getAsString()).isEqualTo(fromTime.toString());

        // Check second condition (queued to)
        JsonObject toCondition = andArray.get(1).getAsJsonObject();
        assertThat(toCondition.getAsJsonObject("queued").get("$lt").getAsString()).isEqualTo(toTime.toString());
    }

    @Test
    public void testCanBuildQueryWithUserCriteriaOnly() throws Exception {
        // Given...
        CouchdbRasQueryBuilder builder = new CouchdbRasQueryBuilder();
        RasSearchCriteriaUser userCriteria = new RasSearchCriteriaUser("alice");

        // When...
        JsonObject query = builder.buildGetRunsQuery(userCriteria);

        // Then...
        assertThat(query).isNotNull();
        JsonArray andArray = query.getAsJsonArray("$and");
        assertThat(andArray).hasSize(1);

        // When only user is provided, it should create an $or condition matching either user or requestor
        JsonObject orWrapper = andArray.get(0).getAsJsonObject();
        assertThat(orWrapper.has("$or")).isTrue();
        JsonArray orArray = orWrapper.getAsJsonArray("$or");
        assertThat(orArray).hasSize(2);

        // Check user condition
        JsonObject userCondition = orArray.get(0).getAsJsonObject();
        assertThat(userCondition.has("user")).isTrue();
        assertThat(userCondition.get("user").getAsString()).isEqualTo("alice");

        // Check requestor condition (should also match alice)
        JsonObject requestorCondition = orArray.get(1).getAsJsonObject();
        assertThat(requestorCondition.has("requestor")).isTrue();
        assertThat(requestorCondition.get("requestor").getAsString()).isEqualTo("alice");
    }

    @Test
    public void testCanBuildQueryWithRequestorCriteriaOnly() throws Exception {
        // Given...
        CouchdbRasQueryBuilder builder = new CouchdbRasQueryBuilder();
        RasSearchCriteriaRequestor requestorCriteria = new RasSearchCriteriaRequestor("bob");

        // When...
        JsonObject query = builder.buildGetRunsQuery(requestorCriteria);

        // Then...
        assertThat(query).isNotNull();
        JsonArray andArray = query.getAsJsonArray("$and");
        assertThat(andArray).hasSize(1);

        // When only requestor is provided, it should create a strict match on requestor
        JsonObject condition = andArray.get(0).getAsJsonObject();
        assertThat(condition.has("requestor")).isTrue();
        assertThat(condition.get("requestor").getAsString()).isEqualTo("bob");
    }

    @Test
    public void testCanBuildQueryWithBothUserAndRequestorCriteria() throws Exception {
        // Given...
        CouchdbRasQueryBuilder builder = new CouchdbRasQueryBuilder();
        RasSearchCriteriaUser userCriteria = new RasSearchCriteriaUser("alice");
        RasSearchCriteriaRequestor requestorCriteria = new RasSearchCriteriaRequestor("bob");

        // When...
        JsonObject query = builder.buildGetRunsQuery(userCriteria, requestorCriteria);

        // Then...
        assertThat(query).isNotNull();
        JsonArray andArray = query.getAsJsonArray("$and");
        assertThat(andArray).hasSize(2);

        // When both are provided, it should create strict matches on both
        JsonObject userCondition = andArray.get(0).getAsJsonObject();
        assertThat(userCondition.has("user")).isTrue();
        assertThat(userCondition.get("user").getAsString()).isEqualTo("alice");

        JsonObject requestorCondition = andArray.get(1).getAsJsonObject();
        assertThat(requestorCondition.has("requestor")).isTrue();
        assertThat(requestorCondition.get("requestor").getAsString()).isEqualTo("bob");
    }

    @Test
    public void testCanBuildQueryWithTestNameCriteria() throws Exception {
        // Given...
        CouchdbRasQueryBuilder builder = new CouchdbRasQueryBuilder();
        RasSearchCriteriaTestName testNameCriteria = new RasSearchCriteriaTestName("dev.galasa.test.MyTest");

        // When...
        JsonObject query = builder.buildGetRunsQuery(testNameCriteria);

        // Then...
        assertThat(query).isNotNull();
        JsonArray andArray = query.getAsJsonArray("$and");
        assertThat(andArray).hasSize(1);

        JsonObject condition = andArray.get(0).getAsJsonObject();
        assertThat(condition.has("testName")).isTrue();
        assertThat(condition.get("testName").getAsString()).isEqualTo("dev.galasa.test.MyTest");
    }

    @Test
    public void testCanBuildQueryWithBundleCriteria() throws Exception {
        // Given...
        CouchdbRasQueryBuilder builder = new CouchdbRasQueryBuilder();
        RasSearchCriteriaBundle bundleCriteria = new RasSearchCriteriaBundle("dev.galasa.test");

        // When...
        JsonObject query = builder.buildGetRunsQuery(bundleCriteria);

        // Then...
        assertThat(query).isNotNull();
        JsonArray andArray = query.getAsJsonArray("$and");
        assertThat(andArray).hasSize(1);

        JsonObject condition = andArray.get(0).getAsJsonObject();
        assertThat(condition.has("bundle")).isTrue();
        assertThat(condition.get("bundle").getAsString()).isEqualTo("dev.galasa.test");
    }

    @Test
    public void testCanBuildQueryWithResultCriteria() throws Exception {
        // Given...
        CouchdbRasQueryBuilder builder = new CouchdbRasQueryBuilder();
        RasSearchCriteriaResult resultCriteria = new RasSearchCriteriaResult("Passed");

        // When...
        JsonObject query = builder.buildGetRunsQuery(resultCriteria);

        // Then...
        assertThat(query).isNotNull();
        JsonArray andArray = query.getAsJsonArray("$and");
        assertThat(andArray).hasSize(1);

        JsonObject condition = andArray.get(0).getAsJsonObject();
        assertThat(condition.has("result")).isTrue();
        assertThat(condition.get("result").getAsString()).isEqualTo("Passed");
    }

    @Test
    public void testCanBuildQueryWithStatusCriteria() throws Exception {
        // Given...
        CouchdbRasQueryBuilder builder = new CouchdbRasQueryBuilder();
        List<TestRunLifecycleStatus> statuses = Arrays.asList(TestRunLifecycleStatus.FINISHED);
        RasSearchCriteriaStatus statusCriteria = new RasSearchCriteriaStatus(statuses);

        // When...
        JsonObject query = builder.buildGetRunsQuery(statusCriteria);

        // Then...
        assertThat(query).isNotNull();
        JsonArray andArray = query.getAsJsonArray("$and");
        assertThat(andArray).hasSize(1);

        JsonObject condition = andArray.get(0).getAsJsonObject();
        assertThat(condition.has("status")).isTrue();
        assertThat(condition.get("status").getAsString()).isEqualTo("finished");
    }

    @Test
    public void testCanBuildQueryWithMultipleCriteria() throws Exception {
        // Given...
        CouchdbRasQueryBuilder builder = new CouchdbRasQueryBuilder();
        Instant fromTime = Instant.parse("2024-01-01T00:00:00Z");
        RasSearchCriteriaQueuedFrom queuedFromCriteria = new RasSearchCriteriaQueuedFrom(fromTime);
        RasSearchCriteriaRunName runNameCriteria = new RasSearchCriteriaRunName("L10", "L20");
        RasSearchCriteriaTestName testNameCriteria = new RasSearchCriteriaTestName("dev.galasa.test.MyTest");
        RasSearchCriteriaResult resultCriteria = new RasSearchCriteriaResult("Passed");

        // When...
        JsonObject query = builder.buildGetRunsQuery(queuedFromCriteria, runNameCriteria, testNameCriteria, resultCriteria);

        // Then...
        assertThat(query).isNotNull();
        JsonArray andArray = query.getAsJsonArray("$and");
        assertThat(andArray).hasSize(4);

        // Verify queued from condition
        JsonObject queuedCondition = andArray.get(0).getAsJsonObject();
        assertThat(queuedCondition.has("queued")).isTrue();

        // Verify runName condition
        JsonObject runNameCondition = andArray.get(1).getAsJsonObject();
        assertThat(runNameCondition.has("runName")).isTrue();
        JsonArray inArray = runNameCondition.getAsJsonObject("runName").getAsJsonArray("$in");
        assertThat(inArray).hasSize(2);
        assertThat(inArray.get(0).getAsString()).isEqualTo("L10");
        assertThat(inArray.get(1).getAsString()).isEqualTo("L20");

        // Verify testName condition
        JsonObject testNameCondition = andArray.get(2).getAsJsonObject();
        assertThat(testNameCondition.has("testName")).isTrue();
        assertThat(testNameCondition.get("testName").getAsString()).isEqualTo("dev.galasa.test.MyTest");

        // Verify result condition
        JsonObject resultCondition = andArray.get(3).getAsJsonObject();
        assertThat(resultCondition.has("result")).isTrue();
        assertThat(resultCondition.get("result").getAsString()).isEqualTo("Passed");
    }

    @Test
    public void testCanBuildQueryWithEmptyStringArrayFiltersOutEmptyStrings() throws Exception {
        // Given...
        CouchdbRasQueryBuilder builder = new CouchdbRasQueryBuilder();
        RasSearchCriteriaRunName runNameCriteria = new RasSearchCriteriaRunName("L10", "", "L20", null);

        // When...
        JsonObject query = builder.buildGetRunsQuery(runNameCriteria);

        // Then...
        assertThat(query).isNotNull();
        JsonArray andArray = query.getAsJsonArray("$and");
        assertThat(andArray).hasSize(1);

        JsonObject condition = andArray.get(0).getAsJsonObject();
        JsonArray inArray = condition.getAsJsonObject("runName").getAsJsonArray("$in");

        // Should only contain non-null, non-empty strings
        assertThat(inArray).hasSize(2);
        assertThat(inArray.get(0).getAsString()).isEqualTo("L10");
        assertThat(inArray.get(1).getAsString()).isEqualTo("L20");
    }

    @Test
    public void testCanBuildComplexQueryWithAllCriteriaTypes() throws Exception {
        // Given...
        CouchdbRasQueryBuilder builder = new CouchdbRasQueryBuilder();
        RasSearchCriteriaRunName runNameCriteria = new RasSearchCriteriaRunName("L10");
        RasSearchCriteriaTestName testNameCriteria = new RasSearchCriteriaTestName("dev.galasa.test.MyTest");
        RasSearchCriteriaBundle bundleCriteria = new RasSearchCriteriaBundle("dev.galasa.test");
        RasSearchCriteriaResult resultCriteria = new RasSearchCriteriaResult("Passed");
        List<TestRunLifecycleStatus> statuses = Arrays.asList(TestRunLifecycleStatus.FINISHED);
        RasSearchCriteriaStatus statusCriteria = new RasSearchCriteriaStatus(statuses);
        RasSearchCriteriaUser userCriteria = new RasSearchCriteriaUser("alice");
        RasSearchCriteriaRequestor requestorCriteria = new RasSearchCriteriaRequestor("bob");
        Instant fromTime = Instant.parse("2024-01-01T00:00:00Z");
        Instant toTime = Instant.parse("2024-12-31T23:59:59Z");
        RasSearchCriteriaQueuedFrom queuedFromCriteria = new RasSearchCriteriaQueuedFrom(fromTime);
        RasSearchCriteriaQueuedTo queuedToCriteria = new RasSearchCriteriaQueuedTo(toTime);

        // When...
        JsonObject query = builder.buildGetRunsQuery(
            runNameCriteria,
            testNameCriteria,
            bundleCriteria,
            resultCriteria,
            statusCriteria,
            userCriteria,
            requestorCriteria,
            queuedFromCriteria,
            queuedToCriteria
        );

        // Then...
        assertThat(query).isNotNull();
        JsonArray andArray = query.getAsJsonArray("$and");

        // Should have: queuedFrom, queuedTo, runName, testName, bundle, result, status, user, requestor = 9 conditions
        assertThat(andArray).hasSize(9);
    }
}
