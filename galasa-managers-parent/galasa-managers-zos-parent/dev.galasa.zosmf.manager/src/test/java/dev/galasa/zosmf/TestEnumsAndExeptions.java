/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosmf;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosmf.IZosmf.ZosmfCustomHeaders;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;

public class TestEnumsAndExeptions {
    
    private static final String EXCEPTION_MESSAGE = "exception-message";
    
    private static final String EXCEPTION_CAUSE = "exception-cause";
    
    @Test
    public void testZosmfRequestType() {
        Assert.assertEquals("Problem with ZosmfRequestType", "DELETE", ZosmfRequestType.DELETE.toString());
        Assert.assertEquals("Problem with ZosmfRequestType", "GET", ZosmfRequestType.GET.toString());
        Assert.assertEquals("Problem with ZosmfRequestType", "POST", ZosmfRequestType.POST.toString());
        Assert.assertEquals("Problem with ZosmfRequestType", "POST_JSON", ZosmfRequestType.POST_JSON.toString());
        Assert.assertEquals("Problem with ZosmfRequestType", "PUT", ZosmfRequestType.PUT.toString());
        Assert.assertEquals("Problem with ZosmfRequestType", "PUT_BINARY", ZosmfRequestType.PUT_BINARY.toString());
        Assert.assertEquals("Problem with ZosmfRequestType", "PUT_JSON", ZosmfRequestType.PUT_JSON.toString());
        Assert.assertEquals("Problem with ZosmfRequestType", "PUT_TEXT", ZosmfRequestType.PUT_TEXT.toString());
    }
    
    @Test
    public void testZosmfCustomHeaders() {
        Assert.assertEquals("Problem with ZosmfCustomHeaders", "X-CSRF-ZOSMF-HEADER", ZosmfCustomHeaders.X_CSRF_ZOSMF_HEADER.toString());
        Assert.assertEquals("Problem with ZosmfCustomHeaders", "X-IBM-Attributes", ZosmfCustomHeaders.X_IBM_ATTRIBUTES.toString());
        Assert.assertEquals("Problem with ZosmfCustomHeaders", "X-IBM-Data-Type", ZosmfCustomHeaders.X_IBM_DATA_TYPE.toString());
        Assert.assertEquals("Problem with ZosmfCustomHeaders", "X-IBM-Intrdr-Lrecl", ZosmfCustomHeaders.X_IBM_INTRDR_LRECL.toString());
        Assert.assertEquals("Problem with ZosmfCustomHeaders", "X-IBM-Intrdr-Recfm", ZosmfCustomHeaders.X_IBM_INTRDR_RECFM.toString());
        Assert.assertEquals("Problem with ZosmfCustomHeaders", "X-IBM-Job-Modify-Version", ZosmfCustomHeaders.X_IBM_JOB_MODIFY_VERSION.toString());
        Assert.assertEquals("Problem with ZosmfCustomHeaders", "X-IBM-Lstat", ZosmfCustomHeaders.X_IBM_LSTAT.toString());
        Assert.assertEquals("Problem with ZosmfCustomHeaders", "X-IBM-Max-Items", ZosmfCustomHeaders.X_IBM_MAX_ITEMS.toString());
        Assert.assertEquals("Problem with ZosmfCustomHeaders", "X-IBM-Option", ZosmfCustomHeaders.X_IBM_OPTION.toString());
        Assert.assertEquals("Problem with ZosmfCustomHeaders", "X-IBM-Requested-Method", ZosmfCustomHeaders.X_IBM_REQUESTED_METHOD.toString());
    }
    
    @Test
    public void testZosmfException1() throws ZosmfException {
        Assert.assertThrows("expected exception should be thrown", ZosmfException.class, ()->{
        	throw new ZosmfException();
        });
    }
    
    @Test
    public void testZosmfException2() throws ZosmfException {
        ZosmfException expectedException = Assert.assertThrows("expected exception should be thrown", ZosmfException.class, ()->{
        	throw new ZosmfException(EXCEPTION_MESSAGE);
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    }
    
    @Test
    public void testZosmfException3() throws ZosmfException {
        ZosmfException expectedException = Assert.assertThrows("expected exception should be thrown", ZosmfException.class, ()->{
        	throw new ZosmfException(new Exception(EXCEPTION_CAUSE));
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosmfException4() throws ZosmfException {
        ZosmfException expectedException = Assert.assertThrows("expected exception should be thrown", ZosmfException.class, ()->{
        	throw new ZosmfException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    }
    
    @Test
    public void testZosmfException5() throws ZosmfException {
        ZosmfException expectedException = Assert.assertThrows("expected exception should be thrown", ZosmfException.class, ()->{
        	throw new ZosmfException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
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
