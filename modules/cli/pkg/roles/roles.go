/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package roles

import (
	"strings"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/utils"
)

func validateRoleName(nameToValidate string) (string, error) {
	var err error
	name := strings.TrimSpace(nameToValidate)

	if name == "" || !utils.IsNameValid(name) {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_ROLE_NAME)
	}
	return name, err
}
