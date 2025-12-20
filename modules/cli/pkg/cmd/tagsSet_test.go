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

func TestTagsSetCommandInCommandCollectionHasName(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	TagsSetCommand, err := commands.GetCommand(COMMAND_NAME_TAGS_SET)
	assert.Nil(t, err)

	assert.Equal(t, COMMAND_NAME_TAGS_SET, TagsSetCommand.Name())
	assert.NotNil(t, TagsSetCommand.CobraCommand())
}

func TestTagsSetHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"tags", "set", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'tags set' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestTagsSetNameFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_TAGS_SET, factory, t)

	var args []string = []string{"tags", "set", "--name", "mytag", "--description", "my description", "--priority", "10"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Nil(t, err)
}

func TestTagsSetRequiresNameFlag(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_TAGS_SET, factory, t)

	var args []string = []string{"tags", "set"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "required flag(s) \"name\" not set")
}

func TestTagsSetRequiresNameAndAtLeastOneOtherFlag(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_TAGS_SET, factory, t)

	var args []string = []string{"tags", "set", "--name", "mytag"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "at least one of the flags in the group [description priority] is required")
}
