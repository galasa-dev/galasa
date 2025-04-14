/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package images

import "github.com/galasa-dev/cli/pkg/spi"

type ImageFileWriter interface {
	WriteImageFile(simpleFileName string, imageBytes []byte) error
	GetImageFilesWrittenCount() int
	IsImageFileWritable(simpleFileName string) (bool, error)
}

type ImageFileWriterImpl struct {
	fs                          spi.FileSystem
	imageFolderPath             string
	imageFilesWrittenCount      int
	forceOverwriteExistingFiles bool
}

func NewImageFileWriter(fs spi.FileSystem, imageFolderPath string, forceOverwriteExistingFiles bool) ImageFileWriter {
	writer := new(ImageFileWriterImpl)
	writer.fs = fs
	writer.imageFolderPath = imageFolderPath
	writer.imageFilesWrittenCount = 0
	writer.forceOverwriteExistingFiles = forceOverwriteExistingFiles
	return writer
}

func (writer *ImageFileWriterImpl) GetImageFilesWrittenCount() int {
	return writer.imageFilesWrittenCount
}

func (writer *ImageFileWriterImpl) IsImageFileWritable(simpleFileName string) (bool, error) {
	fullyQualifiedTargetImageFilePath := writer.simpleFileToFullyQualifiedFilePath(simpleFileName)
	isWritable, err := writer.isFullyQualifiedImageFileWritable(fullyQualifiedTargetImageFilePath)
	return isWritable, err
}

func (writer *ImageFileWriterImpl) simpleFileToFullyQualifiedFilePath(simpleFileName string) string {
	return writer.imageFolderPath + writer.fs.GetFilePathSeparator() + simpleFileName
}

func (writer *ImageFileWriterImpl) isFullyQualifiedImageFileWritable(qualifiedFileName string) (bool, error) {
	var isExistsAlready bool
	var err error
	var isWritable bool = false

	isExistsAlready, err = writer.fs.Exists(qualifiedFileName)
	if err == nil {
		if isExistsAlready {
			// log.Printf("File %s already exists. So not over-writing it.\n", qualifiedFileName)
			isWritable = writer.forceOverwriteExistingFiles
		} else {
			// It's writeable.
			isWritable = true
		}
	}
	return isWritable, err
}

func (writer *ImageFileWriterImpl) WriteImageFile(simpleFileName string, imageBytes []byte) error {
	var err error
	fullyQualifiedTargetImageFilePath := writer.simpleFileToFullyQualifiedFilePath(simpleFileName)

	var isWritable bool
	isWritable, err = writer.isFullyQualifiedImageFileWritable(fullyQualifiedTargetImageFilePath)
	if err == nil {
		if isWritable {
			err = writer.fs.WriteBinaryFile(fullyQualifiedTargetImageFilePath, imageBytes)
			if err == nil {
				writer.imageFilesWrittenCount = writer.imageFilesWrittenCount + 1
			}
		}
	}
	return err
}
