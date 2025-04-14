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

func TestCommandListContainsProjectCommand(t *testing.T) {
	/// Given...
	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	// When...
	projectCommand, err := commands.GetCommand(COMMAND_NAME_PROJECT)
	assert.Nil(t, err)

	// Then...
	assert.NotNil(t, projectCommand)
	assert.Equal(t, COMMAND_NAME_PROJECT, projectCommand.Name())
	assert.Nil(t, projectCommand.Values())
}

func TestProjectHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"project", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'project' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestProjectNoCommandsProducesUsageReport(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	var args []string = []string{"project"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("Usage:\n  galasactl project [command]", "", factory, t)
}
