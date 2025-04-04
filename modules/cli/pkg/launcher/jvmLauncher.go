/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package launcher

import (
	"encoding/json"
	"fmt"
	"log"
	"strconv"
	"strings"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/props"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/utils"
)

// TestLocation the user passes us this information in one string.
// We split it up into these useful chunks.
type TestLocation struct {
	OSGiBundleName         string
	QualifiedJavaClassName string
}

// We gather a summary of passes and failures from the
// test results we read from ras/<runId>/structure.json
type TestResultsSummary struct {
	MethodPasses int
	MethodFails  int
}

// JvmLauncher can act as a launcher, it's given test cases which need to
// be executed, and it launches them within a local JVM.
type JvmLauncher struct {
	// The fully-qualified path to JAVA_HOME where we can find the bin/java command.
	javaHome string

	// The parameters from the command-line.
	cmdParams RunsSubmitLocalCmdParameters

	// An abstraction of the environment, so we can look up things like JAVA_HOME
	env spi.Environment

	// An abstraction of the file system so we can mock it out easily for unit tests.
	fileSystem spi.FileSystem

	// A location galasa can call home
	galasaHome spi.GalasaHome

	// A file system so we can get at embedded content if required.
	// (Like so we can unpack the boot.jar)
	embeddedFileSystem embedded.ReadOnlyFileSystem

	// The collection of tests which are running, or have completed.
	localTests []*LocalTest

	// This timer service allows unit tests to control the time explicitly.
	timeService spi.TimeService

	// Used by the main polling loop to sleep and be interrupted.
	timedSleeper spi.TimedSleeper

	// A service which can create OS processes.
	processFactory ProcessFactory

	// A map of bootstrap properties
	bootstrapProps props.JavaProperties

	// So we can get common objects easily.
	factory spi.Factory
}

// These parameters are gathered from the command-line and passed into the laucher.
type RunsSubmitLocalCmdParameters struct {

	// A list of OBRs, which we hope one of these contains the tests we want to run.
	Obrs []string

	// The local maven repo, eg: file:///home/.m2/repository, where we can load the galasa uber-obr
	LocalMaven string

	// The remote maven repo, eg: maven central, where we can load the galasa uber-obr
	RemoteMaven string

	// The version of galasa we want to launch. This indicates which uber-obr will be
	// loaded.
	TargetGalasaVersion string

	// Should the JVM be launched in debug mode ?
	IsDebugEnabled bool

	// When launched in debug mode, which port should the JVM use to talk to the Java
	// debugger ? This port is either listened on, or attached to depending on the
	// DebugMode field.
	DebugPort uint32

	// A string indicating whether the test JVM should 'attach' to the debug port
	// to talk to the Java debugger (JDB), or whether it should 'listen' on a port
	// ready for the JDB to attach to.
	DebugMode string

	// A string containing the url of the gherkin test file to be exceuted
	GherkinURL string
}

const (
	DEBUG_PORT_DEFAULT uint32 = 2970
)

// -----------------------------------------------------------------------------
// Constructors
// -----------------------------------------------------------------------------

// NewJVMLauncher creates a JVM launcher. Primes it with references to services
// which can be used to launch JVM servers.
// We get the caller's timer service so we can interrupt the caller when we are done.
func NewJVMLauncher(
	factory spi.Factory,
	bootstrapProps props.JavaProperties,
	embeddedFileSystem embedded.ReadOnlyFileSystem,
	runsSubmitLocalCmdParams *RunsSubmitLocalCmdParameters,
	processFactory ProcessFactory,
	galasaHome spi.GalasaHome,
	timedSleeper spi.TimedSleeper,

) (*JvmLauncher, error) {

	var (
		err      error
		launcher *JvmLauncher = nil
	)

	env := factory.GetEnvironment()
	fileSystem := factory.GetFileSystem()

	javaHome := env.GetEnv("JAVA_HOME")

	err = utils.ValidateJavaHome(fileSystem, javaHome)

	if err == nil {
		launcher = new(JvmLauncher)
		launcher.factory = factory
		launcher.javaHome = javaHome
		launcher.cmdParams = *runsSubmitLocalCmdParams
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

//-----------------------------------------------------------------------------
// Implementation of the Launcher interface
//-----------------------------------------------------------------------------

// SubmitTestRuns launch the test runs
//
// groupName - The run group ID. Used to group all the TestRuns together so we
// can query the results later.
//
// classNames - An array of strings in the form "<osgi-bundle-id>/<fully-qualified-java-classname>
// Note: There is no ".class" suffix needed for each entry. That is assumed.
//
// requestType - A metadata marker to indicate how the testRun was scheduled.
// requestor - Who wanted the testRun to launch.
// stream - The stream the test run is part of
// isTraceEnabled - True of the trace for the test run should be gathered.
// overrides - A map of overrides of key-value pairs.
func (launcher *JvmLauncher) SubmitTestRun(
	groupName string,
	className string,
	requestType string,
	requestor string,
	stream string,
	obrFromPortfolio string,
	isTraceEnabled bool,
	gherkinURL string,
	GherkinFeature string,
	overrides map[string]interface{},
) (*galasaapi.TestRuns, error) {

	if gherkinURL != "" {
		log.Printf("JvmLauncher: SubmitTestRun entered. Gherkin Feature=%s", GherkinFeature)
	} else {
		log.Printf("JvmLauncher: SubmitTestRun entered. group=%s className=%s "+
			"requestType=%s requestor=%s stream=%s isTraceEnabled=%v",
			groupName, className, requestType,
			requestor, stream, isTraceEnabled)
	}
	var err error
	testRuns := new(galasaapi.TestRuns)

	// We have some OBRs from the runs submit local command-line.
	var obrs []utils.MavenCoordinates
	obrs, err = buildListOfAllObrs(launcher.cmdParams.Obrs, obrFromPortfolio)
	if err == nil {

		if len(obrs) < 1 && gherkinURL == "" {
			// There are no obrs ! We have no idea how to find the test!
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_NO_OBR_SPECIFIED_ON_INPUTS, className)
		}
		if err == nil {

			var (
				overridesFilePath   string
				temporaryFolderPath string
			)
			temporaryFolderPath, overridesFilePath, err = prepareTempFiles(
				launcher.galasaHome, launcher.fileSystem, overrides)
			if err == nil {

				defer func() {
					deleteTempFiles(launcher.fileSystem, temporaryFolderPath)
				}()

				isComplete := false
				testRuns.Complete = &isComplete
				testRuns.Runs = make([]galasaapi.TestRun, 0)

				var testClassToLaunch *TestLocation
				if className != "" {
					testClassToLaunch, err = classNameUserInputToTestClassLocation(className)
				} else {
					//Set to empty for the command as this is a Gherkin Test
					testClassToLaunch = &TestLocation{
						OSGiBundleName:         "",
						QualifiedJavaClassName: "",
					}
				}

				if err == nil && gherkinURL != "" {
					err = checkGherkinURLisValid(gherkinURL)
				}

				if err == nil {

					var jwt = ""
					if launcher.isCPSRemote() {
						// Though this is a local test run being launched, the CPS will be remote on an ecosystem via REST.
						// If the config store value doesn't start wiht that, then it's not a remote CPS, so we don't need the JWT.
						apiServerUrl := launcher.getCPSRemoteApiServerUrl()
						authenticator := launcher.factory.GetAuthenticator(apiServerUrl, launcher.galasaHome)
						log.Printf("framework.config.store bootstrap property indicates a remote CPS will be used. So we need a valid JWT.\n")
						jwt, err = authenticator.GetBearerToken()
					}

					if err == nil {

						var (
							cmd  string
							args []string
						)
						cmd, args, err = getCommandSyntax(
							launcher.bootstrapProps,
							launcher.galasaHome,
							launcher.fileSystem, launcher.javaHome, obrs,
							*testClassToLaunch, launcher.cmdParams.RemoteMaven, launcher.cmdParams.LocalMaven,
							launcher.cmdParams.TargetGalasaVersion, overridesFilePath,
							gherkinURL,
							isTraceEnabled,
							launcher.cmdParams.IsDebugEnabled,
							launcher.cmdParams.DebugPort,
							launcher.cmdParams.DebugMode,
							jwt,
						)
						if err == nil {
							log.Printf("Launching command '%s' '%v'\n", cmd, args)
							localTest := NewLocalTest(launcher.timedSleeper, launcher.fileSystem, launcher.processFactory)
							err = localTest.launch(cmd, args)

							if err == nil {
								// The JVM process started. Store away its' details
								launcher.localTests = append(launcher.localTests, localTest)

								localTest.testRun = new(galasaapi.TestRun)
								if testClassToLaunch.OSGiBundleName != "" {
									localTest.testRun.SetBundleName(testClassToLaunch.OSGiBundleName)
								}
								localTest.testRun.SetStream(stream)
								localTest.testRun.SetGroup(groupName)
								localTest.testRun.SetRequestor(requestor)
								localTest.testRun.SetTrace(isTraceEnabled)
								localTest.testRun.SetType(requestType)
								localTest.testRun.SetName(localTest.runId)

								localTest.testRun.SetSubmissionId("")

								// The test run we started can be returned to the submitter.
								testRuns.Runs = append(testRuns.Runs, *localTest.testRun)
							}
						}
					}
				}
			}
		}
	}

	return testRuns, err
}

// isCPSRemote - decide whether the config store used by tests is remote or not.
// If it is remote, we are going to have to get a valid JWT to use.
func (launcher *JvmLauncher) isCPSRemote() bool {
	isRemote := false
	configStoreProp := launcher.bootstrapProps["framework.config.store"]
	isRemote = strings.HasPrefix(configStoreProp, "galasacps")
	return isRemote
}

// Gets the https URL of the config store, to be used contacting the remote CPS.
func (launcher *JvmLauncher) getCPSRemoteApiServerUrl() string {
	configStoreGalasaUrl := launcher.bootstrapProps["framework.config.store"]
	// The configuration has a URL like galasacps://myhost/api
	// We need to turn it into something like https://myhost/api
	httpsUrl := strings.Replace(configStoreGalasaUrl, "galasacps", "https", 1)
	return httpsUrl
}

func defaultLocalMavenIfNotSet(localMaven string, fileSystem spi.FileSystem) (string, error) {
	var err error
	returnMavenPath := ""
	if localMaven == "" {
		var userHome string
		userHome, err = fileSystem.GetUserHomeDirPath()
		if err == nil {
			returnMavenPath = "file:///" + strings.ReplaceAll(userHome, "\\", "/") + "/.m2/repository"
		}
	} else {
		returnMavenPath = localMaven
	}
	return returnMavenPath, err
}

func buildListOfAllObrs(obrsFromCommandLine []string, obrFromPortfolio string) ([]utils.MavenCoordinates, error) {
	obrs, err := utils.ValidateObrs(obrsFromCommandLine)
	if err == nil {

		// We may have an obr from the portfolio also...
		if obrFromPortfolio != "" {
			var obrMavenCoordinates utils.MavenCoordinates
			obrMavenCoordinates, err = utils.ValidateObr(obrFromPortfolio)
			if err == nil {
				obrs = append(obrs, obrMavenCoordinates)
			}
		}
	}
	return obrs, err
}

func deleteTempFiles(fileSystem spi.FileSystem, temporaryFolderPath string) {
	fileSystem.DeleteDir(temporaryFolderPath)
}

func prepareTempFiles(
	galasaHome spi.GalasaHome,
	fileSystem spi.FileSystem,
	overrides map[string]interface{},
) (string, string, error) {

	var (
		temporaryFolderPath string
		overridesFilePath   string
		err                 error
	)

	// Create a temporary folder
	temporaryFolderPath, err = fileSystem.MkTempDir()
	if err == nil {
		overridesFilePath, err = createTemporaryOverridesFile(
			temporaryFolderPath, galasaHome, fileSystem, overrides)
	}

	// Clean up the temporary folder if we failed to create the props file.
	if err != nil {
		fileSystem.DeleteDir(temporaryFolderPath)
	}

	return temporaryFolderPath, overridesFilePath, err
}

// createTemporaryOverridesFile Gathers up all the overrides properties and puts
// them into a temporary file in a temporary folder.
//
// Returns:
// - the full path to the new overrides file
// - error if there was one.
func createTemporaryOverridesFile(
	temporaryFolderPath string,
	galasaHome spi.GalasaHome,
	fileSystem spi.FileSystem,
	overrides map[string]interface{},
) (string, error) {
	overrides = addStandardOverrideProperties(galasaHome, overrides)

	// Write the properties to a file
	overridesFilePath := temporaryFolderPath + "overrides.properties"
	err := props.WritePropertiesFile(fileSystem, overridesFilePath, overrides)
	return overridesFilePath, err
}

func addStandardOverrideProperties(
	galasaHome spi.GalasaHome,
	overrides map[string]interface{},
) map[string]interface{} {

	overrideRasStoreProperty(galasaHome, overrides)
	overrideLocalRunIdPrefixProperty(overrides)
	override3270TerminalOutputFormat(overrides)

	return overrides
}

func override3270TerminalOutputFormat(overrides map[string]interface{}) {
	// Force the launched runs to use the "L" prefix in their runids.
	const OVERRIDE_PROPERTY_3270_TERMINAL_OUTPUT_FORMAT = "zos3270.terminal.output"

	// Only set this property if it's not already set by the user, or in the users' override file.
	_, isPropAlreadySet := overrides[OVERRIDE_PROPERTY_3270_TERMINAL_OUTPUT_FORMAT]
	if !isPropAlreadySet {
		overrides[OVERRIDE_PROPERTY_3270_TERMINAL_OUTPUT_FORMAT] = "json,png"
	}
}

func overrideLocalRunIdPrefixProperty(overrides map[string]interface{}) {
	// Force the launched runs to use the "L" prefix in their runids.
	const OVERRIDE_PROPERTY_LOCAL_RUNID_PREFIX = "framework.request.type.LOCAL.prefix"

	// Only set this property if it's not already set by the user, or in the users' override file.
	_, isPropAlreadySet := overrides[OVERRIDE_PROPERTY_LOCAL_RUNID_PREFIX]
	if !isPropAlreadySet {
		overrides[OVERRIDE_PROPERTY_LOCAL_RUNID_PREFIX] = "L"
	}
}

func overrideRasStoreProperty(galasaHome spi.GalasaHome, overrides map[string]interface{}) {
	// Set the ras location to be local disk always.
	const OVERRIDE_PROPERTY_FRAMEWORK_RESULT_STORE = "framework.resultarchive.store"

	// Only set this property if it's not already set by the user, or in the users' override file.
	{
		_, isRasPropAlreadySet := overrides[OVERRIDE_PROPERTY_FRAMEWORK_RESULT_STORE]
		if !isRasPropAlreadySet {
			rasPathUri := "file:///" + galasaHome.GetUrlFolderPath() + "/ras"
			overrides[OVERRIDE_PROPERTY_FRAMEWORK_RESULT_STORE] = rasPathUri
		}
	}
}

func (launcher *JvmLauncher) GetRunsByGroup(groupName string) (*galasaapi.TestRuns, error) {
	log.Printf("JvmLauncher: GetRunsByGroup(groupName=%s) entered. ", groupName)

	var isAllComplete = true
	var testRuns = galasaapi.TestRuns{
		Complete: &isAllComplete,
		Runs:     []galasaapi.TestRun{},
	}

	for _, localTest := range launcher.localTests {

		testName := localTest.testRun.GetName()

		if localTest.isCompleted() {
			log.Printf("GetRunsByGroup: localTest %s is complete.\n", testName)
		} else {
			log.Printf("GetRunsByGroup: localTest %s is not yet complete.\n", testName)
			isAllComplete = false
		}

		var testRun *galasaapi.TestRun
		if localTest.testRun != nil {
			testRun = localTest.testRun
		} else {
			testRun = createSimulatedTestRun(testName)
		}

		testRuns.Runs = append(testRuns.Runs, *testRun)
	}

	log.Printf("JvmLauncher: GetRunsByGroup(groupName=%s) exiting. isComplete:%v testRuns returned:%v\n", groupName, *testRuns.Complete, len(testRuns.Runs))
	for _, testRun := range testRuns.Runs {
		log.Printf("JvmLauncher: GetRunsByGroup test name:%s status:%s result:%s\n", *testRun.Name, *testRun.Status, *testRun.Result)
	}
	return &testRuns, nil
}

// GetRunsById gets the Run information for the run with a specific run identifier
func (launcher *JvmLauncher) GetRunsById(runId string) (*galasaapi.Run, error) {
	log.Printf("JvmLauncher: GetRunsById entered. runId=%s", runId)

	var run *galasaapi.Run
	var err error

	for _, localTest := range launcher.localTests {

		testRunId := localTest.runId

		if testRunId == runId {
			log.Printf("JvmLauncher: GetRunsById - testRunId '%s' matches with what we're looking for: runId '%s'", testRunId, runId)
			run, err = createRunFromLocalTest(localTest)
		}
	}

	//if we didn't find testRun with same runId log and move on to next run
	if run == nil {
		log.Printf("JvmLauncher: GetRunsById - could not find testRun '%s'", runId)
	} else {
		log.Printf("JvmLauncher: GetRunsById(testRunId=%s) - testRun returned:%v\n", runId, run.RunId)
	}

	return run, err
}

// Gets a run based on the submission ID of that run.
// For local runs, the submission ID is the same as the test run id.
func (launcher *JvmLauncher) GetRunsBySubmissionId(submissionId string, groupId string) (*galasaapi.Run, error) {
	log.Printf("JvmLauncher: GetRunsBySubmissionId entered. runId=%s", submissionId)

	log.Printf("JvmLauncher: Local runs cannot find tests based on submission ID")
	return nil, nil
}

func createRunFromLocalTest(localTest *LocalTest) (*galasaapi.Run, error) {

	var run = galasaapi.NewRun()
	var err error

	run.SetRunId(localTest.runId)

	if localTest.rasFolderPathUrl == "" {
		err = fmt.Errorf("createRunFromLocalTest - Don't have enough information to find the structure.json in the RAS folder")
		log.Printf("%v", err.Error())
	} else {
		jsonFilePath := strings.TrimPrefix(localTest.rasFolderPathUrl, "file:///") + "/" + localTest.runId + "/structure.json"
		log.Printf("createRunFromLocalTest - Reading latest test status from '%s'\n", jsonFilePath)

		err = setTestStructureFromRasFile(run, jsonFilePath, localTest.fileSystem)
	}

	return run, err
}

func setTestStructureFromRasFile(run *galasaapi.Run, jsonFilePath string, fileSystem spi.FileSystem) error {

	var testStructure = galasaapi.NewTestStructure()
	var err error
	var isExists bool
	var jsonContent string

	isExists, err = fileSystem.Exists(jsonFilePath)
	if err != nil {
		err = fmt.Errorf("error opening file - %v", err.Error())
	} else {
		if !isExists {
			err = fmt.Errorf("file '%s' does not exist", jsonFilePath)
		} else {
			jsonContent, err = fileSystem.ReadTextFile(jsonFilePath)

			if err != nil {
				err = fmt.Errorf("file '%s' could not be read", jsonFilePath)
			} else {
				if len(jsonContent) <= 0 {
					err = fmt.Errorf("file '%s' is empty. Status could not be read", jsonFilePath)
				} else {
					jsonContentBytes := []byte(jsonContent)

					err = json.Unmarshal(jsonContentBytes, &testStructure)

					if err != nil {
						err = fmt.Errorf("error unmarshalling json file into TestStructure - %v", err.Error())
					} else {
						log.Printf("setTestStructureFromRasFile - testStructure unmarshalled successfully")
						run.SetTestStructure(*testStructure)
					}
				}
			}
		}
	}

	if err != nil {
		log.Printf("setTestStructureFromRasFile - %v", err.Error())
	}
	return err
}

// GetStreams gets a list of streams available on this launcher
func (launcher *JvmLauncher) GetStreams() ([]string, error) {
	log.Printf("JvmLauncher: GetStreams entered.")
	return nil, nil
}

// GetTestCatalog gets the test catalog for a given stream.
func (launcher *JvmLauncher) GetTestCatalog(stream string) (TestCatalog, error) {
	log.Printf("JvmLauncher: GetTestCatalog entered. stream=%s", stream)
	return nil, nil
}

// -----------------------------------------------------------------------------
// Local functions
// -----------------------------------------------------------------------------

// getCommandSyntax From the parameters we aim to build a command-line incantation which would launch the test in a JVM...
// For example:
// java -jar ${BOOT_JAR_PATH} \
// --localmaven file:${M2_PATH}/repository/ \
// --remotemaven $REMOTE_MAVEN \
// --bootstrap file:${HOME}/.galasa/bootstrap.properties \
// --overrides file:${HOME}/.galasa/overrides.properties \
// --obr mvn:dev.galasa/dev.galasa.uber.obr/${OBR_VERSION}/obr \
// --obr mvn:${TEST_OBR_GROUP_ID}/${TEST_OBR_ARTIFACT_ID}/${TEST_OBR_VERSION}/obr \
// --test ${TEST_BUNDLE}/${TEST_JAVA_CLASS} | tee jvm-log.txt | grep "[*][*][*]" | grep -v "[*][*][*][*]" | sed -e "s/[--]*//g"
//
// For example:
//
//	java -jar /Users/mcobbett/builds/galasa/code/external/galasa-dev/cli/pkg/embedded/templates/galasahome/lib/galasa-boot-0.27.0.jar \
//	    --localmaven file:/Users/mcobbett/.m2/repository/ \
//	    --remotemaven https://development.galasa.dev/main/maven-repo/obr/ \
//	    --bootstrap file:/Users/mcobbett/.galasa/bootstrap.properties \
//	    --overrides file:/Users/mcobbett/.galasa/overrides.properties \
//	    --obr mvn:dev.galasa/dev.galasa.uber.obr/0.26.0/obr \
//	    --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
//	    --test dev.galasa.example.banking.payee/dev.galasa.example.banking.payee.TestPayee
func getCommandSyntax(
	bootstrapProperties props.JavaProperties,
	galasaHome spi.GalasaHome,
	fileSystem spi.FileSystem,
	javaHome string,
	testObrs []utils.MavenCoordinates,
	testLocation TestLocation,
	remoteMaven string,
	localMaven string,
	galasaVersionToRun string,
	overridesFilePath string,
	gherkinUrl string,
	isTraceEnabled bool,
	isDebugEnabled bool,
	debugPort uint32,
	debugMode string,
	jwt string,
) (string, []string, error) {

	var cmd string = ""
	var args []string = make([]string, 0)
	var err error
	var bootJarPath string

	// Gather any variable values we need to.
	debugMode, err = calculateDebugMode(debugMode, bootstrapProperties)
	if err == nil {
		debugPort, err = calculateDebugPort(debugPort, bootstrapProperties)
		if err == nil {
			bootJarPath, err = utils.GetGalasaBootJarPath(fileSystem, galasaHome)
		}
	}

	if err == nil {

		separator := fileSystem.GetFilePathSeparator()

		// Note: Even in windows, when the java executable is called 'java.exe'
		// You don't need to add the '.exe' extension it seems.
		cmd = javaHome + separator + "bin" +
			separator + "java"

		args = appendArgsDebugOptions(args, isDebugEnabled, debugMode, debugPort)

		args = appendArgsBootstrapJvmLaunchOptions(args, bootstrapProperties)

		// Note: Any -D properties are options for the JVM, so must appear before the -jar parameter.
		// Parameters after the -jar parameter get passed into the 'main' of the launched java program.
		args = append(args, "-Dfile.encoding=UTF-8")

		nativeGalasaHomeFolderPath := galasaHome.GetNativeFolderPath()
		args = append(args, `-DGALASA_HOME="`+nativeGalasaHomeFolderPath+`"`)

		// If there is a jwt, pass it through.
		if jwt != "" {
			args = append(args, "-DGALASA_JWT="+jwt)
		}

		args = append(args, "-jar")
		args = append(args, bootJarPath)

		// --localmaven file://${M2_PATH}/repository/
		// Note: URLs always have forward-slashes
		localMaven, err = defaultLocalMavenIfNotSet(localMaven, fileSystem)
		args = append(args, "--localmaven")
		args = append(args, localMaven)

		// --remotemaven $REMOTE_MAVEN
		args = append(args, "--remotemaven")
		args = append(args, remoteMaven)

		// --bootstrap file:${HOME}/.galasa/bootstrap.properties
		args = append(args, "--bootstrap")
		bootstrapPath := "file:///" + galasaHome.GetUrlFolderPath() + "/bootstrap.properties"
		args = append(args, bootstrapPath)

		// --overrides file:${HOME}/.galasa/overrides.properties
		args = append(args, "--overrides")
		// Note: We turn the file path provided into a URL so slashes always
		// go the same way.
		overridesPath := "file:///" + strings.ReplaceAll(overridesFilePath, "\\", "/")
		args = append(args, overridesPath)

		for _, obrCoordinate := range testObrs {
			// We are aiming for this:
			// mvn:${TEST_OBR_GROUP_ID}/${TEST_OBR_ARTIFACT_ID}/${TEST_OBR_VERSION}/obr
			args = append(args, "--obr")
			obrMvnPath := "mvn:" + obrCoordinate.GroupId + "/" +
				obrCoordinate.ArtifactId + "/" + obrCoordinate.Version + "/obr"
			args = append(args, obrMvnPath)
		}

		// --obr mvn:dev.galasa/dev.galasa.uber.obr/${OBR_VERSION}/obr
		args = append(args, "--obr")
		galasaUberObrPath := "mvn:dev.galasa/dev.galasa.uber.obr/" + galasaVersionToRun + "/obr"
		args = append(args, galasaUberObrPath)

		if gherkinUrl != "" {
			// -- gherkin file://gherkin.feature file location
			args = append(args, "--gherkin")
			args = append(args, gherkinUrl)
		} else {
			// --test ${TEST_BUNDLE}/${TEST_JAVA_CLASS}
			args = append(args, "--test")
			args = append(args, testLocation.OSGiBundleName+"/"+testLocation.QualifiedJavaClassName)
		}

		if isTraceEnabled {
			args = append(args, "--trace")
		}

	}

	return cmd, args, err
}

func appendArgsDebugOptions(args []string, isDebugEnabled bool, debugMode string, debugPort uint32) []string {

	if isDebugEnabled {
		var buff strings.Builder

		buff.WriteString("-agentlib:jdwp=transport=dt_socket,address=*:")
		buff.WriteString(strconv.FormatUint(uint64(debugPort), 10))
		buff.WriteString(",server=")
		if debugMode == "listen" {
			buff.WriteString("y")
		} else {
			buff.WriteString("n")
		}
		buff.WriteString(",suspend=y")

		args = append(args, buff.String())
	}

	return args
}

func appendArgsBootstrapJvmLaunchOptions(args []string, bootstrapProperties props.JavaProperties) []string {
	// Append all the java launch properties explicitly spelt-out in the boostrap file.
	// The framework.jvm.local.launch.options bootstrap file property can add parameters to the commmand-line.
	// For example -Xmx80m and similar parameters.
	// Use a space-separated list of options and the JVM gets launched with those in front.
	jvmLaunchOptions, isOptionsPresent := bootstrapProperties[api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_OPTIONS]
	if isOptionsPresent {
		// strip off the leading and trailing whitespace.
		jvmLaunchOptions = strings.Trim(jvmLaunchOptions, " \t\n\r")

		// Split based on commas
		launchOptionParts := strings.Split(jvmLaunchOptions, api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_OPTIONS_SEPARATOR)

		// Add each piece to the list of args returned.
		args = append(args, launchOptionParts...)
	}

	return args
}

func calculateDebugPort(debugPort uint32, bootstrapProperties props.JavaProperties) (uint32, error) {
	var err error

	if debugPort == 0 {
		// Debug port was not set on the command-line.

		// Look in the bootstrap properties for a value.
		bootstrapPropsValue, isPresent := bootstrapProperties[api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_DEBUG_PORT]
		if isPresent {
			// Not specified on command line. Use value in bootstrap property instead.
			var debugPortU64 uint64
			debugPortU64, err = strconv.ParseUint(bootstrapPropsValue, 10, 32)
			if err != nil {
				err = galasaErrors.NewGalasaError(
					galasaErrors.GALASA_ERROR_BOOTSTRAP_BAD_DEBUG_PORT_VALUE,
					bootstrapPropsValue,
					api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_DEBUG_PORT,
					strconv.FormatUint(uint64(DEBUG_PORT_DEFAULT), 10),
				)
			} else {
				// Bootstrap property value is good.
				debugPort = uint32(debugPortU64)
			}
		} else {
			// Not specified on command-linem, nothing in bootstrap property.
			debugPort = DEBUG_PORT_DEFAULT
		}
	}
	return debugPort, err
}

func calculateDebugMode(debugMode string, bootstrapProperties props.JavaProperties) (string, error) {
	var err error

	if debugMode == "" {
		// The value hasn't been set on the command-line.

		// Look in the bootstrap properties for a value.
		bootstrapPropsValue, isPresent := bootstrapProperties[api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_DEBUG_MODE]
		if isPresent {
			debugMode = bootstrapPropsValue
			err = checkDebugModeValueIsValid(debugMode, galasaErrors.GALASA_ERROR_BOOTSTRAP_BAD_DEBUG_MODE_VALUE)
		} else {
			// Default to 'listen'
			debugMode = "listen"
		}
	}

	if err == nil {
		err = checkDebugModeValueIsValid(debugMode, galasaErrors.GALASA_ERROR_ARG_BAD_DEBUG_MODE_VALUE)
	}

	return debugMode, err
}

func checkDebugModeValueIsValid(debugMode string, errorMessageIfInvalid *galasaErrors.MessageType) error {
	var err error

	lowerCaseDebugMode := strings.ToLower(debugMode)

	switch lowerCaseDebugMode {
	case "listen":
	case "attach":
		break
	default:
		err = galasaErrors.NewGalasaError(errorMessageIfInvalid, debugMode, api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_DEBUG_MODE)
	}

	return err
}

// User input is expected of the form osgiBundleName/qualifiedJavaClassName
// So split the two pieces apart to help validate them.
func classNameUserInputToTestClassLocation(classNameUserInput string) (*TestLocation, error) {

	var (
		err               error         = nil
		testClassToLaunch *TestLocation = nil
	)

	parts := strings.Split(classNameUserInput, "/")
	if len(parts) < 2 {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_CLASS_INPUT_NO_SLASH, classNameUserInput)
	} else if len(parts) > 2 {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_CLASS_TOO_MANY_SLASHES, classNameUserInput)
	} else if strings.HasSuffix(parts[1], ".class") {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_CLASS_SUFFIX_FOUND, classNameUserInput)
	} else {
		osgiBundleName := parts[0]
		qualifiedJavaClassName := parts[1]

		testClassToLaunch = &TestLocation{
			OSGiBundleName:         osgiBundleName,
			QualifiedJavaClassName: qualifiedJavaClassName,
		}
	}

	return testClassToLaunch, err
}

func checkGherkinURLisValid(gherkinURL string) error {
	var err error
	if !strings.HasSuffix(gherkinURL, ".feature") {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_GHERKIN_URL_BAD_EXTENSION, gherkinURL)
	}
	if !strings.HasPrefix(gherkinURL, "file://") {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_GHERKIN_URL_BAD_URL_PREFIX, gherkinURL)
	}
	return err
}
