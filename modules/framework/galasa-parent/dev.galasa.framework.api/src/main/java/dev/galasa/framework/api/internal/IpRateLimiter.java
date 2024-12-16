/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.internal;

import java.time.Instant;

import dev.galasa.framework.api.IRateLimiter;
import dev.galasa.framework.spi.utils.ITimeService;

/**
 * A rate limiter wrapper used for IP-based rate limiting to keep track of the last time
 * a client sent a request to the API server.
 */
public class IpRateLimiter implements IRateLimiter {
    private final IRateLimiter rateLimiter;
    private ITimeService timeService;
    private Instant lastAccessTime;

    public IpRateLimiter(IRateLimiter rateLimiter, ITimeService timeService) {
        this.rateLimiter = rateLimiter;
        this.timeService = timeService;
        this.lastAccessTime = timeService.now();
    }

    @Override
    public boolean tryToAcquireToken() {
        return rateLimiter.tryToAcquireToken();
    }

    public synchronized void updateLastAccessTime() {
        this.lastAccessTime = timeService.now();
    }

    public synchronized Instant getLastAccessTime() {
        return this.lastAccessTime;
    }
}
