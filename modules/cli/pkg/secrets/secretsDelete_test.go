/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package secrets

import (
	"net/http"
	"strconv"
	"testing"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

func TestCanDeleteASecret(t *testing.T) {
    // Given...
    secretName := "SYSTEM1"

    // Create the expected HTTP interactions with the API server
    deleteSecretInteraction := utils.NewHttpInteraction("/secrets/" + secretName, http.MethodDelete)
    deleteSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        writer.WriteHeader(http.StatusNoContent)
    }

    interactions := []utils.HttpInteraction{
        deleteSecretInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    apiClient := api.InitialiseAPI(apiServerUrl)
    mockByteReader := utils.NewMockByteReader()

    // When...
    err := DeleteSecret(
        secretName,
        console,
        apiClient,
        mockByteReader)

    // Then...
    assert.Nil(t, err, "DeleteSecret returned an unexpected error")
    assert.Empty(t, console.ReadText(), "The console was written to on a successful deletion, it should be empty")
}

func TestDeleteASecretWithBlankNameDisplaysError(t *testing.T) {
    // Given...
    secretName := "    "

	// The client-side validation should fail, so no HTTP interactions will be performed
    interactions := []utils.HttpInteraction{}

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    apiClient := api.InitialiseAPI(apiServerUrl)
    mockByteReader := utils.NewMockByteReader()

    // When...
    err := DeleteSecret(
        secretName,
        console,
        apiClient,
        mockByteReader)

    // Then...
    assert.NotNil(t, err, "DeleteSecret did not return an error as expected")
    errorMsg := err.Error()
    assert.Contains(t, errorMsg, "GAL1172E")
    assert.Contains(t, errorMsg, " Invalid secret name provided")
}

func TestDeleteNonExistantSecretDisplaysError(t *testing.T) {
    // Given...
    nonExistantSecret := "secretDoesNotExist123"

    // Create the expected HTTP interactions with the API server
    deleteSecretInteraction := utils.NewHttpInteraction("/secrets/" + nonExistantSecret, http.MethodDelete)
    deleteSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        writer.Header().Set("Content-Type", "application/json")
        writer.WriteHeader(http.StatusNotFound)
        writer.Write([]byte(`{ "error_message": "No such secret exists" }`))
    }


    interactions := []utils.HttpInteraction{ deleteSecretInteraction }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    apiClient := api.InitialiseAPI(apiServerUrl)
    mockByteReader := utils.NewMockByteReader()

    // When...
    err := DeleteSecret(
        nonExistantSecret,
        console,
        apiClient,
        mockByteReader)

    // Then...
    assert.NotNil(t, err, "SecretsDelete did not return an error but it should have")
    errorMsg := err.Error()
    assert.Contains(t, errorMsg, nonExistantSecret)
    assert.Contains(t, errorMsg, "GAL1170E")
    assert.Contains(t, errorMsg, "Error details from the server are: 'No such secret exists'")
}

func TestSecretsDeleteFailsWithNoExplanationErrorPayloadGivesCorrectMessage(t *testing.T) {
    // Given...
    secretName := "MYSECRET"

    // Create the expected HTTP interactions with the API server
    deleteSecretInteraction := utils.NewHttpInteraction("/secrets/" + secretName, http.MethodDelete)
    deleteSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        writer.WriteHeader(http.StatusInternalServerError)
    }

    interactions := []utils.HttpInteraction{
        deleteSecretInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    apiClient := api.InitialiseAPI(apiServerUrl)
    mockByteReader := utils.NewMockByteReader()

    // When...
    err := DeleteSecret(
        secretName,
        console,
        apiClient,
        mockByteReader)

    // Then...
    assert.NotNil(t, err, "SecretsDelete did not return an error but it should have")
    consoleText := err.Error()
    assert.Contains(t, consoleText , secretName)
    assert.Contains(t, consoleText , "GAL1167E")
}

func TestSecretsDeleteFailsWithNonJsonContentTypeExplanationErrorPayloadGivesCorrectMessage(t *testing.T) {
    // Given...
    secretName := "MYSECRET"

    // Create the expected HTTP interactions with the API server
    deleteSecretInteraction := utils.NewHttpInteraction("/secrets/" + secretName, http.MethodDelete)
    deleteSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        writer.WriteHeader(http.StatusInternalServerError)
        writer.Header().Set("Content-Type", "application/notJsonOnPurpose")
        writer.Write([]byte("something not json but non-zero-length."))
    }

    interactions := []utils.HttpInteraction{
        deleteSecretInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    apiClient := api.InitialiseAPI(apiServerUrl)
    mockByteReader := utils.NewMockByteReader()

    // When...
    err := DeleteSecret(
        secretName,
        console,
        apiClient,
        mockByteReader)

    // Then...
    assert.NotNil(t, err, "SecretsDelete did not return an error but it should have")
    consoleText := err.Error()
    assert.Contains(t, consoleText, secretName)
    assert.Contains(t, consoleText, strconv.Itoa(http.StatusInternalServerError))
    assert.Contains(t, consoleText, "GAL1171E")
    assert.Contains(t, consoleText, "Error details from the server are not in the json format")
}

func TestSecretsDeleteFailsWithBadlyFormedJsonContentExplanationErrorPayloadGivesCorrectMessage(t *testing.T) {
    // Given...
    secretName := "MYSECRET"

    // Create the expected HTTP interactions with the API server
    deleteSecretInteraction := utils.NewHttpInteraction("/secrets/" + secretName, http.MethodDelete)
    deleteSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        writer.Header().Set("Content-Type", "application/json")
        writer.WriteHeader(http.StatusInternalServerError)
        writer.Write([]byte(`{ "this": "isBadJson because it doesnt end in a close braces" `))
    }

    interactions := []utils.HttpInteraction{
        deleteSecretInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    apiClient := api.InitialiseAPI(apiServerUrl)
    mockByteReader := utils.NewMockByteReader()

    // When...
    err := DeleteSecret(
        secretName,
        console,
        apiClient,
        mockByteReader)

    // Then...
    assert.NotNil(t, err, "SecretsDelete did not return an error but it should have")
    consoleText := err.Error()
    assert.Contains(t, consoleText, secretName)
    assert.Contains(t, consoleText, strconv.Itoa(http.StatusInternalServerError))
    assert.Contains(t, consoleText, "GAL1169E")
    assert.Contains(t, consoleText, "Error details from the server are not in a valid json format")
    assert.Contains(t, consoleText, "Cause: 'unexpected end of JSON input'")
}

func TestSecretsDeleteFailsWithFailureToReadResponseBodyGivesCorrectMessage(t *testing.T) {
    // Given...
    secretName := "MYSECRET"

    // Create the expected HTTP interactions with the API server
    deleteSecretInteraction := utils.NewHttpInteraction("/secrets/" + secretName, http.MethodDelete)
    deleteSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        writer.Header().Set("Content-Type", "application/json")
        writer.WriteHeader(http.StatusInternalServerError)
        writer.Write([]byte(`{}`))
    }

    interactions := []utils.HttpInteraction{
        deleteSecretInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    apiClient := api.InitialiseAPI(apiServerUrl)
    mockByteReader := utils.NewMockByteReaderAsMock(true)

    // When...
    err := DeleteSecret(
        secretName,
        console,
        apiClient,
        mockByteReader)

    // Then...
    assert.NotNil(t, err, "SecretsDelete returned an unexpected error")
    consoleText := err.Error()
    assert.Contains(t, consoleText, secretName)
    assert.Contains(t, consoleText, strconv.Itoa(http.StatusInternalServerError))
    assert.Contains(t, consoleText, "GAL1168E")
    assert.Contains(t, consoleText, "Error details from the server could not be read")
}
