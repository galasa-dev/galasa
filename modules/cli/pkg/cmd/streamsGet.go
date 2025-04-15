/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cmd

import (
	"log"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/streams"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/spf13/cobra"
)

type StreamsGetCmdValues struct {
	outputFormat string
}

// Objective: Allow user to do this:
//
//	streams get
type StreamsGetCommand struct {
	cobraCommand *cobra.Command
	values       *StreamsGetCmdValues
}

// ------------------------------------------------------------------------------------------------
// Constructors methods
// ------------------------------------------------------------------------------------------------
func NewStreamsGetCommand(
	factory spi.Factory,
	streamsGetCommand spi.GalasaCommand,
	commsFlagSet GalasaFlagSet,
) (spi.GalasaCommand, error) {

	cmd := new(StreamsGetCommand)
	err := cmd.init(factory, streamsGetCommand, commsFlagSet)
	return cmd, err

}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *StreamsGetCommand) Name() string {
	return COMMAND_NAME_STREAMS_GET
}

func (cmd *StreamsGetCommand) CobraCommand() *cobra.Command {
	return cmd.cobraCommand
}

func (cmd *StreamsGetCommand) Values() interface{} {
	return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------
func (cmd *StreamsGetCommand) init(factory spi.Factory, streamsCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {

	var err error
	cmd.values = &StreamsGetCmdValues{}
	cmd.cobraCommand, err = cmd.createCobraCmd(factory, streamsCommand, commsFlagSet)
	return err

}

func (cmd *StreamsGetCommand) createCobraCmd(
	factory spi.Factory,
	streamsCommand spi.GalasaCommand,
	commsFlagSet GalasaFlagSet,
) (*cobra.Command, error) {

	var err error

	commsFlagSetValues := commsFlagSet.Values().(*CommsFlagSetValues)
	streamCommandValues := streamsCommand.Values().(*StreamsCmdValues)

	streamsGetCobraCmd := &cobra.Command{
		Use:     "get",
		Short:   "Gets a list of test streams",
		Long:    "Get a list of test streams from the Galasa service",
		Aliases: []string{COMMAND_NAME_STREAMS_GET},
		RunE: func(cobraCommand *cobra.Command, args []string) error {
			return cmd.executeStreamsGet(
				factory, streamsCommand.Values().(*StreamsCmdValues), commsFlagSetValues,
			)
		},
	}

	addStreamNameFlag(streamsGetCobraCmd, false, streamCommandValues)

	formatters := streams.GetFormatterNamesAsString()
	streamsGetCobraCmd.Flags().StringVar(&cmd.values.outputFormat, "format", "summary", "the output format of the returned streams. Supported formats are: "+formatters+".")

	streamsCommand.CobraCommand().AddCommand(streamsGetCobraCmd)

	return streamsGetCobraCmd, err

}

func (cmd *StreamsGetCommand) executeStreamsGet(
	factory spi.Factory,
	streamsCmdValues *StreamsCmdValues,
	commsFlagSetValues *CommsFlagSetValues,
) error {

	var err error

	// Operations on the file system will all be relative to the current folder.
	fileSystem := factory.GetFileSystem()

	err = utils.CaptureLog(fileSystem, commsFlagSetValues.logFileName)
	if err == nil {

		commsFlagSetValues.isCapturingLogs = true
		log.Println("Galasa CLI - Get streams from the Galasa service")

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
				var byteReader = factory.GetByteReader()

				getStreamsFunc := func(apiClient *galasaapi.APIClient) error {
					return streams.GetStreams(streamsCmdValues.name, cmd.values.outputFormat, apiClient, console,byteReader)
				}

				err = commsClient.RunAuthenticatedCommandWithRateLimitRetries(getStreamsFunc)

			}

		}

	}

	return err
}
