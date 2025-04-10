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

type UsersCmdValues struct {
	name string
}

type UsersCommand struct {
	values       *UsersCmdValues
	cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors methods
// ------------------------------------------------------------------------------------------------
func NewUsersCommand(rootCmd spi.GalasaCommand, commsFlagSet GalasaFlagSet) (spi.GalasaCommand, error) {

	cmd := new(UsersCommand)
	err := cmd.init(rootCmd, commsFlagSet)
	return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *UsersCommand) Name() string {
	return COMMAND_NAME_USERS
}

func (cmd *UsersCommand) CobraCommand() *cobra.Command {
	return cmd.cobraCommand
}

func (cmd *UsersCommand) Values() interface{} {
	return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------

func (cmd *UsersCommand) init(rootCmd spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {

	var err error

	cmd.values = &UsersCmdValues{}
	cmd.cobraCommand = cmd.createCobraCommand(rootCmd, commsFlagSet)

	return err
}

func (cmd *UsersCommand) createCobraCommand(
	rootCommand spi.GalasaCommand,
	commsFlagSet GalasaFlagSet,
) *cobra.Command {

	usersCobraCmd := &cobra.Command{
		Use:   "users",
		Short: "Manages users in an ecosystem",
		Long:  "Allows interaction with the user servlet to return information about users.",
	}

	usersCobraCmd.PersistentFlags().AddFlagSet(commsFlagSet.Flags())
	rootCommand.CobraCommand().AddCommand(usersCobraCmd)

	return usersCobraCmd
}

func addLoginIdFlag(cmd *cobra.Command, isMandatory bool, userCmdValues *UsersCmdValues) {

	flagName := "login-id"
	var description string

	if isMandatory {
		description = "A mandatory field indicating the login ID of a user."
	} else {
		description = "An optional field indicating the login ID of a user."
	}

	cmd.Flags().StringVar(&userCmdValues.name, flagName, "", description)

	if isMandatory {
		cmd.MarkFlagRequired(flagName)
	}

}
