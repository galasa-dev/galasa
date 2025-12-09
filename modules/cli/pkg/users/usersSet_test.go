/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package users

import (
	"encoding/json"
	"net/http"
	"testing"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

func TestSetUnknownUserReturnsError(t *testing.T) {
	//Given...
	loginId := "test-user"
	getNamedUsersInteraction := utils.NewHttpInteraction("/users", http.MethodGet)
	getNamedUsersInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		actualLoginId := req.URL.Query().Get("loginId")
		assert.Equal(t, loginId, actualLoginId)

		body := `[]`
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(body))
	}

	interactions := []utils.HttpInteraction{
		getNamedUsersInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiClient := api.InitialiseAPI(server.Server.URL)

	mockByteReader := utils.NewMockByteReader()

	//When
	err := SetUsers(loginId, "2", 100, apiClient, mockByteReader)

	//Then
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "User with login id test-user is not known on the Galasa service")
}

func TestSetUnknownRoleReturnsError(t *testing.T) {
	//Given...
	loginId := "test-user"
	getNamedUsersInteraction := utils.NewHttpInteraction("/users", http.MethodGet)
	getNamedUsersInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		actualLoginId := req.URL.Query().Get("loginId")
		assert.Equal(t, loginId, actualLoginId)

		body := `
			[{
				"url": "http://localhost:8080/users/d2055afbc0ae6e513fa9b23c1a000d9f",
				"login-id": "test-user",
				"role" : "2",
				"id": "usernumber201",
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
			}]
		`
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(body))
	}

	getRolesInteraction := utils.NewHttpInteraction("/rbac/roles", http.MethodGet)
	getRolesInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		body := `[]`
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(body))
	}

	interactions := []utils.HttpInteraction{
		getNamedUsersInteraction,
		getRolesInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiClient := api.InitialiseAPI(server.Server.URL)

	mockByteReader := utils.NewMockByteReader()

	//When
	err := SetUsers(loginId, "2", 100, apiClient, mockByteReader)

	//Then
	assert.NotNil(t, err)
	assert.ErrorContains(t, err, "Role name 2 is not known on the Galasa service")
}

func TestSetUserRoleAndPriorityGoodPath(t *testing.T) {
	//Given...
	loginId := "test-user"
	getNamedUsersInteraction := utils.NewHttpInteraction("/users", http.MethodGet)
	getNamedUsersInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		actualLoginId := req.URL.Query().Get("loginId")
		assert.Equal(t, loginId, actualLoginId)

		body := `
			[{
				"url": "http://localhost:8080/users/d2055afbc0ae6e513fa9b23c1a000d9f",
				"login-id": "test-user",
				"role" : "2",
				"id": "usernumber201",
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
			}]
		`
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(body))
	}

	getRolesInteraction := utils.NewHttpInteraction("/rbac/roles", http.MethodGet)
	getRolesInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		body := `
			[{
				"kind": "GalasaRole",
				"apiVersion": "galasa-dev/v1alpha1",
				"metadata": {
					"url": "http://myhost.com:8080/rbac/roles/2",
					"name": "admin",
					"id": "2",
					"description": "Administrator access"
				},
				"data": {
					"actions": []
				}
			}]
		`
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(body))
	}

	putNamedUsersInteraction := utils.NewHttpInteraction("/users/usernumber201", http.MethodPut)
	putNamedUsersInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		loginId := req.URL.Query().Get("login-id")
		assert.NotNil(t, loginId)
		var userUpdateData galasaapi.UserUpdateData

		// We expect the code to send an update user structure with the role of 2 inside.
		err := json.NewDecoder(req.Body).Decode(&userUpdateData)
		assert.Nil(t, err)
		assert.Equal(t, *userUpdateData.Role, "2")
		assert.Equal(t, *userUpdateData.Priority, int32(100))

		body := `
			{
				"url": "http://localhost:8080/users/d2055afbc0ae6e513fa9b23c1a000d9f",
				"login-id": "test-user",
				"role" : "2",
				"id": "usernumber201",
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
		`
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(body))
	}

	interactions := []utils.HttpInteraction{
		getNamedUsersInteraction,
		getRolesInteraction,
		putNamedUsersInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiClient := api.InitialiseAPI(server.Server.URL)

	mockByteReader := utils.NewMockByteReader()

	//When
	err := SetUsers(loginId, "2", 100, apiClient, mockByteReader)

	//Then
	assert.Nil(t, err)
}

func TestSetUserPriorityGoodPath(t *testing.T) {
	//Given...
	loginId := "test-user"
	getNamedUsersInteraction := utils.NewHttpInteraction("/users", http.MethodGet)
	getNamedUsersInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		actualLoginId := req.URL.Query().Get("loginId")
		assert.Equal(t, loginId, actualLoginId)

		body := `
			[{
				"url": "http://localhost:8080/users/d2055afbc0ae6e513fa9b23c1a000d9f",
				"login-id": "test-user",
				"role" : "2",
				"id": "usernumber201",
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
			}]
		`
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(body))
	}

	putNamedUsersInteraction := utils.NewHttpInteraction("/users/usernumber201", http.MethodPut)
	putNamedUsersInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		loginId := req.URL.Query().Get("login-id")
		assert.NotNil(t, loginId)
		var userUpdateData galasaapi.UserUpdateData

		// We expect the code to send an update user structure with the role of 2 inside.
		err := json.NewDecoder(req.Body).Decode(&userUpdateData)
		assert.Nil(t, err)
		assert.Nil(t, userUpdateData.Role)
		assert.Equal(t, *userUpdateData.Priority, int32(100))

		body := `
			{
				"url": "http://localhost:8080/users/d2055afbc0ae6e513fa9b23c1a000d9f",
				"login-id": "test-user",
				"role" : "2",
				"id": "usernumber201",
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
		`
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(body))
	}

	interactions := []utils.HttpInteraction{
		getNamedUsersInteraction,
		putNamedUsersInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiClient := api.InitialiseAPI(server.Server.URL)

	mockByteReader := utils.NewMockByteReader()

	//When
	err := SetUsers(loginId, "", 100, apiClient, mockByteReader)

	//Then
	assert.Nil(t, err)
}

func TestSetUserRoleGoodPath(t *testing.T) {
	//Given...
	loginId := "test-user"
	getNamedUsersInteraction := utils.NewHttpInteraction("/users", http.MethodGet)
	getNamedUsersInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		actualLoginId := req.URL.Query().Get("loginId")
		assert.Equal(t, loginId, actualLoginId)

		body := `
			[{
				"url": "http://localhost:8080/users/d2055afbc0ae6e513fa9b23c1a000d9f",
				"login-id": "test-user",
				"role" : "2",
				"id": "usernumber201",
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
			}]
		`
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(body))
	}

	getRolesInteraction := utils.NewHttpInteraction("/rbac/roles", http.MethodGet)
	getRolesInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		body := `
			[{
				"kind": "GalasaRole",
				"apiVersion": "galasa-dev/v1alpha1",
				"metadata": {
					"url": "http://myhost.com:8080/rbac/roles/2",
					"name": "admin",
					"id": "2",
					"description": "Administrator access"
				},
				"data": {
					"actions": []
				}
			}]
		`
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(body))
	}

	putNamedUsersInteraction := utils.NewHttpInteraction("/users/usernumber201", http.MethodPut)
	putNamedUsersInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		loginId := req.URL.Query().Get("login-id")
		assert.NotNil(t, loginId)
		var userUpdateData galasaapi.UserUpdateData

		// We expect the code to send an update user structure with the role of 2 inside.
		err := json.NewDecoder(req.Body).Decode(&userUpdateData)
		assert.Nil(t, err)
		assert.Equal(t, *userUpdateData.Role, "2")
		assert.Nil(t, userUpdateData.Priority)

		body := `
			{
				"url": "http://localhost:8080/users/d2055afbc0ae6e513fa9b23c1a000d9f",
				"login-id": "test-user",
				"role" : "2",
				"id": "usernumber201",
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
		`
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(body))
	}

	interactions := []utils.HttpInteraction{
		getNamedUsersInteraction,
		getRolesInteraction,
		putNamedUsersInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiClient := api.InitialiseAPI(server.Server.URL)

	mockByteReader := utils.NewMockByteReader()

	//When
	err := SetUsers(loginId, "2", DEFAULT_EMPTY_PRIORITY, apiClient, mockByteReader)

	//Then
	assert.Nil(t, err)
}

func TestSendUpdateOfUserGoodPath(t *testing.T) {
	//Given...
	getNamedUsersInteraction := utils.NewHttpInteraction("/users/usernumber201", http.MethodPut)
	getNamedUsersInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		loginId := req.URL.Query().Get("login-id")
		assert.NotNil(t, loginId)
		var userUpdateData galasaapi.UserUpdateData

		// We expect the code to send an update user structure with the role of 2 inside.
		err := json.NewDecoder(req.Body).Decode(&userUpdateData)
		assert.Nil(t, err)
		assert.Equal(t, *userUpdateData.Role, "2")
		assert.Equal(t, *userUpdateData.Priority, int32(100))

		body := `
			{
				"url": "http://localhost:8080/users/d2055afbc0ae6e513fa9b23c1a000d9f",
				"login-id": "test-user",
				"role" : "2",
				"id": "usernumber201",
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
		`
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(body))
	}

	interactions := []utils.HttpInteraction{
		getNamedUsersInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiClient := api.InitialiseAPI(server.Server.URL)

	mockByteReader := utils.NewMockByteReader()

	//When
	updatedUser, err := sendUserUpdateToRestApi("usernumber201", "2", 100, apiClient, "user201loginId", mockByteReader)

	//Then
	assert.Nil(t, err)
	assert.NotNil(t, updatedUser)
	assert.Equal(t, *updatedUser.LoginId, "test-user")
}

func TestSendUpdateWithoutPriorityIsAccepted(t *testing.T) {
	//Given...
	getNamedUsersInteraction := utils.NewHttpInteraction("/users/usernumber201", http.MethodPut)
	getNamedUsersInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		loginId := req.URL.Query().Get("login-id")
		assert.NotNil(t, loginId)
		var userUpdateData galasaapi.UserUpdateData

		// We expect the code to send an update user structure with the role of 2 inside.
		err := json.NewDecoder(req.Body).Decode(&userUpdateData)
		assert.Nil(t, err)
		assert.Equal(t, *userUpdateData.Role, "2")
		assert.Nil(t, userUpdateData.Priority)

		body := `
			{
				"url": "http://localhost:8080/users/d2055afbc0ae6e513fa9b23c1a000d9f",
				"login-id": "test-user",
				"role" : "2",
				"id": "usernumber201",
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
		`
		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(body))
	}

	interactions := []utils.HttpInteraction{
		getNamedUsersInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiClient := api.InitialiseAPI(server.Server.URL)

	mockByteReader := utils.NewMockByteReader()

	//When
	updatedUser, err := sendUserUpdateToRestApi("usernumber201", "2", DEFAULT_EMPTY_PRIORITY, apiClient, "user201loginId", mockByteReader)

	//Then
	assert.Nil(t, err)
	assert.NotNil(t, updatedUser)
	assert.Equal(t, *updatedUser.LoginId, "test-user")
}

func TestGetRoleFromRestApiReturnsRoleOk(t *testing.T) {
	//Given...
	getNamedUsersInteraction := utils.NewHttpInteraction("/rbac/roles", http.MethodGet)
	getNamedUsersInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		// Prepare a fake role response.
		assert.Equal(t, req.URL.RawQuery, "name=admin")
		roleName := "admin"
		role := &galasaapi.RBACRole{
			Metadata: &galasaapi.RBACRoleMetadata{
				Name: &roleName,
			},
		}

		roles := make([]*galasaapi.RBACRole, 1)
		roles[0] = role
		bodyBytes, _ := json.MarshalIndent(roles, "", "  ")

		writer.Header().Set("Content-Type", "application/json")
		writer.Header().Set("ClientApiVersion", "myVersion")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(bodyBytes))
	}

	interactions := []utils.HttpInteraction{
		getNamedUsersInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiClient := api.InitialiseAPI(server.Server.URL)

	//When
	roleGotBack, err := getRoleFromRestApi("admin", apiClient)

	//Then
	assert.Nil(t, err)
	// Check that the role we queried came back OK.
	assert.NotNil(t, *roleGotBack)
	assert.Equal(t, *roleGotBack.GetMetadata().Name, "admin")
}
