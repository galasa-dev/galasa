/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import javax.validation.constraints.NotNull;

/**
 * This DSS action request that a NEW property is added to the DSS.   If the 
 * property already exists, the transaction will fail.
 * If you want to add a property whether it exists or not, use DssUpdate
 * 
 *  
 *
 */
public class DssAdd implements IDssAction {
    
    private final String key;
    private final String value;

    public DssAdd(@NotNull String key, @NotNull String value) {
        this.key = key;
        this.value = value;
    }
    
    public String getKey() {
        return key;
    }
    
    public String getValue() {
        return value;
    }

    @Override
    public IDssAction applyPrefix(String prefix) {
        return new DssAdd(prefix + this.key, value);
    }

}
