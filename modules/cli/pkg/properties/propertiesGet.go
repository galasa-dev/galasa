/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package properties

import (
	"context"
	"fmt"
	"log"
	"net/http"
	"sort"
	"strings"

	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/propertiesformatter"
	"github.com/galasa-dev/cli/pkg/spi"
)

var (
	propertiesHasYamlFormat = true
	validPropertyFormatters = CreateFormatters(propertiesHasYamlFormat)
)

// GetProperties - performs all the logic to implement the `galasactl properties get` command,
// but in a unit-testable manner.
func GetProperties(
	namespace string,
	name string,
	prefix string,
	suffix string,
	infix string,
	apiClient *galasaapi.APIClient,
	propertiesOutputFormat string,
	console spi.Console,
) error {
	var err error

	err = checkNameNotUsedWithPrefixSuffixInfix(name, prefix, suffix, infix)
	if err == nil {

		err = validateFlagStringFormatsForGet(namespace, name, prefix, suffix, infix)
		if err == nil {
			var chosenFormatter propertiesformatter.PropertyFormatter

			chosenFormatter, err = validateOutputFormatFlagValue(propertiesOutputFormat, validPropertyFormatters)
			if err == nil {
				var cpsProperty []galasaapi.GalasaProperty
				cpsProperty, err = getCpsPropertiesFromRestApi(namespace, name, prefix, suffix, infix, apiClient)

				log.Printf("GetProperties - Galasa Properties collected: %s", getCpsPropertyArrayAsString(cpsProperty))
				if err == nil {
					var outputText string

					outputText, err = chosenFormatter.FormatProperties(cpsProperty)

					if err == nil {
						console.WriteString(outputText)
					}

				}
			}
		}
	}
	return err
}

// Retrieves properties from the ecosystem API that match a given namespace.
// Multiple properties can be returned as the namespace is not unique.
func getCpsPropertiesFromRestApi(
	namespace string,
	name string,
	prefix string,
	suffix string,
	infix string,
	apiClient *galasaapi.APIClient,
) ([]galasaapi.GalasaProperty, error) {

	var err error
	var context context.Context = nil

	var restApiVersion string

	// // An HTTP client which can communicate with the api server in an ecosystem.
	// restClient := api.InitialiseAPI(apiServerUrl)

	var cpsProperties = make([]galasaapi.GalasaProperty, 0)

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	if err == nil {
		var resp *http.Response
		if name == "" {
			apicall := apiClient.ConfigurationPropertyStoreAPIApi.QueryCpsNamespaceProperties(context, namespace).ClientApiVersion(restApiVersion)
			if prefix != "" {
				apicall = apicall.Prefix(prefix)
			}
			if suffix != "" {
				apicall = apicall.Suffix(suffix)
			}
			if infix != "" {
				apicall = apicall.Infix(infix)
			}
			cpsProperties, resp, err = apicall.Execute()
		} else {
			apicall := apiClient.ConfigurationPropertyStoreAPIApi.GetCpsProperty(context, namespace, name).ClientApiVersion(restApiVersion)
			cpsProperties, resp, err = apicall.Execute()
		}

		var statusCode int
		if resp != nil {
			defer resp.Body.Close()
			statusCode = resp.StatusCode
		}

		if err != nil {
			err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_QUERY_NAMESPACE_FAILED, err.Error())
		}
	}

	return cpsProperties, err
}

func CreateFormatters(hasYamlFormat bool) map[string]propertiesformatter.PropertyFormatter {
	validFormatters := make(map[string]propertiesformatter.PropertyFormatter, 0)
	summaryFormatter := propertiesformatter.NewPropertySummaryFormatter()
	rawFormatter := propertiesformatter.NewPropertyRawFormatter()

	validFormatters[summaryFormatter.GetName()] = summaryFormatter
	validFormatters[rawFormatter.GetName()] = rawFormatter

	if hasYamlFormat {
		yamlFormatter := propertiesformatter.NewPropertyYamlFormatter()
		validFormatters[yamlFormatter.GetName()] = yamlFormatter
	}

	return validFormatters
}

// Ensures the user has provided a valid output format as part of the "runs get" command.
func validateOutputFormatFlagValue(propertiesOutputFormat string, validFormatters map[string]propertiesformatter.PropertyFormatter) (propertiesformatter.PropertyFormatter, error) {
	var err error

	chosenFormatter, isPresent := validFormatters[propertiesOutputFormat]

	if !isPresent {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_OUTPUT_FORMAT, propertiesOutputFormat, GetFormatterNamesString(validFormatters))
	}

	return chosenFormatter, err
}

// GetFormatterNamesString builds a string of comma separated, quoted formatter names
func GetFormatterNamesString(validFormatters map[string]propertiesformatter.PropertyFormatter) string {
	// extract names into a sorted slice
	names := make([]string, 0, len(validFormatters))
	for name := range validFormatters {
		names = append(names, name)
	}
	sort.Strings(names)

	// render list of sorted names into string
	formatterNames := strings.Builder{}

	for count, formatterName := range names {
		if count != 0 {
			formatterNames.WriteString(", ")
		}
		formatterNames.WriteString("'" + formatterName + "'")

	}
	return formatterNames.String()
}

func checkNameNotUsedWithPrefixSuffixInfix(name string, prefix string, suffix string, infix string) error {
	var err error
	if name != "" && (prefix != "" || suffix != "" || infix != "") {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_PROPERTIES_FLAG_COMBINATION)
	}
	return err
}

func getCpsPropertyArrayAsString(cpsPropertyArray []galasaapi.GalasaProperty) string {
	propertiesAsString := "["

	for propNumber, property := range cpsPropertyArray {
		propertiesAsString += fmt.Sprintf("{ApiVersion:'%s', Kind:'%s', Namespace:'%s', Name:'%s', Value:'%s'}",
			property.GetApiVersion(), property.GetKind(), property.Metadata.GetNamespace(), property.Metadata.GetName(), property.Data.GetValue())

		//if this is not the last property
		if propNumber != len(cpsPropertyArray)-1 {
			propertiesAsString += ", "
		}

	}

	propertiesAsString += "]"

	return propertiesAsString
}

func validateFlagStringFormatsForGet(namespace string, name string, prefix string, suffix string, infix string) error {
	var err error

	err = validateNamespaceFormat(namespace)
	if err == nil {
		if name != "" {
			err = validatePropertyFieldFormat(name, "name")
		} else {
			//prefix, suffix and infix could be provided exclusive of each other,
			//so we check if a value has been provided for each before validating the value
			if prefix != "" {
				err = validatePropertyFieldFormat(prefix, "prefix")
			}
			if suffix != "" {
				err = validatePropertyFieldFormat(suffix, "suffix")
			}
			if infix != "" {
				err = ValidateInfixes(infix)
			}
		}
	}

	return err
}

func ValidateInfixes(infix string) error {
	var err error
	//infix could have multiple values separated by a comma, or just a value
	infixElements := strings.Split(infix, ",")
	for _, infixElem := range infixElements {
		err = validatePropertyFieldFormat(infixElem, "infix")
		if err != nil {
			//as soon as an invalid value is found,
			//exit the for loop and return
			break
		}
	}
	return err
}
