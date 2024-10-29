/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.artifact.manager;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.ManagerException;

public class DummyTest {

	@Test
	public void test() throws ManagerException {
    	Assert.assertThrows("expected exception should be thrown", ManagerException.class, ()->{
    		throw new ManagerException();
    	});
	}

}
