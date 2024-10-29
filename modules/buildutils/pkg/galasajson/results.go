/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package galasajson

type Results struct {
	Tests []TestResult `json:"tests"`
}

type TestResult struct {
	Name   string `json:"name"`
	Class  string `json:"class"`
	Result string `json:"result"`
}
