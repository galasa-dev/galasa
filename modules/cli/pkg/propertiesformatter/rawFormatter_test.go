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

//PROPERTIES

func TestPropertiesRawFormatterNoDataReturnsNothing(t *testing.T) {
	// For...
	formatter := NewPropertyRawFormatter()
	// No data to format...
	properties := make([]galasaapi.GalasaProperty, 0)

	// When...
	actualFormattedOutput, err := formatter.FormatProperties(properties)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput := ""
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestPropertiesRawFormatterSingleDataReturnsCorrectly(t *testing.T) {
	// For..
	formatter := NewPropertyRawFormatter()

	properties := make([]galasaapi.GalasaProperty, 0)
	property1 := CreateMockGalasaProperty("testNamespace", "name1", "value1")

	properties = append(properties, *property1)

	// When...
	actualFormattedOutput, err := formatter.FormatProperties(properties)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput := "testNamespace|name1|value1\n"
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestPropertiesRawFormatterMultipleDataSeperatesWithNewLine(t *testing.T) {
	// For..
	formatter := NewPropertyRawFormatter()

	properties := make([]galasaapi.GalasaProperty, 0)
	property1 := CreateMockGalasaProperty("namespace", "name1", "value1")
	property2 := CreateMockGalasaProperty("namespace", "name2", "value2")
	properties = append(properties, *property1, *property2)

	// When...
	actualFormattedOutput, err := formatter.FormatProperties(properties)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput := "namespace|name1|value1\n" +
		"namespace|name2|value2\n"
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

// NAMESPACES
func TestNamespacesRawFormatterNoDataReturnsNothing(t *testing.T) {
	// For...
	formatter := NewPropertyRawFormatter()
	// No data to format...
	namespaces := make([]galasaapi.Namespace, 0)

	// When...
	actualFormattedOutput, err := formatter.FormatNamespaces(namespaces)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput := ""
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestNamespacesRawFormatterSingleDataReturnsCorrectly(t *testing.T) {
	// For..
	formatter := NewPropertyRawFormatter()

	namespaces := make([]galasaapi.Namespace, 0)
	namespace1 := CreateNamespace("namespace1", "normal", "")
	namespaces = append(namespaces, *namespace1)

	// When...
	actualFormattedOutput, err := formatter.FormatNamespaces(namespaces)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput := "namespace1|normal\n"
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestNamespacesRawFormatterMultipleDataSeperatesWithNewLine(t *testing.T) {
	// For..
	formatter := NewPropertyRawFormatter()

	namespaces := make([]galasaapi.Namespace, 0)
	namespace1 := CreateNamespace("namespace1", "secure", "")
	namespace2 := CreateNamespace("namespace2", "normal", "")
	namespaces = append(namespaces, *namespace1, *namespace2)

	// When...
	actualFormattedOutput, err := formatter.FormatNamespaces(namespaces)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput := "namespace1|secure\n" +
		"namespace2|normal\n"
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}
