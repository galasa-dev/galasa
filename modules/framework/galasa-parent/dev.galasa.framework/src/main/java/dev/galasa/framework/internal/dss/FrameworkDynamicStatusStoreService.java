/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.dss;

import java.util.Objects;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicResource;
import dev.galasa.framework.spi.IDynamicRun;
import dev.galasa.framework.spi.IDynamicStatusStore;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;

/**
 * <p>
 * This class is used when the FPF class is being operated as the Key-Value
 * store for the Dynamic Status Store. This class registers the Dynamic Status
 * Store as the only DSS.
 * </p>
 * 
 *  
 */

public class FrameworkDynamicStatusStoreService extends FrameworkDynamicStoreKeyAccess
        implements IDynamicStatusStoreService {
    private final String namespace;

    public FrameworkDynamicStatusStoreService(IFramework framework, IDynamicStatusStore dssStore, String namespace) {
        super(dssStore, "dss." + namespace + ".", namespace);
        Objects.requireNonNull(namespace);

        this.namespace = namespace;
    }

    /**
     * <p>
     * Retrieve interface to control a dynamic resource represented in the framework
     * area. This is to allow the resource being managed to be automatically
     * represented on the Web UI and the Eclipse Automation Views.
     * </p>
     * 
     * <p>
     * The properties the framework create from will be
     * dss.framework.resource.namespace.resourceKey . After that the manager can set
     * the property names as necessary.
     * </p>
     * 
     * <p>
     * For example, if the zOS Security Manager is controlling a set of userids on
     * cluster PlexMA, the namespace is already set to 'zossec', the property key
     * would be 'PLEXMA.userid.GAL234'. This would result in the property
     * 'dss.framework.resource.zossec.PLEXMA.userid.GAL234=L3456'. The automation
     * views would build a tree view of the properties starting
     * 'dss.framework.resource'
     * </p>
     * 
     * @param resourceKey - The resource key to prefix the keys along with the namespace
     * @return A tailored IDynamicResource
     */
    @Override
    public IDynamicResource getDynamicResource(String resourceKey) {
        String newPrefix = "dss.framework.resource." + this.namespace + "." + resourceKey + ".";
        return new FrameworkDynamicResource(getDssStore(), newPrefix, this.namespace);
    }

    /**
     * <p>
     * Retrieve an interface to update the Run status with manager related
     * information. This is information above what the framework would display, like
     * status, no. of methods etc.
     * </p>
     * 
     * <p>
     * One possible use would be the zOS Manager reporting the primary zOS Image the
     * test is running on.
     * </p>
     * 
     * @return The dynamic run resource tailored to this namespaces
     * @throws DynamicStatusStoreException
     */
    @Override
    public IDynamicRun getDynamicRun() throws DynamicStatusStoreException {
        return new FrameworkDynamicRun();
    }
}
