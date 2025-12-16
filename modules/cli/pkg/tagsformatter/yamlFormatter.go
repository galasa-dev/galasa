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
