/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package generator

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

const SCHEMAS_PATH = "#/components/schemas/"
func TestArrayWithoutItemsReturnsNonFatalError(t *testing.T) {
	// Given...
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      properties:
        myTestArray:
          type: array
`
	// When...
	schemaTypes, errList, err := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	schemaPath := SCHEMAS_PATH+"MyBeanName"
	assert.Nil(t, err)
	assert.NotNil(t, errList)
	err, errExists := errList[schemaPath + "/myTestArray"]
	assert.True(t, errExists)
	assert.Contains(t, err.Error(), "RetrieveArrayType: Failed to find required items section for ")
	_, schemaTypeExists := schemaTypes[schemaPath]
	assert.False(t, schemaTypeExists)
}

func TestArrayWithoutItemsReturnsNonFatalErrorButContinues(t *testing.T) {
	// Given...
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      properties:
        myTestArray:
          type: array
    ReferencedObject:
      type: object
      properties:
        randomString:
          type: string
`
	// When...
	schemaTypes, errList, err := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	schemaPath := SCHEMAS_PATH+"MyBeanName"
	assert.NotNil(t, errList)
	assert.Nil(t, err)
	err, errExists := errList[schemaPath + "/myTestArray"]
	assert.True(t, errExists)
	assert.Contains(t, err.Error(), "RetrieveArrayType: Failed to find required items section for ")
	assert.Equal(t, 1, len(schemaTypes))
	schemaType, schemaTypeExists := schemaTypes[SCHEMAS_PATH+"ReferencedObject"]
	assert.True(t, schemaTypeExists)
	assert.NotEmpty(t, schemaType.GetProperties(), "Bean must have variable!")
	property1, propertyExists := schemaType.GetProperties()["#/components/schemas/ReferencedObject/randomString"]
	assert.True(t, propertyExists)
	assert.Equal(t, "randomString", property1.GetName(), "Wrong bean variable name read out of the yaml!")
	assert.Equal(t, "string", property1.GetType(), "Wrong bean variable type read out of the yaml!")
	assert.Equal(t, false, property1.IsCollection(), "Wrong bean variable cardinality read out of the yaml!")
}

func TestYamlWithoutComponentsSectionReturnsFatalError(t *testing.T) {
	// Given...
	apiYaml := `openapi: 3.0.3
schemas:
  MyBeanName:
    type: object
`
	// When...
	schemaTypes, errList, err := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	schemaPath := SCHEMAS_PATH+"MyBeanName"
	assert.NotNil(t, err)
	assert.Empty(t, errList)
	assert.Contains(t, err.Error(), "RetrieveSchemasMapFromEntireYamlMap: Failed to find components within ")
	_, schemaTypeExists := schemaTypes[schemaPath]
	assert.False(t, schemaTypeExists)
}

func TestYamlWithoutSchemasSectionReturnsFatalError(t *testing.T) {
	// Given...
	apiYaml := `openapi: 3.0.3
components:
  MyBeanName:
    type: object
`
	// When...
	schemaTypes, errList, err := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	schemaPath := SCHEMAS_PATH+"MyBeanName"
	assert.NotNil(t, err)
	assert.Empty(t, errList)
	assert.Contains(t, err.Error(), "RetrieveSchemasMapFromEntireYamlMap: Failed to find schemas within ")
	_, schemaTypeExists := schemaTypes[schemaPath]
	assert.False(t, schemaTypeExists)
}

func TestSchemaThatReferencesNonExistentPropertyReturnsNonFatalError(t *testing.T) {
	// Given...
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      properties:
        myTestArray:
          type: array
          items:
            $ref: '#/components/schemas/randomSchema'
`
	// When...
	schemaTypes, errList, err := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	schemaPath := SCHEMAS_PATH+"MyBeanName"
	assert.Nil(t, err)
	assert.NotNil(t, errList)
	err, errExists := errList[SCHEMAS_PATH+"MyBeanName/myTestArray"]
	assert.True(t, errExists)
	assert.Contains(t, err.Error(), "ResolveReferences: Failed to find referenced property for ")
	_, schemaTypeExists := schemaTypes[schemaPath]
	assert.False(t, schemaTypeExists)
}

func TestSchemaThatHasNoTypeReturnsNonFatalError(t *testing.T) {
	// Given...
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      properties:
        myTestArray:
          type: array
          items:
            type: string
`
	// When...
	schemaTypes, errList, err := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	schemaPath := SCHEMAS_PATH+"MyBeanName"
	assert.Nil(t, err)
	assert.NotNil(t, errList)
	err, errExists := errList[schemaPath]
	assert.True(t, errExists)
	assert.Contains(t, err.Error(), "RetrieveVarType: Failed to find required type for ")
	_, schemaTypeExists := schemaTypes[schemaPath]
	assert.False(t, schemaTypeExists)
}

func TestArrayWithAllOfPartReturnsNonFatalErrorButContinues(t *testing.T) {
	// Given...
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      properties:
        myTestArray:
          type: array
          items:
            allOf:
            - $ref: '#/components/schemas/ReferencedObject'
    ReferencedObject:
      type: object
      properties:
        randomString:
          type: string
`
	// When...
	schemaTypes, errList, err := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	schemaPath := SCHEMAS_PATH+"MyBeanName"
	assert.NotNil(t, errList)
	assert.Nil(t, err)
	err, errExists := errList[schemaPath + "/myTestArray"]
	assert.True(t, errExists)
	assert.Contains(t, err.Error(), "RetrieveVarType: illegal allOf part found in ")
	assert.Equal(t, 1, len(schemaTypes))
	schemaType, schemaTypeExists := schemaTypes[SCHEMAS_PATH+"ReferencedObject"]
	assert.True(t, schemaTypeExists)
	assert.NotEmpty(t, schemaType.GetProperties(), "Bean must have variable!")
	property1, propertyExists := schemaType.GetProperties()["#/components/schemas/ReferencedObject/randomString"]
	assert.True(t, propertyExists)
	assert.Equal(t, "randomString", property1.GetName(), "Wrong bean variable name read out of the yaml!")
	assert.Equal(t, "string", property1.GetType(), "Wrong bean variable type read out of the yaml!")
	assert.Equal(t, false, property1.IsCollection(), "Wrong bean variable cardinality read out of the yaml!")
}

func TestArrayWithOneOfPartReturnsNonFatalErrorButContinues(t *testing.T) {
	// Given...
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      properties:
        myTestArray:
          type: array
          items:
            oneOf:
            - $ref: '#/components/schemas/ReferencedObject'
    ReferencedObject:
      type: object
      properties:
        randomString:
          type: string
`
	// When...
	schemaTypes, errList, err := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	schemaPath := SCHEMAS_PATH+"MyBeanName"
	assert.NotNil(t, errList)
	assert.Nil(t, err)
	err, errExists := errList[schemaPath + "/myTestArray"]
	assert.True(t, errExists)
	assert.Contains(t, err.Error(), "RetrieveVarType: illegal oneOf part found in ")
	assert.Equal(t, 1, len(schemaTypes))
	schemaType, schemaTypeExists := schemaTypes[SCHEMAS_PATH+"ReferencedObject"]
	assert.True(t, schemaTypeExists)
	assert.NotEmpty(t, schemaType.GetProperties(), "Bean must have variable!")
	property1, propertyExists := schemaType.GetProperties()["#/components/schemas/ReferencedObject/randomString"]
	assert.True(t, propertyExists)
	assert.Equal(t, "randomString", property1.GetName(), "Wrong bean variable name read out of the yaml!")
	assert.Equal(t, "string", property1.GetType(), "Wrong bean variable type read out of the yaml!")
	assert.Equal(t, false, property1.IsCollection(), "Wrong bean variable cardinality read out of the yaml!")
}

func TestGetSchemaTypesFromYamlReturns1BeanOK(t *testing.T) {
	// Given...
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
`
	// When...
	schemaTypes, errList, err  := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	assert.Empty(t, errList)
	assert.Nil(t, err)
	assert.Equal(t, 1, len(schemaTypes))
}

func TestGetSchemaTypesFromYamlReturnsBeanWithName(t *testing.T) {
	// Given...
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
`

	// When...
	schemaTypes, errList, err  := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	assert.Empty(t, errList)
	assert.Nil(t, err)
	assert.Equal(t, 1, len(schemaTypes))
	schemaType, schemaTypeExists := schemaTypes[SCHEMAS_PATH+"MyBeanName"]
	assert.True(t, schemaTypeExists)
	assert.Equal(t, "MyBeanName", schemaType.GetName(), "Wrong bean name read out of the yaml!")
}

func TestGetSchemaTypesFromYamlParsesDescription(t *testing.T) {
	// Given...
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      description: A simple example
`
	// When...
	schemaTypes, errList, err  := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	assert.Empty(t, errList)
	assert.Nil(t, err)
	assert.Equal(t, 1, len(schemaTypes))
	schemaType, schemaTypeExists := schemaTypes[SCHEMAS_PATH+"MyBeanName"]
	assert.True(t, schemaTypeExists)
	assert.Equal(t, "A simple example", schemaType.GetDescription(), "Wrong bean description read out of the yaml!")
}

func TestGetSchemaTypesFromYamlParsesSingleStringVariable(t *testing.T) {
	// Given...
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
	schemaTypes, errList, err  := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	assert.Empty(t, errList)
	assert.Nil(t, err)
	assert.Equal(t, 1, len(schemaTypes))
	schemaType, schemaTypeExists := schemaTypes[SCHEMAS_PATH+"MyBeanName"]
	assert.True(t, schemaTypeExists)
	assert.NotEmpty(t, schemaType.GetProperties(), "Bean must have variable!")
	property, propertyExists := schemaType.GetProperties()["#/components/schemas/MyBeanName/myStringVar"]
	assert.True(t, propertyExists)
	assert.Equal(t, "myStringVar", property.GetName(), "Wrong bean variable name read out of the yaml!")
	assert.Equal(t, "string", property.GetType(), "Wrong bean variable type read out of the yaml!")
}

func TestGetSchemaTypesFromYamlParsesSingleStringVariableWithDescription(t *testing.T) {
	// Given...
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
	// When...
	schemaTypes, errList, err  := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	assert.Empty(t, errList)
	assert.Nil(t, err)
	assert.Equal(t, 1, len(schemaTypes))
	schemaType, schemaTypeExists := schemaTypes[SCHEMAS_PATH+"MyBeanName"]
	assert.True(t, schemaTypeExists)
	property, propertyExists := schemaType.GetProperties()["#/components/schemas/MyBeanName/myStringVar"]
	assert.True(t, propertyExists)
	assert.Equal(t, "a test string", property.GetDescription(), "Wrong bean variable description read out of the yaml!")
}

func TestGetSchemaTypesFromYamlParsesSingleStringVariableWithTrueRequiredField(t *testing.T) {
	// Given...
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
	// When...
	schemaTypes, errList, err  := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	assert.Empty(t, errList)
	assert.Nil(t, err)
	assert.Equal(t, 1, len(schemaTypes))
	schemaType, schemaTypeExists := schemaTypes[SCHEMAS_PATH+"MyBeanName"]
	assert.True(t, schemaTypeExists)
	assert.NotEmpty(t, schemaType.GetProperties(), "Bean must have variable!")
	property, propertyExists := schemaType.GetProperties()["#/components/schemas/MyBeanName/myStringVar"]
	assert.True(t, propertyExists)
	assert.Equal(t, true, property.IsSetInConstructor(), "Wrong bean variable required status read out of the yaml!")
}

func TestGetSchemaTypesFromYamlParsesSingleStringVariableWithNoRequiredFieldReturnsFalse(t *testing.T) {
	// Given...
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
	// When...
	schemaTypes, errList, err  := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	assert.Empty(t, errList)
	assert.Nil(t, err)
	assert.Equal(t, 1, len(schemaTypes))
	schemaType, schemaTypeExists := schemaTypes[SCHEMAS_PATH+"MyBeanName"]
	assert.True(t, schemaTypeExists)
	assert.NotEmpty(t, schemaType.GetProperties(), "Bean must have variable!")
	property, propertyExists := schemaType.GetProperties()["#/components/schemas/MyBeanName/myStringVar"]
	assert.True(t, propertyExists)
	assert.Equal(t, false, property.IsSetInConstructor(), "Wrong bean variable required status read out of the yaml!")
}

func TestGetSchemaTypesFromYamlParsesMultipleStringVariableWithTrueRequiredFields(t *testing.T) {
	// Given...
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      description: A simple example
      required: [myStringVar, myStringVar1]
      properties:
        myStringVar:
          type: string
          description: a test string
        myStringVar1:
          type: string
          description: a test string
`
	// When...
	schemaTypes, errList, err  := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	assert.Empty(t, errList)
	assert.Nil(t, err)
	assert.Equal(t, 1, len(schemaTypes))
	schemaType, schemaTypeExists := schemaTypes[SCHEMAS_PATH+"MyBeanName"]
	assert.True(t, schemaTypeExists)
	assert.NotEmpty(t, schemaType.GetProperties(), "Bean must have variable!")
	property1, propertyExists := schemaType.GetProperties()["#/components/schemas/MyBeanName/myStringVar"]
	assert.True(t, propertyExists)
	property2, propertyExists := schemaType.GetProperties()["#/components/schemas/MyBeanName/myStringVar1"]
	assert.True(t, propertyExists)
	assert.Equal(t, true, property1.IsSetInConstructor(), "Wrong bean variable required status read out of the yaml!")
	assert.Equal(t, true, property2.IsSetInConstructor(), "Wrong bean variable required status read out of the yaml!")
}

func TestGetSchemaTypesFromYamlParsesMultipleStringVariablesWithMixedRequiredFields(t *testing.T) {
	// Given...
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      description: A simple example
      required: [myStringVar1]
      properties:
        myStringVar:
          type: string
          description: a test string
        myStringVar1:
          type: string
          description: a test string in addition to the other
`
	// When...
	schemaTypes, errList, err  := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	assert.Empty(t, errList)
	assert.Nil(t, err)
	assert.Equal(t, 1, len(schemaTypes))
	schemaType, schemaTypeExists := schemaTypes[SCHEMAS_PATH+"MyBeanName"]
	assert.True(t, schemaTypeExists)
	assert.NotEmpty(t, schemaType.GetProperties(), "Bean must have variable!")
	property1, propertyExists := schemaType.GetProperties()["#/components/schemas/MyBeanName/myStringVar"]
	assert.True(t, propertyExists)
	property2, propertyExists := schemaType.GetProperties()["#/components/schemas/MyBeanName/myStringVar1"]
	assert.True(t, propertyExists)
	assert.Equal(t, false, property1.IsSetInConstructor(), "Wrong bean variable required status read out of the yaml!")
	assert.Equal(t, true, property2.IsSetInConstructor(), "Wrong bean variable required status read out of the yaml!")
}

func TestGetSchemaTypesFromYamlParsesMultipleStringVariables(t *testing.T) {
	// Given...
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
        mySecondStringVar:
          type: string
          description: a second test string
`
	// When...
	schemaTypes, errList, err  := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	assert.Empty(t, errList)
	assert.Nil(t, err)
	assert.Equal(t, 1, len(schemaTypes))
	schemaType, schemaTypeExists := schemaTypes[SCHEMAS_PATH+"MyBeanName"]
	assert.True(t, schemaTypeExists)
	assert.NotEmpty(t, schemaType.GetProperties(), "Bean must have variable!")
	property1, propertyExists := schemaType.GetProperties()["#/components/schemas/MyBeanName/myStringVar"]
	assert.True(t, propertyExists)
	property2, propertyExists := schemaType.GetProperties()["#/components/schemas/MyBeanName/mySecondStringVar"]
	assert.True(t, propertyExists)
	assert.Equal(t, "myStringVar", property1.GetName(), "Wrong bean variable name read out of the yaml!")
	assert.Equal(t, "string", property1.GetType(), "Wrong bean variable type read out of the yaml!")
	assert.Equal(t, "mySecondStringVar", property2.GetName(), "Wrong bean variable name read out of the yaml!")
	assert.Equal(t, "string", property2.GetType(), "Wrong bean variable type read out of the yaml!")
}

func TestGetSchemaTypesFromYamlParsesObjectWithArray(t *testing.T) {
	// Given...
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      properties:
        myTestArray:
          type: array
          items:
            type: string
`
	// When...
	schemaTypes, errList, err  := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	assert.Empty(t, errList)
	assert.Nil(t, err)
	assert.Equal(t, 1, len(schemaTypes))
	schemaType, schemaTypeExists := schemaTypes[SCHEMAS_PATH+"MyBeanName"]
	assert.True(t, schemaTypeExists)
	assert.NotEmpty(t, schemaType.GetProperties(), "Bean must have variable!")
	property1, propertyExists := schemaType.GetProperties()["#/components/schemas/MyBeanName/myTestArray"]
	assert.True(t, propertyExists)
	assert.Equal(t, "myTestArray", property1.GetName(), "Wrong bean variable name read out of the yaml!")
	assert.Equal(t, "string", property1.GetType(), "Wrong bean variable type read out of the yaml!")
	assert.Equal(t, true, property1.IsCollection(), "Wrong bean variable cardinality read out of the yaml!")
}

func TestGetSchemaTypesFromYamlParsesObjectWithArrayContainingArray(t *testing.T) {
	// Given...
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      properties:
        myTestArray:
          type: array
          items:
            type: array
            items:
              type: string
`
	// When...
	schemaTypes, errList, err  := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	assert.Empty(t, errList)
	assert.Nil(t, err)
	assert.Equal(t, 1, len(schemaTypes))
	schemaType, schemaTypeExists := schemaTypes[SCHEMAS_PATH+"MyBeanName"]
	assert.True(t, schemaTypeExists)
	assert.NotEmpty(t, schemaType.GetProperties(), "Bean must have variable!")
	property1, propertyExists := schemaType.GetProperties()["#/components/schemas/MyBeanName/myTestArray"]
	assert.True(t, propertyExists)
	assert.Equal(t, "myTestArray", property1.GetName(), "Wrong bean variable name read out of the yaml!")
	assert.Equal(t, "string", property1.GetType(), "Wrong bean variable type read out of the yaml!")
	assert.Equal(t, true, property1.IsCollection(), "Wrong bean variable cardinality read out of the yaml!")
	assert.Equal(t, 2, property1.cardinality.GetDimensions(), "Wrong array dimension read out of the yaml!")
}

func TestGetSchemaTypesFromYamlParsesObjectWith3DArray(t *testing.T) {
	// Given...
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      properties:
        myTestArray:
          type: array
          items:
            type: array
            items:
              type: array
              items:
                type: string
`
	// When...
	schemaTypes, errList, err  := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	assert.Empty(t, errList)
	assert.Nil(t, err)
	assert.Equal(t, 1, len(schemaTypes))
	schemaType, schemaTypeExists := schemaTypes[SCHEMAS_PATH+"MyBeanName"]
	assert.True(t, schemaTypeExists)
	assert.NotEmpty(t, schemaType.GetProperties(), "Bean must have variable!")
	property1, propertyExists := schemaType.GetProperties()["#/components/schemas/MyBeanName/myTestArray"]
	assert.True(t, propertyExists)
	assert.Equal(t, "myTestArray", property1.GetName(), "Wrong bean variable name read out of the yaml!")
	assert.Equal(t, "string", property1.GetType(), "Wrong bean variable type read out of the yaml!")
	assert.Equal(t, true, property1.IsCollection(), "Wrong bean variable cardinality read out of the yaml!")
	assert.Equal(t, 3, property1.cardinality.GetDimensions(), "Wrong array dimension read out of the yaml!")
}

func TestGetSchemaTypesFromYamlParsesNestedObjects(t *testing.T) {
	// Given...
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      properties:
        nestedObject:
          type: object
          properties:
            randomString:
              type: string
`

	// When...
	schemaTypes, errList, err  := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	assert.Empty(t, errList)
	assert.Nil(t, err)
	assert.Equal(t, 2, len(schemaTypes))
	schemaType, schemaTypeExists := schemaTypes[SCHEMAS_PATH+"MyBeanName"]
	assert.True(t, schemaTypeExists)
	assert.NotEmpty(t, schemaType.GetProperties(), "Bean must have variable!")
	property1, propertyExists := schemaType.GetProperties()["#/components/schemas/MyBeanName/nestedObject"]
	assert.True(t, propertyExists)
	property2, propertyExists := property1.resolvedType.GetProperties()["#/components/schemas/MyBeanName/nestedObject/randomString"]
	assert.True(t, propertyExists)
	assert.Equal(t, "nestedObject", property1.GetName(), "Wrong bean variable name read out of the yaml!")
	assert.Equal(t, "randomString", property2.GetName(), "Wrong bean variable name read out of the yaml!")
	nestedschemaType, nestedSchemaTypeExists := schemaTypes["#/components/schemas/MyBeanName/nestedObject"]
	assert.True(t, nestedSchemaTypeExists)
	assert.NotEmpty(t, nestedschemaType.GetProperties(), "Bean must have variable!")
	nestedObjProp, nestedObjPropExists := schemaType.GetProperties()["#/components/schemas/MyBeanName/nestedObject"]
	assert.True(t, nestedObjPropExists)
	nestedObjSchemaType := nestedObjProp.resolvedType
	assert.Equal(t, "MyBeanNameNestedObject", nestedObjSchemaType.name)
}

func TestGetSchemaTypesFromYamlParsesReferenceToObject(t *testing.T) {
	// Given...
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      properties:
        referencingObject:
          $ref: '#/components/schemas/ReferencedObject'
    ReferencedObject:
      type: object
      properties:
        randomString:
          type: string
`

	// When...
	schemaTypes, errList, err  := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	assert.Empty(t, errList)
	assert.Nil(t, err)
	assert.Equal(t, 2, len(schemaTypes))
	schemaType, schemaTypeExists := schemaTypes[SCHEMAS_PATH+"MyBeanName"]
	assert.True(t, schemaTypeExists)
	assert.NotEmpty(t, schemaType.GetProperties(), "Bean must have variable!")
	property1, propertyExists := schemaType.GetProperties()["#/components/schemas/MyBeanName/referencingObject"]
	assert.True(t, propertyExists)
	assert.Equal(t, "referencingObject", property1.GetName(), "Wrong bean variable name read out of the yaml!")
	assert.Equal(t, "object", property1.GetType())
}

func TestGetSchemaTypesFromYamlParsesObjectWithArrayContainingTypeRefToObject(t *testing.T) {
	// Given...
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      properties:
        myTestArray:
          type: array
          items:
            $ref: '#/components/schemas/ReferencedObject'
    ReferencedObject:
      type: object
      properties:
        randomString:
          type: string
`
	// When...
	schemaTypes, errList, err  := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	assert.Empty(t, errList)
	assert.Nil(t, err)
	assert.Equal(t, 2, len(schemaTypes))
	schemaType, schemaTypeExists := schemaTypes[SCHEMAS_PATH+"MyBeanName"]
	assert.True(t, schemaTypeExists)
	assert.NotEmpty(t, schemaType.GetProperties(), "Bean must have variable!")
	property1, propertyExists := schemaType.GetProperties()["#/components/schemas/MyBeanName/myTestArray"]
	assert.True(t, propertyExists)
	assert.Equal(t, "myTestArray", property1.GetName(), "Wrong bean variable name read out of the yaml!")
	assert.Equal(t, "object", property1.GetType(), "Wrong bean variable type read out of the yaml!")
	assert.Equal(t, true, property1.IsCollection(), "Wrong bean variable cardinality read out of the yaml!")
}

func TestGetSchemaTypesFromYamlParsesEnum(t *testing.T) {
	// Given...
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      properties:
        MyEnum:
          type: string
          enum: [randValue1, randValue2]
`

	// When...
	schemaTypes, errList, err  := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	schemaPath := SCHEMAS_PATH+"MyBeanName"
	propertyPath := schemaPath + "/MyEnum"
	assert.Empty(t, errList)
	assert.Nil(t, err)
	assert.Equal(t, 2, len(schemaTypes))
	schemaType, schemaTypeExists := schemaTypes[schemaPath]
	assert.True(t, schemaTypeExists)
	assert.NotEmpty(t, schemaType.GetProperties(), "Bean must have variable!")
	property1, propertyExists := schemaType.GetProperties()[propertyPath]
	assert.True(t, propertyExists)
	assert.Equal(t, true, property1.IsEnum())
	assert.Equal(t, "MyBeanNameMyEnum", property1.resolvedType.name)
	assert.Equal(t, "string", property1.typeName)
	posValue1, posValueExists := property1.GetPossibleValues()["randValue1"]
	assert.True(t, posValueExists)
	assert.Equal(t, "randValue1", posValue1)
	posValue2, posValueExists := property1.GetPossibleValues()["randValue2"]
	assert.True(t, posValueExists)
	assert.Equal(t, "randValue2", posValue2)
	enumSchemaType, enumSchemaTypeExists := schemaTypes[propertyPath]
	assert.Equal(t, true, enumSchemaTypeExists)
	assert.Equal(t, "MyBeanNameMyEnum", enumSchemaType.name)
	assert.Equal(t, "MyEnum", enumSchemaType.ownProperty.name)
}

func TestGetSchemaTypesFromYamlParsesEnumAsConstant(t *testing.T) {
	// Given...
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      properties:
        MyConstant:
          type: string
          enum: [randValue1]
`
	
	// When...
	schemaTypes, errList, err  := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	schemaPath := SCHEMAS_PATH+"MyBeanName"
	propertyPath := schemaPath + "/MyConstant"
	assert.Empty(t, errList)
	assert.Nil(t, err)
	assert.Equal(t, 1, len(schemaTypes))
	schemaType, schemaTypeExists := schemaTypes[schemaPath]
	assert.True(t, schemaTypeExists)
	assert.NotEmpty(t, schemaType.GetProperties(), "Bean must have variable!")
	property1, propertyExists := schemaType.GetProperties()[propertyPath]
	assert.True(t, propertyExists)
	assert.Equal(t, true, property1.IsConstant())
	posValue, posValueExists := property1.GetPossibleValues()["randValue1"]
	assert.True(t, posValueExists)
	assert.Equal(t, "randValue1", posValue)
}

func TestGetSchemaTypesFromYamlParsesClassWithReferenceToPropertyInWiderSchemaMap(t *testing.T) {
	// Given...
	apiYaml := `openapi: 3.0.3
components:
  schemas:
    MyBeanName:
      type: object
      properties:
        myReferencingProperty:
          $ref: '#/components/schemas/ReferencedProperty'
    ReferencedProperty:
      type: integer
`
	
	// When...
	schemaTypes, errList, err  := getSchemaTypesFromYaml([]byte(apiYaml))

	// Then...
	schemaPath := SCHEMAS_PATH+"MyBeanName"
	propertyPath := schemaPath + "/myReferencingProperty"
	assert.Empty(t, errList)
	assert.Nil(t, err)
	assert.Equal(t, 1, len(schemaTypes))
	schemaType, schemaTypeExists := schemaTypes[schemaPath]
	assert.True(t, schemaTypeExists)
	assert.NotEmpty(t, schemaType.GetProperties(), "Bean must have variable!")
	property1, propertyExists := schemaType.GetProperties()[propertyPath]
	assert.True(t, propertyExists)
	assert.Equal(t, "integer", property1.typeName)
	assert.Equal(t, "myReferencingProperty", property1.name)
}