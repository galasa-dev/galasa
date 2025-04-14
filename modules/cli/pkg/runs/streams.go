/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"log"
	"strings"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/launcher"
)

func GetStreams(launcher launcher.Launcher) ([]string, error) {
	log.Println("Getting streams list.")
	streams, err := launcher.GetStreams()
	return streams, err
}

func ValidateStream(validStreamNames []string, streamNameToCheck string) error {
	log.Printf("Validating that stream %s exists in the list of valid streams.\n", streamNameToCheck)

	var err error

	var streamFound = false

	for _, s := range validStreamNames {
		if s == streamNameToCheck {
			log.Println("Stream is found in the list of valid streams.")
			streamFound = true
			break
		}
	}

	if !streamFound {
		// Not a valid stream name. Build the error message.
		log.Println("Stream not found, deciding error.")
		if len(validStreamNames) < 1 {
			// No streams configured.
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_NO_STREAMS_CONFIGURED, streamNameToCheck)
		} else {

			var buffer strings.Builder
			var availableStreamsList string
			for _, s := range validStreamNames {
				buffer.WriteString(" '")
				buffer.WriteString(s)
				buffer.WriteString("'")
			}
			availableStreamsList = buffer.String()
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_STREAM, streamNameToCheck, availableStreamsList)
		}
	}

	return err
}
