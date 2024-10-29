/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts;

import java.util.HashMap;

import javax.validation.constraints.NotNull;

/**
 * CICS TS wrapper class to HashMap to provide additional
 * functionality for use with CECI, CEMT and CEDA
 */
public class CicstsHashMap extends HashMap<String,String> {
    private static final long serialVersionUID = 1L;
    
    public boolean isParameterEquals(@NotNull String parameter, @NotNull String value) {
        String testValue = get(parameter);
        if (testValue == null) {
            return false;
        }
        
        return testValue.equals(value);
    }

    public void checkParameterEquals(@NotNull String parameter, @NotNull String value) throws CicstsManagerException {
        if (!isParameterEquals(parameter, value)) {
            throw new CicstsManagerException("Parameter " + parameter + " does not equal " + value);
        }
    }

}
