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

func TestStreamsSetCommandInCommandCollectionHasName(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	StreamsSetCommand, err := commands.GetCommand(COMMAND_NAME_STREAMS_SET)
	assert.Nil(t, err)

	assert.Equal(t, COMMAND_NAME_STREAMS_SET, StreamsSetCommand.Name())
	assert.NotNil(t, StreamsSetCommand.CobraCommand())
}

func TestStreamsSetHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"streams", "set", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'streams set' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestStreamsSetNameFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_STREAMS_SET, factory, t)

	var args []string = []string{"streams", "set", "--name", "mystream", "--description", "my description", "--maven-repo-url", "https://mymavenrepo"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Nil(t, err)
}

func TestStreamsSetRequiresNameFlag(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_STREAMS_SET, factory, t)

	var args []string = []string{"streams", "set"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "required flag(s) \"name\" not set")
}

func TestStreamsSetRequiresNameAndAtLeastOneOtherFlag(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_STREAMS_SET, factory, t)

	var args []string = []string{"streams", "set", "--name", "mystream"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "at least one of the flags in the group [testcatalog-url maven-repo-url obr] is required")
}
