/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"log"
	"testing"

	"github.com/galasa-dev/cli/pkg/files"
	"github.com/stretchr/testify/assert"
)

func TestLogRedirectorFailsWhenLogFileIsAFolder(t *testing.T) {

	var err error

	fileSystem := files.NewOverridableMockFileSystem()

	// Create a fake folder '.'
	logFileName := "."
	fileSystem.MkdirAll(logFileName)

	err = CaptureLog(fileSystem, logFileName)
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1069")
}

func TestLogRedirectorCapturesFunctionLogsToAFileOk(t *testing.T) {

	var err error

	factory := NewMockFactory()
	fileSystem := factory.GetFileSystem()

	expectedLogFileContent := "Hello, world!"
	executionFunc := func() error {
		log.Println(expectedLogFileContent)
		return nil
	}

	logFileName := "test-log.log"

	err = CaptureExecutionLogs(factory, logFileName, executionFunc)
	assert.Nil(t, err)

	logFileExists, _ := fileSystem.Exists(logFileName)
	assert.True(t, logFileExists)

	// The actual content will be prefixed with a timestamp (e.g. "2024/12/23 13:33:35"),
	// we just want to see if our message was written to the file
	logFileContent, _ := fileSystem.ReadTextFile(logFileName)
	assert.Contains(t, logFileContent, expectedLogFileContent)
}
