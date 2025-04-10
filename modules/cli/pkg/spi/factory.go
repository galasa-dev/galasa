/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package spi

// A final word handler can set the exit code of the entire process.
// Or it could be mocked-out to just collect it and checked in tests.
type FinalWordHandler interface {
	FinalWord(rootCmd GalasaCommand, errorToExctractFrom interface{})
}

// We use the factory to create instances of various classes.
// Some are cached, so you get the same one back each time
// Some are fresh objects created each time.
// We do this so we can have a real and a mock implementation
// to make unit testing easier.
type Factory interface {
	GetFileSystem() FileSystem
	GetEnvironment() Environment
	GetFinalWordHandler() FinalWordHandler
	GetStdOutConsole() Console
	GetStdErrConsole() Console
	GetTimeService() TimeService
	GetAuthenticator(apiServerUrl string, galasaHome GalasaHome) Authenticator
	GetByteReader() ByteReader
}
