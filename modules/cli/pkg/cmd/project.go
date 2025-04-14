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

type ProjectCommand struct {
	cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors
// ------------------------------------------------------------------------------------------------
func NewProjectCmd(rootCmd spi.GalasaCommand) (spi.GalasaCommand, error) {
	cmd := new(ProjectCommand)
	err := cmd.init(rootCmd)
	return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *ProjectCommand) Name() string {
	return COMMAND_NAME_PROJECT
}

func (cmd *ProjectCommand) CobraCommand() *cobra.Command {
	return cmd.cobraCommand
}

func (cmd *ProjectCommand) Values() interface{} {
	return nil
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------

func (cmd *ProjectCommand) init(rootCommand spi.GalasaCommand) error {
	var err error
	cmd.cobraCommand = cmd.createProjectCobraCommand(rootCommand)
	return err
}

func (cmd *ProjectCommand) createProjectCobraCommand(rootCmd spi.GalasaCommand) *cobra.Command {

	projectCmd := &cobra.Command{
		Use:   "project",
		Short: "Manipulate local project source code",
		Long:  "Creates and manipulates Galasa test project source code",
	}

	rootCmd.CobraCommand().AddCommand(projectCmd)

	return projectCmd
}
