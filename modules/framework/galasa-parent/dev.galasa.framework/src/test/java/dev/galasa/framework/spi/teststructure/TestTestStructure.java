/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.teststructure; 

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;


public class TestTestStructure {

    @Test
    public void testCanCreateTestStructureFromNothingWithDefaults() throws Exception {
        TestStructure t = new TestStructure();
        assertThat(t.getTags()).isNotNull().isEmpty();
        assertThat(t.getSubmissionId()).isNull();
        assertThat(t.getTestName()).isNull();
        assertThat(t.getTestShortName()).isNull();
        assertThat(t.getResult()).isNull();
    }

    private TestStructure getPopulatedTestStructure() throws Exception {
        TestStructure t = new TestStructure();
        t.setBundle("myBundle");
        t.addTag("tag1");
        t.addTag("tag2");
        t.setSubmissionId("mySubmissionId");
        t.setTestName("myTestName");
        t.setTestShortName("myShortName");
        t.setResult("myResult");
        return t ;
    }

    private void assertTestStructureIsPopulated(TestStructure t) throws Exception {
        assertThat(t.getBundle()).isEqualTo("myBundle");
        assertThat(t.getTags()).containsExactlyInAnyOrder("tag1","tag2");
        assertThat(t.getSubmissionId()).isEqualTo("mySubmissionId");
        assertThat(t.getTestName()).isEqualTo("myTestName");
        assertThat(t.getTestShortName()).isEqualTo("myShortName");
        assertThat(t.getResult()).isEqualTo("myResult");
    }

    @Test
    public void testCanCreateFilledInTestStructure() throws Exception {

        TestStructure t = getPopulatedTestStructure();
        assertTestStructureIsPopulated(t);
    }

    @Test
    public void testCanCloneTestStructureFromAnother() throws Exception {
        TestStructure t1 = getPopulatedTestStructure();
        TestStructure t2 = new TestStructure(t1);
        assertTestStructureIsPopulated(t2);
    }

    @Test
    public void testCanRemoveTag() throws Exception {
        TestStructure t = getPopulatedTestStructure();
        t.removeTag("tag1");
        assertThat(t.getTags()).containsExactlyInAnyOrder("tag2");
    }

    @Test
    public void testCanReportEvenWhenThereAreNoMethods() throws Exception {
        TestStructure t = getPopulatedTestStructure();
        t.setMethods(null);
        t.report("***");
    }
}
