/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api;

import static dev.galasa.framework.api.common.EnvironmentVariables.*;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockTimeService;
import dev.galasa.framework.api.internal.IpRateLimiter;
import dev.galasa.framework.api.mocks.MockScheduledExecutorService;
import dev.galasa.framework.spi.utils.ITimeService;

public class RateLimitFilterTest extends BaseServletTest {

    class MockRateLimitFilter extends RateLimitFilter {

        private MockScheduledExecutorService mockCleanupScheduler;

        public MockRateLimitFilter(Environment env, ITimeService timeService) {
            super.env = env;
            super.timeService = timeService;
            super.responseBuilder = new ResponseBuilder(env);

            this.mockCleanupScheduler = new MockScheduledExecutorService();
            super.cleanupScheduler = mockCleanupScheduler;
        }

        public MockScheduledExecutorService getMockCleanupScheduler() {
            return mockCleanupScheduler;
        }

        public ConcurrentHashMap<String, IpRateLimiter> getRateLimitersPerIp() {
            return super.rateLimitersPerIp;
        }
    }

    class MockFilterChain implements FilterChain {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            HttpServletResponse servletResponse = (HttpServletResponse) response;
            servletResponse.setStatus(200);
        }
    }

    private MockEnvironment setupMockEnv(
        int globalRequestCapacity,
        int globalRateLimit,
        int ipRequestCapacity,
        int ipRateLimit
    ) {
        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setenv(GALASA_GLOBAL_REQUEST_CAPACITY, String.valueOf(globalRequestCapacity));
        mockEnv.setenv(GALASA_GLOBAL_RATE_LIMIT, String.valueOf(globalRateLimit));

        mockEnv.setenv(GALASA_IP_REQUEST_CAPACITY, String.valueOf(ipRequestCapacity));
        mockEnv.setenv(GALASA_IP_RATE_LIMIT, String.valueOf(ipRateLimit));

        return mockEnv;
    }

    private HttpServletResponse sendMockRequestToFilter(RateLimitFilter rateLimitFilter, String clientIpAddress) throws IOException, ServletException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/ras/runs");
        mockRequest.setRemoteAddr(clientIpAddress);

        HttpServletResponse mockResponse = new MockHttpServletResponse();
        FilterChain mockChain = new MockFilterChain();

        rateLimitFilter.doFilter(mockRequest, mockResponse, mockChain);
        return mockResponse;
    }

    @Test
    public void testInitWithMissingEnvVariablesThrowsError() throws Exception {
        // Given...
        MockEnvironment mockEnv = new MockEnvironment();
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        RateLimitFilter rateLimitFilter = new MockRateLimitFilter(mockEnv, mockTimeService);

        // When...
        ServletException thrown = catchThrowableOfType(() -> {
            rateLimitFilter.init(null);
        }, ServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Failed to initialise rate limit filter");
    }

    @Test
    public void testFilterAllowsRequestWithinRateLimitOk() throws Exception {
        // Given...
        String client1IpAddress = "client1.ip.address";
        int globalRequestCapacity = 10;
        int globalRateLimit = 10;
        int ipRequestCapacity = 5;
        int ipRateLimit = 5;
        MockEnvironment mockEnv = setupMockEnv(globalRequestCapacity, globalRateLimit, ipRequestCapacity, ipRateLimit);

        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        RateLimitFilter rateLimitFilter = new MockRateLimitFilter(mockEnv, mockTimeService);

        // When...
        rateLimitFilter.init(null);
        HttpServletResponse mockResponse = sendMockRequestToFilter(rateLimitFilter, client1IpAddress);

        // Then...
        // The request should have been allowed through the filter
        assertThat(mockResponse.getStatus()).isEqualTo(200);
    }

    @Test
    public void testFilterRejectsRequestExceedingGlobalRateLimit() throws Exception {
        // Given...
        String client1IpAddress = "client1.ip.address";

        // Only allow one request per second globally
        int globalRequestCapacity = 1;
        int globalRateLimit = 1;
        int ipRequestCapacity = 5;
        int ipRateLimit = 5;
        MockEnvironment mockEnv = setupMockEnv(globalRequestCapacity, globalRateLimit, ipRequestCapacity, ipRateLimit);

        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        RateLimitFilter rateLimitFilter = new MockRateLimitFilter(mockEnv, mockTimeService);

        // When...
        rateLimitFilter.init(null);
        HttpServletResponse mockResponse = sendMockRequestToFilter(rateLimitFilter, client1IpAddress);

        // The first request should have been allowed through the filter
        assertThat(mockResponse.getStatus()).isEqualTo(200);

        // Send another request without advancing time
        mockResponse = sendMockRequestToFilter(rateLimitFilter, client1IpAddress);

        // Then...
        // The second request should have exceeded the rate limit
        ServletOutputStream outputStream = mockResponse.getOutputStream();
        assertThat(mockResponse.getStatus()).isEqualTo(429);
        checkErrorStructure(outputStream.toString(), 5429, "GAL5429E", "Rate limit exceeded.");
    }

    @Test
    public void testFilterRejectsRequestExceedingIpRateLimit() throws Exception {
        // Given...
        String client1IpAddress = "client1.ip.address";
        int globalRequestCapacity = 10;
        int globalRateLimit = 10;

        // Only allow one request per second per IP
        int ipRequestCapacity = 1;
        int ipRateLimit = 1;
        MockEnvironment mockEnv = setupMockEnv(globalRequestCapacity, globalRateLimit, ipRequestCapacity, ipRateLimit);

        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        RateLimitFilter rateLimitFilter = new MockRateLimitFilter(mockEnv, mockTimeService);

        // When...
        rateLimitFilter.init(null);
        HttpServletResponse mockResponse = sendMockRequestToFilter(rateLimitFilter, client1IpAddress);

        // The first request should have been allowed through the filter
        assertThat(mockResponse.getStatus()).isEqualTo(200);

        // Send another request without advancing time
        mockResponse = sendMockRequestToFilter(rateLimitFilter, client1IpAddress);

        // Then...
        // The second request should have exceeded the rate limit
        ServletOutputStream outputStream = mockResponse.getOutputStream();
        assertThat(mockResponse.getStatus()).isEqualTo(429);
        checkErrorStructure(outputStream.toString(), 5429, "GAL5429E", "Rate limit exceeded.");
    }

    @Test
    public void testFilterRejectsIpRateLimitedRequestAllowsRequestsFromOtherClients() throws Exception {
        // Given...
        String client1IpAddress = "client1.ip.address";
        String client2IpAddress = "client2.ip.address";
        String client3IpAddress = "client3.ip.address";
        int globalRequestCapacity = 10;
        int globalRateLimit = 10;

        // Only allow one request per second per IP
        int ipRequestCapacity = 1;
        int ipRateLimit = 1;
        MockEnvironment mockEnv = setupMockEnv(globalRequestCapacity, globalRateLimit, ipRequestCapacity, ipRateLimit);

        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        RateLimitFilter rateLimitFilter = new MockRateLimitFilter(mockEnv, mockTimeService);

        // When...
        rateLimitFilter.init(null);
        HttpServletResponse mockResponse = sendMockRequestToFilter(rateLimitFilter, client1IpAddress);

        // The first request should have been allowed through the filter
        assertThat(mockResponse.getStatus()).isEqualTo(200);
        mockResponse = sendMockRequestToFilter(rateLimitFilter, client1IpAddress);

        // The second request should have exceeded the rate limit for this IP address
        ServletOutputStream outputStream = mockResponse.getOutputStream();
        assertThat(mockResponse.getStatus()).isEqualTo(429);
        checkErrorStructure(outputStream.toString(), 5429, "GAL5429E", "Rate limit exceeded.");

        // Requests from different IP addresses should be allowed through regardless of other client rate limits
        assertThat(sendMockRequestToFilter(rateLimitFilter, client2IpAddress).getStatus()).isEqualTo(200);
        assertThat(sendMockRequestToFilter(rateLimitFilter, client3IpAddress).getStatus()).isEqualTo(200);
    }

    @Test
    public void testCleanUpStaleIpsRemovesOldIpEntriesAfterIdleTimeout() throws Exception {
        // Given...
        String client1IpAddress = "client1.ip.address";
        String client2IpAddress = "client2.ip.address";
        int globalRequestCapacity = 10;
        int globalRateLimit = 10;
        int ipRequestCapacity = 1;
        int ipRateLimit = 1;
        MockEnvironment mockEnv = setupMockEnv(globalRequestCapacity, globalRateLimit, ipRequestCapacity, ipRateLimit);

        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockRateLimitFilter rateLimitFilter = new MockRateLimitFilter(mockEnv, mockTimeService);
        ConcurrentHashMap<String, IpRateLimiter> rateLimitersPerIp = rateLimitFilter.getRateLimitersPerIp();

        // When...
        rateLimitFilter.init(null);

        // Send requests from different IP addresses to build up the recorded IP address rate limiters
        assertThat(sendMockRequestToFilter(rateLimitFilter, client1IpAddress).getStatus()).isEqualTo(200);
        assertThat(sendMockRequestToFilter(rateLimitFilter, client2IpAddress).getStatus()).isEqualTo(200);

        assertThat(rateLimitersPerIp).hasSize(2);
        assertThat(rateLimitersPerIp).containsKey(client1IpAddress);
        assertThat(rateLimitersPerIp).containsKey(client2IpAddress);

        // Advance time to exceed the IP idle timeout
        mockTimeService.sleepMillis(10 * RateLimitFilter.IP_IDLE_TIMEOUT_MILLIS);

        // Run the cleanup job
        rateLimitFilter.cleanUpStaleIps();

        // Then...
        assertThat(rateLimitersPerIp).isEmpty();
    }

    @Test
    public void testCleanUpStaleIpsRemovesOldIpEntriesAtIdleTimeoutBoundary() throws Exception {
        // Given...
        String client1IpAddress = "client1.ip.address";
        String client2IpAddress = "client2.ip.address";
        int globalRequestCapacity = 10;
        int globalRateLimit = 10;
        int ipRequestCapacity = 1;
        int ipRateLimit = 1;
        MockEnvironment mockEnv = setupMockEnv(globalRequestCapacity, globalRateLimit, ipRequestCapacity, ipRateLimit);

        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockRateLimitFilter rateLimitFilter = new MockRateLimitFilter(mockEnv, mockTimeService);
        ConcurrentHashMap<String, IpRateLimiter> rateLimitersPerIp = rateLimitFilter.getRateLimitersPerIp();

        // When...
        rateLimitFilter.init(null);

        // Send requests from different IP addresses to build up the recorded IP address rate limiters
        assertThat(sendMockRequestToFilter(rateLimitFilter, client1IpAddress).getStatus()).isEqualTo(200);
        assertThat(sendMockRequestToFilter(rateLimitFilter, client2IpAddress).getStatus()).isEqualTo(200);

        assertThat(rateLimitersPerIp).hasSize(2);
        assertThat(rateLimitersPerIp).containsKey(client1IpAddress);
        assertThat(rateLimitersPerIp).containsKey(client2IpAddress);

        // Advance time to be the same as the IP idle timeout
        mockTimeService.sleepMillis(RateLimitFilter.IP_IDLE_TIMEOUT_MILLIS);

        // Run the cleanup job
        rateLimitFilter.cleanUpStaleIps();

        // Then...
        assertThat(rateLimitersPerIp).isEmpty();
    }

    @Test
    public void testCleanUpStaleIpsDoesNotRemoveRecentIpEntries() throws Exception {
        // Given...
        String client1IpAddress = "client1.ip.address";
        String client2IpAddress = "client2.ip.address";
        int globalRequestCapacity = 10;
        int globalRateLimit = 10;
        int ipRequestCapacity = 1;
        int ipRateLimit = 1;
        MockEnvironment mockEnv = setupMockEnv(globalRequestCapacity, globalRateLimit, ipRequestCapacity, ipRateLimit);

        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockRateLimitFilter rateLimitFilter = new MockRateLimitFilter(mockEnv, mockTimeService);
        ConcurrentHashMap<String, IpRateLimiter> rateLimitersPerIp = rateLimitFilter.getRateLimitersPerIp();

        // When...
        rateLimitFilter.init(null);

        // Send requests from different IP addresses to build up the recorded IP address rate limiters
        assertThat(sendMockRequestToFilter(rateLimitFilter, client1IpAddress).getStatus()).isEqualTo(200);
        assertThat(sendMockRequestToFilter(rateLimitFilter, client2IpAddress).getStatus()).isEqualTo(200);

        assertThat(rateLimitersPerIp).hasSize(2);
        assertThat(rateLimitersPerIp).containsKey(client1IpAddress);
        assertThat(rateLimitersPerIp).containsKey(client2IpAddress);

        // Advance time to be just before the IP idle timeout
        mockTimeService.sleepMillis(RateLimitFilter.IP_IDLE_TIMEOUT_MILLIS - 1);

        // Run the cleanup job
        rateLimitFilter.cleanUpStaleIps();

        // Then...
        assertThat(rateLimitersPerIp).hasSize(2);
        assertThat(rateLimitersPerIp).containsKey(client1IpAddress);
        assertThat(rateLimitersPerIp).containsKey(client2IpAddress);
    }

    @Test
    public void testCleanUpStaleIpsDoesNotRemoveAllIpEntries() throws Exception {
        // Given...
        String client1IpAddress = "client1.ip.address";
        String client2IpAddress = "client2.ip.address";
        String client3IpAddress = "client3.ip.address";
        int globalRequestCapacity = 10;
        int globalRateLimit = 10;
        int ipRequestCapacity = 1;
        int ipRateLimit = 1;
        MockEnvironment mockEnv = setupMockEnv(globalRequestCapacity, globalRateLimit, ipRequestCapacity, ipRateLimit);

        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockRateLimitFilter rateLimitFilter = new MockRateLimitFilter(mockEnv, mockTimeService);
        ConcurrentHashMap<String, IpRateLimiter> rateLimitersPerIp = rateLimitFilter.getRateLimitersPerIp();

        // When...
        rateLimitFilter.init(null);

        // Send requests from different IP addresses to build up the recorded IP address rate limiters
        assertThat(sendMockRequestToFilter(rateLimitFilter, client1IpAddress).getStatus()).isEqualTo(200);
        assertThat(sendMockRequestToFilter(rateLimitFilter, client2IpAddress).getStatus()).isEqualTo(200);
        assertThat(sendMockRequestToFilter(rateLimitFilter, client3IpAddress).getStatus()).isEqualTo(200);

        assertThat(rateLimitersPerIp).hasSize(3);
        assertThat(rateLimitersPerIp).containsKey(client1IpAddress);
        assertThat(rateLimitersPerIp).containsKey(client2IpAddress);
        assertThat(rateLimitersPerIp).containsKey(client3IpAddress);

        // Advance time to be just before the IP idle timeout
        mockTimeService.sleepMillis(RateLimitFilter.IP_IDLE_TIMEOUT_MILLIS - 1);

        // Send a more recent request from the client1 IP address
        assertThat(sendMockRequestToFilter(rateLimitFilter, client1IpAddress).getStatus()).isEqualTo(200);

        // Advance time to exceed the IP idle timeout without timing out the recent request
        mockTimeService.sleepMillis(RateLimitFilter.IP_IDLE_TIMEOUT_MILLIS - 1);

        // Run the cleanup job
        rateLimitFilter.cleanUpStaleIps();

        // Then...
        // Only the client2 IP address should have been marked stale
        assertThat(rateLimitersPerIp).hasSize(1);
        assertThat(rateLimitersPerIp).containsKey(client1IpAddress);
    }

    @Test
    public void testDestroyFilterShutsDownCleanupThread() throws Exception {
        // Given...
        MockEnvironment mockEnv = new MockEnvironment();
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockRateLimitFilter rateLimitFilter = new MockRateLimitFilter(mockEnv, mockTimeService);

        // When...
        rateLimitFilter.destroy();

        // Then...
        MockScheduledExecutorService mockCleanupScheduler = rateLimitFilter.getMockCleanupScheduler();
        assertThat(mockCleanupScheduler.isShutdown()).isTrue();
    }
}