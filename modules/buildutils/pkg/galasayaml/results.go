/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package galasayaml

type Results struct {
	Tests []ResultsTest `yaml:"tests"`
}

type ResultsTest struct {
	Name   string
	Bundle string
	Class  string
}
