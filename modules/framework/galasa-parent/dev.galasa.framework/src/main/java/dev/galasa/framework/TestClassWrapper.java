/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.GenericMethodWrapper.Type;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.teststructure.TestMethod;
import dev.galasa.framework.spi.teststructure.TestStructure;

/**
 * Representation of the test class
 *
 */
public class TestClassWrapper {

    private Log                             logger             = LogFactory.getLog(TestClassWrapper.class);

    private final String                    testBundle;
    private final Class<?>                  testClass;
    private Object                          testClassObject;

    private Result                          resultData;

    private ArrayList<GenericMethodWrapper> beforeClassMethods = new ArrayList<>();
    private ArrayList<TestMethodWrapper>    testMethods        = new ArrayList<>();
    private ArrayList<GenericMethodWrapper> afterClassMethods  = new ArrayList<>();

    private static final String BEFORE_CLASS_ANNOTATION_TYPE = "L" + dev.galasa.BeforeClass.class.getName().replaceAll("\\.", "/") + ";";
    private static final String BEFORE_ANNOTATION_TYPE = "L" + dev.galasa.Before.class.getName().replaceAll("\\.", "/") + ";";
    private static final String TEST_ANNOTATION_TYPE = "L" + dev.galasa.Test.class.getName().replaceAll("\\.", "/") + ";";
    private static final String AFTER_ANNOTATION_TYPE = "L" + dev.galasa.After.class.getName().replaceAll("\\.", "/") + ";";
    private static final String AFTER_CLASS_ANNOTATION_TYPE = "L" + dev.galasa.AfterClass.class.getName().replaceAll("\\.", "/") + ";";

    // Logger statics
    public static final String  LOG_STARTING   = "Starting";
    public static final String  LOG_ENDING     = "Ending";
    public static final String  LOG_START_LINE = "\n" + StringUtils.repeat("-", 23) + " ";
    public static final String  LOG_ASTERS     = StringUtils.repeat("*", 100);

    private final TestStructure testStructure;

    private final boolean       continueOnTestFailure;

    private final TestRunner    testRunner;

    /**
     * Constructor
     * 
     * @param testStructure
     * @param testRunner
     * @throws ConfigurationPropertyStoreException 
     */
    public TestClassWrapper(TestRunner testRunner, String testBundle, Class<?> testClass, TestStructure testStructure) throws ConfigurationPropertyStoreException {
        this.testRunner = testRunner;
        this.testBundle = testBundle;
        this.testClass = testClass;
        this.testStructure = testStructure;

        // Fill-in as much of the test structure as we can at this point.
        // If any failures occur after this, they will at least have the correct test name/bundle...etc attached.
        this.testStructure.setBundle(testBundle);
        this.testStructure.setTestName(testClass.getName());
        this.testStructure.setTestShortName(testClass.getSimpleName());

        this.continueOnTestFailure = this.testRunner.getContinueOnTestFailureFromCPS();
    }

    /**
     * Process the test class looking for test methods and fields that need to be
     * injected
     * 
     * @throws TestRunException
     */
    public void parseTestClass() throws TestRunException {

        logger.debug("Parsing test class...");

        ArrayList<GenericMethodWrapper> temporaryBeforeMethods = new ArrayList<>();
        ArrayList<GenericMethodWrapper> temporaryAfterMethods = new ArrayList<>();
        ArrayList<Method> temporaryTestMethods = new ArrayList<>();

        try {
            // Create a list of test classes and it's super classes
            LinkedList<Class<?>> classListList = new LinkedList<>();
            classListList.add(testClass);
            Class<?> superClass = testClass.getSuperclass();
            while (!superClass.isAssignableFrom(Object.class)) {
                classListList.add(superClass);
                superClass = superClass.getSuperclass();
            }

            Iterator<Class<?>> lit = classListList.descendingIterator();
            while (lit.hasNext()) {
                parseMethods(lit.next(), temporaryBeforeMethods, temporaryTestMethods, temporaryAfterMethods);
            }
        } catch (NoSuchMethodException | SecurityException e) {
            throw new TestRunException("Unable to process test class for methods", e);
        }

        // *** Build the wrappers for the test methods
        for (Method method : temporaryTestMethods) {
            this.testMethods
            .add(new TestMethodWrapper(method, this.testClass, temporaryBeforeMethods, temporaryAfterMethods));
        }

        // Populate more fields in the test structure so reporting has more information.
        ArrayList<TestMethod> structureMethods = new ArrayList<>();
        this.testStructure.setMethods(structureMethods);

        for (GenericMethodWrapper before : this.beforeClassMethods) {
            structureMethods.add(before.getStructure());
        }

        for (TestMethodWrapper testMethod : this.testMethods) {
            structureMethods.add(testMethod.getStructure());
        }

        for (GenericMethodWrapper after : this.afterClassMethods) {
            structureMethods.add(after.getStructure());
        }

        String report = this.testStructure.report(LOG_START_LINE);
        logger.trace("Test Class structure:-" + report);
    }

    /**
     * Instantiate test class and set field values
     * 
     * @throws TestRunException
     */
    public void instantiateTestClass() throws TestRunException {
        try {
            testClassObject = testClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NullPointerException | 
                 IllegalArgumentException | InvocationTargetException | NoSuchMethodException | 
                 SecurityException  e ) {
            throw new TestRunException("Unable to instantiate test class", e);
        }
    }

    /**
     * Run the test methods in declared order together
     * with @BeforeClass, @Before, @After and @AfterClass
     * 
     * @param managers
     * @param dss 
     * @param runName 
     * 
     * @throws TestRunException
     */
    public void runMethods(@NotNull ITestRunManagers managers, IDynamicStatusStoreService dss, String runName) throws TestRunException {

        logger.info(LOG_STARTING + LOG_START_LINE + LOG_ASTERS + LOG_START_LINE + "*** Start of test class "
                + testClass.getName() + LOG_START_LINE + LOG_ASTERS);

        try {
            managers.startOfTestClass();
        } catch (FrameworkException e) {
            throw new TestRunException("Unable to inform managers of start of test class", e);
        }

        // Run @BeforeClass methods
        runGenericMethods(managers, beforeClassMethods);

        // Proceed with the @Test methods only if the result is null (there were no @BeforeClass methods) OR
        // the result is not a full stop (i.e. a failed or env failed result).
        if (getResult() == null || !getResult().isFullStop()) {
            // Run @Test methods
            runTestMethods(managers, dss, runName);
        }

        // Run @AfterClass methods
        runGenericMethods(managers, afterClassMethods);

        try {
            Result newResult = managers.endOfTestClass(getResult(), null); // TODO pass the class level exception
            if (newResult != null) {
                logger.info("Result of test run overridden to " + newResult.getName());
                setResult(newResult, managers);
            }
        } catch (FrameworkException e) {
            throw new TestRunException("Problem with end of test class", e);
        }

        // Test result
        logger.info(LOG_ENDING + LOG_START_LINE + LOG_ASTERS + LOG_START_LINE + "*** " + getResult().getName()
        + " - Test class " + testClass.getName() + LOG_START_LINE + LOG_ASTERS);

        this.testStructure.setResult(getResult().getName());

        managers.testClassResult(getResult(), null);

        String report = this.testStructure.report(LOG_START_LINE);
        logger.trace("Finishing Test Class structure:-" + report);

        return;
    }

    /**
     * Run generic methods. These are methods annotated with @BeforeClass or @AfterClass.
     * The result is set in this test class wrapper at the end of every generic method.
     * @param managers
     * @param genericMethods
     * @throws TestRunException
     */
    private void runGenericMethods(@NotNull ITestRunManagers managers, ArrayList<GenericMethodWrapper> genericMethods) throws TestRunException {
        for (GenericMethodWrapper genericMethod : genericMethods) {
            genericMethod.invoke(managers, this.testClassObject, null);
            // Set the result so far after every generic method
            Result beforeClassMethodResult = genericMethod.getResult();
            setResult(beforeClassMethodResult, managers);
            if (genericMethod.fullStop()) {
                setResult(Result.failed(genericMethod.getType().toString() + " method failed"), managers);
                break;
            }
        }
    }

    /**
     * Run the test methods. These are methods annotated with @Test.
     * The result is set in this test class wrapper from the
     * test method wrapper after each @Test method.
     * @param managers
     * @param dss
     * @param runName
     * @throws TestRunException
     */
    private void runTestMethods(@NotNull ITestRunManagers managers, IDynamicStatusStoreService dss, String runName) throws TestRunException {
        try {
            dss.put("run." + runName + ".method.total", Integer.toString(this.testMethods.size()));

            int actualMethod = 0;
            for (TestMethodWrapper testMethod : this.testMethods) {
                actualMethod++;
                dss.put("run." + runName + ".method.current", Integer.toString(actualMethod));
                dss.put("run." + runName + ".method.name", testMethod.getName());
                // Run @Test method
                testMethod.invoke(managers, this.testClassObject, this.continueOnTestFailure, this);
                // Setting the result so far after every @Test 
                // method happens inside the testMethod class.
                if (testMethod.fullStop()) {
                    break;
                }
            }
            dss.delete("run." + runName + ".method.name");
            dss.delete("run." + runName + ".method.total");
            dss.delete("run." + runName + ".method.current");
        } catch (DynamicStatusStoreException e) {
            throw new TestRunException("Failed to update the run status", e);
        }
    }

    /**
     * Parse test class for test methods
     * 
     * @param temporaryAfterMethods
     * @param temporaryTestMethods
     * @param temporaryBeforeMethods
     * 
     * @param bcelJavaClass
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws TestRunException
     */
    private void parseMethods(Class<?> testClassXXX, List<GenericMethodWrapper> temporaryBeforeMethods,
            List<Method> temporaryTestMethods, List<GenericMethodWrapper> temporaryAfterMethods)
                    throws NoSuchMethodException, TestRunException {
        org.apache.bcel.classfile.JavaClass bcelJavaClass;
        try {
            bcelJavaClass = org.apache.bcel.Repository.lookupClass(testClassXXX);
        } catch (ClassNotFoundException e) {
            throw new TestRunException(e);
        }
        org.apache.bcel.classfile.Method[] bcelMethods = bcelJavaClass.getMethods();
        for (org.apache.bcel.classfile.Method bcelMethod : bcelMethods) {
            if (isTestMethod(bcelMethod)) {
                Method method = testClassXXX.getMethod(bcelMethod.getName());
                Annotation[] annotations = method.getAnnotations();
                for (Annotation annotation : annotations) {
                    storeMethod(method, annotation.annotationType(), temporaryBeforeMethods, temporaryTestMethods,
                            temporaryAfterMethods);
                }
            }
        }
    }

    /**
     * Check if test method has one of the test annotations
     * @param bcelMethod
     * @return
     * @throws TestRunException
     */
    private boolean isTestMethod(org.apache.bcel.classfile.Method bcelMethod) throws TestRunException {
        if (!bcelMethod.getName().equals("<init>")) {
            AnnotationEntry[] annotationEntries = bcelMethod.getAnnotationEntries();
            int testAnnotations = 0;
            for (AnnotationEntry annotationEntry : annotationEntries) {
                if (annotationEntry.getAnnotationType().equals(BEFORE_CLASS_ANNOTATION_TYPE) ||
                        annotationEntry.getAnnotationType().equals(BEFORE_ANNOTATION_TYPE) || 
                        annotationEntry.getAnnotationType().equals(TEST_ANNOTATION_TYPE) || 
                        annotationEntry.getAnnotationType().equals(AFTER_ANNOTATION_TYPE) || 
                        annotationEntry.getAnnotationType().equals(AFTER_CLASS_ANNOTATION_TYPE)) {

                    testAnnotations++;
                    if (!bcelMethod.isPublic()) {
                        throw new TestRunException("Method " + bcelMethod.getName() + " must be public");
                    }
                    if (bcelMethod.getArgumentTypes().length > 0) {
                        throw new TestRunException("Method " + bcelMethod.getName() + " should have no parameters");
                    }
                }
            }
            if (testAnnotations == 1) {
                return true;
            }
            if (testAnnotations > 1) {
                throw new TestRunException("Method " + bcelMethod.getName() + " should have a single test annotation");
            }            
        }
        return false;
    }

    /**
     * Store the test methods
     * 
     * @param method
     * @param annotationType
     */
    private void storeMethod(Method method, Class<? extends Annotation> annotationType,
            List<GenericMethodWrapper> temporaryBeforeMethods, List<Method> temporaryTestMethods,
            List<GenericMethodWrapper> temporaryAfterMethods) {
        if (annotationType == dev.galasa.BeforeClass.class) {
            beforeClassMethods.add(new GenericMethodWrapper(method, this.testClass, Type.BeforeClass));
        }
        if (annotationType == dev.galasa.AfterClass.class) {
            afterClassMethods.add(new GenericMethodWrapper(method, this.testClass, Type.AfterClass));
        }
        if (annotationType == dev.galasa.Before.class) {
            temporaryBeforeMethods.add(new GenericMethodWrapper(method, this.testClass, Type.Before));
        }
        if (annotationType == dev.galasa.After.class) {
            temporaryAfterMethods.add(new GenericMethodWrapper(method, this.testClass, Type.After));
        }
        if (annotationType == dev.galasa.Test.class) {
            temporaryTestMethods.add(method);
        }
    }

    protected void setResult(@Null Result newResult, @Null ITestRunManagers managers) {
        // If the result is already full stop (i.e. a failed or env failed result),
        // do not update the result again after this. The test methods can proceed,
        // if ContinueOnTestFailure is specified, but the result should stay as failed,
        // or it could accidentally get changed back to passed.
        if (getResult() != null && getResult().isFullStop()){
            return;
        }

        if (newResult != null) {
            String from;
            if (this.resultData == null) {
                from = "null";
            } else {
                from = this.resultData.getName();
            }
            logger.info("Result in test class wrapper changed from " + from + " to " + newResult.getName());
            
            this.resultData = newResult;
            if (managers != null ) {
                managers.setResultSoFar(newResult);
            }
        }
    }

    protected Result getResult() {
        return this.resultData;
    }

}