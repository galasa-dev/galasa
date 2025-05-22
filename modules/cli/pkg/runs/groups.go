/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"strings"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/utils"
)

func validateGroupName(groupName string) (string, error) {
	var err error
	trimmedName := strings.TrimSpace(groupName)

	if trimmedName == "" || !utils.IsNameValid(trimmedName) {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_GROUP_NAME_PROVIDED)
	}
	return trimmedName, err
}
