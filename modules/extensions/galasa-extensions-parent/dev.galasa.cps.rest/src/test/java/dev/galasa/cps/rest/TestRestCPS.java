/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cps.rest;

import org.apache.http.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

import dev.galasa.cps.rest.mocks.MockJwtProvider;
import dev.galasa.extensions.common.mocks.*;
import dev.galasa.framework.api.beans.*;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;

import dev.galasa.framework.spi.utils.GalasaGson;

import java.net.URI;
import java.util.*;

public class TestRestCPS {

    private static final String JWT1 = "a fake jwt";

    @Test
    public void testCanCreateARestCPS() throws Exception {
        JwtProvider jwtProvider = new MockJwtProvider(JWT1);
        MockLogFactory logFactory = new MockLogFactory();
        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        CloseableHttpClient mockCloseableHttpClient = new MockCloseableHttpClient(interactions);
        MockHttpClientFactory mockClientFactory = new MockHttpClientFactory(mockCloseableHttpClient);
        URI ecosystemUrl = new URI(RestCPS.URL_SCHEMA_REST+"://my.host/api");
        new RestCPS( ecosystemUrl , mockClientFactory, jwtProvider,logFactory);
    }

    @Test
    public void testCPSInitInvalidCPSUrlNotStartingWithGalasaRestSchemeReturnsError() throws Exception {
        JwtProvider jwtProvider = new MockJwtProvider(JWT1);
        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        CloseableHttpClient mockCloseableHttpClient = new MockCloseableHttpClient(interactions);
        MockHttpClientFactory mockClientFactory = new MockHttpClientFactory(mockCloseableHttpClient);
        URI ecosystemUrl = new URI("an:invalid:url/on/purpose.");
        MockLogFactory logFactory = new MockLogFactory();
        ConfigurationPropertyStoreException ex = catchThrowableOfType( 
            ()-> new RestCPS( ecosystemUrl , mockClientFactory, jwtProvider, logFactory),
            ConfigurationPropertyStoreException.class
        );

        assertThat(ex).isNotNull();
        assertThat(ex).hasMessageContaining("GAL7000E:","an:invalid:url/on/purpose.","is invalid.");
    }


    @Test
    public void testCanGetACpsProperty() throws Exception {
        String fullyQualifiedPropertyName = "myNamespace.myPrefix.myMiddle.mySuffix";
        URI ecosystemUrl = new URI(RestCPS.URL_SCHEMA_REST+"://my.host/api");

        JwtProvider jwtProvider = new MockJwtProvider(JWT1);

        GetNamedCPSPropertiesInteraction getCPSPropertiesInteraction = new GetNamedCPSPropertiesInteraction( 
            JWT1,"https://my.host/api/cps/myNamespace/properties?prefix=myPrefix.myMiddle.mySuffix", "myNamespace", 
            "myPrefix.myMiddle.mySuffix", "abcde",
            "myPrefix.myMiddle.mySuffix.abcd", "dfgdfg"
            );

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(getCPSPropertiesInteraction);

        CloseableHttpClient mockCloseableHttpClient = new MockCloseableHttpClient(interactions);
        MockHttpClientFactory mockClientFactory = new MockHttpClientFactory(mockCloseableHttpClient);
        
        MockLogFactory logFactory = new MockLogFactory();
        RestCPS cps = new RestCPS( ecosystemUrl , mockClientFactory, jwtProvider, logFactory);
        String propGotBack = cps.getProperty(fullyQualifiedPropertyName);
        assertThat(propGotBack).isNotBlank();
    }

    @Test
    public void testAttemptsToGetFrameworkCredsStorePropertyGetsRedacted() throws Exception {
        checkRedactedPropertyReturnsNull("framework.credentials.store");
    }

    @Test
    public void testAttemptsToGetFrameworkDSSStorePropertyGetsRedacted() throws Exception {
        checkRedactedPropertyReturnsNull("framework.dynamicstatus.store");
    }

    @Test
    public void testAttemptsToGetASecurePropertyGetsRedacted() throws Exception {
        checkRedactedPropertyReturnsNull("secure.any.key");
    }

    private void checkRedactedPropertyReturnsNull(String fullyQualifiedPropertyName) throws Exception {
        // Given...

        // This is a special property we never want the local tests to use.

        URI ecosystemUrl = new URI(RestCPS.URL_SCHEMA_REST+"://my.host/api");

        JwtProvider jwtProvider = new MockJwtProvider(JWT1);

        // No interactions. We don't expect any HTTP traffic for this reserved property name.
        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();

        CloseableHttpClient mockCloseableHttpClient = new MockCloseableHttpClient(interactions);
        MockHttpClientFactory mockClientFactory = new MockHttpClientFactory(mockCloseableHttpClient);
        
        MockLogFactory logFactory = new MockLogFactory();
        RestCPS cps = new RestCPS( ecosystemUrl , mockClientFactory, jwtProvider, logFactory);

        // When...
        String value = cps.getProperty(fullyQualifiedPropertyName);

        // Then...
        assertThat(value).as("Expected value to be null, as the property being got is reserved.").isNull();
    }

    // A leading test which tests that the CPS can reach the actual server implementation.
    // Used to get test data, and check that the remote side does actually do what we think it does.
    // @Test
    // public void testCPSCanGetValueFromRealServer() throws Exception {
    //     String jwt = ...";
    //     String ecosystemApiServerUrl = "galasacps://galasa-ecosystem1.galasa.dev/api";
    //     String propertyName = "mcobbett.test.prop1";
    //     String propertyValueExpected = "hello";

    //     JwtProvider jwtProvider = new MockJwtProvider(jwt);
    //     URI ecosystemUrl = new URI(ecosystemApiServerUrl);
    //     RestCPS cps = new RestCPS( ecosystemUrl , new HttpClientFactoryImpl(), jwtProvider);
        
    //     String value = cps.getProperty(propertyName);

    //     assertThat(value).isEqualTo(propertyValueExpected);
    // }

    public static class GetNamedCPSPropertiesInteraction extends AuthenticatedHttpInteraction {

        String propNamespace;
        String propName1;
        String propValue1;
        String propName2;
        String propValue2;

        public GetNamedCPSPropertiesInteraction(
            String expectedJwt,
            String expectedUri, String propNamespace, 
            String propName1, String propValue1, 
            String propName2, String propValue2 
        ) {
            super(expectedUri,expectedJwt);
            this.propName1 = propName1;
            this.propValue1 = propValue1;
            this.propName2 = propName2;
            this.propValue2 = propValue2;
            this.propNamespace = propNamespace;
        }

        @Override
        public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
            super.validateRequest(host,request);
            assertThat(request.getRequestLine().getMethod()).isEqualTo("GET");
            Header[] headers = request.getHeaders("Authorization");
            assertThat(headers).as("There is no bearer token header being passed to the server side.").hasSize(1);
            assertThat(headers[0].getValue()).as("The bearer token is not being passed correctly to the REST API.").isEqualTo("Bearer "+JWT1);
        }

        @Override
        public void validateRequestContentType(HttpRequest request) {
            // We don't expect a Content-type header as there is no payload sent to the server
        }

        @Override
        public MockCloseableHttpResponse getResponse() {

            GalasaProperty[] props = new GalasaProperty[2];
            {
                GalasaPropertyData data = new GalasaPropertyData(propValue1);
                GalasaPropertyMetadata metadata = new GalasaPropertyMetadata(propNamespace,propName1);
                GalasaProperty prop1 = new GalasaProperty(metadata,data);
                props[0]=prop1;
            }
            {
                GalasaPropertyData data = new GalasaPropertyData(propValue2);
                GalasaPropertyMetadata metadata = new GalasaPropertyMetadata(propNamespace,propName2);
                GalasaProperty prop2= new GalasaProperty(metadata,data);
                props[1]=prop2;
            }

            GalasaGson gson = new GalasaGson();
            String updateMessagePayload = gson.toJson(props);

            HttpEntity entity = new MockHttpEntity(updateMessagePayload); 

            MockCloseableHttpResponse response = new MockCloseableHttpResponse();

            MockStatusLine statusLine = new MockStatusLine();
            statusLine.setStatusCode(HttpStatus.SC_OK);
            response.setStatusLine(statusLine);
            response.setEntity(entity);

            return response;
        }
    }

    /**
     * An HTTP interaction which expects the CPS Rest adapter to query all the properties
     * in the "myNamespace" namespace with a "myPrefix" prefix.
     * This interaction will return two property definitions.
     * Then the test checks that the property values are what the mock server returned.
     */
    @Test
    public void testCanGetPrefixedProperties() throws Exception {
        String fullyQualifiedPropertyName1 = "myNamespace.myPrefix.myMiddle1.mySuffix";
        String fullyQualifiedPropertyName2 = "myNamespace.myPrefix.myMiddle2.mySuffix";

        URI ecosystemUrl = new URI(RestCPS.URL_SCHEMA_REST+"://my.host/api");

        JwtProvider jwtProvider = new MockJwtProvider(JWT1);

        GetNamedCPSPropertiesInteraction getCPSPropertiesInteraction = new GetNamedCPSPropertiesInteraction( 
            JWT1,"https://my.host/api/cps/myNamespace/properties?prefix=myPrefix", "myNamespace", 
            "myPrefix.myMiddle1.mySuffix", "abcde",
            "myPrefix.myMiddle2.mySuffix", "fghij"
            );

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(getCPSPropertiesInteraction);

        CloseableHttpClient mockCloseableHttpClient = new MockCloseableHttpClient(interactions);
        MockHttpClientFactory mockClientFactory = new MockHttpClientFactory(mockCloseableHttpClient);
        
        MockLogFactory logFactory = new MockLogFactory();
        RestCPS cps = new RestCPS( ecosystemUrl , mockClientFactory, jwtProvider, logFactory);
        Map<String,String> properties = cps.getPrefixedProperties("myNamespace.myPrefix");
        assertThat(properties).isNotNull().hasSize(2);
        assertThat(properties).containsKeys(fullyQualifiedPropertyName1);
        assertThat(properties).containsKeys(fullyQualifiedPropertyName2);
        assertThat(properties.get(fullyQualifiedPropertyName1)).isEqualTo("abcde");
        assertThat(properties.get(fullyQualifiedPropertyName2)).isEqualTo("fghij");
    }

    // A leading test which tests that the CPS can reach the actual server implementation.
    // Used to get test data, and check that the remote side does actually do what we think it does.
    // @Test
    // public void testCPSCanGetValuesFromRealServer() throws Exception {
    //     String jwt = "...";
    //     String ecosystemApiServerUrl = "galasacps://galasa-ecosystem1.galasa.dev/api";
    //     String propertyName = "mcobbett.test.prop1";
    //     String namespaceAndPrefix = "mcobbett.test";
    //     String propertyValueExpected = "hello";

    //     JwtProvider jwtProvider = new MockJwtProvider(jwt);
    //     URI ecosystemUrl = new URI(ecosystemApiServerUrl);
    //     RestCPS cps = new RestCPS( ecosystemUrl , new HttpClientFactoryImpl(), jwtProvider);
        
    //     Map<String,String> properties = cps.getPrefixedProperties(namespaceAndPrefix);

    //     assertThat(properties.get(propertyName)).isEqualTo(propertyValueExpected);
    // }

    /**
     * An HTTP interaction which expects the CPS Rest adapter to query all the properties
     * in the "myNamespace" namespace.
     * This interaction will return two property definitions.
     * Then the test checks that the property values are what the mock server returned.
     */
    @Test
    public void testCanGetAllPropertiesInANamespace() throws Exception {
        String fullyQualifiedPropertyName1 = "myNamespace.myPrefix.myMiddle1.mySuffix";
        String fullyQualifiedPropertyName2 = "myNamespace.myPrefix.myMiddle2.mySuffix";

        URI ecosystemUrl = new URI(RestCPS.URL_SCHEMA_REST+"://my.host/api");

        JwtProvider jwtProvider = new MockJwtProvider(JWT1);

        GetNamedCPSPropertiesInteraction getCPSPropertiesInteraction = new GetNamedCPSPropertiesInteraction( 
            JWT1,"https://my.host/api/cps/myNamespace/properties", "myNamespace", 
            "myPrefix.myMiddle1.mySuffix", "abcde",
            "myPrefix.myMiddle2.mySuffix", "fghij"
            );

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(getCPSPropertiesInteraction);

        CloseableHttpClient mockCloseableHttpClient = new MockCloseableHttpClient(interactions);
        MockHttpClientFactory mockClientFactory = new MockHttpClientFactory(mockCloseableHttpClient);
        
        MockLogFactory logFactory = new MockLogFactory();
        RestCPS cps = new RestCPS( ecosystemUrl , mockClientFactory, jwtProvider, logFactory);
        Map<String,String> properties = cps.getPropertiesFromNamespace("myNamespace");
        assertThat(properties).isNotNull().hasSize(2);
        assertThat(properties).containsKeys(fullyQualifiedPropertyName1);
        assertThat(properties).containsKeys(fullyQualifiedPropertyName2);
        assertThat(properties.get(fullyQualifiedPropertyName1)).isEqualTo("abcde");
        assertThat(properties.get(fullyQualifiedPropertyName2)).isEqualTo("fghij");
    }

    // A leading test which tests that the CPS can reach the actual server implementation.
    // Used to get test data, and check that the remote side does actually do what we think it does.
    // @Test
    // public void testCPSCanGetValuesFromRealServer() throws Exception {
    //     String jwt = "eyJh...hbPhCB3Q";
    //     String ecosystemApiServerUrl = "galasacps://galasa-ecosystem1.galasa.dev/api";
    //     String propertyName = "mcobbett.test.prop1";
    //     String namespace = "mcobbett";
    //     String propertyValueExpected = "hello";

    //     JwtProvider jwtProvider = new MockJwtProvider(jwt);
    //     URI ecosystemUrl = new URI(ecosystemApiServerUrl);
    //     RestCPS cps = new RestCPS( ecosystemUrl , new HttpClientFactoryImpl(), jwtProvider);
        
    //     Map<String,String> properties = cps.getPropertiesFromNamespace(namespace);

    //     assertThat(properties.get(propertyName)).isEqualTo(propertyValueExpected);
    // }



    @Test
    public void testCanGetAllNamespaces() throws Exception {
        String myNamespace = "myNamespace";
        String[] namespaces = new String[1];
        namespaces[0] = myNamespace;

        URI ecosystemUrl = new URI(RestCPS.URL_SCHEMA_REST+"://my.host/api");

        JwtProvider jwtProvider = new MockJwtProvider(JWT1);

        class GetNamespacesInteraction extends AuthenticatedHttpInteraction {

            String[] namespacesToReturn;

            public GetNamespacesInteraction(String expectedJwt, String expectedUri, String[] namespacesToReturn) {
                super(expectedUri,expectedJwt);
                this.namespacesToReturn = namespacesToReturn ;
            }
  
            @Override
            public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
                super.validateRequest(host,request);
                assertThat(request.getRequestLine().getMethod()).isEqualTo("GET");
            }
    

            @Override
            public void validateRequestContentType(HttpRequest request) {
                // We don't expect a Content-type header as there is no payload sent to the server
            }
    
            @Override
            public MockCloseableHttpResponse getResponse() {
    
                GalasaGson gson = new GalasaGson();
                String msgPayload = gson.toJson(this.namespacesToReturn);
    
                HttpEntity entity = new MockHttpEntity(msgPayload); 
    
                MockCloseableHttpResponse response = new MockCloseableHttpResponse();
    
                MockStatusLine statusLine = new MockStatusLine();
                statusLine.setStatusCode(HttpStatus.SC_OK);
                response.setStatusLine(statusLine);
                response.setEntity(entity);
    
                return response;
            }
        }

        GetNamespacesInteraction getNamespacesInteraction = new GetNamespacesInteraction( JWT1,
            "https://my.host/api/cps/namespace/",
            namespaces);

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(getNamespacesInteraction);

        CloseableHttpClient mockCloseableHttpClient = new MockCloseableHttpClient(interactions);
        MockHttpClientFactory mockClientFactory = new MockHttpClientFactory(mockCloseableHttpClient);
        
        MockLogFactory logFactory = new MockLogFactory();
        RestCPS cps = new RestCPS( ecosystemUrl , mockClientFactory, jwtProvider, logFactory);
        List<String> namespacesGotBack = cps.getNamespaces();
        assertThat(namespacesGotBack).isNotNull().contains(myNamespace);
    }


    // A leading test which tests that the CPS can reach the actual server implementation.
    // Used to get test data, and check that the remote side does actually do what we think it does.
    // @Test
    // public void testCPSCanGetNamespacesFromRealServer() throws Exception {
    //     String jwt = "eyJ...sjhdf"
    //     String ecosystemApiServerUrl = "galasacps://galasa-ecosystem1.galasa.dev/api";
    //     String namespaceExpected = "mcobbett";

    //     JwtProvider jwtProvider = new MockJwtProvider(jwt);
    //     URI ecosystemUrl = new URI(ecosystemApiServerUrl);
    //     RestCPS cps = new RestCPS( ecosystemUrl , new HttpClientFactoryImpl(), jwtProvider);
        
    //     List<String> namespaces = cps.getNamespaces();

    //     assertThat(namespaces).contains(namespaceExpected).contains("framework");
    // }
}
