/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package secretsformatter

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

type SecretSummaryFormatter struct {
}

func NewSecretSummaryFormatter() SecretsFormatter {
	return new(SecretSummaryFormatter)
}

func (*SecretSummaryFormatter) GetName() string {
	return SUMMARY_FORMATTER_NAME
}

func (*SecretSummaryFormatter) FormatSecrets(secrets []galasaapi.GalasaSecret) (string, error) {
	var result string = ""
	var err error = nil
	buff := strings.Builder{}
	totalSecrets := len(secrets)

	if totalSecrets > 0 {
		var table [][]string

		var headers = []string{
			HEADER_SECRET_NAME,
			HEADER_SECRET_TYPE,
			HEADER_LAST_UPDATED_TIME,
			HEADER_LAST_UPDATED_BY,
			HEADER_SECRET_DESCRIPTION,
		}

		table = append(table, headers)
		for _, secret := range secrets {
			var line []string
			name := secret.Metadata.GetName()
			secretType := secret.Metadata.GetType()
			secretDescription := secret.Metadata.GetDescription()
			lastUpdatedTime := secret.Metadata.GetLastUpdatedTime()

			lastUpdatedTimeReadable := ""
			if !lastUpdatedTime.IsZero() {
				lastUpdatedTimeReadable = lastUpdatedTime.Format("2006-01-02 15:04:05")
			}
			lastUpdatedBy := secret.Metadata.GetLastUpdatedBy()

			line = append(line, name, string(secretType), lastUpdatedTimeReadable, lastUpdatedBy, secretDescription)
			table = append(table, line)
		}

		columnLengths := utils.CalculateMaxLengthOfEachColumn(table)
		utils.WriteFormattedTableToStringBuilder(table, &buff, columnLengths)

		buff.WriteString("\n")

	}
	buff.WriteString("Total:" + strconv.Itoa(totalSecrets) + "\n")

	result = buff.String()
	return result, err
}
