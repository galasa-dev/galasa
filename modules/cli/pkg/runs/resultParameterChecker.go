/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"context"
	"log"
	"net/http"
	"sort"
	"strconv"
	"strings"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
)

func ValidateResultParameter(resultInputString string, commsClient api.APICommsClient) (string, error) {
	var err error
	var validResultInputs []string
	var invalidResultInputs []string
	var resultQuery string = ""
	var restApiVersion string

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	if err == nil {

		err = commsClient.RunAuthenticatedCommandWithRateLimitRetries(func(apiClient *galasaapi.APIClient) error {
			var err error
			var context context.Context = nil
			var rasResultNamesData *galasaapi.ResultNames
			var httpResponse *http.Response
	
			rasResultNamesData, httpResponse, err = apiClient.ResultArchiveStoreAPIApi.GetRasResultNames(context).ClientApiVersion(restApiVersion).Execute()
	
			var statusCode int
			if httpResponse != nil {
				defer httpResponse.Body.Close()
				statusCode = httpResponse.StatusCode
			}
	
			if err == nil {
				if statusCode != http.StatusOK {
					httpError := "\nhttp response status code: " + strconv.Itoa(statusCode)
					errString := httpError
					err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_QUERY_RESULTNAMES_FAILED, errString)
				} else {
					rasResultNames := rasResultNamesData.GetResultnames()
					log.Println("List of valid result names from the ecosystem: " + covertArrayToCommaSeparatedStringWithQuotes(rasResultNames))
					resultInputs := strings.Split(resultInputString, ",")
	
					validResultNamesMap := make(map[string]string)
					for _, resultName := range rasResultNames {
						validResultNamesMap[strings.ToLower(resultName)] = resultName
					}
	
					for _, resultInput := range resultInputs {
						matched := false
						matchedResultNameValue := validResultNamesMap[strings.ToLower(resultInput)]
						if len(matchedResultNameValue) > 0 {
							validResultInputs = append(validResultInputs, matchedResultNameValue)
							matched = true
						}
						if !matched {
							invalidResultInputs = append(invalidResultInputs, resultInput)
						}
					}
	
					if len(invalidResultInputs) > 0 {
						err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_INVALID_RESULT_ARGUMENT, covertArrayToCommaSeparatedStringWithQuotes(invalidResultInputs), covertArrayToCommaSeparatedStringWithQuotes(rasResultNames))
					}
					if err == nil {
						resultQuery = strings.Join(validResultInputs[:], ",")
					}
				}
			} else {
				err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_QUERY_RESULTNAMES_FAILED, err.Error())
			}
			return err
		})
	}

	return resultQuery, err
}

func covertArrayToCommaSeparatedStringWithQuotes(array []string) string {

	sort.Strings(array)

	outputString := strings.Builder{}

	for count, element := range array {

		if count != 0 {
			outputString.WriteString(", ")
		}
		outputString.WriteString("'" + element + "'")
	}

	return outputString.String()
}
