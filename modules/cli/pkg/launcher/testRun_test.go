/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package launcher

import (
	"testing"

	"github.com/galasa-dev/cli/pkg/files"
	"github.com/stretchr/testify/assert"
)

const (
	testFileContentString = `
	{
		"runName": "U527",
		"bundle": "dev.galasa.example.banking.account",
		"testName": "dev.galasa.example.banking.account.TestAccount",
		"testShortName": "TestAccount",
		"requestor": "mcobbett",
		"status": "finished",
		"result": "Passed",
		"queued": "2023-02-14T14:16:42.854396Z",
		"startTime": "2023-02-14T14:16:42.881464Z",
		"endTime": "2023-02-14T14:16:43.082639Z",
		"methods": [
		  {
			"className": "dev.galasa.example.banking.account.TestAccount",
			"methodName": "simpleSampleTest",
			"type": "Test",
			"befores": [],
			"afters": [],
			"status": "finished",
			"result": "Passed",
			"runLogStart": 0,
			"runLogEnd": 0,
			"startTime": "2023-02-14T14:16:43.063018Z",
			"endTime": "2023-02-14T14:16:43.076501Z"
		  }
		]
	}
	`
)

func TestCanReadJsonTestRunFileOk(t *testing.T) {

	fileSystem := files.NewMockFileSystem()
	filePath := "/my/file/path"
	fileSystem.WriteTextFile(filePath, testFileContentString)

	testRun, err := readTestRunFromJsonFile(fileSystem, filePath)
	assert.Nil(t, err, "Failed when it should have worked.")

	assert.NotNil(t, testRun, "testRun should not be nil!")
	assert.Equal(t, "dev.galasa.example.banking.account", *testRun.BundleName, "bad bundle name.")

}

func TestCanCreateSimulatedTestRun(t *testing.T) {
	testRun := createSimulatedTestRun("runId58")
	assert.NotNil(t, testRun)
	assert.Equal(t, testRun.GetName(), "runId58")
	assert.NotNil(t, testRun.GetRasRunId())
	assert.NotNil(t, testRun.GetResult())
	assert.NotNil(t, testRun.GetStatus())
}
