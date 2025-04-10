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
)

func DeleteUser(loginId string, apiClient *galasaapi.APIClient, byteReader spi.ByteReader) error {

	userData, err := getUserDataFromRestApi(loginId, apiClient)

	if err == nil {
		if len(userData) != 0 {
			err = deleteUserFromRestApi(userData[0], apiClient, byteReader)
		} else {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_SERVER_DELETE_USER_NOT_FOUND)
		}
	}

	return err

}

func deleteUserFromRestApi(
	user galasaapi.UserData,
	apiClient *galasaapi.APIClient,
	byteReader spi.ByteReader,
) error {

	var context context.Context = nil
	var resp *http.Response

	restApiVersion, err := embedded.GetGalasactlRestApiVersion()

	if err == nil {
		userNumber := user.GetId()
		apiCall := apiClient.UsersAPIApi.DeleteUserByNumber(context, userNumber).ClientApiVersion(restApiVersion)
		resp, err = apiCall.Execute()

		if resp != nil {
			defer resp.Body.Close()
		}

		if err != nil {

			if resp == nil {
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_SERVER_DELETE_USER_FAILED, err.Error())
			} else {
				// Report errors to the user using the user-id rather than the user number, as the
				// user number means nothing to them.
				err = galasaErrors.HttpResponseToGalasaError(
					resp,
					user.GetLoginId(),
					byteReader,
					galasaErrors.GALASA_ERROR_DELETE_USER_NO_RESPONSE_CONTENT,
					galasaErrors.GALASA_ERROR_DELETE_USER_RESPONSE_PAYLOAD_UNREADABLE,
					galasaErrors.GALASA_ERROR_DELETE_USER_UNPARSEABLE_CONTENT,
					galasaErrors.GALASA_ERROR_DELETE_USER_SERVER_REPORTED_ERROR,
					galasaErrors.GALASA_ERROR_DELETE_USER_EXPLANATION_NOT_JSON,
				)
			}

			log.Printf("User with user number '%s', was deleted OK.\n", userNumber)
		}
	}

	return err
}
