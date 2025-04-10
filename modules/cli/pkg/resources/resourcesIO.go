/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package resources

import (
	"encoding/json"
	"log"
	"regexp"
	"strings"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/spi"
	"gopkg.in/yaml.v3"
)

func validateFilePathExists(fileSystem spi.FileSystem, filePath string) error {
	exists, err := fileSystem.Exists(filePath)

	if err != nil {
		return galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_UNABLE_TO_GET_FILE_NAME, err)
	}

	errorMsg := "no such file or directory"
	if !exists {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_UNABLE_TO_GET_FILE_NAME, errorMsg)
	} else {
		if !(strings.HasSuffix(filePath, ".yaml") || strings.HasSuffix(filePath, ".yml")) {
			errorMsg = "not a yaml file"
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_UNABLE_TO_GET_FILE_NAME, errorMsg)
		}
	}

	return err
}

func getYamlFileContent(fileSystem spi.FileSystem, filePath string) (string, error) {
	fileContent, err := fileSystem.ReadTextFile(filePath)
	if err != nil {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_COULD_NOT_GET_YAML_CONTENT, err)
	}
	return fileContent, err
}

func splitByRegexSeparator(input string, seperatorRegex string) (parts []string) {
	reg := regexp.MustCompile(seperatorRegex)
	//returns the start and end indexes of the separatorRegex string
	indexes := reg.FindAllStringIndex(input, -1)
	laststart := 0
	parts = make([]string, len(indexes)+1)
	for i, element := range indexes {
		parts[i] = input[laststart:element[0]]
		laststart = element[1]
	}
	parts[len(indexes)] = input[laststart:]
	return parts
}

func splitYamlIntoParts(yamlInput string) (parts []string) {
	return splitByRegexSeparator(yamlInput, "\n---[-]*\n")
}

func yamlToByteArray(inputYaml string, action string) ([]byte, error) {
	var err error
	var jsonBytes []byte

	parts := splitYamlIntoParts(inputYaml)

	var parsedParts []interface{}

	for _, partYaml := range parts {

		if len(strings.TrimSpace(partYaml)) == 0 { 
			// yaml section is empty
		} else {
			var parsedData interface{}
			err = yaml.Unmarshal([]byte(partYaml), &parsedData)
			if err != nil {
				log.Printf("Unable to unmarshal yaml data to retrieve yaml content")
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_UNABLE_TO_UNMARSHAL_YAML, err, partYaml)
				break
			} else {
				parsedParts = append(parsedParts, parsedData)
			}
		}
	}

	if err == nil {

		rootObj := make(map[string]interface{}, 100)
		rootObj["action"] = action
		rootObj["data"] = parsedParts
		jsonBytes, err = json.MarshalIndent(rootObj, "", "    ")
		if err != nil {
			log.Printf("Unable to marshal yaml data into json byte array")
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_UNABLE_TO_MARSHAL_INTO_JSON, err)
		}
	}

	return jsonBytes, err
}
