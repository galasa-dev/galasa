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

func TestStreamsDeleteCommandInCommandCollectionHasName(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	StreamsDeleteCommand, err := commands.GetCommand(COMMAND_NAME_STREAMS_DELETE)
	assert.Nil(t, err)

	assert.Equal(t, COMMAND_NAME_STREAMS_DELETE, StreamsDeleteCommand.Name())
	assert.NotNil(t, StreamsDeleteCommand.CobraCommand())

}

func TestStreamsDeleteHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"streams", "delete", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'streams delete' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestStreamsDeleteNamespaceNameFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_STREAMS_DELETE, factory, t)

	var args []string = []string{"streams", "delete", "--name", "mystream"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Nil(t, err)
}

func TestStreamsDeleteWithoutNameFlagReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_STREAMS_DELETE, factory, t)

	var args []string = []string{"streams", "delete"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.NotNil(t, err)

	checkOutput("", "Error: required flag(s) \"name\" not set", factory, t)

	assert.NotNil(t, err)
}
