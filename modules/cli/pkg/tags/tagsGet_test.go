/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package tags

import (
	"net/http"
	"strconv"
	"testing"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

func TestGetTagWithInvalidNameReturnsError(t *testing.T) {
	// Given...
	tagName := string(rune(300)) + "is not latin1"
	outputFormat := "summary"

	interactions := []utils.HttpInteraction{}
	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

    apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)
	console := utils.NewMockConsole()

	// When...
	err := GetTag(tagName, outputFormat, commsClient, console, mockByteReader)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "Invalid tag name provided.")
}

func TestGetTagsWithUnknownOutputFormatReturnsError(t *testing.T) {
	// Given...
	tagName := ""
	outputFormat := "unknown"

	interactions := []utils.HttpInteraction{}
	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

    apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)
	console := utils.NewMockConsole()

	// When...
	err := GetTag(tagName, outputFormat, commsClient, console, mockByteReader)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "Unsupported value 'unknown' for parameter --format")
	assert.ErrorContains(t, err, "summary")
	assert.ErrorContains(t, err, "yaml")
}

func TestGetTagsSendsCorrectRequest(t *testing.T) {
	// Given...
	tagName := ""
	outputFormat := "summary"

	getTagInteraction := utils.NewHttpInteraction("/tags", http.MethodGet)
	getTagInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {

		body := `[{
			"apiVersion": "v1alpha1",
			"kind": "GalasaTag",
			"metadata": {
				"id": "tag123",
				"name": "mytag",
				"description": "my core regression tests"
			},
			"data": {
				"description": "my tag's description",
				"priority": 1234
			}
		}]`
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusCreated)
		writer.Write([]byte(body))
	}

	interactions := []utils.HttpInteraction{
		getTagInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

    apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)
	console := utils.NewMockConsole()

	// When...
	err := GetTag(tagName, outputFormat, commsClient, console, mockByteReader)

	// Then...
    expectedOutput :=
`name  priority description
mytag 1234     my core regression tests

Total:1
`
    assert.Nil(t, err, "GetTags returned an unexpected error")
    assert.Equal(t, expectedOutput, console.ReadText())
}

func TestGetTagsCanOutputTagsInYamlFormat(t *testing.T) {
	// Given...
	tagName := ""
	outputFormat := "yaml"

	getTagInteraction := utils.NewHttpInteraction("/tags", http.MethodGet)
	getTagInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {

		body := `[{
			"apiVersion": "v1alpha1",
			"kind": "GalasaTag",
			"metadata": {
				"id": "tag123",
				"name": "mytag",
				"description": "my core regression tests"
			},
			"data": {
				"description": "my tag's description",
				"priority": 1234
			}
		}]`
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusCreated)
		writer.Write([]byte(body))
	}

	interactions := []utils.HttpInteraction{
		getTagInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

    apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)
	console := utils.NewMockConsole()

	// When...
	err := GetTag(tagName, outputFormat, commsClient, console, mockByteReader)

	// Then...
    expectedOutput :=
`apiVersion: v1alpha1
kind: GalasaTag
metadata:
    id: tag123
    name: mytag
    description: my core regression tests
data:
    priority: 1234
`
    assert.Nil(t, err, "GetTags returned an unexpected error")
    assert.Equal(t, expectedOutput, console.ReadText())
}

func TestGetTagsWithNameSendsCorrectRequest(t *testing.T) {
	// Given...
	tagName := "mytag"
	outputFormat := "summary"

	getTagInteraction := utils.NewHttpInteraction("/tags", http.MethodGet)
	getTagInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		requestedName := req.URL.Query().Get("name")
		assert.Equal(t, tagName, requestedName)

		body := `[{
			"apiVersion": "v1alpha1",
			"kind": "GalasaTag",
			"metadata": {
				"id": "tag123",
				"name": "mytag",
				"description": "my core regression tests"
			},
			"data": {
				"description": "my tag's description",
				"priority": 1234
			}
		}]`
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusCreated)
		writer.Write([]byte(body))
	}

	interactions := []utils.HttpInteraction{
		getTagInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

    apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)
	console := utils.NewMockConsole()

	// When...
	err := GetTag(tagName, outputFormat, commsClient, console, mockByteReader)

	// Then...
    expectedOutput :=
`name  priority description
mytag 1234     my core regression tests

Total:1
`
    assert.Nil(t, err, "GetTags returned an unexpected error")
    assert.Equal(t, expectedOutput, console.ReadText())
}

func TestGetTagWithServerFailureGivesCorrectMessage(t *testing.T) {
	// Given...
	tagName := "mytag"
	outputFormat := "summary"

	getTagInteraction := utils.NewHttpInteraction("/tags", http.MethodGet)
	getTagInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {

        writer.Header().Set("Content-Type", "application/json")
        writer.WriteHeader(http.StatusInternalServerError)
        writer.Write([]byte(`{ "error_message": "This is an error message from the server!" }`))
	}

	interactions := []utils.HttpInteraction{
		getTagInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

    apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)
	console := utils.NewMockConsole()

	// When...
	err := GetTag(tagName, outputFormat, commsClient, console, mockByteReader)

	// Then...
	assert.NotNil(t, err)
    assert.ErrorContains(t, err, strconv.Itoa(http.StatusInternalServerError))
    assert.ErrorContains(t, err, "GAL1270E")
    assert.ErrorContains(t, err, "Error details from the server are")
}
