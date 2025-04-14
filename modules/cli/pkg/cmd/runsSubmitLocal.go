/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"log"
	"strconv"

	"github.com/spf13/cobra"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/embedded"
	"github.com/galasa-dev/cli/pkg/images"
	"github.com/galasa-dev/cli/pkg/launcher"
	"github.com/galasa-dev/cli/pkg/runs"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/utils"
)

var ()

// Variables set by cobra's command-line parsing.
type RunsSubmitLocalCmdValues struct {
	runsSubmitLocalCmdParams  *launcher.RunsSubmitLocalCmdParameters
	submitLocalSelectionFlags *utils.TestSelectionFlagValues
}

type RunsSubmitLocalCommand struct {
	values       *RunsSubmitLocalCmdValues
	cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors
// ------------------------------------------------------------------------------------------------
func NewRunsSubmitLocalCommand(factory spi.Factory, runsSubmitCommand spi.GalasaCommand, runsCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) (spi.GalasaCommand, error) {
	cmd := new(RunsSubmitLocalCommand)
	err := cmd.init(factory, runsSubmitCommand, commsFlagSet)
	return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *RunsSubmitLocalCommand) Name() string {
	return COMMAND_NAME_RUNS_SUBMIT_LOCAL
}

func (cmd *RunsSubmitLocalCommand) CobraCommand() *cobra.Command {
	return cmd.cobraCommand
}

func (cmd *RunsSubmitLocalCommand) Values() interface{} {
	return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------

func (cmd *RunsSubmitLocalCommand) init(factory spi.Factory, runsSubmitCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {
	var err error

	// Allocate storage to capture the parsed values.
	cmd.values = &RunsSubmitLocalCmdValues{
		runsSubmitLocalCmdParams:  &launcher.RunsSubmitLocalCmdParameters{},
		submitLocalSelectionFlags: runs.NewTestSelectionFlagValues(),
	}

	cmd.cobraCommand, err = cmd.createRunsSubmitLocalCobraCmd(
		factory,
		runsSubmitCommand,
		commsFlagSet.Values().(*CommsFlagSetValues),
	)
	return err
}

func (cmd *RunsSubmitLocalCommand) createRunsSubmitLocalCobraCmd(
	factory spi.Factory,
	runsSubmitCmd spi.GalasaCommand,
	commsFlagSetValues *CommsFlagSetValues,
) (*cobra.Command, error) {
	var err error

	runsSubmitLocalCobraCmd := &cobra.Command{
		Use:     "local",
		Short:   "submit a list of tests to be run on a local java virtual machine (JVM)",
		Long:    "Submit a list of tests to a local JVM, monitor them and wait for them to complete",
		Args:    cobra.NoArgs,
		Aliases: []string{"runs submit local"},
		RunE: func(cobraCmd *cobra.Command, args []string) error {
			return cmd.executeSubmitLocal(factory, runsSubmitCmd.Values().(*utils.RunsSubmitCmdValues), commsFlagSetValues)
		},
	}

	//currentUserName := runs.GetCurrentUserName()

	runsSubmitLocalCobraCmd.Flags().StringVar(&cmd.values.runsSubmitLocalCmdParams.RemoteMaven, "remoteMaven",
		"https://repo.maven.apache.org/maven2",
		"the url of the remote maven where galasa bundles can be loaded from. "+
			"Defaults to maven central.")

	runsSubmitLocalCobraCmd.Flags().StringVar(&cmd.values.runsSubmitLocalCmdParams.LocalMaven, "localMaven", "",
		"The url of a local maven repository are where galasa bundles can be loaded from on your local file system. Defaults to your home .m2/repository file. Please note that this should be in a URL form e.g. 'file:///Users/myuserid/.m2/repository', or 'file://C:/Users/myuserid/.m2/repository'")

	currentGalasaVersion, _ := embedded.GetGalasaVersion()
	runsSubmitLocalCobraCmd.Flags().StringVar(&cmd.values.runsSubmitLocalCmdParams.TargetGalasaVersion, "galasaVersion",
		currentGalasaVersion,
		"the version of galasa you want to use to run your tests. "+
			"This should match the version of the galasa obr you built your test bundles against.")

	runsSubmitLocalCobraCmd.Flags().StringSliceVar(&cmd.values.runsSubmitLocalCmdParams.Obrs, "obr", make([]string, 0),
		"The maven coordinates of the obr bundle(s) which refer to your test bundles. "+
			"The format of this parameter is 'mvn:${TEST_OBR_GROUP_ID}/${TEST_OBR_ARTIFACT_ID}/${TEST_OBR_VERSION}/obr' "+
			"Multiple instances of this flag can be used to describe multiple obr bundles.")

	runsSubmitLocalCobraCmd.Flags().Uint32Var(&cmd.values.runsSubmitLocalCmdParams.DebugPort, "debugPort", 0,
		"The port to use when the --debug option causes the testcase to connect to a java debugger. "+
			"The default value used is "+strconv.FormatUint(uint64(launcher.DEBUG_PORT_DEFAULT), 10)+" which can be "+
			"overridden by the '"+api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_DEBUG_PORT+"' property in the bootstrap file, "+
			"which in turn can be overridden by this explicit parameter on the galasactl command.",
	)

	runsSubmitLocalCobraCmd.Flags().StringVar(&cmd.values.runsSubmitLocalCmdParams.DebugMode, "debugMode", "",
		"The mode to use when the --debug option causes the testcase to connect to a Java debugger. "+
			"Valid values are 'listen' or 'attach'. "+
			"'listen' means the testcase JVM will pause on startup, waiting for the Java debugger to connect to the debug port "+
			"(see the --debugPort option). "+
			"'attach' means the testcase JVM will pause on startup, trying to attach to a java debugger which is listening on the debug port. "+
			"The default value is 'listen' but can be overridden by the '"+api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_DEBUG_MODE+"' property in the bootstrap file, "+
			"which in turn can be overridden by this explicit parameter on the galasactl command.",
	)

	runsSubmitLocalCobraCmd.Flags().BoolVar(&cmd.values.runsSubmitLocalCmdParams.IsDebugEnabled, "debug", false,
		"When set (or true) the debugger pauses on startup and tries to connect to a Java debugger. "+
			"The connection is established using the --debugMode and --debugPort values.",
	)

	runs.AddClassFlag(runsSubmitLocalCobraCmd, cmd.values.submitLocalSelectionFlags, false, "test class names."+
		" The format of each entry is osgi-bundle-name/java-class-name. Java class names are fully qualified. No .class suffix is needed.")

	runs.AddGherkinFlag(runsSubmitLocalCobraCmd, cmd.values.submitLocalSelectionFlags, false, "Gherkin feature file URL. Should start with 'file://'. ")

	runsSubmitLocalCobraCmd.MarkFlagsRequiredTogether("class", "obr")
	runsSubmitLocalCobraCmd.MarkFlagsOneRequired("class", "gherkin")

	runsSubmitCmd.CobraCommand().AddCommand(runsSubmitLocalCobraCmd)

	return runsSubmitLocalCobraCmd, err
}

func (cmd *RunsSubmitLocalCommand) executeSubmitLocal(
	factory spi.Factory,
	runsSubmitCmdValues *utils.RunsSubmitCmdValues,
	commsFlagSetValues *CommsFlagSetValues,
) error {

	var err error

	// Operations on the file system will all be relative to the current folder.
	fileSystem := factory.GetFileSystem()

	err = utils.CaptureLog(fileSystem, commsFlagSetValues.logFileName)
	if err == nil {
		commsFlagSetValues.isCapturingLogs = true
	
		log.Println("Galasa CLI - Submit tests (Local)")
	
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
	
				timeService := utils.NewRealTimeService()
				timedSleeper := utils.NewRealTimedSleeper()
	
				// the submit is targetting a local JVM
				embeddedFileSystem := embedded.GetReadOnlyFileSystem()
	
				// Something which can kick off new operating system processes
				processFactory := launcher.NewRealProcessFactory()
	
				// Validate the test selection parameters.
				validator := runs.NewObrBasedValidator()
				err = validator.Validate(cmd.values.submitLocalSelectionFlags)
				if err == nil {
	
					bootstrapData := commsClient.GetBootstrapData()
	
					// A launcher is needed to launch anythihng
					var launcherInstance launcher.Launcher
					launcherInstance, err = launcher.NewJVMLauncher(
						factory,
						bootstrapData.Properties, embeddedFileSystem,
						cmd.values.runsSubmitLocalCmdParams,
						processFactory, galasaHome, timedSleeper)
	
					if err == nil {
						var console = factory.GetStdOutConsole()
	
						renderer := images.NewImageRenderer(embeddedFileSystem)
						expander := images.NewImageExpander(fileSystem, renderer, true)
	
						// Do the launching of the tests.
						submitter := runs.NewSubmitter(
							galasaHome,
							fileSystem,
							launcherInstance,
							timeService,
							timedSleeper,
							env,
							console,
							expander,
						)
	
						err = submitter.ExecuteSubmitRuns(
							runsSubmitCmdValues,
							cmd.values.submitLocalSelectionFlags,
						)
	
						if err == nil {
							reportOnExpandedImages(expander)
						}
					}
				}
			}
		}
	}

	return err
}

func reportOnExpandedImages(expander images.ImageExpander) error {

	// Write out a status string to the console about how many files were rendered.
	count := expander.GetExpandedImageFileCount()

	// Only bother writing out a message if any images have been expanded.
	log.Printf("Expanded a total of %d images from .gz files.", count)

	return nil
}
