/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

/**
 * Manager and services use this interface to update the status of a Run within
 * the Automation Views. Most of the data on the automation views will be
 * provided by the framework. However, managers can add addition data, like the
 * zOS Manager can add the primary zOS Image the test is running against. The
 * manager/service will only be able to contribute within it's own namespace.
 * 
 * <p>
 * The underlying properties that support this in the Dynamic Status Store are
 * prefixed 'dss.framework.run.R12343'
 * </p>
 * 
 *  
 *
 */
public interface IDynamicRun {
    /*** To be designed ***/
}
