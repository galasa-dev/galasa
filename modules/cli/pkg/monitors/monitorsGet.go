/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package monitors

import (
	"context"
	"log"
	"net/http"
	"sort"
	"strings"

	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/monitorsformatter"
	"github.com/galasa-dev/cli/pkg/spi"
)

var (
	formatters = createFormatters()
)

func GetMonitors(
	monitorName string,
	format string,
	console spi.Console,
	apiClient *galasaapi.APIClient,
	byteReader spi.ByteReader,
) error {
	var err error
	var chosenFormatter monitorsformatter.MonitorsFormatter
	var monitors []galasaapi.GalasaMonitor

	chosenFormatter, err = validateFormatFlag(format)
	if err == nil {
		if monitorName != "" {
			// The user has provided a monitor name, so try to get that monitor
			var monitor *galasaapi.GalasaMonitor
			monitor, err = getMonitorByName(monitorName, apiClient, byteReader)
			if err == nil {
				monitors = append(monitors, *monitor)
			}
		} else {
			// Get all monitors
			monitors, err = getMonitorsFromRestApi(apiClient, byteReader)
		}

		// If we were able to get the monitors, format them as requested by the user
		if err == nil {
			var formattedOutput string
			formattedOutput, err = chosenFormatter.FormatMonitors(monitors)
			if err == nil {
				console.WriteString(formattedOutput)
			}
		}
	}
	log.Printf("GetMonitors exiting. err is %v\n", err)
	return err
}

func getMonitorByName(
	monitorName string,
	apiClient *galasaapi.APIClient,
	byteReader spi.ByteReader,
) (*galasaapi.GalasaMonitor, error) {
	var err error
	var monitor *galasaapi.GalasaMonitor
	monitorName, err = validateMonitorName(monitorName)
	if err == nil {
		monitor, err = getMonitorFromRestApi(monitorName, apiClient, byteReader)
		if monitor == nil {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_MONITOR_NAME_NOT_FOUND, monitorName)
		}
	}

	return monitor, err
}

func getMonitorFromRestApi(
	monitorName string,
	apiClient *galasaapi.APIClient,
	byteReader spi.ByteReader,
) (*galasaapi.GalasaMonitor, error) {
	var err error
	var httpResponse *http.Response
	var context context.Context = context.Background()
	var restApiVersion string
	var monitor *galasaapi.GalasaMonitor

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	if err == nil {
		monitor, httpResponse, err = apiClient.MonitorsAPIApi.GetMonitorByName(context, monitorName).
			ClientApiVersion(restApiVersion).
			Execute()

		if httpResponse != nil {
			defer httpResponse.Body.Close()
		}

		if err != nil {
			if httpResponse == nil {
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_GET_MONITORS_REQUEST_FAILED, err.Error())
			} else {
				err = galasaErrors.HttpResponseToGalasaError(
					httpResponse,
					"",
					byteReader,
					galasaErrors.GALASA_ERROR_GET_MONITORS_NO_RESPONSE_CONTENT,
					galasaErrors.GALASA_ERROR_GET_MONITORS_RESPONSE_BODY_UNREADABLE,
					galasaErrors.GALASA_ERROR_GET_MONITORS_UNPARSEABLE_CONTENT,
					galasaErrors.GALASA_ERROR_GET_MONITORS_SERVER_REPORTED_ERROR,
					galasaErrors.GALASA_ERROR_GET_MONITORS_EXPLANATION_NOT_JSON,
				)
			}
		}
	}
	return monitor, err
}

func getMonitorsFromRestApi(
	apiClient *galasaapi.APIClient,
	byteReader spi.ByteReader,
) ([]galasaapi.GalasaMonitor, error) {
	var err error
	var httpResponse *http.Response
	var context context.Context = context.Background()
	var restApiVersion string
	var monitors []galasaapi.GalasaMonitor

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	if err == nil {
		monitors, httpResponse, err = apiClient.MonitorsAPIApi.GetMonitors(context).
			ClientApiVersion(restApiVersion).
			Execute()

		if httpResponse != nil {
			defer httpResponse.Body.Close()
		}

		if err != nil {
			if httpResponse == nil {
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_GET_MONITORS_REQUEST_FAILED, err.Error())
			} else {
				err = galasaErrors.HttpResponseToGalasaError(
					httpResponse,
					"",
					byteReader,
					galasaErrors.GALASA_ERROR_GET_MONITORS_NO_RESPONSE_CONTENT,
					galasaErrors.GALASA_ERROR_GET_MONITORS_RESPONSE_BODY_UNREADABLE,
					galasaErrors.GALASA_ERROR_GET_MONITORS_UNPARSEABLE_CONTENT,
					galasaErrors.GALASA_ERROR_GET_MONITORS_SERVER_REPORTED_ERROR,
					galasaErrors.GALASA_ERROR_GET_MONITORS_EXPLANATION_NOT_JSON,
				)
			}
		}
	}
	return monitors, err
}

func createFormatters() map[string]monitorsformatter.MonitorsFormatter {
	formatters := make(map[string]monitorsformatter.MonitorsFormatter, 0)
	summaryFormatter := monitorsformatter.NewMonitorsSummaryFormatter()
	yamlFormatter := monitorsformatter.NewMonitorsYamlFormatter()

	formatters[summaryFormatter.GetName()] = summaryFormatter
	formatters[yamlFormatter.GetName()] = yamlFormatter

	return formatters
}

func GetFormatterNamesAsString() string {
	names := make([]string, 0, len(formatters))
	for name := range formatters {
		names = append(names, name)
	}
	sort.Strings(names)
	formatterNames := strings.Builder{}

	for index, formatterName := range names {

		if index != 0 {
			formatterNames.WriteString(", ")
		}
		formatterNames.WriteString("'" + formatterName + "'")
	}

	return formatterNames.String()
}

func validateFormatFlag(outputFormatString string) (monitorsformatter.MonitorsFormatter, error) {
	var err error

	chosenFormatter, isPresent := formatters[outputFormatString]

	if !isPresent {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_OUTPUT_FORMAT, outputFormatString, GetFormatterNamesAsString())
	}

	return chosenFormatter, err
}
