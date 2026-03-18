/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.secrets.internal;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.beans.generated.SecretRequest;
import dev.galasa.framework.api.beans.generated.SecretRequestKeystorePassword;
import dev.galasa.framework.api.beans.generated.SecretRequestkeystore;
import dev.galasa.framework.api.beans.generated.SecretRequestpassword;
import dev.galasa.framework.api.beans.generated.SecretRequesttoken;
import dev.galasa.framework.api.beans.generated.SecretRequestusername;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.GalasaSecretType;
import dev.galasa.framework.spi.utils.GalasaGson;

public class UpdateSecretRequestValidator extends SecretRequestValidator {

    private static final GalasaGson gson = new GalasaGson();

    private GalasaSecretType existingSecretType;

    public UpdateSecretRequestValidator(GalasaSecretType existingSecretType) {
        this.existingSecretType = existingSecretType;
    }

    @Override
    public void validate(SecretRequest secretRequest) throws InternalServletException {
        if (existingSecretType == null) {
            validateCreateSecretRequest(secretRequest);
        } else {
            validateUpdateSecretRequest(secretRequest);
        }
    }

    private void validateCreateSecretRequest(SecretRequest secretRequest) throws InternalServletException {
        SecretRequestusername username = secretRequest.getusername();
        SecretRequestpassword password = secretRequest.getpassword();
        SecretRequesttoken token = secretRequest.gettoken();
        SecretRequestkeystore keystore = secretRequest.getkeystore();
        SecretRequestKeystorePassword keystorePassword = secretRequest.getKeystorePassword();
        String keystoreType = secretRequest.getKeystoreType();

        validateDescription(secretRequest.getdescription());

        // Validate keystore mutual exclusivity
        validateKeystoreMutualExclusivity(secretRequest);

        // Password and token are mutually exclusive, so error if both are provided
        if (password != null && token != null) {
            ServletError error = new ServletError(GAL5095_ERROR_PASSWORD_AND_TOKEN_PROVIDED);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        // Password cannot be specified on its own
        if (username == null && password != null) {
            ServletError error = new ServletError(GAL5098_ERROR_PASSWORD_MISSING_USERNAME);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        validateSecretRequestFields(username, password, token, keystore, keystorePassword, keystoreType);
    }

    private void validateUpdateSecretRequest(SecretRequest secretRequest) throws InternalServletException {
        SecretRequestusername username = secretRequest.getusername();
        SecretRequestpassword password = secretRequest.getpassword();
        SecretRequesttoken token = secretRequest.gettoken();
        SecretRequestkeystore keystore = secretRequest.getkeystore();
        SecretRequestKeystorePassword keystorePassword = secretRequest.getKeystorePassword();
        String keystoreType = secretRequest.getKeystoreType();

        validateDescription(secretRequest.getdescription());

        // Validate keystore mutual exclusivity
        validateKeystoreMutualExclusivity(secretRequest);

        // Password and token are mutually exclusive, so error if both are provided
        if (password != null && token != null) {
            ServletError error = new ServletError(GAL5095_ERROR_PASSWORD_AND_TOKEN_PROVIDED);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        String requestedType = secretRequest.gettype();
        if (existingSecretType != null && requestedType == null) {
            // The user intends to update an existing secret without changing its type
            // so make sure that the relevant fields have been supplied
            checkProvidedSecretFieldsAreRelevant(existingSecretType, secretRequest);
        } else if (requestedType != null) {
            GalasaSecretType secretType = GalasaSecretType.getFromString(requestedType.toString());
            if (secretType == null) {
                // An unknown type was provided, so throw an error
                String supportedSecretTypes = Arrays.stream(GalasaSecretType.values())
                    .map(GalasaSecretType::toString)
                    .collect(Collectors.joining(", "));
                ServletError error = new ServletError(GAL5074_UNKNOWN_GALASA_SECRET_TYPE, supportedSecretTypes);
                throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
            }
            // A specific type of secret was given, so make sure that all of the required fields
            // for the given type have been provided
            validateSecretTypeFields(secretType, secretRequest);
            checkProvidedSecretFieldsAreRelevant(secretType, secretRequest);
        }
        validateSecretRequestFields(username, password, token, keystore, keystorePassword, keystoreType);
    }

    private void validateSecretTypeFields(GalasaSecretType secretType, SecretRequest secretRequest) throws InternalServletException {
        JsonObject secretRequestJson = gson.toJsonTree(secretRequest).getAsJsonObject();
        for (String requiredField : secretType.getRequiredDataFields()) {
            if (!secretRequestJson.has(requiredField)) {
                ServletError error = new ServletError(GAL5099_ERROR_MISSING_REQUIRED_SECRET_FIELD, secretType.toString(), requiredField);
                throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }

    private void checkProvidedSecretFieldsAreRelevant(GalasaSecretType secretType, SecretRequest secretRequest) throws InternalServletException {
        JsonObject secretRequestJson = gson.toJsonTree(secretRequest).getAsJsonObject();
        Set<String> secretRequestFields = secretRequestJson.keySet()
            .stream()
            .filter(key -> !key.equals("name") && !key.equals("type") && !key.equals("description"))
            .collect(Collectors.toSet());

        List<String> requiredTypeFields = Arrays.asList(secretType.getRequiredDataFields());
        for (String field : secretRequestFields) {
            if (!requiredTypeFields.contains(field)) {
                ServletError error = new ServletError(GAL5100_ERROR_UNEXPECTED_SECRET_FIELD_PROVIDED, secretType.toString(), String.join(", ", requiredTypeFields));
                throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
}
