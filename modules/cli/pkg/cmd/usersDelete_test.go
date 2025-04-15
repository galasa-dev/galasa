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

func TestUsersDeleteCommandInCommandCollectionHasName(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	UsersDeleteCommand, err := commands.GetCommand(COMMAND_NAME_USERS_DELETE)
	assert.Nil(t, err)

	assert.Equal(t, COMMAND_NAME_USERS_DELETE, UsersDeleteCommand.Name())
	assert.NotNil(t, UsersDeleteCommand.CobraCommand())
}

func TestUsersDeleteHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"users", "delete", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'users delete' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestUsersDeleteNamespaceNameFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_USERS_DELETE, factory, t)

	var args []string = []string{"users", "delete", "--login-id", "admin"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Nil(t, err)
}
