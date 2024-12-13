/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.mocks;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MockScheduledExecutorService implements ScheduledExecutorService {

    private boolean isShutdown = false;

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        // Do nothing...
        return null;
    }

    @Override
    public void shutdown() {
        this.isShutdown = true;
    }

    @Override
    public boolean isShutdown() {
        return this.isShutdown;
    }

    @Override
    public List<Runnable> shutdownNow() {
        throw new UnsupportedOperationException("Unimplemented method 'shutdownNow'");
    }

    @Override
    public boolean isTerminated() {
        throw new UnsupportedOperationException("Unimplemented method 'isTerminated'");
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("Unimplemented method 'awaitTermination'");
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        throw new UnsupportedOperationException("Unimplemented method 'submit'");
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        throw new UnsupportedOperationException("Unimplemented method 'submit'");
    }

    @Override
    public Future<?> submit(Runnable task) {
        throw new UnsupportedOperationException("Unimplemented method 'submit'");
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        throw new UnsupportedOperationException("Unimplemented method 'invokeAll'");
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        throw new UnsupportedOperationException("Unimplemented method 'invokeAll'");
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException("Unimplemented method 'invokeAny'");
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException("Unimplemented method 'invokeAny'");
    }

    @Override
    public void execute(Runnable command) {
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        throw new UnsupportedOperationException("Unimplemented method 'schedule'");
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        throw new UnsupportedOperationException("Unimplemented method 'schedule'");
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        throw new UnsupportedOperationException("Unimplemented method 'scheduleWithFixedDelay'");
    }
}
