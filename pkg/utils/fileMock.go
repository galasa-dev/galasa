/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

// ------------------------------------------------------------------------------------
// The implementation of the file read closer interface.
// -----------------------------------------------------------------------------------
type MockFile struct {
    data []byte
    err error

    // The mock struct contains methods which can be over-ridden on a per-test basis.
    VirtualFunction_Close func() error
    VirtualFunction_Read func(p []byte) (int, error)
}

// Creates an implementation of a mock file and allows callers to set up different
// virtual functions to change the mock behaviours.
func NewOverridableMockFile() *MockFile {

    // Allocate the default structure
    mockFile := MockFile{data: nil, err: nil}

    mockFile.VirtualFunction_Close = func() error {
        return mockFile.mockFileClose()
    }

    mockFile.VirtualFunction_Read = func(data []byte) (int, error) {
        return mockFile.mockFileRead(data)
    }

    return &mockFile
}

// ------------------------------------------------------------------------------------
// Interface methods.
// ------------------------------------------------------------------------------------

func (mockFile *MockFile) Close() error {
    return mockFile.VirtualFunction_Close()
}

func (mockFile *MockFile) Read(p []byte) (int, error) {
    return mockFile.VirtualFunction_Read(p)
}

// ------------------------------------------------------------------------------------
// Default implementations of the methods.
// ------------------------------------------------------------------------------------

func (mockFile *MockFile) mockFileClose() error {
    return nil
}

func (mockFile *MockFile) mockFileRead(data []byte) (int, error) {
    copy(data, mockFile.data)
    return len(mockFile.data), mockFile.err
}