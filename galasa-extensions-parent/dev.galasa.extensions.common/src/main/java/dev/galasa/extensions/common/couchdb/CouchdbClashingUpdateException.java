/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.couchdb;

public class CouchdbClashingUpdateException extends CouchdbException {

    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    public CouchdbClashingUpdateException() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public CouchdbClashingUpdateException(String message) {
        super(message);
    }

    /**
     * {@inheritDoc}
     */
    public CouchdbClashingUpdateException(Throwable cause) {
        super(cause);
    }

    /**
     * {@inheritDoc}
     */
    public CouchdbClashingUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

}
