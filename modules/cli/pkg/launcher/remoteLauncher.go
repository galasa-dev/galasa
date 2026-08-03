/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package launcher

import (
	"context"
	"log"
	"net/http"
	"strconv"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/utils"
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
	systemUser string,
	user string,
	stream string,
	obrFromPortfolio string,
	isTraceEnabled bool,
	GherkinURL string,
	GherkinFeature string,
	overrides map[string]interface{},
	tags []string,
) (*galasaapi.TestRuns, error) {

	classNames := make([]string, 1)
	classNames[0] = className

	testRunRequest := galasaapi.NewTestRunRequest()
	testRunRequest.SetClassNames(classNames)
	testRunRequest.SetRequestorType(requestType)
	testRunRequest.SetTestStream(stream)
	testRunRequest.SetTrace(isTraceEnabled)
	testRunRequest.SetOverrides(overrides)
	testRunRequest.SetTags(tags)

	// If a `--user` was not specified, the API will default
	// it to the requestor, so no need to set it here.
	if user != "" {
		log.Printf("RemoteLauncher.SubmitTestRun : attempting to set the run user to %s."+
			" User will default to the authenticated requestor if the authenticated requestor does not have admin rights.", user)
		testRunRequest.SetUser(user)
	}

	var resultGroup *galasaapi.TestRuns
	var err error
	var restApiVersion string

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	if err == nil {
		err = launcher.commsClient.RunAuthenticatedCommandWithRateLimitRetries(func(apiClient *galasaapi.APIClient) error {
			apiCall := apiClient.RunsAPIApi.PostSubmitTestRuns(context.TODO(), groupName).TestRunRequest(*testRunRequest).ClientApiVersion(restApiVersion)

			if err == nil {

				var httpResponse *http.Response

				resultGroup, httpResponse, err = apiCall.Execute()

				if httpResponse != nil {
					defer httpResponse.Body.Close()
				}

				if err != nil {
					log.Println("SubmitTestRun - Failed to submit runs to the Galasa service")

					if httpResponse.StatusCode == 403 {
						err = galasaErrors.NewGalasaError(galasaErrors.GALASA_USER_MISSING_TEST_LAUNCH_PERMISSION, user)
					} else {
						err = galasaErrors.GetGalasaErrorFromCommsResponse(httpResponse, err)
					}
				}
			}

			return err
		})
	}

	if len(resultGroup.GetRuns()) > 0 {
		//
		// The current system user may not match the username in the
		// JWT that was used to authenticate to the Galasa API Server
		//
		submittedRun := resultGroup.GetRuns()[0]
		if submittedRun.Requestor != nil {
			runRequestor := *submittedRun.Requestor

			if systemUser == runRequestor {
				log.Printf("RemoteLauncher.SubmitTestRun : the run requestor will be set to %s\n", runRequestor)
			} else {
				log.Printf("RemoteLauncher.SubmitTestRun : current system user is %s, but the user authenticated to the Galasa Service is %s, so the run requestor will be set to %s\n", systemUser, runRequestor, runRequestor)
			}
		}
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
	var restApiVersion string

	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	if err == nil {
		var testCatalogResponse *http.Response
		err = launcher.commsClient.RunAuthenticatedCommandWithRateLimitRetries(func(apiClient *galasaapi.APIClient) error {
			testCatalog, testCatalogResponse, err = apiClient.StreamsAPIApi.
				GetStreamTestCatalog(context.TODO(), stream).
				ClientApiVersion(restApiVersion).
				Execute()

			var statusCode int
			if testCatalogResponse != nil {
				defer testCatalogResponse.Body.Close()
				statusCode = testCatalogResponse.StatusCode
			}

			if err != nil {
				err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_GET_TEST_CATALOG_CONTENTS_FAILED, stream, err)
			}
			return err
		})
	}
	
		return testCatalog, err
	}
	
	// CreateRunsPortfolio calls POST /runs/portfolios, resolving test class selection server-side.
	func (launcher *RemoteLauncher) CreateRunsPortfolio(flags *utils.TestSelectionFlagValues, overrides map[string]string) (*galasaapi.RunsPortfolio, error) {
		selection := galasaapi.NewRunsPortfolioSelection(flags.Stream)
		if len(*flags.Bundles) > 0 {
			selection.SetBundles(*flags.Bundles)
		}
		if len(*flags.Packages) > 0 {
			selection.SetPackages(*flags.Packages)
		}
		if len(*flags.Tests) > 0 {
			selection.SetTests(*flags.Tests)
		}
		if len(*flags.Tags) > 0 {
			selection.SetTags(*flags.Tags)
		}
		if len(*flags.Classes) > 0 {
			selection.SetClasses(*flags.Classes)
		}
		if flags.RegexSelect != nil && *flags.RegexSelect {
			selection.SetRegex(true)
		}
	
		request := galasaapi.NewRunsPortfolioRequest([]galasaapi.RunsPortfolioSelection{*selection})
		if len(overrides) > 0 {
			request.SetOverrides(overrides)
		}
	
		var portfolio *galasaapi.RunsPortfolio
		var err error
		var restApiVersion string
	
		restApiVersion, err = embedded.GetGalasactlRestApiVersion()
		if err == nil {
			err = launcher.commsClient.RunAuthenticatedCommandWithRateLimitRetries(func(apiClient *galasaapi.APIClient) error {
				var httpResponse *http.Response
				portfolio, httpResponse, err = apiClient.RunsAPIApi.
					CreateRunsPortfolio(context.TODO()).
					RunsPortfolioRequest(*request).
					ClientApiVersion(restApiVersion).
					Execute()
	
				return galasaErrors.GetGalasaErrorFromCommsResponse(httpResponse, err)
			})
		}
		return portfolio, err
	}
	
	// IsLocal returns false as this launcher runs tests remotely
	func (launcher *RemoteLauncher) IsLocal() bool {
		return false
	}
