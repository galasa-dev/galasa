/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"strings"
	"testing"

	"log"

	"github.com/galasa-dev/cli/pkg/embedded"
	"github.com/galasa-dev/cli/pkg/files"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/stretchr/testify/assert"
)

// To validate the string as a valid java package name before we start to use it.
func TestCanCreateHomeFolderGoldenPath(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	embeddedFileSystem := embedded.GetReadOnlyFileSystem()
	mockEnv := NewMockEnv()
	galasaHome, err := NewGalasaHome(mockFileSystem, mockEnv, "")

	if err != nil {
		assert.Fail(t, "Errored when it shouldn't have! "+err.Error())
	}

	// When ...
	err = InitialiseGalasaHomeFolder(galasaHome, mockFileSystem, embeddedFileSystem)

	// Then...
	// Should have created a folder for the parent package.
	if err != nil {
		assert.Fail(t, err.Error())
	}

	// Check the home folder has been created.
	homeDir, _ := mockFileSystem.GetUserHomeDirPath()
	galasaHomeDir := homeDir + "/.galasa"
	assertFolderExists(t, mockFileSystem, galasaHomeDir, "Didn't create "+galasaHomeDir+" folder in home directory.")

	// Check that the lib folder has been created.
	libDir := homeDir + "/.galasa/lib"
	assertFolderExists(t, mockFileSystem, libDir, "Didn't create "+libDir+" folder in home directory.")

	// Check that the folder of the galasa level is created in lib.
	galasaVersion, _ := embedded.GetGalasaVersion()
	galasaVersionLibSubdir := libDir + "/" + galasaVersion
	assertFolderExists(t, mockFileSystem, galasaVersionLibSubdir, "Didn't create "+galasaVersionLibSubdir+" folder in home directory.")

	bootJarVersion, _ := embedded.GetBootJarVersion()
	bootJarName := galasaVersionLibSubdir + "/galasa-boot-" + bootJarVersion + ".jar"

	isExists, _ := mockFileSystem.Exists(bootJarName)
	assert.True(t, isExists, "Failed to unpack the boot jar")

	isExists, _ = mockFileSystem.Exists(galasaHomeDir + "/bootstrap.properties")
	assert.True(t, isExists, "Failed to create file "+galasaHomeDir+"/bootstrap.properties")

	isExists, _ = mockFileSystem.Exists(galasaHomeDir + "/overrides.properties")
	assert.True(t, isExists, "Failed to create file "+galasaHomeDir+"/overrides.properties")

	isExists, _ = mockFileSystem.Exists(galasaHomeDir + "/cps.properties")
	assert.True(t, isExists, "Failed to create file "+galasaHomeDir+"/cps.properties")

	isExists, _ = mockFileSystem.Exists(galasaHomeDir + "/dss.properties")
	assert.True(t, isExists, "Failed to create file "+galasaHomeDir+"/dss.properties")

	isExists, _ = mockFileSystem.Exists(galasaHomeDir + "/credentials.properties")
	assert.True(t, isExists, "Failed to create file "+galasaHomeDir+"/credentials.properties")
}

func assertFolderExists(t *testing.T, mockFileSystem spi.FileSystem, path string, message string) {
	isExist, _ := mockFileSystem.DirExists(path)
	assert.True(t, isExist, message)
}

func TestCanGetGalasaBootJarPath(t *testing.T) {

	fs := files.NewMockFileSystem()
	mockEnv := NewMockEnv()
	galasaHome, _ := NewGalasaHome(fs, mockEnv, "")
	path, err := GetGalasaBootJarPath(fs, galasaHome)

	assert.Nil(t, err)
	assert.NotNil(t, path)
	log.Printf("path got back is '%s'", path)
	assert.True(t, strings.HasPrefix(path, "/User/Home/testuser/.galasa/lib/"))
	assert.True(t, strings.HasSuffix(path, ".jar"))
	assert.Contains(t, path, "galasa-boot-")
}
