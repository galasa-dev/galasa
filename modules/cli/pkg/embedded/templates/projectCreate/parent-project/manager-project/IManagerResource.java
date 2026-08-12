/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package {{.PackageName}};

/**
 * I{{.CapitalizedManagerName}}Resource
 *
 * Resource interface injected into test classes via @{{.CapitalizedManagerName}}Resource annotation.
 * Add methods here that tests will use to interact with the resource.
 */
public interface I{{.CapitalizedManagerName}}Resource {
    
    /**
     * Get the tag for this resource instance
     *
     * @return the resource tag
     */
    String getTag();
    
    // TODO: Add resource methods here
}

