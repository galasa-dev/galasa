/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

/**
 * <p>
 * This interface will allow managers and services to update the a dynamic
 * resource view maintained by the framework. Although the managers and services
 * can maintain a resource status within their own namespace, this interface
 * enables the manager/service to expose the status of a resource in a generic
 * way, so it is viewable to the user from the Web UI or Eclipse view. This
 * avoid the need to have manager specific code in the Web UI or Eclipse plugin.
 * </p>
 * 
 *  
 *
 */
public interface IDynamicResource extends IDynamicStatusStoreKeyAccess {
    /**** To be designed ****/
}
