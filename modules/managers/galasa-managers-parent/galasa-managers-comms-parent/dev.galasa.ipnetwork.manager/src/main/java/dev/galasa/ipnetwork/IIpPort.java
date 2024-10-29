/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ipnetwork;

/**
 * <p>
 * Represents a IP Port.
 * </p>
 * 
 * <p>
 * Use the appropriate host manager annotation to obtain an object
 * </p>
 * 
 *  
 *
 */
public interface IIpPort {

    /**
     * Get the port number
     */
    int getPortNumber();

    IIpHost getHost();

    String getType();
}
