/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.db2;

import java.sql.Connection;

/**
 * 
 * IDb2Instance provides a connection to a tagged Db2 Database.
 * 
 * This connection must be established to connected a IDb2Schema to this database.
 * 
 *  
 *
 */
public interface IDb2Instance {
	
	/**
	 * The Db2 manager provides some basic ease of use methods to submit SQL statements and handle
	 * the records returned, but for more complex uses the standard java.sql.Connection can be retrieved.
	 * 
	 * Note that any archiving will have to be managed within a test if using this option.
	 * 
	 * @return
	 */
	public Connection getConnection();
	
	/**
	 * Requests from the database to return the database name.
	 * 
	 * @return
	 * @throws Db2ManagerException
	 */
	public String getDatabaseName() throws Db2ManagerException;

}
