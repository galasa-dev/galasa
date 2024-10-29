/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.apache.commons.logging;

import dev.galasa.framework.log4j2.bridge.internal.GalasaLogAdapter;

public class LogFactory {

    private static GalasaLogAdapter adapter;

    public static Log getLog(Class<?> clazz) {
        return getAdapter().getLogger(clazz.getName());
    }

    public static Log getLog(String name) {
        return getAdapter().getLogger(name);
    }

    private static GalasaLogAdapter getAdapter() {
        if (adapter == null) { 
            synchronized(LogFactory.class) {
                if (adapter == null) {
                    adapter = new GalasaLogAdapter();
                }
            }
        }

        return adapter;
    }

}
