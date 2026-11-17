/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.utils;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import dev.galasa.framework.mocks.MockIDynamicStatusStoreService;
import dev.galasa.framework.mocks.MockRun;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreMatchException;
import dev.galasa.framework.spi.IDssAction;

public class SerialRunLockTest {

    private static final String LOCK_KEY = "locks.isSerial.my.bundle/my.Class";

    private static MockRun createRun(String runName, boolean isSerial) {
        MockRun run = new MockRun("my.bundle", "my.Class", runName, "stream", "obr", "http://repo", "requestor", false);
        run.setSerial(isSerial);
        return run;
    }

    @Test
    public void testSerialRunClaimsFreeLock() throws Exception {
        // Given...
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        MockRun run = createRun("U1", true);

        // When...
        boolean isClaimed = SerialRunLock.claimIfSerial(dss, run);

        // Then...
        assertThat(isClaimed).isTrue();
        assertThat(dss.get(LOCK_KEY)).isEqualTo("U1");
    }

    @Test
    public void testSerialRunCannotClaimLockOwnedByAnotherRun() throws Exception {
        // Given...
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        SerialRunLock.claimIfSerial(dss, createRun("U1", true));
        dss.put("run.U1.status", "running");
        MockRun secondRun = createRun("U2", true);

        // When...
        boolean isClaimed = SerialRunLock.claimIfSerial(dss, secondRun);

        // Then...
        assertThat(isClaimed).isFalse();
        assertThat(dss.get(LOCK_KEY)).isEqualTo("U1");
    }

    @Test
    public void testSerialRunClaimsLockItAlreadyOwns() throws Exception {
        // Given...
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        MockRun run = createRun("U1", true);
        SerialRunLock.claimIfSerial(dss, run);
        dss.put("run.U1.status", "queued");

        // When...
        boolean isClaimed = SerialRunLock.claimIfSerial(dss, run);

        // Then...
        assertThat(isClaimed).isTrue();
        assertThat(dss.get(LOCK_KEY)).isEqualTo("U1");
    }

    @Test
    public void testNonSerialRunClaimsWithoutTakingLock() throws Exception {
        // Given...
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        MockRun run = createRun("U1", false);

        // When...
        boolean isClaimed = SerialRunLock.claimIfSerial(dss, run);

        // Then...
        assertThat(isClaimed).isTrue();
        assertThat(dss.data).isEmpty();
    }

    @Test
    public void testNonSerialRunIsNotBlockedByLockOwnedByAnotherRun() throws Exception {
        // Given...
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        SerialRunLock.claimIfSerial(dss, createRun("U1", true));
        dss.put("run.U1.status", "running");

        // When...
        boolean isClaimed = SerialRunLock.claimIfSerial(dss, createRun("U2", false));

        // Then...
        assertThat(isClaimed).isTrue();
        assertThat(dss.get(LOCK_KEY)).isEqualTo("U1");
    }

    @Test
    public void testOwnerReleasesLock() throws Exception {
        // Given...
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        MockRun run = createRun("U1", true);
        SerialRunLock.claimIfSerial(dss, run);

        // When...
        SerialRunLock.releaseIfSerial(dss, run);

        // Then...
        assertThat(dss.get(LOCK_KEY)).isNull();
        assertThat(SerialRunLock.claimIfSerial(dss, createRun("U2", true))).isTrue();
    }

    @Test
    public void testRunDoesNotReleaseLockOwnedByAnotherRun() throws Exception {
        // Given...
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        SerialRunLock.claimIfSerial(dss, createRun("U1", true));

        // When...
        SerialRunLock.releaseIfSerial(dss, createRun("U2", true));

        // Then...
        assertThat(dss.get(LOCK_KEY)).isEqualTo("U1");
    }

    @Test
    public void testReleaseWhenLockIsFreeDoesNothing() throws Exception {
        // Given...
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();

        // When...
        SerialRunLock.releaseIfSerial(dss, createRun("U1", true));

        // Then...
        assertThat(dss.data).isEmpty();
    }

    @Test
    public void testNonSerialRunDoesNotReleaseLockOwnedBySameNamedRun() throws Exception {
        // Given...
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        SerialRunLock.claimIfSerial(dss, createRun("U1", true));

        // When...
        SerialRunLock.releaseIfSerial(dss, createRun("U1", false));

        // Then...
        assertThat(dss.get(LOCK_KEY)).isEqualTo("U1");
    }

    @Test
    public void testSerialRunTakesOverLockWhenOwnerRunRecordIsGone() throws Exception {
        // Given... U1 holds the lock but has no run record, so the release was missed.
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        dss.put(LOCK_KEY, "U1");

        // When...
        boolean isClaimed = SerialRunLock.claimIfSerial(dss, createRun("U2", true));

        // Then...
        assertThat(isClaimed).isTrue();
        assertThat(dss.get(LOCK_KEY)).isEqualTo("U2");
    }

    @Test
    public void testSerialRunTakesOverLockWhenOwnerIsFinished() throws Exception {
        // Given... U1 holds the lock but is finished, so the release was missed.
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        dss.put(LOCK_KEY, "U1");
        dss.put("run.U1.status", "finished");

        // When...
        boolean isClaimed = SerialRunLock.claimIfSerial(dss, createRun("U2", true));

        // Then...
        assertThat(isClaimed).isTrue();
        assertThat(dss.get(LOCK_KEY)).isEqualTo("U2");
    }

    @Test
    public void testSerialRunDoesNotTakeOverLockFromActiveOwner() throws Exception {
        for (String ownerStatus : new String[] { "queued", "allocated", "running", "waiting", "up" }) {
            // Given...
            MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
            dss.put(LOCK_KEY, "U1");
            dss.put("run.U1.status", ownerStatus);

            // When...
            boolean isClaimed = SerialRunLock.claimIfSerial(dss, createRun("U2", true));

            // Then...
            assertThat(isClaimed).as("owner status " + ownerStatus).isFalse();
            assertThat(dss.get(LOCK_KEY)).as("owner status " + ownerStatus).isEqualTo("U1");
        }
    }

    @Test
    public void testOwnerWhichWasTakenOverDoesNotReleaseTheNewOwnersLock() throws Exception {
        // Given... U2 took over the lock from U1, which is finished.
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        dss.put(LOCK_KEY, "U1");
        dss.put("run.U1.status", "finished");
        SerialRunLock.claimIfSerial(dss, createRun("U2", true));

        // When... U1 then does its late release.
        SerialRunLock.releaseIfSerial(dss, createRun("U1", true));

        // Then...
        assertThat(dss.get(LOCK_KEY)).isEqualTo("U2");
    }

    @Test
    public void testReleaseForNullRunDoesNothing() throws Exception {
        // Given...
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        SerialRunLock.claimIfSerial(dss, createRun("U1", true));

        // When...
        SerialRunLock.releaseIfSerial(dss, null);

        // Then...
        assertThat(dss.get(LOCK_KEY)).isEqualTo("U1");
    }

    @Test
    public void testFailedReleaseIsNotThrownAndLeavesTheLockForTheNextClaimToTakeOver() throws Exception {
        // Given... a DSS which fails when the lock is released, and an owner which is finished.
        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService() {
            @Override
            public void performActions(IDssAction... actions)
                    throws DynamicStatusStoreException, DynamicStatusStoreMatchException {
                throw new DynamicStatusStoreException("DSS is down");
            }
        };
        dss.put(LOCK_KEY, "U1");
        dss.put("run.U1.status", "finished");

        // When... the release fails, the failure is not thrown.
        SerialRunLock.releaseIfSerial(dss, createRun("U1", true));

        // Then... the lock is still there, and the next run takes it over as the owner is finished.
        assertThat(dss.get(LOCK_KEY)).isEqualTo("U1");
        assertThat(SerialRunLock.claimIfSerial(dss, createRun("U2", true))).isTrue();
        assertThat(dss.get(LOCK_KEY)).isEqualTo("U2");
    }
}
