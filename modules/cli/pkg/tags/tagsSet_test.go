/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package tags

import (
	"encoding/base64"
	"encoding/json"
	"io"
	"net/http"
	"strconv"
	"testing"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

func readTagRequestBody(req *http.Request) galasaapi.TagSetRequest {
    var tagSetRequest galasaapi.TagSetRequest
    requestBodyBytes, _ := io.ReadAll(req.Body)
    defer req.Body.Close()

    _ = json.Unmarshal(requestBodyBytes, &tagSetRequest)
    return tagSetRequest
}



func TestSetTagWithInvalidNameReturnsError(t *testing.T) {
	// Given...
	tagName := ""
	description := ""
	priority := DEFAULT_EMPTY_PRIORITY

	interactions := []utils.HttpInteraction{}
	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

    apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
    apiClient := api.InitialiseAPI(apiServerUrl)

	// When...
	err := SetTag(tagName, description, priority, apiClient, mockByteReader)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "Invalid tag name provided.")
}

func TestSetTagWithInvalidDescriptionReturnsError(t *testing.T) {
	// Given...
	tagName := "mytag"
	description := "     "
	priority := DEFAULT_EMPTY_PRIORITY

	interactions := []utils.HttpInteraction{}
	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

    apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
    apiClient := api.InitialiseAPI(apiServerUrl)

	// When...
	err := SetTag(tagName, description, priority, apiClient, mockByteReader)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "Invalid tag description provided.")
}

func TestSetTagSendsCorrectRequests(t *testing.T) {
	// Given...
	tagName := "mytag"
	description := "my tag's description"
	priority := 1234

	tagId := base64.RawURLEncoding.EncodeToString([]byte(tagName))

	setTagInteraction := utils.NewHttpInteraction("/tags/" + tagId, http.MethodPut)
	setTagInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {

        tagSetRequest := readTagRequestBody(req)
        assert.Equal(t, tagSetRequest.GetDescription(), description)
        assert.Equal(t, tagSetRequest.GetPriority(), int32(priority))

		body := `{
			"apiVersion": "v1alpha1",
			"kind": "GalasaTag",
			"metadata": {
				"id": "tag123",
				"name": "mytag"
			},
			"data": {
				"description": "my tag's description",
				"priority": 1234
			}
		}`
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusCreated)
		writer.Write([]byte(body))
	}

	interactions := []utils.HttpInteraction{
		setTagInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

    apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
    apiClient := api.InitialiseAPI(apiServerUrl)

	// When...
	err := SetTag(tagName, description, priority, apiClient, mockByteReader)

	// Then...
	assert.Nil(t, err)
}

func TestSetTagWithServerFailureGivesCorrectMessage(t *testing.T) {
	// Given...
	tagName := "mytag"
	description := "my tag's description"
	priority := 1234

	tagId := base64.RawURLEncoding.EncodeToString([]byte(tagName))

	setTagInteraction := utils.NewHttpInteraction("/tags/" + tagId, http.MethodPut)
	setTagInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {

        tagSetRequest := readTagRequestBody(req)
        assert.Equal(t, tagSetRequest.GetDescription(), description)
        assert.Equal(t, tagSetRequest.GetPriority(), int32(priority))

        writer.Header().Set("Content-Type", "application/json")
        writer.WriteHeader(http.StatusInternalServerError)
        writer.Write([]byte(`{}`))
	}

	interactions := []utils.HttpInteraction{
		setTagInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

    apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
    apiClient := api.InitialiseAPI(apiServerUrl)

	// When...
	err := SetTag(tagName, description, priority, apiClient, mockByteReader)

	// Then...
	assert.NotNil(t, err)
    assert.ErrorContains(t, err, tagName)
    assert.ErrorContains(t, err, strconv.Itoa(http.StatusInternalServerError))
    assert.ErrorContains(t, err, "GAL1262E")
    assert.ErrorContains(t, err, "Error details from the server are")
}
