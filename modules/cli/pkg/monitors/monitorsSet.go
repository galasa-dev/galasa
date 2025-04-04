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
	"strconv"

	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/spi"
)

func SetMonitor(
	monitorName string,
	isEnabledStr string,
	apiClient *galasaapi.APIClient,
	byteReader spi.ByteReader,
) error {
	var err error
	
	monitorName, err = validateMonitorName(monitorName)
	if err == nil {
		if isEnabledStr != "" {
			err = setMonitorIsEnabledState(monitorName, isEnabledStr, apiClient, byteReader)
		}
	}

	log.Printf("SetMonitor exiting. err is %v\n", err)
	return err
}

func setMonitorIsEnabledState(
	monitorName string,
	isEnabledStr string,
	apiClient *galasaapi.APIClient,
	byteReader spi.ByteReader,
) error {
	var err error
	var desiredEnabledState bool

	desiredEnabledState, err = strconv.ParseBool(isEnabledStr)
	if err == nil {
		err = sendUpdateMonitorStateRequest(monitorName, desiredEnabledState, apiClient, byteReader)
	} else {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_IS_ENABLED_FLAG)
	}

	return err
}

func sendUpdateMonitorStateRequest(
	monitorName string,
	isEnabled bool,
	apiClient *galasaapi.APIClient,
	byteReader spi.ByteReader,
) error {
	var err error
	var httpResponse *http.Response
	var context context.Context = context.Background()
	var restApiVersion string
	restApiVersion, err = embedded.GetGalasactlRestApiVersion()
	
	if err == nil {
		requestBody := *galasaapi.NewUpdateGalasaMonitorRequest()
		monitorData := *galasaapi.NewUpdateGalasaMonitorRequestData()
		monitorData.SetIsEnabled(isEnabled)

		requestBody.SetData(monitorData)

		httpResponse, err = apiClient.MonitorsAPIApi.SetMonitorStatus(context, monitorName).
			UpdateGalasaMonitorRequest(requestBody).
			ClientApiVersion(restApiVersion).
			Execute()

		if httpResponse != nil {
			defer httpResponse.Body.Close()
		}

		if err != nil {
			if httpResponse == nil {
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_UPDATE_MONITOR_REQUEST_FAILED, err.Error())
			} else {
				err = galasaErrors.HttpResponseToGalasaError(
					httpResponse,
					monitorName,
					byteReader,
					galasaErrors.GALASA_ERROR_UPDATE_MONITOR_NO_RESPONSE_CONTENT,
					galasaErrors.GALASA_ERROR_UPDATE_MONITOR_RESPONSE_BODY_UNREADABLE,
					galasaErrors.GALASA_ERROR_UPDATE_MONITOR_UNPARSEABLE_CONTENT,
					galasaErrors.GALASA_ERROR_UPDATE_MONITOR_SERVER_REPORTED_ERROR,
					galasaErrors.GALASA_ERROR_UPDATE_MONITOR_EXPLANATION_NOT_JSON,
				)
			}
		}
	}
	return err
}
