/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.ras.internal.routes.RunQueryRoute;
import dev.galasa.framework.spi.ras.RasSortField;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;


public class RasQueryParameters {

	public static final int DEFAULT_PAGE_NUMBER = 1;
	public static final int DEFAULT_NUMBER_RECORDS_PER_PAGE = 100;
    private final Map<String, Boolean> sortDirectionMap = new HashMap<String,Boolean>(){{
        put("asc",true);
        put("ascending",true);
        put("desc",false);
        put("descending",false);
    }};

    private QueryParameters generalQueryParams ;

    public RasQueryParameters(QueryParameters generalQueryParams) {
        this.generalQueryParams = generalQueryParams;
    }


    // make func to validate status values
    public List<TestRunLifecycleStatus> getStatusesFromParameters () throws InternalServletException{
		// status values received from the query
		List<String> queryStatuses = generalQueryParams.getMultipleString("status", null);

		if (queryStatuses != null){
			List<TestRunLifecycleStatus> returnStatuses = new ArrayList<TestRunLifecycleStatus>();
			for (String status : queryStatuses){
				String statusUppercase = status.toUpperCase();
				if (TestRunLifecycleStatus.isStatusValid(statusUppercase)) {
					returnStatuses.add(TestRunLifecycleStatus.valueOf(statusUppercase));
				} else {
					ServletError error = new ServletError(GAL5014_STATUS_NAME_NOT_RECOGNIZED, status, TestRunLifecycleStatus.getAllAsStringList().toString());
					throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
				}
			}
			return returnStatuses;
		}

		return new ArrayList<TestRunLifecycleStatus>();
	}



    public int getPageNumber() throws InternalServletException {
        int pageNumber = generalQueryParams.getSingleInt("page", DEFAULT_PAGE_NUMBER);
        return pageNumber;
    }

    public int getPageSize() throws InternalServletException {
        return generalQueryParams.getSingleInt("size", DEFAULT_NUMBER_RECORDS_PER_PAGE);
    }

    public Instant getToTime() throws InternalServletException {
        return generalQueryParams.getSingleInstant("to", null);
    }

    public Instant getFromTime() throws InternalServletException {
        return generalQueryParams.getSingleInstant("from", null);
    }

    public String getRequestor() throws InternalServletException {
        return generalQueryParams.getSingleString("requestor", null);
    }

    public String getGroup() throws InternalServletException {
        return generalQueryParams.getSingleString("group", null);
    }

    public String getSubmissionId() throws InternalServletException {
        return generalQueryParams.getSingleString("submissionId", null);
    }

    public String getTestName() throws InternalServletException {
        return generalQueryParams.getSingleString("testname", null);
    }

    public String getBundle() throws InternalServletException {
        return generalQueryParams.getSingleString("bundle", null);
    }

    public String getRunName() throws InternalServletException {
        return generalQueryParams.getSingleString("runname", null);
    }

    public String getPageCursor() throws InternalServletException {
        return generalQueryParams.getSingleString("cursor", null);
    }

    public boolean getIncludeCursor() throws InternalServletException {
        return generalQueryParams.getSingleBoolean("includeCursor", false);
    }

    public RasSortField getSortValue() throws InternalServletException {
        return getSortValue(null);
    }

    public RasSortField getSortValue(String defaultSortValue) throws InternalServletException {
        String sortValue = generalQueryParams.getSingleString("sort", defaultSortValue);
        RasSortField rasSortValue = null;
        if (sortValue != null) {
            rasSortValue = getValidatedSortValue(sortValue);
        }
        return rasSortValue;
    }

    public RasSortField getSortValueByName(String sortFieldName) throws InternalServletException {
        RasSortField sortValue = getSortValue();
        if (sortValue != null && !sortValue.getFieldName().equals(sortFieldName)) {
            sortValue = null;
        }
        return sortValue;
    }

    public List<String> getRunIds() {
        return generalQueryParams.getMultipleString("runId", new ArrayList<String>());
    }

    public boolean isAtLeastOneMandatoryParameterPresent() throws InternalServletException {
        return generalQueryParams.checkAtLeastOneQueryParameterPresent(
            RunQueryRoute.QUERY_PARAMETER_FROM,
            RunQueryRoute.QUERY_PARAMETER_RUNNAME,
            RunQueryRoute.QUERY_PARAMETER_GROUP,
            RunQueryRoute.QUERY_PARAMETER_SUBMISSION_ID
        );
    }

    public int getSize(){
        return generalQueryParams.getSize();
    }


    public List<String> getResultsFromParameters (@NotNull List<String> rasResults) throws InternalServletException{
		// Create map for the lowercase values of all results to ensure we can compare accurately
		Map<String,String> resultNames = new HashMap<String,String>();
		for (String result :rasResults){
			resultNames.put(result.toLowerCase(), result);
		}
		// Return the Results from the URL Query
		List<String> queryResults = generalQueryParams.getMultipleString("result", null);
		// Check the results against the map
		if (queryResults != null){
			List<String> returnResults = new ArrayList<String>();
			for (String result: queryResults){
				String matched = resultNames.get(result.toLowerCase());
				if (matched != null) {
					returnResults.add(matched);
				} else {
					ServletError error = new ServletError(GAL5013_RESULT_NAME_NOT_RECOGNIZED, result, rasResults.toString());
					throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
				}
			}
			return returnResults;
		}
		return null;
	}

    public QueryParameters getGeneralQueryParameters() {
        return this.generalQueryParams;
    }

    public boolean isAscending(String sortFieldName) throws InternalServletException {
        RasSortField sortField = getSortValueByName(sortFieldName);
        boolean isAscending = false;
        if (sortField != null) {
            isAscending = sortDirectionMap.get(sortField.getSortDirection());
        }
        return isAscending;
	}

    public boolean isAscending(RasSortField sortField) throws InternalServletException {
        return sortDirectionMap.get(sortField.getSortDirection());
	}

    private RasSortField getValidatedSortValue(String sortValue) throws InternalServletException {
        String[] sortValueParts = sortValue.split(":");

        if (sortValueParts.length != 2 || !sortDirectionMap.containsKey(sortValueParts[1].toLowerCase())) {
            ServletError error = new ServletError(GAL5011_SORT_VALUE_NOT_RECOGNIZED, sortValue);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        return new RasSortField(sortValueParts[0].toLowerCase(), sortValueParts[1].toLowerCase());
    }

}