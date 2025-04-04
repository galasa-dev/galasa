/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"log"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/auth"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/spf13/cobra"
)

// Objective: Allow user to do this:
//
//		auth tokens get
//	 And then display all tokens or returns empty
type AuthTokensGetCommand struct {
	cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors methods
// ------------------------------------------------------------------------------------------------
func NewAuthTokensGetCommand(
	factory spi.Factory,
	authTokensCommand spi.GalasaCommand,
	commsFlagSet GalasaFlagSet,
) (spi.GalasaCommand, error) {

	cmd := new(AuthTokensGetCommand)

	err := cmd.init(factory, authTokensCommand, commsFlagSet)
	return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *AuthTokensGetCommand) Name() string {
	return COMMAND_NAME_AUTH_TOKENS_GET
}

func (cmd *AuthTokensGetCommand) CobraCommand() *cobra.Command {
	return cmd.cobraCommand
}

func (cmd *AuthTokensGetCommand) Values() interface{} {
	// There are no values.
	return nil
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------
func (cmd *AuthTokensGetCommand) init(
	factory spi.Factory,
	authTokensCommand spi.GalasaCommand,
	commsFlagSet GalasaFlagSet,
) error {
	var err error

	cmd.cobraCommand, err = cmd.createCobraCmd(factory, authTokensCommand, commsFlagSet)

	return err
}

func (cmd *AuthTokensGetCommand) createCobraCmd(
	factory spi.Factory,
	authTokensCommand spi.GalasaCommand,
	commsFlagSet GalasaFlagSet,
) (*cobra.Command, error) {

	var err error

	commsFlagSetValues := commsFlagSet.Values().(*CommsFlagSetValues)

	authTokensGetCommandValues := authTokensCommand.Values().(*AuthTokensCmdValues)
	authGetTokensCobraCmd := &cobra.Command{
		Use:     "get",
		Short:   "Get a list of authentication tokens",
		Long:    "Get a list of tokens used for authentication with the Galasa API server",
		Aliases: []string{COMMAND_NAME_AUTH_TOKENS_GET},
		RunE: func(cobraCommand *cobra.Command, args []string) error {
			return cmd.executeAuthTokensGet(factory, authTokensCommand.Values().(*AuthTokensCmdValues), commsFlagSetValues)
		},
	}

	addLoginIdFlagToAuthTokensGet(authGetTokensCobraCmd, authTokensGetCommandValues)
	authTokensCommand.CobraCommand().AddCommand(authGetTokensCobraCmd)

	return authGetTokensCobraCmd, err
}

func (cmd *AuthTokensGetCommand) executeAuthTokensGet(
	factory spi.Factory,
	authTokenCmdValues *AuthTokensCmdValues,
	commsFlagSetValues *CommsFlagSetValues,
) error {

	var err error
	// Operations on the file system will all be relative to the current folder.
	fileSystem := factory.GetFileSystem()

	err = utils.CaptureLog(fileSystem, commsFlagSetValues.logFileName)
	if err == nil {
		commsFlagSetValues.isCapturingLogs = true
	
		log.Println("Galasa CLI - Get tokens from the ecosystem")
	
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
				getTokensFunc := func(apiClient *galasaapi.APIClient) error {
					return auth.GetTokens(apiClient, console, authTokenCmdValues.loginId)
				}
				err = commsClient.RunAuthenticatedCommandWithRateLimitRetries(getTokensFunc)
			}
		}
	}

	return err
}

func addLoginIdFlagToAuthTokensGet(cmd *cobra.Command, authTokensGetCmdValues *AuthTokensCmdValues) {

	flagName := "user"
	var description string = "Optional. Retrieves a list of access tokens for the user with the given username."

	cmd.Flags().StringVar(&authTokensGetCmdValues.loginId, flagName, "", description)
}
