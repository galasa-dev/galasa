/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import static org.assertj.core.api.Assertions.*;

import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.*;

import javax.validation.constraints.NotNull;
import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.Resolver;
import org.apache.felix.bundlerepository.Resource;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.InvalidSyntaxException;
import dev.galasa.framework.maven.repository.spi.IMavenRepository;
import dev.galasa.framework.mocks.*;
import dev.galasa.framework.mocks.MockTestRunnerEventsProducer.ProducedEvent;
import dev.galasa.framework.spi.*;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class TestTestRunner {

    @Test 
    public void testCanCreateRunnerOK() throws Exception {
        new TestRunner();
    }

    @Test
    public void testCanRunTestRunnerOK() throws Exception {

        String TEST_STREAM_REPO_URL = "http://myhost/myRepositoryForMyRun";
        String TEST_BUNDLE_NAME = "myTestBundle";
        String TEST_CLASS_NAME = MyActualTestClass.class.getName();
        String TEST_RUN_NAME = "myTestRun";
        String TEST_STREAM = "myStreamForMyRun";
        String TEST_STREAM_OBR = "http://myhost/myObrForMyRun";
        String TEST_REQUESTOR_NAME = "daffyduck";
        boolean TEST_IS_LOCAL_RUN_TRUE = true;
        boolean IGNORE_TEST_CLASS_FALSE = false;

        MockFileSystem mockFileSystem = new MockFileSystem();
        Properties overrideProps = new Properties();
        MockIResultArchiveStore ras = new MockIResultArchiveStore("myRunId", mockFileSystem ); 

        MockIDynamicStatusStoreService dss = new MockDSSWhichDoesNothing();

        IRun run = new MockRun(
            TEST_BUNDLE_NAME, 
            TEST_CLASS_NAME , 
            TEST_RUN_NAME, 
            TEST_STREAM, 
            TEST_STREAM_OBR , 
            TEST_STREAM_REPO_URL,
            TEST_REQUESTOR_NAME,
            TEST_IS_LOCAL_RUN_TRUE
        );


        MockFrameworkRuns frameworkRuns = new MockFrameworkRuns( "myRunsGroup", List.of(run));

        MockShutableFramework framework = new MockShutableFramework(ras,dss,TEST_RUN_NAME, run, frameworkRuns );
        IConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();



        IMavenRepository mockMavenRepo = new MockMavenRepository();

        Repository repo1 = new MockRepository(TEST_STREAM_REPO_URL);
        List<Repository> repositories = List.of(repo1);

        boolean IS_RESOLVER_GOING_TO_RESOLVE_TEST_BUNDLE = true;
        Resolver resolver = new MockResolver( IS_RESOLVER_GOING_TO_RESOLVE_TEST_BUNDLE );
        Resource mockResource = new MockResource(TEST_STREAM_OBR);

        MockRepositoryAdmin mockRepoAdmin = new MockRepositoryAdmin(repositories, resolver) {
            @Override
            public Resource[] discoverResources(String filterExpr) throws InvalidSyntaxException {
                Resource[] results = new Resource[1];
                results[0] = mockResource ;
                return results;
            }
        };

        MockBundleManager mockBundleManager = new MockBundleManager();

        Map<String,MockServiceReference<?>> servicesMap = new HashMap<>();

        Map<String,Class<?>> loadedClasses = Map.of( TEST_CLASS_NAME , MyActualTestClass.class );
        MockBundle myBundle1 = new MockBundle( loadedClasses , TEST_BUNDLE_NAME );
        List<Bundle> bundles = List.of(myBundle1);
        MockBundleContext mockBundleContext = new MockBundleContext(servicesMap, bundles);


        class MockAnnotationExtractor implements IAnnotationExtractor {

            Map<String, Annotation> annotationToReturnMap = new HashMap<>();

            public <A, B extends Annotation> void addAnnotation( Class<A> testClass, Class<B> annotationClass , B toReturn) {
                String key = testClass.getName()+"-"+annotationClass.getName();
                annotationToReturnMap.put( key, toReturn );
            }

            @SuppressWarnings("unchecked")
            @Override
            public <A, B extends Annotation> B getAnnotation(Class<A> testClass, Class<B> annotationClass) {
                String key = testClass.getName()+"-"+annotationClass.getName();
                // The following type-cast is unsafe, or would be were we not in full control of all the 
                // inputs and outputs in a unit test setting...
                return  (B) annotationToReturnMap.get(key);
            }
        }

        MockAnnotationExtractor mockAnnotationExtractor = new MockAnnotationExtractor();
        dev.galasa.Test annotationToReturn = MyActualTestClass.class.getAnnotation(dev.galasa.Test.class);
        assertThat(annotationToReturn).isNotNull();

        mockAnnotationExtractor.addAnnotation(
            MyActualTestClass.class, 
            dev.galasa.Test.class,
            annotationToReturn
        );

        TestRunner runner = new TestRunner();
        runner.mavenRepository = mockMavenRepo;
        runner.repositoryAdmin = mockRepoAdmin;

        // Inject the bundle context before we run the test.
        runner.activate(mockBundleContext);

        Result testResult = Result.passed();
        
        MockTestRunManagers mockTestRunManagers = new MockTestRunManagers(IGNORE_TEST_CLASS_FALSE, testResult );

        MockTestRunnerEventsProducer mockEventsPublisher = new MockTestRunnerEventsProducer();
        
        MockTestRunnerDataProvider testRunData = new MockTestRunnerDataProvider(
            cps,
            dss,
            ras,
            run,
            framework,
            overrideProps,
            mockAnnotationExtractor,
            mockBundleManager,
            mockTestRunManagers,
            mockFileSystem,
            mockEventsPublisher
        );

        // When...
        runner.runTest(testRunData);

        /// Then...

        // Check the RAS history
        // We expect started->generating->building->provstart->running->rundone->ending->finished
        List<TestStructure> rasHistory = ras.getTestStructureHistory();
        assertThat(rasHistory).hasSize(9);

        // initial setup.
        assertThat(rasHistory.get(0)).extracting("runName","bundle", "testName", "testShortName", "requestor", "status", "result")
            .containsExactly("myTestRun",null,null, null, "daffyduck", null,null);

        // status = started
        assertThat(rasHistory.get(1)).extracting("runName","bundle", "testName", "testShortName", "requestor", "status", "result")
            .containsExactly("myTestRun",null,null, null, "daffyduck", "started",null);

        // status = generating
        assertThat(rasHistory.get(2)).extracting("runName","bundle", "testName", "testShortName", "requestor", "status", "result")
            .containsExactly("myTestRun","myTestBundle","dev.galasa.framework.MyActualTestClass", "MyActualTestClass", "daffyduck", "generating",null);

        // status = building
        assertThat(rasHistory.get(3)).extracting("runName","bundle", "testName", "testShortName", "requestor", "status", "result")
            .containsExactly("myTestRun","myTestBundle","dev.galasa.framework.MyActualTestClass", "MyActualTestClass", "daffyduck", "building",null);

        // status = provstart
        assertThat(rasHistory.get(4)).extracting("runName","bundle", "testName", "testShortName", "requestor", "status", "result")
            .containsExactly("myTestRun","myTestBundle","dev.galasa.framework.MyActualTestClass", "MyActualTestClass", "daffyduck", "provstart",null);

        // status = running
        assertThat(rasHistory.get(5)).extracting("runName","bundle", "testName", "testShortName", "requestor", "status", "result")
            .containsExactly("myTestRun","myTestBundle","dev.galasa.framework.MyActualTestClass", "MyActualTestClass", "daffyduck", "running",null);

        // status = rundone
        assertThat(rasHistory.get(6)).extracting("runName","bundle", "testName", "testShortName", "requestor", "status", "result")
            .containsExactly("myTestRun","myTestBundle","dev.galasa.framework.MyActualTestClass", "MyActualTestClass", "daffyduck", "rundone","Passed");

        // status = ending
        assertThat(rasHistory.get(7)).extracting("runName","bundle", "testName", "testShortName", "requestor", "status", "result")
            .containsExactly("myTestRun","myTestBundle","dev.galasa.framework.MyActualTestClass", "MyActualTestClass", "daffyduck", "ending","Passed");

        // status = finished
        assertThat(rasHistory.get(8)).extracting("runName","bundle", "testName", "testShortName", "requestor", "status", "result")
            .containsExactly("myTestRun","myTestBundle","dev.galasa.framework.MyActualTestClass", "MyActualTestClass", "daffyduck", "finished","Passed");

        

        // Check the DSS history
        assertThat(dss.history).as("history of activity within the DSS indicates it was used an unexpected number of times").hasSize(5);

        assertThat(dss.history.get(0)).extracting("operation","key")
            .containsExactly(MockIDynamicStatusStoreService.DssHistoryRecordType.PUT, "metrics.runs.local");

        assertThat(dss.history.get(1)).extracting("operation","key")
            .containsExactly(MockIDynamicStatusStoreService.DssHistoryRecordType.DELETE, "run.myTestRun.method.name");

        assertThat(dss.history.get(2)).extracting("operation","key")
            .containsExactly(MockIDynamicStatusStoreService.DssHistoryRecordType.DELETE, "run.myTestRun.method.total");

        assertThat(dss.history.get(3)).extracting("operation","key")
            .containsExactly(MockIDynamicStatusStoreService.DssHistoryRecordType.DELETE, "run.myTestRun.method.current");

        assertThat(dss.history.get(4)).extracting("operation","key")
            .containsExactly(MockIDynamicStatusStoreService.DssHistoryRecordType.DELETE, "run.myTestRun.heartbeat");

        assertThat(framework.isShutDown()).isTrue();

        assertThat(mockBundleManager.getLoadedBundleSymbolicNames()).hasSize(2).contains("myTestBundle","dev.galasa.core.manager");

        assertThat(mockTestRunManagers.calledCountEndOfTestRun).as("End of test run was not announced").isEqualTo(1);
        assertThat(dss.data).as("dss had more than metrics debris inside").hasSize(1).containsKey("metrics.runs.local");
        assertThat(mockTestRunManagers.calledCountShudown).as("Manager not shut down").isEqualTo(1);
        assertThat(mockTestRunManagers.calledCountProvisionGenerate).as("Manager not given a chance to provision").isEqualTo(1);
        assertThat(mockTestRunManagers.calledCountProvisionBuild).as("Manager not get a chance to build").isEqualTo(1);
        assertThat(mockTestRunManagers.calledCountProvisionStart).as("Manager not get a chance to start").isEqualTo(1);
        assertThat(mockTestRunManagers.calledCountProvisionDiscard).as("Managers not given an chance to discard the provisioning.").isEqualTo(1);
        assertThat(mockTestRunManagers.calledCountProvisionStop).isEqualTo(1);
        assertThat(mockTestRunManagers.calledCountStartOfTestClass).isEqualTo(1);
        assertThat(mockTestRunManagers.calledCountEndOfTestClass).isEqualTo(1);
        assertThat(mockTestRunManagers.calledCountEndOfTestRun).isEqualTo(1);
        assertThat(mockTestRunManagers.calledCountAnyReasonTestMethodShouldBeIgnored).isEqualTo(1);

        List<Path> listOfFiles = mockFileSystem.getListOfAllFiles();
        assertThat(listOfFiles).hasSize(2);
        assertThat(listOfFiles.get(0).toString()).isEqualTo("/my/stored/artifacts/root/framework/cps_record.properties");
        assertThat(listOfFiles.get(1).toString()).isEqualTo("/my/stored/artifacts/root/framework/overrides.properties");

        // Check the events which were published by this test run.
        // We expect started->generating->building->provstart->running->rundone->ending->(heartbeat stopped)->finished
        List<ProducedEvent> events = mockEventsPublisher.getHistory();
        assertThat(events).hasSize(9);
        assertThat(events.get(0)).extracting("testRunName","eventType","testRunLifecycleStatus")
            .contains(TEST_RUN_NAME,"TestRunLifecycleStatusChangedEvent",TestRunLifecycleStatus.STARTED);
        assertThat(events.get(1)).extracting("testRunName","eventType","testRunLifecycleStatus")
            .contains(TEST_RUN_NAME,"TestRunLifecycleStatusChangedEvent",TestRunLifecycleStatus.GENERATING);
        assertThat(events.get(2)).extracting("testRunName","eventType","testRunLifecycleStatus")
            .contains(TEST_RUN_NAME,"TestRunLifecycleStatusChangedEvent",TestRunLifecycleStatus.BUILDING);
        assertThat(events.get(3)).extracting("testRunName","eventType","testRunLifecycleStatus")
            .contains(TEST_RUN_NAME,"TestRunLifecycleStatusChangedEvent",TestRunLifecycleStatus.PROVSTART);
        assertThat(events.get(4)).extracting("testRunName","eventType","testRunLifecycleStatus")
            .contains(TEST_RUN_NAME,"TestRunLifecycleStatusChangedEvent",TestRunLifecycleStatus.RUNNING);
        assertThat(events.get(5)).extracting("testRunName","eventType","testRunLifecycleStatus")
            .contains(TEST_RUN_NAME,"TestRunLifecycleStatusChangedEvent",TestRunLifecycleStatus.RUNDONE);
        assertThat(events.get(6)).extracting("testRunName","eventType","testRunLifecycleStatus")
            .contains(TEST_RUN_NAME,"TestRunLifecycleStatusChangedEvent",TestRunLifecycleStatus.ENDING);
        assertThat(events.get(7)).extracting("testRunName","eventType","testRunLifecycleStatus")
            .contains(TEST_RUN_NAME,"TestRunLifecycleStatusChangedEvent",TestRunLifecycleStatus.FINISHED);
        assertThat(events.get(8)).extracting("testRunName","eventType","testRunLifecycleStatus")
            .contains(TEST_RUN_NAME,"TestHeartbeatStoppedEvent",null);
    }


    class MockDSSWhichDoesNothing extends MockIDynamicStatusStoreService {
        @Override
        public Map<String, String> getPrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
            return new HashMap<String,String>();
        }

        @Override
        public void put(@NotNull String key, @NotNull String value) throws DynamicStatusStoreException {
            // Do nothing.
        }
    };

    @Test
    public void testCanCleanupOkIfManagerLoadFails() throws Exception {

        String TEST_STREAM_REPO_URL = "http://myhost/myRepositoryForMyRun";
        String TEST_BUNDLE_NAME = "myTestBundle";
        String TEST_CLASS_NAME = MyActualTestClass.class.getName();
        String TEST_RUN_NAME = "myTestRun";
        String TEST_STREAM = "myStreamForMyRun";
        String TEST_STREAM_OBR = "http://myhost/myObrForMyRun";
        String TEST_REQUESTOR_NAME = "daffyduck";
        boolean TEST_IS_LOCAL_RUN_TRUE = true;
        boolean IGNORE_TEST_CLASS_FALSE = false;

        MockFileSystem mockFileSystem = new MockFileSystem();
        Properties overrideProps = new Properties();
        MockIResultArchiveStore ras = new MockIResultArchiveStore("myRunId", mockFileSystem ); 

        MockIDynamicStatusStoreService dss = new MockDSSWhichDoesNothing();

        IRun run = new MockRun(
            TEST_BUNDLE_NAME, 
            TEST_CLASS_NAME , 
            TEST_RUN_NAME, 
            TEST_STREAM, 
            TEST_STREAM_OBR , 
            TEST_STREAM_REPO_URL,
            TEST_REQUESTOR_NAME,
            TEST_IS_LOCAL_RUN_TRUE
        );


        MockFrameworkRuns frameworkRuns = new MockFrameworkRuns( "myRunsGroup", List.of(run));

        MockShutableFramework framework = new MockShutableFramework(ras,dss,TEST_RUN_NAME, run, frameworkRuns );
        IConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();



        IMavenRepository mockMavenRepo = new MockMavenRepository();

        Repository repo1 = new MockRepository(TEST_STREAM_REPO_URL);
        List<Repository> repositories = List.of(repo1);

        boolean IS_RESOLVER_GOING_TO_RESOLVE_TEST_BUNDLE = true;
        Resolver resolver = new MockResolver( IS_RESOLVER_GOING_TO_RESOLVE_TEST_BUNDLE );
        Resource mockResource = new MockResource(TEST_STREAM_OBR);

        MockRepositoryAdmin mockRepoAdmin = new MockRepositoryAdmin(repositories, resolver) {
            @Override
            public Resource[] discoverResources(String filterExpr) throws InvalidSyntaxException {
                Resource[] results = new Resource[1];
                results[0] = mockResource ;
                return results;
            }
        };

        MockBundleManager mockBundleManager = new MockBundleManager();

        Map<String,MockServiceReference<?>> servicesMap = new HashMap<>();

        Map<String,Class<?>> loadedClasses = Map.of( TEST_CLASS_NAME , MyActualTestClass.class );
        MockBundle myBundle1 = new MockBundle( loadedClasses , TEST_BUNDLE_NAME );
        List<Bundle> bundles = List.of(myBundle1);
        MockBundleContext mockBundleContext = new MockBundleContext(servicesMap, bundles);


        class MockAnnotationExtractor implements IAnnotationExtractor {

            Map<String, Annotation> annotationToReturnMap = new HashMap<>();

            public <A, B extends Annotation> void addAnnotation( Class<A> testClass, Class<B> annotationClass , B toReturn) {
                String key = testClass.getName()+"-"+annotationClass.getName();
                annotationToReturnMap.put( key, toReturn );
            }

            @SuppressWarnings("unchecked")
            @Override
            public <A, B extends Annotation> B getAnnotation(Class<A> testClass, Class<B> annotationClass) {
                String key = testClass.getName()+"-"+annotationClass.getName();
                // The following type-cast is unsafe, or would be were we not in full control of all the 
                // inputs and outputs in a unit test setting...
                return  (B) annotationToReturnMap.get(key);
            }
        }

        MockAnnotationExtractor mockAnnotationExtractor = new MockAnnotationExtractor();
        dev.galasa.Test annotationToReturn = MyActualTestClass.class.getAnnotation(dev.galasa.Test.class);
        assertThat(annotationToReturn).isNotNull();

        mockAnnotationExtractor.addAnnotation(
            MyActualTestClass.class, 
            dev.galasa.Test.class,
            annotationToReturn
        );

        TestRunner runner = new TestRunner() ;

        runner.mavenRepository = mockMavenRepo;
        runner.repositoryAdmin = mockRepoAdmin;

        // Inject the bundle context before we run the test.
        runner.activate(mockBundleContext);

        Result testResult = Result.passed();

        MockTestRunManagers mockTestRunManagers = new MockTestRunManagers(IGNORE_TEST_CLASS_FALSE, testResult );

        MockTestRunnerEventsProducer mockEventsPublisher = new MockTestRunnerEventsProducer();

        MockTestRunnerDataProvider testRunData = new MockTestRunnerDataProvider(
            cps,
            dss,
            ras,
            run,
            framework,
            overrideProps,
            mockAnnotationExtractor,
            mockBundleManager,
            mockTestRunManagers,
            mockFileSystem,
            mockEventsPublisher
        ) {

            // *********** this is the 'spicey bit' for this test, where we simulate a failure.
            @Override
            public ITestRunManagers createTestRunManagers(GalasaTest galasaTest) throws TestRunException {
                throw new NullPointerException("Simulating a bad failure in a custom manager");
            }
        };

        // When...
        TestRunException ex = catchThrowableOfType( ()->runner.runTest(testRunData), TestRunException.class);

        /// Then...
        assertThat(ex).isNotNull();      

        // Check the RAS history
        // We expect started->generating->building->provstart->running->rundone->ending->finished
        List<TestStructure> rasHistory = ras.getTestStructureHistory();
        assertThat(rasHistory).hasSize(3);

        // initial setup.
        assertThat(rasHistory.get(0)).extracting("runName","bundle", "testName", "testShortName", "requestor", "status", "result")
            .containsExactly("myTestRun",null,null, null, "daffyduck", null,null);

        // status = started
        assertThat(rasHistory.get(1)).extracting("runName","bundle", "testName", "testShortName", "requestor", "status", "result")
            .containsExactly("myTestRun",null,null, null, "daffyduck", "started",null);

        // status = finished
        assertThat(rasHistory.get(2)).extracting("runName","bundle", "testName", "testShortName", "requestor", "status", "result")
            .containsExactly("myTestRun","myTestBundle","dev.galasa.framework.MyActualTestClass", "MyActualTestClass", "daffyduck", "finished","EnvFail");

    }
}
