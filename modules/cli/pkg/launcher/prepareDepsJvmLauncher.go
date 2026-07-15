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
    PREPARE_DEPS_COMPLETION_POLL_INTERVAL_SECONDS = 30
)

// PrepareLocalJvmLauncher launches galasa-boot with the --prepare flag to
// download all OBR bundle dependencies to the local Maven cache without
// executing any tests.
type PrepareLocalJvmLauncher struct {
    // Inherit the properties in the base JVM launcher structure
    BaseJvmLauncher

    // The parameters from the command-line.
    cmdParams RunsPrepareLocalCmdParameters
}

// RunsPrepareLocalCmdParameters holds the parameters for the runs prepare local command.
type RunsPrepareLocalCmdParameters struct {

    // A list of OBRs whose bundle dependencies should be downloaded.
    Obrs []string

    // The local maven repo, eg: file:///home/.m2/repository, where bundles will be cached.
    LocalMaven string

    // The remote maven repo, eg: maven central, from which bundles are downloaded.
    RemoteMaven string

    // The version of galasa to use. Determines which uber-obr is loaded.
    TargetGalasaVersion string
}

// -----------------------------------------------------------------------------
// Constructors
// -----------------------------------------------------------------------------

// NewPrepareLocalJvmLauncher creates a PrepareLocalJvmLauncher ready to
// download dependencies for the provided OBRs.
func NewPrepareLocalJvmLauncher(
    factory spi.Factory,
    bootstrapProps props.JavaProperties,
    embeddedFileSystem embedded.ReadOnlyFileSystem,
    cmdParameters *RunsPrepareLocalCmdParameters,
    processFactory ProcessFactory,
    galasaHome spi.GalasaHome,
    timedSleeper spi.TimedSleeper,
) (*PrepareLocalJvmLauncher, error) {
    var (
        err      error
        launcher *PrepareLocalJvmLauncher = nil
    )

    env := factory.GetEnvironment()
    fileSystem := factory.GetFileSystem()

    javaHome := env.GetEnv("JAVA_HOME")

    err = utils.ValidateJavaHome(fileSystem, javaHome)

    if err == nil {
        launcher = new(PrepareLocalJvmLauncher)
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
// Public methods
// -----------------------------------------------------------------------------

// RunPrepare downloads all OBR bundle dependencies to the local Maven cache.
func (launcher *PrepareLocalJvmLauncher) RunPrepare() error {
    log.Printf("PrepareLocalJvmLauncher: RunPrepare entered")
    var err error

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
            cmd, args, err = getPrepareCommandSyntax(
                launcher.bootstrapProps,
                launcher.galasaHome,
                launcher.fileSystem, launcher.javaHome, obrs,
                launcher.cmdParams.RemoteMaven, launcher.cmdParams.LocalMaven,
                launcher.cmdParams.TargetGalasaVersion,
                jwt,
            )

            if err == nil {
                log.Printf("Launching command '%s' '%v'\n", cmd, args)
                localPrepareDeps := NewLocalResourceCleanup(launcher.timedSleeper, launcher.fileSystem, launcher.processFactory)
                err = localPrepareDeps.launch(cmd, args)
                if err == nil {
                    pollInterval := time.Second * time.Duration(PREPARE_DEPS_COMPLETION_POLL_INTERVAL_SECONDS)
                    for !localPrepareDeps.isCompleted() {
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

// getPrepareCommandSyntax builds the command-line arguments to invoke galasa-boot
// in preparation mode (--prepare), which downloads all OBR dependencies without
// running any tests.
//
// For example:
// java -jar ${BOOT_JAR_PATH} \
// --localmaven file:${M2_PATH}/repository/ \
// --remotemaven $REMOTE_MAVEN \
// --bootstrap file:${HOME}/.galasa/bootstrap.properties \
// --obr mvn:dev.galasa/dev.galasa.uber.obr/${OBR_VERSION}/obr \
// --obr mvn:${TEST_OBR_GROUP_ID}/${TEST_OBR_ARTIFACT_ID}/${TEST_OBR_VERSION}/obr \
// --prepare
func getPrepareCommandSyntax(
    bootstrapProperties props.JavaProperties,
    galasaHome spi.GalasaHome,
    fileSystem spi.FileSystem,
    javaHome string,
    obrs []utils.MavenCoordinates,
    remoteMaven string,
    localMaven string,
    galasaVersionToRun string,
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
        []string{remoteMaven},
        localMaven,
        galasaVersionToRun,
        false, // isTraceEnabled — not applicable for prepare
        false, // isDebugEnabled — not applicable for prepare
        0,     // debugPort
        "",    // debugMode
        jwt,
    )

    if err == nil {
        args = append(args, "--prepare")
    }

    return cmd, args, err
}
