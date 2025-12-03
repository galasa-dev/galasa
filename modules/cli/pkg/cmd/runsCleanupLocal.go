/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"log"
	"strconv"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/embedded"
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
	commsFlagSet GalasaFlagSet,
) (spi.GalasaCommand, error) {

    cmd := new(RunsCleanupLocalCommand)

    err := cmd.init(factory, runsCleanupCommand, commsFlagSet)
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
func (cmd *RunsCleanupLocalCommand) init(factory spi.Factory, runsCleanupCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {
    var err error

	cmd.values = &RunsCleanupLocalCmdValues{
		runsCleanupLocalCmdParams:  &launcher.RunsCleanupLocalCmdParameters{},
	}

    cmd.cobraCommand, err = cmd.createCobraCmd(factory, runsCleanupCommand, commsFlagSet.Values().(*CommsFlagSetValues))

    return err
}

func (cmd *RunsCleanupLocalCommand) createCobraCmd(
    factory spi.Factory,
    runsCleanupCommand spi.GalasaCommand,
	commsFlagSetValues *CommsFlagSetValues,
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
			return cmd.executeRunsCleanupLocal(factory, commsFlagSetValues)
        },
    }

	currentGalasaVersion, _ := embedded.GetGalasaVersion()
	runsCleanupLocalCobraCmd.Flags().StringVar(&cmd.values.runsCleanupLocalCmdParams.TargetGalasaVersion, "galasaVersion",
		currentGalasaVersion,
		"the version of galasa you want to use. This should match the version of the galasa obr you built your resource cleanup providers against.")

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

	runsCleanupLocalCobraCmd.Flags().Uint32Var(&cmd.values.runsCleanupLocalCmdParams.DebugPort, "debugPort", 0,
		"The port to use when the --debug option causes the resource cleanup provider to connect to a java debugger. "+
			"The default value used is "+strconv.FormatUint(uint64(launcher.DEBUG_PORT_DEFAULT), 10)+" which can be "+
			"overridden by the '"+api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_DEBUG_PORT+"' property in the bootstrap file, "+
			"which in turn can be overridden by this explicit parameter on the galasactl command.",
	)

	runsCleanupLocalCobraCmd.Flags().StringVar(&cmd.values.runsCleanupLocalCmdParams.DebugMode, "debugMode", "",
		"The mode to use when the --debug option causes the resource management provider to connect to a Java debugger. "+
			"Valid values are 'listen' or 'attach'. "+
			"'listen' means the JVM will pause on startup, waiting for the Java debugger to connect to the debug port "+
			"(see the --debugPort option). "+
			"'attach' means the JVM will pause on startup, trying to attach to a java debugger which is listening on the debug port. "+
			"The default value is 'listen' but can be overridden by the '"+api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_DEBUG_MODE+"' property in the bootstrap file, "+
			"which in turn can be overridden by this explicit parameter on the galasactl command.",
	)

	runsCleanupLocalCobraCmd.Flags().BoolVar(&cmd.values.runsCleanupLocalCmdParams.IsDebugEnabled, "debug", false,
		"When set (or true) the debugger pauses on startup and tries to connect to a Java debugger. "+
			"The connection is established using the --debugMode and --debugPort values.",
	)

	runsCleanupLocalCobraCmd.Flags().BoolVar(&cmd.values.runsCleanupLocalCmdParams.IsTraceEnabled, "trace", false, "Enables trace-level logging")

    runsCleanupCommand.CobraCommand().AddCommand(runsCleanupLocalCobraCmd)

    return runsCleanupLocalCobraCmd, err
}

func (cmd *RunsCleanupLocalCommand) executeRunsCleanupLocal(
    factory spi.Factory,
    commsFlagSetValues *CommsFlagSetValues,
) error {

    var err error
    // Operations on the file system will all be relative to the current folder.
    fileSystem := factory.GetFileSystem()

	err = utils.CaptureLog(fileSystem, commsFlagSetValues.logFileName)
	if err == nil {
		commsFlagSetValues.isCapturingLogs = true
	
		log.Println("Galasa CLI - Run resource cleanup for local runs")

		// Get the ability to query environment variables.
		env := factory.GetEnvironment()

		// Work out where galasa home is, only once.
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

				timedSleeper := utils.NewRealTimedSleeper()

				// the submit is targetting a local JVM
				embeddedFileSystem := embedded.GetReadOnlyFileSystem()

				// Something which can kick off new operating system processes
				processFactory := launcher.NewRealProcessFactory()

				bootstrapData := commsClient.GetBootstrapData()

				// A launcher is needed to launch anything
				var launcherInstance launcher.ResourceCleanupLauncher
				launcherInstance, err = launcher.NewResourceCleanupJVMLauncher(
					factory,
					bootstrapData.Properties, embeddedFileSystem,
					cmd.values.runsCleanupLocalCmdParams,
					processFactory, galasaHome, timedSleeper)

				if err == nil {
					launcherInstance.RunResourceCleanup()
				}
			}
		}
	}

    return err
}
