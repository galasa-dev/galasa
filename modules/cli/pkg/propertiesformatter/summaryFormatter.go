/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package propertiesformatter

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

type PropertySummaryFormatter struct {
}

func NewPropertySummaryFormatter() PropertyFormatter {
	return new(PropertySummaryFormatter)
}

func (*PropertySummaryFormatter) GetName() string {
	return SUMMARY_FORMATTER_NAME
}

func (*PropertySummaryFormatter) FormatProperties(cpsProperties []galasaapi.GalasaProperty) (string, error) {
	var result string
	var err error
	buff := strings.Builder{}
	totalProperties := len(cpsProperties)

	if totalProperties > 0 {
		var table [][]string

		var headers = []string{HEADER_PROPERTY_NAMESPACE, HEADER_PROPERTY_NAME, HEADER_PROPERTY_VALUE}

		table = append(table, headers)
		for _, property := range cpsProperties {
			var line []string
			namespace := *property.Metadata.Namespace
			name := *property.Metadata.Name
			value := *property.Data.Value

			line = append(line, namespace)
			line = append(line, name, cropExtraLongValue(substituteNewLines(value)))
			table = append(table, line)
		}

		columnLengths := utils.CalculateMaxLengthOfEachColumn(table)
		utils.WriteFormattedTableToStringBuilder(table, &buff, columnLengths)

		buff.WriteString("\n")

	}
	buff.WriteString("Total:" + strconv.Itoa(totalProperties) + "\n")

	result = buff.String()
	return result, err
}

func (*PropertySummaryFormatter) FormatNamespaces(namespaces []galasaapi.Namespace) (string, error) {
	var result string
	var err error
	buff := strings.Builder{}
	totalNamespaces := len(namespaces)

	if totalNamespaces > 0 {
		var table [][]string

		var headers = []string{HEADER_NAMESPACE, HEADER_NAMESPACE_TYPE}

		table = append(table, headers)
		for _, namespace := range namespaces {
			var line []string

			line = append(line, *namespace.Name, *namespace.Type)
			table = append(table, line)
		}

		columnLengths := utils.CalculateMaxLengthOfEachColumn(table)
		utils.WriteFormattedTableToStringBuilder(table, &buff, columnLengths)

		buff.WriteString("\n")

	}
	buff.WriteString("Total:" + strconv.Itoa(totalNamespaces) + "\n")

	result = buff.String()
	return result, err
}
