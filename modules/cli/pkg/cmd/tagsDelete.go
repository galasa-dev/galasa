/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"log"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/tags"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/spf13/cobra"
)

type TagsDeleteCommand struct {
    cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors methods
// ------------------------------------------------------------------------------------------------
func NewTagsDeleteCommand(
    factory spi.Factory,
    tagsDeleteCommand spi.GalasaCommand,
    commsFlagSet GalasaFlagSet,
) (spi.GalasaCommand, error) {

    cmd := new(TagsDeleteCommand)

    err := cmd.init(factory, tagsDeleteCommand, commsFlagSet)
    return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *TagsDeleteCommand) Name() string {
    return COMMAND_NAME_TAGS_DELETE
}

func (cmd *TagsDeleteCommand) CobraCommand() *cobra.Command {
    return cmd.cobraCommand
}

func (cmd *TagsDeleteCommand) Values() interface{} {
    return nil
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------
func (cmd *TagsDeleteCommand) init(factory spi.Factory, tagsCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {
    var err error

    cmd.cobraCommand, err = cmd.createCobraCmd(factory, tagsCommand, commsFlagSet.Values().(*CommsFlagSetValues))

    return err
}

func (cmd *TagsDeleteCommand) createCobraCmd(
    factory spi.Factory,
    tagsCommand spi.GalasaCommand,
    commsFlagSetValues *CommsFlagSetValues,
) (*cobra.Command, error) {

    var err error

    tagsCommandValues := tagsCommand.Values().(*TagsCmdValues)
    tagsDeleteCobraCmd := &cobra.Command{
        Use:     "delete",
        Short:   "Deletes a tag from the Galasa service",
        Long:    "Deletes a tag from the Galasa service",
        Aliases: []string{COMMAND_NAME_TAGS_DELETE},
        RunE: func(cobraCommand *cobra.Command, args []string) error {
			return cmd.executeTagsDelete(factory, tagsCommand.Values().(*TagsCmdValues), commsFlagSetValues)
        },
    }

    addTagNameFlag(tagsDeleteCobraCmd, true, tagsCommandValues)

    tagsCommand.CobraCommand().AddCommand(tagsDeleteCobraCmd)

    return tagsDeleteCobraCmd, err
}

func (cmd *TagsDeleteCommand) executeTagsDelete(
    factory spi.Factory,
    tagsCmdValues *TagsCmdValues,
    commsFlagSetValues *CommsFlagSetValues,
) error {

    var err error
    // Operations on the file system will all be relative to the current folder.
    fileSystem := factory.GetFileSystem()

	err = utils.CaptureLog(fileSystem, commsFlagSetValues.logFileName)
	if err == nil {
		commsFlagSetValues.isCapturingLogs = true

		log.Println("Galasa CLI - Delete a tag from the Galasa service")

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
                err = tags.DeleteTag(tagsCmdValues.name, commsClient, byteReader)
			}
		}
	}
    return err
}
