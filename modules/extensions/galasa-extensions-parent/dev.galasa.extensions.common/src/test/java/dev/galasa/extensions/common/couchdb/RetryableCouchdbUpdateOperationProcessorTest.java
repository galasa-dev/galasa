package dev.galasa.extensions.common.couchdb;

import java.time.Instant;

import org.junit.Test;

import dev.galasa.extensions.common.couchdb.RetryableCouchdbUpdateOperationProcessor.BackoffTimeCalculator;
import dev.galasa.extensions.common.couchdb.RetryableCouchdbUpdateOperationProcessor.RetryableCouchdbUpdateOperation;
import dev.galasa.extensions.common.mocks.MockLogFactory;
import dev.galasa.extensions.common.mocks.MockTimeService;

import static org.assertj.core.api.Assertions.*;

public class RetryableCouchdbUpdateOperationProcessorTest {


    @Test
    public void testSuccessfulUpdateDoesNotThrowException() throws Exception {
        MockTimeService mockTimeService = new MockTimeService(Instant.EPOCH);
        MockLogFactory mockLogFactory = new MockLogFactory();

        int attemptsBeforeGivingUp = 10;
        BackoffTimeCalculator backoffTimeCalculator = new BackoffTimeCalculator() {};

        RetryableCouchdbUpdateOperationProcessor processor = new RetryableCouchdbUpdateOperationProcessor(mockTimeService, mockLogFactory);

        RetryableCouchdbUpdateOperation operationThatPasses = new RetryableCouchdbUpdateOperation() {
            @Override
            public void tryToUpdateCouchDb() throws CouchdbException, CouchdbClashingUpdateException  {
                // Simulate successful update
            }
        };

        // When...
        processor.retryCouchDbUpdateOperation(operationThatPasses, attemptsBeforeGivingUp, backoffTimeCalculator);

        // Then...
        // No errors should have been thrown
        assertThat(mockTimeService.now()).as("time passed, procesor waited when it should not have done so.").isEqualTo(Instant.EPOCH);
        assertThat(mockLogFactory.toString()).as("retry processor logged something, when nothing was expected if the retry operation passes first time.").isBlank();
    }

    @Test
    public void testRetriesUntilItGivesUp() throws Exception {
        MockTimeService mockTimeService = new MockTimeService(Instant.EPOCH);
        MockLogFactory mockLogFactory = new MockLogFactory();
        int attemptsBeforeGivingUp = 10;

        // A backoff of 1ms each time, so there is no random element in a unit test, and we can compare the time delayed later.
        BackoffTimeCalculator backoffTimeCalculator = new BackoffTimeCalculator() {
            public long getBackoffDelayMillis() {
                return 1;
            }
        };

        RetryableCouchdbUpdateOperationProcessor processor = new RetryableCouchdbUpdateOperationProcessor(mockTimeService, mockLogFactory);

        RetryableCouchdbUpdateOperation operationThatFails = new RetryableCouchdbUpdateOperation() {
            @Override
            public void tryToUpdateCouchDb() throws CouchdbException, CouchdbClashingUpdateException {
                throw new CouchdbClashingUpdateException("simulating constant failures");
            }
        };

        // When...
        CouchdbException thrown = catchThrowableOfType(() -> {
            processor.retryCouchDbUpdateOperation(operationThatFails, attemptsBeforeGivingUp, backoffTimeCalculator);
        }, CouchdbException.class);

        // Then
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Couchdb operation failed after 10 attempts");

        // We expect 10 backoff attempts, so the time would have advanced by 10 times the backoff time, which is a constant in this test of 1ms
        assertThat(mockTimeService.now()).as("time passed, procesor waited when it should not have done so.").isEqualTo(Instant.EPOCH.plusMillis(attemptsBeforeGivingUp));

        assertThat(mockLogFactory.toString()).as("retry processor didn't log what we expected. ").contains("Couchdb operation failed after 10 attempts","due to conflicts.");
    }

    @Test
    public void testOperationThatPassesAfterFailureDoesNotThrowError() throws Exception {
        MockTimeService mockTimeService = new MockTimeService(Instant.EPOCH);
        MockLogFactory mockLogFactory = new MockLogFactory();
        RetryableCouchdbUpdateOperationProcessor processor = new RetryableCouchdbUpdateOperationProcessor(mockTimeService, mockLogFactory);
        int attemptsBeforeGivingUp = 10;
        BackoffTimeCalculator backoffTimeCalculator = new BackoffTimeCalculator() {
            public long getBackoffDelayMillis() {
                return 1;
            }
        };

        RetryableCouchdbUpdateOperation operationThatPassesAfterFailure = new RetryableCouchdbUpdateOperation() {
            private boolean hasTriedUpdating = false;

            @Override
            public void tryToUpdateCouchDb() throws CouchdbException, CouchdbClashingUpdateException {
                if (!hasTriedUpdating) {
                    hasTriedUpdating = true;
                    throw new CouchdbClashingUpdateException("simulating constant failures");
                }
            }
        };

        // When...
        processor.retryCouchDbUpdateOperation(operationThatPassesAfterFailure, attemptsBeforeGivingUp, backoffTimeCalculator);

        // Then
        // No errors should have been thrown
    }

    @Test
    public void testDefaultBackoffTimeCalculatorGivesNumbersWithinExpectedRange() throws Exception {
        BackoffTimeCalculator backoffTimeCalculator = new BackoffTimeCalculator(){};
        for(int i=0; i<100; i++) {
            long millis = backoffTimeCalculator.getBackoffDelayMillis();
            assertThat(millis).isGreaterThanOrEqualTo(1000L);
            assertThat(millis).isLessThanOrEqualTo(4000L);
        }
    }

    @Test
    public void testWaitForBackOffDelayLogsInterruptedException() throws Exception {
        MockTimeService mockTimeService = new MockTimeService(Instant.EPOCH){
            @Override
            public void sleepMillis(long millis) throws InterruptedException {
                // Simulate InterruptedException
                throw new InterruptedException();
            }
        };
        MockLogFactory mockLogFactory = new MockLogFactory();

        // A backoff of 1ms each time, so there is no random element in a unit test, and we can compare the time delayed later.
        BackoffTimeCalculator backoffTimeCalculator = new BackoffTimeCalculator() {};

        RetryableCouchdbUpdateOperationProcessor processor = new RetryableCouchdbUpdateOperationProcessor(mockTimeService, mockLogFactory);

        // When...
        processor.waitForBackoffDelay(mockTimeService, backoffTimeCalculator);

        // Then
        assertThat(mockLogFactory.toString()).as("retry processor didn't log what we expected. ").contains("Interrupted from waiting during a back-off delay. Ignoring this, but cutting our wait short.");
    }
}
