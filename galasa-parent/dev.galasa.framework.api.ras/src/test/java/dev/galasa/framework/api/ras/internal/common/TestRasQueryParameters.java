/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.common;

import java.util.*;

import org.junit.Test;

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.api.ras.internal.RasServletTest;
import dev.galasa.framework.spi.ras.RasSortField;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;

import static org.assertj.core.api.Assertions.*;

public class TestRasQueryParameters extends RasServletTest{
    //-----------------------------------------------------------------
    // Test getResultsFromParameters
    //-----------------------------------------------------------------
    @Test
    public void testGetResultsFromParametersIfMultipleResultLowerCaseParametersOK() throws Exception {
        // Given...
        List<String> mockRasResultNames = new ArrayList<String>();
        mockRasResultNames.add("Passed");
        mockRasResultNames.add("Failed");
        mockRasResultNames.add("EnvFail");
        mockRasResultNames.add("UNKNOWN");
        Map<String,String[]> mockURLQuery = new HashMap<>();
        mockURLQuery.put("result", new String[] { "passed,failed,envfail" });
        RasQueryParameters params = new RasQueryParameters(new QueryParameters(mockURLQuery));

        // When...
        List<String> returnedResults = params.getResultsFromParameters(mockRasResultNames);

        // Then...
        assertThat(returnedResults).isEqualTo(Arrays.asList("Passed", "Failed", "EnvFail"));
    }

    @Test
    public void testGetResultsFromParametersIfMultipleResultUpperCaseParametersOK() throws Exception {
        // Given...
        List<String> mockRasResultNames = new ArrayList<String>();
        mockRasResultNames.add("Passed");
        mockRasResultNames.add("Failed");
        mockRasResultNames.add("EnvFail");
        mockRasResultNames.add("UNKNOWN");
        Map<String,String[]> mockURLQuery = new HashMap<>();
        mockURLQuery.put("result", new String[] { "PASSED,FAILED,ENVFAIL" });
        RasQueryParameters params = new RasQueryParameters(new QueryParameters(mockURLQuery));

        // When...
        List<String> returnedResults = params.getResultsFromParameters(mockRasResultNames);

        // Then...
        assertThat(returnedResults).isEqualTo(Arrays.asList("Passed", "Failed", "EnvFail"));
    }

    @Test
    public void testGetResultsFromParametersIfMultipleResultMixedCaseParametersOK() throws Exception {
        // Given...
        List<String> mockRasResultNames = new ArrayList<String>();
        mockRasResultNames.add("Passed");
        mockRasResultNames.add("Failed");
        mockRasResultNames.add("EnvFail");
        mockRasResultNames.add("UNKNOWN");
        Map<String,String[]> mockURLQuery = new HashMap<>();
        mockURLQuery.put("result", new String[] { "pasSed,Failed,envfAIl" });
        RasQueryParameters params = new RasQueryParameters(new QueryParameters(mockURLQuery));

        // When...
        List<String> returnedResults = params.getResultsFromParameters(mockRasResultNames);

        // Then...
        assertThat(returnedResults).isEqualTo(Arrays.asList("Passed", "Failed", "EnvFail"));
    }

    @Test
    public void testGetResultsFromParametersIfResultParameterIsInvalidThrowsException() throws Exception {
        // Given...
        List<String> mockRasResultNames = new ArrayList<String>();
        mockRasResultNames.add("Passed");
        mockRasResultNames.add("Failed");
        mockRasResultNames.add("EnvFail");
        mockRasResultNames.add("UNKNOWN");
        Map<String,String[]> mockURLQuery = new HashMap<String,String[]>();
        RasQueryParameters params = new RasQueryParameters(new QueryParameters(mockURLQuery));
        mockURLQuery.put("result", new String[]{"Passed,Failed,garbage"} );

        // When...
        Throwable thrown = catchThrowable( () -> {
            params.getResultsFromParameters(mockRasResultNames);
        });

        // Then...
        assertThat(thrown)
            .isInstanceOf(InternalServletException.class)
            .hasMessageContaining("garbage")
            .hasMessageContaining("GAL5013"); // GAL5013_RESULT_NAME_NOT_RECOGNIZED

    }

    @Test
    public void testGetResultsFromParametersIfResultParameterIsNullReturnsNull() throws Exception {
        // Given...
        List<String> mockRasResultNames = new ArrayList<String>();
        mockRasResultNames.add("Passed");
        mockRasResultNames.add("Failed");
        mockRasResultNames.add("EnvFail");
        mockRasResultNames.add("UNKNOWN");
        Map<String,String[]> mockURLQuery = new HashMap<String,String[]>();
        RasQueryParameters params = new RasQueryParameters(new QueryParameters(mockURLQuery));
        mockURLQuery.put("result", new String[]{} );

        // When...

        List<String> returnedResults = params.getResultsFromParameters(mockRasResultNames);


        // Then...
        assertThat(returnedResults)
            .isNull();

    }

    @Test
    public void testGetResultsFromParametersWithMultipleCommasTogetherReturnsError() throws Exception {
        // Given...
        List<String> mockRasResultNames = new ArrayList<String>();
        mockRasResultNames.add("Passed");
        mockRasResultNames.add("Failed");
        mockRasResultNames.add("EnvFail");
        mockRasResultNames.add("UNKNOWN");
        Map<String,String[]> mockURLQuery = new HashMap<>();
        mockURLQuery.put("result", new String[] { "pasSed,,Failed,env,fAIl" });
        RasQueryParameters params = new RasQueryParameters(new QueryParameters(mockURLQuery));

        // When...
        Throwable thrown = catchThrowable( () -> {
            params.getResultsFromParameters(mockRasResultNames);
        });

        // Then...
        assertThat(thrown)
            .isInstanceOf(InternalServletException.class)
            .hasMessageContaining("''")
            .hasMessageContaining("GAL5013"); // GAL5013_RESULT_NAME_NOT_RECOGNIZED
    }

    @Test
    public void testGetResultsFromParametersWithMultipleCommasInParameterReturnsError() throws Exception {
        // Given...
        List<String> mockRasResultNames = new ArrayList<String>();
        mockRasResultNames.add("Passed");
        mockRasResultNames.add("Failed");
        mockRasResultNames.add("EnvFail");
        mockRasResultNames.add("UNKNOWN");
        Map<String,String[]> mockURLQuery = new HashMap<>();
        mockURLQuery.put("result", new String[] { "pasSed,Failed,env,fAIl" });
        RasQueryParameters params = new RasQueryParameters(new QueryParameters(mockURLQuery));

        /// When...
        Throwable thrown = catchThrowable( () -> {
            params.getResultsFromParameters(mockRasResultNames);
        });

        // Then...
        assertThat(thrown)
            .isInstanceOf(InternalServletException.class)
            .hasMessageContaining("env")
            .hasMessageContaining("GAL5013"); // GAL5013_RESULT_NAME_NOT_RECOGNIZED
    }

    //-----------------------------------------------------------------
    // Test getStatusesFromParameters
    //-----------------------------------------------------------------

    @Test
    public void testGetStatusesFromParametersIfMultipleStatusesLowerCaseParametersOK() throws Exception {
        // Given...
        Map<String,String[]> mockURLQuery = new HashMap<>();
        mockURLQuery.put("status", new String[] { "building,generating,running" });

        RasQueryParameters params = new RasQueryParameters(new QueryParameters(mockURLQuery));

        // When...
        List<TestRunLifecycleStatus> returnedStatuses = params.getStatusesFromParameters();

        // Then...
        assertThat(returnedStatuses).isEqualTo(Arrays.asList(TestRunLifecycleStatus.BUILDING, TestRunLifecycleStatus.GENERATING, TestRunLifecycleStatus.RUNNING));
    }

    @Test
    public void testGetStatusesFromParametersIfMultipleStatusesUpperCaseParametersOK() throws Exception {
        // Given...
        Map<String,String[]> mockURLQuery = new HashMap<>();
        mockURLQuery.put("status", new String[] { "BUILDING,GENERATING,RUNNING" });
        RasQueryParameters params = new RasQueryParameters(new QueryParameters(mockURLQuery));

        // When...
        List<TestRunLifecycleStatus> returnedStatuses = params.getStatusesFromParameters();

        // Then...
        assertThat(returnedStatuses).isEqualTo(Arrays.asList(TestRunLifecycleStatus.BUILDING, TestRunLifecycleStatus.GENERATING, TestRunLifecycleStatus.RUNNING));
    }

    @Test
    public void testGetStatusesFromParametersIfMultipleStatusesMixedCaseParametersOK() throws Exception {
        // Given...
        Map<String,String[]> mockURLQuery = new HashMap<>();
        mockURLQuery.put("status", new String[] { "BuIlDiNg,gEnErAtInG,RuNNinG" });
        RasQueryParameters params = new RasQueryParameters(new QueryParameters(mockURLQuery));

        // When...
        List<TestRunLifecycleStatus> returnedStatuses = params.getStatusesFromParameters();

        // Then...
        assertThat(returnedStatuses).isEqualTo(Arrays.asList(TestRunLifecycleStatus.BUILDING, TestRunLifecycleStatus.GENERATING, TestRunLifecycleStatus.RUNNING));
    }

    @Test
    public void testGetStatusesFromParametersIfStatusParameterIsInvalidThrowsException() throws Exception {
        // Given...
        Map<String,String[]> mockURLQuery = new HashMap<String,String[]>();
        RasQueryParameters params = new RasQueryParameters(new QueryParameters(mockURLQuery));
        mockURLQuery.put("status", new String[]{"building,generating,garbage"} );

        // When...
        Throwable thrown = catchThrowable( () -> {
            params.getStatusesFromParameters();
        });

        // Then...
        assertThat(thrown)
            .isInstanceOf(InternalServletException.class)
            .hasMessageContaining("garbage")
            .hasMessageContaining("GAL5014"); // GAL5014_STATUS_NAME_NOT_RECOGNIZED

    }

    @Test
    public void testGetStatusesFromParametersIfStatusParameterIsNullReturnsEmptyList() throws Exception {
        // Given...
        Map<String,String[]> mockURLQuery = new HashMap<String,String[]>();
        RasQueryParameters params = new RasQueryParameters(new QueryParameters(mockURLQuery));
        mockURLQuery.put("status", new String[]{} );

        // When...

        List<TestRunLifecycleStatus> returnedResults = params.getStatusesFromParameters();

        // Then...
        assertThat(returnedResults.equals(new ArrayList<TestRunLifecycleStatus>()));
    }

    @Test
    public void testGetStatusesFromParametersWithMultipleCommasTogetherReturnsError() throws Exception {
        // Given...
        Map<String,String[]> mockURLQuery = new HashMap<>();
        mockURLQuery.put("status", new String[] { "building,,genrating,running" });
        RasQueryParameters params = new RasQueryParameters(new QueryParameters(mockURLQuery));

        // When...
        Throwable thrown = catchThrowable( () -> {
            params.getStatusesFromParameters();
        });

        // Then...
        assertThat(thrown)
            .isInstanceOf(InternalServletException.class)
            .hasMessageContaining("''")
            .hasMessageContaining("GAL5014"); // GAL5014_STATUS_NAME_NOT_RECOGNIZED
    }

    @Test
    public void testGetStatusesFromParametersWithMultipleCommasInParameterReturnsError() throws Exception {
        // Given...
        Map<String,String[]> mockURLQuery = new HashMap<>();
        mockURLQuery.put("status", new String[] { "building,running,gen,erating" });
        RasQueryParameters params = new RasQueryParameters(new QueryParameters(mockURLQuery));

        /// When...
        Throwable thrown = catchThrowable( () -> {
            params.getStatusesFromParameters();
        });

        // Then...
        assertThat(thrown)
            .isInstanceOf(InternalServletException.class)
            .hasMessageContaining("gen")
            .hasMessageContaining("GAL5014"); // GAL5014_STATUS_NAME_NOT_RECOGNIZED
    }




    //-----------------------------------------------------------------
    // Test isAscending()
    //-----------------------------------------------------------------
    @Test
    public void testSortFieldAscendingReturnsTrue() throws Exception {
        // Given...
        Map<String,String[]> map = new HashMap<String,String[]>();
        map.put("sort", new String[] {"runname:asc"} );
        RasQueryParameters params = new RasQueryParameters(new QueryParameters(map));

        // When...
        boolean isAscending = params.isAscending("runname");

        // Then...
        assertThat(isAscending).isTrue();
    }

    @Test
    public void testSortFieldNoAscOrDescValueThrowsInvalidValueError() throws Exception {
        // Given...
        Map<String,String[]> map = new HashMap<String,String[]>();
        map.put("sort", new String[] {"runname:"} );
        RasQueryParameters params = new RasQueryParameters(new QueryParameters(map));

        // When...
        Throwable thrown = catchThrowable( () -> {
            params.isAscending("runname");
        });

        // Then...
        assertThat(thrown.getMessage()).contains("GAL5011","runname","sort");
    }

    @Test
    public void testSortFieldManySortValuePartsThrowsInvalidValueError() throws Exception {
        // Given...
        Map<String,String[]> map = new HashMap<String,String[]>();
        map.put("sort", new String[] {"runname:asc:desc:asc"} );
        RasQueryParameters params = new RasQueryParameters(new QueryParameters(map));

        // When...
        Throwable thrown = catchThrowable( () -> {
            params.isAscending("runname");
        });

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5011","runname:asc:desc:asc","sort");
    }

    @Test
    public void testSortFieldNoQueryDefaultFalse() throws Exception {
        // Given...
        Map<String,String[]> map = new HashMap<String,String[]>();
        RasQueryParameters params = new RasQueryParameters(new QueryParameters(map));

        // When...
        boolean isAscending = params.isAscending("runname");

        // Then...
        assertThat(isAscending).isFalse();
    }

    @Test
    public void testSortFieldNoValueReturnsDefaultNull() throws Exception {
        // Given...
        Map<String,String[]> map = new HashMap<String,String[]>();
        map.put("sort", new String[] {""} );
        RasQueryParameters params = new RasQueryParameters(new QueryParameters(map));

        // When...
        RasSortField sortField = params.getSortValue();

        // Then...
        assertThat(sortField).isNull();
    }

    @Test
    public void testGetGeneralQueryParameters() throws Exception {
        // Given...
        Map<String,String[]> map = new HashMap<String,String[]>();
        map.put("sort", new String[] {"to:asc"} );
        map.put("runname", new String[] {"C1234"} );
        map.put("requestor", new String[] {"user"} );
        QueryParameters queryParams = new QueryParameters(map);
        RasQueryParameters params = new RasQueryParameters(queryParams);

        // When...
        QueryParameters returnedParameters = params.getGeneralQueryParameters();

        // Then...
        assertThat(returnedParameters).isEqualTo(queryParams);
    }
}