/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import dev.galasa.framework.spi.ValidAnnotatedFields;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@JMeterManagerField
@ValidAnnotatedFields({ IJMeterSession.class })
public @interface JMeterSession{
    /**
     * Provides the session with a specific jmxFile during provisioning
     */
    String jmxPath();

    String propPath() default "";
}