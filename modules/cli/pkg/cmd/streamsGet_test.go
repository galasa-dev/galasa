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

func TestStreamsGetCommandInCommandCollectionHasName(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	StreamsGetCommand, err := commands.GetCommand(COMMAND_NAME_STREAMS_GET)
	assert.Nil(t, err)

	assert.Equal(t, COMMAND_NAME_STREAMS_GET, StreamsGetCommand.Name())
	assert.NotNil(t, StreamsGetCommand.CobraCommand())
}

func TestStreamsGetHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"streams", "get", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'streams get' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestStreamsGetNameFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_STREAMS_GET, factory, t)

	var args []string = []string{"streams", "get", "--name", "mystream"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	parentCmd, err := commandCollection.GetCommand(COMMAND_NAME_STREAMS)
	assert.Nil(t, err)
	assert.Contains(t, parentCmd.Values().(*StreamsCmdValues).name, "mystream")
}

func TestStreamsGetFormatFlagYamlValueReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_STREAMS_GET, factory, t)

	var args []string = []string{"streams", "get", "--name", "mystream", "--format", "yaml"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	parentCmd, err := commandCollection.GetCommand(COMMAND_NAME_STREAMS)
	assert.Nil(t, err)
	assert.Contains(t, parentCmd.Values().(*StreamsCmdValues).name, "mystream")
}

func TestStreamsGetFormatFlagYamlSummaryReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_STREAMS_GET, factory, t)

	var args []string = []string{"streams", "get", "--name", "mystream", "--format", "summary"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	parentCmd, err := commandCollection.GetCommand(COMMAND_NAME_STREAMS)
	assert.Nil(t, err)
	assert.Contains(t, parentCmd.Values().(*StreamsCmdValues).name, "mystream")
}
