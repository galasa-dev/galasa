/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package properties

import (
	"context"
	"io"
	"log"
	"net/http"

	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
)

var (
	APIVERSION = "galasa-dev/v1aplha1"
	KIND       = "GalasaProperty"
)

// SetProperty - performs all the logic to implement the `galasactl properties set` command,
// but in a unit-testable manner.
func SetProperty(
	namespace string,
	name string,
	value string,
	apiClient *galasaapi.APIClient,
) error {
	var err error
	var statusCode int

	err = validateInputsAreNotEmpty(namespace, name)
	if err == nil {
		err = validateNamespaceAndNameFlagStringFormats(namespace, name)
		if err == nil {
			galasaProperty := createGalasaProperty(namespace, name, value)

			log.Printf("SetProperty - Galasa Property to update/create: ApiVersion:'%s', Kind:'%s', Namespace:'%s', Name:'%s', Value:'%s'",
				galasaProperty.GetApiVersion(), galasaProperty.GetKind(), galasaProperty.Metadata.GetNamespace(), galasaProperty.Metadata.GetName(), galasaProperty.Data.GetValue())

			statusCode, err = updateCpsProperty(namespace, name, galasaProperty, apiClient)

			// if updateProperty() returns an error containing "404 Not Found" due to receiving a
			// GAL5017E from the api, we know the property does not exist and
			// so we assume the user wants to create a new property
			if (err != nil) && (statusCode == 404) {
				err = createCpsProperty(namespace, name, galasaProperty, apiClient)
			}
		}
	}

	return err
}

func updateCpsProperty(namespace string,
	name string,
	property *galasaapi.GalasaProperty,
	apiClient *galasaapi.APIClient,
) (int, error) {
	var err error
	var context context.Context = nil
	var responseBody []byte
	var restApiVersion string
	var resp *http.Response = nil

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	if err == nil {
		apicall := apiClient.ConfigurationPropertyStoreAPIApi.UpdateCpsProperty(context, namespace, name).GalasaProperty(*property).ClientApiVersion(restApiVersion)
		_, resp, err = apicall.Execute()
		if err != nil {
			log.Printf("updateCpsPtoperty Failed - Error: '%v'", err.Error())
			if resp != nil {
				defer resp.Body.Close()

				statusCode := resp.StatusCode
				if statusCode != http.StatusNotFound {

					responseBody, err = io.ReadAll(resp.Body)
					log.Printf("updateCpsPtoperty Failed - HTTP response - status code: '%v' payload: '%v' ", statusCode, string(responseBody))

					if err == nil {
						var errorFromServer *galasaErrors.GalasaAPIError
						errorFromServer, err = galasaErrors.GetApiErrorFromResponse(statusCode, responseBody)
						if err == nil {
							err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_PUT_PROPERTY_FAILED, name, errorFromServer.Message)
						} else {
							err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_PUT_PROPERTY_FAILED, name, err.Error())
						}
					}
				}
			}
		}
	}

	return resp.StatusCode, err
}

func createCpsProperty(namespace string,
	name string,
	property *galasaapi.GalasaProperty,
	apiClient *galasaapi.APIClient,
) error {
	var err error
	var context context.Context = nil
	var responseBody []byte
	var restApiVersion string
	var resp *http.Response = nil

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	if err == nil {
		apicall := apiClient.ConfigurationPropertyStoreAPIApi.CreateCpsProperty(context, namespace).GalasaProperty(*property).ClientApiVersion(restApiVersion)
		_, resp, err = apicall.Execute()
		statusCode := resp.StatusCode
		log.Printf("createCpsProperty - HTTP response status code: '%v'", statusCode)

		if err != nil {
			if resp != nil {
				defer resp.Body.Close()

				var errorFromServer *galasaErrors.GalasaAPIError
				responseBody, err = io.ReadAll(resp.Body)
				if err == nil {
					log.Printf("createCpsProperty Failed - HTTP response - status code: '%v' payload: '%v' ", statusCode, string(responseBody))
					errorFromServer, err = galasaErrors.GetApiErrorFromResponse(statusCode, responseBody)
					if err == nil {
						err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_POST_PROPERTY_FAILED, name, errorFromServer.Message)
					} else {
						err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_POST_PROPERTY_FAILED, name, err.Error())
					}
				} else {
					err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_POST_PROPERTY_FAILED, name, err.Error())
				}
			}
		}
	}

	return err
}

func createGalasaProperty(namespace string, name string, value string) *galasaapi.GalasaProperty {
	var property = galasaapi.NewGalasaProperty()

	property.SetApiVersion(APIVERSION)
	property.SetKind(KIND)

	metadata := galasaapi.NewGalasaPropertyMetadata()
	metadata.SetNamespace(namespace)
	metadata.SetName(name)
	property.SetMetadata(*metadata)

	data := galasaapi.NewGalasaPropertyData()
	data.SetValue(value)
	property.SetData(*data)

	return property
}

func validateNamespaceAndNameFlagStringFormats(namespace string, name string) error {
	var err error

	err = validateNamespaceFormat(namespace)
	if err == nil {
		err = validatePropertyFieldFormat(name, "name")
	}

	return err
}
