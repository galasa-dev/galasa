/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package embedded

import (
	"embed"
	"log"

	"github.com/galasa-dev/cli/pkg/props"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
)

const (
	PROPERTY_NAME_GALASACTL_VERSION          = "galasactl.version"
	PROPERTY_NAME_GALASA_BOOT_JAR_VERSION    = "galasa.boot.jar.version"
	PROPERTY_NAME_GALASA_FRAMEWORK_VERSION   = "galasa.framework.version"
	PROPERTY_NAME_GALASACTL_REST_API_VERSION = "galasactl.rest.api.version"
)

// Embed all the template files into the go executable, so there are no extra files
// we need to ship/install/locate on the target machine.
// We can access the "embedded" file system as if they are normal files.
//
//go:embed templates/*
//go:embed fonts/*
var embeddedFileSystem embed.FS

// An instance of the ReadOnlyFileSystem interface, set once, used many times.
// It just delegates to teh embed.FS
var readOnlyFileSystem ReadOnlyFileSystem

type versions struct {
	galasaFrameworkVersion  string
	galasaBootJarVersion    string
	galasactlVersion        string
	galasactlRestApiVersion string
}

var (
	versionsCache *versions = nil
	PropsFileName           = "templates/version/build.properties"
)

func GetGalasaVersion() (string, error) {
	var err error
	fs := GetReadOnlyFileSystem()
	// Note: The cache is set when we read the versions from the embedded file.
	versionsCache, err = readVersionsFromEmbeddedFile(fs, versionsCache)
	var version string
	if err == nil {
		version = versionsCache.galasaFrameworkVersion
	}
	return version, err
}

func GetBootJarVersion() (string, error) {
	var err error
	fs := GetReadOnlyFileSystem()
	// Note: The cache is set when we read the versions from the embedded file.
	versionsCache, err = readVersionsFromEmbeddedFile(fs, versionsCache)
	var version string
	if err == nil {
		version = versionsCache.galasaBootJarVersion
	}
	return version, err
}

func GetGalasaCtlVersion() (string, error) {
	fs := GetReadOnlyFileSystem()
	var err error
	// Note: The cache is set when we read the versions from the embedded file.
	versionsCache, err = readVersionsFromEmbeddedFile(fs, versionsCache)
	var version string
	if err == nil {
		version = versionsCache.galasactlVersion
	}
	return version, err
}

func GetGalasactlRestApiVersion() (string, error) {
	var err error
	fs := GetReadOnlyFileSystem()
	// Note: The cache is set when we read the versions from the embedded file.
	versionsCache, err = readVersionsFromEmbeddedFile(fs, versionsCache)
	var version string
	if err == nil {
		version = versionsCache.galasactlRestApiVersion
	} else {
		log.Printf("Unable to retrieve galasactl rest api version, creating readable error")
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_UNABLE_TO_RETRIEVE_REST_API_VERSION, err.Error())
	}
	return version, err
}

func GetReadOnlyFileSystem() ReadOnlyFileSystem {
	if readOnlyFileSystem == nil {
		readOnlyFileSystem = NewReadOnlyFileSystem()
	}
	return readOnlyFileSystem
}

// readVersionsFromEmbeddedFile - Reads a set of version data from an embedded property file, or returns
// a set of version data we already know about. So that the version data is only ever read once.
func readVersionsFromEmbeddedFile(fs ReadOnlyFileSystem, versionDataAlreadyKnown *versions) (*versions, error) {
	var (
		err   error
		bytes []byte
	)
	if versionDataAlreadyKnown == nil {

		bytes, err = fs.ReadFile(PropsFileName)
		if err == nil {
			propsFileContent := string(bytes)
			properties := props.ReadProperties(propsFileContent)

			versionDataAlreadyKnown = new(versions)

			versionDataAlreadyKnown.galasaBootJarVersion = properties[PROPERTY_NAME_GALASA_BOOT_JAR_VERSION]
			versionDataAlreadyKnown.galasaFrameworkVersion = properties[PROPERTY_NAME_GALASA_FRAMEWORK_VERSION]
			versionDataAlreadyKnown.galasactlVersion = properties[PROPERTY_NAME_GALASACTL_VERSION]
			versionDataAlreadyKnown.galasactlRestApiVersion = properties[PROPERTY_NAME_GALASACTL_REST_API_VERSION]
		}
	}
	return versionDataAlreadyKnown, err
}
