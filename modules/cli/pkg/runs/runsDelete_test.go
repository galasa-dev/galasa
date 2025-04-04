/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"encoding/json"
	"net/http"
	"strconv"
	"testing"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

func createMockRun(runName string, runId string) galasaapi.Run {
    run := *galasaapi.NewRun()
    run.SetRunId(runId)
    testStructure := *galasaapi.NewTestStructure()
    testStructure.SetRunName(runName)

    run.SetTestStructure(testStructure)
    return run
}

func TestCanDeleteARun(t *testing.T) {
    // Given...
    runName := "J20"
    runId := "J234567890"

    // Create the mock run to be deleted
    runToDelete := createMockRun(runName, runId)
    runToDeleteBytes, _ := json.Marshal(runToDelete)
    runToDeleteJson := string(runToDeleteBytes)

    // Create the expected HTTP interactions with the API server
    getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        WriteMockRasRunsResponse(t, writer, req, runName, []string{ runToDeleteJson })
    }

    deleteRunsInteraction := utils.NewHttpInteraction("/ras/runs/" + runId, http.MethodDelete)
    deleteRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        writer.WriteHeader(http.StatusNoContent)
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
        deleteRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    mockTimeService := utils.NewMockTimeService()
	mockByteReader := utils.NewMockByteReader()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

    // When...
    err := RunsDelete(
        runName,
        console,
        commsClient,
        mockTimeService,
		mockByteReader)

    // Then...
    assert.Nil(t, err, "RunsDelete returned an unexpected error")
    assert.Empty(t, console.ReadText(), "The console was written to on a successful deletion, it should be empty")
}

func TestCanDeleteRunAndReruns(t *testing.T) {
    // Given...
    runName := "J20"
    runId := "J234567890"
	reRun1Id := "ABC123"
	reRun2Id := "DEF456"

    // Create the mock runs to be deleted - re-runs should have the same run name but different run IDs
    runToDelete := createMockRun(runName, runId)
    runToDeleteBytes, _ := json.Marshal(runToDelete)
    runToDeleteJson := string(runToDeleteBytes)

	reRun1 := createMockRun(runName, reRun1Id)
    reRun1Bytes, _ := json.Marshal(reRun1)
    reRun1Json := string(reRun1Bytes)

	reRun2 := createMockRun(runName, reRun2Id)
    reRun2Bytes, _ := json.Marshal(reRun2)
    reRun2Json := string(reRun2Bytes)

	runsAsJsonStrings := []string{
		runToDeleteJson,
		reRun1Json,
		reRun2Json,
	}
	
    // Create the expected HTTP interactions with the API server
    getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        WriteMockRasRunsResponse(t, writer, req, runName, runsAsJsonStrings)
    }

	successfulDeleteFunc := func(writer http.ResponseWriter, req *http.Request) {
        writer.WriteHeader(http.StatusNoContent)
    }

    deleteRunInteraction := utils.NewHttpInteraction("/ras/runs/" + runId, http.MethodDelete)
    deleteRunInteraction.WriteHttpResponseFunc = successfulDeleteFunc

    deleteRerun1Interaction := utils.NewHttpInteraction("/ras/runs/" + reRun1Id, http.MethodDelete)
    deleteRerun1Interaction.WriteHttpResponseFunc = successfulDeleteFunc

    deleteRerun2Interaction := utils.NewHttpInteraction("/ras/runs/" + reRun2Id, http.MethodDelete)
    deleteRerun2Interaction.WriteHttpResponseFunc = successfulDeleteFunc

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
        deleteRunInteraction,
		deleteRerun1Interaction,
		deleteRerun2Interaction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    mockTimeService := utils.NewMockTimeService()
	mockByteReader := utils.NewMockByteReader()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

    // When...
    err := RunsDelete(
        runName,
        console,
        commsClient,
        mockTimeService,
		mockByteReader)

    // Then...
    assert.Nil(t, err, "RunsDelete returned an unexpected error")
    assert.Empty(t, console.ReadText(), "The console was written to on a successful deletion, it should be empty")
}

func TestDeleteNonExistantRunDisplaysError(t *testing.T) {
    // Given...
    nonExistantRunName := "runDoesNotExist123"

    // Create the expected HTTP interactions with the API server
    getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        WriteMockRasRunsResponse(t, writer, req, nonExistantRunName, []string{})
    }

    interactions := []utils.HttpInteraction{ getRunsInteraction }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    mockTimeService := utils.NewMockTimeService()
	mockByteReader := utils.NewMockByteReader()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

    // When...
    err := RunsDelete(
        nonExistantRunName,
        console,
        commsClient,
        mockTimeService,
		mockByteReader)

    // Then...
    assert.NotNil(t, err, "RunsDelete did not return an error but it should have")
    consoleOutputText := console.ReadText()
    assert.Contains(t, consoleOutputText, nonExistantRunName)
    assert.Contains(t, consoleOutputText, "GAL1163E")
    assert.Contains(t, consoleOutputText, "The run named 'runDoesNotExist123' could not be deleted")
}

func TestRunsDeleteFailsWithNoExplanationErrorPayloadGivesCorrectMessage(t *testing.T) {
    // Given...
    runName := "J20"
    runId := "J234567890"

    // Create the mock run to be deleted
    runToDelete := createMockRun(runName, runId)
    runToDeleteBytes, _ := json.Marshal(runToDelete)
    runToDeleteJson := string(runToDeleteBytes)

    // Create the expected HTTP interactions with the API server
    getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        WriteMockRasRunsResponse(t, writer, req, runName, []string{ runToDeleteJson })
    }

    deleteRunsInteraction := utils.NewHttpInteraction("/ras/runs/" + runId, http.MethodDelete)
    deleteRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        writer.WriteHeader(http.StatusInternalServerError)
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
        deleteRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    mockTimeService := utils.NewMockTimeService()
	mockByteReader := utils.NewMockByteReader()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

    // When...
    err := RunsDelete(
        runName,
        console,
        commsClient,
        mockTimeService,
		mockByteReader)

    // Then...
    assert.NotNil(t, err, "RunsDelete did not return an error but it should have")
    consoleText := console.ReadText()
    assert.Contains(t, consoleText , runName)
    assert.Contains(t, consoleText , "GAL1159E")
}

func TestRunsDeleteFailsWithNonJsonContentTypeExplanationErrorPayloadGivesCorrectMessage(t *testing.T) {
    // Given...
    runName := "J20"
    runId := "J234567890"

    // Create the mock run to be deleted
    runToDelete := createMockRun(runName, runId)
    runToDeleteBytes, _ := json.Marshal(runToDelete)
    runToDeleteJson := string(runToDeleteBytes)

    // Create the expected HTTP interactions with the API server
    getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        WriteMockRasRunsResponse(t, writer, req, runName, []string{ runToDeleteJson })
    }

    deleteRunsInteraction := utils.NewHttpInteraction("/ras/runs/" + runId, http.MethodDelete)
    deleteRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        writer.WriteHeader(http.StatusInternalServerError)
        writer.Header().Set("Content-Type", "application/notJsonOnPurpose")
        writer.Write([]byte("something not json but non-zero-length."))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
        deleteRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    mockTimeService := utils.NewMockTimeService()
	mockByteReader := utils.NewMockByteReader()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

    // When...
    err := RunsDelete(
        runName,
        console,
        commsClient,
        mockTimeService,
		mockByteReader)

    // Then...
    assert.NotNil(t, err, "RunsDelete did not return an error but it should have")
    consoleText := console.ReadText()
    assert.Contains(t, consoleText, runName)
    assert.Contains(t, consoleText, strconv.Itoa(http.StatusInternalServerError))
    assert.Contains(t, consoleText, "GAL1164E")
    assert.Contains(t, consoleText, "Error details from the server are not in the json format")
}

func TestRunsDeleteFailsWithBadlyFormedJsonContentExplanationErrorPayloadGivesCorrectMessage(t *testing.T) {
    // Given...
    runName := "J20"
    runId := "J234567890"

    // Create the mock run to be deleted
    runToDelete := createMockRun(runName, runId)
    runToDeleteBytes, _ := json.Marshal(runToDelete)
    runToDeleteJson := string(runToDeleteBytes)

    // Create the expected HTTP interactions with the API server
    getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        WriteMockRasRunsResponse(t, writer, req, runName, []string{ runToDeleteJson })
    }

    deleteRunsInteraction := utils.NewHttpInteraction("/ras/runs/" + runId, http.MethodDelete)
    deleteRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        writer.Header().Set("Content-Type", "application/json")
        writer.WriteHeader(http.StatusInternalServerError)
        writer.Write([]byte(`{ "this": "isBadJson because it doesnt end in a close braces" `))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
        deleteRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    mockTimeService := utils.NewMockTimeService()
	mockByteReader := utils.NewMockByteReader()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

    // When...
    err := RunsDelete(
        runName,
        console,
        commsClient,
        mockTimeService,
		mockByteReader)

    // Then...
    assert.NotNil(t, err, "RunsDelete did not return an error but it should have")
    consoleText := console.ReadText()
    assert.Contains(t, consoleText, runName)
    assert.Contains(t, consoleText, strconv.Itoa(http.StatusInternalServerError))
    assert.Contains(t, consoleText, "GAL1161E")
    assert.Contains(t, consoleText, "Error details from the server are not in a valid json format")
    assert.Contains(t, consoleText, "Cause: 'unexpected end of JSON input'")
}

func TestRunsDeleteFailsWithValidErrorResponsePayloadGivesCorrectMessage(t *testing.T) {
    // Given...
    runName := "J20"
    runId := "J234567890"
    apiErrorCode := 5000
    apiErrorMessage := "this is an error from the API server"

    // Create the mock run to be deleted
    runToDelete := createMockRun(runName, runId)
    runToDeleteBytes, _ := json.Marshal(runToDelete)
    runToDeleteJson := string(runToDeleteBytes)

    // Create the expected HTTP interactions with the API server
    getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        WriteMockRasRunsResponse(t, writer, req, runName, []string{ runToDeleteJson })
    }

    deleteRunsInteraction := utils.NewHttpInteraction("/ras/runs/" + runId, http.MethodDelete)
    deleteRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        writer.Header().Set("Content-Type", "application/json")
        writer.WriteHeader(http.StatusInternalServerError)

        apiError := errors.GalasaAPIError{
            Code: apiErrorCode,
            Message: apiErrorMessage,
        }
        apiErrorBytes, _ := json.Marshal(apiError)
        writer.Write(apiErrorBytes)
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
        deleteRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    mockTimeService := utils.NewMockTimeService()
	mockByteReader := utils.NewMockByteReader()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

    // When...
    err := RunsDelete(
        runName,
        console,
        commsClient,
        mockTimeService,
		mockByteReader)

    // Then...
    assert.NotNil(t, err, "RunsDelete did not return an error but it should have")
    consoleText := console.ReadText()
    assert.Contains(t, consoleText, runName)
    assert.Contains(t, consoleText, strconv.Itoa(http.StatusInternalServerError))
    assert.Contains(t, consoleText, "GAL1162E")
    assert.Contains(t, consoleText, apiErrorMessage)
}


func TestRunsDeleteFailsWithFailureToReadResponseBodyGivesCorrectMessage(t *testing.T) {
    // Given...
    runName := "J20"
    runId := "J234567890"

    // Create the mock run to be deleted
    runToDelete := createMockRun(runName, runId)
    runToDeleteBytes, _ := json.Marshal(runToDelete)
    runToDeleteJson := string(runToDeleteBytes)

    // Create the expected HTTP interactions with the API server
    getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        WriteMockRasRunsResponse(t, writer, req, runName, []string{ runToDeleteJson })
    }

    deleteRunsInteraction := utils.NewHttpInteraction("/ras/runs/" + runId, http.MethodDelete)
    deleteRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        writer.Header().Set("Content-Type", "application/json")
        writer.WriteHeader(http.StatusInternalServerError)
        writer.Write([]byte(`{}`))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
        deleteRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    mockTimeService := utils.NewMockTimeService()
	mockByteReader := utils.NewMockByteReaderAsMock(true)
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

    // When...
    err := RunsDelete(
        runName,
        console,
        commsClient,
        mockTimeService,
		mockByteReader)

    // Then...
    assert.NotNil(t, err, "RunsDelete returned an unexpected error")
    consoleText := console.ReadText()
    assert.Contains(t, consoleText, runName)
    assert.Contains(t, consoleText, strconv.Itoa(http.StatusInternalServerError))
    assert.Contains(t, consoleText, "GAL1160E")
    assert.Contains(t, consoleText, "GAL1160E")
    assert.Contains(t, consoleText, "Error details from the server could not be read")
}
