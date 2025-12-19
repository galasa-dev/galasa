/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tagsformatter

import (
	"strconv"
	"strings"

	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/utils"
)

// -----------------------------------------------------
// Displays tags in the following summary format:
//
// name                  priority description          
// core_regression_tests 100      Core regression tests
// my-experimental-tag   5        Experimental tests   
// anothertag 		     10       Dummy tag            
//
// Total:3

const (
	SUMMARY_FORMATTER_NAME = "summary"
)

type TagSummaryFormatter struct {
}

func NewTagSummaryFormatter() TagsFormatter {
	return new(TagSummaryFormatter)
}

func (*TagSummaryFormatter) GetName() string {
	return SUMMARY_FORMATTER_NAME
}

func (*TagSummaryFormatter) FormatTags(tags []galasaapi.GalasaTag) (string, error) {
	var result string = ""
	var err error = nil
	buff := strings.Builder{}
	totalTags := len(tags)

	if totalTags > 0 {
		var table [][]string

		var headers = []string{
			HEADER_TAG_NAME,
			HEADER_TAG_PRIORITY,
			HEADER_TAG_DESCRIPTION,
		}

		table = append(table, headers)
		for _, tag := range tags {
			var line []string
			name := tag.Metadata.GetName()
			tagDescription := tag.Metadata.GetDescription()
			tagPriority := int(tag.Data.GetPriority())

			line = append(line, name, strconv.Itoa(tagPriority), tagDescription)
			table = append(table, line)
		}

		columnLengths := utils.CalculateMaxLengthOfEachColumn(table)
		utils.WriteFormattedTableToStringBuilder(table, &buff, columnLengths)

		buff.WriteString("\n")

	}
	buff.WriteString("Total:" + strconv.Itoa(totalTags) + "\n")

	result = buff.String()
	return result, err
}
