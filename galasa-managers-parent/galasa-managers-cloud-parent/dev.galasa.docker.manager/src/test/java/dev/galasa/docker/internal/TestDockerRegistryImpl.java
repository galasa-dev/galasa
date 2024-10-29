/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.internal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.gson.JsonObject;

import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.DockerProvisionException;
import dev.galasa.docker.internal.properties.DockerPropertiesSingleton;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.spi.IHttpManagerSpi;

public class TestDockerRegistryImpl {
    @Mock
    private IFramework frameworkMock;
    @Mock
    private DockerManagerImpl dockerManagerMock;
    @Mock
    private IHttpManagerSpi httpManagerMock;
    @Mock
    private IHttpClient clientMock;
    @Mock
    private HttpClientResponse<JsonObject> responseMock;
    @Mock
    private HttpClientResponse<JsonObject> bearerResponseMock; 
    @Mock
    private ICredentialsUsernamePassword credentialsMock;
    @Mock
    private ICredentialsService credentialServiceMock;
    
    @BeforeClass
    public static void initialiseCPS() throws DockerManagerException {
    	new DockerPropertiesSingleton().activate();
    	DockerPropertiesSingleton.setCps(new OurCPS());
    }
    
    @Before
    public void init() throws DynamicStatusStoreException, DockerManagerException, FileNotFoundException, DockerProvisionException {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void retrieveBearerTokenAuthorised() throws DockerManagerException, MalformedURLException, CredentialsException, HttpClientException, URISyntaxException {
    	// Creating object
    	DockerRegistryImpl dockerRegistry = crateRegistryImplObject();
    	
    	// Setting registryRealmURL for our test object
    	retrieveRealm(dockerRegistry);
    	
    	// Attempting to use retrieve bearer token method an authorised response 
    	when(clientMock.getJson("")).thenReturn(bearerResponseMock);
    	when(bearerResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
    	JsonObject jsonAuthorisation = new JsonObject();
    	jsonAuthorisation.addProperty("token", "tokenValue");
    	when(bearerResponseMock.getContent()).thenReturn(jsonAuthorisation);
    	String actualToken = dockerRegistry.retrieveBearerToken();    	
    	
    	// Testing results of attempt
    	assertThat(actualToken).as("Checking barer token value").isEqualTo("tokenValue");
    	verify(clientMock, times(1)).addCommonHeader("Authorization", "Bearer tokenValue");
    }
    
    @Test
    public void retrieveBearerTokenUnauthorised() throws DockerManagerException, MalformedURLException, CredentialsException, HttpClientException, URISyntaxException, NoSuchFieldException, SecurityException {
    	// Creating object
    	DockerRegistryImpl dockerRegistry = crateRegistryImplObject();
    	
    	// Setting registryRealmURL
    	retrieveRealm(dockerRegistry);
    	
    	// Attempting to use retrieve bearer token method with an unauthorised response 
    	when(clientMock.getJson("")).thenReturn(bearerResponseMock);
    	when(bearerResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_UNAUTHORIZED);
    	Map<String, String> headers = new HashMap<String, String>();
    	headers.put("WWW-Authenticate", "Basic realm");
    	when(bearerResponseMock.getheaders()).thenReturn(headers);
    	
    	// Mocking the user credentials returned from the credential service
    	when(credentialServiceMock.getCredentials(Mockito.anyString())).thenReturn(credentialsMock);
    	when(credentialsMock.getUsername()).thenReturn("testUsername");
    	when(credentialsMock.getPassword()).thenReturn("testPassword");
    	String user = "testUsername";
    	String password = "testPassword"; //unit test mock password //pragma: allowlist secret
    	when(clientMock.setAuthorisation(user, password)).thenReturn(clientMock);
    	when(clientMock.build()).thenReturn(clientMock);
    	// Base64 encoding credentials to replicate private encoding method (generateDockerRegistryAuthStructure)
    	JsonObject creds = new JsonObject();
		creds.addProperty("username", user);
		creds.addProperty("password", password);
		String token = Base64.getEncoder().encodeToString(creds.toString().getBytes());
    	String actualToken = dockerRegistry.retrieveBearerToken();    	
    	
    	// Testing results of attempt
    	assertThat(actualToken).as("Checking barer token value").isEqualTo(token);
    }
    
    private void retrieveRealm(DockerRegistryImpl dockerRegistry) throws HttpClientException, DockerManagerException {
    	// Create Docker image object used for realm retrieval 
    	DockerImageImpl dockerImageImpl = createImageImplObject();
    	// Setting registryRealmURL for our test registry
    	String path = "/v2/bob/manifests/latest";
    	when(clientMock.getJson(path)).thenReturn(responseMock);
    	when(responseMock.getStatusCode()).thenReturn(HttpStatus.SC_UNAUTHORIZED);
    	Map<String, String> headers = new HashMap<String, String>();
    	headers.put("WWW-Authenticate", "Bearer realm=\"http://x.x.x.x/service/token\"");
    	when(responseMock.getheaders()).thenReturn(headers);
    	dockerRegistry.retrieveRealm(dockerImageImpl);
    }
    
    private DockerRegistryImpl crateRegistryImplObject() throws DockerManagerException, MalformedURLException, CredentialsException {
    	when(dockerManagerMock.getHttpManager()).thenReturn(httpManagerMock);
    	when(httpManagerMock.newHttpClient()).thenReturn(clientMock);
    	when(frameworkMock.getCredentialsService()).thenReturn(credentialServiceMock);
    	DockerRegistryImpl dockerRegistry = new DockerRegistryImpl(frameworkMock, dockerManagerMock, "DOCKERHUB");
    	return dockerRegistry;
    }
    
    private DockerImageImpl createImageImplObject() {
    	DockerImageImpl dockerImageImpl = new DockerImageImpl(null, dockerManagerMock, null, "bob:latest");
    	return dockerImageImpl;
    }
    
    public static class OurCPS implements IConfigurationPropertyStoreService {

		@Override
		public @Null String getProperty(@NotNull String prefix, @NotNull String suffix, String... infixes) throws ConfigurationPropertyStoreException {
			return null;
		}

		@Override
		public @NotNull Map<String, String> getPrefixedProperties(@NotNull String prefix) throws ConfigurationPropertyStoreException {
			return null;
		}

		@Override
		public void setProperty(@NotNull String name, @NotNull String value) throws ConfigurationPropertyStoreException {}

		@Override
		public void deleteProperty(@NotNull String name) throws ConfigurationPropertyStoreException {}

		@Override
		public Map<String, String> getAllProperties() {
			return null;
		}

		@Override
		public String[] reportPropertyVariants(@NotNull String prefix, @NotNull String suffix, String... infixes) {
			return null;
		}

		@Override
		public String reportPropertyVariantsString(@NotNull String prefix, @NotNull String suffix, String... infixes) {
			return null;
		}

		@Override
		public List<String> getCPSNamespaces() {
			return null;
		}
    	
    }
}
