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

func TestResourcesCommandInCommandCollection(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	resourcesCommand, err := commands.GetCommand(COMMAND_NAME_RESOURCES)
	assert.Nil(t, err)

	assert.NotNil(t, resourcesCommand)
	assert.Equal(t, COMMAND_NAME_RESOURCES, resourcesCommand.Name())
	assert.NotNil(t, resourcesCommand.Values())
	assert.IsType(t, &ResourcesCmdValues{}, resourcesCommand.Values())
	assert.NotNil(t, resourcesCommand.CobraCommand())
}

func TestResourcesHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"resources", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'resources' command.", "", factory, t)

	assert.Nil(t, err)
}

func TestResourcesNoCommandsProducesUsageReport(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	var args []string = []string{"resources"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw was reasonable
	checkOutput("Usage:\n  galasactl resources [command]", "", factory, t)
}
