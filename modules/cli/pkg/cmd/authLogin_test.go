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

func TestAuthLoginCommandInCommandCollection(t *testing.T) {
	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	authCommand, err := commands.GetCommand(COMMAND_NAME_AUTH_LOGIN)
	assert.Nil(t, err)

	assert.NotNil(t, authCommand)
	assert.Equal(t, COMMAND_NAME_AUTH_LOGIN, authCommand.Name())
	assert.NotNil(t, authCommand.Values())
	assert.IsType(t, &AuthLoginCmdValues{}, authCommand.Values())
	assert.NotNil(t, authCommand.CobraCommand())
}

func TestAuthLoginHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"auth", "login", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'auth login' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestAuthLoginNoFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_AUTH_LOGIN, factory, t)

	var args []string = []string{"auth", "login"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)
}
