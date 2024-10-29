/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.ceci.internal;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.cicsts.CeciException;

public class TestResponseOutputValueImpl {

    @Test
    public void testGetTextValue() {
        ResponseOutputValueImpl responseOutputValue = new ResponseOutputValueImpl("TEXT");
        Assert.assertEquals("Unxpected result", "TEXT", responseOutputValue.getTextValue());
        
        responseOutputValue = new ResponseOutputValueImpl(new String[] {"TEXT","HEX"});
        Assert.assertEquals("Unxpected result", "TEXT", responseOutputValue.getTextValue());
    }

    @Test
    public void testGetHexValue() {
        ResponseOutputValueImpl responseOutputValue = new ResponseOutputValueImpl("HEX");
        Assert.assertTrue("Unxpected result", Arrays.equals("HEX".toCharArray(), responseOutputValue.getHexValue()));

        responseOutputValue = new ResponseOutputValueImpl(new String[] {"TEXT","HEX"});
        Assert.assertTrue("Unxpected result", Arrays.equals("HEX".toCharArray(), responseOutputValue.getHexValue()));
    }

    @Test
    public void testGetIntValue() throws CeciException {
        ResponseOutputValueImpl responseOutputValue = new ResponseOutputValueImpl(99);
        Assert.assertEquals("Unxpected result", 99, responseOutputValue.getIntValue());
        
        String expectedMessage = "Value is " + String.class.getName() + " type";
        
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ResponseOutputValueImpl responseOutputValue1 = new ResponseOutputValueImpl("TEXT");
        	responseOutputValue1.getIntValue();
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testGetLongValue() throws CeciException {
        ResponseOutputValueImpl responseOutputValue = new ResponseOutputValueImpl(99L);
        Assert.assertEquals("Unxpected result", 99L, responseOutputValue.getLongValue());
        
        String expectedMessage = "Value is " + String.class.getName() + " type";
        
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ResponseOutputValueImpl responseOutputValue1 = new ResponseOutputValueImpl("TEXT");
        	responseOutputValue1.getLongValue();
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testToString() {
        ResponseOutputValueImpl responseOutputValue = new ResponseOutputValueImpl("TEXT");
        Assert.assertEquals("Unxpected result", "TEXT", responseOutputValue.toString());
        
        responseOutputValue = new ResponseOutputValueImpl("HEX");
        Assert.assertEquals("Unxpected result", "HEX", responseOutputValue.toString());
        
        responseOutputValue = new ResponseOutputValueImpl(99);
        Assert.assertEquals("Unxpected result", "99", responseOutputValue.toString());
        
        responseOutputValue = new ResponseOutputValueImpl(99L);
        Assert.assertEquals("Unxpected result", "99", responseOutputValue.toString());
    }

    @Test
    public void testIsArray() {
        ResponseOutputValueImpl responseOutputValue = new ResponseOutputValueImpl("TEXT");
        Assert.assertFalse("Unxpected result", responseOutputValue.isArray());
        
        responseOutputValue = new ResponseOutputValueImpl(new String[] {"TEXT","HEX"});
        Assert.assertTrue("Unxpected result", responseOutputValue.isArray());
    }
}
