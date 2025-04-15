/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package monitorsformatter

import (
	"strconv"
	"strings"

	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/utils"
)

// -----------------------------------------------------
// Summary format.
const (
	SUMMARY_FORMATTER_NAME = "summary"
)

type MonitorsSummaryFormatter struct {
}

func NewMonitorsSummaryFormatter() MonitorsFormatter {
	return new(MonitorsSummaryFormatter)
}

func (*MonitorsSummaryFormatter) GetName() string {
	return SUMMARY_FORMATTER_NAME
}

func (*MonitorsSummaryFormatter) FormatMonitors(monitors []galasaapi.GalasaMonitor) (string, error) {
	var result string
	var err error = nil
	buff := strings.Builder{}
	total := len(monitors)

	if total > 0 {
		var table [][]string

		var headers = []string{
			HEADER_MONITOR_NAME,
			HEADER_MONITOR_KIND,
			HEADER_MONITOR_IS_ENABLED,
		}

		table = append(table, headers)
		for _, monitor := range monitors {
			var line []string
			name := monitor.Metadata.GetName()
			kind := monitor.GetKind()
			isEnabled := monitor.Data.GetIsEnabled()

			line = append(line, name, kind, strconv.FormatBool(isEnabled))
			table = append(table, line)
		}

		columnLengths := utils.CalculateMaxLengthOfEachColumn(table)
		utils.WriteFormattedTableToStringBuilder(table, &buff, columnLengths)

		buff.WriteString("\n")

	}
	buff.WriteString("Total:" + strconv.Itoa(total) + "\n")

	result = buff.String()
	return result, err
}
