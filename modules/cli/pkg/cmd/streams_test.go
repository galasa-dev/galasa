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

func TestStreamsCommandInCommandCollection(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	streamsCommand, err := commands.GetCommand(COMMAND_NAME_STREAMS)
	assert.Nil(t, err)

	assert.NotNil(t, streamsCommand)
	assert.Equal(t, COMMAND_NAME_STREAMS, streamsCommand.Name())
	assert.NotNil(t, streamsCommand.Values())
	assert.IsType(t, &StreamsCmdValues{}, streamsCommand.Values())
	assert.NotNil(t, streamsCommand.CobraCommand())
}

func TestStreamsHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"streams", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...

	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'streams' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestStreamsNoCommandsProducesUsageReport(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	var args []string = []string{"streams"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.Nil(t, err)
	// Check what the user saw was reasonable
	checkOutput("Usage:\n  galasactl streams [command]", "", factory, t)
}
