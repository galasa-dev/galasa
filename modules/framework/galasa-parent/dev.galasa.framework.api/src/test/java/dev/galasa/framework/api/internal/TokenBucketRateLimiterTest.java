/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.internal;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;

import org.junit.Test;

import dev.galasa.framework.api.common.mocks.MockTimeService;

public class TokenBucketRateLimiterTest {

    @Test
    public void testTryToAcquireTokenWithoutExceedingLimitReturnsTrue() throws Exception {
        // Given...
        Instant currentTime = Instant.now();
        MockTimeService mockTimeService = new MockTimeService(currentTime);

        int bucketCapacity = 10;
        int refillRate = 10;
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(bucketCapacity, refillRate, mockTimeService);

        // When...
        boolean isTokenAcquired = rateLimiter.tryToAcquireToken();

        // Then...
        assertThat(isTokenAcquired).isTrue();
    }

    @Test
    public void testTryToAcquireTokenExceedingLimitReturnsFalse() throws Exception {
        // Given...
        Instant currentTime = Instant.now();
        MockTimeService mockTimeService = new MockTimeService(currentTime);

        int bucketCapacity = 1;
        int refillRate = 1;
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(bucketCapacity, refillRate, mockTimeService);

        // When/Then...
        // The second call should return false since the bucket only stores one token
        assertThat(rateLimiter.tryToAcquireToken()).isTrue();
        assertThat(rateLimiter.tryToAcquireToken()).isFalse();
    }

    @Test
    public void testTryToAcquireTokenMultipleRequestsWithoutExceedingLimitReturnsTrue() throws Exception {
        // Given...
        Instant currentTime = Instant.now();
        MockTimeService mockTimeService = new MockTimeService(currentTime);

        int bucketCapacity = 5;
        int refillRate = 2;
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(bucketCapacity, refillRate, mockTimeService);

        // When/Then...
        // The bucket has five tokens, so five calls should all return true
        assertThat(rateLimiter.tryToAcquireToken()).isTrue();
        assertThat(rateLimiter.tryToAcquireToken()).isTrue();
        assertThat(rateLimiter.tryToAcquireToken()).isTrue();
        assertThat(rateLimiter.tryToAcquireToken()).isTrue();
        assertThat(rateLimiter.tryToAcquireToken()).isTrue();
    }

    @Test
    public void testRateLimiterRefillsTokensOverTime() throws Exception {
        // Given...
        Instant currentTime = Instant.now();
        MockTimeService mockTimeService = new MockTimeService(currentTime);

        int bucketCapacity = 3;

        // Set the refill rate to one token per second
        int refillRate = 1;
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(bucketCapacity, refillRate, mockTimeService);

        // When...
        // Three calls are made to use up all tokens in the bucket 
        assertThat(rateLimiter.tryToAcquireToken()).isTrue();
        assertThat(rateLimiter.tryToAcquireToken()).isTrue();
        assertThat(rateLimiter.tryToAcquireToken()).isTrue();

        // All tokens have been used, so the next call should return false
        assertThat(rateLimiter.tryToAcquireToken()).isFalse();

        // Advance time by one second
        mockTimeService.sleepMillis(1 * 1000L);

        // The bucket should have been refilled with one new token
        assertThat(rateLimiter.tryToAcquireToken()).isTrue();
        assertThat(rateLimiter.tryToAcquireToken()).isFalse();

        // Advance time by two seconds
        mockTimeService.sleepMillis(2 * 1000L);

        // The bucket should have been refilled with two new tokens
        assertThat(rateLimiter.tryToAcquireToken()).isTrue();
        assertThat(rateLimiter.tryToAcquireToken()).isTrue();
        assertThat(rateLimiter.tryToAcquireToken()).isFalse();
    }

    @Test
    public void testRateLimiterDoesNotOverfillTokensBucket() throws Exception {
        // Given...
        Instant currentTime = Instant.now();
        MockTimeService mockTimeService = new MockTimeService(currentTime);

        int bucketCapacity = 3;

        // Set the refill rate to one token per second
        int refillRate = 1;
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(bucketCapacity, refillRate, mockTimeService);

        // When...
        // Three calls are made to use up all tokens in the bucket 
        assertThat(rateLimiter.tryToAcquireToken()).isTrue();
        assertThat(rateLimiter.tryToAcquireToken()).isTrue();
        assertThat(rateLimiter.tryToAcquireToken()).isTrue();

        // All tokens have been used, so the next call should return false
        assertThat(rateLimiter.tryToAcquireToken()).isFalse();

        // Advance time by three seconds
        mockTimeService.sleepMillis(3 * 1000L);

        // The bucket should have been refilled with three new token
        assertThat(rateLimiter.tryToAcquireToken()).isTrue();
        assertThat(rateLimiter.tryToAcquireToken()).isTrue();
        assertThat(rateLimiter.tryToAcquireToken()).isTrue();
        assertThat(rateLimiter.tryToAcquireToken()).isFalse();

        // Advance time by 1 minute
        mockTimeService.sleepMillis(60 * 1000L);

        // The bucket should only have been refilled with three tokens
        assertThat(rateLimiter.tryToAcquireToken()).isTrue();
        assertThat(rateLimiter.tryToAcquireToken()).isTrue();
        assertThat(rateLimiter.tryToAcquireToken()).isTrue();
        assertThat(rateLimiter.tryToAcquireToken()).isFalse();
    }
}
