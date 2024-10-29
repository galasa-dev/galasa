/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.gradle.testcatalog;

public class GradleException extends Exception {
    public GradleException(String msg) {
        super(msg);
    }
    public GradleException(String msg, Throwable cause) {
        super(msg,cause);
    }
}
