/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
)

type MockAuthenticator struct {
	apiClient *galasaapi.APIClient

	httpStatusCodeToReturn int
}

func NewMockAuthenticator() *MockAuthenticator {
	return NewMockAuthenticatorWithAPIClient(nil)
}

func NewMockAuthenticatorWithAPIClient(apiClient *galasaapi.APIClient) *MockAuthenticator {

	authenticator := new(MockAuthenticator)
	authenticator.apiClient = apiClient
	return authenticator
}

func (authenticator *MockAuthenticator) SetHttpStatusCodeToReturn(httpStatusCodeToReturn int) {
	authenticator.httpStatusCodeToReturn = httpStatusCodeToReturn
}

func (authenticator *MockAuthenticator) GetBearerToken() (string, error) {
	bearerToken := ""
	var err error

	return bearerToken, err
}

// Gets a new authenticated API client, attempting to log in if a bearer token file does not exist
func (authenticator *MockAuthenticator) GetAuthenticatedAPIClient() (*galasaapi.APIClient, error) {
	var err error
	httpStatusCodeToReturn := authenticator.httpStatusCodeToReturn
	if httpStatusCodeToReturn >= 400 && httpStatusCodeToReturn < 600 {
		mockMsgType := galasaErrors.NewMessageType("TEST123: simulating a failure!", 123, false)
		err = galasaErrors.NewGalasaErrorWithHttpStatusCode(httpStatusCodeToReturn, mockMsgType)
	}

	return authenticator.apiClient, err
}

// Login - performs all the logic to implement the `galasactl auth login` command
func (authenticator *MockAuthenticator) Login() error {
	var err error
	return err
}

// Login - performs all the logic to implement the `galasactl auth login` command
func (authenticator *MockAuthenticator) LogoutOfEverywhere() error {
	var err error
	return err
}
