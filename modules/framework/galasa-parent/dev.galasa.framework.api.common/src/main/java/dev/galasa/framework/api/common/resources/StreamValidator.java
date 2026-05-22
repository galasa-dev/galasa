/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.beans.generated.StreamOBRData;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.utils.StringValidator;

/**
 * Common validator for stream-related validation logic.
 * Provides reusable validation methods for stream names, URLs, and OBR fields.
 */
public class StreamValidator {

    public static final String OBR_VERSION_KEY = "version";
    public static final String OBR_ARTIFACT_ID_KEY = "artifact-id";
    public static final String OBR_GROUP_ID_KEY = "group-id";

    public static final String MAVEN_REPOSITORY_KEY = "repository";
    public static final String TESTCATALOG_KEY = "testCatalog";
    public static final String OBRS_KEY = "obrs";

    private static final StringValidator stringValidator = new StringValidator();

    public void validateStreamName(String streamName) throws InternalServletException {
        if (streamName == null || streamName.trim().isEmpty()) {
            ServletError error = new ServletError(GAL5427_MISSING_STREAM_NAME);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        
        if (!stringValidator.isAlphanumWithDashes(streamName)) {
            ServletError error = new ServletError(GAL5418_INVALID_STREAM_NAME);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Validates that a URL string is a valid, well-formed URL.
     *
     * @param fieldName the name of the field being validated (for error messages)
     * @param urlToCheck the URL string to validate
     * @throws InternalServletException if the URL is invalid
     */
    public URL validateUrl(String fieldName, String urlToCheck) throws InternalServletException {
        URL validatedUrl = null;
        try {
            validatedUrl = new URL(urlToCheck);
            validatedUrl.toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            ServletError error = new ServletError(GAL5436_INVALID_STREAM_URL_PROVIDED, fieldName);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        return validatedUrl;
    }

    /**
     * Validates a repository URL field.
     *
     * @param repositoryUrl the repository URL to validate (can be null for update requests)
     * @param isRequired whether the field is required (true for create, false for update)
     * @throws InternalServletException if validation fails
     */
    public void validateRepository(String repositoryUrl, String mavenSecretName, boolean isRequired) throws InternalServletException {
        if (repositoryUrl == null && isRequired) {
            ServletError error = new ServletError(GAL5434_INVALID_GALASA_STREAM_MISSING_FIELDS, MAVEN_REPOSITORY_KEY, "url");
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        if (repositoryUrl != null) {
            validateUrl(MAVEN_REPOSITORY_KEY, repositoryUrl);
        }

        // The secret name in stream repositories is optional, so only validate it when it has been provided
        if (mavenSecretName != null) {
            validateMavenSecretName(mavenSecretName);
        }
    }

    private void validateMavenSecretName(String mavenSecretName) throws InternalServletException {
        if (mavenSecretName.isBlank() || mavenSecretName.contains(".") || !stringValidator.isLatin1(mavenSecretName)) {
            ServletError error = new ServletError(GAL5092_INVALID_SECRET_NAME_PROVIDED);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Validates a test catalog URL field.
     *
     * @param testCatalogUrl the test catalog URL to validate (can be null for update requests)
     * @param isRequired whether the field is required (true for create, false for update)
     * @throws InternalServletException if validation fails
     */
    public void validateTestCatalogUrl(String testCatalogUrl, boolean isRequired) throws InternalServletException {
        if (testCatalogUrl == null && isRequired) {
            ServletError error = new ServletError(GAL5434_INVALID_GALASA_STREAM_MISSING_FIELDS, TESTCATALOG_KEY, "url");
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        URL validatedUrl = null;
        if (testCatalogUrl != null) {
            validatedUrl = validateUrl(TESTCATALOG_KEY, testCatalogUrl);

            String protocol = validatedUrl.getProtocol().toLowerCase();
            String host = validatedUrl.getHost();
            if ((!protocol.equals("http") && !protocol.equals("https"))
                || (host == null || host.isEmpty())) {
                ServletError error = new ServletError(GAL5456_ERROR_INVALID_TEST_CATALOG_URL);
                throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
            }
    
            if (host.equals("localhost") || host.equals("127.0.0.1") || host.equals("0.0.0.0")) {
                ServletError error = new ServletError(GAL5457_ERROR_TEST_CATALOG_URL_PRIVATE_NETWORK);
                throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }

    /**
     * Validates an array of OBRs.
     * Checks that the array is not null (if required), not empty, and each OBR is valid.
     *
     * @param obrs the array of OBRs to validate (can be null for update requests)
     * @param isRequired whether the field is required (true for create, false for update)
     * @throws InternalServletException if validation fails
     */
    public void validateObrs(StreamOBRData[] obrs, boolean isRequired) throws InternalServletException {
        if (obrs == null && isRequired || obrs != null && obrs.length == 0) {
            ServletError error = new ServletError(GAL5437_INVALID_STREAM_MISSING_OBRS);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        
        if (obrs != null) {
            for (StreamOBRData obr : obrs) {
                validateObr(obr);
            }
        }
    }

    /**
     * Validates a single OBR object.
     * Checks that groupId, artifactId, and version are all present.
     *
     * @param obr the OBR to validate
     * @throws InternalServletException if validation fails
     */
    public void validateObr(StreamOBRData obr) throws InternalServletException {
        validateObrField(OBR_GROUP_ID_KEY, obr.getGroupId());
        validateObrField(OBR_ARTIFACT_ID_KEY, obr.getArtifactId());
        validateObrField(OBR_VERSION_KEY, obr.getversion());
    }

    /**
     * Validates that an OBR field (groupId, artifactId, or version) is not null or empty.
     *
     * @param fieldName the name of the field being validated
     * @param fieldValue the value to validate
     * @throws InternalServletException if the field is null or empty
     */
    private void validateObrField(String fieldName, String fieldValue) throws InternalServletException {
        if (fieldValue == null || fieldValue.isBlank()) {
            ServletError error = new ServletError(GAL5434_INVALID_GALASA_STREAM_MISSING_FIELDS, OBRS_KEY, fieldName);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
