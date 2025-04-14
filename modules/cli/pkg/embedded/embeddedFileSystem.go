/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package embedded

import (
	"embed"
	"io/fs"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
)

type ReadOnlyFileSystem interface {
	ReadFile(filePath string) ([]byte, error)
	ReadDir(directoryPath string) ([]fs.DirEntry, error)
	GetFileSeparator() string
}

type EmbeddedFileSystem struct {
	embeddedFileSystem embed.FS
}

func NewReadOnlyFileSystem() ReadOnlyFileSystem {
	result := EmbeddedFileSystem{
		embeddedFileSystem: embeddedFileSystem,
	}
	return &result
}

// ------------------------------------------------------------------------------------
// Interface methods...
// ------------------------------------------------------------------------------------
func (fs *EmbeddedFileSystem) GetFileSeparator() string {
	return "/"
}

// The only thing which this class actually supports.
func (fs *EmbeddedFileSystem) ReadFile(filePath string) ([]byte, error) {

	bytes, err := fs.embeddedFileSystem.ReadFile(filePath)
	if err != nil {
		galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_EMBEDDED_FS_READ_FAILED, err.Error())
	}
	return bytes, err
}

func (fs *EmbeddedFileSystem) ReadDir(directoryPath string) ([]fs.DirEntry, error) {

	dirEntries, err := fs.embeddedFileSystem.ReadDir(directoryPath)
	if err != nil {
		galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_EMBEDDED_FS_READ_FAILED, err.Error())
	}
	return dirEntries, err
}
