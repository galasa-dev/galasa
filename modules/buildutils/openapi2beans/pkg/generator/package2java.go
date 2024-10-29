/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package generator

import (
	"log"

	"github.com/cbroglie/mustache"
	"github.com/dev-galasa/buildutils/openapi2beans/pkg/embedded"
	"github.com/galasa-dev/cli/pkg/files"
)

func convertJavaPackageToJavaFiles(javaPackage *JavaPackage, fs files.FileSystem, storeFilepath string) {
	log.Print("convertJavaPackageToJavaFiles: Creating classes")
	classTemplate, err := embedded.GetJavaTemplate(embedded.GET_JAVA_TEMPLATE_CLASS_OPTION)
	if err == nil {
		for _, javaClass := range javaPackage.Classes {
			createJavaClassFile(javaClass, fs, classTemplate, storeFilepath)
		}
		log.Print("convertJavaPackageToJavaFiles: Creating enums")
		enumTemplate, err := embedded.GetJavaTemplate(embedded.GET_JAVA_TEMPLATE_ENUM_OPTION)
		if err == nil {
			for _, javaEnum := range javaPackage.Enums {
				createJavaEnumFile(javaEnum, fs, enumTemplate, storeFilepath)
			}
		}
	}
}

// TODO: Make errors output from these functions fatal
// plugs a java class into a moustache template and saves the resulting string in a file
func createJavaClassFile(javaClass *JavaClass, fs files.FileSystem, javaClassTemplate *mustache.Template, storeFilepath string) error {
	log.Println("Creating class: " + javaClass.Name + ".java")
	generatedBeanFileContents, err := javaClassTemplate.Render(javaClass)
	if err == nil {
		err = fs.WriteTextFile(storeFilepath+fs.GetFilePathSeparator()+javaClass.Name+".java", generatedBeanFileContents)
		if err == nil {
			log.Print("Successfully created class: " + javaClass.Name + ".java")
		} else {
			log.Printf("Failed to create: %s. Reason is: %s", javaClass.Name+".java", err.Error())
		}
	} else {
		log.Printf("Failed to render file from mustache template. Reason is: %s", err.Error())
	}
	return err
}

func createJavaEnumFile(javaEnum *JavaEnum, fs files.FileSystem, javaEnumTemplate *mustache.Template, storeFilepath string) error {
	log.Println("Creating enum: " + javaEnum.Name + ".java")
	generatedBeanFileContents, err := javaEnumTemplate.Render(javaEnum)
	if err == nil {
		err = fs.WriteTextFile(storeFilepath+fs.GetFilePathSeparator()+javaEnum.Name+".java", generatedBeanFileContents)
		if err == nil {
			log.Print("Successfully created enum: " + javaEnum.Name + ".java")
		} else {
			log.Printf("Failed to create: %s. Reason is: %s", javaEnum.Name+".java", err.Error())
		}
	} else {
		log.Printf("Failed to render file from mustache template. Reason is: %s", err.Error())
	}
	return err
}
