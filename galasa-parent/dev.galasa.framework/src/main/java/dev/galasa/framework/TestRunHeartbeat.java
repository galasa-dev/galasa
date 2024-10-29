/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.time.Instant;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStore;

public class TestRunHeartbeat extends Thread {

    private final Log                        logger        = LogFactory.getLog(this.getClass());

    private final IFramework                 framework;
    private final IDynamicStatusStoreService dss;
    private final IResultArchiveStore        ras;
    private final String                     key;

    private String                           lastHeartbeat = null;

    private boolean                          shutdown      = false;

    protected TestRunHeartbeat(@NotNull IFramework framework) throws DynamicStatusStoreException {
        this.framework = framework;
        this.dss = this.framework.getDynamicStatusStoreService("framework");
        this.ras = this.framework.getResultArchiveStore();
        this.key = "run." + framework.getTestRunName() + ".heartbeat";

        // *** Set the initial
        setHeartbeat();
    }

    private void setHeartbeat() throws DynamicStatusStoreException {
        String newHeartbeat = Instant.now().toString();

        if (!dss.putSwap(key, lastHeartbeat, newHeartbeat)) {
            // ***
            // *** Error condition, must be another engine on this run, so we must
            // immediately terminate
            // *** Do not allow privision discard or anything else to run as this could
            // affect
            // *** the other engine
            // ***
            logger.fatal("The run heartbeat has been updated by something else");
            logger.fatal("Cannot allow provision discard to run as this could affect the other engine");
            System.exit(0);
        }

        this.lastHeartbeat = newHeartbeat;

        this.ras.flush();
    }

    public void shutdown() {
        this.shutdown = true;
    }

    @Override
    public void run() {

        long nextHeartbeat = 0;
        while (!shutdown) {
            if (System.currentTimeMillis() >= nextHeartbeat) {
                nextHeartbeat = System.currentTimeMillis() + 20000; // TODO do we been to parameterise this?

                try {
                    setHeartbeat();
                } catch (DynamicStatusStoreException e) {
                    logger.error("Heartbeat failed", e);
                    nextHeartbeat = System.currentTimeMillis() + 2000;
                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                shutdown = true;
                Thread.currentThread().interrupt();
                break;
            }
        }

    }
}