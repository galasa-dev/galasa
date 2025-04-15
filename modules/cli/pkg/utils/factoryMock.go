/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"github.com/galasa-dev/cli/pkg/files"
	"github.com/galasa-dev/cli/pkg/spi"
)

type MockFactory struct {
	FinalWordHandler spi.FinalWordHandler
	FileSystem       spi.FileSystem
	Env              spi.Environment
	StdOutConsole    spi.Console
	StdErrConsole    spi.Console
	TimeService      spi.TimeService
	Authenticator    spi.Authenticator
	ByteReader       spi.ByteReader
}

func NewMockFactory() *MockFactory {
	return new(MockFactory)
}

func (factory *MockFactory) GetFileSystem() spi.FileSystem {
	if factory.FileSystem == nil {
		factory.FileSystem = files.NewMockFileSystem()
	}
	return factory.FileSystem
}

func (factory *MockFactory) GetEnvironment() spi.Environment {
	if factory.Env == nil {
		factory.Env = NewMockEnv()
	}
	return factory.Env
}

func (factory *MockFactory) GetFinalWordHandler() spi.FinalWordHandler {
	if factory.FinalWordHandler == nil {
		factory.FinalWordHandler = NewMockFinalWordHandler()
	}
	return factory.FinalWordHandler
}

func (factory *MockFactory) GetStdOutConsole() spi.Console {
	if factory.StdOutConsole == nil {
		factory.StdOutConsole = NewMockConsole()
	}
	return factory.StdOutConsole
}

func (factory *MockFactory) GetStdErrConsole() spi.Console {
	if factory.StdErrConsole == nil {
		factory.StdErrConsole = NewMockConsole()
	}
	return factory.StdErrConsole
}

func (factory *MockFactory) GetTimeService() spi.TimeService {
	if factory.TimeService == nil {
		factory.TimeService = NewMockTimeService()
	}
	return factory.TimeService
}

func (factory *MockFactory) GetAuthenticator(apiServerUrl string, galasaHome spi.GalasaHome) spi.Authenticator {
	if factory.Authenticator == nil {
		factory.Authenticator = NewMockAuthenticator()
	}
	return factory.Authenticator
}

func (factory *MockFactory) GetByteReader() spi.ByteReader {
	if factory.ByteReader == nil {
		factory.ByteReader = NewMockByteReader()
	}
	return factory.ByteReader
}
