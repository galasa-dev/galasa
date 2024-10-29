/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.githubissue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * This annotation will cause the Manager to influence the "failed" result of the test based on 
 * whether a GitHub issue is open or closed.
 * 
 *  
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface GitHubIssue {
    
    /**
     * The issue to check against
     */
    public String issue();
    
    /**
     * The repository to look for the issue, if not provided, will use the default from the CPS
     */
    public String repository() default "";

    /**
     * The CPS id of the GitHub instance, be it public GitHub or an enterprise instance
     */
    public String githubId() default "DEFAULT";
    
    
    /**
     * One or more REGEX strings to check in the failing exception, if any of them match, then this annotation is valid and therefore GitHub
     * will be checked.   If no REGEX is provided, any failure is valid.
     */
    public String[] regex() default {};

}
