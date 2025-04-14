/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package resources

import (
  "testing"

  "github.com/galasa-dev/cli/pkg/files"
  "github.com/stretchr/testify/assert"
)

var (
  validResourcesYamlFileContentSingleProperty = `apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: filling
  namespace: doughnuts
data:
  value: custard`

  validResourcesYamlFileContentMultipleProperties = `apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: filling
  namespace: doughnuts
data:
  value: custard
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: filling2
  namespace: doughnuts2
data:
  value: custard2
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: filling3
  namespace: doughnuts3
data:
  value: custard3`

  invalidResourcesYamlFileContentSingleProperty = `apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
     name: filling
    namespace: doughnuts
data:
    value: custard
`

  invalidResourcesYamlFileContentMultipleProperties = `  apiVersion: galasa-dev/v1alpha1
  kind: GalasaProperty
  metadata:
    name: PropertyName
    namespace: myNamespace
  data:
    value: propertyValue
---
apiVersion: galasa-dev/v1alpha1
 kind: GalasaProperty
metadata:
  name: PropertyName1
  namespace: myNamespace1
data:
  value: propertyValue1
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: PropertyName2
  namespace: myNamespace2
data:
  value: propertyValue2`
)

func TestValidFileYamlPathReturnsOk(t *testing.T) {
  //Given
  fileSystem := files.NewOverridableMockFileSystem()
  fileName := "validFilePath.yaml"
  fileSystem.WriteTextFile(fileName, validResourcesYamlFileContentSingleProperty)

  //When
  err := validateFilePathExists(fileSystem, fileName)

  //Then
  assert.Nil(t, err)
}

func TestValidFileYmlPathReturnsOk(t *testing.T) {
  //Given
  fileSystem := files.NewOverridableMockFileSystem()
  fileName := "validFilePath.yml"
  fileSystem.WriteTextFile(fileName, validResourcesYamlFileContentSingleProperty)

  //When
  err := validateFilePathExists(fileSystem, fileName)

  //Then
  assert.Nil(t, err)
}

func TestInvalidFilePathYamlReturnsError(t *testing.T) {
  //Given
  fileSystem := files.NewOverridableMockFileSystem()
  fileName := "invalidFilePath.yaml"

  //When
  err := validateFilePathExists(fileSystem, fileName)

  //Then
  assert.NotNil(t, err)
  assert.Contains(t, err.Error(), "GAL1109E")
  assert.Contains(t, err.Error(), "no such file or directory")
}

func TestInvalidFilePathYmlReturnsError(t *testing.T) {
  //Given
  fileSystem := files.NewOverridableMockFileSystem()
  fileName := "invalidFilePath.yml"

  //When
  err := validateFilePathExists(fileSystem, fileName)

  //Then
  assert.NotNil(t, err)
  assert.Contains(t, err.Error(), "GAL1109E")
  assert.Contains(t, err.Error(), "no such file or directory")
}

func TestValidFilePathInvalidFileTypeReturnsError(t *testing.T) {
  //Given
  fileSystem := files.NewOverridableMockFileSystem()
  fileName := "invalidFileType.js"
  fileSystem.WriteTextFile(fileName, validResourcesYamlFileContentSingleProperty)

  //When
  err := validateFilePathExists(fileSystem, fileName)

  //Then
  assert.NotNil(t, err)
  assert.Contains(t, err.Error(), "GAL1109E")
  assert.Contains(t, err.Error(), "not a yaml file")
}

func TestGetYamlFileContentReturnsOk(t *testing.T) {
  //Given
  fileSystem := files.NewOverridableMockFileSystem()
  fileName := "validFilePath.yaml"
  fileSystem.WriteTextFile(fileName, validResourcesYamlFileContentMultipleProperties)

  //When
  fileContent, err := getYamlFileContent(fileSystem, fileName)

  //Then
  assert.Nil(t, err)
  assert.NotNil(t, fileContent)
  assert.Equal(t, validResourcesYamlFileContentMultipleProperties, string(fileContent))
}

func TestGetYmlFileContentReturnsOk(t *testing.T) {
  //Given
  fileSystem := files.NewOverridableMockFileSystem()
  fileName := "validFilePath.yaml"
  fileSystem.WriteTextFile(fileName, validResourcesYamlFileContentMultipleProperties)

  //When
  fileContent, err := getYamlFileContent(fileSystem, fileName)

  //Then
  assert.Nil(t, err)
  assert.NotNil(t, fileContent)
  assert.Equal(t, validResourcesYamlFileContentMultipleProperties, string(fileContent))
}

func TestGetYamlFileContentFromNonExistentPathReturnsError(t *testing.T) {
  //Given
  fileSystem := files.NewOverridableMockFileSystem()
  fileName := "invalidFilePath.yaml"

  //When
  fileContent, err := getYamlFileContent(fileSystem, fileName)

  //Then
  assert.NotNil(t, err)
  assert.Contains(t, err.Error(), "GAL1110E")
  assert.Equal(t, "", fileContent)
}

func TestSplitByRegexSeparatorSimpleCase(t *testing.T) {
  //When
  result := splitByRegexSeparator("a-b", "-")

  //Then
  assert.Equal(t, 2, len(result))
  assert.Equal(t, "a", result[0])
  assert.Equal(t, "b", result[1])
}

func TestSplitYamlWithSimpleSeparator(t *testing.T) {
  //When
  result := splitYamlIntoParts("a\n---\nb")

  //Then
  assert.Equal(t, 2, len(result))
  assert.Equal(t, "a", result[0])
  assert.Equal(t, "b", result[1])
}

func TestSplitYamlLongSeparator(t *testing.T) {
  //When
  result := splitYamlIntoParts("a\n---------\nb")

  //Then
  assert.Equal(t, 2, len(result))
  assert.Equal(t, "a", result[0])
  assert.Equal(t, "b", result[1])
}

func TestCanDynamicallyParseJsonSingleItemInvalidYamlReturnsError(t *testing.T) {
  //Given
  inputYaml := invalidResourcesYamlFileContentSingleProperty
  action := "apply"

  //When
  _, err := yamlToByteArray(inputYaml, action)

  //Then
  assert.NotNil(t, err)
  assert.Contains(t, err.Error(), "GAL1111E:")
}

func TestCanDynamicallyParseJsonMultipleItemsInvalidYamlReturnsError(t *testing.T) {
  //Given
  inputYaml := invalidResourcesYamlFileContentMultipleProperties
  action := "apply"

  //When
  _, err := yamlToByteArray(inputYaml, action)

  //Then
  assert.NotNil(t, err)
  assert.Contains(t, err.Error(), "GAL1111E:")
}

func TestCanDynamicallyParseJsonSingleItemReturnsOk(t *testing.T) {
  //Given
  inputYaml := validResourcesYamlFileContentSingleProperty
  action := "apply"

  jsonStr := `{
    "action": "apply",
    "data": [
        {
            "apiVersion": "galasa-dev/v1alpha1",
            "data": {
                "value": "custard"
            },
            "kind": "GalasaProperty",
            "metadata": {
                "name": "filling",
                "namespace": "doughnuts"
            }
        }
    ]
}`

  //When
  jsonBytes, err := yamlToByteArray(inputYaml, action)

  //Then
  assert.Nil(t, err, "Failed when it should have worked. err: %v", err)
  assert.Equal(t, string(jsonBytes), jsonStr)
}

func TestCanDynamicallyParseJsonMultipleItemsReturnsOk(t *testing.T) {
  //Given
  inputYaml := validResourcesYamlFileContentMultipleProperties
  action := "create"

  jsonStr := `{
    "action": "create",
    "data": [
        {
            "apiVersion": "galasa-dev/v1alpha1",
            "data": {
                "value": "custard"
            },
            "kind": "GalasaProperty",
            "metadata": {
                "name": "filling",
                "namespace": "doughnuts"
            }
        },
        {
            "apiVersion": "galasa-dev/v1alpha1",
            "data": {
                "value": "custard2"
            },
            "kind": "GalasaProperty",
            "metadata": {
                "name": "filling2",
                "namespace": "doughnuts2"
            }
        },
        {
            "apiVersion": "galasa-dev/v1alpha1",
            "data": {
                "value": "custard3"
            },
            "kind": "GalasaProperty",
            "metadata": {
                "name": "filling3",
                "namespace": "doughnuts3"
            }
        }
    ]
}`

  //When
  jsonBytes, err := yamlToByteArray(inputYaml, action)

  //Then
  assert.Nil(t, err, "Failed when it should have worked. err: %v", err)
  assert.Equal(t, string(jsonBytes), jsonStr)
}

func TestCanDynamicallyParseJsonTwoItemsLongSeparatorReturnsOk(t *testing.T) {
  //Given
  inputYaml := `apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: filling
  namespace: doughnuts
data:
  value: custard
------------------------------
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: filling2
  namespace: doughnuts2
data:
  value: custard2
`
  action := "update"

  jsonStr := `{
    "action": "update",
    "data": [
        {
            "apiVersion": "galasa-dev/v1alpha1",
            "data": {
                "value": "custard"
            },
            "kind": "GalasaProperty",
            "metadata": {
                "name": "filling",
                "namespace": "doughnuts"
            }
        },
        {
            "apiVersion": "galasa-dev/v1alpha1",
            "data": {
                "value": "custard2"
            },
            "kind": "GalasaProperty",
            "metadata": {
                "name": "filling2",
                "namespace": "doughnuts2"
            }
        }
    ]
}`
  //When
  jsonBytes, err := yamlToByteArray(inputYaml, action)

  //Then
  assert.Nil(t, err, "Failed when it should have worked. err: %v", err)
  assert.Equal(t, string(jsonBytes), jsonStr)
}

func TestCanDynamicallyParseJsonSingleItemWithAList(t *testing.T) {
  //Given
  inputYaml := `apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  - name: filling
  - namespace: doughnuts
data:
  value: custard
`
  action := "update"

  jsonStr := `{
    "action": "update",
    "data": [
        {
            "apiVersion": "galasa-dev/v1alpha1",
            "data": {
                "value": "custard"
            },
            "kind": "GalasaProperty",
            "metadata": [
                {
                    "name": "filling"
                },
                {
                    "namespace": "doughnuts"
                }
            ]
        }
    ]
}`
  //When
  jsonBytes, err := yamlToByteArray(inputYaml, action)

  //Then
  assert.Nil(t, err, "Failed when it should have worked. err: %v", err)
  assert.Equal(t, string(jsonBytes), jsonStr)
}

func TestCanDynamicallyParseJsonTwoItemsLongWithNullStartReturnsOk(t *testing.T) {
  //Given
  inputYaml := `
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: filling
  namespace: doughnuts
data:
  value: custard
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: filling2
  namespace: doughnuts2
data:
 value: custard2`

  action :="apply"
  jsonStr := `{
    "action": "apply",
    "data": [
        {
            "apiVersion": "galasa-dev/v1alpha1",
            "data": {
                "value": "custard"
            },
            "kind": "GalasaProperty",
            "metadata": {
                "name": "filling",
                "namespace": "doughnuts"
            }
        },
        {
            "apiVersion": "galasa-dev/v1alpha1",
            "data": {
                "value": "custard2"
            },
            "kind": "GalasaProperty",
            "metadata": {
                "name": "filling2",
                "namespace": "doughnuts2"
            }
        }
    ]
}`

  //When
  jsonBytes, err := yamlToByteArray(inputYaml, action)

  //Then
  assert.Nil(t, err, "Failed when it should have worked. err: %v", err)
  assert.Equal(t, string(jsonBytes), jsonStr)
}