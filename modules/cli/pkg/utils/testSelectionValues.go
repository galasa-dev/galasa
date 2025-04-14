/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

type TestSelectionFlagValues struct {
	Bundles     *[]string
	Packages    *[]string
	Tests       *[]string
	Tags        *[]string
	Classes     *[]string
	Stream      string
	RegexSelect *bool
	GherkinUrl  *[]string
}
