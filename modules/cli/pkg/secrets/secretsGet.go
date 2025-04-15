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
	"sort"
	"strings"

	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/secretsformatter"
	"github.com/galasa-dev/cli/pkg/spi"
)

var (
	formatters = createFormatters()
)

func GetSecrets(
	secretName string,
	format string,
	console spi.Console,
	apiClient *galasaapi.APIClient,
	byteReader spi.ByteReader,
) error {
	var err error
	var chosenFormatter secretsformatter.SecretsFormatter
	secrets := make([]galasaapi.GalasaSecret, 0)

	chosenFormatter, err = validateFormatFlag(format)
	if err == nil {
		if secretName != "" {
			// The user has provided a secret name, so try to get that secret
			var secret *galasaapi.GalasaSecret
			secret, err = getSecretByName(secretName, apiClient, byteReader)
			if err == nil {
				secrets = append(secrets, *secret)
			}
		} else {
			// Get all secrets
			secrets, err = getSecretsFromRestApi(apiClient, byteReader)
		}

		// If we were able to get the secrets, format them as requested by the user
		if err == nil {
			var formattedOutput string
			formattedOutput, err = chosenFormatter.FormatSecrets(secrets)
			if err == nil {
				console.WriteString(formattedOutput)
			}
		}
	}
	log.Printf("GetSecrets exiting. err is %v\n", err)
	return err
}

func getSecretByName(
	secretName string,
	apiClient *galasaapi.APIClient,
	byteReader spi.ByteReader,
) (*galasaapi.GalasaSecret, error) {
	var err error
	var secret *galasaapi.GalasaSecret
	secretName, err = validateSecretName(secretName)
	if err == nil {
		secret, err = getSecretFromRestApi(secretName, apiClient, byteReader)
	}

	return secret, err
}

func getSecretFromRestApi(
	secretName string,
	apiClient *galasaapi.APIClient,
	byteReader spi.ByteReader,
) (*galasaapi.GalasaSecret, error) {
	var err error
	var httpResponse *http.Response
	var context context.Context = context.Background()
	var restApiVersion string
	var secret *galasaapi.GalasaSecret

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	if err == nil {
		secret, httpResponse, err = apiClient.SecretsAPIApi.GetSecret(context, secretName).
			ClientApiVersion(restApiVersion).
			Execute()

		if httpResponse != nil {
			defer httpResponse.Body.Close()
		}

		if err != nil {
			if httpResponse == nil {
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_GET_SECRET_REQUEST_FAILED, err.Error())
			} else {
				err = galasaErrors.HttpResponseToGalasaError(
					httpResponse,
					secretName,
					byteReader,
					galasaErrors.GALASA_ERROR_GET_SECRET_NO_RESPONSE_CONTENT,
					galasaErrors.GALASA_ERROR_GET_SECRET_RESPONSE_BODY_UNREADABLE,
					galasaErrors.GALASA_ERROR_GET_SECRET_UNPARSEABLE_CONTENT,
					galasaErrors.GALASA_ERROR_GET_SECRET_SERVER_REPORTED_ERROR,
					galasaErrors.GALASA_ERROR_GET_SECRET_EXPLANATION_NOT_JSON,
				)
			}
		}
	}
	return secret, err
}

func getSecretsFromRestApi(
	apiClient *galasaapi.APIClient,
	byteReader spi.ByteReader,
) ([]galasaapi.GalasaSecret, error) {
	var err error
	var httpResponse *http.Response
	var context context.Context = context.Background()
	var restApiVersion string
	var secrets []galasaapi.GalasaSecret

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	if err == nil {
		secrets, httpResponse, err = apiClient.SecretsAPIApi.GetSecrets(context).
			ClientApiVersion(restApiVersion).
			Execute()

		if httpResponse != nil {
			defer httpResponse.Body.Close()
		}

		if err != nil {
			if httpResponse == nil {
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_GET_SECRETS_REQUEST_FAILED, err.Error())
			} else {
				err = galasaErrors.HttpResponseToGalasaError(
					httpResponse,
					"",
					byteReader,
					galasaErrors.GALASA_ERROR_GET_SECRETS_NO_RESPONSE_CONTENT,
					galasaErrors.GALASA_ERROR_GET_SECRETS_RESPONSE_BODY_UNREADABLE,
					galasaErrors.GALASA_ERROR_GET_SECRETS_UNPARSEABLE_CONTENT,
					galasaErrors.GALASA_ERROR_GET_SECRETS_SERVER_REPORTED_ERROR,
					galasaErrors.GALASA_ERROR_GET_SECRETS_EXPLANATION_NOT_JSON,
				)
			}
		}
	}
	return secrets, err
}

func createFormatters() map[string]secretsformatter.SecretsFormatter {
	formatters := make(map[string]secretsformatter.SecretsFormatter, 0)
	summaryFormatter := secretsformatter.NewSecretSummaryFormatter()
	yamlFormatter := secretsformatter.NewSecretYamlFormatter()

	formatters[summaryFormatter.GetName()] = summaryFormatter
	formatters[yamlFormatter.GetName()] = yamlFormatter

	return formatters
}

func GetFormatterNamesAsString() string {
	names := make([]string, 0, len(formatters))
	for name := range formatters {
		names = append(names, name)
	}
	sort.Strings(names)
	formatterNames := strings.Builder{}

	for index, formatterName := range names {

		if index != 0 {
			formatterNames.WriteString(", ")
		}
		formatterNames.WriteString("'" + formatterName + "'")
	}

	return formatterNames.String()
}

func validateFormatFlag(outputFormatString string) (secretsformatter.SecretsFormatter, error) {
	var err error

	chosenFormatter, isPresent := formatters[outputFormatString]

	if !isPresent {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_OUTPUT_FORMAT, outputFormatString, GetFormatterNamesAsString())
	}

	return chosenFormatter, err
}
