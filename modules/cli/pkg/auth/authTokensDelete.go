/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package auth

import (
	"context"
	"io"
	"log"
	"net/http"
	"regexp"

	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
)

var (
	// Expect the pattern:
	// letters and numbers
	// dashes (-) and underscores (_)
	// + ensures a non-empty string
	// ^ matches the start of the string
	// $ matches the end of the string
	TOKEN_ID_PATTERN *regexp.Regexp = regexp.MustCompile(`^[a-zA-Z0-9-_]+$`)
)

// DeleteToken - performs all the logic to implement the `galasactl auth tokens delete --tokenid xxx` command
func DeleteToken(
	tokenId string,
	apiClient *galasaapi.APIClient,
) error {
	var err error

	err = validateTokenId(tokenId)
	if err == nil {
		log.Print("DeleteToken - Valid token ID provided")
		err = deleteTokenFromRestApi(tokenId, apiClient)
	}

	return err
}

// Checks if the given token ID is valid
func validateTokenId(tokenId string) error {
	var err error
	if !TOKEN_ID_PATTERN.MatchString(tokenId) {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_TOKEN_ID_FORMAT, tokenId)
	}
	return err
}

func deleteTokenFromRestApi(tokenId string, apiClient *galasaapi.APIClient) error {
	var err error
	var context context.Context
	var resp *http.Response
	var responseBody []byte
	
	var restApiVersion string
	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	if err == nil {
		_, resp, err = apiClient.AuthenticationAPIApi.DeleteToken(context, tokenId).ClientApiVersion(restApiVersion).Execute()
	
		if err != nil {
			// Try to get the error returned from the API server and return that message
			if resp != nil {
				defer resp.Body.Close()
				statusCode := resp.StatusCode
				if statusCode != http.StatusOK {
					responseBody, err = io.ReadAll(resp.Body)
					log.Printf("deleteTokenFromRestApi - HTTP response - Status Code: '%v' Payload: '%v' ", resp.StatusCode, string(responseBody))
			
					if err == nil {
						var errorFromServer *galasaErrors.GalasaAPIError
						errorFromServer, err = galasaErrors.GetApiErrorFromResponse(statusCode, responseBody)
			
						if err == nil {
							// Return a Galasa API error, because the status code is not 200 (OK)
							err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_REVOKE_TOKEN_FAILED, tokenId, errorFromServer.Message)
						}
					} else {
						err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_UNABLE_TO_READ_RESPONSE_BODY, err)
					}
				}
			} else {
				// No response was received from the API server, so something else may have gone wrong
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_REVOKE_TOKEN_FAILED, tokenId, err.Error())
			}
		}
	}
	return err
}
