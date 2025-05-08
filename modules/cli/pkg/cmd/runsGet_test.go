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

func TestRunsGetCommandInCommandCollection(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	runsGetCommand, err := commands.GetCommand(COMMAND_NAME_RUNS_GET)
	assert.Nil(t, err)

	assert.Equal(t, COMMAND_NAME_RUNS_GET, runsGetCommand.Name())
	assert.NotNil(t, runsGetCommand.Values())
	assert.IsType(t, &RunsGetCmdValues{}, runsGetCommand.Values())
	assert.NotNil(t, runsGetCommand.CobraCommand())
}

func TestRunsGetHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"runs", "get", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'runs get' command.", "", factory, t)
}

func TestRunsGetNoFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_RUNS_GET, factory, t)

	var args []string = []string{"runs", "get"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)
}

func TestRunsGetActiveFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_GET, factory, t)

	var args []string = []string{"runs", "get", "--active"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Equal(t, cmd.Values().(*RunsGetCmdValues).isActiveRuns, true)
}

func TestRunsGetRequestorFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_GET, factory, t)

	var args []string = []string{"runs", "get", "--requestor", "galasateam"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsGetCmdValues).requestor, "galasateam")
}

func TestRunsGetResultFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_GET, factory, t)

	var args []string = []string{"runs", "get", "--result", "passed"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsGetCmdValues).result, "passed")
}

func TestRunsGetNameFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_GET, factory, t)

	var args []string = []string{"runs", "get", "--name", "gerald"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsGetCmdValues).runName, "gerald")
}

func TestRunsGetGroupFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_GET, factory, t)

	var args []string = []string{"runs", "get", "--group", "someGroup"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsGetCmdValues).group, "someGroup")
}

func TestRunsGetageFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_GET, factory, t)

	var args []string = []string{"runs", "get", "--age", "10h"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsGetCmdValues).age, "10h")
}

func TestRunsGetFormatFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_GET, factory, t)

	var args []string = []string{"runs", "get", "--format", "yaml"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsGetCmdValues).outputFormatString, "yaml")
}

func TestRunsGetMultipleNameOverridesToLast(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_GET, factory, t)

	var args []string = []string{"runs", "get", "--name", "C2020", "--name", "C4091"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsGetCmdValues).runName, "C4091")
}

func TestRunsGetMultipleResultFlagsOverridesToLast(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_GET, factory, t)

	var args []string = []string{"runs", "get", "--result", "passed", "--result", "failed"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsGetCmdValues).result, "failed")
}

func TestRunsGetMultipleRequestorFlagsOverridesToLast(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_GET, factory, t)

	var args []string = []string{"runs", "get", "--requestor", "root", "--requestor", "galasa"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsGetCmdValues).requestor, "galasa")
}

func TestRunsGetNameRequestorMutuallyExclusive(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"runs", "get", "--name", "Miller", "--requestor", "root"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "if any flags in the group [name requestor] are set none of the others can be; [name requestor] were all set")

	// Check what the user saw is reasonable.
	checkOutput("", "Error: if any flags in the group [name requestor] are set none of the others can be; [name requestor] were all set", factory, t)
}

func TestRunsGetNameResultMutuallyExclusive(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"runs", "get", "--name", "Miller", "--result", "passed"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "if any flags in the group [name result] are set none of the others can be; [name result] were all set")

	// Check what the user saw is reasonable.
	checkOutput("", "Error: if any flags in the group [name result] are set none of the others can be; [name result] were all set", factory, t)
}

func TestRunsGetNameActiveMutuallyExclusive(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"runs", "get", "--name", "Miller", "--active"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "if any flags in the group [name active] are set none of the others can be; [active name] were all set")

	// Check what the user saw is reasonable.
	checkOutput("", "Error: if any flags in the group [name active] are set none of the others can be; [active name] were all set", factory, t)
}

func TestRunsGetResultActiveMutuallyExclusive(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"runs", "get", "--result", "failed", "--active"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "if any flags in the group [result active] are set none of the others can be; [active result] were all set")

	// Check what the user saw is reasonable.
	checkOutput("", "Error: if any flags in the group [result active] are set none of the others can be; [active result] were all set", factory, t)
}

func TestRunsGetGroupRunNameMutuallyExclusive(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"runs", "get", "--group", "group-1", "--name", "CV123"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "if any flags in the group [group name] are set none of the others can be; [group name] were all set")

	// Check what the user saw is reasonable.
	checkOutput("", "Error: if any flags in the group [group name] are set none of the others can be; [group name] were all set", factory, t)
}

func TestRunsGetWithOneTagsFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_GET, factory, t)

	tag := "my-test-tag"

	var args []string = []string{"runs", "get", "--age", "1d", "--tags", tag}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*RunsGetCmdValues).tags, tag)
}

func TestRunsGetWithMultipleTagsFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_GET, factory, t)

	tag1 := "my-test-tag-1"
	tag2 := "my-test-tag-2"

	var args []string = []string{"runs", "get", "--age", "1d", "--tags", tag1, "--tags", tag2}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	tags := cmd.Values().(*RunsGetCmdValues).tags
	assert.Contains(t, tags, tag1)
	assert.Contains(t, tags, tag2)
}

func TestRunsGetWithCommaSeparatedTagsFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_RUNS_GET, factory, t)

	tag1 := "my-test-tag-1"
	tag2 := "my-test-tag-2"
	tags := []string{ tag1, tag2 }
	commaSeparatedTags := strings.Join(tags, ",")

	var args []string = []string{"runs", "get", "--age", "1d", "--tags", commaSeparatedTags}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	actualTags := cmd.Values().(*RunsGetCmdValues).tags
	assert.Contains(t, actualTags, tag1)
	assert.Contains(t, actualTags, tag2)
}
