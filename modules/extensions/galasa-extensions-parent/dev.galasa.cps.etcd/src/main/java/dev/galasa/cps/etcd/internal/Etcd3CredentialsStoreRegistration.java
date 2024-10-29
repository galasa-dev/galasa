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

import dev.galasa.framework.spi.IFrameworkInitialisation;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsStoreRegistration;

/**
 * This Class is a small OSGI bean that registers the Credentials store as a
 * ETCD cluster or quietly fails.
 * 
 * @author James Davies
 */
@Component(service = { ICredentialsStoreRegistration.class })
public class Etcd3CredentialsStoreRegistration implements ICredentialsStoreRegistration {

    /**
     * This intialise method is a overide that registers the correct store to the
     * framework.
     * 
     * The URI is collected from the Intialisation. If the URI is a etcd scheme then
     * it registers it as a etcd.
     * 
     * @param frameworkInitialisation - gives the registration access to the correct
     *                               URI for the credentials store
     */
    @Override
    public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws CredentialsException {
        URI creds = frameworkInitialisation.getCredentialsStoreUri();

        if (isEtcdUri(creds)) {
            try {
                URI uri = new URI(creds.toString().substring(5));
                frameworkInitialisation.registerCredentialsStore(
                        new Etcd3CredentialsStore(frameworkInitialisation.getFramework(), uri));
            } catch (URISyntaxException e) {
                throw new CredentialsException("Could not find etcd creds store", e);
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
