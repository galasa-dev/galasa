/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ivts.http;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

import com.google.gson.JsonObject;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.http.HttpClient;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.StandAloneHttpClient;


@Test
public class HttpManagerIVT {

    @Logger
    public Log logger;

    @HttpClient
    public IHttpClient client;

    @Test
    public void checkNotNull(){
        assertThat(logger).isNotNull();
        assertThat(client).isNotNull();
    }
    
    @Test
    public void testStandalone() {
    	IHttpClient standaloneClient = StandAloneHttpClient.getHttpClient(10, logger);
    	assertThat(standaloneClient).isInstanceOf(IHttpClient.class);
    }
    
    @Test
    public void SSLContextTest() throws HttpClientException {
    	client.setTrustingSSLContext();
    	assertThat(client.getSSLContext()).isNotNull();
    }
    
    @Test
    public void getTests() throws Exception{
        client.setURI(new URI("https://httpbin.org"));
        String sResponse = client.getText("/get").getContent();
        assertThat(sResponse).startsWith("{");
        assertThat(sResponse).contains("\"url\": \"https://httpbin.org/get\"");

        JsonObject jResponse = client.getJson("/get").getContent();
        assertThat(jResponse.get("url").getAsString()).isEqualTo("https://httpbin.org/get");
    }

    @Test
    public void headerTest() throws Exception{
        String headerName = "Hobbit-Header";
        String headerValue = "Frodo";

        client.setURI(new URI("https://httpbin.org"));
        client.addCommonHeader(headerName, headerValue);

        HttpClientResponse<JsonObject> response = client.getJson("/get");
        
        Map<String, String> headers = response.getheaders();
        logger.info("Response headers: " + headers);
        assertThat(headers).isNotNull();
        assertThat(headers).containsKey("Content-Type");
        
        assertThat(response.getHeader("Content-Type")).isEqualTo("application/json");
        assertThat(response.getProtocolVersion()).contains("HTTP");
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getStatusMessage()).isEqualTo("OK");
        assertThat(response.getStatusLine()).contains("HTTP", "200 OK");
        
        JsonObject json = response.getContent();
        logger.info(json.toString());
        assertThat(json.toString()).contains(headerName);
        assertThat(json.toString()).contains(headerValue);
    }

    @Test
    public void authTest() throws Exception{
        String user = "hobbit";
        String pword = "passw0rd";
        String path = "/basic-auth/" + user + "/" + pword;

        URI httpbin = new URI("https://httpbin.org");
        
        client.setAuthorisation(user, pword);
        client.setAuthorisation(user, pword, httpbin);
        assertThat(client.getUsername()).isEqualTo(user);
        assertThat(client.getUsername(httpbin)).isEqualTo(user);
        
        HttpClientResponse<JsonObject> response = client.getJson(path);
        assertThat(response.getStatusCode()).isEqualTo(200);
        
        assertThat(response.getContent().get("authenticated").getAsBoolean()).isTrue();
        assertThat(response.getContent().get("user").getAsString()).isEqualTo(user);  
    }

    @Test
    public void postTests() throws Exception {
        String key = "Project";
        String value = "Galasa";
        client.setURI(new URI("https://httpbin.org"));
        JsonObject json = new JsonObject();
        json.addProperty(key,value);
        HttpClientResponse<JsonObject> jResponse = client.postJson("/post", json);
        assertThat(jResponse.getStatusCode()).isEqualTo(200);
        logger.info(jResponse.getContent().toString());
        assertThat(jResponse.getContent().get("data").getAsString()).isEqualTo(json.toString());

        HttpClientResponse<String> sResponse = client.postText("/post", json.toString());
        assertThat(sResponse.getStatusCode()).isEqualTo(200);
        logger.info(sResponse.getContent().toString());
        assertThat(sResponse.getContent()).contains(key);
        assertThat(sResponse.getContent()).contains(value);
    }
    
    @Test
    public void deleteTests() throws HttpClientException {
    	HttpClientResponse<String> textResponse = client.deleteText("/anything");
    	assertThat(textResponse.getContent()).contains("DELETE");
    	assertThat(textResponse.getStatusCode()).isEqualTo(200);

    	byte[] bytes = "bytes".getBytes();
    	HttpClientResponse<byte[]> binResponse = client.deleteBinary("/delete", bytes);
    	assertThat(binResponse.getStatusCode()).isEqualTo(200);
    }
    
    @Test
    public void putTests() throws HttpClientException {
    	HttpClientResponse<String> textResponse = client.putText("/anything", "");
    	assertThat(textResponse.getContent()).contains("PUT");
    	assertThat(textResponse.getStatusCode()).isEqualTo(200);

    	byte[] bytes = "bytes".getBytes();
    	HttpClientResponse<byte[]> binResponse = client.putBinary("/put", bytes);
    	assertThat(binResponse.getStatusCode()).isEqualTo(200);
    }
    
    @Test
    public void testBinary() throws HttpClientException {
    	byte[] bytes = "bytes".getBytes();
    	
    	HttpClientResponse<byte[]> response = client.getBinary("/bytes/8", bytes);
    	
    	assertThat(response.getHeader("Content-Length")).isEqualTo("8");
    	assertThat(response.getContent().length).isEqualTo(8);
    	assertThat(response.getHeader("Content-Type")).isEqualTo("application/octet-stream");
    }
    
    @Test
    public void downloadFileTest()
            throws Exception {
        boolean fileExists = false;
        File f = new File("/tmp/jenkins.hpi");

        client.setURI(new URI("https://resources.galasa.dev"));

        InputStream in = client.getFile("/jenkins.hpi").getEntity().getContent();
        OutputStream out = new FileOutputStream(f);

        int count;
        byte data[] = new byte[2048];
        while((count = in.read(data)) != -1) {
            out.write(data, 0, count);
        }
        out.flush();
        out.close();

        
        if (f.exists() && !f.isDirectory() && f.getTotalSpace()>0) {
            fileExists = true;
        }
        assertThat(fileExists).isTrue();

        f.delete();
    }
    
    @Test
    public void buildURITest() {
    	HttpClientException expected = null;
    	
    	try {
    		// dummy request
    		client.getText("http://httpbin.org/anything?should==fail");
    	} catch (HttpClientException e) {
    		logger.info("Caught expected exception: " + e.getMessage());
    		expected = e;
    	}
    	assertThat(expected).isNotNull();
    	
    	expected = null;
    	try {
    		// dummy request
    		client.getText("http://httpbin.org/anything?thisparam=ok&&oops=yes");
    	} catch (HttpClientException e) {
    		logger.info("Caught expected exception: " + e.getMessage());
    		expected = e;
    	}
    	assertThat(expected).isNotNull();
    }
    
}
