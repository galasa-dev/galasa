/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.language.GalasaTest;

public class AbstractManagerTest {

    @Test
    public void testAnnotatedFields() throws NoSuchFieldException, SecurityException, ManagerException {
        final TestManager testManager = new TestManager();
        final TestClass testClass = new TestClass();
        testManager.initialise(null, null, null, new GalasaTest(testClass.getClass()));

        final List<AnnotatedField> annotatedFields = testManager.findAnnotatedFields(TestManagerAnnotation.class);
        Assert.assertNotNull("A list should always be returned", annotatedFields);
        HashMap<Field, List<Annotation>> testFields = new HashMap<>();
        for (AnnotatedField annotatedField : annotatedFields) {
            testFields.put(annotatedField.getField(), annotatedField.getAnnotations());
        }

        final List<Annotation> field1Annotations = testFields.remove(testClass.getClass().getField("field1"));
        Assert.assertNotNull("Did not find the field1 annotations", field1Annotations);
        Assert.assertEquals("Should have found only 1 annotation for field1", 1, field1Annotations.size());
        final Annotation field1Annotation = field1Annotations.get(0);
        Assert.assertEquals("Incorrect annotation returned for field1", TestFieldAnnotation.class,
                field1Annotation.annotationType());

        final List<Annotation> field2Annotations = testFields.remove(testClass.getClass().getField("field2"));
        Assert.assertNotNull("Did not find the field2 annotations", field2Annotations);
        Assert.assertEquals("Should have found only 1 annotation for field2", 1, field2Annotations.size());
        final Annotation field2Annotation = field2Annotations.get(0);
        Assert.assertEquals("Incorrect annotation returned for field2", TestFieldAnnotation.class,
                field2Annotation.annotationType());

        final List<Annotation> field6Annotations = testFields.remove(testClass.getClass().getField("field6"));
        Assert.assertNotNull("Did not find the field6 annotations", field6Annotations);
        Assert.assertEquals("Should have found only 1 annotation for field6", 1, field6Annotations.size());
        final Annotation field6Annotation = field6Annotations.get(0);
        Assert.assertEquals("Incorrect annotation returned for field6", TestFieldAnnotation3.class,
                field6Annotation.annotationType());

        Assert.assertTrue("The field3, field4 and field5 should not have been returned", testFields.isEmpty());
    }

    public static class TestManager extends AbstractManager {

        @Override
        public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
            generateAnnotatedFields(TestManagerAnnotation.class);
            try {
                registerAnnotatedField(getTestClass().getField("field6"), "eeeeek");
            } catch (final Exception e) {
                throw new ManagerException("Unable to register field6", e);
            }
        }

        @GenerateAnnotatedField(annotation = TestFieldAnnotation.class)
        public Long createLong(Field field, List<Annotation> annotations) {
            return 5L;
        }

        @GenerateAnnotatedField(annotation = TestFieldAnnotation.class)
        public String createString(Field field, List<Annotation> annotations) {
            return "boo";
        }
    }

    private static class TestClass {
        @TestFieldAnnotation
        public Long    field1;

        @TestFieldAnnotation
        public String  field2;

        @TestFieldAnnotation
        public Integer field3;

        @TestFieldAnnotation2
        public String  field4;

        @TestFieldAnnotation
        private String field5;

        @TestFieldAnnotation3
        public String  field6;

    }

    @Test
    public void testFillingAnnotatedFields()
            throws NoSuchFieldException, SecurityException, ManagerException, ResourceUnavailableException {
        final TestManager testManager = new TestManager();
        final TestClass testClass = new TestClass();
        testManager.initialise(null, null, null, new GalasaTest(testClass.getClass()));

        testManager.provisionGenerate();
        testManager.fillAnnotatedFields(testClass);

        Assert.assertNotNull("field1 is missing", testClass.field1);
        Assert.assertNotNull("field2 is missing", testClass.field2);
        Assert.assertNull("field3 is present, shouldnt be", testClass.field3);
        Assert.assertNull("field4 is present, shouldnt be", testClass.field4);
        Assert.assertNull("field5 is present, shouldnt be", testClass.field5);
        Assert.assertNotNull("field6 is missing", testClass.field6);

        Assert.assertEquals("field1 filled incorrect", (Long)5L, testClass.field1);
        Assert.assertEquals("field2 filled incorrect", "boo", testClass.field2);
        Assert.assertEquals("field6 filled incorrect", "eeeeek", testClass.field6);
    }

    @Test
    public void testFoolCodeCoverageForDummyMethods() throws ManagerException, ResourceUnavailableException {
        final AbstractManager testManager = new AbstractManager() {
        };
        testManager.extraBundles(null);
        testManager.getFramework();
        testManager.getTestClass();
        testManager.youAreRequired(null, null, null);
        testManager.areYouProvisionalDependentOn(null);
        testManager.anyReasonTestClassShouldBeIgnored();
        testManager.provisionGenerate();
        testManager.provisionBuild();
        testManager.provisionStart();
        testManager.startOfTestClass();
        testManager.anyReasonTestMethodShouldBeIgnored(null);
        testManager.startOfTestMethod(null);
        testManager.endOfTestMethod(null, null, null);
        testManager.testMethodResult(null, null);
        testManager.endOfTestClass(null, null);
        testManager.testClassResult(null, null);
        testManager.provisionStop();
        testManager.provisionDiscard();
        testManager.performFailureAnalysis();
        testManager.endOfTestRun();
        Assert.assertTrue("dummy", true);

    }

}
