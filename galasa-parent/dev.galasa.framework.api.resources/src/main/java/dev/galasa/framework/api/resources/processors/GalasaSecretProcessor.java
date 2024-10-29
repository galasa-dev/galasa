/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.processors;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;
import static dev.galasa.framework.api.common.resources.ResourceAction.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Base64.Decoder;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;

import dev.galasa.ICredentials;
import dev.galasa.framework.api.beans.generated.GalasaSecret;
import dev.galasa.framework.api.beans.generated.GalasaSecretdata;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.GalasaSecretType;
import dev.galasa.framework.api.common.resources.ResourceAction;
import dev.galasa.framework.api.common.resources.Secret;
import dev.galasa.framework.spi.creds.CredentialsToken;
import dev.galasa.framework.spi.creds.CredentialsUsername;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.framework.spi.creds.CredentialsUsernameToken;
import dev.galasa.framework.spi.creds.ICredentialsService;

/**
 * Processor class to handle creating, updating, and deleting GalasaSecret resources
 */
public class GalasaSecretProcessor extends AbstractGalasaResourceProcessor implements IGalasaResourceProcessor {

    private final Log logger = LogFactory.getLog(getClass());

    private static final String DEFAULT_API_VERSION = "galasa-dev/v1alpha1";
    private static final List<String> SUPPORTED_ENCODING_SCHEMES = List.of("base64");

    private ICredentialsService credentialsService;

    public GalasaSecretProcessor(ICredentialsService credentialsService) {
        this.credentialsService = credentialsService;
    }

    @Override
    public List<String> processResource(JsonObject resourceJson, ResourceAction action) throws InternalServletException {
        logger.info("Processing GalasaSecret resource");
        List<String> errors = checkGalasaSecretJsonStructure(resourceJson, action);
        if (errors.isEmpty()) {
            logger.info("GalasaSecret validated successfully");
            GalasaSecret galasaSecret = gson.fromJson(resourceJson, GalasaSecret.class);
            String credentialsId = galasaSecret.getmetadata().getname();
            Secret secret = new Secret(credentialsService, credentialsId);

            if (action == DELETE) {
                logger.info("Deleting secret from credentials store");
                secret.deleteSecretFromCredentialsStore();
                logger.info("Deleted secret from credentials store OK");
            } else {
                secret.loadValueFromCredentialsStore();
                boolean secretExists = secret.existsInCredentialsStore();
                if (action == CREATE && secretExists) {
                    ServletError error = new ServletError(GAL5075_ERROR_SECRET_ALREADY_EXISTS);
                    throw new InternalServletException(error, HttpServletResponse.SC_CONFLICT);
                } else if (action == UPDATE && !secretExists) {
                    ServletError error = new ServletError(GAL5076_ERROR_SECRET_DOES_NOT_EXIST);
                    throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
                }
                
                GalasaSecretType secretType = GalasaSecretType.getFromString(galasaSecret.getmetadata().gettype().toString());
                GalasaSecretdata decodedData = decodeSecretData(galasaSecret);
                ICredentials credentials = getCredentialsFromSecret(secretType, decodedData);

                logger.info("Setting secret in credentials store");
                secret.setSecretToCredentialsStore(credentials);
                logger.info("Secret set in credentials store OK");
            }
            logger.info("Processed GalasaSecret resource OK");
        }
        return errors;
    }

    private GalasaSecretdata decodeSecretData(GalasaSecret galasaSecret) throws InternalServletException {
        String encoding = galasaSecret.getmetadata().getencoding();
        GalasaSecretdata existingData = galasaSecret.getdata();

        GalasaSecretdata decodedData = new GalasaSecretdata();

        if (encoding == null) {
            decodedData = existingData;
        } else if (encoding.equalsIgnoreCase("base64")) {
            logger.info("Base64-decoding the provided GalasaSecret resource data");
            Decoder decoder = Base64.getDecoder();

            String username = existingData.getusername();
            String password = existingData.getpassword();
            String token = existingData.gettoken();

            if (username != null) {
                decodedData.setusername(new String(decoder.decode(username), StandardCharsets.UTF_8));
            }

            if (password != null) {
                decodedData.setpassword(new String(decoder.decode(password), StandardCharsets.UTF_8));
            }

            if (token != null) {
                decodedData.settoken(new String(decoder.decode(token), StandardCharsets.UTF_8));
            }
            logger.info("Decoded the provided GalasaSecret resource data OK");
        } else {
            // This should never be reached since the secret JSON has already been validated
            ServletError error = new ServletError(GAL5073_UNSUPPORTED_GALASA_SECRET_ENCODING, String.join(", ", SUPPORTED_ENCODING_SCHEMES));
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        return decodedData;
    }

    private List<String> checkGalasaSecretJsonStructure(JsonObject secretJson, ResourceAction action) throws InternalServletException {
        checkResourceHasRequiredFields(secretJson, DEFAULT_API_VERSION, action);
        
        List<String> validationErrors = new ArrayList<>();
        validateSecretMetadata(secretJson, validationErrors);

        // Delete operations shouldn't require a 'data' section, just the metadata to identify
        // the credentials entry to delete
        if (validationErrors.isEmpty() && action != DELETE) {
            validateSecretData(secretJson, validationErrors);
        }
        return validationErrors;
    }

    private ICredentials getCredentialsFromSecret(GalasaSecretType secretType, GalasaSecretdata decodedData) {
        ICredentials credentials = null;
        switch (secretType) {
            case USERNAME:
                credentials = new CredentialsUsername(decodedData.getusername());
                break;
            case TOKEN:
                credentials = new CredentialsToken(decodedData.gettoken());
                break;
            case USERNAME_PASSWORD:
                credentials = new CredentialsUsernamePassword(decodedData.getusername(), decodedData.getpassword());
                break;
            case USERNAME_TOKEN:
                credentials = new CredentialsUsernameToken(decodedData.getusername(), decodedData.gettoken());
                break;
            default:
                break;
        }
        return credentials;
    }

    private void validateSecretMetadata(JsonObject secretJson, List<String> validationErrors) {
        JsonObject metadata = secretJson.get("metadata").getAsJsonObject();

        // Check if the secret has a name and a type
        if (!metadata.has("name") || !metadata.has("type")) {
            ServletError error = new ServletError(GAL5070_INVALID_GALASA_SECRET_MISSING_FIELDS, "metadata", "name, type");
            validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
        }

        // Check if the given secret type is a valid type
        if (metadata.has("type")) {
            GalasaSecretType secretType = GalasaSecretType.getFromString(metadata.get("type").getAsString());
            if (secretType == null) {
                String supportedSecretTypes = Arrays.stream(GalasaSecretType.values())
                    .map(GalasaSecretType::toString)
                    .collect(Collectors.joining(", "));
    
                ServletError error = new ServletError(GAL5074_UNKNOWN_GALASA_SECRET_TYPE, supportedSecretTypes);
                validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
            }
        }

        // Check if the given encoding scheme is supported
        if (metadata.has("encoding") && !SUPPORTED_ENCODING_SCHEMES.contains(metadata.get("encoding").getAsString())) {
            ServletError error = new ServletError(GAL5073_UNSUPPORTED_GALASA_SECRET_ENCODING, String.join(", ", SUPPORTED_ENCODING_SCHEMES));
            validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
        }
    }

    private void validateSecretData(JsonObject secretJson, List<String> validationErrors) {
        JsonObject metadata = secretJson.get("metadata").getAsJsonObject();
        JsonObject data = secretJson.get("data").getAsJsonObject();

        GalasaSecretType secretType = GalasaSecretType.getFromString(metadata.get("type").getAsString());
        String[] requiredTypeFields = secretType.getRequiredDataFields();
        List<String> missingFields = getMissingResourceFields(data, Arrays.asList(requiredTypeFields));

        if (!missingFields.isEmpty()) {
            ServletError error = new ServletError(GAL5072_INVALID_GALASA_SECRET_MISSING_TYPE_DATA, secretType.toString(), String.join(", ", missingFields));
            validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
        }
    }
}
