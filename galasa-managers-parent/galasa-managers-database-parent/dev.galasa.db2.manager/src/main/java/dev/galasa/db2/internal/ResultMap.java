/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.db2.internal;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import dev.galasa.db2.IResultMap;

/**
 * Result Map is a basic object for simple SQL statements returns.
 * 
 * 
 *  
 *
 */
public class ResultMap implements IResultMap{
	private Map<String, Object> result = new HashMap<>();;
	
	public void add(String coloumnName, Object value) {
		result.put(coloumnName, value);
	}

	@Override
	public String getStringValue(String columnName) {
		return (String)result.get(columnName);
	}

	@Override
	public float getFloatValue(String columnName) {
		return (Float)result.get(columnName);
	}

	@Override
	public double getDoubleValue(String columnName) {
		return (Double)result.get(columnName);
	}

	@Override
	public Boolean getBooleanValue(String columnName) {
		return (Boolean)result.get(columnName);
	}

	@Override
	public Integer getIntValue(String columnName) {
		return (Integer)result.get(columnName);
	}

	@Override
	public Date getDateValue(String columnName) {
		return (Date)result.get(columnName);
	}

	@Override
	public Time getTimeValue(String columnName) {
		return (Time)result.get(columnName);
	}

	@Override
	public Timestamp getTimestampValue(String columnName) {
		return (Timestamp)result.get(columnName);
	}

	@Override
	public long getLongValue(String columnName) {
		return (Long)result.get(columnName);
	}

	@Override
	public byte[] getBytesValue(String columnName) {
		return (byte[])result.get(columnName);
	}

	@Override
	public BigDecimal getBigDecimalValue(String columnName) {
		return (BigDecimal)result.get(columnName);
	}

	@Override
	public URL getURLValue(String columnName) {
		return (URL)result.get(columnName);
	}
	
	public String valuesToString() {
		StringBuilder sb = new StringBuilder();
		for (Object value:result.entrySet()) {
			sb.append(value.toString() + " ");
		}
		return sb.toString();
	}
	
	public String columnsToString() {
		StringBuilder sb = new StringBuilder();
		for (String value:result.keySet()) {
			sb.append(value.toString() + " ");
		}
		return sb.toString();
	}

}
