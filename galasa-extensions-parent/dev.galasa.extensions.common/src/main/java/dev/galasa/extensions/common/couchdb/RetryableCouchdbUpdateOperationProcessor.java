/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.couchdb;

import java.util.Random;

import dev.galasa.extensions.common.Errors;
import dev.galasa.extensions.common.api.LogFactory;
import dev.galasa.framework.spi.utils.ITimeService;

import org.apache.commons.logging.Log;

/**
 * Allows a lambda function to be used, and that function will be retried a number of times before giving up.
 */
public class RetryableCouchdbUpdateOperationProcessor {

    public static final int DEFAULT_MAX_ATTEMPTS_TO_GO_BEFORE_GIVE_UP = 10;

    private ITimeService timeService ;
    private Log logger;

    /**
     * Lambda supplying the code which should be repeated during successive attempts
     */
    public interface RetryableCouchdbUpdateOperation {
        /**
         * @throws CouchdbException Something went wrong and no retries should be attempted, failing by passing this error upwards to the caller.
         * @throws CouchdbClashingUpdateException Couchdb can't do the update right now, the routine will be re-tried.
         */
        public void tryToUpdateCouchDb() throws CouchdbException, CouchdbClashingUpdateException;
    }

    /**
     * Calculates how much time delay we need to leave between attempts to update.
     */
    public interface BackoffTimeCalculator {
        /**
         * @return The number of milliseconds to wait between successive re-tries of the couchdb update operation.
         */
        public default long getBackoffDelayMillis()  {
            return 1000L + new Random().nextInt(3000);
        }
    }

    public RetryableCouchdbUpdateOperationProcessor(ITimeService timeService, LogFactory logFactory ) {
        this.timeService = timeService;
        this.logger = logFactory.getLog(this.getClass());
    }

    /**
     * Pass an operation you want retried if it fails with a CouchdbClashingUpdateException, using defaults. 
     * 
     * It retries for a default number of times before giving up, with a default random backoff time between retry attempts.
     * 
     * @param retryableOperation The operation we want to retry.
     * @throws CouchdbException A failure occurred.
     */
    public void retryCouchDbUpdateOperation(RetryableCouchdbUpdateOperation retryableOperation) throws CouchdbException {
        retryCouchDbUpdateOperation(retryableOperation, DEFAULT_MAX_ATTEMPTS_TO_GO_BEFORE_GIVE_UP, new BackoffTimeCalculator() {} );
    }

    /**
     * Pass an operation you want retried if it fails with a CouchdbClashingUpdateException.
     * 
     * @param retryableOperation The operation we want to retry.
     * @param attemptsToGoBeforeGiveUp The number of times the retryable operation is attempted before eventually giving up with a failure.
     * @param backofftimeCalculator The lambda operation we consult to find out backoff times between retry attempts
     * @throws CouchdbException A failure occurred.
     */
    public void retryCouchDbUpdateOperation(RetryableCouchdbUpdateOperation retryableOperation, int attemptsToGoBeforeGiveUp, BackoffTimeCalculator backofftimeCalculator) throws CouchdbException{           
        boolean isDone = false;
        int retriesRemaining = attemptsToGoBeforeGiveUp;

        while (!isDone) {

            try {
                retryableOperation.tryToUpdateCouchDb();

                isDone = true;
            } catch (CouchdbClashingUpdateException updateClashedEx) {

                logger.info("Clashing update detected. Backing off for a short time to avoid another clash immediately. ");

                waitForBackoffDelay(timeService, backofftimeCalculator);

                retriesRemaining -= 1;
                if (retriesRemaining == 0) {
                    String msg = Errors.ERROR_GALASA_COUCHDB_UPDATED_FAILED_AFTER_RETRIES.getMessage(Integer.toString(attemptsToGoBeforeGiveUp));
                    logger.info(msg);
                    throw new CouchdbException(msg, updateClashedEx);
                } else {
                    logger.info("Failed to perform the couchdb operation, retrying...");
                }
            }
        }
    }

    void waitForBackoffDelay(ITimeService timeService2, BackoffTimeCalculator backofftimeCalculator) {
        long delayMilliSecs = backofftimeCalculator.getBackoffDelayMillis();
        try {
            logger.info("Waiting "+delayMilliSecs+" during a back-off delay. starting now.");
            timeService2.sleepMillis(delayMilliSecs);
        } catch(InterruptedException ex ) {
            logger.info("Interrupted from waiting during a back-off delay. Ignoring this, but cutting our wait short.");
        }
    }
}