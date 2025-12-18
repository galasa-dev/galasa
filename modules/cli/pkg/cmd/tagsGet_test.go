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

func TestTagsGetCommandInCommandCollectionHasName(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	TagsGetCommand, err := commands.GetCommand(COMMAND_NAME_TAGS_GET)
	assert.Nil(t, err)

	assert.Equal(t, COMMAND_NAME_TAGS_GET, TagsGetCommand.Name())
	assert.NotNil(t, TagsGetCommand.CobraCommand())
}

func TestTagsGetHelpFlagGetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"tags", "get", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'tags get' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestTagsGetNameFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_TAGS_GET, factory, t)

	var args []string = []string{"tags", "get", "--name", "mytag"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Nil(t, err)
}

func TestTagsGetAcceptsNoNameFlag(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_TAGS_GET, factory, t)

	var args []string = []string{"tags", "get"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Nil(t, err)
}

