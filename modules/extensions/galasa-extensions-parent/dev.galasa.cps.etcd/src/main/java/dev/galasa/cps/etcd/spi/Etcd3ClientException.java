/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cps.etcd.spi;

import dev.galasa.cps.etcd.Etcd3ManagerException;

public class Etcd3ClientException extends Etcd3ManagerException {
    private static final long serialVersionUID = 1L;

    public Etcd3ClientException() {
    }

    public Etcd3ClientException(String message) {
        super(message);
    }

    public Etcd3ClientException(Throwable cause) {
        super(cause);
    }

    public Etcd3ClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public Etcd3ClientException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
