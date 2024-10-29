/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"errors"
	"fmt"
	"io"
	"io/fs"
	"os"
	pathUtils "path"
	"path/filepath"
	"runtime"
)

// FileSystem is a thin interface layer above the os package which can be mocked out
type FileSystem interface {
	// MkdirAll creates all folders in the file system if they don't already exist.
	MkdirAll(targetFolderPath string) error
	ReadTextFile(filePath string) (string, error)
	WriteTextFile(targetFilePath string, desiredContents string) error
	WriteBinaryFile(targetFilePath string, desiredContents []byte) error
	Exists(path string) (bool, error)
	DirExists(path string) (bool, error)
	GetUserHomeDir() (string, error)
	OutputWarningMessage(string) error
	MkTempDir() (string, error)
	DeleteDir(path string)

	ReadDir(path string) ([]os.DirEntry, error)
	Open(fileName string) (io.ReadCloser, error)
	WalkDir(root string, walkDirFunc fs.WalkDirFunc) error

	// Returns the normal extension used for executable files.
	// ie: The .exe suffix in windows, or "" in unix-like systems.
	GetExecutableExtension() string

	// GetPathSeparator returns the file path separator specific
	// to this operating system.
	GetFilePathSeparator() string

	GetAllFilePaths(rootPath string) ([]string, error)
}

// TildaExpansion If a file starts with a tilda '~' character, expand it
// to the home folder of the user on this file system.
func TildaExpansion(fileSystem FileSystem, path string) (string, error) {
	var err error = nil
	if path != "" {
		if path[0] == '~' {
			var userHome string
			userHome, err = fileSystem.GetUserHomeDir()
			path = pathUtils.Join(userHome, path[1:])
		}
	}
	return path, err
}

//------------------------------------------------------------------------------------
// The implementation of the real os-delegating variant of the FileSystem interface
//------------------------------------------------------------------------------------

type OSFileSystem struct {
}

// NewOSFileSystem creates an implementation of the thin file system layer which delegates
// to the real os package calls.
func NewOSFileSystem() FileSystem {
	return new(OSFileSystem)
}

// ------------------------------------------------------------------------------------
// Interface methods...
// ------------------------------------------------------------------------------------

func (osFS *OSFileSystem) GetFilePathSeparator() string {
	return string(os.PathSeparator)
}

func (osFS *OSFileSystem) GetExecutableExtension() string {
	var extension string = ""
	if runtime.GOOS == "windows" {
		extension = ".exe"
	}
	return extension
}

func (osFS *OSFileSystem) MkTempDir() (string, error) {
	const DEFAULT_TEMP_FOLDER_PATH_FOR_THIS_OS = ""
	tempFolderPath, err := os.MkdirTemp(DEFAULT_TEMP_FOLDER_PATH_FOR_THIS_OS, "galasa-*")
	return tempFolderPath, err
}

func (osFS *OSFileSystem) DeleteDir(path string) {
	os.RemoveAll(path)
}

func (osFS *OSFileSystem) MkdirAll(targetFolderPath string) error {
	err := os.MkdirAll(targetFolderPath, 0755)
	if err != nil {
		err = fmt.Errorf("failed to create folders at %s - %s", targetFolderPath, err.Error())
	}
	return err
}

func (osFS *OSFileSystem) WriteBinaryFile(targetFilePath string, desiredContents []byte) error {
	err := os.WriteFile(targetFilePath, desiredContents, 0644)
	if err != nil {
		err = fmt.Errorf("failed to write file %s - %s", targetFilePath, err.Error())
	}
	return err
}

func (osFS *OSFileSystem) WriteTextFile(targetFilePath string, desiredContents string) error {
	bytes := []byte(desiredContents)
	err := osFS.WriteBinaryFile(targetFilePath, bytes)
	return err
}

func (*OSFileSystem) ReadTextFile(filePath string) (string, error) {
	text := ""
	bytes, err := os.ReadFile(filePath)
	if err != nil {
		err = fmt.Errorf("failed to read file %s - %s", filePath, err.Error())
	} else {
		text = string(bytes)
	}
	return text, err
}

func (*OSFileSystem) ReadDir(dirPath string) ([]os.DirEntry, error) {
	return os.ReadDir(dirPath)
}

func (*OSFileSystem) Open(fileName string) (io.ReadCloser, error) {
	return os.Open(fileName)
}

func (*OSFileSystem) WalkDir(root string, walkDirFunc fs.WalkDirFunc) error {
	return filepath.WalkDir(root, walkDirFunc)
}

func (*OSFileSystem) Exists(path string) (bool, error) {
	isExists := true
	_, err := os.Stat(path)
	if err != nil {
		if errors.Is(err, os.ErrNotExist) {
			// path/to/whatever does not exist
			isExists = false
			err = nil
		}
	}
	return isExists, err
}

func (*OSFileSystem) DirExists(path string) (bool, error) {
	isDirExists := true
	metadata, err := os.Stat(path)
	if err != nil {
		if errors.Is(err, os.ErrNotExist) {
			// path/to/whatever does not exist
			isDirExists = false
			err = nil
		}
	} else {
		isDirExists = metadata.IsDir()
	}
	return isDirExists, err
}

func (*OSFileSystem) GetUserHomeDir() (string, error) {
	dirName, err := os.UserHomeDir()
	if err != nil {
		err = fmt.Errorf("failed to get user home directory - %s", err.Error())
	}
	return dirName, err
}

func (OSFileSystem) OutputWarningMessage(message string) error {
	_, err := os.Stderr.WriteString(message)
	return err
}

func (osFS *OSFileSystem) GetAllFilePaths(rootPath string) ([]string, error) {
	var collectedFilePaths []string

	err := filepath.Walk(
		rootPath,
		func(path string, info os.FileInfo, err error) error {
			if err == nil {
				if !info.IsDir() {
					// It's not a folder. Only add file names.
					collectedFilePaths = append(collectedFilePaths, path)
				}
			}
			return err
		})
	return collectedFilePaths, err
}
