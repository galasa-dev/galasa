/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
    "testing"

    "github.com/galasa-dev/cli/pkg/utils"
    "github.com/stretchr/testify/assert"
)

func TestCommandListContainsSecretsCommand(t *testing.T) {
    /// Given...
    factory := utils.NewMockFactory()
    commands, _ := NewCommandCollection(factory)

    // When...
    secretsCommand, err := commands.GetCommand(COMMAND_NAME_SECRETS)
    assert.Nil(t, err)

    // Then...
    assert.NotNil(t, secretsCommand)
    assert.Equal(t, COMMAND_NAME_SECRETS, secretsCommand.Name())
    assert.NotNil(t, secretsCommand.Values())
    assert.IsType(t, &SecretsCmdValues{}, secretsCommand.Values())
}

func TestSecretsHelpFlagSetCorrectly(t *testing.T) {
    // Given...
    factory := utils.NewMockFactory()

    var args []string = []string{"secrets", "--help"}

    // When...
    err := Execute(factory, args)

    // Then...
    // Check what the user saw is reasonable.
    checkOutput("The parent command for operations to manipulate secrets in the Galasa service's credentials store", "", factory, t)

    assert.Nil(t, err)
}

func TestSecretsNoCommandsProducesUsageReport(t *testing.T) {
    // Given...
    factory := utils.NewMockFactory()
    var args []string = []string{"secrets"}

    // When...
    err := Execute(factory, args)

    // Then...
    assert.Nil(t, err)

    checkOutput("Usage:\n  galasactl secrets [command]", "", factory, t)
}
