/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package monitorsformatter

import (
	"strings"

	"github.com/galasa-dev/cli/pkg/galasaapi"
	"gopkg.in/yaml.v3"
)

const (
	YAML_FORMATTER_NAME = "yaml"
)

type MonitorsYamlFormatter struct {
}

func NewMonitorsYamlFormatter() MonitorsFormatter {
	return new(MonitorsYamlFormatter)
}

func (*MonitorsYamlFormatter) GetName() string {
	return YAML_FORMATTER_NAME
}

func (*MonitorsYamlFormatter) FormatMonitors(monitors []galasaapi.GalasaMonitor) (string, error) {
	var err error
	buff := strings.Builder{}

	for index, monitor := range monitors {
		content := ""

		if index > 0 {
			content += "---\n"
		}

		var yamlRepresentationBytes []byte
		yamlRepresentationBytes, err = yaml.Marshal(monitor)
		if err == nil {
			yamlStr := string(yamlRepresentationBytes)
			content += yamlStr
		}

		buff.WriteString(content)
	}

	result := buff.String()
	return result, err
}
