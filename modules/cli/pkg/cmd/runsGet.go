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
//    run get --runname 12345
// And then show the results in a human-readable form.

// Variables set by cobra's command-line parsing.
type RunsGetCmdValues struct {
	runName            string
	age                string
	outputFormatString string
	requestor          string
	result             string
	isActiveRuns       bool
	group              string
	tags               []string
}

type RunsGetCommand struct {
	values       *RunsGetCmdValues
	cobraCommand *cobra.Command
}

func NewRunsGetCommand(factory spi.Factory, runsCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) (spi.GalasaCommand, error) {
	cmd := new(RunsGetCommand)
	err := cmd.init(factory, runsCommand, commsFlagSet)
	return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *RunsGetCommand) Name() string {
	return COMMAND_NAME_RUNS_GET
}

func (cmd *RunsGetCommand) CobraCommand() *cobra.Command {
	return cmd.cobraCommand
}

func (cmd *RunsGetCommand) Values() interface{} {
	return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------

func (cmd *RunsGetCommand) init(factory spi.Factory, runsCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {
	var err error
	cmd.values = &RunsGetCmdValues{}
	cmd.cobraCommand, err = cmd.createCobraCommand(factory, runsCommand, commsFlagSet.Values().(*CommsFlagSetValues))
	return err
}

func (cmd *RunsGetCommand) createCobraCommand(
	factory spi.Factory,
	runsCommand spi.GalasaCommand,
	commsFlagSetValues *CommsFlagSetValues,
) (*cobra.Command, error) {

	var err error

	runsGetCobraCmd := &cobra.Command{
		Use:     "get",
		Short:   "Get the details of a test runname which ran or is running.",
		Long:    "Get the details of a test runname which ran or is running, displaying the results to the caller.",
		Args:    cobra.NoArgs,
		Aliases: []string{"runs get"},
		RunE: func(cobraCmd *cobra.Command, args []string) error {
			return cmd.executeRunsGet(factory, commsFlagSetValues)
		},
	}

	units := runs.GetTimeUnitsForErrorMessage()
	formatters := runs.GetFormatterNamesString(runs.CreateFormatters())
	runsGetCobraCmd.PersistentFlags().StringVar(&cmd.values.runName, "name", "", "the name of the test run we want information about."+
		" Cannot be used in conjunction with --requestor, --result or --active flags")
	runsGetCobraCmd.PersistentFlags().StringVar(&cmd.values.group, "group", "", "the name of the group to return tests under that group."+
		" Cannot be used in conjunction with --name")
	runsGetCobraCmd.PersistentFlags().StringVar(&cmd.values.age, "age", "", "the age of the test run(s) we want information about. Supported formats are: 'FROM' or 'FROM:TO', where FROM and TO are each ages,"+
		" made up of an integer and a time-unit qualifier. Supported time-units are "+units+". If missing, the TO part is defaulted to '0h'. Examples: '--age 1d',"+
		" '--age 6h:1h' (list test runs which happened from 6 hours ago to 1 hour ago)."+
		" The TO part must be a smaller time-span than the FROM part.")
	runsGetCobraCmd.PersistentFlags().StringVar(&cmd.values.outputFormatString, "format", "summary", "output format for the data returned. Supported formats are: "+formatters+".")
	runsGetCobraCmd.PersistentFlags().StringVar(&cmd.values.requestor, "requestor", "", "the requestor of the test run we want information about."+
		" Cannot be used in conjunction with --name flag.")
	runsGetCobraCmd.PersistentFlags().StringVar(&cmd.values.result, "result", "", "A filter on the test runs we want information about. Optional. Default is to display test runs with any result. Case insensitive. Value can be a single value or a comma-separated list. For example \"--result Failed,Ignored,EnvFail\"."+
		" Cannot be used in conjunction with --name or --active flag.")
	runsGetCobraCmd.PersistentFlags().BoolVar(&cmd.values.isActiveRuns, "active", false, "parameter to retrieve runs that have not finished yet."+
		" Cannot be used in conjunction with --name or --result flag.")
	runsGetCobraCmd.PersistentFlags().StringSliceVar(&cmd.values.tags, "tags", make([]string, 0), "the tags associated with test runs to be retrieved."+
		" Tags can be supplied in a comma-separated list (e.g. --tags tag1,tag2,tag3) or as separate '--tags' flags (e.g. --tags tag1 --tags tag2).")

	runsGetCobraCmd.MarkFlagsMutuallyExclusive("name", "requestor")
	runsGetCobraCmd.MarkFlagsMutuallyExclusive("name", "result")
	runsGetCobraCmd.MarkFlagsMutuallyExclusive("name", "active")
	runsGetCobraCmd.MarkFlagsMutuallyExclusive("result", "active")
	runsGetCobraCmd.MarkFlagsMutuallyExclusive("group", "name")

	runsCommand.CobraCommand().AddCommand(runsGetCobraCmd)

	return runsGetCobraCmd, err
}

func (cmd *RunsGetCommand) executeRunsGet(
	factory spi.Factory,
	commsFlagSetValues *CommsFlagSetValues,
) error {

	var err error

	// Operations on the file system will all be relative to the current folder.
	fileSystem := factory.GetFileSystem()

	err = utils.CaptureLog(fileSystem, commsFlagSetValues.logFileName)
	if err == nil {
		commsFlagSetValues.isCapturingLogs = true
	
		log.Println("Galasa CLI - Get info about a run")
	
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
				err = runs.GetRuns(
					cmd.values.runName,
					cmd.values.age,
					cmd.values.requestor,
					cmd.values.result,
					cmd.values.isActiveRuns,
					cmd.values.outputFormatString,
					cmd.values.group,
					cmd.values.tags,
					timeService,
					console,
					commsClient,
				)
			}
		}
	}


	log.Printf("executeRunsGet returning %v", err)
	return err
}
