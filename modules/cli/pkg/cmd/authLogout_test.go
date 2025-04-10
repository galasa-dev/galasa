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

func TestAuthogoutCommandInCommandCollectionHasName(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	authLogoutommand, err := commands.GetCommand(COMMAND_NAME_AUTH_LOGOUT)
	assert.Nil(t, err)

	assert.NotNil(t, authLogoutommand)
	assert.Equal(t, COMMAND_NAME_AUTH_LOGOUT, authLogoutommand.Name())
	assert.NotNil(t, authLogoutommand.Values())
	assert.NotNil(t, authLogoutommand.CobraCommand())
}

func TestAuthLogoutHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"auth", "logout", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...

	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'auth logout' command.", "", factory, t)

	assert.Nil(t, err)
}
func TestAuthLogoutNoFlagsExecutesOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_AUTH_LOGOUT, factory, t)

	var args []string = []string{"auth", "logout"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Nil(t, err)
}
