/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package users

import (
	"net/http"
	"testing"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

func TestMultipleUsersGetFormatsResultsOk(t *testing.T) {
	//Given...

	body := `
[
    {
		"url": "http://localhost:8080/users/d2055afbc0ae6e513fa9b23c1a000d9f",
		"login-id": "test-user",
		"role" : "2",
		"id": "d2055afbc0ae6e513fa9b23c1a000d9f",
		"clients": [
			{
				"last-login": "2024-10-28T14:54:49.546029Z",
				"client-name": "web-ui"
			}
		],
		"synthetic" : {
			"role": {
				"metadata" : {
					"name" : "admin"
				}
			}
		}
    },    
	{
		"url": "http://localhost:8080/users/d2055afbc0ae6e513fa9b23c1a000d9f",
		"login-id": "test-user2",
		"role" : "2",
		"id": "d2055afbc0ae6e513fa9b23c1a000d9f",
		"clients": [
			{
				"last-login": "2024-10-28T14:54:49.546029Z",
				"client-name": "web-ui"
			}
		],
		"synthetic" : {
			"role": {
				"metadata" : {
					"name" : "admin"
				}
			}
		}
    },
	{
		"url": "http://localhost:8080/users/d2055afbc0ae6e513fa9b23c1a000d9f",
		"login-id": "test-user3",
		"role" : "2",
		"id": "d2055afbc0ae6e513fa9b23c1a000d9f",
		"clients": [
			{
				"last-login": "2024-10-28T14:54:49.546029Z",
				"client-name": "web-ui"
			},
			{
				"last-login": "2024-10-28T15:32:49.546029Z",
				"client-name": "rest-api"
			}
		],
		"synthetic" : {
			"role": {
				"metadata" : {
					"name" : "admin"
				}
			}
		}
    }    
]
`

	getUsersInteraction := utils.NewHttpInteraction("/users", http.MethodGet)
	getUsersInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(body))
	}

	interactions := []utils.HttpInteraction{
		getUsersInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiClient := api.InitialiseAPI(server.Server.URL)

	console := utils.NewMockConsole()
	expectedOutput := `login-id   role  web-last-login(UTC) rest-api-last-login(UTC)
test-user  admin 2024-10-28 14:54    
test-user2 admin 2024-10-28 14:54    
test-user3 admin 2024-10-28 14:54    2024-10-28 15:32

Total:3
`

	//When
	err := GetUsers("", apiClient, console)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, console.ReadText())
}

func TestMissingLoginIdFlagReturnsBadRequest(t *testing.T) {
	//Given...

	body := `{"error_code": 1155,"error_message": "GAL1155E: The id provided by the --login-id field cannot be an empty string."}`

	getUsersInteraction := utils.NewHttpInteraction("/users", http.MethodGet)
	getUsersInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusBadRequest) // It's going to fail with an error on purpose !
		writer.Write([]byte(body))
	}

	interactions := []utils.HttpInteraction{
		getUsersInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiClient := api.InitialiseAPI(server.Server.URL)

	console := utils.NewMockConsole()
	expectedOutput := `GAL1155E: The loginId provided by the --login-id field cannot be an empty string.`

	//When
	err := GetUsers("     ", apiClient, console)

	//Then
	assert.NotNil(t, err)
	assert.Equal(t, expectedOutput, err.Error())
}

func TestGetTokensByLoginIdReturnsOK(t *testing.T) {
	//Given...
	body := `
[
	{
		"url": "http://localhost:8080/users/d2055afbc0ae6e513fa9b23c1a000d9f",
		"login-id": "test-user",
		"id": "d2055afbc0ae6e513fa9b23c1a000d9f",
		"clients": [
			{
				"last-login": "2024-10-28T14:54:49.546029Z",
				"client-name": "web-ui"
			}
		],
		"synthetic" : {
			"role": {
				"metadata" : {
					"name" : "admin"
				}
			}
		}
	}
]
	`

	getUsersInteraction := utils.NewHttpInteraction("/users", http.MethodGet)
	getUsersInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(body))
	}

	interactions := []utils.HttpInteraction{
		getUsersInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiClient := api.InitialiseAPI(server.Server.URL)

	console := utils.NewMockConsole()
	expectedOutput := `login-id  role  web-last-login(UTC) rest-api-last-login(UTC)
test-user admin 2024-10-28 14:54    

Total:1
`

	//When
	err := GetUsers("test-user", apiClient, console)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, console.ReadText())
}
