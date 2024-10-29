/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.gradle.testcatalog;

public class TestCatalogException extends Exception {
    private static final long serialVersionUID = 1L;

    public TestCatalogException() {
    }

    public TestCatalogException(String message) {
        super(message);
    }

    public TestCatalogException(Throwable cause) {
        super(cause);
    }

    public TestCatalogException(String message, Throwable cause) {
        super(message, cause);
    }

    public TestCatalogException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
