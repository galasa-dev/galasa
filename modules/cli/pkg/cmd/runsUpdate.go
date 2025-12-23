/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"fmt"
	"strings"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/runs"
	"github.com/galasa-dev/cli/pkg/spi"
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

	addTagsFlag := runsUpdateCobraCmd.Flags().StringSlice("add-tags", nil, "comma-separated list of tags to add")
	removeTagsFlag := runsUpdateCobraCmd.Flags().StringSlice("remove-tags", nil, "comma-separated list of tags to remove")

	runsUpdateCobraCmd.MarkFlagRequired("add-tags")
	runsUpdateCobraCmd.MarkFlagRequired("remove-tags")

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
			byteReader := factory.GetByteReader()
			timeService := factory.GetTimeService()

			// Call to process the command in a unit-testable way.
			err = runs.RunsUpdate(
				cmd.values.runName,
				cmd.values.addTags,
				cmd.values.removeTags,
				console,
				commsClient,
				timeService,
				byteReader,
			)
		}
	}

	if err != nil {
		if strings.Contains(err.Error(), "duplicate tag") {
			console.WriteString(fmt.Sprintf("Warning: Duplicate tag(s) detected in --add-tags or --remove-tags. Only unique tags will be applied.\n"))
		} else if strings.Contains(err.Error(), "tag added and then removed") {
			console.WriteString("Error: You have added and then removed the same tag. This may be an unintentional error.\n")
		} else {
			console.WriteString(err.Error())
		}
	}

	return err
}
