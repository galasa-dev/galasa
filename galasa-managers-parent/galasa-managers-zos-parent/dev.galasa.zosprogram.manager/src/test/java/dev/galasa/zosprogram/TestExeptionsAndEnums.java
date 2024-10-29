/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosprogram;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosprogram.ZosProgram.Language;

public class TestExeptionsAndEnums {
    
    private static final String EXCEPTION_MESSAGE = "exception-message";
    
    private static final String EXCEPTION_CAUSE = "exception-cause";
    
    @Test
    public void testLanguage() {
        Assert.assertEquals("Problem with Language", ".cbl", Language.COBOL.getFileExtension());

        Assert.assertEquals("Problem with Language", Language.COBOL, Language.fromExtension(".cbl"));
        
        String extension = ".xxx";
    	String expectedMessage = "Extension " + extension  + " does not match supported languages";
        IllegalArgumentException expectedException = Assert.assertThrows("expected exception should be thrown", IllegalArgumentException.class, ()->{
            Language.fromExtension(extension);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testZosProgramException1() throws ZosProgramException {
    	Assert.assertThrows("expected exception should be thrown", ZosProgramException.class, ()->{
    		throw new ZosProgramException();
    	});
    }
    
    @Test
    public void testZosProgramException2() throws ZosProgramException {
    	ZosProgramException expectedException = Assert.assertThrows("expected exception should be thrown", ZosProgramException.class, ()->{
    		throw new ZosProgramException(EXCEPTION_MESSAGE);
    	});
        Assert.assertEquals("exception should contain expected cause", EXCEPTION_MESSAGE, expectedException.getMessage());
    }
    
    @Test
    public void testZosProgramException3() throws ZosProgramException {
    	ZosProgramException expectedException = Assert.assertThrows("expected exception should be thrown", ZosProgramException.class, ()->{
    		throw new ZosProgramException(new Exception(EXCEPTION_CAUSE));
    	});
        Assert.assertEquals("exception should contain expected cause", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosProgramException4() throws ZosProgramException {
    	ZosProgramException expectedException = Assert.assertThrows("expected exception should be thrown", ZosProgramException.class, ()->{
    		throw new ZosProgramException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
    	});
        Assert.assertEquals("exception should contain expected cause", EXCEPTION_MESSAGE, expectedException.getMessage());
        Assert.assertEquals("exception should contain expected cause", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosProgramException5() throws ZosProgramException {
    	ZosProgramException expectedException = Assert.assertThrows("expected exception should be thrown", ZosProgramException.class, ()->{
    		throw new ZosProgramException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
    	});
        Assert.assertEquals("exception should contain expected cause", EXCEPTION_MESSAGE, expectedException.getMessage());
        Assert.assertEquals("exception should contain expected cause", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
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
        Assert.assertEquals("exception should contain expected cause", EXCEPTION_MESSAGE, expectedException.getMessage());
    }
    
    @Test
    public void testZosManagerException3() throws ZosManagerException {
        ZosManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosManagerException.class, ()->{
        	throw new ZosManagerException(new Exception(EXCEPTION_CAUSE));
        });
        Assert.assertEquals("exception should contain expected cause", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosManagerException4() throws ZosManagerException {
        ZosManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosManagerException.class, ()->{
        	throw new ZosManagerException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
        });
        Assert.assertEquals("exception should contain expected cause", EXCEPTION_MESSAGE, expectedException.getMessage());
        Assert.assertEquals("exception should contain expected cause", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testZosManagerException5() throws ZosManagerException {
        ZosManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosManagerException.class, ()->{
        	throw new ZosManagerException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
        });
        Assert.assertEquals("exception should contain expected cause", EXCEPTION_MESSAGE, expectedException.getMessage());
        Assert.assertEquals("exception should contain expected cause", EXCEPTION_CAUSE, expectedException.getCause().getMessage());
    }
}
