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

func TestAuthTokensGetCommandInCommandCollection(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	AuthTokensGetCommand, err := commands.GetCommand(COMMAND_NAME_AUTH_TOKENS_GET)
	assert.Nil(t, err)

	assert.Equal(t, COMMAND_NAME_AUTH_TOKENS_GET, AuthTokensGetCommand.Name())
	assert.Nil(t, AuthTokensGetCommand.Values())
	assert.NotNil(t, AuthTokensGetCommand.CobraCommand())
}

func TestAuthTokensGetHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"auth", "tokens", "get", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...

	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'auth tokens get' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestAuthTokensGetReturnsWithoutError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_AUTH_TOKENS_GET, factory, t)

	var args []string = []string{"auth", "tokens", "get"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Nil(t, err)
}
