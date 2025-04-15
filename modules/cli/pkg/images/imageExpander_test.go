/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package images

import (
	"bytes"
	"image"
	"image/color"
	"image/png"
	"log"
	"os"
	"runtime"
	"testing"

	"github.com/galasa-dev/cli/pkg/embedded"
	"github.com/galasa-dev/cli/pkg/files"
	"github.com/stretchr/testify/assert"
)

var (
	BLACK = color.RGBA{0, 0, 0, 255}
)

func TestCanCalculateTargetPathsOk(t *testing.T) {
	fs := files.NewMockFileSystem()
	embeddedFs := embedded.NewMockReadOnlyFileSystem()
	renderer := NewImageRenderer(embeddedFs)
	expander := NewImageExpander(fs, renderer, false).(*ImageExpanderImpl)
	folderPath, err := expander.calculateTargetImagePaths("a/b/terminals/c/e.gz")

	assert.Nil(t, err, "could not get paths when we should have been able to.")
	if err == nil {
		assert.Equal(t, "a/b/images/c", folderPath)
	}
}

func TestCalculatesBlankPathIfPathTooShort(t *testing.T) {
	fs := files.NewMockFileSystem()
	embeddedFs := embedded.NewMockReadOnlyFileSystem()
	renderer := NewImageRenderer(embeddedFs)
	expander := NewImageExpander(fs, renderer, false).(*ImageExpanderImpl)
	folderPath, err := expander.calculateTargetImagePaths("a/e.gz")

	assert.Nil(t, err, "could not get paths when we should have been able to.")
	if err == nil {
		assert.Equal(t, "", folderPath, "We expected a blank folder, as rendering this gz file is not appropriate.")
	}
}

func TestCalculatesBlankPathIfPathDoesntContainTerminals(t *testing.T) {
	fs := files.NewMockFileSystem()
	embeddedFs := embedded.NewMockReadOnlyFileSystem()
	renderer := NewImageRenderer(embeddedFs)
	expander := NewImageExpander(fs, renderer, false).(*ImageExpanderImpl)
	folderPath, err := expander.calculateTargetImagePaths("a/b/c/d/e.gz")

	assert.Nil(t, err, "could not get paths when we should have been able to.")
	if err == nil {
		assert.Equal(t, "", folderPath, "We expected a blank folder, as rendering this gz file is not appropriate.")
	}
}

func TestCanExpandAGzFileToAnImageFile(t *testing.T) {

	realFs := files.NewOSFileSystem()
	embeddedFs := embedded.GetReadOnlyFileSystem()

	var gzContents []byte
	var err error

	gzContents, err = realFs.ReadBinaryFile("./testdata/gzipExample/term1-00001.gz")

	assert.Nil(t, err, "could not load the real gz file data")
	if err == nil {

		// Load the real gz contents into the mock file system...
		// so any image files we create don't infect the real file system.
		fs := files.NewMockFileSystem()
		err = fs.WriteBinaryFile("/U423/zos3270/terminals/term1/term1-1.gz", gzContents)
		assert.Nil(t, err, "could not write real gz contents into the mock file system")
		if err == nil {

			renderer := NewImageRenderer(embeddedFs)
			expander := NewImageExpander(fs, renderer, false)

			// When...
			err = expander.ExpandImages("/U423")
			assert.Nil(t, err, "could not expand images")
			if err == nil {

				// Then...
				var isExists bool
				isExists, err = fs.DirExists("/U423/zos3270/images/term1")
				assert.Nil(t, err, "could not find out if file exists or not")
				if err == nil {

					assert.True(t, isExists, "Image folder %s was not created.", "/U423/zos3270/images/term1")
					if isExists {

						// Read the rendered file contents.
						var renderedContents []byte
						renderedContents, err = fs.ReadBinaryFile("/U423/zos3270/images/term1/term1-00001.png")
						assert.Nil(t, err, "could not read rendered file")
						if err == nil {
							isSame := compareImage(t, renderedContents, "./testdata/gzipExample/images-to-compare", "term1-00001.png")
							if isSame {
								// The example gz file contains 10 screens, each of which should be rendered
								assert.Equal(t, 10, expander.GetExpandedImageFileCount(), "wrong number of expanded files counted.")
							}
						}
					}
				}
			}
		}
	}
}

func TestCanExpandASingleFileBumpsCounter(t *testing.T) {

	realFs := files.NewOSFileSystem()
	embeddedFs := embedded.GetReadOnlyFileSystem()

	var gzContents []byte
	var err error

	gzContents, err = realFs.ReadBinaryFile("./testdata/gzipExample/term1-00001.gz")

	assert.Nil(t, err, "could not load the real gz file data")
	if err == nil {

		// Load the real gz contents into the mock file system...
		// so any image files we create don't infect the real file system.
		fs := files.NewMockFileSystem()
		err = fs.WriteBinaryFile("/U423/zos3270/terminals/term1/term1-1.gz", gzContents)
		assert.Nil(t, err, "could not write real gz contents into the mock file system")
		if err == nil {

			renderer := NewImageRenderer(embeddedFs)
			expander := NewImageExpander(fs, renderer, false)

			// When...
			err = expander.ExpandImage("/U423/zos3270/terminals/term1/term1-1.gz")
			assert.Nil(t, err, "could not expand image")
			if err == nil {

				// Then...
				var isExists bool
				isExists, err = fs.DirExists("/U423/zos3270/images/term1")
				assert.Nil(t, err, "could not find out if file exists or not")
				if err == nil {

					assert.True(t, isExists, "Image folder %s was not created.", "/U423/zos3270/images/term1")
					if isExists {

						// Read the rendered file contents.
						var renderedContents []byte
						renderedContents, err = fs.ReadBinaryFile("/U423/zos3270/images/term1/term1-00001.png")
						assert.Nil(t, err, "could not read rendered file")
						if err == nil {
							isSame := compareImage(t, renderedContents, "./testdata/gzipExample/images-to-compare", "term1-00001.png")
							if isSame {
								// The example gz file contains 10 screens, each of which should be rendered
								assert.Equal(t, 10, expander.GetExpandedImageFileCount(), "wrong number of expanded files counted.")
							}
						}
					}
				}
			}
		}
	}
}

func compareImage(t *testing.T, renderedImageToCompare []byte, compareFolderPath string, imageToCompareSimpleFileName string) bool {

	var isSame bool

	realFs := files.NewOSFileSystem()

	operatingSystem := runtime.GOOS

	separatorChar := realFs.GetFilePathSeparator()
	expectedImageFolderPath := compareFolderPath + separatorChar + operatingSystem

	imageFolderExists, err := realFs.DirExists(expectedImageFolderPath)
	assert.Nil(t, err, "Error finding out if folder %s exists or not, so not comparing the image with one we generated earlier. reason: %v", expectedImageFolderPath, err)
	if err == nil {
		if !imageFolderExists {
			log.Printf("Folder %s does not exist, so not comparing the image with one we generated earlier.", expectedImageFolderPath)
		} else {
			expectedImageFilePath := expectedImageFolderPath + separatorChar + imageToCompareSimpleFileName

			imageFileExists, err := realFs.Exists(expectedImageFilePath)
			assert.Nil(t, err, "Error finding out if file %s exists or not, so not comparing the image with one we generated earlier. reason: %v", expectedImageFilePath, err)
			if err == nil {

				if !imageFileExists {
					log.Printf("File %s does not exist, so not comparing the image with one we generated earlier.", expectedImageFilePath)
				} else {
					// Read the file contents which we think the image should be once rendered.
					var expectedContents []byte
					expectedContents, err = realFs.ReadBinaryFile(expectedImageFilePath)
					assert.Nil(t, err, "could not read image file we will compare against")
					if err == nil {
						// Compare the files.
						isSame, _ = compareTwoImages(t, renderedImageToCompare, expectedContents)
					}
				}
			}
		}
	}
	return isSame
}

func compareTwoImages(t *testing.T, renderedContents []byte, expectedContents []byte) (bool, error) {
	var isSame bool = true
	var err error
	var renderedImage image.Image
	var expectedImage image.Image

	renderedImage, err = png.Decode(bytes.NewReader(renderedContents))
	if err == nil {
		expectedImage, err = png.Decode(bytes.NewReader(expectedContents))

		renderedImageBounds := renderedImage.Bounds()
		expectedImageBounds := expectedImage.Bounds()

		// Make sure the rendered image is the same size as the expected image
		if renderedImageBounds == expectedImageBounds {
			for column := expectedImage.Bounds().Min.Y; column < expectedImage.Bounds().Max.Y; column++ {
				for row := expectedImage.Bounds().Min.X; row < expectedImage.Bounds().Max.X; row++ {
					expectedPixelColor := expectedImage.At(column, row)
					renderedPixelColor := renderedImage.At(column, row)

					// We're ignoring colors because some pixels may have slight differences in RGB values, which
					// causes tests to fail. Whenever the expected pixel is black, the rendered pixel should also
					// be black, and when the expected pixel is a color, the rendered pixel should also be a color.
					if (expectedPixelColor == BLACK && renderedPixelColor != BLACK) ||
						(expectedPixelColor != BLACK && renderedPixelColor == BLACK) {
						assert.Fail(t, "error", "rendered RGBA value %v does not match expected RGBA value %v at (x: %d, y: %d)", renderedPixelColor, expectedPixelColor, column, row)
						isSame = false
						break
					}
				}
				if !isSame {
					break
				}
			}
		} else {
			assert.Fail(t, "error", "rendered image bounds %v are different to expected image bounds %v", renderedImageBounds, expectedImageBounds)
			isSame = false
		}

		if isSame {
			log.Printf("Rendered file and stored file to compare against were exactly the same.\n")
		} else {
			// Files don't match, so save the file we got for manual inspection.
			// If the user wants, they can copy this file into the project as expected test data.
			var renderedFile *os.File

			renderedFile, err = os.CreateTemp("", "rendered-image-*.png")
			if err == nil {
				defer renderedFile.Close()
				renderedFile.Write(renderedContents)

				log.Printf("A copy of the rendered file has been saved to %s for manual inspection if required.\n", renderedFile.Name())
			}
		}
	}

	return isSame, err
}
