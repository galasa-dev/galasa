/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package properties

import (
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

// MockServlet
func NewPropertiesServletMock(t *testing.T) *httptest.Server {

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		MockPropertiesServlet(t, w, r)
	}))

	return server
}

func MockPropertiesServlet(t *testing.T, w http.ResponseWriter, r *http.Request) {
	assert.NotEmpty(t, r.Header.Get("ClientApiVersion"))
	if !strings.Contains(r.URL.Path, "/cps/") {
		t.Errorf("Expected to request '/cps/', got: %s", r.URL.Path)
	}
	if r.Header.Get("Accept") != "application/json" {
		t.Errorf("Expected Accept: application/json header, got: %s", r.Header.Get("Accept"))
	}
	w.Header().Set("Content-Type", "application/json")

	var statusCode int
	var namespaceProperties string
	splitUrl := strings.Split(r.URL.Path, "/")
	namespace := splitUrl[2]

	//cps/ns/properties
	statusCode, namespaceProperties = CheckNamespace(namespace)
	if statusCode == 200 {
		if len(splitUrl) == 4 {
			query := r.URL.Query()
			//cps/ns/properties?prefix&suffix&infix
			if query.Has("prefix") || query.Has("suffix") || query.Has("infix") {
				queryValues := r.URL.Query()
				prefixParameter := queryValues.Get("prefix")
				suffixParameter := queryValues.Get("suffix")
				infixParameter := queryValues.Get("infix")

				namespaceProperties = checkQueryParameters(prefixParameter, suffixParameter, infixParameter)
			}
			//cps/ns/properties/propertyname
		} else if len(splitUrl) == 5 {
			propertyName := splitUrl[4]
			namespaceProperties, statusCode = CheckName(propertyName)
		}
	}
	w.WriteHeader(statusCode)
	w.Write([]byte(namespaceProperties))
}

func CheckName(name string) (string, int) {
	statusCode := 200
	namespaceProperties := "[]"
	switch name {
	case "property0":
		namespaceProperties = `[
		{
			"apiVersion": "myApiVersion",
			"kind": "GalasaProperty",
			"metadata": {
				"namespace": "validnamespace",
				"name": "property0"
			},
			"data":{
				"value": "value0"
			}
		}
	]`
	case "invalidName": //property name does not exist
		namespaceProperties = `[]`
	case "emptyValueName": //property name does not exist
		namespaceProperties = `[
			{
				"apiVersion": "myApiVersion",
				"kind": "GalasaProperty",
				"metadata": {
					"namespace": "validnamespace",
					"name": "emptyValueName"
				},
				"data": {
					"value": ""
				}
			}
		]`
	}
	return namespaceProperties, statusCode
}

func checkQueryParameters(prefixParameter string, suffixParameter string, infixParameter string) string {
	var namespaceProperties = ""
	//there are properties in the namespace that match a prefix and/or suffix
	if prefixParameter == "aPrefix" && suffixParameter == "aSuffix" {

		if infixParameter == "anInfix" { //for a single infix
			namespaceProperties = `[
				{
					"apiVersion": "myApiVersion",
					"kind": "GalasaProperty",
					"metadata": {
						"namespace": "validnamespace",
						"name": "aPrefix.anInfix.property.aSuffix"
					},
				    "data": {
						"value": "prefixSuffixInfixVal"
					}
				}
			]`

		} else { //no infix
			namespaceProperties = `[
				{
					"apiVersion": "myApiVersion",
					"kind": "GalasaProperty",
					"metadata": {
						"namespace": "validnamespace",
						"name": "aPrefix.property.aSuffix"
					},
				    "data": {
						"value": "prefixSuffixVal"
					}
				}
			]`

		}

	} else if suffixParameter == "aSuffix" {

		if infixParameter == "anInfix" {
			namespaceProperties = `[
				{
					"apiVersion": "myApiVersion",
					"kind": "GalasaProperty",
					"metadata": {
						"namespace": "validnamespace",
						"name": "property.anInfix.aSuffix"
					},
				    "data": {
						"value": "suffixInfixVal"
					}
				}
			]`

		} else {
			namespaceProperties = `[
				{
					"apiVersion": "myApiVersion",
					"kind": "GalasaProperty",
					"metadata": {
						"namespace": "validnamespace",
						"name": "property.aSuffix"
					},
				    "data": {
						"value": "suffixVal"
					}
				}
			]`

		}

	} else if prefixParameter == "aPrefix" {

		if infixParameter == "anInfix" {
			namespaceProperties = `[
				{
					"apiVersion": "myApiVersion",
					"kind": "GalasaProperty",
					"metadata": {
						"namespace": "validnamespace",
						"name": "aPrefix.anInfix.property"
					},
				    "data": {
						"value": "prefixInfixVal"
					}
				}
			]`
		} else {
			namespaceProperties = `[
				{
					"apiVersion": "myApiVersion",
					"kind": "GalasaProperty",
					"metadata": {
						"namespace": "validnamespace",
						"name": "aPrefix.property"
					},
				    "data": {
						"value": "prefixVal"
					}
				}
			]`
		}

	}
	//there are NO properties in the namespace that match the prefix and/or suffix
	if prefixParameter == "noMatchingPrefix" && suffixParameter == "noMatchingSuffix" {
		namespaceProperties = `[]`
	} else if suffixParameter == "noMatchingSuffix" {
		namespaceProperties = `[]`
	} else if prefixParameter == "noMatchingPrefix" {
		namespaceProperties = `[]`
	}

	//If only the infix parameter is supplied
	if prefixParameter == "" && suffixParameter == "" {
		if infixParameter == "anInfix" {
			namespaceProperties = `[
				{
					"apiVersion": "myApiVersion",
					"kind": "GalasaProperty",
					"metadata": {
						"namespace": "validnamespace",
						"name": "extra.anInfix.extra"
					},
				    "data": {
						"value": "infixVal"
					}
				}
			]`
		} else if infixParameter == "noMatchingInfix" { //singular or multiple infixes that do not match
			namespaceProperties = `[]`
		}
	}

	return namespaceProperties
}

func CheckNamespace(namespace string) (int, string) {
	statusCode := 200
	namespaceProperties := "[]"

	switch namespace {
	case "validnamespace":
		namespaceProperties = `[
			{
				"apiVersion": "myApiVersion",
				"kind": "GalasaProperty",
				"metadata": {
					"namespace": "validnamespace",
					"name": "property0"
				},
				"data": {
					"value": "value0"
				}
			},
			{
				"apiVersion": "myApiVersion",
				"kind": "GalasaProperty",
				"metadata": {
					"namespace": "validnamespace",
					"name": "property1"
				},
				"data": {
					"value": "value1"
				}
			},
			{
				"apiVersion": "myApiVersion",
				"kind": "GalasaProperty",
				"metadata": {
					"namespace": "validnamespace",
					"name": "property2"
				},
				"data": {
					"value": "value2"
				}
			},
			{
				"apiVersion": "myApiVersion",
				"kind": "GalasaProperty",
				"metadata": {
					"namespace": "validnamespace",
					"name": "property3"
				},
				"data": {
					"value": "value3"
				}
			}
		]`
	case "invalidnamespace":
		statusCode = 404
		namespaceProperties = `{
			"error_code": 5016,
			"error_message": "GAL5016E: Error occured when trying to access namespace 'invalidnamespace'. The Namespace provided is invalid."
		}`
	}

	return statusCode, namespaceProperties
}

// -------------------
// TESTS

func TestInvalidNamepsaceReturnsError(t *testing.T) {
	//Given...
	namespace := "invalidnamespace"
	name := ""
	prefix := ""
	suffix := ""
	infix := ""
	propertiesOutputFormat := "summary"

	server := NewPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiClient := api.InitialiseAPI(apiServerUrl)

	//When
	err := GetProperties(namespace, name, prefix, suffix, infix, apiClient, propertiesOutputFormat, mockConsole)

	//Then
	assert.Error(t, err)
	assert.ErrorContains(t, err, "GAL1096E")
}

func TestValidNamespaceReturnsOk(t *testing.T) {
	//Given...
	namespace := "validnamespace"
	name := ""
	prefix := ""
	suffix := ""
	infix := ""
	propertiesOutputFormat := "summary"

	server := NewPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiClient := api.InitialiseAPI(apiServerUrl)

	expectedOutput := `namespace      name      value
validnamespace property0 value0
validnamespace property1 value1
validnamespace property2 value2
validnamespace property3 value3

Total:4
`

	//When
	err := GetProperties(namespace, name, prefix, suffix, infix, apiClient, propertiesOutputFormat, mockConsole)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, mockConsole.ReadText())
}

func TestEmptyNamespaceReturnsEmpty(t *testing.T) {
	//Given...
	namespace := "emptynamespace"
	name := ""
	prefix := ""
	suffix := ""
	infix := ""
	propertiesOutputFormat := "summary"

	server := NewPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiClient := api.InitialiseAPI(apiServerUrl)

	expectedOutput := `Total:0
`
	//When
	err := GetProperties(namespace, name, prefix, suffix, infix, apiClient, propertiesOutputFormat, mockConsole)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, mockConsole.ReadText())
}

func TestValidNamespaceAndPrefixReturnsOk(t *testing.T) {
	//Given...
	namespace := "validnamespace"
	name := ""
	prefix := "aPrefix"
	suffix := ""
	infix := ""
	propertiesOutputFormat := "summary"

	server := NewPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiClient := api.InitialiseAPI(apiServerUrl)

	expectedOutput := `namespace      name             value
validnamespace aPrefix.property prefixVal

Total:1
`

	//When
	err := GetProperties(namespace, name, prefix, suffix, infix, apiClient, propertiesOutputFormat, mockConsole)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, mockConsole.ReadText())
}

func TestValidNamespaceAndSuffixReturnsOk(t *testing.T) {
	//Given...
	namespace := "validnamespace"
	name := ""
	prefix := ""
	suffix := "aSuffix"
	infix := ""
	propertiesOutputFormat := "summary"

	server := NewPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiClient := api.InitialiseAPI(apiServerUrl)

	expectedOutput := `namespace      name             value
validnamespace property.aSuffix suffixVal

Total:1
`

	//When
	err := GetProperties(namespace, name, prefix, suffix, infix, apiClient, propertiesOutputFormat, mockConsole)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, mockConsole.ReadText())
}

func TestValidNamespaceWithMatchingPrefixAndSuffixReturnsOk(t *testing.T) {
	//Given...
	namespace := "validnamespace"
	name := ""
	prefix := "aPrefix"
	suffix := "aSuffix"
	infix := ""
	propertiesOutputFormat := "summary"

	server := NewPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiClient := api.InitialiseAPI(apiServerUrl)

	expectedOutput := `namespace      name                     value
validnamespace aPrefix.property.aSuffix prefixSuffixVal

Total:1
`

	//When
	err := GetProperties(namespace, name, prefix, suffix, infix, apiClient, propertiesOutputFormat, mockConsole)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, mockConsole.ReadText())
}

func TestValidNamespaceWithNoMatchingPrefixAndSuffixReturnsEmpty(t *testing.T) {
	//Given...
	namespace := "validnamespace"
	name := ""
	prefix := "noMatchingPrefix"
	suffix := "noMatchingSuffix"
	infix := ""
	propertiesOutputFormat := "summary"

	server := NewPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiClient := api.InitialiseAPI(apiServerUrl)

	expectedOutput := `Total:0
`

	//When
	err := GetProperties(namespace, name, prefix, suffix, infix, apiClient, propertiesOutputFormat, mockConsole)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, mockConsole.ReadText())
}

func TestValidNamespaceAndNoMatchingPrefixReturnsEmpty(t *testing.T) {
	//Given...
	namespace := "validnamespace"
	name := ""
	prefix := "noMatchingPrefix"
	suffix := ""
	infix := ""
	propertiesOutputFormat := "summary"

	server := NewPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiClient := api.InitialiseAPI(apiServerUrl)

	expectedOutput := `Total:0
`

	//When
	err := GetProperties(namespace, name, prefix, suffix, infix, apiClient, propertiesOutputFormat, mockConsole)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, mockConsole.ReadText())
}

func TestValidNamespaceAndNoMatchingSuffixReturnsEmpty(t *testing.T) {
	//Given...
	namespace := "validnamespace"
	name := ""
	prefix := ""
	suffix := "noMatchingSuffix"
	infix := ""
	propertiesOutputFormat := "summary"

	server := NewPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiClient := api.InitialiseAPI(apiServerUrl)

	expectedOutput := `Total:0
`

	//When
	err := GetProperties(namespace, name, prefix, suffix, infix, apiClient, propertiesOutputFormat, mockConsole)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, mockConsole.ReadText())
}

func TestValidNamespaceWithMatchingInfixReturnsOk(t *testing.T) {
	//Given...
	namespace := "validnamespace"
	name := ""
	prefix := ""
	suffix := ""
	infix := "anInfix"
	propertiesOutputFormat := "summary"

	server := NewPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiClient := api.InitialiseAPI(apiServerUrl)

	expectedOutput := `namespace      name                value
validnamespace extra.anInfix.extra infixVal

Total:1
`
	//When
	err := GetProperties(namespace, name, prefix, suffix, infix, apiClient, propertiesOutputFormat, mockConsole)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, mockConsole.ReadText())
}

func TestValidNamespaceWithMatchingInfixesReturnsOk(t *testing.T) {
	//Given...
	namespace := "validnamespace"
	name := ""
	prefix := ""
	suffix := ""
	infix := "anInfix"
	propertiesOutputFormat := "summary"

	server := NewPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiClient := api.InitialiseAPI(apiServerUrl)

	expectedOutput := `namespace      name                value
validnamespace extra.anInfix.extra infixVal

Total:1
`
	//When
	err := GetProperties(namespace, name, prefix, suffix, infix, apiClient, propertiesOutputFormat, mockConsole)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, mockConsole.ReadText())
}

func TestValidNamespaceWithNoMatchingInfixReturnsEmpty(t *testing.T) {
	//Given...
	namespace := "validnamespace"
	name := ""
	prefix := ""
	suffix := ""
	infix := "noMatchingInfix"
	propertiesOutputFormat := "summary"

	server := NewPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiClient := api.InitialiseAPI(apiServerUrl)

	expectedOutput := `Total:0
`
	//When
	err := GetProperties(namespace, name, prefix, suffix, infix, apiClient, propertiesOutputFormat, mockConsole)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, mockConsole.ReadText())
}

func TestValidNamespaceWithMatchingPrefixAndInfixReturnsOk(t *testing.T) {
	//Given...
	namespace := "validnamespace"
	name := ""
	prefix := "aPrefix"
	suffix := ""
	infix := "anInfix"
	propertiesOutputFormat := "summary"

	server := NewPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiClient := api.InitialiseAPI(apiServerUrl)

	expectedOutput := `namespace      name                     value
validnamespace aPrefix.anInfix.property prefixInfixVal

Total:1
`

	//When
	err := GetProperties(namespace, name, prefix, suffix, infix, apiClient, propertiesOutputFormat, mockConsole)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, mockConsole.ReadText())
}

func TestValidNamespaceWithMatchingSuffixAndInfixReturnsOk(t *testing.T) {
	//Given...
	namespace := "validnamespace"
	name := ""
	prefix := ""
	suffix := "aSuffix"
	infix := "anInfix"
	propertiesOutputFormat := "summary"

	server := NewPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiClient := api.InitialiseAPI(apiServerUrl)

	expectedOutput := `namespace      name                     value
validnamespace property.anInfix.aSuffix suffixInfixVal

Total:1
`

	//When
	err := GetProperties(namespace, name, prefix, suffix, infix, apiClient, propertiesOutputFormat, mockConsole)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, mockConsole.ReadText())
}

func TestValidNamespaceWithMatchingPrefixAndSuffixAndInfixReturnsOk(t *testing.T) {
	//Given...
	namespace := "validnamespace"
	name := ""
	prefix := "aPrefix"
	suffix := "aSuffix"
	infix := "anInfix"
	propertiesOutputFormat := "summary"

	server := NewPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiClient := api.InitialiseAPI(apiServerUrl)

	expectedOutput := `namespace      name                             value
validnamespace aPrefix.anInfix.property.aSuffix prefixSuffixInfixVal

Total:1
`

	//When
	err := GetProperties(namespace, name, prefix, suffix, infix, apiClient, propertiesOutputFormat, mockConsole)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, mockConsole.ReadText())
}

func TestValidNamespaceWithValidNameReturnsOk(t *testing.T) {
	//Given...
	namespace := "validnamespace"
	name := "property0"
	prefix := ""
	suffix := ""
	infix := ""
	propertiesOutputFormat := "summary"

	server := NewPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiClient := api.InitialiseAPI(apiServerUrl)

	expectedOutput := `namespace      name      value
validnamespace property0 value0

Total:1
`
	//When
	err := GetProperties(namespace, name, prefix, suffix, infix, apiClient, propertiesOutputFormat, mockConsole)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, mockConsole.ReadText())
}

func TestValidNameWithEmptyValueValidNameReturnsOk(t *testing.T) {
	//Given...
	namespace := "validnamespace"
	name := "emptyValueName"
	prefix := ""
	suffix := ""
	infix := ""
	propertiesOutputFormat := "summary"

	server := NewPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiClient := api.InitialiseAPI(apiServerUrl)

	expectedOutput := `namespace      name           value
validnamespace emptyValueName 

Total:1
`

	//When
	err := GetProperties(namespace, name, prefix, suffix, infix, apiClient, propertiesOutputFormat, mockConsole)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, mockConsole.ReadText())
}

func TestInvalidPropertyNameReturnsEmpty(t *testing.T) {
	//Given...
	namespace := "validnamespace"
	name := "invalidName"
	prefix := ""
	suffix := ""
	infix := ""
	propertiesOutputFormat := "summary"

	server := NewPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiClient := api.InitialiseAPI(apiServerUrl)

	expectedOutput := `Total:0
`

	//When
	err := GetProperties(namespace, name, prefix, suffix, infix, apiClient, propertiesOutputFormat, mockConsole)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, mockConsole.ReadText())
}

func TestValidNamespaceRawFormatReturnsOk(t *testing.T) {
	//Given...
	namespace := "validnamespace"
	name := ""
	prefix := ""
	suffix := ""
	infix := ""
	propertiesOutputFormat := "raw"

	server := NewPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiClient := api.InitialiseAPI(apiServerUrl)

	expectedOutput := `validnamespace|property0|value0
validnamespace|property1|value1
validnamespace|property2|value2
validnamespace|property3|value3
`
	//When
	err := GetProperties(namespace, name, prefix, suffix, infix, apiClient, propertiesOutputFormat, mockConsole)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, mockConsole.ReadText())
}

func TestEmptyNamespaceRawFormatReturnsOk(t *testing.T) {
	//Given...
	namespace := "emptynamespace"
	name := ""
	prefix := ""
	suffix := ""
	infix := ""
	propertiesOutputFormat := "raw"

	server := NewPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiClient := api.InitialiseAPI(apiServerUrl)

	expectedOutput := ``
	//When
	err := GetProperties(namespace, name, prefix, suffix, infix, apiClient, propertiesOutputFormat, mockConsole)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, mockConsole.ReadText())
}

func TestValidNamespaceYamlFormatReturnsOk(t *testing.T) {
	//Given...
	namespace := "validnamespace"
	name := ""
	prefix := ""
	suffix := ""
	infix := ""
	propertiesOutputFormat := "yaml"

	server := NewPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiClient := api.InitialiseAPI(apiServerUrl)

	expectedOutput := `apiVersion: myApiVersion
kind: GalasaProperty
metadata:
    namespace: validnamespace
    name: property0
data:
    value: value0
---
apiVersion: myApiVersion
kind: GalasaProperty
metadata:
    namespace: validnamespace
    name: property1
data:
    value: value1
---
apiVersion: myApiVersion
kind: GalasaProperty
metadata:
    namespace: validnamespace
    name: property2
data:
    value: value2
---
apiVersion: myApiVersion
kind: GalasaProperty
metadata:
    namespace: validnamespace
    name: property3
data:
    value: value3
`
	//When
	err := GetProperties(namespace, name, prefix, suffix, infix, apiClient, propertiesOutputFormat, mockConsole)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, mockConsole.ReadText())
}

func TestEmptyNamespaceYamlFormatReturnsOk(t *testing.T) {
	//Given...
	namespace := "emptynamespace"
	name := ""
	prefix := ""
	suffix := ""
	infix := ""
	propertiesOutputFormat := "yaml"

	server := NewPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiClient := api.InitialiseAPI(apiServerUrl)

	expectedOutput := ``
	//When
	err := GetProperties(namespace, name, prefix, suffix, infix, apiClient, propertiesOutputFormat, mockConsole)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, mockConsole.ReadText())
}

func TestCreateFormattersSummaryReturnsOk(t *testing.T) {
	//Given
	hasYamlFormat := false

	//When
	validFormatters := CreateFormatters(hasYamlFormat)
	summary, err := validateOutputFormatFlagValue("summary", validFormatters)

	//Then
	assert.Nil(t, err)
	assert.NotNil(t, validFormatters)
	assert.NotNil(t, summary)
}

func TestCreateFormattersRawReturnsOk(t *testing.T) {
	//Given
	hasYamlFormat := false

	//When
	validFormatters := CreateFormatters(hasYamlFormat)
	raw, err := validateOutputFormatFlagValue("raw", validFormatters)

	//Then
	assert.Nil(t, err)
	assert.NotNil(t, validFormatters)
	assert.NotNil(t, raw)
}

func TestCreateFormattersHasYamlReturnsOk(t *testing.T) {
	//Given
	hasYamlFormat := true

	//When
	validFormatters := CreateFormatters(hasYamlFormat)
	yaml, err := validateOutputFormatFlagValue("yaml", validFormatters)

	//Then
	assert.Nil(t, err)
	assert.NotNil(t, validFormatters)
	assert.NotNil(t, yaml)
}

func TestCreateFormattersNoYamlReturnsOk(t *testing.T) {
	//Given
	hasYamlFormat := false

	//When
	validFormatters := CreateFormatters(hasYamlFormat)
	_, err := validateOutputFormatFlagValue("yaml", validFormatters)

	//Then
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1067E")
}

func TestInvalidNamespaceFormatWithCapitalLettersReturnsError(t *testing.T) {
	//Given...
	namespace := "invalidNamespaceFormat"
	name := ""
	prefix := ""
	suffix := ""
	infix := ""
	propertiesOutputFormat := "raw"

	server := NewPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiClient := api.InitialiseAPI(apiServerUrl)

	expectedOutput := ``
	//When
	err := GetProperties(namespace, name, prefix, suffix, infix, apiClient, propertiesOutputFormat, mockConsole)

	//Then
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1140E")
	assert.Equal(t, expectedOutput, mockConsole.ReadText())
}

func TestValidateInfixesWithCommaSeparatedMultipleValidValuesReturnsOk(t *testing.T) {
	//Given
	infix := "voilin,cello,clarinet,guitar"

	//When...
	err := ValidateInfixes(infix)

	//Then....
	assert.Nil(t, err)
}

func TestValidateInfixesWithOneValidValueReturnsOk(t *testing.T) {
	//Given
	infix := "voilin"

	//When...
	err := ValidateInfixes(infix)

	//Then....
	assert.Nil(t, err)
}

func TestValidateInfixesWithCommaSeparateInvalidValuesWithSpaceAfterWordReturnsError(t *testing.T) {
	//Given
	infix := "cello,voilin ,clarinet,guitar"

	//When...
	err := ValidateInfixes(infix)

	//Then....
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1142E")
}

func TestValidateInfixesWithCommaSeparatedInvalidValuesWithSpaceBeforeWordReturnsError(t *testing.T) {
	//Given
	infix := "cello, clarinet,guitar"

	//When...
	err := ValidateInfixes(infix)

	//Then....
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1142E")
}

func TestValidateInfixesWithCommaSeparatedOneInvalidValuesWithSpaceReturnsError(t *testing.T) {
	//Given
	infix := "cello "

	//When...
	err := ValidateInfixes(infix)

	//Then....
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1142E")
}
