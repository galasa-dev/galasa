/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package embedded

import (
	"testing"

	"github.com/cbroglie/mustache"
	"github.com/stretchr/testify/assert"
)

type MockReadOnlyFileSystem struct {
	files map[string]string
}

func NewMockReadOnlyFileSystem() *MockReadOnlyFileSystem {
	fs := MockReadOnlyFileSystem{
		files: make(map[string]string, 0),
	}
	return &fs
}

// WriteFile - This function is not on the ReadOnlyFileSystem interface, but does allow unit tests
// to add data files to the mock file system, so the code under test can read it back.
func (fs *MockReadOnlyFileSystem) WriteFile(filepath string, content string) {
	fs.files[filepath] = content
}

func (fs *MockReadOnlyFileSystem) ReadFile(filepath string) ([]byte, error) {
	content := fs.files[filepath]
	return []byte(content), nil
}

func TestGetJavaTemplateWithClassOptionReturnsTemplate(t *testing.T) {
	// Given...
	var (
		err      error
		template *mustache.Template
		rendered string
	)

	// When...
	template, err = GetJavaTemplate(GET_JAVA_TEMPLATE_CLASS_OPTION)

	// Then...
	assert.Nil(t, err)
	assert.NotNil(t, template)
	rendered, err = template.Render("")
	assert.Nil(t, err)
	assert.Contains(t, rendered, "package")
	assert.Contains(t, rendered, "public class")
}

func TestGetJavaTemplateWithEnumOptionReturnsTemplate(t *testing.T) {
	// Given...
	var (
		err      error
		template *mustache.Template
		rendered string
	)

	// When...
	template, err = GetJavaTemplate(GET_JAVA_TEMPLATE_ENUM_OPTION)

	// Then...
	assert.Nil(t, err)
	assert.NotNil(t, template)
	rendered, err = template.Render("")
	assert.Nil(t, err)
	assert.Contains(t, rendered, "package")
	assert.Contains(t, rendered, "public enum")
}

func TestCanParseTemplatesFromEmbeddedFS(t *testing.T) {
	// Given...
	fs := NewMockReadOnlyFileSystem()
	javaClassTemplateContent := "class template"
	fs.WriteFile(JAVA_CLASS_TEMPLATE_FILEPATH, javaClassTemplateContent)
	javaEnumTemplateContent := "enum content"
	fs.WriteFile(JAVA_ENUM_TEMPLATE_FILEPATH, javaEnumTemplateContent)

	// When...
	templates, err := readTemplatesFromEmbeddedFiles(fs, nil)

	// Then...
	assert.Nil(t, err)
	assert.NotNil(t, templates)
	var renderResult string
	// class
	assert.NotNil(t, templates.JavaClassTemplate)
	renderResult, err = templates.JavaClassTemplate.Render("")
	assert.Nil(t, err)
	assert.Equal(t, javaClassTemplateContent, renderResult)
	// enum
	assert.NotNil(t, templates.JavaEnumTemplate)
	renderResult, err = templates.JavaEnumTemplate.Render("")
	assert.Nil(t, err)
	assert.Equal(t, javaEnumTemplateContent, renderResult)
}

func TestDoesntRereadTemplatesWhenTemplatesAlreadyKnown(t *testing.T) {
	// Given...
	fs := NewMockReadOnlyFileSystem()
	javaClassTemplateContent := "class template"
	fs.WriteFile(JAVA_CLASS_TEMPLATE_FILEPATH, javaClassTemplateContent)
	javaEnumTemplateContent := "enum content"
	fs.WriteFile(JAVA_ENUM_TEMPLATE_FILEPATH, javaEnumTemplateContent)

	// When...
	expectedClassString := "expected class string"
	expectedClassTemplate, err := mustache.ParseString(expectedClassString)
	assert.Nil(t, err)
	expectedEnumString := "expected enum string"
	expectedEnumTemplate, err := mustache.ParseString(expectedEnumString)
	assert.Nil(t, err)

	alreadyKnownTemplates := templates{
		JavaClassTemplate: expectedClassTemplate,
		JavaEnumTemplate:  expectedEnumTemplate,
	}

	templates, err := readTemplatesFromEmbeddedFiles(fs, &alreadyKnownTemplates)

	// Then...
	assert.Nil(t, err)
	assert.NotNil(t, templates)
	var renderResult string
	// class
	assert.NotNil(t, templates.JavaClassTemplate)
	renderResult, err = templates.JavaClassTemplate.Render("")
	assert.Nil(t, err)
	assert.Equal(t, expectedClassString, renderResult)
	// enum
	assert.NotNil(t, templates.JavaEnumTemplate)
	renderResult, err = templates.JavaEnumTemplate.Render("")
	assert.Nil(t, err)
	assert.Equal(t, expectedEnumString, renderResult)
}
