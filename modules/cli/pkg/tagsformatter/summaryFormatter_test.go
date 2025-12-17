/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tagsformatter

import (
    "testing"

    "github.com/galasa-dev/cli/pkg/galasaapi"
    "github.com/stretchr/testify/assert"
)

const (
    API_VERSION = "galasa-dev/v1alpha1"
)

func createMockGalasaTag(
    tagName string,
    description string,
    priority int,
) galasaapi.GalasaTag {
    tag := *galasaapi.NewGalasaTag()

    tag.SetApiVersion(API_VERSION)
    tag.SetKind("GalasaTag")

    tagMetadata := *galasaapi.NewGalasaTagMetadata()
    tagMetadata.SetName(tagName)

    if description != "" {
        tagMetadata.SetDescription(description)
    }

    tagData := *galasaapi.NewGalasaTagData()
    tagData.SetPriority(int32(priority))

    tag.SetMetadata(tagMetadata)
    tag.SetData(tagData)
    return tag
}

func TestTagSummaryFormatterNoDataReturnsTotalCountAllZeros(t *testing.T) {
    // Given...
    formatter := NewTagSummaryFormatter()
    tags := make([]galasaapi.GalasaTag, 0)

    // When...
    actualFormattedOutput, err := formatter.FormatTags(tags)

    // Then...
    assert.Nil(t, err)
    expectedFormattedOutput := "Total:0\n"
    assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestTagSummaryFormatterSingleDataReturnsCorrectly(t *testing.T) {
    // Given...
    formatter := NewTagSummaryFormatter()
    description := "tag for core regression tests"
    tagName := "REGRESSION_TESTS"
    priority := 100
    tag1 := createMockGalasaTag(tagName, description, priority)
    tags := []galasaapi.GalasaTag{ tag1 }

    // When...
    actualFormattedOutput, err := formatter.FormatTags(tags)

    // Then...
    assert.Nil(t, err)
    expectedFormattedOutput :=
`name             priority description
REGRESSION_TESTS 100      tag for core regression tests

Total:1
`
    assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestTagSummaryFormatterMultipleDataSeperatesWithNewLine(t *testing.T) {
    // Given..
    formatter := NewTagSummaryFormatter()
    tags := make([]galasaapi.GalasaTag, 0)

    tag1Name := "TAG1"
    tag1Description := "my first tag"
    tag1Priority := 10
    tag2Name := "TAG2"
    tag2Description := "my second tag"
    tag2Priority := 20
    tag3Name := "TAG3"
    tag3Description := "my third tag"
    tag3Priority := 30

    tag1 := createMockGalasaTag(tag1Name, tag1Description, tag1Priority)
    tag2 := createMockGalasaTag(tag2Name, tag2Description, tag2Priority)
    tag3 := createMockGalasaTag(tag3Name, tag3Description, tag3Priority)
    tags = append(tags, tag1, tag2, tag3)

    // When...
    actualFormattedOutput, err := formatter.FormatTags(tags)

    // Then...
    assert.Nil(t, err)
    expectedFormattedOutput :=
`name priority description
TAG1 10       my first tag
TAG2 20       my second tag
TAG3 30       my third tag

Total:3
`
    assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}
