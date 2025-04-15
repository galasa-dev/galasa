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

func TestRunsPrepareCommandInCommandCollection(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	cmd, err := commands.GetCommand(COMMAND_NAME_RUNS_PREPARE)
	assert.Nil(t, err)

	assert.Equal(t, COMMAND_NAME_RUNS_PREPARE, cmd.Name())
	assert.NotNil(t, cmd.Values())
	assert.IsType(t, &RunsPrepareCmdValues{}, cmd.Values())
	assert.NotNil(t, cmd.CobraCommand())
}

func TestRunsPrepareHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"runs", "prepare", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...

	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'runs prepare' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestRunsPrepareNoParametersReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"runs", "prepare"}

	// When...
	err := Execute(factory, args)

	// Then...

	// Check what the user saw is reasonable.
	checkOutput("", "Error: required flag(s) \"portfolio\" not set", factory, t)

	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "required flag(s) \"portfolio\" not set")
}

func TestRunsPreparePortfolioFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_PREPARE, factory, t)

	var args []string = []string{"runs", "prepare", "--portfolio", "portfolio.file"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsPrepareCmdValues).portfolioFilename, "portfolio.file")
}

func TestRunsPrepareCheckFlagNoParamsReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_RUNS_PREPARE, factory, t)

	var args []string = []string{"runs", "prepare", "--portfolio"}

	// When...
	err := commandCollection.Execute(args)

	// Then...

	// Check what the user saw is reasonable.
	checkOutput("", "Error: flag needs an argument: --portfolio", factory, t)

	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "flag needs an argument: --portfolio")
}

func TestRunsPreparePortfolioAppendFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_PREPARE, factory, t)

	var args []string = []string{"runs", "prepare", "--portfolio", "roo.yaml", "--append"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsPrepareCmdValues).portfolioFilename, "roo.yaml")
	assert.Equal(t, *cmd.Values().(*RunsPrepareCmdValues).prepareAppend, true)
}

func TestRunsPreparePortfolioBundlesFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_PREPARE, factory, t)

	var args []string = []string{"runs", "prepare", "--portfolio", "roo.yaml", "--bundle", "bundle.name"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsPrepareCmdValues).portfolioFilename, "roo.yaml")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Bundles, "bundle.name")
}

func TestRunsPreparePortfolioBundleFlagCommaSeperatedListValuesSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_PREPARE, factory, t)

	var args []string = []string{"runs", "prepare", "--portfolio", "roo.yaml", "--bundle", "comma,seperated,bundles"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsPrepareCmdValues).portfolioFilename, "roo.yaml")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Bundles, "comma")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Bundles, "seperated")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Bundles, "bundles")
}

func TestRunsPreparePortfolioClassFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_PREPARE, factory, t)

	var args []string = []string{"runs", "prepare", "--portfolio", "roo.yaml", "--class", "class.stuff"}

	// When...
	err := commandCollection.Execute(args)

	// Then...

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Nil(t, err)

	assert.Contains(t, cmd.Values().(*RunsPrepareCmdValues).portfolioFilename, "roo.yaml")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Classes, "class.stuff")
}

func TestRunsPreparePortfolioClassFlagCommaSeperatedListValuesSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_PREPARE, factory, t)

	var args []string = []string{"runs", "prepare", "--portfolio", "roo.yaml", "--class", "comma,seperated,classes"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsPrepareCmdValues).portfolioFilename, "roo.yaml")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Classes, "comma")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Classes, "seperated")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Classes, "classes")
}

func TestRunsPreparePortfolioOverrideFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_PREPARE, factory, t)

	var args []string = []string{"runs", "prepare", "--portfolio", "roo.yaml", "--override", "override string one"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsPrepareCmdValues).portfolioFilename, "roo.yaml")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareFlagOverrides, "override string one")
}

func TestRunsPreparePortfolioOverrideFlagCommaSeperatedListValuesSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_PREPARE, factory, t)

	var args []string = []string{"runs", "prepare", "--portfolio", "roo.yaml", "--override", "comma,seperated,overrides"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsPrepareCmdValues).portfolioFilename, "roo.yaml")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareFlagOverrides, "comma")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareFlagOverrides, "seperated")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareFlagOverrides, "overrides")
}

func TestRunsPreparePortfolioPackageFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_PREPARE, factory, t)

	var args []string = []string{"runs", "prepare", "--portfolio", "roo.yaml", "--package", "packagethingy"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsPrepareCmdValues).portfolioFilename, "roo.yaml")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Packages, "packagethingy")
}

func TestRunsPreparePortfolioPackagesFlagCommaSeperatedListValuesSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_PREPARE, factory, t)

	var args []string = []string{"runs", "prepare", "--portfolio", "roo.yaml", "--package", "comma,seperated,packages"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsPrepareCmdValues).portfolioFilename, "roo.yaml")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Packages, "comma")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Packages, "seperated")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Packages, "packages")
}

func TestRunsPreparePortfolioRegexFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_PREPARE, factory, t)

	var args []string = []string{"runs", "prepare", "--portfolio", "roo.yaml", "--regex"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsPrepareCmdValues).portfolioFilename, "roo.yaml")
	assert.Equal(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.RegexSelect, true)
}

func TestRunsPreparePortfolioStreamFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_PREPARE, factory, t)

	var args []string = []string{"runs", "prepare", "--portfolio", "roo.yaml", "--stream", "arlo.stream"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsPrepareCmdValues).portfolioFilename, "roo.yaml")
	assert.Contains(t, cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Stream, "arlo.stream")
}

func TestRunsPreparePortfolioTagFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_PREPARE, factory, t)

	var args []string = []string{"runs", "prepare", "--portfolio", "roo.yaml", "--tag", "tag.stuff"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsPrepareCmdValues).portfolioFilename, "roo.yaml")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Tags, "tag.stuff")
}

func TestRunsPreparePortfolioTagFlagCommaSeperatedListValuesSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_PREPARE, factory, t)

	var args []string = []string{"runs", "prepare", "--portfolio", "roo.yaml", "--tag", "comma,seperated,tags"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsPrepareCmdValues).portfolioFilename, "roo.yaml")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Tags, "comma")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Tags, "seperated")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Tags, "tags")
}

func TestRunsPreparePortfolioTestFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_PREPARE, factory, t)

	var args []string = []string{"runs", "prepare", "--portfolio", "roo.yaml", "--test", "test.stuff"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsPrepareCmdValues).portfolioFilename, "roo.yaml")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Tests, "test.stuff")
}

func TestRunsPreparePortfolioTestFlagCommaSeperatedListValuesSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_PREPARE, factory, t)

	var args []string = []string{"runs", "prepare", "--portfolio", "roo.yaml", "--test", "comma,seperated,tests"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsPrepareCmdValues).portfolioFilename, "roo.yaml")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Tests, "comma")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Tests, "seperated")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Tests, "tests")
}

func TestRunsPrepareAllFlagsReturnOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_PREPARE, factory, t)

	var args []string = []string{"runs", "prepare", "--portfolio", "roo.yaml", "--test", "test,stuff", "--tag", "tag,list",
		"--append", "--bundle", "comma,seperated,bundles", "--class", "class,list", "--override", "list,o,overrides", "--package", "package,list",
		"--regex", "--stream", "stream"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsPrepareCmdValues).portfolioFilename, "roo.yaml")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Tests, "test")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Tests, "stuff")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Tags, "tag")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Tags, "list")
	assert.Equal(t, *cmd.Values().(*RunsPrepareCmdValues).prepareAppend, true)
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Bundles, "comma")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Bundles, "seperated")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Bundles, "bundles")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Classes, "class")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Classes, "list")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareFlagOverrides, "list")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareFlagOverrides, "o")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareFlagOverrides, "overrides")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Packages, "package")
	assert.Contains(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Packages, "list")
	assert.Equal(t, *cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.RegexSelect, true)
	assert.Contains(t, cmd.Values().(*RunsPrepareCmdValues).prepareSelectionFlags.Stream, "stream")
}
