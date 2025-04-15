/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package errors

import (
	"encoding/json"
	"log"
	"net/http"

	"github.com/galasa-dev/cli/pkg/spi"
)

type GalasaAPIError struct {
	Code    int    `json:"error_code"`
	Message string `json:"error_message"`
}

// This function reads a galasa API Error into a structure so that it can be displayed as
// the reason for the failure.
// NOTE: when this function is called ensure that the calling function has the  `defer resp.Body.Close()`
// called in order to ensure that the response body is closed when the function completes
func GetApiErrorFromResponse(statusCode int, body []byte) (*GalasaAPIError, error) {
	return GetApiErrorFromResponseBytes(body, func(marshallingError error) error{
			return NewGalasaErrorWithHttpStatusCode(statusCode, GALASA_ERROR_UNABLE_TO_READ_RESPONSE_BODY, marshallingError)
		},
	) 
}

func GetApiErrorFromResponseBytes(body []byte, marshallingErrorLambda func(marshallingError error) error) (*GalasaAPIError, error) {
	var err error

	apiError := new(GalasaAPIError)

	err = json.Unmarshal(body, &apiError)

	if err != nil {
		log.Printf("GetApiErrorFromResponseBytes failed to unmarshal bytes into a galasa api error structure. %v", err.Error())
		err = marshallingErrorLambda(err)
	}
	return apiError, err
}

func HttpResponseToGalasaError(
	response *http.Response,
	identifier string,
	byteReader spi.ByteReader,
	errorMsgUnexpectedStatusCodeNoResponseBody *MessageType,
	errorMsgUnableToReadResponseBody *MessageType,
	errorMsgResponsePayloadInWrongFormat *MessageType,
	errorMsgReceivedFromApiServer *MessageType,
	errorMsgResponseContentTypeNotJson *MessageType,
) error {
	defer response.Body.Close()
	var err error
	var responseBodyBytes []byte
	statusCode := response.StatusCode

	if response.ContentLength == 0 {
		log.Printf("Failed - HTTP response - status code: '%v'\n", statusCode)
		err = createResponseError(errorMsgUnexpectedStatusCodeNoResponseBody, identifier, statusCode)
	} else {
		
		contentType := response.Header.Get("Content-Type")
		if contentType != "application/json" {
			err = createResponseError(errorMsgResponseContentTypeNotJson, identifier, statusCode)
		} else {
			responseBodyBytes, err = byteReader.ReadAll(response.Body)
			if err != nil {
				err = createResponseErrorWithCause(errorMsgUnableToReadResponseBody, identifier, statusCode, err.Error())
			} else {

				var errorFromServer *GalasaAPIError
				errorFromServer, err = GetApiErrorFromResponseBytes(
					responseBodyBytes,
					func (marshallingError error) error {
						log.Printf("Failed - HTTP response - status code: '%v' payload in response is not json: '%v' \n", statusCode, string(responseBodyBytes))
						return createResponseErrorWithCause(errorMsgResponsePayloadInWrongFormat, identifier, statusCode, marshallingError)
					},
				)

				if err == nil {
					// server returned galasa api error structure we understand.
					log.Printf("Failed - HTTP response - status code: '%v' server responded with error message: '%v' \n", statusCode, errorMsgReceivedFromApiServer)
					err = createResponseErrorWithCause(errorMsgReceivedFromApiServer, identifier, statusCode, errorFromServer.Message)
				}
			}
		}
	}
	return err
}

func createResponseError(errorMsg *MessageType, identifier string, statusCode int) error {
    var err error
    if identifier == "" {
        err = NewGalasaErrorWithHttpStatusCode(statusCode, errorMsg, statusCode)
    } else {
        err = NewGalasaErrorWithHttpStatusCode(statusCode, errorMsg, identifier, statusCode)
    }
    return err
}

func createResponseErrorWithCause(errorMsg *MessageType, identifier string, statusCode int, cause interface{}) error {
    var err error
    if identifier == "" {
        err = NewGalasaErrorWithHttpStatusCode(statusCode, errorMsg, statusCode, cause)
    } else {
        err = NewGalasaErrorWithHttpStatusCode(statusCode, errorMsg, identifier, statusCode, cause)
    }
    return err
}
