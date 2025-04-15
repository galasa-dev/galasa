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

//------
//PROPERTIES testing

func CreateMockGalasaProperty(namespace string, name string, value string) *galasaapi.GalasaProperty {
	var property = galasaapi.NewGalasaProperty()

	property.SetApiVersion("myApiVersion")
	property.SetKind("GalasaProperty")
	metadata := galasaapi.NewGalasaPropertyMetadata()
	metadata.SetNamespace(namespace)
	metadata.SetName(name)
	property.SetMetadata(*metadata)

	data := galasaapi.NewGalasaPropertyData()
	data.SetValue(value)
	property.SetData(*data)

	return property
}

func TestPropertiesSummaryFormatterNoDataReturnsTotalCountAllZeros(t *testing.T) {

	formatter := NewPropertySummaryFormatter()
	// No data to format...
	properties := make([]galasaapi.GalasaProperty, 0)

	// When...
	actualFormattedOutput, err := formatter.FormatProperties(properties)

	assert.Nil(t, err)
	expectedFormattedOutput := "Total:0\n"
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestPropertiesSummaryFormatterSingleDataReturnsCorrectly(t *testing.T) {
	// For..
	formatter := NewPropertySummaryFormatter()
	// No data to format...
	properties := make([]galasaapi.GalasaProperty, 0)
	property1 := CreateMockGalasaProperty("testNamespace", "name1", "value1")
	properties = append(properties, *property1)

	// When...
	actualFormattedOutput, err := formatter.FormatProperties(properties)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput :=
		`namespace     name  value
testNamespace name1 value1

Total:1
`
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestPropertiesSummaryFormatterMultipleDataSeperatesWithNewLine(t *testing.T) {
	// For..
	formatter := NewPropertySummaryFormatter()
	// No data to format...
	properties := make([]galasaapi.GalasaProperty, 0)
	property1 := CreateMockGalasaProperty("testNamespace", "name1", "value1")
	property2 := CreateMockGalasaProperty("testNamespace", "name2", "value2")
	properties = append(properties, *property1, *property2)

	// When...
	actualFormattedOutput, err := formatter.FormatProperties(properties)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput :=
		`namespace     name  value
testNamespace name1 value1
testNamespace name2 value2

Total:2
`
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

// NAMESPACE
func CreateNamespace(namespaceName string, namespaceType string, namespaceUrl string) *galasaapi.Namespace {
	namespace := galasaapi.NewNamespace()
	namespace.SetName(namespaceName)
	namespace.SetType(namespaceType)
	if namespaceUrl != "" {
		namespace.SetPropertiesUrl(namespaceUrl)
	}
	return namespace
}
func TestNamespaceSummaryFormatterNoDataReturnsTotalCountAllZeros(t *testing.T) {

	formatter := NewPropertySummaryFormatter()
	// No data to format...
	namespace := make([]galasaapi.Namespace, 0)

	// When...
	actualFormattedOutput, err := formatter.FormatNamespaces(namespace)

	assert.Nil(t, err)
	expectedFormattedOutput := "Total:0\n"
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestNamespaceSummaryFormatterSinglePropertyReturnsExpectedFormat(t *testing.T) {
	formatter := NewPropertySummaryFormatter()

	namespace := make([]galasaapi.Namespace, 0)
	namespace1 := CreateNamespace("framework", "normal", "")
	namespace = append(namespace, *namespace1)

	// When...
	actualFormattedOutput, err := formatter.FormatNamespaces(namespace)

	assert.Nil(t, err)
	expectedFormattedOutput :=
		"namespace type\n" +
			"framework normal\n" +
			"\n" +
			"Total:1\n"
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestNamespaceSummaryFormatterMultiplePropertiesReturnsExpectedFormat(t *testing.T) {
	formatter := NewPropertySummaryFormatter()

	namespace := make([]galasaapi.Namespace, 0)
	namespace1 := CreateNamespace("framework", "normal", "")
	namespace2 := CreateNamespace("secure", "secure", "")
	namespace3 := CreateNamespace("anamespace", "normal", "")
	namespace = append(namespace, *namespace1, *namespace2, *namespace3)

	// When...
	actualFormattedOutput, err := formatter.FormatNamespaces(namespace)

	assert.Nil(t, err)
	expectedFormattedOutput :=
		"namespace  type\n" +
			"framework  normal\n" +
			"secure     secure\n" +
			"anamespace normal\n" +
			"\n" +
			"Total:3\n"
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}
