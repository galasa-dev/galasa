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

func TestTagsCommandInCommandCollection(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	tagsCommand, err := commands.GetCommand(COMMAND_NAME_TAGS)
	assert.Nil(t, err)

	assert.NotNil(t, tagsCommand)
	assert.Equal(t, COMMAND_NAME_TAGS, tagsCommand.Name())
	assert.NotNil(t, tagsCommand.Values())
	assert.IsType(t, &TagsCmdValues{}, tagsCommand.Values())
	assert.NotNil(t, tagsCommand.CobraCommand())
}

func TestTagsHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"tags", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...

	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'tags' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestTagsNoCommandsProducesUsageReport(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	var args []string = []string{"tags"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.Nil(t, err)
	// Check what the user saw was reasonable
	checkOutput("Usage:\n  galasactl tags [command]", "", factory, t)
}
