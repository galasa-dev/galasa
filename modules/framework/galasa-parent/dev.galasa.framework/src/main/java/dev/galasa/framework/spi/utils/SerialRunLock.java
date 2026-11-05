/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.spi.DssDelete;
import dev.galasa.framework.spi.DssPropertyKeyRunNameSuffix;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreMatchException;
import dev.galasa.framework.spi.IDynamicStatusStoreKeyAccess;
import dev.galasa.framework.spi.IRun;

/**
 * The lock which stops two runs of the same test class running at the same time, for test classes
 * which are annotated with @DoNotRunInParallel.
 *
 * The lock is a key in the "framework" DSS namespace, holding the name of the run which owns it:
 * <pre>
 * dss.framework.locks.isSerial.&lt;bundleName&gt;/&lt;className&gt; = &lt;runName&gt;
 * </pre>
 *
 * The lock is claimed by the scheduler just before it allocates the run.
 * It must be released by whatever stops the run being active. Only runs which are serial
 * (see {@link IRun#isSerial()}) hold the lock.
 *
 * If the release was missed (the owner's run record is gone, or the owner is finished), the lock is stale.
 * A claim takes over a stale lock, so a missed release can't block the test class for ever.
 */
public class SerialRunLock {

    private static final Log logger = LogFactory.getLog(SerialRunLock.class);

    private static final String KEY_PREFIX = "locks.isSerial.";

    private SerialRunLock() {
    }

    /**
     * @param run the serial run.
     * @return the DSS key (without the "dss.framework." namespace prefix) of the lock for the run's test class.
     */
    private static String getKey(IRun run) {
        return KEY_PREFIX + run.getTestBundleName() + "/" + run.getTestClassName();
    }

    /**
     * Claims the lock for the run's test class if the run is serial. Only one run can own the lock at a time.
     *
     * A run which already owns the lock is treated as having claimed it. That happens when a scheduler
     * claimed the lock but failed before it could allocate the run, so the run is still queued.
     *
     * A lock owned by a run which is no longer active (its run record is gone, or it is finished) is taken over.
     *
     * @param dss the DSS in the "framework" namespace.
     * @param run the run which wants to own the lock.
     * @return true if the run is not serial, or now owns the lock. False if another run owns the lock.
     * @throws DynamicStatusStoreException if the DSS could not be updated.
     */
    public static boolean claimIfSerial(IDynamicStatusStoreKeyAccess dss, IRun run) throws DynamicStatusStoreException {
        boolean isClaimed = true;
        if (run.isSerial()) {
            String key = getKey(run);

            // A null old value means that the key must not exist yet.
            isClaimed = dss.putSwap(key, null, run.getName());
            if (!isClaimed) {
                String owner = dss.get(key);
                if (run.getName().equals(owner)) {
                    isClaimed = true;
                } else if (owner != null && isStale(dss, owner)) {
                    // The swap only succeeds if no one else changed the lock since we read it.
                    isClaimed = dss.putSwap(key, owner, run.getName());
                    if (isClaimed) {
                        logger.warn("Run " + run.getName() + " took over the lock " + key + " from run " + owner + " which is not active");
                    }
                }
            }
        }
        return isClaimed;
    }

    /**
     * A lock is stale if its owner can no longer be active: the owner's run record is gone, or the owner is finished.
     * A queued owner is not stale. It may have claimed the lock and be about to be allocated, or its scheduler may
     * have failed. Either way it is the next run to be allocated, and it will release the lock when it is done.
     */
    private static boolean isStale(IDynamicStatusStoreKeyAccess dss, String ownerRunName) throws DynamicStatusStoreException {
        String ownerStatus = dss.get("run." + ownerRunName + "." + DssPropertyKeyRunNameSuffix.STATUS);
        return ownerStatus == null || TestRunLifecycleStatus.FINISHED.toString().equals(ownerStatus);
    }

    /**
     * Releases the lock for the run's test class if the run is serial, but only if the run owns it.
     * A run which does not own the lock (it never claimed it, or already released it) leaves the lock
     * untouched, so a run can never release a lock which another run holds.
     *
     * @param dss the DSS in the "framework" namespace.
     * @param run the run which may own the lock.
     * @throws DynamicStatusStoreException if the DSS could not be updated.
     */
    public static void releaseIfSerial(IDynamicStatusStoreKeyAccess dss, IRun run) throws DynamicStatusStoreException {
        if (run != null && run.isSerial()) {
            try {
                dss.performActions(new DssDelete(getKey(run), run.getName()));
            } catch (DynamicStatusStoreMatchException e) {
                // The lock is not owned by this run, so there is nothing for this run to release.
            }
        }
    }
}
