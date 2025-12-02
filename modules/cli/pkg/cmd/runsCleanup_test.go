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

func TestCommandListContainsRunsCleanupCommand(t *testing.T) {
	/// Given...
	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	// When...
	command, err := commands.GetCommand(COMMAND_NAME_RUNS_CLEANUP)
	assert.Nil(t, err)

	// Then...
	assert.NotNil(t, command)
	assert.Equal(t, COMMAND_NAME_RUNS_CLEANUP, command.Name())
	assert.NotNil(t, command.Values())
	assert.IsType(t, &RunsCleanupCmdValues{}, command.Values())
}

func TestRunsCleanupHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"runs", "cleanup", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'runs cleanup' command", "", factory, t)

	assert.Nil(t, err)
}

func TestRunsCleanupNoCommandsProducesUsageReport(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	var args []string = []string{"runs", "cleanup"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.Nil(t, err)

	checkOutput("Usage:\n  galasactl runs cleanup [command]", "", factory, t)
}
