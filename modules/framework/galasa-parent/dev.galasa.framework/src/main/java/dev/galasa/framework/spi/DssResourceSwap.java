/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import javax.validation.constraints.NotNull;

/**
 * This action is for the framework status area of Manager.
 *
 * This DSS action request that property is swapped with another value.  If the old value does 
 * not match with what is already present, the transaction will fail.
 * 
 *  
 *
 */
public class DssResourceSwap extends DssSwap implements IDssResourceAction {
    
    public DssResourceSwap(@NotNull String key, @NotNull String oldValue, @NotNull String newValue) {
        super(key, oldValue, newValue);
    }
        
    @Override
    public IDssAction applyPrefix(String prefix) {
        return new DssSwap(prefix + getKey(), getOldValue(), getNewValue());
    }


}
