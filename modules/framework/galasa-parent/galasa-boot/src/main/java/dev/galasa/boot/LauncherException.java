/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.boot;

/**
 * @see java.lang.Exception
 */
public class LauncherException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * @see java.lang.Exception#Exception()
     */
    public LauncherException() {
        super();
    }

    /**
     * @see java.lang.Exception#Exception(String)
     */
    public LauncherException(String message) {
        super(message);
    }

    /**
     * @see java.lang.Exception#Exception(Throwable)
     */
    public LauncherException(Throwable cause) {
        super(cause);
    }

    /**
     * @see java.lang.Exception#Exception(String, Throwable)
     */
    public LauncherException(String message, Throwable cause) {
        super(message, cause);
    }

}
