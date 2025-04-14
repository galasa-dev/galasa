/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package spi

import "github.com/galasa-dev/cli/pkg/galasaapi"

type Authenticator interface {
	// Gets a bearer token from the persistent cache if there is one, else logs into the server to get one.
	GetBearerToken() (string, error)

	// Gets a new authenticated API client, attempting to log in if a bearer token file does not exist
	GetAuthenticatedAPIClient() (*galasaapi.APIClient, error)

	// Logs into the server, saving the JWT token obtained in a persistent cache for later
	Login() error

	LogoutOfEverywhere() error
}
