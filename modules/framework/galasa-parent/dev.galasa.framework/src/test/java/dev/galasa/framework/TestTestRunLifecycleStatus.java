/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.util.*;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

public class TestTestRunLifecycleStatus {

    @Test
    public void testGetStatusFromStringFinished() {
        TestRunLifecycleStatus statusGotBack = TestRunLifecycleStatus.getFromString("finished");
        assertThat(statusGotBack).isNotNull().isEqualTo(TestRunLifecycleStatus.FINISHED);
    }

    @Test
    public void testGetStatusFromStringFinishedMatchesWithMixedCase() {
        TestRunLifecycleStatus statusGotBack = TestRunLifecycleStatus.getFromString("fINisheD");
        assertThat(statusGotBack).isNotNull().isEqualTo(TestRunLifecycleStatus.FINISHED);
    }

    @Test
    public void testGetStatusFromStringReturnsNullForGarbageInput() {
        TestRunLifecycleStatus statusGotBack = TestRunLifecycleStatus.getFromString("garbage");
        assertThat(statusGotBack).isNull();
    }


    @Test
    public void testIsStatusValidPassesUsingFinished() {
        boolean isValueGotBack = TestRunLifecycleStatus.isStatusValid("finished");
        assertThat(isValueGotBack).isTrue();
    }

    @Test
    public void testIsStatusValidFailsUsingRubbish() {
        boolean isValueGotBack = TestRunLifecycleStatus.isStatusValid("garbage");
        assertThat(isValueGotBack).isFalse();
    }

    @Test
    public void testToStringOnFinishedEnum() {
        String gotBack = TestRunLifecycleStatus.FINISHED.toString();
        assertThat(gotBack).isEqualTo("finished");
    }

    @Test
    public void testAllAsStringListContainsAFewWeCheck() {
        List<String> gotBackStrings = TestRunLifecycleStatus.getAllAsStringList();
        assertThat(gotBackStrings).contains("finished","building","ending");
    }

}
