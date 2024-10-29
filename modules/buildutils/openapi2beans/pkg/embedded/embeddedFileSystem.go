/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package embedded

import (
	"embed"

	openapi2beans_errors "github.com/dev-galasa/buildutils/openapi2beans/pkg/errors"
)

type ReadOnlyFileSystem interface {
	ReadFile(filepath string) ([]byte, error)
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

//------------------------------------------------------------------------------------
// Interface methods...
//------------------------------------------------------------------------------------

// The only thing which this class actually supports.
func (fs *EmbeddedFileSystem) ReadFile(filepath string) ([]byte, error) {

	bytes, err := fs.embeddedFileSystem.ReadFile(filepath)
	if err != nil {
		openapi2beans_errors.NewError("Error: unable to read embedded file system. Reason is: %s", err.Error())
	}
	return bytes, err
}
