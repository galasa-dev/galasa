/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package users

import (
	"encoding/json"
	"fmt"
	"net/http"
	"strconv"
	"testing"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/utils"

	"github.com/stretchr/testify/assert"
)

func createMockUser(userNumber string, loginId string) galasaapi.UserData {

	user := *galasaapi.NewUserData()
	user.SetId(userNumber)
	user.SetLoginId(loginId)
	user.SetUrl("/my-api-server")

	return user
}

func WriteMockUserResponse(
	t *testing.T,
	writer http.ResponseWriter,
	req *http.Request,
	loginId string,
	userResultStrings []string) {

	writer.Header().Set("Content-Type", "application/json")
	values := req.URL.Query()
	userQueryParamter := values.Get("loginId")

	assert.Equal(t, userQueryParamter, loginId)

	writer.Write([]byte(fmt.Sprintf(`
	[{
		"id": "dummy-doc-id",
		"login-id": "%s",
		"url": "/my-api-server"
	}]`, loginId)))

}

func TestUserDeleteAUser(t *testing.T) {

	//Given...
	userNumber := "dummy-doc-id"
	loginId := "admin"

	userToDelete := createMockUser(userNumber, loginId)
	userToDeleteBytes, _ := json.Marshal(userToDelete)
	userToDeleteJson := string(userToDeleteBytes)

	getUserInteraction := utils.NewHttpInteraction("/users", http.MethodGet)
	getUserInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		WriteMockUserResponse(t, writer, req, loginId, []string{userToDeleteJson})
	}

	deleteUserInteraction := utils.NewHttpInteraction("/users/"+userNumber, http.MethodDelete)
	deleteUserInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.WriteHeader(http.StatusNoContent)
	}

	interactions := []utils.HttpInteraction{
		getUserInteraction,
		deleteUserInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	console := utils.NewMockConsole()
	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()

	// When...
	err := DeleteUser(
		loginId,
		apiClient,
		mockByteReader)

	// Then...
	assert.Nil(t, err, "DeleteUser returned an unexpected error")
	assert.Empty(t, console.ReadText(), "The console was written to on a successful deletion, it should be empty")
}

func TestUserDeleteAUserThrowsAnUnexpectedError(t *testing.T) {

	//Given...
	userNumber := "dummy-doc-id"
	loginId := "admin"

	userToDelete := createMockUser(userNumber, loginId)
	userToDeleteBytes, _ := json.Marshal(userToDelete)
	userToDeleteJson := string(userToDeleteBytes)

	getUserInteraction := utils.NewHttpInteraction("/users", http.MethodGet)
	getUserInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		WriteMockUserResponse(t, writer, req, loginId, []string{userToDeleteJson})
	}

	deleteUserInteraction := utils.NewHttpInteraction("/users/"+userNumber, http.MethodDelete)
	deleteUserInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusInternalServerError)
		writer.Write([]byte(`{}`))
	}

	interactions := []utils.HttpInteraction{
		getUserInteraction,
		deleteUserInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()

	// When...
	err := DeleteUser(
		loginId,
		apiClient,
		mockByteReader)

	// Then...
	assert.NotNil(t, err, "DeleteUser returned an unexpected error")
	assert.Contains(t, err.Error(), strconv.Itoa(http.StatusInternalServerError))
	assert.Contains(t, err.Error(), "GAL1201E")
	assert.Contains(t, err.Error(), "An attempt to delete a user", loginId)
}

func TestUserDeleteAUserNotFoundThrowsError(t *testing.T) {

	//Given...
	loginId := "admin"

	getUserInteraction := utils.NewHttpInteraction("/users", http.MethodGet)
	getUserInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		writer.WriteHeader(http.StatusOK)
		writer.Write([]byte(`[]`))
	}

	interactions := []utils.HttpInteraction{
		getUserInteraction,
	}

	server := utils.NewMockHttpServer(t, interactions)
	defer server.Server.Close()

	apiServerUrl := server.Server.URL
	apiClient := api.InitialiseAPI(apiServerUrl)
	mockByteReader := utils.NewMockByteReader()

	// When...
	err := DeleteUser(
		loginId,
		apiClient,
		mockByteReader)

	// Then...
	assert.NotNil(t, err, "DeleteUser returned an unexpected error")
	assert.Contains(t, err.Error(), "GAL1196E")
	assert.Contains(t, err.Error(), "The user could not be deleted by login ID because it was not found by the Galasa service.")
}
