/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package {{.PackageName}};

/**
 * I{{.CapitalizedManagerName}}Resource Interface
 * 
 * This interface represents a {{.CapitalizedManagerName}} resource that can be injected into test classes.
 * Implement methods here that tests will use to interact with the resource.
 * 
 * Example methods you might add:
 * <pre>
 * String getResourceId();
 * void performAction() throws {{.CapitalizedManagerName}}ManagerException;
 * </pre>
 */
public interface I{{.CapitalizedManagerName}}Resource {
    
    /**
     * Get the tag for this resource instance
     * 
     * @return the resource tag
     */
    String getTag();
    
    /**
     * Example method - replace with your own resource-specific methods
     * 
     * @return a sample value
     */
    String getSampleValue();
}

