/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"io"
	"log"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/spi"
)

// CaptureExecutionLogs captures the logs for a given execution function to a file or stderr.
func CaptureExecutionLogs(factory spi.Factory, logFileName string, executionFunc func() error) error {
	var err error
	fileSystem := factory.GetFileSystem()
	err = CaptureLog(fileSystem, logFileName)
	if err == nil {
		err = executionFunc()
	}
	return err
}

/*
 * CaptureLog(logFileName) decides whether to re-direct the log information to the
 * specified file, or if the file name is "-" or empty, the log information won't be
 * re-directed, but will appear on stderr.
 */
func CaptureLog(fileSystem spi.FileSystem, logFileName string) error {

	var err error

	// Send the log to a file
	if logFileName == "-" {
		// Log to the console. This is the default behaviour.
	} else {
		if logFileName == "" {
			// Log not specified, so needs to be suppressed.
			log.SetOutput(io.Discard)
		} else {

			var isLogFileExisting bool

			isLogFileExisting, err = fileSystem.Exists(logFileName)
			if err == nil {

				if isLogFileExisting {
					// We are going to try to create the log file, causing it to truncate...
					var isDir bool
					isDir, err = fileSystem.DirExists(logFileName)
					if err == nil {
						if isDir {
							err = galasaErrors.NewGalasaError(
								galasaErrors.GALASA_ERROR_LOG_FILE_IS_A_FOLDER,
								logFileName,
							)
						}
					}
				}

				if err == nil {

					// The user has set the logFileName using the --log xxxx syntax
					// Note: If the file exists, it gets truncated.
					// Default permissions are 0666
					var f io.Writer
					f, err = fileSystem.Create(logFileName)
					if err == nil {
						log.SetOutput(f)
					} else {
						err = galasaErrors.NewGalasaError(
							galasaErrors.GALASA_ERROR_OPEN_LOG_FILE_FAILED,
							logFileName,
							err.Error(),
						)
					}
				}
			}
		}
	}

	return err
}
