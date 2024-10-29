/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package generator

import (
	"log"
	"strings"

	openapi2beans_errors "github.com/dev-galasa/buildutils/openapi2beans/pkg/errors"
	"github.com/dev-galasa/buildutils/openapi2beans/pkg/utils"
	"gopkg.in/yaml.v3"
)

const (
	OPENAPI_YAML_SCHEMAS_PATH        = "#/components/schemas"
	OPENAPI_YAML_KEYWORD_COMPONENTS  = "components"
	OPENAPI_YAML_KEYWORD_SCHEMAS     = "schemas"
	OPENAPI_YAML_KEYWORD_DESCRIPTION = "description"
	OPENAPI_YAML_KEYWORD_PROPERTIES  = "properties"
	OPENAPI_YAML_KEYWORD_TYPE        = "type"
	OPENAPI_YAML_KEYWORD_REQUIRED    = "required"
	OPENAPI_YAML_KEYWORD_ITEMS       = "items"
	OPENAPI_YAML_KEYWORD_ALLOF       = "allOf"
	OPENAPI_YAML_KEYWORD_ONEOF       = "oneOf"
	OPENAPI_YAML_KEYWORD_ANYOF       = "anyOf"
	OPENAPI_YAML_KEYWORD_REF         = "$ref"
	OPENAPI_YAML_KEYWORD_ENUM        = "enum"
)

// recursion counter, counts how many times retrieveArrayType is called for each property, 
// is reset to 0 after each retriveVarType call returns to retrieveSchemaComponentsFromMap
// used when an array has an item of array type (when an array would have multiple dimensions)
var arrayDimensions = 0

func getSchemaTypesFromYaml(apiyaml []byte) (map[string]*SchemaType, map[string]error, error) {
	var (
		schemasMap  map[string]interface{}
		schemaTypes = make(map[string]*SchemaType)
		properties  = make(map[string]*Property)
		errMap      = make(map[string]error)
		fatalErr    error
	)

	entireYamlMap := make(map[string]interface{})

	fatalErr = yaml.Unmarshal(apiyaml, &entireYamlMap)

	if fatalErr == nil {
		schemasMap, fatalErr = retrieveSchemasMapFromEntireYamlMap(entireYamlMap)

		if fatalErr == nil {
			retrieveSchemaComponentsFromMap(schemasMap, OPENAPI_YAML_SCHEMAS_PATH, schemaTypes, properties, errMap)
			resolveReferences(properties, schemaTypes, errMap)
		}
	}

	return schemaTypes, errMap, fatalErr
}

func retrieveSchemasMapFromEntireYamlMap(entireYamlMap map[string]interface{}) (map[string]interface{}, error) {
	var err error
	schemasMap := make(map[string]interface{})

	components, isComponentsPresent := entireYamlMap[OPENAPI_YAML_KEYWORD_COMPONENTS]
	if isComponentsPresent {
		componentsMap := components.(map[string]interface{})
		schemas, isSchemasPresent := componentsMap[OPENAPI_YAML_KEYWORD_SCHEMAS]
		if isSchemasPresent {
			schemasMap = schemas.(map[string]interface{})
		} else {
			err = openapi2beans_errors.NewError("RetrieveSchemasMapFromEntireYamlMap: Failed to find schemas within %v", entireYamlMap)
		}
	} else {
		err = openapi2beans_errors.NewError("RetrieveSchemasMapFromEntireYamlMap: Failed to find components within %v", entireYamlMap)
	}
	return schemasMap, err
}

func retrieveSchemaComponentsFromMap(
	inputMap map[string]interface{},
	parentPath string,
	schemaTypes map[string]*SchemaType,
	properties map[string]*Property,
	errMap map[string]error) {
	var err error
	for subMapKey, subMapObj := range inputMap {
		log.Printf("RetrieveSchemaTypesFromMap: %v\n", subMapObj)

		subMap := subMapObj.(map[string]interface{})
		apiSchemaPartPath := parentPath + filepathSeparator + subMapKey
		varName := subMapKey

		var typeName string
		var cardinality Cardinality
		description := retrieveDescription(subMap)
		typeName, cardinality, err = retrieveVarType(subMap, apiSchemaPartPath)
		arrayDimensions = 0
		possibleValues := retrievePossibleValues(subMap)

		if err == nil {
			property := NewProperty(subMapKey, apiSchemaPartPath, description, typeName, possibleValues, nil, cardinality)
			assignPropertyToSchemaType(parentPath, apiSchemaPartPath, property, schemaTypes)

			var schemaType *SchemaType

			if typeName == "object" || property.IsEnum() {
				if parentPath != OPENAPI_YAML_SCHEMAS_PATH {
					varName = resolveNestedObjectName(varName, parentPath)
				}
				schemaType = NewSchemaType(utils.StringToPascal(varName), description, property, nil)
				property.SetResolvedType(schemaType)
				schemaTypes[apiSchemaPartPath] = schemaType
				if typeName == "object" {
					retrieveNestedProperties(subMap, apiSchemaPartPath, schemaTypes, properties, errMap)
					resolvePropertiesMinCardinalities(subMap, schemaType.properties, apiSchemaPartPath)
				}
			}

			properties[apiSchemaPartPath] = property

			if schemaTypeErrored(schemaType, errMap) {
				removeSchemaTypeAndProps(schemaTypes, properties, apiSchemaPartPath)
			}
		}

		if err != nil {
			errMap[apiSchemaPartPath] = err
		}
	}
}

func resolveReferences(properties map[string]*Property, schemaTypes map[string]*SchemaType, errMap map[string]error) {
	for _, property := range properties {
		if property.IsReferencing() {
			referencingPath := strings.Split(property.GetType(), ":")[1]
			referencedProp, isRefPropPresent := properties[referencingPath]
			if isRefPropPresent {
				property.Resolve(referencedProp)
			} else {
				err := openapi2beans_errors.NewError("ResolveReferences: Failed to find referenced property for %v\n", property)
				errMap[property.path] = err
				removeSchemaTypesAndPropsFromProperty(schemaTypes, properties, property)
			}
		}
	}
}

func retrieveNestedProperties(subMap map[string]interface{}, yamlPath string, schemaTypes map[string]*SchemaType, properties map[string]*Property, errMap map[string]error) {
	var schemaPropertiesMap map[string]interface{}

	propertiesObj, isPropertyPresent := subMap[OPENAPI_YAML_KEYWORD_PROPERTIES]
	if isPropertyPresent {
		schemaPropertiesMap = propertiesObj.(map[string]interface{})
		retrieveSchemaComponentsFromMap(schemaPropertiesMap, yamlPath, schemaTypes, properties, errMap)
	}
}

func retrieveVarType(variableMap map[string]interface{}, apiSchemaPartPath string) (varType string, cardinality Cardinality, err error) {
	maxCardinality := 0
	varTypeObj, isTypePresent := variableMap[OPENAPI_YAML_KEYWORD_TYPE]
	refObj, isRefPresent := variableMap[OPENAPI_YAML_KEYWORD_REF]
	_, isAllOfPresent := variableMap[OPENAPI_YAML_KEYWORD_ALLOF]
	_, isOneOfPresent := variableMap[OPENAPI_YAML_KEYWORD_ONEOF]
	_, isAnyOfPresent := variableMap[OPENAPI_YAML_KEYWORD_ANYOF]

	if isTypePresent {
		varType = varTypeObj.(string)
		if varType == "array" {
			varType, err = retrieveArrayType(variableMap, apiSchemaPartPath)
			maxCardinality = MAX_ARRAY_CAPACITY * arrayDimensions
		} else {
			maxCardinality = 1
		}
		cardinality = Cardinality{min: 0, max: maxCardinality}
	} else if isRefPresent {
		varType = "$ref:" + refObj.(string)
	} else if isAllOfPresent {
		err = openapi2beans_errors.NewError("RetrieveVarType: illegal allOf part found in %v\n", apiSchemaPartPath)
	} else if isOneOfPresent {
		err = openapi2beans_errors.NewError("RetrieveVarType: illegal oneOf part found in %v\n", apiSchemaPartPath)
	} else if isAnyOfPresent {

	} else {
		err = openapi2beans_errors.NewError("RetrieveVarType: Failed to find required type for %v\n", apiSchemaPartPath)
	}

	return varType, cardinality, err
}

func retrieveArrayType(varMap map[string]interface{}, schemaPartPath string) (arrayType string, err error) {
	arrayDimensions += 1
	itemsObj, isItemsPresent := varMap[OPENAPI_YAML_KEYWORD_ITEMS]
	if isItemsPresent {
		itemsMap := itemsObj.(map[string]interface{})
		arrayType, _, err = retrieveVarType(itemsMap, schemaPartPath)

	} else {
		err = openapi2beans_errors.NewError("RetrieveArrayType: Failed to find required items section for %v\n", schemaPartPath)
	}

	return arrayType, err
}

func retrieveDescription(subMap map[string]interface{}) (description string) {
	descriptionObj, isDescriptionPresent := subMap[OPENAPI_YAML_KEYWORD_DESCRIPTION]
	if isDescriptionPresent {
		description = descriptionObj.(string)
	}
	return description
}

func resolvePropertiesMinCardinalities(schemaTypeMap map[string]interface{}, schemaTypeProps map[string]*Property, schemaTypePath string) {
	requiredMapObj, isRequiredPresent := schemaTypeMap[OPENAPI_YAML_KEYWORD_REQUIRED]
	if isRequiredPresent {
		requiredMap := requiredMapObj.([]interface{})
		for _, required := range requiredMap {
			property, isPropertyNamePresent := schemaTypeProps[schemaTypePath+filepathSeparator+required.(string)]
			if isPropertyNamePresent {
				property.cardinality.min = 1
			}
		}
	}
}

func retrievePossibleValues(varMap map[string]interface{}) (possibleValues map[string]string) {
	possibleValues = make(map[string]string)
	enumObj, isEnumPresent := varMap[OPENAPI_YAML_KEYWORD_ENUM]
	if isEnumPresent {
		enums := enumObj.([]interface{})
		for _, enum := range enums {
			enumName := enum.(string)
			possibleValues[enumName] = enumName
		}
	}
	return possibleValues
}

func assignPropertyToSchemaType(parentPath string, apiSchemaPartPath string, property *Property, schemaTypes map[string]*SchemaType) {
	schemaType, isPropertyPartOfSchemaType := schemaTypes[parentPath]
	if isPropertyPartOfSchemaType {
		schemaType.properties[apiSchemaPartPath] = property
	}
}

func removeSchemaTypeAndProps(schemaTypes map[string]*SchemaType, properties map[string]*Property, schemaPath string) {
	for propPath := range schemaTypes[schemaPath].properties {
		delete(properties, propPath)
	}
	delete(properties, schemaTypes[schemaPath].ownProperty.path)
	delete(schemaTypes, schemaPath)
}

func schemaTypeErrored(schemaType *SchemaType, errMap map[string]error) bool {
	errors := false
	if schemaType != nil {
		for errPath := range errMap {
			if strings.Contains(errPath, schemaType.ownProperty.path) {
				errors = true
				break
			}
		}
	}
	return errors
}

func resolveNestedObjectName(objectName string, parentPath string) string {
	nameComponents := strings.Split(parentPath, filepathSeparator)[3:]
	newName := ""
	for _, element := range nameComponents {
		newName += element
	}
	newName += utils.StringToPascal(objectName)
	return newName
}

func removeSchemaTypesAndPropsFromProperty(schemaTypes map[string]*SchemaType, properties map[string]*Property, property *Property) {
	for schemaPath, schemaType := range schemaTypes {
		_, propExists := schemaType.properties[property.path]
		if propExists {
			removeSchemaTypeAndProps(schemaTypes, properties, schemaPath)
		}
	}
}
