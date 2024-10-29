/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

/**
 * IDssStatusAction is the basis for all updates to the Dynamic Status Store properties 
 * framework status area
 * 
 *  
 *
 */
public interface IDssResourceAction {
    
    IDssAction applyPrefix(String prefix);
    
}
