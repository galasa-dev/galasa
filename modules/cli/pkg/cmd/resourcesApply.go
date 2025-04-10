/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cmd

import (
	"log"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/resources"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/spf13/cobra"
)

// Variables set by cobra's command-line parsing.
type ResourcesApplyCmdValues struct {
}

type ResourcesApplyCommand struct {
	values       *ResourcesApplyCmdValues
	cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors methods
// ------------------------------------------------------------------------------------------------
func NewResourcesApplyCommand(factory spi.Factory, resourcesCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) (spi.GalasaCommand, error) {

	cmd := new(ResourcesApplyCommand)
	err := cmd.init(factory, resourcesCommand, commsFlagSet)
	return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *ResourcesApplyCommand) Name() string {
	return COMMAND_NAME_RESOURCES_APPLY
}

func (cmd *ResourcesApplyCommand) CobraCommand() *cobra.Command {
	return cmd.cobraCommand
}

func (cmd *ResourcesApplyCommand) Values() interface{} {
	return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------

func (cmd *ResourcesApplyCommand) init(factory spi.Factory, resourcesApplyCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {

	var err error

	cmd.values = &ResourcesApplyCmdValues{}
	cmd.cobraCommand = cmd.createCobraCommand(factory, resourcesApplyCommand, commsFlagSet.Values().(*CommsFlagSetValues))

	return err
}

func (cmd *ResourcesApplyCommand) createCobraCommand(
	factory spi.Factory,
	resourcesCommand spi.GalasaCommand,
	commsFlagSetValues *CommsFlagSetValues,
) *cobra.Command {

	resourcesApplyCommandValues := resourcesCommand.Values().(*ResourcesCmdValues)
	resourcesApplyCmd := &cobra.Command{
		Use:     "apply",
		Short:   "Apply file contents to the ecosystem.",
		Long:    "Create or Update resources from a given file in the Galasa Ecosystem",
		Args:    cobra.NoArgs,
		Aliases: []string{"resources apply"},
		RunE: func(cmd *cobra.Command, args []string) error {
			return executeResourcesApply(factory, resourcesApplyCommandValues, commsFlagSetValues)
		},
	}

	resourcesCommand.CobraCommand().AddCommand(resourcesApplyCmd)

	return resourcesApplyCmd
}

func executeResourcesApply(factory spi.Factory,
	resourcesCmdValues *ResourcesCmdValues,
	commsFlagSetValues *CommsFlagSetValues,
) error {
	action := "apply"

	err := loadAndPassDataIntoResourcesApi(action, factory, resourcesCmdValues, commsFlagSetValues)

	return err
}

func loadAndPassDataIntoResourcesApi(action string, factory spi.Factory, resourcesCmdValues *ResourcesCmdValues, commsFlagSetValues *CommsFlagSetValues) error {
	var err error
	// Operations on the file system will all be relative to the current folder.
	fileSystem := factory.GetFileSystem()
	err = utils.CaptureLog(fileSystem, commsFlagSetValues.logFileName)
	if err == nil {
		commsFlagSetValues.isCapturingLogs = true
	
		log.Println("Galasa CLI -", action, "Resources Command")
	
		// Get the ability to query environment variables.
		env := factory.GetEnvironment()
	
		var galasaHome spi.GalasaHome
		galasaHome, err = utils.NewGalasaHome(fileSystem, env, commsFlagSetValues.CmdParamGalasaHomePath)
	
		if err == nil {

			var commsClient api.APICommsClient
			commsClient, err = api.NewAPICommsClient(
				commsFlagSetValues.bootstrap,
				commsFlagSetValues.maxRetries,
				commsFlagSetValues.retryBackoffSeconds,
				factory,
				galasaHome,
			)

			if err == nil {
				err = resources.ApplyResources(
					action,
					resourcesCmdValues.filePath,
					fileSystem,
					commsClient,
				)
			}
		}
	}

	return err
}
