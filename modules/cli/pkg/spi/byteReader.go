/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package spi

import "io"

// An interface to allow for mocking out "io" package reading-related methods
type ByteReader interface {
    ReadAll(reader io.Reader) ([]byte, error)
}
