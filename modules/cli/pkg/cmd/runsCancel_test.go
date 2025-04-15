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

func TestRunsCancelCommandInCommandCollection(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	runsCancelCommand, err := commands.GetCommand(COMMAND_NAME_RUNS_CANCEL)
	assert.Nil(t, err)

	assert.Equal(t, COMMAND_NAME_RUNS_CANCEL, runsCancelCommand.Name())
	assert.NotNil(t, runsCancelCommand.Values())
	assert.IsType(t, &RunsCancelCmdValues{}, runsCancelCommand.Values())
	assert.NotNil(t, runsCancelCommand.CobraCommand())
}

func TestRunsCancelHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"runs", "cancel", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'runs cancel' command.", "", factory, t)
}

func TestRunsCancelNoFlagsReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"runs", "cancel"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.NotNil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "Error: required flag(s) \"name\" not set", factory, t)
}

func TestRunsCancelNameFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_CANCEL, factory, t)

	var args []string = []string{"runs", "cancel", "--name", "name"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsCancelCmdValues).runName, "name")
}

func TestRunsCancelNameNoParameterReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_RUNS_CANCEL, factory, t)

	var args []string = []string{"runs", "cancel", "--name"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "flag needs an argument: --name")

	// Check what the user saw was reasonable
	checkOutput("", "Error: flag needs an argument: --name", factory, t)
}

func TestRunsCancelUnknownParameterReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_RUNS_CANCEL, factory, t)

	var args []string = []string{"runs", "cancel", "--name", "name1", "--random", "random"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "unknown flag: --random")

	// Check what the user saw was reasonable
	checkOutput("", "Error: unknown flag: --random", factory, t)
}

func TestRunsCancelNameTwiceOverridesToLatestValue(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_CANCEL, factory, t)

	var args []string = []string{"runs", "cancel", "--name", "name1", "--name", "name2"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsCancelCmdValues).runName, "name2")
}
