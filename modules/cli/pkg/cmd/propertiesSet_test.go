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

func TestPropertiesSetCommandInCommandCollection(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	propertiesSetCommand, err := commands.GetCommand(COMMAND_NAME_PROPERTIES_SET)
	assert.Nil(t, err)

	assert.Equal(t, COMMAND_NAME_PROPERTIES_SET, propertiesSetCommand.Name())
	assert.NotNil(t, propertiesSetCommand.Values())
	assert.IsType(t, &PropertiesSetCmdValues{}, propertiesSetCommand.Values())
	assert.NotNil(t, propertiesSetCommand.CobraCommand())
}

func TestPropertiesSetHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"properties", "set", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...

	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'properties set' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestPropertiesSetNoFlagsReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"properties", "set"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "required flag(s) \"name\", \"namespace\", \"value\" not set")

	// Check what the user saw is reasonable.
	checkOutput("", "Error: required flag(s) \"name\", \"namespace\", \"value\" not set", factory, t)
}

func TestPropertiesSetNameNamespaceValueReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_PROPERTIES_SET, factory, t)

	var args []string = []string{"properties", "set", "--namespace", "mince", "--name", "pies.are.so.tasty", "--value", "some kinda value"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	checkOutput("", "", factory, t)

	parentCmd, err := commandCollection.GetCommand(COMMAND_NAME_PROPERTIES)
	assert.Nil(t, err)
	assert.Contains(t, parentCmd.Values().(*PropertiesCmdValues).namespace, "mince")
	assert.Contains(t, parentCmd.Values().(*PropertiesCmdValues).propertyName, "pies.are.so.tasty")
	assert.Contains(t, cmd.Values().(*PropertiesSetCmdValues).propertyValue, "some kinda value")
}

func TestPropertiesSetNamespaceOnlyReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"properties", "set", "--namespace", "sunshine"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("", "Error: required flag(s) \"name\", \"value\" not set", factory, t)

	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "required flag(s) \"name\", \"value\" not set")
}

func TestPropertiesSetOnlyNameReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"properties", "set", "--name", "call.me.little.sunshine"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("", "Error: required flag(s) \"namespace\", \"value\" not set", factory, t)

	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "required flag(s) \"namespace\", \"value\" not set")
}

func TestPropertiesOnlyValueReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"properties", "set", "--value", "ghost"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("", "Error: required flag(s) \"name\", \"namespace\" not set", factory, t)

	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "required flag(s) \"name\", \"namespace\" not set")
}
