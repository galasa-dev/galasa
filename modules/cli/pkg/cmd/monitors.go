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

type MonitorsCmdValues struct {
    name string
}

type MonitorsCommand struct {
    cobraCommand *cobra.Command
    values       *MonitorsCmdValues
}

// ------------------------------------------------------------------------------------------------
// Constructors
// ------------------------------------------------------------------------------------------------

func NewMonitorsCmd(rootCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) (spi.GalasaCommand, error) {
    cmd := new(MonitorsCommand)
    err := cmd.init(rootCommand, commsFlagSet)
    return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public functions
// ------------------------------------------------------------------------------------------------

func (cmd *MonitorsCommand) Name() string {
    return COMMAND_NAME_MONITORS
}

func (cmd *MonitorsCommand) CobraCommand() *cobra.Command {
    return cmd.cobraCommand
}

func (cmd *MonitorsCommand) Values() interface{} {
    return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private functions
// ------------------------------------------------------------------------------------------------

func (cmd *MonitorsCommand) init(rootCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {

    var err error

    cmd.values = &MonitorsCmdValues{}
    cmd.cobraCommand, err = cmd.createCobraCommand(rootCommand, commsFlagSet)

    return err
}

func (cmd *MonitorsCommand) createCobraCommand(rootCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) (*cobra.Command, error) {

    var err error

    monitorsCobraCmd := &cobra.Command{
        Use:   "monitors",
        Short: "Manage monitors in the Galasa service",
        Long:  "The parent command for operations to manipulate monitors in the Galasa service",
    }

    monitorsCobraCmd.PersistentFlags().AddFlagSet(commsFlagSet.Flags())
    rootCommand.CobraCommand().AddCommand(monitorsCobraCmd)

    return monitorsCobraCmd, err
}

func addMonitorNameFlag(cmd *cobra.Command, isMandatory bool, monitorsCmdValues *MonitorsCmdValues) {

	flagName := "name"
	var description string
	if isMandatory {
		description = "A mandatory flag that identifies the monitor to be manipulated by name."
	} else {
		description = "An optional flag that identifies the monitor to be retrieved by name."
	}

	cmd.Flags().StringVar(&monitorsCmdValues.name, flagName, "", description)

	if isMandatory {
		cmd.MarkFlagRequired(flagName)
	}
}
