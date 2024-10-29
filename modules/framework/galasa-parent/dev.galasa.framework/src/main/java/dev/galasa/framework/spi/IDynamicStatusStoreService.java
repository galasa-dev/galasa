/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

/**
 * <p>
 * Used to gain access to properties in the Dynamic Status Store
 * </p>
 * 
 * <p>
 * The framework will be configured with a single Dynamic Status Store where all
 * the dynamic properties for run and resources are kept.
 * </p>
 * 
 * <p>
 * All properties accesses will be within the namespace provided
 * </p>
 * 
 * <p>
 * etcd3 is the preferred dynamic status store for Galasa
 * </p>
 * 
 * <p>
 * An {@link IDynamicStatusStoreService} can be obtained from
 * {@link IFramework#getDynamicStatusStoreService(String)}.
 * </p>
 * 
 *  
 *
 */
public interface IDynamicStatusStoreService extends IDynamicStatusStoreKeyAccess {

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
     * @param resourceKey
     * @return The dynamic resource found, or null.
     */
    IDynamicResource getDynamicResource(String resourceKey);

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
     * @return The dynamic run resource found, or null.
     * @throws DynamicStatusStoreException
     */
    IDynamicRun getDynamicRun() throws DynamicStatusStoreException;

}
