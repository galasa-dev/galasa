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
 * This DSS action request that property is updated in the DSS whether it exists or not. 
 * If you want the transaction to fail if the property already exists, use DssStatusAdd
 * 
 *  
 *
 */
public class DssResourceUpdate extends DssUpdate implements IDssResourceAction {
    
    public DssResourceUpdate(@NotNull String key, @NotNull String value) {
        super(key, value);
    }

    @Override
    public IDssAction applyPrefix(String prefix) {
        return new DssUpdate(prefix + getKey(), getValue());
    }

}
