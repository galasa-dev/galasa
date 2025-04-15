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

func TestCommandListContainsMonitorsGetCommand(t *testing.T) {
    /// Given...
    factory := utils.NewMockFactory()
    commands, _ := NewCommandCollection(factory)

    // When...
    monitorsCommand, err := commands.GetCommand(COMMAND_NAME_MONITORS_GET)
    assert.Nil(t, err)

    // Then...
    assert.NotNil(t, monitorsCommand)
    assert.Equal(t, COMMAND_NAME_MONITORS_GET, monitorsCommand.Name())
    assert.NotNil(t, monitorsCommand.Values())
	assert.IsType(t, &MonitorsGetCmdValues{}, monitorsCommand.Values())
}

func TestMonitorsGetHelpFlagSetCorrectly(t *testing.T) {
    // Given...
    factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_MONITORS_GET, factory, t)

    var args []string = []string{"monitors", "get", "--help"}

    // When...
    err := commandCollection.Execute(args)

    // Then...
    checkOutput("Get a list of monitors or a specific monitor from the Galasa service", "", factory, t)

    assert.Nil(t, err)
}

func TestMonitorsGetNoFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_MONITORS_GET, factory, t)

	var args []string = []string{"monitors", "get"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "", factory, t)
}

