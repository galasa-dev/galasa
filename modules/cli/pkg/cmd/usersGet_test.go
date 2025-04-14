package cmd

import (
	"testing"

	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

func TestUsersGetCommandInCommandCollectionHasName(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	UsersGetCommand, err := commands.GetCommand(COMMAND_NAME_USERS_GET)
	assert.Nil(t, err)

	assert.Equal(t, COMMAND_NAME_USERS_GET, UsersGetCommand.Name())
	assert.NotNil(t, UsersGetCommand.CobraCommand())
}

func TestUsersGetHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"users", "get", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'users get' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestUsersGetNamespaceNameFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_USERS_GET, factory, t)

	var args []string = []string{"users", "get", "--login-id", "me"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	parentCmd, err := commandCollection.GetCommand(COMMAND_NAME_USERS)
	assert.Nil(t, err)
	assert.Contains(t, parentCmd.Values().(*UsersCmdValues).name, "me")
}
