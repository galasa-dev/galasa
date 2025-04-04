/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestCanCountFailuresNoLostOnePass(t *testing.T) {
	// Given...
	var lostRunsMap map[string]*TestRun = make(map[string]*TestRun, 0)

	finishedRuns := TestRun{
		Name:      "myTestRun",
		Bundle:    "myBundle",
		Class:     "com.myco.MyClass",
		Stream:    "myStream",
		Status:    "myStatus",
		Result:    "Passed",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method1", Result: "passed"}, {Method: "method2", Result: "passed"}}}

	finishedRunsMap := make(map[string]*TestRun, 1)
	finishedRunsMap["myTestRun"] = &finishedRuns

	// When
	count := CountTotalFailedRuns(finishedRunsMap, lostRunsMap)

	assert.Equal(t, 0, count, "Failed to count failed test cases.")
}

func TestCanCountFailuresOneLostOneFailed(t *testing.T) {
	// Given...
	lostRun1 := TestRun{
		Name:      "myTestRun",
		Bundle:    "myBundle",
		Class:     "com.myco.MyClass",
		Stream:    "myStream",
		Status:    "myStatus",
		Result:    "Failed",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method1", Result: "passed"}, {Method: "method2", Result: "passed"}}}

	finished1 := TestRun{
		Name:      "myTestRun",
		Bundle:    "myBundle",
		Class:     "com.myco.MyClass",
		Stream:    "myStream",
		Status:    "myStatus",
		Result:    "Failed",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method1", Result: "passed"}, {Method: "method2", Result: "passed"}}}

	finishedRunsMap := make(map[string]*TestRun, 1)
	finishedRunsMap["myTestRun"] = &finished1

	lostRunsMap := make(map[string]*TestRun, 1)
	lostRunsMap["myTestRun"] = &lostRun1

	// When
	count := CountTotalFailedRuns(finishedRunsMap, lostRunsMap)

	assert.Equal(t, 2, count, "Failed to count failed test cases.")
}

func TestCanCallHumanReportNoErrors(t *testing.T) {
	// Given...
	lostRun1 := TestRun{
		Name:      "myTestRun",
		Bundle:    "myBundle",
		Class:     "com.myco.MyClass",
		Stream:    "myStream",
		Status:    "myStatus",
		Result:    "Failed",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method1", Result: "passed"}, {Method: "method2", Result: "passed"}}}

	finished1 := TestRun{
		Name:      "myTestRun",
		Bundle:    "myBundle",
		Class:     "com.myco.MyClass",
		Stream:    "myStream",
		Status:    "myStatus",
		Result:    "Failed",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method1", Result: "passed"}, {Method: "method2", Result: "passed"}}}

	finishedRunsMap := make(map[string]*TestRun, 1)
	finishedRunsMap["myTestRun"] = &finished1

	lostRunsMap := make(map[string]*TestRun, 1)
	lostRunsMap["myTestRun"] = &lostRun1

	// When
	reportText := FinalHumanReadableReportAsString(finishedRunsMap, lostRunsMap)

	assert.Contains(t, reportText, "Final report")
}

func TestHumanReportResultsPrintsInOrder(t *testing.T) {
	// Given...
	lostRun1 := TestRun{
		Name:      "myTestRun",
		Bundle:    "myBundle",
		Class:     "com.myco.MyClass",
		Stream:    "myStream",
		Status:    "myStatus",
		Result:    "Failed",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method1", Result: "passed"}, {Method: "method2", Result: "passed"}}}

	finished1 := TestRun{
		Name:      "myTestRun",
		Bundle:    "myBundle",
		Class:     "com.myco.MyClass",
		Stream:    "myStream",
		Status:    "myStatus",
		Result:    "Passed",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method1", Result: "passed"}, {Method: "method2", Result: "passed"}}}

	finished2 := TestRun{
		Name:      "myTestRun2",
		Bundle:    "myBundle",
		Class:     "com.myco.MyClass",
		Stream:    "myStream",
		Status:    "myStatus",
		Result:    "Failed",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method1", Result: "passed"}, {Method: "method2", Result: "passed"}}}

	finishedRunsMap := make(map[string]*TestRun, 2)
	finishedRunsMap["myTestRun1"] = &finished1
	finishedRunsMap["myTestRun2"] = &finished2

	lostRunsMap := make(map[string]*TestRun, 1)
	lostRunsMap["myTestRun"] = &lostRun1

	// When
	reportText := FinalHumanReadableReportAsString(finishedRunsMap, lostRunsMap)
	//Then
	assert.Contains(t, reportText, "Total=3, Passed=1, Passed With Defects=0, Failed=1, Failed With Defects=0, Lost=1")
}

func TestHumanReportResultsPrintsInOrderWithCustomResult(t *testing.T) {
	// Given...
	lostRun1 := TestRun{
		Name:      "myTestRun",
		Bundle:    "myBundle",
		Class:     "com.myco.MyClass",
		Stream:    "myStream",
		Status:    "myStatus",
		Result:    "Failed",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method1", Result: "passed"}, {Method: "method2", Result: "passed"}}}

	finished1 := TestRun{
		Name:      "myTestRun1",
		Bundle:    "myBundle",
		Class:     "com.myco.MyClass",
		Stream:    "myStream",
		Status:    "myStatus",
		Result:    "Passed",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method1", Result: "passed"}, {Method: "method2", Result: "passed"}}}

	finished2 := TestRun{
		Name:      "myTestRun2",
		Bundle:    "myBundle",
		Class:     "com.myco.MyClass",
		Stream:    "myStream",
		Status:    "myStatus",
		Result:    "Custom",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method1", Result: "passed"}, {Method: "method2", Result: "passed"}}}

	finished3 := TestRun{
		Name:      "myTestRun3",
		Bundle:    "myBundle",
		Class:     "com.myco.MyClass",
		Stream:    "myStream",
		Status:    "myStatus",
		Result:    "Custard",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method1", Result: "passed"}, {Method: "method2", Result: "passed"}}}

	finishedRunsMap := make(map[string]*TestRun, 3)
	finishedRunsMap["myTestRun1"] = &finished1
	finishedRunsMap["myTestRun2"] = &finished2
	finishedRunsMap["myTestRun3"] = &finished3

	lostRunsMap := make(map[string]*TestRun, 1)
	lostRunsMap["myTestRun"] = &lostRun1

	// When
	reportText := FinalHumanReadableReportAsString(finishedRunsMap, lostRunsMap)
	//Then
	assert.Contains(t, reportText, "Total=4, Passed=1, Passed With Defects=0, Failed=0, Failed With Defects=0, Lost=1, EnvFail=0, Custard=1, Custom=1")
}

func TestHumanReportResultsPrintsInOrderWhenAllResultsAreCustomResults(t *testing.T) {
	// Given...
	lostRun1 := TestRun{
		Name:      "myTestRun",
		Bundle:    "myBundle",
		Class:     "com.myco.MyClass",
		Stream:    "myStream",
		Status:    "myStatus",
		Result:    "CustomLost",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method1", Result: "passed"}, {Method: "method2", Result: "passed"}}}

	finished1 := TestRun{
		Name:      "myTestRun1",
		Bundle:    "myBundle",
		Class:     "com.myco.MyClass",
		Stream:    "myStream",
		Status:    "myStatus",
		Result:    "Doughnuts",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method1", Result: "passed"}, {Method: "method2", Result: "passed"}}}

	finished2 := TestRun{
		Name:      "myTestRun2",
		Bundle:    "myBundle",
		Class:     "com.myco.MyClass",
		Stream:    "myStream",
		Status:    "myStatus",
		Result:    "Cookies",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method1", Result: "passed"}, {Method: "method2", Result: "passed"}}}

	finished3 := TestRun{
		Name:      "myTestRun3",
		Bundle:    "myBundle",
		Class:     "com.myco.MyClass",
		Stream:    "myStream",
		Status:    "myStatus",
		Result:    "Jam",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method1", Result: "passed"}, {Method: "method2", Result: "passed"}}}

	finishedRunsMap := make(map[string]*TestRun, 3)
	finishedRunsMap["myTestRun1"] = &finished1
	finishedRunsMap["myTestRun2"] = &finished2
	finishedRunsMap["myTestRun3"] = &finished3

	lostRunsMap := make(map[string]*TestRun, 1)
	lostRunsMap["myTestRun"] = &lostRun1

	// When
	reportText := FinalHumanReadableReportAsString(finishedRunsMap, lostRunsMap)
	//Then
	assert.Contains(t, reportText, "Total=4, Passed=0, Passed With Defects=0, Failed=0, Failed With Defects=0, Lost=1, EnvFail=0, Cookies=1, Doughnuts=1, Jam=1")
}

func TestHumanReportResultsWithNoDataPrintsInOrder(t *testing.T) {
	// Given...
	var lostRunsMap map[string]*TestRun = make(map[string]*TestRun, 0)
	var finishedRunsMap map[string]*TestRun = make(map[string]*TestRun, 0)

	// When
	reportText := FinalHumanReadableReportAsString(finishedRunsMap, lostRunsMap)
	//Then
	assert.Contains(t, reportText, "Total=0, Passed=0, Passed With Defects=0, Failed=0, Failed With Defects=0, Lost=0, EnvFail=0")
}

func TestCanCallHumanInterrimReportNoErrors(t *testing.T) {
	// Given...
	lostRun1 := TestRun{
		Name:      "myTestRun",
		Bundle:    "myBundle",
		Class:     "com.myco.MyClass",
		Stream:    "myStream",
		Status:    "myStatus",
		Result:    "Failed",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method1", Result: "passed"}, {Method: "method2", Result: "passed"}}}

	finished1 := TestRun{
		Name:      "myTestRun1",
		Bundle:    "myBundle",
		Class:     "com.myco.MyClass",
		Stream:    "myStream",
		Status:    "myStatus",
		Result:    "Passed",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method1", Result: "passed"}, {Method: "method2", Result: "passed"}}}

	finished2 := TestRun{
		Name:      "myTestRun2",
		Bundle:    "myBundle",
		Class:     "com.myco.MyClass",
		Stream:    "myStream",
		Status:    "myStatus",
		Result:    "Failed With Defects",
		Overrides: make(map[string]string, 1),
		Tests:     []TestMethod{{Method: "method1", Result: "passed"}, {Method: "method2", Result: "passed"}}}

	finishedRunsMap := make(map[string]*TestRun, 2)
	finishedRunsMap["myTestRun1"] = &finished1
	finishedRunsMap["myTestRun2"] = &finished2

	lostRunsMap := make(map[string]*TestRun, 1)
	lostRunsMap["myTestRun"] = &lostRun1

	// When
	reportText := InterrimProgressReportAsString(make([]TestRun, 0), finishedRunsMap, finishedRunsMap, lostRunsMap, 5)

	//Then...
	assert.Contains(t, reportText, "Progress report")
	assert.Contains(t, reportText, "Total=3, Passed=1, Passed With Defects=0, Failed=0, Failed With Defects=1, Lost=1, EnvFail=0")
}
