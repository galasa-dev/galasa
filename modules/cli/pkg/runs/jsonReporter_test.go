/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"strings"
	"testing"

	"github.com/galasa-dev/cli/pkg/files"
	"github.com/stretchr/testify/assert"
)

func TestJsonReportWorks(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()

	finishedRuns := TestRun{
		Name:           "myTestRun",
		Bundle:         "myBundle",
		Class:          "com.myco.MyClass",
		Stream:         "myStream",
		Obr:            "myOBR",
		Status:         "myStatus",
		Result:         "PASSED",
		Overrides:      make(map[string]string, 1),
		Tests:          []TestMethod{{Method: "method1", Result: "passed"}, {Method: "method2", Result: "passed"}},
		GherkinUrl:     "file:///my.feature",
		GherkinFeature: "my",
		SubmissionId:   "123",
	}

	finishedRunsMap := make(map[string]*TestRun, 1)
	finishedRunsMap["myTestRun"] = &finishedRuns

	// When...
	err := ReportJSON(
		mockFileSystem,
		"myReportJsonFilename",
		finishedRunsMap, nil)

	// Then...
	if err != nil {
		assert.Fail(t, "Report Json failed when it should have passed. "+err.Error())
	}

	var isExists bool
	isExists, err = mockFileSystem.Exists("myReportJsonFilename")
	if err != nil {
		assert.Fail(t, "json report does not exist in correct place. "+err.Error())
	}
	assert.True(t, isExists, "Json report does not exist in the correct place.")

	// We expect a report like this:
	expectedReport := `
	{
		"tests": [
			{
				"name": "myTestRun",
				"bundle": "myBundle",
				"class": "com.myco.MyClass",
				"stream": "myStream",
				"obr": "myOBR",
				"status": "myStatus",
				"queued":"",
				"requestor":"",
				"result": "PASSED",
				"overrides": {},
				"tests": [
					{
						"name": "method1",
						"result": "passed"
					},
					{
						"name": "method2",
						"result": "passed"
					}
				],
				"GherkinUrl":"file:///my.feature",
				"GherkinFeature":"my",
				"group":"",
				"submissionId":"123"
			}
		]
	}`

	var actualContents string
	actualContents, err = mockFileSystem.ReadTextFile("myReportJsonFilename")
	if err != nil {
		assert.Fail(t, "Could not read the json file. "+err.Error())
	}

	expected1 := strings.ReplaceAll(expectedReport, "\n", "")
	expected2 := strings.ReplaceAll(expected1, "\t", "")
	expected3 := strings.ReplaceAll(expected2, " ", "")

	actual1 := strings.ReplaceAll(actualContents, "\n", "")
	actual2 := strings.ReplaceAll(actual1, "\t", "")
	actual3 := strings.ReplaceAll(actual2, " ", "")

	assert.EqualValues(t, expected3, actual3)
}
