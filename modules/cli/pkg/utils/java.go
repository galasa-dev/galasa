/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/spi"
)

type JavaClassDef struct {
	PackageName string
	ClassName   string
}

// validateJavaHome validate that JAVA_HOME is set correctly.
// If $JAVA_HOME ends with a '/' (or '\' for windows, the trailing slash
// is removed before checks are made.
//
// Constraints:
// - It must be set.
// - JAVA_HOME/bin must be a folder which exists.
// - JAVA_HOME/bin/java must exist as a file.
func ValidateJavaHome(fileSystem spi.FileSystem, javaHome string) error {

	var err error

	// Check that the javaHome string is well-formed.
	if javaHome == "" {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_JAVA_HOME_NOT_SET)
	}

	if err == nil {
		javaHome = sanitiseJavaHome(fileSystem, javaHome)
	}

	if err == nil {
		err = checkJavaHomeBinFolderExists(fileSystem, javaHome)
	}

	if err == nil {
		err = checkJavaHomeBinJavaProgram(fileSystem, javaHome)
	}

	return err
}

// checkJavaHomeBinJavaProgramExists check to make sure JAVA_HOME/bin/java is a program which
// - exists
func checkJavaHomeBinJavaProgram(fileSystem spi.FileSystem, javaHome string) error {
	var err error

	// Check that the program $JAVA_HOME/bin/java exists
	separator := fileSystem.GetFilePathSeparator()
	javaProgramPath := javaHome + separator + "bin" +
		separator + "java" + fileSystem.GetExecutableExtension()
	var isProgramThere bool
	isProgramThere, err = fileSystem.Exists(javaProgramPath)
	if err != nil {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_JAVA_PROGRAM_PRESENCE_FAIL, javaProgramPath, err.Error())
	} else {
		if !isProgramThere {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_JAVA_PROGRAM_MISSING, javaProgramPath)
		}
	}
	return err
}

// sanitiseJavaHome Massage the javaHome value to make it more valid.
// - strip off trailing path separators
func sanitiseJavaHome(fs spi.FileSystem, initialJavaHome string) string {

	result := initialJavaHome

	// If the JAVA_HOME ends in a slash, strip it off.
	lastCharacter := initialJavaHome[len(initialJavaHome)-1:]
	if lastCharacter == fs.GetFilePathSeparator() {
		// Last character is a file separator. Strip it off.
		result = initialJavaHome[0 : len(initialJavaHome)-1]
	}
	return result
}

// checkJavaHomeBinFolderExists Checks that the $JAVA_HOME/bin folder exists.
func checkJavaHomeBinFolderExists(fileSystem spi.FileSystem, javaHome string) error {
	var err error
	javaBinFolder := javaHome + fileSystem.GetFilePathSeparator() + "bin"
	var isBinFolderThere bool
	isBinFolderThere, err = fileSystem.DirExists(javaBinFolder)
	if err != nil {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_JAVA_HOME_BIN_PRESENCE_FAIL, javaBinFolder, err.Error())
	} else {
		if !isBinFolderThere {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_JAVA_HOME_BIN_MISSING, javaBinFolder)
		}
	}
	return err
}
