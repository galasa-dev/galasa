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

type TagsCmdValues struct {
    name string
}

type TagsCommand struct {
    cobraCommand *cobra.Command
    values       *TagsCmdValues
}

// ------------------------------------------------------------------------------------------------
// Constructors
// ------------------------------------------------------------------------------------------------

func NewTagsCmd(rootCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) (spi.GalasaCommand, error) {
    cmd := new(TagsCommand)
    err := cmd.init(rootCommand, commsFlagSet)
    return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public functions
// ------------------------------------------------------------------------------------------------

func (cmd *TagsCommand) Name() string {
    return COMMAND_NAME_TAGS
}

func (cmd *TagsCommand) CobraCommand() *cobra.Command {
    return cmd.cobraCommand
}

func (cmd *TagsCommand) Values() interface{} {
    return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private functions
// ------------------------------------------------------------------------------------------------

func (cmd *TagsCommand) init(rootCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {

    var err error

    cmd.values = &TagsCmdValues{}
    cmd.cobraCommand, err = cmd.createCobraCommand(rootCommand, commsFlagSet)

    return err
}

func (cmd *TagsCommand) createCobraCommand(rootCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) (*cobra.Command, error) {

    var err error

    tagsCobraCmd := &cobra.Command{
        Use:   "tags",
        Short: "Manage tags stored in the Galasa service's configuration property store",
        Long:  "The parent command for operations to manipulate tags in the Galasa service's configuration property store",
    }

    tagsCobraCmd.PersistentFlags().AddFlagSet(commsFlagSet.Flags())
    rootCommand.CobraCommand().AddCommand(tagsCobraCmd)

    return tagsCobraCmd, err
}

func addTagNameFlag(cmd *cobra.Command, isMandatory bool, tagsCmdValues *TagsCmdValues) {

	flagName := "name"
	var description string
	if isMandatory {
		description = "A mandatory flag that indicates the name of a tag."
	} else {
		description = "An optional flag that indicates the name of a tag."
	}

	cmd.Flags().StringVar(&tagsCmdValues.name, flagName, "", description)

	if isMandatory {
		cmd.MarkFlagRequired(flagName)
	}
}
