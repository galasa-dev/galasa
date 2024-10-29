/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import javax.validation.constraints.NotNull;

/**
 * This DSS action request that property is swapped with another value.  If the old value does 
 * not match with what is already present, the transaction will fail.
 * 
 *  
 *
 */
public class DssSwap implements IDssAction {
    
    private final String key;
    private final String oldValue;
    private final String newValue;

    public DssSwap(@NotNull String key, @NotNull String oldValue, @NotNull String newValue) {
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    
    public String getKey() {
        return key;
    }
    
    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }
    
    @Override
    public IDssAction applyPrefix(String prefix) {
        return new DssSwap(prefix + this.key, oldValue, newValue);
    }


}
