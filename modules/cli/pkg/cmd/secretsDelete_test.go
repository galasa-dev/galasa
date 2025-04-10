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

func TestCommandListContainsSecretsDeleteCommand(t *testing.T) {
    /// Given...
    factory := utils.NewMockFactory()
    commands, _ := NewCommandCollection(factory)

    // When...
    secretsCommand, err := commands.GetCommand(COMMAND_NAME_SECRETS_DELETE)
    assert.Nil(t, err)

    // Then...
    assert.NotNil(t, secretsCommand)
    assert.Equal(t, COMMAND_NAME_SECRETS_DELETE, secretsCommand.Name())
    assert.Nil(t, secretsCommand.Values())
}

func TestSecretsDeleteHelpFlagSetCorrectly(t *testing.T) {
    // Given...
    factory := utils.NewMockFactory()

    var args []string = []string{"secrets", "delete", "--help"}

    // When...
    err := Execute(factory, args)

    // Then...
    // Check what the user saw is reasonable.
    checkOutput("Deletes a secret from the credentials store", "", factory, t)

    assert.Nil(t, err)
}

func TestSecretsDeleteNoNameFlagProducesErrorMessage(t *testing.T) {
    // Given...
    factory := utils.NewMockFactory()
    var args []string = []string{"secrets", "delete"}

    // When...
    err := Execute(factory, args)

    // Then...
    assert.NotNil(t, err)

    checkOutput("", "Error: required flag(s) \"name\" not set", factory, t)
}
