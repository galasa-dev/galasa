/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package files

// ------------------------------------------------------------------------------------
// The implementation of the io.writer interface.
// -----------------------------------------------------------------------------------
type MockFile struct {
	fileSystem *MockFileSystem
	path       string
	err        error

	// The mock struct contains methods which can be over-ridden on a per-test basis.
	VirtualFunction_Write func(contents []byte) (int, error)
	VirtualFunction_Close func() error
}

// Creates an implementation of a mock file and allows callers to set up different
// virtual functions to change the mock behaviours.
func NewOverridableMockFile(fs *MockFileSystem, filePath string) *MockFile {

	// Allocate the default structure
	mockFile := MockFile{
		fileSystem: fs,
		path:       filePath,
		err:        nil,
	}

	mockFile.VirtualFunction_Write = func(data []byte) (int, error) {
		return mockFile.mockFileWrite(data)
	}

	mockFile.VirtualFunction_Close = func() error {
		return mockFile.mockFileClose()
	}

	return &mockFile
}

// ------------------------------------------------------------------------------------
// Interface methods.
// ------------------------------------------------------------------------------------

func (mockFile *MockFile) Write(contents []byte) (int, error) {
	return mockFile.VirtualFunction_Write(contents)
}

func (mockFile *MockFile) Close() error {
	// Do nothing...
	return nil
}

// ------------------------------------------------------------------------------------
// Default implementations of the methods.
// ------------------------------------------------------------------------------------

func (mockFile *MockFile) mockFileWrite(data []byte) (int, error) {
	fileNode := mockFile.fileSystem.data[mockFile.path]
	fileNode.content = append(fileNode.content, data...)

	return len(data), mockFile.err
}

func (mockFile *MockFile) mockFileClose() error {
	// Do nothing...
	return nil
}
