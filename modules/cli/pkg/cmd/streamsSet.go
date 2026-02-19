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
	"github.com/galasa-dev/cli/pkg/streams"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/spf13/cobra"
)

type StreamsSetCmdValues struct {
	description string
    testCatalogUrl string
    mavenRepositoryUrl string
    obrs []string
}


type StreamsSetCommand struct {
    values *StreamsSetCmdValues
    cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors methods
// ------------------------------------------------------------------------------------------------
func NewStreamsSetCommand(
    factory spi.Factory,
    streamsSetCommand spi.GalasaCommand,
    commsFlagSet GalasaFlagSet,
) (spi.GalasaCommand, error) {

    cmd := new(StreamsSetCommand)

    err := cmd.init(factory, streamsSetCommand, commsFlagSet)
    return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *StreamsSetCommand) Name() string {
    return COMMAND_NAME_STREAMS_SET
}

func (cmd *StreamsSetCommand) CobraCommand() *cobra.Command {
    return cmd.cobraCommand
}

func (cmd *StreamsSetCommand) Values() interface{} {
    return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------
func (cmd *StreamsSetCommand) init(factory spi.Factory, streamsCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {
    var err error

    cmd.values = &StreamsSetCmdValues{}
    cmd.cobraCommand, err = cmd.createCobraCmd(factory, streamsCommand, commsFlagSet.Values().(*CommsFlagSetValues))

    return err
}

func (cmd *StreamsSetCommand) createCobraCmd(
    factory spi.Factory,
    streamsCommand spi.GalasaCommand,
    commsFlagSetValues *CommsFlagSetValues,
) (*cobra.Command, error) {

    var err error

    streamsCommandValues := streamsCommand.Values().(*StreamsCmdValues)
    streamsSetCobraCmd := &cobra.Command{
        Use:     "set",
        Short:   "Creates or updates a test stream in the Galasa service",
        Long:    "Creates or updates a test stream in the Galasa service",
        Aliases: []string{COMMAND_NAME_STREAMS_SET},
        RunE: func(cobraCommand *cobra.Command, args []string) error {
			return cmd.executeStreamsSet(factory, streamsCommand.Values().(*StreamsCmdValues), commsFlagSetValues)
        },
    }

    addStreamNameFlag(streamsSetCobraCmd, true, streamsCommandValues)

    descriptionFlag := "description"
    testCatalogUrlFlag := "testcatalog-url"
    mavenRepoUrlFlag := "maven-repo-url"
    obrFlag := "obr"

    streamsSetCobraCmd.Flags().StringVar(&cmd.values.description, descriptionFlag, "", "the description to associate with the test stream being created or updated")
    streamsSetCobraCmd.Flags().StringVar(&cmd.values.testCatalogUrl, testCatalogUrlFlag, "", "the URL to the test catalog for the test stream being created or updated. For example: https://my-maven-repository/path/to/testcatalog.json")
    streamsSetCobraCmd.Flags().StringVar(&cmd.values.mavenRepositoryUrl, mavenRepoUrlFlag, "", "the URL to the Maven repository containing test material for the test stream to use. For example: https://my-maven-repository")
    streamsSetCobraCmd.Flags().StringSliceVar(&cmd.values.obrs, obrFlag, make([]string, 0), "The maven coordinates of the OBR bundle(s) which refer to your test bundles. The format of this parameter is 'mvn:{OBR_GROUP_ID}/{OBR_ARTIFACT_ID}/{OBR_VERSION}/obr'. "+
        "Multiple instances of this flag can be used to describe multiple OBR bundles.")

    // streams set requires the name flag as well as one of the following: --testcatalog, --maven-repo-url, --obr
	streamsSetCobraCmd.MarkFlagsOneRequired(testCatalogUrlFlag, mavenRepoUrlFlag, obrFlag)

    streamsCommand.CobraCommand().AddCommand(streamsSetCobraCmd)

    return streamsSetCobraCmd, err
}

func (cmd *StreamsSetCommand) executeStreamsSet(
    factory spi.Factory,
    streamsCmdValues *StreamsCmdValues,
    commsFlagSetValues *CommsFlagSetValues,
) error {

    var err error

    // Operations on the file system will all be relative to the current folder.
    fileSystem := factory.GetFileSystem()

	err = utils.CaptureLog(fileSystem, commsFlagSetValues.logFileName)
	if err == nil {
		commsFlagSetValues.isCapturingLogs = true

		log.Println("Galasa CLI - Set a test stream in the Galasa service")

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
				byteReader := factory.GetByteReader()

                setStreamFunc := func(apiClient *galasaapi.APIClient) error {
                    return streams.SetStream(
                        streamsCmdValues.name,
                        cmd.values.description,
                        cmd.values.mavenRepositoryUrl,
                        cmd.values.testCatalogUrl,
                        cmd.values.obrs,
                        apiClient,
                        byteReader,
                    )
                }
                err = commsClient.RunAuthenticatedCommandWithRateLimitRetries(setStreamFunc)
			}
		}
	}
    return err
}
