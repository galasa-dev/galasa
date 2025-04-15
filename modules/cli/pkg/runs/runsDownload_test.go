/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"os"
	"testing"
	"time"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/files"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

const (
	RUN_U1 = `{
		 "runId": "xxx876xxx",
		 "testStructure": {
			 "runName": "U1",
			 "bundle": "myBundleId",
			 "testName": "myTestPackage.MyTestName",
			 "testShortName": "MyTestName",
			 "requestor": "unitTesting",
			 "status" : "Finished",
			 "result" : "Passed",
			 "queued" : "2023-05-10T06:00:13.043037Z",
			 "startTime": "2023-05-10T06:00:36.159003Z",
			 "endTime": "2023-05-10T06:02:53.823338Z",
			 "methods": [{
				 "className": "myTestPackage.MyTestName",
				 "methodName": "myTestMethodName",
				 "type": "test",
				 "status": "Done",
				 "result": "Success",
				 "startTime": null,
				 "endTime": null,
				 "runLogStart":null,
				 "runLogEnd":null,
				 "befores":[]
			 }]
		 },
		 "artifacts": [{
			 "artifactPath": "myPathToArtifact1",
			 "contentType":	"application/json"
		 }]
	 }`

	RUN_U27 = `{
		 "runId": "xxx543xxx",
		 "testStructure": {
			 "runName": "U27",
			 "bundle": "myBun27",
			 "testName": "myTestPackage.MyTest27",
			 "testShortName": "MyTestName27",
			 "requestor": "unitTesting27",
			 "status" : "building",
			 "queued" : "2023-05-10T06:00:13.043037Z",
			 "startTime": "2023-05-10T06:00:36.159003Z",
			 "methods": [{
				 "className": "myTestPackage27.MyTestName27",
				 "methodName": "myTestMethodName",
				 "type": "test",
				 "status": "Done",
				 "result": "UNKNOWN",
				 "startTime": null,
				 "endTime": null,
				 "runLogStart":null,
				 "runLogEnd":null,
				 "befores":[]
			 }]
		 },
		 "artifacts": [{
			 "artifactPath": "myPathToArtifact1",
			 "contentType":	"application/json"
		 }]
	}`

	RUN_U27V2 = `{
		"runId": "xxx987xxx",
		"testStructure": {
			"runName": "U27",
			"bundle": "myBun27",
			"testName": "myTestPackage.MyTest27",
			"testShortName": "MyTestName27",
			"requestor": "unitTesting27",
			"status" : "finished",
			"result" : "Passed",
			"queued" : "2023-05-10T06:00:13.043037Z",
			"startTime": "2023-05-10T06:01:36.159003Z",
			"endTime": "2023-05-10T06:02:53.823338Z",
			"methods": [{
				"className": "myTestPackage27.MyTestName27",
				"methodName": "myTestMethodName",
				"type": "test",
				"status": "Done",
				"result": "UNKNOWN",
				"startTime": null,
				"endTime": null,
				"runLogStart":null,
				"runLogEnd":null,
				"befores":[]
			}]
		},
		"artifacts": [{
			"artifactPath": "myPathToArtifact2",
			"contentType":	"application/json"
		}]
   }`

	RUN_U27V3 = `{
		"runId": "xxx1234xxx",
		"testStructure": {
			"runName": "U27",
			"bundle": "myBun27",
			"testName": "myTestPackage.MyTest27",
			"testShortName": "MyTestName27",
			"requestor": "unitTesting27",
			"status" : "finished",
			"result" : "EnvFail",
			"queued" : "2022-05-10T04:00:13.043037Z",
			"startTime": "2022-05-10T04:01:36.159003Z",
			"methods": [{
				"className": "myTestPackage27.MyTestName27",
				"methodName": "myTestMethodName",
				"type": "test",
				"status": "Done",
				"result": "UNKNOWN",
				"startTime": null,
				"endTime": null,
				"runLogStart":null,
				"runLogEnd":null,
				"befores":[]
			}]
		},
		"artifacts": [{
			"artifactPath": "myPathToArtifact2",
			"contentType":	"application/json"
		}]
	}`

	RUN_U27V4 = `{
		"runId": "xxx4321xxx",
		"testStructure": {
			"runName": "U27",
			"bundle": "myBun27",
			"testName": "myTestPackage.MyTest27",
			"testShortName": "MyTestName27",
			"requestor": "unitTesting27",
			"status" : "finished",
			"result" : "EnvFail",
			"queued" : "2022-05-10T04:00:13.043037Z",
			"startTime": "2022-05-10T04:10:36.159003Z",
			"methods": [{
				"className": "myTestPackage27.MyTestName27",
				"methodName": "myTestMethodName",
				"type": "test",
				"status": "Done",
				"result": "UNKNOWN",
				"startTime": null,
				"endTime": null,
				"runLogStart":null,
				"runLogEnd":null,
				"befores":[]
			}]
		},
		"artifacts": [{
			"artifactPath": "myPathToArtifact2",
			"contentType":	"application/json"
		}]
	}`

	RUN_U27V5 = `{
		"runId": "xxx2468xxx",
		"testStructure": {
			"runName": "U27",
			"bundle": "myBun27",
			"testName": "myTestPackage.MyTest27",
			"testShortName": "MyTestName27",
			"requestor": "unitTesting27",
			"status" : "Building",
			"queued" : "2022-05-10T04:00:13.043037Z",
			"startTime": "2022-05-10T04:10:36.159003Z",
			"methods": [{
				"className": "myTestPackage27.MyTestName27",
				"methodName": "myTestMethodName",
				"type": "test",
				"status": "Done",
				"result": "UNKNOWN",
				"startTime": null,
				"endTime": null,
				"runLogStart":null,
				"runLogEnd":null,
				"befores":[]
			}]
		},
		"artifacts": [{
			"artifactPath": "myPathToArtifact2",
			"contentType":	"application/json"
		}]
	}`
)

type MockArtifact struct {
	Path        string `json:"path"`
	ContentType string `json:"contentType"`
	Size        int    `json:"size"`
}

func NewMockArtifact(mockPath string, mockContentType string, mockSize int) *MockArtifact {
	mockArtifact := MockArtifact{Path: mockPath, ContentType: mockContentType, Size: mockSize}
	return &mockArtifact
}

//------------------------------------------------------------------
// Helper methods
//------------------------------------------------------------------

// Sets a response for requests to /ras/runs
func WriteMockRasRunsResponse(
	t *testing.T,
	writer http.ResponseWriter,
	req *http.Request,
	runName string,
	runResultStrings []string) {

	writer.Header().Set("Content-Type", "application/json")

	values := req.URL.Query()
	runNameQueryParameter := values.Get("runname")

	assert.Equal(t, runNameQueryParameter, runName)

	combinedRunResultStrings := ""
	for index, runResult := range runResultStrings {
		if index > 0 {
			combinedRunResultStrings += ","
		}
		combinedRunResultStrings += runResult
	}

	writer.Write([]byte(fmt.Sprintf(`
	{
		"pageSize": 1,
		"amountOfRuns": %d,
		"runs":[ %s ]
	}`, len(runResultStrings), combinedRunResultStrings)))
}

//------------------------------------------------------------------
// Test methods
//------------------------------------------------------------------

func TestRunsDownloadFailingFileWriteReturnsError(t *testing.T) {
	// Given ...
	runName := "U27"
	runId := "xxx543xxx"
	forceDownload := false

	dummyTxtArtifact := NewMockArtifact("/artifacts/dummy.txt", "text/plain", 1024)

    // Create the expected HTTP interactions with the API server
    getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
	
		assert.Equal(t, runNameQueryParameter, runName)
		
		writer.Write([]byte(fmt.Sprintf(`
		{
			"pageSize": 1,
			"amountOfRuns": 1,
			"runs":[ %s ]
		}`, RUN_U27)))
    }

	getArtifactsInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/artifacts", http.MethodGet)
    getArtifactsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		artifactsJsonStr := fmt.Sprintf(`[
			{
				"path": "%s",
				"contentType": "%s",
				"size": "%d"
			}
		]`, dummyTxtArtifact.Path, dummyTxtArtifact.ContentType, dummyTxtArtifact.Size)
	
		writer.Write([]byte(artifactsJsonStr))
    }

	goodDownloadInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/files" + dummyTxtArtifact.Path, http.MethodGet)
    goodDownloadInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Disposition", "attachment")
		writer.Write([]byte(dummyTxtArtifact.Path))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
		getArtifactsInteraction,
		goodDownloadInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	mockFileSystem := files.NewOverridableMockFileSystem()

	mockFile := files.MockFile{}
	mockFile.VirtualFunction_Write = func(content []byte) (int, error) {
		return 0, errors.New("simulating failed file write")
	}

	mockFileSystem.VirtualFunction_Create = func(path string) (io.WriteCloser, error) {
		return &mockFile, nil
	}

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := DownloadArtifacts(runName, forceDownload, mockFileSystem, mockTimeService, mockConsole, commsClient, ".")

	// Then...
	assert.Contains(t, err.Error(), "GAL1042")
}

func TestRunsDownloadFailingFileCreationReturnsError(t *testing.T) {
	// Given ...
	runName := "U27"
	runId := "xxx543xxx"
	forceDownload := false

	dummyTxtArtifact := NewMockArtifact("/artifacts/dummy.txt", "text/plain", 1024)

    // Create the expected HTTP interactions with the API server
    getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
	
		assert.Equal(t, runNameQueryParameter, runName)
		
		writer.Write([]byte(fmt.Sprintf(`
		{
			"pageSize": 1,
			"amountOfRuns": 1,
			"runs":[ %s ]
		}`, RUN_U27)))
    }

	getArtifactsInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/artifacts", http.MethodGet)
    getArtifactsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		artifactsJsonStr := fmt.Sprintf(`[
			{
				"path": "%s",
				"contentType": "%s",
				"size": "%d"
			}
		]`, dummyTxtArtifact.Path, dummyTxtArtifact.ContentType, dummyTxtArtifact.Size)
	
		writer.Write([]byte(artifactsJsonStr))
    }

	goodDownloadInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/files" + dummyTxtArtifact.Path, http.MethodGet)
    goodDownloadInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Disposition", "attachment")
		writer.Write([]byte(dummyTxtArtifact.Path))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
		getArtifactsInteraction,
		goodDownloadInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	mockFileSystem := files.NewOverridableMockFileSystem()

	mockFileSystem.VirtualFunction_Create = func(path string) (io.WriteCloser, error) {
		return nil, errors.New("simulating failed folder creation")
	}

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := DownloadArtifacts(runName, forceDownload, mockFileSystem, mockTimeService, mockConsole, commsClient, ".")

	// Then...
	assert.Contains(t, err.Error(), "GAL1042")
}

func TestRunsDownloadFailingFolderCreationReturnsError(t *testing.T) {
	// Given ...
	runName := "U27"
	runId := "xxx543xxx"
	forceDownload := false

	dummyTxtArtifact := NewMockArtifact("/artifacts/dummy.txt", "text/plain", 1024)

    // Create the expected HTTP interactions with the API server
    getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
	
		assert.Equal(t, runNameQueryParameter, runName)
		
		writer.Write([]byte(fmt.Sprintf(`
		{
			"pageSize": 1,
			"amountOfRuns": 1,
			"runs":[ %s ]
		}`, RUN_U27)))
    }

	getArtifactsInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/artifacts", http.MethodGet)
    getArtifactsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		artifactsJsonStr := fmt.Sprintf(`[
			{
				"path": "%s",
				"contentType": "%s",
				"size": "%d"
			}
		]`, dummyTxtArtifact.Path, dummyTxtArtifact.ContentType, dummyTxtArtifact.Size)
	
		writer.Write([]byte(artifactsJsonStr))
    }

	goodDownloadInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/files" + dummyTxtArtifact.Path, http.MethodGet)
    goodDownloadInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Disposition", "attachment")
		writer.Write([]byte(dummyTxtArtifact.Path))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
		getArtifactsInteraction,
		goodDownloadInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	mockFileSystem := files.NewOverridableMockFileSystem()

	mockFileSystem.VirtualFunction_MkdirAll = func(path string) error {
		return errors.New("simulating failed folder creation")
	}

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := DownloadArtifacts(runName, forceDownload, mockFileSystem, mockTimeService, mockConsole, commsClient, ".")

	// Then...
	assert.Contains(t, err.Error(), "GAL1041")
}

func TestRunsDownloadExistingFileForceOverwritesMultipleArtifactsToFileSystem(t *testing.T) {
	// Given ...
	runName := "U27"
	runId := "xxx987xxx"
	forceDownload := true

	dummyTxtArtifact := NewMockArtifact("/artifacts/dummy.txt", "text/plain", 1024)
	dummyRunLog := NewMockArtifact("/run.log", "text/plain", 203)
	mockArtifacts := []MockArtifact{
		*dummyTxtArtifact,
		*dummyRunLog,
	}

    // Create the expected HTTP interactions with the API server
    getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
	
		assert.Equal(t, runNameQueryParameter, runName)
		
		writer.Write([]byte(fmt.Sprintf(`
		{
			"pageSize": 1,
			"amountOfRuns": 1,
			"runs":[ %s ]
		}`, RUN_U27V2)))
    }

	getArtifactsInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/artifacts", http.MethodGet)
    getArtifactsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		artifactsBytes, _ := json.Marshal(mockArtifacts)
		writer.Write(artifactsBytes)
    }

	downloadTxtArtifactInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/files" + dummyTxtArtifact.Path, http.MethodGet)
    downloadTxtArtifactInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Disposition", "attachment")
		writer.Write([]byte(dummyTxtArtifact.Path))
    }

	downloadRunLogArtifactInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/files" + dummyRunLog.Path, http.MethodGet)
    downloadRunLogArtifactInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Disposition", "attachment")
		writer.Write([]byte(dummyRunLog.Path))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
		getArtifactsInteraction,
		downloadTxtArtifactInteraction,
		downloadRunLogArtifactInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)
	mockConsole := utils.NewMockConsole()

	mockFileSystem := files.NewMockFileSystem()
	separator := string(os.PathSeparator)
	mockFileSystem.WriteTextFile(runName+separator+"artifacts"+separator+"dummy.txt", "dummy text file")
	mockFileSystem.WriteTextFile(runName+dummyRunLog.Path, "dummy log")

	// When...
	err := DownloadArtifacts(runName, forceDownload, mockFileSystem, mockTimeService, mockConsole, commsClient, ".")

	// Then...
	assert.Nil(t, err)

	downloadedTxtArtifactContents, err := mockFileSystem.ReadTextFile(runName + dummyTxtArtifact.Path)
	assert.Nil(t, err)

	downloadedRunLogContents, err := mockFileSystem.ReadTextFile(runName + dummyRunLog.Path)
	assert.Nil(t, err)

	assert.Equal(t, dummyTxtArtifact.Path, downloadedTxtArtifactContents)
	assert.Equal(t, dummyRunLog.Path, downloadedRunLogContents)

	textGotBack := mockConsole.ReadText()
	assert.Contains(t, textGotBack, runName)
}

func TestRunsDownloadExistingFileNoForceReturnsError(t *testing.T) {
	// Given ...
	runName := "U27"
	runId := "xxx987xxx"
	forceDownload := false

	dummyTxtArtifact := NewMockArtifact("/artifacts/dummy.txt", "text/plain", 1024)
	dummyRunLog := NewMockArtifact("/run.log", "text/plain", 203)
	mockArtifacts := []MockArtifact{
		*dummyTxtArtifact,
		*dummyRunLog,
	}

    // Create the expected HTTP interactions with the API server
    getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
	
		assert.Equal(t, runNameQueryParameter, runName)
		
		writer.Write([]byte(fmt.Sprintf(`
		{
			"pageSize": 1,
			"amountOfRuns": 1,
			"runs":[ %s ]
		}`, RUN_U27V2)))
    }

	getArtifactsInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/artifacts", http.MethodGet)
    getArtifactsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		artifactsBytes, _ := json.Marshal(mockArtifacts)
		writer.Write(artifactsBytes)
    }

	downloadTxtArtifactInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/files" + dummyTxtArtifact.Path, http.MethodGet)
    downloadTxtArtifactInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Disposition", "attachment")
		writer.Write([]byte(dummyTxtArtifact.Path))
    }

	downloadRunLogArtifactInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/files" + dummyRunLog.Path, http.MethodGet)
    downloadRunLogArtifactInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Disposition", "attachment")
		writer.Write([]byte(dummyRunLog.Path))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
		getArtifactsInteraction,
		downloadTxtArtifactInteraction,
		downloadRunLogArtifactInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

	apiServerUrl := server.Server.URL
	mockConsole := utils.NewMockConsole()
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	mockFileSystem := files.NewMockFileSystem()
	separator := string(os.PathSeparator)
	mockFileSystem.WriteTextFile(runName+separator+"dummy.txt", "dummy text file")
	mockFileSystem.WriteTextFile(runName+separator+"run.log", "dummy log")

	// When...
	err := DownloadArtifacts(runName, forceDownload, mockFileSystem, mockTimeService, mockConsole, commsClient, ".")

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1036E")
}

func TestRunsDownloadWritesMultipleArtifactsToFileSystem(t *testing.T) {
	// Given ...
	runName := "U27"
	runId := "xxx987xxx"
	forceDownload := false

	dummyTxtArtifact := NewMockArtifact("/artifacts/dummy.txt", "text/plain", 1024)
	dummyGzArtifact := NewMockArtifact("/artifacts/dummy.gz", "application/x-gzip", 342)
	dummyRunLogArtifact := NewMockArtifact("/run.log", "text/plain", 203)
	mockArtifacts := []MockArtifact{
		*dummyTxtArtifact,
		*dummyGzArtifact,
		*dummyRunLogArtifact,
	}

    // Create the expected HTTP interactions with the API server
    getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
	
		assert.Equal(t, runNameQueryParameter, runName)
		
		writer.Write([]byte(fmt.Sprintf(`
		{
			"pageSize": 1,
			"amountOfRuns": 1,
			"runs":[ %s ]
		}`, RUN_U27V2)))
    }

	getArtifactsInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/artifacts", http.MethodGet)
    getArtifactsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		artifactsBytes, _ := json.Marshal(mockArtifacts)
		writer.Write(artifactsBytes)
    }

	downloadTxtArtifactInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/files" + dummyTxtArtifact.Path, http.MethodGet)
    downloadTxtArtifactInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Disposition", "attachment")
		writer.Write([]byte(dummyTxtArtifact.Path))
    }

	downloadGzipArtifactInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/files" + dummyGzArtifact.Path, http.MethodGet)
    downloadGzipArtifactInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Disposition", "attachment")
		writer.Write([]byte(dummyGzArtifact.Path))
    }

	downloadRunLogArtifactInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/files" + dummyRunLogArtifact.Path, http.MethodGet)
    downloadRunLogArtifactInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Disposition", "attachment")
		writer.Write([]byte(dummyRunLogArtifact.Path))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
		getArtifactsInteraction,
		downloadTxtArtifactInteraction,
		downloadGzipArtifactInteraction,
		downloadRunLogArtifactInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	mockFileSystem := files.NewMockFileSystem()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := DownloadArtifacts(runName, forceDownload, mockFileSystem, mockTimeService, mockConsole, commsClient, ".")

	// Then...
	downloadedTxtArtifactExists, _ := mockFileSystem.Exists(runName + dummyTxtArtifact.Path)
	downloadedGzArtifactExists, _ := mockFileSystem.Exists(runName + dummyGzArtifact.Path)
	downloadedRunLogArtifactExists, _ := mockFileSystem.Exists(runName + dummyRunLogArtifact.Path)

	assert.Nil(t, err)
	assert.True(t, downloadedTxtArtifactExists)
	assert.True(t, downloadedGzArtifactExists)
	assert.True(t, downloadedRunLogArtifactExists)

	textGotBack := mockConsole.ReadText()
	assert.Contains(t, textGotBack, runName)
}

func TestRunsDownloadWritesSingleArtifactToFileSystem(t *testing.T) {
	// Given ...
	runName := "U27"
	runId := "xxx987xxx"
	forceDownload := false

	dummyArtifact := NewMockArtifact("/artifacts/dummy.txt", "text/plain", 1024)

    // Create the expected HTTP interactions with the API server
    getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
	
		assert.Equal(t, runNameQueryParameter, runName)
		
		writer.Write([]byte(fmt.Sprintf(`
		{
			"pageSize": 1,
			"amountOfRuns": 1,
			"runs":[ %s ]
		}`, RUN_U27V2)))
    }

	getArtifactsInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/artifacts", http.MethodGet)
    getArtifactsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		artifactsJsonStr := fmt.Sprintf(`[
			{
				"path": "%s",
				"contentType": "%s",
				"size": "%d"
			}
		]`, dummyArtifact.Path, dummyArtifact.ContentType, dummyArtifact.Size)
	
		writer.Write([]byte(artifactsJsonStr))
    }

	goodDownloadInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/files" + dummyArtifact.Path, http.MethodGet)
    goodDownloadInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Disposition", "attachment")
		writer.Write([]byte(dummyArtifact.Path))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
		getArtifactsInteraction,
		goodDownloadInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	mockFileSystem := files.NewMockFileSystem()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := DownloadArtifacts(runName, forceDownload, mockFileSystem, mockTimeService, mockConsole, commsClient, ".")

	// Then...
	separator := string(os.PathSeparator)
	downloadedArtifactExists, _ := mockFileSystem.Exists(runName + separator + "artifacts" + separator + "dummy.txt")

	assert.Nil(t, err)
	assert.True(t, downloadedArtifactExists)

	textGotBack := mockConsole.ReadText()
	assert.Contains(t, textGotBack, runName)
}

func TestFailingGetFileRequestReturnsError(t *testing.T) {

	// Given...
	runName := "U1"
	runId := "xxx876xxx"
	dummyArtifact := NewMockArtifact("/artifacts/dummy.gz", "application/x-gzip", 30)

    // Create the expected HTTP interactions with the API server
    getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
	
		assert.Equal(t, runNameQueryParameter, runName)
		
		writer.Write([]byte(fmt.Sprintf(`
		{
			"pageSize": 1,
			"amountOfRuns": 1,
			"runs":[ %s ]
		}`, RUN_U1)))
    }

	getArtifactsInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/artifacts", http.MethodGet)
    getArtifactsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		artifactsJsonStr := fmt.Sprintf(`[
			{
				"path": "%s",
				"contentType": "%s",
				"size": "%d"
			}
		]`, dummyArtifact.Path, dummyArtifact.ContentType, dummyArtifact.Size)
	
		writer.Write([]byte(artifactsJsonStr))
    }

	failingDownloadInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/files" + dummyArtifact.Path, http.MethodGet)
    failingDownloadInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		// Make the request to download an artifact fail
		writer.WriteHeader(http.StatusInternalServerError)
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
		getArtifactsInteraction,
		failingDownloadInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)
	mockFileSystem := files.NewMockFileSystem()
	forceDownload := false

	// When...
	err := DownloadArtifacts(runName, forceDownload, mockFileSystem, mockTimeService, mockConsole, commsClient, ".")

	// Then...
	assert.Contains(t, err.Error(), "GAL1074")
}

func TestUnauthorizedGetFileRequestReauthenticatesOk(t *testing.T) {

	// Given...
	runName := "U1"
	runId := "xxx876xxx"
	dummyArtifact := NewMockArtifact("/artifacts/dummy.txt", "text/plain", 1024)

    // Create the expected HTTP interactions with the API server
    getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
	
		assert.Equal(t, runNameQueryParameter, runName)
		
		writer.Write([]byte(fmt.Sprintf(`
		{
			"pageSize": 1,
			"amountOfRuns": 1,
			"runs":[ %s ]
		}`, RUN_U1)))
    }

	getArtifactsInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/artifacts", http.MethodGet)
    getArtifactsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		artifactsJsonStr := fmt.Sprintf(`[
			{
				"path": "%s",
				"contentType": "%s",
				"size": "%d"
			}
		]`, dummyArtifact.Path, dummyArtifact.ContentType, dummyArtifact.Size)
	
		writer.Write([]byte(artifactsJsonStr))
    }

	unauthorizedDownloadInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/files" + dummyArtifact.Path, http.MethodGet)
    unauthorizedDownloadInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		// Make the request to download an artifact fail
		writer.WriteHeader(http.StatusUnauthorized)
    }

	goodDownloadInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/files" + dummyArtifact.Path, http.MethodGet)
    goodDownloadInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Disposition", "attachment")
		writer.Write([]byte(dummyArtifact.Path))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
		getArtifactsInteraction,
		unauthorizedDownloadInteraction,
		goodDownloadInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)
	mockFileSystem := files.NewMockFileSystem()
	forceDownload := false

	// When...
	err := DownloadArtifacts(runName, forceDownload, mockFileSystem, mockTimeService, mockConsole, commsClient, ".")

	// Then...
	assert.Nil(t, err)

	downloadedArtifactContents, err := mockFileSystem.ReadTextFile(runName + dummyArtifact.Path)
	assert.Nil(t, err)

	assert.Equal(t, dummyArtifact.Path, downloadedArtifactContents)

	textGotBack := mockConsole.ReadText()
	assert.Contains(t, textGotBack, runName)
}

func TestFailingGetArtifactsRequestReturnsError(t *testing.T) {

	// Given...
	runName := "U1"
	runId := "xxx876xxx"

    // Create the expected HTTP interactions with the API server
    getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
	
		assert.Equal(t, runNameQueryParameter, runName)
		
		writer.Write([]byte(fmt.Sprintf(`
		{
			"pageSize": 1,
			"amountOfRuns": 1,
			"runs":[ %s ]
		}`, RUN_U1)))
    }

	getArtifactsInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/artifacts", http.MethodGet)
    getArtifactsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		// Make the request to get artifacts fail
		writer.WriteHeader(http.StatusInternalServerError)
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
		getArtifactsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)
	mockFileSystem := files.NewMockFileSystem()
	forceDownload := false

	// When...
	err := DownloadArtifacts(runName, forceDownload, mockFileSystem, mockTimeService, mockConsole, commsClient, ".")

	// Then...
	assert.Contains(t, err.Error(), "GAL1073")
}

func TestRunsDownloadMultipleReRunsWithCorrectOrderFolders(t *testing.T) {
	// Given ...
	runName := "U27"
	runId1 := "xxx543xxx"
	runId2 := "xxx987xxx"

	forceDownload := true
	dummyTxtArtifactRunId1 := NewMockArtifact("/artifacts/dummy1.txt", "text/plain", 1024)
	mockArtifactsRunId1 := []MockArtifact{
		*dummyTxtArtifactRunId1,
	}

	dummyTxtArtifactRunId2 := NewMockArtifact("/artifacts/dummy2.txt", "text/plain", 1024)
	mockArtifactsRunId2 := []MockArtifact{
		*dummyTxtArtifactRunId2,
	}

    // Create the expected HTTP interactions with the API server
    getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
	
		assert.Equal(t, runNameQueryParameter, runName)
		
		writer.Write([]byte(fmt.Sprintf(`
		{
			"pageSize": 1,
			"amountOfRuns": 2,
			"runs":[ %s, %s ]
		}`, RUN_U27, RUN_U27V2)))
    }

	getArtifacts1Interaction := utils.NewHttpInteraction("/ras/runs/" + runId1 + "/artifacts", http.MethodGet)
    getArtifacts1Interaction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		artifactsBytes, _ := json.Marshal(mockArtifactsRunId1)
		writer.Write(artifactsBytes)
    }

	downloadTxt1ArtifactInteraction := utils.NewHttpInteraction("/ras/runs/" + runId1 + "/files" + dummyTxtArtifactRunId1.Path, http.MethodGet)
    downloadTxt1ArtifactInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Disposition", "attachment")
		writer.Write([]byte(dummyTxtArtifactRunId1.Path))
    }

	getArtifacts2Interaction := utils.NewHttpInteraction("/ras/runs/" + runId2 + "/artifacts", http.MethodGet)
    getArtifacts2Interaction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		artifactsBytes, _ := json.Marshal(mockArtifactsRunId2)
		writer.Write(artifactsBytes)
    }

	downloadTxt2ArtifactInteraction := utils.NewHttpInteraction("/ras/runs/" + runId2 + "/files" + dummyTxtArtifactRunId2.Path, http.MethodGet)
    downloadTxt2ArtifactInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Disposition", "attachment")
		writer.Write([]byte(dummyTxtArtifactRunId2.Path))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
		getArtifacts1Interaction,
		downloadTxt1ArtifactInteraction,
		getArtifacts2Interaction,
		downloadTxt2ArtifactInteraction,
    }

    server := utils.NewMockHttpServerWithUnorderedInteractions(t, interactions)
    defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	mockFileSystem := files.NewMockFileSystem()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := DownloadArtifacts(runName, forceDownload, mockFileSystem, mockTimeService, mockConsole, commsClient, ".")

	// Then...
	// U27-1-2023-2023-05-10T06:00:13 	(test did not finish)
	// U27-2 					 		(test finished)
	run1FolderName := runName + "-1-" + mockTimeService.Now().Format("2006-01-02_15:04:05")
	run2FolderName := runName + "-2"

	downloadedTxtArtifactExists1, _ := mockFileSystem.Exists(run1FolderName + dummyTxtArtifactRunId1.Path)
	downloadedTxtArtifactExists2, _ := mockFileSystem.Exists(run2FolderName + dummyTxtArtifactRunId2.Path)
	assert.Nil(t, err)

	assert.True(t, downloadedTxtArtifactExists1)
	assert.True(t, downloadedTxtArtifactExists2)

	textGotBack := mockConsole.ReadText()
	assert.Contains(t, textGotBack, run1FolderName)
	assert.Contains(t, textGotBack, run2FolderName)
}

func TestRunsDownloadMultipleSetsOfUnrelatedReRunsWithCorrectOrderFolders(t *testing.T) {
	// Given ...
	runName := "U27"
	runId1a := "xxx543xxx"
	runId1b := "xxx987xxx"

	runId2a := "xxx1234xxx"
	runId2b := "xxx4321xxx"

	forceDownload := true

	dummyTxtArtifactRunId1a := NewMockArtifact("/artifacts/dummy1a.txt", "text/plain", 1024)
	mockArtifactsRunId1a := []MockArtifact{
		*dummyTxtArtifactRunId1a,
	}

	dummyTxtArtifactRunId1b := NewMockArtifact("/artifacts/dummy1b.txt", "text/plain", 1024)
	mockArtifactsRunId1b := []MockArtifact{
		*dummyTxtArtifactRunId1b,
	}

	dummyTxtArtifactRunId2a := NewMockArtifact("/artifacts/dummy2a.txt", "text/plain", 1024)
	mockArtifactsRunId2a := []MockArtifact{
		*dummyTxtArtifactRunId2a,
	}

	dummyTxtArtifactRunId2b := NewMockArtifact("/artifacts/dummy2b.txt", "text/plain", 1024)
	mockArtifactsRunId2b := []MockArtifact{
		*dummyTxtArtifactRunId2b,
	}

    // Create the expected HTTP interactions with the API server
    getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
	
		assert.Equal(t, runNameQueryParameter, runName)
		
		writer.Write([]byte(fmt.Sprintf(`
		{
			"pageSize": 1,
			"amountOfRuns": 4,
			"runs":[ %s, %s, %s, %s ]
		}`, RUN_U27, RUN_U27V2, RUN_U27V3, RUN_U27V4)))
    }

	getArtifacts1aInteraction := utils.NewHttpInteraction("/ras/runs/" + runId1a + "/artifacts", http.MethodGet)
    getArtifacts1aInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		artifactsBytes, _ := json.Marshal(mockArtifactsRunId1a)
		writer.Write(artifactsBytes)
    }

	downloadTxt1aArtifactInteraction := utils.NewHttpInteraction("/ras/runs/" + runId1a + "/files" + dummyTxtArtifactRunId1a.Path, http.MethodGet)
    downloadTxt1aArtifactInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Disposition", "attachment")
		writer.Write([]byte(dummyTxtArtifactRunId1a.Path))
    }

	getArtifacts1bInteraction := utils.NewHttpInteraction("/ras/runs/" + runId1b + "/artifacts", http.MethodGet)
    getArtifacts1bInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		artifactsBytes, _ := json.Marshal(mockArtifactsRunId1b)
		writer.Write(artifactsBytes)
    }

	downloadTxt1bArtifactInteraction := utils.NewHttpInteraction("/ras/runs/" + runId1b + "/files" + dummyTxtArtifactRunId1b.Path, http.MethodGet)
    downloadTxt1bArtifactInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Disposition", "attachment")
		writer.Write([]byte(dummyTxtArtifactRunId1b.Path))
    }

	getArtifacts2aInteraction := utils.NewHttpInteraction("/ras/runs/" + runId2a + "/artifacts", http.MethodGet)
    getArtifacts2aInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		artifactsBytes, _ := json.Marshal(mockArtifactsRunId2a)
		writer.Write(artifactsBytes)
    }

	downloadTxt2aArtifactInteraction := utils.NewHttpInteraction("/ras/runs/" + runId2a + "/files" + dummyTxtArtifactRunId2a.Path, http.MethodGet)
    downloadTxt2aArtifactInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Disposition", "attachment")
		writer.Write([]byte(dummyTxtArtifactRunId2a.Path))
    }

	getArtifacts2bInteraction := utils.NewHttpInteraction("/ras/runs/" + runId2b + "/artifacts", http.MethodGet)
    getArtifacts2bInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		artifactsBytes, _ := json.Marshal(mockArtifactsRunId2b)
		writer.Write(artifactsBytes)
    }

	downloadTxt2bArtifactInteraction := utils.NewHttpInteraction("/ras/runs/" + runId2b + "/files" + dummyTxtArtifactRunId2b.Path, http.MethodGet)
    downloadTxt2bArtifactInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Disposition", "attachment")
		writer.Write([]byte(dummyTxtArtifactRunId2b.Path))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
		getArtifacts1aInteraction,
		downloadTxt1aArtifactInteraction,
		getArtifacts1bInteraction,
		downloadTxt1bArtifactInteraction,
		getArtifacts2aInteraction,
		downloadTxt2aArtifactInteraction,
		getArtifacts2bInteraction,
		downloadTxt2bArtifactInteraction,
    }

    server := utils.NewMockHttpServerWithUnorderedInteractions(t, interactions)
    defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	mockFileSystem := files.NewMockFileSystem()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)
	mockTimeService.AdvanceClock(time.Second)

	// When...
	err := DownloadArtifacts(runName, forceDownload, mockFileSystem, mockTimeService, mockConsole, commsClient, ".")

	// Then...
	// U27-1-2023-05-10T06:00:13 	(test did not finish)
	// U27-2 					 	(test finished)
	// U27-1-2022-05-10T04:00:13 	(test did not finish)
	// U27-2-2022-05-10T04:00:13    (test did not finish)
	run1aFolderName := runName + "-1-" + mockTimeService.Now().Format("2006-01-02_15:04:05")
	run1bFolderName := runName + "-2"
	run2aFolderName := runName + "-1"
	run2bFolderName := runName + "-2"

	downloadedTxtArtifactExists1a, _ := mockFileSystem.Exists(runName + "-1-" + mockTimeService.Now().Format("2006-01-02_15:04:05") + dummyTxtArtifactRunId1a.Path)
	downloadedTxtArtifactExists1b, _ := mockFileSystem.Exists(runName + "-2" + dummyTxtArtifactRunId1b.Path)

	downloadedTxtArtifactExists2a, _ := mockFileSystem.Exists(runName + "-1" + dummyTxtArtifactRunId2a.Path)
	downloadedTxtArtifactExists2b, _ := mockFileSystem.Exists(runName + "-2" + dummyTxtArtifactRunId2b.Path)

	assert.Nil(t, err)

	assert.True(t, downloadedTxtArtifactExists1a)
	assert.True(t, downloadedTxtArtifactExists1b)
	assert.True(t, downloadedTxtArtifactExists2a)
	assert.True(t, downloadedTxtArtifactExists2b)

	textGotBack := mockConsole.ReadText()
	assert.Contains(t, textGotBack, run1aFolderName)
	assert.Contains(t, textGotBack, run1bFolderName)
	assert.Contains(t, textGotBack, run2aFolderName)
	assert.Contains(t, textGotBack, run2bFolderName)
}

func TestRunsDownloadWithValidRunNameNoArtifacts(t *testing.T) {
	// Given ...
	runName := "U27"

	forceDownload := true

    // Create the expected HTTP interactions with the API server
    getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
	
		assert.Equal(t, runNameQueryParameter, runName)
		
		writer.Write([]byte(`
		{
			"pageSize": 1,
			"amountOfRuns": 0,
			"runs": []
		}`))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	mockFileSystem := files.NewMockFileSystem()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := DownloadArtifacts(runName, forceDownload, mockFileSystem, mockTimeService, mockConsole, commsClient, ".")
	// Then...

	assert.Contains(t, err.Error(), "GAL1083E")
	assert.Contains(t, err.Error(), runName)
	assert.Contains(t, err.Error(), "No artifacts")

}

func TestRunsDownloadWithInvalidRunName(t *testing.T) {
	// Given ...
	runName := "garbage"

	forceDownload := true

	mockConsole := utils.NewMockConsole()
	mockFileSystem := files.NewMockFileSystem()

	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient("api-server-url")

	// When...
	err := DownloadArtifacts(runName, forceDownload, mockFileSystem, mockTimeService, mockConsole, commsClient, ".")

	// Then...

	assert.Contains(t, err.Error(), "GAL1075E")
	assert.Contains(t, err.Error(), "garbage")

}

func TestRunsDownloadAddsTimestampToFolderIfRunNotFinished(t *testing.T) {
	// Given ...
	runName := "U27"
	runId := "xxx2468xxx"

	forceDownload := true
	dummyTxtArtifact := NewMockArtifact("/artifacts/dummy1.txt", "text/plain", 1024)
	mockArtifactsRunId := []MockArtifact{
		*dummyTxtArtifact,
	}

    // Create the expected HTTP interactions with the API server
    getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
	
		assert.Equal(t, runNameQueryParameter, runName)
		
		writer.Write([]byte(fmt.Sprintf(`
		{
			"pageSize": 1,
			"amountOfRuns": 1,
			"runs":[ %s ]
		}`, RUN_U27V5)))
    }

	getArtifactsInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/artifacts", http.MethodGet)
    getArtifactsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		artifactsBytes, _ := json.Marshal(mockArtifactsRunId)
		writer.Write(artifactsBytes)
    }

	goodDownloadInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/files" + dummyTxtArtifact.Path, http.MethodGet)
    goodDownloadInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Disposition", "attachment")
		writer.Write([]byte(dummyTxtArtifact.Path))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
		getArtifactsInteraction,
		goodDownloadInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	mockFileSystem := files.NewMockFileSystem()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := DownloadArtifacts(runName, forceDownload, mockFileSystem, mockTimeService, mockConsole, commsClient, ".")

	// Then...
	run1FolderName := runName + "-" + mockTimeService.Now().Format("2006-01-02_15:04:05")

	downloadedTxtArtifactExists1, _ := mockFileSystem.Exists(run1FolderName + dummyTxtArtifact.Path)

	assert.Nil(t, err)

	assert.True(t, downloadedTxtArtifactExists1)

	textGotBack := mockConsole.ReadText()
	assert.Contains(t, textGotBack, run1FolderName)
}

func TestRunsDownloadWritesSingleArtifactToDestinationFolder(t *testing.T) {
	// Given ...
	runName := "U27"
	runId := "xxx987xxx"
	forceDownload := false

	dummyArtifact := NewMockArtifact("/artifacts/dummy.txt", "text/plain", 1024)

    // Create the expected HTTP interactions with the API server
    getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
	
		assert.Equal(t, runNameQueryParameter, runName)
		
		writer.Write([]byte(fmt.Sprintf(`
		{
			"pageSize": 1,
			"amountOfRuns": 1,
			"runs":[ %s ]
		}`, RUN_U27V2)))
    }

	getArtifactsInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/artifacts", http.MethodGet)
    getArtifactsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")

		artifactsJsonStr := fmt.Sprintf(`[
			{
				"path": "%s",
				"contentType": "%s",
				"size": "%d"
			}
		]`, dummyArtifact.Path, dummyArtifact.ContentType, dummyArtifact.Size)
	
		writer.Write([]byte(artifactsJsonStr))
    }

	goodDownloadInteraction := utils.NewHttpInteraction("/ras/runs/" + runId + "/files" + dummyArtifact.Path, http.MethodGet)
    goodDownloadInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Disposition", "attachment")
		writer.Write([]byte(dummyArtifact.Path))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
		getArtifactsInteraction,
		goodDownloadInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	mockFileSystem := files.NewMockFileSystem()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := DownloadArtifacts(runName, forceDownload, mockFileSystem, mockTimeService, mockConsole, commsClient, "/myfolder")

	// Then...
	downloadedArtifactExists, _ := mockFileSystem.Exists("/myfolder/" + runName + dummyArtifact.Path)

	assert.Nil(t, err)
	assert.True(t, downloadedArtifactExists)

	textGotBack := mockConsole.ReadText()
	assert.Contains(t, textGotBack, "GAL2501I")
	assert.Contains(t, textGotBack, "/myfolder/"+runName)
}
