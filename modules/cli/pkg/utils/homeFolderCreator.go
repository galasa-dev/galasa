/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"github.com/galasa-dev/cli/pkg/embedded"
	"github.com/galasa-dev/cli/pkg/spi"
)

func InitialiseGalasaHomeFolder(home spi.GalasaHome, fileSystem spi.FileSystem, embeddedFileSystem embedded.ReadOnlyFileSystem) error {

	var err error

	fileGenerator := NewFileGenerator(fileSystem, embeddedFileSystem)

	galasaHomeDir := home.GetNativeFolderPath()
	err = fileGenerator.CreateFolder(galasaHomeDir)

	if err == nil {
		err = createLibDirAndContent(fileGenerator, galasaHomeDir+fileSystem.GetFilePathSeparator()+"lib")
	}

	if err == nil {
		err = createBootstrapPropertiesFile(fileGenerator, galasaHomeDir)
	}

	if err == nil {
		err = createOverridesPropertiesFile(fileGenerator, galasaHomeDir)
	}

	if err == nil {
		err = createCPSPropertiesFile(fileGenerator, galasaHomeDir)
	}

	if err == nil {
		err = createDSSPropertiesFile(fileGenerator, galasaHomeDir)
	}

	if err == nil {
		err = createCredentialsPropertiesFile(fileGenerator, galasaHomeDir)
	}

	if err == nil {
		err = createGalasactlPropertiesFile(fileGenerator, galasaHomeDir)
	}

	return err
}

func createBootstrapPropertiesFile(fileGenerator *FileGenerator, galasaHomeDir string) error {

	targetPath := galasaHomeDir + fileGenerator.fileSystem.GetFilePathSeparator() + "bootstrap.properties"

	propertyFile := GeneratedFileDef{
		FileType:                 "properties",
		TargetFilePath:           targetPath,
		EmbeddedTemplateFilePath: "templates/galasahome/bootstrap.properties",
		TemplateParameters:       nil,
	}

	err := fileGenerator.CreateFile(propertyFile, false, false)

	return err
}

func createCPSPropertiesFile(fileGenerator *FileGenerator, galasaHomeDir string) error {

	targetPath := galasaHomeDir + fileGenerator.fileSystem.GetFilePathSeparator() + "cps.properties"

	propertyFile := GeneratedFileDef{
		FileType:                 "properties",
		TargetFilePath:           targetPath,
		EmbeddedTemplateFilePath: "templates/galasahome/cps.properties",
		TemplateParameters:       nil,
	}

	err := fileGenerator.CreateFile(propertyFile, false, false)

	return err
}

func createDSSPropertiesFile(fileGenerator *FileGenerator, galasaHomeDir string) error {

	targetPath := galasaHomeDir + fileGenerator.fileSystem.GetFilePathSeparator() + "dss.properties"

	propertyFile := GeneratedFileDef{
		FileType:                 "properties",
		TargetFilePath:           targetPath,
		EmbeddedTemplateFilePath: "templates/galasahome/dss.properties",
		TemplateParameters:       nil,
	}

	err := fileGenerator.CreateFile(propertyFile, false, false)

	return err
}

func createCredentialsPropertiesFile(fileGenerator *FileGenerator, galasaHomeDir string) error {

	targetPath := galasaHomeDir + fileGenerator.fileSystem.GetFilePathSeparator() + "credentials.properties"

	propertyFile := GeneratedFileDef{
		FileType:                 "properties",
		TargetFilePath:           targetPath,
		EmbeddedTemplateFilePath: "templates/galasahome/credentials.properties",
		TemplateParameters:       nil,
	}

	err := fileGenerator.CreateFile(propertyFile, false, false)

	return err
}

func createOverridesPropertiesFile(fileGenerator *FileGenerator, galasaHomeDir string) error {

	targetPath := galasaHomeDir + fileGenerator.fileSystem.GetFilePathSeparator() + "overrides.properties"

	propertyFile := GeneratedFileDef{
		FileType:                 "properties",
		TargetFilePath:           targetPath,
		EmbeddedTemplateFilePath: "templates/galasahome/overrides.properties",
		TemplateParameters:       nil,
	}

	err := fileGenerator.CreateFile(
		propertyFile,
		false, // Don't force overwrite
		false) // Don't error if the file already exists.

	return err
}

func createGalasactlPropertiesFile(fileGenerator *FileGenerator, galasaHomeDir string) error {

	targetPath := galasaHomeDir + fileGenerator.fileSystem.GetFilePathSeparator() + "galasactl.properties"

	propertyFile := GeneratedFileDef{
		FileType:                 "properties",
		TargetFilePath:           targetPath,
		EmbeddedTemplateFilePath: "templates/galasahome/galasactl.properties",
		TemplateParameters:       nil,
	}

	err := fileGenerator.CreateFile(
		propertyFile,
		false, // Don't force overwrite
		false) // Don't error if the file already exists.

	return err
}

func createLibDirAndContent(fileGenerator *FileGenerator, galasaLibDir string) error {

	err := fileGenerator.CreateFolder(galasaLibDir)

	if err == nil {
		var galasaVersion string
		galasaVersion, err = embedded.GetGalasaVersion()
		if err == nil {
			galasaVersionLibDir := galasaLibDir + fileGenerator.fileSystem.GetFilePathSeparator() + galasaVersion
			err = fileGenerator.CreateFolder(galasaVersionLibDir)

			if err == nil {
				var bootJarVersion string
				bootJarVersion, err = embedded.GetBootJarVersion()

				if err == nil {
					installedBootJar := GeneratedFileDef{
						FileType: "jar",
						TargetFilePath: galasaVersionLibDir + fileGenerator.fileSystem.GetFilePathSeparator() +
							"galasa-boot-" + bootJarVersion + ".jar",
						EmbeddedTemplateFilePath: "templates/galasahome/lib/galasa-boot-" + bootJarVersion + ".jar",
						TemplateParameters:       nil,
					}

					err = fileGenerator.CreateFile(
						installedBootJar,
						false, // don't force overwrite
						false) // don't error if it already exists.
				}
			}
		}
	}

	return err
}

func GetGalasaBootJarPath(fs spi.FileSystem, home spi.GalasaHome) (string, error) {
	var galasaBootJarPath string = ""
	var err error
	var galasaVersion string
	var galasaHomePath = home.GetNativeFolderPath()

	galasaVersion, err = embedded.GetGalasaVersion()

	if err == nil {
		var bootJarVersion string
		bootJarVersion, err = embedded.GetBootJarVersion()

		if err == nil {
			separator := fs.GetFilePathSeparator()

			galasaBootJarPath = galasaHomePath +
				separator + "lib" +
				separator + galasaVersion +
				separator + "galasa-boot-" + bootJarVersion + ".jar"
		}
	}

	return galasaBootJarPath, err
}
