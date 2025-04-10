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

func TestRunsDownloadCommandInCommandCollection(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	runsDownloadCommand, err := commands.GetCommand(COMMAND_NAME_RUNS_DOWNLOAD)
	assert.Nil(t, err)

	assert.Equal(t, COMMAND_NAME_RUNS_DOWNLOAD, runsDownloadCommand.Name())
	assert.NotNil(t, runsDownloadCommand.Values())
	assert.IsType(t, &RunsDownloadCmdValues{}, runsDownloadCommand.Values())
	assert.NotNil(t, runsDownloadCommand.CobraCommand())
}

func TestRunsDownloadHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"runs", "download", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'runs download' command.", "", factory, t)
}

func TestRunsDownloadNoFlagsReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"runs", "download"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.NotNil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "Error: required flag(s) \"name\" not set", factory, t)
}

func TestRunsDownloadNameFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_DOWNLOAD, factory, t)

	var args []string = []string{"runs", "download", "--name", "human1"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsDownloadCmdValues).runNameDownload, "human1")
}

func TestRunsDownloadNameNoParameterReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_RUNS_DOWNLOAD, factory, t)

	var args []string = []string{"runs", "download", "--name"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "flag needs an argument: --name")

	// Check what the user saw was reasonable
	checkOutput("", "Error: flag needs an argument: --name", factory, t)
}

func TestRunsDownloadDestinationReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_RUNS_DOWNLOAD, factory, t)

	var args []string = []string{"runs", "download", "--destination", "random/destination"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "required flag(s) \"name\" not set")

	// Check what the user saw was reasonable
	checkOutput("", "Error: required flag(s) \"name\" not set", factory, t)
}

func TestRunsDownloadNameDestinationReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_DOWNLOAD, factory, t)

	var args []string = []string{"runs", "download", "--name", "foundations", "--destination", "of/decay"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsDownloadCmdValues).runNameDownload, "foundations")
	assert.Contains(t, cmd.Values().(*RunsDownloadCmdValues).runDownloadTargetFolder, "of/decay")
}

func TestRunsDownloadNameForceReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_DOWNLOAD, factory, t)

	var args []string = []string{"runs", "download", "--name", "foundations", "--force"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsDownloadCmdValues).runNameDownload, "foundations")
	assert.Equal(t, cmd.Values().(*RunsDownloadCmdValues).runForceDownload, true)
}

func TestRunsDownloadNameDestinationForceReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_DOWNLOAD, factory, t)

	var args []string = []string{"runs", "download", "--name", "foundations", "--destination", "of/decay", "--force"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsDownloadCmdValues).runNameDownload, "foundations")
	assert.Contains(t, cmd.Values().(*RunsDownloadCmdValues).runDownloadTargetFolder, "of/decay")
	assert.Equal(t, cmd.Values().(*RunsDownloadCmdValues).runForceDownload, true)
}

func TestRunsDownloadNameTwiceOverridesToLatestValue(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_DOWNLOAD, factory, t)

	var args []string = []string{"runs", "download", "--name", "foundations", "--name", "chemicals"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsDownloadCmdValues).runNameDownload, "chemicals")
}
