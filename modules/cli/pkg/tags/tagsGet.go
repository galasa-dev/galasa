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
	"sort"
	"strings"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/tagsformatter"
)

var (
	validFormatters = createFormatters()
)

func GetTag(
	tagName string,
	format string,
	commsClient api.APICommsClient,
	console spi.Console,
	byteReader spi.ByteReader,
) error {
	var err error
	var chosenFormatter tagsformatter.TagsFormatter
	tags := make([]galasaapi.GalasaTag, 0)

	chosenFormatter, err = validateFormatFlag(format)
	if err == nil {
		if tagName != "" {
			// The user has provided a tag name, so try to get that tag
			err = validateTagName(tagName)
		}
	
		if err == nil {
			// Get the tags from the REST API
			tags, err = getTagsFromRestApi(tagName, commsClient, byteReader)
		}

		// If we were able to get the tags, format them as requested by the user
		if err == nil {
			var formattedOutput string
			formattedOutput, err = chosenFormatter.FormatTags(tags)
			if err == nil {
				console.WriteString(formattedOutput)
			}
		}
	}
	return err
}

func createFormatters() map[string]tagsformatter.TagsFormatter {
	validFormatters := make(map[string]tagsformatter.TagsFormatter, 0)
	summaryFormatter := tagsformatter.NewTagSummaryFormatter()
	validFormatters[summaryFormatter.GetName()] = summaryFormatter

	yamlFormatter := tagsformatter.NewTagYamlFormatter()
	validFormatters[yamlFormatter.GetName()] = yamlFormatter

	return validFormatters
}

func GetFormatterNamesAsString() string {
	names := make([]string, 0, len(validFormatters))
	for name := range validFormatters {
		names = append(names, name)
	}
	sort.Strings(names)
	formatterNames := strings.Builder{}

	for index, formatterName := range names {

		if index != 0 {
			formatterNames.WriteString(", ")
		}
		formatterNames.WriteString("'" + formatterName + "'")
	}

	return formatterNames.String()
}

func validateFormatFlag(outputFormatString string) (tagsformatter.TagsFormatter, error) {
	var err error
	chosenFormatter, isPresent := validFormatters[outputFormatString]

	if !isPresent {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_OUTPUT_FORMAT, outputFormatString, GetFormatterNamesAsString())
	}

	return chosenFormatter, err
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
					err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_GET_TAGS_REQUEST_FAILED, err.Error())
				} else {
					err = galasaErrors.HttpResponseToGalasaError(
						httpResponse,
						"",
						byteReader,
						galasaErrors.GALASA_ERROR_GET_TAGS_NO_RESPONSE_CONTENT,
						galasaErrors.GALASA_ERROR_GET_TAGS_RESPONSE_BODY_UNREADABLE,
						galasaErrors.GALASA_ERROR_GET_TAGS_UNPARSEABLE_CONTENT,
						galasaErrors.GALASA_ERROR_GET_TAGS_SERVER_REPORTED_ERROR,
						galasaErrors.GALASA_ERROR_GET_TAGS_EXPLANATION_NOT_JSON,
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