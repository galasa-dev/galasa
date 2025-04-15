/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package embedded

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestFileSeparatorIsASlash(t *testing.T) {
	fs := NewReadOnlyFileSystem()

	separator := fs.GetFileSeparator()

	assert.Equal(t, "/", separator)
}
