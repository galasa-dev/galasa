/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package embedded

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestCanParseVersionsFromEmbeddedFS(t *testing.T) {
	propsFileName := "templates/version/build.properties"
	content := "galasactl.version=myVersion\n" +
		"galasa.boot.jar.version=0.1.2\n" +
		"galasa.framework.version=3.4.5\n" +
		"galasactl.rest.api.version=0.31.0\n" +
		""

	fs := NewMockReadOnlyFileSystem()
	fs.WriteFile(propsFileName, content)

	versions, err := readVersionsFromEmbeddedFile(fs, nil)

	assert.Nil(t, err)
	assert.NotNil(t, versions)
	assert.Equal(t, "0.1.2", versions.galasaBootJarVersion)
	assert.Equal(t, "3.4.5", versions.galasaFrameworkVersion)
	assert.Equal(t, "myVersion", versions.galasactlVersion)
	assert.Equal(t, "0.31.0", versions.galasactlRestApiVersion)
}

func TestDoesntReReadVersionsFromEmbeddedFSWhenAlreadyKnowAnswers(t *testing.T) {
	propsFileName := "templates/version/build.properties"
	content := "galasactl.version=myVersion\n" +
		"galasa.boot.jar.version=0.1.2\n" +
		"galasa.framework.version=3.4.5\n" +
		"galasactl.rest.api.version=0.31.0\n"

	fs := NewMockReadOnlyFileSystem()
	fs.WriteFile(propsFileName, content)

	alreadyKnownVersions := &versions{
		galasaFrameworkVersion:  "myFrameworkVersion",
		galasaBootJarVersion:    "myBootJarVersion",
		galasactlVersion:        "myGalasaCtlVersion",
		galasactlRestApiVersion: "myRestApiVersion",
	}

	versions, err := readVersionsFromEmbeddedFile(fs, alreadyKnownVersions)

	assert.Nil(t, err)
	assert.NotNil(t, versions)
	assert.Equal(t, "myBootJarVersion", versions.galasaBootJarVersion)
	assert.Equal(t, "myFrameworkVersion", versions.galasaFrameworkVersion)
	assert.Equal(t, "myGalasaCtlVersion", versions.galasactlVersion)
	assert.Equal(t, "myRestApiVersion", versions.galasactlRestApiVersion)
}
