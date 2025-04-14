/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"log"
	"sort"

	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/runsformatter"
)

func orderFormattableTests(formattableTest []runsformatter.FormattableTest) []runsformatter.FormattableTest {
	var orderedFormattableTest []runsformatter.FormattableTest

	//get slice of all result labels in ordered form
	orderedResultLabels := getAvailableResultLabelsinOrder(formattableTest)

	//formattableTest runs grouped by results
	//map["passed"] = [run1, run2, ...]
	runsGroupedByResultsMap := make(map[string][]runsformatter.FormattableTest)
	for _, run := range formattableTest {
		runsGroupedByResultsMap[run.Result] = append(runsGroupedByResultsMap[run.Result], run)
	}

	//append tests in order
	for _, result := range orderedResultLabels {
		orderedFormattableTest = append(orderedFormattableTest, runsGroupedByResultsMap[result]...)
	}

	// log.Printf("Returning %v test results\n", len(orderedFormattableTest))
	return orderedFormattableTest
}

// getAvailableResultLabelsinOrder - returns a slice of all available result labels in the order they should be displayed
// The order is important as it determines how the tests will be displayed on the screen
// The standard labels are shown first, followed by any extra labels present in the test data.
// These laels can be used as columns in the eventual output.
func getAvailableResultLabelsinOrder(formattableTest []runsformatter.FormattableTest) []string {
	var orderedResultLabels []string = make([]string, 0)
	orderedResultLabels = append(orderedResultLabels, RESULT_PASSED)
	orderedResultLabels = append(orderedResultLabels, RESULT_PASSED_WITH_DEFECTS)
	orderedResultLabels = append(orderedResultLabels, RESULT_FAILED)
	orderedResultLabels = append(orderedResultLabels, RESULT_FAILED_WITH_DEFECTS)
	orderedResultLabels = append(orderedResultLabels, RESULT_ENVFAIL)

	//Build a list of standard labels to prevent duplication
	var standardResultLabels = make(map[string]struct{}, 0)
	for _, key := range orderedResultLabels {
		//'struct{}{}' allocates no storage. In Go 1.19 we can use just '{}' instead
		standardResultLabels[key] = struct{}{}
	}

	log.Printf("There are %v standard labels: %v\n", len(standardResultLabels), standardResultLabels)

	//Gathering custom labels
	var customResultLabels []string = make([]string, 0)
	// A map to make sure we never add the same custom label to the list twice.
	customResultLabelMap := make(map[string]string, 0)
	for _, run := range formattableTest {
		_, isStandardLabel := standardResultLabels[run.Result]
		if !isStandardLabel {

			_, isCustomLabelWeAlreadyKnowAbout := customResultLabelMap[run.Result]
			if !isCustomLabelWeAlreadyKnowAbout {
				log.Printf("Label '%v' is not a standard result label\n", run.Result)
				customResultLabels = append(customResultLabels, run.Result)
				customResultLabelMap[run.Result] = "known"
			}
		}
	}

	sort.Strings(customResultLabels)
	orderedResultLabels = append(orderedResultLabels, customResultLabels...)

	log.Printf("There are %v labels overall: %v", len(orderedResultLabels), orderedResultLabels)

	return orderedResultLabels
}

func FormattableTestFromGalasaApi(runs []galasaapi.Run, apiServerUrl string) []runsformatter.FormattableTest {
	var formattableTest []runsformatter.FormattableTest

	log.Printf("FormattableTestFromGalasaApi: There are %v runs passed\n", len(runs))

	for _, run := range runs {
		//Get the data for each TestStructure in runs
		newFormattableTest := getTestStructureData(run, apiServerUrl)
		formattableTest = append(formattableTest, newFormattableTest)
	}

	log.Printf("FormattableTestFromGalasaApi: There are %v runs to format\n", len(formattableTest))

	orderedFormattableTest := orderFormattableTests(formattableTest)

	log.Printf("FormattableTestFromGalasaApi: There are %v runs returned\n", len(orderedFormattableTest))

	return orderedFormattableTest
}

func getTestStructureData(run galasaapi.Run, apiServerUrl string) runsformatter.FormattableTest {
	newFormattableTest := runsformatter.NewFormattableTest()

	newFormattableTest.RunId = run.GetRunId()
	newFormattableTest.ApiServerUrl = apiServerUrl

	newFormattableTest.Name = run.TestStructure.GetRunName()
	newFormattableTest.TestName = run.TestStructure.GetTestName()
	newFormattableTest.Status = run.TestStructure.GetStatus()
	newFormattableTest.Result = run.TestStructure.GetResult()
	newFormattableTest.StartTimeUTC = run.TestStructure.GetStartTime()
	newFormattableTest.EndTimeUTC = run.TestStructure.GetEndTime()
	newFormattableTest.QueuedTimeUTC = run.TestStructure.GetQueued()
	newFormattableTest.Requestor = run.TestStructure.GetRequestor()
	newFormattableTest.Bundle = run.TestStructure.GetBundle()
	newFormattableTest.Methods = run.TestStructure.GetMethods()
	newFormattableTest.Group = run.TestStructure.GetGroup()

	return newFormattableTest
}

func FormattableTestFromTestRun(finishedMap map[string]*TestRun, lostMap map[string]*TestRun) []runsformatter.FormattableTest {
	var formattableTest []runsformatter.FormattableTest
	for _, run := range finishedMap {
		isLost := false
		newFormattableTest := getTestRunData(*run, isLost)
		formattableTest = append(formattableTest, newFormattableTest)
	}
	for _, run := range lostMap {
		isLost := true
		newFormattableTest := getTestRunData(*run, isLost)
		formattableTest = append(formattableTest, newFormattableTest)
	}

	orderedFormattableTest := orderFormattableTests(formattableTest)

	return orderedFormattableTest
}

func getTestRunData(run TestRun, isLost bool) runsformatter.FormattableTest {
	newFormattableTest := runsformatter.NewFormattableTest()

	newFormattableTest.RunId = ""
	newFormattableTest.ApiServerUrl = ""

	newFormattableTest.Name = run.Name
	if run.GherkinUrl != "" {
		newFormattableTest.TestName = run.GherkinFeature
	} else {
		newFormattableTest.TestName = run.Stream + "/" + run.Bundle + "/" + run.Class
	}
	newFormattableTest.Status = run.Status
	newFormattableTest.Result = run.Result
	newFormattableTest.StartTimeUTC = ""
	newFormattableTest.EndTimeUTC = ""
	newFormattableTest.QueuedTimeUTC = run.QueuedTimeUTC
	newFormattableTest.Requestor = run.Requestor
	newFormattableTest.Bundle = run.Bundle
	newFormattableTest.Methods = nil
	newFormattableTest.Lost = isLost
	newFormattableTest.Group = run.Group

	return newFormattableTest
}
