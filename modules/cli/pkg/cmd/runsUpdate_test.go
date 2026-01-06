/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"strings"
	"testing"

	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

func TestRunsUpdateCommandInCommandCollection(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	runsUpdateCommand, err := commands.GetCommand(COMMAND_NAME_RUNS_UPDATE)
	assert.Nil(t, err)

	assert.Equal(t, COMMAND_NAME_RUNS_UPDATE, runsUpdateCommand.Name())
	assert.NotNil(t, runsUpdateCommand.Values())
	assert.IsType(t, &RunsUpdateCmdValues{}, runsUpdateCommand.Values())
	assert.NotNil(t, runsUpdateCommand.CobraCommand())
}

func TestRunsUpdateHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"runs", "update", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'runs update' command.", "", factory, t)
}

func TestRunsUpdateNoFlagsReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"runs", "update"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "required flag(s)")
	assert.Contains(t, err.Error(), "name")

	// Check what the user saw was reasonable
	checkOutput("", "Error:", factory, t)
}

func TestRunsUpdateNameFlagWithAddTagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_UPDATE, factory, t)

	var args []string = []string{"runs", "update", "--name", "U12345", "--add-tags", "tag1,tag2"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Equal(t, "U12345", cmd.Values().(*RunsUpdateCmdValues).runName)
	assert.Contains(t, cmd.Values().(*RunsUpdateCmdValues).addTags, "tag1")
	assert.Contains(t, cmd.Values().(*RunsUpdateCmdValues).addTags, "tag2")
}

func TestRunsUpdateNameFlagWithRemoveTagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_UPDATE, factory, t)

	var args []string = []string{"runs", "update", "--name", "U12345", "--remove-tags", "tag3,tag4"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Equal(t, "U12345", cmd.Values().(*RunsUpdateCmdValues).runName)
	assert.Contains(t, cmd.Values().(*RunsUpdateCmdValues).removeTags, "tag3")
	assert.Contains(t, cmd.Values().(*RunsUpdateCmdValues).removeTags, "tag4")
}

func TestRunsUpdateNameFlagWithBothAddAndRemoveTagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_UPDATE, factory, t)

	var args []string = []string{"runs", "update", "--name", "U12345", "--add-tags", "tag1", "--remove-tags", "tag2"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Equal(t, "U12345", cmd.Values().(*RunsUpdateCmdValues).runName)
	assert.Contains(t, cmd.Values().(*RunsUpdateCmdValues).addTags, "tag1")
	assert.Contains(t, cmd.Values().(*RunsUpdateCmdValues).removeTags, "tag2")
}

func TestRunsUpdateWithMultipleAddTagsFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_UPDATE, factory, t)

	tag1 := "my-test-tag-1"
	tag2 := "my-test-tag-2"

	var args []string = []string{"runs", "update", "--name", "U12345", "--add-tags", tag1, "--add-tags", tag2}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	addTags := cmd.Values().(*RunsUpdateCmdValues).addTags
	assert.Contains(t, addTags, tag1)
	assert.Contains(t, addTags, tag2)
}

func TestRunsUpdateWithCommaSeparatedAddTagsFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_UPDATE, factory, t)

	tag1 := "my-test-tag-1"
	tag2 := "my-test-tag-2"
	tags := []string{tag1, tag2}
	commaSeparatedTags := strings.Join(tags, ",")

	var args []string = []string{"runs", "update", "--name", "U12345", "--add-tags", commaSeparatedTags}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	actualTags := cmd.Values().(*RunsUpdateCmdValues).addTags
	assert.Contains(t, actualTags, tag1)
	assert.Contains(t, actualTags, tag2)
}

func TestRunsUpdateWithMultipleRemoveTagsFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_UPDATE, factory, t)

	tag1 := "my-test-tag-1"
	tag2 := "my-test-tag-2"

	var args []string = []string{"runs", "update", "--name", "U12345", "--remove-tags", tag1, "--remove-tags", tag2}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	removeTags := cmd.Values().(*RunsUpdateCmdValues).removeTags
	assert.Contains(t, removeTags, tag1)
	assert.Contains(t, removeTags, tag2)
}

func TestRunsUpdateWithCommaSeparatedRemoveTagsFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_UPDATE, factory, t)

	tag1 := "my-test-tag-1"
	tag2 := "my-test-tag-2"
	tags := []string{tag1, tag2}
	commaSeparatedTags := strings.Join(tags, ",")

	var args []string = []string{"runs", "update", "--name", "U12345", "--remove-tags", commaSeparatedTags}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	actualTags := cmd.Values().(*RunsUpdateCmdValues).removeTags
	assert.Contains(t, actualTags, tag1)
	assert.Contains(t, actualTags, tag2)
}

func TestRunsUpdateMultipleNameFlagsOverridesToLast(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_UPDATE, factory, t)

	var args []string = []string{"runs", "update", "--name", "C2020", "--name", "C4091", "--add-tags", "tag1"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Equal(t, "C4091", cmd.Values().(*RunsUpdateCmdValues).runName)
}
