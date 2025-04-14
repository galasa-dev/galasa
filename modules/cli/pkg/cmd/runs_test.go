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

func TestCommandListContainsRunsCommand(t *testing.T) {
	/// Given...
	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	// When...
	runsCommand, err := commands.GetCommand(COMMAND_NAME_RUNS)
	assert.Nil(t, err)

	// Then...
	assert.NotNil(t, runsCommand)
	assert.Equal(t, COMMAND_NAME_RUNS, runsCommand.Name())
	assert.NotNil(t, runsCommand.Values())
	assert.IsType(t, &RunsCmdValues{}, runsCommand.Values())
}

func TestRunsHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"runs", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'runs' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestRunsNoCommandsProducesUsageReport(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	var args []string = []string{"runs"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.Nil(t, err)

	checkOutput("Usage:\n  galasactl runs [command]", "", factory, t)
}
