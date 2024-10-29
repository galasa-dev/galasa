/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosconsole;

import org.junit.Assert;
import org.junit.Test;

public class TestZosConsoleExceptions {
    
    private static final String EXCEPTION_MESSAGE = "exception-message";
    
    private static final String EXCEPTION_CAUSE = "exception-cause";
    
    @Test
    public void testZosConsoleException1() throws ZosConsoleException {
        Assert.assertThrows("expected exception should be thrown", ZosConsoleException.class, ()->{
        	throw new ZosConsoleException();
        });
    }
    
    @Test
    public void testZosConsoleException2() throws ZosConsoleException {
    	ZosConsoleException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleException.class, ()->{
        	throw new ZosConsoleException(EXCEPTION_MESSAGE);
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    }
    
    @Test
    public void testZosConsoleException3() throws ZosConsoleException {
    	ZosConsoleException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleException.class, ()->{
        	throw new ZosConsoleException(new Exception(EXCEPTION_CAUSE));
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosConsoleException4() throws ZosConsoleException {
    	ZosConsoleException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleException.class, ()->{
        	throw new ZosConsoleException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosConsoleException5() throws ZosConsoleException {
    	ZosConsoleException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleException.class, ()->{
        	throw new ZosConsoleException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosConsoleManagerException1() throws ZosConsoleManagerException {
        Assert.assertThrows("expected exception should be thrown", ZosConsoleManagerException.class, ()->{
        	throw new ZosConsoleManagerException();
        });
    }
    
    @Test
    public void testZosConsoleManagerException2() throws ZosConsoleManagerException {
        ZosConsoleManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleManagerException.class, ()->{
        	throw new ZosConsoleManagerException(EXCEPTION_MESSAGE);
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    }
    
    @Test
    public void testZosConsoleManagerException3() throws ZosConsoleManagerException {
        ZosConsoleManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleManagerException.class, ()->{
        	throw new ZosConsoleManagerException(new Exception(EXCEPTION_CAUSE));
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosConsoleManagerException4() throws ZosConsoleManagerException {
        ZosConsoleManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleManagerException.class, ()->{
        	throw new ZosConsoleManagerException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosConsoleManagerException5() throws ZosConsoleManagerException {
        ZosConsoleManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleManagerException.class, ()->{
        	throw new ZosConsoleManagerException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
}
