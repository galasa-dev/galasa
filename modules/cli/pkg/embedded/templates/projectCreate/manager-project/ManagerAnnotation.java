/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package {{.PackageName}};

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import {{.PackageName}}.internal.{{.CapitalizedManagerName}}ManagerField;
import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * {{.CapitalizedManagerName}} Resource Annotation
 * 
 * This annotation is used to inject a {{.CapitalizedManagerName}} resource into a test class.
 * 
 * Example usage:
 * <pre>
 * {@literal @}{{.CapitalizedManagerName}}Resource
 * public I{{.CapitalizedManagerName}}Resource resource;
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ValidAnnotatedFields({ I{{.CapitalizedManagerName}}Resource.class })
@{{.CapitalizedManagerName}}ManagerField
public @interface {{.CapitalizedManagerName}}Resource {
    
    /**
     * The tag for this resource instance
     * 
     * @return the resource tag, defaults to "PRIMARY"
     */
    public String resourceTag() default "PRIMARY";
}

