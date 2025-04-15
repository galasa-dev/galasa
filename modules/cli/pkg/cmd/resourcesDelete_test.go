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

func TestResourcesDeleteCommandInCommandCollection(t *testing.T) {

	factory := utils.NewMockFactory()
	commands, _ := NewCommandCollection(factory)

	resourcesDeleteCommand, _ := commands.GetCommand(COMMAND_NAME_RESOURCES_DELETE)
	assert.NotNil(t, resourcesDeleteCommand)
	assert.Equal(t, COMMAND_NAME_RESOURCES_DELETE, resourcesDeleteCommand.Name())
	assert.NotNil(t, resourcesDeleteCommand.Values())
	assert.IsType(t, &ResourcesDeleteCmdValues{}, resourcesDeleteCommand.Values())
	assert.NotNil(t, resourcesDeleteCommand.CobraCommand())
}
