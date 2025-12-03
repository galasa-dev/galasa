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

func (formatter *RawFormatter) FormatRuns(runs []FormattableTest) (string, error) {
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

		tags := strings.Join(run.Tags[:], ",")

		buff.WriteString(run.Name)
		buff.WriteString("|")
		buff.WriteString(run.Status)
		buff.WriteString("|")
		buff.WriteString(run.Result)
		buff.WriteString("|")
		buff.WriteString(run.QueuedTimeUTC)
		buff.WriteString("|")
		buff.WriteString(startTimeStringRaw)
		buff.WriteString("|")
		buff.WriteString(endTimeStringRaw)
		buff.WriteString("|")
		buff.WriteString(duration)
		buff.WriteString("|")
		buff.WriteString(run.TestName)
		buff.WriteString("|")
		buff.WriteString(run.Requestor)
		buff.WriteString("|")
		buff.WriteString(run.Bundle)
		buff.WriteString("|")
		buff.WriteString(run.Group)
		buff.WriteString("|")
		buff.WriteString(runLog)
		buff.WriteString("|")
		buff.WriteString(tags)
		buff.WriteString("|")
		buff.WriteString(run.WebUiUrl)
		buff.WriteString("|")
		buff.WriteString(run.RestApiUrl)

		buff.WriteString("\n")

	}
	result = buff.String()
	return result, err
}
