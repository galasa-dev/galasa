/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package monitors

import (
    "encoding/json"
    "fmt"
    "net/http"
    "strconv"
    "testing"

    "github.com/galasa-dev/cli/pkg/api"
    "github.com/galasa-dev/cli/pkg/galasaapi"
    "github.com/galasa-dev/cli/pkg/utils"
    "github.com/stretchr/testify/assert"
)

const (
    API_VERSION = "galasa-dev/v1alpha1"
)

func createMockGalasaMonitor(monitorName string, description string) galasaapi.GalasaMonitor {
    monitor := *galasaapi.NewGalasaMonitor()

    monitor.SetApiVersion(API_VERSION)
    monitor.SetKind("GalasaResourceCleanupMonitor")

    monitorMetadata := *galasaapi.NewGalasaMonitorMetadata()
    monitorMetadata.SetName(monitorName)

    if description != "" {
        monitorMetadata.SetDescription(description)
    }

    monitorData := *galasaapi.NewGalasaMonitorData()
    monitorData.SetIsEnabled(true)

    monitorCleanupData := *galasaapi.NewGalasaMonitorDataResourceCleanupData()
    monitorCleanupData.SetStream("myStream")
    
    monitorFilters := *galasaapi.NewGalasaMonitorDataResourceCleanupDataFilters()
    monitorFilters.SetIncludes([]string{ "dev.galasa.*", "*myMonitorClass" })
    monitorFilters.SetExcludes([]string{ "exclude.me", "*exclude.me.too.*" })

    monitorCleanupData.SetFilters(monitorFilters)
    monitorData.SetResourceCleanupData(monitorCleanupData)

    monitor.SetMetadata(monitorMetadata)
    monitor.SetData(monitorData)
    return monitor
}

func generateExpectedMonitorYaml(monitorName string, description string, monitorKind string) string {
    return fmt.Sprintf(`apiVersion: %s
kind: %s
metadata:
    name: %s
    description: %s
data:
    isEnabled: true
    resourceCleanupData:
        stream: myStream
        filters:
            includes:
                - dev.galasa.*
                - '*myMonitorClass'
            excludes:
                - exclude.me
                - '*exclude.me.too.*'`, API_VERSION, monitorKind, monitorName, description)
}

func TestCanGetAMonitorByName(t *testing.T) {
    // Given...
    monitorName := "customManagerCleanup"
    description := "my custom cleanup monitor"
    outputFormat := "summary"

    // Create the mock monitor to return
    monitor := createMockGalasaMonitor(monitorName, description)
    monitorBytes, _ := json.Marshal(monitor)
    monitorJson := string(monitorBytes)

    // Create the expected HTTP interactions with the API server
    getMonitorInteraction := utils.NewHttpInteraction("/monitors/" + monitorName, http.MethodGet)
    getMonitorInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        writer.Header().Set("Content-Type", "application/json")
        writer.WriteHeader(http.StatusOK)
        writer.Write([]byte(monitorJson))
    }

    interactions := []utils.HttpInteraction{
        getMonitorInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    apiClient := api.InitialiseAPI(apiServerUrl)
    mockByteReader := utils.NewMockByteReader()

    // When...
    err := GetMonitors(
        monitorName,
        outputFormat,
        console,
        apiClient,
        mockByteReader)

    // Then...
    expectedOutput :=
`name                 kind                         is-enabled
customManagerCleanup GalasaResourceCleanupMonitor true

Total:1
`
    assert.Nil(t, err, "GetMonitors returned an unexpected error")
    assert.Equal(t, expectedOutput, console.ReadText())
}

func TestCanGetAMonitorByNameInYamlFormat(t *testing.T) {
    // Given...
    monitorName := "cleanupMonitor"
    description := "my custom cleanup monitor"
    outputFormat := "yaml"

    // Create the mock monitor to return
    monitor := createMockGalasaMonitor(monitorName, description)
    monitorBytes, _ := json.Marshal(monitor)
    monitorJson := string(monitorBytes)

    // Create the expected HTTP interactions with the API server
    getMonitorInteraction := utils.NewHttpInteraction("/monitors/" + monitorName, http.MethodGet)
    getMonitorInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        writer.Header().Set("Content-Type", "application/json")
        writer.WriteHeader(http.StatusOK)
        writer.Write([]byte(monitorJson))
    }

    interactions := []utils.HttpInteraction{
        getMonitorInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    apiClient := api.InitialiseAPI(apiServerUrl)
    mockByteReader := utils.NewMockByteReader()

    // When...
    err := GetMonitors(
        monitorName,
        outputFormat,
        console,
        apiClient,
        mockByteReader)

    // Then...
    expectedOutput := generateExpectedMonitorYaml(monitorName, description, "GalasaResourceCleanupMonitor") + "\n"
    assert.Nil(t, err, "GetMonitors returned an unexpected error")
    assert.Equal(t, expectedOutput, console.ReadText())
}

func TestGetAMonitorWithBlankNameDisplaysError(t *testing.T) {
    // Given...
    monitorName := "    "
    outputFormat := "summary"

    // The client-side validation should fail, so no HTTP interactions will be performed
    interactions := []utils.HttpInteraction{}

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    apiClient := api.InitialiseAPI(apiServerUrl)
    mockByteReader := utils.NewMockByteReader()

    // When...
    err := GetMonitors(
        monitorName,
        outputFormat,
        console,
        apiClient,
        mockByteReader)

    // Then...
    assert.NotNil(t, err, "GetMonitors did not return an error as expected")
    consoleOutputText := err.Error()
    assert.Contains(t, consoleOutputText, "GAL1225E")
    assert.Contains(t, consoleOutputText, " Invalid monitor name provided")
}

func TestGetNonExistantMonitorDisplaysError(t *testing.T) {
    // Given...
    nonExistantMonitor := "monitorDoesNotExist123"
    outputFormat := "summary"

    // Create the expected HTTP interactions with the API server
    getMonitorInteraction := utils.NewHttpInteraction("/monitors/" + nonExistantMonitor, http.MethodGet)
    getMonitorInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        writer.Header().Set("Content-Type", "application/json")
        writer.WriteHeader(http.StatusNotFound)
        writer.Write([]byte(`{ "error_message": "No such monitor exists" }`))
    }


    interactions := []utils.HttpInteraction{ getMonitorInteraction }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    apiClient := api.InitialiseAPI(apiServerUrl)
    mockByteReader := utils.NewMockByteReader()

    // When...
    err := GetMonitors(
        nonExistantMonitor,
        outputFormat,
        console,
        apiClient,
        mockByteReader)

    // Then...
    assert.NotNil(t, err, "MonitorsGet did not return an error but it should have")
    consoleOutputText := err.Error()
    assert.Contains(t, consoleOutputText, nonExistantMonitor)
    assert.Contains(t, consoleOutputText, "GAL1224E")
}

func TestCanGetAllMonitorsOk(t *testing.T) {
    // Given...
    // Don't provide a monitor name so that we can get all monitors
    monitorName := ""
    outputFormat := "summary"

    // Create the mock monitor to return
    monitors := make([]galasaapi.GalasaMonitor, 0)
    monitor1Name := "monitor1"
    monitor2Name := "monitor2"
    description1 := "my first cleanup monitor"
    description2 := "my other cleanup monitor"
    monitor1 := createMockGalasaMonitor(monitor1Name, description1)
    monitor2 := createMockGalasaMonitor(monitor2Name, description2)

    monitors = append(monitors, monitor1, monitor2)
    monitorsBytes, _ := json.Marshal(monitors)
    monitorsJson := string(monitorsBytes)

    // Create the expected HTTP interactions with the API server
    getMonitorInteraction := utils.NewHttpInteraction("/monitors", http.MethodGet)
    getMonitorInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        writer.Header().Set("Content-Type", "application/json")
        writer.WriteHeader(http.StatusOK)
        writer.Write([]byte(monitorsJson))
    }

    interactions := []utils.HttpInteraction{
        getMonitorInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    apiClient := api.InitialiseAPI(apiServerUrl)
    mockByteReader := utils.NewMockByteReader()

    // When...
    err := GetMonitors(
        monitorName,
        outputFormat,
        console,
        apiClient,
        mockByteReader)

    // Then...
    expectedOutput :=
`name     kind                         is-enabled
monitor1 GalasaResourceCleanupMonitor true
monitor2 GalasaResourceCleanupMonitor true

Total:2
`
    assert.Nil(t, err, "GetMonitors returned an unexpected error")
    assert.Equal(t, expectedOutput, console.ReadText())
}

func TestGetMonitorsWithUnknownFormatDisplaysError(t *testing.T) {
    // Given...
    monitorName := ""
    outputFormat := "UNKNOWN FORMAT!"

    // The client-side validation should fail, so no HTTP interactions will be performed
    interactions := []utils.HttpInteraction{}

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    apiClient := api.InitialiseAPI(apiServerUrl)
    mockByteReader := utils.NewMockByteReader()

    // When...
    err := GetMonitors(
        monitorName,
        outputFormat,
        console,
        apiClient,
        mockByteReader)

    // Then...
    assert.NotNil(t, err, "GetMonitors did not return an error as expected")
    consoleOutputText := err.Error()
    assert.Contains(t, consoleOutputText, "GAL1067E")
    assert.Contains(t, consoleOutputText, "Unsupported value 'UNKNOWN FORMAT!'")
    assert.Contains(t, consoleOutputText, "'summary', 'yaml'")
}

func TestGetAllMonitorsFailsWithNoExplanationErrorPayloadGivesCorrectMessage(t *testing.T) {
    // Given...
    monitorName := ""
    outputFormat := "summary"

    // Create the expected HTTP interactions with the API server
    getMonitorInteraction := utils.NewHttpInteraction("/monitors", http.MethodGet)
    getMonitorInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        writer.WriteHeader(http.StatusInternalServerError)
    }

    interactions := []utils.HttpInteraction{
        getMonitorInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    apiClient := api.InitialiseAPI(apiServerUrl)
    mockByteReader := utils.NewMockByteReader()

    // When...
    err := GetMonitors(
        monitorName,
        outputFormat,
        console,
        apiClient,
        mockByteReader)

    // Then...
    assert.NotNil(t, err, "MonitorsGet did not return an error but it should have")
    errorMsg := err.Error()
    assert.Contains(t, errorMsg , "GAL1219E")
}

func TestGetAllMonitorsFailsWithNonJsonContentTypeExplanationErrorPayloadGivesCorrectMessage(t *testing.T) {
    // Given...
    monitorName := ""
    outputFormat := "summary"

    // Create the expected HTTP interactions with the API server
    getMonitorInteraction := utils.NewHttpInteraction("/monitors", http.MethodGet)
    getMonitorInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        writer.WriteHeader(http.StatusInternalServerError)
        writer.Header().Set("Content-Type", "application/notJsonOnPurpose")
        writer.Write([]byte("something not json but non-zero-length."))
    }

    interactions := []utils.HttpInteraction{
        getMonitorInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    apiClient := api.InitialiseAPI(apiServerUrl)
    mockByteReader := utils.NewMockByteReader()

    // When...
    err := GetMonitors(
        monitorName,
        outputFormat,
        console,
        apiClient,
        mockByteReader)

    // Then...
    assert.NotNil(t, err, "MonitorsGet did not return an error but it should have")
    errorMsg := err.Error()
    assert.Contains(t, errorMsg, strconv.Itoa(http.StatusInternalServerError))
    assert.Contains(t, errorMsg, "GAL1223E")
    assert.Contains(t, errorMsg, "Error details from the server are not in the json format")
}

func TestGetAllMonitorsFailsWithBadlyFormedJsonContentExplanationErrorPayloadGivesCorrectMessage(t *testing.T) {
    // Given...
    monitorName := ""
    outputFormat := "summary"

    // Create the expected HTTP interactions with the API server
    getMonitorInteraction := utils.NewHttpInteraction("/monitors", http.MethodGet)
    getMonitorInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        writer.Header().Set("Content-Type", "application/json")
        writer.WriteHeader(http.StatusInternalServerError)
        writer.Write([]byte(`{ "this": "isBadJson because it doesnt end in a close braces" `))
    }

    interactions := []utils.HttpInteraction{
        getMonitorInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    apiClient := api.InitialiseAPI(apiServerUrl)
    mockByteReader := utils.NewMockByteReader()

    // When...
    err := GetMonitors(
        monitorName,
        outputFormat,
        console,
        apiClient,
        mockByteReader)

    // Then...
    assert.NotNil(t, err, "MonitorsGet did not return an error but it should have")
    errorMsg := err.Error()
    assert.Contains(t, errorMsg, strconv.Itoa(http.StatusInternalServerError))
    assert.Contains(t, errorMsg, "GAL1221E")
    assert.Contains(t, errorMsg, "Error details from the server are not in a valid json format")
    assert.Contains(t, errorMsg, "Cause: 'unexpected end of JSON input'")
}

func TestGetAllMonitorsFailsWithFailureToReadResponseBodyGivesCorrectMessage(t *testing.T) {
    // Given...
    monitorName := ""
    outputFormat := "summary"

    // Create the expected HTTP interactions with the API server
    getMonitorInteraction := utils.NewHttpInteraction("/monitors", http.MethodGet)
    getMonitorInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        writer.Header().Set("Content-Type", "application/json")
        writer.WriteHeader(http.StatusInternalServerError)
        writer.Write([]byte(`{}`))
    }

    interactions := []utils.HttpInteraction{
        getMonitorInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
    defer server.Server.Close()

    console := utils.NewMockConsole()
    apiServerUrl := server.Server.URL
    apiClient := api.InitialiseAPI(apiServerUrl)
    mockByteReader := utils.NewMockByteReaderAsMock(true)

    // When...
    err := GetMonitors(
        monitorName,
        outputFormat,
        console,
        apiClient,
        mockByteReader)

    // Then...
    assert.NotNil(t, err, "MonitorsGet returned an unexpected error")
    errorMsg := err.Error()
    assert.Contains(t, errorMsg, strconv.Itoa(http.StatusInternalServerError))
    assert.Contains(t, errorMsg, "GAL1220E")
    assert.Contains(t, errorMsg, "Error details from the server could not be read")
}
