/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package streamsformatter

import (
	"testing"

	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/stretchr/testify/assert"
)

func CreateMockStream(streamName string, isEnabled bool, description string) *galasaapi.Stream {

	var stream = galasaapi.NewStream()
	var streamMetadata = galasaapi.NewStreamMetadata()
	var streamData = galasaapi.NewStreamData()

	streamMetadata.SetName(streamName)
	streamMetadata.SetDescription(description)
	streamData.SetIsEnabled(isEnabled)

	stream.SetData(*streamData)
	stream.SetMetadata(*streamMetadata)

	return stream

}

func TestStreamsSummaryFormatterSingleDataReturnsCorrectly(t *testing.T) {
	// Given...
	formatter := NewStreamsSummaryFormatter()

	streams := make([]galasaapi.Stream, 0)
	stream1 := CreateMockStream("mystream", true, "My test stream")
	streams = append(streams, *stream1)

	// When...
	actualFormattedOutput, err := formatter.FormatStreams(streams)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput :=
		`name     state   description
mystream enabled My test stream

Total:1
`
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestStreamsSummaryFormatterMultipleDataSeperatesWithNewLine(t *testing.T) {
	//Given..
	formatter := NewStreamsSummaryFormatter()

	streams := make([]galasaapi.Stream, 0)
	stream1 := CreateMockStream("mystream", true, "This is a test stream")
	stream2 := CreateMockStream("my-test-stream", false, "Dummy test stream")
	stream3 := CreateMockStream("example_stream", true, "Hello stream")
	streams = append(streams, *stream1, *stream2, *stream3)

	// When...
	actualFormattedOutput, err := formatter.FormatStreams(streams)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput :=
		`name           state    description
mystream       enabled  This is a test stream
my-test-stream disabled Dummy test stream
example_stream enabled  Hello stream

Total:3
`
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}
