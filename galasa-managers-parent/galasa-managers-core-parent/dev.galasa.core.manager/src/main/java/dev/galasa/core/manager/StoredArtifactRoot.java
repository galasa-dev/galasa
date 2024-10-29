/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.core.manager;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.file.Path;

import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * Fill this field Root path of the Stored Artifacts for this Test Run. This can
 * be used to record test logs, job output, trace files etc.
 *
 * Will only populate public {@link java.nio.file.Path} fields.
 * 
 * @see java.nio.file.Path
 */
@Retention(RUNTIME)
@Target(FIELD)
@CoreManagerField
@ValidAnnotatedFields({ Path.class })
public @interface StoredArtifactRoot {

}
