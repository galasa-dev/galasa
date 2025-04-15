/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"log"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/spi"

	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/spf13/cobra"
)

type AuthLoginCmdValues struct {}

type AuthLoginComamnd struct {
	values       *AuthLoginCmdValues
	cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors methods
// ------------------------------------------------------------------------------------------------
func NewAuthLoginCommand(
	factory spi.Factory,
	authCommand spi.GalasaCommand,
	rootCommand spi.GalasaCommand,
	commsFlagSet GalasaFlagSet,
) (spi.GalasaCommand, error) {
	cmd := new(AuthLoginComamnd)
	err := cmd.init(factory, authCommand, commsFlagSet)
	return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *AuthLoginComamnd) Name() string {
	return COMMAND_NAME_AUTH_LOGIN
}

func (cmd *AuthLoginComamnd) CobraCommand() *cobra.Command {
	return cmd.cobraCommand
}

func (cmd *AuthLoginComamnd) Values() interface{} {
	return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------
func (cmd *AuthLoginComamnd) init(factory spi.Factory, authCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {
	var err error

	cmd.values = &AuthLoginCmdValues{}

	cmd.cobraCommand, err = cmd.createCobraCommand(factory, authCommand, commsFlagSet)

	return err
}

func (cmd *AuthLoginComamnd) createCobraCommand(
	factory spi.Factory,
	authCommand spi.GalasaCommand,
	commsFlagSet GalasaFlagSet,
) (*cobra.Command, error) {

	var err error

	commsFlagSetValues := commsFlagSet.Values().(*CommsFlagSetValues)

	authLoginCobraCmd := &cobra.Command{
		Use:   "login",
		Short: "Log in to a Galasa ecosystem using an existing access token",
		Long: "Log in to a Galasa ecosystem using an existing access token stored in the 'galasactl.properties' file in your GALASA_HOME directory. " +
			"If you do not have an access token, request one through your ecosystem's web user interface " +
			"and follow the instructions on the web user interface to populate the 'galasactl.properties' file.",
		Args:    cobra.NoArgs,
		Aliases: []string{"auth login"},
		RunE: func(cobraCommand *cobra.Command, args []string) error {
			return cmd.executeAuthLogin(factory, commsFlagSetValues)
		},
	}

	authCommand.CobraCommand().AddCommand(authLoginCobraCmd)

	return authLoginCobraCmd, err
}

func (cmd *AuthLoginComamnd) executeAuthLogin(
	factory spi.Factory,
	commsFlagSetValues *CommsFlagSetValues,
) error {

	var err error

	// Operations on the file system will all be relative to the current folder.
	fileSystem := factory.GetFileSystem()
	err = utils.CaptureLog(fileSystem, commsFlagSetValues.logFileName)
	if err == nil {
		commsFlagSetValues.isCapturingLogs = true
	
		log.Println("Galasa CLI - Log in to an ecosystem")
	
		// Get the ability to query environment variables.
		env := factory.GetEnvironment()
	
		var galasaHome spi.GalasaHome
		galasaHome, err = utils.NewGalasaHome(fileSystem, env, commsFlagSetValues.CmdParamGalasaHomePath)
		if err != nil {
			panic(err)
		}
	
		var commsClient api.APICommsClient
		commsClient, err = api.NewAPICommsClient(
			commsFlagSetValues.bootstrap,
			commsFlagSetValues.maxRetries,
			commsFlagSetValues.retryBackoffSeconds,
			factory,
			galasaHome,
		)

		if err == nil {
			authenticator := commsClient.GetAuthenticator()
			err = commsClient.RunCommandWithRateLimitRetries(func() error {
				return authenticator.Login()
			})
		}
	}

	return err
}
