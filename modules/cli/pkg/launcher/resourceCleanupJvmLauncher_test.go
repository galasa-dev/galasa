/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package launcher

import (
	"strconv"
	"strings"
	"testing"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/files"
	"github.com/galasa-dev/cli/pkg/props"
	"github.com/galasa-dev/cli/pkg/spi"

	"github.com/galasa-dev/cli/pkg/embedded"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)


func NewMockResourceCleanupLauncherParams() (
	props.JavaProperties,
	*utils.MockEnv,
	spi.FileSystem,
	embedded.ReadOnlyFileSystem,
	*RunsCleanupLocalCmdParameters,
	spi.TimeService,
	spi.TimedSleeper,
	ProcessFactory,
	spi.GalasaHome,
) {
	// Given...
	env := utils.NewMockEnv()
	env.EnvVars["JAVA_HOME"] = "/java"
	fs := files.NewMockFileSystem()
	utils.AddJavaRuntimeToMock(fs, "/java")
	galasaHome, _ := utils.NewGalasaHome(fs, env, "")
	jvmLaunchParams := getBasicCleanupJvmLaunchParams()
	timeService := utils.NewMockTimeService()
	timedSleeper := utils.NewRealTimedSleeper()
	mockProcess := NewMockProcess()
	mockProcessFactory := NewMockProcessFactory(mockProcess)

	bootstrapProps := getBasicBootstrapProperties()

	return bootstrapProps, env, fs, embedded.GetReadOnlyFileSystem(),
		jvmLaunchParams, timeService, timedSleeper, mockProcessFactory, galasaHome
}

func TestCanCreateAResourceCleanupJVMLauncher(t *testing.T) {

	env := utils.NewMockEnv()
	env.EnvVars["JAVA_HOME"] = "/java"

	fs := files.NewMockFileSystem()
	utils.AddJavaRuntimeToMock(fs, "/java")

	galasaHome, _ := utils.NewGalasaHome(fs, env, "")

	jvmLaunchParams := getBasicCleanupJvmLaunchParams()
	timeService := utils.NewMockTimeService()
	timedSleeper := utils.NewRealTimedSleeper()

	mockProcess := NewMockProcess()
	mockProcessFactory := NewMockProcessFactory(mockProcess)

	bootstrapProps := getBasicBootstrapProperties()

	mockFactory := &utils.MockFactory{
		Env:         env,
		FileSystem:  fs,
		TimeService: timeService,
	}

	launcher, err := NewResourceCleanupJVMLauncher(
		mockFactory,
		bootstrapProps, embedded.GetReadOnlyFileSystem(),
		jvmLaunchParams, mockProcessFactory, galasaHome, timedSleeper,
	)

	assert.NoError(t, err, "Constructor should not have failed but it did.")
	assert.NotNil(t, launcher, "Launcher reference was nil, shouldn't have been.")
}

func getBasicCleanupJvmLaunchParams() *RunsCleanupLocalCmdParameters {
	return &RunsCleanupLocalCmdParameters{
		Obrs:                nil,
		RemoteMavenRepos: []string{},
		TargetGalasaVersion: "",
	}
}

func TestCantCreateAResourceCleanupJVMLauncherIfJVMHomeNotSet(t *testing.T) {

	env := utils.NewMockEnv()

	fs := files.NewMockFileSystem()
	utils.AddJavaRuntimeToMock(fs, "/java")

	galasaHome, _ := utils.NewGalasaHome(fs, env, "")

	bootstrapProps := getBasicBootstrapProperties()

	jvmLaunchParams := getBasicCleanupJvmLaunchParams()
	timeService := utils.NewMockTimeService()
	timedSleeper := utils.NewRealTimedSleeper()

	mockProcess := NewMockProcess()
	mockProcessFactory := NewMockProcessFactory(mockProcess)

	mockFactory := &utils.MockFactory{
		Env:         env,
		FileSystem:  fs,
		TimeService: timeService,
	}

	launcher, err := NewResourceCleanupJVMLauncher(
		mockFactory,
		bootstrapProps, embedded.GetReadOnlyFileSystem(),
		jvmLaunchParams, mockProcessFactory, galasaHome, timedSleeper,
	)

	assert.Error(t, err, "Constructor should have failed but it did not.")
	assert.Nil(t, launcher, "Launcher reference was not nil")
	assert.Contains(t, err.Error(), "GAL1050E")
}

func TestCanLaunchLocalJVMResourceCleanup(t *testing.T) {
	// Given...
	bootstrapProps, env, fs, embeddedReadOnlyFS,
		jvmLaunchParams, timeService, timedSleeper, mockProcessFactory, galasaHome := NewMockResourceCleanupLauncherParams()

	mockFactory := &utils.MockFactory{
		Env:         env,
		FileSystem:  fs,
		TimeService: timeService,
	}

	launcher, err := NewResourceCleanupJVMLauncher(
		mockFactory,
		bootstrapProps, embeddedReadOnlyFS,
		jvmLaunchParams, mockProcessFactory, galasaHome, timedSleeper,
	)

	assert.NoError(t, err, "JVM launcher should have been creatable.")
	assert.NotNil(t, launcher, "Launcher returned is nil!")

	// When...
	err = launcher.RunResourceCleanup()

	assert.NoError(t, err, "Launcher should have launched command OK")
}

func TestBadlyFormedCleanupObrFromProfileInfoCausesError(t *testing.T) {

	// Given...
	bootstrapProps, env, fs, embeddedReadOnlyFS,
		jvmLaunchParams, timeService, timedSleeper, mockProcessFactory, galasaHome := NewMockResourceCleanupLauncherParams()
	
	jvmLaunchParams.Obrs = append(jvmLaunchParams.Obrs, "notmaven://group/artifact/version/classifier")

	mockFactory := &utils.MockFactory{
		Env:         env,
		FileSystem:  fs,
		TimeService: timeService,
	}

	launcher, _ := NewResourceCleanupJVMLauncher(
		mockFactory,
		bootstrapProps, embeddedReadOnlyFS,
		jvmLaunchParams, mockProcessFactory, galasaHome, timedSleeper)

	// When...
	err := launcher.RunResourceCleanup()

	assert.NotNil(t, err)
	if err != nil {
		// Expect badly formed OBR
		assert.Contains(t, err.Error(), "GAL1061E:")
	}
}

func getDefaultResourceManagementCommandSyntaxParameters() (
	props.JavaProperties,
	spi.Environment,
	spi.GalasaHome,
	*files.MockFileSystem,
	string,
	[]utils.MavenCoordinates,
	[]string,
	string,
	string,
	bool,
	[]string,
	[]string,
) {
	bootstrapProps := getBasicBootstrapProperties()
	fs := files.NewOverridableMockFileSystem()
	javaHome := "my_java_home"
	obrs := make([]utils.MavenCoordinates, 0)
	obrs = append(
		obrs,
		utils.MavenCoordinates{
			GroupId:    "myGroup",
			ArtifactId: "myArtifact",
			Version:    "0.2",
			Classifier: "myClassifier",
		},
	)
	remoteMavenRepos := []string{"myRemoteMaven"}
	localMaven := ""
	galasaVersionToRun := "0.99.0"
	isTraceEnabled := true

	includesPatterns := []string{"*"}
	excludesPatterns := []string{"*ExcludeMe"}

	env := utils.NewMockEnv()
	galasaHome, _ := utils.NewGalasaHome(fs, env, "")

	return bootstrapProps, env, galasaHome, fs, javaHome, obrs,
		remoteMavenRepos, localMaven, galasaVersionToRun, isTraceEnabled,
		includesPatterns, excludesPatterns
}

func TestCleanupCommandSetsIncludesPatterns(t *testing.T) {

	bootstrapProps, _, galasaHome, fs,
		javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		isTraceEnabled,
		_,
		excludesPatterns := getDefaultResourceManagementCommandSyntaxParameters()

	includesPatterns := []string{"dev.galasa.*", "my.other.bundles.*", "*.more.bundles"}
	isDebugEnabled := false
	var debugPort uint32 = 0
	debugMode := ""

	cmd, args, err := getResourceManagementCommandSyntax(
		bootstrapProps,
		galasaHome,
		fs, javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		includesPatterns,
		excludesPatterns,
		BLANK_JWT,
	)

	assert.NotNil(t, cmd)
	assert.NotNil(t, args)
	assert.Nil(t, err)

	assert.Contains(t, args, "--includes-monitor-pattern")
	assert.Contains(t, args, "dev.galasa.*")
	assert.Contains(t, args, "my.other.bundles.*")
	assert.Contains(t, args, "*.more.bundles")
}

func TestCleanupCommandSetsExcludesPatterns(t *testing.T) {

	bootstrapProps, _, galasaHome, fs,
		javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		isTraceEnabled,
		includesPatterns,
		_ := getDefaultResourceManagementCommandSyntaxParameters()

	excludesPatterns := []string{"dev.galasa.*", "*ExcludeMe", "*.exclude.other.bundles"}
	isDebugEnabled := false
	var debugPort uint32 = 0
	debugMode := ""

	cmd, args, err := getResourceManagementCommandSyntax(
		bootstrapProps,
		galasaHome,
		fs, javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		includesPatterns,
		excludesPatterns,
		BLANK_JWT,
	)

	assert.NotNil(t, cmd)
	assert.NotNil(t, args)
	assert.Nil(t, err)

	assert.Contains(t, args, "--includes-monitor-pattern")
	assert.Contains(t, args, "dev.galasa.*")
	assert.Contains(t, args, "*ExcludeMe")
	assert.Contains(t, args, "*.exclude.other.bundles")
}

func TestCleanupCommandIncludesTraceWhenTraceIsEnabled(t *testing.T) {

	bootstrapProps, _, galasaHome, fs,
		javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		_,
		includesPatterns,
		excludesPatterns := getDefaultResourceManagementCommandSyntaxParameters()

	isTraceEnabled := true
	isDebugEnabled := false
	var debugPort uint32 = 0
	debugMode := ""

	cmd, args, err := getResourceManagementCommandSyntax(
		bootstrapProps,
		galasaHome,
		fs, javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		includesPatterns,
		excludesPatterns,
		BLANK_JWT,
	)

	assert.NotNil(t, cmd)
	assert.NotNil(t, args)
	assert.Nil(t, err)

	assert.Contains(t, args, "--trace")
}

func TestCleanupCommandDoesNotIncludeTraceWhenTraceIsDisabled(t *testing.T) {

	bootstrapProps, _, galasaHome, fs,
		javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		_,
		includesPatterns,
		excludesPatterns := getDefaultResourceManagementCommandSyntaxParameters()

	isTraceEnabled := false
	isDebugEnabled := false
	var debugPort uint32 = 0
	debugMode := ""

	cmd, args, err := getResourceManagementCommandSyntax(
		bootstrapProps,
		galasaHome,
		fs, javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		includesPatterns,
		excludesPatterns,
		BLANK_JWT,
	)

	assert.NotNil(t, cmd)
	assert.NotNil(t, args)
	assert.Nil(t, err)

	assert.NotContains(t, args, "--trace")
}

func TestCleanupCommandSyntaxContainsJavaHomeUnixSlashes(t *testing.T) {
	bootstrapProps, _, galasaHome, fs,
		_,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		isTraceEnabled,
		includesPatterns,
		excludesPatterns := getDefaultResourceManagementCommandSyntaxParameters()

	javaHome := "myJavaHome"
	fs.SetFilePathSeparator("/")
	isDebugEnabled := false
	var debugPort uint32 = 0
	debugMode := ""

	cmd, args, err := getResourceManagementCommandSyntax(
		bootstrapProps,
		galasaHome,
		fs, javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		includesPatterns,
		excludesPatterns,
		BLANK_JWT,
	)

	assert.NotNil(t, cmd)
	assert.NotNil(t, args)
	assert.Nil(t, err)

	assert.Equal(t, cmd, "myJavaHome/bin/java")
}

func TestCleanupCommandSyntaxContainsJavaHomeWindowsSlashes(t *testing.T) {
	bootstrapProps, _, galasaHome, fs,
		_,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		isTraceEnabled,
		includesPatterns,
		excludesPatterns := getDefaultResourceManagementCommandSyntaxParameters()

	javaHome := "myJavaHome"
	fs.SetFilePathSeparator("\\")
	fs.SetExecutableExtension(".exe")

	isDebugEnabled := false
	var debugPort uint32 = 0
	debugMode := ""

	cmd, args, err := getResourceManagementCommandSyntax(
		bootstrapProps,
		galasaHome,
		fs, javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		includesPatterns,
		excludesPatterns,
		BLANK_JWT,
	)

	assert.NotNil(t, cmd)
	assert.NotNil(t, args)
	assert.Nil(t, err)

	assert.Equal(t, cmd, "myJavaHome\\bin\\java")
}

func TestCleanupCommandIncludesGALASA_HOMESystemProperty(t *testing.T) {

	bootstrapProps, _, galasaHome, fs,
		javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		_,
		includesPatterns,
		excludesPatterns := getDefaultResourceManagementCommandSyntaxParameters()

	isTraceEnabled := true
	isDebugEnabled := false
	var debugPort uint32 = 0
	debugMode := ""

	cmd, args, err := getResourceManagementCommandSyntax(
		bootstrapProps,
		galasaHome,
		fs, javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		includesPatterns,
		excludesPatterns,
		BLANK_JWT,
	)

	assert.NotNil(t, cmd)
	assert.NotNil(t, args)
	assert.Nil(t, err)

	assert.Contains(t, args, `-DGALASA_HOME="/User/Home/testuser/.galasa"`)
}

func TestCleeanupCommandAllDashDSystemPropertiesPassedAppearBeforeTheDashJar(t *testing.T) {

	bootstrapProps, _, galasaHome, fs,
		javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		_,
		includesPatterns,
		excludesPatterns := getDefaultResourceManagementCommandSyntaxParameters()

	isTraceEnabled := true
	isDebugEnabled := false
	var debugPort uint32 = 0
	debugMode := ""

	cmd, args, err := getResourceManagementCommandSyntax(
		bootstrapProps,
		galasaHome,
		fs, javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		includesPatterns,
		excludesPatterns,
		BLANK_JWT,
	)

	assert.NotNil(t, cmd)
	assert.NotNil(t, args)
	assert.Nil(t, err)

	// Combine all arguments into a single string.
	allArgs := strings.Join(args, " ")

	allDashDIndexes := getAllIndexesOfSubstring(allArgs, "-D")
	allDashJarIndexes := getAllIndexesOfSubstring(allArgs, "-jar")

	assert.Equal(t, 1, len(allDashJarIndexes), "-jar option is found in command launch parameters an unexpected number of times")
	dashJarIndex := allDashJarIndexes[0]
	for _, dashDIndex := range allDashDIndexes {
		assert.Less(t, dashDIndex, dashJarIndex, "A -Dxxx parameter is found after the -jar parameter, so will do nothing. -D parameters should appear before the -jar parameter")
	}
}

func TestCleanupCommandIncludesFlagsFromBootstrapProperties(t *testing.T) {
	bootstrapProps, _, galasaHome, fs,
		javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		_,
		includesPatterns,
		excludesPatterns := getDefaultResourceManagementCommandSyntaxParameters()

	isTraceEnabled := false
	isDebugEnabled := false
	var debugPort uint32 = 0
	debugMode := ""

	cmd, args, err := getResourceManagementCommandSyntax(
		bootstrapProps,
		galasaHome,
		fs, javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		includesPatterns,
		excludesPatterns,
		BLANK_JWT,
	)

	assert.NotNil(t, cmd)
	assert.NotNil(t, args)
	assert.Nil(t, err)

	assert.Contains(t, args, "-Xmx80m")
}

func TestCleanupCommandIncludesDefaultDebugPortAndMode(t *testing.T) {
	bootstrapProps, _, galasaHome, fs,
		javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		_,
		includesPatterns,
		excludesPatterns := getDefaultResourceManagementCommandSyntaxParameters()

	isTraceEnabled := false
	isDebugEnabled := true // <<<< Debug is turned on. No overrides to debugPort in either boostrap or explicit command option.
	var debugPort uint32 = 0
	debugMode := ""

	cmd, args, err := getResourceManagementCommandSyntax(
		bootstrapProps,
		galasaHome,
		fs, javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		includesPatterns,
		excludesPatterns,
		BLANK_JWT,
	)

	assert.NotNil(t, cmd)
	assert.NotNil(t, args)
	assert.Nil(t, err)

	assert.Contains(t, args, "-agentlib:jdwp=transport=dt_socket,address=*:"+strconv.FormatUint(uint64(DEBUG_PORT_DEFAULT), 10)+",server=y,suspend=y")
}

func TestCleanupCommandDrawsValidDebugPortFromBootstrap(t *testing.T) {
	bootstrapProps, _, galasaHome, fs,
		javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		_,
		includesPatterns,
		excludesPatterns := getDefaultResourceManagementCommandSyntaxParameters()

	isTraceEnabled := false
	isDebugEnabled := true // <<<< Debug is turned on. No overrides to debugPort in either boostrap or explicit command option.
	var debugPort uint32 = 0
	debugMode := ""

	bootstrapProps[api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_DEBUG_PORT] = "345"

	cmd, args, err := getResourceManagementCommandSyntax(
		bootstrapProps,
		galasaHome,
		fs, javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		includesPatterns,
		excludesPatterns,
		BLANK_JWT,
	)

	assert.NotNil(t, cmd)
	assert.NotNil(t, args)
	assert.Nil(t, err)

	assert.Contains(t, args, "-agentlib:jdwp=transport=dt_socket,address=*:345,server=y,suspend=y")
}

func TestCleanupCommandDrawsInvalidDebugPortFromBootstrap(t *testing.T) {
	bootstrapProps, _, galasaHome, fs,
		javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		_,
		includesPatterns,
		excludesPatterns := getDefaultResourceManagementCommandSyntaxParameters()

	isTraceEnabled := false
	isDebugEnabled := true // <<<< Debug is turned on. No overrides to debugPort in either boostrap or explicit command option.
	var debugPort uint32 = 0
	debugMode := ""

	bootstrapProps[api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_DEBUG_PORT] = "-456"

	_, _, err := getResourceManagementCommandSyntax(
		bootstrapProps,
		galasaHome,
		fs, javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		includesPatterns,
		excludesPatterns,
		BLANK_JWT,
	)

	assert.NotNil(t, err)

	assert.Contains(t, err.Error(), "-456")
	assert.Contains(t, err.Error(), api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_DEBUG_PORT)
	assert.Contains(t, err.Error(), "GAL1072E")
}

func TestCleanupCommandDrawsValidDebugModeFromBootstrap(t *testing.T) {
	bootstrapProps, _, galasaHome, fs,
		javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		_,
		includesPatterns,
		excludesPatterns := getDefaultResourceManagementCommandSyntaxParameters()

	isTraceEnabled := false
	isDebugEnabled := true // <<<< Debug is turned on. No overrides to debugPort in either boostrap or explicit command option.
	var debugPort uint32 = 0
	debugMode := ""

	bootstrapProps[api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_DEBUG_MODE] = "attach"

	cmd, args, err := getResourceManagementCommandSyntax(
		bootstrapProps,
		galasaHome,
		fs, javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		includesPatterns,
		excludesPatterns,
		BLANK_JWT,
	)

	assert.NotNil(t, cmd)
	assert.NotNil(t, args)
	assert.Nil(t, err)

	assert.Contains(
		t,
		args,
		"-agentlib:jdwp=transport=dt_socket,address=*:"+
			strconv.FormatUint(uint64(DEBUG_PORT_DEFAULT), 10)+
			",server=n,suspend=y",
	)
}

func TestCleanupCommandDrawsInvalidDebugModeFromBootstrap(t *testing.T) {
	bootstrapProps, _, galasaHome, fs,
		javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		_,
		includesPatterns,
		excludesPatterns := getDefaultResourceManagementCommandSyntaxParameters()

	isTraceEnabled := false
	isDebugEnabled := true // <<<< Debug is turned on. No overrides to debugPort in either boostrap or explicit command option.
	var debugPort uint32 = 0
	debugMode := ""

	bootstrapProps[api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_DEBUG_MODE] = "shout" //  << Invalid !

	_, _, err := getResourceManagementCommandSyntax(
		bootstrapProps,
		galasaHome,
		fs, javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		includesPatterns,
		excludesPatterns,
		BLANK_JWT,
	)

	assert.NotNil(t, err)

	assert.Contains(t, err.Error(), "shout")
	assert.Contains(t, err.Error(), api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_DEBUG_MODE)
	assert.Contains(t, err.Error(), "GAL1070E")
}

func TestCleanupCommandDrawsValidDebugModeListenFromCommandLine(t *testing.T) {
	bootstrapProps, _, galasaHome, fs,
		javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		_,
		includesPatterns,
		excludesPatterns := getDefaultResourceManagementCommandSyntaxParameters()

	isTraceEnabled := false
	isDebugEnabled := true // <<<< Debug is turned on. No overrides to debugPort in either boostrap or explicit command option.
	var debugPort uint32 = 0
	debugMode := "listen"

	cmd, args, err := getResourceManagementCommandSyntax(
		bootstrapProps,
		galasaHome,
		fs, javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		includesPatterns,
		excludesPatterns,
		BLANK_JWT,
	)

	assert.NotNil(t, cmd)
	assert.NotNil(t, args)
	assert.Nil(t, err)

	assert.Contains(
		t,
		args,
		"-agentlib:jdwp=transport=dt_socket,address=*:"+
			strconv.FormatUint(uint64(DEBUG_PORT_DEFAULT), 10)+
			",server=y,suspend=y",
	)
}

func TestCleanupCommandDrawsValidDebugModeAttachFromCommandLine(t *testing.T) {
	bootstrapProps, _, galasaHome, fs,
		javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		_,
		includesPatterns,
		excludesPatterns := getDefaultResourceManagementCommandSyntaxParameters()

	isTraceEnabled := false
	isDebugEnabled := true // <<<< Debug is turned on. No overrides to debugPort in either boostrap or explicit command option.
	var debugPort uint32 = 0
	debugMode := "attach"

	cmd, args, err := getResourceManagementCommandSyntax(
		bootstrapProps,
		galasaHome,
		fs, javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		includesPatterns,
		excludesPatterns,
		BLANK_JWT,
	)

	assert.NotNil(t, cmd)
	assert.NotNil(t, args)
	assert.Nil(t, err)

	assert.Contains(
		t,
		args,
		"-agentlib:jdwp=transport=dt_socket,address=*:"+
			strconv.FormatUint(uint64(DEBUG_PORT_DEFAULT), 10)+
			",server=n,suspend=y",
	)
}

func TestCleanupCommandDrawsInvalidDebugModeFromCommandLine(t *testing.T) {
	bootstrapProps, _, galasaHome, fs,
		javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		_,
		includesPatterns,
		excludesPatterns := getDefaultResourceManagementCommandSyntaxParameters()

	isTraceEnabled := false
	isDebugEnabled := true // <<<< Debug is turned on. No overrides to debugPort in either boostrap or explicit command option.
	var debugPort uint32 = 0
	debugMode := "invalidMode"

	_, _, err := getResourceManagementCommandSyntax(
		bootstrapProps,
		galasaHome,
		fs, javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		includesPatterns,
		excludesPatterns,
		BLANK_JWT,
	)

	assert.NotNil(t, err)

	assert.Contains(t, err.Error(), "invalidMode")
	assert.Contains(t, err.Error(), api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_DEBUG_MODE)
	assert.Contains(t, err.Error(), "GAL1071E")
}

func TestCleanupCommandLocalMavenNotSetDefaults(t *testing.T) {
	// Given...
	bootstrapProps, _, galasaHome, fs,
		javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		_,
		includesPatterns,
		excludesPatterns := getDefaultResourceManagementCommandSyntaxParameters()

	isTraceEnabled := false
	isDebugEnabled := false // <<<< Debug is turned on. No overrides to debugPort in either boostrap or explicit command option.
	var debugPort uint32 = 0
	debugMode := ""

	// When...
	_, args, err := getResourceManagementCommandSyntax(
		bootstrapProps,
		galasaHome,
		fs, javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		includesPatterns,
		excludesPatterns,
		BLANK_JWT,
	)

	// Then...
	assert.Nil(t, err)

	assert.Contains(t, args, "--localmaven")
	assert.Contains(t, args, "file:////User/Home/testuser/.m2/repository")
}

func TestCleanupCommandLocalMavenSet(t *testing.T) {
	// For...
	bootstrapProps, _, galasaHome, fs,
		javaHome,
		obrs,
		remoteMavenRepos,
		_,
		galasaVersionToRun,
		_,
		includesPatterns,
		excludesPatterns := getDefaultResourceManagementCommandSyntaxParameters()

	isTraceEnabled := false
	isDebugEnabled := false // <<<< Debug is turned on. No overrides to debugPort in either boostrap or explicit command option.
	var debugPort uint32 = 0
	debugMode := ""
	localMaven := "mavenRepo"

	// When...
	_, args, err := getResourceManagementCommandSyntax(
		bootstrapProps,
		galasaHome,
		fs, javaHome,
		obrs,
		remoteMavenRepos,
		localMaven,
		galasaVersionToRun,
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		includesPatterns,
		excludesPatterns,
		BLANK_JWT,
	)

	// Then...
	assert.Nil(t, err)

	assert.Contains(t, args, "--localmaven")
	assert.Contains(t, args, "mavenRepo")
}

