/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package images

import (
	"testing"

	"github.com/galasa-dev/cli/pkg/files"
	"github.com/stretchr/testify/assert"
)

func TestCanWriteAFile(t *testing.T) {
	fs := files.NewMockFileSystem()
	fs.MkdirAll("/my/images/folder")
	writer := NewImageFileWriter(fs, "/my/images/folder", false)
	buff := []byte("hello world")

	err := writer.WriteImageFile("File1.png", buff)

	if assert.Nil(t, err, "should have been able to write an image file with no error!") {
		var isExists bool
		isExists, err = fs.Exists("/my/images/folder/File1.png")
		if assert.Nil(t, err, "The image file should have been created! Error trying to see if it's there or not.") {
			if assert.True(t, isExists, "The image file should have been created. It's not there.") {
				var bytesReadBack []byte
				bytesReadBack, err = fs.ReadBinaryFile("/my/images/folder/File1.png")
				if assert.Nil(t, err, "Should have been able to read the file contents back.") {
					stringGotBack := string(bytesReadBack)
					assert.Equal(t, stringGotBack, "hello world", "test didn't get back what it thinks it wrote to disk")
				}
			}
		}
	}

	assert.Equal(t, writer.GetImageFilesWrittenCount(), 1)
}

func TestDoesntWriteOverAFileIfItAlreadyExistsAndForceOverwriteIsTrue(t *testing.T) {
	fs := files.NewMockFileSystem()
	fs.MkdirAll("/my/images/folder")
	writer := NewImageFileWriter(fs, "/my/images/folder", true)
	buff := []byte("hello world")

	err := writer.WriteImageFile("File1.png", buff)

	if assert.Nil(t, err, "should have been able to write an image file with no error!") {
		var isExists bool
		isExists, err = fs.Exists("/my/images/folder/File1.png")
		if assert.Nil(t, err, "The image file should have been created! Error trying to see if it's there or not.") {
			if assert.True(t, isExists, "The image file should have been created. It's not there.") {
				var bytesReadBack []byte
				bytesReadBack, err = fs.ReadBinaryFile("/my/images/folder/File1.png")
				if assert.Nil(t, err, "Should have been able to read the file contents back.") {
					stringGotBack := string(bytesReadBack)
					assert.Equal(t, stringGotBack, "hello world", "test didn't get back what it thinks it wrote to disk")
				}
			}
		}
	}

	// Now try to write the same thing again... but a different message.

	buff = []byte("Not the original file")
	err = writer.WriteImageFile("File1.png", buff)

	if assert.Nil(t, err, "should have been able to write an image file with no error!") {
		var isExists bool
		isExists, err = fs.Exists("/my/images/folder/File1.png")
		if assert.Nil(t, err, "The image file should have been created! Error trying to see if it's there or not.") {
			if assert.True(t, isExists, "The image file should have been created. It's not there.") {
				var bytesReadBack []byte
				bytesReadBack, err = fs.ReadBinaryFile("/my/images/folder/File1.png")
				if assert.Nil(t, err, "Should have been able to read the file contents back.") {
					stringGotBack := string(bytesReadBack)
					assert.Equal(t, stringGotBack, "Not the original file", "test didn't get back the new text. File has not been over-written when it should have been.")
				}
			}
		}
	}

	assert.Equal(t, writer.GetImageFilesWrittenCount(), 2) // One for the original file written, A second for the over-write.
}

func TestDoesntWriteOverAFileIfItAlreadyExistsAndForceOverWriteIsFalse(t *testing.T) {
	fs := files.NewMockFileSystem()
	fs.MkdirAll("/my/images/folder")
	writer := NewImageFileWriter(fs, "/my/images/folder", false)
	buff := []byte("hello world")

	err := writer.WriteImageFile("File1.png", buff)

	if assert.Nil(t, err, "should have been able to write an image file with no error!") {
		var isExists bool
		isExists, err = fs.Exists("/my/images/folder/File1.png")
		if assert.Nil(t, err, "The image file should have been created! Error trying to see if it's there or not.") {
			if assert.True(t, isExists, "The image file should have been created. It's not there.") {
				var bytesReadBack []byte
				bytesReadBack, err = fs.ReadBinaryFile("/my/images/folder/File1.png")
				if assert.Nil(t, err, "Should have been able to read the file contents back.") {
					stringGotBack := string(bytesReadBack)
					assert.Equal(t, stringGotBack, "hello world", "test didn't get back what it thinks it wrote to disk")
				}
			}
		}
	}

	// Now try to write the same thing again... but a different message.

	buff = []byte("Not the original file")
	err = writer.WriteImageFile("File1.png", buff)

	if assert.Nil(t, err, "should have been able to write an image file with no error!") {
		var isExists bool
		isExists, err = fs.Exists("/my/images/folder/File1.png")
		if assert.Nil(t, err, "The image file should have been created! Error trying to see if it's there or not.") {
			if assert.True(t, isExists, "The image file should have been created. It's not there.") {
				var bytesReadBack []byte
				bytesReadBack, err = fs.ReadBinaryFile("/my/images/folder/File1.png")
				if assert.Nil(t, err, "Should have been able to read the file contents back.") {
					stringGotBack := string(bytesReadBack)
					assert.Equal(t, stringGotBack, "hello world", "test didn't get back the original text. File has been over-written when it should not have been.")
				}
			}
		}
	}

	assert.Equal(t, writer.GetImageFilesWrittenCount(), 1)
}
