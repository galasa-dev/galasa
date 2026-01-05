/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"fmt"
	"net/http"
	"testing"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

const (
	RUN_U123_WITH_TAGS = `{
		"runId": "xxx123xxx",
		"testStructure": {
			"runName": "U123",
			"bundle": "myBundleId",
			"testName": "myTestPackage.MyTestName",
			"testShortName": "MyTestName",
			"requestor": "unitTesting",
			"user": "unitTesting",
			"status": "Finished",
			"result": "Passed",
			"queued" : "2023-05-10T06:00:13.043037Z",
			"startTime": "2023-05-10T06:00:36.159003Z",
			"endTime": "2023-05-10T06:02:53.823338Z",
			"tags": [
				"tag1",
				"tag2"
			]
		},
		"artifacts": [],
		"webUiUrl": "http://example.com/test-runs/xxx123xxx",
		"restApiUrl": "http://example.com/api/ras/runs/xxx123xxx"
	}`

	RUN_U123_NO_TAGS = `{
		"runId": "xxx123xxx",
		"testStructure": {
			"runName": "U123",
			"bundle": "myBundleId",
			"testName": "myTestPackage.MyTestName",
			"testShortName": "MyTestName",
			"requestor": "unitTesting",
			"user": "unitTesting",
			"status": "Finished",
			"result": "Passed",
			"queued" : "2023-05-10T06:00:13.043037Z",
			"startTime": "2023-05-10T06:00:36.159003Z",
			"endTime": "2023-05-10T06:02:53.823338Z"
		},
		"artifacts": [],
		"webUiUrl": "http://example.com/test-runs/xxx123xxx",
		"restApiUrl": "http://example.com/api/ras/runs/xxx123xxx"
	}`
)

func TestRunsUpdateWithEmptyRunNameReturnsError(t *testing.T) {
	// Given...
	runName := ""
	addTags := []string{"tag1"}
	removeTags := []string{}

	mockConsole := utils.NewMockConsole()
	mockTimeService := utils.NewMockTimeService()
	mockByteReader := utils.NewMockByteReader()
	
	interactions := []utils.HttpInteraction{}
	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiServerUrl := server.Server.URL
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := RunsUpdate(runName, addTags, removeTags, mockConsole, commsClient, mockTimeService, mockByteReader)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1102E")
	assert.ErrorContains(t, err, "--name")
}

func TestRunsUpdateWithNoTagsReturnsError(t *testing.T) {
	// Given...
	runName := "U123"
	addTags := []string{}
	removeTags := []string{}

	mockConsole := utils.NewMockConsole()
	mockTimeService := utils.NewMockTimeService()
	mockByteReader := utils.NewMockByteReader()
	
	interactions := []utils.HttpInteraction{}
	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiServerUrl := server.Server.URL
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := RunsUpdate(runName, addTags, removeTags, mockConsole, commsClient, mockTimeService, mockByteReader)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1274E")
	assert.ErrorContains(t, err, "--add-tags or --remove-tags")
}

func TestRunsUpdateWithInvalidRunNameReturnsError(t *testing.T) {
	// Given...
	runName := "invalid@runname"
	addTags := []string{"tag1"}
	removeTags := []string{}

	mockConsole := utils.NewMockConsole()
	mockTimeService := utils.NewMockTimeService()
	mockByteReader := utils.NewMockByteReader()
	
	interactions := []utils.HttpInteraction{}
	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiServerUrl := server.Server.URL
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := RunsUpdate(runName, addTags, removeTags, mockConsole, commsClient, mockTimeService, mockByteReader)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1075E")
	assert.ErrorContains(t, err, runName)
}

func TestRunsUpdateWithSameTagInAddAndRemoveReturnsError(t *testing.T) {
	// Given...
	runName := "U123"
	addTags := []string{"tag1", "tag2"}
	removeTags := []string{"tag2", "tag3"}

	mockConsole := utils.NewMockConsole()
	mockTimeService := utils.NewMockTimeService()
	mockByteReader := utils.NewMockByteReader()
	
	interactions := []utils.HttpInteraction{}
	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiServerUrl := server.Server.URL
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := RunsUpdate(runName, addTags, removeTags, mockConsole, commsClient, mockTimeService, mockByteReader)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1273E")
	assert.ErrorContains(t, err, "tag2")
}

func TestRunsUpdateWithNonExistentRunReturnsError(t *testing.T) {
	// Given...
	runName := "U999"
	addTags := []string{"tag1"}
	removeTags := []string{}

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
	getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(EMPTY_RUNS_RESPONSE))
	}

	interactions := []utils.HttpInteraction{
		getRunsInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	mockTimeService := utils.NewMockTimeService()
	mockByteReader := utils.NewMockByteReader()
	apiServerUrl := server.Server.URL
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := RunsUpdate(runName, addTags, removeTags, mockConsole, commsClient, mockTimeService, mockByteReader)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1275E")
	assert.ErrorContains(t, err, runName)
}

func TestRunsUpdateAddTagsToRunWithExistingTagsSucceeds(t *testing.T) {
	// Given...
	runName := "U123"
	addTags := []string{"tag3", "tag4"}
	removeTags := []string{}

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
	getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(fmt.Sprintf(`
			{
				"nextCursor": "",
				"pageSize": 100,
				"amountOfRuns": 1,
				"runs":[ %s ]
			}`, RUN_U123_WITH_TAGS)))
	}

	updateRunInteraction := utils.NewHttpInteraction("/ras/runs/xxx123xxx", http.MethodPut)
	updateRunInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
	}

	interactions := []utils.HttpInteraction{
		getRunsInteraction,
		updateRunInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	mockTimeService := utils.NewMockTimeService()
	mockByteReader := utils.NewMockByteReader()
	apiServerUrl := server.Server.URL
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := RunsUpdate(runName, addTags, removeTags, mockConsole, commsClient, mockTimeService, mockByteReader)

	// Then...
	assert.Nil(t, err)
}

func TestRunsUpdateRemoveTagsFromRunSucceeds(t *testing.T) {
	// Given...
	runName := "U123"
	addTags := []string{}
	removeTags := []string{"tag1"}

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
	getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(fmt.Sprintf(`
			{
				"nextCursor": "",
				"pageSize": 100,
				"amountOfRuns": 1,
				"runs":[ %s ]
			}`, RUN_U123_WITH_TAGS)))
	}

	updateRunInteraction := utils.NewHttpInteraction("/ras/runs/xxx123xxx", http.MethodPut)
	updateRunInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
	}

	interactions := []utils.HttpInteraction{
		getRunsInteraction,
		updateRunInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	mockTimeService := utils.NewMockTimeService()
	mockByteReader := utils.NewMockByteReader()
	apiServerUrl := server.Server.URL
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := RunsUpdate(runName, addTags, removeTags, mockConsole, commsClient, mockTimeService, mockByteReader)

	// Then...
	assert.Nil(t, err)
}

func TestRunsUpdateAddAndRemoveTagsSucceeds(t *testing.T) {
	// Given...
	runName := "U123"
	addTags := []string{"tag3"}
	removeTags := []string{"tag1"}

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
	getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(fmt.Sprintf(`
			{
				"nextCursor": "",
				"pageSize": 100,
				"amountOfRuns": 1,
				"runs":[ %s ]
			}`, RUN_U123_WITH_TAGS)))
	}

	updateRunInteraction := utils.NewHttpInteraction("/ras/runs/xxx123xxx", http.MethodPut)
	updateRunInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
	}

	interactions := []utils.HttpInteraction{
		getRunsInteraction,
		updateRunInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	mockTimeService := utils.NewMockTimeService()
	mockByteReader := utils.NewMockByteReader()
	apiServerUrl := server.Server.URL
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := RunsUpdate(runName, addTags, removeTags, mockConsole, commsClient, mockTimeService, mockByteReader)

	// Then...
	assert.Nil(t, err)
}

func TestRunsUpdateAddTagsToRunWithNoExistingTagsSucceeds(t *testing.T) {
	// Given...
	runName := "U123"
	addTags := []string{"tag1", "tag2"}
	removeTags := []string{}

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
	getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(fmt.Sprintf(`
			{
				"nextCursor": "",
				"pageSize": 100,
				"amountOfRuns": 1,
				"runs":[ %s ]
			}`, RUN_U123_NO_TAGS)))
	}

	updateRunInteraction := utils.NewHttpInteraction("/ras/runs/xxx123xxx", http.MethodPut)
	updateRunInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
	}

	interactions := []utils.HttpInteraction{
		getRunsInteraction,
		updateRunInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	mockTimeService := utils.NewMockTimeService()
	mockByteReader := utils.NewMockByteReader()
	apiServerUrl := server.Server.URL
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := RunsUpdate(runName, addTags, removeTags, mockConsole, commsClient, mockTimeService, mockByteReader)

	// Then...
	assert.Nil(t, err)
}

func TestRunsUpdateWithDuplicateAddTagsRemovesDuplicates(t *testing.T) {
	// Given...
	runName := "U123"
	addTags := []string{"tag3", "tag3", "tag4"}
	removeTags := []string{}

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
	getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(fmt.Sprintf(`
			{
				"nextCursor": "",
				"pageSize": 100,
				"amountOfRuns": 1,
				"runs":[ %s ]
			}`, RUN_U123_WITH_TAGS)))
	}

	updateRunInteraction := utils.NewHttpInteraction("/ras/runs/xxx123xxx", http.MethodPut)
	updateRunInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
	}

	interactions := []utils.HttpInteraction{
		getRunsInteraction,
		updateRunInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	mockTimeService := utils.NewMockTimeService()
	mockByteReader := utils.NewMockByteReader()
	apiServerUrl := server.Server.URL
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := RunsUpdate(runName, addTags, removeTags, mockConsole, commsClient, mockTimeService, mockByteReader)

	// Then...
	assert.Nil(t, err)
}

func TestRunsUpdateWithDuplicateRemoveTagsRemovesDuplicates(t *testing.T) {
	// Given...
	runName := "U123"
	addTags := []string{}
	removeTags := []string{"tag1", "tag1", "tag2"}

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
	getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(fmt.Sprintf(`
			{
				"nextCursor": "",
				"pageSize": 100,
				"amountOfRuns": 1,
				"runs":[ %s ]
			}`, RUN_U123_WITH_TAGS)))
	}

	updateRunInteraction := utils.NewHttpInteraction("/ras/runs/xxx123xxx", http.MethodPut)
	updateRunInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
	}

	interactions := []utils.HttpInteraction{
		getRunsInteraction,
		updateRunInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	mockTimeService := utils.NewMockTimeService()
	mockByteReader := utils.NewMockByteReader()
	apiServerUrl := server.Server.URL
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := RunsUpdate(runName, addTags, removeTags, mockConsole, commsClient, mockTimeService, mockByteReader)

	// Then...
	assert.Nil(t, err)
}

func TestRunsUpdateWithUpdateFailureReturnsError(t *testing.T) {
	// Given...
	runName := "U123"
	addTags := []string{"tag3"}
	removeTags := []string{}

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
	getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(fmt.Sprintf(`
			{
				"nextCursor": "",
				"pageSize": 100,
				"amountOfRuns": 1,
				"runs":[ %s ]
			}`, RUN_U123_WITH_TAGS)))
	}

	updateRunInteraction := utils.NewHttpInteraction("/ras/runs/xxx123xxx", http.MethodPut)
	updateRunInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusInternalServerError)
		writer.Write([]byte(`{"error": "Internal server error"}`))
	}

	interactions := []utils.HttpInteraction{
		getRunsInteraction,
		updateRunInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	mockTimeService := utils.NewMockTimeService()
	mockByteReader := utils.NewMockByteReader()
	apiServerUrl := server.Server.URL
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := RunsUpdate(runName, addTags, removeTags, mockConsole, commsClient, mockTimeService, mockByteReader)

	// Then...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1280E")
}

func TestRunsUpdateAddingExistingTagDoesNotDuplicate(t *testing.T) {
	// Given...
	runName := "U123"
	addTags := []string{"tag1"} // tag1 already exists in the run
	removeTags := []string{}

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
	getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(fmt.Sprintf(`
			{
				"nextCursor": "",
				"pageSize": 100,
				"amountOfRuns": 1,
				"runs":[ %s ]
			}`, RUN_U123_WITH_TAGS)))
	}

	updateRunInteraction := utils.NewHttpInteraction("/ras/runs/xxx123xxx", http.MethodPut)
	updateRunInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
	}

	interactions := []utils.HttpInteraction{
		getRunsInteraction,
		updateRunInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	mockTimeService := utils.NewMockTimeService()
	mockByteReader := utils.NewMockByteReader()
	apiServerUrl := server.Server.URL
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := RunsUpdate(runName, addTags, removeTags, mockConsole, commsClient, mockTimeService, mockByteReader)

	// Then...
	assert.Nil(t, err)
}

func TestRunsUpdateRemovingNonExistentTagSucceeds(t *testing.T) {
	// Given...
	runName := "U123"
	addTags := []string{}
	removeTags := []string{"tag99"} // tag99 doesn't exist in the run

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
	getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(fmt.Sprintf(`
			{
				"nextCursor": "",
				"pageSize": 100,
				"amountOfRuns": 1,
				"runs":[ %s ]
			}`, RUN_U123_WITH_TAGS)))
	}

	updateRunInteraction := utils.NewHttpInteraction("/ras/runs/xxx123xxx", http.MethodPut)
	updateRunInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
	}

	interactions := []utils.HttpInteraction{
		getRunsInteraction,
		updateRunInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	mockTimeService := utils.NewMockTimeService()
	mockByteReader := utils.NewMockByteReader()
	apiServerUrl := server.Server.URL
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := RunsUpdate(runName, addTags, removeTags, mockConsole, commsClient, mockTimeService, mockByteReader)

	// Then...
	assert.Nil(t, err)
}

func TestRunsUpdateRemoveAllTagsSucceeds(t *testing.T) {
	// Given...
	runName := "U123"
	addTags := []string{}
	removeTags := []string{"tag1", "tag2"}

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
	getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(fmt.Sprintf(`
			{
				"nextCursor": "",
				"pageSize": 100,
				"amountOfRuns": 1,
				"runs":[ %s ]
			}`, RUN_U123_WITH_TAGS)))
	}

	updateRunInteraction := utils.NewHttpInteraction("/ras/runs/xxx123xxx", http.MethodPut)
	updateRunInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
	}

	interactions := []utils.HttpInteraction{
		getRunsInteraction,
		updateRunInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	mockTimeService := utils.NewMockTimeService()
	mockByteReader := utils.NewMockByteReader()
	apiServerUrl := server.Server.URL
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := RunsUpdate(runName, addTags, removeTags, mockConsole, commsClient, mockTimeService, mockByteReader)

	// Then...
	assert.Nil(t, err)
}
