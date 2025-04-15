/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package users

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestValidateLoginIdFlagReturnsError_EmptyOrNullValue(t *testing.T) {

	invalidLoginId := ""

	loginId, err := validateLoginIdFlag(invalidLoginId)

	assert.NotNil(t, err)
	assert.Empty(t, loginId)
}
