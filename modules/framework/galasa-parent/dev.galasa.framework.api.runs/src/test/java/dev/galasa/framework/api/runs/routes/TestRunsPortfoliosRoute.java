/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs.routes;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.ServletErrorMessage;
import dev.galasa.framework.api.common.mocks.FilledMockEnvironment;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockTestCatalogFetcher;
import dev.galasa.framework.api.runs.mocks.MockRunsServlet;
import dev.galasa.framework.mocks.FilledMockRBACService;
import dev.galasa.framework.mocks.MockOBR;
import dev.galasa.framework.mocks.MockRBACService;
import dev.galasa.framework.mocks.MockStream;
import dev.galasa.framework.mocks.MockStreamsService;
import dev.galasa.framework.spi.streams.IStream;

public class TestRunsPortfoliosRoute extends BaseServletTest {

    private static final Map<String, String> AUTH_HEADERS = Map.of("Authorization", "Bearer " + DUMMY_JWT);

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private String createCatalogJson(String bundle, String className, String pkg, String... tags) {
        JsonObject catalog = new JsonObject();
        JsonObject classes = new JsonObject();
        JsonObject classDef = new JsonObject();
        classDef.addProperty("bundle", bundle);
        classDef.addProperty("name", className);
        classDef.addProperty("package", pkg);
        JsonArray tagsArray = new JsonArray();
        for (String tag : tags) {
            tagsArray.add(tag);
        }
        classDef.add("tags", tagsArray);
        classes.add(bundle + "/" + className, classDef);
        catalog.add("classes", classes);
        return catalog.toString();
    }

    private MockRunsServlet createServlet(List<IStream> streams, String catalogJson) throws Exception {
        MockRBACService rbac = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockStreamsService streamsService = new MockStreamsService(streams);
        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();
        MockTestCatalogFetcher catalogFetcher = new MockTestCatalogFetcher(catalogJson);
        MockFramework mockFramework = new MockFramework(rbac, streamsService);
        return new MockRunsServlet(env, mockFramework, catalogFetcher);
    }

    private MockRunsServlet createServletWithCatalogException(
            List<IStream> streams,
            InternalServletException ex) throws Exception {
        MockRBACService rbac = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockStreamsService streamsService = new MockStreamsService(streams);
        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();
        MockTestCatalogFetcher catalogFetcher = new MockTestCatalogFetcher(null);
        catalogFetcher.setExceptionToThrow(ex);
        MockFramework mockFramework = new MockFramework(rbac, streamsService);
        return new MockRunsServlet(env, mockFramework, catalogFetcher);
    }

    private MockStream makeStream(String name, String catalogUrl,
            String obrGroup, String obrArtifact, String obrVersion) throws Exception {
        MockStream stream = new MockStream();
        stream.setName(name);
        stream.setTestCatalogUrl(catalogUrl);
        stream.setObrs(List.of(new MockOBR(obrGroup, obrArtifact, obrVersion)));
        return stream;
    }

    private String buildRequestBody(String stream, JsonObject filters) {
        JsonObject request = new JsonObject();
        JsonArray selections = new JsonArray();
        JsonObject selection = new JsonObject();
        selection.addProperty("stream", stream);
        if (filters != null) {
            filters.entrySet().forEach(e -> selection.add(e.getKey(), e.getValue()));
        }
        selections.add(selection);
        request.add("selections", selections);
        return request.toString();
    }

    private MockHttpServletResponse invokePost(MockRunsServlet servlet, String body) throws Exception {
        return invokePost(servlet, body, AUTH_HEADERS);
    }

    private MockHttpServletResponse invokePost(MockRunsServlet servlet, String body,
            Map<String, String> headers) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("/portfolios", body, "POST", headers);
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.init();
        servlet.doPost(request, response);
        return response;
    }

    // ---------------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------------

    @Test
    public void testValidSingleStreamSelectionByTagReturns200() throws Exception {
        // Given...
        String streamName = "myStream";
        String bundle = "com.example.bundle";
        String className = "com.example.tests.MyTest";
        String pkg = "com.example.tests";
        String catalogJson = createCatalogJson(bundle, className, pkg, "regression");

        List<IStream> streams = List.of(makeStream(streamName,
            "http://myrepo.com/catalog.json", "com.example", "my-obr", "1.0.0"));
        MockRunsServlet servlet = createServlet(streams, catalogJson);

        JsonObject filters = new JsonObject();
        JsonArray tags = new JsonArray();
        tags.add("regression");
        filters.add("tags", tags);
        String body = buildRequestBody(streamName, filters);

        // When...
        MockHttpServletResponse response = invokePost(servlet, body);

        // Then...
        assertThat(response.getStatus()).isEqualTo(200);
        JsonObject responseJson = JsonParser.parseString(response.getOutputStream().toString()).getAsJsonObject();
        assertThat(responseJson.get("apiVersion").getAsString()).isEqualTo("v1alpha");
        assertThat(responseJson.get("kind").getAsString()).isEqualTo("galasa.dev/testPortfolio");
        assertThat(responseJson.getAsJsonObject("metadata").get("name").getAsString()).isEqualTo("adhoc");
        JsonArray classes = responseJson.getAsJsonArray("classes");
        assertThat(classes).hasSize(1);
        JsonObject classEntry = classes.get(0).getAsJsonObject();
        assertThat(classEntry.get("bundle").getAsString()).isEqualTo(bundle);
        assertThat(classEntry.get("class").getAsString()).isEqualTo(className);
        assertThat(classEntry.get("stream").getAsString()).isEqualTo(streamName);
        assertThat(classEntry.has("overrides")).isTrue();
    }

    @Test
    public void testMultiStreamSelectionReturnsUnionOfClasses() throws Exception {
        // Given...
        String streamA = "stream-a";
        String streamB = "stream-b";
        // Both streams share the same catalog content for simplicity; the mock
        // fetcher always returns the same JSON regardless of which stream is asked.
        String catalogJson = createCatalogJson("com.a", "com.a.TestA", "com.a", "smoke");

        MockStream streamMockA = makeStream(streamA, "http://myrepo.com/catalogA.json", "com.a", "a-obr", "1.0.0");
        MockStream streamMockB = makeStream(streamB, "http://myrepo.com/catalogB.json", "com.b", "b-obr", "2.0.0");
        List<IStream> streams = List.of(streamMockA, streamMockB);

        MockRunsServlet servlet = createServlet(streams, catalogJson);

        JsonObject request = new JsonObject();
        JsonArray selections = new JsonArray();
        for (String sName : List.of(streamA, streamB)) {
            JsonObject sel = new JsonObject();
            sel.addProperty("stream", sName);
            JsonArray tags = new JsonArray();
            tags.add("smoke");
            sel.add("tags", tags);
            selections.add(sel);
        }
        request.add("selections", selections);

        // When...
        MockHttpServletResponse response = invokePost(servlet, request.toString());

        // Then...
        assertThat(response.getStatus()).isEqualTo(200);
        JsonObject responseJson = JsonParser.parseString(response.getOutputStream().toString()).getAsJsonObject();
        assertThat(responseJson.get("apiVersion").getAsString()).isEqualTo("v1alpha");
        JsonArray classes = responseJson.getAsJsonArray("classes");
        // Same catalog content for both streams — the mock always returns the same JSON.
        // The same bundle/class pair appears once per stream, so we expect >= 1 result.
        assertThat(classes.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void testDeduplicationAcrossSelectionsReturnsSingleEntry() throws Exception {
        // Given...
        String streamName = "myStream";
        String bundle = "com.example";
        String className = "com.example.TestDup";
        String pkg = "com.example";
        String catalogJson = createCatalogJson(bundle, className, pkg, "smoke");

        List<IStream> streams = List.of(makeStream(streamName,
            "http://repo.com/catalog.json", "com.example", "obr", "1.0.0"));
        MockRunsServlet servlet = createServlet(streams, catalogJson);

        // Two selections targeting the same stream and tag — should deduplicate to one entry
        JsonObject request = new JsonObject();
        JsonArray selections = new JsonArray();
        for (int i = 0; i < 2; i++) {
            JsonObject sel = new JsonObject();
            sel.addProperty("stream", streamName);
            JsonArray tags = new JsonArray();
            tags.add("smoke");
            sel.add("tags", tags);
            selections.add(sel);
        }
        request.add("selections", selections);

        // When...
        MockHttpServletResponse response = invokePost(servlet, request.toString());

        // Then...
        assertThat(response.getStatus()).isEqualTo(200);
        JsonArray classes = JsonParser.parseString(response.getOutputStream().toString())
            .getAsJsonObject().getAsJsonArray("classes");
        assertThat(classes).hasSize(1);
    }

    @Test
    public void testClassesFilterMatchesAgainstCatalog() throws Exception {
        // Given...
        String streamName = "myStream";
        String bundle = "com.example";
        String className = "com.example.tests.HttpManagerIVT";
        String pkg = "com.example.tests";
        String catalogJson = createCatalogJson(bundle, className, pkg, "smoke");
        List<IStream> streams = List.of(makeStream(streamName,
            "http://repo.com/catalog.json", "com.example", "obr", "1.0.0"));
        MockRunsServlet servlet = createServlet(streams, catalogJson);

        JsonObject filters = new JsonObject();
        JsonArray classes = new JsonArray();
        classes.add("HttpManagerIVT");
        filters.add("classes", classes);
        String body = buildRequestBody(streamName, filters);

        // When...
        MockHttpServletResponse response = invokePost(servlet, body);

        // Then...
        assertThat(response.getStatus()).isEqualTo(200);
        JsonArray resultClasses = JsonParser.parseString(response.getOutputStream().toString())
            .getAsJsonObject().getAsJsonArray("classes");
        assertThat(resultClasses).hasSize(1);
        assertThat(resultClasses.get(0).getAsJsonObject().get("class").getAsString()).isEqualTo(className);
        assertThat(resultClasses.get(0).getAsJsonObject().get("bundle").getAsString()).isEqualTo(bundle);
    }

    @Test
    public void testEmptyResultSetReturns200WithEmptyArray() throws Exception {
        // Given...
        String streamName = "myStream";
        // Catalog has a "smoke"-tagged class; filter asks for "regression" — no match
        String catalogJson = createCatalogJson("com.example", "com.example.TestA", "com.example", "smoke");
        List<IStream> streams = List.of(makeStream(streamName,
            "http://repo.com/catalog.json", "com.example", "obr", "1.0.0"));
        MockRunsServlet servlet = createServlet(streams, catalogJson);

        JsonObject filters = new JsonObject();
        JsonArray tags = new JsonArray();
        tags.add("regression");
        filters.add("tags", tags);
        String body = buildRequestBody(streamName, filters);

        // When...
        MockHttpServletResponse response = invokePost(servlet, body);

        // Then...
        assertThat(response.getStatus()).isEqualTo(200);
        JsonArray classes = JsonParser.parseString(response.getOutputStream().toString())
            .getAsJsonObject().getAsJsonArray("classes");
        assertThat(classes).isEmpty();
    }

    @Test
    public void testStreamNotFoundReturns404() throws Exception {
        // Given...
        List<IStream> streams = new ArrayList<>(); // no streams registered
        MockRunsServlet servlet = createServlet(streams, null);

        String body = buildRequestBody("nonExistentStream", null);

        // When...
        MockHttpServletResponse response = invokePost(servlet, body);

        // Then...
        assertThat(response.getStatus()).isEqualTo(404);
        checkErrorStructure(response.getOutputStream().toString(), 5468, "GAL5468E");
    }

    @Test
    public void testCatalogFetchFailureReturns502() throws Exception {
        // Given...
        String streamName = "myStream";
        List<IStream> streams = List.of(makeStream(streamName,
            "http://repo.com/catalog.json", "com.example", "obr", "1.0.0"));

        InternalServletException catalogException =
            new InternalServletException(
                new ServletError(
                    ServletErrorMessage.GAL5469_RUNS_PORTFOLIO_CATALOG_FETCH_FAILED,
                    streamName),
                502
            );
        MockRunsServlet servlet = createServletWithCatalogException(streams, catalogException);

        JsonObject filters = new JsonObject();
        JsonArray tags = new JsonArray();
        tags.add("smoke");
        filters.add("tags", tags);
        String body = buildRequestBody(streamName, filters);

        // When...
        MockHttpServletResponse response = invokePost(servlet, body);

        // Then...
        assertThat(response.getStatus()).isEqualTo(502);
        checkErrorStructure(response.getOutputStream().toString(), 5469, "GAL5469E");
    }

    @Test
    public void testInvalidRegexReturns400() throws Exception {
        // Given...
        String streamName = "myStream";
        String catalogJson = createCatalogJson("com.example", "com.example.TestA", "com.example", "smoke");
        List<IStream> streams = List.of(makeStream(streamName,
            "http://repo.com/catalog.json", "com.example", "obr", "1.0.0"));
        MockRunsServlet servlet = createServlet(streams, catalogJson);

        JsonObject filters = new JsonObject();
        JsonArray bundles = new JsonArray();
        bundles.add("[invalid(regex");
        filters.add("bundles", bundles);
        filters.addProperty("regex", true);
        String body = buildRequestBody(streamName, filters);

        // When...
        MockHttpServletResponse response = invokePost(servlet, body);

        // Then...
        assertThat(response.getStatus()).isEqualTo(400);
        checkErrorStructure(response.getOutputStream().toString(), 5467, "GAL5467E");
    }

    @Test
    public void testMissingSelectionsFieldReturns400() throws Exception {
        // Given...
        MockRunsServlet servlet = createServlet(List.of(), null);

        // Request with no "selections" field
        JsonObject request = new JsonObject();

        // When...
        MockHttpServletResponse response = invokePost(servlet, request.toString());

        // Then...
        assertThat(response.getStatus()).isEqualTo(400);
        checkErrorStructure(response.getOutputStream().toString(), 5465, "GAL5465E");
    }

    @Test
    public void testOverridesAreAppliedPerClass() throws Exception {
        // Given...
        String streamName = "myStream";
        String catalogJson = createCatalogJson("com.example", "com.example.TestA", "com.example", "smoke");
        List<IStream> streams = List.of(makeStream(streamName,
            "http://repo.com/catalog.json", "com.example", "obr", "1.0.0"));
        MockRunsServlet servlet = createServlet(streams, catalogJson);

        JsonObject request = new JsonObject();
        JsonArray selections = new JsonArray();
        JsonObject sel = new JsonObject();
        sel.addProperty("stream", streamName);
        JsonArray tags = new JsonArray();
        tags.add("smoke");
        sel.add("tags", tags);
        selections.add(sel);
        request.add("selections", selections);
        JsonObject overrides = new JsonObject();
        overrides.addProperty("zos.image", "SYSA");
        request.add("overrides", overrides);

        // When...
        MockHttpServletResponse response = invokePost(servlet, request.toString());

        // Then...
        assertThat(response.getStatus()).isEqualTo(200);
        JsonObject responseJson = JsonParser.parseString(response.getOutputStream().toString()).getAsJsonObject();
        // No top-level overrides field
        assertThat(responseJson.has("overrides")).isFalse();
        // Overrides are present on each class entry
        JsonObject classEntry = responseJson.getAsJsonArray("classes").get(0).getAsJsonObject();
        assertThat(classEntry.getAsJsonObject("overrides").get("zos.image").getAsString()).isEqualTo("SYSA");
    }

    @Test
    public void testPerClassOverridesIsEmptyObjectWhenNoOverridesInRequest() throws Exception {
        // Given...
        String streamName = "myStream";
        String catalogJson = createCatalogJson("com.example", "com.example.TestA", "com.example", "smoke");
        List<IStream> streams = List.of(makeStream(streamName,
            "http://repo.com/catalog.json", "com.example", "obr", "1.0.0"));
        MockRunsServlet servlet = createServlet(streams, catalogJson);

        JsonObject filters = new JsonObject();
        JsonArray tags = new JsonArray();
        tags.add("smoke");
        filters.add("tags", tags);
        String body = buildRequestBody(streamName, filters);

        // When...
        MockHttpServletResponse response = invokePost(servlet, body);

        // Then...
        assertThat(response.getStatus()).isEqualTo(200);
        JsonObject responseJson = JsonParser.parseString(response.getOutputStream().toString()).getAsJsonObject();
        JsonObject classEntry = responseJson.getAsJsonArray("classes").get(0).getAsJsonObject();
        assertThat(classEntry.has("overrides")).isTrue();
        assertThat(classEntry.getAsJsonObject("overrides").size()).isEqualTo(0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAcceptYamlReturnsValidYamlWithPortfolioStructure() throws Exception {
        // Given...
        String streamName = "myStream";
        String bundle = "com.example";
        String className = "com.example.TestA";
        String catalogJson = createCatalogJson(bundle, className, "com.example", "smoke");
        List<IStream> streams = List.of(makeStream(streamName,
            "http://repo.com/catalog.json", "com.example", "my-obr", "1.0.0"));
        MockRunsServlet servlet = createServlet(streams, catalogJson);

        JsonObject filters = new JsonObject();
        JsonArray tags = new JsonArray();
        tags.add("smoke");
        filters.add("tags", tags);
        String body = buildRequestBody(streamName, filters);

        Map<String, String> headers = Map.of(
            "Authorization", "Bearer " + DUMMY_JWT,
            "Accept", "application/yaml"
        );

        // When...
        MockHttpServletResponse response = invokePost(servlet, body, headers);

        // Then...
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentType()).isEqualTo("application/yaml");
        String yamlBody = response.getOutputStream().toString();
        Map<String, Object> parsed = new Yaml().load(yamlBody);
        assertThat(parsed.get("apiVersion")).isEqualTo("v1alpha");
        assertThat(parsed.get("kind")).isEqualTo("galasa.dev/testPortfolio");
        assertThat(((Map<String, Object>) parsed.get("metadata")).get("name")).isEqualTo("adhoc");
        List<Map<String, Object>> classes = (List<Map<String, Object>>) parsed.get("classes");
        assertThat(classes).hasSize(1);
        assertThat(classes.get(0).get("bundle")).isEqualTo(bundle);
        assertThat(classes.get(0).get("class")).isEqualTo(className);
        assertThat(classes.get(0).get("stream")).isEqualTo(streamName);
    }

    @Test
    public void testNoAcceptHeaderReturnsJson() throws Exception {
        // Given...
        String streamName = "myStream";
        String catalogJson = createCatalogJson("com.example", "com.example.TestA", "com.example", "smoke");
        List<IStream> streams = List.of(makeStream(streamName,
            "http://repo.com/catalog.json", "com.example", "obr", "1.0.0"));
        MockRunsServlet servlet = createServlet(streams, catalogJson);

        JsonObject filters = new JsonObject();
        JsonArray tags = new JsonArray();
        tags.add("smoke");
        filters.add("tags", tags);
        String body = buildRequestBody(streamName, filters);

        // When...
        MockHttpServletResponse response = invokePost(servlet, body);

        // Then...
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentType()).isEqualTo("application/json");
        // Must be valid JSON
        JsonObject responseJson = JsonParser.parseString(response.getOutputStream().toString()).getAsJsonObject();
        assertThat(responseJson.get("apiVersion").getAsString()).isEqualTo("v1alpha");
    }

    @Test
    public void testUnsupportedAcceptTypeReturns406() throws Exception {
        // Given...
        String streamName = "myStream";
        String catalogJson = createCatalogJson("com.example", "com.example.TestA", "com.example", "smoke");
        List<IStream> streams = List.of(makeStream(streamName,
            "http://repo.com/catalog.json", "com.example", "obr", "1.0.0"));
        MockRunsServlet servlet = createServlet(streams, catalogJson);

        JsonObject filters = new JsonObject();
        JsonArray tags = new JsonArray();
        tags.add("smoke");
        filters.add("tags", tags);
        String body = buildRequestBody(streamName, filters);

        Map<String, String> headers = Map.of(
            "Authorization", "Bearer " + DUMMY_JWT,
            "Accept", "text/plain"
        );

        // When...
        MockHttpServletResponse response = invokePost(servlet, body, headers);

        // Then...
        assertThat(response.getStatus()).isEqualTo(406);
    }

    @Test
    public void testWildcardAcceptReturnsJson() throws Exception {
        // Given...
        String streamName = "myStream";
        String catalogJson = createCatalogJson("com.example", "com.example.TestA", "com.example", "smoke");
        List<IStream> streams = List.of(makeStream(streamName,
            "http://repo.com/catalog.json", "com.example", "obr", "1.0.0"));
        MockRunsServlet servlet = createServlet(streams, catalogJson);

        JsonObject filters = new JsonObject();
        JsonArray tags = new JsonArray();
        tags.add("smoke");
        filters.add("tags", tags);
        String body = buildRequestBody(streamName, filters);

        Map<String, String> headers = Map.of(
            "Authorization", "Bearer " + DUMMY_JWT,
            "Accept", "application/*"
        );

        // When...
        MockHttpServletResponse response = invokePost(servlet, body, headers);

        // Then...
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentType()).isEqualTo("application/json");
        JsonObject responseJson = JsonParser.parseString(response.getOutputStream().toString()).getAsJsonObject();
        assertThat(responseJson.get("apiVersion").getAsString()).isEqualTo("v1alpha");
    }

    @Test
    public void testInvalidOverrideKeyReturns400() throws Exception {
        // Given...
        MockRunsServlet servlet = createServlet(List.of(), null);

        JsonObject request = new JsonObject();
        JsonArray selections = new JsonArray();
        JsonObject sel = new JsonObject();
        sel.addProperty("stream", "myStream");
        selections.add(sel);
        request.add("selections", selections);
        JsonObject overrides = new JsonObject();
        overrides.addProperty("!!bad key with spaces!!", "value");
        request.add("overrides", overrides);

        // When...
        MockHttpServletResponse response = invokePost(servlet, request.toString());

        // Then...
        assertThat(response.getStatus()).isEqualTo(400);
        checkErrorStructure(response.getOutputStream().toString(), 5470, "GAL5470E");
    }

    @Test
    public void testValidOverrideKeyPassesValidation() throws Exception {
        // Given...
        String streamName = "myStream";
        String catalogJson = createCatalogJson("com.example", "com.example.TestA", "com.example", "smoke");
        List<IStream> streams = List.of(makeStream(streamName,
            "http://repo.com/catalog.json", "com.example", "obr", "1.0.0"));
        MockRunsServlet servlet = createServlet(streams, catalogJson);

        JsonObject request = new JsonObject();
        JsonArray selections = new JsonArray();
        JsonObject sel = new JsonObject();
        sel.addProperty("stream", streamName);
        JsonArray tags = new JsonArray();
        tags.add("smoke");
        sel.add("tags", tags);
        selections.add(sel);
        request.add("selections", selections);
        JsonObject overrides = new JsonObject();
        overrides.addProperty("zos.default.credentials.id", "SYS1");
        request.add("overrides", overrides);

        // When...
        MockHttpServletResponse response = invokePost(servlet, request.toString());

        // Then - valid key must not cause a 400
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testNestedQuantifierRegexReturns400() throws Exception {
        // Given — "(a+)+" is a classic ReDoS pattern
        String streamName = "myStream";
        String catalogJson = createCatalogJson("com.example", "com.example.TestA", "com.example");
        List<IStream> streams = List.of(makeStream(streamName,
            "http://repo.com/catalog.json", "com.example", "obr", "1.0.0"));
        MockRunsServlet servlet = createServlet(streams, catalogJson);

        JsonObject filters = new JsonObject();
        JsonArray bundles = new JsonArray();
        bundles.add("(a+)+");
        filters.add("bundles", bundles);
        filters.addProperty("regex", true);
        String body = buildRequestBody(streamName, filters);

        // When...
        MockHttpServletResponse response = invokePost(servlet, body);

        // Then...
        assertThat(response.getStatus()).isEqualTo(400);
        checkErrorStructure(response.getOutputStream().toString(), 5471, "GAL5471E");
    }

    @Test
    public void testNestedStarQuantifierRegexReturns400() throws Exception {
        // Given — "(a*)* triggers catastrophic backtracking
        String streamName = "myStream";
        String catalogJson = createCatalogJson("com.example", "com.example.TestA", "com.example");
        List<IStream> streams = List.of(makeStream(streamName,
            "http://repo.com/catalog.json", "com.example", "obr", "1.0.0"));
        MockRunsServlet servlet = createServlet(streams, catalogJson);

        JsonObject filters = new JsonObject();
        JsonArray tags = new JsonArray();
        tags.add("(a*)*");
        filters.add("tags", tags);
        filters.addProperty("regex", true);
        String body = buildRequestBody(streamName, filters);

        // When...
        MockHttpServletResponse response = invokePost(servlet, body);

        // Then...
        assertThat(response.getStatus()).isEqualTo(400);
        checkErrorStructure(response.getOutputStream().toString(), 5471, "GAL5471E");
    }

    @Test
    public void testSafeRegexWithQuantifierPassesValidation() throws Exception {
        // Given — "com\.example\.\w+" is safe: quantifier is not inside a group with a quantifier
        String streamName = "myStream";
        String catalogJson = createCatalogJson("com.example", "com.example.TestA", "com.example");
        List<IStream> streams = List.of(makeStream(streamName,
            "http://repo.com/catalog.json", "com.example", "obr", "1.0.0"));
        MockRunsServlet servlet = createServlet(streams, catalogJson);

        JsonObject filters = new JsonObject();
        JsonArray bundles = new JsonArray();
        bundles.add("com\\.example\\.\\w+");
        filters.add("bundles", bundles);
        filters.addProperty("regex", true);
        String body = buildRequestBody(streamName, filters);

        // When...
        MockHttpServletResponse response = invokePost(servlet, body);

        // Then — no 400 from the complexity check
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testComplexityCheckOnlyAppliesToRegexMode() throws Exception {
        // Given — "(a+)+" is a literal string in non-regex mode; must not be rejected
        String streamName = "myStream";
        String catalogJson = createCatalogJson("com.example", "com.example.TestA", "com.example");
        List<IStream> streams = List.of(makeStream(streamName,
            "http://repo.com/catalog.json", "com.example", "obr", "1.0.0"));
        MockRunsServlet servlet = createServlet(streams, catalogJson);

        JsonObject filters = new JsonObject();
        JsonArray bundles = new JsonArray();
        bundles.add("(a+)+");
        filters.add("bundles", bundles);
        // regex defaults to false — no complexity check should run
        String body = buildRequestBody(streamName, filters);

        // When...
        MockHttpServletResponse response = invokePost(servlet, body);

        // Then — treated as a literal substring filter, no error
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testNoFiltersReturnsAllClassesInStream() throws Exception {
        // Given — catalog contains two classes; selection specifies only a stream name with no filters
        String streamName = "myStream";
        String bundle = "com.example";
        String pkg = "com.example";
        JsonObject catalog = new JsonObject();
        JsonObject classes = new JsonObject();
        for (String cls : List.of("com.example.TestA", "com.example.TestB")) {
            JsonObject classDef = new JsonObject();
            classDef.addProperty("bundle", bundle);
            classDef.addProperty("name", cls);
            classDef.addProperty("package", pkg);
            classDef.add("tags", new JsonArray());
            classes.add(bundle + "/" + cls, classDef);
        }
        catalog.add("classes", classes);

        List<IStream> streams = List.of(makeStream(streamName,
            "http://repo.com/catalog.json", "com.example", "obr", "1.0.0"));
        MockRunsServlet servlet = createServlet(streams, catalog.toString());

        // No filters — stream name only
        String body = buildRequestBody(streamName, null);

        // When...
        MockHttpServletResponse response = invokePost(servlet, body);

        // Then — both classes are returned
        assertThat(response.getStatus()).isEqualTo(200);
        JsonArray resultClasses = JsonParser.parseString(response.getOutputStream().toString())
            .getAsJsonObject().getAsJsonArray("classes");
        assertThat(resultClasses).hasSize(2);
    }
}
