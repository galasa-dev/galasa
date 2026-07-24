/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs.routes;

import static dev.galasa.framework.api.common.MimeType.*;
import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.galasa.framework.api.beans.generated.RunsPortfolioRequest;
import dev.galasa.framework.api.beans.generated.RunsPortfolioSelection;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.ITestCatalogFetcher;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.MimeType;
import dev.galasa.framework.api.common.ProtectedRoute;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.TestCatalogFetcher;
import dev.galasa.framework.api.runs.validators.RunsPortfolioRequestValidator;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.streams.IStream;
import dev.galasa.framework.spi.streams.IStreamsService;
import dev.galasa.framework.spi.streams.StreamsException;

/**
 * Handles POST /runs/portfolios.
 *
 * Accepts test selection criteria for one or more named streams and returns the
 * resolved set of matching test classes. The response is stateless — nothing is
 * persisted on the server.
 */
public class RunsPortfoliosRoute extends ProtectedRoute {

    // Regex to match exactly /portfolios or /portfolios/
    protected static final String path = "\\/portfolios\\/?";

    private static final List<MimeType> SUPPORTED_CONTENT_TYPES = List.of(APPLICATION_JSON, APPLICATION_YAML);

    private static final String PORTFOLIO_API_VERSION = "v1alpha";
    private static final String PORTFOLIO_KIND = "galasa.dev/testPortfolio";
    private static final String PORTFOLIO_METADATA_NAME = "adhoc";

    private static final String REGEX_QUANTIFIER_CHARS = "+*?{";

    private final IStreamsService streamsService;
    private final ITestCatalogFetcher catalogFetcher;
    private final RunsPortfolioRequestValidator validator;

    public RunsPortfoliosRoute(
        ResponseBuilder responseBuilder,
        IStreamsService streamsService,
        ICredentialsService credentialsService,
        RBACService rbacService,
        HttpClient httpClient
    ) throws StreamsException {
        this(responseBuilder, streamsService, rbacService,
            new TestCatalogFetcher(httpClient, credentialsService));
    }

    RunsPortfoliosRoute(
        ResponseBuilder responseBuilder,
        IStreamsService streamsService,
        RBACService rbacService,
        ITestCatalogFetcher catalogFetcher
    ) throws StreamsException {
        super(responseBuilder, path, rbacService);
        this.streamsService = streamsService;
        this.catalogFetcher = catalogFetcher;
        this.validator = new RunsPortfolioRequestValidator();
    }

    @Override
    public HttpServletResponse handlePostRequest(
        String pathInfo,
        HttpRequestContext requestContext,
        HttpServletResponse response
    ) throws ServletException, IOException, FrameworkException {

        HttpServletRequest request = requestContext.getRequest();
        String body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        RunsPortfolioRequest portfolioRequest = gson.fromJson(body, RunsPortfolioRequest.class);
        validator.validate(portfolioRequest);

        String responseContentType = getResponseType(request.getHeader("Accept"), APPLICATION_JSON, SUPPORTED_CONTENT_TYPES);
        Map<String, String> overrides = parseOverrides(body);
        validator.validateOverrideKeys(overrides);

        // Create deduplication keys in the form "stream/bundle/class" to avoid adding the same test again
        Set<String> seenClasses = new HashSet<>();
        List<Map<String, Object>> resolvedClasses = new ArrayList<>();

        for (RunsPortfolioSelection selection : portfolioRequest.getselections()) {
            String streamName = selection.getstream();

            IStream stream = getStreamByName(streamName);
            boolean isRegexEnabled = selection.getregex();

            List<Pattern> bundlePatterns = buildPatterns(selection.getbundles(), isRegexEnabled);
            List<Pattern> packagePatterns = buildPatterns(selection.getpackages(), isRegexEnabled);
            List<Pattern> testPatterns = buildPatterns(selection.gettests(), isRegexEnabled);
            List<Pattern> classPatterns = buildPatterns(selection.getclasses(), isRegexEnabled);
            List<Pattern> tagPatterns = buildPatterns(selection.gettags(), isRegexEnabled);

            boolean hasTestCatalogFilter = !bundlePatterns.isEmpty() || !packagePatterns.isEmpty()
                || !testPatterns.isEmpty() || !classPatterns.isEmpty() || !tagPatterns.isEmpty();

            String testCatalogJson = catalogFetcher.fetchTestCatalog(stream);

            if (testCatalogJson != null) {

                JsonObject testCatalog = JsonParser.parseString(testCatalogJson).getAsJsonObject();
                if (testCatalog.has("classes")) {
                    for (Map.Entry<String, JsonElement> entry : testCatalog.getAsJsonObject("classes").entrySet()) {
                        JsonObject classDef = entry.getValue().getAsJsonObject();
                        String bundle = classDef.has("bundle") ? classDef.get("bundle").getAsString() : "";
                        String className = classDef.has("name") ? classDef.get("name").getAsString() : "";
                        String pkg = classDef.has("package") ? classDef.get("package").getAsString() : "";

                        if (!hasTestCatalogFilter
                            || !bundlePatterns.isEmpty() && matchesAnyPattern(bundle, bundlePatterns)
                            || !packagePatterns.isEmpty() && matchesAnyPattern(pkg, packagePatterns)
                            || !testPatterns.isEmpty() && matchesAnyPattern(className, testPatterns)
                            || !classPatterns.isEmpty() && matchesAnyPattern(className, classPatterns)
                            || !tagPatterns.isEmpty() && matchesAnyTag(classDef, tagPatterns)
                        ) {
                            addResolvedClass(resolvedClasses, seenClasses, bundle, className, streamName, overrides);
                        }
                    }
                }
            }
        }

        Map<String, Object> doc = buildPortfolioDocument(resolvedClasses);
        String responseBody = responseContentType.equals(APPLICATION_YAML.toString())
            ? serialiseToYaml(doc)
            : serialiseToJson(doc);

        return getResponseBuilder().buildResponse(request, response, responseContentType,
            responseBody, HttpServletResponse.SC_OK);
    }

    private Map<String, String> parseOverrides(String body) {
        Map<String, String> overrides = new HashMap<>();
        JsonObject requestJson = JsonParser.parseString(body).getAsJsonObject();

        if (requestJson.has("overrides") && !requestJson.get("overrides").isJsonNull()) {

            JsonObject overridesJson = requestJson.getAsJsonObject("overrides");
            for (Map.Entry<String, JsonElement> entry : overridesJson.entrySet()) {
                overrides.put(entry.getKey(), entry.getValue().getAsString());
            }
        }
        return overrides;
    }

    private Map<String, Object> buildPortfolioDocument(List<Map<String, Object>> resolvedClasses) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("name", PORTFOLIO_METADATA_NAME);

        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("apiVersion", PORTFOLIO_API_VERSION);
        doc.put("kind", PORTFOLIO_KIND);
        doc.put("metadata", metadata);
        doc.put("classes", resolvedClasses);

        return doc;
    }

    private String serialiseToJson(Map<String, Object> doc) {
        return gson.toJson(doc);
    }

    private String serialiseToYaml(Map<String, Object> doc) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        return new Yaml(options).dump(doc);
    }

    private IStream getStreamByName(String streamName) throws InternalServletException, FrameworkException {
        try {
            IStream stream = streamsService.getStreamByName(streamName);
            if (stream == null) {
                ServletError error = new ServletError(GAL5468_RUNS_PORTFOLIO_STREAM_NOT_FOUND, streamName);
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }
            return stream;
        } catch (StreamsException e) {
            ServletError error = new ServletError(GAL5468_RUNS_PORTFOLIO_STREAM_NOT_FOUND, streamName);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND, e);
        }
    }

    private List<Pattern> buildPatterns(String[] values, boolean isRegexEnabled) throws InternalServletException {
        List<Pattern> patterns = new ArrayList<>();

        if (values != null) {
            for (String value : values) {
                if (isRegexEnabled) {
                    checkRegexComplexity(value);
                }
                try {
                    patterns.add(isRegexEnabled ? Pattern.compile(value) : Pattern.compile(Pattern.quote(value)));
                } catch (PatternSyntaxException e) {
                    ServletError error = new ServletError(GAL5467_RUNS_PORTFOLIO_INVALID_REGEX, value);
                    throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST, e);
                }
            }
        }
        return patterns;
    }

    /**
     * Rejects regex patterns that contain nested quantifiers, which can cause
     * catastrophic backtracking (ReDoS). A nested quantifier is a quantifier
     * ({@code +}, {@code *}, {@code ?}, or {@code {n,m}}) applied directly to a
     * group that itself contains a quantifier, e.g. {@code (a+)+}.
     *
     * The check walks the pattern character by character, tracking the nesting
     * depth of capturing and non-capturing groups. For each group it records
     * whether that group's body contained a quantifier. When the group is closed
     * and immediately followed by a quantifier, and the body flag is set, the
     * pattern is rejected.
     *
     * Escaped characters (preceded by {@code \}) are skipped so that literal
     * {@code \+}, {@code \*}, etc. do not trigger a false positive.
     * 
     * @param pattern the regex pattern to validate
     */
    private void checkRegexComplexity(String pattern) throws InternalServletException {
        // Stack entry: whether the group at this depth has seen a quantifier in its body
        List<Boolean> groupHasQuantifier = new ArrayList<>();
        boolean isEscaped = false;

        for (int i = 0; i < pattern.length(); i++) {
            char patternChar = pattern.charAt(i);

            if (isEscaped) {
                isEscaped = false;
            } else if (patternChar == '\\') {
                isEscaped = true;
            } else if (patternChar == '(') {
                groupHasQuantifier.add(false);
            } else if (patternChar == ')') {
                if (!groupHasQuantifier.isEmpty()) {
                    boolean bodyHadQuantifier = groupHasQuantifier.remove(groupHasQuantifier.size() - 1);

                    // Check if the character after ')' is a quantifier
                    if (bodyHadQuantifier && i + 1 < pattern.length()) {
                        char nextChar = pattern.charAt(i + 1);
                        if (REGEX_QUANTIFIER_CHARS.indexOf(nextChar) >= 0) {
                            ServletError error = new ServletError(GAL5471_RUNS_PORTFOLIO_REGEX_TOO_COMPLEX, pattern);
                            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
                        }
                    }
                }
            } else if (REGEX_QUANTIFIER_CHARS.indexOf(patternChar) >= 0) {
                // Mark every enclosing group as having seen a quantifier
                for (int groupIndex = 0; groupIndex < groupHasQuantifier.size(); groupIndex++) {
                    groupHasQuantifier.set(groupIndex, true);
                }
            }
        }
    }

    private boolean matchesAnyPattern(String value, List<Pattern> patterns) {
        boolean isMatchFound = false;
        for (Pattern pattern : patterns) {
            if (pattern.matcher(value).find()) {
                isMatchFound = true;
                break;
            }
        }
        return isMatchFound;
    }

    private boolean matchesAnyTag(JsonObject classDef, List<Pattern> tagPatterns) {
        boolean isMatchFound = false;

        if (classDef.has("tags") && classDef.get("tags").isJsonArray()) {
            for (JsonElement tagElem : classDef.get("tags").getAsJsonArray()) {
                if (matchesAnyPattern(tagElem.getAsString(), tagPatterns)) {
                    isMatchFound = true;
                    break;
                }
            }
        }
        return isMatchFound;
    }

    private void addResolvedClass(
        List<Map<String, Object>> resolvedClasses,
        Set<String> seenClasses,
        String bundle,
        String className,
        String stream,
        Map<String, String> overrides
    ) {
        String key = stream + "/" + bundle + "/" + className;
        if (!seenClasses.contains(key)) {
            seenClasses.add(key);

            Map<String, Object> classEntry = new LinkedHashMap<>();
            classEntry.put("bundle", bundle);
            classEntry.put("class", className);
            classEntry.put("stream", stream);
            classEntry.put("overrides", new LinkedHashMap<>(overrides));
            resolvedClasses.add(classEntry);
        }
    }
}
