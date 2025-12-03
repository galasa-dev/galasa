/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package launcher

import (
	"strconv"
	"strings"

	"github.com/galasa-dev/cli/pkg/api"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/props"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/utils"
)

// -----------------------------------------------------------------------------
// Local functions that can be used by different types of JVM launchers
// -----------------------------------------------------------------------------

func getBaseCommandSyntax(
	bootstrapProperties props.JavaProperties,
	galasaHome spi.GalasaHome,
	fileSystem spi.FileSystem,
	javaHome string,
	obrs []utils.MavenCoordinates,
	remoteMavenRepos []string,
	localMaven string,
	galasaVersionToRun string,
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
		for _, repo := range remoteMavenRepos {
			args = append(args, "--remotemaven")
			args = append(args, repo)
		}

		// --bootstrap file:${HOME}/.galasa/bootstrap.properties
		args = append(args, "--bootstrap")
		bootstrapPath := "file:///" + galasaHome.GetUrlFolderPath() + "/bootstrap.properties"
		args = append(args, bootstrapPath)

		for _, obrCoordinate := range obrs {
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

		if isTraceEnabled {
			args = append(args, "--trace")
		}
	}

	return cmd, args, err
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

// isCPSRemote - decide whether the config store used by tests is remote or not.
// If it is remote, we are going to have to get a valid JWT to use.
func isCPSRemote(bootstrapProps props.JavaProperties) bool {
	isRemote := false
	configStoreProp := bootstrapProps["framework.config.store"]
	isRemote = strings.HasPrefix(configStoreProp, "galasacps")
	return isRemote
}

// Gets the https URL of the config store, to be used contacting the remote CPS.
func getCPSRemoteApiServerUrl(bootstrapProps props.JavaProperties) string {
	configStoreGalasaUrl := bootstrapProps["framework.config.store"]
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

		// Split into separate characters
		launchOptionChars := strings.Split(jvmLaunchOptions, "")

		// Process each character in turn
		var argBuilder strings.Builder
		for i, inQuotes := 0, false; i < len(launchOptionChars); i++ {
			if launchOptionChars[i] == api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_OPTIONS_QUOTE {
				// Start or end of quoted block. Update flag and discard the quote.
				inQuotes = !inQuotes
			} else {
				if !inQuotes {
					if (launchOptionChars[i] == api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_OPTIONS_SEPARATOR) {
						// If we've reached an unquoted space, that marks the end of the argument so
						// we add what we've built so far to the list of args returned
						args = append(args, argBuilder.String())
						argBuilder.Reset()
					} else {
						argBuilder.WriteString(launchOptionChars[i])
					}
				} else {
					if i < len(launchOptionChars) - 1 &&
							launchOptionChars[i] == api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_OPTIONS_ESCAPE &&
							launchOptionChars[i+1] == api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_OPTIONS_QUOTE {
						// It's an escaped quote. We include the quote in the argument but discard the escape character.
						argBuilder.WriteString(api.BOOTSTRAP_PROPERTY_NAME_LOCAL_JVM_LAUNCH_OPTIONS_QUOTE)
						i++
					} else {
						argBuilder.WriteString(launchOptionChars[i])
					}
				}
			}
		}

		// Add the last argument to the list
		if argBuilder.Len() != 0 {
			args = append(args, argBuilder.String())
		}
	}

	return args
}