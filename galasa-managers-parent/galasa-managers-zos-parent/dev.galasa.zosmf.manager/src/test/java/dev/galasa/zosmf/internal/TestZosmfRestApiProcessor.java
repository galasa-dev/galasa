/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosmf.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//import org.powermock.reflect.Whitebox;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosmf.IZosmf;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.ZosmfException;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ZosmfRequestType.class})
public class TestZosmfRestApiProcessor {
//    
//    private ZosmfRestApiProcessor zosmfRestApiProcessor;
//    
//    private ZosmfRestApiProcessor zosmfRestApiProcessorSpy;
//
//    @Mock
//    private IZosImage zosImageMock;
//    
//    @Mock
//    private ZosmfImpl zosmfMock1;
//    
//    @Mock
//    private ZosmfImpl zosmfMock2;
//    
//    @Mock
//    private ZosmfImpl zosmfMock3;
//    
//    @Mock
//    private IZosmfResponse zosmfResponseMock;
//    
//    private HashMap<String, IZosmf> zosmfs = new LinkedHashMap<>();
//    
//    private static final String PATH = "request-path";
//
//    @Test
//    public void testSendRequest() throws ZosmfException {
//        zosmfs.put("image1", zosmfMock1);
//        zosmfRestApiProcessor = new ZosmfRestApiProcessor(zosmfs);
//        zosmfRestApiProcessorSpy = PowerMockito.spy(zosmfRestApiProcessor);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(zosmfMock1.get(Mockito.anyString(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfMock1.getRequestRetry()).thenReturn(1);
//        HashMap<String, String> headers = new HashMap<>();
//        headers.put("key", "value");
//        
//        IZosmfResponse response = zosmfRestApiProcessorSpy.sendRequest(ZosmfRequestType.GET, PATH, headers, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)), false);
//        Assert.assertEquals("sendRequest() should return the expected value", HttpStatus.SC_OK, response.getStatusCode());
//        
//        response = zosmfRestApiProcessorSpy.sendRequest(ZosmfRequestType.GET, PATH, null, null, null, false);
//        Assert.assertEquals("sendRequest() should return the expected value", HttpStatus.SC_OK, response.getStatusCode());
//        
//        zosmfs.put("image1", zosmfMock1);
//        zosmfs.put("image2", zosmfMock1);
//        zosmfRestApiProcessor = new ZosmfRestApiProcessor(zosmfs);
//        zosmfRestApiProcessorSpy = PowerMockito.spy(zosmfRestApiProcessor);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND).thenReturn(HttpStatus.SC_OK);
//        response = zosmfRestApiProcessorSpy.sendRequest(ZosmfRequestType.GET, PATH, null, null, null, false);
//        Assert.assertEquals("sendRequest() should return the expected value", HttpStatus.SC_OK, response.getStatusCode());
//
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(zosmfMock1.postJson(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(zosmfResponseMock);
//        response = zosmfRestApiProcessorSpy.sendRequest(ZosmfRequestType.POST_JSON, PATH, null, null, null, false);
//        Assert.assertEquals("sendRequest() should return the expected value", HttpStatus.SC_OK, response.getStatusCode());
//
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(zosmfMock1.putText(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(zosmfResponseMock);
//        response = zosmfRestApiProcessorSpy.sendRequest(ZosmfRequestType.PUT_TEXT, PATH, null, null, null, false);
//        Assert.assertEquals("sendRequest() should return the expected value", HttpStatus.SC_OK, response.getStatusCode());
//
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(zosmfMock1.putJson(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(zosmfResponseMock);
//        response = zosmfRestApiProcessorSpy.sendRequest(ZosmfRequestType.PUT_JSON, PATH, null, null, null, false);
//        Assert.assertEquals("sendRequest() should return the expected value", HttpStatus.SC_OK, response.getStatusCode());
//
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(zosmfMock1.putBinary(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(zosmfResponseMock);
//        response = zosmfRestApiProcessorSpy.sendRequest(ZosmfRequestType.PUT_BINARY, PATH, null, null, null, false);
//        Assert.assertEquals("sendRequest() should return the expected value", HttpStatus.SC_OK, response.getStatusCode());
//
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(zosmfMock1.delete(Mockito.anyString(), Mockito.any())).thenReturn(zosmfResponseMock);
//        response = zosmfRestApiProcessorSpy.sendRequest(ZosmfRequestType.DELETE, PATH, null, null, null, false);
//        Assert.assertEquals("sendRequest() should return the expected value", HttpStatus.SC_OK, response.getStatusCode());
//
//        ZosmfRequestType INVALID = PowerMockito.mock(ZosmfRequestType.class);
//        String expectedMessage = "Unable to get valid response from zOS/MF server";
//        ZosmfException expectedException = Assert.assertThrows("expected exception should be thrown", ZosmfException.class, ()->{
//            zosmfRestApiProcessorSpy.sendRequest(INVALID, PATH, null, null, null, false);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetCurrentZosmfServer() throws ZosmfException {
//        zosmfs.put("image1", zosmfMock1);
//        zosmfRestApiProcessor = new ZosmfRestApiProcessor(zosmfs);
//        zosmfRestApiProcessorSpy = PowerMockito.spy(zosmfRestApiProcessor);
//        
//        Assert.assertEquals("getNextZosmf() should set the expected value", zosmfMock1, zosmfRestApiProcessorSpy.getCurrentZosmfServer());
//    }
//    
//    @Test
//    public void testGetNextZosmf() throws ZosmfException {
//        zosmfs.put("image1", zosmfMock1);
//        zosmfRestApiProcessor = new ZosmfRestApiProcessor(zosmfs);
//        zosmfRestApiProcessorSpy = PowerMockito.spy(zosmfRestApiProcessor);
//        
//        zosmfRestApiProcessorSpy.getNextZosmf();
//        Assert.assertEquals("getNextZosmf() should set the expected value", zosmfMock1, Whitebox.getInternalState(zosmfRestApiProcessorSpy, "currentZosmf"));
//        
//        zosmfs.put("image1", zosmfMock1);
//        zosmfs.put("image2", zosmfMock2);
//        zosmfs.put("image3", zosmfMock3);
//        zosmfRestApiProcessor = new ZosmfRestApiProcessor(zosmfs);
//        zosmfRestApiProcessorSpy = PowerMockito.spy(zosmfRestApiProcessor);
//        
//        zosmfRestApiProcessorSpy.getNextZosmf();
//        Assert.assertEquals("getNextZosmf() should set the expected value", zosmfMock2, Whitebox.getInternalState(zosmfRestApiProcessorSpy, "currentZosmf"));
//        
//        zosmfRestApiProcessorSpy.getNextZosmf();
//        Assert.assertEquals("getNextZosmf() should set the expected value", zosmfMock3, Whitebox.getInternalState(zosmfRestApiProcessorSpy, "currentZosmf"));
//        
//        zosmfRestApiProcessorSpy.getNextZosmf();
//        Assert.assertEquals("getNextZosmf() should set the expected value", zosmfMock1, Whitebox.getInternalState(zosmfRestApiProcessorSpy, "currentZosmf"));
//        
//        Whitebox.setInternalState(zosmfRestApiProcessorSpy, "zosmfs", zosmfs = new LinkedHashMap<>());
//        zosmfRestApiProcessorSpy.getNextZosmf();
//        Assert.assertEquals("getNextZosmf() should set the expected value", zosmfMock1, Whitebox.getInternalState(zosmfRestApiProcessorSpy, "currentZosmf"));
//    }
}
