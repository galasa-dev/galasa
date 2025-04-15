/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"strings"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
)

type MavenCoordinates struct {
	GroupId    string
	ArtifactId string
	Version    string
	Classifier string
}

// We expect a parameter to be of the form:
// mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr
// Validate that the --obr parameter or portfolio field  passed by the user conform to this convention by splitting the
// input into pieces.
func ValidateObrs(obrInputs []string) ([]MavenCoordinates, error) {

	var err error
	obrs := make([]MavenCoordinates, 0)

	for _, obr := range obrInputs {
		var coordinates MavenCoordinates
		coordinates, err = ValidateObr(obr)
		if err == nil {
			obrs = append(obrs, coordinates)
		} else {
			break
		}
	}
	return obrs, err
}

// We expect a parameter to be of the form:
// mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr
// Validate that the --obr parameter or portfolio field passed by the user conform to this convention by splitting the
// input into pieces.
func ValidateObr(obr string) (MavenCoordinates, error) {
	var err error
	var coordinates MavenCoordinates

	parts := strings.Split(obr, "/")
	if len(parts) < 4 {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_OBR_NOT_ENOUGH_PARTS, obr)
	} else if len(parts) > 4 {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_OBR_TOO_MANY_PARTS, obr)
	} else if !strings.HasPrefix(parts[0], "mvn:") {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_OBR_NO_MVN_PREFIX, obr)
	} else if parts[3] != "obr" {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_OBR_NO_OBR_SUFFIX, obr)
	} else {
		groupId := strings.ReplaceAll(parts[0], "mvn:", "")
		coordinates = MavenCoordinates{
			GroupId:    groupId,
			ArtifactId: parts[1],
			Version:    parts[2],
			Classifier: parts[3],
		}
	}
	return coordinates, err
}
