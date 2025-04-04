/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package errors

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestGetApiErrorArrayEmptyParsesInputOk(t *testing.T) {
	// Given

	bodyString := `[]`

	bodyBytes := []byte(bodyString)

	var err error

	// When
	_, err = NewGalasaApiErrorsArray(bodyBytes)

	// Then
	assert.Nil(t, err, "NewGalasaApiErrorsArray failed with a non-nil error!")
}

func TestGetApiErrorArrayWithSingleJsonObjectsParsesInputOk(t *testing.T) {
	// Given

	bodyString := `[
        {
            "error_code" : 2003,
            "error_message" : "Error: GAL2003 - Invalid yaml format"
        }
    ]`

	bodyBytes := []byte(bodyString)

	var parsedErrors *GalasaAPIErrorsArray
	var err error

	// When
	parsedErrors, err = NewGalasaApiErrorsArray(bodyBytes)

	// Then
	assert.Nil(t, err, "NewGalasaApiErrorsArray failed with a non-nil error!")
	assert.NotNil(t, parsedErrors, "NewGalasaApiErrorsArray returned no error, but the parsed structure is nil!")
	parsedErrorMessages := parsedErrors.GetErrorMessages()
	assert.NotNil(t, parsedErrorMessages, "The list of errors inside the parsed structure is nil.")
	assert.Equal(t, 1, len(parsedErrorMessages), "Wrong number of errors collected!")
}

func TestGetApiErrorArrayWithMultipleJsonObjectsParsesInputOk(t *testing.T) {
	// Given

	bodyString := `[
        {
            "error_code" : 2003,
            "error_message" : "Error: GAL2003 - Invalid yaml format"
        },
        {
            "error_code": 343,
            "error_message": "GAL343 - Unable to marshal into json"
        }
    ]`

	bodyBytes := []byte(bodyString)

	var parsedErrors *GalasaAPIErrorsArray
	var err error

	// When
	parsedErrors, err = NewGalasaApiErrorsArray(bodyBytes)

	// Then
	assert.Nil(t, err, "NewGalasaApiErrorsArray failed with a non-nil error!")
	assert.NotNil(t, parsedErrors, "NewGalasaApiErrorsArray returned no error, but the parsed structure is nil!")
	parsedErrorMessages := parsedErrors.GetErrorMessages()
	assert.NotNil(t, parsedErrorMessages, "The list of errors inside the parsed structure is nil.")
	assert.Equal(t, 2, len(parsedErrorMessages), "Wrong number of errors collected!")
}
