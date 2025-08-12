/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras; 

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.spi.teststructure.TestStructure;


public class TestRASSearchCriteriaStatus {
    
    private TestStructure createMockTestStructure(String status){
			TestStructure testStructure = new TestStructure();
            testStructure.setStatus(status);
            return testStructure;
    }

    private RasSearchCriteriaStatus setupRasSearchCriteriaStatus(){
        List<TestRunLifecycleStatus> statuses = Arrays.asList(TestRunLifecycleStatus.values());
        RasSearchCriteriaStatus searchCriteria = new RasSearchCriteriaStatus(statuses);
        return searchCriteria;
    }

    /*
	*TESTS
	*/
    
    @Test
    public void testGetDefaultStatusNames(){
        //Given ...
        TestRunLifecycleStatus[] values = TestRunLifecycleStatus.values();
        RasSearchCriteriaStatus searchCriteria = setupRasSearchCriteriaStatus();
        //When ...
        List<TestRunLifecycleStatus> returnedStatuses = searchCriteria.getStatuses();
        //Then ...
        assertThat(returnedStatuses).containsExactlyInAnyOrder(values);
    } 

    @Test
    public void testCriteriaMatchedReturnsTrueWhenValidStatusInTestStructure(){
        //Given ...
        RasSearchCriteriaStatus searchCriteria = setupRasSearchCriteriaStatus();

        TestStructure testStructure = createMockTestStructure("running");
        //When ...
        boolean criteriaMatched = searchCriteria.criteriaMatched(testStructure);
        //Then ...
        assertThat(criteriaMatched).isTrue();
    } 

    @Test
    public void testCriteriaMatchedReturnsFalseWhenInvalidStatusInTestStructure(){
        //Given ...
       RasSearchCriteriaStatus searchCriteria = setupRasSearchCriteriaStatus();

        TestStructure testStructure = createMockTestStructure("smthnWrong");
        //When ...
        boolean criteriaMatched = searchCriteria.criteriaMatched(testStructure);
        //Then ...
       assertThat(criteriaMatched).isFalse();
    }

    @Test
    public void testCriteriaMatchedReturnsFalseWhenGivenNullStatusInTestStructure(){
        //Given ...
        RasSearchCriteriaStatus searchCriteria = setupRasSearchCriteriaStatus();

        TestStructure testStructure = createMockTestStructure(null);
        //When ...
        boolean criteriaMatched = searchCriteria.criteriaMatched(testStructure);
        //Then ...
        assertThat(criteriaMatched).isFalse();
    }

    @Test
    public void testMultipleCriteriaReturnsAsStringArray(){
        //Given ...
        RasSearchCriteriaStatus searchCriteria = setupRasSearchCriteriaStatus();
        //When ...
        String[] returnedStatuses = searchCriteria.getStatusesAsStrings();
        //Then ...
        assertThat(returnedStatuses).containsExactlyInAnyOrderElementsOf(List.of("queued","finished","building","generating","running","rundone","up","started","provstart","ending","cancelling","allocated","waiting"));
    }
}