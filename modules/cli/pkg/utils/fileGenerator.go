/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"bytes"
	"log"
	"text/template"

	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/spi"
)

//---------------------------------------------------------------------------------------------------
// File generation types
//---------------------------------------------------------------------------------------------------

type GeneratedFileDef struct {
	FileType                 string
	TargetFilePath           string
	EmbeddedTemplateFilePath string
	TemplateParameters       interface{}
}

type FileGenerator struct {
	fileSystem         spi.FileSystem
	embeddedFileSystem embedded.ReadOnlyFileSystem
}

//-------------------------------------------------------------------------------------------------
// Public functions.
//-------------------------------------------------------------------------------------------------

func NewFileGenerator(fileSystem spi.FileSystem, embeddedFileSystem embedded.ReadOnlyFileSystem) *FileGenerator {
	fileGenerator := &FileGenerator{fileSystem: fileSystem, embeddedFileSystem: embeddedFileSystem}
	return fileGenerator
}

func (generator *FileGenerator) CreateFolder(targetFolderPath string) error {
	err := generator.fileSystem.MkdirAll(targetFolderPath)
	return err
}

// createFile creates a file on the file system.
// If forceOverwrite is false, and there is already a file there, then an error will occur.
func (generator *FileGenerator) CreateFile(
	generatedFile GeneratedFileDef,
	forceOverwrite bool,
	isExistsAnError bool) error {

	log.Printf("Creating file of type %s at %s\n", generatedFile.FileType, generatedFile.TargetFilePath)

	isAllowed, err := generator.checkAllowedToWrite(generatedFile.TargetFilePath, forceOverwrite, isExistsAnError)
	if err == nil {

		if isAllowed {
			var isBinary bool = false
			switch generatedFile.FileType {
			case "jar":
				isBinary = true
			}

			if isBinary {
				err = generator.createBinaryFile(generatedFile)
			} else {
				err = generator.createTextFile(generatedFile)
			}

			if err == nil {
				log.Printf("Created file %s OK.", generatedFile.TargetFilePath)
			}
		}
	}

	return err
}

//-------------------------------------------------------------------------------------------------------------
// Internal logic
//-------------------------------------------------------------------------------------------------------------

func (generator *FileGenerator) createBinaryFile(generatedFileDef GeneratedFileDef) error {
	data, err := generator.embeddedFileSystem.ReadFile(generatedFileDef.EmbeddedTemplateFilePath)
	if err == nil {
		err = generator.fileSystem.WriteBinaryFile(generatedFileDef.TargetFilePath, data)
	}
	return err
}

func (generator *FileGenerator) createTextFile(generatedFile GeneratedFileDef) error {
	template, err := generator.loadEmbeddedTemplate(generatedFile.EmbeddedTemplateFilePath)
	if err == nil {
		var fileContents string
		fileContents, err = generator.substituteParametersIntoTemplate(template, generatedFile.TemplateParameters)
		if err == nil {
			// Write it out to the target file.
			err = generator.fileSystem.WriteTextFile(generatedFile.TargetFilePath, fileContents)
		}
	}
	return err
}

// checkAllowedToWrite - Checks to see if we are allowed to write a file.
// The file may exist already. If it does, then we won't be able to over-write it unless
// the forceOverWrite flag is true.
func (generator *FileGenerator) checkAllowedToWrite(targetFilePath string, forceOverwrite bool, isExistsAnError bool) (bool, error) {
	isAllowed := true
	isAlreadyExists, err := generator.fileSystem.Exists(targetFilePath)
	if err == nil {
		if isAlreadyExists && (!forceOverwrite) {
			isAllowed = false
			if isExistsAnError {
				log.Printf("File %s exists, and we cannot over-write it as the --force flag is not set.", targetFilePath)
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_CANNOT_OVERWRITE_FILE, targetFilePath)
			} else {
				log.Printf("File %s exists, no need to create it then.", targetFilePath)
			}
		}
	}
	return isAllowed, err
}

type BlankTemplateParameters struct{}

// substituteParametersIntoTemplate renders the golang template into a string
func (generator *FileGenerator) substituteParametersIntoTemplate(template *template.Template, templateParameters interface{}) (string, error) {
	var buffer bytes.Buffer
	fileContents := ""

	if templateParameters == nil {
		// Template substitution blows up if there are no template
		// parameters. So create a blank set of template parameters
		// to keep the substitution engine happy.
		templateParameters = BlankTemplateParameters{}
	}

	err := template.Execute(&buffer, templateParameters)
	if err == nil {
		fileContents = buffer.String()
	}
	return fileContents, err
}

func (generator *FileGenerator) loadEmbeddedTemplate(embeddedTemplateFilePath string) (*template.Template, error) {
	// Load-up the template file from the embedded file system.
	data, err := generator.embeddedFileSystem.ReadFile(embeddedTemplateFilePath)
	var templ *template.Template = nil
	if err == nil {
		// Parse the string data into a golang template
		rawTemplate := template.New(embeddedTemplateFilePath)
		templ, err = rawTemplate.Parse(string(data))
	}
	return templ, err
}
