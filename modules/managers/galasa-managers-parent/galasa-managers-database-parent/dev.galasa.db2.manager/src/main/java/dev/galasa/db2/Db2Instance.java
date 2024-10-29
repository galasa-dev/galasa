/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.db2;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.ResultSet;

import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * Db2 Instance
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}Db2</code> annotation requests a connection to a Db2 instance with a specified tag.
 * 
 * @galasa.examples 
 * <code>{@literal @}Db2(tag="PRIMARY")<br>
 * public IDb2 db;<br>
 * </code>
 * 
 * @galasa.extra
 * The <code>IDb2</code> interface gives the test access to a standard java.sql.Connection. This connection can then be used as standard to interact 
 * with the Db2
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Db2ManagerField
@ValidAnnotatedFields({ IDb2Instance.class })
public @interface Db2Instance {
	String tag() default "PRIMARY";
}
