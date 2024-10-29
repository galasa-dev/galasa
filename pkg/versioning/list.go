/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package versioning

import (
	"errors"
	"fmt"
	"log"
	"os"
	"path"
	"regexp"
	"sort"
	"strings"

	"galasa.dev/buildUtilities/pkg/utils"
)

// To match the `version = "x.y.z"' pattern
// Note: (?m) enables multi-line matching.
var versionLineRegex = regexp.MustCompile(`(?m)^[ \t]*version[ \t]*[=]?[\t ]*["'](.*)['"].*$`)
var projectNameRegex = regexp.MustCompile(`(?m)^rootProject.name[\t ]*=[\t ]*["'](.*)["'].*$`)

func ListExecute(fs utils.FileSystem, sourceCodeFolderPath string) error {

	modules, err := getModules(fs, sourceCodeFolderPath)

	if err == nil {
		err = printModuleListing(modules)
	}

	return err
}

func getModules(fs utils.FileSystem, sourceCodeFolderPath string) ([]Module, error) {
	var modules []Module
	var err error

	err = checkFolderExists(fs, sourceCodeFolderPath)

	if err == nil {
		// Get all the folders under the source code folder recursively which have a build.gradle file inside.
		var buildGradleFolderPaths []string
		buildGradleFolderPaths, err = gatherEligibleBuildGradleFolders(fs, sourceCodeFolderPath)

		if err == nil {
			modules, err = extractModulesFromBuildGradleFolders(fs, buildGradleFolderPaths)
		}
	}

	return modules, err
}

func checkFolderExists(fs utils.FileSystem, sourceCodeFolderPath string) error {
	// Get the metadata about the folder path we are being pointed at.
	isExisting, err := fs.DirExists(sourceCodeFolderPath)
	if err == nil {
		if !isExisting {
			err = errors.New("sourceCodeFolderPath is not a folder which exists!")
		}
	}
	return err
}

func printModuleListing(modules []Module) error {
	for _, module := range modules {
		fmt.Fprintf(os.Stdout, "%s %s\n", module.GetProjectName(), module.GetVersion())
	}
	return nil
}

func extractModulesFromBuildGradleFolders(fs utils.FileSystem, buildGradleFolderPaths []string) ([]Module, error) {
	var err error
	var modules []Module = make([]Module, 0)

	for _, buildGradleFolderPath := range buildGradleFolderPaths {
		var module Module

		module, err = extractModuleFromBuildGradleFolder(fs, buildGradleFolderPath)
		if err != nil {
			log.Printf("Error extracting the module from build gradle folder. %v", err)
			break
		}

		if module != nil {
			modules = append(modules, module)
		}
	}

	// Sort the results by project name.
	sort.Slice(modules, func(i, j int) bool {
		return modules[i].GetProjectName() < modules[j].GetProjectName()
	})

	return modules, err
}

func extractModuleFromBuildGradleFolder(fs utils.FileSystem, buildGradleFolderPath string) (Module, error) {
	var module Module

	buildGradleFilePath := path.Join(buildGradleFolderPath, "build.gradle")

	contentsString, err := fs.ReadTextFile(buildGradleFilePath)
	if err == nil {
		matches := versionLineRegex.FindStringSubmatch(contentsString)

		if matches == nil {
			// There was no version in this build.gradle file. Warning ?
			log.Printf("Warning: build.gradle file has no version line so folder %s does not contain a module.\n", buildGradleFolderPath)
		} else {
			// There is a match.
			version := matches[1]

			var projectName string
			projectName, err = extractProjectNameFromGradleSettings(fs, buildGradleFolderPath)

			if err == nil {

				if projectName != "" {
					module = NewModule(projectName, buildGradleFolderPath, version)
				}
			}
		}
	}

	return module, err
}

func extractProjectNameFromGradleSettings(fs utils.FileSystem, folderPath string) (string, error) {

	var projectName string = ""

	gradlePropsFilePath := path.Join(folderPath, "settings.gradle")

	contentsString, err := fs.ReadTextFile(gradlePropsFilePath)
	if err != nil {
		if strings.Contains(err.Error(), "no such file") {
			log.Printf("Warning: settings.gradle file is not present, so folder %v does not contain a module.\n", folderPath)
			err = nil
		}
	} else {
		matches := projectNameRegex.FindStringSubmatch(contentsString)
		if matches == nil {
			// There was no version in this settings.gradle file. Warning ?
			log.Printf("Warning: settings.gradle file has no project name, so folder %v does not contain a module.\n", folderPath)
		} else {
			// There is a match. We know the project name now.
			projectName = matches[1]
		}
	}

	return projectName, err

}

type ModuleImpl struct {
	projectName string
	path        string
	version     string
}

type Module interface {
	GetProjectName() string
	GetPath() string
	GetVersion() string
}

func NewModule(projectName string, path string, version string) Module {
	module := new(ModuleImpl)
	module.projectName = projectName
	module.path = path
	module.version = version
	return module
}

func (module *ModuleImpl) GetProjectName() string {
	return module.projectName
}
func (module *ModuleImpl) GetPath() string {
	return module.path
}
func (module *ModuleImpl) GetVersion() string {
	return module.version
}

func gatherEligibleBuildGradleFolders(fs utils.FileSystem, sourceCodeFolderPath string) ([]string, error) {
	var buildFolders []string = make([]string, 0)

	filePaths, err := fs.GetAllFilePaths(sourceCodeFolderPath)
	for _, filePath := range filePaths {
		dirPart, filePart := path.Split(filePath)
		if "build.gradle" == filePart {

			buildFolders = append(buildFolders, dirPart)
		}
	}

	if err != nil {
		log.Printf("impossible to walk directories: %s", err)
	} else {
		sort.Strings(filePaths)
	}

	return buildFolders, err
}
