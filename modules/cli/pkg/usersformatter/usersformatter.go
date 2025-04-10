/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package usersformatter

import (
	"github.com/galasa-dev/cli/pkg/galasaapi"
)

//Print in the following fashion:
// login-id                web-last-login   rest-api-last-login
// mcobbett@mydomain.co.uk 2024-09-08:14:24 2024-09-08:14:24
// eamon@mydomain.co.uk    2024-09-08:15:18 2024-09-08:15:18
//
// Total:2

// -----------------------------------------------------
// UsersFormatter - implementations can take a collection of user results
// and turn them into a string for display to the user.
const (
	HEADER_USER_LOGIN_ID      = "login-id"
	HEADER_USER_ROLE          = "role"
	HEADER_WEBUI_LAST_LOGIN   = "web-last-login(UTC)"
	HEADER_RESTAPI_LAST_LOGIN = "rest-api-last-login(UTC)"
)

type UserFormatter interface {
	FormatUsers(userResults []galasaapi.UserData) (string, error)
	GetName() string
}
