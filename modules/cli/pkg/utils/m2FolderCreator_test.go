/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"errors"
	"strings"
	"testing"

	"github.com/galasa-dev/cli/pkg/embedded"
	"github.com/galasa-dev/cli/pkg/files"
	"github.com/stretchr/testify/assert"
)

// To validate the string as a valid java package name before we start to use it.
func TestCanCreateM2FolderAndSettingsXML(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	embeddedFileSystem := embedded.GetReadOnlyFileSystem()
	isDevelopment := false

	// When ...
	err := InitialiseM2Folder(mockFileSystem, embeddedFileSystem, isDevelopment)

	// Then...
	// Should have created a folder for the parent package.
	if err != nil {
		assert.Fail(t, err.Error())
	}

	// Check the home folder has been created.
	homeDir, _ := mockFileSystem.GetUserHomeDirPath()
	m2Dir := homeDir + "/.m2"
	assertFolderExists(t, mockFileSystem, m2Dir, "Didn't create "+m2Dir+" folder in home directory.")

	isExists, _ := mockFileSystem.Exists(m2Dir + "/settings.xml")
	assert.True(t, isExists, "Failed to create file "+m2Dir+"/settings.xml")

	settingsContent, _ := mockFileSystem.ReadTextFile(m2Dir + "/settings.xml")
	assert.Contains(t, settingsContent, "<url>https://development.galasa.dev/main/maven-repo/obr</url>", "Test settings.xml didn't have the correct obr url")

}

func TestWhenM2SettingsExistButReposNotPresentYouGetAWarningAboutRequiredReposYouShouldAddToSettingsXML(t *testing.T) {
	// Given...
	settingsXmlContents := "Something that doesn't contain https://devel??ment.galasa.dev/main/maven-repo/obr or https://repo.maven.apa??e.org/maven2"
	mockFileSystem := newMockFSContainingSettingsXml(settingsXmlContents)

	embeddedFileSystem := embedded.GetReadOnlyFileSystem()
	isDevelopment := false

	// When ...
	err := InitialiseM2Folder(mockFileSystem, embeddedFileSystem, isDevelopment)

	// Then...
	// Should have created a folder for the parent package.
	if err != nil {
		assert.Fail(t, err.Error())
	}

	warningMessagesCaptured := mockFileSystem.GetAllWarningMessages()
	if !(strings.Contains(warningMessagesCaptured, "https://development.galasa.dev/main/maven-repo/obr") ||
		strings.Contains(warningMessagesCaptured, "https://repo.maven.apache.org/maven2")) {
		assert.Fail(t, "settings.xml existed but didn't contain either of our magic repositories to get the galasa obr, so we should have got warnings. Warnings messages we did get:"+warningMessagesCaptured)
	}

	if !strings.Contains(warningMessagesCaptured, "GAL2000W") {
		assert.Fail(t, "Wrong error message.")
	}

}

func TestWhenM2SettingsExistAndContainsReferenceToMavenCentralNoWarnigsExpected(t *testing.T) {
	checkThatDifferentSettingsXmlFileContentsCauseFailure(t, "Something that contains https://repo.maven.apache.org/maven2")
}

func TestWhenM2SettingsExistAndContainsReferenceToBleedingEdgeRepoNoWarnigsExpected(t *testing.T) {
	checkThatDifferentSettingsXmlFileContentsCauseFailure(t, "Something that contains  https://development.galasa.dev/main/maven-repo/obr")
}

func checkThatDifferentSettingsXmlFileContentsCauseFailure(t *testing.T, settingsXmlContents string) {
	// Given...
	mockFileSystem := newMockFSContainingSettingsXml(settingsXmlContents)

	embeddedFileSystem := embedded.GetReadOnlyFileSystem()
	isDevelopment := false

	// When ...
	err := InitialiseM2Folder(mockFileSystem, embeddedFileSystem, isDevelopment)

	// Then...
	// Should have created a folder for the parent package.
	if err != nil {
		assert.Fail(t, err.Error())
	}

	warningMessagesCaptured := mockFileSystem.GetAllWarningMessages()
	assert.Equal(t, warningMessagesCaptured, "", "Warnings issued but they should not have been !")
}

func newMockFSContainingSettingsXml(settingsXmlContents string) *files.MockFileSystem {
	mockFileSystem := files.NewOverridableMockFileSystem()
	homeDir, _ := mockFileSystem.GetUserHomeDirPath()
	m2Dir := homeDir + "/.m2"
	mockFileSystem.WriteTextFile(m2Dir+"/settings.xml", settingsXmlContents)
	return mockFileSystem
}

func TestWhenM2SettingsExistCheckFailsErrorGetsReturned(t *testing.T) {

	// Given...
	settingsXmlContents := "Something that doesn't contain https://devel??ment.galasa.dev/main/maven-repo/obr or https://repo.maven.apa??e.org/maven2"
	mockFileSystem := newMockFSContainingSettingsXml(settingsXmlContents)

	embeddedFileSystem := embedded.GetReadOnlyFileSystem()
	isDevelopment := false

	mockFileSystem.VirtualFunction_Exists = func(targetFolderPath string) (bool, error) {
		return false, errors.New("Simulated Exists() method failure")
	}

	// When ...
	err := InitialiseM2Folder(mockFileSystem, embeddedFileSystem, isDevelopment)

	// Then...

	if err == nil {
		assert.Fail(t, "Should have failed!")
	}

	assert.Equal(t, err.Error(), "Simulated Exists() method failure")
}

func TestWhenM2SettingsReadTextFileFailsErrorGetsReturned(t *testing.T) {

	// Given...
	settingsXmlContents := "Something that doesn't contain https://devel??ment.galasa.dev/main/maven-repo/obr or https://repo.maven.apa??e.org/maven2"
	mockFileSystem := newMockFSContainingSettingsXml(settingsXmlContents)

	embeddedFileSystem := embedded.GetReadOnlyFileSystem()
	isDevelopment := false

	mockFileSystem.VirtualFunction_ReadTextFile = func(targetFolderPath string) (string, error) {
		return "", errors.New("Simulated Exists() method failure")
	}

	// When ...
	err := InitialiseM2Folder(mockFileSystem, embeddedFileSystem, isDevelopment)

	// Then...

	if err == nil {
		assert.Fail(t, "Should have failed!")
	}

	assert.Equal(t, err.Error(), "Simulated Exists() method failure")
}
