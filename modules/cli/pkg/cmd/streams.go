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

type StreamsCmdValues struct {
	name string
}

type StreamsCommand struct {
	values       *StreamsCmdValues
	cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors methods
// ------------------------------------------------------------------------------------------------
func NewStreamsCommand(rootCmd spi.GalasaCommand, commsFlagSet GalasaFlagSet) (spi.GalasaCommand, error) {

	cmd := new(StreamsCommand)
	err := cmd.init(rootCmd, commsFlagSet)
	return cmd, err

}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *StreamsCommand) Name() string {
	return COMMAND_NAME_STREAMS
}

func (cmd *StreamsCommand) CobraCommand() *cobra.Command {
	return cmd.cobraCommand
}

func (cmd *StreamsCommand) Values() interface{} {
	return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------

func (cmd *StreamsCommand) init(rootCmd spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {

	var err error
	cmd.values = &StreamsCmdValues{}
	cmd.cobraCommand = cmd.createCobraCommand(rootCmd, commsFlagSet)

	return err

}

func (cmd *StreamsCommand) createCobraCommand(
	rootCommand spi.GalasaCommand,
	commsFlagSet GalasaFlagSet,
) *cobra.Command {

	streamsCobraCmd := &cobra.Command{
		Use:   "streams",
		Short: "Manages test streams in a Galasa service",
		Long:  "Parent command for managing test streams in a Galasa service",
	}

	streamsCobraCmd.PersistentFlags().AddFlagSet(commsFlagSet.Flags())
	rootCommand.CobraCommand().AddCommand(streamsCobraCmd)

	return streamsCobraCmd

}

func addStreamNameFlag(cmd *cobra.Command, isMandatory bool, streamCmdValues *StreamsCmdValues) {

	flagName := "name"
	var description string

	if isMandatory {
		description = "A mandatory field indicating the name of a test stream."
	} else {
		description = "An optional field indicating the name of a test stream"
	}

	cmd.Flags().StringVar(&streamCmdValues.name, flagName, "", description)

	if isMandatory {
		cmd.MarkFlagRequired(flagName)
	}

}
