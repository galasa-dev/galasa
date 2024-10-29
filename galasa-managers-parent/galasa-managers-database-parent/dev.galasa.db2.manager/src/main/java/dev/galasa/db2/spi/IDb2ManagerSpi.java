/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.db2.spi;

import dev.galasa.db2.Db2ManagerException;
import dev.galasa.db2.IDb2Instance;
import dev.galasa.db2.IDb2Schema;

/**
 * Db2 Manager SPI
 * 
 * Provides either a connection to a DB or a simple connection to a schema within the DB. If the DB has not been connected
 *  too before requesting a schema from that database, the connection will be initialised.
 *  
 *  
 *  
 *
 */
public interface IDb2ManagerSpi {
	/**
	 * Provides the db2 instance back. Both the Connection and meta can be retireived with this.
	 * @param tag
	 * @return
	 * @throws Db2ManagerException
	 */
	public IDb2Instance getInstanceFromTag(String tag) throws Db2ManagerException;
	
	/**
	 * Retrieve Schema impl from tag
	 * @param tag
	 * @param db2Tag
	 * @return
	 * @throws Db2ManagerException
	 */
	public IDb2Schema getSchemaFromTag(String tag, String db2Tag) throws Db2ManagerException;
	
	/**
	 * Retrieve Schema impl from tag
	 * 
	 * @param tag
	 * @param db2Tag
	 * @param archive
	 * @return
	 * @throws Db2ManagerException
	 */
	public IDb2Schema getSchemaFromTag(String tag, String db2Tag, boolean archive) throws Db2ManagerException;
	
	/**
	 * Retrieve Schema impl from tag
	 * 
	 * @param tag
	 * @param db2Tag
	 * @param archive
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * @return
	 * @throws Db2ManagerException
	 */
	public IDb2Schema getSchemaFromTag(String tag, String db2Tag, boolean archive, int resultSetType, int resultSetConcurrency) throws Db2ManagerException;
}
