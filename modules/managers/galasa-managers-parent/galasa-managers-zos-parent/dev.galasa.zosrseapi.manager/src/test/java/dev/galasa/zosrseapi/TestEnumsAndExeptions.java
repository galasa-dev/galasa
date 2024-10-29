/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosrseapi;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosrseapi.IRseapi.RseapiRequestType;

public class TestEnumsAndExeptions {
    
    private static final String EXCEPTION_MESSAGE = "exception-message";
    
    private static final String EXCEPTION_CAUSE = "exception-cause";
    
    @Test
    public void testRseapiRequestType() {
        Assert.assertEquals("Problem with RseapiRequestType", "DELETE", RseapiRequestType.DELETE.getRequestType());
        Assert.assertEquals("Problem with RseapiRequestType", "GET", RseapiRequestType.GET.getRequestType());
        Assert.assertEquals("Problem with RseapiRequestType", "POST", RseapiRequestType.POST_JSON.getRequestType());
        Assert.assertEquals("Problem with RseapiRequestType", "PUT", RseapiRequestType.PUT_TEXT.getRequestType());
        Assert.assertEquals("Problem with RseapiRequestType", "PUT", RseapiRequestType.PUT_JSON.getRequestType());
    }
    
    @Test
    public void testRseapiException1() throws RseapiException {
    	Assert.assertThrows(RseapiManagerException.class, ()->{
        	throw new RseapiException();
        });
    }
    
    @Test
    public void testRseapiException2() throws RseapiException {
    	RseapiManagerException expectedException = Assert.assertThrows(RseapiManagerException.class, ()->{
        	throw new RseapiException(EXCEPTION_MESSAGE);
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    }
    
    @Test
    public void testRseapiException3() throws RseapiException {
    	RseapiManagerException expectedException = Assert.assertThrows(RseapiManagerException.class, ()->{
        	throw new RseapiException(new Exception(EXCEPTION_CAUSE));
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testRseapiException4() throws RseapiException {
    	RseapiManagerException expectedException = Assert.assertThrows(RseapiManagerException.class, ()->{
        	throw new RseapiException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testRseapiException5() throws RseapiException {
    	RseapiManagerException expectedException = Assert.assertThrows(RseapiManagerException.class, ()->{
        	throw new RseapiException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosManagerException1() throws ZosManagerException {
    	Assert.assertThrows(ZosManagerException.class, ()->{
        	throw new ZosManagerException();
        });
    }
}
