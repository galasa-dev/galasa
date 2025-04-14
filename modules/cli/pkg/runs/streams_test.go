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

var streams = []string{"bsf", "prod"}

func TestValidateStreamFound(t *testing.T) {
	actual := ValidateStream(streams, "prod")
	assert.Nil(t, actual, "Validate didn't find the correct stream!")
}

func TestValidateStreamNotFoundNormal(t *testing.T) {
	err := ValidateStream(streams, "apple")
	expected := "GAL1030E: Stream 'apple' is not found in the ecosystem. Valid streams are: 'bsf' 'prod'. " +
		"Try again using a valid stream, or ask your Galasa system administrator to " +
		"add a new stream with the desired name."
	if assert.NotNil(t, err, "ValidateStream found a stream where it should have returned an error message.") {
		errorString := err.Error()
		assert.Equal(t, expected, errorString, "Validate didn't fail with the correct response.")
	}
}

func TestValidateStreamNotFoundNoStreamsToFind(t *testing.T) {
	emptyStreams := []string{}
	err := ValidateStream(emptyStreams, "orange")
	expected := "GAL1029E: Stream 'orange' is not found in the ecosystem. There are no streams set up. " +
		"Ask your Galasa system administrator to add a new stream with the desired name."
	if assert.NotNil(t, err) {
		errorString := err.Error()
		assert.Equal(t, expected, errorString, "Validate didn't fail with the correct response.")
	}
}
