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
 * This DSS action request that a property is deleted.  If an old value
 * is provided,  then this will be checked before deleting and fail the transaction if the 
 * values are different
 * 
 *  
 *
 */
public class DssResourceDelete extends DssDelete implements IDssResourceAction {
    
    public DssResourceDelete(@NotNull String key, String oldValue) {
        super(key, oldValue);
    }

    @Override
    public IDssAction applyPrefix(String prefix) {
        return new DssDelete(prefix + getKey(), getOldValue());
    }


}
