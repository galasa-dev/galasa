/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package properties

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

func NewPropertiesNamespaceServletMock(t *testing.T, state string) *httptest.Server {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		mockPropertiesNamespaceServlet(t, w, r, state)
	}))
	return server
}

func mockPropertiesNamespaceServlet(t *testing.T, writer http.ResponseWriter, request *http.Request, state string) {
	assert.NotEmpty(t, request.Header.Get("ClientApiVersion"))
	writer.Header().Set("Content-Type", "application/json")
	var statusCode int
	var body string
	statusCode = 200
	if state == "populated" {
		body = `[{"name" : "framework", "propertiesUrl"  : "/cps/framework/properties","type" : "normal"},` +
			`{"name" : "secure", "propertiesUrl"  : "/cps/secure/properties","type" : "secure"},` +
			`{"name" : "anamespace",	"propertiesUrl"  : "/cps/anamespace/properties", "type" : "normal"}]`
	} else if state == "empty" {
		body = "[]"
	} else {
		statusCode = 500
		body = `{"error_code": 5000,"error_message": "GAL5000E: Error occured when trying to access the endpoint. Report the problem to your Galasa Ecosystem owner."}`
	}
	writer.WriteHeader(statusCode)
	writer.Write([]byte(body))
}

func TestMultipleNamespacesPathReturnsOk(t *testing.T) {
	//Given...
	namespaceOutputFormat := "summary"
	serverState := "populated"
	server := NewPropertiesNamespaceServletMock(t, serverState)
	apiClient := api.InitialiseAPI(server.URL)
	defer server.Close()

	console := utils.NewMockConsole()
	expectedOutput := "namespace  type\n" +
		"framework  normal\n" +
		"secure     secure\n" +
		"anamespace normal\n" +
		"\n" +
		"Total:3\n"

	//When
	err := GetPropertiesNamespaces(apiClient, namespaceOutputFormat, console)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, console.ReadText())
}

func TestEmptyNamespacesPathReturnsOk(t *testing.T) {
	//Given...
	namespaceOutputFormat := "summary"
	serverState := "empty"
	server := NewPropertiesNamespaceServletMock(t, serverState)
	apiClient := api.InitialiseAPI(server.URL)
	defer server.Close()

	console := utils.NewMockConsole()
	expectedOutput := "Total:0\n"

	//When
	err := GetPropertiesNamespaces(apiClient, namespaceOutputFormat, console)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, console.ReadText())
}

func TestInvalidPathReturnsError(t *testing.T) {
	//Given...
	namespaceOutputFormat := "summary"
	serverState := ""
	server := NewPropertiesNamespaceServletMock(t, serverState)
	apiClient := api.InitialiseAPI(server.URL)
	defer server.Close()

	console := utils.NewMockConsole()

	//When
	err := GetPropertiesNamespaces(apiClient, namespaceOutputFormat, console)

	//Then
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1103E")
}

func TestMultipleNamespacesRawFormatReturnsOk(t *testing.T) {
	//Given...
	namespaceOutputFormat := "raw"
	serverState := "populated"
	server := NewPropertiesNamespaceServletMock(t, serverState)
	apiClient := api.InitialiseAPI(server.URL)
	defer server.Close()

	console := utils.NewMockConsole()
	expectedOutput := `framework|normal
secure|secure
anamespace|normal
`

	//When
	err := GetPropertiesNamespaces(apiClient, namespaceOutputFormat, console)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, console.ReadText())
}

func TestEmptyNamespacesRawFormatReturnsOk(t *testing.T) {
	//Given...
	namespaceOutputFormat := "raw"
	serverState := "empty"
	server := NewPropertiesNamespaceServletMock(t, serverState)
	apiClient := api.InitialiseAPI(server.URL)
	defer server.Close()

	console := utils.NewMockConsole()
	expectedOutput := ``

	//When
	err := GetPropertiesNamespaces(apiClient, namespaceOutputFormat, console)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, console.ReadText())
}
