/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"regexp"

	"github.com/galasa-dev/cli/pkg/errors"
)

var (
	// Expect the pattern letters + number
	// ^ matches the start of the string
	// $ matches the end of the string
	// If ^ and $ are not specified, then M2M2 would match (two instances of the match)!
	RUN_NAME_PATTERN *regexp.Regexp = regexp.MustCompile("^[a-zA-Z]+[0-9]+$")
)

// ---------------------------------------------------
// Functions called by other things

// ValidateRunName - Checks that a run name is valid, as much as we can.
// Returns an error if it's invalid, nil if it looks valid.
// This function does not consult with an ecosystem, just checks the
// format of the runName.
func ValidateRunName(runName string) error {

	var err error

	isMatching := RUN_NAME_PATTERN.MatchString(runName)

	if !isMatching {
		err = errors.NewGalasaError(errors.GALASA_ERROR_INVALID_RUN_NAME, runName)
	}

	return err
}
