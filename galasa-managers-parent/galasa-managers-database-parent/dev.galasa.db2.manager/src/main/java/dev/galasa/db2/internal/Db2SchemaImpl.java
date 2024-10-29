/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.db2.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.db2.Db2ManagerException;
import dev.galasa.db2.IDb2Schema;
import dev.galasa.db2.IResultMap;
import dev.galasa.db2.internal.properties.Db2DSESchemaName;
import dev.galasa.framework.spi.IFramework;

/**
 * This Db2Schema class provides simple functionality to target simple SQL statements
 * directly to a specified schema.
 * 
 * Interactions with this schema can be recorded to the RAS through the annotation field archive=true
 * 
 * Most but not all object types have been supported, to keep the methods simple:
 * - String
 * - Integer
 * - Float
 * - Double
 * - Boolean
 * - byte[]
 * - BigDecimal
 * - Date
 * - Time
 * - Timestamp
 * - Long
 * - URL
 * 
 * For any unsupported object types please gain a standard
 * sql connection from the IDb2Instance
 * 
 *  
 *
 */
public class Db2SchemaImpl implements IDb2Schema{
	private Connection 		conn;
	private String 			schemaName;
	private boolean 		archive;
	private Path 			artifactRoot;
	private int 			resultSetType;
	private int 			resultSetConcurrency;
	
	private static String 	DB2_NAMESPACE = "db2";
	private static String 	DB2_STATEMENTS = "statements";
	
	private Integer 		written = 0;
	
	private static final Log  	logger = LogFactory.getLog(Db2SchemaImpl.class);
	
	
	public Db2SchemaImpl(IFramework framework,Db2InstanceImpl db2, String tag, boolean archive,
			int resultSetType, int resultSetConcurrency) throws Db2ManagerException {
		this.artifactRoot 			= framework.getResultArchiveStore().getStoredArtifactsRoot();
		this.archive 				= archive;
		this.schemaName 			= Db2DSESchemaName.get(tag);
		this.resultSetType 			= resultSetType;
		this.resultSetConcurrency 	= resultSetConcurrency;
		this.conn 					= db2.getConnection();
		
		try {
			this.conn.setSchema(this.schemaName);
		} catch (SQLException e) {
			throw new Db2ManagerException("Failed to set Schema ", e);
		}
	}

	@Override
	public String getSchemaName() {
		return this.schemaName;
		
	} 
		
	/**
	 * Executes SQL statement
	 */
	@Override
	public IResultMap executeSql(String stmt, Object... params) throws Db2ManagerException {
		ResultMap rsm = new ResultMap();
		try {
			PreparedStatement ps = createPreparedStatement(stmt, params);
			if (stmt.toUpperCase().startsWith("SELECT")) {
				ResultSet rs = ps.executeQuery();
				ResultSetMetaData meta = rs.getMetaData();
				
				rs.next();
				for (int i=1;i<=meta.getColumnCount();i++) {
					rsm.add(meta.getColumnName(i), rs.getObject(i));
				}
				ps.close();
			} else{
				int rc = ps.executeUpdate();
				rsm.add("RC", rc);
				ps.close();
			}
			
			if (archive) {
				writeCommandAndResponseToRas(stmt, rsm.valuesToString());
			}
		} catch (SQLException e) {
			throw new Db2ManagerException("Failed SQL Statement", e);
		}
		
		
		return rsm;
	}

	/**
	 * Executes a query that expects the return of multiple records. Returns them as a list of results
	 */
	@Override
	public List<IResultMap> executeSqlList(String stmt, Object... params) throws Db2ManagerException {
		List<IResultMap> rsmList = new ArrayList<>();
		try {
			PreparedStatement ps = createPreparedStatement(stmt, params);
			ResultSet rs = ps.executeQuery();
			ResultSetMetaData meta = rs.getMetaData();
			
			while (rs.next()) {
				ResultMap rsm = new ResultMap();
				for (int i=1;i<=meta.getColumnCount();i++) {
					rsm.add(meta.getColumnName(i), rs.getObject(i));
				}
				rsmList.add(rsm);
			}
			
			if (archive) {
				StringBuilder sb = new StringBuilder();
				for (IResultMap rm :rsmList) {
					sb.append(rm.valuesToString());
					sb.append("\n");
				}
				writeCommandAndResponseToRas(stmt, sb.toString().trim());
			}
			ps.close();
		} catch (SQLException e) {
			throw new Db2ManagerException("Failed SQL Statement", e);
		}
		return rsmList;
	}

	/**
	 * Executes lines of SQL statements one by one.
	 */
	@Override
	public List<IResultMap> executeSqlFile(InputStream in) throws Db2ManagerException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		List<IResultMap> results = new ArrayList<IResultMap>();
		
		try {
			while (br.ready()) {
				String stmt = br.readLine();
				results.add(executeSql(stmt));
			}
		} catch (IOException e) {
			throw new Db2ManagerException("Problem reading file", e);
		}
		return results;
	}
	
	private void writeCommandAndResponseToRas(String stmt, String resp) {
		// Statement in
		writeToRas(stmt, artifactRoot.resolve(DB2_NAMESPACE).
				resolve(this.schemaName).
				resolve(DB2_STATEMENTS).
				resolve(written.toString() + "-" + stmt.split(" ")[0]).
				resolve("in"));
		//Response
		writeToRas(resp, artifactRoot.resolve(DB2_NAMESPACE).
				resolve(this.schemaName).
				resolve(DB2_STATEMENTS).
				resolve(written.toString() + "-" + stmt.split(" ")[0]).
				resolve("out"));
		
		written++;
		
	}
	
	private void writeToRas(String content, Path path) {
		try {
			Files.write(path, content.getBytes(), StandardOpenOption.CREATE);
		} catch (Exception e) {
			logger.info("Unable to log message for a queue", e);
		}
	}
	
	private PreparedStatement createPreparedStatement(String stmt, Object... params) throws SQLException, Db2ManagerException {
		PreparedStatement ps = conn.prepareStatement(stmt, resultSetType, resultSetConcurrency);
		for (int i=0;i<params.length;i++) {
			if (params[i] instanceof String) {
				ps.setString(i+1, (String)params[i]);
			} else if (params[i] instanceof Integer) {
				ps.setInt(i+1, (Integer)params[i]);
			} else if (params[i] instanceof Float) {
				ps.setFloat(i+1, (Float)params[i]);
			} else if (params[i] instanceof Double) {
				ps.setDouble(i+1, (Double)params[i]);
			} else if (params[i] instanceof Boolean) {
				ps.setBoolean(i+1, (Boolean)params[i]);
			} else if (params[i] instanceof byte[]) {
				ps.setBytes(i+1, (byte[])params[i]);
			} else if (params[i] instanceof BigDecimal) {
				ps.setBigDecimal(i+1, (BigDecimal)params[i]);
			}  else if (params[i] instanceof Date) {
				ps.setDate(i+1, (Date)params[i]);
			} else if (params[i] instanceof Time) {
				ps.setTime(i+1, (Time)params[i]);
			} else if (params[i] instanceof Timestamp) {
				ps.setTimestamp(i+1, (Timestamp)params[i]);
			} else if (params[i] instanceof Long) {
				ps.setLong(i+1, (Long)params[i]);
			} else if (params[i] instanceof URL) {
				ps.setURL(i+1, (URL)params[i]);
			} else {
				throw new Db2ManagerException("Galasa's execute SQL for Db2 implementation only supports basic types. Please retrieve the connection from the IDb2Instance object for full java SQL capabilities");
			}
			
		}
		return ps;
	}
}
