/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package secrets

import (
	"strings"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/utils"
)

func validateSecretName(secretName string) (string, error) {
    var err error
    secretName = strings.TrimSpace(secretName)

    if secretName == "" || !utils.IsNameValid(secretName) {
        err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_SECRET_NAME)
    }
    return secretName, err
}

func validateDescription(description string) (string, error) {
    var err error
    description = strings.TrimSpace(description)

    if description == "" || !utils.IsLatin1(description) {
        err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_SECRET_DESCRIPTION)
    }
    return description, err
}
