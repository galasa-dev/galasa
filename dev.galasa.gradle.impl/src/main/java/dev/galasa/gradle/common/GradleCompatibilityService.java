/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.gradle.common;

import org.gradle.util.GradleVersion;

public class GradleCompatibilityService implements ICompatibilityService {
    
    public static final GradleVersion CURRENT_GRADLE_VERSION = GradleVersion.current();

    public boolean isCurrentVersionLaterThanGradle8() {
        return CURRENT_GRADLE_VERSION.compareTo(GradleVersion.version("8.0")) >= 0;
    }
}
