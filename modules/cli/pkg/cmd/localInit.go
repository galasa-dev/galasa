/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"github.com/galasa-dev/cli/pkg/embedded"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/spf13/cobra"
)

type LocalInitCommand struct {
	cobraCommand *cobra.Command
	values       *LocalInitCmdValues
}

type LocalInitCmdValues struct {
	isDevelopmentLocalInit bool
}

// ------------------------------------------------------------------------------------------------
// Constructors
// ------------------------------------------------------------------------------------------------
func NewLocalInitCommand(factory spi.Factory, localCommand spi.GalasaCommand, rootCmd spi.GalasaCommand) (spi.GalasaCommand, error) {

	cmd := new(LocalInitCommand)
	err := cmd.init(factory, localCommand, rootCmd)
	return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *LocalInitCommand) Name() string {
	return COMMAND_NAME_LOCAL_INIT
}

func (cmd *LocalInitCommand) CobraCommand() *cobra.Command {
	return cmd.cobraCommand
}

func (cmd *LocalInitCommand) Values() interface{} {
	return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------

func (cmd *LocalInitCommand) init(factory spi.Factory, localCommand spi.GalasaCommand, rootCmd spi.GalasaCommand) error {
	var err error

	cmd.values = &LocalInitCmdValues{}
	cmd.cobraCommand = cmd.createCobraCommand(factory, localCommand, rootCmd)

	return err
}

func (cmd *LocalInitCommand) createCobraCommand(
	factory spi.Factory,
	localCommand spi.GalasaCommand,
	rootCmd spi.GalasaCommand,
) *cobra.Command {

	localInitCobraCmd := &cobra.Command{
		Use:   "init",
		Short: "Initialises Galasa home folder",
		Long:  "Initialises Galasa home folder in home directory with all the properties files",
		Args:  cobra.NoArgs,
		RunE: func(cobraCommand *cobra.Command, args []string) error {
			return cmd.executeEnvInit(factory, cmd.values, rootCmd.Values().(*RootCmdValues))
		},
	}

	localInitCobraCmd.Flags().BoolVar(&cmd.values.isDevelopmentLocalInit, "development", false, "Use bleeding-edge galasa versions and repositories.")

	localCommand.CobraCommand().AddCommand(localInitCobraCmd)

	return localInitCobraCmd
}

func (cmd *LocalInitCommand) executeEnvInit(factory spi.Factory, localInitCmdValues *LocalInitCmdValues, rootCmdValues *RootCmdValues) error {

	var err error

	// Operations on the file system will all be relative to the current folder.
	fileSystem := factory.GetFileSystem()

	err = utils.CaptureLog(fileSystem, rootCmdValues.logFileName)
	if err == nil {

		rootCmdValues.isCapturingLogs = true

		env := factory.GetEnvironment()

		err = localEnvInit(fileSystem, env, rootCmdValues.CmdParamGalasaHomePath, localInitCmdValues.isDevelopmentLocalInit)
	}
	return err
}

func localEnvInit(
	fileSystem spi.FileSystem,
	env spi.Environment,
	cmdFlagGalasaHome string,
	isDevelopment bool,
) error {

	galasaHome, err := utils.NewGalasaHome(fileSystem, env, cmdFlagGalasaHome)
	if err == nil {
		embeddedFileSystem := embedded.GetReadOnlyFileSystem()
		err = utils.InitialiseGalasaHomeFolder(galasaHome, fileSystem, embeddedFileSystem)
		if err == nil {
			err = utils.InitialiseM2Folder(fileSystem, embeddedFileSystem, isDevelopment)
		}
	}
	return err
}
