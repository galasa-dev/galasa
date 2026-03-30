/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package auth

import (
	"context"
	"log"
	"net/http"
	"time"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/spi"
)

type authenticatorImpl struct {
	apiServerUrl string
	fileSystem   spi.FileSystem
	galasaHome   spi.GalasaHome
	timeService  spi.TimeService
	env          spi.Environment
	cache        JwtCache
	console      spi.Console
}

func NewAuthenticator(
	apiServerUrl string,
	fileSystem spi.FileSystem,
	galasaHome spi.GalasaHome,
	timeService spi.TimeService,
	env spi.Environment,
	jwtCache JwtCache,
) spi.Authenticator {

	authenticator := new(authenticatorImpl)

	authenticator.apiServerUrl = apiServerUrl
	authenticator.timeService = timeService
	authenticator.galasaHome = galasaHome
	authenticator.fileSystem = fileSystem
	authenticator.env = env

	authenticator.cache = jwtCache

	return authenticator
}

// SetConsole allows setting the console for displaying warnings
func (authenticator *authenticatorImpl) SetConsole(console spi.Console) {
	authenticator.console = console
}

func (authenticator *authenticatorImpl) GetBearerToken() (string, error) {

	var bearerToken string
	var err error
	var galasaTokenValue string

	log.Printf("GetBearerToken entered.\n")

	_, galasaTokenValue, err = getAuthProperties(authenticator.fileSystem, authenticator.galasaHome, authenticator.env)
	if err == nil {
		bearerToken, err = authenticator.cache.Get(authenticator.apiServerUrl, galasaTokenValue)
		if err == nil {
			if bearerToken == "" {
				// Attempt to log in
				log.Printf("Logging in to the Galasa Ecosystem at '%s'", authenticator.apiServerUrl)
				err = authenticator.Login()
				if err == nil {
					log.Printf("Logged in to the Galasa Ecosystem at '%s' OK", authenticator.apiServerUrl)
					bearerToken, err = authenticator.cache.Get(authenticator.apiServerUrl, galasaTokenValue)
				}
			}
		}
	}

	log.Printf("GetBearerToken exiting. length of bearerToken: %v err:%v\n", len(bearerToken), err)

	return bearerToken, err
}

// Gets a new authenticated API client, attempting to log in if a bearer token file does not exist
func (authenticator *authenticatorImpl) GetAuthenticatedAPIClient() (*galasaapi.APIClient, error) {

	log.Printf("GetAuthenticatedAPIClient entered.\n")

	bearerToken, err := authenticator.GetBearerToken()
	var apiClient *galasaapi.APIClient
	if err == nil {
		apiClient = api.InitialiseAuthenticatedAPI(authenticator.apiServerUrl, bearerToken)
	}

	log.Printf("GetAuthenticatedAPIClient exiting. err: %v\n", err)

	return apiClient, err
}

// Login - performs all the logic to implement the `galasactl auth login` command
func (authenticator *authenticatorImpl) Login() error {
	var err error
	var authProperties galasaapi.AuthProperties
	var galasaTokenValue string

	log.Printf("Login entered.\n")

	authProperties, galasaTokenValue, err = getAuthProperties(authenticator.fileSystem, authenticator.galasaHome, authenticator.env)
	if err == nil {
		var jwt string
		jwt, err = authenticator.getJwtFromRestApi(authenticator.apiServerUrl, authProperties)
		if err == nil {
			err = authenticator.cache.Put(authenticator.apiServerUrl, galasaTokenValue, jwt)
		}
	}

	log.Printf("Login exiting. %v\n", err)

	return err
}

// Logout - performs all the logout to implement the `galasactl auth login` command
func (authenticator *authenticatorImpl) LogoutOfEverywhere() error {
	var err error

	log.Printf("LogoutOfEverywhere entered.")

	authenticator.cache.ClearAll()
	return err
}

// Gets a JSON Web Token (JWT) from the API server's /auth endpoint
func (authenticator *authenticatorImpl) getJwtFromRestApi(apiServerUrl string, authProperties galasaapi.AuthProperties) (string, error) {
	var err error
	var context context.Context = nil
	var jwt string
	var restApiVersion string

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	if err == nil {
		apiClient := api.InitialiseAPI(apiServerUrl)

		var tokenResponse *galasaapi.TokenResponse
		var httpResponse *http.Response
		tokenResponse, httpResponse, err = apiClient.AuthenticationAPIApi.CreateToken(context).
			AuthProperties(authProperties).
			ClientApiVersion(restApiVersion).
			Execute()

		var statusCode int
		if httpResponse != nil {
			defer httpResponse.Body.Close()
			statusCode = httpResponse.StatusCode
		}

		if err != nil {
			log.Println("Failed to retrieve bearer token from API server")
			err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_RETRIEVING_BEARER_TOKEN_FROM_API_SERVER, err.Error())
		} else {
			log.Println("Bearer token received from API server OK")
			jwt = tokenResponse.GetJwt()

			// Check if the token is approaching expiry and display a warning
			if tokenResponse.HasTokenExpiryTime() {
				warningDays := 14 // Default value
				if tokenResponse.HasTokenExpiryWarningDays() {
					warningDays = int(tokenResponse.GetTokenExpiryWarningDays())
				}
				authenticator.checkTokenExpiryAndWarn(tokenResponse.GetTokenExpiryTime(), warningDays)
			}
		}
	}

	return jwt, err
}

// checkTokenExpiryAndWarn checks if a token is approaching expiry and displays a warning
func (authenticator *authenticatorImpl) checkTokenExpiryAndWarn(expiryTimeStr string, warningThresholdDays int) {

	expiryTime, err := time.Parse(time.RFC3339, expiryTimeStr)
	if err != nil {
		log.Printf("Failed to parse token expiry time: %v", err)
		return
	}

	now := authenticator.timeService.Now()
	daysUntilExpiry := expiryTime.Sub(now).Hours() / 24

	if daysUntilExpiry <= float64(warningThresholdDays) && daysUntilExpiry > 0 {
		warningMessage := "GAL1200W: Your personal access token will expire soon. Once it expires, you will no longer be able to use it to contact your Galasa service. Create a new personal access token on your Galasa service's web user interface to continue to authenticate with the Galasa service."

		if authenticator.console != nil {
			authenticator.console.WriteString(warningMessage + "\n")
		} else {
			log.Println(warningMessage)
		}
	}
}
