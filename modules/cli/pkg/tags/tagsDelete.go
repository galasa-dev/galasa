/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package tags

import (
	"context"
	"log"
	"net/http"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/spi"
)

func DeleteTag(tagName string, commsClient api.APICommsClient, byteReader spi.ByteReader) error {
	var err error

	// We have the tag name but we need the tag ID
	tag, err := getTagFromRestApi(tagName, commsClient, byteReader)

	if err == nil {
		err = deleteTagFromRestApi(tag, commsClient, byteReader)
	}
	return err
}

func deleteTagFromRestApi(
	tag galasaapi.GalasaTag,
	commsClient api.APICommsClient,
	byteReader spi.ByteReader,
) error {

	var context context.Context = nil
	var resp *http.Response

	tagMetadata := tag.GetMetadata()
	tagId := *tagMetadata.Id
	tagName := *tagMetadata.Name

	restApiVersion, err := embedded.GetGalasactlRestApiVersion()
	if err == nil {

		err = commsClient.RunAuthenticatedCommandWithRateLimitRetries(func(apiClient *galasaapi.APIClient) error {
			var err error
			resp, err = apiClient.TagsAPIApi.DeleteTagByName(context, tagId).ClientApiVersion(restApiVersion).Execute()

			if resp != nil {
				defer resp.Body.Close()
			}

			if err != nil {
				if resp == nil {
					// We never got a response, error sending it or something?
					err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_FAILED_TO_DELETE_TAG)
				} else {
					err = galasaErrors.HttpResponseToGalasaError(
						resp,
						*tag.GetMetadata().Name,
						byteReader,
						galasaErrors.GALASA_ERROR_DELETE_TAG_NO_RESPONSE_CONTENT,
						galasaErrors.GALASA_ERROR_DELETE_TAG_RESPONSE_BODY_UNREADABLE,
						galasaErrors.GALASA_ERROR_DELETE_TAG_UNPARSEABLE_CONTENT,
						galasaErrors.GALASA_ERROR_DELETE_TAG_SERVER_REPORTED_ERROR,
						galasaErrors.GALASA_ERROR_DELETE_TAG_EXPLANATION_NOT_JSON,
					)
				}
			}

			if err == nil {
				log.Printf("Tag with ID '%s' and name '%s', was deleted OK.\n", tagId, tagName)
			}
			return err
		})
	}

	return err
}
