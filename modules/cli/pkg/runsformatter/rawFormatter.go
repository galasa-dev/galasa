/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runsformatter

import "strings"

// -----------------------------------------------------
// Summary format.
const (
	RAW_FORMATTER_NAME = "raw"
)

type RawFormatter struct {
}

func NewRawFormatter() RunsFormatter {
	return new(RawFormatter)
}

func (*RawFormatter) GetName() string {
	return RAW_FORMATTER_NAME
}

func (*RawFormatter) IsNeedingMethodDetails() bool {
	return false
}

func (*RawFormatter) FormatRuns(runs []FormattableTest) (string, error) {
	var result string = ""
	var err error
	buff := strings.Builder{}

	for _, run := range runs {
		if run.Lost {
			//don't do anything for this iteration if run is lost
			continue
		}
		startTimeStringRaw := run.StartTimeUTC
		endTimeStringRaw := run.EndTimeUTC

		duration := getDuration(startTimeStringRaw, endTimeStringRaw)

		runLog := run.ApiServerUrl + RAS_RUNS_URL + run.RunId + "/runlog"

		buff.WriteString(run.Name + "|" +
			run.Status + "|" +
			run.Result + "|" +
			run.QueuedTimeUTC + "|" +
			startTimeStringRaw + "|" +
			endTimeStringRaw + "|" +
			duration + "|" +
			run.TestName + "|" +
			run.Requestor + "|" +
			run.Bundle + "|" +
			run.Group + "|" +
			runLog,
		)

		buff.WriteString("\n")

	}
	result = buff.String()
	return result, err
}
