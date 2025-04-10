/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"context"
	"fmt"
	"io"
	"log"
	"net/http"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	galasaapi "github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/spi"
)

var (
	CANCEL_STATUS = "finished"
	CANCEL_RESULT = "cancelled"
)

func CancelRun(
	runName string,
	timeService spi.TimeService,
	console spi.Console,
	commsClient api.APICommsClient,
) error {
	var err error
	var runId string

	log.Println("CancelRun entered.")

	if runName == "" {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_MISSING_NAME_FLAG, runName)
	}

	if (err == nil) && (runName != "") {
		err = ValidateRunName(runName)
	}

	if err == nil {

		runId, err = getRunIdFromRunName(runName, timeService, commsClient)

		if err == nil {

			updateRunStatusRequest := createUpdateRunStatusRequest(CANCEL_STATUS, CANCEL_RESULT)

			err = cancelRun(runName, runId, updateRunStatusRequest, commsClient)

			if err == nil {
				consoleErr := console.WriteString(fmt.Sprintf(galasaErrors.GALASA_INFO_RUNS_CANCEL_SUCCESS.Template, runName))

				// Console error is not as important to report as the original error if there was one.
				if consoleErr != nil {
					err = consoleErr
				}
			}

		}

	}

	log.Printf("CancelRun exiting. err is %v\n", err)
	return err
}

func cancelRun(runName string,
	runId string,
	runStatusUpdateRequest *galasaapi.UpdateRunStatusRequest,
	commsClient api.APICommsClient,
) error {
	var err error
	var restApiVersion string
	var responseBody []byte

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	if err == nil {

		err = commsClient.RunAuthenticatedCommandWithRateLimitRetries(func(apiClient *galasaapi.APIClient) error {
			var err error
			var resp *http.Response
			var context context.Context = nil

			_, resp, err = apiClient.ResultArchiveStoreAPIApi.PutRasRunStatusById(context, runId).
				UpdateRunStatusRequest(*runStatusUpdateRequest).
				ClientApiVersion(restApiVersion).Execute()
	
			if resp != nil {
				defer resp.Body.Close()
				statusCode := resp.StatusCode
				if statusCode != http.StatusAccepted {
					responseBody, err = io.ReadAll(resp.Body)
					log.Printf("putRasRunStatusById Failed - HTTP Response - Status Code: '%v' Payload: '%v'\n", statusCode, string(responseBody))
	
					if err == nil {
						var errorFromServer *galasaErrors.GalasaAPIError
						errorFromServer, err = galasaErrors.GetApiErrorFromResponse(statusCode, responseBody)
	
						if err == nil {
							err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_CANCEL_RUN_FAILED, runName, errorFromServer.Message)
						} else {
							err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_CANCEL_RUN_RESPONSE_PARSING)
						}
	
					} else {
						err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_UNABLE_TO_READ_RESPONSE_BODY, err)
					}
				}
			}
			return err
		})
	}
	return err
}
