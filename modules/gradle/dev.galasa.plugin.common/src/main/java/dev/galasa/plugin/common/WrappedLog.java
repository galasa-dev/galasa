/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.plugin.common;

public interface WrappedLog {
    void info(String message);
    void warn(String message);
    void error(String message);
}
