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
 * Db2 Schema
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}Db2Schema</code> annotation requests schema to be created or accessed on a tagged Db2.
 * 
 * @galasa.examples 
 * <code>{@literal @}Db2Schema(tag="PRIMARY", schemaName=mySchema)<br>
 * public IDb2Schema schema;<br>
 * </code>
 * 
 * @galasa.extra
 * The <code>IDb2</code> interface gives the test the ability to applyStatements and retrieve basic information about this schema
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Db2ManagerField
@ValidAnnotatedFields({ IDb2Schema.class })
public @interface Db2Schema {

	String tag() default "PRIMARY";
	
	String db2Tag() default "PRIMARY";
	
	boolean archive() default false;
	
	int resultSetType() default ResultSet.TYPE_SCROLL_INSENSITIVE;
	
	int resultSetConcurrency() default ResultSet.CONCUR_READ_ONLY;
}
