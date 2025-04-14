/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package api

import (
	"testing"

	"github.com/galasa-dev/cli/pkg/files"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

type MockUrlResolutionService struct {
	StringToReturnFromGet string
	ErrorToReturnFromGet  error
}

func (mockSericeData *MockUrlResolutionService) Get(url string) (string, error) {
	return mockSericeData.StringToReturnFromGet, mockSericeData.ErrorToReturnFromGet
}

func TestCanReadPropertiesInRemoteHttpBoostrap(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()

	mockUrlResolutionService := &MockUrlResolutionService{StringToReturnFromGet: "a=b", ErrorToReturnFromGet: nil}

	// Empty environment variables.
	mockEnvironment := utils.NewMockEnv()
	galasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	bootstrapData, err := LoadBootstrap(galasaHome, mockFileSystem, mockEnvironment, "http://my.fake.server/dummy-url/bootstrap", mockUrlResolutionService)

	if err != nil {
		assert.Fail(t, "Loading bootstrap failed when it should have worked. error:%s", err.Error())
	}
	assert.Equal(t, bootstrapData.Properties["a"], "b", "Failed to read the dummy boostrap properties.")
	assert.Equal(t, bootstrapData.ApiServerURL, "http://my.fake.server/dummy-url", "Failed to default API server URL to stripped bootstrap part.")
}

func TestCanReadPropertiesInLocalFileBoostrap(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockFileSystem.WriteTextFile("my-bootstrap-file", "a=b")

	// Shouldn't need to consult any network traffic.
	var mockUrlResolutionService *MockUrlResolutionService = nil

	// Empty environment variables.
	mockEnvironment := utils.NewMockEnv()
	galasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	bootstrapData, err := LoadBootstrap(
		galasaHome, mockFileSystem, mockEnvironment, "my-bootstrap-file",
		mockUrlResolutionService)

	if err != nil {
		assert.Fail(t, "Loading bootstrap failed when it should have worked. error:%s", err.Error())
	}
	assert.Equal(t, bootstrapData.Properties["a"], "b", "Failed to read the dummy boostrap properties.")
	assert.Equal(t, "http://127.0.0.1", bootstrapData.ApiServerURL, "Failed to default API server URL to stripped bootstrap part.")
}

func TestCanReadRemoteApiServerUrlFromLocalFileBoostrap(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockFileSystem.WriteTextFile(
		"my-bootstrap-file",
		"a=b\n"+
			BOOTSTRAP_PROPERTY_NAME_REMOTE_API_SERVER_URL+"= http://my.fake.server/dummy-url ",
	)

	// Shouldn't need to consult any network traffic.
	var mockUrlResolutionService *MockUrlResolutionService = nil

	// Empty environment variables.
	mockEnvironment := utils.NewMockEnv()

	galasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	bootstrapData, err := LoadBootstrap(galasaHome,
		mockFileSystem, mockEnvironment,
		"my-bootstrap-file", mockUrlResolutionService)

	if err != nil {
		assert.Fail(t, "Loading bootstrap failed when it should have worked. error:%s", err.Error())
	}
	assert.Equal(t, bootstrapData.Properties["a"], "b", "Failed to read the dummy boostrap properties.")
	assert.Equal(t, bootstrapData.ApiServerURL, "http://my.fake.server/dummy-url", "Failed to set API server URL to value found in the bootstrap.")
}

func TestCanReadLocalBootstrapFileFromDefaultPlace(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	home, _ := mockFileSystem.GetUserHomeDirPath()
	mockFileSystem.WriteTextFile(
		home+"/.galasa/bootstrap.properties",
		"a=b\n"+
			BOOTSTRAP_PROPERTY_NAME_REMOTE_API_SERVER_URL+"= http://my.fake.server/dummy-url ",
	)

	// Shouldn't need to consult any network traffic.
	var mockUrlResolutionService *MockUrlResolutionService = nil

	// Empty environment variables.
	mockEnvironment := utils.NewMockEnv()

	galasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	var bootstrapPath = "" // Causes the default file in .galasa to be read.
	bootstrapData, err := LoadBootstrap(galasaHome,
		mockFileSystem, mockEnvironment, bootstrapPath, mockUrlResolutionService)

	if err != nil {
		assert.Fail(t, "Loading bootstrap failed when it should have worked. error:%s", err.Error())
	}
	assert.Equal(t, bootstrapData.Properties["a"], "b", "Failed to read the dummy boostrap properties.")
	assert.Equal(t, bootstrapData.ApiServerURL, "http://my.fake.server/dummy-url", "Failed to set API server URL to value found in the bootstrap.")
}

// TestBootstrapFromEnvVarGetsUsed When the environment variable GALASA_BOOTSTRAP refers to a file,
// and no bootstrap is supplied any other way, then that bootstrap value is used.
// (Where it could refer to file on disk for example, or a remote ecosystem.)
func TestBootstrapFromEnvVarGetsUsed(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()

	// Create a bootstrap not in the default place. We should find it as the Env Var points to it.
	mockFileSystem.WriteTextFile(
		"/my.bootstrap.properties",
		"a=b\n"+
			BOOTSTRAP_PROPERTY_NAME_REMOTE_API_SERVER_URL+"= http://my.fake.server/dummy-url ",
	)

	// Shouldn't need to consult any network traffic.
	var mockUrlResolutionService *MockUrlResolutionService = nil

	// Empty environment variables.
	mockEnvironment := utils.NewMockEnv()
	mockEnvironment.EnvVars["GALASA_BOOTSTRAP"] = "/my.bootstrap.properties"

	galasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	var bootstrapPath = "" // Causes the default file in .galasa to be read.
	bootstrapData, err := LoadBootstrap(
		galasaHome, mockFileSystem, mockEnvironment,
		bootstrapPath, mockUrlResolutionService)

	if err != nil {
		assert.Fail(t, "Loading bootstrap failed when it should have worked. error:%s", err.Error())
	}
	assert.Equal(t, bootstrapData.Properties["a"], "b", "Failed to read the dummy boostrap properties.")
	assert.Equal(t, bootstrapData.ApiServerURL, "http://my.fake.server/dummy-url", "Failed to set API server URL to value found in the bootstrap.")
}

func TestBootstrapExpandsTildaPathToHome(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()

	// Create a bootstrap not in the default place. We should find it as the Env Var points to it.
	mockFileSystem.WriteTextFile(
		"/User/Home/testuser/my.bootstrap.properties",
		"a=b\n"+
			BOOTSTRAP_PROPERTY_NAME_REMOTE_API_SERVER_URL+"= http://my.fake.server/dummy-url ",
	)

	// Shouldn't need to consult any network traffic.
	var mockUrlResolutionService *MockUrlResolutionService = nil

	// Empty environment variables.
	mockEnvironment := utils.NewMockEnv()
	mockEnvironment.EnvVars["GALASA_BOOTSTRAP"] = "~/my.bootstrap.properties"

	galasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	var bootstrapPath = "" // Causes the default file in .galasa to be read.
	bootstrapData, err := LoadBootstrap(
		galasaHome, mockFileSystem, mockEnvironment,
		bootstrapPath, mockUrlResolutionService)

	if err != nil {
		assert.Fail(t, "Loading bootstrap failed when it should have worked. error:%s", err.Error())
	}
	assert.Equal(t, bootstrapData.Properties["a"], "b", "Failed to read the dummy boostrap properties.")
	assert.Equal(t, bootstrapData.ApiServerURL, "http://my.fake.server/dummy-url", "Failed to set API server URL to value found in the bootstrap.")
}

func TestBootstrapExpandsFileColonPath(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()

	// Create a bootstrap not in the default place. We should find it as the Env Var points to it.
	mockFileSystem.WriteTextFile(
		"/my.bootstrap.properties",
		"a=b\n"+
			BOOTSTRAP_PROPERTY_NAME_REMOTE_API_SERVER_URL+"= http://my.fake.server/dummy-url ",
	)

	// Shouldn't need to consult any network traffic.
	var mockUrlResolutionService *MockUrlResolutionService = nil

	// Empty environment variables.
	mockEnvironment := utils.NewMockEnv()
	mockEnvironment.EnvVars["GALASA_BOOTSTRAP"] = "file:///my.bootstrap.properties"

	galasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	var bootstrapPath = "" // Causes the default file in .galasa to be read.
	bootstrapData, err := LoadBootstrap(
		galasaHome,
		mockFileSystem,
		mockEnvironment,
		bootstrapPath,
		mockUrlResolutionService)

	if err != nil {
		assert.Fail(t, "Loading bootstrap failed when it should have worked. error:%s", err.Error())
	}
	assert.Equal(t, bootstrapData.Properties["a"], "b", "Failed to read the dummy boostrap properties.")
	assert.Equal(t, bootstrapData.ApiServerURL, "http://my.fake.server/dummy-url", "Failed to set API server URL to value found in the bootstrap.")
}

func TestBootstrapFileURLReturnsBadWhenLeadingFileNotStripped(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()

	// Create a bootstrap not in the default place. We should find it as the Env Var points to it.
	mockFileSystem.WriteTextFile(
		"file:/my.bootstrap.properties",
		"a=b\n"+
		BOOTSTRAP_PROPERTY_NAME_REMOTE_API_SERVER_URL+"= http://my.fake.server/dummy-url",
	)

	// Shouldn't need to consult any network traffic.
	var mockUrlResolutionService *MockUrlResolutionService = nil

	// Empty environment variables.
	mockEnvironment := utils.NewMockEnv()

	galasaHome, _ := utils.NewGalasaHome(mockFileSystem, mockEnvironment, "")

	var bootstrapPath = "file:/my.bootstrap.properties"
	_, err := LoadBootstrap(
		galasaHome,
		mockFileSystem,
		mockEnvironment,
		bootstrapPath,
		mockUrlResolutionService)
	
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1091E:")
}