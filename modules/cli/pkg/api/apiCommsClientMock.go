/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package api

import (
	"github.com/galasa-dev/cli/pkg/utils"
)

func NewMockAPICommsClient(apiServerUrl string) APICommsClient {

	maxAttempts := 3
	retryBackoffSeconds := 0

	apiClient := InitialiseAPI(apiServerUrl)
	mockAuthenticator := utils.NewMockAuthenticatorWithAPIClient(apiClient)
	mockTimeService := utils.NewMockTimeService()

	bootstrapData := &BootstrapData{
		ApiServerURL: apiServerUrl,
	}

	return &APICommsClientImpl{
		maxAttempts: maxAttempts,
		retryBackoffSeconds: float64(retryBackoffSeconds),
		timeService: mockTimeService,
		bootstrapData: bootstrapData,
		apiClient: apiClient,
		authenticator: mockAuthenticator,
	}
}
