/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package usersformatter

import (
	"testing"
	"time"

	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/stretchr/testify/assert"
)

func CreateMockUser(loginId string, clientName string, lastLogin string) *galasaapi.UserData {
	var user = galasaapi.NewUserData()
	var client = galasaapi.NewFrontEndClient()
	var client2 = galasaapi.NewFrontEndClient()

	// Define layout for parsing ISO 8601 datetime format
	layout := "2006-01-02T15:04:05.000Z"

	// Parse lastLogin into time.Time
	parsedTime, err := time.Parse(layout, lastLogin)
	if err != nil {
		return nil
	}

	// Set parsed time on the clients
	client.SetClientName("web-ui")
	client.SetLastLogin(parsedTime)

	client2.SetClientName("rest-api")
	client2.SetLastLogin(parsedTime) // Using the same parsed time for demonstration

	user.SetLoginId(loginId)
	user.SetClients([]galasaapi.FrontEndClient{*client, *client2})

	role := galasaapi.NewRBACRole()
	roleMetadata := galasaapi.NewRBACRoleMetadata()
	role.Metadata = roleMetadata
	role.Metadata.SetId("2")
	role.Metadata.SetName("admin")
	role.Metadata.SetUrl("http://myhost/api/roles/2")
	role.Metadata.SetDescription("A test role")

	synthetic := galasaapi.NewUserSynthetics()
	synthetic.SetRole(*role)
	synthetic.Role.SetApiVersion("myVersion")
	user.SetSynthetic(*synthetic)

	return user
}

func TestUserSummaryFormatterNoDataReturnsTotalCountAllZeros(t *testing.T) {
	//Given...
	formatter := NewUserSummaryFormatter()
	// No data to format...
	users := make([]galasaapi.UserData, 0)

	// When...
	actualFormattedOutput, err := formatter.FormatUsers(users)

	//Then...
	assert.Nil(t, err)
	expectedFormattedOutput := "Total:0\n"
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestTokenSummaryFormatterSingleDataReturnsCorrectly(t *testing.T) {
	// Given...
	formatter := NewUserSummaryFormatter()
	// No data to format...x
	users := make([]galasaapi.UserData, 0)
	user1 := CreateMockUser("test-user", "web-ui", "2023-12-03T18:25:43.511Z")
	users = append(users, *user1)

	// When...
	actualFormattedOutput, err := formatter.FormatUsers(users)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput :=
		`login-id  role  web-last-login(UTC) rest-api-last-login(UTC)
test-user admin 2023-12-03 18:25    2023-12-03 18:25

Total:1
`
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestTokenSummaryFormatterMultipleDataSeperatesWithNewLine(t *testing.T) {
	// For..
	formatter := NewUserSummaryFormatter()
	// No data to format...
	users := make([]galasaapi.UserData, 0)
	user1 := CreateMockUser("test-user", "web-ui", "2023-12-03T18:25:43.511Z")
	user2 := CreateMockUser("test-user-2", "web-ui", "2023-12-03T18:25:43.511Z")
	user3 := CreateMockUser("test-user-3", "web-ui", "2023-12-03T18:25:43.511Z")
	users = append(users, *user1, *user2, *user3)

	// When...
	actualFormattedOutput, err := formatter.FormatUsers(users)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput :=
		`login-id    role  web-last-login(UTC) rest-api-last-login(UTC)
test-user   admin 2023-12-03 18:25    2023-12-03 18:25
test-user-2 admin 2023-12-03 18:25    2023-12-03 18:25
test-user-3 admin 2023-12-03 18:25    2023-12-03 18:25

Total:3
`
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}
