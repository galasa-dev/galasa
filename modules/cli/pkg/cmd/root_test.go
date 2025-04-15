/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"errors"
	"testing"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/spf13/cobra"
	"github.com/stretchr/testify/assert"
)

func checkOutput(expectedStdOutput string, expectedStdErr string, factory spi.Factory, t *testing.T) {
	stdOutConsole := factory.GetStdOutConsole().(*utils.MockConsole)
	outText := stdOutConsole.ReadText()
	if expectedStdOutput != "" {
		assert.Contains(t, outText, expectedStdOutput)
	} else {
		assert.Empty(t, outText)
	}

	stdErrConsole := factory.GetStdErrConsole().(*utils.MockConsole)
	errText := stdErrConsole.ReadText()
	if expectedStdErr != "" {
		assert.Contains(t, errText, expectedStdErr)
	} else {
		assert.Empty(t, errText)
	}

	finalWordHandler := factory.GetFinalWordHandler().(*utils.MockFinalWordHandler)
	o := finalWordHandler.ReportedObject
	assert.Nil(t, o)
}

func setupTestCommandCollection(command string, factory spi.Factory, t *testing.T) (CommandCollection, spi.GalasaCommand) {
	commandCollection, err := NewCommandCollection(factory)
	assert.Nil(t, err)

	var cmd spi.GalasaCommand
	cmd, err = commandCollection.GetCommand(command)
	assert.Nil(t, err)
	cmd.CobraCommand().RunE = func(cobraCmd *cobra.Command, args []string) error { return nil }
	return commandCollection, cmd
}

func TestCommandsCollectionHasARootCommand(t *testing.T) {
	factory := utils.NewMockFactory()
	commands, err := NewCommandCollection(factory)
	assert.Nil(t, err)
	rootCommand, err := commands.GetCommand(COMMAND_NAME_ROOT)
	assert.Nil(t, err)
	assert.NotNil(t, rootCommand)
}

func TestRootCommandInCommandCollectionHasAName(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	// When...
	commands, err := NewCommandCollection(factory)
	// Then...
	assert.Nil(t, err)
	var rootCommand spi.GalasaCommand
	rootCommand, err = commands.GetCommand(COMMAND_NAME_ROOT)
	assert.Nil(t, err)

	assert.Equal(t, rootCommand.Name(), COMMAND_NAME_ROOT)
}

func TestRootCommandInCommandCollectionHasACobraCommand(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	// When...
	commands, err := NewCommandCollection(factory)

	// Then...
	assert.Nil(t, err)
	rootCommand := commands.GetRootCommand()

	assert.NotNil(t, rootCommand.CobraCommand())
}

func TestRootCommandInCommandCollectionHasAValuesStructure(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	// When...
	commands, err := NewCommandCollection(factory)
	// Then...
	assert.Nil(t, err)
	rootCommand := commands.GetRootCommand()

	values := rootCommand.Values()
	assert.NotNil(t, values)
	assert.IsType(t, &RootCmdValues{}, values)
}

func TestVersionFromCommandLine(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = make([]string, 0)
	args = append(args, "--version")

	// When...
	err := Execute(factory, args)

	// Then...
	assert.Nil(t, err)

	// Lets check that the version came out.
	checkOutput("galasactl version", "", factory, t)
}

func TestNoParamsFromCommandLine(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = make([]string, 0)
	args = append(args, "")

	// When...
	Execute(factory, args)

	// Then...

	// Check what the user saw is reasonable.
	checkOutput("A tool for controlling Galasa resources", "", factory, t)
}

func TestCanGetNormalExitCodeAndErrorTextFromAnError(t *testing.T) {
	var err = errors.New("my text")
	errorText, exitCode, isStackTraceWanted := extractErrorDetails(err)
	assert.Equal(t, errorText, "my text", "Failed to extract the exit text from an error!")
	assert.Equal(t, 1, exitCode, "Wrong default exit code")
	assert.True(t, isStackTraceWanted, "We want stack trace from non-galasa errors")
}

func TestCanGetNormalExitCodeAndErrorTextFromAGalasaError(t *testing.T) {
	err := galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_SUBMIT_MISSING_ACTION_FLAGS)
	errorText, exitCode, isStackTraceWanted := extractErrorDetails(err)
	assert.Contains(t, errorText, "GAL1009E", "Failed to extract the exit text from a galasa error!")
	assert.Equal(t, 1, exitCode, "Wrong default exit code")
	assert.False(t, isStackTraceWanted, "We don't want stack trace from galasa errors")
}

func TestCanGetTestsFailedExitCodeAndErrorTextFromATestFailedGalasaErrorPointer(t *testing.T) {
	err := galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_TESTS_FAILED, 14)
	errorText, exitCode, isStackTraceWanted := extractErrorDetails(err)
	assert.Contains(t, errorText, "GAL1017E", "Failed to extract the exit text from a galasa error!")
	assert.Equal(t, 2, exitCode, "Wrong default exit code")
	assert.False(t, isStackTraceWanted, "We don't want stack trace from galasa errors")
}

func TestRootHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'galasactl' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestRootNoCommandsReturnsUsageReport(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("Usage:\n  galasactl [command]", "", factory, t)

	assert.Nil(t, err)
}
