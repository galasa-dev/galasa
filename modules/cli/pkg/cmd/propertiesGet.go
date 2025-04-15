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
	"github.com/galasa-dev/cli/pkg/properties"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/spf13/cobra"
)

//Objective: Allow user to do this:
//	properties get --namespace "framework" --prefix "pro" --sufix "ty"
//  And then display all properties filtered by either prefix, suffix or both, or empty if not found
//OR
//	properties get --namespace "framework" --name "hello"
//  And then display value of specified property or return empty if not found

// Variables set by cobra's command-line parsing.
type PropertiesGetCmdValues struct {
	propertiesPrefix       string
	propertiesSuffix       string
	propertiesInfix        string
	propertiesOutputFormat string
}

type PropertiesGetCommand struct {
	values       *PropertiesGetCmdValues
	cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors methods
// ------------------------------------------------------------------------------------------------
func NewPropertiesGetCommand(factory spi.Factory, propertiesCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) (spi.GalasaCommand, error) {

	cmd := new(PropertiesGetCommand)
	err := cmd.init(factory, propertiesCommand, commsFlagSet)
	return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *PropertiesGetCommand) Name() string {
	return COMMAND_NAME_PROPERTIES_GET
}

func (cmd *PropertiesGetCommand) CobraCommand() *cobra.Command {
	return cmd.cobraCommand
}

func (cmd *PropertiesGetCommand) Values() interface{} {
	return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------

func (cmd *PropertiesGetCommand) init(factory spi.Factory, propertiesCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {

	var err error

	cmd.values = &PropertiesGetCmdValues{}
	cmd.cobraCommand = cmd.createCobraCommand(factory, propertiesCommand, commsFlagSet.Values().(*CommsFlagSetValues))

	return err
}

func (cmd *PropertiesGetCommand) createCobraCommand(
	factory spi.Factory,
	propertiesCommand spi.GalasaCommand,
	commsFlagSetValues *CommsFlagSetValues,
) *cobra.Command {

	propertiesCommandValues := propertiesCommand.Values().(*PropertiesCmdValues)
	propertiesGetCobraCmd := &cobra.Command{
		Use:     "get",
		Short:   "Get the details of properties in a namespace.",
		Long:    "Get the details of all properties in a namespace, filtered with flags if present",
		Args:    cobra.NoArgs,
		Aliases: []string{"properties get"},
		RunE: func(cobraCmd *cobra.Command, args []string) error {
			return cmd.executePropertiesGet(factory, propertiesCommandValues, commsFlagSetValues)
		},
	}

	propertiesHasYamlFormat := true
	formatters := properties.GetFormatterNamesString(properties.CreateFormatters(propertiesHasYamlFormat))
	propertiesGetCobraCmd.PersistentFlags().StringVar(&cmd.values.propertiesPrefix, "prefix", "",
		"Prefix to match against the start of the property name within the namespace."+
			" Optional. Cannot be used in conjunction with the '--name' option."+
			" The first character of the prefix must be in the 'a'-'z' or 'A'-'Z' ranges, "+
			"and following characters can be 'a'-'z', 'A'-'Z', '0'-'9', '.' (period), '-' (dash) or '_' (underscore)")
	propertiesGetCobraCmd.PersistentFlags().StringVar(&cmd.values.propertiesSuffix, "suffix", "",
		"Suffix to match against the end of the property name within the namespace."+
			" Optional. Cannot be used in conjunction with the '--name' option."+
			" The first character of the suffix must be in the 'a'-'z' or 'A'-'Z' ranges, "+
			"and following characters can be 'a'-'z', 'A'-'Z', '0'-'9', '.' (period), '-' (dash) or '_' (underscore)")
	propertiesGetCobraCmd.PersistentFlags().StringVar(&cmd.values.propertiesInfix, "infix", "",
		"Infix(es) that could be part of the property name within the namespace."+
			" Multiple infixes can be supplied as a comma-separated list without spaces. "+
			" Optional. Cannot be used in conjunction with the '--name' option."+
			" The first character of each infix must be in the 'a'-'z' or 'A'-'Z' ranges, "+
			"and following characters can be 'a'-'z', 'A'-'Z', '0'-'9', '.' (period), '-' (dash) or '_' (underscore)")
	propertiesGetCobraCmd.PersistentFlags().StringVar(&cmd.values.propertiesOutputFormat, "format", "summary",
		"output format for the data returned. Supported formats are: "+formatters+".")

	// The namespace property is mandatory for get.
	addNamespaceFlag(propertiesGetCobraCmd, true, propertiesCommandValues)
	addPropertyNameFlag(propertiesGetCobraCmd, false, propertiesCommandValues)

	// Name field cannot be used in conjunction wiht the prefix, suffix or infix commands.
	propertiesGetCobraCmd.MarkFlagsMutuallyExclusive("name", "prefix")
	propertiesGetCobraCmd.MarkFlagsMutuallyExclusive("name", "suffix")
	propertiesGetCobraCmd.MarkFlagsMutuallyExclusive("name", "infix")

	propertiesCommand.CobraCommand().AddCommand(propertiesGetCobraCmd)

	return propertiesGetCobraCmd
}

func (cmd *PropertiesGetCommand) executePropertiesGet(
	factory spi.Factory,
	propertiesCmdValues *PropertiesCmdValues,
	commsFlagSetValues *CommsFlagSetValues,
) error {
	var err error

	// Operations on the file system will all be relative to the current folder.
	fileSystem := factory.GetFileSystem()

	err = utils.CaptureLog(fileSystem, commsFlagSetValues.logFileName)
	if err == nil {
		commsFlagSetValues.isCapturingLogs = true
	
		log.Println("Galasa CLI - Get ecosystem properties")
	
		// Get the ability to query environment variables.
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
	
				getPropertiesFunc := func(apiClient *galasaapi.APIClient) error {
					// Call to process the command in a unit-testable way.
					return properties.GetProperties(
						propertiesCmdValues.namespace,
						propertiesCmdValues.propertyName,
						cmd.values.propertiesPrefix,
						cmd.values.propertiesSuffix,
						cmd.values.propertiesInfix,
						apiClient,
						cmd.values.propertiesOutputFormat,
						console,
					)
				}
				err = commsClient.RunAuthenticatedCommandWithRateLimitRetries(getPropertiesFunc)
			}
		}
	}

	return err
}
