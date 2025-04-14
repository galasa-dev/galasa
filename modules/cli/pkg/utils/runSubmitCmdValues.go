/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

// RunsSubmitCmdParameters - Holds variables set by cobra's command-line parsing.
// We collect the parameters here so that our unit tests can feed in different values
// easily.
type RunsSubmitCmdValues struct {
	PollIntervalSeconds           int
	NoExitCodeOnTestFailures      bool
	ReportYamlFilename            string
	ReportJsonFilename            string
	ReportJunitFilename           string
	GroupName                     string
	ProgressReportIntervalMinutes int
	Throttle                      int
	Overrides                     []string
	Trace                         bool
	Requestor                     string
	RequestType                   string
	ThrottleFileName              string
	PortfolioFileName             string
	OverrideFilePaths             []string
	TestSelectionFlagValues       *TestSelectionFlagValues
}
