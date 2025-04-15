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

func TestRunsSubmitCommandInCommandCollection(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	cmd, err := commands.GetCommand(COMMAND_NAME_RUNS_SUBMIT)
	assert.Nil(t, err)

	assert.Equal(t, COMMAND_NAME_RUNS_SUBMIT, cmd.Name())
	assert.NotNil(t, cmd.Values())
	assert.IsType(t, &utils.RunsSubmitCmdValues{}, cmd.Values())
	assert.NotNil(t, cmd.CobraCommand())
}

func TestRunsSubmitHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"runs", "submit", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'runs submit' command.", "", factory, t)
}

func TestRunsSubmitNoFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)
}

func TestRunsSubmitBundleFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--bundle", "1lilbundle"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Bundles, "1lilbundle")
}

func TestRunsSubmitBundleFlagCSListReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--bundle", "bundle,list,woo"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Bundles, "bundle")
	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Bundles, "woo")
	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Bundles, "list")
}

func TestRunsSubmitClassFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--class", "1lilclass"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Classes, "1lilclass")
}

func TestRunsSubmitClassFlagCSListReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--class", "class,list,woo"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Classes, "class")
	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Classes, "woo")
	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Classes, "list")
}

func TestRunsSubmitGroupFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--group", "group.name"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*utils.RunsSubmitCmdValues).GroupName, "group.name")
}

func TestRunsSubmitNoexitcodeontestfailuresFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--noexitcodeontestfailures"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Equal(t, cmd.Values().(*utils.RunsSubmitCmdValues).NoExitCodeOnTestFailures, true)
}

func TestRunsSubmitOverrideFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--override", "1liloverride"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*utils.RunsSubmitCmdValues).Overrides, "1liloverride")
}

func TestRunsSubmitOverrideFlagCSListReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--override", "override,list,woo"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*utils.RunsSubmitCmdValues).Overrides, "override")
	assert.Contains(t, cmd.Values().(*utils.RunsSubmitCmdValues).Overrides, "woo")
	assert.Contains(t, cmd.Values().(*utils.RunsSubmitCmdValues).Overrides, "list")
}

func TestRunsSubmitOverridefileFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--overridefile", "filepathtotheoverrides"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*utils.RunsSubmitCmdValues).OverrideFilePaths, "filepathtotheoverrides")
}

func TestRunsSubmitPackageFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--package", "1lilpackage"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Packages, "1lilpackage")
}

func TestRunsSubmitPackageFlagCSListReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--package", "package,list,woo"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Packages, "package")
	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Packages, "woo")
	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Packages, "list")
}

func TestRunsSubmitTagFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--tag", "1liltag"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Tags, "1liltag")
}

func TestRunsSubmitTagFlagCSListReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--tag", "tag,list,woo"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Tags, "tag")
	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Tags, "woo")
	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Tags, "list")
}

func TestRunsSubmitTestFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--test", "1liltest"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Tests, "1liltest")
}

func TestRunsSubmitTestFlagCSListReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--test", "test,list,woo"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Tests, "test")
	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Tests, "woo")
	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Tests, "list")
}

func TestRunsSubmitPollFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--poll", "10"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Equal(t, cmd.Values().(*utils.RunsSubmitCmdValues).PollIntervalSeconds, 10)
}

func TestRunsSubmitPollStringParamFlagReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--poll", "badstringinput"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "invalid argument \"badstringinput\" for \"--poll\" flag: strconv.ParseInt: parsing \"badstringinput\": invalid syntax")

	// Check what the user saw is reasonable.
	checkOutput("", "Error: invalid argument \"badstringinput\" for \"--poll\" flag: strconv.ParseInt: parsing \"badstringinput\": invalid syntax", factory, t)

}

func TestRunsSubmitProtfolioFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--portfolio", "yay.portfolio"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*utils.RunsSubmitCmdValues).PortfolioFileName, "yay.portfolio")
}

func TestRunsSubmitRegexFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--regex"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Equal(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.RegexSelect, true)
}

func TestRunsSubmitReportJsonFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--reportjson", "aFile.json"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*utils.RunsSubmitCmdValues).ReportJsonFilename, "aFile.json")
}

func TestRunsSubmitReportjunitFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--reportjunit", "afile.junit"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*utils.RunsSubmitCmdValues).ReportJunitFilename, "afile.junit")
}

func TestRunsSubmitReportyamlFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--reportyaml", "afile.yaml"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*utils.RunsSubmitCmdValues).ReportYamlFilename, "afile.yaml")
}

func TestRunsSubmitRequestTypeFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--requesttype", "nonsense"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*utils.RunsSubmitCmdValues).RequestType, "nonsense")
}

func TestRunsSubmitStreamFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--stream", "streamname"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Stream, "streamname")
}

func TestRunsSubmitThrottleFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--throttle", "1"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Equal(t, cmd.Values().(*utils.RunsSubmitCmdValues).Throttle, 1)
}

func TestRunsSubmitThrottleStringParamFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--throttle", "badparam"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "invalid argument \"badparam\" for \"--throttle\" flag: strconv.ParseInt: parsing \"badparam\": invalid syntax")

	// Check what the user saw is reasonable.
	checkOutput("", "Error: invalid argument \"badparam\" for \"--throttle\" flag: strconv.ParseInt: parsing \"badparam\": invalid syntax", factory, t)
}

func TestRunsSubmitThrottleFileFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--throttlefile", "filepathnamething"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*utils.RunsSubmitCmdValues).ThrottleFileName, "filepathnamething")
}

func TestRunsSubmitTraceFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit", "--trace"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Equal(t, cmd.Values().(*utils.RunsSubmitCmdValues).Trace, true)
}

func TestRunsSubmitAllFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_SUBMIT, factory, t)

	var args []string = []string{"runs", "submit",
		"--bundle", "bundleParam",
		"--class", "classParam",
		"--package", "packageParam",
		"--tag", "tagParam",
		"--test", "testParam",
		"--overridefile", "override/path",
		"--portfolio", "portfolio.file",
		"--throttlefile", "throttle.file",
		"--reportjson", "file.json",
		"--reportjunit", "file.junit",
		"--reportyaml", "file.yaml",
		"--override", "overrideParam",
		"--group", "namedegroup",
		"--requesttype", "hiddenfromsight",
		"--stream", "mambono5",
		"--poll", "5",
		"--throttle", "6",
		"--noexitcodeontestfailures",
		"--regex",
		"--trace"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Bundles, "bundleParam")
	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Classes, "classParam")
	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Packages, "packageParam")
	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Tags, "tagParam")
	assert.Contains(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Tests, "testParam")
	assert.Contains(t, cmd.Values().(*utils.RunsSubmitCmdValues).OverrideFilePaths, "override/path")
	assert.Contains(t, cmd.Values().(*utils.RunsSubmitCmdValues).PortfolioFileName, "portfolio.file")
	assert.Contains(t, cmd.Values().(*utils.RunsSubmitCmdValues).ThrottleFileName, "throttle.file")
	assert.Contains(t, cmd.Values().(*utils.RunsSubmitCmdValues).ReportJsonFilename, "file.json")
	assert.Contains(t, cmd.Values().(*utils.RunsSubmitCmdValues).ReportJunitFilename, "file.junit")
	assert.Contains(t, cmd.Values().(*utils.RunsSubmitCmdValues).ReportYamlFilename, "file.yaml")
	assert.Contains(t, cmd.Values().(*utils.RunsSubmitCmdValues).Overrides, "overrideParam")
	assert.Contains(t, cmd.Values().(*utils.RunsSubmitCmdValues).GroupName, "namedegroup")
	assert.Contains(t, cmd.Values().(*utils.RunsSubmitCmdValues).RequestType, "hiddenfromsight")
	assert.Contains(t, cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.Stream, "mambono5")
	assert.Equal(t, cmd.Values().(*utils.RunsSubmitCmdValues).PollIntervalSeconds, 5)
	assert.Equal(t, cmd.Values().(*utils.RunsSubmitCmdValues).Throttle, 6)
	assert.Equal(t, cmd.Values().(*utils.RunsSubmitCmdValues).NoExitCodeOnTestFailures, true)
	assert.Equal(t, *cmd.Values().(*utils.RunsSubmitCmdValues).TestSelectionFlagValues.RegexSelect, true)
	assert.Equal(t, cmd.Values().(*utils.RunsSubmitCmdValues).Trace, true)
}

// Flags
//   --bundle /
//   --class /
//   --group /
//   --noexitcodeontestfailures /
//   --overrides /
//   --overridefile /
