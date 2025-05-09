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
			 }],
			 "tags": [
			 	"core",
				"anothertag"
			 ]
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

	RESULT_NAMES_RESPONSE = `
		{
			"resultnames":["UNKNOWN","Passed","Failed","EnvFail"]
		}`
)

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
	runName := "U456"
	age := "2d:24h"
	requestor := ""
	result := ""
	group := ""
	tags := make([]string, 0)
	shouldGetActive := false

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
	
		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
		assert.Equal(t, runNameQueryParameter, runName)

		writer.Write([]byte(fmt.Sprintf(`
			{
				"nextCursor": "",
				"pageSize": 100,
				"amountOfRuns": 1,
				"runs":[ %s ]
			}`, RUN_U456)))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

	// Then...
	// We expect
	if err != nil {
		assert.Fail(t, "Failed with an error when we expected it to pass. Error is "+err.Error())
	} else {
		textGotBack := mockConsole.ReadText()
		assert.Contains(t, textGotBack, runName)
		want :=
			"submitted-time(UTC) name requestor   status   result test-name                group      tags\n" +
				"2023-05-10 06:00:13 U456 unitTesting Finished Passed myTestPackage.MyTestName dummyGroup \n" +
				"\n" +
				"Total:1 Passed:1\n"
		assert.Equal(t, want, textGotBack)
	}
}

func TestRunsGetOfRunNameWhichDoesNotExistProducesError(t *testing.T) {
	// Given ...
	age := "2d:24h"
	runName := "garbage"
	requestor := ""
	result := ""
	shouldGetActive := false
	group := ""
	tags := make([]string, 0)

    interactions := []utils.HttpInteraction{}

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	mockConsole := utils.NewMockConsole()

	outputFormat := "summary"
	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

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
	age := ""
	runName := "U456"
	requestor := ""
	result := ""
	shouldGetActive := false
	group := ""
	tags := make([]string, 0)

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
	
		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
		assert.Equal(t, runNameQueryParameter, runName)

		runsToReturn := []string{RUN_U456, RUN_U456_v2}

		writer.Write([]byte(fmt.Sprintf(`
			{
				"nextCursor": "",
				"pageSize": 100,
				"amountOfRuns": 2,
				"runs":[ %s ]
			}`, strings.Join(runsToReturn, ","))))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	mockConsole := utils.NewMockConsole()
	outputFormat := "summary"
	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

	// Then...
	// We expect
	if err != nil {
		assert.Fail(t, "Failed with an error when we expected it to pass. Error is "+err.Error())
	} else {
		textGotBack := mockConsole.ReadText()
		assert.Contains(t, textGotBack, runName)
		want :=
			"submitted-time(UTC) name requestor     status   result           test-name                group      tags\n" +
				"2023-05-10 06:00:13 U456 unitTesting   Finished Passed           myTestPackage.MyTestName dummyGroup \n" +
				"2023-05-10 06:00:13 U456 unitTesting22 Finished LongResultString myTestPackage.MyTest2               anothertag,core\n" +
				"\n" +
				"Total:2 Passed:1\n"
		assert.Equal(t, want, textGotBack)
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
	tags := make([]string, 0)

	mockConsole := utils.NewMockConsole()
	outputFormat := "summary"
	apiServerUrl := server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

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
	age := ""
	runName := "U456"
	requestor := ""
	result := ""
	shouldGetActive := false
	group := ""
	tags := make([]string, 0)

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
	
		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
		assert.Equal(t, runNameQueryParameter, runName)

		writer.Write([]byte(fmt.Sprintf(`
			{
				"nextCursor": "",
				"pageSize": 100,
				"amountOfRuns": 1,
				"runs":[ %s ]
			}`, RUN_U456)))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "details"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

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
				"tags                : \n" +
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
	group := ""
	age := ""
	runName := "U456"
	requestor := ""
	result := ""
	shouldGetActive := false
	tags := make([]string, 0)

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusInternalServerError)
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "details"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

	// Then...
	// We expect
	assert.Error(t, err)
	assert.ErrorContains(t, err, "500")
	assert.ErrorContains(t, err, "GAL1068")
}

func TestRunsGetOfRunNameWhichExistsProducesExpectedRaw(t *testing.T) {

	// Given ...
	age := ""
	runName := "U456"
	requestor := ""
	result := ""
	shouldGetActive := false
	group := ""
	tags := make([]string, 0)

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
	
		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
		assert.Equal(t, runNameQueryParameter, runName)

		writer.Write([]byte(fmt.Sprintf(`
			{
				"nextCursor": "",
				"pageSize": 100,
				"amountOfRuns": 1,
				"runs":[ %s ]
			}`, RUN_U456)))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "raw"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

	// Then...
	// We expect
	assert.Nil(t, err)
	textGotBack := mockConsole.ReadText()
	assert.Contains(t, textGotBack, runName)
	want := "U456|Finished|Passed|2023-05-10T06:00:13.043037Z|2023-05-10T06:00:36.159003Z|2023-05-10T06:02:53.823338Z|137664|myTestPackage.MyTestName|unitTesting|myBundleId|dummyGroup|" + apiServerUrl + "/ras/runs/xxx876xxx/runlog|\n"
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
	tags := make([]string, 0)

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		query := req.URL.Query()
		assert.NotNil(t, query.Get("from"))
		assert.NotEqualValues(t, query.Get("from"), "")
		assert.NotNil(t, query.Get("to"))
		assert.NotEqualValues(t, query.Get("to"), "")
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(200)
		writer.Write([]byte(EMPTY_RUNS_RESPONSE))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

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
	tags := make([]string, 0)

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		query := req.URL.Query()
		assert.NotNil(t, query.Get("from"))
		assert.NotEqualValues(t, query.Get("from"), "")
		assert.EqualValues(t, query.Get("to"), "")
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(200)
		writer.Write([]byte(EMPTY_RUNS_RESPONSE))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

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
	tags := make([]string, 0)

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		query := req.URL.Query()
		assert.NotNil(t, query.Get("from"))
		assert.NotEqualValues(t, query.Get("from"), "")
		assert.EqualValues(t, query.Get("to"), "")
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(200)
		writer.Write([]byte(EMPTY_RUNS_RESPONSE))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

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
	tags := make([]string, 0)

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		query := req.URL.Query()
		assert.EqualValues(t, query.Get("from"), "")
		assert.EqualValues(t, query.Get("to"), "")
		assert.EqualValues(t, query.Get("runname"), "U456")
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(200)
		writer.Write([]byte(EMPTY_RUNS_RESPONSE))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

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
	tags := make([]string, 0)

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		query := req.URL.Query()
		assert.EqualValues(t, query.Get("from"), "")
		assert.EqualValues(t, query.Get("to"), "")
		assert.EqualValues(t, query.Get("runname"), "U456")
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(200)
		writer.Write([]byte(EMPTY_RUNS_RESPONSE))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

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
	tags := make([]string, 0)

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		query := req.URL.Query()
		assert.EqualValues(t, query.Get("from"), "")
		assert.EqualValues(t, query.Get("to"), "")
		assert.EqualValues(t, query.Get("runname"), runName)

		// The request should not have the requestor parameter
		assert.NotContains(t, req.URL.RawQuery, "requestor")

		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(200)
		writer.Write([]byte(EMPTY_RUNS_RESPONSE))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

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
	tags := make([]string, 0)

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		query := req.URL.Query()
		assert.EqualValues(t, query.Get("from"), "")
		assert.EqualValues(t, query.Get("to"), "")
		assert.EqualValues(t, query.Get("runname"), runName)
		assert.Contains(t, req.URL.RawQuery, "requestor="+url.QueryEscape(requestor))
		assert.EqualValues(t, query.Get("requestor"), requestor)
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(200)
		writer.Write([]byte(EMPTY_RUNS_RESPONSE))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

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
	tags := make([]string, 0)

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		query := req.URL.Query()
		assert.EqualValues(t, query.Get("from"), "")
		assert.EqualValues(t, query.Get("to"), "")
		assert.EqualValues(t, query.Get("runname"), runName)
		assert.EqualValues(t, query.Get("requestor"), requestor)
		assert.Contains(t, req.URL.RawQuery, "requestor="+url.QueryEscape(requestor))
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(200)
		writer.Write([]byte(EMPTY_RUNS_RESPONSE))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

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
	tags := make([]string, 0)

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		query := req.URL.Query()
		assert.EqualValues(t, query.Get("from"), "")
		assert.EqualValues(t, query.Get("to"), "")
		assert.EqualValues(t, query.Get("runname"), runName)
		assert.EqualValues(t, query.Get("requestor"), requestor)
		assert.Contains(t, req.URL.RawQuery, "requestor="+url.QueryEscape(requestor))
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(200)
		writer.Write([]byte(EMPTY_RUNS_RESPONSE))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

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
	tags := make([]string, 0)

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		query := req.URL.Query()
		assert.EqualValues(t, query.Get("from"), "")
		assert.EqualValues(t, query.Get("to"), "")
		assert.EqualValues(t, query.Get("runname"), runName)
		assert.Contains(t, req.URL.RawQuery, "requestor="+url.QueryEscape(requestor))
		assert.EqualValues(t, query.Get("requestor"), requestor)
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(200)
		writer.Write([]byte(EMPTY_RUNS_RESPONSE))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

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
	tags := make([]string, 0)

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		query := req.URL.Query()
		assert.EqualValues(t, query.Get("from"), "")
		assert.EqualValues(t, query.Get("to"), "")
		assert.EqualValues(t, query.Get("runname"), runName)
		assert.EqualValues(t, query.Get("requestor"), requestor)
		assert.Contains(t, req.URL.RawQuery, "requestor="+url.QueryEscape(requestor))

		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(200)
		writer.Write([]byte(EMPTY_RUNS_RESPONSE))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.Nil(t, err)
}

func TestRunsGetURLQueryWithResultSuppliedReturnsOK(t *testing.T) {
	// Given ...
	age := ""
	runName := "U456"
	requestor := ""
	result := "Passed"
	shouldGetActive := false
	group := ""
	tags := make([]string, 0)

	resultNamesInteraction := utils.NewHttpInteraction("/ras/resultnames", http.MethodGet)
    resultNamesInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(RESULT_NAMES_RESPONSE))
    }

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
	
		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
		assert.Equal(t, runNameQueryParameter, runName)

		writer.Write([]byte(fmt.Sprintf(`
			{
				"nextCursor": "",
				"pageSize": 100,
				"amountOfRuns": 1,
				"runs":[ %s ]
			}`, RUN_U456)))
    }

    interactions := []utils.HttpInteraction{
		resultNamesInteraction,
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.Nil(t, err)
	textGotBack := mockConsole.ReadText()
	assert.Contains(t, textGotBack, "Passed")
}

func TestRunsGetURLQueryWithMultipleResultSuppliedReturnsOK(t *testing.T) {
	// Given ...
	age := ""
	runName := "U456"
	requestor := ""
	result := "Passed,envfail"
	shouldGetActive := false
	group := ""
	tags := make([]string, 0)

	resultNamesInteraction := utils.NewHttpInteraction("/ras/resultnames", http.MethodGet)
    resultNamesInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(RESULT_NAMES_RESPONSE))
    }

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
	
		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
		assert.Equal(t, runNameQueryParameter, runName)

		writer.Write([]byte(fmt.Sprintf(`
			{
				"nextCursor": "",
				"pageSize": 100,
				"amountOfRuns": 1,
				"runs":[ %s ]
			}`, RUN_U456)))
    }

    interactions := []utils.HttpInteraction{
		resultNamesInteraction,
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...

	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.Nil(t, err)
	textGotBack := mockConsole.ReadText()
	assert.Contains(t, textGotBack, "Passed")
}

func TestRunsGetURLQueryWithResultNotSuppliedReturnsOK(t *testing.T) {
	// Given ...
	age := ""
	runName := "U456"
	requestor := ""
	result := ""
	shouldGetActive := false
	group := ""
	tags := make([]string, 0)

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
	
		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
		assert.Equal(t, runNameQueryParameter, runName)

		writer.Write([]byte(fmt.Sprintf(`
			{
				"nextCursor": "",
				"pageSize": 100,
				"amountOfRuns": 1,
				"runs":[ %s ]
			}`, RUN_U456)))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.Nil(t, err)
}

func TestRunsGetURLQueryWithInvalidResultSuppliedReturnsError(t *testing.T) {
	// Given ...
	age := ""
	runName := "U456"
	requestor := ""
	result := "garbage"
	shouldGetActive := false
	group := ""
	tags := make([]string, 0)

	resultNamesInteraction := utils.NewHttpInteraction("/ras/resultnames", http.MethodGet)
    resultNamesInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(RESULT_NAMES_RESPONSE))
    }

    interactions := []utils.HttpInteraction{
		resultNamesInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.Error(t, err)
	assert.ErrorContains(t, err, "GAL1087E")
	assert.ErrorContains(t, err, result)
}

func TestActiveAndResultAreMutuallyExclusiveShouldReturnError(t *testing.T) {
	// Given ...
	age := ""
	runName := "U456"
	requestor := ""
	result := "Passed"
	shouldGetActive := true
	group := ""
	tags := make([]string, 0)

    interactions := []utils.HttpInteraction{}

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.Error(t, err)
	assert.ErrorContains(t, err, "GAL1088E")
}

func TestActiveParameterReturnsOk(t *testing.T) {
	// Given ...
	age := ""
	runName := "U456"
	requestor := ""
	result := ""
	shouldGetActive := true
	group := ""
	tags := make([]string, 0)

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
	
		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
		assert.Equal(t, runNameQueryParameter, runName)

		writer.Write([]byte(fmt.Sprintf(`
			{
				"nextCursor": "",
				"pageSize": 100,
				"amountOfRuns": 1,
				"runs":[ %s ]
			}`, RUN_U456)))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

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
	tags := make([]string, 0)

	mockEnv := utils.NewMockEnv()
	mockEnv.SetUserName(requestor)

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		query := req.URL.Query()
		assert.EqualValues(t, query.Get("from"), "")
		assert.EqualValues(t, query.Get("to"), "")
		assert.EqualValues(t, query.Get("runname"), runName)
		assert.EqualValues(t, query.Get("requestor"), requestor)
		assert.NotContains(t, req.URL.RawQuery, "status="+url.QueryEscape("finished"))
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(200)
		writer.Write([]byte(EMPTY_RUNS_RESPONSE))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

	// Then ...
	assert.Nil(t, err)
}

func TestRunsGetWithNextCursorGetsNextPageOfRuns(t *testing.T) {

	// Given ...
	page2Cursor := "page2"
	page3Cursor := "page3"

	age := ""
	runName := "U456"
	requestor := ""
	result := ""
	shouldGetActive := false
	group := ""
	tags := make([]string, 0)

	getRunsInteraction1 := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction1.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
	
		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
		assert.Equal(t, runNameQueryParameter, runName)

		writer.Write([]byte(fmt.Sprintf(`
			{
				"nextCursor": "%s",
				"pageSize": 1,
				"amountOfRuns": 1,
				"runs":[ %s ]
			}`, page2Cursor, RUN_U456)))
    }

	getRunsInteraction2 := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction2.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
	
		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
		assert.Equal(t, runNameQueryParameter, runName)

		writer.Write([]byte(fmt.Sprintf(`
			{
				"nextCursor": "%s",
				"pageSize": 1,
				"amountOfRuns": 1,
				"runs":[ %s ]
			}`, page3Cursor, RUN_U456)))
    }

	getRunsInteraction3 := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction3.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
	
		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
		assert.Equal(t, runNameQueryParameter, runName)

		writer.Write([]byte(fmt.Sprintf(`
			{
				"nextCursor": "%s",
				"pageSize": 1,
				"amountOfRuns": 0,
				"runs":[]
			}`, page3Cursor)))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction1,
        getRunsInteraction2,
        getRunsInteraction3,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "raw"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

	// Then...
	assert.Nil(t, err)
	runsReturned := mockConsole.ReadText()
	assert.Contains(t, runsReturned, runName)

	run := "U456|Finished|Passed|2023-05-10T06:00:13.043037Z|2023-05-10T06:00:36.159003Z|2023-05-10T06:02:53.823338Z|137664|myTestPackage.MyTestName|unitTesting|myBundleId|dummyGroup|" + apiServerUrl + "/ras/runs/xxx876xxx/runlog|\n"
	expectedResults := run + run
	assert.Equal(t, runsReturned, expectedResults)
}

func TestRunsGetOfGroupWhichExistsProducesExpectedRaw(t *testing.T) {

	// Given ...
	age := ""
	runName := "U456"
	requestor := ""
	result := ""
	shouldGetActive := false
	group := "dummyGroup"
	tags := make([]string, 0)

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
	
		values := req.URL.Query()
		runNameQueryParameter := values.Get("runname")
		assert.Equal(t, runNameQueryParameter, runName)

		writer.Write([]byte(fmt.Sprintf(`
			{
				"nextCursor": "",
				"pageSize": 100,
				"amountOfRuns": 1,
				"runs":[ %s ]
			}`, RUN_U456)))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "raw"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

	// Then...
	// We expect
	assert.Nil(t, err)
	textGotBack := mockConsole.ReadText()
	assert.Contains(t, textGotBack, runName)
	want := "U456|Finished|Passed|2023-05-10T06:00:13.043037Z|2023-05-10T06:00:36.159003Z|2023-05-10T06:02:53.823338Z|137664|myTestPackage.MyTestName|unitTesting|myBundleId|dummyGroup|" + apiServerUrl + "/ras/runs/xxx876xxx/runlog|\n"
	assert.Equal(t, textGotBack, want)
}

func TestRunsGetWithBadGroupNameThrowsError(t *testing.T) {

	// Given ...
	runName := "U457"
	age := ""
	requestor := ""
	result := ""
	shouldGetActive := false
	outputFormat := "raw"
	tags := make([]string, 0)

	group := string(rune(300)) + "NONLATIN1"

    interactions := []utils.HttpInteraction{}

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

	// Then...
	assert.NotNil(t, err, "A non-Latin-1 group name should throw an error")
	assert.ErrorContains(t, err, "GAL1105E")
	assert.ErrorContains(t, err, "Invalid group name provided")
}

func TestRunsGetWithOneTagSendsRequestWithCorrectTagsQuery(t *testing.T) {

	// Given ...
	runName := "U456"
	age := "2d:24h"
	requestor := ""
	result := ""
	group := ""
	tag := "core"
	tags := []string{ tag }
	shouldGetActive := false

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
	
		values := req.URL.Query()
		tagsQueryParameter := values.Get("tags")
		assert.Equal(t, tagsQueryParameter, tag)

		writer.Write([]byte(fmt.Sprintf(`
			 {
				 "nextCursor": "",
				 "pageSize": 100,
				 "amountOfRuns": 1,
				 "runs":[ %s ]
			 }`, RUN_U456_v2)))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

	// Then...
	assert.Nil(t, err, "Failed with an error when we expected it to pass")
	textGotBack := mockConsole.ReadText()
	assert.Contains(t, textGotBack, runName)
	want :=
		"submitted-time(UTC) name requestor     status   result           test-name             group tags\n" +
			"2023-05-10 06:00:13 U456 unitTesting22 Finished LongResultString myTestPackage.MyTest2       anothertag,core\n" +
			"\n" +
			"Total:1\n"
	assert.Equal(t, want, textGotBack)
}

func TestRunsGetWithMultipleTagsSendsRequestWithCorrectTagsQuery(t *testing.T) {

	// Given ...
	runName := "U456"
	age := "2d:24h"
	requestor := ""
	result := ""
	group := ""
	tag1 := "core"
	tag2 := "anothertag"
	tags := []string{ tag1, tag2 }
	shouldGetActive := false

	getRunsInteraction := utils.NewHttpInteraction("/ras/runs", http.MethodGet)
    getRunsInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
	
		values := req.URL.Query()
		tagsQueryParameter := values.Get("tags")
		assert.Equal(t, tagsQueryParameter, strings.Join(tags, ","))

		writer.Write([]byte(fmt.Sprintf(`
			 {
				 "nextCursor": "",
				 "pageSize": 100,
				 "amountOfRuns": 1,
				 "runs":[ %s ]
			 }`, RUN_U456_v2)))
    }

    interactions := []utils.HttpInteraction{
        getRunsInteraction,
    }

    server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	outputFormat := "summary"
	mockConsole := utils.NewMockConsole()

	apiServerUrl := server.Server.URL
	mockTimeService := utils.NewMockTimeService()
	commsClient := api.NewMockAPICommsClient(apiServerUrl)

	// When...
	err := GetRuns(runName, age, requestor, result, shouldGetActive, outputFormat, group, tags, mockTimeService, mockConsole, commsClient)

	// Then...
	assert.Nil(t, err, "Failed with an error when we expected it to pass")
	textGotBack := mockConsole.ReadText()
	assert.Contains(t, textGotBack, runName)
	want :=
		"submitted-time(UTC) name requestor     status   result           test-name             group tags\n" +
			"2023-05-10 06:00:13 U456 unitTesting22 Finished LongResultString myTestPackage.MyTest2       anothertag,core\n" +
			"\n" +
			"Total:1\n"
	assert.Equal(t, want, textGotBack)
}
