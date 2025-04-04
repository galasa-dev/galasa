/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package secretsformatter

import (
	"fmt"
	"testing"

	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/stretchr/testify/assert"
)

func createMockGalasaSecret(secretName string) galasaapi.GalasaSecret {
	return createMockGalasaSecretWithDescription(secretName, "")
}

func generateExpectedSecretYaml(secretName string) string {
    return fmt.Sprintf(
`apiVersion: %s
kind: GalasaSecret
metadata:
    name: %s
    lastUpdatedTime: 2024-01-01T10:00:00Z
    lastUpdatedBy: %s
    encoding: %s
    type: UsernamePassword
data:
    username: %s
    password: %s`, API_VERSION, secretName, DUMMY_USERNAME, DUMMY_ENCODING, DUMMY_USERNAME, DUMMY_PASSWORD)
}

func TestSecretsYamlFormatterNoDataReturnsBlankString(t *testing.T) {
    // Given...
    formatter := NewSecretYamlFormatter()
    formattableSecret := make([]galasaapi.GalasaSecret, 0)

    // When...
    actualFormattedOutput, err := formatter.FormatSecrets(formattableSecret)

    // Then...
    assert.Nil(t, err)
    expectedFormattedOutput := ""
    assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestSecretsYamlFormatterSingleDataReturnsCorrectly(t *testing.T) {
    // Given..
    formatter := NewSecretYamlFormatter()
    formattableSecrets := make([]galasaapi.GalasaSecret, 0)
    secretName := "SECRET1"
    secret1 := createMockGalasaSecret(secretName)
    formattableSecrets = append(formattableSecrets, secret1)

    // When...
    actualFormattedOutput, err := formatter.FormatSecrets(formattableSecrets)

    // Then...
    assert.Nil(t, err)
    expectedFormattedOutput := generateExpectedSecretYaml(secretName) + "\n"
    assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestSecretsYamlFormatterMultipleDataSeperatesWithNewLine(t *testing.T) {
    // For..
    formatter := NewSecretYamlFormatter()
    formattableSecrets := make([]galasaapi.GalasaSecret, 0)

    secret1Name := "MYSECRET"
    secret2Name := "MY-NEXT-SECRET"
    secret1 := createMockGalasaSecret(secret1Name)
    secret2 := createMockGalasaSecret(secret2Name)
    formattableSecrets = append(formattableSecrets, secret1, secret2)

    // When...
    actualFormattedOutput, err := formatter.FormatSecrets(formattableSecrets)

    // Then...
    assert.Nil(t, err)
    expectedSecret1Output := generateExpectedSecretYaml(secret1Name)
    expectedSecret2Output := generateExpectedSecretYaml(secret2Name)
    expectedFormattedOutput := fmt.Sprintf(`%s
---
%s
`, expectedSecret1Output, expectedSecret2Output)
    assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}