/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"log"
	"sort"
	"strings"

	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/spi"

	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/spf13/cobra"
)

// MavenCoordinates holds common substitution parameters a pom.xml file
// template uses.
type MavenCoordinates struct {
	GroupId    string
	ArtifactId string
	Name       string
}

// GradleCoordinates holds common substitution parameters .gradle file
// templates use.
type GradleCoordinates struct {
	GroupId string
	Name    string
}

type ProjectCreateCmdValues struct {
	packageName                string
	force                      bool
	isOBRProjectRequired       bool
	featureNamesCommaSeparated string
	useMaven                   bool
	useGradle                  bool
	isDevelopmentProjectCreate bool
}

type ProjectCreateCommand struct {
	cobraCommand *cobra.Command
	values       *ProjectCreateCmdValues
}

// ------------------------------------------------------------------------------------------------
// Constructors
// ------------------------------------------------------------------------------------------------
func NewProjectCreateCmd(factory spi.Factory, projectCmd spi.GalasaCommand, rootCmd spi.GalasaCommand) (spi.GalasaCommand, error) {
	var err error

	cmd := new(ProjectCreateCommand)
	err = cmd.init(factory, projectCmd, rootCmd)

	return cmd, err
}

// ------------------------------------------------------------------------------------------------
// Public methods
// ------------------------------------------------------------------------------------------------
func (cmd *ProjectCreateCommand) Name() string {
	return COMMAND_NAME_PROJECT_CREATE
}

func (cmd *ProjectCreateCommand) CobraCommand() *cobra.Command {
	return cmd.cobraCommand
}

func (cmd *ProjectCreateCommand) Values() interface{} {
	return cmd.values
}

// ------------------------------------------------------------------------------------------------
// Private methods
// ------------------------------------------------------------------------------------------------

func (cmd *ProjectCreateCommand) init(factory spi.Factory, projectCmd spi.GalasaCommand, rootCmd spi.GalasaCommand) error {
	var err error

	cmd.values = &ProjectCreateCmdValues{}
	cmd.cobraCommand, err = cmd.createCobraCommand(factory, projectCmd, rootCmd)

	return err
}

func (cmd *ProjectCreateCommand) createCobraCommand(
	factory spi.Factory,
	projectCmd spi.GalasaCommand,
	rootCmd spi.GalasaCommand,
) (*cobra.Command, error) {

	var err error

	projectCreateCmd := &cobra.Command{
		Use:     "create",
		Short:   "Creates a new Galasa project",
		Long:    "Creates a new Galasa test project with optional OBR project and build process files",
		Args:    cobra.NoArgs,
		Aliases: []string{"project create"},
		RunE: func(cobraCmd *cobra.Command, args []string) error {
			return cmd.executeCreateProject(factory, rootCmd.Values().(*RootCmdValues))
		},
	}

	projectCreateCmd.Flags().StringVar(&cmd.values.packageName, "package", "", "Java package name for tests we create. "+
		"Forms part of the project name, maven/gradle group/artifact ID, "+
		"and OSGi bundle name. It may reflect the name of your organisation or company, "+
		"the department, function or application under test. "+
		"For example: dev.galasa.banking.example")
	projectCreateCmd.MarkFlagRequired("package")

	projectCreateCmd.Flags().BoolVar(&cmd.values.isDevelopmentProjectCreate, "development", false, "Use bleeding-edge galasa versions and repositories.")

	projectCreateCmd.Flags().BoolVar(&cmd.values.force, "force", false, "Force-overwrite files which already exist.")
	projectCreateCmd.Flags().BoolVar(&cmd.values.isOBRProjectRequired, "obr", false, "An OSGi Object Bundle Resource (OBR) project is needed.")
	projectCreateCmd.Flags().StringVar(&cmd.values.featureNamesCommaSeparated, "features", "feature1",
		"A comma-separated list of features you are testing. "+
			"These must be able to form parts of a java package name. "+
			"For example: \"payee,account\"")

	projectCreateCmd.Flags().BoolVar(&cmd.values.useMaven, "maven", false, "Generate maven build artifacts. "+
		"Can be used in addition to the --gradle flag. "+
		"If this flag is not used, and the gradle option is not used, then behaviour of this flag defaults to true.")
	projectCreateCmd.Flags().BoolVar(&cmd.values.useGradle, "gradle", false, "Generate gradle build artifacts. "+
		"Can be used in addition to the --maven flag.")

	projectCmd.CobraCommand().AddCommand(projectCreateCmd)

	return projectCreateCmd, err
}

func (cmd *ProjectCreateCommand) executeCreateProject(factory spi.Factory, rootCmdValues *RootCmdValues) error {

	var err error

	// Operations on the file system will all be relative to the current folder.
	fileSystem := factory.GetFileSystem()

	err = utils.CaptureLog(fileSystem, rootCmdValues.logFileName)
	if err == nil {

		rootCmdValues.isCapturingLogs = true

		log.Println("Galasa CLI - Create project")

		err = createProject(fileSystem,
			cmd.values.packageName,
			cmd.values.featureNamesCommaSeparated,
			cmd.values.isOBRProjectRequired,
			cmd.values.force,
			cmd.values.useMaven,
			cmd.values.useGradle,
			cmd.values.isDevelopmentProjectCreate,
		)
	}
	return err
}

// createProject will create the following artifacts in the specified file system:
//
// Maven Build
//
//		. - All files are relative to the current directory.
//		└── packageName - The parent package
//			├── pom.xml - The parent pom.
//	 		├── packageName.test - The tests project.
//	  		│   └── pom.xml
//	  		│   └── src
//	  		│       └── main
//	  		│           └── java
//	  		│               └── packageName - There will be multiple nested folders if there are dots ('.') in the package name
//	  		│                   └── SampleTest.java - Contains an example Galasa testcase
//	  		└── packageName.obr - The OBR project. (only if the --obr option is used).
//	    	 	 └── pom.xml
//
// Gradle Build
//
//		. - All files are relative to the current directory.
//		└── packageName - The parent package
//			├── settings.gradle - Gradle settings file
//	 		├── packageName.test - The tests project.
//	  		│   └── build.gradle
//	  		│   └── bnd.bnd - the test project's OSGi bundle file
//	  		│   └── src
//	  		│       └── main
//	  		│           └── java
//	  		│               └── packageName - There will be multiple nested folders if there are dots ('.') in the package name
//	  		│                   └── SampleTest.java - Contains an example Galasa testcase
//	  		└── packageName.obr - The OBR project. (only if the --obr option is used).
//	    	 	 └── build.gradle
//
// isOBRProjectRequired - Controls whether the optional OBR project is going to be created.
// featureNamesCommaSeparated - eg: kettle,toaster. Causes a kettle and toaster project to be created with a sample test in.
// isDevelopment - if true, the user wants to use bleeding-edge versions of galasa code and maven repositories.
func createProject(
	fileSystem spi.FileSystem,
	packageName string,
	featureNamesCommaSeparated string,
	isOBRProjectRequired bool,
	forceOverwrite bool,
	useMaven bool,
	useGradle bool,
	isDevelopment bool,
) error {

	log.Printf("Creating project using packageName:%s\n", packageName)

	var err error

	// By default, create a Maven project unless --gradle is the only flag set
	if !useGradle && !useMaven {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_MAVEN_AND_OR_GRADLE_FLAG_MUST_BE_SET)
	} else {

		embeddedFileSystem := embedded.GetReadOnlyFileSystem()

		fileGenerator := utils.NewFileGenerator(fileSystem, embeddedFileSystem)

		// Separate out the feature names from a string into a slice of strings.
		var featureNames []string
		featureNames, err = separateFeatureNamesFromCommaSeparatedList(featureNamesCommaSeparated)

		if err == nil {
			err = utils.ValidateJavaPackageName(packageName)
			if err == nil {

				// Create the parent folder
				parentProjectFolder := packageName
				err = fileGenerator.CreateFolder(parentProjectFolder)

				if err == nil {
					err = createParentFolderContents(
						fileGenerator, packageName, featureNames, isOBRProjectRequired,
						forceOverwrite, useMaven, useGradle, isDevelopment)
				}

				if err == nil {
					err = createTestProjects(fileGenerator, packageName, featureNames, forceOverwrite,
						useMaven, useGradle, isDevelopment)
					if err == nil {
						if isOBRProjectRequired {
							err = createOBRProject(fileGenerator, packageName, featureNames,
								forceOverwrite, useMaven, useGradle)
						}
					}
				}
			}
		}
	}

	return err
}

func createParentFolderContents(
	fileGenerator *utils.FileGenerator,
	packageName string,
	featureNames []string,
	isOBRProjectRequired bool,
	forceOverwrite bool,
	useMaven bool,
	useGradle bool,
	isDevelopment bool,
) error {
	var err error

	if useMaven {
		err = createParentFolderPom(fileGenerator, packageName, featureNames,
			isOBRProjectRequired, forceOverwrite, isDevelopment)
	}

	if err == nil {
		if useGradle {
			err = createParentFolderSettingsGradle(fileGenerator, packageName,
				featureNames, isOBRProjectRequired, forceOverwrite, isDevelopment)
		}
	}

	if err == nil {
		err = createGitIgnoreFile(packageName, fileGenerator, forceOverwrite, useMaven, useGradle)
	}

	return err
}

func createGitIgnoreFile(
	packageName string,
	fileGenerator *utils.FileGenerator,
	forceOverwrite bool,
	useMaven bool,
	useGradle bool,
) error {

	type GitIgnoreParameters struct {
		IsMavenUsed  bool
		IsGradleUsed bool
	}

	templateParameters := GitIgnoreParameters{
		IsMavenUsed:  useMaven,
		IsGradleUsed: useGradle,
	}

	targetFile := utils.GeneratedFileDef{
		FileType:                 ".gitignore",
		TargetFilePath:           packageName + "/.gitignore",
		EmbeddedTemplateFilePath: "templates/projectCreate/parent-project/git-ignore.template",
		TemplateParameters:       templateParameters,
	}

	err := fileGenerator.CreateFile(targetFile, forceOverwrite, true)
	return err
}

func separateFeatureNamesFromCommaSeparatedList(featureNamesCommaSeparated string) ([]string, error) {
	featureNames := strings.Split(featureNamesCommaSeparated, ",")

	var err error

	// Validate each feature name can form part of a package...
	// meaning it should be a valid package name in it's own right.
	for _, featureName := range featureNames {
		err = utils.ValidateJavaPackageName(featureName)
		if err != nil {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_FEATURE_NAME, featureName, err.Error())
			featureNames = nil
			break
		}
	}

	sort.Strings(featureNames)

	return featureNames, err
}

// Creates a pom.xml file in the parent project folder for Maven projects.
func createParentFolderPom(
	fileGenerator *utils.FileGenerator,
	packageName string,
	featureNames []string,
	isOBRRequired bool,
	forceOverwrite bool,
	IsDevelopment bool,
) error {

	type ParentPomParameters struct {
		Coordinates MavenCoordinates

		// Version of Galasa we are targetting
		GalasaVersion string

		IsOBRRequired    bool
		ObrName          string
		ChildModuleNames []string
		IsDevelopment    bool
	}

	galasaVersion, err := embedded.GetGalasaVersion()
	if err == nil {

		templateParameters := ParentPomParameters{
			Coordinates:      MavenCoordinates{ArtifactId: packageName, GroupId: packageName, Name: packageName},
			GalasaVersion:    galasaVersion,
			IsOBRRequired:    isOBRRequired,
			ObrName:          packageName + ".obr",
			ChildModuleNames: make([]string, len(featureNames)),
			IsDevelopment:    IsDevelopment,
		}
		// Populate the child module names
		for index, featureName := range featureNames {
			templateParameters.ChildModuleNames[index] = packageName + "." + featureName
		}

		targetFile := utils.GeneratedFileDef{
			FileType:                 "pom",
			TargetFilePath:           packageName + "/pom.xml",
			EmbeddedTemplateFilePath: "templates/projectCreate/parent-project/pom.xml",
			TemplateParameters:       templateParameters}

		err = fileGenerator.CreateFile(targetFile, forceOverwrite, true)
	}
	return err
}

// Creates the settings.gradle file in the parent project folder for Gradle projects.
func createParentFolderSettingsGradle(
	fileGenerator *utils.FileGenerator,
	packageName string,
	featureNames []string,
	isOBRRequired bool,
	forceOverwrite bool,
	isDevelopment bool,
) error {

	type ParentGradleParameters struct {
		Coordinates GradleCoordinates

		IsOBRRequired    bool
		ObrName          string
		ChildModuleNames []string
		IsDevelopment    bool
	}

	templateParameters := ParentGradleParameters{
		Coordinates:      GradleCoordinates{GroupId: packageName, Name: packageName},
		IsOBRRequired:    isOBRRequired,
		ObrName:          packageName + ".obr",
		ChildModuleNames: make([]string, len(featureNames)),
		IsDevelopment:    isDevelopment,
	}

	// Populate the child module names
	for index, featureName := range featureNames {
		templateParameters.ChildModuleNames[index] = packageName + "." + featureName
	}

	targetFile := utils.GeneratedFileDef{
		FileType:                 "gradle",
		TargetFilePath:           packageName + "/settings.gradle",
		EmbeddedTemplateFilePath: "templates/projectCreate/parent-project/settings.gradle.template",
		TemplateParameters:       templateParameters}

	err := fileGenerator.CreateFile(targetFile, forceOverwrite, true)
	return err
}

// createTestProjects - creates a number of projects, each of whichh containing tests which test a feature.
func createTestProjects(
	fileGenerator *utils.FileGenerator,
	packageName string,
	featureNames []string,
	forceOverwrite bool,
	useMaven bool,
	useGradle bool,
	isDevelopment bool,
) error {

	var err error
	for _, featureName := range featureNames {
		err = createTestProject(fileGenerator, packageName, featureName, forceOverwrite, useMaven, useGradle, isDevelopment)
		if err != nil {
			break
		}
	}
	return err
}

// createTestProject - creates a single project to contain tests which test a feature.
func createTestProject(
	fileGenerator *utils.FileGenerator,
	packageName string,
	featureName string,
	forceOverwrite bool,
	useMaven bool,
	useGradle bool,
	isDevelopment bool,
) error {

	targetFolderPath := packageName + "/" + packageName + "." + featureName
	log.Printf("Creating tests project %s\n", targetFolderPath)

	// Create the base test folder
	err := fileGenerator.CreateFolder(targetFolderPath)
	if err == nil {
		if useMaven {
			err = createTestFolderPom(fileGenerator, targetFolderPath, packageName, featureName, forceOverwrite)
		}

		if useGradle {
			err = createTestFolderGradle(fileGenerator, targetFolderPath, packageName, featureName, forceOverwrite, isDevelopment)
		}
	}

	if err == nil {
		err = createJavaSourceFolder(fileGenerator, targetFolderPath, packageName, featureName, forceOverwrite)
	}

	if err == nil {
		err = createTestResourceFolder(fileGenerator, targetFolderPath, forceOverwrite)
	}

	if err == nil {
		log.Printf("Tests project %s created OK.", targetFolderPath)
	}
	return err
}

func createOBRProject(
	fileGenerator *utils.FileGenerator,
	packageName string,
	featureNames []string,
	forceOverwrite bool,
	useMaven bool,
	useGradle bool) error {

	targetFolderPath := packageName + "/" + packageName + ".obr"
	log.Printf("Creating obr project %s\n", targetFolderPath)

	// Create the base test folder
	err := fileGenerator.CreateFolder(targetFolderPath)
	if err == nil {
		if useMaven {
			err = createOBRFolderPom(fileGenerator, targetFolderPath, packageName, featureNames, forceOverwrite)
		}

		if useGradle {
			err = createOBRFolderBuildGradle(fileGenerator, targetFolderPath, packageName, featureNames, forceOverwrite)
		}
	}

	if err == nil {
		log.Printf("OBR project %s created OK.", targetFolderPath)
	}
	return err
}

func createJavaSourceFolder(fileGenerator *utils.FileGenerator, testFolderPath string, packageName string, featureName string, forceOverwrite bool) error {

	// The folder is the package name but with slashes.
	// eg: my.package becomes my/package
	packageNameWithSlashes := strings.Replace(packageName, ".", "/", -1)
	targetSrcFolderPath := testFolderPath + "/src/main/java/" + packageNameWithSlashes + "/" + featureName
	err := fileGenerator.CreateFolder(targetSrcFolderPath)
	if err == nil {
		// Create our first test java source file.
		classNameNoClassSuffix := "Test" + utils.UppercaseFirstLetter(featureName)
		templateBundlePath := "templates/projectCreate/parent-project/test-project/src/main/java/TestSimple.java.template"
		err = createJavaSourceFile(fileGenerator, targetSrcFolderPath, packageName,
			featureName, forceOverwrite, classNameNoClassSuffix, templateBundlePath)

		if err == nil {
			// Create our second test java source file. To show that you can have multiples.
			classNameNoClassSuffix = "Test" + utils.UppercaseFirstLetter(featureName) + "Extended"
			templateBundlePath := "templates/projectCreate/parent-project/test-project/src/main/java/TestExtended.java.template"
			err = createJavaSourceFile(fileGenerator, targetSrcFolderPath, packageName,
				featureName, forceOverwrite, classNameNoClassSuffix, templateBundlePath)
		}
	}
	return err
}

func createTestResourceFolder(
	fileGenerator *utils.FileGenerator, targetSrcFolderPath string,
	forceOverwrite bool) error {

	var err error

	// Create the folder for the resources to sit in.
	targetResourceFolderPath := targetSrcFolderPath + "/src/main/resources/textfiles"
	err = fileGenerator.CreateFolder(targetResourceFolderPath)
	if err == nil {

		targetFilePath := targetResourceFolderPath + "/sampleText.txt"
		templateBundlePath := "templates/projectCreate/parent-project/test-project/src/main/resources/textfiles/sampleText.txt"

		targetFile := utils.GeneratedFileDef{
			FileType:                 "TextFile",
			TargetFilePath:           targetFilePath,
			EmbeddedTemplateFilePath: templateBundlePath,
			TemplateParameters:       nil}

		err = fileGenerator.CreateFile(targetFile, forceOverwrite, true)
	}
	return err
}

func createJavaSourceFile(fileGenerator *utils.FileGenerator, targetSrcFolderPath string,
	packageName string, featureName string, forceOverwrite bool,
	classNameNoClassSuffix string, templateBundlePath string) error {

	// JavaTestTemplateSubstitutionParameters holds all the substitution parameters a java test file
	// template uses
	type JavaTestTemplateSubstitutionParameters struct {
		Package   string
		ClassName string
	}

	templateParameters := JavaTestTemplateSubstitutionParameters{
		Package:   packageName + "." + featureName,
		ClassName: classNameNoClassSuffix}

	targetFile := utils.GeneratedFileDef{
		FileType:                 "JavaSourceFile",
		TargetFilePath:           targetSrcFolderPath + "/" + classNameNoClassSuffix + ".java",
		EmbeddedTemplateFilePath: templateBundlePath,
		TemplateParameters:       templateParameters}

	err := fileGenerator.CreateFile(targetFile, forceOverwrite, true)
	return err
}

// Creates a pom.xml file in a Maven test project directory.
func createTestFolderPom(fileGenerator *utils.FileGenerator, targetTestFolderPath string,
	packageName string, featureName string, forceOverwrite bool) error {

	type TestPomParameters struct {
		Parent      MavenCoordinates
		Coordinates MavenCoordinates
		FeatureName string
	}

	pomTemplateParameters := TestPomParameters{
		Parent:      MavenCoordinates{GroupId: packageName, ArtifactId: packageName, Name: packageName},
		Coordinates: MavenCoordinates{GroupId: packageName, ArtifactId: packageName + "." + featureName, Name: packageName + "." + featureName},
		FeatureName: featureName}

	targetFile := utils.GeneratedFileDef{
		FileType:                 "pom",
		TargetFilePath:           targetTestFolderPath + "/pom.xml",
		EmbeddedTemplateFilePath: "templates/projectCreate/parent-project/test-project/pom.xml",
		TemplateParameters:       pomTemplateParameters}

	err := fileGenerator.CreateFile(targetFile, forceOverwrite, true)
	return err
}

// Creates a build.gradle and a bnd.bnd file in a Gradle test project directory.
func createTestFolderGradle(fileGenerator *utils.FileGenerator, targetTestFolderPath string,
	packageName string, featureName string, forceOverwrite bool, isDevelopment bool) error {

	type TestGradleParameters struct {
		Parent      GradleCoordinates
		Coordinates GradleCoordinates
		// Version of Galasa we are targetting
		GalasaVersion string
		IsDevelopment bool
	}

	galasaVersion, err := embedded.GetGalasaVersion()

	if err == nil {
		gradleProjectTemplateParameters := TestGradleParameters{
			Parent:        GradleCoordinates{GroupId: packageName, Name: packageName},
			Coordinates:   GradleCoordinates{GroupId: packageName, Name: packageName + "." + featureName},
			GalasaVersion: galasaVersion,
			IsDevelopment: isDevelopment}

		buildGradleFile := utils.GeneratedFileDef{
			FileType:                 "gradle",
			TargetFilePath:           targetTestFolderPath + "/build.gradle",
			EmbeddedTemplateFilePath: "templates/projectCreate/parent-project/test-project/build.gradle.template",
			TemplateParameters:       gradleProjectTemplateParameters}

		err = fileGenerator.CreateFile(buildGradleFile, forceOverwrite, true)

		if err == nil {
			bndFile := utils.GeneratedFileDef{
				FileType:                 "bnd",
				TargetFilePath:           targetTestFolderPath + "/bnd.bnd",
				EmbeddedTemplateFilePath: "templates/projectCreate/parent-project/test-project/bnd.bnd",
				TemplateParameters:       gradleProjectTemplateParameters}
			err = fileGenerator.CreateFile(bndFile, forceOverwrite, true)
		}
	}

	return err
}

// Creates a pom.xml file in an OBR project directory.
func createOBRFolderPom(fileGenerator *utils.FileGenerator, targetOBRFolderPath string, packageName string,
	featureNames []string, forceOverwrite bool) error {

	type OBRPomParameters struct {
		Parent      MavenCoordinates
		Coordinates MavenCoordinates
		Modules     []MavenCoordinates
	}

	// Fill-in all the parameters the template needs.
	pomTemplateParameters := OBRPomParameters{
		Parent:      MavenCoordinates{GroupId: packageName, ArtifactId: packageName, Name: packageName},
		Coordinates: MavenCoordinates{GroupId: packageName, ArtifactId: packageName + ".obr", Name: packageName + ".obr"},
		Modules:     make([]MavenCoordinates, len(featureNames))}
	// Populate the list of modules.
	for index, featureName := range featureNames {
		pomTemplateParameters.Modules[index] = MavenCoordinates{
			GroupId: packageName, ArtifactId: packageName + "." + featureName, Name: packageName + "." + featureName}
	}

	targetFile := utils.GeneratedFileDef{
		FileType:                 "pom",
		TargetFilePath:           targetOBRFolderPath + "/pom.xml",
		EmbeddedTemplateFilePath: "templates/projectCreate/parent-project/obr-project/pom.xml",
		TemplateParameters:       pomTemplateParameters}

	err := fileGenerator.CreateFile(targetFile, forceOverwrite, true)
	return err
}

// Creates a build.gradle file in an OBR project directory.
func createOBRFolderBuildGradle(fileGenerator *utils.FileGenerator, targetOBRFolderPath string, packageName string,
	featureNames []string, forceOverwrite bool) error {

	type OBRGradleParameters struct {
		Parent        GradleCoordinates
		Coordinates   GradleCoordinates
		Modules       []GradleCoordinates
		GalasaVersion string
	}

	buildGradleTemplateParameters := OBRGradleParameters{
		Parent:      GradleCoordinates{GroupId: packageName, Name: packageName},
		Coordinates: GradleCoordinates{GroupId: packageName, Name: packageName + ".obr"},
		Modules:     make([]GradleCoordinates, len(featureNames))}

	// Populate the list of modules.
	for index, featureName := range featureNames {
		buildGradleTemplateParameters.Modules[index] = GradleCoordinates{
			GroupId: packageName, Name: packageName + "." + featureName}
	}

	var galasaVersion string
	var err error
	galasaVersion, err = embedded.GetGalasaVersion()
	if err == nil {
		buildGradleTemplateParameters.GalasaVersion = galasaVersion

		targetFile := utils.GeneratedFileDef{
			FileType:                 "gradle",
			TargetFilePath:           targetOBRFolderPath + "/build.gradle",
			EmbeddedTemplateFilePath: "templates/projectCreate/parent-project/obr-project/build.gradle.template",
			TemplateParameters:       buildGradleTemplateParameters}

		err = fileGenerator.CreateFile(targetFile, forceOverwrite, true)
	}
	return err
}
