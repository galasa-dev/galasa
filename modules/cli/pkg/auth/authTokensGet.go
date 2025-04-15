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
	"strings"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/tokensformatter"
)

// GetTokens - performs all the logic to implement the `galasactl auth tokens get` command
func GetTokens(
	apiClient *galasaapi.APIClient,
	console spi.Console,
	loginId string,
) error {

	authTokens, err := getAuthTokensFromRestApi(apiClient, loginId)

	if err == nil {
		err = formatFetchedTokensAndWriteToConsole(authTokens, console)
	}

	return err
}

func getAuthTokensFromRestApi(apiClient *galasaapi.APIClient, loginId string) ([]galasaapi.AuthToken, error) {
	var context context.Context = nil
	var authTokens []galasaapi.AuthToken
	var err error

	apiCall := apiClient.AuthenticationAPIApi.GetTokens(context)

	if loginId != "" {

		loginId, err = validateLoginIdFlag(loginId)

		if err == nil {
			apiCall = apiCall.LoginId(loginId)
		}
	}

	if err == nil {

		var tokens *galasaapi.AuthTokens
		var resp *http.Response

		tokens, resp, err = apiCall.Execute()

		var statusCode int
		if resp != nil {
			defer resp.Body.Close()
			statusCode = resp.StatusCode
		}

		if err != nil {
			log.Println("getAuthTokensFromRestApi - Failed to retrieve list of tokens from API server")
			err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_RETRIEVING_TOKEN_LIST_FROM_API_SERVER, err.Error())
		} else {
			authTokens = tokens.GetTokens()
			log.Printf("getAuthTokensFromRestApi -  %v tokens collected", len(authTokens))
		}
	}

	return authTokens, err
}

func formatFetchedTokensAndWriteToConsole(authTokens []galasaapi.AuthToken, console spi.Console) error {

	summaryFormatter := tokensformatter.NewTokenSummaryFormatter()

	outputText, err := summaryFormatter.FormatTokens(authTokens)

	if err == nil {
		console.WriteString(outputText)
	}

	return err

}

func validateLoginIdFlag(loginId string) (string, error) {

	var err error

	loginId = strings.TrimSpace(loginId)
	hasSpace := strings.Contains(loginId, " ")

	if loginId == "" {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_USER_FLAG_VALUE)
	}

	if hasSpace {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_LOGIN_ID, loginId)
	}

	return loginId, err
}
