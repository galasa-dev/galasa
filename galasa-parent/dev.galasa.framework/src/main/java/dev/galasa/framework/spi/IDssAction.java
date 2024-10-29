/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

/**
 * IDssAction is the basis for all updates to the Dynamic Status Store properties
 * 
 *  
 *
 */
public interface IDssAction {
    
    IDssAction applyPrefix(String prefix);
    
}
