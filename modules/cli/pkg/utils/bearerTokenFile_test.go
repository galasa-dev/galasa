/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"errors"
	"sort"
	"strings"
	"testing"
	"time"

	"github.com/galasa-dev/cli/pkg/files"
	"github.com/stretchr/testify/assert"
)

var encryptionSecret string = "My long test encryption secret"

func TestWriteBearerTokenJsonFileWritesJwtJsonToFile(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := NewMockEnv()
	mockGalasaHome, _ := NewGalasaHome(mockFileSystem, mockEnvironment, "")

	token := "blah"

	file := NewBearerTokenFile(mockFileSystem, mockGalasaHome, "baseFile.json", NewMockTimeService())

	// When...
	err := file.WriteJwt(token, encryptionSecret)

	// Then...
	assert.Nil(t, err, "Should not return an error when writing a JWT to the bearer-token.json file in an existing galasa home directory")

	bearerTokenJson, _ := mockFileSystem.ReadTextFile(mockGalasaHome.GetNativeFolderPath() + "/bearer-tokens/baseFile.json")

	decryptedJson, err := Decrypt(encryptionSecret, bearerTokenJson)

	assert.Nil(t, err)
	assert.Contains(t, decryptedJson, token)
}

func TestWriteBearerTokenJsonWithFailingWriteOperationReturnsError(t *testing.T) {
	// Given...
	mockFileSystem := files.NewOverridableMockFileSystem()
	mockEnvironment := NewMockEnv()
	mockGalasaHome, _ := NewGalasaHome(mockFileSystem, mockEnvironment, "")

	mockFileSystem.VirtualFunction_WriteTextFile = func(path string, contents string) error {
		return errors.New("simulating a failed write operation")
	}

	token := "blah"

	file := NewBearerTokenFile(mockFileSystem, mockGalasaHome, "baseFile.json", NewMockTimeService())

	// When...
	err := file.WriteJwt(token, encryptionSecret)

	// Then...
	assert.NotNil(t, err, "Should return an error when writing the bearer-token.json file fails")
	assert.ErrorContains(t, err, "GAL1042E")
}

func TestGetBearerTokenFromTokenJsonFileReturnsBearerToken(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := NewMockEnv()
	mockGalasaHome, _ := NewGalasaHome(mockFileSystem, mockEnvironment, "")

	// This is a dummy JWT that expires 1 hour after the Unix epoch
	expectedToken := "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjM2MDB9._j3Fchdx5IIqgGrdEGWXHxdgVyoBEyoD2-IBvhlxF1s" //This is a mock JWT and contains no info //pragma: allowlist secret
	mockCurrentTime := time.UnixMilli(0)
	mockTimeService := NewOverridableMockTimeService(mockCurrentTime)

	file := NewBearerTokenFile(mockFileSystem, mockGalasaHome, "baseFile.json", mockTimeService)
	file.WriteJwt(expectedToken, encryptionSecret)

	// When...
	bearerToken, err := file.ReadJwt(encryptionSecret)

	// Then...
	assert.Nil(t, err, "Should not return an error when a valid bearer token exists and is valid in bearer-token.json")
	assert.Equal(t, expectedToken, bearerToken)
}

func TestGetBearerTokenFromTokenJsonFileWithMissingTokenFileReturnsError(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := NewMockEnv()
	mockGalasaHome, _ := NewGalasaHome(mockFileSystem, mockEnvironment, "")
	mockTimeService := NewMockTimeService()

	file := NewBearerTokenFile(mockFileSystem, mockGalasaHome, "baseFile.json", mockTimeService)

	// When...
	_, err := file.ReadJwt(encryptionSecret)

	// Then...
	assert.NotNil(t, err, "Should return an error when bearer token file does not exist")
	assert.ErrorContains(t, err, "GAL1107E")
}

func TestGetBearerTokenFromTokenJsonFileWithBadContentsReturnsError(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := NewMockEnv()
	mockGalasaHome, _ := NewGalasaHome(mockFileSystem, mockEnvironment, "")
	mockTimeService := NewMockTimeService()

	file := NewBearerTokenFile(mockFileSystem, mockGalasaHome, "baseFile.json", mockTimeService)

	mockFileSystem.WriteTextFile(
		mockGalasaHome.GetNativeFolderPath()+"/baseFile.json",
		"notabearertoken")

	// When...
	_, err := file.ReadJwt(encryptionSecret)

	// Then...
	assert.NotNil(t, err, "Should return an error when the bearer token file exists but doesn't contain valid JSON")
	assert.ErrorContains(t, err, "GAL1107E")
}

func TestGetAllFilePathsReturnsTwoFiles(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := NewMockEnv()
	mockGalasaHome, _ := NewGalasaHome(mockFileSystem, mockEnvironment, "")

	// This is a dummy JWT that expires 1 hour after the Unix epoch
	expectedToken := "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjM2MDB9._j3Fchdx5IIqgGrdEGWXHxdgVyoBEyoD2-IBvhlxF1s" //This is a mock JWT and contains no info //pragma: allowlist secret
	mockCurrentTime := time.UnixMilli(0)
	mockTimeService := NewOverridableMockTimeService(mockCurrentTime)

	file := NewBearerTokenFile(mockFileSystem, mockGalasaHome, "baseFile1.json", mockTimeService)
	file.WriteJwt(expectedToken, encryptionSecret)
	file = NewBearerTokenFile(mockFileSystem, mockGalasaHome, "baseFile2.json", mockTimeService)
	file.WriteJwt(expectedToken, encryptionSecret)

	allFiles, err := ListAllBearerTokenFiles(mockFileSystem, mockGalasaHome)
	assert.Nil(t, err)

	assert.Equal(t, 2, len(allFiles))
	sort.Sort(sort.StringSlice(allFiles))

	assert.True(t, strings.HasSuffix(allFiles[0], "baseFile1.json"), "Full path returned: "+allFiles[0]+" suffix expected: baseFile1.json")
	assert.True(t, strings.HasSuffix(allFiles[1], "baseFile2.json"), "Full path returned: "+allFiles[1]+" suffix expected: baseFile2.json")
}

func TestDeleteAllBearerTokensWorks(t *testing.T) {
	// Given...
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := NewMockEnv()
	mockGalasaHome, _ := NewGalasaHome(mockFileSystem, mockEnvironment, "")

	// This is a dummy JWT that expires 1 hour after the Unix epoch
	expectedToken := "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjM2MDB9._j3Fchdx5IIqgGrdEGWXHxdgVyoBEyoD2-IBvhlxF1s" //This is a mock JWT and contains no info //pragma: allowlist secret
	mockCurrentTime := time.UnixMilli(0)
	mockTimeService := NewOverridableMockTimeService(mockCurrentTime)

	file := NewBearerTokenFile(mockFileSystem, mockGalasaHome, "baseFile1.json", mockTimeService)
	file.WriteJwt(expectedToken, encryptionSecret)
	file = NewBearerTokenFile(mockFileSystem, mockGalasaHome, "baseFile2.json", mockTimeService)
	file.WriteJwt(expectedToken, encryptionSecret)

	allFiles, err := ListAllBearerTokenFiles(mockFileSystem, mockGalasaHome)
	assert.Nil(t, err)
	assert.Equal(t, 2, len(allFiles))

	// When we delete all the files...
	err = DeleteAllBearerTokenFiles(mockFileSystem, mockGalasaHome)

	// Then
	assert.Nil(t, err)

	allFiles, err = ListAllBearerTokenFiles(mockFileSystem, mockGalasaHome)
	assert.Nil(t, err)
	assert.Equal(t, 0, len(allFiles))

}

func TestTokenFileWhichExistsSaysItExists(t *testing.T) {
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := NewMockEnv()
	mockGalasaHome, _ := NewGalasaHome(mockFileSystem, mockEnvironment, "")

	// This is a dummy JWT that expires 1 hour after the Unix epoch
	expectedToken := "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjM2MDB9._j3Fchdx5IIqgGrdEGWXHxdgVyoBEyoD2-IBvhlxF1s" //This is a mock JWT and contains no info //pragma: allowlist secret
	mockCurrentTime := time.UnixMilli(0)
	mockTimeService := NewOverridableMockTimeService(mockCurrentTime)

	file := NewBearerTokenFile(mockFileSystem, mockGalasaHome, "baseFile1.json", mockTimeService)
	file.WriteJwt(expectedToken, encryptionSecret)

	isExists, err := file.Exists()
	assert.Nil(t, err)
	assert.True(t, isExists)
}

func TestTokenFileWhichDoesntExistSaysItDoesntExist(t *testing.T) {
	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := NewMockEnv()
	mockGalasaHome, _ := NewGalasaHome(mockFileSystem, mockEnvironment, "")

	// This is a dummy JWT that expires 1 hour after the Unix epoch
	mockCurrentTime := time.UnixMilli(0)
	mockTimeService := NewOverridableMockTimeService(mockCurrentTime)

	file := NewBearerTokenFile(mockFileSystem, mockGalasaHome, "baseFile1.json", mockTimeService)

	isExists, err := file.Exists()
	assert.Nil(t, err)
	assert.False(t, isExists)
}

func TestTokenFileWhichIsDeletedNoLongerExists(t *testing.T) {

	mockFileSystem := files.NewMockFileSystem()
	mockEnvironment := NewMockEnv()
	mockGalasaHome, _ := NewGalasaHome(mockFileSystem, mockEnvironment, "")

	// This is a dummy JWT that expires 1 hour after the Unix epoch
	mockCurrentTime := time.UnixMilli(0)
	mockTimeService := NewOverridableMockTimeService(mockCurrentTime)

	// Create the jwt on disk.
	expectedToken := "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjM2MDB9._j3Fchdx5IIqgGrdEGWXHxdgVyoBEyoD2-IBvhlxF1s" //This is a mock JWT and contains no info //pragma: allowlist secret
	file := NewBearerTokenFile(mockFileSystem, mockGalasaHome, "baseFile1.json", mockTimeService)
	file.WriteJwt(expectedToken, encryptionSecret)

	// Check it exists
	isExists, err := file.Exists()
	assert.Nil(t, err)
	assert.True(t, isExists)

	// Now delete it.
	err = file.DeleteJwt()
	assert.Nil(t, err)

	// Check it no longer exists.
	isExists, err = file.Exists()
	assert.Nil(t, err)
	assert.False(t, isExists)
}
