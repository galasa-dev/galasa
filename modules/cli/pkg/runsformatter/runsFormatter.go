/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runsformatter

import (
	"fmt"
	"strconv"
	"strings"
	"time"

	"github.com/galasa-dev/cli/pkg/galasaapi"
)

// -----------------------------------------------------
// RunsFormatter - implementations can take a collection of run results
// and turn them into a string for display to the user.
const (
	DATE_FORMAT = "2006-01-02 15:04:05"

	RUN_RESULT_TOTAL               = "Total"
	RUN_RESULT_PASSED              = "Passed"
	RUN_RESULT_PASSED_WITH_DEFECTS = "Passed With Defects"
	RUN_RESULT_FAILED              = "Failed"
	RUN_RESULT_FAILED_WITH_DEFECTS = "Failed With Defects"
	RUN_RESULT_LOST                = "Lost"
	RUN_RESULT_ENVFAIL             = "EnvFail"
	RUN_RESULT_UNKNOWN             = "UNKNOWN"
	RUN_RESULT_ACTIVE              = "Active"
	RUN_RESULT_IGNORED             = "Ignored"

	HEADER_RUNNAME        = "name"
	HEADER_STATUS         = "status"
	HEADER_RESULT         = "result"
	HEADER_TEST_NAME      = "test-name"
	HEADER_SUBMITTED_TIME = "submitted-time(UTC)"
	HEADER_START_TIME     = "start-time(UTC)"
	HEADER_END_TIME       = "end-time(UTC)"
	HEADER_DURATION       = "duration(ms)"
	HEADER_BUNDLE         = "bundle"
	HEADER_REQUESTOR      = "requestor"
	HEADER_RUN_LOG        = "run-log"
	HEADER_METHOD_NAME    = "method"
	HEADER_METHOD_TYPE    = "type"
	HEADER_GROUP          = "group"

	RAS_RUNS_URL = "/ras/runs/"
)

// -----------------------------------------------------
// Structure to store test data from external structures to be used in by FomatRuns()
type FormattableTest struct {
	RunId         string
	Name          string
	TestName      string
	Status        string
	Result        string
	StartTimeUTC  string
	EndTimeUTC    string
	QueuedTimeUTC string
	Requestor     string
	Bundle        string
	ApiServerUrl  string
	Group         string
	Methods       []galasaapi.TestMethod
	Lost          bool
}

func NewFormattableTest() FormattableTest {
	this := FormattableTest{}
	return this
}

var RESULT_LABELS = []string{RUN_RESULT_PASSED, RUN_RESULT_PASSED_WITH_DEFECTS, RUN_RESULT_FAILED, RUN_RESULT_FAILED_WITH_DEFECTS, RUN_RESULT_LOST, RUN_RESULT_ENVFAIL, RUN_RESULT_UNKNOWN, RUN_RESULT_ACTIVE, RUN_RESULT_IGNORED}

type RunsFormatter interface {
	FormatRuns(testResultsData []FormattableTest) (string, error)
	GetName() string

	// IsNeedingDetails - Does this formatter require all of the detailed fields to be filled-in,
	// so they can be displayed ? True if so, false otherwise.
	// The caller may need to make sure such things are gathered before calling, and some
	// formatters may not need all the detail.
	IsNeedingMethodDetails() bool
}

// -----------------------------------------------------
// Functions for time formats and duration
func formatTimeReadable(rawTime string) string {
	var formattedTimeString string
	if len(rawTime) < 19 {
		formattedTimeString = ""
	} else {
		formattedTimeString = rawTime[0:10] + " " + rawTime[11:19]
	}
	return formattedTimeString
}

func formatTimeForDurationCalculation(rawTime string) time.Time {
	parsedTime, err := time.Parse(time.RFC3339, rawTime)
	if err != nil {
		fmt.Println(err)
	}
	return parsedTime
}

func calculateDurationMilliseconds(start time.Time, end time.Time) string {
	duration := strconv.FormatInt(end.Sub(start).Milliseconds(), 10)

	return duration
}

func getDuration(startTimeStringRaw string, endTimeStringRaw string) string {
	var duration string = ""

	var startTimeStringForDuration time.Time
	var endTimeStringForDuration time.Time

	if len(startTimeStringRaw) > 0 {
		startTimeStringForDuration = formatTimeForDurationCalculation(startTimeStringRaw)
		if len(endTimeStringRaw) > 0 {
			endTimeStringForDuration = formatTimeForDurationCalculation(endTimeStringRaw)
			duration = calculateDurationMilliseconds(startTimeStringForDuration, endTimeStringForDuration)
		}
	}
	return duration
}

func getReadableTime(timeStringRaw string) string {
	var timeStringReadable string = ""
	if len(timeStringRaw) > 0 {
		timeStringReadable = formatTimeReadable(timeStringRaw)
	}
	return timeStringReadable
}

// -----------------------------------------------------
// Functions for result report
func generateResultTotalsReport(totalResults int, resultsCount map[string]int) string {
	var resultString string = RUN_RESULT_TOTAL + ":" + strconv.Itoa(totalResults)
	for _, label := range RESULT_LABELS {
		labelResult := resultsCount[label]
		if labelResult > 0 {

			resultString += " "

			resultLabelNoSpaces := strings.ReplaceAll(label, " ", "")
			resultString += resultLabelNoSpaces + ":" + strconv.Itoa(labelResult)
		}
	}

	return resultString
}

func accumulateResults(resultCounts map[string]int, run FormattableTest) {
	runResult := run.Result
	if len(runResult) > 0 {
		resultTotal, isPresent := resultCounts[runResult]
		if isPresent {
			resultTotal++
			resultCounts[runResult] = resultTotal
		}
	} else {
		resultCounts[RUN_RESULT_ACTIVE]++
	}

}

func initialiseResultMap() map[string]int {
	resultCounts := make(map[string]int, 0)

	for _, label := range RESULT_LABELS {
		resultCounts[label] = 0
	}

	return resultCounts
}
