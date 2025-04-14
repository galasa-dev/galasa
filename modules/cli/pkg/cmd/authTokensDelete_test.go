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

func TestAuthTokensDeleteCommandInCommandCollection(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	AuthTokensDeleteCommand, err := commands.GetCommand(COMMAND_NAME_AUTH_TOKENS_DELETE)
	assert.Nil(t, err)

	assert.Equal(t, COMMAND_NAME_AUTH_TOKENS_DELETE, AuthTokensDeleteCommand.Name())
	assert.NotNil(t, AuthTokensDeleteCommand.Values())
	assert.IsType(t, &AuthTokensDeleteCmdValues{}, AuthTokensDeleteCommand.Values())
	assert.NotNil(t, AuthTokensDeleteCommand.CobraCommand())
}

func TestAuthTokensDeleteHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"auth", "tokens", "delete", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...

	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'auth tokens delete' command.", "", factory, t)

	assert.Nil(t, err)
}


func TestAuthTokensDeleteWithTokenIdReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_AUTH_TOKENS_DELETE, factory, t)

	var args []string = []string{"auth", "tokens", "delete", "--tokenid", "abc"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Nil(t, err)
}

func TestAuthTokensDeleteWithoutTokenIdFlagValueReturnsAppropriateOutput(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_AUTH_TOKENS_DELETE, factory, t)

	var args []string = []string{"auth", "tokens", "delete", "--tokenid"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	// Check what the user saw is reasonable.
	assert.NotNil(t, err)
	checkOutput("", "flag needs an argument: --tokenid", factory, t)

}
