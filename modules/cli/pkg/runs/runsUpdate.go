/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"context"
	"fmt"
	"log"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/spi"
)

// ---------------------------------------------------

// RunsUpdate - performs all the logic to implement the `galasactl runs update` command,
// but in a unit-testable manner.
func RunsUpdate(
	runName string,
	addTags []string,
	removeTags []string,
	console spi.Console,
	commsClient api.APICommsClient,
	timeService spi.TimeService,
	byteReader spi.ByteReader,
) error {
	var err error

	log.Printf("RunsUpdate entered.")

	if runName == "" {
		return galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_MISSING_REQUIRED_PARAMETER, "name")
	}

	if len(addTags) == 0 || len(removeTags) == 0 {
		if len(addTags) == 0 {
			return galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_MISSING_REQUIRED_PARAMETER, "add-tags")
		}
		
		if len(removeTags) == 0 {
			return galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_MISSING_REQUIRED_PARAMETER, "remove-tags")
		}
	}


	// Validate the runName as best we can without contacting the ecosystem.
	err = ValidateRunName(runName)
	if err != nil {
		return err
	}

	// Remove duplicates from addTags
	uniqueAddTags := make(map[string]struct{}, len(addTags))
	for _, tag := range addTags {
		uniqueAddTags[tag] = struct{}{}
	}
	addTags = make([]string, 0, len(uniqueAddTags))
	for tag := range uniqueAddTags {
		addTags = append(addTags, tag)
	}

	// Remove duplicates from removeTags
	uniqueRemoveTags := make(map[string]struct{}, len(removeTags))
	for _, tag := range removeTags {
		uniqueRemoveTags[tag] = struct{}{}
	}
	removeTags = make([]string, 0, len(uniqueRemoveTags))
	for tag := range uniqueRemoveTags {
		removeTags = append(removeTags, tag)
	}

	// Check for tags that are both added and removed.
	for _, tag := range addTags {
		if contains(removeTags, tag) {
			console.WriteString(fmt.Sprintf("Error: Tag '%s' was both added and removed. This may be an unintentional error.\n", tag))
			return galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_TAG_ADDED_AND_REMOVED, tag)
		}
	}

	requestorParameter := ""
	userParameter := ""
	resultParameter := ""
	fromAgeHours := 0
	toAgeHours := 0
	group := ""
	shouldGetActive := false
	isNeedingMethodDetails := false

	runsQuery := NewRunsQuery(
		runName,
		requestorParameter,
		userParameter,
		resultParameter,
		group,
		fromAgeHours,
		toAgeHours,
		shouldGetActive,
		isNeedingMethodDetails,
		addTags,
		timeService.Now(),
	)

	var runs []galasaapi.Run
	runs, err = GetRunsFromRestApi(runsQuery, commsClient)

	if err == nil {

		if len(runs) == 0 {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_SERVER_UPDATE_RUN_NOT_FOUND, runName)
		} else {
			err = updateRuns(runs, commsClient, byteReader)
		}
	}

	if err != nil {
		console.WriteString(err.Error())
	}

	log.Printf("RunsUpdate exiting. err is %v\n", err)
	return err
}

func updateRuns(
	runs []galasaapi.Run,
	commsClient api.APICommsClient,
	byteReader spi.ByteReader,
) error {
	var err error
	var restApiVersion string

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()
	if err == nil {
		for _, run := range runs {
			err = updateRun(run, commsClient, byteReader, restApiVersion)
			if err != nil {
				break
			}
		}
	}

	return err
}

func updateRun(
	run galasaapi.Run,
	commsClient api.APICommsClient,
	byteReader spi.ByteReader,
	restApiVersion string,
) error {
	var err error

	runId := run.GetRunId()
	runName := *run.GetTestStructure().RunName

	// Remove existing tags that are in removeTags
	for _, tag := range removeTags {
		if contains(run.GetTags(), tag) {
			run.GetTags().Remove(tag)
		}
	}

	// Add new tags from addTags
	for _, tag := range addTags {
		if !contains(run.GetTags(), tag) {
			run.GetTags().Add(tag)
		}
	}

	err = commsClient.RunAuthenticatedCommandWithRateLimitRetries(func(apiClient *galasaapi.APIClient) error {
		var err error
		var context context.Context = nil
		var httpResponse *http.Response

		apicall := apiClient.ResultArchiveStoreAPIApi.UpdateRasRunById(context, runId, run).ClientApiVersion(restApiVersion)
		httpResponse, err = apicall.Execute()

		if httpResponse != nil {
			defer httpResponse.Body.Close()
		}

		// 200-299 http status codes manifest in an error.
		if err != nil {
			if httpResponse == nil {
				// We never got a response, error sending it or something ?
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_SERVER_UPDATE_RUNS_FAILED, err.Error())
			} else {
				err = galasaErrors.HttpResponseToGalasaError(
					httpResponse,
					runName,
					byteReader,
					galasaErrors.GALASA_ERROR_UPDATE_RUNS_NO_RESPONSE_CONTENT,
					galasaErrors.GALASA_ERROR_UPDATE_RUNS_RESPONSE_PAYLOAD_UNREADABLE,
					galasaErrors.GALASA_ERROR_UPDATE_RUNS_UNPARSEABLE_CONTENT,
					galasaErrors.GALASA_ERROR_UPDATE_RUNS_SERVER_REPORTED_ERROR,
					galasaErrors.GALASA_ERROR_UPDATE_RUNS_EXPLANATION_NOT_JSON,
				)
			}
		}

		if err == nil {
			log.Printf("Run with runId '%s' and runName '%s', was updated OK.\n", runId, run.TestStructure.GetRunName())
		}
		return err
	})
	return err
}

func contains(slice []string, item string) bool {
	for _, a := range slice {
		if a == item {
			return true
		}
	}
	return false
}


func createUpdateRunTagsRequest(tags []string) *galasaapi.UpdateRunRequest {
	var UpdateRunRequest = galasaapi.NewUpdateRunRequest()

	UpdateRunRequest.SetTags(tags)

	return UpdateRunRequest
}
