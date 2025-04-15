/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package embedded

import (
	"io/fs"
	"os"
	"path/filepath"
	"strings"
)

type MockDirEntry struct {
	os.DirEntry
	DirName string
}

func (mockDirEntry MockDirEntry) Name() string {
	return mockDirEntry.DirName
}

type MockReadOnlyFileSystem struct {
	files map[string]string
}

func NewMockReadOnlyFileSystem() *MockReadOnlyFileSystem {
	fs := MockReadOnlyFileSystem{
		files: make(map[string]string, 0),
	}
	return &fs
}

// WriteFile - This function is not on the ReadOnlyFileSystem interface, but does allow unit tests
// to add data files to the mock file system, so the code under test can read it back.
func (fs *MockReadOnlyFileSystem) WriteFile(filePath string, content string) {
	fs.files[filePath] = content
}

func (fs *MockReadOnlyFileSystem) GetFileSeparator() string {
	return "/"
}

func (fs *MockReadOnlyFileSystem) ReadFile(filePath string) ([]byte, error) {
	content := fs.files[filePath]
	return []byte(content), nil
}

func (fs *MockReadOnlyFileSystem) ReadDir(directoryPath string) ([]fs.DirEntry, error) {
	dirEntries := make([]os.DirEntry, 0)
	for key := range fs.files {
		if strings.HasPrefix(key, directoryPath) && key != directoryPath {
			dirEntries = append(dirEntries, MockDirEntry{DirName: filepath.Base(key)})
		}
	}
	return dirEntries, nil
}
