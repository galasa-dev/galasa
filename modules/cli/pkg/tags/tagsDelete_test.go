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


func TestDeleteTagWithInvalidNameReturnsError(t *testing.T) {
	// Given...
	tagName := ""

	interactions := []utils.HttpInteraction{}
	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

    apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := DeleteTag(tagName, commsClient, mockByteReader)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "Invalid tag name provided.")
}

func TestDeleteTagNotFoundOnServiceReturnsError(t *testing.T) {
	// Given...
	tagName := "mytag"

	getTagsInteraction := utils.NewHttpInteraction("/tags", http.MethodGet)
	getTagsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		requestedName := req.URL.Query().Get("name")
		assert.Equal(t, tagName, requestedName)

		body := `[]`
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(body))
	}

	interactions := []utils.HttpInteraction{
		getTagsInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

    apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := DeleteTag(tagName, commsClient, mockByteReader)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "No such tag named 'mytag' exists within the Galasa service")
}

func TestDeleteTagSendsCorrectRequests(t *testing.T) {
	// Given...
	tagName := "mytag"

	getTagsInteraction := utils.NewHttpInteraction("/tags", http.MethodGet)
	getTagsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		requestedName := req.URL.Query().Get("name")
		assert.Equal(t, tagName, requestedName)

		body := `[{
			"metadata": {
				"id": "tag123",
				"name": "mytag"
			},
			"data": {}
		}]`
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(body))
	}

	deleteTagInteraction := utils.NewHttpInteraction("/tags/tag123", http.MethodDelete)
	deleteTagInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusNoContent)
	}

	interactions := []utils.HttpInteraction{
		getTagsInteraction,
		deleteTagInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

    apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := DeleteTag(tagName, commsClient, mockByteReader)

	// Then...
	assert.Nil(t, err)
}

func TestDeleteTagWithServerFailureDuringGetGivesCorrectMessage(t *testing.T) {
	// Given...
	tagName := "mytag"

	getTagsInteraction := utils.NewHttpInteraction("/tags", http.MethodGet)
	getTagsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        writer.Header().Set("Content-Type", "application/json")
        writer.WriteHeader(http.StatusInternalServerError)
        writer.Write([]byte(`{}`))
	}

	interactions := []utils.HttpInteraction{
		getTagsInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

    apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := DeleteTag(tagName, commsClient, mockByteReader)

	// Then...
	assert.NotNil(t, err)
    assert.ErrorContains(t, err, tagName)
    assert.ErrorContains(t, err, strconv.Itoa(http.StatusInternalServerError))
    assert.ErrorContains(t, err, "GAL1253E")
    assert.ErrorContains(t, err, "Error details from the server are")
}

func TestDeleteTagWithServerFailureDuringDeleteGivesCorrectMessage(t *testing.T) {
	// Given...
	tagName := "mytag"

	getTagsInteraction := utils.NewHttpInteraction("/tags", http.MethodGet)
	getTagsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		requestedName := req.URL.Query().Get("name")
		assert.Equal(t, tagName, requestedName)

		body := `[{
			"metadata": {
				"id": "tag123",
				"name": "mytag"
			},
			"data": {}
		}]`
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(body))
	}

	deleteTagInteraction := utils.NewHttpInteraction("/tags/tag123", http.MethodDelete)
	deleteTagInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        writer.Header().Set("Content-Type", "application/json")
        writer.WriteHeader(http.StatusInternalServerError)
        writer.Write([]byte(`{}`))
	}

	interactions := []utils.HttpInteraction{
		getTagsInteraction,
		deleteTagInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

    apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := DeleteTag(tagName, commsClient, mockByteReader)

	// Then...
	assert.NotNil(t, err)
    assert.ErrorContains(t, err, tagName)
    assert.ErrorContains(t, err, strconv.Itoa(http.StatusInternalServerError))
    assert.ErrorContains(t, err, "GAL1253E")
    assert.ErrorContains(t, err, "Error details from the server are")
}
