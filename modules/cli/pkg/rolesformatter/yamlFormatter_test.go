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

func TestRolesYamlFormatterHasCorrectName(t *testing.T) {
	formatter := NewRolesYamlFormatter()
	assert.Equal(t, formatter.GetName(), "yaml")
}

func TestRolesYamlFormatterValidData(t *testing.T) {
	// Given...
	formatter := NewRolesYamlFormatter()
	roles := createTestRoles()

	// When...
	actualFormattedOutput, err := formatter.FormatRoles(roles)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput := `apiVersion: galasa-dev/v1alpha1
kind: GalasaRole
metadata:
    id: role1Id
    name: role1Name
    description: role1Description
    url: http://myHost/api/rbac/roles/role1Id
data:
    actions:
        - action1
        - action2
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaRole
metadata:
    id: role2Id
    name: role2Name
    description: role2Description
    url: http://myHost/api/rbac/roles/role2Id
data:
    actions:
        - action1
        - action2
`
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestRolesYamlFormatterNoDataReturnsTotalCountAllZeros(t *testing.T) {
	// Given...
	formatter := NewRolesYamlFormatter()
	roles := make([]galasaapi.RBACRole, 0)

	// When...
	actualFormattedOutput, err := formatter.FormatRoles(roles)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput := ""
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}
