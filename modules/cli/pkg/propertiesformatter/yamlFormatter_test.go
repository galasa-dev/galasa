/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package propertiesformatter

import (
	"testing"

	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/stretchr/testify/assert"
)

// PROPERTIES
func TestPropertiesYamlFormatterNoDataReturnsBlankString(t *testing.T) {

	formatter := NewPropertyYamlFormatter()
	// No data to format...
	formattableProperty := make([]galasaapi.GalasaProperty, 0)

	// When...
	actualFormattedOutput, err := formatter.FormatProperties(formattableProperty)

	assert.Nil(t, err)
	expectedFormattedOutput := ""
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestPropertiesYamlFormatterSingleDataReturnsCorrectly(t *testing.T) {
	// For..
	formatter := NewPropertyYamlFormatter()
	// No data to format...
	formattableProperties := make([]galasaapi.GalasaProperty, 0)
	property1 := CreateMockGalasaProperty("namespace", "name1", "value1")
	formattableProperties = append(formattableProperties, *property1)

	// When...
	actualFormattedOutput, err := formatter.FormatProperties(formattableProperties)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput := `apiVersion: myApiVersion
kind: GalasaProperty
metadata:
    namespace: namespace
    name: name1
data:
    value: value1
`
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestPropertiesYamlFormatterMultipleDataSeperatesWithNewLine(t *testing.T) {
	// For..
	formatter := NewPropertyYamlFormatter()
	// No data to format...
	formattableProperties := make([]galasaapi.GalasaProperty, 0)
	property1 := CreateMockGalasaProperty("namespace", "name1", "value1")
	property2 := CreateMockGalasaProperty("namespace", "name2", "value2")
	formattableProperties = append(formattableProperties, *property1, *property2)

	// When...
	actualFormattedOutput, err := formatter.FormatProperties(formattableProperties)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput := `apiVersion: myApiVersion
kind: GalasaProperty
metadata:
    namespace: namespace
    name: name1
data:
    value: value1
---
apiVersion: myApiVersion
kind: GalasaProperty
metadata:
    namespace: namespace
    name: name2
data:
    value: value2
`
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}
