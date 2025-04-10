/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package auth

import (
	"log"
	"path/filepath"
	"strings"

	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/props"
	"github.com/galasa-dev/cli/pkg/spi"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
)

const (
	TOKEN_PROPERTY  = "GALASA_TOKEN"
	TOKEN_SEPARATOR = ":"
)

// Gets authentication properties from the user's galasactl.properties file or from the environment or a mixture.
func getAuthProperties(fileSystem spi.FileSystem, galasaHome spi.GalasaHome, env spi.Environment) (galasaapi.AuthProperties, string, error) {
	var err error
	authProperties := galasaapi.NewAuthProperties()

	// Work out which file we we want to draw properties from.
	galasactlPropertiesFilePath := filepath.Join(galasaHome.GetNativeFolderPath(), "galasactl.properties")

	// Get the file-based token property if we can
	galasaToken, fileAccessErr := getPropertyFromFile(fileSystem, galasactlPropertiesFilePath, TOKEN_PROPERTY)

	// Over-write the token property value if there is an environment variable set to do that.
	galasaToken = getPropertyWithOverride(env, galasaToken, galasactlPropertiesFilePath, TOKEN_PROPERTY)

	// Make sure all the properties have values that we need.
	err = checkPropertyIsSet(galasaToken, TOKEN_PROPERTY, galasactlPropertiesFilePath, fileAccessErr)
	if err == nil {
		var refreshToken string
		var clientId string

		// Get the authentication properties from the token
		refreshToken, clientId, err = extractPropertiesFromToken(galasaToken)
		if err == nil {
			authProperties.SetClientId(clientId)
			authProperties.SetRefreshToken(refreshToken)
		}
	}

	return *authProperties, galasaToken, err
}

func checkPropertyIsSet(propertyValue string, propertyName string, galasactlPropertiesFilePath string, fileAccessErr error) error {
	var err error
	if propertyValue == "" {
		// Property has not been set.
		if fileAccessErr != nil {
			// Being unable to read the file was the cause of this.
			err = fileAccessErr
		} else {
			log.Printf("Error: Auth property '%s' has not been set", propertyName)
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_AUTH_PROPERTY_NOT_AVAILABLE, propertyName, galasactlPropertiesFilePath)
		}
	}
	return err
}

func getPropertyWithOverride(env spi.Environment, valueFromFile string, filePathGatheredFrom string, propertyName string) string {
	value := env.GetEnv(propertyName)
	if value != "" {
		// env var has been set.
		if valueFromFile == "" {
			log.Printf("environment variable '%s' over-rides a value from file '%s'", propertyName, filePathGatheredFrom)
		} else {
			log.Printf("environment variable '%s' used to control authentication.", propertyName)
		}
	} else {
		value = valueFromFile
	}
	return value
}

// Gets a property from the user's galasactl.properties file
func getPropertyFromFile(fileSystem spi.FileSystem, galasactlPropertiesFilePath string, propertyName string) (string, error) {
	var err error
	var galasactlProperties props.JavaProperties
	galasactlProperties, err = props.ReadPropertiesFile(fileSystem, galasactlPropertiesFilePath)
	if err != nil {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_FAILED_TO_READ_FILE, galasactlPropertiesFilePath, err.Error())
	}

	return galasactlProperties[propertyName], err
}

func extractPropertiesFromToken(token string) (string, string, error) {
	var err error
	var refreshToken string
	var clientId string

	// The GALASA_TOKEN property should be in the form {GALASA_ACCESS_TOKEN}:{GALASA_CLIENT_ID},
	// so it should split into two parts.
	tokenParts := strings.Split(token, TOKEN_SEPARATOR)

	if len(tokenParts) == 2 && tokenParts[0] != "" && tokenParts[1] != "" {
		refreshToken = tokenParts[0]
		clientId = tokenParts[1]
	} else {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_BAD_TOKEN_PROPERTY_FORMAT, TOKEN_PROPERTY, TOKEN_SEPARATOR)
	}

	return refreshToken, clientId, err
}
