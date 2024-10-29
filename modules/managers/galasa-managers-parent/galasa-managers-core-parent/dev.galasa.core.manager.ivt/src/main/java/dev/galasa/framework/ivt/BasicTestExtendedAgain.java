/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.ivt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.After;
import dev.galasa.Test;

/**
 * Basic test to execute the eJAT annotated methods
 */
@Test
public class BasicTestExtendedAgain extends BasicTestExtended {
	
	private static Log logger = LogFactory.getLog(BasicTestExtendedAgain.class);
	
	/**
	 * Constructor
	 */
	public BasicTestExtendedAgain() {
		logger.info("Constructor");	
	}

	
	/**
	 * {@literal @}Test method 5
	 */
	@Test
	public void testTest5() {
		logger.info("@Test annotated method - 5");
	}
	
	/**
	 * {@literal @}Test method 6
	 */
	@Test
	public void testTest6() {
		logger.info("@Test annotated method - 6");
	}
	
	/**
	 * {@literal @}After method
	 */
	@After
	public void testAfterAgain() {
		logger.info("@After annotated method");
	}
	
}
