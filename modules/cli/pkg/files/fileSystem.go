/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package files

import (
	"errors"
	"io"
	"os"
	pathUtils "path"
	"path/filepath"
	"runtime"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/spi"
)

// TildaExpansion If a file starts with a tilda '~' character, expand it
// to the home folder of the user on this file system.
func TildaExpansion(fileSystem spi.FileSystem, path string) (string, error) {
	var err error
	if path != "" {
		if path[0] == '~' {
			var userHome string
			userHome, err = fileSystem.GetUserHomeDirPath()
			path = pathUtils.Join(userHome, path[1:])
		}
	}
	return path, err
}

func TildaExpansionMultiple(fileSystem spi.FileSystem, paths []string) ([]string, error) {
	expandedPaths := make([]string, len(paths))
	var expandedPath string
	var err error

	for i, p := range paths {
		expandedPath, err = TildaExpansion(fileSystem, p)
		if err != nil {
			break
		}
		expandedPaths[i] = expandedPath
	}
	return expandedPaths, err
}

//------------------------------------------------------------------------------------
// The implementation of the real os-delegating variant of the FileSystem interface
//------------------------------------------------------------------------------------

type OSFileSystem struct {
}

// NewOSFileSystem creates an implementation of the thin file system layer which delegates
// to the real os package calls.
func NewOSFileSystem() spi.FileSystem {
	return new(OSFileSystem)
}

// ------------------------------------------------------------------------------------
// Interface methods...
// ------------------------------------------------------------------------------------

func (osFS *OSFileSystem) Create(path string) (io.WriteCloser, error) {
	fileWriter, err := os.Create(path)
	return fileWriter, err
}

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

func (osFS *OSFileSystem) DeleteFile(path string) {
	os.Remove(path)
}

func (osFS *OSFileSystem) MkdirAll(targetFolderPath string) error {
	err := os.MkdirAll(targetFolderPath, 0755)
	if err != nil {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_FAILED_TO_CREATE_FOLDERS, targetFolderPath, err.Error())
	}
	return err
}

func (osFS *OSFileSystem) WriteBinaryFile(targetFilePath string, desiredContents []byte) error {
	err := os.WriteFile(targetFilePath, desiredContents, 0644)
	if err != nil {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_FAILED_TO_WRITE_FILE, targetFilePath, err.Error())
	}
	return err
}

func (osFS *OSFileSystem) WriteTextFile(targetFilePath string, desiredContents string) error {
	bytes := []byte(desiredContents)
	err := osFS.WriteBinaryFile(targetFilePath, bytes)
	return err
}

func (osFS *OSFileSystem) ReadTextFile(filePath string) (string, error) {
	text := ""
	bytes, err := osFS.ReadBinaryFile(filePath)
	if err == nil {
		text = string(bytes)
	}
	return text, err
}

func (*OSFileSystem) ReadBinaryFile(filePath string) ([]byte, error) {
	bytes, err := os.ReadFile(filePath)
	if err != nil {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_FAILED_TO_READ_FILE, filePath, err.Error())
	}
	return bytes, err
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

func (*OSFileSystem) GetUserHomeDirPath() (string, error) {
	dirName, err := os.UserHomeDir()
	if err != nil {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_FAILED_TO_FIND_USER_HOME, err.Error())
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
