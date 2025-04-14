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

//Objective: Allow user to do this:
//	auth tokens ...

type AuthTokensCmdValues struct {
	loginId   string
}

type AuthTokensCommand struct {
	values       *AuthTokensCmdValues
	cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors methods
// ------------------------------------------------------------------------------------------------
func NewAuthTokensCommand(authCommand spi.GalasaCommand) (spi.GalasaCommand, error) {
	cmd := new(AuthTokensCommand)

	err := cmd.init(authCommand)
	return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *AuthTokensCommand) Name() string {
	return COMMAND_NAME_AUTH_TOKENS
}

func (cmd *AuthTokensCommand) CobraCommand() *cobra.Command {
	return cmd.cobraCommand
}

func (cmd *AuthTokensCommand) Values() interface{} {
	return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------
func (cmd *AuthTokensCommand) init(authCommand spi.GalasaCommand) error {
	var err error
	cmd.values = &AuthTokensCmdValues{}
	cmd.cobraCommand, err = cmd.createAuthTokensCobraCmd(authCommand)
	return err
}

func (cmd *AuthTokensCommand) createAuthTokensCobraCmd(
	authCommand spi.GalasaCommand,
) (*cobra.Command, error) {

	var err error
	authTokensCmd := &cobra.Command{
		Use:     "tokens",
		Short:   "Queries tokens in an ecosystem",
		Long:    "Allows interaction with a Galasa Ecosystem's auth store to query tokens and retrieve their details",
		Aliases: []string{COMMAND_NAME_AUTH_TOKENS},
		Args:    cobra.NoArgs,
	}

	authCommand.CobraCommand().AddCommand(authTokensCmd)

	return authTokensCmd, err
}

