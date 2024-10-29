/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cps.etcd.internal;

import java.net.URI;
import java.net.URISyntaxException;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreRegistration;
import dev.galasa.framework.spi.IFrameworkInitialisation;

/**
 * This Class is a small OSGI bean that registers the CPS store as a ETCD
 * cluster or quietly fails.
 * 
 * @author James Davies
 */
@Component(service = { IConfigurationPropertyStoreRegistration.class })
public class Etcd3ConfigurationPropertyRegistration implements IConfigurationPropertyStoreRegistration {

    /**
     * This intialise method is a overide that registers the correct store to the
     * framework.
     * 
     * The URI is collected from the Intialisation. If the URI is a etcd scheme then
     * it registers it as a etcd.
     * 
     * @param frameworkInitialisation - gives the registration access to the correct
     *                               URI for the cps
     */
    @Override
    public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation)
            throws ConfigurationPropertyStoreException {
        URI cps = frameworkInitialisation.getBootstrapConfigurationPropertyStore();

        if (isEtcdUri(cps)) {
            try {
                URI uri = new URI(cps.toString().substring(5));
                frameworkInitialisation.registerConfigurationPropertyStore(new Etcd3ConfigurationPropertyStore(uri));
            } catch (URISyntaxException e) {
                throw new ConfigurationPropertyStoreException("Could not create URI", e);
            }
        }
    }

    /**
     * Small method to check the URI for the correct type for etcd.
     * 
     * @param uri - the uri for the cps.
     * @return - if etcd is applicable.
     */
    public static boolean isEtcdUri(URI uri) {
        return "etcd".equals(uri.getScheme());
    }
}
