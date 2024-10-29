/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

/**
 * <p>
 * Used by the Galasa Framework to initialise the various Result Archive Stores
 * that may exist within the OSGi instance. The framework can run with zero or
 * more Result Archive Stores·
 * </p>
 * 
 * <p>
 * The RASs should use @{link
 * dev.galasa.framework.spi.IFrameworkInitialisation#getResultArchiveStoreUris}
 * to obtain a list of active URIs which the framework wants initialised. The
 * RAS should examine this and determine if it is required. It is up to the RAS
 * if it wants to support multiple URIs of it's own implementation, eg
 * file:///dir1, file:///dir2
 * </p>
 * 
 * 
 *  
 *
 */
public interface IResultArchiveStoreService extends IResultArchiveStore {
}