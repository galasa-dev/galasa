/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package secretsformatter

import (
	"strings"

	"github.com/galasa-dev/cli/pkg/galasaapi"
	"gopkg.in/yaml.v3"
)

const (
	YAML_FORMATTER_NAME = "yaml"
)

type SecretYamlFormatter struct {
}

func NewSecretYamlFormatter() SecretsFormatter {
	return new(SecretYamlFormatter)
}

func (*SecretYamlFormatter) GetName() string {
	return YAML_FORMATTER_NAME
}

func (*SecretYamlFormatter) FormatSecrets(secrets []galasaapi.GalasaSecret) (string, error) {
	var err error
	buff := strings.Builder{}

	for index, secret := range secrets {
		secretString := ""

		if index > 0 {
			secretString += "---\n"
		}

		var yamlRepresentationBytes []byte
		yamlRepresentationBytes, err = yaml.Marshal(secret)
		if err == nil {
			yamlStr := string(yamlRepresentationBytes)
			secretString += yamlStr
		}

		buff.WriteString(secretString)
	}

	result := buff.String()
	return result, err
}
