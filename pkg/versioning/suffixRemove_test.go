/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package versioning

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestCanSubstituteVersionsForBlankSuffix(t *testing.T) {
	mockFs := createTwoModuleFs()
	err := SuffixRemoveExecute(mockFs, "/my")
	assert.Nil(t, err)

	// Now get the versions out again.
	var modules []Module
	modules, err = getModules(mockFs, "/my")

	assert.Nil(t, err)
	assert.Len(t, modules, 2)

	assert.Equal(t, modules[0].GetProjectName(), "my.random.folder.module1")
	assert.Equal(t, modules[0].GetPath(), "/my/random/folder/module1/")
	assert.Equal(t, modules[0].GetVersion(), "0.36.0")

	assert.Equal(t, modules[1].GetProjectName(), "my.random.folder.module3")
	assert.Equal(t, modules[1].GetPath(), "/my/random/folder/module3/")
	assert.Equal(t, modules[1].GetVersion(), "0.36.0")
}
