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
	"github.com/galasa-dev/cli/pkg/tags"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/spf13/cobra"
)

type TagsSetCmdValues struct {
	description string
	priority int
}


type TagsSetCommand struct {
    values *TagsSetCmdValues
    cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors methods
// ------------------------------------------------------------------------------------------------
func NewTagsSetCommand(
    factory spi.Factory,
    tagsSetCommand spi.GalasaCommand,
    commsFlagSet GalasaFlagSet,
) (spi.GalasaCommand, error) {

    cmd := new(TagsSetCommand)

    err := cmd.init(factory, tagsSetCommand, commsFlagSet)
    return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *TagsSetCommand) Name() string {
    return COMMAND_NAME_TAGS_SET
}

func (cmd *TagsSetCommand) CobraCommand() *cobra.Command {
    return cmd.cobraCommand
}

func (cmd *TagsSetCommand) Values() interface{} {
    return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------
func (cmd *TagsSetCommand) init(factory spi.Factory, tagsCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {
    var err error

    cmd.values = &TagsSetCmdValues{}
    cmd.cobraCommand, err = cmd.createCobraCmd(factory, tagsCommand, commsFlagSet.Values().(*CommsFlagSetValues))

    return err
}

func (cmd *TagsSetCommand) createCobraCmd(
    factory spi.Factory,
    tagsCommand spi.GalasaCommand,
    commsFlagSetValues *CommsFlagSetValues,
) (*cobra.Command, error) {

    var err error

    tagsCommandValues := tagsCommand.Values().(*TagsCmdValues)
    tagsSetCobraCmd := &cobra.Command{
        Use:     "set",
        Short:   "Creates or updates a tag in the Galasa service",
        Long:    "Creates or updates a tag in the Galasa service",
        Aliases: []string{COMMAND_NAME_TAGS_SET},
        RunE: func(cobraCommand *cobra.Command, args []string) error {
			return cmd.executeTagsSet(factory, tagsCommand.Values().(*TagsCmdValues), commsFlagSetValues)
        },
    }

    addTagNameFlag(tagsSetCobraCmd, true, tagsCommandValues)

    descriptionFlag := "description"
    priorityFlag := "priority"

    tagsSetCobraCmd.Flags().StringVar(&cmd.values.description, descriptionFlag, "", "the description to associate with the tag being created or updated")
    tagsSetCobraCmd.Flags().IntVar(&cmd.values.priority, priorityFlag, 0, "the priority to set for the tag being created or updated")

	// A tag must have a name and at least one of the other flags
	tagsSetCobraCmd.MarkFlagsOneRequired(
		descriptionFlag,
        priorityFlag,
	)

    tagsCommand.CobraCommand().AddCommand(tagsSetCobraCmd)

    return tagsSetCobraCmd, err
}

func (cmd *TagsSetCommand) executeTagsSet(
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

		log.Println("Galasa CLI - Set a tag in the Galasa service")

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

            // If the user did not explicitly pass --priority, treat it as "empty"
            // We can't use 0 as the "empty" value because 0 is a valid value and
            // we want to allow users to set priority to 0.
            if !cmd.cobraCommand.Flags().Changed("priority") {
                cmd.values.priority = tags.DEFAULT_EMPTY_PRIORITY
            }

			if err == nil {
				byteReader := factory.GetByteReader()

                setTagFunc := func(apiClient *galasaapi.APIClient) error {
                    return tags.SetTag(tagsCmdValues.name, cmd.values.description, cmd.values.priority, apiClient, byteReader)
                }
                err = commsClient.RunAuthenticatedCommandWithRateLimitRetries(setTagFunc)
			}
		}
	}
    return err
}
