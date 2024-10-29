/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package versioning

import (
	"testing"

	"galasa.dev/buildUtilities/pkg/utils"
	"github.com/stretchr/testify/assert"
)

func createSingleModuleFs() *utils.MockFileSystem {
	fs := utils.NewOverridableMockFileSystem()

	fs.MkdirAll("/my/second/random/folder/module2")
	fs.MkdirAll("/my/random/folder/module1")
	fs.WriteTextFile("/my/random/folder/module1/build.gradle",
		`# Not a version yet
		version = "0.36.0-SNAPSHOT" # Some comment.
		`)
	fs.WriteTextFile("/my/random/folder/module1/settings.gradle",
		`rootProject.name = 'my.random.folder.module1'`)
	return fs
}

func createTwoModuleFs() *utils.MockFileSystem {
	fs := utils.NewOverridableMockFileSystem()

	// Folders which are not a module.
	fs.MkdirAll("/my/second/random/folder/module2")

	// Folders which hold a real module.
	fs.MkdirAll("/my/random/folder/module1")
	fs.WriteTextFile("/my/random/folder/module1/build.gradle",
		`# Not a version yet
		version = "0.36.0-SNAPSHOT" # Some comment.
		`)
	fs.WriteTextFile("/my/random/folder/module1/settings.gradle",
		`rootProject.name = 'my.random.folder.module1'`)

	// Folders which hold a second module
	fs.MkdirAll("/my/random/folder/module3")
	fs.WriteTextFile("/my/random/folder/module3/build.gradle",
		`# Not a version yet
		version = "0.36.0-dev" # Some comment.
		`)
	fs.WriteTextFile("/my/random/folder/module3/settings.gradle",
		`rootProject.name = 'my.random.folder.module3'`)
	return fs
}

func TestCanFindGradleFolder(t *testing.T) {
	fs := createSingleModuleFs()

	buildGradleFolderPaths, err := gatherEligibleBuildGradleFolders(fs, "/my")

	assert.Nil(t, err)
	assert.Len(t, buildGradleFolderPaths, 1)
	assert.Contains(t, buildGradleFolderPaths, "/my/random/folder/module1/")
}

func TestCanFindAModule(t *testing.T) {
	fs := createSingleModuleFs()

	buildGradleFolderPaths, _ := gatherEligibleBuildGradleFolders(fs, "/my")
	modules, err := extractModulesFromBuildGradleFolders(fs, buildGradleFolderPaths)
	assert.Nil(t, err)
	assert.Len(t, modules, 1)
	assert.Equal(t, modules[0].GetProjectName(), "my.random.folder.module1")
	assert.Equal(t, modules[0].GetPath(), "/my/random/folder/module1/")
	assert.Equal(t, modules[0].GetVersion(), "0.36.0-SNAPSHOT")
}

func TestCanFindTwoModules(t *testing.T) {
	fs := createTwoModuleFs()

	buildGradleFolderPaths, _ := gatherEligibleBuildGradleFolders(fs, "/my")
	modules, err := extractModulesFromBuildGradleFolders(fs, buildGradleFolderPaths)
	assert.Nil(t, err)
	assert.Len(t, modules, 2)

	assert.Equal(t, modules[0].GetProjectName(), "my.random.folder.module1")
	assert.Equal(t, modules[0].GetPath(), "/my/random/folder/module1/")
	assert.Equal(t, modules[0].GetVersion(), "0.36.0-SNAPSHOT")

	assert.Equal(t, modules[1].GetProjectName(), "my.random.folder.module3")
	assert.Equal(t, modules[1].GetPath(), "/my/random/folder/module3/")
	assert.Equal(t, modules[1].GetVersion(), "0.36.0-dev")
}

func TestVersionRegexMatchesExampleDoubleQuotes(t *testing.T) {
	matches := versionLineRegex.FindStringSubmatch(`  version= "12.13.24-dev"`)
	assert.NotNil(t, matches)
	assert.Len(t, matches, 2)
	assert.Equal(t, matches[1], "12.13.24-dev")
}

func TestVersionRegexMatchesExampleSingleQuotes(t *testing.T) {
	matches := versionLineRegex.FindStringSubmatch(`  version= '12.13.24-dev'   `)
	assert.NotNil(t, matches)
	assert.Len(t, matches, 2)
	assert.Equal(t, matches[1], "12.13.24-dev")
}

func TestVersionRegexMatchesRealExample(t *testing.T) {
	contents := `plugins {
    id 'galasa.manager'
}

description = 'Galasa zOS File Manager - zOS/MF Implementation'

version = '0.21.0' # hello

dependencies {
    implementation project (':galasa-managers-zos-parent:dev.galasa.zos.manager')
}`
	matches := versionLineRegex.FindStringSubmatch(contents)
	assert.NotNil(t, matches)
	assert.Len(t, matches, 2)
	assert.Equal(t, matches[1], "0.21.0")
}

func createSingleModuleWithNoVersionInBuildGradleFs() *utils.MockFileSystem {
	fs := utils.NewOverridableMockFileSystem()

	fs.MkdirAll("/my/second/random/folder/module2")
	fs.MkdirAll("/my/random/folder/module1")
	fs.WriteTextFile("/my/random/folder/module1/build.gradle",
		`# Not a version yet
		version = # There isn't a version here. Not a valid module.
		`)
	fs.WriteTextFile("/my/random/folder/module1/settings.gradle",
		`rootProject.name = 'my.random.folder.module1'`)
	return fs
}

func TestIgnoresModuleWithNoVersionInBuildGradle(t *testing.T) {
	fs := createSingleModuleWithNoVersionInBuildGradleFs()

	buildGradleFolderPaths, _ := gatherEligibleBuildGradleFolders(fs, "/my")
	modules, err := extractModulesFromBuildGradleFolders(fs, buildGradleFolderPaths)
	assert.Nil(t, err)
	assert.Len(t, modules, 0)
}

func TestVersionRegexMatchesVersionNMissingEquals(t *testing.T) {
	contents := `plugins {
    id 'galasa.manager'
}

description = 'Galasa zOS File Manager - zOS/MF Implementation'

version '0.34.0'

dependencies {
    implementation project (':galasa-managers-zos-parent:dev.galasa.zos.manager')
}`
	matches := versionLineRegex.FindStringSubmatch(contents)
	assert.NotNil(t, matches)
	assert.Len(t, matches, 2)
	assert.Equal(t, matches[1], "0.34.0")
}

func TestVersionRegexMatchesVersionnMissingFullExample(t *testing.T) {
	contents := `]]plugins {
    id 'biz.aQute.bnd.builder'
    id 'org.openapi.generator' version "5.0.1"
    id 'galasa.api.server'

    // testCompile requires Java plugin.
    id 'java'
}

description 'Galasa API - RAS'

version '0.34.0'

dependencies {
`
	matches := versionLineRegex.FindStringSubmatch(contents)
	assert.NotNil(t, matches)
	assert.Len(t, matches, 2)
	assert.Equal(t, matches[1], "0.34.0")
}
