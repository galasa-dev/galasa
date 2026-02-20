/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package streams

import (
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

func readStreamRequestBody(req *http.Request) galasaapi.StreamUpdateRequest {
	var streamUpdateRequest galasaapi.StreamUpdateRequest
	requestBodyBytes, _ := io.ReadAll(req.Body)
	defer req.Body.Close()

	_ = json.Unmarshal(requestBodyBytes, &streamUpdateRequest)
	return streamUpdateRequest
}

func TestSetStreamWithInvalidNameReturnsError(t *testing.T) {
	// Given...
	streamName := ""
	description := ""
	mavenRepositoryUrl := ""
	testCatalogUrl := ""
	obrs := []string{}

	interactions := []utils.HttpInteraction{}
	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
	apiClient := api.InitialiseAPI(apiServerUrl)

	// When...
	err := SetStream(streamName, description, mavenRepositoryUrl, testCatalogUrl, obrs, apiClient, mockByteReader)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1233E")
	assert.ErrorContains(t, err, "The stream name provided by the --name field cannot be an empty string")
}

func TestSetStreamWithInvalidDescriptionReturnsError(t *testing.T) {
	// Given...
	streamName := "mystream"
	description := "     "
	mavenRepositoryUrl := ""
	testCatalogUrl := ""
	obrs := []string{}

	interactions := []utils.HttpInteraction{}
	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
	apiClient := api.InitialiseAPI(apiServerUrl)

	// When...
	err := SetStream(streamName, description, mavenRepositoryUrl, testCatalogUrl, obrs, apiClient, mockByteReader)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1283E")
	assert.ErrorContains(t, err, "Invalid stream description provided")
}

func TestSetStreamWithInvalidMavenUrlReturnsError(t *testing.T) {
	// Given...
	streamName := "mystream"
	description := "my description"
	mavenRepositoryUrl := "not a valid url"
	testCatalogUrl := ""
	obrs := []string{}

	interactions := []utils.HttpInteraction{}
	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
	apiClient := api.InitialiseAPI(apiServerUrl)

	// When...
	err := SetStream(streamName, description, mavenRepositoryUrl, testCatalogUrl, obrs, apiClient, mockByteReader)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1284E")
	assert.ErrorContains(t, err, "Invalid URL provided")
}

func TestSetStreamWithInvalidTestCatalogUrlReturnsError(t *testing.T) {
	// Given...
	streamName := "mystream"
	description := "my description"
	mavenRepositoryUrl := ""
	testCatalogUrl := "not a valid url"
	obrs := []string{}

	interactions := []utils.HttpInteraction{}
	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
	apiClient := api.InitialiseAPI(apiServerUrl)

	// When...
	err := SetStream(streamName, description, mavenRepositoryUrl, testCatalogUrl, obrs, apiClient, mockByteReader)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1284E")
	assert.ErrorContains(t, err, "Invalid URL provided")
}

func TestSetStreamSendsCorrectRequest(t *testing.T) {
	// Given...
	streamName := "mystream"
	description := "my stream's description"
	mavenRepositoryUrl := "https://maven.example.com/repo"
	testCatalogUrl := "https://maven.example.com/repo/testcatalog.json"
	obrs := []string{}

	setStreamInteraction := utils.NewHttpInteraction("/streams/"+streamName, http.MethodPut)
	setStreamInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {

		streamUpdateRequest := readStreamRequestBody(req)
		assert.Equal(t, description, streamUpdateRequest.GetDescription())
		
		repository, hasRepository := streamUpdateRequest.GetRepositoryOk()
		assert.True(t, hasRepository)
		assert.Equal(t, mavenRepositoryUrl, repository.GetUrl())
		
		testCatalog, hasTestCatalog := streamUpdateRequest.GetTestCatalogOk()
		assert.True(t, hasTestCatalog)
		assert.Equal(t, testCatalogUrl, testCatalog.GetUrl())

		body := `{
			"apiVersion": "v1alpha1",
			"kind": "GalasaStream",
			"metadata": {
				"name": "mystream",
				"description": "my stream's description"
			},
			"data": {
				"repository": {
					"url": "https://maven.example.com/repo"
				},
				"testCatalog": {
					"url": "https://maven.example.com/repo/testcatalog.json"
				}
			}
		}`
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(body))
	}

	interactions := []utils.HttpInteraction{
		setStreamInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
	apiClient := api.InitialiseAPI(apiServerUrl)

	// When...
	err := SetStream(streamName, description, mavenRepositoryUrl, testCatalogUrl, obrs, apiClient, mockByteReader)

	// Then...
	assert.Nil(t, err)
}

func TestSetStreamWithObrsUpdatesObrData(t *testing.T) {
	// Given...
	streamName := "mystream"
	description := "my stream's description"
	mavenRepositoryUrl := ""
	testCatalogUrl := ""
	obrs := []string{"mvn:dev.galasa/dev.galasa.obr/0.1.0/obr"}

	setStreamInteraction := utils.NewHttpInteraction("/streams/"+streamName, http.MethodPut)
	setStreamInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {

		streamUpdateRequest := readStreamRequestBody(req)
		assert.Equal(t, description, streamUpdateRequest.GetDescription())
		
		obrs := streamUpdateRequest.GetObrs()
		assert.Len(t, obrs, 1)
		assert.Equal(t, "dev.galasa", obrs[0].GetGroupId())
		assert.Equal(t, "dev.galasa.obr", obrs[0].GetArtifactId())
		assert.Equal(t, "0.1.0", obrs[0].GetVersion())

		body := `{
			"apiVersion": "v1alpha1",
			"kind": "GalasaStream",
			"metadata": {
				"name": "mystream",
				"description": "my stream's description"
			},
			"data": {
				"obrs": [
					{
						"group-id": "dev.galasa",
						"artifact-id": "dev.galasa.obr",
						"version": "0.1.0"
					}
				]
			}
		}`
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(body))
	}

	interactions := []utils.HttpInteraction{
		setStreamInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
	apiClient := api.InitialiseAPI(apiServerUrl)

	// When...
	err := SetStream(streamName, description, mavenRepositoryUrl, testCatalogUrl, obrs, apiClient, mockByteReader)

	// Then...
	assert.Nil(t, err)
}

func TestSetStreamWithTooManyObrPartsReturnsError(t *testing.T) {
	// Given...
	streamName := "mystream"
	description := "my stream's description"
	mavenRepositoryUrl := ""
	testCatalogUrl := ""
	obrs := []string{"mvn:/this/obr/has/too/many/parts/obr"}

	interactions := []utils.HttpInteraction{}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
	apiClient := api.InitialiseAPI(apiServerUrl)

	// When...
	err := SetStream(streamName, description, mavenRepositoryUrl, testCatalogUrl, obrs, apiClient, mockByteReader)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1061E")
	assert.ErrorContains(t, err, "Badly formed OBR parameter")
}

func TestSetStreamWithTooFewObrPartsReturnsError(t *testing.T) {
	// Given...
	streamName := "mystream"
	description := "my stream's description"
	mavenRepositoryUrl := ""
	testCatalogUrl := ""
	obrs := []string{"mvn:not-enough-parts/obr"}

	interactions := []utils.HttpInteraction{}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
	apiClient := api.InitialiseAPI(apiServerUrl)

	// When...
	err := SetStream(streamName, description, mavenRepositoryUrl, testCatalogUrl, obrs, apiClient, mockByteReader)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1060E")
	assert.ErrorContains(t, err, "Badly formed OBR parameter")
}

func TestSetStreamWithNoMavenPrefixReturnsError(t *testing.T) {
	// Given...
	streamName := "mystream"
	description := "my stream's description"
	mavenRepositoryUrl := ""
	testCatalogUrl := ""
	obrs := []string{"dev.galasa/dev.galasa.obr/0.1.0/obr"}

	interactions := []utils.HttpInteraction{}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
	apiClient := api.InitialiseAPI(apiServerUrl)

	// When...
	err := SetStream(streamName, description, mavenRepositoryUrl, testCatalogUrl, obrs, apiClient, mockByteReader)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1062E")
	assert.ErrorContains(t, err, "Badly formed OBR parameter")
}

func TestSetStreamWithNoObrSuffixReturnsError(t *testing.T) {
	// Given...
	streamName := "mystream"
	description := "my stream's description"
	mavenRepositoryUrl := ""
	testCatalogUrl := ""
	obrs := []string{"mvn:dev.galasa/dev.galasa.obr/0.1.0"}

	interactions := []utils.HttpInteraction{}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
	apiClient := api.InitialiseAPI(apiServerUrl)

	// When...
	err := SetStream(streamName, description, mavenRepositoryUrl, testCatalogUrl, obrs, apiClient, mockByteReader)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1060E")
	assert.ErrorContains(t, err, "Badly formed OBR parameter")
}

func TestSetStreamWithServerFailureGivesCorrectMessage(t *testing.T) {
	// Given...
	streamName := "mystream"
	description := "my stream's description"
	mavenRepositoryUrl := ""
	testCatalogUrl := ""
	obrs := []string{}

	setStreamInteraction := utils.NewHttpInteraction("/streams/"+streamName, http.MethodPut)
	setStreamInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {

		streamUpdateRequest := readStreamRequestBody(req)
		assert.Equal(t, description, streamUpdateRequest.GetDescription())

		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusInternalServerError)
		writer.Write([]byte(`{}`))
	}

	interactions := []utils.HttpInteraction{
		setStreamInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiServerUrl := server.Server.URL
	mockByteReader := utils.NewMockByteReader()
	apiClient := api.InitialiseAPI(apiServerUrl)

	// When...
	err := SetStream(streamName, description, mavenRepositoryUrl, testCatalogUrl, obrs, apiClient, mockByteReader)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, streamName)
	assert.ErrorContains(t, err, strconv.Itoa(http.StatusInternalServerError))
	assert.ErrorContains(t, err, "GAL1289E")
	assert.ErrorContains(t, err, "Error details from the server are")
}
