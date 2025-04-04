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
	"github.com/galasa-dev/cli/pkg/monitors"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/spf13/cobra"
)

type MonitorsSetCmdValues struct {
	isEnabledStr string
}

type MonitorsSetCommand struct {
	values *MonitorsSetCmdValues
    cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors methods
// ------------------------------------------------------------------------------------------------
func NewMonitorsSetCommand(
    factory spi.Factory,
    monitorsSetCommand spi.GalasaCommand,
    commsFlagSet GalasaFlagSet,
) (spi.GalasaCommand, error) {

    cmd := new(MonitorsSetCommand)

    err := cmd.init(factory, monitorsSetCommand, commsFlagSet)
    return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *MonitorsSetCommand) Name() string {
    return COMMAND_NAME_MONITORS_SET
}

func (cmd *MonitorsSetCommand) CobraCommand() *cobra.Command {
    return cmd.cobraCommand
}

func (cmd *MonitorsSetCommand) Values() interface{} {
	return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------
func (cmd *MonitorsSetCommand) init(factory spi.Factory, monitorsCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {
    var err error

	cmd.values = &MonitorsSetCmdValues{}
    cmd.cobraCommand, err = cmd.createCobraCmd(factory, monitorsCommand, commsFlagSet.Values().(*CommsFlagSetValues))

    return err
}

func (cmd *MonitorsSetCommand) createCobraCmd(
    factory spi.Factory,
    monitorsCommand spi.GalasaCommand,
    commsFlagSetValues *CommsFlagSetValues,
) (*cobra.Command, error) {

    var err error

    monitorsCommandValues := monitorsCommand.Values().(*MonitorsCmdValues)
    monitorsSetCobraCmd := &cobra.Command{
        Use:     "set",
        Short:   "Update a monitor in the Galasa service",
        Long:    "Updates a monitor with the given name in the Galasa service",
        Aliases: []string{COMMAND_NAME_MONITORS_SET},
        RunE: func(cobraCommand *cobra.Command, args []string) error {
			return cmd.executeMonitorsSet(factory, monitorsCommand.Values().(*MonitorsCmdValues), commsFlagSetValues)
        },
    }

	addMonitorNameFlag(monitorsSetCobraCmd, true, monitorsCommandValues)
	isEnabledFlag := "is-enabled"

	monitorsSetCobraCmd.Flags().StringVar(&cmd.values.isEnabledStr, isEnabledFlag, "", "A boolean flag that determines whether the given monitor should be enabled or disabled. Supported values are 'true' and 'false'.")

	monitorsSetCobraCmd.MarkFlagsOneRequired(
		isEnabledFlag,
	)

    monitorsCommand.CobraCommand().AddCommand(monitorsSetCobraCmd)

    return monitorsSetCobraCmd, err
}

func (cmd *MonitorsSetCommand) executeMonitorsSet(
    factory spi.Factory,
    monitorsCmdValues *MonitorsCmdValues,
    commsFlagSetValues *CommsFlagSetValues,
) error {

    var err error
    // Operations on the file system will all be relative to the current folder.
    fileSystem := factory.GetFileSystem()

	err = utils.CaptureLog(fileSystem, commsFlagSetValues.logFileName)
	if err == nil {
		commsFlagSetValues.isCapturingLogs = true

		log.Println("Galasa CLI - Update monitors in the Galasa service")

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

				setMonitorsFunc := func(apiClient *galasaapi.APIClient) error {
					return monitors.SetMonitor(
						monitorsCmdValues.name,
						cmd.values.isEnabledStr,
						apiClient,
						byteReader,
					)
				}
				err = commsClient.RunAuthenticatedCommandWithRateLimitRetries(setMonitorsFunc)
			}
		}
	}

    return err
}
