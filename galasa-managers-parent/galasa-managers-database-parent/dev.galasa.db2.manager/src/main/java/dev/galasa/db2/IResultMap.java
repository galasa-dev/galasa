/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.db2;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * A simple interface to provide easy access to multiple types of returns.
 * 
 *  
 *
 */
public interface IResultMap {

	public String getStringValue(String columnName);
	
	public float getFloatValue(String columnName);
	
	public double getDoubleValue(String columnName);
	
	public Boolean getBooleanValue(String columnName);
	
	public Integer getIntValue(String columnName);
	
	public Date getDateValue(String columnName);
	
	public Time getTimeValue(String columnName);
	
	public Timestamp getTimestampValue(String columnName);
	
	public long getLongValue(String columnName);
	
	public byte[] getBytesValue(String columnName);
	
	public BigDecimal getBigDecimalValue(String columnName);
	
	public URL getURLValue(String columnName);
	
	public String valuesToString();
	
	public String columnsToString();
	
}