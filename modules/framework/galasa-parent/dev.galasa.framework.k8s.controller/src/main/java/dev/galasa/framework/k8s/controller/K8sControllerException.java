/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import dev.galasa.framework.spi.FrameworkException;

public class K8sControllerException extends FrameworkException {
    private static final long serialVersionUID = 1L;

    public K8sControllerException() {
    }

    public K8sControllerException(String message) {
        super(message);
    }

    public K8sControllerException(Throwable cause) {
        super(cause);
    }

    public K8sControllerException(String message, Throwable cause) {
        super(message, cause);
    }

    public K8sControllerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
