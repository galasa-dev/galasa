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

func TestCommandListContainsLocalCommand(t *testing.T) {
	/// Given...
	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	// When...
	localCommand, err := commands.GetCommand(COMMAND_NAME_LOCAL)
	assert.Nil(t, err)

	// Then...
	assert.NotNil(t, localCommand)
	assert.Equal(t, COMMAND_NAME_LOCAL, localCommand.Name())
	assert.Nil(t, localCommand.Values())
}

func TestLocalHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"local", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'local' command", "", factory, t)

	assert.Nil(t, err)
}

func TestLocalNoCommandsProducesUsageReport(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	var args []string = []string{"local"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("Usage:\n  galasactl local [command]", "", factory, t)
}
