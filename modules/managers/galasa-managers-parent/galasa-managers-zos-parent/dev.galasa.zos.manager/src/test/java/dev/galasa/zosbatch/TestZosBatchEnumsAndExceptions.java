/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zosbatch.IZosBatchJob.JobStatus;

public class TestZosBatchEnumsAndExceptions {
    
    private static final String EXCEPTION_MESSAGE = "exception-message";
    
    private static final String EXCEPTION_CAUSE = "exception-cause";
    
    @Test
    public void testJobStatus() {
        Assert.assertEquals("Problem with JobStatus", "INPUT", JobStatus.INPUT.toString());
        Assert.assertEquals("Problem with JobStatus", "ACTIVE", JobStatus.ACTIVE.toString());
        Assert.assertEquals("Problem with JobStatus", "OUTPUT", JobStatus.OUTPUT.toString()); 
        Assert.assertEquals("Problem with JobStatus", "NOTFOUND", JobStatus.NOTFOUND.toString()); 
        Assert.assertEquals("Problem with JobStatus", "UNKNOWN", JobStatus.UNKNOWN.toString());

        Assert.assertEquals("Problem with JobStatus", JobStatus.INPUT, JobStatus.valueOfLabel("INPUT"));
        Assert.assertEquals("Problem with JobStatus", JobStatus.ACTIVE, JobStatus.valueOfLabel("ACTIVE"));
        Assert.assertEquals("Problem with JobStatus", JobStatus.OUTPUT, JobStatus.valueOfLabel("OUTPUT"));
        Assert.assertEquals("Problem with JobStatus", JobStatus.NOTFOUND, JobStatus.valueOfLabel("NOTFOUND"));
        Assert.assertEquals("Problem with JobStatus", JobStatus.UNKNOWN, JobStatus.valueOfLabel("UNKNOWN"));
        Assert.assertEquals("Problem with JobStatus", JobStatus.UNKNOWN, JobStatus.valueOfLabel("INVALID"));
    }
    
    @Test
    public void testZosBatchException1() throws ZosBatchException {
    	Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
    		throw new ZosBatchException();
    	});
    }
    
    @Test
	public void testZosBatchException2() throws ZosBatchException {
		ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
			throw new ZosBatchException(new Exception(EXCEPTION_CAUSE));
		});
    	Assert.assertEquals("exception should contain expected cause", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
	}
    
    @Test
	public void testZosBatchException3() throws ZosBatchException {
		ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
			throw new ZosBatchException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
		});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    	Assert.assertEquals("exception should contain expected cause", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
	}

	@Test
    public void testZosBatchException4() throws ZosBatchException {
		ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
    		throw new ZosBatchException(EXCEPTION_MESSAGE);
    	});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    }
    
    @Test
    public void testZosBatchException5() throws ZosBatchException {
    	ZosBatchException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchException.class, ()->{
    		throw new ZosBatchException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), true, true);
    	});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    	Assert.assertEquals("exception should contain expected cause", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosBatchManagerException1() throws ZosBatchManagerException {
    	Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
    		throw new ZosBatchManagerException();
    	});
    }
    
    @Test
	public void testZosBatchManagerException2() throws ZosBatchManagerException {
		ZosBatchManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
			throw new ZosBatchManagerException(new Exception(EXCEPTION_CAUSE));
		});
    	Assert.assertEquals("exception should contain expected cause", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
	}
    
    @Test
	public void testZosBatchManagerException3() throws ZosBatchManagerException {
		ZosBatchManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
			throw new ZosBatchManagerException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
		});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    	Assert.assertEquals("exception should contain expected cause", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
	}

	@Test
    public void testZosBatchManagerException4() throws ZosBatchManagerException {
    	ZosBatchManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
    		throw new ZosBatchManagerException(EXCEPTION_MESSAGE);
    	});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    }
    
    @Test
    public void testZosBatchManagerException5() throws ZosBatchManagerException {
    	ZosBatchManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
    		throw new ZosBatchManagerException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), true, true);
    	});
    	Assert.assertEquals("exception should contain expected message", EXCEPTION_MESSAGE, expectedException.getMessage());
    	Assert.assertEquals("exception should contain expected cause", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
}
