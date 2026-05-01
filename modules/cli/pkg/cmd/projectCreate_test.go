/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"errors"
	"fmt"
	"regexp"
	"strings"
	"testing"

	"github.com/galasa-dev/cli/pkg/files"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

func TestCanCreateProjectFailsIfPackageNameInvalid(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	forceOverwrite := true
	isObrProjectRequired := false
	featureNamesCommandSeparatedList := "test"
	maven := true
	gradle := false
	isDevelopment := false

	// When ...
	err := createProject(mockFileSystem, "very.INVALID_PACKAGE_NAME.very",
		featureNamesCommandSeparatedList, isObrProjectRequired, forceOverwrite, maven, gradle, isDevelopment, false, "")

	// Then...
	// Should have created a folder for the parent package.
	assert.NotNil(t, err, "Should return an error if java package name is invalid")
	assert.Contains(t, err.Error(), "GAL1037E:", "Wrong error message reported.")
}

func TestCanCreateProjectGoldenPathNoOBR(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	forceOverwrite := true
	isObrProjectRequired := false
	featureNamesCommandSeparatedList := "test"
	maven := true
	gradle := false
	isDevelopment := false
	packageName := "my.test.pkg"

	// When ...
	err := createProject(
		mockFileSystem, packageName, featureNamesCommandSeparatedList,
		isObrProjectRequired, forceOverwrite, maven, gradle, isDevelopment, false, "")

	// Then...
	// Should have created a folder for the parent package.
	if err != nil {
		assert.Fail(t, "Golden path should not return an error. %s", err.Error())
	}

	assertParentFolderAndContentsCreated(t, mockFileSystem, isObrProjectRequired, maven, gradle, packageName)
	assertTestFolderAndContentsCreatedOk(t, mockFileSystem, "test", maven, gradle)
}

func assertParentFolderAndContentsCreated(t *testing.T, mockFileSystem spi.FileSystem, isObrProjectRequired bool, isMaven bool, isGradle bool, packageName string) {
	parentFolderExists, err := mockFileSystem.DirExists(packageName)
	assert.Nil(t, err)
	assert.True(t, parentFolderExists, "Parent folder was not created.")

	if isMaven {
		parentPomXmlFileExists, err := mockFileSystem.Exists("my.test.pkg/pom.xml")
		assert.Nil(t, err)
		assert.True(t, parentPomXmlFileExists, "Parent folder pom.xml was not created.")

		gitIgnoreFileExists, err := mockFileSystem.Exists("my.test.pkg/.gitignore")
		assert.Nil(t, err)
		assert.True(t, gitIgnoreFileExists, "Parent folder .gitignore was not created.")

		text, err := mockFileSystem.ReadTextFile("my.test.pkg/pom.xml")
		assert.Nil(t, err)
		assert.Contains(t, text, "<groupId>my.test.pkg</groupId>", "parent pom.xml didn't substitute the group id")
		assert.Contains(t, text, "<artifactId>my.test.pkg</artifactId>", "parent pom.xml didn't substitute the artifact id")

		assert.Contains(t, text, "<module>my.test.pkg.test</module>", "parent pom.xml didn't have a test module included")

		if isObrProjectRequired {
			assert.Contains(t, text, "<module>my.test.pkg.obr</module>", "parent pom.xml didn't have an obr module included")
		} else {
			assert.NotContains(t, text, "<module>my.test.pkg.obr</module>", "parent pom.xml should not have an obr module included")
		}
	}

	if isGradle {
		parentSettingsGradleFileExists, err := mockFileSystem.Exists("my.test.pkg/settings.gradle")
		assert.Nil(t, err)
		assert.True(t, parentSettingsGradleFileExists, "Parent folder settings.gradle was not created.")

		text, err := mockFileSystem.ReadTextFile("my.test.pkg/settings.gradle")
		assert.Nil(t, err)
		assert.Contains(t, text, "include 'my.test.pkg.test'", "parent settings.gradle didn't have a test module included")

		if isObrProjectRequired {
			assert.Contains(t, text, "include 'my.test.pkg.obr'", "parent settings.gradle didn't have an obr module included")
		} else {
			assert.NotContains(t, text, "include 'my.test.pkg.obr'", "parent settings.gradle should not have an obr module included")

			// Make sure the OBR folder does not exist.
			expectedObrFolderPath := packageName + "/" + packageName + ".obr"
			var obrFolderExists bool
			obrFolderExists, _ = mockFileSystem.DirExists(expectedObrFolderPath)
			assert.False(t, obrFolderExists, "OBR folder exists, when it should not!")
		}
	}
}

func assertTestFolderAndContentsCreatedOk(t *testing.T, mockFileSystem spi.FileSystem, featureName string, isMaven bool, isGradle bool) {

	testFolderExists, err := mockFileSystem.DirExists("my.test.pkg/my.test.pkg." + featureName)
	assert.Nil(t, err)
	assert.True(t, testFolderExists, "Test"+featureName+" folder was not created.")

	if isMaven {
		expectedPomFilePath := "my.test.pkg/my.test.pkg." + featureName + "/pom.xml"
		testPomXmlFileExists, err := mockFileSystem.Exists(expectedPomFilePath)
		assert.Nil(t, err)
		assert.True(t, testPomXmlFileExists, "Test folder pom.xml was not created for feature."+featureName)

		text, err := mockFileSystem.ReadTextFile(expectedPomFilePath)
		assert.Nil(t, err)
		assert.Contains(t, text, "<groupId>my.test.pkg</groupId>", "Test folder pom.xml didn't substitute the group id")
		assert.Contains(t, text, "<artifactId>my.test.pkg.test</artifactId>", "Test folder pom.xml didn't substitute the artifact id")
	}

	if isGradle {
		expectedBuildGradleFilePath := "my.test.pkg/my.test.pkg." + featureName + "/build.gradle"
		testBuildGradleFileExists, err := mockFileSystem.Exists(expectedBuildGradleFilePath)
		assert.Nil(t, err)
		assert.True(t, testBuildGradleFileExists, "Test folder build.gradle was not created for feature."+featureName)

		expectedBndFilePath := "my.test.pkg/my.test.pkg." + featureName + "/bnd.bnd"
		testBndFileExists, err := mockFileSystem.Exists(expectedBndFilePath)
		assert.Nil(t, err)
		assert.True(t, testBndFileExists, "Test folder bnd.bnd was not created for feature."+featureName)

		buildGradleText, err := mockFileSystem.ReadTextFile(expectedBuildGradleFilePath)
		assert.Nil(t, err)
		assert.Contains(t, buildGradleText, "group = 'my.test.pkg'", "Test folder build.gradle didn't substitute the group id")

		bndFileText, err := mockFileSystem.ReadTextFile(expectedBndFilePath)
		assert.Nil(t, err)
		assert.Contains(t, bndFileText, "Bundle-Name: my.test.pkg", "Test folder bnd.bnd didn't substitute the bundle name")
	}

	testSrcFolderExists, err := mockFileSystem.DirExists("my.test.pkg/my.test.pkg.test/src/main/java/my/test/pkg/test")
	assert.Nil(t, err)
	assert.True(t, testSrcFolderExists, "Test src folder was not created.")

	// Examine the test java file generated.
	expectedJavaFilePath := "my.test.pkg/my.test.pkg." + featureName + "/src/main/java/my/test/pkg/" + featureName + "/Test" + utils.UppercaseFirstLetter(featureName) + ".java"
	assertJavaFileWasGenerated(t, mockFileSystem, expectedJavaFilePath, "my.test.pkg")

	// Examine the extended test java file generated.
	expectedJavaFilePath = "my.test.pkg/my.test.pkg." + featureName + "/src/main/java/my/test/pkg/" + featureName + "/Test" + utils.UppercaseFirstLetter(featureName) + "Extended.java"
	assertJavaFileWasGenerated(t, mockFileSystem, expectedJavaFilePath, "my.test.pkg")

	// Examine the resources file generated.
	expectedTextFilePath := "my.test.pkg/my.test.pkg." + featureName + "/src/main/resources/textfiles/sampleText.txt"
	isTestResourcesTextFileExists, err := mockFileSystem.Exists(expectedTextFilePath)
	assert.Nil(t, err)
	assert.True(t, isTestResourcesTextFileExists, "Test text resource file was not created.")
}

func assertJavaFileWasGenerated(t *testing.T, mockFileSystem spi.FileSystem, expectedJavaFilePath string, packageName string) {
	testJavaFileExists, err := mockFileSystem.Exists(expectedJavaFilePath)
	assert.Nil(t, err)
	assert.True(t, testJavaFileExists, "Test java file was not created.")

	var text string
	text, err = mockFileSystem.ReadTextFile(expectedJavaFilePath)
	assert.Nil(t, err)
	assert.Contains(t, text, "package "+packageName, "Test java file didn't substitute the java package")
}

func TestCreateProjectErrorsWhenMkAllDirsFails(t *testing.T) {

	// Given...
	mockFileSystem := files.NewOverridableMockFileSystem()
	forceOverwrite := true
	isObrProjectRequired := false
	featureNamesCommandSeparatedList := "test"
	maven := true
	gradle := false
	isDevelopment := false

	// Over-ride the default MkdirAll function so that it fails...
	mockFileSystem.VirtualFunction_MkdirAll = func(targetFolderPath string) error {
		return errors.New("Simulated MkdirAll failure")
	}

	// When ...
	err := createProject(
		mockFileSystem, "my.test.pkg", featureNamesCommandSeparatedList,
		isObrProjectRequired, forceOverwrite, maven, gradle, isDevelopment, false, "")

	// Then...
	assert.NotNil(t, err, "Sumulated error didn't bubble up to the top.")
	assert.Contains(t, err.Error(), "Simulated MkdirAll failure", "Failed to return reason for failure.")

}

func TestCreateProjectErrorsWhenWriteTextFileFails(t *testing.T) {
	// Given...
	mockFileSystem := files.NewOverridableMockFileSystem()
	forceOverwrite := true
	isObrProjectRequired := false
	featureNamesCommandSeparatedList := "test"
	maven := true
	gradle := false
	isDevelopment := false

	// Over-ride the default WriteTextFile function so that it fails...
	mockFileSystem.VirtualFunction_WriteTextFile = func(targetFilePath string, desiredContents string) error {
		return errors.New("Simulated WriteTextFile failure")
	}

	// When ...
	err := createProject(
		mockFileSystem, "my.test.pkg", featureNamesCommandSeparatedList,
		isObrProjectRequired, forceOverwrite, maven, gradle, isDevelopment, false, "")

	// Then...
	assert.NotNil(t, err, "Sumulated error didn't bubble up to the top.")
	assert.Contains(t, err.Error(), "Simulated WriteTextFile failure", "Failed to return reason for failure.")
}

func TestCreateProjectPomFileAlreadyExistsNoForceOverwrite(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	forceOverwrite := false
	isObrProjectRequired := false
	testPackageName := "my.test.pkg"
	featureNamesCommandSeparatedList := "test"
	maven := true
	gradle := false
	isDevelopment := false

	// Create a pom.xml file already...
	mockFileSystem.MkdirAll(testPackageName)
	mockFileSystem.WriteTextFile(testPackageName+"/pom.xml", "dummy test pom.xml")

	// When ...
	err := createProject(
		mockFileSystem, testPackageName, featureNamesCommandSeparatedList,
		isObrProjectRequired, forceOverwrite, maven, gradle, isDevelopment, false, "")

	// Then...
	// Should have created a folder for the parent package.
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1036E")
}

func TestCreateProjectPomFileAlreadyExistsWithForceOverwrite(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	isObrProjectRequired := false
	forceOverwrite := true
	testPackageName := "my.test.pkg"
	featureNamesCommandSeparatedList := "test"
	maven := true
	gradle := false
	isDevelopment := false

	// Create a pom.xml file already...
	mockFileSystem.MkdirAll(testPackageName)
	mockFileSystem.WriteTextFile(testPackageName+"/pom.xml", "dummy test pom.xml")

	// When ...
	err := createProject(
		mockFileSystem, testPackageName, featureNamesCommandSeparatedList,
		isObrProjectRequired, forceOverwrite, maven, gradle, isDevelopment, false, "")

	// Then...
	// Should have created a folder for the parent package.
	if err != nil {
		assert.Fail(t, err.Error())
	}

	// Check that the pom with decent inputs has overwritten the dummy test pom contents.
	parentPomXmlFileExists, err := mockFileSystem.Exists("my.test.pkg/pom.xml")
	assert.Nil(t, err)
	assert.True(t, parentPomXmlFileExists, "Parent folder pom.xml was not created.")

	text, err := mockFileSystem.ReadTextFile("my.test.pkg/pom.xml")
	assert.Nil(t, err)
	assert.True(t, strings.HasPrefix(text, "<?xml"), "pom template is expanding as HTML by accident!")
	assert.Contains(t, text, "<groupId>my.test.pkg</groupId>", "parent pom.xml didn't substitute the group id")
	assert.Contains(t, text, "<artifactId>my.test.pkg</artifactId>", "parent pom.xml didn't substitute the artifact id")

}

func TestCanCreateProjectGoldenPathWithOBR(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	forceOverwrite := true
	isObrProjectRequired := true
	featureNamesCommandSeparatedList := "test"
	maven := true
	gradle := false
	isDevelopment := false
	packageName := "my.test.pkg"

	// When ...
	err := createProject(
		mockFileSystem, packageName, featureNamesCommandSeparatedList,
		isObrProjectRequired, forceOverwrite, maven, gradle, isDevelopment, false, "")

	// Then...
	// Should have created a folder for the parent package.
	if err != nil {
		assert.Fail(t, err.Error())
	}

	assertParentFolderAndContentsCreated(t, mockFileSystem, isObrProjectRequired, maven, gradle, packageName)
	assertTestFolderAndContentsCreatedOk(t, mockFileSystem, "test", maven, gradle)
	assertOBRFolderAndContentsCreatedOK(t, mockFileSystem, maven, gradle)
}

func assertOBRFolderAndContentsCreatedOK(t *testing.T, mockFileSystem spi.FileSystem, isMaven bool, isGradle bool) {
	testFolderExists, err := mockFileSystem.DirExists("my.test.pkg/my.test.pkg.obr")
	assert.Nil(t, err)
	assert.True(t, testFolderExists, "Test folder was not created.")

	if isMaven {
		expectedPomFilePath := "my.test.pkg/my.test.pkg.obr/pom.xml"
		testPomXmlFileExists, err := mockFileSystem.Exists(expectedPomFilePath)
		assert.Nil(t, err)
		assert.True(t, testPomXmlFileExists, "Test folder pom.xml was not created.")

		text, err := mockFileSystem.ReadTextFile(expectedPomFilePath)
		assert.Nil(t, err)
		assert.Contains(t, text, "<groupId>my.test.pkg</groupId>", "Test folder pom.xml didn't substitute the group id")
		assert.Contains(t, text, "<artifactId>my.test.pkg.obr</artifactId>", "Test folder pom.xml didn't substitute the artifact id")
	}

	if isGradle {
		expectedBuildGradleFilePath := "my.test.pkg/my.test.pkg.obr/build.gradle"
		testBuildGradleFileExists, err := mockFileSystem.Exists(expectedBuildGradleFilePath)
		assert.Nil(t, err)
		assert.True(t, testBuildGradleFileExists, "Test folder build.gradle was not created.")

		text, err := mockFileSystem.ReadTextFile(expectedBuildGradleFilePath)
		assert.Nil(t, err)
		assert.Contains(t, text, "group = 'my.test.pkg'", "Test folder build.gradle didn't substitute the group id")

		var line string
		line, err = findLineContaining(text, "dev.galasa.obr")
		assert.Nil(t, err)

		pattern := ".*version '(.*)'$"
		galasaObrLineRegex, err := regexp.Compile(pattern)
		assert.Nil(t, err)

		match := galasaObrLineRegex.FindStringSubmatch(line)
		assert.NotNil(t, match, "No line declaring dev.galasa.obr line version")
		assert.Equal(t, len(match), 2, "Failed to find more that 0 matches")
		assert.NotEmpty(t, match[1])

	}
}

func findLineContaining(text string, textToFindOnLine string) (string, error) {
	textLines := strings.Split(text, "\n")
	lineMatching := ""
	isMatched := false
	var err error
	for _, line := range textLines {
		if strings.Contains(line, textToFindOnLine) {
			lineMatching = line
			isMatched = true
		}
	}

	if !isMatched {
		err = errors.New("Failed to find string " + textToFindOnLine)
	}
	return lineMatching, err
}

func TestCreateProjectWithTwoFeaturesWorks(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	forceOverwrite := false
	isObrProjectRequired := false
	testPackageName := "my.test.pkg"
	featureNamesCommandSeparatedList := "account,payee"
	maven := true
	gradle := false
	isDevelopment := false

	// When ...
	err := createProject(
		mockFileSystem, testPackageName,
		featureNamesCommandSeparatedList, isObrProjectRequired,
		forceOverwrite, maven, gradle, isDevelopment, false, "")

	// Then...
	// Should have created a folder for the parent package.
	if err != nil {
		assert.Fail(t, "err should not have been set. "+err.Error())
	}

	isAccountModuleExists, _ := mockFileSystem.DirExists(testPackageName + "/" + testPackageName + ".account")
	assert.True(t, isAccountModuleExists, "account feature module does not exist.")

	isPayeeModuleExists, _ := mockFileSystem.DirExists(testPackageName + "/" + testPackageName + ".payee")
	assert.True(t, isPayeeModuleExists, "payee feature module does not exist.")
}

func TestCreateProjectWithInvalidFeaturesFails(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	forceOverwrite := false
	isObrProjectRequired := false
	testPackageName := "my.test.pkg"
	featureNamesCommandSeparatedList := "Account"
	maven := true
	gradle := false
	isDevelopment := false

	// When ...
	err := createProject(mockFileSystem, testPackageName,
		featureNamesCommandSeparatedList, isObrProjectRequired,
		forceOverwrite, maven, gradle, isDevelopment, false, "")

	// Then...
	// Should have created a folder for the parent package.
	assert.NotNil(t, err, "err should have been set!")
	assert.Contains(t, err.Error(), "GAL1045E")

}

func TestCanCreateGradleProjectWithNoOBR(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	forceOverwrite := true
	isObrProjectRequired := false
	featureNamesCommandSeparatedList := "test"
	maven := false
	gradle := true
	isDevelopment := false
	packageName := "my.test.pkg"

	// When ...
	err := createProject(mockFileSystem, packageName, featureNamesCommandSeparatedList,
		isObrProjectRequired, forceOverwrite, maven, gradle, isDevelopment, false, "")

	// Then...
	// Should have created a folder for the parent package.
	if err != nil {
		assert.Fail(t, "Creating a Maven project should not return an error. %s", err.Error())
	}

	assertParentFolderAndContentsCreated(t, mockFileSystem, isObrProjectRequired, maven, gradle, packageName)
	assertTestFolderAndContentsCreatedOk(t, mockFileSystem, "test", maven, gradle)
}

func TestCanCreateGradleProjectWithOBR(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	forceOverwrite := true
	isObrProjectRequired := true
	featureNamesCommandSeparatedList := "test"
	maven := false
	gradle := true
	isDevelopment := false
	packageName := "my.test.pkg"

	// When ...
	err := createProject(
		mockFileSystem, packageName, featureNamesCommandSeparatedList,
		isObrProjectRequired, forceOverwrite, maven, gradle, isDevelopment, false, "")

	// Then...
	// Should have created a folder for the parent package.
	if err != nil {
		assert.Fail(t, err.Error())
	}

	assertParentFolderAndContentsCreated(t, mockFileSystem, isObrProjectRequired, maven, gradle, packageName)
	assertTestFolderAndContentsCreatedOk(t, mockFileSystem, "test", maven, gradle)
	assertOBRFolderAndContentsCreatedOK(t, mockFileSystem, maven, gradle)
}

func TestCanCreateMavenAndGradleProject(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	forceOverwrite := true
	isObrProjectRequired := false
	featureNamesCommandSeparatedList := "test"
	maven := true
	gradle := true
	isDevelopment := false
	packageName := "my.test.pkg"

	// When ...
	err := createProject(
		mockFileSystem, packageName, featureNamesCommandSeparatedList,
		isObrProjectRequired, forceOverwrite, maven, gradle, isDevelopment, false, "")

	// Then...
	// Should have created a folder for the parent package.
	if err != nil {
		assert.Fail(t, err.Error())
	}

	assertParentFolderAndContentsCreated(t, mockFileSystem, isObrProjectRequired, maven, gradle, packageName)
	assertTestFolderAndContentsCreatedOk(t, mockFileSystem, "test", maven, gradle)
}

func TestCreateProjectInsistsOnGradleAndOrMaven(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	forceOverwrite := true
	isObrProjectRequired := false
	featureNamesCommandSeparatedList := "test"
	maven := false
	gradle := false
	isDevelopment := false

	err := createProject(
		mockFileSystem, "my.test.pkg", featureNamesCommandSeparatedList,
		isObrProjectRequired, forceOverwrite, maven, gradle, isDevelopment, false, "")

	// Then...
	// Should throw an error asking for flags to be set
	assert.NotNil(t, err, "err should have been set!")
	assert.Contains(t, err.Error(), "GAL1089E")
}

func TestCanCreateGradleProjectNonDevelopmentModeGeneratesCommentedOutMavenRepoReference(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	forceOverwrite := true
	isObrProjectRequired := true
	featureNamesCommandSeparatedList := "test"
	maven := false
	gradle := true
	isDevelopment := false

	// When ...
	err := createProject(
		mockFileSystem, "my.test.pkg", featureNamesCommandSeparatedList,
		isObrProjectRequired, forceOverwrite, maven, gradle, isDevelopment, false, "")

	// Then...
	// Should have created a folder for the parent package.
	if err != nil {
		assert.Fail(t, err.Error())
	}

	settingsGradleText, err := mockFileSystem.ReadTextFile("my.test.pkg/settings.gradle")
	assert.Nil(t, err)
	assert.Contains(t, settingsGradleText, "//    url 'https://development.galasa.dev/main/maven-repo/obr'", "parent settings.gradle didn't have a commented-out bleeding edge repo ref.")

	buildGradleText, err := mockFileSystem.ReadTextFile("my.test.pkg/my.test.pkg.test/build.gradle")
	assert.Nil(t, err)
	assert.Contains(t, buildGradleText, "//    url 'https://development.galasa.dev/main/maven-repo/obr'", "child build.gradle didn't have a commented-out bleeding edge repo ref.")

}

func TestCanCreateGradleProjectDevelopmentModeGeneratesMavenRepoReference(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	forceOverwrite := true
	isObrProjectRequired := true
	featureNamesCommandSeparatedList := "test"
	maven := false
	gradle := true
	isDevelopment := true

	// When ...
	err := createProject(
		mockFileSystem, "my.test.pkg", featureNamesCommandSeparatedList,
		isObrProjectRequired, forceOverwrite, maven, gradle, isDevelopment, false, "")

	// Then...
	// Should have created a folder for the parent package.
	if err != nil {
		assert.Fail(t, err.Error())
	}

	settingsGradleText, err := mockFileSystem.ReadTextFile("my.test.pkg/settings.gradle")
	assert.Nil(t, err)
	assert.Contains(t, settingsGradleText, "           url 'https://development.galasa.dev/main/maven-repo/obr'", "parent settings.gradle didn't have an uncommented bleeding edge repo ref.")

	buildGradleText, err := mockFileSystem.ReadTextFile("my.test.pkg/my.test.pkg.test/build.gradle")
	assert.Nil(t, err)
	assert.Contains(t, buildGradleText, "       url 'https://development.galasa.dev/main/maven-repo/obr'", "child build.gradle didn't have an uncommented bleeding edge repo ref.")
}

func TestCreateProjectUsingCommandLineNoPackageSet(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"project", "create"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.NotNil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("", "Error: required flag(s) \"package\" not set", factory, t)
}

func TestCreateProjectUsingCommandLineNoFeaturesSetWorks(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"project", "create", "--package", "my.pkg", "--maven"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.Nil(t, err)
	fmt.Printf("error returned by Execute method: %v\n", err)

	// Check what the user saw no output
	checkOutput("", "", factory, t)

	// Check that the default folder was created.
	fs := factory.GetFileSystem()
	var isExists bool
	isExists, err = fs.DirExists("my.pkg")
	assert.Nil(t, err)
	assert.True(t, isExists)
}

func TestCreateProjectUsingCommandLineNoMavenNorGradleFails(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	// Note: No --maven or --gradle flags here:
	var args []string = []string{"project", "create", "--package", "my.package"}

	// When...
	err := Execute(factory, args)

	// Then...

	// Check what the user saw is reasonable.
	checkOutput("", "Error: GAL1089E: Need to use --maven and/or --gradle parameter", factory, t)

	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1089E:")
}

func TestCommandsCollectionContainsProjectCreateCommand(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	// When...
	commands, _ := NewCommandCollection(factory)
	projectCreateCommand, err := commands.GetCommand(COMMAND_NAME_PROJECT_CREATE)
	assert.Nil(t, err)

	assert.NotNil(t, projectCreateCommand)
	assert.IsType(t, &ProjectCreateCmdValues{}, projectCreateCommand.Values())
	assert.NotNil(t, projectCreateCommand.CobraCommand())
	assert.Equal(t, COMMAND_NAME_PROJECT_CREATE, projectCreateCommand.Name())
}

func TestProjectCreateHelpFlagSetCorrectly(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()

	var args []string = []string{"project", "create", "--help"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.Nil(t, err)

	// Check what the user saw is reasonable.
	checkOutput("Displays the options for the 'project create' command", "", factory, t)
}

func TestProjectCreateNoFlagReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_PROJECT_CREATE, factory, t)

	var args []string = []string{"project", "create"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "required flag(s) \"package\" not set")

	// Check what the user saw is reasonable.
	checkOutput("", "required flag(s) \"package\" not set", factory, t)
}

func TestProjectCreatePackageFlagReturnsNoError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_PROJECT_CREATE, factory, t)

	var args []string = []string{"project", "create", "--package", "package.name"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*ProjectCreateCmdValues).packageName, "package.name")
}

func TestProjectCreatePackageFlagNoPackageReturnsError(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, _ := setupTestCommandCollection(COMMAND_NAME_PROJECT_CREATE, factory, t)

	var args []string = []string{"project", "create", "--package"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "flag needs an argument: --package")

	checkOutput("", "flag needs an argument: --package", factory, t)
}

func TestProjectCreatePackageAndFeatureFlagsReturnsOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_PROJECT_CREATE, factory, t)

	var args []string = []string{"project", "create", "--package", "package.name", "--features", "comma,seperated,test,list"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*ProjectCreateCmdValues).packageName, "package.name")
	assert.Contains(t, cmd.Values().(*ProjectCreateCmdValues).featureNamesCommaSeparated, "comma,seperated,test,list")
}

func TestProjectCreatePackageAndForceFlagsReturnsNoOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_PROJECT_CREATE, factory, t)

	var args []string = []string{"project", "create", "--package", "package.name", "--force"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*ProjectCreateCmdValues).packageName, "package.name")
	assert.Equal(t, cmd.Values().(*ProjectCreateCmdValues).force, true)
}

func TestProjectCreatePackageAndObrFlagsReturnsNoOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_PROJECT_CREATE, factory, t)

	var args []string = []string{"project", "create", "--package", "package.name", "--obr"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*ProjectCreateCmdValues).packageName, "package.name")
	assert.Equal(t, cmd.Values().(*ProjectCreateCmdValues).isOBRProjectRequired, true)
}

func TestProjectCreatePackageAndMavenFlagsReturnsNoOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_PROJECT_CREATE, factory, t)

	var args []string = []string{"project", "create", "--package", "package.name", "--maven"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*ProjectCreateCmdValues).packageName, "package.name")
	assert.Equal(t, cmd.Values().(*ProjectCreateCmdValues).useMaven, true)
}

func TestProjectCreatePackageAndGradleFlagsReturnsNoOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_PROJECT_CREATE, factory, t)

	var args []string = []string{"project", "create", "--package", "package.name", "--gradle"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*ProjectCreateCmdValues).packageName, "package.name")
	assert.Equal(t, cmd.Values().(*ProjectCreateCmdValues).useGradle, true)
}

func TestProjectCreateAllFlagsReturnsNoOk(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	commandCollection, cmd := setupTestCommandCollection(COMMAND_NAME_PROJECT_CREATE, factory, t)

	var args []string = []string{"project", "create", "--package", "package.name", "--features", "feature,list", "--force", "--obr", "--maven", "--gradle"}

	// When...
	err := commandCollection.Execute(args)

	// Then...
	assert.Nil(t, err)

	checkOutput("", "", factory, t)

	assert.Contains(t, cmd.Values().(*ProjectCreateCmdValues).packageName, "package.name")
	assert.Contains(t, cmd.Values().(*ProjectCreateCmdValues).featureNamesCommaSeparated, "feature,list")
	assert.Equal(t, cmd.Values().(*ProjectCreateCmdValues).force, true)
	assert.Equal(t, cmd.Values().(*ProjectCreateCmdValues).isOBRProjectRequired, true)
	assert.Equal(t, cmd.Values().(*ProjectCreateCmdValues).useMaven, true)
	assert.Equal(t, cmd.Values().(*ProjectCreateCmdValues).useGradle, true)
}

func TestSeparateFeatureNamesSortsTheResults(t *testing.T) {

	features1, err := separateFeatureNamesFromCommaSeparatedList("payee,account")
	assert.Nil(t, err)
	assert.NotNil(t, features1)
	assert.Equal(t, 2, len(features1))
	assert.Equal(t, features1[0], "account")
	assert.Equal(t, features1[1], "payee")

	// When we do the same thing in a different order...
	features2, err2 := separateFeatureNamesFromCommaSeparatedList("account,payee")
	// We should get the same results.
	assert.Nil(t, err2)
	assert.NotNil(t, features2)
	assert.Equal(t, 2, len(features2))
	assert.Equal(t, features2[0], "account")
	assert.Equal(t, features2[1], "payee")
}

// ========================================
// Tests for Manager Project Creation
// ========================================

func TestProjectCreateWithManagerFlagCreatesManagerProject(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	forceOverwrite := true
	isObrProjectRequired := false
	featureNamesCommandSeparatedList := ""
	maven := true
	gradle := false
	isDevelopment := false
	packageName := "dev.galasa.example"
	isManagerProject := true
	managerName := "example"

	// When ...
	err := createProject(
		mockFileSystem, packageName, featureNamesCommandSeparatedList,
		isObrProjectRequired, forceOverwrite, maven, gradle, isDevelopment,
		isManagerProject, managerName)

	// Then...
	if err != nil {
		assert.Fail(t, "Manager project creation should not return an error. %s", err.Error())
	}

	// Assert manager project structure was created
	assertManagerProjectCreated(t, mockFileSystem, packageName, managerName, maven, gradle)
}

func TestProjectCreateWithManagerFlagAndNoManagerNameUsesPackageName(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	packageName := "dev.galasa.example"
	isManagerProject := true
	managerName := "" // Empty manager name should default to last part of package

	// When ...
	err := createProject(
		mockFileSystem, packageName, "", false, true, true, false, false,
		isManagerProject, managerName)

	// Then...
	assert.Nil(t, err)

	// Should use "example" as the manager name (last part of package)
	assertManagerProjectCreated(t, mockFileSystem, packageName, "example", true, false)
}

func TestProjectCreateWithManagerAndOBRCreatesManagerInOBR(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	packageName := "dev.galasa.example"
	isManagerProject := true
	managerName := "example"
	isObrProjectRequired := true

	// When ...
	err := createProject(
		mockFileSystem, packageName, "", isObrProjectRequired, true, true, false, false,
		isManagerProject, managerName)

	// Then...
	assert.Nil(t, err)

	// Check OBR includes manager (OBR is created inside the parent package directory)
	obrPomPath := packageName + "/" + packageName + ".obr/pom.xml"
	obrPomExists, err := mockFileSystem.Exists(obrPomPath)
	assert.Nil(t, err)
	assert.True(t, obrPomExists, "OBR pom.xml should exist at "+obrPomPath)

	obrPomText, err := mockFileSystem.ReadTextFile(obrPomPath)
	assert.Nil(t, err)
	assert.Contains(t, obrPomText, packageName+".manager", "OBR should reference manager bundle")
}

func TestProjectCreateManagerWithInvalidManagerNameFails(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	packageName := "dev.galasa.example"
	isManagerProject := true
	managerName := "Invalid-Manager-Name!" // Invalid characters

	// When ...
	err := createProject(
		mockFileSystem, packageName, "", false, true, true, false, false,
		isManagerProject, managerName)

	// Then...
	assert.NotNil(t, err, "Should return error for invalid manager name")
	assert.Contains(t, err.Error(), "GAL1037E:", "Should return package name validation error")
}

func TestProjectCreateManagerCommandLineWithManagerFlag(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	var args []string = []string{"project", "create", "--package", "dev.galasa.example", "--manager", "--managerName", "example", "--maven"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.Nil(t, err)

	// Check manager project was created
	fs := factory.GetFileSystem()
	managerDirExists, err := fs.DirExists("dev.galasa.example/dev.galasa.example.manager")
	assert.Nil(t, err)
	assert.True(t, managerDirExists, "Manager directory should be created")
}

func TestProjectCreateManagerCommandLineWithoutManagerNameUsesDefault(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	var args []string = []string{"project", "create", "--package", "dev.galasa.example", "--manager", "--maven"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.Nil(t, err)

	// Check manager project was created with default name
	fs := factory.GetFileSystem()
	managerDirExists, err := fs.DirExists("dev.galasa.example/dev.galasa.example.manager")
	assert.Nil(t, err)
	assert.True(t, managerDirExists, "Manager directory should be created with default name")
}

func TestProjectCreateManagerAndTestsTogetherFails(t *testing.T) {
	// Given...
	factory := utils.NewMockFactory()
	// Both --manager and --features should not be allowed together
	var args []string = []string{"project", "create", "--package", "dev.galasa.example", "--manager", "--features", "test1", "--maven"}

	// When...
	err := Execute(factory, args)

	// Then...
	assert.NotNil(t, err, "Should not allow both --manager and --features flags")
	assert.Contains(t, err.Error(), "manager", "Error should mention manager flag")
	assert.Contains(t, err.Error(), "features", "Error should mention features flag")
}

// Helper function to assert manager project structure
func assertManagerProjectCreated(t *testing.T, mockFileSystem spi.FileSystem, packageName string, managerName string, isMaven bool, isGradle bool) {
	managerBundleName := packageName + ".manager"
	managerDir := packageName + "/" + managerBundleName

	// Check manager directory exists
	managerDirExists, err := mockFileSystem.DirExists(managerDir)
	assert.Nil(t, err)
	assert.True(t, managerDirExists, "Manager directory should exist: "+managerDir)

	// Check manager source directory structure
	srcMainJavaDir := managerDir + "/src/main/java/" + strings.ReplaceAll(packageName, ".", "/")
	srcMainJavaDirExists, err := mockFileSystem.DirExists(srcMainJavaDir)
	assert.Nil(t, err)
	assert.True(t, srcMainJavaDirExists, "Manager src/main/java directory should exist")

	// Check key manager files exist
	capitalizedManagerName := capitalizeFirst(managerName)

	// Public API files
	assertJavaFileExists(t, mockFileSystem, srcMainJavaDir+"/"+capitalizedManagerName+"Resource.java", "Manager annotation")
	assertJavaFileExists(t, mockFileSystem, srcMainJavaDir+"/I"+capitalizedManagerName+"Resource.java", "Resource interface")
	assertJavaFileExists(t, mockFileSystem, srcMainJavaDir+"/I"+capitalizedManagerName+"Manager.java", "Manager interface")
	assertJavaFileExists(t, mockFileSystem, srcMainJavaDir+"/"+capitalizedManagerName+"ManagerException.java", "Manager exception")

	// Internal implementation files
	internalDir := srcMainJavaDir + "/internal"
	assertJavaFileExists(t, mockFileSystem, internalDir+"/"+capitalizedManagerName+"ManagerImpl.java", "Manager implementation")
	assertJavaFileExists(t, mockFileSystem, internalDir+"/"+capitalizedManagerName+"ResourceImpl.java", "Resource implementation")
	assertJavaFileExists(t, mockFileSystem, internalDir+"/"+capitalizedManagerName+"ManagerField.java", "Manager field annotation")

	// Check build files
	if isMaven {
		pomExists, err := mockFileSystem.Exists(managerDir + "/pom.xml")
		assert.Nil(t, err)
		assert.True(t, pomExists, "Manager pom.xml should exist")

		pomText, err := mockFileSystem.ReadTextFile(managerDir + "/pom.xml")
		assert.Nil(t, err)
		assert.Contains(t, pomText, "<artifactId>"+managerBundleName+"</artifactId>", "pom.xml should have correct artifact ID")
		assert.Contains(t, pomText, "<groupId>"+packageName+"</groupId>", "pom.xml should have correct group ID")
	}

	if isGradle {
		buildGradleExists, err := mockFileSystem.Exists(managerDir + "/build.gradle")
		assert.Nil(t, err)
		assert.True(t, buildGradleExists, "Manager build.gradle should exist")

		bndExists, err := mockFileSystem.Exists(managerDir + "/bnd.bnd")
		assert.Nil(t, err)
		assert.True(t, bndExists, "Manager bnd.bnd should exist")
	}

	// Check test directory exists (with internal subdirectory)
	srcTestJavaDir := managerDir + "/src/test/java/" + strings.ReplaceAll(packageName, ".", "/") + "/internal"
	srcTestJavaDirExists, err := mockFileSystem.DirExists(srcTestJavaDir)
	assert.Nil(t, err)
	assert.True(t, srcTestJavaDirExists, "Manager test directory should exist")

	// Check unit test file exists
	assertJavaFileExists(t, mockFileSystem, srcTestJavaDir+"/"+capitalizedManagerName+"ManagerImplTest.java", "Manager unit test")
}

func assertJavaFileExists(t *testing.T, mockFileSystem spi.FileSystem, filePath string, description string) {
	fileExists, err := mockFileSystem.Exists(filePath)
	assert.Nil(t, err, "Error checking if "+description+" exists")
	assert.True(t, fileExists, description+" should exist at: "+filePath)

	// Verify it's a valid Java file with package declaration
	content, err := mockFileSystem.ReadTextFile(filePath)
	assert.Nil(t, err, "Error reading "+description)
	assert.Contains(t, content, "package ", description+" should contain package declaration")
	assert.Contains(t, content, "/*", description+" should contain copyright header")
}
