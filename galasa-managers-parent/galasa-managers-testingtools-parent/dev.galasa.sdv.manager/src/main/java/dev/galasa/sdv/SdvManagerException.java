/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.sdv;

import dev.galasa.ManagerException;

/**
 * This class provides a generic Exception which can be thrown
 * throughout the SDV Manager.
 *
 */
public class SdvManagerException extends ManagerException {

    private static final long serialVersionUID = 1L;

    public SdvManagerException() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public SdvManagerException(String message) {
        super(message);
    }

    public SdvManagerException(String message, Throwable cause) {
        super(message, cause);
    }

}
