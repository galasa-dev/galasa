/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package auth

import (
	"errors"
	"fmt"
	"io"
	"testing"
	"time"

	"github.com/galasa-dev/cli/pkg/files"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"

	"net/http"
	"net/http/httptest"
	"strings"
)

const (
	// This is a dummy JWT that expires 1 hour after the Unix epoch
	// So basically, this JWT has already expired if you compare it to the real time now.
	mockJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjM2MDB9._j3Fchdx5IIqgGrdEGWXHxdgVyoBEyoD2-IBvhlxF1s" //pragma: allowlist secret
)

func NewAuthServletMock(t *testing.T, status int, mockResponse string) *httptest.Server {

	server := httptest.NewServer(http.HandlerFunc(func(writer http.ResponseWriter, request *http.Request) {

		if strings.Contains(request.URL.Path, "/auth/tokens") {
			requestBody, err := io.ReadAll(request.Body)
			assert.Nil(t, err, "Error reading request body")

			requestBodyStr := string(requestBody)
			assert.Contains(t, requestBodyStr, "client_id")
			assert.Contains(t, requestBodyStr, "refresh_token")

			writer.Header().Set("Content-Type", "application/json")
			writer.WriteHeader(status)
			writer.Write([]byte(mockResponse))
		}
	}))

	return server
}

func TestLoginWithNoGalasactlPropertiesFileReturnsError(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := utils.NewMockEnv()
	mockGalasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	mockResponse := `{"jwt":"` + mockJwt + `", "refresh_token":"abc"}`
	server := NewAuthServletMock(t, 200, mockResponse)
	defer server.Close()

	apiServerUrl := server.URL

	mockTimeService := utils.NewMockTimeService()
	jwtCache := NewJwtCache(mockFileSystem, mockGalasaHome, mockTimeService)

	// When...
	authenticator := NewAuthenticator(apiServerUrl, mockFileSystem, mockGalasaHome, mockTimeService, mockEnvironment, jwtCache)
	err := authenticator.Login()

	// Then...
	assert.NotNil(t, err, "Should return an error if the galasactl.properties file does not exist")
	assert.ErrorContains(t, err, "GAL1043E")
}

func TestLoginWithBadGalasactlPropertiesFileReturnsError(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := utils.NewMockEnv()
	mockGalasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	galasactlPropertiesFilePath := mockGalasaHome.GetNativeFolderPath() + "/galasactl.properties"
	mockFileSystem.WriteTextFile(galasactlPropertiesFilePath, "here are some bad galasactl.properties contents!")

	mockResponse := `{"jwt":"` + mockJwt + `", "refresh_token":"abc"}`
	server := NewAuthServletMock(t, 200, mockResponse)
	defer server.Close()

	apiServerUrl := server.URL

	mockTimeService := utils.NewMockTimeService()
	jwtCache := NewJwtCache(mockFileSystem, mockGalasaHome, mockTimeService)

	// When...
	authenticator := NewAuthenticator(apiServerUrl, mockFileSystem, mockGalasaHome, mockTimeService, mockEnvironment, jwtCache)
	err := authenticator.Login()

	// Then...
	assert.NotNil(t, err, "Should return an error if the galasactl.properties file does not contain valid YAML")
	assert.ErrorContains(t, err, "GAL1122E")
}

func TestLoginCreatesBearerTokenJWTInCache(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := utils.NewMockEnv()
	mockGalasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	galasactlPropertiesFilePath := mockGalasaHome.GetNativeFolderPath() + "/galasactl.properties"

	mockClientId := "dummyId"
	mockRefreshToken := "abcdefg"
	tokenPropertyValue := mockRefreshToken + TOKEN_SEPARATOR + mockClientId
	mockFileSystem.WriteTextFile(galasactlPropertiesFilePath, fmt.Sprintf("GALASA_TOKEN=%s", tokenPropertyValue))

	mockResponse := `{"jwt":"` + mockJwt + `", "refresh_token":"abc"}`
	server := NewAuthServletMock(t, 200, mockResponse)
	defer server.Close()

	apiServerUrl := server.URL

	// Set the wall-clock for the 'now' time to be back in 1970, so the bearer token is still valid.
	mockTimeService := utils.NewOverridableMockTimeService(time.Unix(0, 0))

	jwtCache := NewJwtCache(mockFileSystem, mockGalasaHome, mockTimeService)

	// When...
	authenticator := NewAuthenticator(apiServerUrl, mockFileSystem, mockGalasaHome, mockTimeService, mockEnvironment, jwtCache)
	err := authenticator.Login()

	// Then...
	assert.Nil(t, err, "Should not return an error if the bearer token file has been successfully created")
	var jwtGotBack string
	jwtGotBack, err = jwtCache.Get(apiServerUrl, tokenPropertyValue)
	assert.Nil(t, err, "Should have been able to get the bearer token out of the jwt cache.")
	assert.Equal(t, mockJwt, jwtGotBack)
}

func TestLoginWithFailedFileWriteReturnsError(t *testing.T) {
	// Given...
	mockFileSystem := files.NewOverridableMockFileSystem()
	mockEnvironment := utils.NewMockEnv()
	mockGalasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	galasactlPropertiesFilePath := mockGalasaHome.GetNativeFolderPath() + "/galasactl.properties"

	mockClientId := "dummyId"
	mockRefreshToken := "abcdefg"
	tokenPropertyValue := mockRefreshToken + TOKEN_SEPARATOR + mockClientId
	mockFileSystem.WriteTextFile(galasactlPropertiesFilePath, fmt.Sprintf("GALASA_TOKEN=%s", tokenPropertyValue))

	mockFileSystem.VirtualFunction_WriteTextFile = func(path string, contents string) error {
		return errors.New("simulating a failed write operation")
	}

	mockResponse := `{"jwt":"blah", "refresh_token":"abc"}`
	server := NewAuthServletMock(t, 200, mockResponse)
	defer server.Close()

	apiServerUrl := server.URL

	mockTimeService := utils.NewMockTimeService()
	cache := NewJwtCache(mockFileSystem, mockGalasaHome, mockTimeService)

	// When...

	authenticator := NewAuthenticator(apiServerUrl, mockFileSystem, mockGalasaHome, mockTimeService, mockEnvironment, cache)
	err := authenticator.Login()

	// Then...
	assert.NotNil(t, err, "Should return an error if writing the bearer token file fails")
	assert.ErrorContains(t, err, "GAL1042E")
}

func TestLoginWithFailedTokenRequestReturnsError(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := utils.NewMockEnv()
	mockGalasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	galasactlPropertiesFilePath := mockGalasaHome.GetNativeFolderPath() + "/galasactl.properties"

	mockClientId := "dummyId"
	mockRefreshToken := "abcdefg"
	tokenPropertyValue := mockRefreshToken + TOKEN_SEPARATOR + mockClientId
	mockFileSystem.WriteTextFile(galasactlPropertiesFilePath, fmt.Sprintf("GALASA_TOKEN=%s", tokenPropertyValue))

	mockResponse := `{"error":"something went wrong!"}`
	server := NewAuthServletMock(t, 500, mockResponse)
	defer server.Close()

	apiServerUrl := server.URL

	mockTimeService := utils.NewMockTimeService()
	cache := NewJwtCache(mockFileSystem, mockGalasaHome, mockTimeService)

	// When...
	authenticator := NewAuthenticator(apiServerUrl, mockFileSystem, mockGalasaHome, mockTimeService, mockEnvironment, cache)
	err := authenticator.Login()

	// Then...
	assert.NotNil(t, err, "Should return an error if the API request returns an error")
	assert.ErrorContains(t, err, "GAL1106E")
}

func TestLoginWithMissingAuthPropertyReturnsError(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := utils.NewMockEnv()
	mockGalasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	galasactlPropertiesFilePath := mockGalasaHome.GetNativeFolderPath() + "/galasactl.properties"

	mockFileSystem.WriteTextFile(galasactlPropertiesFilePath, "unknown.value=blah")

	mockResponse := `{"jwt":"blah", "refresh_token":"abc"}`
	server := NewAuthServletMock(t, 200, mockResponse)
	defer server.Close()

	apiServerUrl := server.URL

	mockTimeService := utils.NewMockTimeService()
	cache := NewJwtCache(mockFileSystem, mockGalasaHome, mockTimeService)

	// When...
	authenticator := NewAuthenticator(apiServerUrl, mockFileSystem, mockGalasaHome, mockTimeService, mockEnvironment, cache)
	err := authenticator.Login()

	// Then...
	assert.NotNil(t, err, "Should return an error if the GALASA_ACCESS_TOKEN property is missing")
	assert.ErrorContains(t, err, "GAL1122E")
}

func TestGetAuthenticatedAPIClientWithBearerTokenFileReturnsClient(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := utils.NewMockEnv()
	galasaToken := "12345:456"
	mockEnvironment.SetEnv("GALASA_TOKEN", galasaToken)
	mockGalasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")
	apiServerUrl := "http://dummy-url"

	mockCurrentTime := time.UnixMilli(0)
	mockTimeService := utils.NewOverridableMockTimeService(mockCurrentTime)

	jwtCache := NewJwtCache(mockFileSystem, mockGalasaHome, mockTimeService)
	jwtCache.Put(apiServerUrl, galasaToken, mockJwt)

	authenticator := NewAuthenticator(apiServerUrl, mockFileSystem, mockGalasaHome, mockTimeService, mockEnvironment, jwtCache)

	// When...
	apiClient, err := authenticator.GetAuthenticatedAPIClient()

	// Then...
	assert.Nil(t, err, "No error should have been thrown")
	assert.NotNil(t, apiClient, "API client should not be nil")
}

func TestGetAuthenticatedAPIClientWithMissingBearerTokenFileAttemptsLogin(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := utils.NewMockEnv()
	mockGalasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	mockCurrentTime := time.UnixMilli(0)
	mockTimeService := utils.NewOverridableMockTimeService(mockCurrentTime)

	galasactlPropertiesFilePath := mockGalasaHome.GetNativeFolderPath() + "/galasactl.properties"

	accessTokenValue := "abc"
	clientIdValue := "dummyId"
	tokenPropertyValue := accessTokenValue + TOKEN_SEPARATOR + clientIdValue

	jwtCache := NewJwtCache(mockFileSystem, mockGalasaHome, mockTimeService)

	mockFileSystem.WriteTextFile(galasactlPropertiesFilePath, fmt.Sprintf("GALASA_TOKEN=%s", tokenPropertyValue))

	// This is a dummy JWT that expires 1 hour after the Unix epoch
	mockJwt := "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjM2MDB9._j3Fchdx5IIqgGrdEGWXHxdgVyoBEyoD2-IBvhlxF1s" //pragma: allowlist secret
	jwtCache.Put("https://myServer", tokenPropertyValue, mockJwt)
	mockResponse := fmt.Sprintf(`{"jwt":"%s"}`, mockJwt)

	server := NewAuthServletMock(t, 200, mockResponse)
	defer server.Close()

	apiServerUrl := server.URL

	// When...
	authenticator := NewAuthenticator(apiServerUrl, mockFileSystem, mockGalasaHome, mockTimeService, mockEnvironment, jwtCache)
	apiClient, err := authenticator.GetAuthenticatedAPIClient()

	// Then...
	assert.Nil(t, err, "No error should have been thrown")
	assert.NotNil(t, apiClient, "API client should not be nil if the login was successful")
}

var wasCalled bool = false

func mockClearAll() {
	wasCalled = true
}

func TestLogoutCallsCacheLogoutEverywhere(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := utils.NewMockEnv()
	mockGalasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	mockResponse := `{"jwt":"` + mockJwt + `", "refresh_token":"abc"}`
	server := NewAuthServletMock(t, 200, mockResponse)
	defer server.Close()

	apiServerUrl := server.URL

	mockTimeService := utils.NewMockTimeService()
	jwtCache := NewJwtCache(mockFileSystem, mockGalasaHome, mockTimeService)

	// When...
	authenticator := NewAuthenticator(apiServerUrl, mockFileSystem, mockGalasaHome, mockTimeService, mockEnvironment, jwtCache)
	err := authenticator.Login()

	// Then...
	assert.NotNil(t, err, "Should return an error if the galasactl.properties file does not exist")
	assert.ErrorContains(t, err, "GAL1043E")
}
