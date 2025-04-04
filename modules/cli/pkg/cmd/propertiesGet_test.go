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

func TestPropertiesGetCommandInCommandCollectionHasName(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	propertiesGetCommand, err := commands.GetCommand(COMMAND_NAME_PROPERTIES_GET)
	assert.Nil(t, err)

	assert.Equal(t, COMMAND_NAME_PROPERTIES_GET, propertiesGetCommand.Name())
	assert.NotNil(t, propertiesGetCommand.Values())
	assert.IsType(t, &PropertiesGetCmdValues{}, propertiesGetCommand.Values())
	assert.NotNil(t, propertiesGetCommand.CobraCommand())
}

func TestPropertiesGetHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"properties", "get", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'properties get' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestPropertiesGetNoArgsReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	var args []string = []string{"properties", "get"}
	// When...
	err := Execute(factory, args)

	// Then...
	checkOutput("", "Error: required flag(s) \"namespace\" not set", factory, t)

	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "required flag(s) \"namespace\" not set")
}

func TestPropertiesGetNamespaceNameFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_PROPERTIES_GET, factory, t)

	var args []string = []string{"properties", "get", "--namespace", "mince", "--name", "pies.are.so.tasty"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("", "", factory, t)

	parentCmd, err := commandCollection.GetCommand(COMMAND_NAME_PROPERTIES)
	assert.Nil(t, err)
	assert.Contains(t, parentCmd.Values().(*PropertiesCmdValues).namespace, "mince")
	assert.Contains(t, parentCmd.Values().(*PropertiesCmdValues).propertyName, "pies.are.so.tasty")
}

func TestPropertiesGetNamespaceFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_PROPERTIES_GET, factory, t)

	var args []string = []string{"properties", "get", "--namespace", "mince"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	checkOutput("", "", factory, t)

	parentCmd, err := commandCollection.GetCommand(COMMAND_NAME_PROPERTIES)
	assert.Nil(t, err)
	assert.Contains(t, parentCmd.Values().(*PropertiesCmdValues).namespace, "mince")
}

func TestPropertiesGetNamespaceNamePrefixFlagsReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_PROPERTIES_GET, factory, t)

	var args []string = []string{"properties", "get", "--namespace", "mince", "--name", "pies.are.so.tasty", "--prefix", "something"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	checkOutput("", "Error: if any flags in the group [name prefix] are set", factory, t)

	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "if any flags in the group [name prefix] are set")
}

func TestPropertiesGetNamespaceNameSuffixFlagsReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_PROPERTIES_GET, factory, t)

	var args []string = []string{"properties", "get", "--namespace", "mince", "--name", "pies.are.so.tasty", "--suffix", "something"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	checkOutput("", "Error: if any flags in the group [name suffix] are set", factory, t)

	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "if any flags in the group [name suffix] are set")
}

func TestPropertiesGetNamespaceNameInfixFlagsReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_PROPERTIES_GET, factory, t)

	var args []string = []string{"properties", "get", "--namespace", "mince", "--name", "pies.are.so.tasty", "--infix", "something"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	checkOutput("", "Error: if any flags in the group [name infix] are set", factory, t)

	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "if any flags in the group [name infix] are set")
}

func TestPropertiesGetNamespacePrefixFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_PROPERTIES_GET, factory, t)

	var args []string = []string{"properties", "get", "--namespace", "mince", "--prefix", "something"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	checkOutput("", "", factory, t)

	parentCmd, err := commandCollection.GetCommand(COMMAND_NAME_PROPERTIES)
	assert.Nil(t, err)
	assert.Contains(t, parentCmd.Values().(*PropertiesCmdValues).namespace, "mince")
	assert.Contains(t, cmd.Values().(*PropertiesGetCmdValues).propertiesPrefix, "something")
}

func TestPropertiesGetNamespaceSufffixFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_PROPERTIES_GET, factory, t)

	var args []string = []string{"properties", "get", "--namespace", "mince", "--suffix", "something"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	checkOutput("", "", factory, t)

	parentCmd, err := commandCollection.GetCommand(COMMAND_NAME_PROPERTIES)
	assert.Nil(t, err)
	assert.Contains(t, parentCmd.Values().(*PropertiesCmdValues).namespace, "mince")
	assert.Contains(t, cmd.Values().(*PropertiesGetCmdValues).propertiesSuffix, "something")
}

func TestPropertiesGetNamespaceInfixFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_PROPERTIES_GET, factory, t)

	var args []string = []string{"properties", "get", "--namespace", "mince", "--infix", "something"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	checkOutput("", "", factory, t)

	parentCmd, err := commandCollection.GetCommand(COMMAND_NAME_PROPERTIES)
	assert.Nil(t, err)
	assert.Contains(t, parentCmd.Values().(*PropertiesCmdValues).namespace, "mince")
	assert.Contains(t, cmd.Values().(*PropertiesGetCmdValues).propertiesInfix, "something")
}

func TestPropertiesGetNamespacePrefixSuffixInfixFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_PROPERTIES_GET, factory, t)

	var args []string = []string{"properties", "get", "--namespace", "mince", "--prefix", "something", "--suffix", "suffixthingy", "--infix", "infixthingy"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check if what the user saw was reasonable
	checkOutput("", "", factory, t)

	parentCmd, err := commandCollection.GetCommand(COMMAND_NAME_PROPERTIES)
	assert.Nil(t, err)
	assert.Contains(t, parentCmd.Values().(*PropertiesCmdValues).namespace, "mince")
	assert.Contains(t, cmd.Values().(*PropertiesGetCmdValues).propertiesPrefix, "something")
	assert.Contains(t, cmd.Values().(*PropertiesGetCmdValues).propertiesSuffix, "suffixthingy")
	assert.Contains(t, cmd.Values().(*PropertiesGetCmdValues).propertiesInfix, "infixthingy")
}

func TestPropertiesGetNameAndPrefixSuffixInfixMutuallyExclusive(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_PROPERTIES_GET, factory, t)

	var args []string = []string{"properties", "get", "--namespace", "mince", "--name", "pies.are.so.tasty", "--prefix", "something", "--suffix", "suffixthingy", "--infix", "infixthingy"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "if any flags in the group [name infix] are set")

	// Check if what the user saw was reasonable
	checkOutput("", "if any flags in the group [name infix] are set", factory, t)
}

func TestPropertiesGetNamespaceNoParameterReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_PROPERTIES_GET, factory, t)

	var args []string = []string{"properties", "get", "--namespace"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "flag needs an argument: --namespace")

	// Check if what the user saw was reasonable
	checkOutput("", "Error: flag needs an argument: --namespace", factory, t)
}

func TestPropertiesGetNamespaceSuffixNoParameterReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_PROPERTIES_GET, factory, t)

	var args []string = []string{"properties", "get", "--namespace", "guitar", "--suffix"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "flag needs an argument: --suffix")

	// Check if what the user saw was reasonable
	checkOutput("", "Error: flag needs an argument: --suffix", factory, t)
}

func TestPropertiesGetNamespaceRepeatedOverridesToLast(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_PROPERTIES_GET, factory, t)

	var args []string = []string{"properties", "get", "--namespace", "wildwest", "--namespace", "whistle"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check if what the user saw was reasonable
	checkOutput("", "", factory, t)

	parentCmd, err := commandCollection.GetCommand(COMMAND_NAME_PROPERTIES)
	assert.Nil(t, err)
	assert.Contains(t, parentCmd.Values().(*PropertiesCmdValues).namespace, "whistle")
}
