/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.ceci.internal;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

import dev.galasa.cicsts.CeciManagerException;
import dev.galasa.cicsts.ICeciResponseOutputValue;

public class TestCeciResponseImpl {
    
    @Mock
    ICeciResponseOutputValue responseOutputValueMock;

    private static CeciResponseImpl ceciResponseImplNormal;

    private static CeciResponseImpl ceciResponseImplAbend;
    
    @BeforeClass
    public static void beforeClass() {
        ceciResponseImplNormal = new CeciResponseImpl("NORMAL", 0, 0);
        ceciResponseImplAbend = new CeciResponseImpl("ABEND ABCD", 0, 0);
    }

    @Test
    public void testIsNormal() {
        Assert.assertTrue("Unxpected result", ceciResponseImplNormal.isNormal());
    }
    
    @Test
    public void testCheckNormal() throws CeciManagerException {
        Assert.assertEquals("Unxpected result", ceciResponseImplNormal, ceciResponseImplNormal.checkNormal());
        
        CeciManagerException expectedException = Assert.assertThrows("expected exception should be thrown", CeciManagerException.class, ()->{
        	ceciResponseImplAbend.checkNormal();
        });
        Assert.assertEquals("CECI response is not 'NORMAL', actual response is 'ABEND ABCD'", expectedException.getMessage());
    	
    }
    
    @Test
    public void testCheckNotAbended() throws CeciManagerException {
        Assert.assertEquals("Unxpected result", ceciResponseImplNormal, ceciResponseImplNormal.checkNotAbended());
        
        CeciManagerException expectedException = Assert.assertThrows("expected exception should be thrown", CeciManagerException.class, ()->{
        	ceciResponseImplAbend.checkNotAbended();
        });
        Assert.assertEquals("CECI response is an abend 'ABEND ABCD'", expectedException.getMessage());
    	
    }
    
    @Test
    public void testCheckAbended() throws CeciManagerException {
        Assert.assertEquals("Unxpected result", ceciResponseImplAbend, ceciResponseImplAbend.checkAbended(null));
        Assert.assertEquals("Unxpected result", ceciResponseImplAbend, ceciResponseImplAbend.checkAbended("ABCD"));
        
        CeciManagerException expectedException = Assert.assertThrows("expected exception should be thrown", CeciManagerException.class, ()->{
        	ceciResponseImplNormal.checkAbended(null);
        });
        Assert.assertEquals("exception should contain expected cause", "CECI response was not an abend - response = 'NORMAL'", expectedException.getMessage());
        
        expectedException = Assert.assertThrows("expected exception should be thrown", CeciManagerException.class, ()->{
        	ceciResponseImplNormal.checkAbended("ABCD");
        });
        Assert.assertEquals("exception should contain expected cause", "CECI response did not abend with 'ABCD' - response = 'NORMAL'", expectedException.getMessage());
    	
    }

    @Test
    public void testGetResponse() {
        Assert.assertEquals("Unxpected result", "NORMAL", ceciResponseImplNormal.getResponse());
    }

    @Test
    public void testGetEIBRESP() {
        Assert.assertEquals("Unxpected result", 0, ceciResponseImplNormal.getEIBRESP());
    }

    @Test
    public void testGetEIBRESP2() {
        Assert.assertEquals("Unxpected result", 0, ceciResponseImplNormal.getEIBRESP2());
    }

    @Test
    public void testResponseOutputValues() {
        Map<String, ICeciResponseOutputValue> responseOutput = new LinkedHashMap<>();
        responseOutput.put("KEY", responseOutputValueMock);
        ceciResponseImplNormal.setResponseOutput(responseOutput);
        Assert.assertTrue("Unxpected result", responseOutput.equals(ceciResponseImplNormal.getResponseOutputValues()));
    }

    @Test
    public void testToString() {
        String result = String.format("RESPONSE: %s EIBRESP=%+010d EIBRESP2=%+010d", "NORMAL", 0, 0);
        Assert.assertEquals("Unxpected result", result, ceciResponseImplNormal.toString());
    }

}
