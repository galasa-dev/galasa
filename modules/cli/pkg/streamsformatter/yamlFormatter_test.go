/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package streamsformatter

import (
	"fmt"
	"testing"

	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/stretchr/testify/assert"
)

const (
	STREAM_DESCRIPTION = "This a Galasa test stream"
	MAVEN_REPO_URL     = "mvn:myGroup/myArtifact/0.38.0/obr"
	TEST_CATALOG_URL   = "mvn:myGroup/myArtifact/0.38.0/obr"
	MAVEN_GROUP_ID     = "myGroup"
	MAVEN_ARTIFACT_ID  = "myArtifact"
	MAVEN_VERSION      = "0.38.0"
)

func createMockTestStreamYamlFormat(streamName string) *galasaapi.Stream {
	var stream = galasaapi.NewStream()

	stream.SetKind("GalasaStream")
	stream.SetApiVersion("galasa-dev/v1alpha1")

	var streamMetadata = galasaapi.NewStreamMetadata()
	streamMetadata.SetDescription(STREAM_DESCRIPTION)
	streamMetadata.SetName(streamName)
	stream.SetMetadata(*streamMetadata)

	var streamData = galasaapi.NewStreamData()
	var testCatalog = galasaapi.NewStreamTestCatalog()
	var streamRepoUrl = galasaapi.NewStreamRepository()

	streamRepoUrl.SetUrl(MAVEN_REPO_URL)
	testCatalog.SetUrl(TEST_CATALOG_URL)
	streamData.SetRepository(*streamRepoUrl)
	streamData.SetTestCatalog(*testCatalog)

	// Create OBR data
	groupId := MAVEN_GROUP_ID
	artifactId := MAVEN_ARTIFACT_ID
	version := MAVEN_VERSION

	obrData := galasaapi.StreamOBRData{
		GroupId:    &groupId,
		ArtifactId: &artifactId,
		Version:    &version,
	}

	obrsSlice := []galasaapi.StreamOBRData{obrData}
	streamData.SetObrs(obrsSlice)

	stream.SetData(*streamData)

	return stream
}

func generateExpectedStreamYaml(streamName string) string {
	return fmt.Sprintf(
		`apiVersion: galasa-dev/v1alpha1
kind: GalasaStream
metadata:
    name: %s
    description: %s
data:
    repository:
        url: %s
    obrs:
        - group-id: %s
          artifact-id: %s
          version: %s
    testCatalog:
        url: %s`,
		streamName, STREAM_DESCRIPTION, MAVEN_REPO_URL, MAVEN_GROUP_ID, MAVEN_ARTIFACT_ID, MAVEN_VERSION, TEST_CATALOG_URL)
}

func TestSecretsYamlFormatterNoDataReturnsBlankString(t *testing.T) {
	// Given...
	formatter := NewStreamsYamlFormatter()
	formattableStreams := make([]galasaapi.Stream, 0)

	// When...
	actualFormattedOutput, err := formatter.FormatStreams(formattableStreams)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput := ""
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestStreamsYamlFormatterSingleDataReturnsCorrectly(t *testing.T) {
	// Given..
	formatter := NewStreamsYamlFormatter()
	formattableStreams := make([]galasaapi.Stream, 0)
	streamName := "mystream"

	stream1 := createMockTestStreamYamlFormat(streamName)
	formattableStreams = append(formattableStreams, *stream1)

	// When...
	actualFormattedOutput, err := formatter.FormatStreams(formattableStreams)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput := generateExpectedStreamYaml(streamName) + "\n"
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestStreamsYamlFormatterMultipleDataReturnsCorrectly(t *testing.T) {
	// Given..
	formatter := NewStreamsYamlFormatter()
	formattableStreams := make([]galasaapi.Stream, 0)
	streamName1 := "mystream"
	streamName2 := "mystream2"

	stream1 := createMockTestStreamYamlFormat(streamName1)
	stream2 := createMockTestStreamYamlFormat(streamName2)
	formattableStreams = append(formattableStreams, *stream1, *stream2)

	// When...
	actualFormattedOutput, err := formatter.FormatStreams(formattableStreams)

	// Then...
	assert.Nil(t, err)
	expectedFormatted1Output := generateExpectedStreamYaml(streamName1)
	expectedFormatted2Output := generateExpectedStreamYaml(streamName2)

	expectedFormattedOutput := fmt.Sprintf(`%s
---
%s
`, expectedFormatted1Output, expectedFormatted2Output)

	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}
