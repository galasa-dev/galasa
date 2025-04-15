/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package streams

import (
	"strings"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/utils"
)

func validateStreamName(streamName string) (string, error) {

	var err error
	streamName = strings.TrimSpace(streamName)

	if streamName == "" {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_MISSING_STREAM_NAME_FLAG)
	} else {
		if !utils.IsNameValid(streamName) {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_STREAM_NAME)
		}
	}

	return streamName, err

}
