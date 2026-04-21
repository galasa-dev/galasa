/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package secrets

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"strconv"
	"testing"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/files"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

func readSecretRequestBody(req *http.Request) galasaapi.SecretRequest {
	var secretRequest galasaapi.SecretRequest
	requestBodyBytes, _ := io.ReadAll(req.Body)
	defer req.Body.Close()

	_ = json.Unmarshal(requestBodyBytes, &secretRequest)
	return secretRequest
}

func TestCanCreateAUsernameSecret(t *testing.T) {
	// Given...
	secretName := "SYSTEM1"
	username := "my-username"
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := ""
	keystoreEncoded := ""
	keystoreFile := ""
	keystorePassword := ""
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := ""
	secretType := ""
	description := ""

	// Create the expected HTTP interactions with the API server
	createSecretInteraction := utils.NewHttpInteraction("/secrets/"+secretName, http.MethodPut)

	// Validate the request body
	createSecretInteraction.ValidateRequestFunc = func(t *testing.T, req *http.Request) {
		secretRequest := readSecretRequestBody(req)
		assert.Equal(t, secretRequest.GetName(), secretName)
		assert.Empty(t, secretRequest.GetType())

		requestUsername := secretRequest.GetUsername()
		assert.Equal(t, requestUsername.GetValue(), username)
		assert.Empty(t, requestUsername.GetEncoding())

		requestPassword := secretRequest.GetPassword()
		assert.Empty(t, requestPassword.GetValue())
		assert.Empty(t, requestPassword.GetEncoding())

		requestToken := secretRequest.GetToken()
		assert.Empty(t, requestToken.GetValue())
		assert.Empty(t, requestToken.GetEncoding())
	}

	createSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.WriteHeader(http.StatusCreated)
	}

	interactions := []utils.HttpInteraction{
		createSecretInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.Nil(t, err, "SetSecret returned an unexpected error")
	assert.Empty(t, console.ReadText(), "The console was written to on a successful creation, it should be empty")
}

func TestCanCreateAUsernamePasswordSecret(t *testing.T) {
	// Given...
	secretName := "SYSTEM1"
	username := "my-username"
	password := "my-password"
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := ""
	keystoreEncoded := ""
	keystoreFile := ""
	keystorePassword := ""
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := ""
	secretType := ""
	description := "my secret description"

	// Create the expected HTTP interactions with the API server
	createSecretInteraction := utils.NewHttpInteraction("/secrets/"+secretName, http.MethodPut)

	// Validate the request body
	createSecretInteraction.ValidateRequestFunc = func(t *testing.T, req *http.Request) {
		secretRequest := readSecretRequestBody(req)
		assert.Equal(t, secretRequest.GetName(), secretName)
		assert.Empty(t, secretRequest.GetType())
		assert.Equal(t, secretRequest.GetDescription(), description)

		requestUsername := secretRequest.GetUsername()
		assert.Equal(t, requestUsername.GetValue(), username)
		assert.Empty(t, requestUsername.GetEncoding())

		requestPassword := secretRequest.GetPassword()
		assert.Equal(t, requestPassword.GetValue(), password)
		assert.Empty(t, requestPassword.GetEncoding())

		requestToken := secretRequest.GetToken()
		assert.Empty(t, requestToken.GetValue())
		assert.Empty(t, requestToken.GetEncoding())
	}

	createSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.WriteHeader(http.StatusCreated)
	}

	interactions := []utils.HttpInteraction{
		createSecretInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.Nil(t, err, "SetSecret returned an unexpected error")
	assert.Empty(t, console.ReadText(), "The console was written to on a successful creation, it should be empty")
}

func TestCanCreateAUsernameTokenSecret(t *testing.T) {
	// Given...
	secretName := "SYSTEM1"
	username := "my-username"
	password := ""
	token := "my-token"
	base64Username := ""
	base64Password := ""
	base64Token := ""
	keystoreEncoded := ""
	keystoreFile := ""
	keystorePassword := ""
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := ""
	secretType := ""
	description := ""

	// Create the expected HTTP interactions with the API server
	createSecretInteraction := utils.NewHttpInteraction("/secrets/"+secretName, http.MethodPut)

	// Validate the request body
	createSecretInteraction.ValidateRequestFunc = func(t *testing.T, req *http.Request) {
		secretRequest := readSecretRequestBody(req)
		assert.Equal(t, secretRequest.GetName(), secretName)
		assert.Empty(t, secretRequest.GetType())

		requestUsername := secretRequest.GetUsername()
		assert.Equal(t, requestUsername.GetValue(), username)
		assert.Empty(t, requestUsername.GetEncoding())

		requestPassword := secretRequest.GetPassword()
		assert.Empty(t, requestPassword.GetValue())
		assert.Empty(t, requestPassword.GetEncoding())

		requestToken := secretRequest.GetToken()
		assert.Equal(t, requestToken.GetValue(), token)
		assert.Empty(t, requestToken.GetEncoding())
	}

	createSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.WriteHeader(http.StatusCreated)
	}

	interactions := []utils.HttpInteraction{
		createSecretInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.Nil(t, err, "SetSecret returned an unexpected error")
	assert.Empty(t, console.ReadText(), "The console was written to on a successful creation, it should be empty")
}

func TestCanCreateATokenSecret(t *testing.T) {
	// Given...
	secretName := "SYSTEM1"
	username := ""
	password := ""
	token := "my-token"
	base64Username := ""
	base64Password := ""
	base64Token := ""
	keystoreEncoded := ""
	keystoreFile := ""
	keystorePassword := ""
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := ""
	secretType := ""
	description := ""

	// Create the expected HTTP interactions with the API server
	createSecretInteraction := utils.NewHttpInteraction("/secrets/"+secretName, http.MethodPut)

	// Validate the request body
	createSecretInteraction.ValidateRequestFunc = func(t *testing.T, req *http.Request) {
		secretRequest := readSecretRequestBody(req)
		assert.Equal(t, secretRequest.GetName(), secretName)
		assert.Empty(t, secretRequest.GetType())

		requestUsername := secretRequest.GetUsername()
		assert.Empty(t, requestUsername.GetValue())
		assert.Empty(t, requestUsername.GetEncoding())

		requestPassword := secretRequest.GetPassword()
		assert.Empty(t, requestPassword.GetValue())
		assert.Empty(t, requestPassword.GetEncoding())

		requestToken := secretRequest.GetToken()
		assert.Equal(t, requestToken.GetValue(), token)
		assert.Empty(t, requestToken.GetEncoding())
	}

	createSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.WriteHeader(http.StatusCreated)
	}

	interactions := []utils.HttpInteraction{
		createSecretInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.Nil(t, err, "SetSecret returned an unexpected error")
	assert.Empty(t, console.ReadText(), "The console was written to on a successful creation, it should be empty")
}

func TestCanUpdateASecret(t *testing.T) {
	// Given...
	secretName := "SYSTEM1"
	username := ""
	password := "my-new-password"
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := ""
	keystoreEncoded := ""
	keystoreFile := ""
	keystorePassword := ""
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := ""
	secretType := ""
	description := ""

	// Create the expected HTTP interactions with the API server
	updateSecretInteraction := utils.NewHttpInteraction("/secrets/"+secretName, http.MethodPut)

	// Validate the request body
	updateSecretInteraction.ValidateRequestFunc = func(t *testing.T, req *http.Request) {
		secretRequest := readSecretRequestBody(req)
		assert.Equal(t, secretRequest.GetName(), secretName)
		assert.Empty(t, secretRequest.GetType())

		requestUsername := secretRequest.GetUsername()
		assert.Empty(t, requestUsername.GetValue())
		assert.Empty(t, requestUsername.GetEncoding())

		requestPassword := secretRequest.GetPassword()
		assert.Equal(t, requestPassword.GetValue(), password)
		assert.Empty(t, requestPassword.GetEncoding())

		requestToken := secretRequest.GetToken()
		assert.Empty(t, requestToken.GetValue())
		assert.Empty(t, requestToken.GetEncoding())
	}

	updateSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.WriteHeader(http.StatusNoContent)
	}

	interactions := []utils.HttpInteraction{
		updateSecretInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.Nil(t, err, "SetSecret returned an unexpected error")
	assert.Empty(t, console.ReadText(), "The console was written to on a successful creation, it should be empty")
}

func TestCanUpdateAUsernamePasswordSecretInBase64Format(t *testing.T) {
	// Given...
	secretName := "SYSTEM1"
	username := ""
	password := ""
	token := ""
	base64Username := "my-base64-username"
	base64Password := "my-base64-password"
	base64Token := ""
	keystoreEncoded := ""
	keystoreFile := ""
	keystorePassword := ""
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := ""
	secretType := ""
	description := ""

	// Create the expected HTTP interactions with the API server
	updateSecretInteraction := utils.NewHttpInteraction("/secrets/"+secretName, http.MethodPut)

	// Validate the request body
	updateSecretInteraction.ValidateRequestFunc = func(t *testing.T, req *http.Request) {
		secretRequest := readSecretRequestBody(req)
		assert.Equal(t, secretRequest.GetName(), secretName)
		assert.Empty(t, secretRequest.GetType())

		requestUsername := secretRequest.GetUsername()
		assert.Equal(t, requestUsername.GetValue(), base64Username)
		assert.Equal(t, requestUsername.GetEncoding(), BASE64_ENCODING)

		requestPassword := secretRequest.GetPassword()
		assert.Equal(t, requestPassword.GetValue(), base64Password)
		assert.Equal(t, requestPassword.GetEncoding(), BASE64_ENCODING)

		requestToken := secretRequest.GetToken()
		assert.Empty(t, requestToken.GetValue())
		assert.Empty(t, requestToken.GetEncoding())
	}

	updateSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.WriteHeader(http.StatusNoContent)
	}

	interactions := []utils.HttpInteraction{
		updateSecretInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.Nil(t, err, "SetSecret returned an unexpected error")
	assert.Empty(t, console.ReadText(), "The console was written to on a successful creation, it should be empty")
}

func TestCanUpdateATokenSecretInBase64Format(t *testing.T) {
	// Given...
	secretName := "SYSTEM1"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := "my-base64-token"
	keystoreEncoded := ""
	keystoreFile := ""
	keystorePassword := ""
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := ""
	secretType := ""
	description := ""

	// Create the expected HTTP interactions with the API server
	updateSecretInteraction := utils.NewHttpInteraction("/secrets/"+secretName, http.MethodPut)

	// Validate the request body
	updateSecretInteraction.ValidateRequestFunc = func(t *testing.T, req *http.Request) {
		secretRequest := readSecretRequestBody(req)
		assert.Equal(t, secretRequest.GetName(), secretName)
		assert.Empty(t, secretRequest.GetType())

		requestUsername := secretRequest.GetUsername()
		assert.Empty(t, requestUsername.GetValue())
		assert.Empty(t, requestUsername.GetEncoding())

		requestPassword := secretRequest.GetPassword()
		assert.Empty(t, requestPassword.GetValue())
		assert.Empty(t, requestPassword.GetEncoding())

		requestToken := secretRequest.GetToken()
		assert.Equal(t, requestToken.GetValue(), base64Token)
		assert.Equal(t, requestToken.GetEncoding(), BASE64_ENCODING)
	}

	updateSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.WriteHeader(http.StatusNoContent)
	}

	interactions := []utils.HttpInteraction{
		updateSecretInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.Nil(t, err, "SetSecret returned an unexpected error")
	assert.Empty(t, console.ReadText(), "The console was written to on a successful creation, it should be empty")
}

func TestCanUpdateASecretsTypeOk(t *testing.T) {
	// Given...
	secretName := "SYSTEM1"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := "my-base64-token"
	keystoreEncoded := ""
	keystoreFile := ""
	keystorePassword := ""
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := ""
	secretType := "token"
	description := "my new token"

	// Create the expected HTTP interactions with the API server
	updateSecretInteraction := utils.NewHttpInteraction("/secrets/"+secretName, http.MethodPut)

	// Validate the request body
	updateSecretInteraction.ValidateRequestFunc = func(t *testing.T, req *http.Request) {
		secretRequest := readSecretRequestBody(req)
		assert.Equal(t, secretRequest.GetName(), secretName)
		assert.Equal(t, secretRequest.GetType(), galasaapi.TOKEN)

		requestUsername := secretRequest.GetUsername()
		assert.Empty(t, requestUsername.GetValue())
		assert.Empty(t, requestUsername.GetEncoding())

		requestPassword := secretRequest.GetPassword()
		assert.Empty(t, requestPassword.GetValue())
		assert.Empty(t, requestPassword.GetEncoding())

		requestToken := secretRequest.GetToken()
		assert.Equal(t, requestToken.GetValue(), base64Token)
		assert.Equal(t, requestToken.GetEncoding(), BASE64_ENCODING)
	}

	updateSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.WriteHeader(http.StatusNoContent)
	}

	interactions := []utils.HttpInteraction{
		updateSecretInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.Nil(t, err, "SetSecret returned an unexpected error")
	assert.Empty(t, console.ReadText(), "The console was written to on a successful creation, it should be empty")
}

func TestUpdateSecretWithNoNameThrowsError(t *testing.T) {
	// Given...
	secretName := ""
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := "my-base64-token"
	keystoreEncoded := ""
	keystoreFile := ""
	keystorePassword := ""
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := ""
	secretType := ""
	description := ""

	// Validation should fail, so no HTTP interactions should take place
	interactions := []utils.HttpInteraction{}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.NotNil(t, err, "SetSecret did not return an error as expected")
	errorMsg := err.Error()
	assert.Contains(t, errorMsg, "GAL1172E")
	assert.Contains(t, errorMsg, "Invalid secret name provided")
}

func TestUpdateSecretWithNonLatin1NameThrowsError(t *testing.T) {
	// Given...
	secretName := string(rune(300)) + "NONLATIN1"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := "my-base64-token"
	keystoreEncoded := ""
	keystoreFile := ""
	keystorePassword := ""
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := ""
	secretType := ""
	description := ""

	// Validation should fail, so no HTTP interactions should take place
	interactions := []utils.HttpInteraction{}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.NotNil(t, err, "SetSecret did not return an error as expected")
	errorMsg := err.Error()
	assert.Contains(t, errorMsg, "GAL1172E")
	assert.Contains(t, errorMsg, "Invalid secret name provided")
}

func TestUpdateSecretWithNonLatin1DescriptionThrowsError(t *testing.T) {
	// Given...
	secretName := "MYSECRET"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := "my-base64-token"
	keystoreEncoded := ""
	keystoreFile := ""
	keystorePassword := ""
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := ""
	secretType := ""
	description := string(rune(256)) + " is not latin-1"

	// Validation should fail, so no HTTP interactions should take place
	interactions := []utils.HttpInteraction{}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.NotNil(t, err, "SetSecret did not return an error as expected")
	errorMsg := err.Error()
	assert.Contains(t, errorMsg, "GAL1194E")
	assert.Contains(t, errorMsg, "Invalid secret description provided")
}

func TestUpdateSecretWithBlankDescriptionThrowsError(t *testing.T) {
	// Given...
	secretName := "MYSECRET"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := "my-base64-token"
	keystoreEncoded := ""
	keystoreFile := ""
	keystorePassword := ""
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := ""
	secretType := ""
	description := "       "

	// Validation should fail, so no HTTP interactions should take place
	interactions := []utils.HttpInteraction{}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.NotNil(t, err, "SetSecret did not return an error as expected")
	errorMsg := err.Error()
	assert.Contains(t, errorMsg, "GAL1194E")
	assert.Contains(t, errorMsg, "Invalid secret description provided")
}

func TestUpdateSecretWithUnknownTypeThrowsError(t *testing.T) {
	// Given...
	secretName := "MYSECRET"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := "my-base64-token"
	keystoreEncoded := ""
	keystoreFile := ""
	keystorePassword := ""
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := ""
	secretType := "UNKNOWN"
	description := "this should fail!"

	// Validation should fail, so no HTTP interactions should take place
	interactions := []utils.HttpInteraction{}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.NotNil(t, err, "SetSecret did not return an error as expected")
	errorMsg := err.Error()
	assert.Contains(t, errorMsg, "GAL1186E")
	assert.Contains(t, errorMsg, "Invalid secret type provided")
}

func TestUpdateSecretWithInvalidFlagCombinationThrowsError(t *testing.T) {
	// Given...
	// Provide a unencoded credentials and base64-encoded ones
	secretName := "MYSECRET"
	username := "my-username"
	password := "my-password"
	token := "my-token"
	base64Username := "my-base64-username"
	base64Password := "my-base64-password"
	base64Token := "my-base64-token"
	keystoreEncoded := ""
	keystoreFile := ""
	keystorePassword := ""
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := ""
	secretType := ""
	description := ""

	// Validation should fail, so no HTTP interactions should take place
	interactions := []utils.HttpInteraction{}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.NotNil(t, err, "SetSecret did not return an error as expected")
	errorMsg := err.Error()
	assert.Contains(t, errorMsg, "GAL1193E")
	assert.Contains(t, errorMsg, "Invalid flag combination provided")
}

func TestSetSecretFailsWithNoExplanationErrorPayloadGivesCorrectMessage(t *testing.T) {
	// Given...
	secretName := "MYSECRET"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := "my-base64-token"
	keystoreEncoded := ""
	keystoreFile := ""
	keystorePassword := ""
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := ""
	secretType := ""
	description := ""

	// Create the expected HTTP interactions with the API server
	updateSecretInteraction := utils.NewHttpInteraction("/secrets/"+secretName, http.MethodPut)
	updateSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.WriteHeader(http.StatusInternalServerError)
	}

	interactions := []utils.HttpInteraction{
		updateSecretInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.NotNil(t, err, "SetSecret did not return an error but it should have")
	consoleText := err.Error()
	assert.Contains(t, consoleText, secretName)
	assert.Contains(t, consoleText, "GAL1187E")
	assert.Contains(t, consoleText, "Unexpected http status code 500 received from the server")
}

func TestSetSecretFailsWithNonJsonContentTypeExplanationErrorPayloadGivesCorrectMessage(t *testing.T) {
	// Given...
	secretName := "MYSECRET"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := "my-base64-token"
	keystoreEncoded := ""
	keystoreFile := ""
	keystorePassword := ""
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := ""
	secretType := ""
	description := ""

	// Create the expected HTTP interactions with the API server
	updateSecretInteraction := utils.NewHttpInteraction("/secrets/"+secretName, http.MethodPut)
	updateSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.WriteHeader(http.StatusInternalServerError)
		writer.Header().Set("Content-Type", "application/notJsonOnPurpose")
		writer.Write([]byte("something not json but non-zero-length."))
	}

	interactions := []utils.HttpInteraction{
		updateSecretInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.NotNil(t, err, "SetSecret did not return an error but it should have")
	consoleText := err.Error()
	assert.Contains(t, consoleText, secretName)
	assert.Contains(t, consoleText, strconv.Itoa(http.StatusInternalServerError))
	assert.Contains(t, consoleText, "GAL1191E")
	assert.Contains(t, consoleText, "Error details from the server are not in the json format")
}

func TestSetSecretFailsWithBadlyFormedJsonContentExplanationErrorPayloadGivesCorrectMessage(t *testing.T) {
	// Given...
	secretName := "MYSECRET"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := "my-base64-token"
	keystoreEncoded := ""
	keystoreFile := ""
	keystorePassword := ""
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := ""
	secretType := ""
	description := ""

	// Create the expected HTTP interactions with the API server
	updateSecretInteraction := utils.NewHttpInteraction("/secrets/"+secretName, http.MethodPut)
	updateSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusInternalServerError)
		writer.Write([]byte(`{ "this": "isBadJson because it doesnt end in a close braces" `))
	}

	interactions := []utils.HttpInteraction{
		updateSecretInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.NotNil(t, err, "SetSecret did not return an error but it should have")
	consoleText := err.Error()
	assert.Contains(t, consoleText, secretName)
	assert.Contains(t, consoleText, strconv.Itoa(http.StatusInternalServerError))
	assert.Contains(t, consoleText, "GAL1189E")
	assert.Contains(t, consoleText, "Error details from the server are not in a valid json format")
	assert.Contains(t, consoleText, "Cause: 'unexpected end of JSON input'")
}

func TestSetSecretFailsWithValidErrorResponsePayloadGivesCorrectMessage(t *testing.T) {
	// Given...
	secretName := "MYSECRET"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := "my-base64-token"
	keystoreEncoded := ""
	keystoreFile := ""
	keystorePassword := ""
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := ""
	secretType := ""
	description := ""
	apiErrorCode := 5000
	apiErrorMessage := "this is an error from the API server"

	// Create the expected HTTP interactions with the API server
	updateSecretInteraction := utils.NewHttpInteraction("/secrets/"+secretName, http.MethodPut)
	updateSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusInternalServerError)

		apiError := errors.GalasaAPIError{
			Code:    apiErrorCode,
			Message: apiErrorMessage,
		}
		apiErrorBytes, _ := json.Marshal(apiError)
		writer.Write(apiErrorBytes)
	}

	interactions := []utils.HttpInteraction{
		updateSecretInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.NotNil(t, err, "SetSecret did not return an error but it should have")
	consoleText := err.Error()
	assert.Contains(t, consoleText, secretName)
	assert.Contains(t, consoleText, strconv.Itoa(http.StatusInternalServerError))
	assert.Contains(t, consoleText, "GAL1190E")
	assert.Contains(t, consoleText, apiErrorMessage)
}

func TestSecretsSetFailsWithFailureToReadResponseBodyGivesCorrectMessage(t *testing.T) {
	// Given...
	secretName := "MYSECRET"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := "my-base64-token"
	keystoreEncoded := ""
	keystoreFile := ""
	keystorePassword := ""
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := ""
	secretType := ""
	description := ""

	// Create the expected HTTP interactions with the API server
	updateSecretInteraction := utils.NewHttpInteraction("/secrets/"+secretName, http.MethodPut)
	updateSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusInternalServerError)
		writer.Write([]byte(`{}`))
	}

	interactions := []utils.HttpInteraction{
		updateSecretInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReaderAsMock(true)
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.NotNil(t, err, "SetSecret did not return an error but it should have")
	consoleText := err.Error()
	assert.Contains(t, consoleText, secretName)
	assert.Contains(t, consoleText, strconv.Itoa(http.StatusInternalServerError))
	assert.Contains(t, consoleText, "GAL1188E")
	assert.Contains(t, consoleText, "Error details from the server could not be read")
}

func TestCanCreateAKeystoreSecretWithJKSType(t *testing.T) {
	// Given...
	secretName := "JKS_KEYSTORE"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := ""
	keystoreEncoded := "VGhpc0lzQUR1bW15S2V5c3RvcmVEYXRhRm9yVGVzdGluZ1B1cnBvc2VzT25seQ=="
	keystoreFile := ""
	keystorePassword := "changeit"
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := "JKS"
	secretType := ""
	description := "JKS keystore for testing"

	// Create the expected HTTP interactions with the API server
	createSecretInteraction := utils.NewHttpInteraction("/secrets/"+secretName, http.MethodPut)
	createSecretInteraction.ValidateRequestFunc = func(t *testing.T, req *http.Request) {
		secretRequest := readSecretRequestBody(req)
		assert.Equal(t, secretRequest.GetName(), secretName)
		assert.Equal(t, secretRequest.GetDescription(), description)

		// Verify keystore fields
		requestKeystore := secretRequest.GetKeystore()
		assert.Equal(t, requestKeystore.GetValue(), keystoreEncoded)
		assert.Empty(t, requestKeystore.GetEncoding())

		requestKeystorePassword := secretRequest.GetKeystorePassword()
		assert.Equal(t, requestKeystorePassword.GetValue(), keystorePassword)
		assert.Empty(t, requestKeystorePassword.GetEncoding())

		assert.Equal(t, secretRequest.GetKeystoreType(), keystoreType)

		// Verify other fields are empty
		requestUsername := secretRequest.GetUsername()
		assert.Empty(t, requestUsername.GetValue())
		requestPassword := secretRequest.GetPassword()
		assert.Empty(t, requestPassword.GetValue())
		requestToken := secretRequest.GetToken()
		assert.Empty(t, requestToken.GetValue())
	}

	createSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.WriteHeader(http.StatusCreated)
	}

	interactions := []utils.HttpInteraction{
		createSecretInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.Nil(t, err, "SetSecret returned an unexpected error")
	assert.Empty(t, console.ReadText(), "The console was written to on a successful creation, it should be empty")
}

func TestCanCreateAKeystoreSecretWithPKCS12Type(t *testing.T) {
	// Given...
	secretName := "PKCS12_KEYSTORE"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := ""
	keystoreEncoded := "UEtDUzEyS2V5c3RvcmVEYXRhRm9yVGVzdGluZ1B1cnBvc2VzT25seUhlcmU="
	keystoreFile := ""
	keystorePassword := "mysecretpassword"
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := "PKCS12"
	secretType := ""
	description := "PKCS12 keystore for testing"

	// Create the expected HTTP interactions with the API server
	createSecretInteraction := utils.NewHttpInteraction("/secrets/"+secretName, http.MethodPut)
	createSecretInteraction.ValidateRequestFunc = func(t *testing.T, req *http.Request) {
		secretRequest := readSecretRequestBody(req)
		assert.Equal(t, secretRequest.GetName(), secretName)
		assert.Equal(t, secretRequest.GetDescription(), description)

		// Verify keystore fields
		requestKeystore := secretRequest.GetKeystore()
		assert.Equal(t, requestKeystore.GetValue(), keystoreEncoded)
		assert.Empty(t, requestKeystore.GetEncoding())

		requestKeystorePassword := secretRequest.GetKeystorePassword()
		assert.Equal(t, requestKeystorePassword.GetValue(), keystorePassword)
		assert.Empty(t, requestKeystorePassword.GetEncoding())

		assert.Equal(t, secretRequest.GetKeystoreType(), keystoreType)

		// Verify other fields are empty
		requestUsername := secretRequest.GetUsername()
		assert.Empty(t, requestUsername.GetValue())
		requestPassword := secretRequest.GetPassword()
		assert.Empty(t, requestPassword.GetValue())
		requestToken := secretRequest.GetToken()
		assert.Empty(t, requestToken.GetValue())
	}

	createSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.WriteHeader(http.StatusCreated)
	}

	interactions := []utils.HttpInteraction{
		createSecretInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.Nil(t, err, "SetSecret returned an unexpected error")
	assert.Empty(t, console.ReadText(), "The console was written to on a successful creation, it should be empty")
}

func TestCanCreateAKeystoreSecretFromFile(t *testing.T) {
	// Given...
	secretName := "FILE_KEYSTORE"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := ""
	keystoreEncoded := ""
	keystoreFile := "/path/to/keystore.p12"
	keystorePassword := "changeit"
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := "PKCS12"
	secretType := ""
	description := "Keystore from file for testing"

	// Dummy keystore file content (doesn't need to be a real keystore)
	keystoreFileContent := []byte("This is dummy keystore file content for testing")

	// Create the expected HTTP interactions with the API server
	createSecretInteraction := utils.NewHttpInteraction("/secrets/"+secretName, http.MethodPut)
	createSecretInteraction.ValidateRequestFunc = func(t *testing.T, req *http.Request) {
		secretRequest := readSecretRequestBody(req)
		assert.Equal(t, secretRequest.GetName(), secretName)
		assert.Equal(t, secretRequest.GetDescription(), description)

		// Verify keystore fields - should contain base64 encoded data from file
		requestKeystore := secretRequest.GetKeystore()
		assert.NotEmpty(t, requestKeystore.GetValue())
		assert.Empty(t, requestKeystore.GetEncoding())

		requestKeystorePassword := secretRequest.GetKeystorePassword()
		assert.Equal(t, requestKeystorePassword.GetValue(), keystorePassword)
		assert.Empty(t, requestKeystorePassword.GetEncoding())

		assert.Equal(t, secretRequest.GetKeystoreType(), keystoreType)
	}

	createSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.WriteHeader(http.StatusCreated)
	}

	interactions := []utils.HttpInteraction{
		createSecretInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// Set up the mock file system to return keystore data when the file is read
	mockFileSystem.WriteBinaryFile(keystoreFile, keystoreFileContent)

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.Nil(t, err, "SetSecret returned an unexpected error")
	assert.Empty(t, console.ReadText(), "The console was written to on a successful creation, it should be empty")
}

func TestCanCreateAKeystoreSecretWithBase64EncodedKeystore(t *testing.T) {
	// Given...
	secretName := "BASE64_KEYSTORE"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := ""
	keystoreEncoded := ""
	keystoreFile := ""
	keystorePassword := "changeit"
	base64KeystoreEncoded := "QmFzZTY0RW5jb2RlZEtleXN0b3JlRGF0YUZvclRlc3RpbmdQdXJwb3Nlcw=="
	base64KeystorePassword := ""
	keystoreType := "PKCS12"
	secretType := ""
	description := "Base64 encoded keystore for testing"

	// Create the expected HTTP interactions with the API server
	createSecretInteraction := utils.NewHttpInteraction("/secrets/"+secretName, http.MethodPut)
	createSecretInteraction.ValidateRequestFunc = func(t *testing.T, req *http.Request) {
		secretRequest := readSecretRequestBody(req)
		assert.Equal(t, secretRequest.GetName(), secretName)
		assert.Equal(t, secretRequest.GetDescription(), description)

		// Verify keystore fields
		requestKeystore := secretRequest.GetKeystore()
		assert.Equal(t, requestKeystore.GetValue(), base64KeystoreEncoded)
		assert.Equal(t, requestKeystore.GetEncoding(), "base64")

		requestKeystorePassword := secretRequest.GetKeystorePassword()
		assert.Equal(t, requestKeystorePassword.GetValue(), keystorePassword)
		assert.Empty(t, requestKeystorePassword.GetEncoding())

		assert.Equal(t, secretRequest.GetKeystoreType(), keystoreType)
	}

	createSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.WriteHeader(http.StatusCreated)
	}

	interactions := []utils.HttpInteraction{
		createSecretInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.Nil(t, err, "SetSecret returned an unexpected error")
	assert.Empty(t, console.ReadText(), "The console was written to on a successful creation, it should be empty")
}

func TestCanCreateAKeystoreSecretWithBase64EncodedPassword(t *testing.T) {
	// Given...
	secretName := "BASE64_PASSWORD_KEYSTORE"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := ""
	keystoreEncoded := "S2V5c3RvcmVXaXRoQmFzZTY0UGFzc3dvcmRGb3JUZXN0aW5nUHVycG9zZXM="
	keystoreFile := ""
	keystorePassword := ""
	base64KeystoreEncoded := ""
	base64KeystorePassword := "Y2hhbmdlaXQ=" // "changeit" in base64
	keystoreType := "PKCS12"
	secretType := ""
	description := "Keystore with base64 encoded password for testing"

	// Create the expected HTTP interactions with the API server
	createSecretInteraction := utils.NewHttpInteraction("/secrets/"+secretName, http.MethodPut)
	createSecretInteraction.ValidateRequestFunc = func(t *testing.T, req *http.Request) {
		secretRequest := readSecretRequestBody(req)
		assert.Equal(t, secretRequest.GetName(), secretName)
		assert.Equal(t, secretRequest.GetDescription(), description)

		// Verify keystore fields
		requestKeystore := secretRequest.GetKeystore()
		assert.Equal(t, requestKeystore.GetValue(), keystoreEncoded)
		assert.Empty(t, requestKeystore.GetEncoding())

		requestKeystorePassword := secretRequest.GetKeystorePassword()
		assert.Equal(t, requestKeystorePassword.GetValue(), base64KeystorePassword)
		assert.Equal(t, requestKeystorePassword.GetEncoding(), "base64")

		assert.Equal(t, secretRequest.GetKeystoreType(), keystoreType)
	}

	createSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.WriteHeader(http.StatusCreated)
	}

	interactions := []utils.HttpInteraction{
		createSecretInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.Nil(t, err, "SetSecret returned an unexpected error")
	assert.Empty(t, console.ReadText(), "The console was written to on a successful creation, it should be empty")
}

func TestCanUpdateAKeystoreSecretWithNewKeystoreData(t *testing.T) {
	// Given...
	secretName := "UPDATE_KEYSTORE_DATA"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := ""
	keystoreEncoded := "TmV3S2V5c3RvcmVEYXRhRm9yVXBkYXRlVGVzdGluZ1B1cnBvc2VzPT0="
	keystoreFile := ""
	keystorePassword := "newpassword"
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := "JKS"
	secretType := ""
	description := "Updated keystore with new data"

	// Create the expected HTTP interactions with the API server
	updateSecretInteraction := utils.NewHttpInteraction("/secrets/"+secretName, http.MethodPut)
	updateSecretInteraction.ValidateRequestFunc = func(t *testing.T, req *http.Request) {
		secretRequest := readSecretRequestBody(req)
		assert.Equal(t, secretRequest.GetName(), secretName)
		assert.Equal(t, secretRequest.GetDescription(), description)

		// Verify keystore fields
		requestKeystore := secretRequest.GetKeystore()
		assert.Equal(t, requestKeystore.GetValue(), keystoreEncoded)
		assert.Empty(t, requestKeystore.GetEncoding())

		requestKeystorePassword := secretRequest.GetKeystorePassword()
		assert.Equal(t, requestKeystorePassword.GetValue(), keystorePassword)
		assert.Empty(t, requestKeystorePassword.GetEncoding())

		assert.Equal(t, secretRequest.GetKeystoreType(), keystoreType)
	}

	updateSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.WriteHeader(http.StatusNoContent)
	}

	interactions := []utils.HttpInteraction{
		updateSecretInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.Nil(t, err, "SetSecret returned an unexpected error")
	assert.Empty(t, console.ReadText(), "The console was written to on a successful update, it should be empty")
}

func TestCanUpdateAKeystoreSecretWithNewPassword(t *testing.T) {
	// Given...
	secretName := "UPDATE_KEYSTORE_PASSWORD"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := ""
	keystoreEncoded := "U2FtZUtleXN0b3JlRGF0YUJ1dE5ld1Bhc3N3b3JkRm9yVGVzdGluZz0="
	keystoreFile := ""
	keystorePassword := "updatedpassword"
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := "PKCS12"
	secretType := ""
	description := "Updated keystore password"

	// Create the expected HTTP interactions with the API server
	updateSecretInteraction := utils.NewHttpInteraction("/secrets/"+secretName, http.MethodPut)
	updateSecretInteraction.ValidateRequestFunc = func(t *testing.T, req *http.Request) {
		secretRequest := readSecretRequestBody(req)
		assert.Equal(t, secretRequest.GetName(), secretName)
		assert.Equal(t, secretRequest.GetDescription(), description)

		// Verify keystore fields
		requestKeystore := secretRequest.GetKeystore()
		assert.Equal(t, requestKeystore.GetValue(), keystoreEncoded)
		assert.Empty(t, requestKeystore.GetEncoding())

		requestKeystorePassword := secretRequest.GetKeystorePassword()
		assert.Equal(t, requestKeystorePassword.GetValue(), keystorePassword)
		assert.Empty(t, requestKeystorePassword.GetEncoding())

		assert.Equal(t, secretRequest.GetKeystoreType(), keystoreType)
	}

	updateSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.WriteHeader(http.StatusNoContent)
	}

	interactions := []utils.HttpInteraction{
		updateSecretInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.Nil(t, err, "SetSecret returned an unexpected error")
	assert.Empty(t, console.ReadText(), "The console was written to on a successful update, it should be empty")
}

func TestCanUpdateAKeystoreSecretType(t *testing.T) {
	// Given...
	secretName := "UPDATE_KEYSTORE_TYPE"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := ""
	keystoreEncoded := "S2V5c3RvcmVXaXRoVHlwZUNoYW5nZUZyb21KS1NUb1BLQ1MxMj0="
	keystoreFile := ""
	keystorePassword := "mypassword"
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := "PKCS12"
	secretType := ""
	description := "Updated keystore type from JKS to PKCS12"

	// Create the expected HTTP interactions with the API server
	updateSecretInteraction := utils.NewHttpInteraction("/secrets/"+secretName, http.MethodPut)
	updateSecretInteraction.ValidateRequestFunc = func(t *testing.T, req *http.Request) {
		secretRequest := readSecretRequestBody(req)
		assert.Equal(t, secretRequest.GetName(), secretName)
		assert.Equal(t, secretRequest.GetDescription(), description)

		// Verify keystore fields
		requestKeystore := secretRequest.GetKeystore()
		assert.Equal(t, requestKeystore.GetValue(), keystoreEncoded)
		assert.Empty(t, requestKeystore.GetEncoding())

		requestKeystorePassword := secretRequest.GetKeystorePassword()
		assert.Equal(t, requestKeystorePassword.GetValue(), keystorePassword)
		assert.Empty(t, requestKeystorePassword.GetEncoding())

		assert.Equal(t, secretRequest.GetKeystoreType(), keystoreType)
	}

	updateSecretInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.WriteHeader(http.StatusNoContent)
	}

	interactions := []utils.HttpInteraction{
		updateSecretInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.Nil(t, err, "SetSecret returned an unexpected error")
	assert.Empty(t, console.ReadText(), "The console was written to on a successful update, it should be empty")
}

func TestUpdateKeystoreSecretWithInvalidTypeThrowsError(t *testing.T) {
	// Given...
	secretName := "INVALID_TYPE_KEYSTORE"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := ""
	keystoreEncoded := "VmFsaWRLZXlzdG9yZURhdGFCdXRJbnZhbGlkVHlwZT0="
	keystoreFile := ""
	keystorePassword := "mypassword"
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := "INVALID_TYPE"
	secretType := ""
	description := "Keystore with invalid type"

	// Validation should fail, so no HTTP interactions should take place
	interactions := []utils.HttpInteraction{}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.NotNil(t, err, "SetSecret did not return an error as expected")
	errorMsg := err.Error()
	assert.Contains(t, errorMsg, "GAL1291E")
	assert.Contains(t, errorMsg, "Invalid keystore type")
}

func TestUpdateKeystoreSecretWithBothEncodedAndFileThrowsError(t *testing.T) {
	// Given...
	secretName := "BOTH_ENCODED_AND_FILE"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := ""
	keystoreEncoded := "S2V5c3RvcmVFbmNvZGVkRGF0YT0="
	keystoreFile := "/path/to/keystore.jks"
	keystorePassword := "mypassword"
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := "JKS"
	secretType := ""
	description := "Both keystoreEncoded and keystoreFile provided"

	// Validation should fail, so no HTTP interactions should take place
	interactions := []utils.HttpInteraction{}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.NotNil(t, err, "SetSecret did not return an error as expected")
	errorMsg := err.Error()
	assert.Contains(t, errorMsg, "GAL1193E")
	assert.Contains(t, errorMsg, "Invalid flag combination provided")
}

func TestUpdateKeystoreSecretWithBothEncodedAndBase64EncodedThrowsError(t *testing.T) {
	// Given...
	secretName := "BOTH_ENCODED_TYPES"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := ""
	keystoreEncoded := "S2V5c3RvcmVFbmNvZGVkRGF0YT0="
	keystoreFile := ""
	keystorePassword := "mypassword"
	base64KeystoreEncoded := "QmFzZTY0S2V5c3RvcmVFbmNvZGVkRGF0YT0="
	base64KeystorePassword := ""
	keystoreType := "PKCS12"
	secretType := ""
	description := "Both keystoreEncoded and base64KeystoreEncoded provided"

	// Validation should fail, so no HTTP interactions should take place
	interactions := []utils.HttpInteraction{}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.NotNil(t, err, "SetSecret did not return an error as expected")
	errorMsg := err.Error()
	assert.Contains(t, errorMsg, "GAL1193E")
	assert.Contains(t, errorMsg, "Invalid flag combination provided")
}

func TestUpdateKeystoreSecretWithBothPasswordAndBase64PasswordThrowsError(t *testing.T) {
	// Given...
	secretName := "BOTH_PASSWORD_TYPES"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := ""
	keystoreEncoded := "S2V5c3RvcmVEYXRhV2l0aEJvdGhQYXNzd29yZHM9"
	keystoreFile := ""
	keystorePassword := "plainpassword"
	base64KeystoreEncoded := ""
	base64KeystorePassword := "YmFzZTY0cGFzc3dvcmQ="
	keystoreType := "JKS"
	secretType := ""
	description := "Both keystorePassword and base64KeystorePassword provided"

	// Validation should fail, so no HTTP interactions should take place
	interactions := []utils.HttpInteraction{}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.NotNil(t, err, "SetSecret did not return an error as expected")
	errorMsg := err.Error()
	assert.Contains(t, errorMsg, "GAL1193E")
	assert.Contains(t, errorMsg, "Invalid flag combination provided")
}

func TestCreateKeystoreSecretWithoutPasswordThrowsError(t *testing.T) {
	// Given...
	secretName := "KEYSTORE_NO_PASSWORD"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := ""
	keystoreEncoded := "S2V5c3RvcmVXaXRob3V0UGFzc3dvcmQ9"
	keystoreFile := ""
	keystorePassword := ""
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := "JKS"
	secretType := ""
	description := "Keystore without password"

	// Validation should fail, so no HTTP interactions should take place
	interactions := []utils.HttpInteraction{}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.NotNil(t, err, "SetSecret did not return an error as expected")
	errorMsg := err.Error()
	assert.Contains(t, errorMsg, "GAL1193E")
	assert.Contains(t, errorMsg, "Invalid flag combination provided")
}

func TestCreateKeystoreSecretWithBlankPasswordThrowsError(t *testing.T) {
	// Given...
	secretName := "KEYSTORE_BLANK_PASSWORD"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := ""
	keystoreEncoded := "S2V5c3RvcmVXaXRoQmxhbmtQYXNzd29yZD0="
	keystoreFile := ""
	keystorePassword := "    "
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := "PKCS12"
	secretType := ""
	description := "Keystore with blank password"

	// Validation should fail, so no HTTP interactions should take place
	interactions := []utils.HttpInteraction{}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.NotNil(t, err, "SetSecret did not return an error as expected")
	errorMsg := err.Error()
	assert.Contains(t, errorMsg, "GAL1193E")
	assert.Contains(t, errorMsg, "Invalid flag combination provided")
}

func TestCreateKeystoreSecretFromNonExistentFileThrowsError(t *testing.T) {
	// Given...
	secretName := "KEYSTORE_FILE_NOT_FOUND"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := ""
	keystoreEncoded := ""
	keystoreFile := "/path/to/nonexistent/keystore.jks"
	keystorePassword := "mypassword"
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := "JKS"
	secretType := ""
	description := "Keystore from non-existent file"

	// Validation should fail, so no HTTP interactions should take place
	interactions := []utils.HttpInteraction{}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.NotNil(t, err, "SetSecret did not return an error as expected")
	errorMsg := err.Error()
	assert.Contains(t, errorMsg, "file does not exist")
}

func TestCreateKeystoreSecretFromUnreadableFileThrowsError(t *testing.T) {
	// Given...
	secretName := "KEYSTORE_FILE_UNREADABLE"
	username := ""
	password := ""
	token := ""
	base64Username := ""
	base64Password := ""
	base64Token := ""
	keystoreEncoded := ""
	keystoreFile := "/path/to/unreadable/keystore.jks"
	keystorePassword := "mypassword"
	base64KeystoreEncoded := ""
	base64KeystorePassword := ""
	keystoreType := "PKCS12"
	secretType := ""
	description := "Keystore from unreadable file"

	// Validation should fail, so no HTTP interactions should take place
	interactions := []utils.HttpInteraction{}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()
	mockFileSystem := files.NewMockFileSystem()

	// Override the ReadBinaryFile function to simulate a read error
	mockFS := mockFileSystem.(*files.MockFileSystem)
	mockFS.VirtualFunction_ReadBinaryFile = func(filePath string) ([]byte, error) {
		return nil, fmt.Errorf("permission denied: cannot read file %s", filePath)
	}

	// When...
	err := SetSecret(
		secretName,
		username,
		password,
		token,
		base64Username,
		base64Password,
		base64Token,
		keystoreEncoded,
		keystoreFile,
		keystorePassword,
		base64KeystoreEncoded,
		base64KeystorePassword,
		keystoreType,
		secretType,
		description,
		console,
		apiClient,
		mockByteReader,
		mockFileSystem)

	// Then...
	assert.NotNil(t, err, "SetSecret did not return an error as expected")
	errorMsg := err.Error()
	assert.Contains(t, errorMsg, "permission denied")
	assert.Contains(t, errorMsg, keystoreFile)
}
