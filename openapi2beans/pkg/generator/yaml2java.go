/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package generator

import (
	"log"
	"strings"

	openapi2beans_errors "github.com/dev-galasa/buildutils/openapi2beans/pkg/errors"
	"github.com/galasa-dev/cli/pkg/files"
)

var filepathSeparator = "/"
const JAVA_FILE_EXTENSION_LENGTH = 5

func GenerateFiles(fs files.FileSystem, projectFilePath string, apiFilePath string, packageName string, force bool) error {
	var fatalErr error
	var apiyaml string
	var errList map[string]error
	filepathSeparator = fs.GetFilePathSeparator()

	apiyaml, fatalErr = fs.ReadTextFile(apiFilePath)
	if fatalErr == nil {
		var schemaTypes map[string]*SchemaType
		schemaTypes, errList, fatalErr = getSchemaTypesFromYaml([]byte(apiyaml))
		if fatalErr == nil {
			if len(errList) > 0 {
				fatalErr = handleErrList(errList)
			}
			if fatalErr == nil {
				storeFilepath := generateStoreFilepath(projectFilePath, packageName)
				fatalErr = generateDirectories(fs, storeFilepath, force)
				if fatalErr == nil {
					javaPackage := translateSchemaTypesToJavaPackage(schemaTypes, packageName)
					convertJavaPackageToJavaFiles(javaPackage, fs, storeFilepath)
				}
			}
		}
	}

	return fatalErr
}

// Cleans or creates the store folder at the storeFilepath
func generateDirectories(fs files.FileSystem, storeFilepath string, force bool) error {
	log.Println("Cleaning generated beans directory: " + storeFilepath)
	exists, err := fs.DirExists(storeFilepath)
	if err == nil {
		if exists {
			var javaFilepaths []string
			javaFilepaths, err = retrieveAllJavaFiles(fs, storeFilepath)
			if err == nil && len(javaFilepaths) > 0 {
				if !force {
					err = openapi2beans_errors.NewError("The tool is unable to create files in folder %s because files in that folder already exist. Generating files is a destructive operation, removing all Java files in that folder prior to new files being created.\nIf you wish to proceed, delete the files manually, or re-run the tool using the --force option", storeFilepath)
				} else {
					deleteAllJavaFiles(fs, javaFilepaths)
				}
			}
		} else {
			log.Printf("Creating output directory: %s\n", storeFilepath)
			err = fs.MkdirAll(storeFilepath)
		}
	}
	return err
}

func handleErrList(errList map[string]error) error {
	log.Println("Failing on non-fatal errors:")
	var err error
	errorString := ""
	for _, individualError := range errList {
		errorString += individualError.Error()
	}
	err = openapi2beans_errors.NewError(errorString)
	return err
}

// Creates the store filepath from the output filepath + the package name seperated out into folders
func generateStoreFilepath(outputFilepath string, packageName string) string {
	packageFilepath := strings.ReplaceAll(packageName, ".", filepathSeparator)
	if outputFilepath[len(outputFilepath)-1:] != filepathSeparator {
		outputFilepath += filepathSeparator
	}
	return outputFilepath + packageFilepath
}

func deleteAllJavaFiles(fs files.FileSystem, javaFilepaths []string) {
	for _, filepath := range javaFilepaths {
		fs.DeleteFile(filepath)
	}
}

func retrieveAllJavaFiles(fs files.FileSystem, storeFilepath string) ([]string, error) {
	var javaFilepaths []string
	filepaths, err := fs.GetAllFilePaths(storeFilepath)
	for _, filepath := range filepaths {
		filename := filepath[len(storeFilepath)+1:]
		if len(filename) - JAVA_FILE_EXTENSION_LENGTH > 0 { // makes sure file name is longer than just the .java extension
			if filename[len(filename) - JAVA_FILE_EXTENSION_LENGTH:] == ".java" && !strings.Contains(filename, filepathSeparator) {
				javaFilepaths = append(javaFilepaths, filepath)
			}
		}
	}
	return javaFilepaths, err
}
