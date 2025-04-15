/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestCRLFtoLFdoesNotCorruptANormalMultiLineString(t *testing.T) {
	input := "my test string\nwith multiple lines"
	output := StringWithNewLinesInsteadOfCRLFs(input)
	assert.Equal(t, input, output)
}

func TestCRLFtoLFdoesNotCorruptANormalSingleLineString(t *testing.T) {
	input := "my test string"
	output := StringWithNewLinesInsteadOfCRLFs(input)
	assert.Equal(t, input, output)
}

func TestCRLFtoLFdoesNotCorruptANormalSingleLineStringWithANewLine(t *testing.T) {
	input := "my test string\n"
	output := StringWithNewLinesInsteadOfCRLFs(input)
	assert.Equal(t, input, output)
}

func TestCRLFtoLFReplacesSingleCRWithNewLine(t *testing.T) {
	input := "my test string\rwith multiple lines"
	expected := "my test string\nwith multiple lines"
	output := StringWithNewLinesInsteadOfCRLFs(input)
	assert.Equal(t, expected, output)
}

func TestCRLFtoLFReplacesSingleCRAtEndOfLineWithNewLine(t *testing.T) {
	input := "my test string\r"
	expected := "my test string\n"
	output := StringWithNewLinesInsteadOfCRLFs(input)
	assert.Equal(t, expected, output)
}

func TestCRLFtoLFReplacesSingleCRLFWithNewLine(t *testing.T) {
	input := "my test string\f\rwith multiple lines"
	expected := "my test string\nwith multiple lines"
	output := StringWithNewLinesInsteadOfCRLFs(input)
	assert.Equal(t, expected, output)
}

func TestCRLFtoLFReplacesSingleCRLFAtEndOfLinbeWithNewLine(t *testing.T) {
	input := "my test string\f\r"
	expected := "my test string\n"
	output := StringWithNewLinesInsteadOfCRLFs(input)
	assert.Equal(t, expected, output)
}

func TestCRLFtoLFReplacesSingleLFCRWithNewLine(t *testing.T) {
	input := "my test string\r\fwith multiple lines"
	expected := "my test string\nwith multiple lines"
	output := StringWithNewLinesInsteadOfCRLFs(input)
	assert.Equal(t, expected, output)
}

func TestCRLFtoLFReplacesSingleAtEndOfLineLFCRWithNewLine(t *testing.T) {
	input := "my test string\r\f"
	expected := "my test string\n"
	output := StringWithNewLinesInsteadOfCRLFs(input)
	assert.Equal(t, expected, output)
}

func TestCRLFtoLFReplacesSinglCRWithNewLine(t *testing.T) {
	input := "my test string\fwith multiple lines"
	expected := "my test string\nwith multiple lines"
	output := StringWithNewLinesInsteadOfCRLFs(input)
	assert.Equal(t, expected, output)
}

func TestCRLFtoLFReplacesSinglCRAtEndOfLineWithNewLine(t *testing.T) {
	input := "my test string\f"
	expected := "my test string\n"
	output := StringWithNewLinesInsteadOfCRLFs(input)
	assert.Equal(t, expected, output)
}
