/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"fmt"
	"log"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/secrets"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/spf13/cobra"
)

type SecretsSetCmdValues struct {
    secretType string
    base64Username string
    base64Password string
    base64Token string
    username string
    password string
    token string
	description string
}

type SecretsSetCommand struct {
    values *SecretsSetCmdValues
    cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors methods
// ------------------------------------------------------------------------------------------------
func NewSecretsSetCommand(
    factory spi.Factory,
    secretsSetCommand spi.GalasaCommand,
    commsFlagSet GalasaFlagSet,
) (spi.GalasaCommand, error) {

    cmd := new(SecretsSetCommand)

    err := cmd.init(factory, secretsSetCommand, commsFlagSet)
    return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *SecretsSetCommand) Name() string {
    return COMMAND_NAME_SECRETS_SET
}

func (cmd *SecretsSetCommand) CobraCommand() *cobra.Command {
    return cmd.cobraCommand
}

func (cmd *SecretsSetCommand) Values() interface{} {
    return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------
func (cmd *SecretsSetCommand) init(factory spi.Factory, secretsCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {
    var err error

    cmd.values = &SecretsSetCmdValues{}
    cmd.cobraCommand, err = cmd.createCobraCmd(factory, secretsCommand, commsFlagSet.Values().(*CommsFlagSetValues))

    return err
}

func (cmd *SecretsSetCommand) createCobraCmd(
    factory spi.Factory,
    secretsCommand spi.GalasaCommand,
    commsFlagSetValues *CommsFlagSetValues,
) (*cobra.Command, error) {

    var err error

    secretsCommandValues := secretsCommand.Values().(*SecretsCmdValues)
    secretsSetCobraCmd := &cobra.Command{
        Use:     "set",
        Short:   "Creates or updates a secret in the credentials store",
        Long:    "Creates or updates a secret in the credentials store",
        Aliases: []string{COMMAND_NAME_SECRETS_SET},
        RunE: func(cobraCommand *cobra.Command, args []string) error {
            return cmd.executeSecretsSet(factory, secretsCommand.Values().(*SecretsCmdValues), commsFlagSetValues)
        },
    }

    addSecretNameFlag(secretsSetCobraCmd, true, secretsCommandValues)

    usernameFlag := "username"
    passwordFlag := "password"
    tokenFlag := "token"

    base64UsernameFlag := "base64-username"
    base64PasswordFlag := "base64-password"
    base64TokenFlag := "base64-token"

	descriptionFlag := "description"

    secretsSetCobraCmd.Flags().StringVar(&cmd.values.secretType, "type", "", fmt.Sprintf("the desired secret type to convert an existing secret into. Supported types are: %v.", galasaapi.AllowedGalasaSecretTypeEnumValues))
    secretsSetCobraCmd.Flags().StringVar(&cmd.values.description, descriptionFlag, "", "the description to associate with the secret being created or updated")
    secretsSetCobraCmd.Flags().StringVar(&cmd.values.username, usernameFlag, "", "a username to set into a secret")
    secretsSetCobraCmd.Flags().StringVar(&cmd.values.password, passwordFlag, "", "a password to set into a secret")
    secretsSetCobraCmd.Flags().StringVar(&cmd.values.token, tokenFlag, "", "a token to set into a secret")

    secretsSetCobraCmd.Flags().StringVar(&cmd.values.base64Username, base64UsernameFlag, "", "a base64-encoded username to set into a secret")
    secretsSetCobraCmd.Flags().StringVar(&cmd.values.base64Password, base64PasswordFlag, "", "a base64-encoded password to set into a secret")
    secretsSetCobraCmd.Flags().StringVar(&cmd.values.base64Token, base64TokenFlag, "", "a base64-encoded token to set into a secret")

    // A non-encoded credential cannot be provided alongside an encoded credential
    secretsSetCobraCmd.MarkFlagsMutuallyExclusive(usernameFlag, base64UsernameFlag)

    // A password cannot be provided alongside a token (there is no secret type that allows both)
    secretsSetCobraCmd.MarkFlagsMutuallyExclusive(passwordFlag, tokenFlag, base64PasswordFlag, base64TokenFlag)

	// A secret must have a name and at least one of the credentials flags
	secretsSetCobraCmd.MarkFlagsOneRequired(
		usernameFlag,
		passwordFlag,
		tokenFlag,
		base64UsernameFlag,
		base64PasswordFlag,
		base64TokenFlag,
		descriptionFlag,
	)

    secretsCommand.CobraCommand().AddCommand(secretsSetCobraCmd)

    return secretsSetCobraCmd, err
}

func (cmd *SecretsSetCommand) executeSecretsSet(
    factory spi.Factory,
    secretsCmdValues *SecretsCmdValues,
    commsFlagSetValues *CommsFlagSetValues,
) error {

    var err error
    // Operations on the file system will all be relative to the current folder.
    fileSystem := factory.GetFileSystem()

	err = utils.CaptureLog(fileSystem, commsFlagSetValues.logFileName)
	if err == nil {
        commsFlagSetValues.isCapturingLogs = true
    
        log.Println("Galasa CLI - Set secrets from the ecosystem")
    
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
                byteReader := factory.GetByteReader()

                setSecretFunc := func(apiClient *galasaapi.APIClient) error {
                    return secrets.SetSecret(
                        secretsCmdValues.name,
                        cmd.values.username,
                        cmd.values.password,
                        cmd.values.token,
                        cmd.values.base64Username,
                        cmd.values.base64Password,
                        cmd.values.base64Token,
                        cmd.values.secretType,
                        cmd.values.description,
                        console,
                        apiClient,
                        byteReader,
                    )
                }
                err = commsClient.RunAuthenticatedCommandWithRateLimitRetries(setSecretFunc)
            }
        }
    }

    return err
}
