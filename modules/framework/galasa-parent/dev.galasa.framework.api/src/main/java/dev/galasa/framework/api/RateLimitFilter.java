/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.MimeType;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.SystemEnvironment;
import dev.galasa.framework.api.internal.IpRateLimiter;
import dev.galasa.framework.api.internal.TokenBucketRateLimiter;
import dev.galasa.framework.spi.utils.ITimeService;
import dev.galasa.framework.spi.utils.SystemTimeService;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;
import static dev.galasa.framework.api.common.EnvironmentVariables.*;

/**
 * A servlet filter that implements rate limiting for the Galasa API server.
 *
 * This filter uses a global rate limiter to prevent clients from exhausting the API server's
 * resources, causing it to become unresponsive. The global rate limit serves as an upper-limit
 * safety net to keep the API server running under heavy load.
 *
 * The filter also applies a rate limit to clients based on their IP addresses so that
 * a client is not able to exceed the global rate limit alone, preventing other clients from being able
 * to use the API server. This IP-based rate limit should be stricter than the global rate limit
 * so that API server resources are shared fairly amongst clients.
 *
 * Both the global and individual rate limits are configurable through environment variables.
 */
@Component(service = Filter.class, scope = ServiceScope.PROTOTYPE, property = {
        "osgi.http.whiteboard.filter.pattern=/*" }, name = "Galasa Rate Limit Filter")
public class RateLimitFilter implements Filter {

    // Mark IPs stale after 1 minute
    public static final long IP_IDLE_TIMEOUT_MILLIS = 60 * 1000L;

    // The javax HttpServletResponse class is missing the 429 status code from its constants
    private static final int TOO_MANY_REQUESTS_CODE = 429;

    private final Log logger = LogFactory.getLog(getClass());

    protected ITimeService timeService = new SystemTimeService();
    protected ResponseBuilder responseBuilder = new ResponseBuilder();

    private IRateLimiter globalRateLimiter;
    protected ConcurrentHashMap<String, IpRateLimiter> rateLimitersPerIp = new ConcurrentHashMap<>();

    private int ipRateLimitRequestCapacity;
    private int ipRefillRatePerSecond;

    protected Environment env = new SystemEnvironment();

    protected ScheduledExecutorService cleanupScheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Rate limit filter initialising");

        try {
            this.ipRateLimitRequestCapacity = Integer.parseInt(env.getenv(GALASA_IP_REQUEST_CAPACITY));
            this.ipRefillRatePerSecond = Integer.parseInt(env.getenv(GALASA_IP_RATE_LIMIT));
            logger.info("Request capacity per IP set to: " + ipRateLimitRequestCapacity);
            logger.info("Request rate limit per IP set to: " + ipRefillRatePerSecond);

            int globalRefillRatePerSecond = Integer.parseInt(env.getenv(GALASA_GLOBAL_RATE_LIMIT));
            int globalRequestCapacity = Integer.parseInt(env.getenv(GALASA_GLOBAL_REQUEST_CAPACITY));
            globalRateLimiter = new TokenBucketRateLimiter(globalRequestCapacity, globalRefillRatePerSecond, timeService);
            logger.info("Global request capacity set to: " + globalRequestCapacity);
            logger.info("Global request rate limit set to: " + globalRefillRatePerSecond);

        } catch (NumberFormatException e) {
            String formattedEnvVariablesStr = String.join(
                ", ",
                List.of(GALASA_GLOBAL_RATE_LIMIT, GALASA_GLOBAL_REQUEST_CAPACITY, GALASA_IP_REQUEST_CAPACITY, GALASA_IP_RATE_LIMIT)
            );

            String errorMsg = "Failed to initialise rate limit filter. One or more of the following environment variables have not been set valid values: ["
                + formattedEnvVariablesStr + "]";
            logger.error(errorMsg);
            throw new ServletException(errorMsg);
        }

        // Schedule periodic cleanup of stale IPs
        cleanupScheduler.scheduleAtFixedRate(this::cleanUpStaleIps, 1, 1, TimeUnit.MINUTES);
        logger.info("Rate limit filter initialised");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        logger.info("RateLimitFilter - doFilter() entered");
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;

        String clientIp = servletRequest.getRemoteAddr();

        // Check the global rate limit hasn't been exceeded
        logger.info("Checking whether the global rate limit has been exceeded");
        if (!globalRateLimiter.tryToAcquireToken()) {
            logger.info("Global rate limit has been exceeded");
            sendRateLimitExceededResponse(servletRequest, servletResponse);
        } else {
            logger.info("Global rate limit has not been exceeded, checking IP-based rate limit");

            // Check that the user's own rate limit hasn't been exceeded (based on their IP)
            rateLimitersPerIp.computeIfAbsent(clientIp, ip -> {
                IRateLimiter tokenBucketLimiter = new TokenBucketRateLimiter(ipRateLimitRequestCapacity, ipRefillRatePerSecond, timeService);
                return new IpRateLimiter(tokenBucketLimiter, timeService);
            });

            IpRateLimiter ipRateLimiter = rateLimitersPerIp.get(clientIp);
            ipRateLimiter.updateLastAccessTime();

            if (!ipRateLimiter.tryToAcquireToken()) {
                logger.info("IP-based rate limit has been exceeded");
                sendRateLimitExceededResponse(servletRequest, servletResponse);
            } else {
                // Allow the request through the filter
                logger.info("IP-based rate limit has not been exceeded");
                chain.doFilter(request, response);
            }
        }
        logger.info("RateLimitFilter - doFilter() exiting");
    }

    @Override
    public void destroy() {
        logger.info("Shutting down rate limit filter");

        cleanupScheduler.shutdown();

        logger.info("Rate limit filter shut down OK");
    }

    private void sendRateLimitExceededResponse(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        String errorString = new ServletError(GAL5429_TOO_MANY_REQUESTS).toJsonString();
        responseBuilder.buildResponse(servletRequest, servletResponse, MimeType.APPLICATION_JSON.toString(), errorString, TOO_MANY_REQUESTS_CODE);
    }

    // Package-level to allow unit testing
    void cleanUpStaleIps() {
        logger.info("Cleaning up stale IPs from rate limit checks");
        List<String> ipsToRemove = new ArrayList<>();
        for (Entry<String, IpRateLimiter> entry : rateLimitersPerIp.entrySet()) {
            if (Duration.between(entry.getValue().getLastAccessTime(), timeService.now()).toMillis() >= IP_IDLE_TIMEOUT_MILLIS) {
                ipsToRemove.add(entry.getKey());
            }
        }

        for (String ipToRemove : ipsToRemove) {
            rateLimitersPerIp.remove(ipToRemove);
        }

        int ipsToRemoveCount = ipsToRemove.size();
        if (ipsToRemoveCount > 0) {
            logger.info("Removed " + ipsToRemoveCount + " stale IP(s) OK");
        } else {
            logger.info("No stale IPs to remove");
        }
    }
}
