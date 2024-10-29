/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.galasa.framework.api.common.HttpMethod;

/**
 * An enum representing the API routes that do not require a JWT in order to send
 * requests to. Each route contains a list of allowed HTTP methods indicating the
 * methods that will not be blocked by the JWT filter.
 */
public enum UnauthenticatedRoute {

    AUTH("/auth", HttpMethod.GET, HttpMethod.POST),
    AUTH_TOKENS("/auth/tokens", HttpMethod.POST),
    AUTH_CALLBACK("/auth/callback", HttpMethod.GET),
    BOOTSTRAP("/bootstrap", HttpMethod.GET),
    BOOTSTRAP_EXTERNAL("/bootstrap/external", HttpMethod.GET),
    HEALTH("/health", HttpMethod.GET),
    OPENAPI("/openapi", HttpMethod.GET),
    ;

    private String route;
    private List<HttpMethod> allowedMethods;

    private UnauthenticatedRoute(String route, HttpMethod... allowedMethods) {
        this.route = route;
        this.allowedMethods = Arrays.asList(allowedMethods);
    }

    public static Map<String, List<String>> getRoutesAsMap() {
        Map<String, List<String>> routeMap = new HashMap<>();
        for (UnauthenticatedRoute route : values()) {
            routeMap.put(route.toString(), route.getAllowedMethods());
        }
        return routeMap;
    }

    @Override
    public String toString() {
        return this.route;
    }

    public List<String> getAllowedMethods() {
        return this.allowedMethods
            .stream()
            .map(HttpMethod::toString)
            .collect(Collectors.toList());
    }
}
