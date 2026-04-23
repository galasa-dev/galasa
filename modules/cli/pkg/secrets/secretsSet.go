/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package secrets

import (
	"context"
	"encoding/base64"
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
	keystoreValues *SecretsSetKeystoreValues,
	secretType string,
	description string,
	console spi.Console,
	apiClient *galasaapi.APIClient,
	byteReader spi.ByteReader,
	fileSystem spi.FileSystem,
) error {
	var err error

	secretName, err = validateSecretName(secretName)
	if err == nil {
		log.Printf("Secret name validated OK")
		if description != "" {
			description, err = validateDescription(description)
		}

		if err == nil {
			err = validateFlagCombination(username, password, token, base64Username, base64Password, base64Token, keystoreValues)

			if err == nil {
				requestUsername := createSecretRequestUsername(username, base64Username)
				requestToken := createSecretRequestToken(token, base64Token)
				
				var requestKeystore galasaapi.SecretRequestKeystore
				var requestKeystorePassword galasaapi.SecretRequestKeystorePassword
				var requestPassword galasaapi.SecretRequestPassword

				// Handle keystore data - either from file or encoded string
				if keystoreValues.KeystoreFile != "" || keystoreValues.Base64KeystoreEncoded != "" {
					requestKeystore, err = createSecretRequestKeystore(keystoreValues.KeystoreFile, keystoreValues.Base64KeystoreEncoded, fileSystem)
				}
				
				if err == nil {
					if requestKeystore.GetValue() != "" {
						requestKeystorePassword = createSecretRequestKeystorePassword(password, base64Password)
					} else {
						requestPassword = createSecretRequestPassword(password, base64Password)
					}
				}

				var secretTypeValue galasaapi.NullableGalasaSecretType
				if err == nil && secretType != "" {
					secretTypeValue, err = validateSecretType(secretType)
				}

				if err == nil {
					secretRequest := createSecretRequest(secretName, requestUsername, requestPassword, requestToken, requestKeystore, requestKeystorePassword, keystoreValues.KeystoreType, secretTypeValue, description)
					err = sendSetSecretRequest(secretRequest, apiClient, byteReader)
				}
			}
		}
	}
	log.Printf("SecretsSet exiting.")
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

func createSecretRequestKeystore(keystoreFile string, base64KeystoreEncoded string, fileSystem spi.FileSystem) (galasaapi.SecretRequestKeystore, error) {
	var err error
	requestKeystore := *galasaapi.NewSecretRequestKeystore()

	if base64KeystoreEncoded != "" {
		// Already base64 encoded
		requestKeystore.SetValue(base64KeystoreEncoded)
		// while it is base64 encoded, it does not need encoding set
		// requestKeystore.SetEncoding(BASE64_ENCODING) 
	} else if keystoreFile != "" {
		// Read file and base64 encode it
		var fileBytes []byte
		fileBytes, err = fileSystem.ReadBinaryFile(keystoreFile)
		if err == nil {
			if len(fileBytes) == 0 {
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_EMPTY_KEYSTORE_FILE, keystoreFile)
			} else {
				encodedKeystore := base64.StdEncoding.EncodeToString(fileBytes)
				requestKeystore.SetValue(encodedKeystore)
			}
		}
	}
	return requestKeystore, err
}

func createSecretRequestKeystorePassword(keystorePassword string, base64KeystorePassword string) galasaapi.SecretRequestKeystorePassword {
	requestKeystorePassword := *galasaapi.NewSecretRequestKeystorePassword()

	if base64KeystorePassword != "" {
		requestKeystorePassword.SetValue(base64KeystorePassword)
		requestKeystorePassword.SetEncoding(BASE64_ENCODING)
	} else if keystorePassword != "" {
		requestKeystorePassword.SetValue(keystorePassword)
	}
	return requestKeystorePassword
}

func createSecretRequest(
	secretName string,
	username galasaapi.SecretRequestUsername,
	password galasaapi.SecretRequestPassword,
	token galasaapi.SecretRequestToken,
	keystore galasaapi.SecretRequestKeystore,
	keystorePassword galasaapi.SecretRequestKeystorePassword,
	keystoreType string,
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

	if keystore.GetValue() != "" {
		secretRequest.SetKeystore(keystore)
	}

	if keystorePassword.GetValue() != "" {
		secretRequest.SetKeystorePassword(keystorePassword)
	}

	if keystoreType != "" {
		secretRequest.SetKeystoreType(keystoreType)
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
	keystoreValues *SecretsSetKeystoreValues,
) error {
	var err error

	// Make sure that a field and its base64 equivalent haven't both been provided
	if (username != "" && base64Username != "") ||
		(password != "" && base64Password != "") ||
		(token != "" && base64Token != "") {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_SET_SECRET_INVALID_FLAG_COMBINATION)
	}

	// Make sure keystoreFile and base64KeystoreEncoded aren't both provided
	if (keystoreValues.KeystoreFile != "" && keystoreValues.Base64KeystoreEncoded != "") {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_SET_SECRET_INVALID_KEYSTORE_FLAGS)
	}

	if err == nil {

		// Check if keystore is provided
		hasKeystore := keystoreValues.KeystoreFile != "" || keystoreValues.Base64KeystoreEncoded != ""

		if hasKeystore {
			err = keystoreValues.Validate()
		}
	}

	return err
}

