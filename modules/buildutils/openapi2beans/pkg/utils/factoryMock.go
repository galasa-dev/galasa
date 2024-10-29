/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"github.com/galasa-dev/cli/pkg/files"
	"github.com/galasa-dev/cli/pkg/utils"
)

type MockFactory struct {
	fileSystem       files.FileSystem
	env              utils.Environment
	stdOutConsole    utils.Console
	stdErrConsole    utils.Console
	timeService      utils.TimeService
}

func NewMockFactory() Factory {
	return &MockFactory{}
}

func (mockFactory *MockFactory) GetFileSystem() files.FileSystem {
	if mockFactory.fileSystem == nil {
		mockFactory.fileSystem = files.NewMockFileSystem()
	}
	return mockFactory.fileSystem
}

func (mockFactory *MockFactory) GetEnvironment() utils.Environment {
	if mockFactory.env == nil {
		mockFactory.env = utils.NewMockEnv()
	}
	return mockFactory.env
}

func (mockFactory *MockFactory) GetStdOutConsole() utils.Console {
	if mockFactory.stdOutConsole == nil {
		mockFactory.stdOutConsole = utils.NewMockConsole()
	}
	return mockFactory.stdOutConsole
}

func (mockFactory *MockFactory) GetStdErrConsole() utils.Console {
	if mockFactory.stdErrConsole == nil {
		mockFactory.stdErrConsole = utils.NewMockConsole()
	}
	return mockFactory.stdErrConsole
}

func (mockFactory *MockFactory) GetTimeService() utils.TimeService {
	if mockFactory.timeService == nil {
		mockFactory.timeService = utils.NewMockTimeService()
	}
	return mockFactory.timeService
}
