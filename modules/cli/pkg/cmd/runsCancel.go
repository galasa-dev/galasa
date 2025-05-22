/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"log"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/runs"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/spf13/cobra"
)

// Objective: Allow the user to do this:
//    runs cancel --name U123
// And then galasactl cancels the run by abandoning it.

type RunsCancelCommand struct {
	values       *RunsCancelCmdValues
	cobraCommand *cobra.Command
}

type RunsCancelCmdValues struct {
	runName string
	group   string
}

// ------------------------------------------------------------------------------------------------
// Constructors methods
// ------------------------------------------------------------------------------------------------
func NewRunsCancelCommand(factory spi.Factory, runsCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) (spi.GalasaCommand, error) {
	cmd := new(RunsCancelCommand)
	err := cmd.init(factory, runsCommand, commsFlagSet)
	return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *RunsCancelCommand) Name() string {
	return COMMAND_NAME_RUNS_CANCEL
}

func (cmd *RunsCancelCommand) CobraCommand() *cobra.Command {
	return cmd.cobraCommand
}

func (cmd *RunsCancelCommand) Values() interface{} {
	return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------
func (cmd *RunsCancelCommand) init(factory spi.Factory, runsCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {
	var err error
	cmd.values = &RunsCancelCmdValues{}
	cmd.cobraCommand, err = cmd.createRunsCancelCobraCmd(
		factory,
		runsCommand,
		commsFlagSet.Values().(*CommsFlagSetValues),
	)
	return err
}

func (cmd *RunsCancelCommand) createRunsCancelCobraCmd(factory spi.Factory,
	runsCommand spi.GalasaCommand,
	commsFlagSetValues *CommsFlagSetValues,
) (*cobra.Command, error) {

	var err error

	runsCancelCmd := &cobra.Command{
		Use:     "cancel",
		Short:   "cancel an active run in the ecosystem",
		Long:    "Cancel an active test run in the ecosystem if it is stuck or looping.",
		Args:    cobra.NoArgs,
		Aliases: []string{"runs cancel"},
		RunE: func(cobraCmd *cobra.Command, args []string) error {
			return cmd.executeCancel(factory, commsFlagSetValues)
		},
	}

	runsCancelCmd.PersistentFlags().StringVar(&cmd.values.runName, "name", "", "A flag indicating the name of the test run to cancel. Cannot be used with --group")
	runsCancelCmd.PersistentFlags().StringVar(&cmd.values.group, "group", "", "A flag indicating the name of the test group to cancel. Cannot be used with --name")

	runsCancelCmd.MarkFlagsMutuallyExclusive("group", "name")
	runsCancelCmd.MarkFlagsOneRequired("name", "group")

	runsCommand.CobraCommand().AddCommand(runsCancelCmd)

	return runsCancelCmd, err
}

func (cmd *RunsCancelCommand) executeCancel(
	factory spi.Factory,
	commsFlagSetValues *CommsFlagSetValues,
) error {

	var err error

	// Operations on the file system will all be relative to the current folder.
	fileSystem := factory.GetFileSystem()

	err = utils.CaptureLog(fileSystem, commsFlagSetValues.logFileName)
	if err == nil {
		commsFlagSetValues.isCapturingLogs = true

		log.Println("Galasa CLI - Cancel an active run by abandoning it.")

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

				// Call to process command in unit-testable way.
				err = runs.CancelRun(
					cmd.values.runName,
					timeService,
					console,
					commsClient,
					cmd.values.group,
				)
			}
		}
	}

	log.Printf("executeRunsCancel returning %v\n", err)
	return err
}
