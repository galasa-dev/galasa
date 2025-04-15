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
	"regexp"
	"sort"
	"strconv"
	"strings"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/runsformatter"
	"github.com/galasa-dev/cli/pkg/spi"
)

var (
	validFormatters = CreateFormatters()

	// When parsing the '--age' parameter value....
	// (^[\\D]*) - matches any leading garbage, which is non-digits. Should be empty.
	// ([0-9]+) - matches any digit sequence. Should be an integer.
	// (.*) - matches any time unit. Should be a valid time unit from our map above.
	agePartRegex *regexp.Regexp = regexp.MustCompile(`(^[\D]*)([0-9]+)(.*)`)

	// Comma separated string of valid status values
	// specifically, status values of an 'active' test
	activeStatusNames = "started,ending,generating,building,provstart,running,rundone,up"
)

// ---------------------------------------------------

// GetRuns - performs all the logic to implement the `galasactl runs get` command,
// but in a unit-testable manner.
func GetRuns(
	runName string,
	age string,
	requestorParameter string,
	resultParameter string,
	shouldGetActive bool,
	outputFormatString string,
	group string,
	timeService spi.TimeService,
	console spi.Console,
	commsClient api.APICommsClient,
) error {
	var err error
	var fromAge int
	var toAge int

	log.Printf("GetRuns entered.")

	if runName == "" && age == "" && group == "" {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_NO_TEST_RUN_IDENTIFIER_FLAG_SPECIFIED)
	}

	if err == nil && runName != "" {
		// Validate the runName as best we can without contacting the ecosystem.
		err = ValidateRunName(runName)
	}

	if err == nil && age != "" {
		fromAge, toAge, err = getTimesFromAge(age)
	}

	if err == nil && group != "" {
		group, err = validateGroupname(group)
	}

	if err == nil && resultParameter != "" {
		if shouldGetActive {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_ACTIVE_AND_RESULT_ARE_MUTUALLY_EXCLUSIVE)
		}
		if err == nil {
			resultParameter, err = ValidateResultParameter(resultParameter, commsClient)
		}
	}

	if err == nil {
		var chosenFormatter runsformatter.RunsFormatter
		chosenFormatter, err = validateOutputFormatFlagValue(outputFormatString, validFormatters)
		if err == nil {
			var runJson []galasaapi.Run
			runJson, err = GetRunsFromRestApi(runName, requestorParameter, resultParameter, fromAge, toAge, shouldGetActive, timeService, commsClient, group)
			if err == nil {
				// Some formatters need extra fields filled-in so they can be displayed.
				if chosenFormatter.IsNeedingMethodDetails() {
					log.Println("This type of formatter needs extra detail about each run to display")
					runJson, err = GetRunDetailsFromRasSearchRuns(runJson, commsClient)
				}

				if err == nil {
					var outputText string

					log.Printf("There are %v results to display in total.\n", len(runJson))

					//convert galsaapi.Runs tests into formattable data
					apiServerUrl := commsClient.GetBootstrapData().ApiServerURL
					formattableTest := FormattableTestFromGalasaApi(runJson, apiServerUrl)
					outputText, err = chosenFormatter.FormatRuns(formattableTest)

					if err == nil {
						err = writeOutput(outputText, console)
					}
				}
			}
		}
	}
	log.Printf("GetRuns exiting. err is %v", err)
	return err
}

func CreateFormatters() map[string]runsformatter.RunsFormatter {
	validFormatters := make(map[string]runsformatter.RunsFormatter, 0)
	summaryFormatter := runsformatter.NewSummaryFormatter()
	validFormatters[summaryFormatter.GetName()] = summaryFormatter

	detailedFormatter := runsformatter.NewDetailsFormatter()
	validFormatters[detailedFormatter.GetName()] = detailedFormatter

	rawFormatter := runsformatter.NewRawFormatter()
	validFormatters[rawFormatter.GetName()] = rawFormatter

	return validFormatters
}

func writeOutput(outputText string, console spi.Console) error {
	err := console.WriteString(outputText)
	return err
}

// GetFormatterNamesString builds a string of comma separated, quoted formatter names
func GetFormatterNamesString(validFormatters map[string]runsformatter.RunsFormatter) string {
	// extract names into a sorted slice
	names := make([]string, 0, len(validFormatters))
	for name := range validFormatters {
		names = append(names, name)
	}
	sort.Strings(names)

	// render list of sorted names into string
	formatterNames := strings.Builder{}

	for count, formatterName := range names {

		if count != 0 {
			formatterNames.WriteString(", ")
		}
		formatterNames.WriteString("'" + formatterName + "'")
	}

	return formatterNames.String()
}

// Ensures the user has provided a valid output format as part of the "runs get" command.
func validateOutputFormatFlagValue(outputFormatString string, validFormatters map[string]runsformatter.RunsFormatter) (runsformatter.RunsFormatter, error) {
	var err error

	chosenFormatter, isPresent := validFormatters[outputFormatString]

	if !isPresent {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_OUTPUT_FORMAT, outputFormatString, GetFormatterNamesString(validFormatters))
	}

	return chosenFormatter, err
}

func GetRunDetailsFromRasSearchRuns(runs []galasaapi.Run, commsClient api.APICommsClient) ([]galasaapi.Run, error) {
	var err error
	var runsDetails []galasaapi.Run = make([]galasaapi.Run, 0)
	var details *galasaapi.Run

	var restApiVersion string
	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	if err == nil {
		for _, run := range runs {
			details, err = getRunByRunIdFromRestApi(run.GetRunId(), commsClient, restApiVersion)
			if err == nil && details != nil {
				runsDetails = append(runsDetails, *details)
			}
		}
	}

	return runsDetails, err
}

func getRunByRunIdFromRestApi(
	runId string,
	commsClient api.APICommsClient,
	restApiVersion string,
) (*galasaapi.Run, error) {
	var err error
	var details *galasaapi.Run

	err = commsClient.RunAuthenticatedCommandWithRateLimitRetries(func(apiClient *galasaapi.APIClient) error {
		var err error
		var httpResponse *http.Response
		var context context.Context = nil
	
		log.Printf("Getting details for run %v\n", runId)
		details, httpResponse, err = apiClient.ResultArchiveStoreAPIApi.GetRasRunById(context, runId).ClientApiVersion(restApiVersion).Execute()
	
		var statusCode int
		if httpResponse != nil {
			defer httpResponse.Body.Close()
			statusCode = httpResponse.StatusCode
		}
	
		if err != nil {
			err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_QUERY_RUNS_FAILED, err.Error())
		} else {
			if statusCode != http.StatusOK {
				err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_QUERY_RUNS_NON_OK_STATUS, strconv.Itoa(httpResponse.StatusCode))
			}
		}
		return err
	})
	return details, err
}

// Retrieves test runs from the ecosystem API that match a given runName.
// Multiple test runs can be returned as the runName is not unique.
func GetRunsFromRestApi(
	runName string,
	requestorParameter string,
	resultParameter string,
	fromAgeMins int,
	toAgeMins int,
	shouldGetActive bool,
	timeService spi.TimeService,
	commsClient api.APICommsClient,
	group string,
) ([]galasaapi.Run, error) {

	var err error
	var results []galasaapi.Run = make([]galasaapi.Run, 0)

	var pageNumberWanted int32 = 1
	gotAllResults := false
	var restApiVersion string

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()
	if err == nil {

		runsQuery := NewRunsQuery(
			runName,
			requestorParameter,
			resultParameter,
			group,
			fromAgeMins,
			toAgeMins,
			shouldGetActive,
			timeService.Now(),
		)

		for !gotAllResults && err == nil {

			log.Printf("Requesting page '%d' ", pageNumberWanted)

			var runData *galasaapi.RunResults
			runData, err = runsQuery.GetRunsPageFromRestApi(commsClient, restApiVersion)

			if err == nil {
				// Add all the runs into our set of results.
				// Note: The ... syntax means 'all of the array', so they all get appended at once.
				runsOnThisPage := runData.GetRuns()
				results = append(results, runsOnThisPage...)

				log.Printf("total runs: %v", len(results))

				// Have we processed the last page ?
				if !runData.HasNextCursor() || len(runsOnThisPage) < int(runData.GetPageSize()) {
					gotAllResults = true
				} else {
					runsQuery.SetPageCursor(runData.GetNextCursor())
					pageNumberWanted++
				}
			}
		}
	}

	log.Printf("total runs returned: %v", len(results))

	return results, err
}

func getTimesFromAge(age string) (int, int, error) {
	// Validate the age parameter
	// Validate that the time unit is either 'w', 'd', 'h'
	// Validate that if both FROM and TO are specified, FROM is older than TO

	var err error
	var fromAge int
	var toAge int = 0

	ageParts := strings.Split(age, ":")

	if len(ageParts) > 2 {
		// Too many colons.
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_AGE_PARAMETER, age, GetTimeUnitsForErrorMessage())
	} else {
		// No colons !... only 'from' time specified.
		fromPart := ageParts[0]
		if !agePartRegex.MatchString(fromPart) {
			// Invalid from part.
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_FROM_AGE_SPECIFIED, age, GetTimeUnitsForErrorMessage())
		} else {
			fromAge, err = getMinutesFromAgePart(fromPart, age)

			if fromAge == 0 {
				// 'from' can't be 0 hours.
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_AGE_PARAMETER, age, GetTimeUnitsForErrorMessage())
			}
		}

		if (err == nil) && (len(ageParts) > 1) {
			// One colon, indicates there is a 'to' part.
			toPart := ageParts[1]
			toAge, err = getMinutesFromAgePart(toPart, age)
			if err == nil {
				// From value must be bigger than to value
				if toAge > 0 && fromAge <= toAge {
					err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_FROM_AGE_SMALLER_THAN_TO_AGE, age)
				}
			}
		}
	}

	return fromAge, toAge, err
}

// getHoursFromAgePart - Input value is '15d' or '14h' for example.
func getMinutesFromAgePart(agePart string, errorMessageValue string) (int, error) {
	var err error
	var minutes int = 0
	var duration int

	// Separate the integer part from time unit part.
	// Expecting something like 14d which will then break down into further matches: Index 0 is 14d, index 1 is 14, index 2 is d
	durationPart := agePartRegex.FindStringSubmatch(agePart)
	leadingGarbage := durationPart[1]
	durationNumber := durationPart[2]
	durationUnitStr := durationPart[3]

	if leadingGarbage != "" {
		// Some leading garbage prior to the 'FROM' field. It must be empty.
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_AGE_PARAMETER, errorMessageValue, GetTimeUnitsForErrorMessage())
	} else {

		if len(durationPart) == 0 {
			// Invalid from. It must be some time in the past.
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_FROM_AGE_SPECIFIED, errorMessageValue, GetTimeUnitsForErrorMessage())
		} else {
			// we can extract the integer part now

			duration, err = getValueAsInt(durationNumber)
			if err == nil {
				if duration < 0 {
					// Number part of the duration can't be negative.
					err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_NEGATIVE_AGE_SPECIFIED, errorMessageValue, GetTimeUnitsForErrorMessage())
				} else {

					timeUnit, isRecognisedTimeUnit := GetTimeUnitFromShortName(durationUnitStr)
					if !isRecognisedTimeUnit {
						// Bad time unit.
						err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_BAD_TIME_UNIT_AGE_SPECIFIED, errorMessageValue, GetTimeUnitsForErrorMessage())
					} else {
						minutes = duration * timeUnit.GetMinuteMultiplier()
					}
				}
			}
		}
	}
	return minutes, err
}

func getValueAsInt(value string) (int, error) {
	var age int
	var err error
	if age, err = strconv.Atoi(value); err != nil {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_FROM_OR_TO_PARAMETER, value)
	}
	return age, err
}
