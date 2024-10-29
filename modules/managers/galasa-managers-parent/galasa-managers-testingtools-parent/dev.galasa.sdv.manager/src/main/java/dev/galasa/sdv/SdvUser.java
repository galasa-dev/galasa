/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.sdv;

import dev.galasa.framework.spi.ValidAnnotatedFields;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * A zOS User that will have SDV recording switched on, on a specified CICS TS Region.
 *
 * <p>
 * Used to populate a {@link ISdvUser} field
 * </p>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@SdvManagerField
@ValidAnnotatedFields({ISdvUser.class})
public @interface SdvUser {

    /**
     * The tag of the CICS region the User is to be associated with. SDV Recording will take place
     * for this user on this CICS Region.
     */
    String cicsTag() default "PRIMARY";

    /**
     * The SDV manager will select a user from the zOS image pool user matching this roleTag.
     */
    String roleTag();

}
