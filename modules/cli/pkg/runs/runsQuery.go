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
	"strconv"
	"time"

	"github.com/galasa-dev/cli/pkg/api"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
)

type RunsQuery struct {
    pageCursor string
    runName string
    requestor string
    result string
    group string
    fromTime time.Time
    toTime time.Time
    shouldGetActive bool
}

func NewRunsQuery(
    runName string,
    requestor string,
    result string,
    group string,
    fromAgeMins int,
    toAgeMins int,
    shouldGetActive bool,
    now time.Time,
) *RunsQuery {
    runsQuery := &RunsQuery{
        runName: runName,
        requestor: requestor,
        result: result,
        group: group,
        shouldGetActive: shouldGetActive,
    }

    if fromAgeMins != 0 {
        runsQuery.fromTime = now.Add(-(time.Duration(fromAgeMins) * time.Minute)).UTC() // Add a minus, so subtract
    }

    if toAgeMins != 0 {
        runsQuery.toTime = now.Add(-(time.Duration(toAgeMins) * time.Minute)).UTC() // Add a minus, so subtract
    }

    return runsQuery
}

func (query *RunsQuery) SetPageCursor(newPageCursor string) {
    query.pageCursor = newPageCursor
}

func (query *RunsQuery) GetRunsPageFromRestApi(
    commsClient api.APICommsClient,
    restApiVersion string,
) (*galasaapi.RunResults, error) {
    var err error
    var runData *galasaapi.RunResults

    err = commsClient.RunAuthenticatedCommandWithRateLimitRetries(func(apiClient *galasaapi.APIClient) error {
        var err error
        var httpResponse *http.Response
        var context context.Context = nil
    
        apicall := apiClient.ResultArchiveStoreAPIApi.GetRasSearchRuns(context).ClientApiVersion(restApiVersion).IncludeCursor("true")
        if !query.fromTime.IsZero() {
            apicall = apicall.From(query.fromTime)
        }
        if !query.toTime.IsZero() {
            apicall = apicall.To(query.toTime)
        }
        if query.runName != "" {
            apicall = apicall.Runname(query.runName)
        }
        if query.requestor != "" {
            apicall = apicall.Requestor(query.requestor)
        }
        if query.result != "" {
            apicall = apicall.Result(query.result)
        }
        if query.shouldGetActive {
            apicall = apicall.Status(activeStatusNames)
        }
        if query.pageCursor != "" {
            apicall = apicall.Cursor(query.pageCursor)
        }
        if query.group != "" {
            apicall = apicall.Group(query.group)
        }
        apicall = apicall.Sort("from:desc")
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
    
                // Copy the results from this page into our bigger list of results.
                log.Printf("runsOnThisPage: %v", len(runData.GetRuns()))
            }
        }
        return err
    })
    return runData, err
}
