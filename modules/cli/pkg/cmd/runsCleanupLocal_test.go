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

func TestRunsCleanupLocalCommandInCommandCollection(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	cmd, err := commands.GetCommand(COMMAND_NAME_RUNS_CLEANUP_LOCAL)
	assert.Nil(t, err)

	assert.Equal(t, COMMAND_NAME_RUNS_CLEANUP_LOCAL, cmd.Name())
	assert.NotNil(t, cmd.Values())
	assert.IsType(t, &RunsCleanupLocalCmdValues{}, cmd.Values())
	assert.NotNil(t, cmd.CobraCommand())
}

func TestRunsCleanupLocalHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"runs", "cleanup", "local", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...

	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'runs cleanup local' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestRunsCleanupLocalObrFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_CLEANUP_LOCAL, factory, t)

	var args []string = []string{"runs", "cleanup", "local", "--obr", "mvn:a.big.ol.obr"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsCleanupLocalCmdValues).runsCleanupLocalCmdParams.Obrs, "mvn:a.big.ol.obr")
}

func TestRunsCleanupLocalMultipleObrAndPatternFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_CLEANUP_LOCAL, factory, t)

	var args []string = []string{"runs", "cleanup", "local",
		"--obr", "mvn:a.big.ol.obr",
		"--obr", "mvn:another.obr",
		"--includes-pattern", "dev.galasa.*",
		"--includes-pattern", "my.other.bundles.*",
		"--excludes-pattern", "*ExcludeMe",
		"--excludes-pattern", "exclude.these.as.well.*",
	}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsCleanupLocalCmdValues).runsCleanupLocalCmdParams.Obrs, "mvn:a.big.ol.obr")
	assert.Contains(t, cmd.Values().(*RunsCleanupLocalCmdValues).runsCleanupLocalCmdParams.Obrs, "mvn:another.obr")
	assert.Contains(t, cmd.Values().(*RunsCleanupLocalCmdValues).runsCleanupLocalCmdParams.IncludesPatterns, "dev.galasa.*")
	assert.Contains(t, cmd.Values().(*RunsCleanupLocalCmdValues).runsCleanupLocalCmdParams.IncludesPatterns, "my.other.bundles.*")
	assert.Contains(t, cmd.Values().(*RunsCleanupLocalCmdValues).runsCleanupLocalCmdParams.ExcludesPatterns, "*ExcludeMe")
	assert.Contains(t, cmd.Values().(*RunsCleanupLocalCmdValues).runsCleanupLocalCmdParams.ExcludesPatterns, "exclude.these.as.well.*")
}

func TestRunsCleanupLocalLocalMavenFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_CLEANUP_LOCAL, factory, t)

	var args []string = []string{"runs", "cleanup", "local", "--obr", "mvn:a.big.ol.obr", "--localMaven", "maven/repo/location"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsCleanupLocalCmdValues).runsCleanupLocalCmdParams.Obrs, "mvn:a.big.ol.obr")
	assert.Contains(t, cmd.Values().(*RunsCleanupLocalCmdValues).runsCleanupLocalCmdParams.LocalMaven, "maven/repo/location")
}

func TestRunsCleanupLocalRemoteMavenFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_CLEANUP_LOCAL, factory, t)

	var args []string = []string{"runs", "cleanup", "local", "--obr", "mvn:a.big.ol.obr", "--remoteMaven", "remote.maven.location"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsCleanupLocalCmdValues).runsCleanupLocalCmdParams.Obrs, "mvn:a.big.ol.obr")
	assert.Contains(t, cmd.Values().(*RunsCleanupLocalCmdValues).runsCleanupLocalCmdParams.RemoteMavenRepos, "remote.maven.location")
}

func TestRunsCleanupLocalAllFlagsWorkTogether(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_CLEANUP_LOCAL, factory, t)

	var args []string = []string{"runs", "cleanup", "local",
		"--obr", "mvn:a.big.ol.obr",
		"--localMaven", "local/maven/location",
		"--remoteMaven", "remote.maven.location",
		"--includes-pattern", "dev.galasa.*",
		"--excludes-pattern", "*ignoreme",
	}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsCleanupLocalCmdValues).runsCleanupLocalCmdParams.Obrs, "mvn:a.big.ol.obr")
	assert.Contains(t, cmd.Values().(*RunsCleanupLocalCmdValues).runsCleanupLocalCmdParams.LocalMaven, "local/maven/location")
	assert.Contains(t, cmd.Values().(*RunsCleanupLocalCmdValues).runsCleanupLocalCmdParams.RemoteMavenRepos, "remote.maven.location")
	assert.Contains(t, cmd.Values().(*RunsCleanupLocalCmdValues).runsCleanupLocalCmdParams.IncludesPatterns, "dev.galasa.*")
	assert.Contains(t, cmd.Values().(*RunsCleanupLocalCmdValues).runsCleanupLocalCmdParams.ExcludesPatterns, "*ignoreme")
}

