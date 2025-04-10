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

func TestCommandListContainsSecretsGetCommand(t *testing.T) {
    /// Given...
    factory := utils.NewMockFactory()
    commands, _ := NewCommandCollection(factory)

    // When...
    secretsCommand, err := commands.GetCommand(COMMAND_NAME_SECRETS_GET)
    assert.Nil(t, err)

    // Then...
    assert.NotNil(t, secretsCommand)
    assert.Equal(t, COMMAND_NAME_SECRETS_GET, secretsCommand.Name())
    assert.NotNil(t, secretsCommand.Values())
	assert.IsType(t, &SecretsGetCmdValues{}, secretsCommand.Values())
}

func TestSecretsGetHelpFlagSetCorrectly(t *testing.T) {
    // Given...
    factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_SECRETS_GET, factory, t)

    var args []string = []string{"secrets", "get", "--help"}

    // When...
    err := commandCollection.Execute(args)

    // Then...
    checkOutput("Get a list of secrets or a specific secret from the credentials store", "", factory, t)

    assert.Nil(t, err)
}

func TestSecretsGetNoFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_SECRETS_GET, factory, t)

	var args []string = []string{"secrets", "get"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)
}

