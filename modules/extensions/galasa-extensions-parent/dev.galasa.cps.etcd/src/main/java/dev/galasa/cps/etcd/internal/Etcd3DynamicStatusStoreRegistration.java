/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cps.etcd.internal;

import java.net.URI;
import java.net.URISyntaxException;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.Environment;
import dev.galasa.framework.spi.IDynamicStatusStoreRegistration;
import dev.galasa.framework.spi.IFrameworkInitialisation;
import dev.galasa.framework.spi.SystemEnvironment;

/**
 * This Class is a small OSGI bean that registers the DSS store as a ETCD
 * cluster or quietly fails.
 * 
 * @author James Davies
 */
@Component(service = { IDynamicStatusStoreRegistration.class })
public class Etcd3DynamicStatusStoreRegistration implements IDynamicStatusStoreRegistration {

    private static final String MAX_GRPC_MESSAGE_SIZE_ENV_VAR = "MAX_GRPC_MESSAGE_SIZE";
    private static final int DEFAULT_MAX_GRPC_MESSAGE_SIZE = 4194304;

    private final Log logger = LogFactory.getLog(Etcd3DynamicStatusStoreRegistration.class);

    private Environment env;

    private int maxgRPCMessageSize = DEFAULT_MAX_GRPC_MESSAGE_SIZE;

    public Etcd3DynamicStatusStoreRegistration(){
        this(new SystemEnvironment());
    }

    public Etcd3DynamicStatusStoreRegistration(Environment env) {
        this.env = env;
    }

    /**
     * This intialise method is a overide that registers the correct store to the
     * framework.
     * 
     * The URI is collected from the Intialisation. If the URI is a etcd scheme then
     * it registers it as a etcd.
     * 
     * @param frameworkInitialisation - gives the registration access to the correct
     *                               URI for the dss
     * @throws DynamicStatusStoreException A failure occurred.
     */
    @Override
    public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation)
            throws DynamicStatusStoreException {
        URI dss = frameworkInitialisation.getDynamicStatusStoreUri();

        if (isEtcdUri(dss)) {
            try {
                URI uri = new URI(dss.toString().substring(5));
                setMaxgRPCMessageSizeFromEnvironmentOrDefault();
                frameworkInitialisation.registerDynamicStatusStore(new Etcd3DynamicStatusStore(uri, this.maxgRPCMessageSize));
            } catch (URISyntaxException e) {
                throw new DynamicStatusStoreException("Could not create URI", e);
            }
        }
    }

    /**
     * A simple check of the scheme to make sure it realtes to a Etcd store
     * 
     * @param uri - location of etcd store
     * @return boolean
     */
    public static boolean isEtcdUri(URI uri) {
        return "etcd".equals(uri.getScheme());
    }

    private void setMaxgRPCMessageSizeFromEnvironmentOrDefault() {
        String value = this.env.getenv(MAX_GRPC_MESSAGE_SIZE_ENV_VAR);
        if (value != null && !value.isBlank()) {
            try {
                int parsed = Integer.parseInt(value.trim());
                if (parsed >= 0) {
                    this.maxgRPCMessageSize = parsed;
                }
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid value was set in the environment for maximum gRPC message size in bytes," +
                " setting to default value " + DEFAULT_MAX_GRPC_MESSAGE_SIZE);
            }
        }
    }

    /*
     * This method exists for unit testing purposes.
     */
    public int getMaxgRPCMessageSize() {
        return this.maxgRPCMessageSize;
    }
}
