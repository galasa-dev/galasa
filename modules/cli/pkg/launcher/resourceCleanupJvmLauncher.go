/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package launcher

import (
	"log"
	"time"

	"github.com/galasa-dev/cli/pkg/embedded"
	"github.com/galasa-dev/cli/pkg/props"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/utils"
)

const (
	// The amount of time in seconds to wait between checking for completion
	RESOURCE_CLEANUP_COMPLETION_POLL_INTERVAL_SECONDS = 30
)

// ResourceCleanupJvmLauncher launches resource management locally,
// it is given OBRs containing resource management providers which 
// need to be executed, and it launches them within a local JVM.
type ResourceCleanupJvmLauncher struct {
	// Inherit the properties in the base JVM launcher structure
	BaseJvmLauncher

	// The parameters from the command-line.
	cmdParams RunsCleanupLocalCmdParameters
}

type RunsCleanupLocalCmdParameters struct {

	// A list of OBRs containing the resource cleanup providers we want to run.
	Obrs []string

	// The local maven repo, eg: file:///home/.m2/repository, where we can load the galasa uber-obr
	LocalMaven string

	// The remote maven repositories, eg: maven central, where we can load the galasa uber-obr
	// and any other OBRs providing resource cleanup services
	RemoteMavenRepos []string

	// The version of galasa we want to launch. This indicates which uber-obr will be
	// loaded.
	TargetGalasaVersion string

	// Should the JVM be launched in debug mode ?
	IsDebugEnabled bool

	// When launched in debug mode, which port should the JVM use to talk to the Java
	// debugger ? This port is either listened on, or attached to depending on the
	// DebugMode field.
	DebugPort uint32

	// A string indicating whether the JVM should 'attach' to the debug port
	// to talk to the Java debugger (JDB), or whether it should 'listen' on a port
	// ready for the JDB to attach to.
	DebugMode string

	// The list of glob patterns representing the resource cleanup services that we should load
	IncludesPatterns []string

	// The list of glob patterns representing the resource cleanup services that we should not load
	ExcludesPatterns []string

	// Determines whether trace-level logging should be enabled
	IsTraceEnabled bool
}

// -----------------------------------------------------------------------------
// Constructors
// -----------------------------------------------------------------------------
func NewResourceCleanupJVMLauncher(
	factory spi.Factory,
	bootstrapProps props.JavaProperties,
	embeddedFileSystem embedded.ReadOnlyFileSystem,
	cmdParameters *RunsCleanupLocalCmdParameters,
	processFactory ProcessFactory,
	galasaHome spi.GalasaHome,
	timedSleeper spi.TimedSleeper,
) (*ResourceCleanupJvmLauncher, error) {
	var (
		err      error
		launcher *ResourceCleanupJvmLauncher = nil
	)

	env := factory.GetEnvironment()
	fileSystem := factory.GetFileSystem()

	javaHome := env.GetEnv("JAVA_HOME")

	err = utils.ValidateJavaHome(fileSystem, javaHome)

	if err == nil {
		launcher = new(ResourceCleanupJvmLauncher)
		launcher.factory = factory
		launcher.javaHome = javaHome
		launcher.cmdParams = *cmdParameters
		launcher.env = env
		launcher.fileSystem = fileSystem
		launcher.embeddedFileSystem = embeddedFileSystem
		launcher.processFactory = processFactory
		launcher.galasaHome = galasaHome
		launcher.timeService = factory.GetTimeService()
		launcher.timedSleeper = timedSleeper
		launcher.bootstrapProps = bootstrapProps

		// Make sure the home folder has the boot jar unpacked and ready to invoke.
		err = utils.InitialiseGalasaHomeFolder(
			launcher.galasaHome,
			launcher.fileSystem,
			launcher.embeddedFileSystem,
		)
	}

	return launcher, err
}

// -----------------------------------------------------------------------------
// Implementation of the ResourceCleanupLauncher interface
// -----------------------------------------------------------------------------
func (launcher *ResourceCleanupJvmLauncher) RunResourceCleanup() error {
	log.Printf("JvmLauncher: RunResourceCleanup entered")
	var err error

	// We have some OBRs from the command-line
	var obrs []utils.MavenCoordinates
	obrs, err = utils.ValidateObrs(launcher.cmdParams.Obrs)
	if err == nil {
		var jwt = ""
		if isCPSRemote(launcher.bootstrapProps) {
			apiServerUrl := getCPSRemoteApiServerUrl(launcher.bootstrapProps)
			authenticator := launcher.factory.GetAuthenticator(apiServerUrl, launcher.galasaHome)
			log.Printf("framework.config.store bootstrap property indicates a remote CPS will be used. So we need a valid JWT.\n")
			jwt, err = authenticator.GetBearerToken()
		}

		if err == nil {
			var (
				cmd  string
				args []string
			)
			cmd, args, err = getResourceManagementCommandSyntax(
				launcher.bootstrapProps,
				launcher.galasaHome,
				launcher.fileSystem, launcher.javaHome, obrs,
				launcher.cmdParams.RemoteMavenRepos, launcher.cmdParams.LocalMaven,
				launcher.cmdParams.TargetGalasaVersion,
				launcher.cmdParams.IsTraceEnabled,
				launcher.cmdParams.IsDebugEnabled,
				launcher.cmdParams.DebugPort,
				launcher.cmdParams.DebugMode,
				launcher.cmdParams.IncludesPatterns,
				launcher.cmdParams.ExcludesPatterns,
				jwt,
			)

			if err == nil {
				log.Printf("Launching command '%s' '%v'\n", cmd, args)
				localResourceCleanup := NewLocalResourceCleanup(launcher.timedSleeper, launcher.fileSystem, launcher.processFactory)
				err = localResourceCleanup.launch(cmd, args)
				if err == nil {
					pollInterval := time.Second * time.Duration(RESOURCE_CLEANUP_COMPLETION_POLL_INTERVAL_SECONDS)

					for !localResourceCleanup.isCompleted() {
						launcher.timedSleeper.Sleep(pollInterval)
					}
				}
			}
		}
	}

	return err
}

// -----------------------------------------------------------------------------
// Local functions
// -----------------------------------------------------------------------------
// getResourceManagementCommandSyntax From the parameters we aim to build a command-line incantation that would launch resource management locally
//
// For example:
// java -jar ${BOOT_JAR_PATH} \
// --localmaven file:${M2_PATH}/repository/ \
// --remotemaven $REMOTE_MAVEN \
// --bootstrap file:${HOME}/.galasa/bootstrap.properties \
// --obr mvn:dev.galasa/dev.galasa.uber.obr/${OBR_VERSION}/obr \
// --obr mvn:${OBR_GROUP_ID}/${OBR_ARTIFACT_ID}/${OBR_VERSION}/obr \
// --includes-monitor-pattern "dev.galasa.*"
// --excludes-monitor-pattern "*MyUnwantedCleanupJob"
// --local-resource-management
func getResourceManagementCommandSyntax(
	bootstrapProperties props.JavaProperties,
	galasaHome spi.GalasaHome,
	fileSystem spi.FileSystem,
	javaHome string,
	obrs []utils.MavenCoordinates,
	remoteMavenRepos []string,
	localMaven string,
	galasaVersionToRun string,
	isTraceEnabled bool,
	isDebugEnabled bool,
	debugPort uint32,
	debugMode string,
	includesPatterns []string,
	excludesPatterns []string,
	jwt string,
) (string, []string, error) {

	var cmd string = ""
	var args []string = make([]string, 0)
	var err error

	cmd, args, err = getBaseCommandSyntax(
		bootstrapProperties,
		galasaHome,
		fileSystem,
		javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		isTraceEnabled,
		isDebugEnabled,
		debugPort,
		debugMode,
		jwt,
	)

	if err == nil {

		args = append(args, "--local-resource-management")

		// --includes-monitor-pattern $PATTERN
		for _, pattern := range includesPatterns {
			args = append(args, "--includes-monitor-pattern")
			args = append(args, pattern)
		}

		// --excludes-monitor-pattern $PATTERN
		for _, pattern := range excludesPatterns {
			args = append(args, "--excludes-monitor-pattern")
			args = append(args, pattern)
		}
	}

	return cmd, args, err
}
