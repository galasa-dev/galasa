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

type PropertiesCmdValues struct {
	namespace          string
	propertyName       string
}

type PropertiesCommand struct {
	values       *PropertiesCmdValues
	cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors methods
// ------------------------------------------------------------------------------------------------
func NewPropertiesCommand(rootCmd spi.GalasaCommand, commsFlagSet GalasaFlagSet) (spi.GalasaCommand, error) {

	cmd := new(PropertiesCommand)
	err := cmd.init(rootCmd, commsFlagSet)
	return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *PropertiesCommand) Name() string {
	return COMMAND_NAME_PROPERTIES
}

func (cmd *PropertiesCommand) CobraCommand() *cobra.Command {
	return cmd.cobraCommand
}

func (cmd *PropertiesCommand) Values() interface{} {
	return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------

func (cmd *PropertiesCommand) init(rootCmd spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {

	var err error

	cmd.values = &PropertiesCmdValues{}
	cmd.cobraCommand = cmd.createCobraCommand(rootCmd, commsFlagSet)

	return err
}

func (cmd *PropertiesCommand) createCobraCommand(
	rootCommand spi.GalasaCommand,
	commsFlagSet GalasaFlagSet,
) *cobra.Command {
	propertiesCobraCmd := &cobra.Command{
		Use:   "properties",
		Short: "Manages properties in an ecosystem",
		Long:  "Allows interaction with the CPS to create, query and maintain properties in Galasa Ecosystem",
	}

	propertiesCobraCmd.PersistentFlags().AddFlagSet(commsFlagSet.Flags())
	rootCommand.CobraCommand().AddCommand(propertiesCobraCmd)

	return propertiesCobraCmd
}

func addNamespaceFlag(cmd *cobra.Command, isMandatory bool, propertiesCmdValues *PropertiesCmdValues) {

	flagName := "namespace"
	var description string
	if isMandatory {
		description = "A mandatory flag that describes the container for a collection of properties."
	} else {
		description = "An optional flag that describes the container for a collection of properties."
	}
	description += "The first character of the namespace must be in the 'a'-'z' range, " +
		"and following characters can be 'a'-'z' or '0'-'9'"

	cmd.PersistentFlags().StringVarP(&propertiesCmdValues.namespace, flagName, "s", "", description)

	if isMandatory {
		cmd.MarkPersistentFlagRequired(flagName)
	}

}

// Some sub-commands need a name field to be mandatory, some don't.
func addPropertyNameFlag(cmd *cobra.Command, isMandatory bool, propertiesCmdValues *PropertiesCmdValues) {
	flagName := "name"
	var description string
	if isMandatory {
		description = "A mandatory field indicating the name of a property in the namespace."
	} else {
		description = "An optional field indicating the name of a property in the namespace."
	}
	description += "The first character of the name must be in the 'a'-'z' or 'A'-'Z' ranges, " +
		"and following characters can be 'a'-'z', 'A'-'Z', '0'-'9', '.' (period), '-' (dash) or '_' (underscore)"

	cmd.PersistentFlags().StringVarP(&propertiesCmdValues.propertyName, flagName, "n", "", description)

	if isMandatory {
		cmd.MarkPersistentFlagRequired(flagName)
	}
}
