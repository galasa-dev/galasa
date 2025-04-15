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
    API_VERSION = "galasa-dev/v1alpha1"
    DUMMY_ENCODING = "myencoding"
    DUMMY_USERNAME = "dummy-username"
    DUMMY_PASSWORD = "dummy-password"
)

func createMockGalasaSecretWithDescription(
    secretName string,
    description string,
) galasaapi.GalasaSecret {
    secret := *galasaapi.NewGalasaSecret()

    secret.SetApiVersion(API_VERSION)
    secret.SetKind("GalasaSecret")

    secretMetadata := *galasaapi.NewGalasaSecretMetadata()
    secretMetadata.SetName(secretName)
    secretMetadata.SetEncoding(DUMMY_ENCODING)
    secretMetadata.SetType("UsernamePassword")
    secretMetadata.SetLastUpdatedBy(DUMMY_USERNAME)
    secretMetadata.SetLastUpdatedTime(time.Date(2024, 01, 01, 10, 0, 0, 0, time.UTC))

    if description != "" {
        secretMetadata.SetDescription(description)
    }

    secretData := *galasaapi.NewGalasaSecretData()
    secretData.SetUsername(DUMMY_USERNAME)
    secretData.SetPassword(DUMMY_PASSWORD)

    secret.SetMetadata(secretMetadata)
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
