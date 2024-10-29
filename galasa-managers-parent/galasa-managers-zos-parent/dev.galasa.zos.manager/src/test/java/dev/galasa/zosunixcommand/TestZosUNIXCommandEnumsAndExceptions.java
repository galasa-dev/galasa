/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosunixcommand;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zosfile.IZosUNIXFile.UNIXFileDataType;

public class TestZosUNIXCommandEnumsAndExceptions {
    
    private static final String EXCEPTION_MESSAGE = "exception-message";
    
    private static final String EXCEPTION_CAUSE = "exception-cause";
    
    @Test
    public void testUNIXFileDataType() {
        Assert.assertEquals("Problem with UNIXFileDataType", "text", UNIXFileDataType.TEXT.toString());
        Assert.assertEquals("Problem with UNIXFileDataType", "binary", UNIXFileDataType.BINARY.toString());
    }
    
    @Test
    public void testZosUNIXCommandException1() throws ZosUNIXCommandException {
    	Assert.assertThrows("expected exception should be thrown", ZosUNIXCommandException.class, ()->{
    		throw new ZosUNIXCommandException();
    	});
    }
    
    @Test
    public void testZosUNIXCommandException2() throws ZosUNIXCommandException {
    	ZosUNIXCommandException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXCommandException.class, ()->{
    		throw new ZosUNIXCommandException(EXCEPTION_MESSAGE);
    	});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    }
    
    @Test
    public void testZosUNIXCommandException3() throws ZosUNIXCommandException {
    	ZosUNIXCommandException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXCommandException.class, ()->{
    		throw new ZosUNIXCommandException(new Exception(EXCEPTION_CAUSE));
    	});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosUNIXCommandException4() throws ZosUNIXCommandException {
    	ZosUNIXCommandException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXCommandException.class, ()->{
    		throw new ZosUNIXCommandException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
    	});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosUNIXCommandException5() throws ZosUNIXCommandException {
    	ZosUNIXCommandException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXCommandException.class, ()->{
    		throw new ZosUNIXCommandException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
    	});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosUNIXCommandManagerException1() throws ZosUNIXCommandManagerException {
    	Assert.assertThrows("expected exception should be thrown", ZosUNIXCommandManagerException.class, ()->{
    		throw new ZosUNIXCommandManagerException();
    	});
    }
    
    @Test
    public void testZosUNIXCommandManagerException2() throws ZosUNIXCommandManagerException {
    	ZosUNIXCommandManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXCommandManagerException.class, ()->{
    		throw new ZosUNIXCommandManagerException(EXCEPTION_MESSAGE);
    	});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    }
    
    @Test
    public void testZosUNIXCommandManagerException3() throws ZosUNIXCommandManagerException {
    	ZosUNIXCommandManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXCommandManagerException.class, ()->{
    		throw new ZosUNIXCommandManagerException(new Exception(EXCEPTION_CAUSE));
    	});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosUNIXCommandManagerException4() throws ZosUNIXCommandManagerException {
    	ZosUNIXCommandManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXCommandManagerException.class, ()->{
    		throw new ZosUNIXCommandManagerException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
    	});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosUNIXCommandManagerException5() throws ZosUNIXCommandManagerException {
    	ZosUNIXCommandManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXCommandManagerException.class, ()->{
    		throw new ZosUNIXCommandManagerException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
    	});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosUNIXCommandAuthFailException1() throws ZosUNIXCommandAuthFailException {
    	Assert.assertThrows("expected exception should be thrown", ZosUNIXCommandAuthFailException.class, ()->{
    		throw new ZosUNIXCommandAuthFailException();
    	});
    }
    
    @Test
    public void testZosUNIXCommandAuthFailException2() throws ZosUNIXCommandAuthFailException {
    	ZosUNIXCommandAuthFailException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXCommandAuthFailException.class, ()->{
    		throw new ZosUNIXCommandAuthFailException(EXCEPTION_MESSAGE);
    	});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    }
    
    @Test
    public void testZosUNIXCommandAuthFailException3() throws ZosUNIXCommandAuthFailException {
    	ZosUNIXCommandAuthFailException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXCommandAuthFailException.class, ()->{
    		throw new ZosUNIXCommandAuthFailException(new Exception(EXCEPTION_CAUSE));
    	});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosUNIXCommandAuthFailException4() throws ZosUNIXCommandAuthFailException {
    	ZosUNIXCommandAuthFailException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXCommandAuthFailException.class, ()->{
    		throw new ZosUNIXCommandAuthFailException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
    	});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosUNIXCommandAuthFailException5() throws ZosUNIXCommandAuthFailException {
    	ZosUNIXCommandAuthFailException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXCommandAuthFailException.class, ()->{
    		throw new ZosUNIXCommandAuthFailException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
    	});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
}
