/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"bytes"
	"fmt"
	"log"
	"os"
	"sort"
	"strings"
)

const (
	RESULT_PASSED              = "Passed"
	RESULT_PASSED_WITH_DEFECTS = "Passed With Defects"
	RESULT_FAILED              = "Failed"
	RESULT_FAILED_WITH_DEFECTS = "Failed With Defects"
	RESULT_LOST                = "Lost"
	RESULT_ENVFAIL             = "EnvFail"
)

func CountTotalFailedRuns(finishedRuns map[string]*TestRun, lostRuns map[string]*TestRun) int {

	totalFailed := len(lostRuns)

	for _, run := range finishedRuns {
		// Anything which didn't pass failed by definition.
		if !strings.HasPrefix(run.Result, RESULT_PASSED) {
			totalFailed = totalFailed + 1
		}
	}

	return totalFailed
}

// FinalHumanReadableReport - Creates a human readable report of how it went.
func FinalHumanReadableReport(finishedRuns map[string]*TestRun, lostRuns map[string]*TestRun) {
	report := FinalHumanReadableReportAsString(finishedRuns, lostRuns)
	log.Print(report)
	fmt.Fprint(os.Stdout, report)
}

func FinalHumanReadableReportAsString(finishedRuns map[string]*TestRun, lostRuns map[string]*TestRun) string {

	totalResults := 0
	resultCounts := make(map[string]int, 0)

	resultCounts[RESULT_PASSED] = 0
	resultCounts[RESULT_FAILED] = 0
	resultCounts[RESULT_PASSED_WITH_DEFECTS] = 0
	resultCounts[RESULT_FAILED_WITH_DEFECTS] = 0
	resultCounts[RESULT_ENVFAIL] = 0

	for _, run := range finishedRuns {
		c, isFound := resultCounts[run.Result]
		if !isFound {
			resultCounts[run.Result] = 1
		} else {
			resultCounts[run.Result] = c + 1
		}
		totalResults += 1
	}

	resultCounts[RESULT_LOST] = len(lostRuns)
	totalResults += len(lostRuns)

	var buff bytes.Buffer

	fmt.Fprintln(&buff, "***")
	fmt.Fprintln(&buff, "*** Final report")
	fmt.Fprintln(&buff, "*** ---------------")
	fmt.Fprintln(&buff, "***")
	fmt.Fprintln(&buff, "*** Passed test runs:-")
	found := false
	for runName, run := range finishedRuns {
		if strings.HasPrefix(run.Result, RESULT_PASSED) && !strings.HasPrefix(run.Result, RESULT_PASSED_WITH_DEFECTS) {
			fmt.Fprintf(&buff, "***     Run %v - %v/%v/%v\n", runName, run.Stream, run.Bundle, run.Class)
			found = true
		}
	}
	if !found {
		fmt.Fprintln(&buff, "***     None")
	}

	fmt.Fprintln(&buff, "***")
	fmt.Fprintln(&buff, "*** Failed test runs:-")
	found = false
	for runName, run := range finishedRuns {
		if strings.HasPrefix(run.Result, RESULT_FAILED) && !strings.HasPrefix(run.Result, RESULT_FAILED_WITH_DEFECTS) {
			fmt.Fprintf(&buff, "***     Run %v - %v/%v/%v\n", runName, run.Stream, run.Bundle, run.Class)
			found = true
		}
	}
	if !found {
		fmt.Fprintln(&buff, "***     None")
	}

	fmt.Fprintln(&buff, "***")
	fmt.Fprintln(&buff, "*** Passed With Defects test runs:-")
	found = false
	for runName, run := range finishedRuns {
		if strings.HasPrefix(run.Result, RESULT_PASSED_WITH_DEFECTS) {
			fmt.Fprintf(&buff, "***     Run %v - %v/%v/%v\n", runName, run.Stream, run.Bundle, run.Class)
			found = true
		}
	}
	if !found {
		fmt.Fprintln(&buff, "***     None")
	}

	fmt.Fprintln(&buff, "***")
	fmt.Fprintln(&buff, "*** Failed With Defects test runs:-")
	found = false
	for runName, run := range finishedRuns {
		if strings.HasPrefix(run.Result, RESULT_FAILED_WITH_DEFECTS) {
			log.Printf("***     Run %v - %v/%v/%v\n", runName, run.Stream, run.Bundle, run.Class)
			found = true
		}
	}
	if !found {
		fmt.Fprintln(&buff, "***     None")
	}

	fmt.Fprintln(&buff, "***")
	fmt.Fprintln(&buff, "*** Other test runs:-")
	found = false
	for runName, run := range finishedRuns {
		if !strings.HasPrefix(run.Result, RESULT_PASSED) && !strings.HasPrefix(run.Result, RESULT_FAILED) {
			fmt.Fprintf(&buff, "***     Run %v(%v) - %v/%v/%v\n", runName, run.Result, run.Stream, run.Bundle, run.Class)
			found = true
		}
	}
	if !found {
		fmt.Fprintln(&buff, "***     None")
	}
	fmt.Fprintln(&buff, "***")
	fmt.Fprintln(&buff, "*** Results")
	resultsSoFar := fmt.Sprintf("*** Total=%v", totalResults)

	//Printing results in  a fixed order
	//Total, Passed, Passed With Defects, Failed, Failed With Defects, Lost, EnvFail, Custom Keys...
	orderedResultLabels := orderResultLabelKeys(resultCounts)

	for _, key := range orderedResultLabels {
		resultsSoFar = resultsSoFar + fmt.Sprintf(", %v=%v", key, resultCounts[key])
	}

	fmt.Fprintln(&buff, resultsSoFar)
	return buff.String()
}

func InterrimProgressReport(
	readyRuns []TestRun,
	submittedRuns map[string]*TestRun,
	finishedRuns map[string]*TestRun,
	lostRuns map[string]*TestRun,
	throttle int) {

	report := InterrimProgressReportAsString(readyRuns, submittedRuns, finishedRuns, lostRuns, throttle)
	log.Print(report)
}

func InterrimProgressReportAsString(
	readyRuns []TestRun,
	submittedRuns map[string]*TestRun,
	finishedRuns map[string]*TestRun,
	lostRuns map[string]*TestRun,
	throttle int) string {

	ready := len(readyRuns)
	submitted := len(submittedRuns)
	finished := len(finishedRuns)
	lost := len(lostRuns)

	totalResults := 0
	resultCounts := make(map[string]int, 0)

	for _, run := range finishedRuns {
		c, isFound := resultCounts[run.Result]
		if !isFound {
			resultCounts[run.Result] = 1
		} else {
			resultCounts[run.Result] = c + 1
		}
		totalResults += 1
	}

	resultCounts[RESULT_LOST] = len(lostRuns)
	totalResults += lost

	var buff bytes.Buffer

	fmt.Fprintln(&buff, "***")
	fmt.Fprintln(&buff, "*** Progress report")
	fmt.Fprintln(&buff, "*** ---------------")
	for runName, run := range submittedRuns {
		log.Printf("***     Run %v is currently %v - %v/%v/%v\n", runName, run.Status, run.Stream, run.Bundle, run.Class)
	}
	fmt.Fprintln(&buff, "*** ----------------------------------------------------------------------------")
	fmt.Fprintf(&buff, "*** run status, ready=%v, submitted=%v, finished=%v, lost=%v\n", ready, submitted, finished, lost)
	fmt.Fprintf(&buff, "*** throttle=%v\n", throttle)

	if len(resultCounts) > 0 {
		resultsSoFar := fmt.Sprintf("*** Results so far:\n*** Total=%v", totalResults)

		orderedResultLabels := orderResultLabelKeys(resultCounts)
		for _, key := range orderedResultLabels {
			resultsSoFar = resultsSoFar + fmt.Sprintf(", %v=%v", key, resultCounts[key])
		}
		fmt.Fprintln(&buff, resultsSoFar)
	}

	fmt.Fprintln(&buff, "***")
	return buff.String()
}

func orderResultLabelKeys(resultCounts map[string]int) []string {

	var orderedResultLabels []string
	orderedResultLabels = append(orderedResultLabels, RESULT_PASSED)
	orderedResultLabels = append(orderedResultLabels, RESULT_PASSED_WITH_DEFECTS)
	orderedResultLabels = append(orderedResultLabels, RESULT_FAILED)
	orderedResultLabels = append(orderedResultLabels, RESULT_FAILED_WITH_DEFECTS)
	orderedResultLabels = append(orderedResultLabels, RESULT_LOST)
	orderedResultLabels = append(orderedResultLabels, RESULT_ENVFAIL)

	//Build a list of standard labels to prevent duplication
	var standardResultLabels = make(map[string]struct{})
	for _, key := range orderedResultLabels {
		//'struct{}{}' allocates no storage. In Go 1.19 we can use just '{}' instead
		standardResultLabels[key] = struct{}{}
	}

	//Gathering custom labels
	var customResultLabels []string
	for keyLabel := range resultCounts {
		_, isStandardLabel := standardResultLabels[keyLabel]
		if !isStandardLabel {
			customResultLabels = append(customResultLabels, keyLabel)
		}
	}

	sort.Strings(customResultLabels)
	orderedResultLabels = append(orderedResultLabels, customResultLabels...)

	return orderedResultLabels
}
