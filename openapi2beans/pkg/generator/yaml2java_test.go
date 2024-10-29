/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package generator

import (
	"fmt"
	"testing"

	"github.com/galasa-dev/cli/pkg/files"
	"github.com/stretchr/testify/assert"
)

func TestGenerateFilesProducesFileFromSingleGenericObjectSchema(t *testing.T) {
	// Given...
	packageName := "generated"
	mockFileSystem := files.NewMockFileSystem()
	projectFilepath := "dev/wyvinar"
	apiFilePath := "test-resources/single-bean.yaml"
	objectName := "MyBeanName"
	generatedCodeFilePath := "dev/wyvinar/generated/MyBeanName.java"
	testapiyaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
`
	mockFileSystem.WriteTextFile(apiFilePath, testapiyaml)

	// When...
	err := GenerateFiles(mockFileSystem, projectFilepath, apiFilePath, packageName, true)

	// Then...
	assert.Nil(t, err)
	generatedClassFile := openGeneratedFile(t, mockFileSystem, generatedCodeFilePath)
	assertClassFileGeneratedOk(t, generatedClassFile, objectName)
}

func TestGenerateFilesProducesCorrectClassDescription(t *testing.T) {
	// Given...
	packageName := "generated"
	mockFileSystem := files.NewMockFileSystem()
	projectFilepath := "dev/wyvinar"
	apiFilePath := "test-resources/single-bean.yaml"
	objectName := "MyBeanName"
	generatedCodeFilePath := "dev/wyvinar/generated/MyBeanName.java"
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      description: A simple example
`
	mockFileSystem.WriteTextFile(apiFilePath, apiYaml)

	// When...
	err := GenerateFiles(mockFileSystem, projectFilepath, apiFilePath, packageName, true)

	// Then...
	assert.Nil(t, err)
	generatedClassFile := openGeneratedFile(t, mockFileSystem, generatedCodeFilePath)
	assertClassFileGeneratedOk(t, generatedClassFile, objectName)
	assert.Contains(t, generatedClassFile, "// A simple example")
}

func TestGenerateFilesProducesCorrectVariableCode(t *testing.T) {
	// Given...
	packageName := "generated"
	mockFileSystem := files.NewMockFileSystem()
	projectFilepath := "dev/wyvinar"
	apiFilePath := "test-resources/single-bean.yaml"
	objectName := "MyBeanName"
	generatedCodeFilePath := "dev/wyvinar/generated/MyBeanName.java"
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      description: A simple example
      properties:
        myStringVar:
          type: string
`
	// When...
	mockFileSystem.WriteTextFile(apiFilePath, apiYaml)

	// When...
	err := GenerateFiles(mockFileSystem, projectFilepath, apiFilePath, packageName, true)

	// Then...
	assert.Nil(t, err)
	generatedClassFile := openGeneratedFile(t, mockFileSystem, generatedCodeFilePath)
	assertClassFileGeneratedOk(t, generatedClassFile, objectName)
	getter := `public String getMyStringVar() {
        return this.myStringVar;
    }`
	setter := `public void setMyStringVar(String myStringVar) {
        this.myStringVar = myStringVar;
    }`
	varCreation := `private String myStringVar;`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
}

func TestGenerateFilesProducesCorrectVariableDescription(t *testing.T) {
	// Given...
	packageName := "generated"
	mockFileSystem := files.NewMockFileSystem()
	projectFilepath := "dev/wyvinar"
	apiFilePath := "test-resources/single-bean.yaml"
	objectName := "MyBeanName"
	generatedCodeFilePath := "dev/wyvinar/generated/MyBeanName.java"
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      description: A simple example
      properties:
        myStringVar:
          type: string
          description: a test string
`
	mockFileSystem.WriteTextFile(apiFilePath, apiYaml)

	// When...
	err := GenerateFiles(mockFileSystem, projectFilepath, apiFilePath, packageName, true)

	// Then...
	assert.Nil(t, err)
	generatedClassFile := openGeneratedFile(t, mockFileSystem, generatedCodeFilePath)
	assertClassFileGeneratedOk(t, generatedClassFile, objectName)
	getter := `public String getMyStringVar() {
        return this.myStringVar;
    }`
	setter := `public void setMyStringVar(String myStringVar) {
        this.myStringVar = myStringVar;
    }`
	varCreation := `// a test string
    private String myStringVar;`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
}

func TestGenerateFilesProducesCorrectSerializedNameOverride(t *testing.T) {
	// Given...
	packageName := "generated"
	mockFileSystem := files.NewMockFileSystem()
	projectFilepath := "dev/wyvinar"
	apiFilePath := "test-resources/single-bean.yaml"
	objectName := "MyBeanName"
	generatedCodeFilePath := "dev/wyvinar/generated/MyBeanName.java"
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      description: A simple example
      properties:
        my_string_var:
          type: string
`
	// When...
	mockFileSystem.WriteTextFile(apiFilePath, apiYaml)

	// When...
	err := GenerateFiles(mockFileSystem, projectFilepath, apiFilePath, packageName, true)

	// Then...
	assert.Nil(t, err)
	generatedClassFile := openGeneratedFile(t, mockFileSystem, generatedCodeFilePath)
	assertClassFileGeneratedOk(t, generatedClassFile, objectName)
	getter := `public String getMyStringVar() {
        return this.myStringVar;
    }`
	setter := `public void setMyStringVar(String myStringVar) {
        this.myStringVar = myStringVar;
    }`
	varSerializedName := `@SerializedName("my_string_var")`
	varCreation := `private String myStringVar;`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
	assert.Contains(t, generatedClassFile, varSerializedName)
	assert.Contains(t, generatedClassFile, "import com.google.gson.annotations.SerializedName;")
}

func TestGenerateFilesDoesntContainSerializedNameWithoutSnakeCaseName(t *testing.T) {
	// Given...
	packageName := "generated"
	mockFileSystem := files.NewMockFileSystem()
	projectFilepath := "dev/wyvinar"
	apiFilePath := "test-resources/single-bean.yaml"
	objectName := "MyBeanName"
	generatedCodeFilePath := "dev/wyvinar/generated/MyBeanName.java"
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      description: A simple example
      properties:
        myStringVar:
          type: string
`
	// When...
	mockFileSystem.WriteTextFile(apiFilePath, apiYaml)

	// When...
	err := GenerateFiles(mockFileSystem, projectFilepath, apiFilePath, packageName, true)

	// Then...
	assert.Nil(t, err)
	generatedClassFile := openGeneratedFile(t, mockFileSystem, generatedCodeFilePath)
	assertClassFileGeneratedOk(t, generatedClassFile, objectName)
	getter := `public String getMyStringVar() {
        return this.myStringVar;
    }`
	setter := `public void setMyStringVar(String myStringVar) {
        this.myStringVar = myStringVar;
    }`
	varCreation := `private String myStringVar;`
	varSerializedName := `@SerializedName("my_string_var")`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
	assert.NotContains(t, generatedClassFile, varSerializedName)
	assert.NotContains(t, generatedClassFile, "import com.google.gson.annotations.SerializedName;")
}

func TestGenerateFilesProducesMultipleVariables(t *testing.T) {
	// Given...
	packageName := "generated"
	mockFileSystem := files.NewMockFileSystem()
	projectFilepath := "dev/wyvinar"
	apiFilePath := "test-resources/single-bean.yaml"
	objectName := "MyBeanName"
	generatedCodeFilePath := "dev/wyvinar/generated/MyBeanName.java"
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      description: A simple example
      properties:
        myStringVar:
          type: string
          description: a test string
        myIntVar:
          type: integer
          description: a test integer
`
	mockFileSystem.WriteTextFile(apiFilePath, apiYaml)

	// When...
	err := GenerateFiles(mockFileSystem, projectFilepath, apiFilePath, packageName, true)

	// Then...
	assert.Nil(t, err)
	generatedClassFile := openGeneratedFile(t, mockFileSystem, generatedCodeFilePath)
	assertClassFileGeneratedOk(t, generatedClassFile, objectName)
	getter := `public String getMyStringVar() {
        return this.myStringVar;
    }`
	setter := `public void setMyStringVar(String myStringVar) {
        this.myStringVar = myStringVar;
    }`
	varCreation := `// a test string
    private String myStringVar;`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
	intGetter := `public String getMyStringVar() {
        return this.myStringVar;
    }`
	intSetter := `public void setMyIntVar(int myIntVar) {
        this.myIntVar = myIntVar;
    }`
	intVarCreation := `// a test integer
    private int myIntVar;`
	assert.Contains(t, generatedClassFile, intGetter)
	assert.Contains(t, generatedClassFile, intSetter)
	assert.Contains(t, generatedClassFile, intVarCreation)
}

func TestGenerateFilesProducesVariablesOfAllPrimitiveTypes(t *testing.T) {
	// Given...
	packageName := "generated"
	mockFileSystem := files.NewMockFileSystem()
	projectFilepath := "dev/wyvinar"
	apiFilePath := "test-resources/single-bean.yaml"
	objectName := "MyBeanName"
	generatedCodeFilePath := "dev/wyvinar/generated/MyBeanName.java"
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      description: A simple example
      properties:
        myStringVar:
          type: string
          description: a test string
        myIntVar:
          type: integer
          description: a test integer
        myBoolVar:
          type: boolean
          description: a test boolean
        myDoubleVar:
          type: number
          description: a test double
`
	mockFileSystem.WriteTextFile(apiFilePath, apiYaml)

	// When...
	err := GenerateFiles(mockFileSystem, projectFilepath, apiFilePath, packageName, true)

	// Then...
	assert.Nil(t, err)
	generatedClassFile := openGeneratedFile(t, mockFileSystem, generatedCodeFilePath)
	assertClassFileGeneratedOk(t, generatedClassFile, objectName)
	getter := `public String getMyStringVar() {
        return this.myStringVar;
    }`
	setter := `public void setMyStringVar(String myStringVar) {
        this.myStringVar = myStringVar;
    }`
	varCreation := `// a test string
    private String myStringVar;`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
	getter = `public int getMyIntVar() {
        return this.myIntVar;
    }`
	setter = `public void setMyIntVar(int myIntVar) {
        this.myIntVar = myIntVar;
    }`
	varCreation = `// a test integer
    private int myIntVar;`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
	getter = `public boolean getMyBoolVar() {
        return this.myBoolVar;
    }`
	setter = `public void setMyBoolVar(boolean myBoolVar) {
        this.myBoolVar = myBoolVar;
    }`
	varCreation = `// a test boolean
    private boolean myBoolVar;`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
	getter = `public double getMyDoubleVar() {
        return this.myDoubleVar;
    }`
	setter = `public void setMyDoubleVar(double myDoubleVar) {
        this.myDoubleVar = myDoubleVar;
    }`
	varCreation = `// a test double
    private double myDoubleVar;`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
}

func TestGenerateFilesProcessesRequiredVariable(t *testing.T) {
	// Given...
	packageName := "generated"
	mockFileSystem := files.NewMockFileSystem()
	projectFilepath := "dev/wyvinar"
	apiFilePath := "test-resources/single-bean.yaml"
	objectName := "MyBeanName"
	generatedCodeFilePath := "dev/wyvinar/generated/MyBeanName.java"
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      description: A simple example
      required: [myStringVar]
      properties:
        myStringVar:
          type: string
          description: a test string
`
	mockFileSystem.WriteTextFile(apiFilePath, apiYaml)

	// When...
	err := GenerateFiles(mockFileSystem, projectFilepath, apiFilePath, packageName, true)

	// Then...
	assert.Nil(t, err)
	generatedClassFile := openGeneratedFile(t, mockFileSystem, generatedCodeFilePath)
	assertClassFileGeneratedOk(t, generatedClassFile, objectName)
	getter := `public String getMyStringVar() {
        return this.myStringVar;
    }`
	setter := `public void setMyStringVar(String myStringVar) {
        this.myStringVar = myStringVar;
    }`
	varCreation := `// a test string
    private String myStringVar;`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
	assert.Contains(t, generatedClassFile, `    public MyBeanName(String myStringVar) {
        this.myStringVar = myStringVar;
    }`)
}

func TestGenerateFilesProducesMultipleRequiredVariables(t *testing.T) {
	// Given...
	packageName := "generated"
	mockFileSystem := files.NewMockFileSystem()
	projectFilepath := "dev/wyvinar"
	apiFilePath := "test-resources/single-bean.yaml"
	objectName := "MyBeanName"
	generatedCodeFilePath := "dev/wyvinar/generated/MyBeanName.java"
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      required: [myStringVar, myIntVar]
      description: A simple example
      properties:
        myStringVar:
          type: string
          description: a test string
        myIntVar:
          type: integer
          description: a test integer
`
	mockFileSystem.WriteTextFile(apiFilePath, apiYaml)

	// When...
	err := GenerateFiles(mockFileSystem, projectFilepath, apiFilePath, packageName, true)

	// Then...
	assert.Nil(t, err)
	generatedClassFile := openGeneratedFile(t, mockFileSystem, generatedCodeFilePath)
	assertClassFileGeneratedOk(t, generatedClassFile, objectName)
	getter := `public String getMyStringVar() {
        return this.myStringVar;
    }`
	setter := `public void setMyStringVar(String myStringVar) {
        this.myStringVar = myStringVar;
    }`
	varCreation := `// a test string
    private String myStringVar;`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
	getter = `public int getMyIntVar() {
        return this.myIntVar;
    }`
	setter = `public void setMyIntVar(int myIntVar) {
        this.myIntVar = myIntVar;
    }`
	varCreation = `// a test integer
    private int myIntVar;`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
	assert.Contains(t, generatedClassFile, `    public MyBeanName(int myIntVar, String myStringVar) {
        this.myIntVar = myIntVar;
        this.myStringVar = myStringVar;
    }`)
}

func TestGenerateFilesProducesMultipleVariablesWithMixedRequiredStatus(t *testing.T) {
	// Given...
	packageName := "generated"
	mockFileSystem := files.NewMockFileSystem()
	projectFilepath := "dev/wyvinar"
	apiFilePath := "test-resources/single-bean.yaml"
	objectName := "MyBeanName"
	generatedCodeFilePath := "dev/wyvinar/generated/MyBeanName.java"
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      required: [myStringVar]
      description: A simple example
      properties:
        myStringVar:
          type: string
          description: a test string
        myIntVar:
          type: integer
          description: a test integer
`
	mockFileSystem.WriteTextFile(apiFilePath, apiYaml)

	// When...
	err := GenerateFiles(mockFileSystem, projectFilepath, apiFilePath, packageName, true)

	// Then...
	assert.Nil(t, err)
	generatedClassFile := openGeneratedFile(t, mockFileSystem, generatedCodeFilePath)
	assertClassFileGeneratedOk(t, generatedClassFile, objectName)
	getter := `public String getMyStringVar() {
        return this.myStringVar;
    }`
	setter := `public void setMyStringVar(String myStringVar) {
        this.myStringVar = myStringVar;
    }`
	varCreation := `// a test string
    private String myStringVar;`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
	getter = `public int getMyIntVar() {
        return this.myIntVar;
    }`
	setter = `public void setMyIntVar(int myIntVar) {
        this.myIntVar = myIntVar;
    }`
	varCreation = `// a test integer
    private int myIntVar;`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
	assert.Contains(t, generatedClassFile, `    public MyBeanName(String myStringVar) {
        this.myStringVar = myStringVar;
    }`)
}

func TestGenerateFilesProducesArray(t *testing.T) {
	// Given...
	packageName := "generated"
	mockFileSystem := files.NewMockFileSystem()
	projectFilepath := "dev/wyvinar"
	apiFilePath := "test-resources/single-bean.yaml"
	objectName := "MyBeanName"
	generatedCodeFilePath := "dev/wyvinar/generated/MyBeanName.java"
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      description: A simple example
      properties:
        myArrayVar:
          type: array
          description: a test array
          items:
            type: string
`
	mockFileSystem.WriteTextFile(apiFilePath, apiYaml)

	// When...
	err := GenerateFiles(mockFileSystem, projectFilepath, apiFilePath, packageName, true)

	// Then...
	assert.Nil(t, err)
	generatedClassFile := openGeneratedFile(t, mockFileSystem, generatedCodeFilePath)
	assertClassFileGeneratedOk(t, generatedClassFile, objectName)
	getter := `public String[] getMyArrayVar() {
        return this.myArrayVar;
    }`
	setter := `public void setMyArrayVar(String[] myArrayVar) {
        this.myArrayVar = myArrayVar;
    }`
	varCreation := `// a test array
    private String[] myArrayVar;`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
}

func TestGenerateFilesProduces2DArrayFromNestedArrayStructure(t *testing.T) {
	// Given...
	packageName := "generated"
	mockFileSystem := files.NewMockFileSystem()
	projectFilepath := "dev/wyvinar"
	apiFilePath := "test-resources/single-bean.yaml"
	objectName := "MyBeanName"
	generatedCodeFilePath := "dev/wyvinar/generated/MyBeanName.java"
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      description: A simple example
      properties:
        myArrayVar:
          type: array
          description: a test 2d array
          items:
            type: array
            items:
              type: string
`
	mockFileSystem.WriteTextFile(apiFilePath, apiYaml)

	// When...
	err := GenerateFiles(mockFileSystem, projectFilepath, apiFilePath, packageName, true)

	// Then...
	assert.Nil(t, err)
	generatedClassFile := openGeneratedFile(t, mockFileSystem, generatedCodeFilePath)
	assertClassFileGeneratedOk(t, generatedClassFile, objectName)
	getter := `public String[][] getMyArrayVar() {
        return this.myArrayVar;
    }`
	setter := `public void setMyArrayVar(String[][] myArrayVar) {
        this.myArrayVar = myArrayVar;
    }`
	varCreation := `// a test 2d array
    private String[][] myArrayVar;`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
}

func TestGenerateFilesProduces3DArrayFromNestedArrayStructure(t *testing.T) {
	// Given...
	packageName := "generated"
	mockFileSystem := files.NewMockFileSystem()
	projectFilepath := "dev/wyvinar"
	apiFilePath := "test-resources/single-bean.yaml"
	objectName := "MyBeanName"
	generatedCodeFilePath := "dev/wyvinar/generated/MyBeanName.java"
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      description: A simple example
      properties:
        myArrayVar:
          type: array
          description: a test 3d array
          items:
            type: array
            items:
              type: array
              items:
                type: string
`
	mockFileSystem.WriteTextFile(apiFilePath, apiYaml)

	// When...
	err := GenerateFiles(mockFileSystem, projectFilepath, apiFilePath, packageName, true)

	// Then...
	assert.Nil(t, err)
	generatedClassFile := openGeneratedFile(t, mockFileSystem, generatedCodeFilePath)
	assertClassFileGeneratedOk(t, generatedClassFile, objectName)
	getter := `public String[][][] getMyArrayVar() {
        return this.myArrayVar;
    }`
	setter := `public void setMyArrayVar(String[][][] myArrayVar) {
        this.myArrayVar = myArrayVar;
    }`
	varCreation := `// a test 3d array
    private String[][][] myArrayVar;`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
}

func TestGenerateFilesProducesMultipleClassFilesFromNestedObject(t *testing.T) {
	// Given...
	packageName := "generated"
	mockFileSystem := files.NewMockFileSystem()
	projectFilepath := "dev/wyvinar"
	apiFilePath := "test-resources/single-bean.yaml"
	objectName := "MyBeanName"
	generatedCodeFilePath := "dev/wyvinar/generated/MyBeanName.java"
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      description: A simple example
      properties:
        myNestedObject:
          type: object
`
	// When...
	mockFileSystem.WriteTextFile(apiFilePath, apiYaml)

	// When...
	err := GenerateFiles(mockFileSystem, projectFilepath, apiFilePath, packageName, true)

	// Then...
	assert.Nil(t, err)
	generatedClassFile := openGeneratedFile(t, mockFileSystem, generatedCodeFilePath)
	assertClassFileGeneratedOk(t, generatedClassFile, objectName)
	getter := `public MyBeanNameMyNestedObject getMyNestedObject() {
        return this.myNestedObject;
    }`
	setter := `public void setMyNestedObject(MyBeanNameMyNestedObject myNestedObject) {
        this.myNestedObject = myNestedObject;
    }`
	varCreation := `private MyBeanNameMyNestedObject myNestedObject;`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
	generatedNestedClassFile := openGeneratedFile(t, mockFileSystem, "dev/wyvinar/generated/MyBeanNameMyNestedObject.java")
	assertClassFileGeneratedOk(t, generatedNestedClassFile, "MyBeanNameMyNestedObject")
}

func TestGenerateFilesProducesClassWithVariableOfTypeReferencedObject(t *testing.T) {
	// Given...
	packageName := "generated"
	mockFileSystem := files.NewMockFileSystem()
	projectFilepath := "dev/wyvinar"
	apiFilePath := "test-resources/single-bean.yaml"
	objectName := "MyBeanName"
	generatedCodeFilePath := "dev/wyvinar/generated/MyBeanName.java"
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      description: A simple example
      properties:
        myReferencingProperty:
          $ref: '#/components/schemas/MyReferencedObject'
    MyReferencedObject:
      type: object
`
	// When...
	mockFileSystem.WriteTextFile(apiFilePath, apiYaml)

	// When...
	err := GenerateFiles(mockFileSystem, projectFilepath, apiFilePath, packageName, true)

	// Then...
	assert.Nil(t, err)
	generatedClassFile := openGeneratedFile(t, mockFileSystem, generatedCodeFilePath)
	assertClassFileGeneratedOk(t, generatedClassFile, objectName)
	getter := `public MyReferencedObject getMyReferencingProperty() {
        return this.myReferencingProperty;
    }`
	setter := `public void setMyReferencingProperty(MyReferencedObject myReferencingProperty) {
        this.myReferencingProperty = myReferencingProperty;
    }`
	varCreation := `private MyReferencedObject myReferencingProperty;`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
	generatedReferencedClassFile := openGeneratedFile(t, mockFileSystem, "dev/wyvinar/generated/MyReferencedObject.java")
	assertClassFileGeneratedOk(t, generatedReferencedClassFile, "MyReferencedObject")
}

func TestGenerateFilesProducesArrayWithReferenceToObject(t *testing.T) {
	// Given...
	packageName := "generated"
	mockFileSystem := files.NewMockFileSystem()
	projectFilepath := "dev/wyvinar"
	apiFilePath := "test-resources/single-bean.yaml"
	objectName := "MyBeanName"
	generatedCodeFilePath := "dev/wyvinar/generated/MyBeanName.java"
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      description: A simple example
      properties:
        myArrayVar:
          type: array
          description: a test array
          items:
            $ref: '#/components/schemas/MyReferencedObject'
    MyReferencedObject:
      type: object
`
	mockFileSystem.WriteTextFile(apiFilePath, apiYaml)

	// When...
	err := GenerateFiles(mockFileSystem, projectFilepath, apiFilePath, packageName, true)

	// Then...
	assert.Nil(t, err)
	generatedClassFile := openGeneratedFile(t, mockFileSystem, generatedCodeFilePath)
	assertClassFileGeneratedOk(t, generatedClassFile, objectName)
	getter := `public MyReferencedObject[] getMyArrayVar() {
        return this.myArrayVar;
    }`
	setter := `public void setMyArrayVar(MyReferencedObject[] myArrayVar) {
        this.myArrayVar = myArrayVar;
    }`
	varCreation := `private MyReferencedObject[] myArrayVar;`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
	generatedReferencedClassFile := openGeneratedFile(t, mockFileSystem, "dev/wyvinar/generated/MyReferencedObject.java")
	assertClassFileGeneratedOk(t, generatedReferencedClassFile, "MyReferencedObject")
}

func TestGenerateFilesProducesEnumAndClass(t *testing.T) {
	// Given...
	packageName := "generated"
	mockFileSystem := files.NewMockFileSystem()
	projectFilepath := "dev/wyvinar"
	apiFilePath := "test-resources/single-bean.yaml"
	objectName := "MyBeanName"
	generatedCodeFilePath := "dev/wyvinar/generated/MyBeanName.java"
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      description: bean with an enum property
      properties:
        myEnum:
          type: string
          description: an enum with 2 values to test against.
          enum: [string1, string2]
`
	// When...
	mockFileSystem.WriteTextFile(apiFilePath, apiYaml)

	// When...
	err := GenerateFiles(mockFileSystem, projectFilepath, apiFilePath, packageName, true)

	// Then...
	assert.Nil(t, err)
	generatedClassFile := openGeneratedFile(t, mockFileSystem, generatedCodeFilePath)
	assertClassFileGeneratedOk(t, generatedClassFile, objectName)
	getter := `public MyBeanNameMyEnum getMyEnum() {
        return this.myEnum;
    }`
	setter := `public void setMyEnum(MyBeanNameMyEnum myEnum) {
        this.myEnum = myEnum;
    }`
	varCreation := `private MyBeanNameMyEnum myEnum;`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
	assert.Contains(t, generatedClassFile, `    public MyBeanName(MyBeanNameMyEnum myEnum) {
        this.myEnum = myEnum;
    }`)
	generatedEnumFile := openGeneratedFile(t, mockFileSystem, "dev/wyvinar/generated/MyBeanNameMyEnum.java")
	expectedEnumFile := `public enum MyBeanNameMyEnum {
    @SerializedName("string1")
    STRING_1("string1"),

    @SerializedName("string2")
    STRING_2("string2");

    %s
}`
	assert.Contains(t, generatedEnumFile, fmt.Sprintf(expectedEnumFile, fmt.Sprintf(ENUM_METHODS_TEMPLATE, "MyBeanNameMyEnum")))
}

func TestGenerateFilesProducesEnumWithNilValueIsntSetInConstructor(t *testing.T) {
	// Given...
	packageName := "generated"
	mockFileSystem := files.NewMockFileSystem()
	projectFilepath := "dev/wyvinar"
	apiFilePath := "test-resources/single-bean.yaml"
	objectName := "MyBeanName"
	generatedCodeFilePath := "dev/wyvinar/generated/MyBeanName.java"
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      description: A simple example
      properties:
        myEnum:
          type: string
          enum: [randValue1, nil]
`
	// When...
	mockFileSystem.WriteTextFile(apiFilePath, apiYaml)

	// When...
	err := GenerateFiles(mockFileSystem, projectFilepath, apiFilePath, packageName, true)

	// Then...
	assert.Nil(t, err)
	generatedClassFile := openGeneratedFile(t, mockFileSystem, generatedCodeFilePath)
	assertClassFileGeneratedOk(t, generatedClassFile, objectName)
	getter := `public MyBeanNameMyEnum getMyEnum() {
        return this.myEnum;
    }`
	setter := `public void setMyEnum(MyBeanNameMyEnum myEnum) {
        this.myEnum = myEnum;
    }`
	varCreation := `private MyBeanNameMyEnum myEnum;`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
	assert.Contains(t, generatedClassFile, `    public MyBeanName() {
    }`)
	generatedEnumFile := openGeneratedFile(t, mockFileSystem, "dev/wyvinar/generated/MyBeanNameMyEnum.java")
	expectedEnumFile := `public enum MyBeanNameMyEnum {
    @SerializedName("randValue1")
    RAND_VALUE_1("randValue1");

    %s
}`
	assert.Contains(t, generatedEnumFile, fmt.Sprintf(expectedEnumFile, fmt.Sprintf(ENUM_METHODS_TEMPLATE, "MyBeanNameMyEnum")))
}

func TestGenerateFilesProducesConstantCorrectly(t *testing.T) {
	// Given...
	packageName := "generated"
	mockFileSystem := files.NewMockFileSystem()
	projectFilepath := "dev/wyvinar"
	apiFilePath := "test-resources/single-bean.yaml"
	objectName := "MyBeanName"
	generatedCodeFilePath := "dev/wyvinar/generated/MyBeanName.java"
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      description: A simple example
      properties:
        myConstVar:
          type: string
          description: a test constant
          enum: [constVal]
`
	mockFileSystem.WriteTextFile(apiFilePath, apiYaml)

	// When...
	err := GenerateFiles(mockFileSystem, projectFilepath, apiFilePath, packageName, true)

	// Then...
	assert.Nil(t, err)
	generatedClassFile := openGeneratedFile(t, mockFileSystem, generatedCodeFilePath)
	assertClassFileGeneratedOk(t, generatedClassFile, objectName)
	constAssignment := `public final String MY_CONST_VAR = "constVal"`
	assert.Contains(t, generatedClassFile, constAssignment)
}

func TestGenerateFilesProducesClassWithReferencedStringProperty(t *testing.T) {
	// Given...
	packageName := "generated"
	mockFileSystem := files.NewMockFileSystem()
	projectFilepath := "dev/wyvinar"
	apiFilePath := "test-resources/single-bean.yaml"
	objectName := "MyBeanName"
	generatedCodeFilePath := "dev/wyvinar/generated/MyBeanName.java"
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      description: A simple example
      properties:
        myStringVar:
          $ref: '#/components/schemas/myReferencedProperty'
    myReferencedProperty:
      type: string
`
	// When...
	mockFileSystem.WriteTextFile(apiFilePath, apiYaml)

	// When...
	err := GenerateFiles(mockFileSystem, projectFilepath, apiFilePath, packageName, true)

	// Then...
	assert.Nil(t, err)
	generatedClassFile := openGeneratedFile(t, mockFileSystem, generatedCodeFilePath)
	assertClassFileGeneratedOk(t, generatedClassFile, objectName)
	getter := `public String getMyStringVar() {
        return this.myStringVar;
    }`
	setter := `public void setMyStringVar(String myStringVar) {
        this.myStringVar = myStringVar;
    }`
	varCreation := `private String myStringVar;`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
}

func TestGenerateFilesProducesClassWithDashInPropertyName(t *testing.T) {
	// Given...
	packageName := "generated"
	mockFileSystem := files.NewMockFileSystem()
	projectFilepath := "dev/wyvinar"
	apiFilePath := "test-resources/single-bean.yaml"
	objectName := "MyBeanName"
	generatedCodeFilePath := "dev/wyvinar/generated/MyBeanName.java"
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      description: A simple example
      properties:
        my-string-var:
          type: string
`
	// When...
	mockFileSystem.WriteTextFile(apiFilePath, apiYaml)

	// When...
	err := GenerateFiles(mockFileSystem, projectFilepath, apiFilePath, packageName, true)

	// Then...
	assert.Nil(t, err)
	generatedClassFile := openGeneratedFile(t, mockFileSystem, generatedCodeFilePath)
	assertClassFileGeneratedOk(t, generatedClassFile, objectName)
	getter := `public String getMyStringVar() {
        return this.myStringVar;
    }`
	setter := `public void setMyStringVar(String myStringVar) {
        this.myStringVar = myStringVar;
    }`
	varCreation := `private String myStringVar;`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
}

func TestGenerateFilesProducesClassWithReferencedArrayProperty(t *testing.T) {
	// Given...
	packageName := "generated"
	mockFileSystem := files.NewMockFileSystem()
	projectFilepath := "dev/wyvinar"
	apiFilePath := "test-resources/single-bean.yaml"
	objectName := "MyBeanName"
	generatedCodeFilePath := "dev/wyvinar/generated/MyBeanName.java"
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      description: A simple example
      properties:
        myReferencingArrayProp:
          $ref: '#/components/schemas/myReferencedArrayProperty'
    myReferencedArrayProperty:
      type: array
      items:
        type: string
`
	// When...
	mockFileSystem.WriteTextFile(apiFilePath, apiYaml)

	// When...
	err := GenerateFiles(mockFileSystem, projectFilepath, apiFilePath, packageName, true)

	// Then...
	assert.Nil(t, err)
	generatedClassFile := openGeneratedFile(t, mockFileSystem, generatedCodeFilePath)
	assertClassFileGeneratedOk(t, generatedClassFile, objectName)
	getter := `public String[] getMyReferencingArrayProp() {
        return this.myReferencingArrayProp;
    }`
	setter := `public void setMyReferencingArrayProp(String[] myReferencingArrayProp) {
        this.myReferencingArrayProp = myReferencingArrayProp;
    }`
	varCreation := `private String[] myReferencingArrayProp;`
	assert.Contains(t, generatedClassFile, getter)
	assert.Contains(t, generatedClassFile, setter)
	assert.Contains(t, generatedClassFile, varCreation)
}

func TestGenerateFilesProducesAcceptibleCodeUsingAllPreviousTestsAipYaml(t *testing.T) {
	// Given...
	packageName := "generated"
	mockFileSystem := files.NewMockFileSystem()
	projectFilepath := "dev/wyvinar"
	apiFilePath := "test-resources/single-bean.yaml"
	objectName := "MyBeanName"
	generatedCodeFilePath := "dev/wyvinar/generated/MyBeanName.java"
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      description: A simple example
      required: [myNestedObject, myDoubleVar, my2DArrayVar, myEnum]
      properties:
        myStringVar:
          type: string
          description: a test string
        myIntVar:
          type: integer
          description: a test integer
        myBoolVar:
          type: boolean
          description: a test boolean
        myDoubleVar:
          type: number
          description: a test double
        myArrayVar:
          type: array
          description: a test array
          items:
            type: string
        my2DArrayVar:
          type: array
          description: a test 2d array
          items:
            type: array
            items:
              type: string
        my3DArrayVar:
          type: array
          description: a test 3d array
          items:
            type: array
            items:
              type: array
              items:
                type: string
        myNestedObject:
          type: object
        myObjectReferencingProperty:
          $ref: '#/components/schemas/MyReferencedObject'
        myAnyOfReferencingArrayVar:
          type: array
          description: a test array
          items:
            anyOf:
            - $ref: '#/components/schemas/MyReferencedObject'
        myEnum:
          type: string
          enum: [randValue1, nil]
        myConstVar:
          type: string
          description: a test constant
          enum: [constVal]
        myReferencingArrayProp:
          $ref: '#/components/schemas/myReferencedArrayProperty'
        myReferencingStringProperty:
          $ref: '#/components/schemas/myReferencedStringProperty'
    MyReferencedObject:
      type: object
    myReferencedStringProperty:
      type: string
    myReferencedArrayProperty:
      type: array
      items:
        type: string
`
	// When...
	mockFileSystem.WriteTextFile(apiFilePath, apiYaml)

	// When...
	err := GenerateFiles(mockFileSystem, projectFilepath, apiFilePath, packageName, true)

	// Then...
	assert.Nil(t, err)
	generatedClassFile := openGeneratedFile(t, mockFileSystem, generatedCodeFilePath)
	assertClassFileGeneratedOk(t, generatedClassFile, objectName)
}

func TestGenerateStoreFilepathReturnsPathWithSlashBetweenProjectPathAndPackagePath(t *testing.T) {
	// Given...
	projectFilepath := "openapi2beans.dev/src/main/java"
	packageName := "this.package.hallo"

	// When...
	resultingPath := generateStoreFilepath(projectFilepath, packageName)

	// Then...
	assert.Equal(t, "openapi2beans.dev/src/main/java/this/package/hallo", resultingPath)
}

func TestGenerateStoreFilepathReturnsPathWithSlashBetweenProjectPathWithSlashAndPackagePath(t *testing.T) {
	// Given...
	projectFilepath := "openapi2beans.dev/src/main/java/"
	packageName := "this.package.hallo"

	// When...
	resultingPath := generateStoreFilepath(projectFilepath, packageName)

	// Then...
	assert.Equal(t, "openapi2beans.dev/src/main/java/this/package/hallo", resultingPath)
}

func TestGenerateDirectoriesCleansExistingJavaFilesFromFolder(t *testing.T) {
	// Given...
	mfs := files.NewMockFileSystem()
	storeFilepath := "openapi2beans.dev/src/main/java/this/package/hallo"
	randomFilepath := "openapi2beans.dev/src/main/java/this/package/hallo/smthn.java"
	mfs.MkdirAll(storeFilepath)
	mfs.WriteTextFile(randomFilepath, "public class emptyClass{}")

	// When...
	generateDirectories(mfs, storeFilepath, true)

	// Then...
	fileExists, err := mfs.Exists(randomFilepath)
	assert.Nil(t, err)
	assert.False(t, fileExists)
}

func TestGenerateDirectoriesErrorsWhenForceIsFalseAndThereIsAJavaFile(t *testing.T) {
	// Given...
	mfs := files.NewMockFileSystem()
	storeFilepath := "openapi2beans.dev/src/main/java/this/package/hallo"
	randomFilepath := "openapi2beans.dev/src/main/java/this/package/hallo/smthn.java"
	mfs.MkdirAll(storeFilepath)
	mfs.WriteTextFile(randomFilepath, "public class emptyClass{}")

	// When...
	err := generateDirectories(mfs, storeFilepath, false)

	// Then...
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "The tool is unable to create files in folder openapi2beans.dev/src/main/java/this/package/hallo because files in that folder already exist. Generating files is a destructive operation, removing all Java files in that folder prior to new files being created.\nIf you wish to proceed, delete the files manually, or re-run the tool using the --force option")
}

func TestGenerateDirectoriesDoesntRemoveNonJavaFiles(t *testing.T) {
	// Given...
	mfs := files.NewMockFileSystem()
	storeFilepath := "openapi2beans.dev/src/main/java/this/package/hallo"
	randomFilepath := "openapi2beans.dev/src/main/java/this/package/hallo/smthn.txt"
	mfs.MkdirAll(storeFilepath)
	mfs.WriteTextFile(randomFilepath, "this is a note")

	// When...
	generateDirectories(mfs, storeFilepath, true)

	// Then...
	fileExists, err := mfs.Exists(randomFilepath)
	assert.Nil(t, err)
	assert.True(t, fileExists)
}

func TestGenerateDirectoriesCleansExistingJavaFilesFromFolderWithSingleCharJavaFile(t *testing.T) {
	// Given...
	mfs := files.NewMockFileSystem()
	storeFilepath := "openapi2beans.dev/src/main/java/this/package/hallo"
	randomFilepath := "openapi2beans.dev/src/main/java/this/package/hallo/j.java"
	mfs.MkdirAll(storeFilepath)
	mfs.WriteTextFile(randomFilepath, "public class emptyClass{}")

	// When...
	generateDirectories(mfs, storeFilepath, true)

	// Then...
	fileExists, err := mfs.Exists(randomFilepath)
	assert.Nil(t, err)
	assert.False(t, fileExists)
}

func TestGenerateDirectoriesCleansExistingJavaFilesFromFolderButNotNonJavaFiles(t *testing.T) {
	// Given...
	mfs := files.NewMockFileSystem()
	storeFilepath := "openapi2beans.dev/src/main/java/this/package/hallo"
	randomFilepath := "openapi2beans.dev/src/main/java/this/package/hallo/smthn.java"
	mfs.MkdirAll(storeFilepath)
	mfs.WriteTextFile(randomFilepath, "public class emptyClass{}")
	textFilepath := "openapi2beans.dev/src/main/java/this/package/hallo/text.txt"
	mfs.WriteTextFile(textFilepath, "random note")

	// When...
	generateDirectories(mfs, storeFilepath, true)

	// Then...
	fileExists, err := mfs.Exists(randomFilepath)
	assert.Nil(t, err)
	assert.False(t, fileExists)
	fileExists, err = mfs.Exists(textFilepath)
	assert.Nil(t, err)
	assert.True(t, fileExists)
}

func TestGenerateDirectoriesCleansExistingJavaFilesFromFolderButNotSubFolder(t *testing.T) {
	// Given...
	mfs := files.NewMockFileSystem()
	storeFilepath := "openapi2beans.dev/src/main/java/this/package/hallo"
	randomFilepath := "openapi2beans.dev/src/main/java/this/package/hallo/smthn.java"
	mfs.MkdirAll(storeFilepath)
	mfs.WriteTextFile(randomFilepath, "public class emptyClass{}")
	deepFilepath := "openapi2beans.dev/src/main/java/this/package/hallo/more"
	deepRandomFilepath := "openapi2beans.dev/src/main/java/this/package/hallo/more/ohno.java"
	mfs.MkdirAll(deepFilepath)
	mfs.WriteTextFile(deepRandomFilepath, "public class emptyClassMk2{}")

	// When...
	generateDirectories(mfs, storeFilepath, true)

	// Then...
	fileExists, err := mfs.Exists(randomFilepath)
	assert.Nil(t, err)
	assert.False(t, fileExists)
	fileExists, err = mfs.Exists(deepRandomFilepath)
	assert.Nil(t, err)
	assert.True(t, fileExists)
}

func TestRetrieveAllJavaFilesWithNoJavaFiles(t *testing.T) {
	// Given...
	mfs := files.NewMockFileSystem()
	storeFilepath := "openapi2beans.dev/src/main/java/this/package/hallo"
	mfs.MkdirAll(storeFilepath)

	// When...
	javafilepaths, err := retrieveAllJavaFiles(mfs, storeFilepath)

	// Then...
	assert.Nil(t, err)
	assert.Empty(t, javafilepaths)
}

func TestRetrieveAllJavaFilesWithOneJavaFile(t *testing.T) {
	// Given...
	mfs := files.NewMockFileSystem()
	storeFilepath := "openapi2beans.dev/src/main/java/this/package/hallo"
	randomFilepath := "openapi2beans.dev/src/main/java/this/package/hallo/smthn.java"
	mfs.MkdirAll(storeFilepath)
	mfs.WriteTextFile(randomFilepath, "public class emptyClass{}")

	// When...
	javafilepaths, err := retrieveAllJavaFiles(mfs, storeFilepath)

	// Then...
	assert.Nil(t, err)
	assert.NotEmpty(t, javafilepaths)
	assert.Equal(t, randomFilepath, javafilepaths[0])
}

func TestRetrieveAllJavaFilesWithMultipleJavaFiles(t *testing.T) {
	// Given...
	mfs := files.NewMockFileSystem()
	storeFilepath := "openapi2beans.dev/src/main/java/this/package/hallo"
	randomFilepath := "openapi2beans.dev/src/main/java/this/package/hallo/smthn.java"
	randomFilepath2 := "openapi2beans.dev/src/main/java/this/package/hallo/bonsai.java"
	mfs.MkdirAll(storeFilepath)
	mfs.WriteTextFile(randomFilepath, "public class emptyClass{}")
	mfs.WriteTextFile(randomFilepath2, "public class Bonsai{private int leaves;}")

	// When...
	javafilepaths, err := retrieveAllJavaFiles(mfs, storeFilepath)

	// Then...
	assert.Nil(t, err)
	assert.NotEmpty(t, javafilepaths)
	assert.Equal(t, 2, len(javafilepaths))
	assert.Contains(t, javafilepaths, randomFilepath)
	assert.Contains(t, javafilepaths, randomFilepath2)
}

func TestRetrieveAllJavaFilesDoesntPickUpNonJavaFile(t *testing.T) {
	// Given...
	mfs := files.NewMockFileSystem()
	storeFilepath := "openapi2beans.dev/src/main/java/this/package/hallo"
	randomFilepath := "openapi2beans.dev/src/main/java/this/package/hallo/smthn.java"
	randomFilepath2 := "openapi2beans.dev/src/main/java/this/package/hallo/bonsai.txt"
	mfs.MkdirAll(storeFilepath)
	mfs.WriteTextFile(randomFilepath, "public class emptyClass{}")
	mfs.WriteTextFile(randomFilepath2, "I am a bonsai, short n stout")

	// When...
	javafilepaths, err := retrieveAllJavaFiles(mfs, storeFilepath)

	// Then...
	assert.Nil(t, err)
	assert.NotEmpty(t, javafilepaths)
	assert.Equal(t, 1, len(javafilepaths))
	assert.Contains(t, javafilepaths, randomFilepath)
	assert.NotContains(t, javafilepaths, randomFilepath2)
}

func TestRetrieveAllJavaFilesDoesntPickUpDeepJavaFile(t *testing.T) {
	// Given...
	mfs := files.NewMockFileSystem()
	storeFilepath := "openapi2beans.dev/src/main/java/this/package/hallo"
	randomFilepath := "openapi2beans.dev/src/main/java/this/package/hallo/smthn.java"
	randomFilepath2 := "openapi2beans.dev/src/main/java/this/package/hallo/tree/bonsai.java"
	mfs.MkdirAll(storeFilepath)
	mfs.WriteTextFile(randomFilepath, "public class emptyClass{}")
	mfs.WriteTextFile(randomFilepath2, "public class Bonsai{private int branches}")

	// When...
	javafilepaths, err := retrieveAllJavaFiles(mfs, storeFilepath)

	// Then...
	assert.Nil(t, err)
	assert.NotEmpty(t, javafilepaths)
	assert.Equal(t, 1, len(javafilepaths))
	assert.Contains(t, javafilepaths, randomFilepath)
	assert.NotContains(t, javafilepaths, randomFilepath2)
}
