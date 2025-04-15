/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package propertiesformatter

import (
	"strings"

	"github.com/galasa-dev/cli/pkg/galasaapi"
)

//Print in the following fashion:
//PROPERTIES
// namespace	name	    value
// framework	property1	value1
// framework	property2	value2
// Total:2
//NAMESPACES
// namespace	type
// framework	normal
// secure       secure
// Total:2

// -----------------------------------------------------
// PropertyFormatter - implementations can take a collection of properties/namespaces results
// and turn them into a string for display to the user.
const (
	//properties display
	HEADER_PROPERTY_NAMESPACE = "namespace"
	HEADER_PROPERTY_NAME      = "name"
	HEADER_PROPERTY_VALUE     = "value"

	//namespaces display
	HEADER_NAMESPACE      = "namespace"
	HEADER_NAMESPACE_TYPE = "type"

	PROPERTY_VALUE_MAX_VISIBLE_LENGTH = 60
)

type PropertyFormatter interface {
	FormatProperties(propertyResults []galasaapi.GalasaProperty) (string, error)
	FormatNamespaces(namespaces []galasaapi.Namespace) (string, error)
	GetName() string
}

func substituteNewLines(originalPropValue string) string {
	newLinesReplacedValue := strings.Replace(originalPropValue, "\n", "\\n", -1)
	return newLinesReplacedValue
}

func cropExtraLongValue(originalPropValue string) string {
	croppedValue := originalPropValue
	if len(originalPropValue) > PROPERTY_VALUE_MAX_VISIBLE_LENGTH {
		croppedValue = originalPropValue[:PROPERTY_VALUE_MAX_VISIBLE_LENGTH] + `...(cropped)`
	}
	return croppedValue
}
