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
	"github.com/galasa-dev/cli/pkg/utils"
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

func validateTagName(tagName string) error {
	var err error
	log.Println("Validating the provided tag name")

	if tagName == "" || !utils.IsLatin1(tagName) {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_TAG_NAME)
	}

	if err == nil {
		log.Println("Tag name validated OK")
	}
	return err
}

func getTagFromRestApi(
	tagName string,
	commsClient api.APICommsClient,
	byteReader spi.ByteReader,
) (galasaapi.GalasaTag, error) {
	var err error
	var tagResults []galasaapi.GalasaTag = make([]galasaapi.GalasaTag, 0)
	var matchingTag galasaapi.GalasaTag

	err = validateTagName(tagName)

	if err == nil {
		log.Println("getTagFromRestApi - Fetching the tag with the given name from the REST API")
		tagResults, err = getTagsFromRestApi(tagName, commsClient, byteReader)

		if err == nil {
			if len(tagResults) > 0 {
				matchingTag = tagResults[0]
				log.Printf("getTagFromRestApi - Found the tag with ID %s", *matchingTag.GetMetadata().Id)
			} else {
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_TAG_NOT_FOUND, tagName)
			}
		}
	}
    return matchingTag, err
}

func getTagsFromRestApi(
	tagName string,
	commsClient api.APICommsClient,
	byteReader spi.ByteReader,
) ([]galasaapi.GalasaTag, error) {
	var err error
	var restApiVersion string
	var tagResults []galasaapi.GalasaTag = make([]galasaapi.GalasaTag, 0)

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()
	if err == nil {
		err = commsClient.RunAuthenticatedCommandWithRateLimitRetries(func(apiClient *galasaapi.APIClient) error {
			var err error
			var httpResponse *http.Response
			var context context.Context = nil

			apiCall := apiClient.TagsAPIApi.GetTags(context).ClientApiVersion(restApiVersion)

			if tagName != "" {
				apiCall = apiCall.Name(tagName)
			}

			tagResults, httpResponse, err = apiCall.Execute()

			if httpResponse != nil {
				defer httpResponse.Body.Close()
			}

			if err != nil {
				if httpResponse == nil {
					// We never got a response, error sending it or something?
					err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_FAILED_TO_DELETE_TAG)
				} else {
					err = galasaErrors.HttpResponseToGalasaError(
						httpResponse,
						tagName,
						byteReader,
						galasaErrors.GALASA_ERROR_DELETE_TAG_NO_RESPONSE_CONTENT,
						galasaErrors.GALASA_ERROR_DELETE_TAG_RESPONSE_BODY_UNREADABLE,
						galasaErrors.GALASA_ERROR_DELETE_TAG_UNPARSEABLE_CONTENT,
						galasaErrors.GALASA_ERROR_DELETE_TAG_SERVER_REPORTED_ERROR,
						galasaErrors.GALASA_ERROR_DELETE_TAG_EXPLANATION_NOT_JSON,
					)
				}
			} else {
				log.Printf("total tags returned: %v", len(tagResults))
			}
			return err
		})
	}
	return tagResults, err
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
