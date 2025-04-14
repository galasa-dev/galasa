/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package auth

import (
	"fmt"
	"testing"

	"github.com/galasa-dev/cli/pkg/files"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

func TestGetAuthPropertiesWithValidPropertiesUnmarshalsAuthProperties(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := utils.NewMockEnv()
	mockGalasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	accessTokenValue := "abc"
	clientIdValue := "dummyId"
	tokenPropertyValue := accessTokenValue + TOKEN_SEPARATOR + clientIdValue

	mockFileSystem.WriteTextFile(
		mockGalasaHome.GetNativeFolderPath()+"/galasactl.properties",
		fmt.Sprintf("GALASA_TOKEN=%s", tokenPropertyValue))

	// When...
	authProperties, tokenGotBack, err := getAuthProperties(mockFileSystem, mockGalasaHome, mockEnvironment)

	// Then...
	assert.Nil(t, err, "Should not return an error if the galasactl.properties exists and the token property is present")
	assert.Equal(t, clientIdValue, authProperties.GetClientId())
	assert.Equal(t, accessTokenValue, authProperties.GetRefreshToken())
	assert.Equal(t, tokenGotBack, tokenPropertyValue)
}

func TestGetAuthPropertiesWithNoClientIdInTokenReturnsError(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := utils.NewMockEnv()
	mockGalasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	tokenPropertyValue := "this-is-my-access-token"

	mockFileSystem.WriteTextFile(
		mockGalasaHome.GetNativeFolderPath()+"/galasactl.properties",
		fmt.Sprintf("GALASA_TOKEN=%s", tokenPropertyValue))

	// When...
	_, _, err := getAuthProperties(mockFileSystem, mockGalasaHome, mockEnvironment)

	// Then...
	assert.NotNil(t, err, "Should return an error as the galasactl.properties exists but is missing part of the token value.")
	assert.Contains(t, err.Error(), "GAL1125E")
	assert.Contains(t, err.Error(), "GALASA_TOKEN")
}

func TestGetAuthPropertiesWithOnlySeparatorReturnsError(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := utils.NewMockEnv()
	mockGalasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	mockFileSystem.WriteTextFile(
		mockGalasaHome.GetNativeFolderPath()+"/galasactl.properties",
		fmt.Sprintf("GALASA_TOKEN=%s", TOKEN_SEPARATOR))

	// When...
	_, _, err := getAuthProperties(mockFileSystem, mockGalasaHome, mockEnvironment)

	// Then...
	assert.NotNil(t, err, "Should return an error as the galasactl.properties exists but is missing the access token and client ID parts of the token.")
	assert.Contains(t, err.Error(), "GAL1125E")
	assert.Contains(t, err.Error(), "GALASA_TOKEN")
}

func TestGetAuthPropertiesWithSeparatorButNoClientIdReturnsError(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := utils.NewMockEnv()
	mockGalasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	tokenPropertyValue := "my-token" + TOKEN_SEPARATOR

	mockFileSystem.WriteTextFile(
		mockGalasaHome.GetNativeFolderPath()+"/galasactl.properties",
		fmt.Sprintf("GALASA_TOKEN=%s", tokenPropertyValue))

	// When...
	_, _, err := getAuthProperties(mockFileSystem, mockGalasaHome, mockEnvironment)

	// Then...
	assert.NotNil(t, err, "Should return an error as the galasactl.properties exists but is missing the client ID part of the token.")
	assert.Contains(t, err.Error(), "GAL1125E")
	assert.Contains(t, err.Error(), "GALASA_TOKEN")
}

func TestGetAuthPropertiesWithSeparatorButNoAccessTokenReturnsError(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := utils.NewMockEnv()
	mockGalasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	tokenPropertyValue := TOKEN_SEPARATOR + "my-client-id"

	mockFileSystem.WriteTextFile(
		mockGalasaHome.GetNativeFolderPath()+"/galasactl.properties",
		fmt.Sprintf("GALASA_TOKEN=%s", tokenPropertyValue))

	// When...
	_, _, err := getAuthProperties(mockFileSystem, mockGalasaHome, mockEnvironment)

	// Then...
	assert.NotNil(t, err, "Should return an error as the galasactl.properties exists but is missing the access token part of the token.")
	assert.Contains(t, err.Error(), "GAL1125E")
	assert.Contains(t, err.Error(), "GALASA_TOKEN")
}

func TestGetAuthPropertiesWithBadlyFormattedTokenReturnsError(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := utils.NewMockEnv()
	mockGalasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	tokenPropertyValue := "this:is:a:token:with:too:many:parts"

	mockFileSystem.WriteTextFile(
		mockGalasaHome.GetNativeFolderPath()+"/galasactl.properties",
		fmt.Sprintf("GALASA_TOKEN=%s", tokenPropertyValue))

	// When...
	_, _, err := getAuthProperties(mockFileSystem, mockGalasaHome, mockEnvironment)

	// Then...
	assert.NotNil(t, err, "Should return an error as the galasactl.properties exists but the access token is missing from the file.")
	assert.Contains(t, err.Error(), "GAL1125E")
	assert.Contains(t, err.Error(), "GALASA_TOKEN")
}

func TestGetAuthPropertiesWithEmptyGalasactlPropertiesReturnsError(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := utils.NewMockEnv()
	mockGalasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	mockFileSystem.WriteTextFile(mockGalasaHome.GetNativeFolderPath()+"/galasactl.properties", "")

	// When...
	_, _, err := getAuthProperties(mockFileSystem, mockGalasaHome, mockEnvironment)

	// Then...
	assert.NotNil(t, err, "Should return an error if the galasactl.properties is empty and an environment variable has not been set")
	assert.ErrorContains(t, err, "GAL1122E")
}

func TestGetAuthPropertiesWithMissingTokenPropertyReturnsError(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := utils.NewMockEnv()
	mockGalasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	// Create a galasactl.properties file that is missing the GALASA_TOKEN property
	mockFileSystem.WriteTextFile(mockGalasaHome.GetNativeFolderPath()+"/galasactl.properties", "unknown.value=blah")

	// When...
	_, _, err := getAuthProperties(mockFileSystem, mockGalasaHome, mockEnvironment)

	// Then...
	assert.NotNil(t, err, "Should return an error if the galasactl.properties exists and is missing a token property")
	assert.ErrorContains(t, err, "GAL1122E")
}

func TestGetAuthPropertiesWithMissingGalasactlPropertiesFileReturnsError(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := utils.NewMockEnv()
	mockGalasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	// When...
	_, _, err := getAuthProperties(mockFileSystem, mockGalasaHome, mockEnvironment)

	// Then...
	assert.NotNil(t, err, "Should return an error if the galasactl.properties does not exist")
	assert.ErrorContains(t, err, "GAL1043E")
}

func TestGetAuthPropertiesTokenEnvVarOverridesFileValue(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := utils.NewMockEnv()
	mockGalasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	accessTokenValue := "token-from-file"
	clientIdValue := "client-id-from-file"
	tokenPropertyValue := accessTokenValue + TOKEN_SEPARATOR + clientIdValue

	mockFileSystem.WriteTextFile(
		mockGalasaHome.GetNativeFolderPath()+"/galasactl.properties",
		fmt.Sprintf("GALASA_TOKEN=%s", tokenPropertyValue))

	accessTokenValue = "token-from-env-var"
	clientIdValue = "client-id-from-env-var"
	tokenPropertyValue = accessTokenValue + TOKEN_SEPARATOR + clientIdValue

	mockEnvironment.SetEnv(TOKEN_PROPERTY, tokenPropertyValue)

	// When...
	authProperties, _, err := getAuthProperties(mockFileSystem, mockGalasaHome, mockEnvironment)

	// Then...
	assert.Nil(t, err, "Should not return an error if the galasactl.properties exists and all required properties are present")
	assert.Equal(t, clientIdValue, authProperties.GetClientId())
	assert.Equal(t, accessTokenValue, authProperties.GetRefreshToken())
}
