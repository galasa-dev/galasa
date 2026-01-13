/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"context"
	"log"
	"net/http"
	"slices"
	"strings"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/utils"
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

	err = validateRunNameAndTags(runName, addTags, removeTags)
	if err != nil {
		return err
	}

	addTags = removeDuplicateTags(addTags)
	removeTags = removeDuplicateTags(removeTags)

	// Check for tags that are both added and removed.
	for _, tag := range addTags {
		if slices.Contains(removeTags, tag) {
			return galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_UPDATE_RUN_INVALID_TAG_UPDATE, tag)
		}
	}

	runsQuery := NewRunsQuery(
		runName,
		"",    // requestorParameter
		"",    // userParameter
		"",    // resultParameter
		"",    // group
		0,     // fromAgeHours
		0,     // toAgeHours
		false, // shouldGetActive
		false, // isNeedingMethodDetails
		addTags,
		timeService.Now(),
	)

	var runs []galasaapi.Run
	runs, err = GetRunsFromRestApi(runsQuery, commsClient)

	if err == nil {
		if len(runs) == 0 {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_UPDATE_RUN_NOT_FOUND, runName)
		} else {
			err = updateRuns(runs, addTags, removeTags, commsClient, byteReader)
		}
	}

	log.Printf("RunsUpdate exiting. err is %v\n", err)
	return err
}

func updateRuns(
	runs []galasaapi.Run,
	addTags []string,
	removeTags []string,
	commsClient api.APICommsClient,
	byteReader spi.ByteReader,
) error {
	var err error
	var restApiVersion string

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()
	if err == nil {
		for _, run := range runs {
			err = updateRun(run, addTags, removeTags, commsClient, byteReader, restApiVersion)
			if err != nil {
				break
			}
		}
	}

	return err
}

func updateRun(
	run galasaapi.Run,
	addTags []string,
	removeTags []string,
	commsClient api.APICommsClient,
	byteReader spi.ByteReader,
	restApiVersion string,
) error {
	var err error

	runId := run.GetRunId()
	runName := *run.GetTestStructure().RunName

	// Get current tags from the run
	currentTags := run.TestStructure.GetTags()

	// Create a new tag list by filtering out tags to remove
	newTags := make([]string, 0)
	for _, tag := range currentTags {
		if !slices.Contains(removeTags, tag) {
			newTags = append(newTags, tag)
		}
	}

	// Add new tags that aren't already present
	for _, tag := range addTags {
		if !slices.Contains(newTags, tag) {
			newTags = append(newTags, tag)
		}
	}

	// Create update request with new tags
	updateRequest := createUpdateRunTagsRequest(newTags)

	err = commsClient.RunAuthenticatedCommandWithRateLimitRetries(func(apiClient *galasaapi.APIClient) error {
		var err error
		var context context.Context = nil
		var httpResponse *http.Response

		_, httpResponse, err = apiClient.ResultArchiveStoreAPIApi.PutRasRunTagsOrStatusById(context, runId).
			UpdateRunRequest(*updateRequest).
			ClientApiVersion(restApiVersion).Execute()

		if httpResponse != nil {
			defer httpResponse.Body.Close()
		}

		// Non 200-299 http status codes manifest as an error.
		if err != nil {
			if httpResponse == nil {
				// We never got a response, error sending it?
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_UPDATE_RUN_REQUEST_FAILED, runName, err.Error())
			} else {
				err = galasaErrors.HttpResponseToGalasaError(
					httpResponse,
					runName,
					byteReader,
					galasaErrors.GALASA_ERROR_UPDATE_RUN_NO_RESPONSE_CONTENT,
					galasaErrors.GALASA_ERROR_UPDATE_RUN_RESPONSE_PAYLOAD_UNREADABLE,
					galasaErrors.GALASA_ERROR_UPDATE_RUN_UNPARSEABLE_CONTENT,
					galasaErrors.GALASA_ERROR_UPDATE_RUN_SERVER_REPORTED_ERROR,
					galasaErrors.GALASA_ERROR_UPDATE_RUN_EXPLANATION_NOT_JSON,
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

func validateRunNameAndTags(runName string, addTags []string, removeTags []string) error {
	var err error

	if runName == "" {
		return galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_MISSING_NAME_FLAG, "--name")
	}

	if len(addTags) == 0 && len(removeTags) == 0 {
		return galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_UPDATE_RUN_MISSING_FIELD, "--add-tags or --remove-tags")
	}

	// Validate the runName as best we can without contacting the ecosystem.
	err = ValidateRunName(runName)
	if err != nil {
		return err
	}

	// Validate all tags in addTags
	for _, tag := range addTags {
		err = validateTagName(tag)
		if err != nil {
			return err
		}
	}

	// Validate all tags in removeTags
	for _, tag := range removeTags {
		err = validateTagName(tag)
		if err != nil {
			return err
		}
	}

	return nil
}

func validateTagName(tagName string) error {
	trimmedTag := strings.TrimSpace(tagName)

	if trimmedTag == "" || !utils.IsLatin1(tagName) {
		return galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_UPDATE_RUN_INVALID_TAG_NAME, tagName)
	}

	return nil
}

func removeDuplicateTags(tags []string) []string {
	uniqueTags := make(map[string]struct{}, len(tags))
	for _, tag := range tags {
		uniqueTags[tag] = struct{}{}
	}
	result := make([]string, 0, len(uniqueTags))
	for tag := range uniqueTags {
		result = append(tags, tag)
	}
	return result
}

func createUpdateRunTagsRequest(tags []string) *galasaapi.UpdateRunRequest {
	var UpdateRunRequest = galasaapi.NewUpdateRunRequest()

	UpdateRunRequest.SetTags(tags)

	return UpdateRunRequest
}
