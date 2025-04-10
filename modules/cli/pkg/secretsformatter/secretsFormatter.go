/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package secretsformatter

import (
	"github.com/galasa-dev/cli/pkg/galasaapi"
)

// Displays secrets in the following format:
// name
// SYSTEM1
// MY_ZOS_SECRET
// ANOTHER-SECRET
// Total:3

// -----------------------------------------------------
// SecretsFormatter - implementations can take a collection of secrets
// and turn them into a string for display to the user.
const (
	HEADER_SECRET_NAME        = "name"
	HEADER_SECRET_TYPE        = "type"
	HEADER_SECRET_DESCRIPTION = "description"
	HEADER_LAST_UPDATED_TIME  = "last-updated(UTC)"
	HEADER_LAST_UPDATED_BY    = "last-updated-by"
)

type SecretsFormatter interface {
	FormatSecrets(secrets []galasaapi.GalasaSecret) (string, error)
	GetName() string
}
