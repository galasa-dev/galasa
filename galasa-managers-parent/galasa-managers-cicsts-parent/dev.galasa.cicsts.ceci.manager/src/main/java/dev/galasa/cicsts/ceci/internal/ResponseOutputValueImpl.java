/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.ceci.internal;

import dev.galasa.cicsts.CeciException;
import dev.galasa.cicsts.ICeciResponseOutputValue;

public class ResponseOutputValueImpl implements ICeciResponseOutputValue {

    private Object value;
    public ResponseOutputValueImpl(Object value) {
        this.value = value;
    }
    
    @Override
    public String getTextValue() {
        if (value.getClass().isArray()) {
            Object[] objects = (Object[]) value;
            return (String) objects[0];
        }
        return value.toString();
    }
    
    @Override
    public char[] getHexValue() {
        if (value.getClass().isArray()) {
            Object[] objects = (Object[]) value;
            return ((String) objects[1]).toCharArray();
        }
        return ((String) value).toCharArray();
    }

    @Override
    public int getIntValue() throws CeciException {
        if (!(value instanceof Integer)) {
            throw new CeciException("Value is " + value.getClass().getName() + " type");
        }
        return (int) value;
    }

    @Override
    public long getLongValue() throws CeciException {
        if (!(value instanceof Long)) {
            throw new CeciException("Value is " + value.getClass().getName() + " type");
        }
        return (long) value;
    }
    
    @Override
    public String toString() {
        return getTextValue();
    }

    @Override
    public boolean isArray() {
        return value.getClass().isArray();
    }
}
