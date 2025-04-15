/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package launcher

import (
	"log"
	"strconv"
	"strings"
	"testing"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/files"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/props"
	"github.com/galasa-dev/cli/pkg/spi"

	"github.com/galasa-dev/cli/pkg/embedded"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

var (
	validStructureJsonFileContent = `{
        "runName": "L0",
        "bundle": "dev.galasa.examples.banking.account",
        "testName": "dev.galasa.examples.banking.account.TestAccount",
        "testShortName": "TestAccount",
        "requestor": "unknown",
        "status": "finished",
        "result": "Passed",
        "queued": "2024-03-14T10:11:02.185556Z",
        "startTime": "2024-03-14T10:11:02.221697Z",
        "endTime": "2024-03-14T10:11:02.439849Z",
        "methods": [
          {
            "className": "dev.galasa.examples.banking.account.TestAccount",
            "methodName": "simpleSampleTest",
            "type": "Test",
            "befores": [],
            "afters": [],
            "status": "finished",
            "result": "Passed",
            "runLogStart": 0,
            "runLogEnd": 0,
            "startTime": "2024-03-14T10:11:02.413588Z",
            "endTime": "2024-03-14T10:11:02.428472Z"
        }
      ]
    }`
)

const (
	BLANK_JWT = ""
)

func NewMockLauncherParams() (
	props.JavaProperties,
	*utils.MockEnv,
	spi.FileSystem,
	embedded.ReadOnlyFileSystem,
	*RunsSubmitLocalCmdParameters,
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
	jvmLaunchParams := getBasicJvmLaunchParams()
	timeService := utils.NewMockTimeService()
	timedSleeper := utils.NewRealTimedSleeper()
	mockProcess := NewMockProcess()
	mockProcessFactory := NewMockProcessFactory(mockProcess)

	bootstrapProps := getBasicBootstrapProperties()

	return bootstrapProps, env, fs, embedded.GetReadOnlyFileSystem(),
		jvmLaunchParams, timeService, timedSleeper, mockProcessFactory, galasaHome
}

func TestCanCreateAJVMLauncher(t *testing.T) {

	env := utils.NewMockEnv()
	env.EnvVars["JAVA_HOME"] = "/java"

	fs := files.NewMockFileSystem()
	utils.AddJavaRuntimeToMock(fs, "/java")

	galasaHome, _ := utils.NewGalasaHome(fs, env, "")

	jvmLaunchParams := getBasicJvmLaunchParams()
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

	launcher, err := NewJVMLauncher(
		mockFactory,
		bootstrapProps, embedded.GetReadOnlyFileSystem(),
		jvmLaunchParams, mockProcessFactory, galasaHome, timedSleeper,
	)
	if err != nil {
		assert.Fail(t, "Constructor should not have failed but it did. error:%s", err.Error())
	}
	assert.NotNil(t, launcher, "Launcher reference was nil, shouldn't have been.")
}

func getBasicJvmLaunchParams() *RunsSubmitLocalCmdParameters {
	return &RunsSubmitLocalCmdParameters{
		Obrs:                nil,
		RemoteMaven:         "",
		TargetGalasaVersion: "",
	}
}

func getBasicBootstrapProperties() props.JavaProperties {
	props := props.JavaProperties{}
	props[api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_OPTIONS] = "-Xmx80m"
	return props
}

func TestCantCreateAJVMLauncherIfJVMHomeNotSet(t *testing.T) {

	env := utils.NewMockEnv()
	// env.EnvVars["JAVA_HOME"] = "/java"

	fs := files.NewMockFileSystem()
	utils.AddJavaRuntimeToMock(fs, "/java")

	galasaHome, _ := utils.NewGalasaHome(fs, env, "")

	bootstrapProps := getBasicBootstrapProperties()

	jvmLaunchParams := getBasicJvmLaunchParams()
	timeService := utils.NewMockTimeService()
	timedSleeper := utils.NewRealTimedSleeper()

	mockProcess := NewMockProcess()
	mockProcessFactory := NewMockProcessFactory(mockProcess)

	mockFactory := &utils.MockFactory{
		Env:         env,
		FileSystem:  fs,
		TimeService: timeService,
	}

	launcher, err := NewJVMLauncher(
		mockFactory,
		bootstrapProps, embedded.GetReadOnlyFileSystem(),
		jvmLaunchParams, mockProcessFactory, galasaHome, timedSleeper,
	)
	if err == nil {
		assert.Fail(t, "Constructor should have failed but it did not.")
	}
	assert.Nil(t, launcher, "Launcher reference was not nil")
	assert.Contains(t, err.Error(), "GAL1050E")
}

func TestCanCreateJvmLauncher(t *testing.T) {
	env := utils.NewMockEnv()
	env.EnvVars["JAVA_HOME"] = "/java"

	fs := files.NewMockFileSystem()
	utils.AddJavaRuntimeToMock(fs, "/java")

	jvmLaunchParams := getBasicJvmLaunchParams()
	timeService := utils.NewMockTimeService()
	timedSleeper := utils.NewRealTimedSleeper()
	mockProcess := NewMockProcess()
	mockProcessFactory := NewMockProcessFactory(mockProcess)
	galasaHome, _ := utils.NewGalasaHome(fs, env, "")

	bootstrapProps := getBasicBootstrapProperties()

	mockFactory := &utils.MockFactory{
		Env:         env,
		FileSystem:  fs,
		TimeService: timeService,
	}

	launcher, err := NewJVMLauncher(
		mockFactory,
		bootstrapProps, embedded.GetReadOnlyFileSystem(),
		jvmLaunchParams, mockProcessFactory, galasaHome, timedSleeper,
	)

	if err != nil {
		assert.Fail(t, "JVM launcher should have been creatable.")
	}
	assert.NotNil(t, launcher, "Launcher returned is nil!")
}

func TestCanLaunchLocalJvmTest(t *testing.T) {
	// Given...
	bootstrapProps, env, fs, embeddedReadOnlyFS,
		jvmLaunchParams, timeService, timedSleeper, mockProcessFactory, galasaHome := NewMockLauncherParams()

	mockFactory := &utils.MockFactory{
		Env:         env,
		FileSystem:  fs,
		TimeService: timeService,
	}

	launcher, err := NewJVMLauncher(
		mockFactory,
		bootstrapProps, embeddedReadOnlyFS,
		jvmLaunchParams, mockProcessFactory, galasaHome, timedSleeper,
	)

	if err != nil {
		assert.Fail(t, "JVM launcher should have been creatable.")
	}
	assert.NotNil(t, launcher, "Launcher returned is nil!")

	isTraceEnabled := true
	var overrides map[string]interface{} = make(map[string]interface{})

	// When...
	testRuns, err := launcher.SubmitTestRun(
		"myGroup",
		"galasa.dev.example.banking.account/galasa.dev.example.banking.account.TestAccount",
		"myRequestType-UnitTest",
		"myRequestor",
		"unitTestStream",
		"mvn:myGroup/myArtifact/myClassifier/obr",
		isTraceEnabled,
		"", // No Gherkin URL supplied
		"", // No Gherkin Feature supplied
		overrides,
	)
	if err != nil {
		assert.Fail(t, "Launcher should have launched command OK")
	} else {
		assert.NotNil(t, testRuns, "Returned test runs is nil, should have been an object.")

		assert.Len(t, testRuns.Runs, 1, "Returned test runs array doesn't contain correct number of tests launched.")
		assert.False(t, *testRuns.Complete, "Returned test runs should not already be complete")
	}
}

func TestCanGetRunGroupStatus(t *testing.T) {
	// Given...
	env := utils.NewMockEnv()
	env.EnvVars["JAVA_HOME"] = "/java"

	fs := files.NewMockFileSystem()
	utils.AddJavaRuntimeToMock(fs, "/java")

	galasaHome, _ := utils.NewGalasaHome(fs, env, "")

	jvmLaunchParams := getBasicJvmLaunchParams()
	timeService := utils.NewMockTimeService()

	mockProcess := NewMockProcess()
	mockProcessFactory := NewMockProcessFactory(mockProcess)

	bootstrapProps := getBasicBootstrapProperties()

	mockFactory := &utils.MockFactory{
		Env:         env,
		FileSystem:  fs,
		TimeService: timeService,
	}

	launcher, err := NewJVMLauncher(
		mockFactory,
		bootstrapProps, embedded.GetReadOnlyFileSystem(),
		jvmLaunchParams, mockProcessFactory, galasaHome, utils.NewRealTimedSleeper(),
	)
	if err != nil {
		assert.Fail(t, "Launcher should have launched command OK")
	}

	isTraceEnabled := true
	var overrides map[string]interface{} = make(map[string]interface{})

	launcher.SubmitTestRun(
		"myGroup",
		"galasa.dev.example.banking.account/galasa.dev.example.banking.account.TestAccount",
		"myRequestType-UnitTest",
		"myRequestor",
		"unitTestStream",
		"mvn:myGroup/myArtifact/myClassifier/obr",
		isTraceEnabled,
		"", // No Gherkin URL supplied
		"", // No Gherkin Feature supplied
		overrides,
	)

	// Wait for the child process to complete...
	mockProcess.Wait()

	// Simulate the test writing some final status to disk.
	structureJsonContent := `
	{
		"runName": "U2",
		"bundle": "dev.galasa.example.banking.account",
		"testName": "dev.galasa.example.banking.account.TestAccountExtended",
		"testShortName": "TestAccountExtended",
		"requestor": "unknown",
		"status": "finished",
		"result": "Passed",
		"group": "none",
		"queued": "2023-02-17T16:24:52.041118Z",
		"startTime": "2023-02-17T16:24:52.068591Z",
		"endTime": "2023-02-17T16:24:52.268396Z",
		"methods": [
		  {
			"className": "dev.galasa.example.banking.account.TestAccountExtended",
			"methodName": "simpleSampleTest",
			"type": "Test",
			"befores": [],
			"afters": [],
			"status": "finished",
			"result": "Passed",
			"runLogStart": 0,
			"runLogEnd": 0,
			"startTime": "2023-02-17T16:24:52.238868Z",
			"endTime": "2023-02-17T16:24:52.263756Z"
		  },
		  {
			"className": "dev.galasa.example.banking.account.TestAccountExtended",
			"methodName": "testRetrieveBundleResourceFileAsStringMethod",
			"type": "Test",
			"befores": [],
			"afters": [],
			"status": "finished",
			"result": "Passed",
			"runLogStart": 0,
			"runLogEnd": 0,
			"startTime": "2023-02-17T16:24:52.264511Z",
			"endTime": "2023-02-17T16:24:52.265325Z"
		  }
		]
	}`
	fs.WriteTextFile("/temp/ras/L12345/structure.json", structureJsonContent)

	// When...
	testRuns, err := launcher.GetRunsByGroup("myGroup")

	// Then...
	if err != nil {
		assert.Fail(t, "Launcher should have returned some test status")
	} else {
		assert.NotNil(t, testRuns, "Returned test runs status is nil, should have been an object.")

		assert.Len(t, testRuns.Runs, 1, "Returned test runs array doesn't contain correct number of tests launched.")
		log.Printf("getRunsByGroup returned *testRUns.Complete of %v", *testRuns.Complete)
		if !*testRuns.Complete {
			assert.Fail(t, "Returned test runs should all be marked as complete")
		}
	}
}

func TestJvmLauncherSetsRASStoreOverride(t *testing.T) {
	overrides := make(map[string]interface{})
	fs := files.NewMockFileSystem()
	env := utils.NewMockEnv()
	galasaHome, _ := utils.NewGalasaHome(fs, env, "")

	overridesGotBack := addStandardOverrideProperties(galasaHome, overrides)
	assert.Contains(t, overridesGotBack, "framework.resultarchive.store")
}

func TestJvmLauncherSets3270TerminalOutputFormatProperty(t *testing.T) {
	overrides := make(map[string]interface{})
	fs := files.NewMockFileSystem()
	env := utils.NewMockEnv()
	galasaHome, _ := utils.NewGalasaHome(fs, env, "")

	overridesGotBack := addStandardOverrideProperties(galasaHome, overrides)

	assert.Contains(t, overridesGotBack, "zos3270.terminal.output")
	assert.Contains(t, overridesGotBack["zos3270.terminal.output"], "png")
	assert.Contains(t, overridesGotBack["zos3270.terminal.output"], "json")
}

func TestCanCreateTempPropsFile(t *testing.T) {
	overrides := make(map[string]interface{})
	fs := files.NewMockFileSystem()
	env := utils.NewMockEnv()
	galasaHome, _ := utils.NewGalasaHome(fs, env, "")

	// When
	tempFolder, tempPropsFile, err := prepareTempFiles(galasaHome, fs, overrides)

	// Then the temp folder should exist.
	assert.Nil(t, err)
	assert.NotEmpty(t, tempFolder)
	exists, err := fs.DirExists(tempFolder)
	assert.Nil(t, err)
	assert.True(t, exists)

	// The temp property file should exist
	assert.NotEmpty(t, tempPropsFile)
	overridesGotBack, err := props.ReadPropertiesFile(fs, tempPropsFile)
	assert.Nil(t, err)
	assert.Contains(t, overridesGotBack, "framework.resultarchive.store")
	assert.Contains(t, overridesGotBack, "framework.request.type.LOCAL.prefix")
}

func TestBadlyFormedObrFromProfileInfoCausesError(t *testing.T) {

	// Given...
	bootstrapProps, env, fs, embeddedReadOnlyFS,
		jvmLaunchParams, timeService, timedSleeper, mockProcessFactory, galasaHome := NewMockLauncherParams()

	mockFactory := &utils.MockFactory{
		Env:         env,
		FileSystem:  fs,
		TimeService: timeService,
	}

	launcher, _ := NewJVMLauncher(
		mockFactory,
		bootstrapProps, embeddedReadOnlyFS,
		jvmLaunchParams, mockProcessFactory, galasaHome, timedSleeper)

	isTraceEnabled := true
	var overrides map[string]interface{} = make(map[string]interface{})

	// When...
	var err error
	_, err = launcher.SubmitTestRun(
		"myGroup",
		"galasa.dev.example.banking.account/galasa.dev.example.banking.account.TestAccount",
		"myRequestType-UnitTest",
		"myRequestor",
		"unitTestStream",
		"notmaven://group/artifact/version/classifier",
		isTraceEnabled,
		"", // No Gherkin URL supplied
		"", // No Gherkin Feature supplied
		overrides,
	)

	assert.NotNil(t, err)
	if err != nil {
		// Expect badly formed OBR
		assert.Contains(t, err.Error(), "GAL1061E:")
	}
}

func TestNoObrsFromParameterOrProfileCausesError(t *testing.T) {

	// Given...
	bootstrapProps, env, fs, embeddedReadOnlyFS,
		jvmLaunchParams, timeService, timedSleeper, mockProcessFactory, galasaHome := NewMockLauncherParams()

	mockFactory := &utils.MockFactory{
		Env:         env,
		FileSystem:  fs,
		TimeService: timeService,
	}
	launcher, _ := NewJVMLauncher(
		mockFactory,
		bootstrapProps, embeddedReadOnlyFS,
		jvmLaunchParams, mockProcessFactory, galasaHome, timedSleeper)

	isTraceEnabled := true
	var overrides map[string]interface{} = make(map[string]interface{})

	// Empty list of obrs from the command-line.
	jvmLaunchParams.Obrs = make([]string, 0)

	// When...
	var err error
	_, err = launcher.SubmitTestRun(
		"myGroup",
		"galasa.dev.example.banking.account/galasa.dev.example.banking.account.TestAccount",
		"myRequestType-UnitTest",
		"myRequestor",
		"unitTestStream",
		"", // No Obr from the profile record.
		isTraceEnabled,
		"", // No Gherkin URL supplied
		"", // No Gherkin Feature supplied
		overrides,
	)

	assert.NotNil(t, err)
	if err != nil {
		// Expect Can't run this test because no obr information specified.
		assert.Contains(t, err.Error(), "GAL1094E:")
	}
}

func getDefaultCommandSyntaxTestParameters() (
	props.JavaProperties,
	spi.Environment,
	spi.GalasaHome,
	*files.MockFileSystem,
	string,
	[]utils.MavenCoordinates,
	TestLocation,
	string,
	string,
	string,
	string,
	bool,
) {
	bootstrapProps := getBasicBootstrapProperties()
	fs := files.NewOverridableMockFileSystem()
	javaHome := "my_java_home"
	testObrs := make([]utils.MavenCoordinates, 0)
	testObrs = append(
		testObrs,
		utils.MavenCoordinates{
			GroupId:    "myGroup",
			ArtifactId: "myArtifact",
			Version:    "0.2",
			Classifier: "myClassifier",
		},
	)
	testLocation := TestLocation{
		OSGiBundleName:         "myTestBundle",
		QualifiedJavaClassName: "myClass",
	}
	remoteMaven := "myRemoteMaven"
	localMaven := ""
	galasaVersionToRun := "0.99.0"
	overridesFilePath := "C:/myFolder/myOverrides.props"
	isTraceEnabled := true

	env := utils.NewMockEnv()
	galasaHome, _ := utils.NewGalasaHome(fs, env, "")

	return bootstrapProps, env, galasaHome, fs, javaHome, testObrs, testLocation,
		remoteMaven, localMaven, galasaVersionToRun, overridesFilePath, isTraceEnabled
}

func TestCommandIncludesTraceWhenTraceIsEnabled(t *testing.T) {

	bootstrapProps, _, galasaHome, fs,
		javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		_ := getDefaultCommandSyntaxTestParameters()

	isTraceEnabled := true
	isDebugEnabled := false
	var debugPort uint32 = 0
	debugMode := ""

	cmd, args, err := getCommandSyntax(
		bootstrapProps,
		galasaHome,
		fs, javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		"", // No Gherkin URL supplied
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		BLANK_JWT,
	)

	assert.NotNil(t, cmd)
	assert.NotNil(t, args)
	assert.Nil(t, err)

	assert.Contains(t, args, "--trace")
}

func TestCommandDoesNotIncludeTraceWhenTraceIsDisabled(t *testing.T) {
	bootstrapProps, _, galasaHome, fs,
		javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		_ := getDefaultCommandSyntaxTestParameters()

	isTraceEnabled := false
	isDebugEnabled := false
	var debugPort uint32 = 0
	debugMode := ""

	cmd, args, err := getCommandSyntax(
		bootstrapProps, galasaHome, fs, javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		"", // No Gherkin URL supplied
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode, BLANK_JWT,
	)

	assert.NotNil(t, cmd)
	assert.NotNil(t, args)
	assert.Nil(t, err)

	assert.NotContains(t, args, "--trace")
}

func TestCommandSyntaxContainsJavaHomeUnixSlashes(t *testing.T) {
	bootstrapProps, _, galasaHome, fs,
		_,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		isTraceEnabled := getDefaultCommandSyntaxTestParameters()

	javaHome := "myJavaHome"
	fs.SetFilePathSeparator("/")
	isDebugEnabled := false
	var debugPort uint32 = 0
	debugMode := ""

	cmd, args, err := getCommandSyntax(
		bootstrapProps, galasaHome, fs, javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		"", // No Gherkin URL supplied
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		BLANK_JWT,
	)

	assert.NotNil(t, cmd)
	assert.NotNil(t, args)
	assert.Nil(t, err)

	assert.Equal(t, cmd, "myJavaHome/bin/java")
}

func TestCommandSyntaxContainsJavaHomeWindowsSlashes(t *testing.T) {
	bootstrapProps, _, galasaHome, fs,
		_,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		isTraceEnabled := getDefaultCommandSyntaxTestParameters()

	javaHome := "myJavaHome"
	fs.SetFilePathSeparator("\\")
	fs.SetExecutableExtension(".exe")

	isDebugEnabled := false
	var debugPort uint32 = 0
	debugMode := ""

	cmd, args, err := getCommandSyntax(
		bootstrapProps, galasaHome, fs, javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		"", // No Gherkin URL supplied
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		BLANK_JWT,
	)

	assert.NotNil(t, cmd)
	assert.NotNil(t, args)
	assert.Nil(t, err)

	assert.Equal(t, cmd, "myJavaHome\\bin\\java")
}

func TestValidClassInputGetsSplitCorrectly(t *testing.T) {
	userInput := "myBundle/myClass"
	testLocation, err := classNameUserInputToTestClassLocation(userInput)
	assert.NotNil(t, testLocation)
	assert.Nil(t, err)
	assert.Equal(t, testLocation.OSGiBundleName, "myBundle")
	assert.Equal(t, testLocation.QualifiedJavaClassName, "myClass")
}

func TestInvalidClassInputNoSlashGetsError(t *testing.T) {
	userInput := "myBundleNoSlashmyClass"
	testLocation, err := classNameUserInputToTestClassLocation(userInput)
	assert.Nil(t, testLocation)
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1064E")
}

func TestInvalidClassInputManySlashesGetsError(t *testing.T) {
	userInput := "myBundle/With/More/Slashes/Class"
	testLocation, err := classNameUserInputToTestClassLocation(userInput)
	assert.Nil(t, testLocation)
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1065E")
}

func TestInvalidClassInputWithClassSuffixGetsError(t *testing.T) {
	userInput := "myBundle/myClass.class"
	testLocation, err := classNameUserInputToTestClassLocation(userInput)
	assert.Nil(t, testLocation)
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1066E")
}

func TestCommandIncludesGALASA_HOMESystemProperty(t *testing.T) {

	bootstrapProps, _, galasaHome, fs,
		javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		_ := getDefaultCommandSyntaxTestParameters()

	isTraceEnabled := true
	isDebugEnabled := false
	var debugPort uint32 = 0
	debugMode := ""

	cmd, args, err := getCommandSyntax(
		bootstrapProps,
		galasaHome,
		fs, javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		"", // No Gherkin URL supplied
		isTraceEnabled,
		isDebugEnabled,
		debugPort,
		debugMode,
		BLANK_JWT,
	)

	assert.NotNil(t, cmd)
	assert.NotNil(t, args)
	assert.Nil(t, err)

	assert.Contains(t, args, `-DGALASA_HOME="/User/Home/testuser/.galasa"`)
}

func TestCommandAllDashDSystemPropertiesPassedAppearBeforeTheDashJar(t *testing.T) {

	bootstrapProps, _, galasaHome, fs,
		javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		_ := getDefaultCommandSyntaxTestParameters()

	isTraceEnabled := true
	isDebugEnabled := false
	var debugPort uint32 = 0
	debugMode := ""

	cmd, args, err := getCommandSyntax(
		bootstrapProps,
		galasaHome,
		fs, javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		"", // No Gherkin URL supplied
		isTraceEnabled,
		isDebugEnabled,
		debugPort,
		debugMode,
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

func TestFindingIndexesOfSubstring(t *testing.T) {
	originalString := "012345678901234567890"
	indexes := getAllIndexesOfSubstring(originalString, "01")
	if assert.Equal(t, 2, len(indexes)) {
		assert.Equal(t, 0, indexes[0])
		assert.Equal(t, 10, indexes[1])
	}
}

func getAllIndexesOfSubstring(originalString string, subString string) []int {
	var result []int = make([]int, 0)

	searchString := originalString
	charactersProcessed := 0
	isDone := false
	for !isDone {

		index := strings.Index(searchString, subString)
		if index == -1 {
			isDone = true
		} else {
			result = append(result, index+charactersProcessed)
			searchString = searchString[index+1:]
			charactersProcessed += index + 1
		}
	}

	return result
}

func TestCommandIncludesFlagsFromBootstrapProperties(t *testing.T) {
	bootstrapProps, _, galasaHome, fs,
		javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		_ := getDefaultCommandSyntaxTestParameters()

	isTraceEnabled := false
	isDebugEnabled := false
	var debugPort uint32 = 0
	debugMode := ""

	cmd, args, err := getCommandSyntax(
		bootstrapProps, galasaHome, fs, javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		"", // No Gherkin URL supplied
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		BLANK_JWT,
	)

	assert.NotNil(t, cmd)
	assert.NotNil(t, args)
	assert.Nil(t, err)

	assert.Contains(t, args, "-Xmx80m")
}

func TestCommandIncludesTwoFlagsFromBootstrapProperties(t *testing.T) {
	bootstrapProps,
		_, galasaHome, fs,
		javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		_ := getDefaultCommandSyntaxTestParameters()

	bootstrapProps[api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_OPTIONS] = "-Xmx40m -Xms20m"
	isTraceEnabled := false
	isDebugEnabled := false
	var debugPort uint32 = 0
	debugMode := ""

	cmd, args, err := getCommandSyntax(
		bootstrapProps, galasaHome, fs, javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		"", // No Gherkin URL supplied
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		BLANK_JWT,
	)

	assert.NotNil(t, cmd)
	assert.NotNil(t, args)
	assert.Nil(t, err)

	assert.Contains(t, args, "-Xmx40m")
	assert.Contains(t, args, "-Xms20m")
}

func TestCommandIncludesDefaultDebugPortAndMode(t *testing.T) {
	bootstrapProps,
		_, galasaHome, fs,
		javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		_ := getDefaultCommandSyntaxTestParameters()

	isTraceEnabled := false
	isDebugEnabled := true // <<<< Debug is turned on. No overrides to debugPort in either boostrap or explicit command option.
	var debugPort uint32 = 0
	debugMode := ""

	command, args, err := getCommandSyntax(
		bootstrapProps, galasaHome, fs, javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		"", // No Gherkin URL supplied
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		BLANK_JWT,
	)

	assert.NotNil(t, command)
	assert.NotNil(t, args)
	assert.Nil(t, err)

	assert.Contains(t, args, "-agentlib:jdwp=transport=dt_socket,address=*:"+strconv.FormatUint(uint64(DEBUG_PORT_DEFAULT), 10)+",server=y,suspend=y")
}

func TestCommandDrawsValidDebugPortFromBootstrap(t *testing.T) {
	bootstrapProps,
		_, galasaHome, fs,
		javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		_ := getDefaultCommandSyntaxTestParameters()

	isTraceEnabled := false
	isDebugEnabled := true // <<<< Debug is turned on. No overrides to debugPort in either boostrap or explicit command option.
	var debugPort uint32 = 0
	debugMode := ""

	bootstrapProps[api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_DEBUG_PORT] = "345"

	command, args, err := getCommandSyntax(
		bootstrapProps, galasaHome, fs, javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		"", // No Gherkin URL supplied
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		BLANK_JWT,
	)

	assert.NotNil(t, command)
	assert.NotNil(t, args)
	assert.Nil(t, err)

	assert.Contains(t, args, "-agentlib:jdwp=transport=dt_socket,address=*:345,server=y,suspend=y")
}

func TestCommandDrawsInvalidDebugPortFromBootstrap(t *testing.T) {
	bootstrapProps,
		_, galasaHome, fs,
		javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		_ := getDefaultCommandSyntaxTestParameters()

	isTraceEnabled := false
	isDebugEnabled := true // <<<< Debug is turned on. No overrides to debugPort in either boostrap or explicit command option.
	var debugPort uint32 = 0
	debugMode := ""

	bootstrapProps[api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_DEBUG_PORT] = "-456"

	_, _, err := getCommandSyntax(
		bootstrapProps, galasaHome, fs, javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		"", // No Gherkin URL supplied
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		BLANK_JWT,
	)

	assert.NotNil(t, err)

	assert.Contains(t, err.Error(), "-456")
	assert.Contains(t, err.Error(), api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_DEBUG_PORT)
	assert.Contains(t, err.Error(), "GAL1072E")
}

func TestCommandDrawsValidDebugModeFromBootstrap(t *testing.T) {
	bootstrapProps,
		_, galasaHome, fs,
		javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		_ := getDefaultCommandSyntaxTestParameters()

	isTraceEnabled := false
	isDebugEnabled := true // <<<< Debug is turned on. No overrides to debugPort in either boostrap or explicit command option.
	var debugPort uint32 = 0
	debugMode := ""

	bootstrapProps[api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_DEBUG_MODE] = "attach"

	command, args, err := getCommandSyntax(
		bootstrapProps, galasaHome, fs, javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		"", // No Gherkin URL supplied
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		BLANK_JWT,
	)

	assert.NotNil(t, command)
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

func TestCommandDrawsInvalidDebugModeFromBootstrap(t *testing.T) {
	bootstrapProps,
		_, galasaHome, fs,
		javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		_ := getDefaultCommandSyntaxTestParameters()

	isTraceEnabled := false
	isDebugEnabled := true // <<<< Debug is turned on. No overrides to debugPort in either boostrap or explicit command option.
	var debugPort uint32 = 0
	debugMode := ""

	bootstrapProps[api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_DEBUG_MODE] = "shout" //  << Invalid !

	_, _, err := getCommandSyntax(
		bootstrapProps, galasaHome, fs, javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		"", // No Gherkin URL supplied
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		BLANK_JWT,
	)

	assert.NotNil(t, err)

	assert.Contains(t, err.Error(), "shout")
	assert.Contains(t, err.Error(), api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_DEBUG_MODE)
	assert.Contains(t, err.Error(), "GAL1070E")
}

func TestCommandDrawsValidDebugModeListenFromCommandLine(t *testing.T) {
	bootstrapProps,
		_, galasaHome, fs,
		javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		_ := getDefaultCommandSyntaxTestParameters()

	isTraceEnabled := false
	isDebugEnabled := true // <<<< Debug is turned on. No overrides to debugPort in either boostrap or explicit command option.
	var debugPort uint32 = 0
	debugMode := "listen"

	command, args, err := getCommandSyntax(
		bootstrapProps, galasaHome, fs, javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		"", // No Gherkin URL supplied
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		BLANK_JWT,
	)

	assert.NotNil(t, command)
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

func TestCommandDrawsValidDebugModeAttachFromCommandLine(t *testing.T) {
	bootstrapProps,
		_, galasaHome, fs,
		javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		_ := getDefaultCommandSyntaxTestParameters()

	isTraceEnabled := false
	isDebugEnabled := true // <<<< Debug is turned on. No overrides to debugPort in either boostrap or explicit command option.
	var debugPort uint32 = 0
	debugMode := "attach"

	command, args, err := getCommandSyntax(
		bootstrapProps, galasaHome, fs, javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		"", // No Gherkin URL supplied
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		BLANK_JWT,
	)

	assert.NotNil(t, command)
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

func TestCommandDrawsInvalidDebugModeFromCommandLine(t *testing.T) {
	bootstrapProps,
		_, galasaHome, fs,
		javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		_ := getDefaultCommandSyntaxTestParameters()

	isTraceEnabled := false
	isDebugEnabled := true // <<<< Debug is turned on. No overrides to debugPort in either boostrap or explicit command option.
	var debugPort uint32 = 0
	debugMode := "invalidMode"

	_, _, err := getCommandSyntax(
		bootstrapProps, galasaHome, fs, javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		"", // No Gherkin URL supplied
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		BLANK_JWT,
	)

	assert.NotNil(t, err)

	assert.Contains(t, err.Error(), "invalidMode")
	assert.Contains(t, err.Error(), api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_DEBUG_MODE)
	assert.Contains(t, err.Error(), "GAL1071E")
}

func TestLocalMavenNotSetDefaults(t *testing.T) {
	// For...
	bootstrapProps,
		_, galasaHome, fs,
		javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		_ := getDefaultCommandSyntaxTestParameters()

	isTraceEnabled := false
	isDebugEnabled := false // <<<< Debug is turned on. No overrides to debugPort in either boostrap or explicit command option.
	var debugPort uint32 = 0
	debugMode := ""

	// When...
	_, args, err := getCommandSyntax(
		bootstrapProps, galasaHome, fs, javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		"", // No Gherkin URL supplied
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		BLANK_JWT,
	)

	// Then...
	assert.Nil(t, err)

	assert.Contains(t, args, "--localmaven")
	assert.Contains(t, args, "file:////User/Home/testuser/.m2/repository")
}

func TestLocalMavenSet(t *testing.T) {
	// For...
	bootstrapProps,
		_, galasaHome, fs,
		javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		_,
		galasaVersionToRun,
		overridesFilePath,
		_ := getDefaultCommandSyntaxTestParameters()

	isTraceEnabled := false
	isDebugEnabled := false // <<<< Debug is turned on. No overrides to debugPort in either boostrap or explicit command option.
	var debugPort uint32 = 0
	debugMode := ""
	localMaven := "mavenRepo"

	// When...
	_, args, err := getCommandSyntax(
		bootstrapProps, galasaHome, fs, javaHome,
		testObrs,
		testLocation,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		overridesFilePath,
		"", // No Gherkin URL supplied
		isTraceEnabled,
		isDebugEnabled, debugPort, debugMode,
		BLANK_JWT,
	)

	// Then...
	assert.Nil(t, err)

	assert.Contains(t, args, "--localmaven")
	assert.Contains(t, args, "mavenRepo")
}

func NewMockGherkinParams() (
	props.JavaProperties,
	*utils.MockEnv,
	spi.FileSystem,
	embedded.ReadOnlyFileSystem,
	*RunsSubmitLocalCmdParameters,
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
	jvmLaunchParams := getBasicJvmLaunchParams()
	timeService := utils.NewMockTimeService()
	mockProcess := NewMockProcess()
	mockProcessFactory := NewMockProcessFactory(mockProcess)

	bootstrapProps := getBasicBootstrapProperties()

	return bootstrapProps, env, fs, embedded.GetReadOnlyFileSystem(),
		jvmLaunchParams, timeService, utils.NewRealTimedSleeper(), mockProcessFactory, galasaHome
}

func TestCanLaunchLocalJvmGherkinTest(t *testing.T) {
	// Given...
	bootstrapProps, env, fs, embeddedReadOnlyFS,
		jvmLaunchParams, timeService, timedSleeper, mockProcessFactory, galasaHome := NewMockGherkinParams()

	mockFactory := &utils.MockFactory{
		Env:         env,
		FileSystem:  fs,
		TimeService: timeService,
	}

	launcher, err := NewJVMLauncher(
		mockFactory,
		bootstrapProps, embeddedReadOnlyFS,
		jvmLaunchParams, mockProcessFactory, galasaHome, timedSleeper,
	)

	if err != nil {
		assert.Fail(t, "JVM launcher should have been creatable.")
	}
	assert.NotNil(t, launcher, "Launcher returned is nil!")

	isTraceEnabled := true
	var overrides map[string]interface{} = make(map[string]interface{})

	// When...
	testRuns, err := launcher.SubmitTestRun(
		"myGroup",
		"", // No Java Class as this is a gherkin test
		"", // No RequestType as this is a gherkin test
		"myRequestor",
		"", // No Stream as this is a gherkin test
		"", // No OBR as this is a gherkin test
		isTraceEnabled,
		"file:///dev.galasa.simbank.tests/src/main/java/dev/galasa/simbank/tests/GherkinLog.feature",
		"GherkinLog",
		overrides,
	)
	if err != nil {
		assert.Fail(t, "Launcher should have launched command OK")
	} else {
		assert.NotNil(t, testRuns, "Returned test runs is nil, should have been an object.")

		assert.Len(t, testRuns.Runs, 1, "Returned test runs array doesn't contain correct number of tests launched.")
		assert.False(t, *testRuns.Complete, "Returned test runs should not already be complete")
	}
}

func TestBadGherkinURLSuffixReturnsError(t *testing.T) {
	// Given...
	bootstrapProps, env, fs, embeddedReadOnlyFS,
		jvmLaunchParams, timeService, timedSleeper, mockProcessFactory, galasaHome := NewMockGherkinParams()

	mockFactory := &utils.MockFactory{
		Env:         env,
		FileSystem:  fs,
		TimeService: timeService,
	}

	launcher, err := NewJVMLauncher(
		mockFactory,
		bootstrapProps, embeddedReadOnlyFS,
		jvmLaunchParams, mockProcessFactory, galasaHome, timedSleeper,
	)
	if err != nil {
		assert.Fail(t, "JVM launcher should have been creatable.")
	}
	assert.NotNil(t, launcher, "Launcher returned is nil!")

	isTraceEnabled := true
	var overrides map[string]interface{} = make(map[string]interface{})

	// When...
	_, err = launcher.SubmitTestRun(
		"myGroup",
		"", // No Java Class as this is a gherkin test
		"", // No RequestType as this is a gherkin test
		"myRequestor",
		"", // No Stream as this is a gherkin test
		"", // No OBR as this is a gherkin test
		isTraceEnabled,
		"file:///dev.galasa.simbank.tests/src/main/java/dev/galasa/simbank/tests/GherkinLog.future",
		"GherkinLog",
		overrides,
	)

	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1137E")
}

func TestBadGherkinURLPrefixReutrnsError(t *testing.T) {
	// Given...
	bootstrapProps, env, fs, embeddedReadOnlyFS,
		jvmLaunchParams, timeService, timedSleeper, mockProcessFactory, galasaHome := NewMockGherkinParams()

	mockFactory := &utils.MockFactory{
		Env:         env,
		FileSystem:  fs,
		TimeService: timeService,
	}

	launcher, err := NewJVMLauncher(
		mockFactory,
		bootstrapProps, embeddedReadOnlyFS,
		jvmLaunchParams, mockProcessFactory, galasaHome, timedSleeper,
	)

	if err != nil {
		assert.Fail(t, "JVM launcher should have been creatable.")
	}
	assert.NotNil(t, launcher, "Launcher returned is nil!")

	isTraceEnabled := true
	var overrides map[string]interface{} = make(map[string]interface{})

	// When...
	_, err = launcher.SubmitTestRun(
		"myGroup",
		"", // No Java Class as this is a gherkin test
		"", // No RequestType as this is a gherkin test
		"myRequestor",
		"", // No Stream as this is a gherkin test
		"", // No OBR as this is a gherkin test
		isTraceEnabled,
		"https://dev.galasa.simbank.tests/src/main/java/dev/galasa/simbank/tests/GherkinLog.feature",
		"GherkinLog",
		overrides,
	)

	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1138E")
}

func TestSetTestStructureFromRasFileValidFileContentReturnsOk(t *testing.T) {
	//Given...
	var run = galasaapi.NewRun()
	run.SetRunId("L0")
	jsonFileName := "structure.json"

	_, _, fs, _,
		_, _, _, _, galasaHome := NewMockLauncherParams()

	jsonFilePath := galasaHome.GetNativeFolderPath() + "/" + jsonFileName
	fs.WriteTextFile(jsonFilePath, validStructureJsonFileContent)

	//When...
	err := setTestStructureFromRasFile(run, jsonFilePath, fs)

	//Then...
	assert.Nil(t, err)
	assert.Equal(t, "L0", run.GetRunId())
	assert.True(t, run.HasTestStructure())
	assert.Equal(t, "simpleSampleTest", run.GetTestStructure().Methods[0].GetMethodName())
}

func TestSetTestStructureFromRasFileInvalidFileContentReturnsError(t *testing.T) {
	//Given...
	var run = galasaapi.NewRun()
	run.SetRunId("L0")
	jsonFileName := "structure.json"
	//improper key-value pair in 'methods'
	invalidStructureJsonFileContent := `{
		"runName": "L0",
		"bundle": "dev.galasa.examples.banking.account",
		"testName": "dev.galasa.examples.banking.account.TestAccount",
		"testShortName": "TestAccount",
		"requestor": "unknown",
		"status": "finished",
		"result": "Passed",
		"queued": "2024-03-14T10:11:02.185556Z",
		"startTime": "2024-03-14T10:11:02.221697Z",
		"endTime": "2024-03-14T10:11:02.439849Z",
		"methods": [
			{
			"className": "dev.galasa.examples.banking.account.TestAccount",
			"methodName": "simpleSampleTest",
			"type": "Test",
			"befores": [],
			"afters": [],
			"status": "finished",
			"result": "Passed",
			"runLogStart": 0,
			"runLogEnd": 0,
			"new":
			"startTime": "2024-03-14T10:11:02.413588Z",
			"endTime": "2024-03-14T10:11:02.428472Z"
		}
		]
	}`
	_, _, fs, _,
		_, _, _, _, galasaHome := NewMockLauncherParams()

	jsonFilePath := galasaHome.GetNativeFolderPath() + "/" + jsonFileName
	fs.WriteTextFile(jsonFilePath, invalidStructureJsonFileContent)

	//When...
	err := setTestStructureFromRasFile(run, jsonFilePath, fs)

	//Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "error unmarshalling json file into TestStructure")
}

func TestSetTestStructureFromRasFileEmptyFileContentReturnsError(t *testing.T) {
	//Given...
	var run = galasaapi.NewRun()
	run.SetRunId("L0")
	jsonFileName := "structure.json"

	_, _, fs, _,
		_, _, _, _, galasaHome := NewMockLauncherParams()

	jsonFilePath := galasaHome.GetNativeFolderPath() + "/" + jsonFileName
	fs.WriteTextFile(jsonFilePath, "")
	//When...
	err := setTestStructureFromRasFile(run, jsonFilePath, fs)

	//Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "file '/User/Home/testuser/.galasa/structure.json' is empty. Status could not be read")
}

func TestSetTestStructureFromRasFileInvalidFilePathReturnsError(t *testing.T) {
	//Given...
	var run = galasaapi.NewRun()
	run.SetRunId("L0")
	jsonFileName := "structure.json"

	_, _, fs, _,
		_, _, _, _, _ := NewMockLauncherParams()

	invalidJsonFilePath := "invalidJsonFilePath/" + jsonFileName

	//When...
	err := setTestStructureFromRasFile(run, invalidJsonFilePath, fs)

	//Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "file 'invalidJsonFilePath/structure.json' does not exist")
}

func TestCreateRunFromLocalTestValidRasFolderPathReturnsOk(t *testing.T) {
	//Given...
	_, _, fs, _,
		_, _, timedSleeper, mockProcessFactory, galasaHome := NewMockLauncherParams()

	localTest := NewLocalTest(timedSleeper, fs, mockProcessFactory)
	localTest.runId = "L0"
	localTest.rasFolderPathUrl = galasaHome.GetNativeFolderPath()

	jsonFilePath := localTest.rasFolderPathUrl + "/" + localTest.runId + "/structure.json"
	fs.WriteTextFile(jsonFilePath, validStructureJsonFileContent)

	//When...
	run, err := createRunFromLocalTest(localTest)

	//Then...
	assert.Nil(t, err)
	assert.Equal(t, run.GetRunId(), localTest.runId)
	assert.Equal(t, "L0", run.TestStructure.GetRunName())
	assert.Equal(t, "dev.galasa.examples.banking.account", run.TestStructure.GetBundle())
	assert.Equal(t, "Passed", run.TestStructure.GetResult())
	assert.Equal(t, "simpleSampleTest", run.GetTestStructure().Methods[0].GetMethodName())
	assert.Equal(t, "finished", run.TestStructure.GetStatus())
}

func TestCreateRunFromLocalTestInvalidRasFolderPathReturnsError(t *testing.T) {
	//Given...
	_, _, fs, _,
		_, _, timedSleeper, mockProcessFactory, _ := NewMockLauncherParams()

	localTest := NewLocalTest(timedSleeper, fs, mockProcessFactory)
	localTest.runId = "L0"
	localTest.rasFolderPathUrl = ""

	//When...
	_, err := createRunFromLocalTest(localTest)

	//Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "Don't have enough information to find the structure.json in the RAS folder")
}

func TestGetRunsByIdReturnsOk(t *testing.T) {
	// Given...
	bootstrapProps, env, fs, embeddedReadOnlyFS,
		jvmLaunchParams, timeService, timedSleeper, _, galasaHome := NewMockLauncherParams()

	mockProcess := NewMockProcess()
	mockProcessFactory := NewMockProcessFactory(mockProcess)

	mockFactory := &utils.MockFactory{
		Env:         env,
		FileSystem:  fs,
		TimeService: timeService,
	}

	launcher, _ := NewJVMLauncher(
		mockFactory,
		bootstrapProps, embeddedReadOnlyFS,
		jvmLaunchParams, mockProcessFactory, galasaHome, timedSleeper,
	)

	isTraceEnabled := true
	var overrides map[string]interface{} = make(map[string]interface{})

	launcher.SubmitTestRun(
		"myGroup",
		"galasa.dev.examples.banking.account/galasa.dev.examples.banking.account.TestAccount",
		"CLI",
		"unknown",
		"unitTestStream",
		"mvn:myGroup/myArtifact/myClassifier/obr",
		isTraceEnabled,
		"", // No Gherkin URL supplied
		"", // No Gherkin Feature supplied
		overrides,
	)

	// Wait for the child process to complete...
	mockProcess.Wait()

	fs.WriteTextFile("/temp/ras/L12345/structure.json", validStructureJsonFileContent)

	// When...
	run, err := launcher.GetRunsById("L12345")

	// Then...
	assert.Nil(t, err)
	assert.Equal(t, "L12345", run.GetRunId())
	assert.Equal(t, "dev.galasa.examples.banking.account", run.TestStructure.GetBundle())
	assert.Equal(t, "Passed", run.TestStructure.GetResult())
	assert.Equal(t, "simpleSampleTest", run.GetTestStructure().Methods[0].GetMethodName())
}
