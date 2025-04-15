/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package launcher

import (
	"fmt"
	"strconv"
	"strings"

	"github.com/galasa-dev/cli/pkg/galasaapi"
)

// Structure used to collect parameters which are sent to the mock, so we can get them back in the
// test and assert they had certain values.
type LaunchParameters struct {
	GroupName        string
	ClassName        string
	RequestType      string
	Requestor        string
	Stream           string
	ObrFromPortfolio string
	IsTraceEnabled   bool
	GherkinURL       string
	GherkinFeature   string
	Overrides        map[string]interface{}
}

type MockLauncher struct {
	allTestRuns  *galasaapi.TestRuns
	nextRunId    int
	launches     []LaunchParameters
	submissionId int
}

func NewMockLauncher() *MockLauncher {
	launcher := new(MockLauncher)
	launcher.allTestRuns = newEmptyTestRun()
	launcher.allTestRuns.Runs = make([]galasaapi.TestRun, 0)
	launcher.nextRunId = 100
	launcher.submissionId = 0
	return launcher
}

//-------------------------------------------------------------------
// Implementation of the launcher interface.
//-------------------------------------------------------------------

// GetRunsByGroup gets the lust of test runs for this groupName
func (launcher *MockLauncher) GetRunsByGroup(groupName string) (*galasaapi.TestRuns, error) {
	return launcher.allTestRuns, nil
}

func newEmptyTestRun() *galasaapi.TestRuns {
	isCompleteValue := false
	testRuns := new(galasaapi.TestRuns)
	testRuns.Complete = &isCompleteValue
	testRuns.Runs = []galasaapi.TestRun{}
	return testRuns
}

func (launcher *MockLauncher) GetRecordedLaunchRecords() []LaunchParameters {
	return launcher.launches
}

// SubmitTestRuns launch the test runs
func (launcher *MockLauncher) SubmitTestRun(
	groupName string,
	className string,
	requestType string,
	requestor string,
	stream string,
	obrFromPortfolio string,
	isTraceEnabled bool,
	GherkinURL string,
	GherkinFeature string,
	overrides map[string]interface{},
) (*galasaapi.TestRuns, error) {

	launchRecord := LaunchParameters{
		GroupName:        groupName,
		ClassName:        className,
		RequestType:      requestType,
		Requestor:        requestor,
		Stream:           stream,
		ObrFromPortfolio: obrFromPortfolio,
		IsTraceEnabled:   isTraceEnabled,
		Overrides:        overrides,
	}
	launcher.launches = append(launcher.launches, launchRecord)

	name := fmt.Sprintf("M%d", launcher.nextRunId)
	launcher.nextRunId += 1

	newTestRun := galasaapi.NewTestRun()
	newTestRun.SetGroup(groupName)
	newTestRun.SetSubmissionId(strconv.Itoa(launcher.submissionId))

	classNameParts := strings.Split(className, "/")
	bundleName := classNameParts[0]
	qualifiedClassName := classNameParts[1]

	newTestRun.SetBundleName(bundleName)
	newTestRun.SetTestName(qualifiedClassName)

	newTestRun.SetStream(stream)
	newTestRun.SetName(name)
	launcher.allTestRuns.Runs = append(launcher.allTestRuns.Runs, *newTestRun)

	newTestRun.SetStatus("finished")
	newTestRun.SetResult("Passed")

	// Add the new test run to an empty list, so the caller can read things off it.
	testRunList := newEmptyTestRun()
	testRunList.Runs = append(testRunList.Runs, *newTestRun)

	// Add the new test run to our list so we can return details when asked about it later.
	launcher.allTestRuns.Runs = append(launcher.allTestRuns.Runs, *newTestRun)

	return launcher.allTestRuns, nil
}

// GetRunsById gets the Run information for the run with a specific run identifier
func (launcher *MockLauncher) GetRunsById(runId string) (*galasaapi.Run, error) {
	return &galasaapi.Run{}, nil
}

// Gets a run based on the submission ID of that run.
func (launcher *MockLauncher) GetRunsBySubmissionId(submissionId string, groupId string) (*galasaapi.Run, error) {
	return &galasaapi.Run{}, nil
}

// GetStreams gets a list of streams available on this launcher
func (launcher *MockLauncher) GetStreams() ([]string, error) {
	return make([]string, 0), nil
}

// GetTestCatalog gets the test catalog for a given stream.
func (launcher *MockLauncher) GetTestCatalog(stream string) (TestCatalog, error) {
	return nil, nil
}
