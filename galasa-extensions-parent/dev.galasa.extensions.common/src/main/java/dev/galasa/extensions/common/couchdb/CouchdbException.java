/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.couchdb;

public class CouchdbException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    public CouchdbException() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public CouchdbException(String message) {
        super(message);
    }

    /**
     * {@inheritDoc}
     */
    public CouchdbException(Throwable cause) {
        super(cause);
    }

    /**
     * {@inheritDoc}
     */
    public CouchdbException(String message, Throwable cause) {
        super(message, cause);
    }

}
