/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import javax.validation.constraints.NotNull;

/**
 * This DSS action request that all properties with this prefix are deleted.
 * 
 *  
 *
 */
public class DssDeletePrefix implements IDssAction {
    
    private final String prefix;

    public DssDeletePrefix(@NotNull String prefix) {
        this.prefix = prefix;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    @Override
    public IDssAction applyPrefix(String prefix) {
        return new DssDeletePrefix(prefix + this.prefix);
    }

    
}
