/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"io"

	"github.com/galasa-dev/cli/pkg/spi"
)

// Implementation of a byte reader to allow mocking out methods from the io package
type ByteReaderImpl struct {
}

func NewByteReader() spi.ByteReader {
	return new(ByteReaderImpl)
}

func (*ByteReaderImpl) ReadAll(reader io.Reader) ([]byte, error) {
	return io.ReadAll(reader)
}
