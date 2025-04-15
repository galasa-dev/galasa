/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"log"

	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/spf13/cobra"
)

type AuthLogoutCmdValues struct {
}

type AuthLogoutCommand struct {
	values       *AuthLogoutCmdValues
	cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors methods
// ------------------------------------------------------------------------------------------------
func NewAuthLogoutCommand(factory spi.Factory, authCommand spi.GalasaCommand, rootCmd spi.GalasaCommand) (spi.GalasaCommand, error) {
	cmd := new(AuthLogoutCommand)

	err := cmd.init(factory, authCommand, rootCmd)
	return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *AuthLogoutCommand) Name() string {
	return COMMAND_NAME_AUTH_LOGOUT
}

func (cmd *AuthLogoutCommand) CobraCommand() *cobra.Command {
	return cmd.cobraCommand
}

func (cmd *AuthLogoutCommand) Values() interface{} {
	return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------
func (cmd *AuthLogoutCommand) init(factory spi.Factory, authCommand spi.GalasaCommand, rootCmd spi.GalasaCommand) error {
	var err error
	cmd.values = &AuthLogoutCmdValues{}
	cmd.cobraCommand, err = cmd.createCobraCmd(factory, authCommand, rootCmd)
	return err
}

func (cmd *AuthLogoutCommand) createCobraCmd(factory spi.Factory, authCommand spi.GalasaCommand, rootCmd spi.GalasaCommand) (*cobra.Command, error) {
	var err error

	authLogoutCmd := &cobra.Command{
		Use:     "logout",
		Short:   "Log out from a Galasa ecosystem",
		Long:    "Log out from a Galasa ecosystem that you have previously logged in to",
		Aliases: []string{"auth logout"},
		RunE: func(cobraCommand *cobra.Command, args []string) error {
			return cmd.executeAuthLogout(factory, rootCmd.Values().(*RootCmdValues))
		},
	}

	authCommand.CobraCommand().AddCommand(authLogoutCmd)

	return authLogoutCmd, err
}

func (cmd *AuthLogoutCommand) executeAuthLogout(
	factory spi.Factory,
	rootCmdValues *RootCmdValues,
) error {

	var err error

	// Operations on the file system will all be relative to the current folder.
	fileSystem := factory.GetFileSystem()

	err = utils.CaptureLog(fileSystem, rootCmdValues.logFileName)
	if err == nil {
		rootCmdValues.isCapturingLogs = true

		log.Println("Galasa CLI - Log out of an ecosystem")

		// Get the ability to query environment variables.
		env := factory.GetEnvironment()

		var galasaHome spi.GalasaHome
		galasaHome, err = utils.NewGalasaHome(fileSystem, env, rootCmdValues.CmdParamGalasaHomePath)
		if err == nil {
			authenticator := factory.GetAuthenticator(
				"",
				galasaHome,
			)
			err = authenticator.LogoutOfEverywhere()
		}
	}
	return err
}
