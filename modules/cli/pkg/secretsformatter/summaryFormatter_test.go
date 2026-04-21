/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package secretsformatter

import (
    "testing"
    "time"

    "github.com/galasa-dev/cli/pkg/galasaapi"
    "github.com/stretchr/testify/assert"
)

const (
    API_VERSION             = "galasa-dev/v1alpha1"
    DUMMY_ENCODING          = "myencoding"
    DUMMY_USERNAME          = "dummy-username"
    DUMMY_PASSWORD          = "dummy-password"
    DUMMY_KEYSTORE          = "dGVzdC1rZXlzdG9yZS1kYXRh"
	DUMMY_KEYSTORE_PASSWORD = "keystore-password"
)

func createMockSecretBase(
	secretName string,
	description string,
	secretType galasaapi.GalasaSecretType,
) galasaapi.GalasaSecret {
    secret := *galasaapi.NewGalasaSecret()

    secret.SetApiVersion(API_VERSION)
    secret.SetKind("GalasaSecret")

    secretMetadata := *galasaapi.NewGalasaSecretMetadata()
    secretMetadata.SetName(secretName)
    secretMetadata.SetEncoding(DUMMY_ENCODING)
    secretMetadata.SetType(secretType)
    secretMetadata.SetLastUpdatedBy(DUMMY_USERNAME)
    secretMetadata.SetLastUpdatedTime(time.Date(2024, 01, 01, 10, 0, 0, 0, time.UTC))

    if description != "" {
        secretMetadata.SetDescription(description)
    }

	secret.SetMetadata(secretMetadata)

    return secret
}

func createMockGalasaSecretWithDescription(
    secretName string,
    description string,
) galasaapi.GalasaSecret {
    secret := createMockSecretBase(secretName, description, galasaapi.GalasaSecretType("UsernamePassword"))

    secretData := *galasaapi.NewGalasaSecretData()
    secretData.SetUsername(DUMMY_USERNAME)
    secretData.SetPassword(DUMMY_PASSWORD)

    secret.SetData(secretData)
    return secret
}

func createMockKeystoreSecretWithDescription(
	secretName string,
	description string,
	keystoreType string,
) galasaapi.GalasaSecret {
	secret := createMockSecretBase(secretName, description, galasaapi.GalasaSecretType("KeyStore"))

	secretData := *galasaapi.NewGalasaSecretData()
	secretData.SetKeystore(DUMMY_KEYSTORE)
	secretData.SetKeystorePassword(DUMMY_KEYSTORE_PASSWORD)
	secretData.SetKeystoreType(keystoreType)

	secret.SetData(secretData)
	return secret
}

func TestSecretSummaryFormatterNoDataReturnsTotalCountAllZeros(t *testing.T) {
    // Given...
    formatter := NewSecretSummaryFormatter()
    secrets := make([]galasaapi.GalasaSecret, 0)

    // When...
    actualFormattedOutput, err := formatter.FormatSecrets(secrets)

    // Then...
    assert.Nil(t, err)
    expectedFormattedOutput := "Total:0\n"
    assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestSecretSummaryFormatterSingleDataReturnsCorrectly(t *testing.T) {
    // Given...
    formatter := NewSecretSummaryFormatter()
    description := "secret for system1"
    secretName := "MYSECRET"
    secret1 := createMockGalasaSecretWithDescription(secretName, description)
    secrets := []galasaapi.GalasaSecret{ secret1 }

    // When...
    actualFormattedOutput, err := formatter.FormatSecrets(secrets)

    // Then...
    assert.Nil(t, err)
    expectedFormattedOutput :=
`name     type             last-updated(UTC)   last-updated-by description
MYSECRET UsernamePassword 2024-01-01 10:00:00 dummy-username  secret for system1

Total:1
`
    assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestSecretSummaryFormatterMultipleDataSeperatesWithNewLine(t *testing.T) {
    // Given..
    formatter := NewSecretSummaryFormatter()
    secrets := make([]galasaapi.GalasaSecret, 0)

    secret1Name := "SECRET1"
    secret1Description := "my first secret"
    secret2Name := "SECRET2"
    secret2Description := "my second secret"
    secret3Name := "SECRET3"
    secret3Description := "my third secret"

    secret1 := createMockGalasaSecretWithDescription(secret1Name, secret1Description)
    secret2 := createMockGalasaSecretWithDescription(secret2Name, secret2Description)
    secret3 := createMockGalasaSecretWithDescription(secret3Name, secret3Description)
    secrets = append(secrets, secret1, secret2, secret3)

    // When...
    actualFormattedOutput, err := formatter.FormatSecrets(secrets)

    // Then...
    assert.Nil(t, err)
    expectedFormattedOutput :=
`name    type             last-updated(UTC)   last-updated-by description
SECRET1 UsernamePassword 2024-01-01 10:00:00 dummy-username  my first secret
SECRET2 UsernamePassword 2024-01-01 10:00:00 dummy-username  my second secret
SECRET3 UsernamePassword 2024-01-01 10:00:00 dummy-username  my third secret

Total:3
`
    assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

// --------------------------------
// Keystore Secret Tests
// --------------------------------

func TestSecretSummaryFormatterKeystoreJKSDisplaysOK(t *testing.T) {
	// Given...
	formatter := NewSecretSummaryFormatter()
	description := "my jks keystore"
	secretName := "MYJKSKEYSTORE"
	secret1 := createMockKeystoreSecretWithDescription(secretName, description, "JKS")
	secrets := []galasaapi.GalasaSecret{secret1}

	// When...
	actualFormattedOutput, err := formatter.FormatSecrets(secrets)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput :=
`name          type     last-updated(UTC)   last-updated-by description
MYJKSKEYSTORE KeyStore 2024-01-01 10:00:00 dummy-username  my jks keystore

Total:1
`
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestSecretSummaryFormatterKeystorePKCS12DisplaysOK(t *testing.T) {
	// Given...
	formatter := NewSecretSummaryFormatter()
	description := "my pkcs12 keystore"
	secretName := "MYPKCS12KEYSTORE"
	secret1 := createMockKeystoreSecretWithDescription(secretName, description, "PKCS12")
	secrets := []galasaapi.GalasaSecret{secret1}

	// When...
	actualFormattedOutput, err := formatter.FormatSecrets(secrets)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput :=
`name             type     last-updated(UTC)   last-updated-by description
MYPKCS12KEYSTORE KeyStore 2024-01-01 10:00:00 dummy-username  my pkcs12 keystore

Total:1
`
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestSecretSummaryFormatterKeystoreWithoutDescriptionDisplaysOK(t *testing.T) {
	// Given...
	formatter := NewSecretSummaryFormatter()
	secretName := "KEYSTOREWITHOUTDESC"
	secret1 := createMockKeystoreSecretWithDescription(secretName, "", "JKS")
	secrets := []galasaapi.GalasaSecret{secret1}

	// When...
	actualFormattedOutput, err := formatter.FormatSecrets(secrets)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput :=
`name                type     last-updated(UTC)   last-updated-by description
KEYSTOREWITHOUTDESC KeyStore 2024-01-01 10:00:00 dummy-username  

Total:1
`
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestSecretSummaryFormatterMixedSecretsDisplaysCorrectly(t *testing.T) {
	// Given...
	formatter := NewSecretSummaryFormatter()
	secrets := make([]galasaapi.GalasaSecret, 0)

	secret1 := createMockGalasaSecretWithDescription("USERPWD", "username password secret")
	secret2 := createMockKeystoreSecretWithDescription("JKSSTORE", "jks keystore", "JKS")
	secret3 := createMockKeystoreSecretWithDescription("PKCS12STORE", "pkcs12 keystore", "PKCS12")
	secrets = append(secrets, secret1, secret2, secret3)

	// When...
	actualFormattedOutput, err := formatter.FormatSecrets(secrets)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput :=
`name        type             last-updated(UTC)   last-updated-by description
USERPWD     UsernamePassword 2024-01-01 10:00:00 dummy-username  username password secret
JKSSTORE    KeyStore         2024-01-01 10:00:00 dummy-username  jks keystore
PKCS12STORE KeyStore         2024-01-01 10:00:00 dummy-username  pkcs12 keystore

Total:3
`
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}
