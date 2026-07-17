/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.secrets.internal;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.util.Base64;

import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.beans.generated.SecretRequest;
import dev.galasa.framework.api.beans.generated.SecretRequestbinary;
import dev.galasa.framework.api.beans.generated.SecretRequestkeystore;
import dev.galasa.framework.api.beans.generated.SecretRequestKeystorePassword;
import dev.galasa.framework.api.beans.generated.SecretRequestpassword;
import dev.galasa.framework.api.beans.generated.SecretRequesttoken;
import dev.galasa.framework.api.beans.generated.SecretRequestusername;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.SecretValidator;

public class SecretRequestValidator extends SecretValidator<SecretRequest> {

    @Override
    public void validate(SecretRequest secretRequest) throws InternalServletException {
        SecretRequestusername username = secretRequest.getusername();
        SecretRequestpassword password = secretRequest.getpassword();
        SecretRequesttoken token = secretRequest.gettoken();
        SecretRequestkeystore keystore = secretRequest.getkeystore();
        SecretRequestKeystorePassword keystorePassword = secretRequest.getKeystorePassword();
        String keystoreType = secretRequest.getKeystoreType();
        SecretRequestbinary binary = secretRequest.getbinary();

        // Check that the secret has been given a name
        validateSecretName(secretRequest.getname());

        validateDescription(secretRequest.getdescription());

        // Validate binary mutual exclusivity with all other fields
        validateBinaryMutualExclusivity(secretRequest);

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

        validateSecretRequestFields(username, password, token, keystore, keystorePassword, keystoreType, binary);
    }

    protected void validateSecretRequestFields(
        SecretRequestusername username,
        SecretRequestpassword password,
        SecretRequesttoken token,
        SecretRequestkeystore keystore,
        SecretRequestKeystorePassword keystorePassword,
        String keystoreType,
        SecretRequestbinary binary
    ) throws InternalServletException {
        if (username != null) {
            validateField(username.getvalue(), username.getencoding());
        }

        if (password != null) {
            validateField(password.getvalue(), password.getencoding());
        }

        if (token != null) {
            validateField(token.getvalue(), token.getencoding());
        }

        if (keystore != null) {
            validateField(keystore.getvalue(), keystore.getencoding());
            validateKeystoreBase64Encoding(keystore.getvalue());
        }

        if (keystorePassword != null) {
            // "" is valid (no-password keystore), but whitespace-only is not
            validateEncoding(keystorePassword.getencoding());
            if (keystorePassword.getvalue() != null && !keystorePassword.getvalue().isEmpty() && keystorePassword.getvalue().isBlank()) {
                ServletError error = new ServletError(GAL5096_ERROR_MISSING_SECRET_VALUE);
                throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
            }
        }

        if (keystoreType != null) {
            validateField(keystoreType, null);
        }

        if (binary != null) {
            validateBinaryField(binary);
        }
    }

    private void validateField(String value, String encoding) throws InternalServletException {
        validateEncoding(encoding);

        if (value == null || value.isBlank()) {
            ServletError error = new ServletError(GAL5096_ERROR_MISSING_SECRET_VALUE);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void validateEncoding(String encoding) throws InternalServletException {
        if (encoding != null && !SUPPORTED_ENCODING_SCHEMES.contains(encoding)) {
            ServletError error = new ServletError(GAL5073_UNSUPPORTED_GALASA_SECRET_ENCODING, String.join(", ", SUPPORTED_ENCODING_SCHEMES));
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Validates that keystore credentials are not mixed with username, password or token fields.
     * Keystore credentials are mutually exclusive with username, password and token fields.
     *
     * @param secretRequest the request to validate
     * @throws InternalServletException if validation fails
     */
    protected void validateBinaryMutualExclusivity(SecretRequest secretRequest) throws InternalServletException {
        SecretRequestbinary binary = secretRequest.getbinary();
        if (binary == null) {
            return;
        }

        if (secretRequest.getusername() != null) {
            throwBinaryMutualExclusivityError("username");
        }
        if (secretRequest.getpassword() != null) {
            throwBinaryMutualExclusivityError("password");
        }
        if (secretRequest.gettoken() != null) {
            throwBinaryMutualExclusivityError("token");
        }
        if (secretRequest.getkeystore() != null) {
            throwBinaryMutualExclusivityError("keystore");
        }
        if (secretRequest.getKeystorePassword() != null) {
            throwBinaryMutualExclusivityError("keystorePassword");
        }
        if (secretRequest.getKeystoreType() != null) {
            throwBinaryMutualExclusivityError("keystoreType");
        }
    }

    private void throwBinaryMutualExclusivityError(String conflictingField) throws InternalServletException {
        ServletError error = new ServletError(GAL5463_MUTUALLY_EXCLUSIVE_BINARY_FIELD_PROVIDED, conflictingField);
        throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
    }

    protected void validateKeystoreMutualExclusivity(SecretRequest secretRequest) throws InternalServletException {
        SecretRequestkeystore keystore = secretRequest.getkeystore();
        SecretRequestusername username = secretRequest.getusername();
        SecretRequestpassword password = secretRequest.getpassword();
        SecretRequesttoken token = secretRequest.gettoken();

        if (keystore != null && username != null) {
            ServletError error = new ServletError(GAL5451_MUTUALLY_EXCLUSIVE_FIELDS_PROVIDED, "username");
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        if (keystore != null && password != null) {
            ServletError error = new ServletError(GAL5451_MUTUALLY_EXCLUSIVE_FIELDS_PROVIDED, "password");
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        if (keystore != null && token != null) {
            ServletError error = new ServletError(GAL5451_MUTUALLY_EXCLUSIVE_FIELDS_PROVIDED, "token");
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Validates that the binary field contains a non-empty, valid base64-encoded value.
     *
     * @param binary the binary request object to validate
     * @throws InternalServletException if validation fails
     */
    private void validateBinaryField(SecretRequestbinary binary) throws InternalServletException {
        validateField(binary.getvalue(), binary.getencoding());
        // Validate that it is valid base64
        try {
            Base64.getDecoder().decode(binary.getvalue());
        } catch (IllegalArgumentException e) {
            ServletError error = new ServletError(GAL5452_INVALID_BASE64_ENCODING, "binary");
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Validates that the keystore value contains valid base64-encoded data.
     *
     * @param keystoreValue the keystore value to validate
     * @throws InternalServletException if validation fails
     */
    private void validateKeystoreBase64Encoding(String keystoreValue) throws InternalServletException {
        if (keystoreValue == null || keystoreValue.isBlank()) {
            throwInvalidKeystoreEncodingError();
        }

        // Validate that it's valid base64
        try {
            Base64.getDecoder().decode(keystoreValue);
        } catch (IllegalArgumentException e) {
            throwInvalidKeystoreEncodingError();
        }
    }

    private void throwInvalidKeystoreEncodingError() throws InternalServletException {
        ServletError error = new ServletError(GAL5452_INVALID_BASE64_ENCODING, "keystore");
        throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
    }
}
