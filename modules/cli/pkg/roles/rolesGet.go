/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package roles

import (
	"context"
	"log"
	"net/http"
	"sort"
	"strings"

	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/rolesformatter"
	"github.com/galasa-dev/cli/pkg/spi"
)

var (
	formatters = createFormatters()
)

func GetRoles(
	roleName string,
	format string,
	console spi.Console,
	apiClient *galasaapi.APIClient,
	byteReader spi.ByteReader,
) error {
	var err error
	var chosenFormatter rolesformatter.RolesFormatter
	roles := make([]galasaapi.RBACRole, 0)

	chosenFormatter, err = validateFormatFlag(format)
	if err == nil {
		log.Printf("formatter flag is valid.\n")
		if roleName != "" {
			// The user has provided a Role name, so try to get that Role
			var role *galasaapi.RBACRole
			role, err = getRoleByName(roleName, apiClient, byteReader)
			if err == nil {
				roles = append(roles, *role)
			}
		} else {
			// Get all Roles
			roles, err = getRolesFromRestApi(apiClient, byteReader)
		}

		// If we were able to get the Roles, format them as requested by the user
		if err == nil {
			var formattedOutput string
			formattedOutput, err = chosenFormatter.FormatRoles(roles)
			if err == nil {
				console.WriteString(formattedOutput)
			}
		}
	}
	log.Printf("GetRoles exiting. err is %v\n", err)
	return err
}

func getRoleByName(
	roleName string,
	apiClient *galasaapi.APIClient,
	byteReader spi.ByteReader,
) (*galasaapi.RBACRole, error) {
	var err error
	var role *galasaapi.RBACRole

	roleName, err = validateRoleName(roleName)
	if err == nil {
		role, err = getRoleFromRestApiGivenName(roleName, apiClient, byteReader)
	}

	return role, err
}

func getRoleFromRestApiGivenName(
	roleName string,
	apiClient *galasaapi.APIClient,
	byteReader spi.ByteReader,
) (*galasaapi.RBACRole, error) {

	var roles []galasaapi.RBACRole
	var role *galasaapi.RBACRole
	var httpResponse *http.Response
	var context context.Context = context.Background()

	restApiVersion, err := embedded.GetGalasactlRestApiVersion()
	if err == nil {
		log.Printf("Getting single role data from remote service\n")
		roles, httpResponse, err = apiClient.RoleBasedAccessControlAPIApi.GetRBACRoles(context).
			ClientApiVersion(restApiVersion).
			Name(roleName).
			Execute()

		if httpResponse != nil {
			defer httpResponse.Body.Close()
		}

		if err != nil {
			if httpResponse == nil {
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_GET_ROLES_REQUEST_FAILED, err.Error())
			} else {
				err = galasaErrors.HttpResponseToGalasaError(
					httpResponse,
					"",
					byteReader,
					galasaErrors.GALASA_ERROR_GET_ROLES_NO_RESPONSE_CONTENT,
					galasaErrors.GALASA_ERROR_GET_ROLES_RESPONSE_BODY_UNREADABLE,
					galasaErrors.GALASA_ERROR_GET_ROLES_UNPARSEABLE_CONTENT,
					galasaErrors.GALASA_ERROR_GET_ROLES_SERVER_REPORTED_ERROR,
					galasaErrors.GALASA_ERROR_GET_ROLES_EXPLANATION_NOT_JSON,
				)
			}
		} else {
			log.Printf("Got back %v roles %v", len(roles), roles)
			if len(roles) < 1 {
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_ROLE_NAME_NOT_FOUND, roleName)
			} else {
				role = &roles[0]
			}
		}
	}

	return role, err
}

func getRolesFromRestApi(
	apiClient *galasaapi.APIClient,
	byteReader spi.ByteReader,
) ([]galasaapi.RBACRole, error) {
	var err error
	var httpResponse *http.Response
	var context context.Context = context.Background()
	var restApiVersion string
	var roles []galasaapi.RBACRole

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	if err == nil {
		log.Printf("Getting role data from remote service\n")
		roles, httpResponse, err = apiClient.RoleBasedAccessControlAPIApi.GetRBACRoles(context).
			ClientApiVersion(restApiVersion).
			Execute()

		if httpResponse != nil {
			defer httpResponse.Body.Close()
		}

		if err != nil {
			if httpResponse == nil {
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_GET_ROLES_REQUEST_FAILED, err.Error())
			} else {
				err = galasaErrors.HttpResponseToGalasaError(
					httpResponse,
					"",
					byteReader,
					galasaErrors.GALASA_ERROR_GET_ROLES_NO_RESPONSE_CONTENT,
					galasaErrors.GALASA_ERROR_GET_ROLES_RESPONSE_BODY_UNREADABLE,
					galasaErrors.GALASA_ERROR_GET_ROLES_UNPARSEABLE_CONTENT,
					galasaErrors.GALASA_ERROR_GET_ROLES_SERVER_REPORTED_ERROR,
					galasaErrors.GALASA_ERROR_GET_ROLES_EXPLANATION_NOT_JSON,
				)
			}
		} else {
			log.Printf("Got back %v roles %v", len(roles), roles)
		}
	}
	return roles, err
}

func createFormatters() map[string]rolesformatter.RolesFormatter {
	formatters := make(map[string]rolesformatter.RolesFormatter, 0)
	summaryFormatter := rolesformatter.NewRolesSummaryFormatter()
	yamlFormatter := rolesformatter.NewRolesYamlFormatter()

	formatters[summaryFormatter.GetName()] = summaryFormatter
	formatters[yamlFormatter.GetName()] = yamlFormatter

	return formatters
}

func GetFormatterNamesAsString() string {
	names := make([]string, 0, len(formatters))
	for name := range formatters {
		names = append(names, name)
	}
	sort.Strings(names)
	formatterNames := strings.Builder{}

	for index, formatterName := range names {

		if index != 0 {
			formatterNames.WriteString(", ")
		}
		formatterNames.WriteString("'" + formatterName + "'")
	}

	return formatterNames.String()
}

func validateFormatFlag(outputFormatString string) (rolesformatter.RolesFormatter, error) {
	var err error

	chosenFormatter, isPresent := formatters[outputFormatString]

	if !isPresent {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_OUTPUT_FORMAT, outputFormatString, GetFormatterNamesAsString())
	}

	return chosenFormatter, err
}
