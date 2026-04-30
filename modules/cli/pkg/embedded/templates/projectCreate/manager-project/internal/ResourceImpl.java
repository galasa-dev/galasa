/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package {{.PackageName}}.internal;

import {{.PackageName}}.I{{.CapitalizedManagerName}}Resource;

/**
 * {{.CapitalizedManagerName}}ResourceImpl
 * 
 * Implementation of the {{.CapitalizedManagerName}} resource interface.
 * This class provides the actual functionality that tests will use.
 */
public class {{.CapitalizedManagerName}}ResourceImpl implements I{{.CapitalizedManagerName}}Resource {
    
    private final String tag;
    
    /**
     * Constructor
     * 
     * @param tag the resource tag
     */
    public {{.CapitalizedManagerName}}ResourceImpl(String tag) {
        this.tag = tag;
    }

    /**
     * Get the tag for this resource instance
     * 
     * @return the resource tag
     */
    @Override
    public String getTag() {
        return this.tag;
    }

    /**
     * Example method implementation
     * 
     * @return a sample value
     */
    @Override
    public String getSampleValue() {
        return "Sample value from {{.CapitalizedManagerName}} resource with tag: " + tag;
    }
}

