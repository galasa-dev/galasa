/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package launcher

import (
	"testing"

	"github.com/galasa-dev/cli/pkg/embedded"
	"github.com/galasa-dev/cli/pkg/files"
	"github.com/galasa-dev/cli/pkg/props"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

func getBasicPrepareJvmLaunchParams() *RunsPrepareLocalCmdParameters {
	return &RunsPrepareLocalCmdParameters{
		Obrs:                []string{"mvn:my.group/my.artifact/0.1.0/obr"},
		RemoteMaven:         "https://repo.maven.apache.org/maven2",
		TargetGalasaVersion: "0.99.0",
	}
}

func getDefaultPrepareCommandSyntaxParameters() (
	props.JavaProperties,
	*utils.MockEnv,
	spi.GalasaHome,
	spi.FileSystem,
	string,
	[]utils.MavenCoordinates,
	string,
	string,
	string,
	string,
) {
	bootstrapProps := props.JavaProperties{}

	javaHome := "/java"
	fs := files.NewMockFileSystem()
	utils.AddJavaRuntimeToMock(fs, javaHome)

	obrs := make([]utils.MavenCoordinates, 0)
	obrs = append(obrs, utils.MavenCoordinates{
		GroupId:    "myGroup",
		ArtifactId: "myArtifact",
		Version:    "0.2",
		Classifier: "myClassifier",
	})
	remoteMaven := "myRemoteMaven"
	localMaven := ""
	galasaVersionToRun := "0.99.0"
	overridesFilePath := "C:/myFolder/myOverrides.props"

	env := utils.NewMockEnv()
	galasaHome, _ := utils.NewGalasaHome(fs, env, "")

	return bootstrapProps, env, galasaHome, fs, javaHome, obrs,
		remoteMaven, localMaven, galasaVersionToRun, overridesFilePath
}

func TestCanCreateAPrepareLocalJvmLauncher(t *testing.T) {
	env := utils.NewMockEnv()
	env.EnvVars["JAVA_HOME"] = "/java"

	fs := files.NewMockFileSystem()
	utils.AddJavaRuntimeToMock(fs, "/java")

	galasaHome, _ := utils.NewGalasaHome(fs, env, "")
	jvmLaunchParams := getBasicPrepareJvmLaunchParams()
	timeService := utils.NewMockTimeService()
	timedSleeper := utils.NewRealTimedSleeper()
	mockProcess := NewMockProcess()
	mockProcessFactory := NewMockProcessFactory(mockProcess)
	bootstrapProps := getBasicBootstrapProperties()

	mockFactory := &utils.MockFactory{
		Env:         env,
		FileSystem:  fs,
		TimeService: timeService,
	}

	launcher, err := NewPrepareLocalJvmLauncher(
		mockFactory,
		bootstrapProps, embedded.GetReadOnlyFileSystem(),
		jvmLaunchParams,
		mockProcessFactory, galasaHome, timedSleeper)

	if err != nil {
		assert.Fail(t, "Constructor should not have failed but it did. error:%s", err.Error())
	}
	assert.NotNil(t, launcher, "Launcher reference was nil, shouldn't have been.")
}

func TestPrepareFlagAppearsInPrepareCommandArgs(t *testing.T) {
	// For...
	bootstrapProps, _, galasaHome, fs,
		javaHome,
		obrs,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		_ := getDefaultPrepareCommandSyntaxParameters()

	// When...
	_, args, err := getPrepareCommandSyntax(
		bootstrapProps, galasaHome, fs, javaHome,
		obrs,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		BLANK_JWT,
	)

	// Then...
	assert.Nil(t, err)
	assert.Contains(t, args, "--prepare")
}

func TestTestFlagIsAbsentFromPrepareCommandArgs(t *testing.T) {
	// For...
	bootstrapProps, _, galasaHome, fs,
		javaHome,
		obrs,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		_ := getDefaultPrepareCommandSyntaxParameters()

	// When...
	_, args, err := getPrepareCommandSyntax(
		bootstrapProps, galasaHome, fs, javaHome,
		obrs,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		BLANK_JWT,
	)

	// Then...
	assert.Nil(t, err)
	assert.NotContains(t, args, "--test")
	assert.NotContains(t, args, "--gherkin")
}

func TestObrAppearsInPrepareCommandArgs(t *testing.T) {
	// For...
	bootstrapProps, _, galasaHome, fs,
		javaHome,
		obrs,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		_ := getDefaultPrepareCommandSyntaxParameters()

	// When...
	_, args, err := getPrepareCommandSyntax(
		bootstrapProps, galasaHome, fs, javaHome,
		obrs,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		BLANK_JWT,
	)

	// Then...
	assert.Nil(t, err)
	assert.Contains(t, args, "--obr")
}

func TestRemoteMavenAppearsInPrepareCommandArgs(t *testing.T) {
	// For...
	bootstrapProps, _, galasaHome, fs,
		javaHome,
		obrs,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		_ := getDefaultPrepareCommandSyntaxParameters()

	// When...
	_, args, err := getPrepareCommandSyntax(
		bootstrapProps, galasaHome, fs, javaHome,
		obrs,
		remoteMaven,
		localMaven,
		galasaVersionToRun,
		BLANK_JWT,
	)

	// Then...
	assert.Nil(t, err)
	assert.Contains(t, args, "--remotemaven")
	assert.Contains(t, args, remoteMaven)
}
