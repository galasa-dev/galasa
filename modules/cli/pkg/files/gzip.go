/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package files

import (
	"bytes"
	"compress/gzip"
	"io"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/spi"
)

// For gzip files, we need a utility which reads and writes gzip files to a file system.
type GzipImpl struct {
	path string
	fs   spi.FileSystem
}

type GzipFile interface {
	ReadBytes() ([]byte, error)
	WriteBytes(binaryContent []byte) error
}

func NewGzipFile(fs spi.FileSystem, pathToGzip string) GzipFile {
	gzip := new(GzipImpl)
	gzip.path = pathToGzip
	gzip.fs = fs
	return gzip
}

func (gzipFile *GzipImpl) WriteBytes(binaryContent []byte) error {
	var err error

	// Convert from binary data into compressed binary data.
	var buff []byte
	contentWriter := bytes.NewBuffer(buff)

	gzipWriter := gzip.NewWriter(contentWriter)
	defer gzipWriter.Close()

	_, err = gzipWriter.Write(binaryContent)
	if err != nil {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_FAILED_TO_COMPRESS_BINARY_DATA, gzipFile.path, err.Error())
	} else {

		err = gzipWriter.Flush()
		if err != nil {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_FAILED_TO_FLUSH_BINARY_DATA, gzipFile.path, err.Error())
		} else {

			err = gzipWriter.Close()
			if err != nil {
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_FAILED_TO_CLOSE_GZIP_FILE, gzipFile.path, err.Error())
			} else {

				// We have compressed binary data now.
				compressedBinaryContent := contentWriter.Bytes()
				err = gzipFile.fs.WriteBinaryFile(gzipFile.path, compressedBinaryContent)
			}
		}
	}

	return err
}

func (gzipFile *GzipImpl) ReadBytes() ([]byte, error) {
	var uncompressedBytes []byte

	buffer, err := gzipFile.fs.ReadBinaryFile(gzipFile.path)
	if err != nil {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_FAILED_TO_OPEN_GZIP_FILE, gzipFile.path, err.Error())
	} else {

		bufferReader := bytes.NewReader(buffer)

		var zipReader *gzip.Reader
		zipReader, err = gzip.NewReader(bufferReader)
		if err != nil {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_FAILED_TO_SETUP_READER_GZIP_FILE, gzipFile.path, err.Error())

		} else {
			defer zipReader.Close()

			uncompressedBytes, err = io.ReadAll(zipReader)
			if err != nil {
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_FAILED_TO_UNCOMPRESS_GZIP_FILE, gzipFile.path, err.Error())
			}
		}
	}
	return uncompressedBytes, err
}
