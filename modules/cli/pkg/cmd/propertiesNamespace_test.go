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

func TestPropertiesNamespaceCommandInCommandCollection(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	propertiesNamespaceCommand, err := commands.GetCommand(COMMAND_NAME_PROPERTIES_NAMESPACE)
	assert.Nil(t, err)

	assert.Equal(t, COMMAND_NAME_PROPERTIES_NAMESPACE, propertiesNamespaceCommand.Name())
	assert.Nil(t, propertiesNamespaceCommand.Values())
	assert.NotNil(t, propertiesNamespaceCommand.CobraCommand())
}

func TestPropertiesNamespaceHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"properties", "namespaces", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...

	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'properties namespaces' command", "", factory, t)

	assert.Nil(t, err)
}

func TestPropertiesNamespaceProducesUsageReport(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"properties", "namespaces"}

	// When...
	err := Execute(factory, args)

	// Then...

	// Check what the user saw is reasonable.
	checkOutput("Usage:\n  galasactl properties namespaces [command]", "", factory, t)

	assert.Nil(t, err)
}
