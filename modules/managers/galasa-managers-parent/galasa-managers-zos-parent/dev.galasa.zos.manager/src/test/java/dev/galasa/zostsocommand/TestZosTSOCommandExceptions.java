/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zostsocommand;

import org.junit.Assert;
import org.junit.Test;

public class TestZosTSOCommandExceptions {
    
    private static final String EXCEPTION_MESSAGE = "exception-message";
    
    private static final String EXCEPTION_CAUSE = "exception-cause";
    
    @Test
    public void testZosTSOCommandException1() throws ZosTSOCommandException {
    	Assert.assertThrows("expected exception should be thrown", ZosTSOCommandException.class, ()->{
    		throw new ZosTSOCommandException();
    	});
    }
    
    @Test
    public void testZosTSOCommandException2() throws ZosTSOCommandException {
    	ZosTSOCommandException expectedException = Assert.assertThrows("expected exception should be thrown", ZosTSOCommandException.class, ()->{
    		throw new ZosTSOCommandException(EXCEPTION_MESSAGE);
    	});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    }
    
    @Test
    public void testZosTSOCommandException3() throws ZosTSOCommandException {
    	ZosTSOCommandException expectedException = Assert.assertThrows("expected exception should be thrown", ZosTSOCommandException.class, ()->{
    		throw new ZosTSOCommandException(new Exception(EXCEPTION_CAUSE));
    	});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosTSOCommandException4() throws ZosTSOCommandException {
    	ZosTSOCommandException expectedException = Assert.assertThrows("expected exception should be thrown", ZosTSOCommandException.class, ()->{
    		throw new ZosTSOCommandException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
    	});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosTSOCommandException5() throws ZosTSOCommandException {
    	ZosTSOCommandException expectedException = Assert.assertThrows("expected exception should be thrown", ZosTSOCommandException.class, ()->{
    		throw new ZosTSOCommandException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
    	});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosTSOCommandManagerException1() throws ZosTSOCommandManagerException {
    	Assert.assertThrows("expected exception should be thrown", ZosTSOCommandManagerException.class, ()->{
    		throw new ZosTSOCommandManagerException();
    	});
    }
    
    @Test
    public void testZosTSOCommandManagerException2() throws ZosTSOCommandManagerException {
    	ZosTSOCommandManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosTSOCommandManagerException.class, ()->{
    		throw new ZosTSOCommandManagerException(EXCEPTION_MESSAGE);
    	});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    }
    
    @Test
    public void testZosTSOCommandManagerException3() throws ZosTSOCommandManagerException {
    	ZosTSOCommandManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosTSOCommandManagerException.class, ()->{
    		throw new ZosTSOCommandManagerException(new Exception(EXCEPTION_CAUSE));
    	});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosTSOCommandManagerException4() throws ZosTSOCommandManagerException {
    	ZosTSOCommandManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosTSOCommandManagerException.class, ()->{
    		throw new ZosTSOCommandManagerException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
    	});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosTSOCommandManagerException5() throws ZosTSOCommandManagerException {
    	ZosTSOCommandManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosTSOCommandManagerException.class, ()->{
    		throw new ZosTSOCommandManagerException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
    	});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
}
