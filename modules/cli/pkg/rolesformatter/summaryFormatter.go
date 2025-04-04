/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package rolesformatter

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

type RolesSummaryFormatter struct {
}

func NewRolesSummaryFormatter() RolesFormatter {
	return new(RolesSummaryFormatter)
}

func (*RolesSummaryFormatter) GetName() string {
	return SUMMARY_FORMATTER_NAME
}

func (*RolesSummaryFormatter) FormatRoles(roles []galasaapi.RBACRole) (string, error) {
	var result string
	var err error = nil
	buff := strings.Builder{}
	total := len(roles)

	if total > 0 {
		var table [][]string

		var headers = []string{
			HEADER_ROLE_NAME,
			HEADER_ROLE_DESCRIPTION,
		}

		table = append(table, headers)
		for _, role := range roles {
			var line []string
			name := role.Metadata.GetName()
			description := role.Metadata.GetDescription()
			line = append(line, name, description)
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
