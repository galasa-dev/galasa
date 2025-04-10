/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package errors

import (
	"net/http"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestGetApiErrorEmptyParsesInputOk(t *testing.T) {
	// Given

	bodyString := `{}`

	bodyBytes := []byte(bodyString)
	statusCode := http.StatusInternalServerError

	var err error

	// When
	_, err = GetApiErrorFromResponse(statusCode, bodyBytes)

	// Then
	assert.Nil(t, err, "GetApiErrorFromResponse, empty body failed with a non-nil error!")
}

func TestGetApiErrorSingleJsonObjectsParsesInputOk(t *testing.T) {
	// Given

	bodyString := `{
            "error_code" : 2003,
            "error_message" : "Error: GAL2003 - Invalid yaml format"
        }`

	bodyBytes := []byte(bodyString)
	statusCode := http.StatusInternalServerError

	var parsedError *GalasaAPIError
	var err error

	// When
	parsedError, err = GetApiErrorFromResponse(statusCode, bodyBytes)

	// Then
	assert.Nil(t, err, "NewGalasaApiErrorsArray failed with a non-nil error!")
	assert.NotNil(t, parsedError)
}
