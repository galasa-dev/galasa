/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tagsformatter

import (
	"strings"

	"github.com/galasa-dev/cli/pkg/galasaapi"
	"gopkg.in/yaml.v3"
)

// -----------------------------------------------------
// Displays tags in the following YAML format:
//
// apiVersion: galasa-dev/v1alpha1
// kind: GalasaTag
// metadata:
//     name: core-regression
//     description: my core regression tag
// data:
//     priority: 100
// ---
// apiVersion: galasa-dev/v1alpha1
// kind: GalasaTag
// metadata:
//     name: my-other-tag
//     description: example test tag
// data:
//     priority: 1

const (
	YAML_FORMATTER_NAME = "yaml"
)

type TagYamlFormatter struct {
}

func NewTagYamlFormatter() TagsFormatter {
	return new(TagYamlFormatter)
}

func (*TagYamlFormatter) GetName() string {
	return YAML_FORMATTER_NAME
}

func (*TagYamlFormatter) FormatTags(tags []galasaapi.GalasaTag) (string, error) {
	var err error
	buff := strings.Builder{}

	for index, tag := range tags {
		tagString := ""

		if index > 0 {
			tagString += "---\n"
		}

		var yamlRepresentationBytes []byte
		yamlRepresentationBytes, err = yaml.Marshal(tag)
		if err == nil {
			yamlStr := string(yamlRepresentationBytes)
			tagString += yamlStr
		}

		buff.WriteString(tagString)
	}

	result := buff.String()
	return result, err
}
