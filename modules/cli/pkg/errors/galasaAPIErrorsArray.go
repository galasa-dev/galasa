/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package errors

import (
	"encoding/json"
	"log"
)

// This function reads an array of galasa API Errors into an array of GalasaAPIError structure so that
// all errors can be displayed as a human readable format.
// NOTE: when this function is called ensure that the calling function has the  `defer resp.Body.Close()`
// called in order to ensure that the response body is closed when the function completes
type GalasaAPIErrorsArray struct {
	errorArray *[]GalasaAPIError
}

func NewGalasaApiErrorsArray(body []byte) (*GalasaAPIErrorsArray, error) {
	var err error
	var jsonArray []GalasaAPIError

	errorsGathered := new(GalasaAPIErrorsArray)

	err = json.Unmarshal(body, &jsonArray)

	if err != nil {
		log.Printf("NewGalasaApiErrorsArray FAIL - %v", err)
	}

	errorsGathered.errorArray = &jsonArray
	return errorsGathered, err
}

// // This Function will return a string array of all the error messages within the GalasaAPIErrorsArray array to be
// displayed in a human readble format
func (apiErrors *GalasaAPIErrorsArray) GetErrorMessages() []string {
	var errorString []string

	for _, errorMsg := range *apiErrors.errorArray {
		errorString = append(errorString, errorMsg.Message)
	}

	return errorString
}
