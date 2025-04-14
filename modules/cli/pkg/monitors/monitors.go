/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package monitors

import (
	"strings"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/utils"
)

func validateMonitorName(monitorName string) (string, error) {
    var err error
    monitorName = strings.TrimSpace(monitorName)

    if monitorName == "" || !utils.IsNameValid(monitorName) {
        err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_MONITOR_NAME)
    }
    return monitorName, err
}
