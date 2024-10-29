/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;

public class DssUtils {

    private final static Log logger = LogFactory.getLog(DssUtils.class);

    public static void incrementMetric(IDynamicStatusStoreService dss, String metric) {
        try {
            incrementProperty(dss, metric);
        } catch (Exception e) {
            logger.warn("Failed to update metric", e);
        }
    }

    public static void incrementProperty(IDynamicStatusStoreService dss, String property)
            throws DynamicStatusStoreException {

        while (true) {
            long oldValue = 0;
            String sOldValue = AbstractManager.nulled(dss.get(property));
            if (sOldValue != null) {
                oldValue = Long.parseLong(sOldValue);
            }

            oldValue++;

            if (dss.putSwap(property, sOldValue, Long.toString(oldValue))) {
                return;
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new DynamicStatusStoreException("Swap wait interrupted", e);
            }
        }

    }

}
