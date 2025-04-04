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

func TestResourcesCreateCommandInCommandCollection(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	resourcesCreateCommand, err := commands.GetCommand(COMMAND_NAME_RESOURCES_CREATE)
	assert.Nil(t, err)

	assert.NotNil(t, resourcesCreateCommand)
	assert.Equal(t, COMMAND_NAME_RESOURCES_CREATE, resourcesCreateCommand.Name())
	assert.NotNil(t, resourcesCreateCommand.Values())
	assert.IsType(t, &ResourcesCreateCmdValues{}, resourcesCreateCommand.Values())
	assert.NotNil(t, resourcesCreateCommand.CobraCommand())
}

func TestResourcesCreateHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"resources", "create", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...

	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'resources create' command", "", factory, t)

	assert.Nil(t, err)
}

func TestResourcesCreateNoFlagsReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"resources", "create"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("", "Error: required flag(s) \"file\" not set", factory, t)

	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "required flag(s) \"file\" not set")
}

func TestResourcesCreateFileFlagReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_RESOURCES_CREATE, factory, t)

	var args []string = []string{"resources", "create", "--file", "mince.yaml"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	checkOutput("", "", factory, t)

	parentCmd, err := commandCollection.GetCommand(COMMAND_NAME_RESOURCES)
	assert.Nil(t, err)
	assert.Contains(t, parentCmd.Values().(*ResourcesCmdValues).filePath, "mince.yaml")
}
