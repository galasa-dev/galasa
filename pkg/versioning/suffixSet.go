/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package versioning

import (
	"errors"
	"path"
	"strings"

	"galasa.dev/buildUtilities/pkg/utils"
)

func SuffixSetExecute(fs utils.FileSystem, sourceCodeFolderPath string, desiredSuffix string) error {
	var err error

	err = validateSuffix(desiredSuffix)

	if err == nil {
		var modules []Module
		modules, err = getModules(fs, sourceCodeFolderPath)
		if err == nil {
			err = setSuffixOnAllModules(fs, modules, desiredSuffix)
		}
	}

	return err
}

func validateSuffix(suffixToValidate string) error {
	var err error

	if strings.HasPrefix(suffixToValidate, "-") || strings.HasPrefix(suffixToValidate, "_") {
		// It's valid.
	} else {
		err = errors.New("Invalid suffix. It must start with a '-' or '_' character, or be blank.")
	}

	return err
}

func setSuffixOnAllModules(fs utils.FileSystem, modules []Module, desiredSuffix string) error {

	var err error
	for _, module := range modules {
		currentVersion := module.GetVersion()
		var desiredVersion string
		desiredVersion = calculateDesiredVersion(currentVersion, desiredSuffix)
		err = substitutedBuildGradleVersion(fs, module, desiredVersion)
	}
	return err
}

func substitutedBuildGradleVersion(fs utils.FileSystem, module Module, desiredVersion string) error {

	buildGradleFilePath := path.Join(module.GetPath(), "build.gradle")
	buildGradleFileContents, err := fs.ReadTextFile(buildGradleFilePath)

	if err == nil {
		matches := versionLineRegex.FindStringSubmatchIndex(buildGradleFileContents)

		// matches[0] is the start of the whole string
		// matches[1] is the end of the whole string
		// matches[2] is the start of the versions part which needs replacing.
		// matches[3] is the end of the versions part which needs replacing.

		startIndex := matches[2]
		endIndex := matches[3]

		beforeMatch := buildGradleFileContents[:startIndex]
		afterMatch := buildGradleFileContents[endIndex:]

		contentAfterSubstiitution := beforeMatch + desiredVersion + afterMatch

		err = fs.WriteTextFile(buildGradleFilePath, contentAfterSubstiitution)
	}

	return err
}

func calculateDesiredVersion(currentVersion string, desiredSuffix string) string {
	var desiredVersion string
	var nonSuffixedVersion string

	// Remove anything after one of the delimeters
	nonSuffixedVersion = removeSuffix(currentVersion, "_")
	nonSuffixedVersion = removeSuffix(nonSuffixedVersion, "-")

	// Now append the desired suffix.
	desiredVersion = nonSuffixedVersion + desiredSuffix

	return desiredVersion
}

func removeSuffix(stringToManipulate string, delimeter string) string {
	var result string
	index := strings.Index(stringToManipulate, delimeter)
	if index != -1 {
		result = stringToManipulate[:index]
	} else {
		result = stringToManipulate
	}
	return result
}
