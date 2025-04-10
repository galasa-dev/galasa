/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package spi

import "io"

// FileSystem is a thin interface layer above the os package which can be mocked out
type FileSystem interface {
	// MkdirAll creates all folders in the file system if they don't already exist.
	MkdirAll(targetFolderPath string) error
	ReadTextFile(filePath string) (string, error)
	ReadBinaryFile(filePath string) ([]byte, error)
	WriteTextFile(targetFilePath string, desiredContents string) error
	WriteBinaryFile(targetFilePath string, desiredContents []byte) error
	Exists(path string) (bool, error)
	DirExists(path string) (bool, error)
	GetUserHomeDirPath() (string, error)
	OutputWarningMessage(string) error
	MkTempDir() (string, error)
	DeleteDir(path string)
	DeleteFile(path string)

	// Creates a file in the file system if it can.
	Create(path string) (io.WriteCloser, error)

	// Returns the normal extension used for executable files.
	// ie: The .exe suffix in windows, or "" in unix-like systems.
	GetExecutableExtension() string

	// GetPathSeparator returns the file path separator specific
	// to this operating system.
	GetFilePathSeparator() string

	// Gets all the file paths recursively from a starting folder.
	GetAllFilePaths(rootPath string) ([]string, error)
}
