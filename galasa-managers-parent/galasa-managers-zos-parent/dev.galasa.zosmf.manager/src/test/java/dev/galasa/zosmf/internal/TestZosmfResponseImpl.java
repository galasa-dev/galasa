/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosmf.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//import org.powermock.reflect.Whitebox;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.internal.properties.Https;
import dev.galasa.zosmf.internal.properties.RequestRetry;
import dev.galasa.zosmf.internal.properties.ServerPort;
import dev.galasa.zosmf.internal.properties.SysplexServers;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({SysplexServers.class, ServerPort.class, Https.class, RequestRetry.class})
public class TestZosmfResponseImpl {
//    
//    private ZosmfResponseImpl zosmfResponse;
//    
//    private ZosmfResponseImpl zosmfResponseSpy;
//    
//    @Mock
//    private IHttpClient httpClientMock;
//    
//    @Mock
//    private HttpClientResponse<String> httpClientResponseStringMock;
//    
//    @Mock
//    private HttpClientResponse<byte[]> httpClientResponseByteMock;
//    
//    @Mock
//    private HttpClientResponse<JsonObject> httpClientResponseJsonMock;
//    
//    @Mock
//    private CloseableHttpResponse closeableHttpResponseMock;
//    
//    @Mock 
//    private HttpEntity httpEntity;
//    
//    @Mock
//    private StatusLine statusLineMock;
//
//    private static final String URL = "http://domain/";
//
//    private static final String PATH = "request-path";
//    
//    private static final String CONTENT_STRING = "content";
//    
//    private static final String JSONOBJECT_CONTENT_STRING = "{\"name\": \"value\"}";
//    
//    private static final JsonObject JSONOBJECT_CONTENT = new JsonParser().parse((String) JSONOBJECT_CONTENT_STRING).getAsJsonObject();
//    
//    private static final String JSONARRAY_CONTENT_STRING = "[\"element1\", \"element2\"]";
//    
//    private static final JsonArray JSONARRAY_CONTENT = new JsonParser().parse((String) JSONARRAY_CONTENT_STRING).getAsJsonArray();
//
//    private static final String STATUS_LINE = "status-line";
//    
//    @Before
//    public void setup() throws Exception {        
//        zosmfResponse = new ZosmfResponseImpl(URL, PATH);
//        zosmfResponseSpy = PowerMockito.spy(zosmfResponse);
//    }
//    
//    @Test
//    public void testGetJsonContent() throws ZosmfException {
//        Whitebox.setInternalState(zosmfResponseSpy, "content", JSONOBJECT_CONTENT_STRING);
//        Assert.assertTrue("getJsonContent() should return the expected value", JSONOBJECT_CONTENT.equals(zosmfResponseSpy.getJsonContent()));
//        
//        Whitebox.setInternalState(zosmfResponseSpy, "content", JSONOBJECT_CONTENT_STRING.getBytes());
//        Assert.assertTrue("getJsonContent() should return the expected value", JSONOBJECT_CONTENT.equals(zosmfResponseSpy.getJsonContent()));
//        
//        Whitebox.setInternalState(zosmfResponseSpy, "content", new ByteArrayInputStream(JSONOBJECT_CONTENT_STRING.getBytes()));
//        Assert.assertTrue("getJsonContent() should return the expected value", JSONOBJECT_CONTENT.equals(zosmfResponseSpy.getJsonContent()));
//        
//        Whitebox.setInternalState(zosmfResponseSpy, "content", JSONOBJECT_CONTENT);
//        Assert.assertTrue("getJsonContent() should return the expected value", JSONOBJECT_CONTENT.equals(zosmfResponseSpy.getJsonContent()));
//        
//        Whitebox.setInternalState(zosmfResponseSpy, "content", new Integer(0));
//        String expectedMessage = "Content not a JsonObject - " + Integer.class.getName();
//        ZosmfException expectedException = Assert.assertThrows("expected exception should be thrown", ZosmfException.class, ()->{
//        	zosmfResponseSpy.getJsonContent();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetJsonArrayContent() throws ZosmfException {
//        Whitebox.setInternalState(zosmfResponseSpy, "content", JSONARRAY_CONTENT_STRING);
//        Assert.assertTrue("getJsonArrayContent() should return the expected value", JSONARRAY_CONTENT.equals(zosmfResponseSpy.getJsonArrayContent()));
//        
//        Whitebox.setInternalState(zosmfResponseSpy, "content", JSONARRAY_CONTENT_STRING.getBytes());
//        Assert.assertTrue("getJsonArrayContent() should return the expected value", JSONARRAY_CONTENT.equals(zosmfResponseSpy.getJsonArrayContent()));
//        
//        Whitebox.setInternalState(zosmfResponseSpy, "content", new ByteArrayInputStream(JSONARRAY_CONTENT_STRING.getBytes()));
//        Assert.assertTrue("getJsonArrayContent() should return the expected value", JSONARRAY_CONTENT.equals(zosmfResponseSpy.getJsonArrayContent()));
//        
//        Whitebox.setInternalState(zosmfResponseSpy, "content", new Integer(0));
//        String expectedMessage = "Content not a JsonArray Object - " + Integer.class.getName();
//        ZosmfException expectedException = Assert.assertThrows("expected exception should be thrown", ZosmfException.class, ()->{
//        	zosmfResponseSpy.getJsonArrayContent();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testTextContent() throws ZosmfException {
//        Whitebox.setInternalState(zosmfResponseSpy, "content", CONTENT_STRING);
//        Assert.assertTrue("getTextContent() should return the expected value", CONTENT_STRING.equals(zosmfResponseSpy.getTextContent()));
//        
//        Whitebox.setInternalState(zosmfResponseSpy, "content", new Integer(0));
//        String expectedMessage = "Content not a String Object - " + Integer.class.getName();
//        ZosmfException expectedException = Assert.assertThrows("expected exception should be thrown", ZosmfException.class, ()->{
//        	zosmfResponseSpy.getTextContent();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testSetHttpClientresponseHttpClientResponse() throws ZosmfException {
//        Mockito.when(httpClientResponseStringMock.getContent()).thenReturn(CONTENT_STRING);
//        Mockito.when(httpClientResponseStringMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(httpClientResponseStringMock.getStatusLine()).thenReturn(STATUS_LINE);
//        
//        zosmfResponseSpy.setHttpClientresponse(httpClientResponseStringMock);
//
//        Assert.assertEquals("getContent() should return the expected value", CONTENT_STRING, zosmfResponseSpy.getContent());
//        Assert.assertEquals("getStatusCode() should return the expected value", HttpStatus.SC_OK, zosmfResponseSpy.getStatusCode());
//        Assert.assertEquals("getStatusLine() should return the expected value", STATUS_LINE, zosmfResponseSpy.getStatusLine());
//    }
//    
//    @Test
//    public void testSetHttpClientresponseCloseableHttpResponse() throws UnsupportedOperationException, IOException, ZosmfException {
//    	Mockito.when(closeableHttpResponseMock.getEntity()).thenReturn(httpEntity);
//    	Mockito.when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(CONTENT_STRING.getBytes()));
//    	Mockito.when(closeableHttpResponseMock.getStatusLine()).thenReturn(statusLineMock);
//    	Mockito.when(statusLineMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//    	Mockito.when(statusLineMock.getReasonPhrase()).thenReturn(STATUS_LINE);
//    	
//    	zosmfResponseSpy.setHttpClientresponse(closeableHttpResponseMock);
//    	Assert.assertTrue("getContent() should return the expected value", CONTENT_STRING.equals(IOUtils.toString((InputStream) zosmfResponseSpy.getContent(), StandardCharsets.UTF_8)));
//    	Assert.assertEquals("getStatusCode() should return the expected value", HttpStatus.SC_OK, zosmfResponseSpy.getStatusCode());
//    	Assert.assertEquals("getStatusLine() should return the expected value", STATUS_LINE, zosmfResponseSpy.getStatusLine());
//    	
//    	Mockito.when(httpEntity.getContent()).thenThrow(new IOException());
//    	String expectedMessage = "Could not retrieve response";
//    	ZosmfException expectedException = Assert.assertThrows("expected exception should be thrown", ZosmfException.class, ()->{
//    		zosmfResponseSpy.setHttpClientresponse(closeableHttpResponseMock);
//    	});
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetRequestUrl() throws ZosmfException, MalformedURLException {
//        Assert.assertTrue("getRequestUrl() should return the expected value", new URL(URL + PATH).equals(zosmfResponseSpy.getRequestUrl()));
//    }
}
