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

func TestCommandListContainsSecretsSetCommand(t *testing.T) {
    /// Given...
    factory := utils.NewMockFactory()
    commands, _ := NewCommandCollection(factory)

    // When...
    secretsCommand, err := commands.GetCommand(COMMAND_NAME_SECRETS_SET)
    assert.Nil(t, err)

    // Then...
    assert.NotNil(t, secretsCommand)
    assert.Equal(t, COMMAND_NAME_SECRETS_SET, secretsCommand.Name())
    assert.NotNil(t, secretsCommand.Values())
	assert.IsType(t, &SecretsSetCmdValues{}, secretsCommand.Values())
}

func TestSecretsSetHelpFlagSetCorrectly(t *testing.T) {
    // Given...
    factory := utils.NewMockFactory()

    var args []string = []string{"secrets", "set", "--help"}

    // When...
    err := Execute(factory, args)

    // Then...
    checkOutput("Creates or updates a secret in the credentials store", "", factory, t)

    assert.Nil(t, err)
}

func TestSecretsSetNoNameFlagProducesErrorMessage(t *testing.T) {
    // Given...
    factory := utils.NewMockFactory()
    var args []string = []string{"secrets", "set"}

    // When...
    err := Execute(factory, args)

    // Then...
    assert.NotNil(t, err)

    checkOutput("", `Error: required flag(s) "name" not set`, factory, t)
}

func TestSecretsSetNonEncodedUsernameFlagWithEncodedFlagProducesErrorMessage(t *testing.T) {
    // Given...
    factory := utils.NewMockFactory()
    var args []string = []string{"secrets", "set",
		"--name", "SYSTEM1",
		"--username", "myuser",
		"--base64-username", "mybase64user",
	}

    // When...
    err := Execute(factory, args)

    // Then...
    assert.NotNil(t, err)

    checkOutput("", "Error: if any flags in the group [username base64-username] are set none of the others can be", factory, t)
}

func TestSecretsSetNonEncodedPasswordFlagWithEncodedFlagProducesErrorMessage(t *testing.T) {
    // Given...
    factory := utils.NewMockFactory()
    var args []string = []string{"secrets", "set",
		"--name", "SYSTEM1",
		"--username", "myuser",
		"--password", "mypassword",
		"--base64-password", "my-base64-password",
	}

    // When...
    err := Execute(factory, args)

    // Then...
    assert.NotNil(t, err)

    checkOutput("", "Error: if any flags in the group [password token base64-password base64-token] are set none of the others can be", factory, t)
}

func TestSecretsSetNonEncodedTokenFlagWithEncodedFlagProducesErrorMessage(t *testing.T) {
    // Given...
    factory := utils.NewMockFactory()
    var args []string = []string{"secrets", "set",
		"--name", "SYSTEM1",
		"--username", "myuser",
		"--token", "mytoken",
		"--base64-token", "my-base64-token",
	}

    // When...
    err := Execute(factory, args)

    // Then...
    assert.NotNil(t, err)

    checkOutput("", "Error: if any flags in the group [password token base64-password base64-token] are set none of the others can be", factory, t)
}

func TestSecretsSetPasswordAndTokenFlagsProducesErrorMessage(t *testing.T) {
    // Given...
    factory := utils.NewMockFactory()
    var args []string = []string{"secrets", "set",
		"--name", "SYSTEM1",
		"--password", "mypassword",
		"--token", "mytoken",
	}

    // When...
    err := Execute(factory, args)

    // Then...
    assert.NotNil(t, err)

    checkOutput("", "Error: if any flags in the group [password token base64-password base64-token] are set none of the others can be", factory, t)
}

func TestSecretsSetWithOnlyNameFlagProducesErrorMessage(t *testing.T) {
    // Given...
    factory := utils.NewMockFactory()
    var args []string = []string{"secrets", "set", "--name", "SYSTEM1" }

    // When...
    err := Execute(factory, args)

    // Then...
    assert.NotNil(t, err)

    checkOutput("", "Error: at least one of the flags in the group [username password token base64-username base64-password base64-token description] is required", factory, t)
}
