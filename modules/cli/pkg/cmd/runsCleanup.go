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

type RunsCleanupCmdValues struct {
}

type RunsCleanupCommand struct {
    cobraCommand *cobra.Command
    values       *RunsCleanupCmdValues
}

// ------------------------------------------------------------------------------------------------
// Constructors
// ------------------------------------------------------------------------------------------------

func NewRunsCleanupCmd(runsCommand spi.GalasaCommand) (spi.GalasaCommand, error) {
    cmd := new(RunsCleanupCommand)
    err := cmd.init(runsCommand)
    return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public functions
// ------------------------------------------------------------------------------------------------

func (cmd *RunsCleanupCommand) Name() string {
    return COMMAND_NAME_RUNS_CLEANUP
}

func (cmd *RunsCleanupCommand) CobraCommand() *cobra.Command {
    return cmd.cobraCommand
}

func (cmd *RunsCleanupCommand) Values() any {
    return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private functions
// ------------------------------------------------------------------------------------------------

func (cmd *RunsCleanupCommand) init(runsCommand spi.GalasaCommand) error {

    var err error

    cmd.values = &RunsCleanupCmdValues{}
    cmd.cobraCommand, err = cmd.createCobraCommand(runsCommand)

    return err
}

func (cmd *RunsCleanupCommand) createCobraCommand(runsCommand spi.GalasaCommand) (*cobra.Command, error) {

    var err error

    runsCleanupCobraCmd := &cobra.Command{
        Use:   "cleanup",
        Short: "Run resource cleanup jobs for resources provisioned by Galasa",
        Long:  "The parent command for operations to run cleanup jobs for resources provisioned by Galasa",
    }

    runsCommand.CobraCommand().AddCommand(runsCleanupCobraCmd)

    return runsCleanupCobraCmd, err
}

