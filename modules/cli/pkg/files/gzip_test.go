/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package files

import (
	"encoding/json"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestCanRoundTripDataViaAGzipFile(t *testing.T) {
	fs := NewMockFileSystem()

	testString := "hello world"
	testGzipFilePath := "mydata.gz"
	testStringBytes := []byte(testString)

	gzipFileOut := NewGzipFile(fs, testGzipFilePath)
	err := gzipFileOut.WriteBytes(testStringBytes)

	assert.Nil(t, err, "Should not have got an error!")

	// Then it should exist.
	if err == nil {
		var isExists bool
		isExists, err = fs.Exists(testGzipFilePath)
		assert.True(t, isExists)

		if isExists {

			// We should be able to read back the binary data
			gzipFileIn := NewGzipFile(fs, testGzipFilePath)
			var bytesGotBack []byte
			bytesGotBack, err = gzipFileIn.ReadBytes()

			assert.Nil(t, err, "Should not have got an error")
			if err == nil {

				testStringGotBack := string(bytesGotBack)
				assert.Equal(t, testString, testStringGotBack)
			}
		}
	}

}

func TestCanReadGzFile(t *testing.T) {
	fs := NewOSFileSystem()
	testGzipFilePath := "./testdata/term1-00001.gz"
	gzipFile := NewGzipFile(fs, testGzipFilePath)
	content, err := gzipFile.ReadBytes()

	assert.Nil(t, err)
	if err == nil {
		contentString := string(content)
		assert.NotEmpty(t, contentString)

		var result map[string]interface{}
		err = json.Unmarshal([]byte(content), &result)

		assert.Nil(t, err)
		if err == nil {
			assert.Equal(t, result["id"], "term1")
		}
	}
}
