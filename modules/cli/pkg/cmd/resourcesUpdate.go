/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cmd

import (
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/spf13/cobra"
)

// Variables set by cobra's command-line parsing.
type ResourcesUpdateCmdValues struct {
}

type ResourcesUpdateCommand struct {
	values       *ResourcesUpdateCmdValues
	cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors methods
// ------------------------------------------------------------------------------------------------
func NewResourcesUpdateCommand(factory spi.Factory, resourcesCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) (spi.GalasaCommand, error) {

	cmd := new(ResourcesUpdateCommand)
	err := cmd.init(factory, resourcesCommand, commsFlagSet)
	return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *ResourcesUpdateCommand) Name() string {
	return COMMAND_NAME_RESOURCES_UPDATE
}

func (cmd *ResourcesUpdateCommand) CobraCommand() *cobra.Command {
	return cmd.cobraCommand
}

func (cmd *ResourcesUpdateCommand) Values() interface{} {
	return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------

func (cmd *ResourcesUpdateCommand) init(factory spi.Factory, resourcesCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {

	var err error

	cmd.values = &ResourcesUpdateCmdValues{}
	cmd.cobraCommand = cmd.createCobraCommand(factory, resourcesCommand, commsFlagSet.Values().(*CommsFlagSetValues))

	return err
}

func (cmd *ResourcesUpdateCommand) createCobraCommand(
	factory spi.Factory,
	resourcesCommand spi.GalasaCommand,
	commsFlagSetValues *CommsFlagSetValues,
) *cobra.Command {

	resourcesUpdateCommandValues := resourcesCommand.Values().(*ResourcesCmdValues)
	resourcesUpdateCmd := &cobra.Command{
		Use:     "update",
		Short:   "Update Galasa Ecosystem resources.",
		Long:    "Update Galasa Ecosystem resources from definitions held in a file.",
		Args:    cobra.NoArgs,
		Aliases: []string{"resources update"},
		RunE: func(cmd *cobra.Command, args []string) error {
			return executeResourcesUpdate(factory, resourcesUpdateCommandValues, commsFlagSetValues)
		},
	}

	resourcesCommand.CobraCommand().AddCommand(resourcesUpdateCmd)

	return resourcesUpdateCmd
}

func executeResourcesUpdate(factory spi.Factory,
	resourcesCmdValues *ResourcesCmdValues,
	commsFlagSetValues *CommsFlagSetValues,
) error {
	action := "update"

	err := loadAndPassDataIntoResourcesApi(action, factory, resourcesCmdValues, commsFlagSetValues)

	return err
}
