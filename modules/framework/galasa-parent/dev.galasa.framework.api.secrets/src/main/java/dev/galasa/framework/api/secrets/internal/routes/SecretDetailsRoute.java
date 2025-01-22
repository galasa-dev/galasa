/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.secrets.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;
import static dev.galasa.framework.api.common.resources.GalasaSecretType.*;

import java.io.IOException;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ICredentials;
import dev.galasa.framework.api.beans.generated.GalasaSecret;
import dev.galasa.framework.api.beans.generated.SecretRequest;
import dev.galasa.framework.api.beans.generated.SecretRequestpassword;
import dev.galasa.framework.api.beans.generated.SecretRequesttoken;
import dev.galasa.framework.api.beans.generated.SecretRequestusername;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.GalasaSecretType;
import dev.galasa.framework.api.secrets.internal.SecretRequestValidator;
import dev.galasa.framework.api.secrets.internal.UpdateSecretRequestValidator;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.CredentialsToken;
import dev.galasa.framework.spi.creds.CredentialsUsername;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.framework.spi.creds.CredentialsUsernameToken;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.rbac.BuiltInAction;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.utils.ITimeService;

public class SecretDetailsRoute extends AbstractSecretsRoute {

    // Regex to match /secrets/{secret-id} and /secrets/{secret-id}/
    // where {secret-id} can consist of the following characters:
    // - Alphanumeric characters (a-zA-Z0-9)
    // - Underscores (_)
    // - Dashes (-)
    private static final String PATH_PATTERN = "\\/([a-zA-Z0-9_-]+)\\/?";

    private ICredentialsService credentialsService;

    private Log logger = LogFactory.getLog(getClass());

    public SecretDetailsRoute(
        ResponseBuilder responseBuilder,
        ICredentialsService credentialsService,
        ITimeService timeService,
        RBACService rbacService
    ) {
        super(responseBuilder, PATH_PATTERN, timeService, rbacService);
        this.credentialsService = credentialsService;
    }

    @Override
    public HttpServletResponse handleGetRequest(
        String pathInfo,
        QueryParameters queryParams,
        HttpRequestContext requestContext,
        HttpServletResponse response
    ) throws FrameworkException {

        HttpServletRequest request = requestContext.getRequest();

        boolean shouldRedactSecretValues = !isActionPermitted(BuiltInAction.SECRETS_GET_UNREDACTED_VALUES, requestContext.getUsername());

        logger.info("handleGetRequest() entered. Getting secret with the given name");
        String secretName = getSecretNameFromPath(pathInfo);
        GalasaSecret secret = getSecretByName(secretName, shouldRedactSecretValues);

        logger.info("handleGetRequest() exiting");
        return getResponseBuilder().buildResponse(request, response, "application/json",
            gson.toJson(secret), HttpServletResponse.SC_OK);
    }


    @Override
    public HttpServletResponse handlePutRequest(
        String pathInfo,
        HttpRequestContext requestContext,
        HttpServletResponse response
    ) throws FrameworkException, IOException {
        logger.info("handlePutRequest() entered. Validating request payload");

        HttpServletRequest request = requestContext.getRequest();
        checkRequestHasContent(request);

        String secretName = getSecretNameFromPath(pathInfo);
        String lastUpdatedByUser = requestContext.getUsername();
        SecretRequest secretPayload = parseRequestBody(request, SecretRequest.class);

        ICredentials existingSecret = credentialsService.getCredentials(secretName);
        GalasaSecretType existingSecretType = getSecretType(existingSecret);
        validateUpdateRequest(existingSecretType, secretPayload, existingSecret);

        ICredentials decodedSecret = null;
        int responseCode = HttpServletResponse.SC_NO_CONTENT;
        if (existingSecret == null) {
            // No secret with the given name exists, so create a new one
            decodedSecret = buildDecodedCredentialsToSet(secretPayload, lastUpdatedByUser);
            responseCode = HttpServletResponse.SC_CREATED;
        } else if (secretPayload.gettype() != null) {
            // When a secret type is given, all relevant fields for that type are required,
            // so overwrite the existing secret to change its type
            decodedSecret = buildDecodedCredentialsToSet(secretPayload, lastUpdatedByUser);
        } else {
            // A secret already exists and no type was given, so just update the secret by
            // overriding its existing values with the values provided in the request
            decodedSecret = getOverriddenSecret(existingSecretType, existingSecret, secretPayload);
            String description = getOverriddenValue(existingSecret.getDescription(), secretPayload.getdescription());
            setSecretMetadataProperties(decodedSecret, description, lastUpdatedByUser);
        }
        credentialsService.setCredentials(secretName, decodedSecret);

        logger.info("handlePutRequest() exiting");
        return getResponseBuilder().buildResponse(request, response, responseCode);
    }

    @Override
    public HttpServletResponse handleDeleteRequest(
        String pathInfo,
        HttpRequestContext requestContext,
        HttpServletResponse response
    ) throws FrameworkException {
        logger.info("handleDeleteRequest() entered");

        HttpServletRequest request = requestContext.getRequest();

        // The provided name is implicitly validated by the route's regex pattern
        String secretName = getSecretNameFromPath(pathInfo);
        deleteSecret(secretName);

        logger.info("handleDeleteRequest() exiting");
        return getResponseBuilder().buildResponse(request, response, HttpServletResponse.SC_NO_CONTENT);
    }

    private void validateUpdateRequest(GalasaSecretType existingSecretType, SecretRequest secretPayload, ICredentials existingSecret) throws InternalServletException {
        SecretRequestValidator updateSecretValidator = new UpdateSecretRequestValidator(existingSecretType);
        updateSecretValidator.validate(secretPayload);

        logger.info("Request payload validated OK");
    }

    private String getSecretNameFromPath(String pathInfo) throws InternalServletException {
        Matcher matcher = this.getPathRegex().matcher(pathInfo);
        matcher.matches();
        return matcher.group(1);
    }

    private GalasaSecret getSecretByName(String secretName, boolean shouldRedactSecretValues) throws InternalServletException {
        GalasaSecret secret = null;
        try {
            ICredentials credentials = credentialsService.getCredentials(secretName);

            if (credentials == null) {
                ServletError error = new ServletError(GAL5093_ERROR_SECRET_NOT_FOUND);
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }

            logger.info("A secret with the given name was found OK");

            secret = createGalasaSecretFromCredentials(secretName, credentials, shouldRedactSecretValues);
        } catch (CredentialsException e) {
            ServletError error = new ServletError(GAL5094_FAILED_TO_GET_SECRET_FROM_CREDS);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return secret;
    }

    private void deleteSecret(String secretName) throws InternalServletException {
        try {
            if (credentialsService.getCredentials(secretName) == null) {
                ServletError error = new ServletError(GAL5076_ERROR_SECRET_DOES_NOT_EXIST);
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }
            logger.info("Attempting to delete the secret with the given name");

            credentialsService.deleteCredentials(secretName);

            logger.info("The secret with the given name was deleted OK");

        } catch (CredentialsException e) {
            ServletError error = new ServletError(GAL5078_FAILED_TO_DELETE_SECRET);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private ICredentials getOverriddenSecret(GalasaSecretType existingSecretType, ICredentials existingSecret, SecretRequest secretRequest) throws InternalServletException {
        ICredentials overriddenSecret = existingSecret;

        if (existingSecretType == USERNAME) {
            CredentialsUsername usernameSecret = (CredentialsUsername) existingSecret;
            String overriddenUsername = getOverriddenUsername(usernameSecret.getUsername(), secretRequest.getusername());
            overriddenSecret = new CredentialsUsername(overriddenUsername);
        } else if (existingSecretType == TOKEN) {
            CredentialsToken tokenSecret = (CredentialsToken) existingSecret;
            String overriddenToken = getOverriddenToken(new String(tokenSecret.getToken()), secretRequest.gettoken());
            overriddenSecret = new CredentialsToken(overriddenToken);
        } else if (existingSecretType == USERNAME_PASSWORD) {
            CredentialsUsernamePassword usernamePasswordSecret = (CredentialsUsernamePassword) existingSecret;
            String overriddenUsername = getOverriddenUsername(usernamePasswordSecret.getUsername(), secretRequest.getusername());
            String overriddenPassword = getOverriddenPassword(usernamePasswordSecret.getPassword(), secretRequest.getpassword());
            overriddenSecret = new CredentialsUsernamePassword(overriddenUsername, overriddenPassword);
        } else if (existingSecretType == USERNAME_TOKEN) {
            CredentialsUsernameToken usernameTokenSecret = (CredentialsUsernameToken) existingSecret;
            String overriddenUsername = getOverriddenUsername(usernameTokenSecret.getUsername(), secretRequest.getusername());
            String overriddenToken = getOverriddenToken(new String(usernameTokenSecret.getToken()), secretRequest.gettoken());
            overriddenSecret = new CredentialsUsernameToken(overriddenUsername, overriddenToken);
        } else {
            // The credentials are in an unknown format, throw an error
            ServletError error = new ServletError(GAL5101_ERROR_UNEXPECTED_SECRET_TYPE_DETECTED);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return overriddenSecret;
    }

    private String getOverriddenValue(String originalValue, String possibleOverride) {
        String newValue = originalValue;
        if (possibleOverride != null) {
            newValue = possibleOverride;
        }
        return newValue;
    }

    private String getOverriddenUsername(String existingUsername, SecretRequestusername requestUsername) throws InternalServletException {
        String overriddenUsername = existingUsername;
        if (requestUsername != null) {
            String possiblyDecodedUsername = decodeSecretValue(requestUsername.getvalue(), requestUsername.getencoding());
            overriddenUsername = getOverriddenValue(existingUsername, possiblyDecodedUsername);
        }
        return overriddenUsername;
    }

    private String getOverriddenPassword(String existingPassword, SecretRequestpassword requestPassword) throws InternalServletException {
        String overriddenPassword = existingPassword;
        if (requestPassword != null) {
            String possiblyDecodedPassword = decodeSecretValue(requestPassword.getvalue(), requestPassword.getencoding());
            overriddenPassword = getOverriddenValue(existingPassword, possiblyDecodedPassword);
        }
        return overriddenPassword;
    }

    private String getOverriddenToken(String existingToken, SecretRequesttoken requestToken) throws InternalServletException {
        String overriddenToken = existingToken;
        if (requestToken != null) {
            String possiblyDecodedToken = decodeSecretValue(requestToken.getvalue(), requestToken.getencoding());
            overriddenToken = getOverriddenValue(existingToken, possiblyDecodedToken);
        }
        return overriddenToken;
    }
}
