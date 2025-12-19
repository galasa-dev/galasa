/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"log"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/tags"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/spf13/cobra"
)

type TagsGetCmdValues struct {
	outputFormat string
}


type TagsGetCommand struct {
    values *TagsGetCmdValues
    cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors methods
// ------------------------------------------------------------------------------------------------
func NewTagsGetCommand(
    factory spi.Factory,
    tagsGetCommand spi.GalasaCommand,
    commsFlagGet GalasaFlagSet,
) (spi.GalasaCommand, error) {

    cmd := new(TagsGetCommand)

    err := cmd.init(factory, tagsGetCommand, commsFlagGet)
    return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *TagsGetCommand) Name() string {
    return COMMAND_NAME_TAGS_GET
}

func (cmd *TagsGetCommand) CobraCommand() *cobra.Command {
    return cmd.cobraCommand
}

func (cmd *TagsGetCommand) Values() any {
    return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------
func (cmd *TagsGetCommand) init(factory spi.Factory, tagsCommand spi.GalasaCommand, commsFlagGet GalasaFlagSet) error {
    var err error

    cmd.values = &TagsGetCmdValues{}
    cmd.cobraCommand, err = cmd.createCobraCmd(factory, tagsCommand, commsFlagGet.Values().(*CommsFlagSetValues))

    return err
}

func (cmd *TagsGetCommand) createCobraCmd(
    factory spi.Factory,
    tagsCommand spi.GalasaCommand,
    commsFlagGetValues *CommsFlagSetValues,
) (*cobra.Command, error) {

    var err error

    tagsCommandValues := tagsCommand.Values().(*TagsCmdValues)
    tagsGetCobraCmd := &cobra.Command{
        Use:     "get",
        Short:   "Gets tags from the Galasa service",
        Long:    "Gets tags from the Galasa service",
        Aliases: []string{COMMAND_NAME_TAGS_GET},
        RunE: func(cobraCommand *cobra.Command, args []string) error {
			return cmd.executeTagsGet(factory, tagsCommand.Values().(*TagsCmdValues), commsFlagGetValues)
        },
    }

    addTagNameFlag(tagsGetCobraCmd, false, tagsCommandValues)

    formatters := tags.GetFormatterNamesAsString()
    tagsGetCobraCmd.Flags().StringVar(&cmd.values.outputFormat, "format", "summary", "output format for the data returned. Supported formats are: "+formatters+".")

    tagsCommand.CobraCommand().AddCommand(tagsGetCobraCmd)

    return tagsGetCobraCmd, err
}

func (cmd *TagsGetCommand) executeTagsGet(
    factory spi.Factory,
    tagsCmdValues *TagsCmdValues,
    commsFlagGetValues *CommsFlagSetValues,
) error {

    var err error
    // Operations on the file system will all be relative to the current folder.
    fileSystem := factory.GetFileSystem()

	err = utils.CaptureLog(fileSystem, commsFlagGetValues.logFileName)
	if err == nil {
		commsFlagGetValues.isCapturingLogs = true

		log.Println("Galasa CLI - Get a tag from the Galasa service")

		env := factory.GetEnvironment()

		var galasaHome spi.GalasaHome
		galasaHome, err = utils.NewGalasaHome(fileSystem, env, commsFlagGetValues.CmdParamGalasaHomePath)
		if err == nil {

			var commsClient api.APICommsClient
			commsClient, err = api.NewAPICommsClient(
				commsFlagGetValues.bootstrap,
				commsFlagGetValues.maxRetries,
				commsFlagGetValues.retryBackoffSeconds,
				factory,
				galasaHome,
			)

			if err == nil {
				byteReader := factory.GetByteReader()
                console := factory.GetStdOutConsole()

                err = tags.GetTag(tagsCmdValues.name, cmd.values.outputFormat, commsClient, console, byteReader)
			}
		}
	}
    return err
}
