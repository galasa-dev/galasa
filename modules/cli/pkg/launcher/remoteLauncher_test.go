/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package launcher

import (
	"crypto/hmac"
	"crypto/sha256"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/auth"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

const (
	// This is a dummy JWT that expires 1 hour after the Unix epoch
	// This JWT has already expired if you compare it to the real time now.
	mockExpiredJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjM2MDB9._j3Fchdx5IIqgGrdEGWXHxdgVyoBEyoD2-IBvhlxF1s" //pragma: allowlist secret
)

func createValidMockJwt() string {
	expirationTime := time.Now().Add(1 * time.Hour).Unix()

	headerClaimsJsonStr := `{ "alg": "HS256", "typ": "JWT" }`
	claimsJsonStr := fmt.Sprintf(`{ "exp": "%v" }`, expirationTime)

	// Base64 encode the JWT claims
	encodedHeaderClaims := base64.RawURLEncoding.EncodeToString([]byte(headerClaimsJsonStr))
	encodedClaims := base64.RawURLEncoding.EncodeToString([]byte(claimsJsonStr))

	// Concatenate the encoded header and body claims
	toSign := encodedHeaderClaims + "." + encodedClaims

	// Sign the token with HMAC-SHA256
	signingKey := "my-signing-key"
	hash := hmac.New(sha256.New, []byte(signingKey))
	hash.Write([]byte(toSign))
	signature := base64.RawURLEncoding.EncodeToString(hash.Sum(nil))

	// Create the signed JWT
	validJwt := toSign + "." + signature
	return validJwt
}

func TestProcessingGoodPropertiesExtractsStreamsOk(t *testing.T) {

	var inputProperties []galasaapi.GalasaProperty = make([]galasaapi.GalasaProperty, 0)

	name1 := "thames"
	name1full := "test.stream." + name1 + ".repo"
	name2 := "avon"
	name2full := "test.stream." + name2 + ".repo"

	inputProperties = append(inputProperties, galasaapi.GalasaProperty{
		Metadata: &galasaapi.GalasaPropertyMetadata{
			Name: &name1full,
		},
	})

	inputProperties = append(inputProperties, galasaapi.GalasaProperty{
		Metadata: &galasaapi.GalasaPropertyMetadata{
			Name: &name2full,
		},
	})

	streams, err := getStreamNamesFromProperties(inputProperties)
	assert.Nil(t, err)
	assert.NotNil(t, streams)
	assert.Equal(t, 2, len(streams))

	assert.Equal(t, streams[0], name1)
	assert.Equal(t, streams[1], name2)
}

func TestProcessingEmptyPropertiesListExtractsZeroStreamsOk(t *testing.T) {

	var inputProperties []galasaapi.GalasaProperty = make([]galasaapi.GalasaProperty, 0)

	streams, err := getStreamNamesFromProperties(inputProperties)

	assert.Nil(t, err)
	assert.NotNil(t, streams)
	assert.Equal(t, 0, len(streams))
}

func TestGetTestCatalogHttpErrorGetsReported(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		fmt.Printf("URL arrived at the mock test server: %s\n", r.RequestURI)
		switch r.RequestURI {
		case "/cps/framework/properties?prefix=test.stream.myStream&suffix=location":

			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusOK)

			name := "mycpsPropName"
			value := "a duff value" // This is intentionally duff, which will cause an HTTP error when the production code tries to GET using this as a URL.
			payload := []galasaapi.GalasaProperty{
				{
					Metadata: &galasaapi.GalasaPropertyMetadata{
						Name: &name,
					},
					Data: &galasaapi.GalasaPropertyData{
						Value: &value,
					},
				},
			}
			payloadBytes, _ := json.Marshal(payload)
			w.Write(payloadBytes)

			fmt.Printf("mock server sending payload: %s\n", string(payloadBytes))

		}
	}))
	defer server.Close()

	mockFactory := utils.NewMockFactory()
	apiServerUrl := server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	authenticator := utils.NewMockAuthenticatorWithAPIClient(apiClient)
	mockFactory.Authenticator = authenticator

	mockFileSystem := mockFactory.GetFileSystem()
	mockEnvironment := mockFactory.GetEnvironment()
	mockGalasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")
	mockFileSystem.WriteTextFile(mockGalasaHome.GetUrlFolderPath()+"/bootstrap.properties", "")

	bootstrap := ""
	maxAttempts := 3
	retryBackoffSeconds := 1

	commsClient, _ := api.NewAPICommsClient(bootstrap, maxAttempts, float64(retryBackoffSeconds), mockFactory, mockGalasaHome)

	launcher := NewRemoteLauncher(commsClient)

	_, err := launcher.GetTestCatalog("myStream")

	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1144E") // Failed to get the test catalog.
}

func TestGetRunsByGroupWithInvalidBearerTokenGetsNewTokenOk(t *testing.T) {
	groupId := "group1"

	initialLoginOperation := utils.NewHttpInteraction("/auth/tokens", http.MethodPost)
	initialLoginOperation.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusCreated)

		mockResponse := fmt.Sprintf(`{"jwt": "%s", "refresh_token": "abc"}`, mockExpiredJwt)
		writer.Write([]byte(mockResponse))
	}

	unauthorizedGetRunsInteraction := utils.NewHttpInteraction("/runs/"+groupId, http.MethodGet)
	unauthorizedGetRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusUnauthorized)
		writer.Write([]byte(`{ "error_message": "Invalid bearer token provided!" }`))
	}

	newLoginInteraction := utils.NewHttpInteraction("/auth/tokens", http.MethodPost)
	newLoginInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusCreated)

		newJwt := createValidMockJwt()
		fmt.Println(newJwt)
		mockResponse := fmt.Sprintf(`{"jwt": "%s", "refresh_token": "abc"}`, newJwt)
		writer.Write([]byte(mockResponse))
	}

	getRunsInteraction := utils.NewHttpInteraction("/runs/"+groupId, http.MethodGet)
	getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)

		mockRun := galasaapi.NewTestRun()

		mockRuns := galasaapi.NewTestRuns()
		mockRuns.Runs = []galasaapi.TestRun{*mockRun}
		mockRunsBytes, _ := json.Marshal(mockRuns)

		writer.Write(mockRunsBytes)
	}

	interactions := []utils.HttpInteraction{
		initialLoginOperation,
		unauthorizedGetRunsInteraction,
		newLoginInteraction,
		getRunsInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	mockFactory := utils.NewMockFactory()

	apiServerUrl := server.Server.URL
	mockFileSystem := mockFactory.GetFileSystem()
	mockEnvironment := mockFactory.GetEnvironment()
	mockGalasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")
	mockTimeService := mockFactory.GetTimeService()
	jwtCache := auth.NewJwtCache(mockFileSystem, mockGalasaHome, mockTimeService)

	mockFileSystem.WriteTextFile(mockGalasaHome.GetUrlFolderPath()+"/galasactl.properties", "GALASA_TOKEN=my:token")
	mockFileSystem.WriteTextFile(mockGalasaHome.GetUrlFolderPath()+"/bootstrap.properties", "")

	authenticator := auth.NewAuthenticator(apiServerUrl, mockFileSystem, mockGalasaHome, mockTimeService, mockEnvironment, jwtCache)
	mockFactory.Authenticator = authenticator

	bootstrap := ""
	maxAttempts := 3
	retryBackoffSeconds := 1

	commsClient, _ := api.NewAPICommsClient(bootstrap, maxAttempts, float64(retryBackoffSeconds), mockFactory, mockGalasaHome)

	launcher := NewRemoteLauncher(commsClient)

	_, err := launcher.GetRunsByGroup(groupId)

	assert.Nil(t, err)
}
