/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package launcher

import (
	"context"
	"encoding/json"
	"io"
	"log"
	"net/http"
	"strconv"
	"strings"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
)

// RemoteLauncher A launcher, which launches and monitors tests on a remote ecosystem via HTTP/HTTPS.
type RemoteLauncher struct {
	commsClient api.APICommsClient
}

//----------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------

// NewRemoteLauncher create a remote launcher.
func NewRemoteLauncher(commsClient api.APICommsClient) *RemoteLauncher {
	log.Printf("NewRemoteLauncher(%s) entered.", commsClient.GetBootstrapData().ApiServerURL)

	launcher := new(RemoteLauncher)

	// A comms client that communicates with the API server in a Galasa service.
	launcher.commsClient = commsClient

	return launcher
}

//----------------------------------------------------------------------------------
// Implementation of the launcher interface.
//----------------------------------------------------------------------------------

// GetRunsByGroup get all the testruns which are associated with a named group.
func (launcher *RemoteLauncher) GetRunsByGroup(groupName string) (*galasaapi.TestRuns, error) {
	log.Printf("GetRunsByGroup(%s) entered.", groupName)
	var (
		testRuns       *galasaapi.TestRuns
		err            error
		restApiVersion string
	)
	restApiVersion, err = embedded.GetGalasactlRestApiVersion()
	if err == nil {
		err = launcher.commsClient.RunAuthenticatedCommandWithRateLimitRetries(func(apiClient *galasaapi.APIClient) error {
			var httpResponse *http.Response
			testRuns, httpResponse, err = apiClient.RunsAPIApi.GetRunsGroup(context.TODO(), groupName).ClientApiVersion(restApiVersion).Execute()

			return galasaErrors.GetGalasaErrorFromCommsResponse(httpResponse, err)
		})
	}
	return testRuns, err
}

func (launcher *RemoteLauncher) SubmitTestRun(
	groupName string,
	className string,
	requestType string,
	requestor string,
	stream string,
	obrFromPortfolio string,
	isTraceEnabled bool,
	GherkinURL string,
	GherkinFeature string,
	overrides map[string]interface{},
) (*galasaapi.TestRuns, error) {

	classNames := make([]string, 1)
	classNames[0] = className

	testRunRequest := galasaapi.NewTestRunRequest()
	testRunRequest.SetClassNames(classNames)
	testRunRequest.SetRequestorType(requestType)
	testRunRequest.SetRequestor(requestor)
	testRunRequest.SetTestStream(stream)
	testRunRequest.SetTrace(isTraceEnabled)
	testRunRequest.SetOverrides(overrides)

	log.Printf("RemoteLauncher.SubmitTestRuns : using requestor %s\n", requestor)

	var resultGroup *galasaapi.TestRuns
	var err error
	var restApiVersion string

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	if err == nil {
		err = launcher.commsClient.RunAuthenticatedCommandWithRateLimitRetries(func(apiClient *galasaapi.APIClient) error {
			var httpResponse *http.Response
			resultGroup, httpResponse, err = apiClient.RunsAPIApi.PostSubmitTestRuns(context.TODO(), groupName).TestRunRequest(*testRunRequest).ClientApiVersion(restApiVersion).Execute()

			return galasaErrors.GetGalasaErrorFromCommsResponse(httpResponse, err)
		})
	}
	return resultGroup, err
}

func (launcher *RemoteLauncher) GetRunsById(runId string) (*galasaapi.Run, error) {
	var err error
	var rasRun *galasaapi.Run
	var restApiVersion string

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	if err == nil {
		err = launcher.commsClient.RunAuthenticatedCommandWithRateLimitRetries(func(apiClient *galasaapi.APIClient) error {
			var httpResponse *http.Response
			rasRun, _, err = apiClient.ResultArchiveStoreAPIApi.GetRasRunById(context.TODO(), runId).ClientApiVersion(restApiVersion).Execute()

			return galasaErrors.GetGalasaErrorFromCommsResponse(httpResponse, err)
		})
	}
	return rasRun, err
}

// Gets the latest run based on the submission ID of that run.
// For local runs, the submission ID is the same as the test run id.
func (launcher *RemoteLauncher) GetRunsBySubmissionId(submissionId string, groupId string) (*galasaapi.Run, error) {
	log.Printf("RemoteLauncher: GetRunsBySubmissionId entered. runId=%v groupId=%v", submissionId, groupId)
	var err error
	var rasRun *galasaapi.Run
	var restApiVersion string

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	if err == nil {
		var runData *galasaapi.RunResults

		err = launcher.commsClient.RunAuthenticatedCommandWithRateLimitRetries(func(apiClient *galasaapi.APIClient) error {
			var err error
			var httpResponse *http.Response
			var context context.Context = nil

			apicall := apiClient.ResultArchiveStoreAPIApi.GetRasSearchRuns(context).ClientApiVersion(restApiVersion).
				IncludeCursor("true").
				SubmissionId(submissionId).Group(groupId).Sort("from:desc")

			runData, httpResponse, err = apicall.Execute()

			var statusCode int
			if httpResponse != nil {
				defer httpResponse.Body.Close()
				statusCode = httpResponse.StatusCode
			}

			if err != nil {
				err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_QUERY_RUNS_FAILED, err.Error())
			} else {
				if statusCode != http.StatusOK {
					httpError := "\nhttp response status code: " + strconv.Itoa(statusCode)
					errString := httpError
					err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_QUERY_RUNS_FAILED, errString)
				} else {

					log.Printf("HTTP status was OK")

					if runData.GetAmountOfRuns() > 0 {
						runs := runData.GetRuns()
						rasRun = &runs[0]
					}

				}
			}
			return err
		})
	}
	return rasRun, err
}

func (launcher *RemoteLauncher) GetStreams() ([]string, error) {

	var streams []string

	var restApiVersion string
	var err error

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	if err == nil {
		var properties []galasaapi.GalasaProperty
		err = launcher.commsClient.RunAuthenticatedCommandWithRateLimitRetries(func(apiClient *galasaapi.APIClient) error {
			var httpResponse *http.Response
			properties, httpResponse, err = apiClient.ConfigurationPropertyStoreAPIApi.
				QueryCpsNamespaceProperties(context.TODO(), "framework").Prefix("test.stream").Suffix("repo").ClientApiVersion(restApiVersion).Execute()

			return galasaErrors.GetGalasaErrorFromCommsResponse(httpResponse, err)
		})

		if err == nil {
			streams, err = getStreamNamesFromProperties(properties)
		}
	}
	return streams, err
}

// When passed an array of GalasaProperty objects, extract the stream names from them.
func getStreamNamesFromProperties(properties []galasaapi.GalasaProperty) ([]string, error) {
	var err error
	var streams []string = make([]string, 0)
	for _, property := range properties {
		propertyNamePtr := property.GetMetadata().Name

		propertyName := *propertyNamePtr
		// This is something like "test.stream.zosk8s.repo"

		streamName := propertyName[12 : len(propertyName)-5]
		streams = append(streams, streamName)
	}
	return streams, err
}

func (launcher *RemoteLauncher) GetTestCatalog(stream string) (TestCatalog, error) {

	var err error
	var testCatalog TestCatalog
	var cpsProperty []galasaapi.GalasaProperty
	var restApiVersion string

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	if err == nil {
		var cpsResponse *http.Response
		err = launcher.commsClient.RunAuthenticatedCommandWithRateLimitRetries(func(apiClient *galasaapi.APIClient) error {
			cpsProperty, cpsResponse, err = apiClient.ConfigurationPropertyStoreAPIApi.QueryCpsNamespaceProperties(context.TODO(), "framework").Prefix("test.stream." + stream).Suffix("location").ClientApiVersion(restApiVersion).Execute()

			var statusCode int
			if cpsResponse != nil {
				defer cpsResponse.Body.Close()
				statusCode = cpsResponse.StatusCode
			}

			if err != nil {
				err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_PROPERTY_GET_FAILED, stream, err)
			} else if len(cpsProperty) < 1 {
				err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_CATALOG_NOT_FOUND, stream)
			}
			return err
		})

		if err == nil {
			streamLocation := cpsProperty[0].Data.Value
			catalogString := new(strings.Builder)
			var resp *http.Response
			resp, err = http.Get(*streamLocation)
			if err != nil {
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_GET_TEST_CATALOG_CONTENTS_FAILED, *streamLocation, stream, err)
			} else {
				defer resp.Body.Close()

				_, err = io.Copy(catalogString, resp.Body)
				if err != nil {
					err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_CATALOG_COPY_FAILED, *streamLocation, stream, err)
				}
			}

			if err == nil {
				err = json.Unmarshal([]byte(catalogString.String()), &testCatalog)
				if err != nil {
					err = galasaErrors.NewGalasaError(
						galasaErrors.GALASA_ERROR_CATALOG_UNMARSHAL_FAILED, *streamLocation, stream, err)
				}
			}
		}
	}

	return testCatalog, err
}
