/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package rolesformatter

import (
	"github.com/galasa-dev/cli/pkg/galasaapi"
)

// Displays roles in the following format:
// name            description
// admin           Someone with super-user access
// tester          Able to write and launch tests
// Total:2

// -----------------------------------------------------
// RoleFormatter - implementations can take a collection of roles
// and turn them into a string for display to the user.
const (
	HEADER_ROLE_NAME        = "name"
	HEADER_ROLE_DESCRIPTION = "description"
)

type RolesFormatter interface {
	FormatRoles(roles []galasaapi.RBACRole) (string, error)
	GetName() string
}
