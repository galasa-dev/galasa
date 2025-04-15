/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package streams

import (
	"context"
	"log"
	"net/http"

	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/spi"
)

func DeleteStream(streamName string, apiClient *galasaapi.APIClient, byteReader spi.ByteReader) error {

	var err error

	streamName, err = validateStreamName(streamName)

	if err == nil {
		err = deleteStreamFromRestApi(streamName, apiClient, byteReader)
	}

	return err

}

func deleteStreamFromRestApi(
	streamName string,
	apiClient *galasaapi.APIClient,
	byteReader spi.ByteReader,
) error {

	var context context.Context = nil
	var resp *http.Response

	restApiVersion, err := embedded.GetGalasactlRestApiVersion()

	if err == nil {

		apiCall := apiClient.StreamsAPIApi.DeleteStreamByName(context, streamName).ClientApiVersion(restApiVersion)
		resp, err = apiCall.Execute()

		if resp != nil {
			defer resp.Body.Close()
		}

		if err != nil {

			if resp == nil {

				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_FAILED_TO_DELETE_STREAM, err.Error())

			} else {

				err = galasaErrors.HttpResponseToGalasaError(
					resp,
					streamName,
					byteReader,
					galasaErrors.GALASA_ERROR_DELETE_STREAMS_NO_RESPONSE_CONTENT,
					galasaErrors.GALASA_ERROR_DELETE_STREAMS_RESPONSE_BODY_UNREADABLE,
					galasaErrors.GALASA_ERROR_DELETE_STREAMS_UNPARSEABLE_CONTENT,
					galasaErrors.GALASA_ERROR_DELETE_STREAMS_SERVER_REPORTED_ERROR,
					galasaErrors.GALASA_ERROR_DELETE_STREAMS_EXPLANATION_NOT_JSON,
				)

			}

			log.Printf("Test stream with name '%s', was deleted OK.\n", streamName)
		}
	}

	return err

}
