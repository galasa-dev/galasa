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
 * This DSS action request that a NEW property is added to the DSS.   If the 
 * property already exists, the transaction will fail.
 * If you want to add a property whether it exists or not, use DssUpdate
 * 
 *  
 *
 */
public class DssResourceAdd extends DssAdd implements IDssResourceAction {

    public DssResourceAdd(@NotNull String key, @NotNull String value) {
        super(key, value);
    }

    @Override
    public IDssAction applyPrefix(String prefix) {
        return new DssAdd(prefix + getKey(), getValue());
    }

}
