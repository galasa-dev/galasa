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

func TestCanCalculateVersionCanAddSuffixFromUnSuffixed(t *testing.T) {
	newVersion := calculateDesiredVersion("0.0.1", "-SNAPSHOT")
	assert.Equal(t, "0.0.1-SNAPSHOT", newVersion)
}

func TestCanCalculateVersionCanReplaceSuffixWhichHasDashSeparator(t *testing.T) {
	newVersion := calculateDesiredVersion("0.0.1-dev", "-SNAPSHOT")
	assert.Equal(t, "0.0.1-SNAPSHOT", newVersion)
}

func TestCanCalculateVersionCanReplaceSuffixWhichHasUnderscoreSeparator(t *testing.T) {
	newVersion := calculateDesiredVersion("0.0.1_dev", "-SNAPSHOT")
	assert.Equal(t, "0.0.1-SNAPSHOT", newVersion)
}

func TestCanCalculateVersionCanReplaceSuffixWhichHasTwoSuffixesAlready(t *testing.T) {
	newVersion := calculateDesiredVersion("0.0.1-dev-mine", "-SNAPSHOT")
	assert.Equal(t, "0.0.1-SNAPSHOT", newVersion)
}

func TestSetFailsIfSuffixIsInvalid(t *testing.T) {
	err := SuffixSetExecute(nil, "", "notvalid")
	assert.NotNil(t, err)
}

func TestCanSubstituteVersions(t *testing.T) {
	mockFs := createTwoModuleFs()
	err := SuffixSetExecute(mockFs, "/my", "-alpha")
	assert.Nil(t, err)

	// Now get the versions out again.
	var modules []Module
	modules, err = getModules(mockFs, "/my")

	assert.Nil(t, err)
	assert.Len(t, modules, 2)

	assert.Equal(t, modules[0].GetProjectName(), "my.random.folder.module1")
	assert.Equal(t, modules[0].GetPath(), "/my/random/folder/module1/")
	assert.Equal(t, modules[0].GetVersion(), "0.36.0-alpha")

	assert.Equal(t, modules[1].GetProjectName(), "my.random.folder.module3")
	assert.Equal(t, modules[1].GetPath(), "/my/random/folder/module3/")
	assert.Equal(t, modules[1].GetVersion(), "0.36.0-alpha")
}
