/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.ManagerException;
import dev.galasa.framework.IResult;
import dev.galasa.framework.spi.language.GalasaMethod;
import dev.galasa.framework.spi.language.GalasaTest;

/**
 * <p>
 * This is the main interface used by the Framework to drive the Manager
 * lifecycle.
 * </p>
 *
 * <p>
 * For the Framework to find a Manager, the Manager must register this interface
 * as an OSGi Service Component, normally using the
 * {@link org.osgi.service.component.annotations.Component} annotation.
 * </p>
 *
 *  
 *
 */
public interface IManager {

    /**
     * <p>
     * Gives the ability for the Manager to request additional bundles to be loaded
     * dependent of CPS Properties. This can be used to load OSGi fragment bundles
     * for implements of the TPI/SPI this Manager provides.
     * </p>
     *
     * <p>
     * For example, the zOS Manager provides the zOS Batch Manager TPI. This TPI can
     * be provided via various means, the default being zOS/MF, which is implemented
     * by the zOS Batch zOSMF Manager. The zOS Manager can look at the CPS to
     * determine which "fragment" should used to implement the the TPI
     * </p>
     *
     * <p>
     * This method is called only once during the lifecycle and should not be
     * dependent on what is in the Test Class
     * </p>
     *
     * @param framework Full initialised Framework
     * @return null for no extra bundles, or a {@link java.util.List} contain the
     *         symbolic names of the bundles to load
     */
    List<String> extraBundles(@NotNull IFramework framework) throws ManagerException;

    /**
     *
     * <p>
     * Initialise the Manager, if required. The Manager should examine the testClass
     * and see if this manager should participate in the Test Run lifecycle. If the
     * Manager needs to take part, it should add itself to the activeManager. A
     * Manager add itself only to the activeManagers.
     * </p>
     *
     * <p>
     * If a Manager is dependent on another Manager, it should look for implementers
     * in allManager and call the youAreRequired method of the other Manager. If the
     * required Manager has not yet been initialised, it should flag the
     * youAreRequired call for when the Manager is initialised
     * </p>
     *
     * <p>
     * This will be the only time the testClass is passed to the Manager, so should
     * be preserved
     * </p>
     * 
     * @param framework      A fully initialised Framework - preserve it for later
     *                       use
     * @param allManagers    All Managers found in OSGi
     * @param activeManagers The Manager should add itself to this list if it is to
     *                       be activated. Do not add other managers.
     * @param galasaTest     The Test class the framework will be running
     * @throws ManagerException If there is a problem initialising the Manager
     */
    void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException;
    
    /**
     * The framework will check each manager during Shared Environment Build to see if they support shared environments.
     * Those Managers that don't provision resources should return true.
     * Those Managers provision resources that can't be reused in a shared environment should return false.
     * AbsrtactManager will return false by default
     * 
     * @return true if you support shared environments, or false if not
     */
    boolean doYouSupportSharedEnvironments();

    /**
     * <p>
     * Called if another Manager requires this one. If this Manager has not been
     * initialised yet, this call should be flagged until it the initialise method
     * is called
     * <p>
     *
     * <p>
     * If the Manager has previously been intialised, but believed it wasn't
     * required, then it should drive the initialise routines now.
     * </p>
     *
     * @param allManagers    All Managers found in OSGi
     * @param activeManagers The Manager should add itself to this list if it is to
     *                       be activated. Do not add other managers.
     * @param galasaTest     The Test class the framework will be running
     */
    void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException;

    /**
     * Is this Manager dependent on the other Manager. Basically, return true if you
     * require the other Manager at all during the provision* lifecycle methods.
     *
     * @param otherManager The other Manager
     * @return true this Manager is provisionally dependent on the other Manager
     */
    boolean areYouProvisionalDependentOn(@NotNull IManager otherManager);

    /**
     * <p>
     * Should the test class be run at all? Used to set the Test Class to Ignored.
     * Examples of use would be if the CICS TS Version is too low for the
     * functionality this Test is testing
     * </p>
     *
     * @return - Return a descriptive text string for the reason the Test should be
     *         ignored
     * @throws ManagerException - Just in case something goes wrong, eg CPS
     */
    String anyReasonTestClassShouldBeIgnored() throws ManagerException;

    /**
     * <p>
     * Provision resource names, resource pools, settings etc, ready for building.
     * </p>
     *
     * <p>
     * No building should occur during this process. Only resolution activities
     * should occur so all managers indicate they have all the resources they
     * require before attempting to build anything. If a manager is unable to
     * acquire any resource, it should throw ResourceUnavailableException and the
     * Test Run will be put into Waiting state for a later retry, if in Automation
     * </p>
     *
     * @throws ManagerException             If anything goes wrong
     * @throws ResourceUnavailableException IF resources are unavailable for this
     *                                      run
     */
    void provisionGenerate() throws ManagerException, ResourceUnavailableException;

    /**
     * <p>
     * Build everything that is needed for this Test Run. The Managers will be
     * called in the order resolved by the areYouProvisionalDependentOn method.
     * </p>
     *
     * <p>
     * If this method fails, the Test Run will fail with Environmental Failure,
     * unless ResourceUnavailableException is thrown, where it is assumed it is a
     * temporary condition and the run will be put into Waiting state if in
     * Automation.
     * </p>
     *
     * @throws ManagerException If unable to build the environment
     */
    void provisionBuild() throws ManagerException, ResourceUnavailableException;

    /**
     * <p>
     * Start the provisioned environment. The Managers will be called in the order
     * resolved by the areYouProvisionalDependentOn method.
     * </p>
     *
     * <p>
     * If this method fails, the Test Run will fail with Environmental Failure,
     * unless ResourceUnavailableException is thrown, where it is assumed it is a
     * temporary condition and the run will be put into Waiting state if in
     * Automation.
     * </p>
     *
     * @throws ManagerException If unable to start the environment
     */
    void provisionStart() throws ManagerException, ResourceUnavailableException;

    /**
     * <p>
     * Called when we have instantiated the Test Class and ready to start running
     * it.
     * </p>
     *
     * @throws ManagerException On the off chance something when wrong
     */
    void startOfTestClass() throws ManagerException;

    /**
     * Fill/refill the annotated fields that belong to this Manager. This is called
     * before the start of every Test Method just incase the tester decided to
     * overwrite the field.
     *
     * @param instantiatedTestClass The Instantiated Test class to fill
     * @throws ManagerException On the off chance something when wrong
     */
    void fillAnnotatedFields(Object instantiatedTestClass) throws ManagerException;

    /**
     * <p>
     * Should the Test Method be run at all? Used to set the Test Method to Ignored.
     * Examples of use would be if the CICS TS version is too low for the
     * functionality this Test Method is testing
     * </p>
     *
     * @return - Return a descriptive text string for the reason the Test Method
     *         should be ignored
     * @throws ManagerException - Just in case something goes wrong, eg CPS
     */
    String anyReasonTestMethodShouldBeIgnored(@NotNull GalasaMethod galasaMethod) throws ManagerException;

    /**
     * Called when we are about to start the Test Method.
     * 
     * @param galasaMethod The current executing method 
     *
     * @throws ManagerException On the off chance something when wrong
     */
    void startOfTestMethod(@NotNull GalasaMethod galasaMethod) throws ManagerException;

    /**
     * <p>
     * Called when the Test Method has finished
     * </p>
     *
     * <p>
     * If the Manager would like to override the result, it can return the result it
     * wishes to be used for the record
     * </p>
     * 
     * @param galasaMethod       The current test method
     * @param currentResult    What the current result is, will not include what the
     *                         other Managers wish it to be.
     * @param currentException What the current Exception is
     * @return Override the test result, or null if ok
     * @throws ManagerException If something went wrong
     */
    Result endOfTestMethod(@NotNull GalasaMethod galasaMethod, @NotNull Result currentResult, Throwable currentException)
            throws ManagerException;

    /**
     * <p>
     * Called once the Test Method result is resolved, can be used to emit the
     * result to another server, for example
     * </p>
     *
     * @param finalResult    The final resolved result
     * @param finalException The Exception
     * @throws ManagerException If something went wrong with recording the result
     */
    void testMethodResult(@NotNull String finalResult, Throwable finalException) throws ManagerException;

    /**
     * <p>
     * Called when the Test Class has finished
     * </p>
     *
     * <p>
     * If the Manager would like to override the result, it can return the result it
     * wishes to be used for the record
     * </p>
     *
     * @param currentResult    What the current result is, will not include what the
     *                         other Managers wish it to be.
     * @param currentException What the current Exception is
     * @return Override the test result, or null if ok
     * @throws ManagerException If something went wrong
     */
    Result endOfTestClass(@NotNull Result currentResult, Throwable currentException) throws ManagerException;

    /**
     * <p>
     * Called once the Test Class result is resolved, can be used to emit the result
     * to another server, for example
     * </p>
     *
     * @param finalResult    The final resolved result
     * @param finalException The Exception
     * @throws ManagerException If something went wrong with recording the result
     */
    void testClassResult(@NotNull String finalResult, Throwable finalException) throws ManagerException;

    /**
     * <p>
     * Stop the provisioned environment. Called in reverse provide dependent order.
     * </p>
     *
     * <p>
     * Called after recording the Test Class result as failure here should not
     * affect the Test result
     * </p>
     */
    void provisionStop();

    /**
     * <p>
     * Discard the provisioned environment. Called in reverse provide dependent
     * order.
     * </p>
     *
     * <p>
     * The Manager is free to clean up resources at this point, if it is reasonably
     * quick so that it doesn't slow the throughput of Tests through the automation
     * system and can be executed under the Testers credentials if running locally.
     * </p>
     */
    void provisionDiscard();

    /**
     * <p>
     * Can be used to perform Failure Analysis at this point and record the results
     * in the RAS.
     * </p>
     */
    void performFailureAnalysis();

    /**
     * <p>
     * About to shutdown everything
     * </p>
     */
    void endOfTestRun();
    
    /**
     * Gives the Managers the opportunity to close everything down like http clients etc.   Managers must not call 
     * other Managers in this method as they may have shutdown already.  Calls to the Framework, CPS, RAS etc will be safe.
     */
    void shutdown();

    /**
     * The result has changed, it could change again. It could be set the the same thing multiple times.
     * This call is used to propogate the very latest overall test result state down to the managers, so they know the test state.
     * This can be used by the @TestResultProvider annotation for example, to maintain a 'current test result' which tests themselves, 
     * and @AfterClass methods can use.
     * 
     * @param newResult
     * 
     * @since 0.41.0
     */
    default void setResultSoFar(IResult newResult) {}

}