/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package {{.PackageName}};

import dev.galasa.ManagerException;

/**
 * {{.CapitalizedManagerName}}ManagerException
 * 
 * Exception thrown by the {{.CapitalizedManagerName}} Manager when errors occur.
 */
public class {{.CapitalizedManagerName}}ManagerException extends ManagerException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor with message
     * 
     * @param message the error message
     */
    public {{.CapitalizedManagerName}}ManagerException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     * 
     * @param message the error message
     * @param cause the underlying cause
     */
    public {{.CapitalizedManagerName}}ManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with cause
     * 
     * @param cause the underlying cause
     */
    public {{.CapitalizedManagerName}}ManagerException(Throwable cause) {
        super(cause);
    }
}

