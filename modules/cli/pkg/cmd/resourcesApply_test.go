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

func TestResourcesApplyCommandInCommandCollection(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	resourcesApplyCommand, err := commands.GetCommand(COMMAND_NAME_RESOURCES_APPLY)
	assert.Nil(t, err)

	assert.NotNil(t, resourcesApplyCommand)
	assert.Equal(t, COMMAND_NAME_RESOURCES_APPLY, resourcesApplyCommand.Name())
	assert.NotNil(t, resourcesApplyCommand.Values())
	assert.IsType(t, &ResourcesApplyCmdValues{}, resourcesApplyCommand.Values())
	assert.NotNil(t, resourcesApplyCommand.CobraCommand())
}

func TestResourcesApplyHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"resources", "apply", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'resources apply' command", "", factory, t)

	assert.Nil(t, err)
}

func TestResourcesApplyNoFlagsReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"resources", "apply"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("", "Error: required flag(s) \"file\" not set", factory, t)

	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "required flag(s) \"file\" not set")
}

func TestResourcesApplyFileFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_RESOURCES_APPLY, factory, t)

	var args []string = []string{"resources", "apply", "--file", "mince.yaml"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	checkOutput("", "", factory, t)

	parentCmd, err := commandCollection.GetCommand(COMMAND_NAME_RESOURCES)
	assert.Nil(t, err)
	assert.Contains(t, parentCmd.Values().(*ResourcesCmdValues).filePath, "mince.yaml")
}
