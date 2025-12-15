/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package errors

import (
	"regexp"
	"slices"
	"testing"

	"github.com/stretchr/testify/assert"
)

const (
	// This regex pattern matches strings where the start of the string:
	// - Starts with "GAL"
	// - Followed by at least one number in the range 0-9
	// - Followed by "E:"
	GAL_ERROR_PATTERN = "^GAL[0-9]+E:"
)

func TestNextMessageNumberToAllocateIsHighestSoFar(t *testing.T) {
	// Given...
	errorRegexPattern, _ := regexp.Compile(GAL_ERROR_PATTERN)

	messageCodes := make([]int, 0, len(GALASA_ALL_MESSAGES))
	for key, value := range GALASA_ALL_MESSAGES {
		if errorRegexPattern.MatchString(value.Template) {
			// This is an error message, get the message code
			messageCodes = append(messageCodes, key)
		}
	}
	highestCodeSoFar := slices.Max(messageCodes)

	// Then
	assert.Less(t, highestCodeSoFar, GALxxx_NEXT_MESSAGE_NUMBER_TO_USE,
		"Highest message number in use is higher than the GALxxx_NEXT_MESSAGE_NUMBER_TO_USE marker value. "+
        "Edit the GALxxx_NEXT_MESSAGE_NUMBER_TO_USE value to be higher than that.")
}

