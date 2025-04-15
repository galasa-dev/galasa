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

func TestUsersCommandInCommandCollection(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	usersCommand, err := commands.GetCommand(COMMAND_NAME_USERS)
	assert.Nil(t, err)

	assert.NotNil(t, usersCommand)
	assert.Equal(t, COMMAND_NAME_USERS, usersCommand.Name())
	assert.NotNil(t, usersCommand.Values())
	assert.IsType(t, &UsersCmdValues{}, usersCommand.Values())
	assert.NotNil(t, usersCommand.CobraCommand())
}

func TestUsersHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"users", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...

	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'users' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestUsersNoCommandsProducesUsageReport(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	var args []string = []string{"users"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.Nil(t, err)
	// Check what the user saw was reasonable
	checkOutput("Usage:\n  galasactl users [command]", "", factory, t)
}
