/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"bytes"
	"strings"
	"testing"
	"text/template"

	"galasa.dev/buildUtilities/pkg/galasayaml"
	"github.com/stretchr/testify/assert"
	"gopkg.in/yaml.v3"
)

// ---------------------------------------------------------------------------
// YAML parsing tests
// ---------------------------------------------------------------------------

const releaseYamlWithExclusions = `
apiVersion: galasa.dev/v1alpha
kind: Release
metadata:
  name: galasa-release
release:
  version: 1.0.0
framework:
  bundles:
    - artifact: some-artifact
      version: 1.2.3
      isolated: true
      exclusions:
        - groupId: org.unwanted
          artifactId: transitive-dep
        - groupId: org.also-unwanted
          artifactId: another-dep
`

const releaseYamlWithoutExclusions = `
apiVersion: galasa.dev/v1alpha
kind: Release
metadata:
  name: galasa-release
release:
  version: 1.0.0
framework:
  bundles:
    - artifact: plain-artifact
      version: 2.0.0
      isolated: true
`

func TestYamlParsing_BundleWithExclusions_PopulatesExclusionsField(t *testing.T) {
	// Given...
	var rel galasayaml.Release

	// When...
	err := yaml.Unmarshal([]byte(releaseYamlWithExclusions), &rel)

	// Then...
	assert.Nil(t, err)
	assert.Len(t, rel.Framework.Bundles, 1)

	bundle := rel.Framework.Bundles[0]
	assert.Equal(t, "some-artifact", bundle.Artifact)
	assert.Len(t, bundle.Exclusions, 2)

	assert.Equal(t, "org.unwanted", bundle.Exclusions[0].GroupId)
	assert.Equal(t, "transitive-dep", bundle.Exclusions[0].ArtifactId)
	assert.Equal(t, "org.also-unwanted", bundle.Exclusions[1].GroupId)
	assert.Equal(t, "another-dep", bundle.Exclusions[1].ArtifactId)
}

func TestYamlParsing_BundleWithoutExclusions_ExclusionsFieldIsNil(t *testing.T) {
	// Given...
	var rel galasayaml.Release

	// When...
	err := yaml.Unmarshal([]byte(releaseYamlWithoutExclusions), &rel)

	// Then...
	assert.Nil(t, err)
	assert.Len(t, rel.Framework.Bundles, 1)
	assert.Nil(t, rel.Framework.Bundles[0].Exclusions)
}

// ---------------------------------------------------------------------------
// bundleExclusions helper tests
// ---------------------------------------------------------------------------

func TestBundleExclusions_WithEntries_ReturnsMappedSlice(t *testing.T) {
	// Given...
	yamlExclusions := []galasayaml.Exclusion{
		{GroupId: "org.unwanted", ArtifactId: "transitive-dep"},
	}

	// When...
	result := bundleExclusions(yamlExclusions)

	// Then...
	assert.Len(t, result, 1)
	assert.Equal(t, "org.unwanted", result[0].GroupId)
	assert.Equal(t, "transitive-dep", result[0].ArtifactId)
}

func TestBundleExclusions_WithEmptySlice_ReturnsNil(t *testing.T) {
	// Given / When...
	result := bundleExclusions([]galasayaml.Exclusion{})

	// Then...
	assert.Nil(t, result)
}

func TestBundleExclusions_WithNilSlice_ReturnsNil(t *testing.T) {
	// Given / When...
	result := bundleExclusions(nil)

	// Then...
	assert.Nil(t, result)
}

// ---------------------------------------------------------------------------
// Template rendering tests
// ---------------------------------------------------------------------------

const pomTemplate = `{{range .Artifacts}}<dependency>
<groupId>{{.GroupId}}</groupId>
<artifactId>{{.ArtifactId}}</artifactId>{{if .Version}}
<version>{{.Version}}</version>{{end}}{{if .Exclusions}}
<exclusions>{{range .Exclusions}}
<exclusion>
<groupId>{{.GroupId}}</groupId>
<artifactId>{{.ArtifactId}}</artifactId>
</exclusion>{{end}}
</exclusions>{{end}}
</dependency>
{{end}}`

func renderTemplate(t *testing.T, data templateData) string {
	t.Helper()
	tmpl, err := template.New("test").Parse(pomTemplate)
	assert.Nil(t, err)
	var buf bytes.Buffer
	err = tmpl.Execute(&buf, data)
	assert.Nil(t, err)
	return buf.String()
}

func TestTemplateRendering_ArtifactWithExclusions_RendersExclusionsBlock(t *testing.T) {
	// Given...
	data := templateData{
		Release: "1.0.0",
		Artifacts: []artifact{
			{
				GroupId:    "com.example",
				ArtifactId: "some-artifact",
				Version:    "1.2.3",
				Exclusions: []exclusion{
					{GroupId: "org.unwanted", ArtifactId: "transitive-dep"},
				},
			},
		},
	}

	// When...
	output := renderTemplate(t, data)

	// Then...
	assert.True(t, strings.Contains(output, "<exclusions>"), "expected <exclusions> block in output")
	assert.True(t, strings.Contains(output, "<exclusion>"), "expected <exclusion> element in output")
	assert.True(t, strings.Contains(output, "<groupId>org.unwanted</groupId>"), "expected exclusion groupId")
	assert.True(t, strings.Contains(output, "<artifactId>transitive-dep</artifactId>"), "expected exclusion artifactId")
}

func TestTemplateRendering_ArtifactWithoutExclusions_NoExclusionsBlock(t *testing.T) {
	// Given...
	data := templateData{
		Release: "1.0.0",
		Artifacts: []artifact{
			{
				GroupId:    "com.example",
				ArtifactId: "plain-artifact",
				Version:    "2.0.0",
			},
		},
	}

	// When...
	output := renderTemplate(t, data)

	// Then...
	assert.False(t, strings.Contains(output, "<exclusions>"), "unexpected <exclusions> block in output")
	assert.True(t, strings.Contains(output, "<artifactId>plain-artifact</artifactId>"))
}
