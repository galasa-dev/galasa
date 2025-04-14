/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"testing"

	"github.com/galasa-dev/cli/pkg/files"
	"github.com/galasa-dev/cli/pkg/images"
	"github.com/galasa-dev/cli/pkg/launcher"
	"github.com/galasa-dev/cli/pkg/props"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

func TestCanWriteAndReadBackThrottleFile(t *testing.T) {

	mockFileSystem := files.NewMockFileSystem()
	env := utils.NewMockEnv()
	mockLauncher := launcher.NewMockLauncher()
	mockTimeService := utils.NewMockTimeService()
	timedSleeper := utils.NewRealTimedSleeper()

	galasaHome, _ := utils.NewGalasaHome(mockFileSystem, env, "")

	console := utils.NewMockConsole()

	submitter := NewSubmitter(
		galasaHome,
		mockFileSystem,
		mockLauncher,
		mockTimeService,
		timedSleeper,
		env,
		console,
		images.NewImageExpanderNullImpl(),
	)

	err := submitter.writeThrottleFile("throttle", 101)
	if err != nil {
		assert.Fail(t, "Should not have failed to write a throttle file. "+err.Error())
	}

	isThrottleFileExists, err := mockFileSystem.Exists("throttle")
	if err != nil {
		assert.Fail(t, "Should not have failed to check for the existence of a throttle file. "+err.Error())
	}

	assert.True(t, isThrottleFileExists, "throttle file does not exist!")

	var readBackThrottle int
	readBackThrottle, err = submitter.readThrottleFile("throttle")
	if err != nil {
		assert.Fail(t, "Should not have failed to read from a throttle file. "+err.Error())
	}
	assert.Equal(t, readBackThrottle, 101, "read back the wrong throttle value")
}

func TestReadBackThrottleFileFailsIfNoThrottleFileThere(t *testing.T) {

	var err error
	mockFileSystem := files.NewMockFileSystem()
	env := utils.NewMockEnv()
	mockLauncher := launcher.NewMockLauncher()
	mockTimeService := utils.NewMockTimeService()

	galasaHome, _ := utils.NewGalasaHome(mockFileSystem, env, "")

	console := utils.NewMockConsole()

	submitter := NewSubmitter(
		galasaHome,
		mockFileSystem,
		mockLauncher,
		mockTimeService,
		utils.NewRealTimedSleeper(),
		env,
		console,
		images.NewImageExpanderNullImpl(),
	)

	_, err = submitter.readThrottleFile("throttle")
	if err == nil {
		assert.Fail(t, "Should have failed to read from a throttle file. ")
	}
	assert.Contains(t, err.Error(), "GAL1048", "Error returned should contain GAL1048 error indicating read throttle file failed."+err.Error())
}

func TestReadBackThrottleFileFailsIfFileContainsInvalidInt(t *testing.T) {

	var err error
	mockFileSystem := files.NewMockFileSystem()

	mockFileSystem.WriteTextFile("throttle", "abc")

	env := utils.NewMockEnv()
	mockLauncher := launcher.NewMockLauncher()
	mockTimeService := utils.NewMockTimeService()

	galasaHome, _ := utils.NewGalasaHome(mockFileSystem, env, "")
	console := utils.NewMockConsole()

	submitter := NewSubmitter(
		galasaHome,
		mockFileSystem,
		mockLauncher,
		mockTimeService,
		utils.NewRealTimedSleeper(),
		env,
		console,
		images.NewImageExpanderNullImpl(),
	)

	_, err = submitter.readThrottleFile("throttle")
	if err == nil {
		assert.Fail(t, "Should have failed to read from a throttle file. ")
	}
	assert.Contains(t, err.Error(), "GAL1049E", "Error returned should contain GAL1049E error indicating read invalid throttle file content."+err.Error())
}

func TestUpdateThrottleFromFileIfDifferentChangesValueWhenDifferent(t *testing.T) {

	mockFileSystem := files.NewMockFileSystem()

	env := utils.NewMockEnv()
	mockLauncher := launcher.NewMockLauncher()
	mockTimeService := utils.NewMockTimeService()

	galasaHome, _ := utils.NewGalasaHome(mockFileSystem, env, "")
	console := utils.NewMockConsole()
	submitter := NewSubmitter(
		galasaHome,
		mockFileSystem,
		mockLauncher,
		mockTimeService,
		utils.NewRealTimedSleeper(),
		env,
		console,
		images.NewImageExpanderNullImpl(),
	)

	mockFileSystem.WriteTextFile("throttle", "10")
	newValue, isLost := submitter.updateThrottleFromFileIfDifferent("throttle", 20, false)

	assert.Equal(t, 10, newValue)
	assert.False(t, isLost)
}

func TestUpdateThrottleFromFileIfDifferentDoesntChangeIfFileMissing(t *testing.T) {

	mockFileSystem := files.NewMockFileSystem()

	env := utils.NewMockEnv()
	mockLauncher := launcher.NewMockLauncher()
	mockTimeService := utils.NewMockTimeService()

	galasaHome, _ := utils.NewGalasaHome(mockFileSystem, env, "")
	console := utils.NewMockConsole()
	submitter := NewSubmitter(
		galasaHome,
		mockFileSystem,
		mockLauncher,
		mockTimeService,
		utils.NewRealTimedSleeper(),
		env,
		console,
		images.NewImageExpanderNullImpl(),
	)

	// mockFileSystem.WriteTextFile("throttle", "10") - file is missing now.
	newValue, isLost := submitter.updateThrottleFromFileIfDifferent("throttle", 20, false)

	assert.Equal(t, 20, newValue)
	assert.True(t, isLost)
}

func TestOverridesReadFromOverridesFile(t *testing.T) {

	fileProps := make(map[string]interface{})
	fileProps["c"] = "d"

	mockFileSystem := files.NewMockFileSystem()
	props.WritePropertiesFile(mockFileSystem, "/tmp/temp.properties", fileProps)

	commandParameters := utils.RunsSubmitCmdValues{
		Overrides:         []string{"a=b"},
		OverrideFilePaths: []string{"/tmp/temp.properties"},
	}

	env := utils.NewMockEnv()
	mockLauncher := launcher.NewMockLauncher()
	mockTimeService := utils.NewMockTimeService()

	galasaHome, _ := utils.NewGalasaHome(mockFileSystem, env, "")
	console := utils.NewMockConsole()
	submitter := NewSubmitter(
		galasaHome,
		mockFileSystem,
		mockLauncher,
		mockTimeService,
		utils.NewRealTimedSleeper(),
		env,
		console,
		images.NewImageExpanderNullImpl(),
	)

	overrides, err := submitter.buildOverrideMap(commandParameters)

	assert.Nil(t, err)
	assert.NotNil(t, overrides)
	assert.Contains(t, overrides, "a", "command-line override wasn't used.")
	assert.Equal(t, overrides["a"], "b", "command-line override not passed correctly.")
	assert.Contains(t, overrides, "c", "file-based override wasn't used")
	assert.Equal(t, overrides["c"], "d", "file-based override value wasn't passed correctly.")
}

func TestOverridesFileSpecifiedButDoesNotExist(t *testing.T) {

	fileProps := make(map[string]interface{})
	fileProps["c"] = "d"

	mockFileSystem := files.NewMockFileSystem()
	props.WritePropertiesFile(mockFileSystem, "/tmp/temp.properties", fileProps)

	commandParameters := utils.RunsSubmitCmdValues{
		Overrides:         []string{"a=b"},
		OverrideFilePaths: []string{"/tmp/temp.wrong.file.properties"},
	}

	env := utils.NewMockEnv()
	mockLauncher := launcher.NewMockLauncher()
	mockTimeService := utils.NewMockTimeService()

	galasaHome, _ := utils.NewGalasaHome(mockFileSystem, env, "")
	console := utils.NewMockConsole()
	submitter := NewSubmitter(
		galasaHome,
		mockFileSystem,
		mockLauncher,
		mockTimeService,
		utils.NewRealTimedSleeper(),
		env,
		console,
		images.NewImageExpanderNullImpl(),
	)
	overrides, err := submitter.buildOverrideMap(commandParameters)

	assert.NotNil(t, err)
	assert.Nil(t, overrides)

	assert.Contains(t, err.Error(), "GAL1059")
}

func TestOverrideFileCorrectedWhenDefaultedAndOverridesFileNotExists(t *testing.T) {

	mockFileSystem := files.NewMockFileSystem()
	env := utils.NewMockEnv()
	galasaHome, err := utils.NewGalasaHome(mockFileSystem, env, "")
	if err != nil {
		assert.Fail(t, "Should not have failed! message = %s", err.Error())
	}

	commandParameters := utils.RunsSubmitCmdValues{
		Overrides:         []string{"a=b"},
		OverrideFilePaths: []string{"="},
	}

	mockLauncher := launcher.NewMockLauncher()
	mockTimeService := utils.NewMockTimeService()
	console := utils.NewMockConsole()
	submitter := NewSubmitter(
		galasaHome,
		mockFileSystem,
		mockLauncher,
		mockTimeService,
		utils.NewRealTimedSleeper(),
		env,
		console,
		images.NewImageExpanderNullImpl(),
	)
	err = submitter.correctOverrideFilePathParameter(&commandParameters)

	if err != nil {
		assert.Fail(t, "Should not have failed! message = %s", err.Error())
	}
	// We expect the default behaviour with missing command-line parameter, and missing overrides file in ~/.galasa to
	// result in an ignored overrideFilePath.
	assert.Equal(t, commandParameters.OverrideFilePaths, []string{"="})
}

func TestOverrideFileCorrectedWhenDefaultedAndNoOverridesFileDoesExist(t *testing.T) {

	mockFileSystem := files.NewMockFileSystem()
	env := utils.NewMockEnv()
	galasaHome, err := utils.NewGalasaHome(mockFileSystem, env, "")
	if err != nil {
		assert.Fail(t, "Should not have failed! message = %s", err.Error())
	}

	// A dummy overrides file in .galasa
	home, _ := mockFileSystem.GetUserHomeDirPath()
	separator := mockFileSystem.GetFilePathSeparator()
	path := home + separator + ".galasa" + separator + "overrides.properties"
	fileProps := make(map[string]interface{})
	fileProps["c"] = "d"
	props.WritePropertiesFile(mockFileSystem, path, fileProps)

	commandParameters := utils.RunsSubmitCmdValues{
		Overrides:         []string{"a=b"},
		OverrideFilePaths: []string{path},
	}

	mockLauncher := launcher.NewMockLauncher()
	mockTimeService := utils.NewMockTimeService()
	console := utils.NewMockConsole()
	submitter := NewSubmitter(
		galasaHome,
		mockFileSystem,
		mockLauncher,
		mockTimeService,
		utils.NewRealTimedSleeper(),
		env,
		console,
		images.NewImageExpanderNullImpl(),
	)
	err = submitter.correctOverrideFilePathParameter(&commandParameters)

	if err != nil {
		assert.Fail(t, "Should not have failed! message = %s", err.Error())
	}
	assert.Equal(t, commandParameters.OverrideFilePaths, []string{path}, "Wrong path of overrides file set. Expected %s", path)
}

func TestOverridesWithDashFileDontReadFromAnyFile(t *testing.T) {

	mockFileSystem := files.NewMockFileSystem()
	env := utils.NewMockEnv()
	galasaHome, _ := utils.NewGalasaHome(mockFileSystem, env, "")

	commandParameters := utils.RunsSubmitCmdValues{
		Overrides:         []string{"a=b"},
		OverrideFilePaths: []string{"-"},
	}

	mockLauncher := launcher.NewMockLauncher()
	mockTimeService := utils.NewMockTimeService()
	console := utils.NewMockConsole()
	submitter := NewSubmitter(
		galasaHome,
		mockFileSystem,
		mockLauncher,
		mockTimeService,
		utils.NewRealTimedSleeper(),
		env,
		console,
		images.NewImageExpanderNullImpl(),
	)
	overrides, err := submitter.buildOverrideMap(commandParameters)

	if err != nil {
		assert.Fail(t, "Should not have failed! message = %s", err.Error())
	}
	assert.NotNil(t, overrides)
	assert.Contains(t, overrides, "a", "command-line override wasn't used.")
	assert.Equal(t, overrides["a"], "b", "command-line override not passed correctly.")
}

func TestValidateAndCorrectParametersSetsDefaultOverrideFile(t *testing.T) {

	mockFileSystem := files.NewMockFileSystem()
	env := utils.NewMockEnv()
	env.SetUserName("myuserid")

	galasaHome, err := utils.NewGalasaHome(mockFileSystem, env, "")
	if err != nil {
		assert.Fail(t, "Should not have failed! message = %s", err.Error())
	}

	commandParameters := &utils.RunsSubmitCmdValues{
		Overrides:         []string{"a=b"},
		OverrideFilePaths: []string{""},
	}

	regexSelectValue := false
	submitSelectionFlags := &utils.TestSelectionFlagValues{
		Bundles:     new([]string),
		Packages:    new([]string),
		Tests:       new([]string),
		Tags:        new([]string),
		Classes:     new([]string),
		Stream:      "myStream",
		RegexSelect: &regexSelectValue,
		GherkinUrl:  new([]string),
	}

	mockLauncher := launcher.NewMockLauncher()

	mockTimeService := utils.NewMockTimeService()
	console := utils.NewMockConsole()
	submitter := NewSubmitter(
		galasaHome,
		mockFileSystem,
		mockLauncher,
		mockTimeService,
		utils.NewRealTimedSleeper(),
		env,
		console,
		images.NewImageExpanderNullImpl(),
	)
	err = submitter.validateAndCorrectParams(commandParameters, submitSelectionFlags)

	assert.Nil(t, err)
	assert.NotEmpty(t, commandParameters.OverrideFilePaths)
}

func TestLocalLaunchCanUseAPortfolioOk(t *testing.T) {

	mockFileSystem := files.NewMockFileSystem()

	obrName := "myobr"
	bundleName := "myBundle"
	className := "myClass"

	portfolioFilePath := "myportfolio.yaml"
	_ = createTestPortfolioFile(t, mockFileSystem, portfolioFilePath, bundleName, className, "", obrName)

	env := utils.NewMockEnv()
	env.SetUserName("myuserid")

	galasaHome, err := utils.NewGalasaHome(mockFileSystem, env, "")
	if err != nil {
		assert.Fail(t, "Should not have failed! message = %s", err.Error())
	}

	commandParameters := &utils.RunsSubmitCmdValues{}
	commandParameters.PortfolioFileName = portfolioFilePath

	regexSelectValue := false
	submitSelectionFlags := &utils.TestSelectionFlagValues{
		Bundles:     new([]string),
		Packages:    new([]string),
		Tests:       new([]string),
		Tags:        new([]string),
		Classes:     new([]string),
		Stream:      "",
		RegexSelect: &regexSelectValue,
		GherkinUrl:  new([]string),
	}

	mockLauncher := launcher.NewMockLauncher()

	mockTimeService := utils.NewMockTimeService()
	console := utils.NewMockConsole()
	submitter := NewSubmitter(
		galasaHome,
		mockFileSystem,
		mockLauncher,
		mockTimeService,
		utils.NewRealTimedSleeper(),
		env,
		console,
		images.NewImageExpanderNullImpl(),
	)
	// Do the launching of the tests.
	err = submitter.ExecuteSubmitRuns(
		commandParameters,
		submitSelectionFlags,
	)

	assert.Nil(t, err)

	launchesRecorded := mockLauncher.GetRecordedLaunchRecords()

	assert.Equal(t, 1, len(launchesRecorded))
	if len(launchesRecorded) > 0 {
		assert.Equal(t, obrName, launchesRecorded[0].ObrFromPortfolio)
		assert.Equal(t, bundleName+"/"+className, launchesRecorded[0].ClassName)
	}
	assert.Contains(t, console.ReadText(), bundleName+"/"+className)
}

func TestSubmitRunwithGherkinFile(t *testing.T) {
	mockFileSystem := files.NewMockFileSystem()
	env := utils.NewMockEnv()
	env.SetUserName("myuserid")

	galasaHome, err := utils.NewGalasaHome(mockFileSystem, env, "")
	if err != nil {
		assert.Fail(t, "Should not have failed! message = %s", err.Error())
	}

	mockLauncher := launcher.NewMockLauncher()

	mockTimeService := utils.NewMockTimeService()
	console := utils.NewMockConsole()
	submitter := NewSubmitter(
		galasaHome,
		mockFileSystem,
		mockLauncher,
		mockTimeService,
		utils.NewRealTimedSleeper(),
		env,
		console,
		images.NewImageExpanderNullImpl(),
	)

	groupName := "groupname"
	var readyRuns []TestRun
	testRun := TestRun{
		GherkinUrl: "gherkin.feature",
	}
	readyRuns = append(readyRuns, testRun)
	submittedRuns := make(map[string]*TestRun)
	lostRuns := make(map[string]*TestRun)
	runOverrides := new(map[string]string)
	trace := false
	requestor := "user"
	requestType := ""

	run, err := submitter.submitRun(groupName, readyRuns, submittedRuns, lostRuns, runOverrides, trace, requestor, requestType)
	assert.Nil(t, err)
	assert.Empty(t, run)
	assert.Contains(t, submittedRuns["M100"].GherkinUrl, "gherkin.feature")

}

func TestGetPortfolioReturnsGherkinPortfolio(t *testing.T) {
	mockFileSystem := files.NewMockFileSystem()
	env := utils.NewMockEnv()
	env.SetUserName("myuserid")

	galasaHome, err := utils.NewGalasaHome(mockFileSystem, env, "")
	if err != nil {
		assert.Fail(t, "Should not have failed! message = %s", err.Error())
	}

	mockLauncher := launcher.NewMockLauncher()

	mockTimeService := utils.NewMockTimeService()
	console := utils.NewMockConsole()
	submitter := NewSubmitter(
		galasaHome,
		mockFileSystem,
		mockLauncher,
		mockTimeService,
		utils.NewRealTimedSleeper(),
		env,
		console,
		images.NewImageExpanderNullImpl(),
	)

	flags := NewTestSelectionFlagValues()

	*flags.GherkinUrl = make([]string, 3)
	(*flags.GherkinUrl)[0] = "file:///demo/gherkin.feature"
	(*flags.GherkinUrl)[1] = "file:///demo/test.feature"
	(*flags.GherkinUrl)[2] = "file:///demo/excellent.feature"

	portfolio, err := submitter.getPortfolio("", flags)

	assert.Nil(t, err)
	assert.NotEmpty(t, portfolio)
	assert.Contains(t, portfolio.Classes[0].GherkinUrl, "file:///demo/gherkin.feature")
	assert.Contains(t, portfolio.Classes[1].GherkinUrl, "file:///demo/test.feature")
	assert.Contains(t, portfolio.Classes[2].GherkinUrl, "file:///demo/excellent.feature")
}

func TestGetReadyRunsFromPortfolioReturnsGherkinReadyRuns(t *testing.T) {
	mockFileSystem := files.NewMockFileSystem()
	env := utils.NewMockEnv()
	env.SetUserName("myuserid")

	galasaHome, err := utils.NewGalasaHome(mockFileSystem, env, "")
	if err != nil {
		assert.Fail(t, "Should not have failed! message = %s", err.Error())
	}

	mockLauncher := launcher.NewMockLauncher()

	mockTimeService := utils.NewMockTimeService()
	console := utils.NewMockConsole()
	submitter := NewSubmitter(
		galasaHome,
		mockFileSystem,
		mockLauncher,
		mockTimeService,
		utils.NewRealTimedSleeper(),
		env,
		console,
		images.NewImageExpanderNullImpl(),
	)

	flags := NewTestSelectionFlagValues()

	*flags.GherkinUrl = make([]string, 3)
	(*flags.GherkinUrl)[0] = "file:///demo/gherkin.feature"
	(*flags.GherkinUrl)[1] = "file:///demo/test.feature"
	(*flags.GherkinUrl)[2] = "file:///demo/excellent.feature"

	portfolio, err := submitter.getPortfolio("", flags)
	if err != nil {
		assert.Fail(t, "Should not have failed! message = %s", err.Error())
	}

	overrides := make(map[string]string)

	readyRuns := submitter.buildListOfRunsToSubmit(portfolio, overrides)

	assert.NotEmpty(t, readyRuns)
	assert.Contains(t, readyRuns[0].GherkinUrl, "file:///demo/gherkin.feature")
	assert.Contains(t, readyRuns[0].GherkinFeature, "gherkin")
	assert.Contains(t, readyRuns[1].GherkinUrl, "file:///demo/test.feature")
	assert.Contains(t, readyRuns[1].GherkinFeature, "test")
	assert.Contains(t, readyRuns[2].GherkinUrl, "file:///demo/excellent.feature")
	assert.Contains(t, readyRuns[2].GherkinFeature, "excellent")
}

func TestSubmitRunsFromGherkinPortfolioOutputsFeatureNames(t *testing.T) {
	mockFileSystem := files.NewMockFileSystem()
	env := utils.NewMockEnv()
	env.SetUserName("myuserid")

	galasaHome, err := utils.NewGalasaHome(mockFileSystem, env, "")
	if err != nil {
		assert.Fail(t, "Should not have failed! message = %s", err.Error())
	}

	mockLauncher := launcher.NewMockLauncher()

	mockTimeService := utils.NewMockTimeService()
	console := utils.NewMockConsole()
	submitter := NewSubmitter(
		galasaHome,
		mockFileSystem,
		mockLauncher,
		mockTimeService,
		utils.NewRealTimedSleeper(),
		env,
		console,
		images.NewImageExpanderNullImpl(),
	)

	flags := NewTestSelectionFlagValues()

	*flags.GherkinUrl = make([]string, 3)
	(*flags.GherkinUrl)[0] = "file:///demo/gherkin.feature"
	(*flags.GherkinUrl)[1] = "file:///demo/test.feature"
	(*flags.GherkinUrl)[2] = "file:///demo/excellent.feature"

	portfolio, err := submitter.getPortfolio("", flags)
	if err != nil {
		assert.Fail(t, "Should not have failed! message = %s", err.Error())
	}

	overrides := make(map[string]string)

	readyRuns := submitter.buildListOfRunsToSubmit(portfolio, overrides)

	assert.NotEmpty(t, readyRuns)
	assert.Contains(t, readyRuns[0].GherkinUrl, "file:///demo/gherkin.feature")
	assert.Contains(t, readyRuns[0].GherkinFeature, "gherkin")
	assert.Contains(t, readyRuns[1].GherkinUrl, "file:///demo/test.feature")
	assert.Contains(t, readyRuns[1].GherkinFeature, "test")
	assert.Contains(t, readyRuns[2].GherkinUrl, "file:///demo/excellent.feature")
	assert.Contains(t, readyRuns[2].GherkinFeature, "excellent")
}

func TestOverridesReadFromMultipleOverrideFiles(t *testing.T) {

	fileProps := make(map[string]interface{})
	fileProps["c"] = "d"

	dummyFileProps := make(map[string]interface{})
	dummyFileProps["e"] = "f"

	mockFileSystem := files.NewMockFileSystem()
	props.WritePropertiesFile(mockFileSystem, "/tmp/temp.properties", fileProps)
	props.WritePropertiesFile(mockFileSystem, "/tmp/temp2.properties", dummyFileProps)

	commandParameters := utils.RunsSubmitCmdValues{
		Overrides:         []string{"a=b"},
		OverrideFilePaths: []string{"/tmp/temp.properties", "/tmp/temp2.properties"},
	}

	env := utils.NewMockEnv()
	mockLauncher := launcher.NewMockLauncher()
	mockTimeService := utils.NewMockTimeService()

	galasaHome, _ := utils.NewGalasaHome(mockFileSystem, env, "")
	console := utils.NewMockConsole()
	submitter := NewSubmitter(
		galasaHome,
		mockFileSystem,
		mockLauncher,
		mockTimeService,
		utils.NewRealTimedSleeper(),
		env,
		console,
		images.NewImageExpanderNullImpl(),
	)

	overrides, err := submitter.buildOverrideMap(commandParameters)

	assert.Nil(t, err)
	assert.NotNil(t, overrides)
	assert.Contains(t, overrides, "a", "command-line override wasn't used.")
	assert.Equal(t, overrides["a"], "b", "command-line override not passed correctly.")
	assert.Contains(t, overrides, "c", "file-based override wasn't used")
	assert.Equal(t, overrides["c"], "d", "file-based override value wasn't passed correctly.")
	assert.Contains(t, overrides, "e", "file-based override for 'e' wasn't used")
	assert.Equal(t, "f", overrides["e"], "file-based override for 'e' wasn't passed correctly")

}

func TestOverridesReadFromMultipleWithDashSkipsOverrideFile(t *testing.T) {

	fileProps := make(map[string]interface{})
	fileProps["c"] = "d"

	dummyFileProps := make(map[string]interface{})
	dummyFileProps["e"] = "f"

	mockFileSystem := files.NewMockFileSystem()
	props.WritePropertiesFile(mockFileSystem, "/tmp/temp.properties", fileProps)
	props.WritePropertiesFile(mockFileSystem, "/tmp/temp2.properties", dummyFileProps)

	commandParameters := utils.RunsSubmitCmdValues{
		Overrides:         []string{"a=b"},
		OverrideFilePaths: []string{"/tmp/temp.properties", "-", "/tmp/temp2.properties"}, //Pass an empty path (-), should skip over
	}

	env := utils.NewMockEnv()
	mockLauncher := launcher.NewMockLauncher()
	mockTimeService := utils.NewMockTimeService()

	galasaHome, _ := utils.NewGalasaHome(mockFileSystem, env, "")
	console := utils.NewMockConsole()
	submitter := NewSubmitter(
		galasaHome,
		mockFileSystem,
		mockLauncher,
		mockTimeService,
		utils.NewRealTimedSleeper(),
		env,
		console,
		images.NewImageExpanderNullImpl(),
	)

	overrides, err := submitter.buildOverrideMap(commandParameters)

	assert.Nil(t, err)
	assert.NotNil(t, overrides)
	assert.Contains(t, overrides, "a", "command-line override wasn't used.")
	assert.Equal(t, overrides["a"], "b", "command-line override not passed correctly.")
	assert.Contains(t, overrides, "c", "file-based override wasn't used")
	assert.Equal(t, overrides["c"], "d", "file-based override value wasn't passed correctly.")
	assert.Contains(t, overrides, "e", "file-based override for 'e' wasn't used")
	assert.Equal(t, "f", overrides["e"], "file-based override for 'e' wasn't passed correctly")

}

func TestOverridesFileSpecifiedWhereSomeFilesDoNotExistInArray(t *testing.T) {

	fileProps := make(map[string]interface{})
	fileProps["c"] = "d"

	mockFileSystem := files.NewMockFileSystem()
	props.WritePropertiesFile(mockFileSystem, "/tmp/temp.properties", fileProps)

	commandParameters := utils.RunsSubmitCmdValues{
		Overrides:         []string{"a=b"},
		OverrideFilePaths: []string{"/tmp/temp.properties", "/tmp/temp.wrong.file.properties"},
	}

	env := utils.NewMockEnv()
	mockLauncher := launcher.NewMockLauncher()
	mockTimeService := utils.NewMockTimeService()

	galasaHome, _ := utils.NewGalasaHome(mockFileSystem, env, "")
	console := utils.NewMockConsole()
	submitter := NewSubmitter(
		galasaHome,
		mockFileSystem,
		mockLauncher,
		mockTimeService,
		utils.NewRealTimedSleeper(),
		env,
		console,
		images.NewImageExpanderNullImpl(),
	)
	overrides, err := submitter.buildOverrideMap(commandParameters)

	assert.NotNil(t, err)
	assert.Nil(t, overrides)

	assert.Contains(t, err.Error(), "GAL1059")
}

func TestOverridesFileSpecifiedWhereSomeOverridesAreInvalid(t *testing.T) {

	fileProps := make(map[string]interface{})
	fileProps["c"] = "d"

	mockFileSystem := files.NewMockFileSystem()
	props.WritePropertiesFile(mockFileSystem, "/tmp/temp.properties", fileProps)

	commandParameters := utils.RunsSubmitCmdValues{
		Overrides:         []string{"a=b", "c&d", "e=f"},
		OverrideFilePaths: []string{"/tmp/temp.properties"},
	}

	env := utils.NewMockEnv()
	mockLauncher := launcher.NewMockLauncher()
	mockTimeService := utils.NewMockTimeService()

	galasaHome, _ := utils.NewGalasaHome(mockFileSystem, env, "")
	console := utils.NewMockConsole()
	submitter := NewSubmitter(
		galasaHome,
		mockFileSystem,
		mockLauncher,
		mockTimeService,
		utils.NewRealTimedSleeper(),
		env,
		console,
		images.NewImageExpanderNullImpl(),
	)
	overrides, err := submitter.buildOverrideMap(commandParameters)

	assert.NotNil(t, err)
	assert.Nil(t, overrides)

	assert.Contains(t, err.Error(), "GAL1010")
}
