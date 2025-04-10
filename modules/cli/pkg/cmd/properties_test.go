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

func TestPropertiesCommandInCommandCollection(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	propertiesCommand, err := commands.GetCommand(COMMAND_NAME_PROPERTIES)
	assert.Nil(t, err)

	assert.NotNil(t, propertiesCommand)
	assert.Equal(t, COMMAND_NAME_PROPERTIES, propertiesCommand.Name())
	assert.NotNil(t, propertiesCommand.Values())
	assert.IsType(t, &PropertiesCmdValues{}, propertiesCommand.Values())
	assert.NotNil(t, propertiesCommand.CobraCommand())
}

func TestPropertiesHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"properties", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...

	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'properties' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestPropertiesNoCommandsProducesUsageReport(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	var args []string = []string{"properties"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.Nil(t, err)
	// Check what the user saw was reasonable
	checkOutput("Usage:\n  galasactl properties [command]", "", factory, t)
}
