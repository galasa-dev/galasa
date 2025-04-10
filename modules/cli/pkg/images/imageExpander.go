/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package images

import (
	"log"
	"strings"

	"github.com/galasa-dev/cli/pkg/files"
	"github.com/galasa-dev/cli/pkg/spi"
)

// Given a root folder, we scan for .gz files which need expansion into images.

type ImageExpander interface {
	ExpandImages(rootFolderPath string) error
	ExpandImage(pathToFile string) error
	GetExpandedImageFileCount() int
}

// ********** A null implementation. Does nothing. ***********
type ImageExpanderNullImpl struct {
	expandedFileCounter int
}

func (expander *ImageExpanderNullImpl) GetExpandedImageFileCount() int {
	return expander.expandedFileCounter
}

func (expander *ImageExpanderNullImpl) ExpandImages(rootFolderPath string) error {
	return nil
}

func (expander *ImageExpanderNullImpl) ExpandImage(filePath string) error {
	return nil
}

func NewImageExpanderNullImpl() ImageExpander {
	expander := new(ImageExpanderNullImpl)
	expander.expandedFileCounter = 0
	return expander
}

// ******** A real implementation of an expander *******
type ImageExpanderImpl struct {
	fs                          spi.FileSystem
	renderer                    ImageRenderer
	expandedFileCounter         int
	forceOverwriteExistingFiles bool
}

func NewImageExpander(fs spi.FileSystem, renderer ImageRenderer, forceOverwriteExistingFiles bool) ImageExpander {
	expander := new(ImageExpanderImpl)
	expander.fs = fs
	expander.renderer = renderer
	expander.expandedFileCounter = 0
	expander.forceOverwriteExistingFiles = forceOverwriteExistingFiles
	return expander
}

func (expander *ImageExpanderImpl) GetExpandedImageFileCount() int {
	return expander.expandedFileCounter
}

func (expander *ImageExpanderImpl) ExpandImage(filePath string) error {
	var err error

	if strings.HasSuffix(filePath, ".gz") {
		// It's a gz file we might like to expand.
		err = expander.expandGzFile(filePath)
	}
	return err
}

func (expander *ImageExpanderImpl) ExpandImages(rootFolderPath string) error {
	var err error

	log.Printf("Expanding any 3270 image descriptions we have into images. Folder scanned: %s\n", rootFolderPath)

	var paths []string
	paths, err = expander.fs.GetAllFilePaths(rootFolderPath)

	if err == nil {
		for _, filePath := range paths {
			err = expander.ExpandImage(filePath)
			if err != nil {
				break
			}
		}
	}

	return err
}

func (expander *ImageExpanderImpl) expandGzFile(gzFilePath string) error {
	var err error

	var targetImageFolderPath string
	targetImageFolderPath, err = expander.calculateTargetImagePaths(gzFilePath)
	if err == nil {

		// Only bother going further if the target folder is non-blank.
		if targetImageFolderPath != "" {

			err = expander.fs.MkdirAll(targetImageFolderPath)
			if err == nil {

				gzip := files.NewGzipFile(expander.fs, gzFilePath)
				var binaryContent []byte
				binaryContent, err = gzip.ReadBytes()
				if err != nil {
					log.Printf("Could not read the contents of hte gzip file. cause:%v\n", err)
				} else {

					writer := NewImageFileWriter(expander.fs, targetImageFolderPath, expander.forceOverwriteExistingFiles)

					err = expander.renderer.RenderJsonBytesToImageFiles(binaryContent, writer)

					expander.expandedFileCounter = expander.expandedFileCounter + writer.GetImageFilesWrittenCount()
				}
			}
		}
	}
	return err
}

func (expander *ImageExpanderImpl) calculateTargetImagePaths(gzFilePath string) (string, error) {
	var err error
	var desiredImageFolderPath string

	// Figure out the file path of the image we want to create.
	// Into a folder called "images" next to the .gz folder
	separator := expander.fs.GetFilePathSeparator()
	filePathParts := strings.Split(gzFilePath, separator)

	if len(filePathParts)-3 < 0 {
		// log.Printf("gz file %s found, but it's not in a 'terminals/termXXX' folder so ignoring.\n", gzFilePath)
	} else {
		// The json descriptions of the panels appear in zos3270/terminals/term1/term1-0001.gz
		// So we want images to appear in zos3270/images/term1/term1-0001.png
		if filePathParts[len(filePathParts)-3] != "terminals" {
			// It's not in the correct structure, so ignoring.
			// log.Printf("gz file %s found, but it's not in a 'terminals' folder so ignoring.\n", gzFilePath)
		} else {

			// Replace the .gz file extension with .png
			simpleFileName := filePathParts[len(filePathParts)-1]
			indexOfExtension := strings.LastIndex(simpleFileName, ".gz")
			simpleTargetFileName := simpleFileName[:indexOfExtension] + ".png"
			filePathParts[len(filePathParts)-1] = simpleTargetFileName

			// Replace the "terminals" part of the path with "images"
			filePathParts[len(filePathParts)-3] = "images"

			// Roll together all the parts of the files to get the folder and the file
			desiredImageFolderPath = strings.Join(filePathParts[:len(filePathParts)-1], separator)
		}
	}
	return desiredImageFolderPath, err
}
