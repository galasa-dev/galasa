/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"errors"
	"io"

	"github.com/galasa-dev/cli/pkg/spi"
)

// Mock implementation of a byte reader to allow for simulating failed read operations
type MockByteReader struct {
	throwReadError bool
}

func NewMockByteReaderAsMock(throwReadError bool) *MockByteReader {
	return &MockByteReader{
		throwReadError: throwReadError,
	}
}

func NewMockByteReader() spi.ByteReader {
	return NewMockByteReaderAsMock(false)
}

func (mockReader *MockByteReader) ReadAll(reader io.Reader) ([]byte, error) {
	var err error
	var bytes []byte
	if mockReader.throwReadError {
		err = errors.New("simulating a read failure")
	} else {
		bytes, err = io.ReadAll(reader)
	}
	return bytes, err
}
