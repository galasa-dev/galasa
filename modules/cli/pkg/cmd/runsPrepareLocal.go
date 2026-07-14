/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"log"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/embedded"
	"github.com/galasa-dev/cli/pkg/launcher"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/spf13/cobra"
)

type RunsPrepareLocalCmdValues struct {
	runsPrepareLocalCmdParams *launcher.RunsPrepareLocalCmdParameters
}

type RunsPrepareLocalCommand struct {
	values       *RunsPrepareLocalCmdValues
	cobraCommand *cobra.Command
}

// ------------------------------------------------------------------------------------------------
// Constructors
// ------------------------------------------------------------------------------------------------

func NewRunsPrepareLocalCommand(
	factory spi.Factory,
	runsPrepareCommand spi.GalasaCommand,
	commsFlagSet GalasaFlagSet,
) (spi.GalasaCommand, error) {
	cmd := new(RunsPrepareLocalCommand)
	err := cmd.init(factory, runsPrepareCommand, commsFlagSet)
	return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------

func (cmd *RunsPrepareLocalCommand) Name() string {
	return COMMAND_NAME_RUNS_PREPARE_LOCAL
}

func (cmd *RunsPrepareLocalCommand) CobraCommand() *cobra.Command {
	return cmd.cobraCommand
}

func (cmd *RunsPrepareLocalCommand) Values() interface{} {
	return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------

func (cmd *RunsPrepareLocalCommand) init(factory spi.Factory, runsPrepareCommand spi.GalasaCommand, commsFlagSet GalasaFlagSet) error {
	var err error

	cmd.values = &RunsPrepareLocalCmdValues{
		runsPrepareLocalCmdParams: &launcher.RunsPrepareLocalCmdParameters{},
	}

	cmd.cobraCommand, err = cmd.createCobraCmd(factory, runsPrepareCommand, commsFlagSet.Values().(*CommsFlagSetValues))

	return err
}

func (cmd *RunsPrepareLocalCommand) createCobraCmd(
	factory spi.Factory,
	runsPrepareCommand spi.GalasaCommand,
	commsFlagSetValues *CommsFlagSetValues,
) (*cobra.Command, error) {

	var err error

	runsPrepareLocalCobraCmd := &cobra.Command{
		Use:     "local",
		Short:   "download test bundle dependencies to a local Maven cache",
		Long:    "Downloads all OSGi bundle dependencies referenced by the provided OBRs to the local Maven cache without executing any tests. " +
			"Run this command from a network-connected environment, then share the populated local Maven cache with restricted test runners. " +
			"Use 'galasactl runs submit local --offline --localMaven <path>' to execute tests without any remote Maven access.",
		Args:    cobra.NoArgs,
		Aliases: []string{COMMAND_NAME_RUNS_PREPARE_LOCAL},
		RunE: func(cobraCommand *cobra.Command, args []string) error {
			return cmd.executePrepareDepsLocal(factory, commsFlagSetValues)
		},
	}

	currentGalasaVersion, _ := embedded.GetGalasaVersion()
	runsPrepareLocalCobraCmd.Flags().StringVar(&cmd.values.runsPrepareLocalCmdParams.TargetGalasaVersion, "galasaVersion",
		currentGalasaVersion,
		"the version of galasa you want to use to prepare dependencies. "+
			"This should match the version of the galasa obr you built your test bundles against.")

	runsPrepareLocalCobraCmd.Flags().StringVar(&cmd.values.runsPrepareLocalCmdParams.RemoteMaven, "remoteMaven",
		"https://repo.maven.apache.org/maven2",
		"the url of the remote maven where galasa bundles can be downloaded from. "+
			"Defaults to maven central.")

	runsPrepareLocalCobraCmd.Flags().StringVar(&cmd.values.runsPrepareLocalCmdParams.LocalMaven, "localMaven", "",
		"The url of a local maven repository where galasa bundles will be cached on your local file system. "+
			"Defaults to your home .m2/repository file. "+
			"Please note that this should be in a URL form e.g. 'file:///Users/myuserid/.m2/repository', or 'file://C:/Users/myuserid/.m2/repository'")

	runsPrepareLocalCobraCmd.Flags().StringSliceVar(&cmd.values.runsPrepareLocalCmdParams.Obrs, "obr", make([]string, 0),
		"The maven coordinates of the obr bundle(s) whose dependencies should be downloaded. "+
			"The format of this parameter is 'mvn:${TEST_OBR_GROUP_ID}/${TEST_OBR_ARTIFACT_ID}/${TEST_OBR_VERSION}/obr' "+
			"Multiple instances of this flag can be used to describe multiple obr bundles.")

	runsPrepareLocalCobraCmd.MarkFlagRequired("obr")

	runsPrepareCommand.CobraCommand().AddCommand(runsPrepareLocalCobraCmd)

	return runsPrepareLocalCobraCmd, err
}

func (cmd *RunsPrepareLocalCommand) executePrepareDepsLocal(
	factory spi.Factory,
	commsFlagSetValues *CommsFlagSetValues,
) error {

	var err error

	// Operations on the file system will all be relative to the current folder.
	fileSystem := factory.GetFileSystem()

	err = utils.CaptureLog(fileSystem, commsFlagSetValues.logFileName)
	if err == nil {
		commsFlagSetValues.isCapturingLogs = true

		log.Println("Galasa CLI - Prepare dependencies (local)")

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

				// the prepare targets a local JVM
				embeddedFileSystem := embedded.GetReadOnlyFileSystem()

				// Something which can kick off new operating system processes
				processFactory := launcher.NewRealProcessFactory()

				bootstrapData := commsClient.GetBootstrapData()

				// A launcher is needed to launch the JVM
				var launcherInstance *launcher.PrepareLocalJvmLauncher
				launcherInstance, err = launcher.NewPrepareLocalJvmLauncher(
					factory,
					bootstrapData.Properties, embeddedFileSystem,
					cmd.values.runsPrepareLocalCmdParams,
					processFactory, galasaHome, timedSleeper)

				if err == nil {
					err = launcherInstance.RunPrepare()
				}
			}
		}
	}

	return err
}
