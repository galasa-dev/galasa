/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"log"
	"os"
	"reflect"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/spi"
)

// The real implementation of the interface.
type RealFinalWordHandler struct {
}

func NewRealFinalWordHandler() spi.FinalWordHandler {
	handler := new(RealFinalWordHandler)
	return handler
}

func (handler *RealFinalWordHandler) FinalWord(rootCmd spi.GalasaCommand, errorToExctractFrom interface{}) {

	rootCmdValues := rootCmd.Values().(*RootCmdValues)

	text, exitCode, isStackTraceWanted := extractErrorDetails(errorToExctractFrom)
	if rootCmdValues.isCapturingLogs {
		log.Println(text)
	}

	if isStackTraceWanted && rootCmdValues.isCapturingLogs {
		galasaErrors.LogStackTrace()
	}

	if rootCmdValues.isCapturingLogs {
		log.Printf("Exit code is %v", exitCode)
	}

	os.Exit(exitCode)
}

func extractErrorDetails(errorToExctractFrom interface{}) (string, int, bool) {
	exitCode := 0
	errorText := ""
	var isStackTraceWanted bool = false

	if errorToExctractFrom == nil {
		errorText = "OK"
	} else {
		exitCode = 1
		isStackTraceWanted = true

		// If it's a pointer to a galasa error.
		galasaErrorPtr, isGalasaError := errorToExctractFrom.(*galasaErrors.GalasaError)
		if isGalasaError {
			errorType := (galasaErrorPtr).GetMessageType()
			if errorType.Ordinal == galasaErrors.GALASA_ERROR_TESTS_FAILED.Ordinal {
				// The failure was because some tests failed, rather than the tool or infrastructure failed.
				exitCode = 2
			}
			// Don't log a stack trace for Galasa errors. We know where they come from.
			isStackTraceWanted = false
		}

		err, isErrorType := errorToExctractFrom.(error)
		if isErrorType {
			errorText = err.Error()
		} else {

			stringValue, isString := errorToExctractFrom.(string)
			if isString {
				errorText = stringValue
			} else {
				errorText = "unknown error."
			}
		}
	}

	return errorText, exitCode, isStackTraceWanted
}

func IsInstanceOf(objectPtr interface{}, typePtr interface{}) bool {
	return reflect.TypeOf(objectPtr) == reflect.TypeOf(typePtr)
}
