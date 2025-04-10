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

func TestPropertiesNamespaceGetCommandInCommandCollection(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	propertiesNamespaceGetCommand, err := commands.GetCommand(COMMAND_NAME_PROPERTIES_NAMESPACE_GET)
	assert.Nil(t, err)

	assert.Equal(t, COMMAND_NAME_PROPERTIES_NAMESPACE_GET, propertiesNamespaceGetCommand.Name())
	assert.NotNil(t, propertiesNamespaceGetCommand.Values())
	assert.IsType(t, &PropertiesNamespaceGetCmdValues{}, propertiesNamespaceGetCommand.Values())
	assert.NotNil(t, propertiesNamespaceGetCommand.CobraCommand())
}

func TestPropertiesNamespaceGetHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"properties", "namespaces", "get", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...

	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'properties namespaces get' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestPropertiesNamespacesGetReturnsWithoutError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_PROPERTIES_NAMESPACE_GET, factory, t)

	var args []string = []string{"properties", "namespaces", "get"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Nil(t, err)
}

func TestPropertiesNamespacesGetFormatReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_PROPERTIES_NAMESPACE_GET, factory, t)

	var args []string = []string{"properties", "namespaces", "get", "--format", "yaml"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*PropertiesNamespaceGetCmdValues).namespaceOutputFormat, "yaml")
}
