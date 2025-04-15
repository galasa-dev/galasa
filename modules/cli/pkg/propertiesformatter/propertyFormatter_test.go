/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package propertiesformatter

import (
	"log"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestLongPropertyGetsCropped(t *testing.T) {
	// For...
	original := "012345678901234567890123456789012345678901234567890123456789-this-is-over-long"
	expectedPropertyValue := "012345678901234567890123456789012345678901234567890123456789...(cropped)"

	croppedValue := cropExtraLongValue(original)
	// Then...
	assert.Equal(t, croppedValue, expectedPropertyValue)
}

func TestShortPropertyGetsUnaffectedByCropping(t *testing.T) {
	// For...
	original := "this-is-short-enough-not-to-be-cropped"
	expectedPropertyValue := original

	croppedValue := cropExtraLongValue(original)
	// Then...
	assert.Equal(t, croppedValue, expectedPropertyValue)
}

func TestShortPropertyWithNewLinesGetsReplacedBySlashN(t *testing.T) {
	// For...
	original := "this-is-a-value\nspread-over-multiple\nlines"
	expectedPropertyValue := "this-is-a-value\nspread-over-multiple\nlines"

	croppedValue := cropExtraLongValue(original)
	log.Print("original:" + original)
	log.Print("cropped:" + croppedValue)
	// Then...
	assert.Equal(t, croppedValue, expectedPropertyValue)
}
