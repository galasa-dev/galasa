/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package props

import (
	"testing"

	"github.com/galasa-dev/cli/pkg/files"
	"github.com/stretchr/testify/assert"
)

func TestCanCreateAPropsFileAndReadItBack(t *testing.T) {
	var props map[string]interface{} = make(map[string]interface{})
	props["a"] = "b"
	props["d"] = "e"

	fs := files.NewMockFileSystem()

	WritePropertiesFile(fs, "myPropsFile.properties", props)

	propsGotBack, err := ReadPropertiesFile(fs, "myPropsFile.properties")

	assert.Nil(t, err)
	assert.Contains(t, propsGotBack, "a")
	assert.Contains(t, propsGotBack, "d")
	assert.Equal(t, propsGotBack["a"], "b")
	assert.Equal(t, propsGotBack["d"], "e")
}

func TestCanCreateReadAPropsFileBackWithCommentsIgnored(t *testing.T) {

	fs := files.NewMockFileSystem()

	text := `

# A comment line.

a = b


= Invalid. should be ignored.
	`
	fs.WriteTextFile("myPropsFile.properties", text)

	propsGotBack, err := ReadPropertiesFile(fs, "myPropsFile.properties")

	assert.Nil(t, err)
	assert.Contains(t, propsGotBack, "a")
	assert.Equal(t, propsGotBack["a"], "b")
}
