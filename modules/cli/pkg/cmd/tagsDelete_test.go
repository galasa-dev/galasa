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

func TestTagsDeleteCommandInCommandCollectionHasName(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	TagsDeleteCommand, err := commands.GetCommand(COMMAND_NAME_TAGS_DELETE)
	assert.Nil(t, err)

	assert.Equal(t, COMMAND_NAME_TAGS_DELETE, TagsDeleteCommand.Name())
	assert.NotNil(t, TagsDeleteCommand.CobraCommand())
}

func TestTagsDeleteHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"tags", "delete", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'tags delete' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestTagsDeleteNamespaceNameFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_TAGS_DELETE, factory, t)

	var args []string = []string{"tags", "delete", "--name", "mytag"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Nil(t, err)
}

func TestTagsDeleteNamespaceRequiresNameFlag(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_TAGS_DELETE, factory, t)

	var args []string = []string{"tags", "delete"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "required flag(s) \"name\" not set")
}
