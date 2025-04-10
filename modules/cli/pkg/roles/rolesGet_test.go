/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package roles

import (
	"encoding/json"
	"net/http"
	"testing"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

const (
	API_VERSION = "galasa-dev/v1alpha1"
)

func createTestGalasaRole(id string, name string, description string) galasaapi.RBACRole {
	role := *galasaapi.NewRBACRole()

	role.SetApiVersion(API_VERSION)
	role.SetKind("GalasaRole")

	metadata := *galasaapi.NewRBACRoleMetadata()
	metadata.SetName(name)
	metadata.SetId(id)
	if description != "" {
		metadata.SetDescription(description)
	}
	metadata.SetUrl("https://myhost:myport/rbac/roles/" + id)

	data := *galasaapi.NewRBACRoleData()
	actionStrings := make([]string, 2)
	actionStrings[0] = "action1"
	actionStrings[1] = "action2"
	data.SetActions(actionStrings)

	role.SetMetadata(metadata)
	role.SetData(data)

	return role
}

func TestGetANamedRoleWhenRoleExistsFindsItOkSummaryFormat(t *testing.T) {
	// Given...
	roleId := "role1"
	roleName := "role1Name"
	description := "role1Description"
	outputFormat := "summary"

	// Create the test role array to return
	role := createTestGalasaRole(roleId, roleName, description)
	roles := make([]galasaapi.RBACRole, 0)
	roles = append(roles, role)
	rolesBytes, _ := json.Marshal(roles)
	rolesJson := string(rolesBytes)

	// Create the expected HTTP interactions with the API server.
	// We expect it to call /rbac/roles to get all the roles, then use that to find the one we name.
	getRoleInteraction := utils.NewHttpInteraction("/rbac/roles", http.MethodGet)
	getRoleInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(rolesJson))
	}

	interactions := []utils.HttpInteraction{
		getRoleInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()

	// When...
	err := GetRoles(
		roleName,
		outputFormat,
		console,
		apiClient,
		mockByteReader)

	// Then...
	expectedOutput :=
		`name      description
role1Name role1Description

Total:1
`
	assert.Nil(t, err, "GetRoles returned an unexpected error")
	assert.Equal(t, expectedOutput, console.ReadText())
}

func TestCanGetAllRolesOkSummaryFormat(t *testing.T) {
	// Given...
	roleId := "role1"
	roleName := "role1Name"
	description := "role1Description"
	outputFormat := "summary"
	roleNameToLookFor := ""

	// Create the test role array to return
	role := createTestGalasaRole(roleId, roleName, description)
	roles := make([]galasaapi.RBACRole, 0)
	roles = append(roles, role)
	rolesBytes, _ := json.Marshal(roles)
	rolesJson := string(rolesBytes)

	// Create the expected HTTP interactions with the API server.
	// We expect it to call /rbac/roles to get all the roles, then use that to find the one we name.
	getRoleInteraction := utils.NewHttpInteraction("/rbac/roles", http.MethodGet)
	getRoleInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(rolesJson))
	}

	interactions := []utils.HttpInteraction{
		getRoleInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()

	// When...
	err := GetRoles(
		roleNameToLookFor,
		outputFormat,
		console,
		apiClient,
		mockByteReader)

	// Then...
	expectedOutput :=
		`name      description
role1Name role1Description

Total:1
`
	assert.Nil(t, err, "GetRoles returned an unexpected error")
	assert.Equal(t, expectedOutput, console.ReadText())
}

func TestCanGetARoleByIdWhenRoleExistsFindsItOkInYamlFormat(t *testing.T) {
	// Given...
	roleId := "role1"
	roleName := "role1Name"
	description := "role1Description"
	outputFormat := "yaml"

	// Create the test role array to return
	role := createTestGalasaRole(roleId, roleName, description)
	roles := make([]galasaapi.RBACRole, 0)
	roles = append(roles, role)
	rolesBytes, _ := json.Marshal(roles)
	rolesJson := string(rolesBytes)

	// Create the expected HTTP interactions with the API server.
	// We expect it to call /rbac/roles to get all the roles, then use that to find the one we name.
	getRoleInteraction := utils.NewHttpInteraction("/rbac/roles", http.MethodGet)
	getRoleInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(rolesJson))
	}

	interactions := []utils.HttpInteraction{
		getRoleInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()

	// When...
	err := GetRoles(
		roleName,
		outputFormat,
		console,
		apiClient,
		mockByteReader)

	// Then...
	expectedOutput :=
		`apiVersion: galasa-dev/v1alpha1
kind: GalasaRole
metadata:
    id: role1
    name: role1Name
    description: role1Description
    url: https://myhost:myport/rbac/roles/role1
data:
    actions:
        - action1
        - action2
`
	assert.Nil(t, err, "GetRoles returned an unexpected error")
	assert.Equal(t, expectedOutput, console.ReadText())
}

func TestCanGetARoleByIdWhenRoleDoesNotExistCausesError(t *testing.T) {
	// Given...
	outputFormat := "summary"

	// Create the test role array to return
	rolesJson := "[]"

	// Create the expected HTTP interactions with the API server.
	// We expect it to call /rbac/roles to get all the roles, then use that to find the one we name.
	getRoleInteraction := utils.NewHttpInteraction("/rbac/roles", http.MethodGet)
	getRoleInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(rolesJson))
	}

	interactions := []utils.HttpInteraction{
		getRoleInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()

	// When...
	err := GetRoles(
		"NotTheCorrectName",
		outputFormat,
		console,
		apiClient,
		mockByteReader)

	// Then...
	// We expect an error.
	assert.NotNil(t, err, "GetRoles didnt return an expected error")
	assert.Contains(t, err.Error(), "GAL1210E")
}

func TestTryGettingAnythingWithAnInvalidFormatterNameFailsImmediately(t *testing.T) {
	// Not expecting any iteractions...
	interactions := []utils.HttpInteraction{}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()

	err := GetRoles(
		"validRoleName",
		"unknownOutputFormatInvalid",
		console,
		apiClient,
		mockByteReader)

	assert.NotNil(t, err, "Expected an error, didn't get one!")
	assert.Contains(t, err.Error(), "GAL1067E")
}

func TestCanGetARoleByIdWhenRoleExistsFindsItOkSummaryFormat(t *testing.T) {
	// Given...
	roleName := "role1Name"
	outputFormat := "summary"

	// Create the expected HTTP interactions with the API server.
	// We expect it to call /rbac/roles to get all the roles, then use that to find the one we name.
	getRoleInteraction := utils.NewHttpInteraction("/rbac/roles", http.MethodGet)
	getRoleInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte("Not valid json format output from fake service"))
	}

	interactions := []utils.HttpInteraction{
		getRoleInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()

	// When...
	err := GetRoles(
		roleName,
		outputFormat,
		console,
		apiClient,
		mockByteReader)

	// Then...
	assert.NotNil(t, err, "GetRoles returned an no error when one was expected")
	assert.Contains(t, err.Error(), "GAL1206E")
}
