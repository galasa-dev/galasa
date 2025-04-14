/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package rolesformatter

import (
	"testing"

	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/stretchr/testify/assert"
)

const (
	API_VERSION = "galasa-dev/v1alpha1"
)

func createTestRoles() []galasaapi.RBACRole {

	role1 := galasaapi.NewRBACRole()
	role1.SetApiVersion(API_VERSION)
	role1.SetKind("GalasaRole")

	role1Metadata := *galasaapi.NewRBACRoleMetadata()
	role1Metadata.SetName("role1Name")
	role1Metadata.SetId("role1Id")
	role1Metadata.SetDescription("role1Description")
	role1Metadata.SetUrl("http://myHost/api/rbac/roles/role1Id")
	role1.Metadata = &role1Metadata

	role1Data := *galasaapi.NewRBACRoleData()
	actions1 := make([]string, 0)
	actions1 = append(actions1, "action1")
	actions1 = append(actions1, "action2")
	role1Data.Actions = actions1
	role1.Data = &role1Data

	role2 := galasaapi.NewRBACRole()
	role2.SetApiVersion(API_VERSION)
	role2.SetKind("GalasaRole")

	role2Metadata := *galasaapi.NewRBACRoleMetadata()
	role2Metadata.SetName("role2Name")
	role2Metadata.SetId("role2Id")
	role2Metadata.SetDescription("role2Description")
	role2Metadata.SetUrl("http://myHost/api/rbac/roles/role2Id")
	role2.Metadata = &role2Metadata

	role2Data := *galasaapi.NewRBACRoleData()
	actions2 := make([]string, 0)
	actions2 = append(actions2, "action1")
	actions2 = append(actions2, "action2")
	role2Data.Actions = actions2
	role2.Data = &role2Data

	roles := make([]galasaapi.RBACRole, 0)
	roles = append(roles, *role1)
	roles = append(roles, *role2)

	return roles
}

func TestRolesSummaryFormatterHasCorrectName(t *testing.T) {
	formatter := NewRolesSummaryFormatter()
	assert.Equal(t, formatter.GetName(), "summary")
}

func TestRolesSummaryFormatterValidDataReturnsTotalCountTwo(t *testing.T) {
	// Given...
	formatter := NewRolesSummaryFormatter()
	roles := createTestRoles()

	// When...
	actualFormattedOutput, err := formatter.FormatRoles(roles)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput := `name      description
role1Name role1Description
role2Name role2Description

Total:2
`
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestRolesSummaryFormatterNoDataReturnsTotalCountAllZeros(t *testing.T) {
	// Given...
	formatter := NewRolesSummaryFormatter()
	roles := make([]galasaapi.RBACRole, 0)

	// When...
	actualFormattedOutput, err := formatter.FormatRoles(roles)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput := "Total:0\n"
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}
