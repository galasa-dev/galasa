/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosrseapi.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//import org.powermock.reflect.Whitebox;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosrseapi.IRseapi;
import dev.galasa.zosrseapi.IRseapi.RseapiRequestType;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.RseapiException;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({RseapiRequestType.class})
public class TestRseapiRestApiProcessor {
//    
//    private RseapiRestApiProcessor rseapiRestApiProcessor;
//    
//    private RseapiRestApiProcessor rseapiRestApiProcessorSpy;
//
//    @Mock
//    private IZosImage zosImageMock;
//    
//    @Mock
//    private RseapiImpl rseapiMock1;
//    
//    @Mock
//    private RseapiImpl rseapiMock2;
//    
//    @Mock
//    private RseapiImpl rseapiMock3;
//    
//    @Mock
//    private IRseapiResponse rseapiResponseMock;
//    
//    @Rule
//    public TestName testName = new TestName();
//    
//    private RseapiRequestType rseapiRequestTypeINVALID;
//    
//    private HashMap<String, IRseapi> rseapis = new LinkedHashMap<>();
//    
//    private static final String PATH = "request-path";
//    
//    @Before
//    public void mockEnum() {
//    	if (testName.getMethodName().equals("testSendRequest")) {
//	    	rseapiRequestTypeINVALID = PowerMockito.mock(RseapiRequestType.class);
//	    	Whitebox.setInternalState(rseapiRequestTypeINVALID, "ordinal", 6);
//	    	PowerMockito.mockStatic(RseapiRequestType.class);
//	        PowerMockito.when(RseapiRequestType.values()).thenReturn(new RseapiRequestType[]{RseapiRequestType.GET, RseapiRequestType.PUT_TEXT, RseapiRequestType.PUT_BINARY, RseapiRequestType.PUT_JSON, RseapiRequestType.POST_JSON, RseapiRequestType.DELETE, rseapiRequestTypeINVALID});
//    	}
//    }
//
//    @Test
//    public void testSendRequest() throws RseapiException {
//        rseapis.put("image1", rseapiMock1);
//        rseapiRestApiProcessor = new RseapiRestApiProcessor(rseapis);
//        rseapiRestApiProcessorSpy = PowerMockito.spy(rseapiRestApiProcessor);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(rseapiMock1.get(Mockito.anyString(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiMock1.getRequestRetry()).thenReturn(1);
//        HashMap<String, String> headers = new HashMap<>();
//        headers.put("key", "value");
//        
//        IRseapiResponse response = rseapiRestApiProcessorSpy.sendRequest(RseapiRequestType.GET, PATH, headers, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)), false);
//        Assert.assertEquals("sendRequest() should return the expected value", HttpStatus.SC_OK, response.getStatusCode());
//        
//        response = rseapiRestApiProcessorSpy.sendRequest(RseapiRequestType.GET, PATH, null, null, null, false);
//        Assert.assertEquals("sendRequest() should return the expected value", HttpStatus.SC_OK, response.getStatusCode());
//        
//        rseapis.put("image1", rseapiMock1);
//        rseapis.put("image2", rseapiMock1);
//        rseapiRestApiProcessor = new RseapiRestApiProcessor(rseapis);
//        rseapiRestApiProcessorSpy = PowerMockito.spy(rseapiRestApiProcessor);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND).thenReturn(HttpStatus.SC_OK);
//        response = rseapiRestApiProcessorSpy.sendRequest(RseapiRequestType.GET, PATH, null, null, null, false);
//        Assert.assertEquals("sendRequest() should return the expected value", HttpStatus.SC_OK, response.getStatusCode());
//
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(rseapiMock1.putText(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(rseapiResponseMock);
//        response = rseapiRestApiProcessorSpy.sendRequest(RseapiRequestType.PUT_TEXT, PATH, null, null, null, false);
//        Assert.assertEquals("sendRequest() should return the expected value", HttpStatus.SC_OK, response.getStatusCode());
//
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(rseapiMock1.putBinary(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(rseapiResponseMock);
//        response = rseapiRestApiProcessorSpy.sendRequest(RseapiRequestType.PUT_BINARY, PATH, null, null, null, false);
//        Assert.assertEquals("sendRequest() should return the expected value", HttpStatus.SC_OK, response.getStatusCode());
//
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(rseapiMock1.putJson(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(rseapiResponseMock);
//        response = rseapiRestApiProcessorSpy.sendRequest(RseapiRequestType.PUT_JSON, PATH, null, null, null, false);
//        Assert.assertEquals("sendRequest() should return the expected value", HttpStatus.SC_OK, response.getStatusCode());
//        
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(rseapiMock1.postJson(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(rseapiResponseMock);
//        response = rseapiRestApiProcessorSpy.sendRequest(RseapiRequestType.POST_JSON, PATH, null, null, null, false);
//        Assert.assertEquals("sendRequest() should return the expected value", HttpStatus.SC_OK, response.getStatusCode());
//
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(rseapiMock1.delete(Mockito.anyString(), Mockito.any())).thenReturn(rseapiResponseMock);
//        response = rseapiRestApiProcessorSpy.sendRequest(RseapiRequestType.DELETE, PATH, null, null, null, false);
//        Assert.assertEquals("sendRequest() should return the expected value", HttpStatus.SC_OK, response.getStatusCode());
//        
//        Mockito.when(rseapiMock1.get(Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException());
//        String expectedMessage = "Unable to get valid response from RSE API server";
//        RseapiException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiException.class, ()->{
//        	rseapiRestApiProcessorSpy.sendRequest(RseapiRequestType.GET, PATH, null, null, null, false);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    	
//    	expectedMessage = "Unable to get valid response from RSE API server";
//        expectedException = Assert.assertThrows("expected exception should be thrown", RseapiException.class, ()->{
//        	rseapiRestApiProcessorSpy.sendRequest(rseapiRequestTypeINVALID, PATH, null, null, null, false);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//  
//    @Test
//    public void testGetCurrentRseapiServer() throws RseapiException {
//        rseapis.put("image1", rseapiMock1);
//        rseapiRestApiProcessor = new RseapiRestApiProcessor(rseapis);
//        rseapiRestApiProcessorSpy = PowerMockito.spy(rseapiRestApiProcessor);
//        
//        Assert.assertEquals("getNextRseapi() should set the expected value", rseapiMock1, rseapiRestApiProcessorSpy.getCurrentRseapiServer());
//    }
//    
//    @Test
//    public void testGetNextRseapi() throws RseapiException {
//        rseapis.put("image1", rseapiMock1);
//        rseapiRestApiProcessor = new RseapiRestApiProcessor(rseapis);
//        rseapiRestApiProcessorSpy = PowerMockito.spy(rseapiRestApiProcessor);
//        
//        rseapiRestApiProcessorSpy.getNextRseapi();
//        Assert.assertEquals("getNextRseapi() should set the expected value", rseapiMock1, Whitebox.getInternalState(rseapiRestApiProcessorSpy, "currentRseapi"));
//        
//        rseapis.put("image1", rseapiMock1);
//        rseapis.put("image2", rseapiMock2);
//        rseapis.put("image3", rseapiMock3);
//        rseapiRestApiProcessor = new RseapiRestApiProcessor(rseapis);
//        rseapiRestApiProcessorSpy = PowerMockito.spy(rseapiRestApiProcessor);
//        
//        rseapiRestApiProcessorSpy.getNextRseapi();
//        Assert.assertEquals("getNextRseapi() should set the expected value", rseapiMock2, Whitebox.getInternalState(rseapiRestApiProcessorSpy, "currentRseapi"));
//        
//        rseapiRestApiProcessorSpy.getNextRseapi();
//        Assert.assertEquals("getNextRseapi() should set the expected value", rseapiMock3, Whitebox.getInternalState(rseapiRestApiProcessorSpy, "currentRseapi"));
//        
//        rseapiRestApiProcessorSpy.getNextRseapi();
//        Assert.assertEquals("getNextRseapi() should set the expected value", rseapiMock1, Whitebox.getInternalState(rseapiRestApiProcessorSpy, "currentRseapi"));
//        
//        Whitebox.setInternalState(rseapiRestApiProcessorSpy, "rseapis", rseapis = new LinkedHashMap<>());
//        rseapiRestApiProcessorSpy.getNextRseapi();
//        Assert.assertEquals("getNextRseapi() should set the expected value", rseapiMock1, Whitebox.getInternalState(rseapiRestApiProcessorSpy, "currentRseapi"));
//    }
}
