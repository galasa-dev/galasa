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
	"github.com/galasa-dev/cli/pkg/utils"
)

func SetStream(
	streamName string,
	description string,
	mavenRepositoryUrl string,
	testCatalogUrl string,
	obrs []string,
	apiClient *galasaapi.APIClient,
	byteReader spi.ByteReader,
) error {
	var err error

	streamName, err = validateStreamName(streamName)

	if err == nil {
		if description != "" {
			description, err = validateDescription(description)
		}

		if mavenRepositoryUrl != "" {
			mavenRepositoryUrl, err = validateUrl(mavenRepositoryUrl)
		}

		if testCatalogUrl != "" {
			testCatalogUrl, err = validateUrl(testCatalogUrl)
		}

		var streamObrs []galasaapi.StreamOBRData
		if len(obrs) > 0 {
			var obrCoordinates []utils.MavenCoordinates
			obrCoordinates, err = utils.ValidateObrs(obrs)
			if err == nil {
				streamObrs, err = convertObrsToObrBeans(obrCoordinates)
			}
		}

		if err == nil {
			_, err = sendStreamUpdateToRestApi(
				streamName,
				description,
				mavenRepositoryUrl,
				testCatalogUrl,
				streamObrs,
				apiClient,
				byteReader,
			)
		}
	}
	return err
}

func sendStreamUpdateToRestApi(
	streamName string,
	description string,
	mavenRepositoryUrl string,
	testCatalogUrl string,
	obrs []galasaapi.StreamOBRData,
	apiClient *galasaapi.APIClient,
	byteReader spi.ByteReader,
) (*galasaapi.Stream, error) {
	var err error
	var restApiVersion string
	var context context.Context = nil
	var streamGotBack *galasaapi.Stream

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	streamSetRequest := buildStreamUpdateRequest(description, mavenRepositoryUrl, testCatalogUrl, obrs)

	apiCall := apiClient.StreamsAPIApi.UpdateStream(context, streamName).
		StreamUpdateRequest(*streamSetRequest).
		ClientApiVersion(restApiVersion)

	if err == nil {

		var resp *http.Response

		streamGotBack, resp, err = apiCall.Execute()

		if resp != nil {
			defer resp.Body.Close()
		}

        if err != nil {
			log.Println("sendStreamUpdateToRestApi - Failed to update test stream record in the Galasa service")

            if resp == nil {
                err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_SET_STREAM_REQUEST_FAILED, streamName, err.Error())
            } else {
                err = galasaErrors.HttpResponseToGalasaError(
                    resp,
                    streamName,
                    byteReader,
                    galasaErrors.GALASA_ERROR_SET_STREAM_NO_RESPONSE_CONTENT,
                    galasaErrors.GALASA_ERROR_SET_STREAM_RESPONSE_BODY_UNREADABLE,
                    galasaErrors.GALASA_ERROR_SET_STREAM_UNPARSEABLE_CONTENT,
                    galasaErrors.GALASA_ERROR_SET_STREAM_SERVER_REPORTED_ERROR,
                    galasaErrors.GALASA_ERROR_SET_STREAM_EXPLANATION_NOT_JSON,
                )
            }
        } else {
			log.Println("sendStreamUpdateToRestApi - Test stream updated OK")
		}
	}
	return streamGotBack, err
}

func buildStreamUpdateRequest(
	description string,
	mavenRepositoryUrl string,
	testCatalogUrl string,
	obrs []galasaapi.StreamOBRData,
) *galasaapi.StreamUpdateRequest {
	var streamSetRequest *galasaapi.StreamUpdateRequest = galasaapi.NewStreamUpdateRequest()

	if description != "" {
		streamSetRequest.SetDescription(description)
	}

	if mavenRepositoryUrl != "" {
		repository := galasaapi.NewStreamUpdateRequestRepository()
		repository.SetUrl(mavenRepositoryUrl)
		streamSetRequest.SetRepository(*repository)
	}

	if testCatalogUrl != "" {
		testCatalog := galasaapi.NewStreamUpdateRequestTestCatalog()
		testCatalog.SetUrl(testCatalogUrl)
		streamSetRequest.SetTestCatalog(*testCatalog)
	}

	if len(obrs) > 0 {
		streamSetRequest.SetObrs(obrs)
	}
	return streamSetRequest
}
