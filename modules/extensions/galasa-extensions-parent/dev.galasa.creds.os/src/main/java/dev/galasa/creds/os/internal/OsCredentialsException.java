/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal;

import dev.galasa.framework.spi.creds.CredentialsException;

/**
 * Exception thrown when there is an error accessing OS-native credentials storage.
 */
public class OsCredentialsException extends CredentialsException {

    private static final long serialVersionUID = 1L;

    public OsCredentialsException() {
        super();
    }

    public OsCredentialsException(String message) {
        super(message);
    }

    public OsCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }

    public OsCredentialsException(Throwable cause) {
        super(cause);
    }
}
