/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tags

import (
	"log"
	"strings"
	"github.com/galasa-dev/cli/pkg/utils"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
)

func validateTagName(tagName string) error {
	var err error
	log.Println("Validating the provided tag name")

	err = validateStringIsLatin1AndNotBlank(tagName, galasaErrors.GALASA_ERROR_TAGS_INVALID_NAME)

	if err == nil {
		log.Println("Tag name validated OK")
	}
	return err
}

func validateDescription(description string) error {
	var err error
	log.Println("Validating the provided description")

	err = validateStringIsLatin1AndNotBlank(description, galasaErrors.GALASA_ERROR_INVALID_TAG_DESCRIPTION)
	if err == nil {
		log.Println("Description validated OK")
	}
	return err
}

func validateStringIsLatin1AndNotBlank(str string, errMessageType *galasaErrors.MessageType) error {
	var err error
	str = strings.TrimSpace(str)

	if str == "" || !utils.IsLatin1(str) {
			err = galasaErrors.NewGalasaError(errMessageType)
	}
	return err
}