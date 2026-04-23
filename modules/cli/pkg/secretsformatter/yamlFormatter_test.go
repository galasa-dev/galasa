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

func generateExpectedYamlBase(secretName string, secretType string, dataSection string) string {
    return fmt.Sprintf(
`apiVersion: %s
kind: GalasaSecret
metadata:
    name: %s
    lastUpdatedTime: 2024-01-01T10:00:00Z
    lastUpdatedBy: %s
    encoding: %s
    type: %s
data:
%s`, API_VERSION, secretName, DUMMY_USERNAME, DUMMY_ENCODING, secretType, dataSection)
}

func createMockGalasaSecret(secretName string) galasaapi.GalasaSecret {
	return createMockGalasaSecretWithDescription(secretName, "")
}

func generateExpectedSecretYaml(secretName string) string {
    dataSection := fmt.Sprintf(`    username: %s
    password: %s`, DUMMY_USERNAME, DUMMY_PASSWORD)
    return generateExpectedYamlBase(secretName, "UsernamePassword", dataSection)
}

func createMockKeystoreSecret(secretName string, keystoreType string) galasaapi.GalasaSecret {
    return createMockKeystoreSecretWithDescription(secretName, "", keystoreType)
}

func generateExpectedKeystoreSecretYaml(secretName string, keystoreType string) string {
    dataSection := fmt.Sprintf(`    keystore: %s
    keystorePassword: %s
    keystoreType: %s`, DUMMY_KEYSTORE, DUMMY_KEYSTORE_PASSWORD, keystoreType)
    return generateExpectedYamlBase(secretName, "KeyStore", dataSection)
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

func TestSecretsYamlFormatterKeystoreJKSReturnsCorrectly(t *testing.T) {
	// Given...
	formatter := NewSecretYamlFormatter()
	formattableSecrets := make([]galasaapi.GalasaSecret, 0)
	secretName := "MYJKSKEYSTORE"
	secret1 := createMockKeystoreSecret(secretName, "JKS")
	formattableSecrets = append(formattableSecrets, secret1)

	// When...
	actualFormattedOutput, err := formatter.FormatSecrets(formattableSecrets)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput := generateExpectedKeystoreSecretYaml(secretName, "JKS") + "\n"
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestSecretsYamlFormatterKeystorePKCS12ReturnsCorrectly(t *testing.T) {
	// Given...
	formatter := NewSecretYamlFormatter()
	formattableSecrets := make([]galasaapi.GalasaSecret, 0)
	secretName := "MYPKCS12KEYSTORE"
	secret1 := createMockKeystoreSecret(secretName, "PKCS12")
	formattableSecrets = append(formattableSecrets, secret1)

	// When...
	actualFormattedOutput, err := formatter.FormatSecrets(formattableSecrets)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput := generateExpectedKeystoreSecretYaml(secretName, "PKCS12") + "\n"
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestSecretsYamlFormatterKeystoreWithoutDescriptionReturnsCorrectly(t *testing.T) {
	// Given...
	formatter := NewSecretYamlFormatter()
	formattableSecrets := make([]galasaapi.GalasaSecret, 0)
	secretName := "KEYSTOREWITHOUTDESC"
	secret1 := createMockKeystoreSecret(secretName, "JKS")
	formattableSecrets = append(formattableSecrets, secret1)

	// When...
	actualFormattedOutput, err := formatter.FormatSecrets(formattableSecrets)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput := generateExpectedKeystoreSecretYaml(secretName, "JKS") + "\n"
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestSecretsYamlFormatterMixedSecretsReturnsCorrectly(t *testing.T) {
	// Given...
	formatter := NewSecretYamlFormatter()
	formattableSecrets := make([]galasaapi.GalasaSecret, 0)

	secret1Name := "USERPWD"
	secret1 := createMockGalasaSecret(secret1Name)
	
	secret2Name := "JKSSTORE"
	secret2 := createMockKeystoreSecret(secret2Name, "JKS")
	
	secret3Name := "PKCS12STORE"
	secret3 := createMockKeystoreSecret(secret3Name, "PKCS12")
	
	formattableSecrets = append(formattableSecrets, secret1, secret2, secret3)

	// When...
	actualFormattedOutput, err := formatter.FormatSecrets(formattableSecrets)

	// Then...
	assert.Nil(t, err)
	expectedSecret1Output := generateExpectedSecretYaml(secret1Name)
	expectedSecret2Output := generateExpectedKeystoreSecretYaml(secret2Name, "JKS")
	expectedSecret3Output := generateExpectedKeystoreSecretYaml(secret3Name, "PKCS12")
	expectedFormattedOutput := fmt.Sprintf(`%s
---
%s
---
%s
`, expectedSecret1Output, expectedSecret2Output, expectedSecret3Output)
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}