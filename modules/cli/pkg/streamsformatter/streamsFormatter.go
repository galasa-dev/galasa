/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package streamsformatter

import (
	"github.com/galasa-dev/cli/pkg/galasaapi"
)

//Print in the following fashion:
// name			state   	description
// mystream		enabled 	Experimental tests
// fakestream   enabled 	Fake tests
//
// Total:2

// ------------------------------------------------------
// StreamsFormatter - implemetations can take a collection of stream results
// and turn them into a string for display to the user

const (
	HEADER_STREAM_NAME        = "name"
	HEADER_STREAM_STATE       = "state"
	HEADER_STREAM_DESCRIPTION = "description"
)

type StreamsFormatter interface {
	FormatStreams(streamResults []galasaapi.Stream) (string, error)
	GetName() string
}
