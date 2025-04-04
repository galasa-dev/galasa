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
	"github.com/galasa-dev/cli/pkg/users"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/spf13/cobra"
)

// Objective: Allow user to do this:
//
//	users delete
type UsersDeleteCommand struct {
	cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors methods
// ------------------------------------------------------------------------------------------------
func NewUsersDeleteCommand(
	factory spi.Factory,
	usersDeleteCommand spi.GalasaCommand,
	commsFlagSet GalasaFlagSet,
) (spi.GalasaCommand, error) {

	cmd := new(UsersDeleteCommand)

	err := cmd.init(factory, usersDeleteCommand, commsFlagSet)
	return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *UsersDeleteCommand) Name() string {
	return COMMAND_NAME_USERS_DELETE
}

func (cmd *UsersDeleteCommand) CobraCommand() *cobra.Command {
	return cmd.cobraCommand
}

func (cmd *UsersDeleteCommand) Values() interface{} {
	// There are no values.
	return nil
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------
func (cmd *UsersDeleteCommand) init(factory spi.Factory, usersCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {
	var err error

	cmd.cobraCommand, err = cmd.createCobraCmd(factory, usersCommand, commsFlagSet)

	return err
}

func (cmd *UsersDeleteCommand) createCobraCmd(
	factory spi.Factory,
	usersCommand spi.GalasaCommand,
	commsFlagSet GalasaFlagSet,
) (*cobra.Command, error) {

	var err error

	commsFlagSetValues := commsFlagSet.Values().(*CommsFlagSetValues)

	userCommandValues := usersCommand.Values().(*UsersCmdValues)
	usersDeleteCobraCmd := &cobra.Command{
		Use:     "delete",
		Short:   "Deletes a user by login ID",
		Long:    "Deletes a single user by their login ID from the ecosystem",
		Aliases: []string{COMMAND_NAME_USERS_DELETE},
		RunE: func(cobraCommand *cobra.Command, args []string) error {
			return cmd.executeUsersDelete(factory, usersCommand.Values().(*UsersCmdValues), commsFlagSetValues)
		},
	}

	addLoginIdFlag(usersDeleteCobraCmd, MANDATORY_FLAG, userCommandValues)

	usersCommand.CobraCommand().AddCommand(usersDeleteCobraCmd)

	return usersDeleteCobraCmd, err
}

func (cmd *UsersDeleteCommand) executeUsersDelete(
	factory spi.Factory,
	userCmdValues *UsersCmdValues,
	commsFlagSetValues *CommsFlagSetValues,
) error {

	var err error
	// Operations on the file system will all be relative to the current folder.
	fileSystem := factory.GetFileSystem()
	byteReader := factory.GetByteReader()

	err = utils.CaptureLog(fileSystem, commsFlagSetValues.logFileName)
	if err == nil {
		commsFlagSetValues.isCapturingLogs = true
	
		log.Println("Galasa CLI - Delete user from the ecosystem")
	
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
				deleteUserFunc := func(apiClient *galasaapi.APIClient) error {
					// Call to process the command in a unit-testable way.
					return users.DeleteUser(userCmdValues.name, apiClient, byteReader)
				}
				err = commsClient.RunAuthenticatedCommandWithRateLimitRetries(deleteUserFunc)
			}
		}
	}

	return err
}
