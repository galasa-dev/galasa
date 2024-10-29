/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.mq;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@MqManagerField
@ValidAnnotatedFields({ IMessageQueue.class })
public @interface Queue {

    String queueMgrTag() default "PRIMARY";
    String name() default "";
    String tag() default "";
    boolean archive() default true;

}
