/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.ivt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.After;
import dev.galasa.AfterClass;
import dev.galasa.Before;
import dev.galasa.BeforeClass;
import dev.galasa.Test;

/**
 * Basic test to execute the eJAT annotated methods
 */
@Test
public class BasicTest {
	
	private static Log logger = LogFactory.getLog(BasicTest.class);
	
	/**
	 * Constructor
	 */
	public BasicTest() {
		logger.info("Constructor");		
	}
	
	/**
	 * {@literal @}BeforeClass method
	 */
	@BeforeClass
	public void testBeforeClass() {
		logger.info("@BeforeClass annotated method");
	}
	
	/**
	 * {@literal @}Before method
	 */
	@Before
	public void testBefore() {
		logger.info("@Before annotated method");
	}
	
	/**
	 * {@literal @}Test method 1
	 */
	@Test
	public void testTest1() {
		logger.info("@Test annotated method - 1");
	}
	
	/**
	 * {@literal @}Test method 2
	 */
	@Test
	public void testTest2() {
		logger.info("@Test annotated method - 2");
	}
	
	/**
	 * {@literal @}Test method 3
	 */
	@Test
	public void testTest3() {
		logger.info("@Test annotated method - 3");
	}
	
	/**
	 * {@literal @}After method
	 */
	@After
	public void testAfter() {
		logger.info("@After annotated method");
	}
	
	/**
	 * {@literal @}AfterClass method
	 */
	@AfterClass
	public void testAfterClass() {
		logger.info("@AfterClass annotated method");
	}
}
