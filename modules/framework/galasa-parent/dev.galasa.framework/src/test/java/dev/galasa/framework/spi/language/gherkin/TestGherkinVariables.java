/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import org.junit.Test;

public class TestGherkinVariables {

    @Test
    public void testProcessHeaderSplitsColumnsOkWithNoData() throws Exception {
        // Given...
        GherkinVariables varsToTest = new GherkinVariables();

        // When...
        varsToTest.processHeaderLine("|a|b|c|");

        // Then...
        Map<String,ArrayList<String>> columnMap = varsToTest.getVariables();
        assertThat(columnMap).hasSize(3);

        assertThat(columnMap.get("a")).isNotNull().isEmpty();
        assertThat(columnMap.get("b")).isNotNull().isEmpty();
        assertThat(columnMap.get("c")).isNotNull().isEmpty();
    }

    @Test 
    public void  TestProcessHeadColumnsWithSpacePaddingGetTrimmed() throws Exception {
        // Given...
        GherkinVariables varsToTest = new GherkinVariables();

        // When...
        varsToTest.processHeaderLine("| a | b | c |\n");

        // Then...
        Map<String,ArrayList<String>> columnMap = varsToTest.getVariables();
        assertThat(columnMap).hasSize(3);

        assertThat(columnMap.get("a")).isNotNull().isEmpty();
        assertThat(columnMap.get("b")).isNotNull().isEmpty();
        assertThat(columnMap.get("c")).isNotNull().isEmpty();
    }


    @Test
    public void testProcessHeaderAnd2DataLinesOk() throws Exception {
        // Given...
        GherkinVariables varsToTest = new GherkinVariables();
        varsToTest.processHeaderLine("| a | b | c |\n");

        // When...
        varsToTest.processDataLine("| 3 | 4 | 5 |\n");
        varsToTest.processDataLine("| 1 | 2 | 3 |");

        // Then...
        assertThat(varsToTest.getNumberOfInstances()).isEqualTo(2);
    }

    @Test
    public void testProcessingDataLinesCanBeGotOutUsingInstsanceNumber() throws Exception {
         // Given...
        GherkinVariables varsToTest = new GherkinVariables();
        varsToTest.processHeaderLine("| a | b | c |\n");
        varsToTest.processDataLine("| 3 | 4 | 5 |\n");
        varsToTest.processDataLine("| 1 | 2 | 3 |");

        // When...
        Map<String,Object> instance1 = varsToTest.getVariableInstance(1);
        Map<String,Object> instance0 = varsToTest.getVariableInstance(0);

        // Then...
        assertThat(instance1.get("a")).isNotNull().isEqualTo("1");     
        assertThat(instance0.get("a")).isNotNull().isEqualTo("3");
    }


    @Test
    public void testGetVariableInstanceBeyondValidInstance() throws Exception {
        // Given...
        GherkinVariables varsToTest = new GherkinVariables();
        varsToTest.processHeaderLine("| a | b | c |\n");
        varsToTest.processDataLine("| 3 | 4 | 5 |\n");
        varsToTest.processDataLine("| 1 | 2 | 3 |");

        // When...
        Throwable thrown = catchThrowable(() -> {
            varsToTest.getVariableInstance(2);
        });

        // Then...
        assertThat(thrown).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    public void testGetVariablesDoesntReturnADeepCopy() throws Exception {
        // Given...
        GherkinVariables varsToTest = new GherkinVariables();
        varsToTest.processHeaderLine("| a | b | c |\n");
        varsToTest.processDataLine("| 3 | 4 | 5 |\n");
        varsToTest.processDataLine("| 1 | 2 | 3 |");

        // When...
        Map<String,ArrayList<String>> varsOut1 = varsToTest.getVariables();

        // Then...
        Map<String,ArrayList<String>> varsOut2 = varsToTest.getVariables();
        assertThat(varsOut1.hashCode()).isEqualTo(varsOut2.hashCode());
    }

    @Test 
    public void testGetNumberOfInstancesWhenTableEmpty() throws Exception {
        // Given...
        GherkinVariables varsToTest = new GherkinVariables();
        varsToTest.processHeaderLine("| a | b | c |\n");

        // When...
        int instanceCount = varsToTest.getNumberOfInstances();

        // Then...
        assertThat(instanceCount).isEqualTo(0);
    }

}