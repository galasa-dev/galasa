/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package roles

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestValidRoleNameValidatesOk(t *testing.T) {
	roleNamePutIn := "validRole"
	roleNameGotBack, err := validateRoleName(roleNamePutIn)
	assert.Nil(t, err, "No error expected but there was one reported")
	assert.Equal(t, roleNameGotBack, "validRole")
}

func TestValidRoleNameWithSpacesValidatesOkAndGetsTrimmedAtFront(t *testing.T) {
	roleNamePutIn := "  validRole"
	roleNameGotBack, err := validateRoleName(roleNamePutIn)
	assert.Nil(t, err, "No error expected but there was one reported")
	assert.Equal(t, roleNameGotBack, "validRole")
}

func TestValidRoleNameWithSpacesValidatesOkAndGetsTrimmedAtBack(t *testing.T) {
	roleNamePutIn := "validRole  "
	roleNameGotBack, err := validateRoleName(roleNamePutIn)
	assert.Nil(t, err, "No error expected but there was one reported")
	assert.Equal(t, roleNameGotBack, "validRole")
}

func TestValidRoleNameWithNumbersValidatesOkAndGetsTrimmedAtBack(t *testing.T) {
	roleNamePutIn := "validRole0123456789"
	roleNameGotBack, err := validateRoleName(roleNamePutIn)
	assert.Nil(t, err, "No error expected but there was one reported")
	assert.Equal(t, roleNameGotBack, "validRole0123456789")
}

func TestValidRoleNameWithDotsValidatesWithErrorReported(t *testing.T) {
	roleNamePutIn := "invalid Role with spaces"
	_, err := validateRoleName(roleNamePutIn)
	assert.NotNil(t, err, "Expected error but there was no error reported")
	assert.Contains(t, err.Error(), "GAL1209E")
}

func TestValidRoleNameWithSpacesValidatesWithErrorReported(t *testing.T) {
	roleNamePutIn := "invalid.Role.with.dots"
	_, err := validateRoleName(roleNamePutIn)
	assert.NotNil(t, err, "Expected error but there was no error reported")
	assert.Contains(t, err.Error(), "GAL1209E")
}

func TestValidRoleNameWithPercentsValidatesWithErrorReported(t *testing.T) {
	roleNamePutIn := "invalid%Role%with%dots"
	_, err := validateRoleName(roleNamePutIn)
	assert.NotNil(t, err, "Expected error but there was no error reported")
	assert.Contains(t, err.Error(), "GAL1209E")
}

func TestValidRoleNameWithWeirdCharactersFailsToValidate(t *testing.T) {
	roleNamePutIn := "∞∞∞5%QÑ"
	_, err := validateRoleName(roleNamePutIn)
	assert.NotNil(t, err, "Expected error but there was no error reported")
	assert.Contains(t, err.Error(), "GAL1209E")
}
