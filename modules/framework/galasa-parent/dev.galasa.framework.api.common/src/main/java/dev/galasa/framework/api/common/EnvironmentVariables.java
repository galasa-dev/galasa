/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

/**
 * A class to define all environment variables used in the Galasa API server.
 */
public class EnvironmentVariables {

    /**
     * Represents the API server's external URL that is used by clients to communicate with the API server.
     */
    public static final String GALASA_EXTERNAL_API_URL = "GALASA_EXTERNAL_API_URL";

    /**
     * Represents the issuer URL of the Dex server so that the API server can communicate with Dex via OpenID Connect REST APIs.
     */
    public static final String GALASA_DEX_ISSUER = "GALASA_DEX_ISSUER";

    /**
     * Represents the hostname of the Dex server's gRPC API to allow the API server to communicate with Dex's gRPC API.
     */
    public static final String GALASA_DEX_GRPC_HOSTNAME = "GALASA_DEX_GRPC_HOSTNAME";

    /**
     * An ordered, comma-separated list of JWT claims to use when Galasa sets a username for an ecosystem user.
     */
    public static final String GALASA_USERNAME_CLAIMS = "GALASA_USERNAME_CLAIMS";

    /**
     * A comma-separated list of allowed origins that the API server is permitted to respond to.
     */
    public static final String GALASA_ALLOWED_ORIGINS = "GALASA_ALLOWED_ORIGINS";

    /**
     * The Kubernetes namespace that the Galasa service is running within so that the API server can query
     * Kubernetes resources, like deployments for monitors, that are available in its namespace.
     */
    public static final String GALASA_KUBERNETES_NAMESPACE = "NAMESPACE";
}
