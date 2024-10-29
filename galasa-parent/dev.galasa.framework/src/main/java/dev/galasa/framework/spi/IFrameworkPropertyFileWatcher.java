/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

public interface IFrameworkPropertyFileWatcher {

    public enum Event {
        MODIFIED,
        NEW,
        DELETE
    }

    void propertyModified(String key, Event event, String oldValue, String newValue);
}
