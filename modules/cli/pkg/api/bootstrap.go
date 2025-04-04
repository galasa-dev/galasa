/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package api

import (
	"io"
	"net/http"
	"strings"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/files"
	"github.com/galasa-dev/cli/pkg/props"
	"github.com/galasa-dev/cli/pkg/spi"
)

const (
	BOOTSTRAP_PROPERTY_NAME_REMOTE_API_SERVER_URL              string = "framework.api.server.url"
	BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_OPTIONS           string = "galasactl.jvm.local.launch.options"
	BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_OPTIONS_SEPARATOR string = " "

	// A uint32 value, says which port will be used when the testcase JVM connects to a Java Debugger.
	BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_DEBUG_PORT string = "galasactl.jvm.local.launch.debug.port"
	// When the JVM connects to a Java Debugger, should it :
	// 'listen' on the debug port, waiting for the java debugger to connect,
	// or
	// 'attach' to the debug port, which already has the java debugger set up.
	BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_DEBUG_MODE string = "galasactl.jvm.local.launch.debug.mode"
)

type BootstrapData struct {
	// Path - the raw path that a user has given us, either from the command-line
	// option or the GALASA_BOOTSTRAP environment variable.
	Path string

	// URL - The URL on which can be followed to reach the bootstrap contents.
	ApiServerURL string

	// Properties - The properties which are read from the bootstrap
	Properties props.JavaProperties
}

type UrlResolutionService interface {
	Get(url string) (string, error)
}

type RealUrlResolutionService struct {
}

// get - Gets the string contents from a URL
func (*RealUrlResolutionService) Get(url string) (string, error) {
	var resp *http.Response
	var contents string = ""
	var err error

	resp, err = http.Get(url)
	// Wrap the error inside a galasa error.
	if err == nil {

		// Make sure the http response is closed (eventually).
		defer resp.Body.Close()

		statusCode := resp.StatusCode
		if statusCode != http.StatusOK {
			err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_RESP_UNEXPECTED_ERROR)
		} else {
			buffer := new(strings.Builder)
			_, err = io.Copy(buffer, resp.Body)
			if err == nil {
				contents = buffer.String()
			} else {
				err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_UNABLE_TO_READ_RESPONSE_BODY, err)
			}
		}
	}
	return contents, err
}

// getDefaultBootstrapPath - Work out where the boostrap file can normally be found.
func getDefaultBootstrapPath(galasaHome spi.GalasaHome) string {

	// Turn the path into a URL
	// This may involve changing the direction of slash characters.
	baseUrl := "file://"

	// All URLs have forward-facing slashes.
	fullUrl := baseUrl + galasaHome.GetUrlFolderPath() + "/bootstrap.properties"

	return fullUrl
}

// loadBootstrap - Loads the contents of a bootstrap file into memory.
// bootstrapPath - Where do we find the bootstrap contents from ? This can be a URL must end in /bootstrap
func LoadBootstrap(
	galasaHome spi.GalasaHome,
	fileSystem spi.FileSystem,
	env spi.Environment,
	bootstrapPath string,
	urlResolutionService UrlResolutionService,
) (*BootstrapData, error) {

	var err error

	var bootstrap *BootstrapData = nil

	path := GetBootstrapLocation(env, galasaHome, bootstrapPath)

	// Default the API server to assume it's running locally, natively.
	defaultApiServerURL := "http://127.0.0.1"

	if strings.HasPrefix(path, "http:") || strings.HasPrefix(path, "https:") {
		// The path looks like a URL...
		bootstrap, err = loadBootstrapFromUrl(path, defaultApiServerURL, urlResolutionService)
	} else {

		// The path looks like a file...
		bootstrap, err = loadBootstrapFromFile(path, defaultApiServerURL, fileSystem)
	}

	if err == nil {
		// Now we have a collection of boot properties. Find the Api server URL if there is one
		// to populate the specified field in the structure.
		if bootstrap.Properties != nil {
			apiServerUrlFromPropsFile := bootstrap.Properties[BOOTSTRAP_PROPERTY_NAME_REMOTE_API_SERVER_URL]
			if apiServerUrlFromPropsFile != "" {
				bootstrap.ApiServerURL = apiServerUrlFromPropsFile
			}
		}
	} else {
		// Don't return any data if there was a failure.
		bootstrap = nil
	}

	if err != nil {
		err = galasaErrors.NewGalasaErrorWithCause(err, galasaErrors.GALASA_ERROR_FAILED_TO_LOAD_BOOTSTRAP_FILE, path, err.Error())
	}

	return bootstrap, err
}

func GetBootstrapLocation(env spi.Environment, galasaHome spi.GalasaHome, explicitUserBootstrap string) string {

	path := explicitUserBootstrap

	// If the --bootstrap flag wasn't specified by the user... default to the value in the
	// GALASA_BOOTSTRAP environment variable.
	if path == "" {
		path = env.GetEnv("GALASA_BOOTSTRAP")
	}

	// If it's still not clear, use the default bootstrap.properties in the ${HOME}/.galasa folder.
	if path == "" {
		path = getDefaultBootstrapPath(galasaHome)
	}
	return path
}

func cleanPath(fileSystem spi.FileSystem, path string) (string, error) {
	var err error
	if path != "" {
		path = removeLeadingFileColon(path)
		err = validateURL(path)
		if err == nil {
			path, err = files.TildaExpansion(fileSystem, path)
		}
	}
	return path, err
}

func removeLeadingFileColon(path string) string {
	path = strings.TrimPrefix(path, "file://")
	return path
}

func validateURL(path string) error {
	var err error
	if strings.HasPrefix(path, "file:") {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_BAD_BOOTSTRAP_FILE_URL, path)
	}
	return err
}

func loadBootstrapFromFile(path string, defaultApiServerURL string, fileSystem spi.FileSystem) (*BootstrapData, error) {
	bootstrap := new(BootstrapData)
	var content string
	var err error

	bootstrap.ApiServerURL = defaultApiServerURL

	path, err = cleanPath(fileSystem, path)
	if err == nil {
		content, err = fileSystem.ReadTextFile(path)
	}

	if err != nil {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_FAILED_TO_READ_BOOTSTRAP_FILE, path, err.Error())
	} else {
		// read the lines and extract the properties
		//	fmt.Printf("bootstrap contents:-\n%v\n", bootstrapString.String())
		bootstrap.Properties = props.ReadProperties(content)
	}

	if err != nil {
		bootstrap = nil
	}
	return bootstrap, err
}

func loadBootstrapFromUrl(path string, defaultApiServerURL string,
	urlResolutionService UrlResolutionService) (*BootstrapData, error) {

	bootstrap := new(BootstrapData)
	bootstrap.Path = path
	var err error

	bootstrap.ApiServerURL = defaultApiServerURL

	// Check that the provided url has /bootstrap at the end.
	// Then strip it off to form the URL we will use.

	if !strings.HasSuffix(path, "/bootstrap") {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_BOOTSTRAP_URL_BAD_ENDING, bootstrap.Path)
	} else {
		// strip off the /bootstrap to get the ApiServerURL
		bootstrap.ApiServerURL = path[:len(path)-10]

		// Use the bootstrap URL to get the bootstrap contents
		var bootstrapContents string
		bootstrapContents, err = urlResolutionService.Get(path)
		if err != nil {
			// Wrap any http error as a galasa error.
			err = galasaErrors.NewGalasaErrorWithCause(err, galasaErrors.GALASA_ERROR_FAILED_TO_GET_BOOTSTRAP, path, err.Error())
		} else {
			// read the lines and extract the properties
			//	fmt.Printf("bootstrap contents:-\n%v\n", bootstrapString.String())
			bootstrap.Properties = props.ReadProperties(bootstrapContents)
		}
	}

	return bootstrap, err
}
