/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.galasaecosystem;

import java.net.URL;
import java.net.InetSocketAddress;

/**
 * Types of Endpoints for the Galasa Ecosystem
 * 
 *  
 *
 */
public enum EcosystemEndpoint {

    /**
     * Configuration Property Store eg, etcd:http://etcd.server:2323
     */
    CPS(URL.class),
    /**
     * Dynamic Status Store eg, etcd:http://etcd.server:2323
     */
    DSS(URL.class),
    /**
     * Result Archive Store eg, etcd:http://etcd.server:2323
     */
    RAS(URL.class),
    /**
     * Credentials Store eg, couchdb:http://etcd.server:2323
     */
    CREDS(URL.class),
    /**
     * API eg, http://etcd.server:2323
     */
    API(URL.class),
    /**
     * Prometheus endpoint eg, http://etcd.server:2323
     */
    PROMETHEUS(URL.class),
    /**
     * Grafana endpoint eg, http://etcd.server:2323
     */
    GRAFANA(URL.class),
    /**
     * The metrics server metrics port eg, http://etcd.server:2323
     */
    METRICS_METRICS(URL.class),
    /**
     * The metrics server health port eg, http://etcd.server:2323
     */
    METRICS_HEALTH(URL.class),
    /**
     * The resource management health port eg, http://etcd.server:2323
     */
    RESOURCE_MANAGEMENT_METRICS(URL.class),
    /**
     * The resource management health port eg, http://etcd.server:2323
     */
    RESOURCE_MANAGEMENT_HEALTH(URL.class),
    /**
     * The engine controller metrics port eg, http://etcd.server:2323
     */
    ENGINE_CONTROLLER_METRICS(URL.class),
    /**
     * The engine controller health port eg, http://etcd.server:2323
     */
    ENGINE_CONTROLLER_HEALTH(URL.class),

    /**
     * SimBank Telnet port
     */
    SIMBANK_TELNET(InetSocketAddress.class),
    /**
     * SimBank Web Service port
     */
    SIMBANK_WEBSERVICE(URL.class),
    /**
     * SimBank Database port
     */
    SIMBANK_DATABASE(InetSocketAddress.class),
    /**
     * SimBank Database port
     */
    SIMBANK_MANAGEMENT_FACILITY(URL.class);

    private final Class<?> endpointType;
    
    private EcosystemEndpoint(Class<?> endpointType) {
        this.endpointType = endpointType;
    }
    
    /**
     * @return the type of endpoint
     */
    public Class<?> getEndpointType() {
        return this.endpointType;
    }

}
