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

type SecretsCmdValues struct {
    name string
}

type SecretsCommand struct {
    cobraCommand *cobra.Command
    values       *SecretsCmdValues
}

// ------------------------------------------------------------------------------------------------
// Constructors
// ------------------------------------------------------------------------------------------------

func NewSecretsCmd(rootCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) (spi.GalasaCommand, error) {
    cmd := new(SecretsCommand)
    err := cmd.init(rootCommand, commsFlagSet)
    return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public functions
// ------------------------------------------------------------------------------------------------

func (cmd *SecretsCommand) Name() string {
    return COMMAND_NAME_SECRETS
}

func (cmd *SecretsCommand) CobraCommand() *cobra.Command {
    return cmd.cobraCommand
}

func (cmd *SecretsCommand) Values() interface{} {
    return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private functions
// ------------------------------------------------------------------------------------------------

func (cmd *SecretsCommand) init(rootCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {

    var err error

    cmd.values = &SecretsCmdValues{}
    cmd.cobraCommand, err = cmd.createCobraCommand(rootCommand, commsFlagSet)

    return err
}

func (cmd *SecretsCommand) createCobraCommand(rootCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) (*cobra.Command, error) {

    var err error

    secretsCobraCmd := &cobra.Command{
        Use:   "secrets",
        Short: "Manage secrets stored in the Galasa service's credentials store",
        Long:  "The parent command for operations to manipulate secrets in the Galasa service's credentials store",
    }

    secretsCobraCmd.PersistentFlags().AddFlagSet(commsFlagSet.Flags())
    rootCommand.CobraCommand().AddCommand(secretsCobraCmd)

    return secretsCobraCmd, err
}

func addSecretNameFlag(cmd *cobra.Command, isMandatory bool, secretsCmdValues *SecretsCmdValues) {

	flagName := "name"
	var description string
	if isMandatory {
		description = "A mandatory flag that identifies the secret to be created or manipulated."
	} else {
		description = "An optional flag that identifies the secret to be retrieved."
	}

	cmd.Flags().StringVar(&secretsCmdValues.name, flagName, "", description)

	if isMandatory {
		cmd.MarkFlagRequired(flagName)
	}
}
