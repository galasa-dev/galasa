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

type MonitorsGetCmdValues struct {
	outputFormat string
}

type MonitorsGetCommand struct {
	values *MonitorsGetCmdValues
    cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors methods
// ------------------------------------------------------------------------------------------------
func NewMonitorsGetCommand(
    factory spi.Factory,
    monitorsGetCommand spi.GalasaCommand,
    commsFlagSet GalasaFlagSet,
) (spi.GalasaCommand, error) {

    cmd := new(MonitorsGetCommand)

    err := cmd.init(factory, monitorsGetCommand, commsFlagSet)
    return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *MonitorsGetCommand) Name() string {
    return COMMAND_NAME_MONITORS_GET
}

func (cmd *MonitorsGetCommand) CobraCommand() *cobra.Command {
    return cmd.cobraCommand
}

func (cmd *MonitorsGetCommand) Values() interface{} {
	return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------
func (cmd *MonitorsGetCommand) init(factory spi.Factory, monitorsCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {
    var err error

	cmd.values = &MonitorsGetCmdValues{}
    cmd.cobraCommand, err = cmd.createCobraCmd(factory, monitorsCommand, commsFlagSet.Values().(*CommsFlagSetValues))

    return err
}

func (cmd *MonitorsGetCommand) createCobraCmd(
    factory spi.Factory,
    monitorsCommand spi.GalasaCommand,
    commsFlagSetValues *CommsFlagSetValues,
) (*cobra.Command, error) {

    var err error

    monitorsCommandValues := monitorsCommand.Values().(*MonitorsCmdValues)
    monitorsGetCobraCmd := &cobra.Command{
        Use:     "get",
        Short:   "Get monitors from the Galasa service",
        Long:    "Get a list of monitors or a specific monitor from the Galasa service",
        Aliases: []string{COMMAND_NAME_MONITORS_GET},
        RunE: func(cobraCommand *cobra.Command, args []string) error {
			return cmd.executeMonitorsGet(factory, monitorsCommand.Values().(*MonitorsCmdValues), commsFlagSetValues)
        },
    }

    addMonitorNameFlag(monitorsGetCobraCmd, false, monitorsCommandValues)

	formatters := monitors.GetFormatterNamesAsString()
	monitorsGetCobraCmd.Flags().StringVar(&cmd.values.outputFormat, "format", "summary", "the output format of the returned monitors. Supported formats are: "+formatters+".")

    monitorsCommand.CobraCommand().AddCommand(monitorsGetCobraCmd)

    return monitorsGetCobraCmd, err
}

func (cmd *MonitorsGetCommand) executeMonitorsGet(
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
	
		log.Println("Galasa CLI - Get monitors from the Galasa service")
	
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

				getMonitorsFunc := func(apiClient *galasaapi.APIClient) error {
					return monitors.GetMonitors(monitorsCmdValues.name, cmd.values.outputFormat, console, apiClient, byteReader)
				}
				err = commsClient.RunAuthenticatedCommandWithRateLimitRetries(getMonitorsFunc)
			}
		}
	}

    return err
}
