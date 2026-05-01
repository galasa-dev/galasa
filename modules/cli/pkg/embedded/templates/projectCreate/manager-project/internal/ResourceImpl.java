/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package {{.PackageName}}.internal;

import java.util.Objects;

import {{.PackageName}}.I{{.CapitalizedManagerName}}Resource;

public class {{.CapitalizedManagerName}}ResourceImpl implements I{{.CapitalizedManagerName}}Resource {
    
    private final String tag;
    
    public {{.CapitalizedManagerName}}ResourceImpl(String tag) {
        this.tag = Objects.requireNonNull(tag, "tag cannot be null");
    }

    @Override
    public String getTag() {
        return this.tag;
    }
}

