/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos;

import org.junit.Assert;
import org.junit.Test;

public class TestZosManagerException {
    
    private static final String EXCEPTION_MESSAGE = "exception-message";
    
    private static final String EXCEPTION_CAUSE = "exception-cause";
    
    @Test
    public void testZosManagerException1() throws ZosManagerException {
        Assert.assertThrows("expected exception should be thrown", ZosManagerException.class, ()->{
        	throw new ZosManagerException();
        });
    }
    
    @Test
    public void testZosManagerException2() throws ZosManagerException {
        ZosManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosManagerException.class, ()->{
        	throw new ZosManagerException(EXCEPTION_MESSAGE);
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    }
    
    @Test
    public void testZosManagerException3() throws ZosManagerException {
        ZosManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosManagerException.class, ()->{
        	throw new ZosManagerException(new Exception(EXCEPTION_CAUSE));
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosManagerException4() throws ZosManagerException {
        ZosManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosManagerException.class, ()->{
        	throw new ZosManagerException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosManagerException5() throws ZosManagerException {
        ZosManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosManagerException.class, ()->{
        	throw new ZosManagerException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
}
