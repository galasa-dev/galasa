/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import javax.validation.constraints.NotNull;

/**
 * This DSS action request that a property is deleted.  If an old value
 * is provided,  then this will be checked before deleting and fail the transaction if the 
 * values are different
 * 
 *  
 *
 */
public class DssDelete implements IDssAction {
    
    private final String key;
    private final String oldValue;

    public DssDelete(@NotNull String key, String oldValue) {
        this.key = key;
        this.oldValue = oldValue;
    }
    
    public String getKey() {
        return key;
    }
    
    public String getOldValue() {
        return oldValue;
    }
    
    @Override
    public IDssAction applyPrefix(String prefix) {
        return new DssDelete(prefix + this.key, oldValue);
    }


}
