/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.plugin.common;

// Logs errors, then raises an exception.
public interface ErrorRaiser <Ex extends Exception> {
    void raiseError(String template, Object...  parameters) throws Ex ;
    void raiseError( Throwable cause, String template, Object...  parameters) throws Ex;
}
