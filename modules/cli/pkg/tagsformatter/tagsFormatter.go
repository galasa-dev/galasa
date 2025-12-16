/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package tagsformatter

import (
	"github.com/galasa-dev/cli/pkg/galasaapi"
)

// Displays tags in the following format:
// name                  priority description          
// core_regression_tests 100      Core regression tests
// my-experimental-tag   5        Experimental tests   
// anothertag 		     10       Dummy tag            
// Total:3

// -----------------------------------------------------
// TagsFormatter - implementations can take a collection of tags
// and turn them into a string for display to the user.
const (
	HEADER_TAG_NAME        = "name"
	HEADER_TAG_DESCRIPTION = "description"
	HEADER_TAG_PRIORITY    = "priority"
)

type TagsFormatter interface {
	FormatTags(secrets []galasaapi.GalasaTag) (string, error)
	GetName() string
}
