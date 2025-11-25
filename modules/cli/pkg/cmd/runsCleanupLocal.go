/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"log"

	"github.com/galasa-dev/cli/pkg/launcher"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/spf13/cobra"
)

type RunsCleanupLocalCmdValues struct {
	runsCleanupLocalCmdParams  *launcher.RunsCleanupLocalCmdParameters
}

type RunsCleanupLocalCommand struct {
	values *RunsCleanupLocalCmdValues
    cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors methods
// ------------------------------------------------------------------------------------------------
func NewRunsCleanupLocalCommand(
    factory spi.Factory,
    runsCleanupCommand spi.GalasaCommand,
	rootCmd spi.GalasaCommand,
) (spi.GalasaCommand, error) {

    cmd := new(RunsCleanupLocalCommand)

    err := cmd.init(factory, runsCleanupCommand, rootCmd)
    return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *RunsCleanupLocalCommand) Name() string {
    return COMMAND_NAME_RUNS_CLEANUP_LOCAL
}

func (cmd *RunsCleanupLocalCommand) CobraCommand() *cobra.Command {
    return cmd.cobraCommand
}

func (cmd *RunsCleanupLocalCommand) Values() interface{} {
	return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------
func (cmd *RunsCleanupLocalCommand) init(factory spi.Factory, runsCleanupCommand spi.GalasaCommand, rootCmd spi.GalasaCommand) error {
    var err error

	cmd.values = &RunsCleanupLocalCmdValues{
		runsCleanupLocalCmdParams:  &launcher.RunsCleanupLocalCmdParameters{},
	}

    cmd.cobraCommand, err = cmd.createCobraCmd(factory, runsCleanupCommand, rootCmd)

    return err
}

func (cmd *RunsCleanupLocalCommand) createCobraCmd(
    factory spi.Factory,
    runsCleanupCommand spi.GalasaCommand,
	rootCmd spi.GalasaCommand,
) (*cobra.Command, error) {

    var err error

    runsCleanupLocalCobraCmd := &cobra.Command{
        Use:     "local",
        Short:   "Runs resource cleanup providers once for resources provisioned by Galasa",
        Long:    "Runs resource cleanup providers loaded from provided OBRs for resources provisioned by Galasa as part of local test runs. The loaded providers are only run once and do not run as a daemon process. "+
			"The providers that are loaded are determined by the patterns provided in the '--includes-pattern' and '--excludes-pattern' flags. By default, all cleanup providers in the provided OBRs will be loaded and none will be excluded.\n"+
			"Supported glob patterns include the following special characters:\n"+
			"'*' (wildcard) Matches zero or more characters.\n"+
			"'?' matches exactly one character\n"+
			"For example, the pattern 'dev.galasa*' will match any monitor that includes 'dev.galasa' as its prefix, so a provider like 'dev.galasa.core.CoreResourceMonitorClass' will be matched.",
        Aliases: []string{COMMAND_NAME_RUNS_CLEANUP_LOCAL},
        RunE: func(cobraCommand *cobra.Command, args []string) error {
			return cmd.executeRunsCleanupLocal(factory, runsCleanupCommand.Values().(*RunsCleanupCmdValues), rootCmd.Values().(*RootCmdValues))
        },
    }

	runsCleanupLocalCobraCmd.Flags().StringSliceVar(&cmd.values.runsCleanupLocalCmdParams.RemoteMavenRepos, "remoteMaven",
		[]string{"https://repo.maven.apache.org/maven2"},
		"the urls of the remote maven repositories where galasa bundles can be loaded from. "+
			"Defaults to maven central.")

	runsCleanupLocalCobraCmd.Flags().StringVar(&cmd.values.runsCleanupLocalCmdParams.LocalMaven, "localMaven", "",
		"The url of a local maven repository are where galasa bundles can be loaded from on your local file system. Defaults to your home .m2/repository file. Please note that this should be in a URL form e.g. 'file:///Users/myuserid/.m2/repository', or 'file://C:/Users/myuserid/.m2/repository'")

	runsCleanupLocalCobraCmd.Flags().StringSliceVar(&cmd.values.runsCleanupLocalCmdParams.Obrs, "obr", make([]string, 0),
		"The maven coordinates of the obr bundle(s) which refer to your resource cleanup bundles. "+
			"The format of this parameter is 'mvn:${OBR_GROUP_ID}/${OBR_ARTIFACT_ID}/${OBR_VERSION}/obr' "+
			"Multiple instances of this flag can be used to describe multiple obr bundles.")

	runsCleanupLocalCobraCmd.Flags().StringSliceVar(&cmd.values.runsCleanupLocalCmdParams.IncludesPatterns, "includes-pattern", []string{"*"},
		"The glob pattern(s) representing the resource cleanup providers that should be loaded. "+
			"Supported glob patterns include the following special characters:\n"+
			"'*' (wildcard) Matches zero or more characters.\n"+
			"'?' matches exactly one character\n"+
			"For example, the pattern 'dev.galasa*' will match any provider that includes 'dev.galasa' as its prefix, so a provider like 'dev.galasa.core.CoreResourceCleanupClass' will be matched.")

	runsCleanupLocalCobraCmd.Flags().StringSliceVar(&cmd.values.runsCleanupLocalCmdParams.ExcludesPatterns, "excludes-pattern", make([]string, 0),
		"The glob pattern(s) representing the resource cleanup providers that should not be loaded. "+
			"Supported glob patterns include the following special characters:\n"+
			"'*' (wildcard) Matches zero or more characters.\n"+
			"'?' matches exactly one character\n"+
			"For example, the pattern '*MyResourceCleanupClass' will match any provider that ends with 'MyResourceCleanupClass' such as 'my.company.resources.MyResourceCleanupClass' and so that provider will not be loaded.")

	runsCleanupLocalCobraCmd.MarkFlagRequired("obr")

    runsCleanupCommand.CobraCommand().AddCommand(runsCleanupLocalCobraCmd)

    return runsCleanupLocalCobraCmd, err
}

func (cmd *RunsCleanupLocalCommand) executeRunsCleanupLocal(
    factory spi.Factory,
    runsCleanupCmdValues *RunsCleanupCmdValues,
    rootCmdValues *RootCmdValues,
) error {

    var err error
    // Operations on the file system will all be relative to the current folder.
    fileSystem := factory.GetFileSystem()

	err = utils.CaptureLog(fileSystem, rootCmdValues.logFileName)
	if err == nil {
		rootCmdValues.isCapturingLogs = true
	
		log.Println("Galasa CLI - Run resource cleanup for local runs")
	}

    return err
}
