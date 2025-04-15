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

func TestCommandListContainsMonitorsCommand(t *testing.T) {
    /// Given...
    factory := utils.NewMockFactory()
    commands, _ := NewCommandCollection(factory)

    // When...
    monitorsCommand, err := commands.GetCommand(COMMAND_NAME_MONITORS)
    assert.Nil(t, err)

    // Then...
    assert.NotNil(t, monitorsCommand)
    assert.Equal(t, COMMAND_NAME_MONITORS, monitorsCommand.Name())
    assert.NotNil(t, monitorsCommand.Values())
    assert.IsType(t, &MonitorsCmdValues{}, monitorsCommand.Values())
}

func TestMonitorsHelpFlagSetCorrectly(t *testing.T) {
    // Given...
    factory := utils.NewMockFactory()

    var args []string = []string{"monitors", "--help"}

    // When...
    err := Execute(factory, args)

    // Then...
    // Check what the user saw is reasonable.
    checkOutput("The parent command for operations to manipulate monitors in the Galasa service", "", factory, t)

    assert.Nil(t, err)
}

func TestMonitorsNoCommandsProducesUsageReport(t *testing.T) {
    // Given...
    factory := utils.NewMockFactory()
    var args []string = []string{"monitors"}

    // When...
    err := Execute(factory, args)

    // Then...
    assert.Nil(t, err)

    checkOutput("Usage:\n  galasactl monitors [command]", "", factory, t)
}
