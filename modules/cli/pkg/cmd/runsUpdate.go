/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/runs"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/spf13/cobra"
)

// Objective: Allow the user to do this:
//    runs update --name U12345 --add-tags tag1,tag2 --remove-tags tag3

// Variables set by cobra's command-line parsing.
type RunsUpdateCmdValues struct {
	runName     string
	addTags     []string
	removeTags  []string
}

type RunsUpdateCommand struct {
	values       *RunsUpdateCmdValues
	cobraCommand *cobra.Command
}

func NewRunsUpdateCommand(factory spi.Factory, runsCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) (spi.GalasaCommand, error) {
	cmd := new(RunsUpdateCommand)
	err := cmd.init(factory, runsCommand, commsFlagSet)
	return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *RunsUpdateCommand) Name() string {
	return COMMAND_NAME_RUNS_UPDATE
}

func (cmd *RunsUpdateCommand) CobraCommand() *cobra.Command {
	return cmd.cobraCommand
}

func (cmd *RunsUpdateCommand) Values() interface{} {
	return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------
func (cmd *RunsUpdateCommand) init(factory spi.Factory, runsCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {
	var err error
	cmd.values = &RunsUpdateCmdValues{}
	cmd.cobraCommand, err = cmd.createCobraCommand(factory, runsCommand, commsFlagSet.Values().(*CommsFlagSetValues))
	return err
}

func (cmd *RunsUpdateCommand) createCobraCommand(
	factory spi.Factory,
	runsCommand spi.GalasaCommand,
	commsFlagSetValues *CommsFlagSetValues,
) (*cobra.Command, error) {

	var err error

	runsUpdateCobraCmd := &cobra.Command{
		Use:     "update",
		Short:   "Update tags for a named test run.",
		Long:    "Update tags for a named test run.",
		Args:    cobra.NoArgs,
		Aliases: []string{"runs update"},
		RunE: func(cobraCmd *cobra.Command, args []string) error {
			return cmd.executeRunsUpdate(factory, commsFlagSetValues)
		},
	}

	runsUpdateCobraCmd.Flags().StringVar(&cmd.values.runName, "name", "", "the name of the test run we want to update")
	runsUpdateCobraCmd.MarkFlagRequired("name")

	runsUpdateCobraCmd.Flags().StringSliceVar(&cmd.values.addTags, "add-tags", nil, "comma-separated list of tags to add")
	runsUpdateCobraCmd.Flags().StringSliceVar(&cmd.values.removeTags, "remove-tags", nil, "comma-separated list of tags to remove")

	runsCommand.CobraCommand().AddCommand(runsUpdateCobraCmd)

	return runsUpdateCobraCmd, err
}

func (cmd *RunsUpdateCommand) executeRunsUpdate(
	factory spi.Factory,
	commsFlagSetValues *CommsFlagSetValues,
) error {

	var err error

	// Operations on the file system will all be relative to the current folder.
	fileSystem := factory.GetFileSystem()

	err = utils.CaptureLog(fileSystem, commsFlagSetValues.logFileName)
	if err == nil {
		commsFlagSetValues.isCapturingLogs = true

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

				var console = factory.GetStdOutConsole()
				timeService := factory.GetTimeService()

				// Call to process the command in a unit-testable way.
				err = runs.RunsUpdate(
					cmd.values.runName,
					cmd.values.addTags,
					cmd.values.removeTags,
					console,
					commsClient,
					timeService,
				)
			}
		}
	}

	return err
}
