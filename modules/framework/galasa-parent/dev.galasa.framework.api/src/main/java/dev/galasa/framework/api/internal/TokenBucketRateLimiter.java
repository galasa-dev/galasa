/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.internal;

import java.time.Duration;
import java.time.Instant;

import dev.galasa.framework.api.IRateLimiter;
import dev.galasa.framework.spi.utils.ITimeService;

/**
 * A rate limiter that applies the Token Bucket algorithm to rate-limit API requests. It works by
 * maintaining a "bucket" that can store a fixed number of "tokens". A token represents a permit
 * to execute a request.
 * 
 * When a request is made to the API server, the bucket is checked for available tokens and if
 * the bucket is not empty, then the request will take one token from the bucket.
 * 
 * If the bucket is empty (i.e. the rate limit has been exceeded), then the request will be denied.
 * 
 * This bucket of tokens is constantly refilled over time based on a given refill rate so that more
 * requests can be made gradually.
 */
public class TokenBucketRateLimiter implements IRateLimiter {
    private final int bucketCapacity;
    private final int refillRateSeconds;
    private int tokens;

    private Instant lastRefillTIme;
    private ITimeService timeService;

    public TokenBucketRateLimiter(int bucketCapacity, int refillRate, ITimeService timeService) {
        this.bucketCapacity = bucketCapacity;
        this.timeService = timeService;
        this.refillRateSeconds = refillRate;
        this.tokens = bucketCapacity;
        this.lastRefillTIme = timeService.now();
    }

    public synchronized boolean tryToAcquireToken() {
        boolean isTokenAcquired = false;
        refillBucket();
        if (tokens > 0) {
            tokens--;
            isTokenAcquired = true;
        }
        return isTokenAcquired;
    }

    private void refillBucket() {
        Instant now = timeService.now();
        Duration timeSinceLastRefill = Duration.between(lastRefillTIme, now);

        // Check if any tokens should be added to the bucket based on the refill rate
        int tokensToAdd = (int) (timeSinceLastRefill.toSeconds() * refillRateSeconds);
        if (tokensToAdd > 0) {
            tokens = Math.min(bucketCapacity, tokens + tokensToAdd);

            // Update the last refill time so that we don't over-fill the bucket next time
            lastRefillTIme = now;
        }
    }
}
