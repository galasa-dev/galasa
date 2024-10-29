/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan;

import org.junit.Assert;
import org.junit.Test;

public class TestExceptions {

    @Test
    public void testCheckpointException() throws CheckpointException {
    	Assert.assertThrows("expected exception should be thrown", CheckpointException.class, ()->{
    		throw new CheckpointException();
    	});
    	Assert.assertThrows("expected exception should be thrown", CheckpointException.class, ()->{
    		throw new CheckpointException("EXCEPTION");
    	});
    	Assert.assertThrows("expected exception should be thrown", CheckpointException.class, ()->{
    		throw new CheckpointException(new Exception());
    	});
    	Assert.assertThrows("expected exception should be thrown", CheckpointException.class, ()->{
    		throw new CheckpointException("EXCEPTION", new Exception());
    	});
    	Assert.assertThrows("expected exception should be thrown", CheckpointException.class, ()->{
    		throw new CheckpointException("EXCEPTION", new Exception(), false, false);
    	});
    }

    @Test
    public void testFailTextFoundException() throws FailTextFoundException {
    	Assert.assertThrows("expected exception should be thrown", FailTextFoundException.class, ()->{
    		throw new FailTextFoundException();
    	});
    	Assert.assertThrows("expected exception should be thrown", FailTextFoundException.class, ()->{
    		throw new FailTextFoundException("EXCEPTION");
    	});
    	Assert.assertThrows("expected exception should be thrown", FailTextFoundException.class, ()->{
    		throw new FailTextFoundException(new Exception());
    	});
    	Assert.assertThrows("expected exception should be thrown", FailTextFoundException.class, ()->{
    		throw new FailTextFoundException("EXCEPTION", new Exception());
    	});
    	Assert.assertThrows("expected exception should be thrown", FailTextFoundException.class, ()->{
    		throw new FailTextFoundException("EXCEPTION", new Exception(), false, false);
    	});
    }

    @Test
	public void testIncorrectOccurrencesException() throws IncorrectOccurrencesException {
		Assert.assertThrows("expected exception should be thrown", IncorrectOccurrencesException.class, ()->{
			throw new IncorrectOccurrencesException();
		});
		Assert.assertThrows("expected exception should be thrown", IncorrectOccurrencesException.class, ()->{
			throw new IncorrectOccurrencesException("EXCEPTION");
		});
		Assert.assertThrows("expected exception should be thrown", IncorrectOccurrencesException.class, ()->{
			throw new IncorrectOccurrencesException(new Exception());
		});
		Assert.assertThrows("expected exception should be thrown", IncorrectOccurrencesException.class, ()->{
			throw new IncorrectOccurrencesException("EXCEPTION", new Exception());
		});
		Assert.assertThrows("expected exception should be thrown", IncorrectOccurrencesException.class, ()->{
			throw new IncorrectOccurrencesException("EXCEPTION", new Exception(), false, false);
		});
	}

	@Test
    public void testMissingTextException() throws MissingTextException {
    	Assert.assertThrows("expected exception should be thrown", MissingTextException.class, ()->{
    		throw new MissingTextException();
    	});
    	Assert.assertThrows("expected exception should be thrown", MissingTextException.class, ()->{
    		throw new MissingTextException("EXCEPTION");
    	});
    	Assert.assertThrows("expected exception should be thrown", MissingTextException.class, ()->{
    		throw new MissingTextException(new Exception());
    	});
    	Assert.assertThrows("expected exception should be thrown", MissingTextException.class, ()->{
    		throw new MissingTextException("EXCEPTION", new Exception());
    	});
    	Assert.assertThrows("expected exception should be thrown", MissingTextException.class, ()->{
    		throw new MissingTextException("EXCEPTION", new Exception(), false, false);
    	});
    }

    @Test
    public void testTextScanException() throws TextScanException {
    	Assert.assertThrows("expected exception should be thrown", TextScanException.class, ()->{
    		throw new TextScanException();
    	});
    	Assert.assertThrows("expected exception should be thrown", TextScanException.class, ()->{
    		throw new TextScanException("EXCEPTION");
    	});
    	Assert.assertThrows("expected exception should be thrown", TextScanException.class, ()->{
    		throw new TextScanException(new Exception());
    	});
    	Assert.assertThrows("expected exception should be thrown", TextScanException.class, ()->{
    		throw new TextScanException("EXCEPTION", new Exception());
    	});
    	Assert.assertThrows("expected exception should be thrown", TextScanException.class, ()->{
    		throw new TextScanException("EXCEPTION", new Exception(), false, false);
    	});
    }

    @Test
    public void testTextScanManagerException() throws TextScanManagerException {
    	Assert.assertThrows("expected exception should be thrown", TextScanManagerException.class, ()->{
    		throw new TextScanManagerException();
    	});
    	Assert.assertThrows("expected exception should be thrown", TextScanManagerException.class, ()->{
    		throw new TextScanManagerException("EXCEPTION");
    	});
    	Assert.assertThrows("expected exception should be thrown", TextScanManagerException.class, ()->{
    		throw new TextScanManagerException(new Exception());
    	});
    	Assert.assertThrows("expected exception should be thrown", TextScanManagerException.class, ()->{
    		throw new TextScanManagerException("EXCEPTION", new Exception());
    	});
    	Assert.assertThrows("expected exception should be thrown", TextScanManagerException.class, ()->{
    		throw new TextScanManagerException("EXCEPTION", new Exception(), false, false);
    	});
    }
    // 
    // 
    // TextScanManagerException
    
}

