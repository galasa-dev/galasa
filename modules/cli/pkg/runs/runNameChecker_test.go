/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

// ---------------------------------------------------
// Functions
func TestValidRunNamePassesValidation(t *testing.T) {
	err := ValidateRunName("U345")
	assert.Nil(t, err)
}

func TestInvalidRunNameStartsWithLettersValidationFailsWithError(t *testing.T) {
	checkInvalidRunNameFails(t, "345MMM")
}

func TestInvalidRunNameContainsSeparatorValidationFailsWithError(t *testing.T) {
	checkInvalidRunNameFails(t, "MMM-656")
}

func TestInvalidRunNameContainsNumberInMiddleValidationFailsWithError(t *testing.T) {
	checkInvalidRunNameFails(t, "MMM656MMM666")
}

func checkInvalidRunNameFails(t *testing.T, runName string) {
	err := ValidateRunName(runName)
	assert.NotNil(t, err, "Should not have validated OK.")
	if err != nil {
		assert.ErrorContains(t, err, "GAL1075E")
		assert.ErrorContains(t, err, runName)
	}
}
