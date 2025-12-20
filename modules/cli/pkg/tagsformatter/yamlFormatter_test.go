/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tagsformatter

import (
	"fmt"
	"testing"

	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/stretchr/testify/assert"
)

func generateExpectedTagYaml(tagName string, description string, priority int) string {
    return fmt.Sprintf(
`apiVersion: %s
kind: GalasaTag
metadata:
    name: %s
    description: %s
data:
    priority: %v`, API_VERSION, tagName, description, priority)
}

func TestTagsYamlFormatterNoDataReturnsBlankString(t *testing.T) {
    // Given...
    formatter := NewTagYamlFormatter()
    formattableTag := make([]galasaapi.GalasaTag, 0)

    // When...
    actualFormattedOutput, err := formatter.FormatTags(formattableTag)

    // Then...
    assert.Nil(t, err)
    expectedFormattedOutput := ""
    assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestTagsYamlFormatterSingleDataReturnsCorrectly(t *testing.T) {
    // Given..
    formatter := NewTagYamlFormatter()
    formattableTags := make([]galasaapi.GalasaTag, 0)
    tagName := "tag_one"
    tag1 := createMockGalasaTag(tagName, "my tag's description", 100)
    formattableTags = append(formattableTags, tag1)

    // When...
    actualFormattedOutput, err := formatter.FormatTags(formattableTags)

    // Then...
    assert.Nil(t, err)
    expectedFormattedOutput := generateExpectedTagYaml(tagName, "my tag's description", 100) + "\n"
    assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestTagsYamlFormatterMultipleDataSeperatesWithNewLine(t *testing.T) {
    // For..
    formatter := NewTagYamlFormatter()
    formattableTags := make([]galasaapi.GalasaTag, 0)

    tag1Name := "core-regression"
    tag2Name := "not-core-experimental"
    description := "this is a tag description"
    priority := 12
    tag1 := createMockGalasaTag(tag1Name, description, priority)
    tag2 := createMockGalasaTag(tag2Name, description, priority)
    formattableTags = append(formattableTags, tag1, tag2)

    // When...
    actualFormattedOutput, err := formatter.FormatTags(formattableTags)

    // Then...
    assert.Nil(t, err)
    expectedTag1Output := generateExpectedTagYaml(tag1Name, description, priority)
    expectedTag2Output := generateExpectedTagYaml(tag2Name, description, priority)
    expectedFormattedOutput := fmt.Sprintf(`%s
---
%s
`, expectedTag1Output, expectedTag2Output)
    assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}