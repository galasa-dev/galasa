/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package generator

import (
	"testing"

	"github.com/dev-galasa/buildutils/openapi2beans/pkg/utils"
	"github.com/stretchr/testify/assert"
)

func TestTranslateSchemaTypesToJavaPackageReturnsPackageWithJavaClass(t *testing.T) {
	// Given...
	var schemaType *SchemaType
	name := "MyBean"
	ownProp := NewProperty(name, "#/components/schemas/MyBean", "", "object", nil, schemaType, Cardinality{min: 0, max: 1})
	schemaType = NewSchemaType(name, "", ownProp, nil)
	schemaTypeMap := make(map[string]*SchemaType)
	schemaTypeMap["#/components/schemas/MyBean"] = schemaType

	// When...
	javaPackage := translateSchemaTypesToJavaPackage(schemaTypeMap, TARGET_JAVA_PACKAGE)

	// Then...
	assert.Equal(t, "MyBean", javaPackage.Classes["MyBean"].Name)
}

func TestTranslateSchemaTypesToJavaPackageReturnsPackageWithJavaClassWithDescription(t *testing.T) {
	// Given...
	var schemaType *SchemaType
	name := "MyBean"
	ownProp := NewProperty(name, "#/components/schemas/MyBean", "", "object", nil, schemaType, Cardinality{min: 0, max: 1})
	schemaType = NewSchemaType(name, "a lil description", ownProp, nil)
	schemaTypeMap := make(map[string]*SchemaType)
	schemaTypeMap["#/components/schemas/MyBean"] = schemaType

	// When...
	javaPackage := translateSchemaTypesToJavaPackage(schemaTypeMap, TARGET_JAVA_PACKAGE)

	// Then...
	assert.Equal(t, "MyBean", javaPackage.Classes["MyBean"].Name)
	assert.Contains(t, javaPackage.Classes[name].Description, "a lil description")
}

func TestTranslateSchemaTypesToJavaPackageWithClassWithDataMember(t *testing.T) {
	// Given...
	propName1 := "myRandomProperty"
	property := NewProperty(propName1, "#/components/schemas/MyBean/"+propName1, "", "string", nil, nil, Cardinality{min: 0, max: 1})
	properties := make(map[string]*Property)
	properties["#/components/schemas/MyBean/"+propName1] = property
	var schemaType *SchemaType
	schemaName := "MyBean"
	ownProp := NewProperty(schemaName, "#/components/schemas/MyBean", "", "object", nil, schemaType, Cardinality{min: 0, max: 1})
	schemaType = NewSchemaType(schemaName, "", ownProp, properties)
	schemaTypeMap := make(map[string]*SchemaType)
	schemaTypeMap["#/components/schemas/MyBean"] = schemaType

	// When...
	javaPackage := translateSchemaTypesToJavaPackage(schemaTypeMap, TARGET_JAVA_PACKAGE)

	// Then...
	class, classExists := javaPackage.Classes[schemaName]
	assert.True(t, classExists)
	assert.Equal(t, "MyBean", class.Name)
	assert.Equal(t, "myRandomProperty", class.DataMembers[0].Name)
	assert.Equal(t, "MyRandomProperty", class.DataMembers[0].PascalCaseName)
	assert.Equal(t, "String", class.DataMembers[0].MemberType)
	assert.Equal(t, []string([]string(nil)), class.DataMembers[0].Description)
	assert.Equal(t, "", class.DataMembers[0].ConstantVal)
}

func TestTranslateSchemaTypesToJavaPackageWithClassWithDataMemberWithSnakeCaseNameHasSerializedNameOverride(t *testing.T) {
	// Given...
	propName1 := "my_random_property"
	property := NewProperty(propName1, "#/components/schemas/MyBean/"+propName1, "", "string", nil, nil, Cardinality{min: 0, max: 1})
	properties := make(map[string]*Property)
	properties["#/components/schemas/MyBean/"+propName1] = property
	var schemaType *SchemaType
	schemaName := "MyBean"
	ownProp := NewProperty(schemaName, "#/components/schemas/MyBean", "", "object", nil, schemaType, Cardinality{min: 0, max: 1})
	schemaType = NewSchemaType(schemaName, "", ownProp, properties)
	schemaTypeMap := make(map[string]*SchemaType)
	schemaTypeMap["#/components/schemas/MyBean"] = schemaType

	// When...
	javaPackage := translateSchemaTypesToJavaPackage(schemaTypeMap, TARGET_JAVA_PACKAGE)

	// Then...
	class, classExists := javaPackage.Classes[schemaName]
	assert.True(t, classExists)
	assert.Equal(t, "MyBean", class.Name)
	assert.Equal(t, "myRandomProperty", class.DataMembers[0].Name)
	assert.Equal(t, "MyRandomProperty", class.DataMembers[0].PascalCaseName)
	assert.Equal(t, "my_random_property", class.DataMembers[0].SerializedNameOverride)
	assert.Equal(t, "String", class.DataMembers[0].MemberType)
	assert.Equal(t, []string([]string(nil)), class.DataMembers[0].Description)
	assert.Equal(t, "", class.DataMembers[0].ConstantVal)
}

func TestTranslateSchemaTypesToJavaPackageWithClassWithDataMemberWithoutSnakeCaseNameHasNotGotSerializedNameOverride(t *testing.T) {
	// Given...
	propName1 := "myRandomProperty"
	property := NewProperty(propName1, "#/components/schemas/MyBean/"+propName1, "", "string", nil, nil, Cardinality{min: 0, max: 1})
	properties := make(map[string]*Property)
	properties["#/components/schemas/MyBean/"+propName1] = property
	var schemaType *SchemaType
	schemaName := "MyBean"
	ownProp := NewProperty(schemaName, "#/components/schemas/MyBean", "", "object", nil, schemaType, Cardinality{min: 0, max: 1})
	schemaType = NewSchemaType(schemaName, "", ownProp, properties)
	schemaTypeMap := make(map[string]*SchemaType)
	schemaTypeMap["#/components/schemas/MyBean"] = schemaType

	// When...
	javaPackage := translateSchemaTypesToJavaPackage(schemaTypeMap, TARGET_JAVA_PACKAGE)

	// Then...
	class, classExists := javaPackage.Classes[schemaName]
	assert.True(t, classExists)
	assert.Equal(t, "MyBean", class.Name)
	assert.Equal(t, "myRandomProperty", class.DataMembers[0].Name)
	assert.Equal(t, "MyRandomProperty", class.DataMembers[0].PascalCaseName)
	assert.Empty(t, class.DataMembers[0].SerializedNameOverride)
	assert.Equal(t, "String", class.DataMembers[0].MemberType)
	assert.Equal(t, []string([]string(nil)), class.DataMembers[0].Description)
	assert.Equal(t, "", class.DataMembers[0].ConstantVal)
}

func TestTranslateSchemaTypesToJavaPackageWithClassWithMultipleDataMembers(t *testing.T) {
	// Given...
	propName1 := "myRandomProperty1"
	property1 := NewProperty(propName1, "#/components/schemas/MyBean/"+propName1, "", "string", nil, nil, Cardinality{min: 0, max: 1})
	properties := make(map[string]*Property)
	properties["#/components/schemas/MyBean/"+propName1] = property1
	propName2 := "myRandomProperty2"
	property2 := NewProperty(propName2, "#/components/schemas/MyBean/"+propName2, "", "string", nil, nil, Cardinality{min: 0, max: 1})
	properties["#/components/schemas/MyBean/"+propName2] = property2
	var schemaType *SchemaType
	schemaName := "MyBean"
	ownProp := NewProperty(schemaName, "#/components/schemas/MyBean", "", "object", nil, schemaType, Cardinality{min: 0, max: 1})
	schemaType = NewSchemaType(schemaName, "", ownProp, properties)
	schemaTypeMap := make(map[string]*SchemaType)
	schemaTypeMap["#/components/schemas/MyBean"] = schemaType

	// When...
	javaPackage := translateSchemaTypesToJavaPackage(schemaTypeMap, TARGET_JAVA_PACKAGE)

	// Then...
	class, classExists := javaPackage.Classes[schemaName]
	assert.True(t, classExists)
	assert.Equal(t, "MyBean", class.Name)
	assert.Equal(t, "myRandomProperty1", class.DataMembers[1].Name)
	assert.Equal(t, "MyRandomProperty1", class.DataMembers[1].PascalCaseName)
	assert.Equal(t, "String", class.DataMembers[1].MemberType)
	assert.Equal(t, []string([]string(nil)), class.DataMembers[1].Description)
	assert.Equal(t, "", class.DataMembers[1].ConstantVal)
	assert.Equal(t, "myRandomProperty2", class.DataMembers[0].Name)
	assert.Equal(t, "MyRandomProperty2", class.DataMembers[0].PascalCaseName)
	assert.Equal(t, "String", class.DataMembers[0].MemberType)
	assert.Equal(t, []string([]string(nil)), class.DataMembers[0].Description)
	assert.Equal(t, "", class.DataMembers[0].ConstantVal)
}

func TestTranslateSchemaTypesToJavaPackageWithClassWithArrayDataMember(t *testing.T) {
	// Given...
	propName1 := "myRandomProperty1"
	property1 := NewProperty(propName1, "#/components/schemas/MyBean/"+propName1, "", "string", nil, nil, Cardinality{min: 0, max: MAX_ARRAY_CAPACITY})
	properties := make(map[string]*Property)
	properties["#/components/schemas/MyBean/"+propName1] = property1
	var schemaType *SchemaType
	schemaName := "MyBean"
	ownProp := NewProperty(schemaName, "#/components/schemas/MyBean", "", "object", nil, schemaType, Cardinality{min: 0, max: 1})
	schemaType = NewSchemaType(schemaName, "", ownProp, properties)
	schemaTypeMap := make(map[string]*SchemaType)
	schemaTypeMap["#/components/schemas/MyBean"] = schemaType

	// When...
	javaPackage := translateSchemaTypesToJavaPackage(schemaTypeMap, TARGET_JAVA_PACKAGE)

	// Then...
	class, classExists := javaPackage.Classes[schemaName]
	assert.True(t, classExists)
	assert.Equal(t, "MyBean", class.Name)
	assert.Equal(t, "myRandomProperty1", class.DataMembers[0].Name)
	assert.Equal(t, "MyRandomProperty1", class.DataMembers[0].PascalCaseName)
	assert.Equal(t, "String[]", class.DataMembers[0].MemberType)
	assert.Equal(t, []string([]string(nil)), class.DataMembers[0].Description)
	assert.Equal(t, "", class.DataMembers[0].ConstantVal)
}

func TestTranslateSchemaTypesToJavaPackageWithClassWithMixedArrayAndPrimitiveDataMembers(t *testing.T) {
	// Given...
	propName1 := "myRandomProperty1"
	property1 := NewProperty(propName1, "#/components/schemas/MyBean/"+propName1, "", "string", nil, nil, Cardinality{min: 0, max: MAX_ARRAY_CAPACITY})
	properties := make(map[string]*Property)
	properties["#/components/schemas/MyBean/"+propName1] = property1
	propName2 := "myRandomProperty2"
	property2 := NewProperty(propName2, "#/components/schemas/MyBean/"+propName2, "", "string", nil, nil, Cardinality{min: 0, max: 1})
	properties["#/components/schemas/MyBean/"+propName2] = property2
	var schemaType *SchemaType
	schemaName := "MyBean"
	ownProp := NewProperty(schemaName, "#/components/schemas/MyBean", "", "object", nil, schemaType, Cardinality{min: 0, max: 1})
	schemaType = NewSchemaType(schemaName, "", ownProp, properties)
	schemaTypeMap := make(map[string]*SchemaType)
	schemaTypeMap["#/components/schemas/MyBean"] = schemaType

	// When...
	javaPackage := translateSchemaTypesToJavaPackage(schemaTypeMap, TARGET_JAVA_PACKAGE)

	// Then...
	class, classExists := javaPackage.Classes[schemaName]
	assert.True(t, classExists)
	assert.Equal(t, "MyBean", class.Name)
	assert.Equal(t, "myRandomProperty2", class.DataMembers[0].Name)
	assert.Equal(t, "MyRandomProperty2", class.DataMembers[0].PascalCaseName)
	assert.Equal(t, "String", class.DataMembers[0].MemberType)
	assert.Equal(t, []string([]string(nil)), class.DataMembers[0].Description)
	assert.Equal(t, "", class.DataMembers[0].ConstantVal)
	assert.Equal(t, "myRandomProperty1", class.DataMembers[1].Name)
	assert.Equal(t, "MyRandomProperty1", class.DataMembers[1].PascalCaseName)
	assert.Equal(t, "String[]", class.DataMembers[1].MemberType)
	assert.Equal(t, []string([]string(nil)), class.DataMembers[1].Description)
	assert.Equal(t, "", class.DataMembers[1].ConstantVal)
}

func TestTranslateSchemaTypesToJavaPackageWithClassWithArrayOfArray(t *testing.T) {
	// Given...
	propName1 := "myRandomProperty1"
	property1 := NewProperty(propName1, "#/components/schemas/MyBean/"+propName1, "", "string", nil, nil, Cardinality{min: 0, max: MAX_ARRAY_CAPACITY*2})
	properties := make(map[string]*Property)
	properties["#/components/schemas/MyBean/"+propName1] = property1
	var schemaType *SchemaType
	schemaName := "MyBean"
	ownProp := NewProperty(schemaName, "#/components/schemas/MyBean", "", "object", nil, schemaType, Cardinality{min: 0, max: 1})
	schemaType = NewSchemaType(schemaName, "", ownProp, properties)
	schemaTypeMap := make(map[string]*SchemaType)
	schemaTypeMap["#/components/schemas/MyBean"] = schemaType

	// When...
	javaPackage := translateSchemaTypesToJavaPackage(schemaTypeMap, TARGET_JAVA_PACKAGE)

	// Then...
	class, classExists := javaPackage.Classes[schemaName]
	assert.True(t, classExists)
	assert.Equal(t, "MyBean", class.Name)
	assert.Equal(t, "myRandomProperty1", class.DataMembers[0].Name)
	assert.Equal(t, "MyRandomProperty1", class.DataMembers[0].PascalCaseName)
	assert.Equal(t, "String[][]", class.DataMembers[0].MemberType)
	assert.Equal(t, []string([]string(nil)), class.DataMembers[0].Description)
	assert.Equal(t, "", class.DataMembers[0].ConstantVal)
}

func TestTranslateSchemaTypesToJavaPackageWithClassWithReferenceToOtherClass(t *testing.T) {
	// Given...
	schemaTypeMap := make(map[string]*SchemaType)
	var referencedSchemaType *SchemaType
	referencedSchemaName := "MyReferencedBean"
	referencedOwnProp := NewProperty(referencedSchemaName, "#/components/schemas/MyReferencedBean", "", "object", nil, referencedSchemaType, Cardinality{min: 0, max: 1})
	referencedSchemaType = NewSchemaType(referencedSchemaName, "", referencedOwnProp, nil)
	schemaTypeMap["#/components/schemas/MyReferencedBean"] = referencedSchemaType
	propName1 := "myReferencingProp"
	property1 := NewProperty(propName1, "#/components/schemas/MyBean/"+propName1, "", "object", nil, referencedSchemaType, Cardinality{min: 0, max: 1})
	properties := make(map[string]*Property)
	properties["#/components/schemas/MyBean/"+propName1] = property1
	var schemaType *SchemaType
	schemaName := "MyBean"
	ownProp := NewProperty(schemaName, "#/components/schemas/MyBean", "", "object", nil, schemaType, Cardinality{min: 0, max: 1})
	schemaType = NewSchemaType(schemaName, "", ownProp, properties)
	schemaTypeMap["#/components/schemas/MyBean"] = schemaType

	// When...
	javaPackage := translateSchemaTypesToJavaPackage(schemaTypeMap, TARGET_JAVA_PACKAGE)

	// Then...
	class, classExists := javaPackage.Classes[schemaName]
	assert.True(t, classExists)
	assert.Equal(t, "MyBean", class.Name)
	assert.Equal(t, "myReferencingProp", class.DataMembers[0].Name)
	assert.Equal(t, "MyReferencingProp", class.DataMembers[0].PascalCaseName)
	assert.Equal(t, "MyReferencedBean", class.DataMembers[0].MemberType)
	assert.Equal(t, []string([]string(nil)), class.DataMembers[0].Description)
	assert.Equal(t, "", class.DataMembers[0].ConstantVal)
	class, classExists = javaPackage.Classes[referencedSchemaName]
	assert.True(t, classExists)
	assert.Equal(t, "MyReferencedBean", class.Name)
}

func TestTranslateSchemaTypesToJavaPackageWithClassWithArrayOfReferenceToClass(t *testing.T) {
	// Given...
	schemaTypeMap := make(map[string]*SchemaType)
	var referencedSchemaType *SchemaType
	referencedSchemaName := "MyReferencedBean"
	referencedOwnProp := NewProperty(referencedSchemaName, "#/components/schemas/MyReferencedBean", "", "object", nil, referencedSchemaType, Cardinality{min: 0, max: 1})
	referencedSchemaType = NewSchemaType(referencedSchemaName, "", referencedOwnProp, nil)
	schemaTypeMap["#/components/schemas/MyReferencedBean"] = referencedSchemaType
	propName1 := "myRandomProperty1"
	property1 := NewProperty(propName1, "#/components/schemas/MyBean/"+propName1, "", "object", nil, referencedSchemaType, Cardinality{min: 0, max: MAX_ARRAY_CAPACITY})
	properties := make(map[string]*Property)
	properties["#/components/schemas/MyBean/"+propName1] = property1
	var schemaType *SchemaType
	schemaName := "MyBean"
	ownProp := NewProperty(schemaName, "#/components/schemas/MyBean", "", "object", nil, schemaType, Cardinality{min: 0, max: 1})
	schemaType = NewSchemaType(schemaName, "", ownProp, properties)
	schemaTypeMap["#/components/schemas/MyBean"] = schemaType

	// When...
	javaPackage := translateSchemaTypesToJavaPackage(schemaTypeMap, TARGET_JAVA_PACKAGE)

	// Then...
	class, classExists := javaPackage.Classes[schemaName]
	assert.True(t, classExists)
	assert.Equal(t, "MyBean", class.Name)
	assert.Equal(t, "myRandomProperty1", class.DataMembers[0].Name)
	assert.Equal(t, "MyRandomProperty1", class.DataMembers[0].PascalCaseName)
	assert.Equal(t, "MyReferencedBean[]", class.DataMembers[0].MemberType)
	assert.Equal(t, []string([]string(nil)), class.DataMembers[0].Description)
	assert.Equal(t, "", class.DataMembers[0].ConstantVal)
	class, classExists = javaPackage.Classes[referencedSchemaName]
	assert.True(t, classExists)
	assert.Equal(t, "MyReferencedBean", class.Name)
}

func TestTranslateSchemaTypesToJavaPackageWithClassWithRequiredProperty(t *testing.T) {
	// Given...
	propName1 := "myRandomProperty1"
	property1 := NewProperty(propName1, "#/components/schemas/MyBean/"+propName1, "", "string", nil, nil, Cardinality{min: 1, max: 1})
	properties := make(map[string]*Property)
	properties["#/components/schemas/MyBean/"+propName1] = property1
	schemaTypeMap := make(map[string]*SchemaType)
	var schemaType *SchemaType
	schemaName := "MyBean"
	ownProp := NewProperty(schemaName, "#/components/schemas/MyBean", "", "object", nil, schemaType, Cardinality{min: 0, max: 1})
	schemaType = NewSchemaType(schemaName, "", ownProp, properties)
	schemaTypeMap["#/components/schemas/MyBean"] = schemaType

	// When...
	javaPackage := translateSchemaTypesToJavaPackage(schemaTypeMap, TARGET_JAVA_PACKAGE)

	// Then...
	class, classExists := javaPackage.Classes[schemaName]
	assert.True(t, classExists)
	assert.Equal(t, "MyBean", class.Name)
	assert.Equal(t, "myRandomProperty1", class.DataMembers[0].Name)
	assert.Equal(t, "MyRandomProperty1", class.DataMembers[0].PascalCaseName)
	assert.Equal(t, "String", class.DataMembers[0].MemberType)
	assert.Equal(t, []string([]string(nil)), class.DataMembers[0].Description)
	assert.Equal(t, "", class.DataMembers[0].ConstantVal)
	assert.Equal(t, "myRandomProperty1", class.RequiredMembers[0].DataMember.Name)
	assert.Equal(t, "MyRandomProperty1", class.RequiredMembers[0].DataMember.PascalCaseName)
	assert.Equal(t, "String", class.RequiredMembers[0].DataMember.MemberType)
	assert.Equal(t, []string([]string(nil)), class.RequiredMembers[0].DataMember.Description)
	assert.Equal(t, "", class.RequiredMembers[0].DataMember.ConstantVal)
}

func TestTranslateSchemaTypesToJavaPackageWithEnum(t *testing.T) {
	// Given...
	possibleValues := map[string]string{
		"randValue1": "randValue1",
		"randValue2": "randValue2",
	}
	schemaTypeMap := make(map[string]*SchemaType)
	var schemaType *SchemaType
	schemaName := "myEnum"
	ownProp := NewProperty(schemaName, "#/components/schemas/myEnum", "", "string", possibleValues, schemaType, Cardinality{min: 0, max: 1})
	schemaType = NewSchemaType(schemaName, "test enum description", ownProp, nil)
	schemaTypeMap["#/components/schemas/myEnum"] = schemaType

	// When...
	javaPackage := translateSchemaTypesToJavaPackage(schemaTypeMap, TARGET_JAVA_PACKAGE)

	// Then...
	enum, enumExists := javaPackage.Enums[utils.StringToPascal(schemaName)]
	assert.True(t, enumExists)
	assert.Equal(t, "MyEnum", enum.Name)
	assert.Equal(t, []string([]string{"test enum description"}), enum.Description)
	assert.Equal(t, "randValue1", enum.EnumValues[0].StringFormat)
	assert.Equal(t, "RAND_VALUE_1", enum.EnumValues[0].ConstFormatName)
	assert.Equal(t, "randValue2", enum.EnumValues[1].StringFormat)
	assert.Equal(t, "RAND_VALUE_2", enum.EnumValues[1].ConstFormatName)
}

func TestTranslateSchemaTypesToJavaPackageWithClassWithEnum(t *testing.T) {
	// Given...
	possibleValues := map[string]string{
		"randValue1": "randValue1",
		"randValue2": "randValue2",
	}
	schemaTypeMap := make(map[string]*SchemaType)
	var enumSchemaType *SchemaType
	enumSchemaName := "MyEnum"
	enumOwnProp := NewProperty(enumSchemaName, SCHEMAS_PATH+enumSchemaName, "", "string", possibleValues, enumSchemaType, Cardinality{min: 0, max: 1})
	enumSchemaType = NewSchemaType(enumSchemaName, "", enumOwnProp, nil)
	schemaTypeMap["#/components/schemas/MyEnum"] = enumSchemaType
	var classSchemaType *SchemaType
	classSchemaName := "MyBean"
	enumPropName := "beansEnum"
	propMap := make(map[string]*Property)
	enumProp := NewProperty(enumPropName, SCHEMAS_PATH+classSchemaName+"/"+enumPropName, "", enumSchemaName, possibleValues, enumSchemaType, enumOwnProp.cardinality)
	propMap["#/components/schemas/MyBean/beansEnum"] = enumProp
	classOwnProp := NewProperty(classSchemaName, SCHEMAS_PATH+classSchemaName, "", classSchemaName, nil, classSchemaType, Cardinality{min: 0, max: 1})
	classSchemaType = NewSchemaType(classSchemaName, "", classOwnProp, propMap)
	schemaTypeMap[SCHEMAS_PATH+classSchemaName] = classSchemaType

	// When...
	javaPackage := translateSchemaTypesToJavaPackage(schemaTypeMap, TARGET_JAVA_PACKAGE)

	// Then...
	enum, enumExists := javaPackage.Enums[enumSchemaName]
	assert.True(t, enumExists)
	assert.Equal(t, "MyEnum", enum.Name)
	assert.Equal(t, "randValue1", enum.EnumValues[0].StringFormat)
	assert.Equal(t, "RAND_VALUE_1", enum.EnumValues[0].ConstFormatName)
	assert.Equal(t, "randValue2", enum.EnumValues[1].StringFormat)
	assert.Equal(t, "RAND_VALUE_2", enum.EnumValues[1].ConstFormatName)

	class, classExists := javaPackage.Classes[classSchemaName]
	assert.True(t, classExists)
	assert.Equal(t, "MyBean", class.Name)
	assert.Equal(t, "beansEnum", class.DataMembers[0].Name)
	assert.Equal(t, "BeansEnum", class.DataMembers[0].PascalCaseName)
	assert.Equal(t, "MyEnum", class.DataMembers[0].MemberType)
	assert.Equal(t, []string([]string(nil)), class.DataMembers[0].Description)
	assert.Equal(t, "", class.DataMembers[0].ConstantVal)
}

func TestTranslateSchemaTypesToJavaPackageWithClassWithStringConstant(t *testing.T) {
	// Given...
	propName1 := "MyConstant"
	possibleValues := map[string]string{
		"constVal": "constVal",
	}
	property := NewProperty(propName1, "#/components/schemas/MyBean/"+propName1, "", "string", possibleValues, nil, Cardinality{min: 0, max: 1})
	properties := make(map[string]*Property)
	properties["#/components/schemas/MyBean/"+propName1] = property
	var schemaType *SchemaType
	schemaName := "MyBean"
	ownProp := NewProperty(schemaName, "#/components/schemas/MyBean", "", "object", nil, schemaType, Cardinality{min: 0, max: 1})
	schemaType = NewSchemaType(schemaName, "", ownProp, properties)
	schemaTypeMap := make(map[string]*SchemaType)
	schemaTypeMap["#/components/schemas/MyBean"] = schemaType

	// When...
	javaPackage := translateSchemaTypesToJavaPackage(schemaTypeMap, TARGET_JAVA_PACKAGE)

	// Then...
	class, classExists := javaPackage.Classes[schemaName]
	assert.True(t, classExists)
	assert.NotEmpty(t, class.ConstantDataMembers)
	assert.Equal(t, "MyBean", class.Name)
	assert.Equal(t, "MY_CONSTANT", class.ConstantDataMembers[0].Name)
	assert.Equal(t, "String", class.ConstantDataMembers[0].MemberType)
	assert.Equal(t, []string([]string(nil)), class.ConstantDataMembers[0].Description)
	assert.Equal(t, "\"constVal\"", class.ConstantDataMembers[0].ConstantVal)
}

