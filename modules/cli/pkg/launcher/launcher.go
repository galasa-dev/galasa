/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package launcher

import (
	"github.com/galasa-dev/cli/pkg/galasaapi"
)

type TestCatalog map[string]interface{}

// ----------------------------------------------------------------------------------
// Launcher something which launches and monitors tests in some environment.
type Launcher interface {

	// GetRunsByGroup gets the lust of test runs for this groupName
	GetRunsByGroup(groupName string) (*galasaapi.TestRuns, error)

	// SubmitTestRuns launch the test runs
	SubmitTestRun(
		groupName string,
		className string,
		requestType string,
		requestor string,
		stream string,
		obrFromPortfolio string,
		isTraceEnabled bool,
		gherkinURL string,
		GherkinFeature string,
		overrides map[string]interface{},
	) (*galasaapi.TestRuns, error)

	// GetRunsById gets the Run information for the run with a specific run identifier
	GetRunsById(runId string) (*galasaapi.Run, error)

	// Gets a run based on the submission ID of that run.
	GetRunsBySubmissionId(submissionId string, groupId string) (*galasaapi.Run, error)

	// GetStreams gets a list of streams available on this launcher
	GetStreams() ([]string, error)

	// GetTestCatalog gets the test catalog for a given stream.
	GetTestCatalog(stream string) (TestCatalog, error)
}
