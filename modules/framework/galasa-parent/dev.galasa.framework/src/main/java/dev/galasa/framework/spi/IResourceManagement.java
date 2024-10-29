/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import java.util.concurrent.ScheduledExecutorService;

public interface IResourceManagement {

    ScheduledExecutorService getScheduledExecutorService();

    void resourceManagementRunSuccessful();
}
