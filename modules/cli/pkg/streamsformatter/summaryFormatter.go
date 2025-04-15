/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package streamsformatter

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
	ENABLED_STATE          = "enabled"
	DISABLED_STATE         = "disabled"
)

type StreamsSummaryFormatter struct {
}

func NewStreamsSummaryFormatter() StreamsFormatter {
	return new(StreamsSummaryFormatter)
}

func (*StreamsSummaryFormatter) GetName() string {
	return SUMMARY_FORMATTER_NAME
}

func (*StreamsSummaryFormatter) FormatStreams(streams []galasaapi.Stream) (string, error) {

	var result string
	var err error = nil
	buff := strings.Builder{}
	totalStreams := len(streams)

	if totalStreams > 0 {

		var table [][]string
		var headers = []string{HEADER_STREAM_NAME, HEADER_STREAM_STATE, HEADER_STREAM_DESCRIPTION}

		table = append(table, headers)

		for _, stream := range streams {

			var line []string
			var state string

			streamName := stream.Metadata.GetName()
			streamDescription := stream.Metadata.GetDescription()

			if stream.GetData().IsEnabled != nil && *stream.GetData().IsEnabled {
				state = ENABLED_STATE
			} else {
				state = DISABLED_STATE
			}

			line = append(line, streamName, state, streamDescription)
			table = append(table, line)

		}

		columnLengths := utils.CalculateMaxLengthOfEachColumn(table)
		utils.WriteFormattedTableToStringBuilder(table, &buff, columnLengths)

		buff.WriteString("\n")

	}
	buff.WriteString("Total:" + strconv.Itoa(totalStreams) + "\n")

	result = buff.String()
	return result, err
}
