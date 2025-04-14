/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import "github.com/galasa-dev/cli/pkg/spi"

func AddJavaRuntimeToMock(fileSystem spi.FileSystem, baseJavaFolderName string) {
	separator := fileSystem.GetFilePathSeparator()
	fileSystem.MkdirAll(baseJavaFolderName + separator + "bin")
	fileSystem.WriteBinaryFile(baseJavaFolderName+separator+
		"bin"+separator+"java"+fileSystem.GetExecutableExtension(),
		[]byte("some random content pretending to be a JRE program"),
	)
}
