/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package generator

import (
	"sort"
	"strings"

	"github.com/dev-galasa/buildutils/openapi2beans/pkg/utils"
)

type JavaPackage struct {
	Name    string
	Classes map[string]*JavaClass
	Enums   map[string]*JavaEnum
}

func NewJavaPackage(name string) *JavaPackage {
	javaPackage := JavaPackage{
		Name:    name,
		Classes: make(map[string]*JavaClass),
		Enums:   make(map[string]*JavaEnum),
	}
	return &javaPackage
}

type JavaClass struct {
	Name                 string
	Description          []string
	JavaPackage          *JavaPackage
	DataMembers          []*DataMember
	RequiredMembers      []*RequiredMember
	ConstantDataMembers  []*DataMember
	HasSerializedNameVar bool
}

func NewJavaClass(name string, description string, javaPackage *JavaPackage, dataMembers []*DataMember, requiredMembers []*RequiredMember, constantDataMembers []*DataMember, hasSerializedNameVar bool) *JavaClass {
	javaClass := JavaClass{
		Name:                 name,
		Description:          SplitDescription(description),
		JavaPackage:          javaPackage,
		DataMembers:          dataMembers,
		RequiredMembers:      requiredMembers,
		ConstantDataMembers:  constantDataMembers,
		HasSerializedNameVar: hasSerializedNameVar,
	}
	javaClass.Sort()
	return &javaClass
}

func SplitDescription(description string) []string {
	splitDescription := strings.Split(description, "\n")
	if len(splitDescription) == 1 && splitDescription[0] == "" {
		splitDescription = nil
	} else if len(splitDescription) > 1 {
		splitDescription = splitDescription[:len(splitDescription)-2]
	}
	return splitDescription
}

// sorts DataMembers, RequiredMembers, and ConstantDataMembers.
// order is:
// boolean > int > double > String > other
func (class JavaClass) Sort() {
	sort.SliceStable(class.DataMembers, func(i int, j int) bool {
		return isDataMemberLessThanComparison(class.DataMembers[i], class.DataMembers[j])
	})
	sort.SliceStable(class.ConstantDataMembers, func(i int, j int) bool {
		return isDataMemberLessThanComparison(class.ConstantDataMembers[i], class.ConstantDataMembers[j])
	})
	if class.RequiredMembers != nil {
		class.RequiredMembers[0].IsFirst = false
	}
	sort.SliceStable(class.RequiredMembers, func(i int, j int) bool {
		return isDataMemberLessThanComparison(class.RequiredMembers[i].DataMember, class.RequiredMembers[j].DataMember)
	})
	if class.RequiredMembers != nil {
		for _, requiredMember := range class.RequiredMembers {
			requiredMember.IsFirst = false
		}
		class.RequiredMembers[0].IsFirst = true
	}
}

type DataMember struct {
	Name                   string
	PascalCaseName         string
	MemberType             string
	Description            []string
	ConstantVal            string
	SerializedNameOverride string
}

func NewDataMember(name string, memberType string, description string) *DataMember {
	var serializedOverrideName string
	if isSnakeCase(name) {
		serializedOverrideName = name
	}

	// If the name is kebab-case (eg: my-variable) then lets turn it into
	// snake-case (eg: my_variable) which we can then turn easily into the other
	// cases.
	name = strings.ReplaceAll(name, "-", "_")

	dataMember := DataMember{
		Name:                   utils.StringToCamel(name),
		PascalCaseName:         utils.StringToPascal(name),
		MemberType:             memberType,
		Description:            SplitDescription(description),
		SerializedNameOverride: serializedOverrideName,
	}
	return &dataMember
}

func isSnakeCase(name string) bool {
	var isSnakeCase bool = strings.Contains(name, "_") || strings.Contains(name, "-")
	return isSnakeCase
}

func (dataMember DataMember) IsConstant() bool {
	return dataMember.ConstantVal != ""
}

type RequiredMember struct {
	IsFirst    bool
	DataMember *DataMember
}

type JavaEnum struct {
	Name        string
	Description []string
	EnumValues  []EnumValue
	JavaPackage *JavaPackage
}

type EnumValue struct {
	ConstFormatName string
	StringFormat    string
	IsFinal         bool
}

func NewJavaEnum(name string, description string, enumValues []string, javaPackage *JavaPackage) *JavaEnum {
	javaEnum := JavaEnum{
		Name:        name,
		Description: SplitDescription(description),
		EnumValues:  stringArrayToEnumValues(enumValues),
		JavaPackage: javaPackage,
	}
	javaEnum.Sort()
	return &javaEnum
}

func (enum JavaEnum) Sort() {
	sort.SliceStable(enum.EnumValues, func(i int, j int) bool {
		return enum.EnumValues[i].ConstFormatName < enum.EnumValues[j].ConstFormatName
	})
	enum.EnumValues[len(enum.EnumValues)-1].IsFinal = true
}

func stringArrayToEnumValues(stringEnums []string) []EnumValue {
	var enumValues []EnumValue
	for _, value := range stringEnums {
		var constantFormatName string
		var stringFormat string
		if value != "nil" {
			constantFormatName = utils.StringToScreamingSnake(value)
			stringFormat = value
			enumValue := EnumValue{
				ConstFormatName: constantFormatName,
				StringFormat:    stringFormat,
			}

			enumValues = append(enumValues, enumValue)
		}
	}
	return enumValues
}

// function used for sorting; groups variables by type and then alphabetically
// order of variables is:
// boolean > int > double > String > other
func isDataMemberLessThanComparison(dataMember *DataMember, comparisonMember *DataMember) bool {
	less := true
	switch memberType := dataMember.MemberType; {
	case strings.Contains(memberType, "boolean"):
		switch comparisonMemberTpye := comparisonMember.MemberType; {
		case strings.Contains(comparisonMemberTpye, "boolean"):
			less = dataMember.Name > comparisonMember.Name
		default:
			less = true
		}
	case strings.Contains(memberType, "int"):
		switch comparisonMember.MemberType {
		case "boolean":
			less = false
		case "int":
			less = dataMember.Name > comparisonMember.Name
		default:
			less = true
		}
	case strings.Contains(memberType, "double"):
		switch comparisonMemberType := comparisonMember.MemberType; {
		case strings.Contains(comparisonMemberType, "boolean"), strings.Contains(comparisonMemberType, "int"):
			less = false
		case strings.Contains(comparisonMemberType, "double"):
			less = dataMember.Name > comparisonMember.Name
		default:
			less = true
		}
	case strings.Contains(memberType, "String"):
		switch comparisonMemberType := comparisonMember.MemberType; {
		case strings.Contains(comparisonMemberType, "boolean"), strings.Contains(comparisonMemberType, "int"), strings.Contains(comparisonMemberType, "double"):
			less = false
		case strings.Contains(comparisonMemberType, "String"):
			less = dataMember.Name > comparisonMember.Name
		default:
			less = true
		}
	default:
		if dataMember.MemberType == comparisonMember.MemberType {
			less = dataMember.Name > comparisonMember.Name
		} else {
			less = dataMember.MemberType > comparisonMember.MemberType
		}
	}
	return less
}
