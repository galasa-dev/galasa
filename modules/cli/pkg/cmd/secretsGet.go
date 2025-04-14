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
	"github.com/galasa-dev/cli/pkg/secrets"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/spf13/cobra"
)

type SecretsGetCmdValues struct {
	outputFormat string
}

type SecretsGetCommand struct {
	values *SecretsGetCmdValues
    cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors methods
// ------------------------------------------------------------------------------------------------
func NewSecretsGetCommand(
    factory spi.Factory,
    secretsGetCommand spi.GalasaCommand,
    commsFlagSet GalasaFlagSet,
) (spi.GalasaCommand, error) {

    cmd := new(SecretsGetCommand)

    err := cmd.init(factory, secretsGetCommand, commsFlagSet)
    return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *SecretsGetCommand) Name() string {
    return COMMAND_NAME_SECRETS_GET
}

func (cmd *SecretsGetCommand) CobraCommand() *cobra.Command {
    return cmd.cobraCommand
}

func (cmd *SecretsGetCommand) Values() interface{} {
	return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------
func (cmd *SecretsGetCommand) init(factory spi.Factory, secretsCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {
    var err error

	cmd.values = &SecretsGetCmdValues{}
    cmd.cobraCommand, err = cmd.createCobraCmd(factory, secretsCommand, commsFlagSet.Values().(*CommsFlagSetValues))

    return err
}

func (cmd *SecretsGetCommand) createCobraCmd(
    factory spi.Factory,
    secretsCommand spi.GalasaCommand,
    commsFlagSetValues *CommsFlagSetValues,
) (*cobra.Command, error) {

    var err error

    secretsCommandValues := secretsCommand.Values().(*SecretsCmdValues)
    secretsGetCobraCmd := &cobra.Command{
        Use:     "get",
        Short:   "Get secrets from the credentials store",
        Long:    "Get a list of secrets or a specific secret from the credentials store",
        Aliases: []string{COMMAND_NAME_SECRETS_GET},
        RunE: func(cobraCommand *cobra.Command, args []string) error {
			return cmd.executeSecretsGet(factory, secretsCommand.Values().(*SecretsCmdValues), commsFlagSetValues)
        },
    }

    addSecretNameFlag(secretsGetCobraCmd, false, secretsCommandValues)

	formatters := secrets.GetFormatterNamesAsString()
	secretsGetCobraCmd.Flags().StringVar(&cmd.values.outputFormat, "format", "summary", "the output format of the returned secrets. Supported formats are: "+formatters+".")

    secretsCommand.CobraCommand().AddCommand(secretsGetCobraCmd)

    return secretsGetCobraCmd, err
}

func (cmd *SecretsGetCommand) executeSecretsGet(
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
	
		log.Println("Galasa CLI - Get secrets from the ecosystem")
	
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

				getSecretsFunc := func(apiClient *galasaapi.APIClient) error {
					return secrets.GetSecrets(secretsCmdValues.name, cmd.values.outputFormat, console, apiClient, byteReader)
				}
				err = commsClient.RunAuthenticatedCommandWithRateLimitRetries(getSecretsFunc)
			}
		}
	}

    return err
}
