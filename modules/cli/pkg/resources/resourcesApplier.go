/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package resources

import (
	"bytes"
	"fmt"
	"io"
	"log"
	"net/http"
	"strings"

	"github.com/galasa-dev/cli/pkg/api"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/spi"
)

// ApplyResources - performs all the logic to implement the
// `galasactl resources apply/create/update` command,
// but in a unit-testable manner.
func ApplyResources(
	action string,
	filePath string,
	fileSystem spi.FileSystem,
	commsClient api.APICommsClient,
) error {
	var err error
	var fileContent string
	var jsonBytes []byte

	err = validateFilePathExists(fileSystem, filePath)

	if err == nil {
		fileContent, err = getYamlFileContent(fileSystem, filePath)

		if err == nil {
			//convert resources in yaml file into a json payload
			jsonBytes, err = yamlToByteArray(fileContent, action)
		}

		if err == nil {
			err = sendResourcesRequestToServer(jsonBytes, commsClient)
		}
	}
	return err
}

func sendResourcesRequestToServer(payloadJsonToSend []byte, commsClient api.APICommsClient) error {

	var err error
	var responseBody []byte
	var bearerToken string

	apiServerUrl := commsClient.GetBootstrapData().ApiServerURL
	resourcesApiServerUrl := apiServerUrl + "/resources/"

	err = commsClient.RunAuthenticatedCommandWithRateLimitRetries(func(apiClient *galasaapi.APIClient) error {
		var err error
		bearerToken, err = commsClient.GetAuthenticator().GetBearerToken()
		if err == nil {
			var req *http.Request
			req, err = http.NewRequest("POST", resourcesApiServerUrl, bytes.NewBuffer(payloadJsonToSend))
		
			if err == nil {
				req.Header.Set("Content-Type", "application/json")
				req.Header.Set("Accept", "application/json")
				req.Header.Set("Accept-Encoding", "gzip,deflate,br")
				req.Header.Set("Authorization", "Bearer "+bearerToken)
		
				// WARNING:
				// Don't leave the following log statement enabled. It might log secret namespace property values, which would be a security violation.
				// log.Printf("sendResourcesRequestToServer url:%s - headers:%s - payload: '%s'", resourcesApiServerUrl, req.Header, string(payloadJsonToSend))
		
				var resp *http.Response
				client := &http.Client{}
		
				// A non-2xx status code doesn't cause an error.
				// If there is an error, the response should be nil
				resp, err = client.Do(req)
				if err == nil {
					statusCode := resp.StatusCode
		
					defer resp.Body.Close()
		
					if statusCode != http.StatusOK && statusCode != http.StatusCreated {
		
						responseBody, err = io.ReadAll(resp.Body)
						log.Printf("sendResourcesRequestToServer Failed - HTTP response - status code:%v payload:%v", statusCode, string(responseBody))
		
						if err == nil {
							//only 400 response status codes are expected to return a list of errors
							if statusCode == 400 {
								//Get a list of errors for each resource (obtained from the yaml file given in the command)
								var errorsFromServer *galasaErrors.GalasaAPIErrorsArray
								errorsFromServer, err = galasaErrors.NewGalasaApiErrorsArray(responseBody)
		
								if err == nil {
									errMessages := errorsFromServer.GetErrorMessages()
									responseString := fmt.Sprint(strings.Join(errMessages, "\n"))
									err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_RESOURCES_RESP_BAD_REQUEST, responseString)
								} else {
									err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_RESOURCE_RESPONSE_PARSING, err)
								}
							} else if statusCode == 401 {
								err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_RESOURCE_RESP_UNAUTHORIZED_OPERATION)
							} else if statusCode == 500 {
								err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_RESOURCES_RESP_SERVER_ERROR)
							} else {
								err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_RESP_UNEXPECTED_ERROR)
							}
						} else {
							err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_UNABLE_TO_READ_RESPONSE_BODY, err)
						}
		
					} else {
						log.Println("response Status:", resp.Status)
						log.Println("response Headers:", resp.Header)
					}
				}
			}
		}
		return err
	})

	return err
}
