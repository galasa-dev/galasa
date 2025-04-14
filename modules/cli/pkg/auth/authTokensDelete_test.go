/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package auth

import (
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/stretchr/testify/assert"
)

func newDeleteTokensServletMock(t *testing.T) *httptest.Server {

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		mockDeleteTokensServlet(t, w, r)
	}))

	return server
}

func mockDeleteTokensServlet(t *testing.T, w http.ResponseWriter, r *http.Request) {
	if !strings.Contains(r.URL.Path, "/auth/tokens") {
		t.Errorf("Expected to request '/auth/tokens', got: %s", r.URL.Path)
	}
	if r.Header.Get("Accept") != "application/json" {
		t.Errorf("Expected Accept: application/json header, got: %s", r.Header.Get("Accept"))
	}
	w.Header().Set("Content-Type", "application/json")

	var statusCode int
	var jsonRespBody string
	splitUrl := strings.Split(r.URL.Path, "/")
	tokenId := splitUrl[3]

	//auth/tokens/{tokenId}
	if tokenId == "unauthorizedToken" {
		statusCode = 401
		jsonRespBody = `{
			"error_code": 5401,
			"error_message": "GAL5401E: Unauthorized."
		}`
	} else if tokenId == "serverErrToken" {
		statusCode = 500
		jsonRespBody = `{
			"error_code": 5000,
			"error_message": "GAL5000E: Internal server error occurred."
		}`
	} else if tokenId == "notFoundToken" {
		statusCode = 404
		jsonRespBody = `{
			"error_code": 5404,
			"error_message": "GAL5404E: Token record with the provided ID was not found."
		}`
	} else if tokenId == "invalidResponse" {
		statusCode = 500
		jsonRespBody = `this is not a valid JSON response from the API server!`
	} else {
		statusCode = 200
		jsonRespBody = ""
	}

	w.WriteHeader(statusCode)
	w.Write([]byte(jsonRespBody))
}

func TestInvalidTokenIdWithSpecialCharactersReturnsError(t *testing.T) {
	// Given...
	invalidTokenIdFormat := "siuIOHUDH98Y:73Gioud@£!%H..."

	server := newDeleteTokensServletMock(t)
	apiClient := api.InitialiseAPI(server.URL)
	defer server.Close()

	// When
	err := DeleteToken(invalidTokenIdFormat, apiClient)

	// Then
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "The provided token ID, '" + invalidTokenIdFormat + "', does not match formatting requirements")
	assert.Contains(t, err.Error(), "GAL1154E")
}

func TestDeleteTokenWithAlphabeticTokenReturnsOk(t *testing.T) {
	// Given...
	tokenId := "validToken"

	server := newDeleteTokensServletMock(t)
	apiClient := api.InitialiseAPI(server.URL)
	defer server.Close()

	// When...
	err := DeleteToken(tokenId, apiClient)

	// Then...
	assert.Nil(t, err)
}

func TestDeleteTokenWithNumericTokenReturnsOk(t *testing.T) {
	// Given...
	tokenId := "123456789"

	server := newDeleteTokensServletMock(t)
	apiClient := api.InitialiseAPI(server.URL)
	defer server.Close()

	// When...
	err := DeleteToken(tokenId, apiClient)

	// Then...
	assert.Nil(t, err)
}

func TestDeleteTokenWithAlphanumericTokenReturnsOk(t *testing.T) {
	// Given...
	tokenId := "validtoken123WithAlphanum456Characters789"

	server := newDeleteTokensServletMock(t)
	apiClient := api.InitialiseAPI(server.URL)
	defer server.Close()

	// When...
	err := DeleteToken(tokenId, apiClient)

	// Then...
	assert.Nil(t, err)
}

func TestDeleteTokenWithAlphanumDashesTokenReturnsOk(t *testing.T) {
	// Given...
	tokenId := "token-with-123-dashes-and-numbers"

	server := newDeleteTokensServletMock(t)
	apiClient := api.InitialiseAPI(server.URL)
	defer server.Close()

	// When...
	err := DeleteToken(tokenId, apiClient)

	// Then...
	assert.Nil(t, err)
}

func TestDeleteTokenWithAlphanumUnderscoresTokenReturnsOk(t *testing.T) {
	// Given...
	tokenId := "token_with_123_underscores_and_numbers"

	server := newDeleteTokensServletMock(t)
	apiClient := api.InitialiseAPI(server.URL)
	defer server.Close()

	// When...
	err := DeleteToken(tokenId, apiClient)

	// Then...
	assert.Nil(t, err)
}

func TestDeleteTokenWithAlphanumUnderscoresAndDashesTokenReturnsOk(t *testing.T) {
	// Given...
	tokenId := "token-with_dashes-123_underscores_and_numbers"

	server := newDeleteTokensServletMock(t)
	apiClient := api.InitialiseAPI(server.URL)
	defer server.Close()

	// When...
	err := DeleteToken(tokenId, apiClient)

	// Then...
	assert.Nil(t, err)
}

func TestDeleteWithValidNotFoundTokenReturnsOk(t *testing.T) {
	// Given...
	tokenId := "notFoundToken"

	server := newDeleteTokensServletMock(t)
	apiClient := api.InitialiseAPI(server.URL)
	defer server.Close()

	// When...
	err := DeleteToken(tokenId, apiClient)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "Failed to revoke the token with ID")
	assert.Contains(t, err.Error(), "GAL1153E")
}

func TestDeleteUnauthorizedTokenReturnsError(t *testing.T) {
	// Given...
	tokenId := "unauthorizedToken"

	server := newDeleteTokensServletMock(t)
	apiClient := api.InitialiseAPI(server.URL)
	defer server.Close()

	// When...
	err := DeleteToken(tokenId, apiClient)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "Unauthorized")
	assert.Contains(t, err.Error(), "GAL5401E")
}

func TestDeleteTokenWithInternalServerErrorReturnsError(t *testing.T) {
	// Given...
	tokenId := "serverErrToken"

	server := newDeleteTokensServletMock(t)
	apiClient := api.InitialiseAPI(server.URL)
	defer server.Close()

	// When...
	err := DeleteToken(tokenId, apiClient)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "Internal server error occurred")
	assert.Contains(t, err.Error(), "GAL5000E")
}

func TestDeleteTokenWithInvalidServerResponseReturnsError(t *testing.T) {
	// Given...
	tokenId := "invalidResponse"

	server := newDeleteTokensServletMock(t)
	apiClient := api.InitialiseAPI(server.URL)
	defer server.Close()

	// When...
	err := DeleteToken(tokenId, apiClient)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "Error reading the HTTP Response body")
	assert.Contains(t, err.Error(), "GAL1116E")
}
