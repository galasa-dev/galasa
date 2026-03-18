/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.secrets.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;
import static dev.galasa.framework.api.beans.generated.GalasaSecretType.*;

import java.io.ByteArrayOutputStream;
import java.security.KeyStore;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsKeyStore;
import dev.galasa.ICredentialsToken;
import dev.galasa.ICredentialsUsername;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.ICredentialsUsernameToken;
import dev.galasa.framework.api.beans.generated.GalasaSecret;
import dev.galasa.framework.api.beans.generated.GalasaSecretdata;
import dev.galasa.framework.api.beans.generated.GalasaSecretmetadata;
import dev.galasa.framework.api.beans.generated.SecretRequest;
import dev.galasa.framework.api.beans.generated.SecretRequestkeystore;
import dev.galasa.framework.api.beans.generated.SecretRequestKeystorePassword;
import dev.galasa.framework.api.beans.generated.SecretRequestpassword;
import dev.galasa.framework.api.beans.generated.SecretRequesttoken;
import dev.galasa.framework.api.beans.generated.SecretRequestusername;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ProtectedRoute;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.GalasaResourceValidator;
import dev.galasa.framework.api.common.resources.GalasaSecretType;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.CredentialsKeyStore;
import dev.galasa.framework.spi.creds.CredentialsToken;
import dev.galasa.framework.spi.creds.CredentialsUsername;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.framework.spi.creds.CredentialsUsernameToken;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.utils.ITimeService;

public abstract class AbstractSecretsRoute extends ProtectedRoute {

    public static final String REDACTED_SECRET_VALUE = "******";
    private static final String DEFAULT_RESPONSE_ENCODING = "base64";

    protected ITimeService timeService;

    private static final Map<Class<? extends ICredentials>, GalasaSecretType> credentialsToSecretTypes = Map.of(
        CredentialsUsername.class, GalasaSecretType.USERNAME,
        CredentialsToken.class, GalasaSecretType.TOKEN,
        CredentialsUsernamePassword.class, GalasaSecretType.USERNAME_PASSWORD,
        CredentialsUsernameToken.class, GalasaSecretType.USERNAME_TOKEN,
        CredentialsKeyStore.class, GalasaSecretType.KEYSTORE
    );

    public AbstractSecretsRoute(
        ResponseBuilder responseBuilder,
        String path,
        ITimeService timeService,
        RBACService rbacService
    ) {
        super(responseBuilder, path, rbacService);
        this.timeService = timeService;
    }

    protected GalasaSecret createGalasaSecretFromCredentials(String secretName, ICredentials credentials, boolean shouldRedactSecretValues) throws InternalServletException {
        GalasaSecretmetadata metadata = new GalasaSecretmetadata(null);
        GalasaSecretdata data = new GalasaSecretdata();
        
        metadata.setname(secretName);

        if (shouldRedactSecretValues) {
            setRedactedSecretTypeValues(metadata, data, credentials);
        } else {
            metadata.setencoding(DEFAULT_RESPONSE_ENCODING);
            setSecretTypeValuesFromCredentials(metadata, data, credentials);
        }

        setSecretMetadata(metadata, credentials.getDescription(), credentials.getLastUpdatedByUser(), credentials.getLastUpdatedTime());
        GalasaSecret secret = new GalasaSecret();
        secret.setApiVersion(GalasaResourceValidator.DEFAULT_API_VERSION);
        secret.setdata(data);
        secret.setmetadata(metadata);

        return secret;
    }

    protected ICredentials buildDecodedCredentialsToSet(SecretRequest secretRequest, String lastUpdatedByUser) throws InternalServletException {
        ICredentials decodedSecret = decodeCredentialsFromSecretPayload(secretRequest);
        setSecretMetadataProperties(decodedSecret, secretRequest.getdescription(), lastUpdatedByUser);
        return decodedSecret;
    }

    /**
     * Decodes credentials from a secret request payload.
     *
     * Precedence order:
     * 1. Username-based credentials (username + password/token, or username alone)
     * 2. Keystore credentials
     * 3. Token-only credentials
     *
     * @param secretRequest the request containing encoded credential data
     * @return decoded credentials, or null if no valid credentials found
     * @throws InternalServletException if decoding fails
     */
    private ICredentials decodeCredentialsFromSecretPayload(SecretRequest secretRequest) throws InternalServletException {
        ICredentials credentials = null;
        SecretRequestusername username = secretRequest.getusername();
        
        if (username != null) {
            credentials = decodeUsernameBasedCredentials(secretRequest, username);
        } else {
            SecretRequestkeystore keystore = secretRequest.getkeystore();
            if (keystore != null) {
                credentials = decodeKeystoreCredentials(secretRequest, keystore);
            } else {
                SecretRequesttoken token = secretRequest.gettoken();
                if (token != null) {
                    credentials = decodeTokenCredentials(token);
                }
            }
        }
        return credentials;
    }

    /**
     * Decodes username-based credentials (username + password, username + token, or username alone).
     *
     * @param secretRequest the request containing the credential data
     * @param username the username request object
     * @return decoded username-based credentials
     * @throws InternalServletException if decoding fails
     */
    private ICredentials decodeUsernameBasedCredentials(SecretRequest secretRequest, SecretRequestusername username) throws InternalServletException {
        String decodedUsername = decodeSecretValue(username.getvalue(), username.getencoding());
        ICredentials credentials;
        
        SecretRequestpassword password = secretRequest.getpassword();
        if (password != null) {
            // We have a username and password
            String decodedPassword = decodeSecretValue(password.getvalue(), password.getencoding());
            credentials = new CredentialsUsernamePassword(decodedUsername, decodedPassword);
        } else {
            SecretRequesttoken token = secretRequest.gettoken();
            if (token != null) {
                // We have a username and token
                String decodedToken = decodeSecretValue(token.getvalue(), token.getencoding());
                credentials = new CredentialsUsernameToken(decodedUsername, decodedToken);
            } else {
                // We have a username only
                credentials = new CredentialsUsername(decodedUsername);
            }
        }
        return credentials;
    }

    /**
     * Decodes keystore credentials with optional password and type.
     *
     * @param secretRequest the request containing the credential data
     * @param keystore the keystore request object
     * @return decoded keystore credentials
     * @throws InternalServletException if decoding fails or keystore creation fails
     */
    private ICredentials decodeKeystoreCredentials(SecretRequest secretRequest, SecretRequestkeystore keystore) throws InternalServletException {
        String decodedKeystore = decodeSecretValue(keystore.getvalue(), keystore.getencoding());
        
        // Get optional password
        String decodedKeystorePass = "";
        SecretRequestKeystorePassword keystorePassword = secretRequest.getKeystorePassword();
        if (keystorePassword != null) {
            decodedKeystorePass = decodeSecretValue(keystorePassword.getvalue(), keystorePassword.getencoding());
        }
        
        String type = secretRequest.getKeystoreType();
        
        // Create CredentialsKeyStore - it expects base64-encoded keystore with "base64:" prefix
        try {
            return new CredentialsKeyStore(decodedKeystore, decodedKeystorePass, type);
        } catch (CredentialsException | IllegalArgumentException e) {
            ServletError error = new ServletError(GAL5450_FAILED_TO_CREATE_KEYSTORE_CREDENTIALS);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST, e);
        }
    }

    /**
     * Decodes token-only credentials.
     *
     * @param token the token request object
     * @return decoded token credentials
     * @throws InternalServletException if decoding fails
     */
    private ICredentials decodeTokenCredentials(SecretRequesttoken token) throws InternalServletException {
        String decodedToken = decodeSecretValue(token.getvalue(), token.getencoding());
        return new CredentialsToken(decodedToken);
    }

    protected String decodeSecretValue(String possiblyEncodedValue, String encoding) throws InternalServletException {
        String decodedValue = possiblyEncodedValue;
        if (encoding != null && possiblyEncodedValue != null) {
            try {
                if (encoding.equalsIgnoreCase(DEFAULT_RESPONSE_ENCODING)) {
                    byte[] decodedBytes = Base64.getDecoder().decode(possiblyEncodedValue);
                    decodedValue = new String(decodedBytes);
                }
            } catch (IllegalArgumentException e) {
                ServletError error = new ServletError(GAL5097_FAILED_TO_DECODE_SECRET_VALUE, DEFAULT_RESPONSE_ENCODING);
                throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        return decodedValue;
    }

    private void setRedactedSecretTypeValues(GalasaSecretmetadata metadata, GalasaSecretdata data, ICredentials credentials) throws InternalServletException {
        GalasaSecretType secretType = getSecretType(credentials);
        if (secretType == GalasaSecretType.USERNAME) {
            data.setusername(REDACTED_SECRET_VALUE);
            metadata.settype(Username);
        } else if (secretType == GalasaSecretType.USERNAME_PASSWORD) {
            data.setusername(REDACTED_SECRET_VALUE);
            data.setpassword(REDACTED_SECRET_VALUE);
            metadata.settype(USERNAME_PASSWORD);
        } else if (secretType == GalasaSecretType.USERNAME_TOKEN) {
            data.setusername(REDACTED_SECRET_VALUE);
            data.settoken(REDACTED_SECRET_VALUE);
            metadata.settype(USERNAME_TOKEN);
        } else if (secretType == GalasaSecretType.TOKEN) {
            data.settoken(REDACTED_SECRET_VALUE);
            metadata.settype(Token);
        } else if (secretType == GalasaSecretType.KEYSTORE) {
            ICredentialsKeyStore keyStoreCredentials = (ICredentialsKeyStore) credentials;
            data.setkeystore(REDACTED_SECRET_VALUE);
            data.setKeystorePassword(REDACTED_SECRET_VALUE);
            // KeyStore type is not sensitive, so don't redact it
            data.setKeystoreType(keyStoreCredentials.getKeyStoreType());
            metadata.settype(KEY_STORE);
        } else {
            // The credentials are in an unknown format, throw an error
            ServletError error = new ServletError(GAL5101_ERROR_UNEXPECTED_SECRET_TYPE_DETECTED);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void setSecretTypeValuesFromCredentials(GalasaSecretmetadata metadata, GalasaSecretdata data, ICredentials credentials) throws InternalServletException {
        GalasaSecretType secretType = getSecretType(credentials);
        if (secretType == GalasaSecretType.USERNAME) {
            ICredentialsUsername usernameCredentials = (ICredentialsUsername) credentials;
            data.setusername(encodeValue(usernameCredentials.getUsername()));

            metadata.settype(Username);
        } else if (secretType == GalasaSecretType.USERNAME_PASSWORD) {
            ICredentialsUsernamePassword usernamePasswordCredentials = (ICredentialsUsernamePassword) credentials;
            data.setusername(encodeValue(usernamePasswordCredentials.getUsername()));
            data.setpassword(encodeValue(usernamePasswordCredentials.getPassword()));

            metadata.settype(USERNAME_PASSWORD);
        } else if (secretType == GalasaSecretType.USERNAME_TOKEN) {
            ICredentialsUsernameToken usernameTokenCredentials = (ICredentialsUsernameToken) credentials;
            data.setusername(encodeValue(usernameTokenCredentials.getUsername()));
            data.settoken(encodeValue(new String(usernameTokenCredentials.getToken())));

            metadata.settype(USERNAME_TOKEN);
        } else if (secretType == GalasaSecretType.TOKEN) {
            ICredentialsToken tokenCredentials = (ICredentialsToken) credentials;
            data.settoken(encodeValue(new String(tokenCredentials.getToken())));
            metadata.settype(Token);
        } else if (secretType == GalasaSecretType.KEYSTORE) {
            ICredentialsKeyStore keyStoreCredentials = (ICredentialsKeyStore) credentials;
            data.setkeystore(encodeValue(keyStoreCredentials.getEncodedKeyStore()));
            data.setKeystorePassword(encodeValue(keyStoreCredentials.getKeyStorePassword()));
            data.setKeystoreType(keyStoreCredentials.getKeyStoreType());
            
            metadata.settype(KEY_STORE);
        } else {
            // The credentials are in an unknown format, throw an error
            ServletError error = new ServletError(GAL5101_ERROR_UNEXPECTED_SECRET_TYPE_DETECTED);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void setSecretMetadata(GalasaSecretmetadata metadata, String description, String username, Instant timestamp) {
        metadata.setdescription(description);
        metadata.setLastUpdatedBy(username);

        if (timestamp != null) {
            metadata.setLastUpdatedTime(timestamp.toString());
        }
    }

    private String encodeValue(String value) {
        String encodedValue = value;
        if (DEFAULT_RESPONSE_ENCODING.equals("base64")) {
            encodedValue = Base64.getEncoder().encodeToString(value.getBytes());
        }
        return encodedValue;
    }

    protected GalasaSecretType getSecretType(ICredentials existingSecret) {
        GalasaSecretType existingSecretType = null;
        if (existingSecret != null) {
            existingSecretType = credentialsToSecretTypes.get(existingSecret.getClass());
        }
        return existingSecretType;
    }

    protected void setSecretMetadataProperties(ICredentials secret, String description, String lastUpdatedByUser) {
        if (description != null && !description.isBlank()) {
            secret.setDescription(description);
        }
        secret.setLastUpdatedByUser(lastUpdatedByUser);
        secret.setLastUpdatedTime(timeService.now());
    }
}