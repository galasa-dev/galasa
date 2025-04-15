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
// auth tokens delete --tokenid xxx
// And delete the token of that id
type AuthTokensDeleteCmdValues struct {
	tokenId string
}
type AuthTokensDeleteCommand struct {
	values       *AuthTokensDeleteCmdValues
	cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors methods
// ------------------------------------------------------------------------------------------------
func NewAuthTokensDeleteCommand(
	factory spi.Factory,
	authTokensCommand spi.GalasaCommand,
	commsFlagSet GalasaFlagSet,
) (spi.GalasaCommand, error) {

	cmd := new(AuthTokensDeleteCommand)
	err := cmd.init(factory, authTokensCommand, commsFlagSet)
	return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *AuthTokensDeleteCommand) Name() string {
	return COMMAND_NAME_AUTH_TOKENS_DELETE
}

func (cmd *AuthTokensDeleteCommand) CobraCommand() *cobra.Command {
	return cmd.cobraCommand
}

func (cmd *AuthTokensDeleteCommand) Values() interface{} {
	return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------
func (cmd *AuthTokensDeleteCommand) init(
	factory spi.Factory,
	authTokensCommand spi.GalasaCommand,
	commsFlagSet GalasaFlagSet,
) error {
	var err error

	cmd.values = &AuthTokensDeleteCmdValues{}
	cmd.cobraCommand, err = cmd.createCobraCmd(factory, authTokensCommand, commsFlagSet)

	return err
}

func (cmd *AuthTokensDeleteCommand) createCobraCmd(
	factory spi.Factory,
	authTokensCommand spi.GalasaCommand,
	commsFlagSet GalasaFlagSet,
) (*cobra.Command, error) {

	var err error

	commsFlagSetValues := commsFlagSet.Values().(*CommsFlagSetValues)

	authDeleteTokensCobraCmd := &cobra.Command{
		Use:     "delete",
		Short:   "Revokes a personal access token",
		Long:    "Revokes a token used for authentication with the Galasa API server through the provided token id",
		Aliases: []string{COMMAND_NAME_AUTH_TOKENS_DELETE},
		RunE: func(cobraCommand *cobra.Command, args []string) error {
			return cmd.executeAuthTokensDelete(factory, commsFlagSetValues)
		},
	}

	authDeleteTokensCobraCmd.Flags().StringVar(&cmd.values.tokenId, "tokenid", "", "The ID of the token to be revoked.")
	authDeleteTokensCobraCmd.MarkFlagRequired("tokenid")

	authTokensCommand.CobraCommand().AddCommand(authDeleteTokensCobraCmd)

	return authDeleteTokensCobraCmd, err
}

func (cmd *AuthTokensDeleteCommand) executeAuthTokensDelete(
	factory spi.Factory,
	commsFlagSetValues *CommsFlagSetValues,
) error {

	var err error
	// Operations on the file system will all be relative to the current folder.
	fileSystem := factory.GetFileSystem()

	err = utils.CaptureLog(fileSystem, commsFlagSetValues.logFileName)
	if err == nil {
		commsFlagSetValues.isCapturingLogs = true
	
		log.Println("Galasa CLI - Revoke a token from the ecosystem")
	
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
				deleteTokenFunc := func(apiClient *galasaapi.APIClient) error {
					return auth.DeleteToken(cmd.values.tokenId, apiClient)
				}
				err = commsClient.RunAuthenticatedCommandWithRateLimitRetries(deleteTokenFunc)
			}
		}
	}

	return err
}
