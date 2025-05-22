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
	group string,
) error {
	var err error

	log.Println("CancelRun entered.")

	if runName != "" {

		err = ValidateRunName(runName)
		if err == nil {
			err = cancelByRunName(runName, timeService, commsClient, console)
		}

	}

	if group != "" {

		group, err = validateGroupName(group)
		if err == nil {
			err = cancelRunsByGroupName(group, commsClient, console)
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

func cancelRunsByGroup(groupName string,
	groupStatusUpdateRequest *galasaapi.UpdateGroupStatusRequest,
	commsClient api.APICommsClient,
) (int, error) {

	var err error
	var restApiVersion string
	var responseBody []byte
	var statusCode int

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	if err == nil {

		err = commsClient.RunAuthenticatedCommandWithRateLimitRetries(func(apiClient *galasaapi.APIClient) error {

			var err error
			var resp *http.Response
			var context context.Context = nil

			_, resp, err = apiClient.RunsAPIApi.PutRunStatusByGroupId(context, groupName).
				UpdateGroupStatusRequest(*groupStatusUpdateRequest).
				ClientApiVersion(restApiVersion).Execute()

			if resp != nil {
				defer resp.Body.Close()
				statusCode = resp.StatusCode

				if statusCode == http.StatusOK {
					log.Printf("putRunStatusByGroupId OK - HTTP Response - Status Code: '%v' Payload: '%v'\n", statusCode, string(responseBody))
				} else {
					if statusCode != http.StatusAccepted {

						responseBody, err = io.ReadAll(resp.Body)
						log.Printf("putRunStatusByGroupId Failed - HTTP Response - Status Code: '%v' Payload: '%v'\n", statusCode, string(responseBody))

						if err == nil {
							var errorFromServer *galasaErrors.GalasaAPIError
							errorFromServer, err = galasaErrors.GetApiErrorFromResponse(statusCode, responseBody)

							if err == nil {
								err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_CANCEL_GROUPED_RUNS_FAILED, groupName, errorFromServer.Message)
							} else {
								err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_CANCEL_RUN_RESPONSE_PARSING)
							}

						} else {
							err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_UNABLE_TO_READ_RESPONSE_BODY, err)
						}

					}
				}
			}
			return err
		})
	}

	return statusCode, err
}

func cancelByRunName(runName string, timeService spi.TimeService, commsClient api.APICommsClient, console spi.Console) error {

	var err error
	var runId string

	runId, err = getRunIdFromRunName(runName, timeService, commsClient)

	if err == nil {

		updateRunStatusRequest := createUpdateRunStatusRequest(CANCEL_STATUS, CANCEL_RESULT)
		err = cancelRun(runName, runId, updateRunStatusRequest, commsClient)

		if err == nil {
			err = writeConsoleMessage(console, *galasaErrors.GALASA_INFO_RUNS_CANCEL_SUCCESS, runName)
		}

	}

	return err

}

func cancelRunsByGroupName(groupName string, commsClient api.APICommsClient, console spi.Console) error {

	var err error
	var statusCode int

	groupStatusUpdateRequest := createGroupUpdateStatusRequest()
	statusCode, err = cancelRunsByGroup(groupName, groupStatusUpdateRequest, commsClient)

	if err == nil {
		if statusCode == http.StatusAccepted {
			err = writeConsoleMessage(console, *galasaErrors.GALASA_INFO_GROUP_RUNS_CANCEL_SUCCESS, groupName)
		} else if statusCode == http.StatusOK {
			err = writeConsoleMessage(console, *galasaErrors.GALASA_INFO_GROUP_RUNS_ALREADY_FINISHED, groupName)
		}
	}

	return err
}

func writeConsoleMessage(console spi.Console, errorMessage galasaErrors.MessageType, groupName string) error {
	return console.WriteString(fmt.Sprintf(errorMessage.Template, groupName))
}
