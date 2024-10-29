/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
    "os"
)

// ------------------------------------------------------------------------------------
// The implementation of the DirEntry interface.
// -----------------------------------------------------------------------------------
type MockDirEntry struct {
    os.DirEntry
    DirName string
}

// ------------------------------------------------------------------------------------
// Interface methods.
// ------------------------------------------------------------------------------------

func (mockDirEntry MockDirEntry) Name() string {
    return mockDirEntry.DirName
}
