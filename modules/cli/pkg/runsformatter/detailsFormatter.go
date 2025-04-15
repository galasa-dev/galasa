/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runsformatter

import (
	"strings"

	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/utils"
)

// -----------------------------------------------------
// Detailed format.
const (
	DETAILS_FORMATTER_NAME = "details"
)

type DetailsFormatter struct {
}

// -----------------------------------------------------
// Constructors
func NewDetailsFormatter() RunsFormatter {
	return new(DetailsFormatter)

}

// -----------------------------------------------------
// Functions in the RunsFormatter interface
func (*DetailsFormatter) GetName() string {
	return DETAILS_FORMATTER_NAME
}

func (*DetailsFormatter) IsNeedingMethodDetails() bool {
	return true
}

func (*DetailsFormatter) FormatRuns(runs []FormattableTest) (string, error) {
	var result string
	var err error

	totalResults := len(runs)
	resultCountsMap := initialiseResultMap()

	buff := strings.Builder{}

	if len(runs) > 0 {

		for i, run := range runs {
			if run.Lost {
				resultCountsMap[RUN_RESULT_LOST] += 1
			} else {
				accumulateResults(resultCountsMap, run)
				coreDetailsTable := tabulateCoreRunDetails(run)
				coreDetailsColumnLengths := utils.CalculateMaxLengthOfEachColumn(coreDetailsTable)
				utils.WriteFormattedTableToStringBuilder(coreDetailsTable, &buff, coreDetailsColumnLengths)

				buff.WriteString("\n")

				methodTable := initialiseMethodTable()
				methodTable = tabulateRunMethodsToTable(run.Methods, methodTable)
				methodColumnLengths := utils.CalculateMaxLengthOfEachColumn(methodTable)
				utils.WriteFormattedTableToStringBuilder(methodTable, &buff, methodColumnLengths)

				if i < len(runs)-1 {
					buff.WriteString("\n---\n\n")
				}
			}

		}

		buff.WriteString("\n")
	}
	totalReportString := generateResultTotalsReport(totalResults, resultCountsMap)
	buff.WriteString(totalReportString + "\n")

	result = buff.String()

	return result, err
}

// -----------------------------------------------------
// Internal functions
func tabulateCoreRunDetails(run FormattableTest) [][]string {
	startTimeStringRaw := run.StartTimeUTC
	endTimeStringRaw := run.EndTimeUTC

	startTimeStringReadable := getReadableTime(startTimeStringRaw)
	endTimeStringReadable := getReadableTime(endTimeStringRaw)

	duration := getDuration(startTimeStringRaw, endTimeStringRaw)

	var table = [][]string{
		{HEADER_RUNNAME, ": " + run.Name},
		{HEADER_STATUS, ": " + run.Status},
		{HEADER_RESULT, ": " + run.Result},
		{HEADER_SUBMITTED_TIME, ": " + formatTimeReadable(run.QueuedTimeUTC)},
		{HEADER_START_TIME, ": " + startTimeStringReadable},
		{HEADER_END_TIME, ": " + endTimeStringReadable},
		{HEADER_DURATION, ": " + duration},
		{HEADER_TEST_NAME, ": " + run.TestName},
		{HEADER_REQUESTOR, ": " + run.Requestor},
		{HEADER_BUNDLE, ": " + run.Bundle},
		{HEADER_GROUP, ": " + run.Group},
		{HEADER_RUN_LOG, ": " + run.ApiServerUrl + RAS_RUNS_URL + run.RunId + "/runlog"},
	}
	return table
}

func initialiseMethodTable() [][]string {
	var methodTable [][]string
	var headers = []string{HEADER_METHOD_NAME, HEADER_METHOD_TYPE, HEADER_STATUS, HEADER_RESULT, HEADER_START_TIME, HEADER_END_TIME, HEADER_DURATION}
	methodTable = append(methodTable, headers)

	return methodTable
}

func tabulateRunMethodsToTable(methods []galasaapi.TestMethod, methodTable [][]string) [][]string {
	for _, method := range methods {
		startTimeStringRaw := method.GetStartTime()
		endTimeStringRaw := method.GetEndTime()

		startTimeStringReadable := getReadableTime(startTimeStringRaw)
		endTimeStringReadable := getReadableTime(endTimeStringRaw)

		duration := getDuration(startTimeStringRaw, endTimeStringRaw)

		var line []string
		line = append(line,
			method.GetMethodName(),
			method.GetType(),
			method.GetStatus(),
			method.GetResult(),
			startTimeStringReadable,
			endTimeStringReadable,
			duration,
		)
		methodTable = append(methodTable, line)
	}

	return methodTable
}
