/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"strconv"
	"strings"
	"testing"

	"github.com/galasa-dev/cli/pkg/files"
	"github.com/stretchr/testify/assert"
)

func submitFinishedRunsAndReturnJunitReport(t *testing.T, finishedRunsMap map[string]*TestRun, lostRunsMap map[string]*TestRun, expectedReport string) {
	mockFileSystem := files.NewMockFileSystem()

	err := ReportJunit(
		mockFileSystem,
		"myReportJunitFilename",
		"myGroup",
		finishedRunsMap, lostRunsMap)

	// Then...
	if err != nil {
		assert.Fail(t, "ReportJunit failed when it should have passed. "+err.Error())
	}

	isExists, err := mockFileSystem.Exists("myReportJunitFilename")
	if err != nil {
		assert.Fail(t, "junit report does not exist in correct place. "+err.Error())
	}
	assert.True(t, isExists, "Junit report does not exist in the correct place.")

	actualContents, err := mockFileSystem.ReadTextFile("myReportJunitFilename")
	if err != nil {
		assert.Fail(t, "Could not read the junit file. "+err.Error())
	}

	expected := stripWhitespace(expectedReport)
	actual := stripWhitespace(actualContents)

	//Then...
	assert.Equal(t, len(expected), len(actual), "Lengths are not valid. Expected:%d Actual:%d", len(expected), len(actual))

	for index := range expected {
		expectedChar := expected[index]
		actualChar := actual[index]
		expectedCharOrd, _ := strconv.Atoi(string(expectedChar))
		actualCharOrd, _ := strconv.Atoi(string(actualChar))
		assert.Equal(t, expectedChar, actualChar, "Characters are not the same! expected:'%v' actual:'%v'", expectedCharOrd, actualCharOrd)
	}

	assert.EqualValues(t, expected, actual)

}

func stripWhitespace(input string) string {
	temp1 := strings.ReplaceAll(input, "\n", "")
	temp2 := strings.ReplaceAll(temp1, "\t", "")
	result := strings.ReplaceAll(temp2, " ", "")
	return result
}

func TestJunitReportPassedRunWith2PassedTestsWorks(t *testing.T) {
	// Given...
	finishedRuns := TestRun{
		Name:      "myTestRun",
		Bundle:    "myBundle",
		Class:     "com.myco.MyClass",
		Stream:    "myStream",
		Status:    "myStatus",
		Result:    "PASSED",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method1", Result: "Passed"}, {Method: "method2", Result: "Passed"}}}

	finishedRunsMap := make(map[string]*TestRun, 1)
	finishedRunsMap["myTestRun"] = &finishedRuns

	// We expect a report like this:
	expectedReport := `<?xml version="1.0" encoding="UTF-8" ?>
	<testsuites id="myGroup" name="Galasa test run" tests="2" failures="0" time="0">
		<testsuite id="myTestRun" name="myStream/myBundle/com.myco.MyClass" tests="2" failures="0" time="0">
			<testcase id="method1" name="method1" time="0"></testcase>
			<testcase id="method2" name="method2" time="0"></testcase>
		</testsuite>
	</testsuites>`

	// When...
	submitFinishedRunsAndReturnJunitReport(t, finishedRunsMap, nil, expectedReport)
}

func TestJunitReportFailedRunWith2PassedTestsWorks(t *testing.T) {
	// Given...
	finishedRuns := TestRun{
		Name:      "myTestRun",
		Bundle:    "myBundle",
		Class:     "com.myco.MyClass",
		Stream:    "myStream",
		Status:    "myStatus",
		Result:    "PASSED",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method1", Result: "Passed"}, {Method: "method2", Result: "Passed"}}}

	finishedRunsMap := make(map[string]*TestRun, 1)
	finishedRunsMap["myTestRun"] = &finishedRuns

	// We expect a report like this:
	expectedReport := `<?xml version="1.0" encoding="UTF-8" ?>
	<testsuites id="myGroup" name="Galasa test run" tests="2" failures="0" time="0">
		<testsuite id="myTestRun" name="myStream/myBundle/com.myco.MyClass" tests="2" failures="0" time="0">
			<testcase id="method1" name="method1" time="0"></testcase>
			<testcase id="method2" name="method2" time="0"></testcase>
		</testsuite>
	</testsuites>`

	// When...
	submitFinishedRunsAndReturnJunitReport(t, finishedRunsMap, nil, expectedReport)
}

func TestJunitReportWith0TestsWorks(t *testing.T) {
	// Given...
	finishedRuns := TestRun{
		Name:      "myTestRun",
		Bundle:    "myBundle",
		Class:     "com.myco.MyClass",
		Stream:    "myStream",
		Status:    "myStatus",
		Result:    "FAILED",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{}}

	finishedRunsMap := make(map[string]*TestRun, 1)
	finishedRunsMap["myTestRun"] = &finishedRuns

	// We expect a report like this:
	expectedReport := `<?xml version="1.0" encoding="UTF-8" ?>
	<testsuites id="myGroup" name="Galasa test run" tests="0" failures="0" time="0">
		<testsuite id="myTestRun" name="myStream/myBundle/com.myco.MyClass" tests="0" failures="0" time="0"></testsuite>
	</testsuites>`

	// When...
	submitFinishedRunsAndReturnJunitReport(t, finishedRunsMap, nil, expectedReport)
}

func TestJunitReportWith1FailedTestWorks(t *testing.T) {
	// Given...
	finishedRuns := TestRun{
		Name:           "myTestRun",
		Bundle:         "myBundle",
		Class:          "com.myco.MyClass",
		Stream:         "myStream",
		Status:         "myStatus",
		Result:         "FAILED",
		Overrides:      make(map[string]string, 1),
		Tests:          []TestMethod{{Method: "method1", Result: "Passed"}, {Method: "method2", Result: "failed"}},
		GherkinUrl:     "file:///my.feature",
		GherkinFeature: "my"}

	finishedRunsMap := make(map[string]*TestRun, 1)
	finishedRunsMap["myTestRun"] = &finishedRuns

	// We expect a report like this:
	expectedReport := `<?xml version="1.0" encoding="UTF-8" ?>
	<testsuites id="myGroup" name="Galasa test run" tests="2" failures="1" time="0">
		<testsuite id="myTestRun" name="myStream/myBundle/com.myco.MyClass" tests="2" failures="1" time="0">
			<testcase id="method1" name="method1" time="0"></testcase>
			<testcase id="method2" name="method2" time="0">
				<failure message="Failure messages are unavailable at this time" type="Unknown"></failure>
			</testcase>
		</testsuite>
	</testsuites>`

	//When...
	submitFinishedRunsAndReturnJunitReport(t, finishedRunsMap, nil, expectedReport)
}

func TestJunitReportWith2RunsAndMixedResultTestsReturnsOk(t *testing.T) {
	// Given...
	finishedRuns1 := TestRun{
		Name:      "myTestRun1",
		Bundle:    "myBundle1",
		Class:     "com.myco.MyClass1",
		Stream:    "myStream1",
		Status:    "myStatus1",
		Result:    "FAILED",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method1.1", Result: "Passed"}, {Method: "method1.2", Result: "failed"}}}

	finishedRuns2 := TestRun{
		Name:      "myTestRun2",
		Bundle:    "myBundle2",
		Class:     "com.myco.MyClass2",
		Stream:    "myStream2",
		Status:    "myStatus2",
		Result:    "PASSED",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method2.1", Result: "Passed"}, {Method: "method2.2", Result: "Passed"}}}

	finishedRunsMap := make(map[string]*TestRun, 1)
	finishedRunsMap["myTestRun1"] = &finishedRuns1
	finishedRunsMap["myTestRun2"] = &finishedRuns2

	// We expect a report like this:
	expectedReport := `<?xml version="1.0" encoding="UTF-8" ?>
	<testsuites id="myGroup" name="Galasa test run" tests="4" failures="1" time="0">
		<testsuite id="myTestRun1" name="myStream1/myBundle1/com.myco.MyClass1" tests="2" failures="1" time="0">
			<testcase id="method1.1" name="method1.1" time="0"></testcase>
			<testcase id="method1.2" name="method1.2" time="0">
				<failure message="Failure messages are unavailable at this time" type="Unknown"></failure>
			</testcase>
		</testsuite>
		<testsuite id="myTestRun2" name="myStream2/myBundle2/com.myco.MyClass2" tests="2" failures="0" time="0">
			<testcase id="method2.1" name="method2.1" time="0"></testcase>
			<testcase id="method2.2" name="method2.2" time="0"></testcase>
		</testsuite>
	</testsuites>`

	//When..
	submitFinishedRunsAndReturnJunitReport(t, finishedRunsMap, nil, expectedReport)
}

func TestJunitReportWithManyRunsAndMixedResultTestsReturnsAlphabeticallyOk(t *testing.T) {
	// Given...
	finishedRuns1 := TestRun{
		Name:      "zoo",
		Bundle:    "myBundle1",
		Class:     "com.myco.MyClass1",
		Stream:    "myStream1",
		Status:    "myStatus1",
		Result:    "FAILED",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method1.1", Result: "Passed"}, {Method: "method1.2", Result: "failed"}}}

	finishedRuns2 := TestRun{
		Name:      "myTestRun2",
		Bundle:    "myBundle2",
		Class:     "com.myco.MyClass2",
		Stream:    "myStream2",
		Status:    "myStatus2",
		Result:    "PASSED",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method2.1", Result: "Passed"}, {Method: "method2.2", Result: "Passed"}}}

	finishedRuns3 := TestRun{
		Name:      "eagle",
		Bundle:    "myBundle2",
		Class:     "com.myco.MyClass2",
		Stream:    "myStream2",
		Status:    "myStatus2",
		Result:    "PASSED",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method3.1", Result: "Passed"}}}

	finishedRunsMap := make(map[string]*TestRun, 1)
	finishedRunsMap["zoo"] = &finishedRuns1
	finishedRunsMap["myTestRun2"] = &finishedRuns2
	finishedRunsMap["eagle"] = &finishedRuns3

	// We expect a report like this:
	expectedReport := `<?xml version="1.0" encoding="UTF-8" ?>
	<testsuites id="myGroup" name="Galasa test run" tests="5" failures="1" time="0">
		<testsuite id="eagle" name="myStream2/myBundle2/com.myco.MyClass2" tests="1" failures="0" time="0">
			<testcase id="method3.1" name="method3.1" time="0"></testcase>
		</testsuite>	
		<testsuite id="myTestRun2" name="myStream2/myBundle2/com.myco.MyClass2" tests="2" failures="0" time="0">
			<testcase id="method2.1" name="method2.1" time="0"></testcase>
			<testcase id="method2.2" name="method2.2" time="0"></testcase>
		</testsuite>
		<testsuite id="zoo" name="myStream1/myBundle1/com.myco.MyClass1" tests="2" failures="1" time="0">
			<testcase id="method1.1" name="method1.1" time="0"></testcase>
			<testcase id="method1.2" name="method1.2" time="0">
				<failure message="Failure messages are unavailable at this time" type="Unknown"></failure>
			</testcase>
		</testsuite>
	</testsuites>`

	// When...
	submitFinishedRunsAndReturnJunitReport(t, finishedRunsMap, nil, expectedReport)
}

func TestJunitReportPassedRunWithLostRunsWorks(t *testing.T) {
	// Given...
	finishedRuns := TestRun{
		Name:      "myTestRun",
		Bundle:    "myBundle",
		Class:     "com.myco.MyClass",
		Stream:    "myStream",
		Status:    "myStatus",
		Result:    "PASSED",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method1", Result: "Passed"}, {Method: "method2", Result: "Passed"}}}

	finishedRunsMap := make(map[string]*TestRun, 1)
	finishedRunsMap["myTestRun"] = &finishedRuns

	lostRuns := TestRun{
		Name:      "myLostRun",
		Bundle:    "myBundle",
		Class:     "com.myco.MyClass",
		Stream:    "myStream",
		Status:    "myStatus",
		Result:    "UNKNOWN",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{}}

	lostRunsMap := make(map[string]*TestRun, 3)
	lostRunsMap["myLostRun"] = &lostRuns
	lostRunsMap["myLostRun1"] = &lostRuns
	lostRunsMap["myLostRun2"] = &lostRuns

	// We expect a report like this:
	expectedReport := `<?xml version="1.0" encoding="UTF-8" ?>
	<testsuites id="myGroup" name="Galasa test run" tests="5" failures="3" time="0">
		<testsuite id="myTestRun" name="myStream/myBundle/com.myco.MyClass" tests="2" failures="0" time="0">
			<testcase id="method1" name="method1" time="0"></testcase>
			<testcase id="method2" name="method2" time="0"></testcase>
		</testsuite>
	</testsuites>`

	// When...
	submitFinishedRunsAndReturnJunitReport(t, finishedRunsMap, lostRunsMap, expectedReport)
}
