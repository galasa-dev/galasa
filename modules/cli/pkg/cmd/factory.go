/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"github.com/galasa-dev/cli/pkg/auth"
	"github.com/galasa-dev/cli/pkg/files"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/utils"
)

// Allocates real objects with real implementations,
// none of which are generally great for unit testing.
// eg: A real file system can leave debris behind when a test runs.
type RealFactory struct {
	stdOutConsole spi.Console
	stdErrConsole spi.Console
}

func NewRealFactory() spi.Factory {
	return &RealFactory{}
}

func (*RealFactory) GetFileSystem() spi.FileSystem {
	return files.NewOSFileSystem()
}

func (*RealFactory) GetEnvironment() spi.Environment {
	return utils.NewEnvironment()
}

func (*RealFactory) GetFinalWordHandler() spi.FinalWordHandler {
	return NewRealFinalWordHandler()
}

// We only ever expect there to be a single console object, which collects all the
// command output.
func (factory *RealFactory) GetStdOutConsole() spi.Console {
	if factory.stdOutConsole == nil {
		factory.stdOutConsole = utils.NewRealConsole()
	}
	return factory.stdOutConsole
}

func (factory *RealFactory) GetStdErrConsole() spi.Console {
	if factory.stdErrConsole == nil {
		factory.stdErrConsole = utils.NewRealConsole()
	}
	return factory.stdErrConsole
}

func (*RealFactory) GetTimeService() spi.TimeService {
	return utils.NewRealTimeService()
}

func (factory *RealFactory) GetAuthenticator(apiServerUrl string, galasaHome spi.GalasaHome) spi.Authenticator {
	jwtCache := auth.NewJwtCache(factory.GetFileSystem(), galasaHome, factory.GetTimeService())
	return auth.NewAuthenticator(apiServerUrl, factory.GetFileSystem(), galasaHome, factory.GetTimeService(), factory.GetEnvironment(), jwtCache)
}

func (*RealFactory) GetByteReader() spi.ByteReader {
	return utils.NewByteReader()
}
