/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package streamsformatter

import (
	"strings"

	"github.com/galasa-dev/cli/pkg/galasaapi"
	"gopkg.in/yaml.v3"
)

const (
	YAML_FORMATTER_NAME = "yaml"
)

type StreamsYamlFormatter struct {
}

func NewStreamsYamlFormatter() StreamsFormatter {
	return new(StreamsYamlFormatter)
}

func (*StreamsYamlFormatter) GetName() string {
	return YAML_FORMATTER_NAME
}

func (*StreamsYamlFormatter) FormatStreams(streams []galasaapi.Stream) (string, error) {
	var err error
	buff := strings.Builder{}

	for index, stream := range streams {
		content := ""

		if index > 0 {
			content += "---\n"
		}

		var yamlRepresentationBytes []byte
		yamlRepresentationBytes, err = yaml.Marshal(stream)
		if err == nil {
			yamlStr := string(yamlRepresentationBytes)
			content += yamlStr
		}

		buff.WriteString(content)
	}

	result := buff.String()
	return result, err
}
