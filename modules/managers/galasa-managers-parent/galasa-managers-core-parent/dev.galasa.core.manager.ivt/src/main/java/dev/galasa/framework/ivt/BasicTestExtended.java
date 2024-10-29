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
public class BasicTestExtended extends BasicTest {
	
	private static Log logger = LogFactory.getLog(BasicTestExtended.class);
	
	/**
	 * Constructor
	 */
	public BasicTestExtended() {
		logger.info("Constructor");		
	}
	
	/**
	 * {@literal @}BeforeClass method
	 */
	@BeforeClass
	public void testBeforeClassExtended() {
		logger.info("@BeforeClass annotated method");
	}
	
	/**
	 * {@literal @}Before method
	 */
	@Before
	@Override
	public void testBefore() {
		logger.info("@Before annotated method");
	}
	
	/**
	 * {@literal @}Test method 1
	 */
	@Test
	@Override
	public void testTest1() {
		logger.info("@Test annotated method - 1");
	}
	
	/**
	 * {@literal @}Test method 3
	 */
	@Test
	@Override
	public void testTest3() {
		logger.info("@Test annotated method - 3");
	}
	
	/**
	 * {@literal @}Test method 4
	 */
	@Test
	public void testTest4() {
		logger.info("@Test annotated method - 4");
	}
	
	/**
	 * {@literal @}After method
	 */
	@After
	@Override
	public void testAfter() {
		logger.info("@After annotated method");
	}
	
	/**
	 * {@literal @}AfterClass method
	 */
	@AfterClass
	@Override
	public void testAfterClass() {
		logger.info("@AfterClass annotated method");
	}
}
