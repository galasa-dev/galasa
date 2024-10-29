/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.time.*;
import java.time.format.DateTimeParseException;
import javax.servlet.http.HttpServletResponse;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;
import static javax.servlet.http.HttpServletResponse.*;


/**
 * A wrapper for a map of query parameters, with methods for extracting
 * and verifying the parameter contents.
 */
public class QueryParameters {

    Map<String,String[]> params ;

	/**
	 * @param paramMap The query parameters, probably coming from the request.
	 */
    public QueryParameters( Map<String,String[]> paramMap ) {
        this.params = paramMap;
    }

	/**
	 *
	 * @param queryParameterName The query parameter
	 * @param defaultValue
	 * @return
	 * @throws InternalServletException when there are multiple instances of the key, with error GAL5006
	 */
    public String getSingleString(String queryParameterName, String defaultValue ) throws InternalServletException {
		String returnedValue = defaultValue ;
		String[] paramValues = params.get(queryParameterName);
		if (paramValues != null && paramValues.length >0) {

			if (paramValues.length >1) {
				ServletError error = new ServletError(
					GAL5006_INVALID_QUERY_PARAM_DUPLICATES,
					queryParameterName);
				throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
			}

			String firstOccurrance = paramValues[0];
			if( firstOccurrance != null ) {
				String trimmedFirstOccurrance = firstOccurrance.trim();
				if ( !(trimmedFirstOccurrance.isEmpty())) {
					returnedValue = trimmedFirstOccurrance;
				}
			}
		}
		return returnedValue;
	}

	// /**
	//  *
	//  * @param queryParameterName The query parameter
	//  * @param defaultValues
	//  * @return
	//  */
    public List<String> getMultipleString(String queryParameterName, List<String> defaultValues ) {
		List<String> returnedValues = defaultValues ;
		String[] paramValues = params.get(queryParameterName);
		if (paramValues != null && paramValues.length > 0) {
			returnedValues = Arrays.asList(paramValues[0].split(","));
		}

		return returnedValues;
	}


	/**
	 * @param queryParameterName
	 * @param defaultValue Returned if there are no occurrances of the query parameter.
	 * @return
	 * @throws InternalServletException when there are multiple values of this query parameter, or
	 * when the value of the query parameter is not a valid integer.
	 */
    public int getSingleInt(String queryParameterName, int defaultValue) throws InternalServletException {
		int returnedValue = defaultValue ;
		String paramValueStr = getSingleString(queryParameterName, Integer.toString(defaultValue));

		if (paramValueStr != null ) {
			try {
				returnedValue = Integer.parseInt(paramValueStr.trim());
			} catch (NumberFormatException ex) {

				ServletError error = new ServletError(
					GAL5005_INVALID_QUERY_PARAM_NOT_INTEGER,
					queryParameterName,paramValueStr);
				throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST, ex);
			}
		}
		return returnedValue;
	}

    /**
     * @param queryParameterName the query parameter to retrieve the value of
     * @param defaultValue Returned if there are no occurrances of the query parameter.
     * @return the value of the given query parameter as a boolean
     * @throws InternalServletException when there are multiple values of this query parameter
     */
    public boolean getSingleBoolean(String queryParameterName, boolean defaultValue) throws InternalServletException {
        boolean returnedValue = defaultValue;
        String paramValueStr = getSingleString(queryParameterName, Boolean.toString(defaultValue));

        if (paramValueStr != null) {
            String trimmedParamValue = paramValueStr.trim();
            if (!trimmedParamValue.equalsIgnoreCase("true") && !trimmedParamValue.equalsIgnoreCase("false")) {
                ServletError error = new ServletError(GAL5090_INVALID_QUERY_PARAM_NOT_BOOLEAN, queryParameterName, paramValueStr);
                throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
            } else {
                returnedValue = Boolean.parseBoolean(trimmedParamValue);
            }
        }
        return returnedValue;
    }

	/**
	 * @param queryParameterName
	 * @param defaultValue Returned if there are no occurrances of the query parameter.
	 * @return
	 * @throws InternalServletException when there are multiple values of this query parameter, or
	 * when the value of the query parameter is not a valid date-time.
	 */
    public Instant getSingleInstant(String queryParameterName, Instant defaultValue) throws InternalServletException {
		Instant returnedValue = defaultValue ;
		String paramValueStr = getSingleString(queryParameterName, null);

		if (paramValueStr != null ) {
			try {
				returnedValue = Instant.parse(paramValueStr.trim());
			} catch (DateTimeParseException ex) {
				ServletError error = new ServletError(
					GAL5001_INVALID_DATE_TIME_FIELD,
					queryParameterName,paramValueStr
				);
				throw new InternalServletException(error, SC_BAD_REQUEST, ex);
			}
		}
		return returnedValue;
	}

	public boolean isParameterPresent(String queryParameter) {		
        return params.containsKey(queryParameter)
            && params.get(queryParameter) != null 
            && params.get(queryParameter)[0] != "";
    }

	public boolean checkAtLeastOneQueryParameterPresent(String... queryParameters ) throws InternalServletException {
		// This function will return false when none of the parameters provided are in the query
		boolean result = false;
		int paramswithvalue = 0 ;
		for (String param : queryParameters){
			if ((this.params.containsKey(param)) && (this.params.get(param) != null ) && (this.params.get(param)[0] != "" )) {
				paramswithvalue +=1;
			}
		}
		if (paramswithvalue !=0) {
			result = true;
		}
		return result;
	}

    public int getSize() {
        return this.params.size();
    }

}