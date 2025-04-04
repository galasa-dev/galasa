/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package runs

import (
	"log"
	"time"

	"github.com/galasa-dev/cli/pkg/api"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/spi"
)

func getRunIdFromRunName(runName string,
	timeService spi.TimeService,
	commsClient api.APICommsClient,
) (string, error) {
	var err error
	var runs []galasaapi.Run
	var runId string

	requestorParameter := ""
	resultParameter := ""
	group := ""
	fromAgeHours := 0
	toAgeHours := 0
	shouldGetActive := true

	runs, err = GetRunsFromRestApi(runName, requestorParameter, resultParameter, fromAgeHours, toAgeHours, shouldGetActive, timeService, commsClient, group)

	if err == nil {

		if len(runs) > 1 {

			// More than 1 active run has been found with this runName, as multiple runs might be stuck in active state like ending
			// So find the run with the first startTime, and attempt to reset that one

			firstRun := runs[0]
			for _, run := range runs {

				firstRunStart := firstRun.TestStructure.GetStartTime()
				thisRunStart := run.TestStructure.GetStartTime()

				firstRunStartTime, _ := time.Parse(time.RFC3339, firstRunStart)
				thisRunStartTime, _ := time.Parse(time.RFC3339, thisRunStart)

				if thisRunStartTime.Before(firstRunStartTime) {
					firstRun = run
				}

			}

			runId = firstRun.GetRunId()

		} else if len(runs) == 1 {

			runId = runs[0].GetRunId()

		} else {

			log.Printf("No active runs found matching run name: '%s'", runName)
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_NO_ACTIVE_RUNS_WITH_RUNNAME, runName)

		}
	}

	return runId, err
}

func createUpdateRunStatusRequest(status string, result string) *galasaapi.UpdateRunStatusRequest {
	var updateRunStatusRequest = galasaapi.NewUpdateRunStatusRequest()

	updateRunStatusRequest.SetStatus(status)
	updateRunStatusRequest.SetResult(result)

	return updateRunStatusRequest
}
