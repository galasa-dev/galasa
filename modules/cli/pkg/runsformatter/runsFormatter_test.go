/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runsformatter

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestFormatTimeReadableDoesNotBlowUpWhenInputIsBlank(t *testing.T) {
	//When
	blankOutput := formatTimeReadable("")
	//Then - if we got this far, it didn't blow up with a slice out of bounds error
	assert.Equal(t, "", blankOutput)
}

func TestFormatTimeReadableTooShortTimeShouldReturnBlank(t *testing.T) {
	//When
	blankOutput := formatTimeReadable("2023-05-04T10:45:2") //18 char string
	//Then - if we got this far, it didn't blow up with a slice out of bounds error
	assert.Equal(t, "", blankOutput)
}

func TestFormatTimeReadableNormalTimeShouldReturnReadableTimeStamp(t *testing.T) {
	//When
	output := formatTimeReadable("2023-05-04T10:45:29.545323Z") //long char
	//Then - if we got this far, it didn't blow up with a slice out of bounds error
	assert.Equal(t, "2023-05-04 10:45:29", output)
}

func TestFormatTimeReadableShortestValidTimeShouldReturnReadableTimeStamp(t *testing.T) {
	//When
	output := formatTimeReadable("2023-05-04T10:45:29") //19 char string
	//Then - if we got this far, it didn't blow up with a slice out of bounds error
	assert.Equal(t, "2023-05-04 10:45:29", output)
}
