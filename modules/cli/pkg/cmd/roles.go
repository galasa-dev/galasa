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

type RolesCmdValues struct {
	name string
}

type RolesCommand struct {
	cobraCommand *cobra.Command
	values       *RolesCmdValues
}

// ------------------------------------------------------------------------------------------------
// Constructors
// ------------------------------------------------------------------------------------------------

func NewRolesCmd(rootCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) (spi.GalasaCommand, error) {
	cmd := new(RolesCommand)
	err := cmd.init(rootCommand, commsFlagSet)
	return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public functions
// ------------------------------------------------------------------------------------------------

func (cmd *RolesCommand) Name() string {
	return COMMAND_NAME_ROLES
}

func (cmd *RolesCommand) CobraCommand() *cobra.Command {
	return cmd.cobraCommand
}

func (cmd *RolesCommand) Values() interface{} {
	return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private functions
// ------------------------------------------------------------------------------------------------

func (cmd *RolesCommand) init(rootCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {

	var err error

	cmd.values = &RolesCmdValues{}
	cmd.cobraCommand, err = cmd.createCobraCommand(rootCommand, commsFlagSet)

	return err
}

func (cmd *RolesCommand) createCobraCommand(rootCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) (*cobra.Command, error) {

	var err error

	RolesCobraCmd := &cobra.Command{
		Use:   "roles",
		Short: "Manage roles stored in the Galasa service",
		Long:  "The parent command for operations to manipulate Roles in the Galasa service",
	}

	RolesCobraCmd.PersistentFlags().AddFlagSet(commsFlagSet.Flags())
	rootCommand.CobraCommand().AddCommand(RolesCobraCmd)

	return RolesCobraCmd, err
}

func addRolesNameFlag(cmd *cobra.Command, isMandatory bool, RolesCmdValues *RolesCmdValues) {

	flagName := "name"
	var description string
	if isMandatory {
		description = "A mandatory flag that identifies the role to be created or manipulated."
	} else {
		description = "An optional flag that identifies the role to be retrieved by name."
	}

	cmd.Flags().StringVar(&RolesCmdValues.name, flagName, "", description)

	if isMandatory {
		cmd.MarkFlagRequired(flagName)
	}
}
