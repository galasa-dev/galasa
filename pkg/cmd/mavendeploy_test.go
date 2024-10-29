/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"net/http"
	"net/http/httptest"
	"strconv"
	"testing"

	"galasa.dev/buildUtilities/pkg/utils"
	"github.com/stretchr/testify/assert"
)

func createLocalArtifacts(mockFileSystem utils.FileSystem, numArtifacts int, artifactParentPath string) {
	mockFileSystem.MkdirAll(artifactParentPath)

	for i := 0; i < numArtifacts; i++ {
		artifactBasePath := artifactParentPath + "/artifact-" + strconv.Itoa(i+1)
		mockFileSystem.MkdirAll(artifactBasePath)
		mockFileSystem.WriteTextFile(artifactBasePath+"/maven-metadata.xml", "dummy maven-metadata.xml")
		mockFileSystem.MkdirAll(artifactBasePath + "/0.27.0")
		mockFileSystem.WriteTextFile(artifactBasePath+"/0.27.0/pom.xml", "dummy pom.xml")
	}
}

func TestCanDeploySingleArtifact(t *testing.T) {

	// Given...
	mockFileSystem := utils.NewMockFileSystem()
	mockArtifactGroupPath := "localRepository/test/artifact/group"
	createLocalArtifacts(mockFileSystem, 1, mockArtifactGroupPath)

	mockDeployDirectory := "localRepository"
	mockDeployGroup := "test.artifact.group"
	mockDeployVersion := "0.27.0"
	mockBasicAuth := "test"

	mockServer := httptest.NewServer(http.HandlerFunc(func(writer http.ResponseWriter, req *http.Request) {
		assert.Equal(t, "/test/artifact/group/artifact-1/0.27.0/pom.xml", req.URL.Path, "Incorrect URL request")
		assert.Equal(t, "PUT", req.Method, "Incorrect HTTP method")
		assert.Equal(t, mockBasicAuth, req.Header.Get("Authorization"), "Authorization header incorrectly set")

		writer.WriteHeader(http.StatusCreated)
	}))

	defer mockServer.Close()

	// When...
	err := mavenDeploy(mockFileSystem, mockServer.URL, mockDeployDirectory, mockDeployGroup, mockDeployVersion, mockBasicAuth)

	// Then...
	assert.Nil(t, err, "Failed to deploy artifact")
}

func TestCanDeployMultipleArtifacts(t *testing.T) {

	// Given...
	mockFileSystem := utils.NewMockFileSystem()
	mockArtifactGroupPath := "localRepository/test/artifact/group"
	createLocalArtifacts(mockFileSystem, 3, mockArtifactGroupPath)

	mockDeployDirectory := "localRepository"
	mockDeployGroup := "test.artifact.group"
	mockDeployVersion := "0.27.0"
	mockBasicAuth := "test"

	numPutRequests := 0
	mockServer := httptest.NewServer(http.HandlerFunc(func(writer http.ResponseWriter, req *http.Request) {
		assert.Equal(t, "PUT", req.Method, "Incorrect HTTP method")
		assert.Equal(t, mockBasicAuth, req.Header.Get("Authorization"), "Authorization header incorrectly set")

		writer.WriteHeader(http.StatusCreated)
		numPutRequests++
	}))

	defer mockServer.Close()

	// When...
	err := mavenDeploy(mockFileSystem, mockServer.URL, mockDeployDirectory, mockDeployGroup, mockDeployVersion, mockBasicAuth)

	// Then...
	assert.Nil(t, err, "Failed to deploy artifacts")
	assert.Equal(t, 3, numPutRequests)
}

func TestCanDeployNestedArtifact(t *testing.T) {

	// Given...
	mockFileSystem := utils.NewMockFileSystem()
	mockArtifactGroupPath := "localRepository/test/artifact/group"
	mockArtifactParentPath := mockArtifactGroupPath + "/artifact-parent"
	createLocalArtifacts(mockFileSystem, 1, mockArtifactParentPath)

	mockDeployDirectory := "localRepository"
	mockDeployGroup := "test.artifact.group"
	mockDeployVersion := "0.27.0"
	mockBasicAuth := "test"

	mockServer := httptest.NewServer(http.HandlerFunc(func(writer http.ResponseWriter, req *http.Request) {
		assert.Equal(t, "/test/artifact/group/artifact-parent/artifact-1/0.27.0/pom.xml", req.URL.Path, "Incorrect URL request")
		assert.Equal(t, "PUT", req.Method, "Incorrect HTTP method")
		assert.Equal(t, mockBasicAuth, req.Header.Get("Authorization"), "Authorization header incorrectly set")

		writer.WriteHeader(http.StatusCreated)
	}))

	defer mockServer.Close()

	// When...
	err := mavenDeploy(mockFileSystem, mockServer.URL, mockDeployDirectory, mockDeployGroup, mockDeployVersion, mockBasicAuth)

	// Then...
	assert.Nil(t, err, "Failed to deploy artifacts")
}

func TestCanDeployArtifactsWithNestedArtifacts(t *testing.T) {

	// Given...
	mockFileSystem := utils.NewMockFileSystem()

	// Create 3 non-nested artifacts
	mockArtifactGroupPath := "localRepository/test/artifact/group"
	createLocalArtifacts(mockFileSystem, 3, mockArtifactGroupPath)

	// Create 2 nested artifacts
	mockArtifactParentPath := mockArtifactGroupPath + "/nested-artifact-1"
	createLocalArtifacts(mockFileSystem, 1, mockArtifactParentPath)
	mockArtifactParentPath = mockArtifactGroupPath + "/nested-artifact-2"
	createLocalArtifacts(mockFileSystem, 1, mockArtifactParentPath)

	mockDeployDirectory := "localRepository"
	mockDeployGroup := "test.artifact.group"
	mockDeployVersion := "0.27.0"
	mockBasicAuth := "test"

	numPutRequests := 0
	mockServer := httptest.NewServer(http.HandlerFunc(func(writer http.ResponseWriter, req *http.Request) {
		assert.Equal(t, "PUT", req.Method, "Incorrect HTTP method")
		assert.Equal(t, mockBasicAuth, req.Header.Get("Authorization"), "Authorization header incorrectly set")

		writer.WriteHeader(http.StatusCreated)
		numPutRequests++
	}))

	defer mockServer.Close()

	// When...
	err := mavenDeploy(mockFileSystem, mockServer.URL, mockDeployDirectory, mockDeployGroup, mockDeployVersion, mockBasicAuth)

	// Then...
	assert.Nil(t, err, "Failed to deploy artifacts")
	assert.Equal(t, 5, numPutRequests)
}

func TestDoesNotDeployArtifactsWhenNoneExist(t *testing.T) {

	// Given...
	mockFileSystem := utils.NewMockFileSystem()

	mockArtifactGroupPath := "localRepository/test/artifact/group"
	mockFileSystem.MkdirAll(mockArtifactGroupPath)

	dummyDirPath := mockArtifactGroupPath + "/dummy"
	mockFileSystem.MkdirAll(dummyDirPath)

	mockDeployDirectory := "localRepository"
	mockDeployGroup := "test.artifact.group"
	mockDeployVersion := "0.27.0"
	mockBasicAuth := "test"

	numPutRequests := 0
	mockServer := httptest.NewServer(http.HandlerFunc(func(writer http.ResponseWriter, req *http.Request) {
		assert.Equal(t, "PUT", req.Method, "Incorrect HTTP method")
		assert.Equal(t, mockBasicAuth, req.Header.Get("Authorization"), "Authorization header incorrectly set")

		writer.WriteHeader(http.StatusCreated)
		numPutRequests++
	}))

	defer mockServer.Close()

	// When...
	err := mavenDeploy(mockFileSystem, mockServer.URL, mockDeployDirectory, mockDeployGroup, mockDeployVersion, mockBasicAuth)

	// Then...
	assert.Nil(t, err, "Should not deploy artifacts")
	assert.Zero(t, numPutRequests)
}

func TestDoesNotDeployArtifactWithNoVersionDirectory(t *testing.T) {

	// Given...
	mockFileSystem := utils.NewMockFileSystem()

	mockArtifactGroupPath := "localRepository/test/artifact/group"
	mockFileSystem.MkdirAll(mockArtifactGroupPath)

	artifactBasePath := mockArtifactGroupPath + "/bad-artifact"
	mockFileSystem.MkdirAll(artifactBasePath)
	mockFileSystem.WriteTextFile(artifactBasePath+"/maven-metadata.xml", "dummy maven-metadata.xml")

	mockDeployDirectory := "localRepository"
	mockDeployGroup := "test.artifact.group"
	mockDeployVersion := "0.27.0"
	mockBasicAuth := "test"

	numPutRequests := 0
	mockServer := httptest.NewServer(http.HandlerFunc(func(writer http.ResponseWriter, req *http.Request) {
		assert.Equal(t, "PUT", req.Method, "Incorrect HTTP method")
		assert.Equal(t, mockBasicAuth, req.Header.Get("Authorization"), "Authorization header incorrectly set")

		writer.WriteHeader(http.StatusCreated)
		numPutRequests++
	}))

	defer mockServer.Close()

	// When...
	err := mavenDeploy(mockFileSystem, mockServer.URL, mockDeployDirectory, mockDeployGroup, mockDeployVersion, mockBasicAuth)

	// Then...
	assert.Nil(t, err, "Should not deploy artifact")
	assert.Zero(t, numPutRequests)
}

func TestFailingPutRequestStopsDeployingArtifacts(t *testing.T) {

	// Given...
	mockFileSystem := utils.NewMockFileSystem()
	mockArtifactGroupPath := "localRepository/test/artifact/group"
	createLocalArtifacts(mockFileSystem, 3, mockArtifactGroupPath)

	mockDeployDirectory := "localRepository"
	mockDeployGroup := "test.artifact.group"
	mockDeployVersion := "0.27.0"
	mockBasicAuth := "test"

	numPutRequests := 0
	mockServer := httptest.NewServer(http.HandlerFunc(func(writer http.ResponseWriter, req *http.Request) {
		assert.Equal(t, "PUT", req.Method, "Incorrect HTTP method")
		assert.Equal(t, mockBasicAuth, req.Header.Get("Authorization"), "Authorization header incorrectly set")

		writer.WriteHeader(http.StatusInternalServerError)
		numPutRequests++
	}))

	defer mockServer.Close()

	// When...
	err := mavenDeploy(mockFileSystem, mockServer.URL, mockDeployDirectory, mockDeployGroup, mockDeployVersion, mockBasicAuth)

	// Then...
	assert.NotNil(t, err, "Put requests should have returned a HTTP 500 error")

	// Deployment should stop after the first PUT request
	assert.Equal(t, 1, numPutRequests)
}
