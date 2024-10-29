/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.db2;

import java.io.InputStream;
import java.util.List;

/**
 * IDb2Schema provides a connection to a specific db2 schema. Statements submited that do not specify a 
 * schema will use this as its default schema.
 * 
 *  Allows users to submit statements as string, or as a batch of statements from a file.
 * 
 *  
 *
 */
public interface IDb2Schema {
	
	/**
	 * Executes a single statement, with any parameters
	 * 
	 * @param stmt
	 * @param params
	 * @return IResultsMap
	 * @throws Db2ManagerException
	 */
	public IResultMap executeSql(String stmt, Object... params) throws Db2ManagerException;
	
	/**
	 * Executes a single statement with any parameters which expects multiple records returned.
	 * 
	 * @param stmt
	 * @param params
	 * @return List<IResultsMap>
	 * @throws Db2ManagerException
	 */
	public List<IResultMap> executeSqlList(String stmt, Object... params) throws Db2ManagerException;
	
	/**
	 * Executes a file of statements which are line separated.
	 * 
	 * @param in
	 * @return List<IResultMap>
	 * @throws Db2ManagerException
	 */
	public List<IResultMap> executeSqlFile(InputStream in) throws Db2ManagerException;
	
	/**
	 * Returns the name of the Schema.
	 * 
	 * @return
	 */
	public String getSchemaName();
	
}
