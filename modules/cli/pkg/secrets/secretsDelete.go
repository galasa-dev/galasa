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

	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/spi"
)

func DeleteSecret(
    secretName string,
    console spi.Console,
    apiClient *galasaapi.APIClient,
    byteReader spi.ByteReader,
) error {
    var err error

    secretName, err = validateSecretName(secretName)
    if err == nil {
        log.Printf("Secret name validated OK")
        err = sendDeleteSecretRequest(secretName, apiClient, byteReader)
    }
    log.Printf("SecretsDelete exiting. err is %v\n", err)
    return err
}

func sendDeleteSecretRequest(
    secretName string,
    apiClient *galasaapi.APIClient,
    byteReader spi.ByteReader,
) error {
    var err error
    var httpResponse *http.Response
    var context context.Context = context.Background()
    var restApiVersion string

    restApiVersion, err = embedded.GetGalasactlRestApiVersion()

    if err == nil {
        httpResponse, err = apiClient.SecretsAPIApi.DeleteSecret(context, secretName).
            ClientApiVersion(restApiVersion).
            Execute()

        if httpResponse != nil {
            defer httpResponse.Body.Close()
        }

        if err != nil {
            if httpResponse == nil {
                // We never got a response, error sending it or something?
                err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_DELETE_SECRET_REQUEST_FAILED, err.Error())
            } else {
                err = galasaErrors.HttpResponseToGalasaError(
                    httpResponse,
                    secretName,
                    byteReader,
                    galasaErrors.GALASA_ERROR_DELETE_SECRET_NO_RESPONSE_CONTENT,
                    galasaErrors.GALASA_ERROR_DELETE_SECRET_RESPONSE_BODY_UNREADABLE,
                    galasaErrors.GALASA_ERROR_DELETE_SECRET_UNPARSEABLE_CONTENT,
                    galasaErrors.GALASA_ERROR_DELETE_SECRET_SERVER_REPORTED_ERROR,
                    galasaErrors.GALASA_ERROR_DELETE_SECRET_EXPLANATION_NOT_JSON,
                )
            }
        }
    }
    return err
}
