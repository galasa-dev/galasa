/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package {{.PackageName}};

/**
 * I{{.CapitalizedManagerName}}Manager Interface
 * 
 * This is the main manager interface. It can be used by other managers
 * that depend on this manager to access its functionality.
 * 
 * Typically, this interface is kept minimal and most functionality
 * is exposed through the resource interface (I{{.CapitalizedManagerName}}Resource).
 */
public interface I{{.CapitalizedManagerName}}Manager {
    
    /**
     * Example method - add manager-level methods here if needed
     * 
     * @return manager information
     */
    String getManagerInfo();
}

