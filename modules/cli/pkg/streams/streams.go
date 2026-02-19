/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package streams

import (
	"net/url"
	"strings"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/utils"
)

func validateStreamName(streamName string) (string, error) {

	var err error
	streamName = strings.TrimSpace(streamName)

	if streamName == "" {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_MISSING_STREAM_NAME_FLAG)
	} else {
		if !utils.IsNameValid(streamName) {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_STREAM_NAME)
		}
	}

	return streamName, err

}

func validateDescription(description string) (string, error) {
    var err error
    description = strings.TrimSpace(description)

    if description == "" || !utils.IsLatin1(description) {
        err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_STREAM_DESCRIPTION)
    }
    return description, err
}

func validateUrl(urlString string) (string, error) {
	var err error
	urlString = strings.TrimSpace(urlString)

	if urlString == "" {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_URL)
	} else {
		_, parseErr := url.ParseRequestURI(urlString)
		if parseErr != nil {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_URL, urlString, parseErr.Error())
		}
	}

	return urlString, err
}

func convertObrsToObrBeans(obrCoordinates []utils.MavenCoordinates) ([]galasaapi.StreamOBRData, error) {
	streamObrs := make([]galasaapi.StreamOBRData, 0, len(obrCoordinates))

	for _, coordinates := range obrCoordinates {
		// Create StreamOBRData object from MavenCoordinates
		streamObrData := galasaapi.NewStreamOBRData()
		streamObrData.SetGroupId(coordinates.GroupId)
		streamObrData.SetArtifactId(coordinates.ArtifactId)
		streamObrData.SetVersion(coordinates.Version)

		streamObrs = append(streamObrs, *streamObrData)
	}

	return streamObrs, nil
}
