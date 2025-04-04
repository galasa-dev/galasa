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

func TestRunsResetCommandInCommandCollection(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	runsResetCommand, err := commands.GetCommand(COMMAND_NAME_RUNS_RESET)
	assert.Nil(t, err)

	assert.Equal(t, COMMAND_NAME_RUNS_RESET, runsResetCommand.Name())
	assert.NotNil(t, runsResetCommand.Values())
	assert.IsType(t, &RunsResetCmdValues{}, runsResetCommand.Values())
	assert.NotNil(t, runsResetCommand.CobraCommand())
}

func TestRunsResetHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"runs", "reset", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'runs reset' command.", "", factory, t)
}

func TestRunsResetNoFlagsReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"runs", "reset"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.NotNil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "Error: required flag(s) \"name\" not set", factory, t)
}

func TestRunsResetNameFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_RESET, factory, t)

	var args []string = []string{"runs", "reset", "--name", "name"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsResetCmdValues).runName, "name")
}

func TestRunsResetNameNoParameterReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_RUNS_RESET, factory, t)

	var args []string = []string{"runs", "reset", "--name"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "flag needs an argument: --name")

	// Check what the user saw was reasonable
	checkOutput("", "Error: flag needs an argument: --name", factory, t)
}

func TestRunsResetUnknownParameterReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_RUNS_RESET, factory, t)

	var args []string = []string{"runs", "reset", "--name", "name1", "--random", "random"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "unknown flag: --random")

	// Check what the user saw was reasonable
	checkOutput("", "Error: unknown flag: --random", factory, t)
}

func TestRunsResetNameTwiceOverridesToLatestValue(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_RESET, factory, t)

	var args []string = []string{"runs", "reset", "--name", "name1", "--name", "name2"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsResetCmdValues).runName, "name2")
}
