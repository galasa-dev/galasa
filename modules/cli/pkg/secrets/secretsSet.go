/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package secrets

import (
    "context"
    "log"
    "net/http"
    "strings"

    "github.com/galasa-dev/cli/pkg/embedded"
    galasaErrors "github.com/galasa-dev/cli/pkg/errors"
    "github.com/galasa-dev/cli/pkg/galasaapi"
    "github.com/galasa-dev/cli/pkg/spi"
)

const (
    BASE64_ENCODING = "base64"
)

// Creates or updates a Galasa Secret using the provided parameters into an ecosystem's credentials store
func SetSecret(
    secretName string,
    username string,
    password string,
    token string,
    base64Username string,
    base64Password string,
    base64Token string,
    secretType string,
    description string,
    console spi.Console,
    apiClient *galasaapi.APIClient,
    byteReader spi.ByteReader,
) error {
    var err error

    secretName, err = validateSecretName(secretName)
    if err == nil {
        log.Printf("Secret name validated OK")
        if description != "" {
            description, err = validateDescription(description)
        }

        if err == nil {
            err = validateFlagCombination(username, password, token, base64Username, base64Password, base64Token)

            if err == nil {
                requestUsername := createSecretRequestUsername(username, base64Username)
                requestPassword := createSecretRequestPassword(password, base64Password)
                requestToken := createSecretRequestToken(token, base64Token)

                var secretTypeValue galasaapi.NullableGalasaSecretType
                if secretType != "" {
                    secretTypeValue, err = validateSecretType(secretType)
                }

                if err == nil {
                    secretRequest := createSecretRequest(secretName, requestUsername, requestPassword, requestToken, secretTypeValue, description)
                    err = sendSetSecretRequest(secretRequest, apiClient, byteReader)
                }
            }
        }
    }
    log.Printf("SecretsSet exiting. err is %v\n", err)
    return err
}

func createSecretRequestUsername(username string, base64Username string) galasaapi.SecretRequestUsername {
    requestUsername := *galasaapi.NewSecretRequestUsername()

    username = strings.TrimSpace(username)
    base64Username = strings.TrimSpace(base64Username)

    if base64Username != "" {
        requestUsername.SetValue(base64Username)
        requestUsername.SetEncoding(BASE64_ENCODING)
    } else if username != "" {
        requestUsername.SetValue(username)
    }
    return requestUsername
}

func createSecretRequestPassword(password string, base64Password string) galasaapi.SecretRequestPassword {
    requestPassword := *galasaapi.NewSecretRequestPassword()

    if base64Password != "" {
        requestPassword.SetValue(base64Password)
        requestPassword.SetEncoding(BASE64_ENCODING)
    } else if password != "" {
        requestPassword.SetValue(password)
    }
    return requestPassword
}

func createSecretRequestToken(token string, base64Token string) galasaapi.SecretRequestToken {
    requestToken := *galasaapi.NewSecretRequestToken()

    if base64Token != "" {
        requestToken.SetValue(base64Token)
        requestToken.SetEncoding(BASE64_ENCODING)
    } else if token != "" {
        requestToken.SetValue(token)
    }
    return requestToken
}

func createSecretRequest(
    secretName string,
    username galasaapi.SecretRequestUsername,
    password galasaapi.SecretRequestPassword,
    token galasaapi.SecretRequestToken,
    secretType galasaapi.NullableGalasaSecretType,
    description string,
) *galasaapi.SecretRequest {
    secretRequest := galasaapi.NewSecretRequest()
    secretRequest.SetName(secretName)

    if description != "" {
        secretRequest.SetDescription(description)
    }

    if secretType.IsSet() {
        secretRequest.SetType(*secretType.Get())
    }

    if username.GetValue() != "" {
        secretRequest.SetUsername(username)
    }

    if password.GetValue() != "" {
        secretRequest.SetPassword(password)
    }

    if token.GetValue() != "" {
        secretRequest.SetToken(token)
    }
    return secretRequest
}

func sendSetSecretRequest(
    secretRequest *galasaapi.SecretRequest,
    apiClient *galasaapi.APIClient,
    byteReader spi.ByteReader,
) error {
    var err error
    var httpResponse *http.Response
    var context context.Context = context.Background()
    var restApiVersion string

    restApiVersion, err = embedded.GetGalasactlRestApiVersion()
    secretName := secretRequest.GetName()

    if err == nil {
        httpResponse, err = apiClient.SecretsAPIApi.UpdateSecret(context, secretName).
            ClientApiVersion(restApiVersion).
            SecretRequest(*secretRequest).
            Execute()

        if httpResponse != nil {
            defer httpResponse.Body.Close()
        }

        if err != nil {
            if httpResponse == nil {
                err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_SET_SECRET_REQUEST_FAILED, err.Error())
            } else {
                err = galasaErrors.HttpResponseToGalasaError(
                    httpResponse,
                    secretName,
                    byteReader,
                    galasaErrors.GALASA_ERROR_SET_SECRET_NO_RESPONSE_CONTENT,
                    galasaErrors.GALASA_ERROR_SET_SECRET_RESPONSE_BODY_UNREADABLE,
                    galasaErrors.GALASA_ERROR_SET_SECRET_UNPARSEABLE_CONTENT,
                    galasaErrors.GALASA_ERROR_SET_SECRET_SERVER_REPORTED_ERROR,
                    galasaErrors.GALASA_ERROR_SET_SECRET_EXPLANATION_NOT_JSON,
                )
            }
        }
    }
    return err
}

func validateSecretType(secretType string) (galasaapi.NullableGalasaSecretType, error) {
    var err error
    var nullableSecretType galasaapi.NullableGalasaSecretType
    secretType = strings.TrimSpace(secretType)

    // Try to convert the provided type into a GalasaSecretType value
    for _, supportedType := range galasaapi.AllowedGalasaSecretTypeEnumValues {
        if strings.EqualFold(secretType, string(supportedType)) {
            nullableSecretType = *galasaapi.NewNullableGalasaSecretType(&supportedType)
            break
        }
    }
    if !nullableSecretType.IsSet() {
        err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_SECRET_TYPE_PROVIDED, galasaapi.AllowedGalasaSecretTypeEnumValues)
    }
    return nullableSecretType, err
}

func validateFlagCombination(
    username string,
    password string,
    token string,
    base64Username string,
    base64Password string,
    base64Token string,
) error {
    var err error

    // Make sure that a field and its base64 equivalent haven't both been provided
    if (username != "" && base64Username != "") ||
        (password != "" && base64Password != "") ||
        (token != "" && base64Token != "") {
        err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_SET_SECRET_INVALID_FLAG_COMBINATION)
    }
    return err
}