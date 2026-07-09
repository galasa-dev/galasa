/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package secrets

import (
	"slices"
	"strings"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
)

type SecretsSetKeystoreValues struct {
	KeystoreFile string
	Base64KeystoreEncoded string
	KeystorePassword      string
    Base64KeystorePassword string
	KeystoreType string
}

func NewSecretsSetKeystoreValues(
	file, encoded, password, base64Password, keystoreType string,
) *SecretsSetKeystoreValues {
    return &SecretsSetKeystoreValues{
        KeystoreFile:           file,
        Base64KeystoreEncoded:  encoded,
        KeystorePassword:       password,
        Base64KeystorePassword: base64Password,
        KeystoreType:           keystoreType,
    }
}

func (k SecretsSetKeystoreValues) Validate() error {
    return k.validateKeystoreType()
}

func (k SecretsSetKeystoreValues) validateKeystoreType() error {
    var err error
    if k.KeystoreType != "" {
        validTypes := []string{"JKS", "PKCS12"}
        if !slices.Contains(validTypes, strings.ToUpper(k.KeystoreType)) {
            err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_KEYSTORE_TYPE)
        }
    }
    return err
}
