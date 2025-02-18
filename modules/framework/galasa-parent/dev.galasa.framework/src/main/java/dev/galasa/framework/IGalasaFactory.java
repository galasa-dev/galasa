/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;


/**
 * Implementations allow the caller to instantiate internal instances of interfaces without having access to the internals.
 */
public interface IGalasaFactory {
    
    public IFrameworkInitialisationStrategy newTestRunInitStrategy();

    public IFrameworkInitialisationStrategy newResourceManagerInitStrategy();

    public IFrameworkInitialisationStrategy newDefaultInitStrategy();
}