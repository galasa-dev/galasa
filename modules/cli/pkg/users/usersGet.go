/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package users

import (
	"context"
	"log"
	"net/http"

	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/usersformatter"
)

func GetUsers(loginId string, apiClient *galasaapi.APIClient, console spi.Console) error {

	userData, err := getUserDataFromRestApi(loginId, apiClient)

	if err == nil {
		err = formatFetchedUsersAndWriteToConsole(userData, console)
	}

	return err
}

func formatFetchedUsersAndWriteToConsole(users []galasaapi.UserData, console spi.Console) error {

	summaryFormatter := usersformatter.NewUserSummaryFormatter()

	outputText, err := summaryFormatter.FormatUsers(users)

	if err == nil {
		console.WriteString(outputText)
	}

	return err
}

func getUserDataFromRestApi(
	loginId string, // Optional. Could be ""
	apiClient *galasaapi.APIClient,
) ([]galasaapi.UserData, error) {

	var context context.Context = nil
	var users []galasaapi.UserData
	var err error
	var restApiVersion string

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	apiCall := apiClient.UsersAPIApi.GetUserByLoginId(context).ClientApiVersion(restApiVersion)

	if loginId != "" {

		loginId, err = validateLoginIdFlag(loginId)

		if err == nil {
			apiCall = apiCall.LoginId(loginId)
		}
	}

	if err == nil {

		var usersIn []galasaapi.UserData
		var resp *http.Response

		usersIn, resp, err = apiCall.Execute()

		var statusCode int
		if resp != nil {
			defer resp.Body.Close()
			statusCode = resp.StatusCode
		}

		if err != nil {
			log.Println("getUserDataFromRestApi - Failed to retrieve list of users from API server")
			err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_RETRIEVING_USER_LIST_FROM_API_SERVER, err.Error())
		} else {
			users = usersIn
			log.Printf("getUserDataFromRestApi -  %v users collected", len(users))
		}

	}

	//Since we have the loginId filter, we can return the first index.
	//It will always be the currently logged in user
	return users, err
}
