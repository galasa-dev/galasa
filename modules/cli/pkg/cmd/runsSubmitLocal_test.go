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

func TestRunsSubmitLocalCommandInCommandCollection(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	cmd, err := commands.GetCommand(COMMAND_NAME_RUNS_SUBMIT_LOCAL)
	assert.Nil(t, err)

	assert.Equal(t, COMMAND_NAME_RUNS_SUBMIT_LOCAL, cmd.Name())
	assert.NotNil(t, cmd.Values())
	assert.IsType(t, &RunsSubmitLocalCmdValues{}, cmd.Values())
	assert.NotNil(t, cmd.CobraCommand())
}

func TestRunsSubmitLocalHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"runs", "submit", "local", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...

	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'runs submit local' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestRunsSubmitLocalWithoutObrWithClassErrors(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	var args []string = []string{"runs", "submit", "local", "--class", "osgi.bundle/class.path"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw was reasonable
	checkOutput("", "if any flags in the group [class obr] are set they must all be set; missing [obr]", factory, t)

	// Should throw an error asking for flags to be set
	assert.NotNil(t, err, "err should have been set!")
	assert.Contains(t, err.Error(), "if any flags in the group [class obr] are set they must all be set; missing [obr]")
}

func TestRunsSubmitLocalWithoutClassWithObrErrors(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	var args []string = []string{"runs", "submit", "local", "--obr", "mvn:second.breakfast/elevenses/0.1.0/brunch"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw was reasonable
	checkOutput("", "if any flags in the group [class obr] are set they must all be set; missing [class]", factory, t)

	// Should throw an error asking for flags to be set
	assert.NotNil(t, err, "err should have been set!")
	assert.Contains(t, err.Error(), "if any flags in the group [class obr] are set they must all be set; missing [class]")
}

func TestMultipleRequiredFlagsNotSetReturnsListInError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	var args []string = []string{"runs", "submit", "local"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw was reasonable
	checkOutput("", "at least one of the flags in the group [class gherkin] is required", factory, t)

	// Should throw an error asking for flags to be set
	assert.NotNil(t, err, "err should have been set!")
	assert.Contains(t, err.Error(), "at least one of the flags in the group [class gherkin] is required")
}

func TestRunsSubmitLocalClassObrFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT_LOCAL, factory, t)

	var args []string = []string{"runs", "submit", "local", "--class", "my.class", "--obr", "mvn:a.big.ol.obr"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, *cmd.Values().(*RunsSubmitLocalCmdValues).submitLocalSelectionFlags.Classes, "my.class")
	assert.Contains(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.Obrs, "mvn:a.big.ol.obr")
}

func TestRunsSubmitLocalDebugFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT_LOCAL, factory, t)

	var args []string = []string{"runs", "submit", "local", "--class", "my.class", "--obr", "mvn:a.big.ol.obr", "--debug"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, *cmd.Values().(*RunsSubmitLocalCmdValues).submitLocalSelectionFlags.Classes, "my.class")
	assert.Contains(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.Obrs, "mvn:a.big.ol.obr")
	assert.Equal(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.IsDebugEnabled, true)
}

func TestRunsSubmitLocalDebugModeFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT_LOCAL, factory, t)

	var args []string = []string{"runs", "submit", "local", "--class", "my.class", "--obr", "mvn:a.big.ol.obr", "--debugMode", "slow"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, *cmd.Values().(*RunsSubmitLocalCmdValues).submitLocalSelectionFlags.Classes, "my.class")
	assert.Contains(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.Obrs, "mvn:a.big.ol.obr")
	assert.Contains(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.DebugMode, "slow")
}

func TestRunsSubmitLocalDebugPortFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT_LOCAL, factory, t)

	var args []string = []string{"runs", "submit", "local", "--class", "my.class", "--obr", "mvn:a.big.ol.obr", "--debugPort", "5000"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, *cmd.Values().(*RunsSubmitLocalCmdValues).submitLocalSelectionFlags.Classes, "my.class")
	assert.Contains(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.Obrs, "mvn:a.big.ol.obr")
	assert.Equal(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.DebugPort, uint32(5000))
}

func TestRunsSubmitLocalGalasaVersionFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT_LOCAL, factory, t)

	var args []string = []string{"runs", "submit", "local", "--class", "my.class", "--obr", "mvn:a.big.ol.obr", "--galasaVersion", "0.1.0"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, *cmd.Values().(*RunsSubmitLocalCmdValues).submitLocalSelectionFlags.Classes, "my.class")
	assert.Contains(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.Obrs, "mvn:a.big.ol.obr")
	assert.Contains(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.TargetGalasaVersion, "0.1.0")
}

func TestRunsSubmitLocalLocalMavenFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT_LOCAL, factory, t)

	var args []string = []string{"runs", "submit", "local", "--class", "my.class", "--obr", "mvn:a.big.ol.obr", "--localMaven", "maven/repo/location"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, *cmd.Values().(*RunsSubmitLocalCmdValues).submitLocalSelectionFlags.Classes, "my.class")
	assert.Contains(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.Obrs, "mvn:a.big.ol.obr")
	assert.Contains(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.LocalMaven, "maven/repo/location")
}

func TestRunsSubmitLocalRemoteMavenFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT_LOCAL, factory, t)

	var args []string = []string{"runs", "submit", "local", "--class", "my.class", "--obr", "mvn:a.big.ol.obr", "--remoteMaven", "remote.maven.location"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, *cmd.Values().(*RunsSubmitLocalCmdValues).submitLocalSelectionFlags.Classes, "my.class")
	assert.Contains(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.Obrs, "mvn:a.big.ol.obr")
	assert.Contains(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.RemoteMaven, "remote.maven.location")
}

func TestRunsSubmitLocalAllFlagsWorkTogether(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT_LOCAL, factory, t)

	var args []string = []string{"runs", "submit", "local",
		"--class", "my.class",
		"--obr", "mvn:a.big.ol.obr",
		"--galasaVersion", "0.1.0",
		"--debug",
		"--debugMode", "thorough",
		"--debugPort", "515",
		"--localMaven", "local/maven/location",
		"--remoteMaven", "remote.maven.location"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, *cmd.Values().(*RunsSubmitLocalCmdValues).submitLocalSelectionFlags.Classes, "my.class")
	assert.Contains(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.Obrs, "mvn:a.big.ol.obr")
	assert.Contains(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.TargetGalasaVersion, "0.1.0")
	assert.Equal(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.IsDebugEnabled, true)
	assert.Contains(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.DebugMode, "thorough")
	assert.Equal(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.DebugPort, uint32(515))
	assert.Contains(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.LocalMaven, "local/maven/location")
	assert.Contains(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.RemoteMaven, "remote.maven.location")
}

func TestRunsSubmitLocaGherkinFlagsWork(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT_LOCAL, factory, t)

	var args []string = []string{"runs", "submit", "local",
		"--gherkin", "gherkin.feature",
		"--galasaVersion", "0.1.0",
		"--debug",
		"--debugMode", "thorough",
		"--debugPort", "515",
		"--localMaven", "local/maven/location",
		"--remoteMaven", "remote.maven.location"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, *cmd.Values().(*RunsSubmitLocalCmdValues).submitLocalSelectionFlags.GherkinUrl, "gherkin.feature")
	assert.Contains(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.TargetGalasaVersion, "0.1.0")
	assert.Equal(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.IsDebugEnabled, true)
	assert.Contains(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.DebugMode, "thorough")
	assert.Equal(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.DebugPort, uint32(515))
	assert.Contains(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.LocalMaven, "local/maven/location")
	assert.Contains(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.RemoteMaven, "remote.maven.location")
	assert.Empty(t, cmd.Values().(*RunsSubmitLocalCmdValues).runsSubmitLocalCmdParams.Obrs)
	assert.Empty(t, cmd.Values().(*RunsSubmitLocalCmdValues).submitLocalSelectionFlags.Classes)
}
