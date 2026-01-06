/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package tags

import (
	"context"
	"encoding/base64"
	"log"
	"math"
	"net/http"

	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/spi"
)

const (
	DEFAULT_EMPTY_PRIORITY = math.MinInt
)

func SetTag(
	tagName string,
	description string,
	priority int,
	apiClient *galasaapi.APIClient,
	byteReader spi.ByteReader,
) error {
	var err error

	err = validateTagName(tagName)

	if err == nil {
		if description != "" {
			err = validateDescription(description)
		}

		if err == nil {
			_, err = sendTagUpdateToRestApi(tagName, description, priority, apiClient, byteReader)
		}
	}
	return err
}

func sendTagUpdateToRestApi(
	tagName string,
	description string,
	priority int,
	apiClient *galasaapi.APIClient,
	byteReader spi.ByteReader,
) (*galasaapi.GalasaTag, error) {
	var err error
	var restApiVersion string
	var context context.Context = nil
	var tagGotBack *galasaapi.GalasaTag

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	var tagSetRequest *galasaapi.TagSetRequest = galasaapi.NewTagSetRequest()

	if description != "" {
		tagSetRequest.SetDescription(description)
	}

	if priority != DEFAULT_EMPTY_PRIORITY {
		tagSetRequest.SetPriority(int32(priority))
	}

	// Tag IDs on the Galasa service are base64 URL encoded versions of the tag names
	tagId := base64.RawURLEncoding.EncodeToString([]byte(tagName))

	apiCall := apiClient.TagsAPIApi.SetTag(context, tagId).TagSetRequest(*tagSetRequest).ClientApiVersion(restApiVersion)
	if err == nil {

		var resp *http.Response

		tagGotBack, resp, err = apiCall.Execute()

		if resp != nil {
			defer resp.Body.Close()
		}

        if err != nil {
			log.Println("sendTagUpdateToRestApi - Failed to update tag record in the Galasa service")

            if resp == nil {
                err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_SET_TAG_REQUEST_FAILED, tagName, err.Error())
            } else {
                err = galasaErrors.HttpResponseToGalasaError(
                    resp,
                    tagName,
                    byteReader,
                    galasaErrors.GALASA_ERROR_SET_TAG_NO_RESPONSE_CONTENT,
                    galasaErrors.GALASA_ERROR_SET_TAG_RESPONSE_BODY_UNREADABLE,
                    galasaErrors.GALASA_ERROR_SET_TAG_UNPARSEABLE_CONTENT,
                    galasaErrors.GALASA_ERROR_SET_TAG_SERVER_REPORTED_ERROR,
                    galasaErrors.GALASA_ERROR_SET_TAG_EXPLANATION_NOT_JSON,
                )
            }
        } else {
			log.Println("sendTagUpdateToRestApi - Tag updated OK")
		}
	}
	return tagGotBack, err
}
