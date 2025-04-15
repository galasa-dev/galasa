/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"fmt"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

func WriteMockRasRunsPutStatusQueuedResponse(
	t *testing.T,
	writer http.ResponseWriter,
	req *http.Request,
	runName string) {

	var statusCode int
	var response string

	if runName == "U123" {
		statusCode = 202
		response = fmt.Sprintf("The request to reset run %s has been received.", runName)
		writer.Header().Set("Content-Type", "text/plain")
	} else if runName == "U120" {
		statusCode = 400
		response = `{
			"error_code": 5049, 
			"error_message": "GAL5049E: Error occured when trying to reset the run 'U120'. The run has already completed."
		}`
		writer.Header().Set("Content-Type", "application/json")
	} else if runName == "U121" {
		statusCode = 400
		response = `{{
			not for parsing
		}`
		writer.Header().Set("Content-Type", "application/json")
	}

	writer.WriteHeader(statusCode)
	writer.Write([]byte(response))
}

func NewRunsResetServletMock(
	t *testing.T,
	runName string,
	runId string,
	runResultStrings []string,
) *httptest.Server {

	server := httptest.NewServer(http.HandlerFunc(func(writer http.ResponseWriter, req *http.Request) {

		assert.NotEmpty(t, req.Header.Get("ClientApiVersion"))
		acceptHeader := req.Header.Get("Accept")
		if req.URL.Path == "/ras/runs" {
			assert.Equal(t, "application/json", acceptHeader, "Expected Accept: application/json header, got: %s", acceptHeader)
			WriteMockRasRunsResponse(t, writer, req, runName, runResultStrings)
		} else if req.URL.Path == "/ras/runs/"+runId {
			assert.Equal(t, "application/json", acceptHeader, "Expected Accept: application/json header, got: %s", acceptHeader)
			WriteMockRasRunsPutStatusQueuedResponse(t, writer, req, runName)
		}
	}))

	return server
}

//------------------------------------------------------------------
// Test methods
//------------------------------------------------------------------

func TestRunsResetWithOneActiveRunReturnsOK(t *testing.T) {
	// Given ...
	runName := "U123"
	runId := "xxx123xxx"

	runResultStrings := []string{RUN_U123_RE_RUN}

	server := NewRunsResetServletMock(t, runName, runId, runResultStrings)
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := ResetRun(runName, mockTimeService, mockConsole, commsClient)

	// Then...
	assert.Nil(t, err)
	textGotBack := mockConsole.ReadText()
	assert.Contains(t, textGotBack, "GAL2503I")
	assert.Contains(t, textGotBack, runName)
}

func TestRunsResetWithMultipleActiveRunsReturnsOK(t *testing.T) {
	// Given ...
	runName := "U123"
	runId := "xxx122xxx"

	runResultStrings := []string{RUN_U123_FIRST_RUN, RUN_U123_RE_RUN, RUN_U123_RE_RUN_2}

	server := NewRunsResetServletMock(t, runName, runId, runResultStrings)
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := ResetRun(runName, mockTimeService, mockConsole, commsClient)

	// Then...
	assert.Nil(t, err)
	textGotBack := mockConsole.ReadText()
	assert.Contains(t, textGotBack, "GAL2503I")
	assert.Contains(t, textGotBack, runName)
}

func TestRunsResetWithNoActiveRunReturnsError(t *testing.T) {
	// Given ...
	runName := "U123"
	runId := "xxx123xxx"

	runResultStrings := []string{}

	server := NewRunsResetServletMock(t, runName, runId, runResultStrings)
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := ResetRun(runName, mockTimeService, mockConsole, commsClient)

	// Then...
	assert.Contains(t, err.Error(), "GAL1132")
	assert.Contains(t, err.Error(), runName)
}

func TestRunsResetWithInvalidRunNameReturnsError(t *testing.T) {
	// Given ...
	runName := "garbage"
	runId := "xxx123xxx"

	runResultStrings := []string{RUN_U123_RE_RUN}

	server := NewRunsResetServletMock(t, runName, runId, runResultStrings)
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := ResetRun(runName, mockTimeService, mockConsole, commsClient)

	// Then...
	assert.Contains(t, err.Error(), "GAL1075")
	assert.Contains(t, err.Error(), runName)
}

func TestRunsResetWhereOperationFailedServerSideReturnsError(t *testing.T) {
	// Given ...
	runName := "U120"
	runId := "xxx120xxx"

	runResultStrings := []string{RUN_U120}

	server := NewRunsResetServletMock(t, runName, runId, runResultStrings)
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := ResetRun(runName, mockTimeService, mockConsole, commsClient)

	// Then...
	assert.Error(t, err)
	assert.ErrorContains(t, err, "GAL1133")
}

func TestRunsResetWhereServerSideResponseCannotBeParsedReturnsError(t *testing.T) {
	// Given ...
	runName := "U121"
	runId := "xxx121xxx"

	runResultStrings := []string{RUN_U121}

	server := NewRunsResetServletMock(t, runName, runId, runResultStrings)
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := ResetRun(runName, mockTimeService, mockConsole, commsClient)

	// Then...
	assert.Error(t, err)
	assert.ErrorContains(t, err, "GAL1134")
}
