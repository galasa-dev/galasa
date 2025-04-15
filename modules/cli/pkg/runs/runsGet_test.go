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
	"net/url"
	"strings"
	"testing"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/runsformatter"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

const (
	RUN_U456 = `{
		 "runId": "xxx876xxx",
		 "testStructure": {
			 "runName": "U456",
			 "bundle": "myBundleId",
			 "testName": "myTestPackage.MyTestName",
			 "testShortName": "MyTestName",
			 "requestor": "unitTesting",
			 "status": "Finished",
			 "result": "Passed",
			 "group": "dummyGroup",
			 "queued" : "2023-05-10T06:00:13.043037Z",
			 "startTime": "2023-05-10T06:00:36.159003Z",
			 "endTime": "2023-05-10T06:02:53.823338Z",
			 "methods": [{
				 "className": "myTestPackage.MyTestName",
				 "methodName": "myTestMethodName",
				 "type": "test",
				 "status": "Done",
				 "result": "Success",
				 "startTime": "2023-05-10T06:00:13.254335Z",
				 "endTime": "2023-05-10T06:03:11.882739Z",
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

	RUN_U456_v2 = `{
		 "runId": "xxx543xxx",
		 "testStructure": {
			 "runName": "U456",
			 "bundle": "myBun2",
			 "testName": "myTestPackage.MyTest2",
			 "testShortName": "MyTestName22",
			 "requestor": "unitTesting22",
			 "status" : "Finished",
			 "result" : "LongResultString",
			 "queued" : "2023-05-10T06:00:13.405966Z",
			 "startTime": "2023-05-10T06:02:26.801649Z",
			 "endTime": "2023-05-10T06:04:04.448826Z",
			 "methods": [{
				 "className": "myTestPackage22.MyTestName2",
				 "methodName": "myTestMethodName",
				 "type": "test",
				 "status": "Done",
				 "result": "UNKNOWN",
				 "startTime": "2023-05-10T06:02:28.457784Z",
				 "endTime": "2023-05-10T06:04:28.585024Z",
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

	EMPTY_RUNS_RESPONSE = `
		{
			"pageSize": 1,
			"amountOfRuns": 0,
			"runs":[]
		}`
)

func NewRunsGetServletMock(t *testing.T, status int, nextPageCursors []string, pages map[string][]string, pageSize int, runName string, runResultStrings ...string) *httptest.Server {

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		clientVersion := r.Header.Get("ClientApiVersion")
		assert.NotEmpty(t, clientVersion, "Client version header not set.")
		if strings.Contains(r.URL.Path, "/ras/runs/") {
			ConfigureServerForDetailsEndpoint(t, w, r, status, runResultStrings...)
		} else if strings.Contains(r.URL.Path, "/ras/resultnames") {
			ConfigureServerForResultNamesEndpoint(t, w, r, status)
		} else {
			nextCursor := ""
			if len(nextPageCursors) > 0 {
				// Advance the expected page cursors by one
				nextCursor = nextPageCursors[0]
				nextPageCursors = nextPageCursors[1:]
			}
			ConfigureServerForRasRunsEndpoint(t, w, r, pages, nextCursor, runName, pageSize, status)
		}
	}))
	return server
}

func ConfigureServerForDetailsEndpoint(t *testing.T, w http.ResponseWriter, r *http.Request, status int, runResultStrings ...string) {
	if r.Header.Get("Accept") != "application/json" {
		t.Errorf("Expected Accept: application/json header, got: %s", r.Header.Get("Accept"))
	}
	urlParts := strings.Split(r.URL.Path, "/")
	runid := urlParts[3]
	for _, runResult := range runResultStrings {
		assert.Contains(t, runResult, runid)
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	combinedRunResultStrings := ""
	for index, runResult := range runResultStrings {
		if index > 0 {
			combinedRunResultStrings += ","
		}
		combinedRunResultStrings += runResult
	}

	w.Write([]byte(fmt.Sprintf(`
			%s
		`, combinedRunResultStrings)))
}

func ConfigureServerForRasRunsEndpoint(
	t *testing.T,
	w http.ResponseWriter,
	r *http.Request,
	pages map[string][]string,
	nextPageCursor string,
	runName string,
	pageSize int,
	status int,
) {
	if r.URL.Path != "/ras/runs" {
		t.Errorf("Expected to request '/ras/runs', got: %s", r.URL.Path)
	}
	if r.Header.Get("Accept") != "application/json" {
		t.Errorf("Expected Accept: application/json header, got: %s", r.Header.Get("Accept"))
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)

	values := r.URL.Query()
	runNameQueryParameter := values.Get("runname")

	var pageRunsJson []string
	var keyExists bool
	cursorQueryParameter := values.Get("cursor")

	// Keys of the pages map correspond to page cursors, including
	// an empty string key for the first request to /ras/runs
	pageRunsJson, keyExists = pages[cursorQueryParameter]
	assert.True(t, keyExists)

	// Subsequent requests shouldn't be made to the same page,
	// so delete the page since we've visited it
	delete(pages, cursorQueryParameter)

	assert.Equal(t, runNameQueryParameter, runName)
	combinedRunResultStrings := ""
	for index, runResult := range pageRunsJson {
		if index > 0 {
			combinedRunResultStrings += ","
		}
		combinedRunResultStrings += runResult
	}

	w.Write([]byte(fmt.Sprintf(`
		 {
			 "nextCursor": "%s",
			 "pageSize": %d,
			 "amountOfRuns": %d,
			 "runs":[ %s ]
		 }`, nextPageCursor, pageSize, len(pageRunsJson), combinedRunResultStrings)))
}

func ConfigureServerForResultNamesEndpoint(t *testing.T, w http.ResponseWriter, r *http.Request, status int) {
	if r.URL.Path != "/ras/resultnames" {
		t.Errorf("Expected to request '/ras/resultnames', got: %s", r.URL.Path)
	}
	if r.Header.Get("Accept") != "application/json" {
		t.Errorf("Expected Accept: application/json header, got: %s", r.Header.Get("Accept"))
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)

	w.Write([]byte(`
			{
				"resultnames":["UNKNOWN","Passed","Failed","EnvFail"]
			}
	`))

}

// ------------------------------------------------------------------
// Testing that the output format string passed by the user on the command-line
// is valid and supported.
func TestOutputFormatSummaryValidatesOk(t *testing.T) {
	validFormatters := CreateFormatters()
	outputFormatter, err := validateOutputFormatFlagValue("summary", validFormatters)
	if err != nil {
		assert.Fail(t, "Summary validate gave unexpected error "+err.Error())
	}
	assert.NotNil(t, outputFormatter)
}

func TestOutputFormatGarbageStringValidationGivesError(t *testing.T) {
	validFormatters := CreateFormatters()
	_, err := validateOutputFormatFlagValue("garbage", validFormatters)
	if err == nil {
		assert.Fail(t, "Garbage output format flag value should have given validation error.")
	}
	assert.Contains(t, err.Error(), "GAL1067")
	assert.Contains(t, err.Error(), "'garbage'")
	assert.Contains(t, err.Error(), "'summary'")
	assert.Contains(t, err.Error(), "'details'")
	assert.Contains(t, err.Error(), "'raw'")
}

func TestRunsGetOfRunNameWhichExistsProducesExpectedSummary(t *testing.T) {

	// Given ...
	pages := make(map[string][]string, 0)
	pages[""] = []string{RUN_U456}
	nextPageCursors := []string{""}

	runName := "U456"
	age := "2d:24h"
	requestor := ""
	result := ""
	pageSize := 100
	group := ""

	server := NewRunsGetServletMock(t, http.StatusOK, nextPageCursors, pages, pageSize, runName)
	shouldGetActive := false
	defer server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then...
	// We expect
	if err != nil {
		assert.Fail(t, "Failed with an error when we expected it to pass. Error is "+err.Error())
	} else {
		textGotBack := mockConsole.ReadText()
		assert.Contains(t, textGotBack, runName)
		want :=
			"submitted-time(UTC) name requestor   status   result test-name                group\n" +
				"2023-05-10 06:00:13 U456 unitTesting Finished Passed myTestPackage.MyTestName dummyGroup\n" +
				"\n" +
				"Total:1 Passed:1\n"
		assert.Equal(t, want, textGotBack)
	}
}

func TestRunsGetOfRunNameWhichDoesNotExistProducesError(t *testing.T) {
	// Given ...
	age := "2d:24h"
	runName := "garbage"
	pages := make(map[string][]string, 0)
	pages[""] = []string{RUN_U456}
	nextPageCursors := []string{""}
	requestor := ""
	result := ""
	shouldGetActive := false
	group := ""
	pageSize := 100

	server := NewRunsGetServletMock(t, http.StatusOK, nextPageCursors, pages, pageSize, runName)
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	outputFormat := "summary"
	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then...
	// We expect

	assert.NotNil(t, err, "Garbage runname value should not have failed.")
	if err != nil {
		assert.ErrorContains(t, err, "GAL1075E")
		assert.ErrorContains(t, err, runName)
	}
}

func TestRunsGetWhereRunNameExistsTwiceProducesTwoRunResultLines(t *testing.T) {
	// Given ...
	pages := make(map[string][]string, 0)
	pages[""] = []string{RUN_U456, RUN_U456_v2}
	nextPageCursors := []string{""}
	age := ""
	runName := "U456"
	requestor := ""
	result := ""
	shouldGetActive := false
	pageSize := 100
	group := ""

	server := NewRunsGetServletMock(t, http.StatusOK, nextPageCursors, pages, pageSize, runName)
	defer server.Close()

	mockConsole := utils.NewMockConsole()
	outputFormat := "summary"
	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then...
	// We expect
	if err != nil {
		assert.Fail(t, "Failed with an error when we expected it to pass. Error is "+err.Error())
	} else {
		textGotBack := mockConsole.ReadText()
		assert.Contains(t, textGotBack, runName)
		want :=
			"submitted-time(UTC) name requestor     status   result           test-name                group\n" +
				"2023-05-10 06:00:13 U456 unitTesting   Finished Passed           myTestPackage.MyTestName dummyGroup\n" +
				"2023-05-10 06:00:13 U456 unitTesting22 Finished LongResultString myTestPackage.MyTest2    \n" +
				"\n" +
				"Total:2 Passed:1\n"
		assert.Equal(t, textGotBack, want)
	}
}

func TestFailingGetRunsRequestReturnsError(t *testing.T) {

	// Given...
	server := httptest.NewServer(http.HandlerFunc(func(writer http.ResponseWriter, req *http.Request) {
		writer.WriteHeader(http.StatusInternalServerError)
	}))
	defer server.Close()

	group := ""
	age := ""
	runName := "garbage"
	requestor := ""
	result := ""
	shouldGetActive := false

	mockConsole := utils.NewMockConsole()
	outputFormat := "summary"
	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then...
	assert.Contains(t, err.Error(), "GAL1075")
}

func TestOutputFormatDetailsValidatesOk(t *testing.T) {
	validFormatters := CreateFormatters()
	outputFormatter, err := validateOutputFormatFlagValue("details", validFormatters)
	if err != nil {
		assert.Fail(t, "Details validate gave unexpected error "+err.Error())
	}
	assert.NotNil(t, outputFormatter)
}

func TestRunsGetOfRunNameWhichExistsProducesExpectedDetails(t *testing.T) {

	// Given ...
	pages := make(map[string][]string, 0)
	pages[""] = []string{RUN_U456}
	nextPageCursors := []string{""}
	age := ""
	runName := "U456"
	requestor := ""
	result := ""
	shouldGetActive := false
	group := ""
	pageSize := 100

	server := NewRunsGetServletMock(t, http.StatusOK, nextPageCursors, pages, pageSize, runName, RUN_U456)
	defer server.Close()

	outputFormat := "details"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then...
	// We expect
	if err != nil {
		assert.Fail(t, "Failed with an error when we expected it to pass. Error is "+err.Error())
	} else {
		textGotBack := mockConsole.ReadText()
		assert.Contains(t, textGotBack, runName)
		want :=
			"name                : U456\n" +
				"status              : Finished\n" +
				"result              : Passed\n" +
				"submitted-time(UTC) : 2023-05-10 06:00:13\n" +
				"start-time(UTC)     : 2023-05-10 06:00:36\n" +
				"end-time(UTC)       : 2023-05-10 06:02:53\n" +
				"duration(ms)        : 137664\n" +
				"test-name           : myTestPackage.MyTestName\n" +
				"requestor           : unitTesting\n" +
				"bundle              : myBundleId\n" +
				"group               : dummyGroup\n" +
				"run-log             : " + apiServerUrl + "/ras/runs/xxx876xxx/runlog\n" +
				"\n" +
				"method           type status result  start-time(UTC)     end-time(UTC)       duration(ms)\n" +
				"myTestMethodName test Done   Success 2023-05-10 06:00:13 2023-05-10 06:03:11 178628\n" +
				"\n" +
				"Total:1 Passed:1\n"
		assert.Equal(t, textGotBack, want)
	}
}

func TestGetFormatterNamesStringMultipleFormattersFormatsOk(t *testing.T) {
	validFormatters := make(map[string]runsformatter.RunsFormatter, 0)
	validFormatters["first"] = nil
	validFormatters["second"] = nil

	result := GetFormatterNamesString(validFormatters)

	assert.NotNil(t, result)
	assert.Equal(t, result, "'first', 'second'")
}

func TestAPIInternalErrorIsHandledOk(t *testing.T) {
	// Given ...
	pages := make(map[string][]string, 0)
	pages[""] = []string{RUN_U456}
	nextPageCursors := []string{""}
	group := ""
	age := ""
	runName := "U456"
	requestor := ""
	result := ""
	shouldGetActive := false
	pageSize := 100

	server := NewRunsGetServletMock(t, http.StatusInternalServerError, nextPageCursors, pages, pageSize, runName)
	defer server.Close()

	outputFormat := "details"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then...
	// We expect
	assert.Error(t, err)
	assert.ErrorContains(t, err, "500")
	assert.ErrorContains(t, err, "GAL1068")
}

func TestRunsGetOfRunNameWhichExistsProducesExpectedRaw(t *testing.T) {

	// Given ...
	pages := make(map[string][]string, 0)
	pages[""] = []string{RUN_U456}
	nextPageCursors := []string{""}
	age := ""
	runName := "U456"
	requestor := ""
	result := ""
	shouldGetActive := false
	pageSize := 100
	group := ""

	server := NewRunsGetServletMock(t, http.StatusOK, nextPageCursors, pages, pageSize, runName)
	defer server.Close()

	outputFormat := "raw"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then...
	// We expect
	assert.Nil(t, err)
	textGotBack := mockConsole.ReadText()
	assert.Contains(t, textGotBack, runName)
	want := "U456|Finished|Passed|2023-05-10T06:00:13.043037Z|2023-05-10T06:00:36.159003Z|2023-05-10T06:02:53.823338Z|137664|myTestPackage.MyTestName|unitTesting|myBundleId|dummyGroup|" + apiServerUrl + "/ras/runs/xxx876xxx/runlog\n"
	assert.Equal(t, textGotBack, want)
}

func TestRunsGetWithFromAndToAge(t *testing.T) {

	// Given ...
	age := "5d:12h"

	//When ...
	from, to, err := getTimesFromAge(age)

	// Then...
	// We expect
	// from = 5*1440 = 7200
	// to   = 12*60 = 720
	assert.Nil(t, err)
	assert.NotNil(t, from)
	assert.NotNil(t, to)
	assert.EqualValues(t, 7200, from)
	assert.EqualValues(t, 720, to)
}

func TestRunsGetWithJustFromAge(t *testing.T) {

	// Given
	age := "20m"

	// When
	from, to, err := getTimesFromAge(age)

	// Then...
	// We expect
	// from = 20
	// to not provided = 0
	assert.Nil(t, err)
	assert.NotNil(t, from)
	assert.NotNil(t, to)
	assert.EqualValues(t, 20, from)
	assert.EqualValues(t, 0, to)
}

func TestRunsGetWithNoRunNameAndNoFromAgeReturnsError(t *testing.T) {

	// Given
	age := "0h"

	// When
	_, _, err := getTimesFromAge(age)

	// Then...
	// We expect
	assert.Error(t, err)
	assert.ErrorContains(t, err, "GAL1078")
	assert.Contains(t, err.Error(), "'w'")
	assert.Contains(t, err.Error(), "'d'")
	assert.Contains(t, err.Error(), "'h'")
	assert.Contains(t, err.Error(), "'m'")
	assert.Contains(t, err.Error(), "(weeks)")
	assert.Contains(t, err.Error(), "(days)")
	assert.Contains(t, err.Error(), "(hours)")
	assert.Contains(t, err.Error(), "(minutes)")
}

func TestRunsGetWithBadlyFormedFromAndToParameter(t *testing.T) {

	// Given
	age := "1y:1s"

	// When
	_, _, err := getTimesFromAge(age)

	// Then...
	// We expect
	assert.Error(t, err)
	assert.ErrorContains(t, err, "GAL1078")
	assert.Contains(t, err.Error(), "'w'")
	assert.Contains(t, err.Error(), "'d'")
	assert.Contains(t, err.Error(), "'h'")
	assert.Contains(t, err.Error(), "'m'")
	assert.Contains(t, err.Error(), "(weeks)")
	assert.Contains(t, err.Error(), "(days)")
	assert.Contains(t, err.Error(), "(hours)")
	assert.Contains(t, err.Error(), "(minutes)")
}

func TestRunsGetWithOlderToAgeThanFromAge(t *testing.T) {

	// Given
	age := "1d:3d"

	// When
	_, _, err := getTimesFromAge(age)

	// Then...
	// We expect
	assert.Error(t, err)
	assert.ErrorContains(t, err, "GAL1077")
}

func TestRunsGetURLQueryWithFromAndToDate(t *testing.T) {
	// Given ...
	age := "5d:12h"
	runName := "U456"
	requestor := ""
	result := ""
	shouldGetActive := false
	group := ""

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		query := r.URL.Query()
		assert.NotNil(t, query.Get("from"))
		assert.NotEqualValues(t, query.Get("from"), "")
		assert.NotNil(t, query.Get("to"))
		assert.NotEqualValues(t, query.Get("to"), "")
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(200)
		w.Write([]byte(EMPTY_RUNS_RESPONSE))
	}))
	defer server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.Nil(t, err)
}

func TestRunsGetURLQueryJustFromAge(t *testing.T) {
	// Given ...
	age := "2d"
	runName := ""
	requestor := ""
	result := ""
	shouldGetActive := false
	group := ""

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		query := r.URL.Query()
		assert.NotNil(t, query.Get("from"))
		assert.NotEqualValues(t, query.Get("from"), "")
		assert.EqualValues(t, query.Get("to"), "")
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(200)
		w.Write([]byte(EMPTY_RUNS_RESPONSE))
	}))
	defer server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.Nil(t, err)
}

func TestRunsGetURLQueryWithNoRunNameAndNoFromAgeReturnsError(t *testing.T) {
	// Given ...
	age := ""
	runName := ""
	requestor := ""
	result := ""
	shouldGetActive := false
	group := ""

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		query := r.URL.Query()
		assert.EqualValues(t, query.Get("from"), "")
		assert.EqualValues(t, query.Get("to"), "")
		assert.EqualValues(t, query.Get("runname"), "")
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(200)
		w.Write([]byte(EMPTY_RUNS_RESPONSE))
	}))
	defer server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "1079")
}

func TestRunsGetURLQueryWithOlderToAgeThanFromAgeReturnsError(t *testing.T) {
	// Given ...
	age := "1d:1w"
	runName := "U456"
	requestor := ""
	result := ""
	shouldGetActive := false
	group := ""

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		query := r.URL.Query()
		assert.EqualValues(t, query.Get("from"), "")
		assert.EqualValues(t, query.Get("to"), "")
		assert.EqualValues(t, query.Get("runname"), "U456")
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(200)
		w.Write([]byte(EMPTY_RUNS_RESPONSE))
	}))
	defer server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "1077")
}

func TestRunsGetURLQueryWithBadlyFormedFromAndToParameterReturnsError(t *testing.T) {
	// Given ...
	age := "1y:1s"
	runName := "U456"
	requestor := ""
	result := ""
	shouldGetActive := false
	group := ""

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		query := r.URL.Query()
		assert.EqualValues(t, query.Get("from"), "")
		assert.EqualValues(t, query.Get("to"), "")
		assert.EqualValues(t, query.Get("runname"), "U456")
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(200)
		w.Write([]byte(EMPTY_RUNS_RESPONSE))
	}))
	defer server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1078")
	assert.Contains(t, err.Error(), "'w'")
	assert.Contains(t, err.Error(), "'d'")
	assert.Contains(t, err.Error(), "'h'")
	assert.Contains(t, err.Error(), "'m'")
	assert.Contains(t, err.Error(), "(weeks)")
	assert.Contains(t, err.Error(), "(days)")
	assert.Contains(t, err.Error(), "(hours)")
	assert.Contains(t, err.Error(), "(minutes)")
}

// Fine-grained tests for validating and extracting age parameter values.age
func TestAgeWithMissingColonGivesError(t *testing.T) {

	_, _, err := getTimesFromAge("3d2d")

	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1078")
	assert.Contains(t, err.Error(), "'w'")
	assert.Contains(t, err.Error(), "'d'")
	assert.Contains(t, err.Error(), "'h'")
	assert.Contains(t, err.Error(), "'m'")
	assert.Contains(t, err.Error(), "(weeks)")
	assert.Contains(t, err.Error(), "(days)")
	assert.Contains(t, err.Error(), "(hours)")
	assert.Contains(t, err.Error(), "(minutes)")
}

func TestAgeWithTwoColonGivesError(t *testing.T) {

	_, _, err := getTimesFromAge("3d::2d")

	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1078")
	assert.Contains(t, err.Error(), "'w'")
	assert.Contains(t, err.Error(), "'d'")
	assert.Contains(t, err.Error(), "'h'")
	assert.Contains(t, err.Error(), "'m'")
	assert.Contains(t, err.Error(), "(weeks)")
	assert.Contains(t, err.Error(), "(days)")
	assert.Contains(t, err.Error(), "(hours)")
	assert.Contains(t, err.Error(), "(minutes)")
}

func TestAgeWithExtraColonAfterToPartGivesError(t *testing.T) {

	_, _, err := getTimesFromAge("3d:2d:")

	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1078")
	assert.Contains(t, err.Error(), "'w'")
	assert.Contains(t, err.Error(), "'d'")
	assert.Contains(t, err.Error(), "'h'")
	assert.Contains(t, err.Error(), "'m'")
	assert.Contains(t, err.Error(), "(weeks)")
	assert.Contains(t, err.Error(), "(days)")
	assert.Contains(t, err.Error(), "(hours)")
	assert.Contains(t, err.Error(), "(minutes)")
}

func TestAgeWithExtraGarbageAfterToPartGivesError(t *testing.T) {

	_, _, err := getTimesFromAge("3d:2dgarbage")

	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1082")
	assert.Contains(t, err.Error(), "'w'")
	assert.Contains(t, err.Error(), "'d'")
	assert.Contains(t, err.Error(), "'h'")
	assert.Contains(t, err.Error(), "'m'")
	assert.Contains(t, err.Error(), "(weeks)")
	assert.Contains(t, err.Error(), "(days)")
	assert.Contains(t, err.Error(), "(hours)")
	assert.Contains(t, err.Error(), "(minutes)")
}

func TestAgeWithZeroFromGivesError(t *testing.T) {

	_, _, err := getTimesFromAge("0d")

	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1078")
	assert.Contains(t, err.Error(), "'w'")
	assert.Contains(t, err.Error(), "'d'")
	assert.Contains(t, err.Error(), "'h'")
	assert.Contains(t, err.Error(), "'m'")
	assert.Contains(t, err.Error(), "(weeks)")
	assert.Contains(t, err.Error(), "(days)")
	assert.Contains(t, err.Error(), "(hours)")
	assert.Contains(t, err.Error(), "(minutes)")
}

func TestAgeWithZeroToIsOk(t *testing.T) {

	_, _, err := getTimesFromAge("1d:0d")

	assert.Nil(t, err)
}

func TestAgeWithSameFromAndToGivesError(t *testing.T) {

	_, _, err := getTimesFromAge("1d:1d")

	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1077")
}

func TestAgeWithMinutesUnitReturnsOk(t *testing.T) {

	_, _, err := getTimesFromAge("10m")

	assert.Nil(t, err)
}

func TestAgeWithSameFromAndToDurationGivesError(t *testing.T) {

	_, _, err := getTimesFromAge("1d:24h")

	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1077")
}

func TestAgeWithNegativeFromGivesError(t *testing.T) {

	_, _, err := getTimesFromAge("-1d")

	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1078")
	assert.Contains(t, err.Error(), "'w'")
	assert.Contains(t, err.Error(), "'d'")
	assert.Contains(t, err.Error(), "'h'")
	assert.Contains(t, err.Error(), "'m'")
	assert.Contains(t, err.Error(), "(weeks)")
	assert.Contains(t, err.Error(), "(days)")
	assert.Contains(t, err.Error(), "(hours)")
	assert.Contains(t, err.Error(), "(minutes)")
}

func TestAgeWithHugeNumberGivesError(t *testing.T) {

	_, _, err := getTimesFromAge("12375612351237651273512376512765123d")

	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "GAL1078")
	assert.Contains(t, err.Error(), "'w'")
	assert.Contains(t, err.Error(), "'d'")
	assert.Contains(t, err.Error(), "'h'")
	assert.Contains(t, err.Error(), "'m'")
	assert.Contains(t, err.Error(), "(weeks)")
	assert.Contains(t, err.Error(), "(days)")
	assert.Contains(t, err.Error(), "(hours)")
	assert.Contains(t, err.Error(), "(minutes)")
}

func TestRunsGetURLQueryWithRequestorNotSuppliedReturnsOK(t *testing.T) {
	// Given ...
	age := ""
	runName := "U456"
	requestor := ""
	result := ""
	shouldGetActive := false
	group := ""

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		query := r.URL.Query()
		assert.EqualValues(t, query.Get("from"), "")
		assert.EqualValues(t, query.Get("to"), "")
		assert.EqualValues(t, query.Get("runname"), runName)

		// The request should not have the requestor parameter
		assert.NotContains(t, r.URL.RawQuery, "requestor")

		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(200)
		w.Write([]byte(EMPTY_RUNS_RESPONSE))
	}))
	defer server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.Nil(t, err)
}

func TestRunsGetURLQueryWithRequestorSuppliedReturnsOK(t *testing.T) {
	// Given ...
	age := ""
	runName := "U456"
	requestor := "User123"
	result := ""
	shouldGetActive := false
	group := ""

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		query := r.URL.Query()
		assert.EqualValues(t, query.Get("from"), "")
		assert.EqualValues(t, query.Get("to"), "")
		assert.EqualValues(t, query.Get("runname"), runName)
		assert.Contains(t, r.URL.RawQuery, "requestor="+url.QueryEscape(requestor))
		assert.EqualValues(t, query.Get("requestor"), requestor)
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(200)
		w.Write([]byte(EMPTY_RUNS_RESPONSE))
	}))
	defer server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.Nil(t, err)
}

func TestRunsGetURLQueryWithNumericRequestorSuppliedReturnsOK(t *testing.T) {
	// Given ...
	age := ""
	runName := "U456"
	requestor := "9876543210"
	result := ""
	shouldGetActive := false
	group := ""

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		query := r.URL.Query()
		assert.EqualValues(t, query.Get("from"), "")
		assert.EqualValues(t, query.Get("to"), "")
		assert.EqualValues(t, query.Get("runname"), runName)
		assert.EqualValues(t, query.Get("requestor"), requestor)
		assert.Contains(t, r.URL.RawQuery, "requestor="+url.QueryEscape(requestor))
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(200)
		w.Write([]byte(EMPTY_RUNS_RESPONSE))
	}))
	defer server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.Nil(t, err)
}

func TestRunsGetURLQueryWithDashInRequestorSuppliedReturnsOK(t *testing.T) {
	// Given ...
	age := ""
	runName := "U456"
	requestor := "User-123"
	result := ""
	shouldGetActive := false
	group := ""

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		query := r.URL.Query()
		assert.EqualValues(t, query.Get("from"), "")
		assert.EqualValues(t, query.Get("to"), "")
		assert.EqualValues(t, query.Get("runname"), runName)
		assert.EqualValues(t, query.Get("requestor"), requestor)
		assert.Contains(t, r.URL.RawQuery, "requestor="+url.QueryEscape(requestor))
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(200)
		w.Write([]byte(EMPTY_RUNS_RESPONSE))
	}))
	defer server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.Nil(t, err)
}

func TestRunsGetURLQueryWithAmpersandRequestorSuppliedReturnsOK(t *testing.T) {
	// Given ...
	age := ""
	runName := "U456"
	requestor := "User&123"
	result := ""
	shouldGetActive := false
	group := ""

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		query := r.URL.Query()
		assert.EqualValues(t, query.Get("from"), "")
		assert.EqualValues(t, query.Get("to"), "")
		assert.EqualValues(t, query.Get("runname"), runName)
		assert.EqualValues(t, query.Get("requestor"), requestor)
		assert.Contains(t, r.URL.RawQuery, "requestor="+url.QueryEscape(requestor))
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(200)
		w.Write([]byte(EMPTY_RUNS_RESPONSE))
	}))
	defer server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.Nil(t, err)
}

func TestRunsGetURLQueryWithSpecialCharactersRequestorSuppliedReturnsOK(t *testing.T) {
	// Given ...
	age := ""
	runName := "U456"
	requestor := "User&!@£$%^&*(){}#/',."
	result := ""
	shouldGetActive := false
	group := ""

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		query := r.URL.Query()
		assert.EqualValues(t, query.Get("from"), "")
		assert.EqualValues(t, query.Get("to"), "")
		assert.EqualValues(t, query.Get("runname"), runName)
		assert.EqualValues(t, query.Get("requestor"), requestor)
		assert.Contains(t, r.URL.RawQuery, "requestor="+url.QueryEscape(requestor))

		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(200)
		w.Write([]byte(EMPTY_RUNS_RESPONSE))
	}))
	defer server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.Nil(t, err)
}

func TestRunsGetURLQueryWithResultSuppliedReturnsOK(t *testing.T) {
	// Given ...
	pages := make(map[string][]string, 0)
	pages[""] = []string{RUN_U456}
	nextPageCursors := []string{""}
	age := ""
	runName := "U456"
	requestor := ""
	result := "Passed"
	shouldGetActive := false
	pageSize := 100
	group := ""

	server := NewRunsGetServletMock(t, http.StatusOK, nextPageCursors, pages, pageSize, runName)
	defer server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.Nil(t, err)
	textGotBack := mockConsole.ReadText()
	assert.Contains(t, textGotBack, "Passed")
}

func TestRunsGetURLQueryWithMultipleResultSuppliedReturnsOK(t *testing.T) {
	// Given ...
	pages := make(map[string][]string, 0)
	pages[""] = []string{RUN_U456}
	nextPageCursors := []string{""}
	age := ""
	runName := "U456"
	requestor := ""
	result := "Passed,envfail"
	shouldGetActive := false
	pageSize := 100
	group := ""

	server := NewRunsGetServletMock(t, http.StatusOK, nextPageCursors, pages, pageSize, runName)
	defer server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...

	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.Nil(t, err)
	textGotBack := mockConsole.ReadText()
	assert.Contains(t, textGotBack, "Passed")
}

func TestRunsGetURLQueryWithResultNotSuppliedReturnsOK(t *testing.T) {
	// Given ...
	pages := make(map[string][]string, 0)
	pages[""] = []string{RUN_U456}
	nextPageCursors := []string{""}
	age := ""
	runName := "U456"
	requestor := ""
	result := ""
	shouldGetActive := false
	group := ""
	pageSize := 100

	server := NewRunsGetServletMock(t, http.StatusOK, nextPageCursors, pages, pageSize, runName)
	defer server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.Nil(t, err)
}

func TestRunsGetURLQueryWithInvalidResultSuppliedReturnsError(t *testing.T) {
	// Given ...
	pages := make(map[string][]string, 0)
	pages[""] = []string{RUN_U456}
	nextPageCursors := []string{""}
	age := ""
	runName := "U456"
	requestor := ""
	result := "garbage"
	shouldGetActive := false
	pageSize := 100
	group := ""

	server := NewRunsGetServletMock(t, http.StatusOK, nextPageCursors, pages, pageSize, runName)
	defer server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.Error(t, err)
	assert.ErrorContains(t, err, "GAL1087E")
	assert.ErrorContains(t, err, result)
}

func TestActiveAndResultAreMutuallyExclusiveShouldReturnError(t *testing.T) {
	// Given ...
	pages := make(map[string][]string, 0)
	pages[""] = []string{RUN_U456}
	nextPageCursors := []string{""}
	age := ""
	runName := "U456"
	requestor := ""
	result := "Passed"
	shouldGetActive := true
	pageSize := 100
	group := ""

	server := NewRunsGetServletMock(t, http.StatusOK, nextPageCursors, pages, pageSize, runName)
	defer server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.Error(t, err)
	assert.ErrorContains(t, err, "GAL1088E")
}

func TestActiveParameterReturnsOk(t *testing.T) {
	// Given ...
	pages := make(map[string][]string, 0)
	pages[""] = []string{RUN_U456}
	nextPageCursors := []string{""}
	age := ""
	runName := "U456"
	requestor := ""
	result := ""
	shouldGetActive := true
	pageSize := 100
	group := ""

	server := NewRunsGetServletMock(t, http.StatusOK, nextPageCursors, pages, pageSize, runName)
	defer server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.Nil(t, err)
}

func TestRunsGetActiveRunsBuildsQueryCorrectly(t *testing.T) {
	// Given ...
	age := ""
	runName := "U456"
	requestor := "tester"
	result := ""
	shouldGetActive := true
	group := ""

	mockEnv := utils.NewMockEnv()
	mockEnv.SetUserName(requestor)

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		query := r.URL.Query()
		assert.EqualValues(t, query.Get("from"), "")
		assert.EqualValues(t, query.Get("to"), "")
		assert.EqualValues(t, query.Get("runname"), runName)
		assert.EqualValues(t, query.Get("requestor"), requestor)
		assert.NotContains(t, r.URL.RawQuery, "status="+url.QueryEscape("finished"))
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(200)
		w.Write([]byte(EMPTY_RUNS_RESPONSE))
	}))
	defer server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.Nil(t, err)
}

func TestRunsGetWithNextCursorGetsNextPageOfRuns(t *testing.T) {

	// Given ...
	pages := make(map[string][]string, 0)
	pages[""] = []string{RUN_U456}
	pages["page2"] = []string{RUN_U456}
	pages["page3"] = []string{}
	nextPageCursors := []string{"page2", "page3"}

	age := ""
	runName := "U456"
	requestor := ""
	result := ""
	shouldGetActive := false
	pageSize := 1
	group := ""

	server := NewRunsGetServletMock(t, http.StatusOK, nextPageCursors, pages, pageSize, runName)
	defer server.Close()

	outputFormat := "raw"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then...
	assert.Nil(t, err)
	runsReturned := mockConsole.ReadText()
	assert.Contains(t, runsReturned, runName)

	run := "U456|Finished|Passed|2023-05-10T06:00:13.043037Z|2023-05-10T06:00:36.159003Z|2023-05-10T06:02:53.823338Z|137664|myTestPackage.MyTestName|unitTesting|myBundleId|dummyGroup|" + apiServerUrl + "/ras/runs/xxx876xxx/runlog\n"
	expectedResults := run + run
	assert.Equal(t, runsReturned, expectedResults)
}

func TestRunsGetOfGroupWhichExistsProducesExpectedRaw(t *testing.T) {

	// Given ...
	pages := make(map[string][]string, 0)
	pages[""] = []string{RUN_U456}
	nextPageCursors := []string{""}
	age := ""
	runName := "U456"
	requestor := ""
	result := ""
	shouldGetActive := false
	pageSize := 100
	group := "dummyGroup"

	server := NewRunsGetServletMock(t, http.StatusOK, nextPageCursors, pages, pageSize, runName)
	defer server.Close()

	outputFormat := "raw"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then...
	// We expect
	assert.Nil(t, err)
	textGotBack := mockConsole.ReadText()
	assert.Contains(t, textGotBack, runName)
	want := "U456|Finished|Passed|2023-05-10T06:00:13.043037Z|2023-05-10T06:00:36.159003Z|2023-05-10T06:02:53.823338Z|137664|myTestPackage.MyTestName|unitTesting|myBundleId|dummyGroup|" + apiServerUrl + "/ras/runs/xxx876xxx/runlog\n"
	assert.Equal(t, textGotBack, want)
}

func TestRunsGetWithBadGroupNameThrowsError(t *testing.T) {

	// Given ...
	pages := make(map[string][]string, 0)
	pages[""] = []string{}
	nextPageCursors := []string{ "" }

	runName := "U457"
	age := ""
	requestor := ""
	result := ""
	shouldGetActive := false
	pageSize := 100
	outputFormat := "raw"

	group := string(rune(300)) + "NONLATIN1"

	server := NewRunsGetServletMock(t, http.StatusOK, nextPageCursors, pages, pageSize, runName)
	defer server.Close()

	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
    commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, mockTimeService, mockConsole, commsClient)

	// Then...
	assert.NotNil(t, err, "A non-Latin-1 group name should throw an error")
	assert.ErrorContains(t, err, "GAL1105E")
	assert.ErrorContains(t, err, "Invalid group name provided")
}