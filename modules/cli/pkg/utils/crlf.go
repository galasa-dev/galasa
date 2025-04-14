/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"bytes"
)

// Walks throught the input string and converts :
// 0x0d characters to 0x0a (NewLine) characters
// 0x0d 0x0c (CR-LF) to 0x0a (NewLine) characters
//
// so that the entire string can be printed using Golang printf/println without
// line endings being mangled.
func StringWithNewLinesInsteadOfCRLFs(input string) string {

	inputLength := len(input)

	var buff bytes.Buffer = *bytes.NewBufferString("")
	var inputIndex int

	for inputIndex < inputLength {

		thisChar := input[inputIndex]
		var nextChar byte
		if inputIndex+1 < inputLength {
			nextChar = input[inputIndex+1]
		}

		var outChar byte
		if (thisChar == '\r' && nextChar == '\f') || (thisChar == '\f' && nextChar == '\r') {
			outChar = '\n'
			inputIndex += 2
		} else if thisChar == '\r' || thisChar == '\f' {
			outChar = '\n'
			inputIndex += 1
		} else {
			outChar = thisChar
			inputIndex += 1
		}

		buff.WriteString(string(outChar))
	}

	output := buff.String()

	return output
}
