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

// Objective: Allow user to do this:
//
//	streams delete
type StreamsDeleteCommand struct {
	cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors methods
// ------------------------------------------------------------------------------------------------
func NewStreamsDeleteCommand(
	factory spi.Factory,
	streamsDeleteCommand spi.GalasaCommand,
	commsFlagSet GalasaFlagSet,
) (spi.GalasaCommand, error) {

	cmd := new(StreamsDeleteCommand)
	err := cmd.init(factory, streamsDeleteCommand, commsFlagSet)
	return cmd, err

}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *StreamsDeleteCommand) Name() string {
	return COMMAND_NAME_STREAMS_DELETE
}

func (cmd *StreamsDeleteCommand) CobraCommand() *cobra.Command {
	return cmd.cobraCommand
}

func (cmd *StreamsDeleteCommand) Values() interface{} {
	return nil
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------

func (cmd *StreamsDeleteCommand) init(factory spi.Factory, streamsCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {

	var err error

	cmd.cobraCommand, err = cmd.createCobraCmd(factory, streamsCommand, commsFlagSet)

	return err

}

func (cmd *StreamsDeleteCommand) createCobraCmd(
	factory spi.Factory,
	streamsCommand spi.GalasaCommand,
	commsFlagSet GalasaFlagSet,
) (*cobra.Command, error) {

	var err error

	commsFlagSetValues := commsFlagSet.Values().(*CommsFlagSetValues)
	streamsCommandValues := streamsCommand.Values().(*StreamsCmdValues)

	streamsDeleteCobraCmd := &cobra.Command{
		Use:     "delete",
		Short:   "Deletes a test stream by name",
		Long:    "Deletes a single test stream with the given name from the Galasa service",
		Aliases: []string{COMMAND_NAME_STREAMS_DELETE},
		RunE: func(cobraCommand *cobra.Command, args []string) error {
			return cmd.executeStreamsDelete(
				factory, streamsCommand.Values().(*StreamsCmdValues), commsFlagSetValues,
			)
		},
	}

	addStreamNameFlag(streamsDeleteCobraCmd, true, streamsCommandValues)
	streamsCommand.CobraCommand().AddCommand(streamsDeleteCobraCmd)

	return streamsDeleteCobraCmd, err

}

func (cmd *StreamsDeleteCommand) executeStreamsDelete(
	factory spi.Factory,
	streamsCmdValues *StreamsCmdValues,
	commsFlagSetValues *CommsFlagSetValues,
) error {

	var err error

	// Operations on the file system will all be relative to the current folder.
	fileSystem := factory.GetFileSystem()
	byteReader := factory.GetByteReader()

	err = utils.CaptureLog(fileSystem, commsFlagSetValues.logFileName)

	if err == nil {

		commsFlagSetValues.isCapturingLogs = true

		log.Println("Galasa CLI - Delete test stream from the Galasa service")

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
				deleteStreamFunc := func(apiClient *galasaapi.APIClient) error {
					// Call to process the command in a unit-testable way.
					return streams.DeleteStream(streamsCmdValues.name, apiClient, byteReader)
				}
				err = commsClient.RunAuthenticatedCommandWithRateLimitRetries(deleteStreamFunc)
			}
		}
	}

	return err

}
