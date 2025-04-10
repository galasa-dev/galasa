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

func TestCommandListContainsMonitorsSetCommand(t *testing.T) {
    /// Given...
    factory := utils.NewMockFactory()
    commands, _ := NewCommandCollection(factory)

    // When...
    monitorsCommand, err := commands.GetCommand(COMMAND_NAME_MONITORS_SET)
    assert.Nil(t, err)

    // Then...
    assert.NotNil(t, monitorsCommand)
    assert.Equal(t, COMMAND_NAME_MONITORS_SET, monitorsCommand.Name())
    assert.NotNil(t, monitorsCommand.Values())
	assert.IsType(t, &MonitorsSetCmdValues{}, monitorsCommand.Values())
}

func TestMonitorsSetHelpFlagSetCorrectly(t *testing.T) {
    // Given...
    factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_MONITORS_SET, factory, t)

    var args []string = []string{"monitors", "set", "--help"}

    // When...
    err := commandCollection.Execute(args)

    // Then...
    checkOutput("Updates a monitor with the given name in the Galasa service", "", factory, t)

    assert.Nil(t, err)
}

func TestMonitorsSetWithNameAndIsEnabledFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_MONITORS_SET, factory, t)

	var args []string = []string{"monitors", "set", "--name", "myMonitor", "--is-enabled", "true"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)
}

func TestMonitorsSetNoFlagsReturnsErrorMessage(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_MONITORS_SET, factory, t)

	var args []string = []string{"monitors", "set"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.NotNil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", `Error: required flag(s) "name" not set`, factory, t)
}

func TestMonitorsSetWithNameFlagOnlyReturnsErrorMessage(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_MONITORS_SET, factory, t)

	var args []string = []string{"monitors", "set", "--name", "myCustomMonitor"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.NotNil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", `Error: at least one of the flags in the group [is-enabled] is required`, factory, t)
}

