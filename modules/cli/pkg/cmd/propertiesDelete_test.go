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

func TestPropertiesDeleteCommandInCommandCollectionHasName(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	propertiesDeleteCommand, err := commands.GetCommand(COMMAND_NAME_PROPERTIES_DELETE)
	assert.Nil(t, err)

	assert.Equal(t, COMMAND_NAME_PROPERTIES_DELETE, propertiesDeleteCommand.Name())
	assert.Nil(t, propertiesDeleteCommand.Values())
	assert.NotNil(t, propertiesDeleteCommand.CobraCommand())
}

func TestPropertiesDeleteHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"properties", "delete", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'properties delete' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestPropertiesDeleteNoArgsReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	var args []string = []string{"properties", "delete"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw was reasonable
	checkOutput("", "Error: required flag(s) \"name\", \"namespace\" not set", factory, t)

	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "required flag(s) \"name\", \"namespace\" not set")
}

func TestPropertiesDeleteWithoutName(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	var args []string = []string{"properties", "delete", "--namespace", "jitters"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Ceck what the user saw was reasonable
	checkOutput("", "Error: required flag(s) \"name\" not set", factory, t)

	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "required flag(s) \"name\" not set")
}

func TestPropertiesDeleteWithoutNamespace(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	var args []string = []string{"properties", "delete", "--name", "jeepers"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw was reasonable
	checkOutput("", "Error: required flag(s) \"namespace\" not set", factory, t)

	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "required flag(s) \"namespace\" not set")
}

func TestPropertiesDeleteWithNameAndNamespace(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_PROPERTIES_DELETE, factory, t)

	var args []string = []string{"properties", "delete", "--namespace", "gyro", "--name", "space.ball"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	checkOutput("", "", factory, t)

	parentCmd, err := commandCollection.GetCommand(COMMAND_NAME_PROPERTIES)
	assert.Nil(t, err)
	assert.Contains(t, parentCmd.Values().(*PropertiesCmdValues).namespace, "gyro")
	assert.Contains(t, parentCmd.Values().(*PropertiesCmdValues).propertyName, "space.ball")
}
