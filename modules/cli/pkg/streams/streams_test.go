/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package streams

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestValidateStreamNameFlagReturnsError_EmptyOrNullValue(t *testing.T) {

	invalidStreamName := ""

	streamName, err := validateStreamName(invalidStreamName)

	assert.NotNil(t, err)
	assert.Empty(t, streamName)
}

func TestValidateStreamNameRegexFlagReturnsError(t *testing.T) {

	invalidStreamName := "my.stream"

	streamName, err := validateStreamName(invalidStreamName)

	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1235E")
	assert.NotEmpty(t, streamName)
}

func TestValidateStreamNameRegexWithUnderScoreFlagReturnsNoError(t *testing.T) {

	validStreamName := "my_stream"

	streamName, err := validateStreamName(validStreamName)

	assert.Nil(t, err)
	assert.NotEmpty(t, streamName)
}

func TestValidateStreamNameRegexWithDashFlagReturnsNoError(t *testing.T) {

	validStreamName := "my-stream"

	streamName, err := validateStreamName(validStreamName)

	assert.Nil(t, err)
	assert.NotEmpty(t, streamName)
}
