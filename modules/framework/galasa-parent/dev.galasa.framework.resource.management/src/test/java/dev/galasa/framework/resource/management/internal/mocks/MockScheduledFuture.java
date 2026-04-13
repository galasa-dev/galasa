/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal.mocks;

import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MockScheduledFuture implements ScheduledFuture<Object> {

    private boolean isCancelled = false;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        this.isCancelled = true;
        return true;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public Object get() {
        return null;
    }

    @Override
    public Object get(long timeout, TimeUnit unit) {
        return null;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return 0;
    }

    @Override
    public int compareTo(Delayed o) {
        return 0;
    }
}
