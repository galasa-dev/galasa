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
	"github.com/stretchr/testify/assert"
)

// MockServlet
func newSetPropertiesServletMock(t *testing.T) *httptest.Server {

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		mockSetPropertiesServlet(t, w, r)
	}))

	return server
}

func mockSetPropertiesServlet(t *testing.T, w http.ResponseWriter, r *http.Request) {
	assert.NotEmpty(t, r.Header.Get("ClientApiVersion"))
	if !strings.Contains(r.URL.Path, "/cps/") {
		t.Errorf("Expected to request '/cps/', got: %s", r.URL.Path)
	}
	if r.Header.Get("Accept") != "application/json" {
		t.Errorf("Expected Accept: application/json header, got: %s", r.Header.Get("Accept"))
	}
	w.Header().Set("Content-Type", "application/json")

	var statusCode int
	var response string
	splitUrl := strings.Split(r.URL.Path, "/")
	namespace := splitUrl[2]

	statusCode, response = CheckNamespace(namespace)
	if namespace == "validnamespace" {
		if len(splitUrl) == 5 {
			propertyName := splitUrl[4]
			//UPDATE -> cps/ns/properties/name
			statusCode, response = updateProperty(propertyName)
		} else if len(splitUrl) == 4 {
			statusCode, response = createProperty()
		}
	}

	w.WriteHeader(statusCode)
	w.Write([]byte(response))
}

func createProperty() (int, string) {
	statusCode := 201
	response := ""
	return statusCode, response
}

func updateProperty(propertyName string) (int, string) {
	statusCode := 200
	response := ""

	if propertyName == "invalidName" {
		statusCode = 404
		response = `{
			"error_code": 5018,
			"error_message": "GAL5018E: Error occured when trying to access property 'propertyName'. The property name provided is invalid."
			}`
	} else if propertyName == "newName" { //property should be created
		statusCode = 404
		response = `{
			"error_code": 5017,
			"error_message": "GAL5017E: Error occured when trying to access property 'newName'. The property name provided is invalid."
			}`
	}

	return statusCode, response
}

// --------
// CREATING
// bad value feturn error
func TestCreatePropertyWithValidNamespaceReturnsOk(t *testing.T) {
	//Given...
	namespace := "validnamespace"
	name := "newName"
	value := "newValue"

	server := newSetPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	apiClient := api.InitialiseAPI(apiServerUrl)

	//When
	err := SetProperty(namespace, name, value, apiClient)

	//Then
	assert.Nil(t, err)
}

func TestCreatePropertyWithValidNamespaceWithAtSymbolReturnsOk(t *testing.T) {
	//Given...
	namespace := "validnamespace"
	name := "Galasadelivery@ibm.com"
	value := "someValue"

	server := newSetPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	apiClient := api.InitialiseAPI(apiServerUrl)

	//When
	err := SetProperty(namespace, name, value, apiClient)

	//Then
	assert.Nil(t, err)
}

func TestUpdatePropertyWithInvalidNamespaceAndInvalidPropertyNameReturnsError(t *testing.T) {
	//Given...
	namespace := "invalidnamespace"
	name := "newName"
	value := "newValue"

	server := newSetPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	apiClient := api.InitialiseAPI(apiServerUrl)

	//When
	err := SetProperty(namespace, name, value, apiClient)

	//Then
	assert.Error(t, err)
	assert.ErrorContains(t, err, "GAL1100E:")
}

// --------
// UPDATING
func TestUpdatePropertyWithValidNamespaceAndVaidNameValueReturnsOk(t *testing.T) {
	//Given...
	namespace := "validnamespace"
	name := "validName"
	value := "updatedValue"

	server := newSetPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	apiClient := api.InitialiseAPI(apiServerUrl)

	//When
	err := SetProperty(namespace, name, value, apiClient)

	//Then
	assert.Nil(t, err)
}

func TestUpdatePropertyWithValidNamespaceAndVaidNameValueWithAtSymbolReturnsOk(t *testing.T) {
	//Given...
	namespace := "validnamespace"
	name := "Galasadelivery@ibm.com"
	value := "updatedValue"

	server := newSetPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	apiClient := api.InitialiseAPI(apiServerUrl)

	//When
	err := SetProperty(namespace, name, value, apiClient)

	//Then
	assert.Nil(t, err)
}

func TestUpdatePropertyWithInvalidNamespaceAndValidNameReturnsError(t *testing.T) {
	//Given...
	namespace := "invalidnamespace"
	name := "validName"
	value := "updatedValue"

	server := newSetPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	apiClient := api.InitialiseAPI(apiServerUrl)

	//When
	err := SetProperty(namespace, name, value, apiClient)

	//Then
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1100E")
}

func TestSetNoNamespaceReturnsError(t *testing.T) {
	//Given...
	namespace := ""
	name := "invalidName"
	value := "newValue"

	server := newSetPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	apiClient := api.InitialiseAPI(apiServerUrl)

	//When
	err := SetProperty(namespace, name, value, apiClient)

	//Then
	assert.Error(t, err)
	assert.ErrorContains(t, err, "GAL1101E:")
}

func TestSetNoNameReturnsError(t *testing.T) {
	//Given...
	namespace := "namespace"
	name := ""
	value := "newValue"

	server := newSetPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	apiClient := api.InitialiseAPI(apiServerUrl)

	//When
	err := SetProperty(namespace, name, value, apiClient)

	//Then
	assert.Error(t, err)
	assert.ErrorContains(t, err, "GAL1102E:")
}

func TestCreateGalasaPropertyReturnsOk(t *testing.T) {
	//Given
	namesapce := "framework"
	name := "name"
	value := "value"

	//When
	property := createGalasaProperty(namesapce, name, value)

	//Then
	assert.Equal(t, property.GetApiVersion(), APIVERSION)
	assert.Equal(t, property.GetKind(), KIND)
	assert.Equal(t, property.Metadata.GetNamespace(), namesapce)
	assert.Equal(t, property.Metadata.GetName(), name)
	assert.Equal(t, property.Data.GetValue(), value)
}

func TestInvalidNamespaceFormatWithStartingNumReturnsError(t *testing.T) {
	//Given...
	namespace := "7invalidnamespaceformat"
	name := "validName"
	value := "updatedValue"

	server := newSetPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	apiClient := api.InitialiseAPI(apiServerUrl)

	//When
	err := SetProperty(namespace, name, value, apiClient)

	//Then
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1140E")
}
