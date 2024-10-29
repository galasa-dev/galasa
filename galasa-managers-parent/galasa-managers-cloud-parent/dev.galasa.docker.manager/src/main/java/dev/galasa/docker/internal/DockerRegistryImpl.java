/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.internal;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Base64;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsToken;
import dev.galasa.ICredentialsUsername;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.ICredentialsUsernameToken;
import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.internal.properties.DockerImagePrefix;
import dev.galasa.docker.internal.properties.DockerRegistryCredentials;
import dev.galasa.docker.internal.properties.DockerRegistryURL;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;

/**
 * Docker RegistryImpl. Controls the location of where docker images can be
 * pulled from
 * 
 *   
 */
public class DockerRegistryImpl {
	private IFramework 							framework;
	private DockerManagerImpl 					dockerManager;
	private URL 								registryUrl;
	private String 								registryId;
	private String 								prefix;

	private ICredentialsService 				credService;

	private String 								registryRealmType;
	private URL 								registryRealmURL;

	private String								 authToken;

	private IHttpClient 						client;
	private IHttpClient 						realmClient;

	private static final Log logger 			= LogFactory.getLog(DockerRegistryImpl.class);

	/**
	 * Sets up the registry that the manager can use to pull images from.
	 * 
	 * @param framework
	 * @param dockerManager
	 * @param registryId
	 * @throws DockerManagerException
	 */
	public DockerRegistryImpl(IFramework framework, DockerManagerImpl dockerManager, String registryId)
			throws DockerManagerException {
		this.framework = framework;
		this.dockerManager = dockerManager;
		this.registryId = registryId;
		this.registryUrl = DockerRegistryURL.get(this);
		this.prefix = DockerImagePrefix.get(this);
		
		this.client = dockerManager.getHttpManager().newHttpClient();
		this.realmClient = dockerManager.getHttpManager().newHttpClient();

		try {
			this.client.setURI(this.registryUrl.toURI());
			this.credService = framework.getCredentialsService();
		} catch (URISyntaxException e) {
			logger.error("Registry URL is incompatible", e);
			throw new DockerManagerException("Could not parse Docker registry URL.", e);
		} catch (CredentialsException e) {
			logger.error("Could not access credential store from framework.", e);
			throw new DockerManagerException("Could not access credential store from framework.", e);
		}

	}

	/**
	 * Checks the registry for and image.
	 * 
	 * @param image
	 * @return true/false
	 */
	public boolean doYouHave(DockerImageImpl image) {
		String resp = null;
		String path = "";
		try {
			registryAuthenticate(image);

			path = "/v2/" + getPrefix() + image.getImageName() + "/manifests/" + image.getTag();
			logger.trace("Checking if image is available at location: " + path);

			HttpClientResponse<JsonObject> response = client.getJson(path);
			if (response.getStatusCode() == (HttpStatus.SC_OK)) {
				return true;
			}
			return false;
		} catch (HttpClientException e) {
			return false;
		} catch (IllegalStateException e) {
			return false;
		} catch (DockerManagerException e) {
			logger.trace("Failed to access registry", e);
			return false;
		} catch (ClassCastException e) {
			logger.trace("Invalid JSON returned from Docker Registry\n" + resp, e);
			return false;
		}
	}

	/**
	 * Registry authentication
	 * 
	 * @param image
	 * @throws DockerManagerException
	 */
	public void registryAuthenticate(DockerImageImpl image) throws DockerManagerException {
		if (!retrieveRealm(image)) {
			logger.info("No authentication required");
			return;
		}

		if ("Bearer realm".equalsIgnoreCase(this.registryRealmType)) {
			this.authToken = retrieveBearerToken();
			return;
		}

		if ("Basic realm".equalsIgnoreCase(this.registryRealmType)) {
			this.authToken = retrieveBasicToken();
			return;
		}
	}

	/**
	 * Attempts to gain a bearer token from realm, if unauthorized tries basic credentials login 
	 * retreive token
	 * 
	 * @return String token
	 * @throws DockerManagerException
	 */
	public String retrieveBearerToken() throws DockerManagerException {
		try {
			this.realmClient.setURI(this.registryRealmURL.toURI());
			HttpClientResponse<JsonObject> response = this.realmClient.getJson("");
			if (response.getStatusCode() == (HttpStatus.SC_OK)) {
				JsonObject json = response.getContent();
				String token = json.get("token").getAsString();
				this.client.addCommonHeader("Authorization", "Bearer "+token);
				return token;
			}
			if (response.getStatusCode() == (HttpStatus.SC_UNAUTHORIZED)) {
				Map<String, String> headers = response.getheaders();
				for (String key : headers.keySet()) {
					if (key.equalsIgnoreCase("WWW-Authenticate")) {
						String authType = parseAuthRealmType(headers.get(key));
						if ("Basic realm".equals(authType)) {
							return retrieveBasicToken();
						} else {
							throw new DockerManagerException("Dont know how to authenticate to registry: " + this.registryUrl);
						}
					}
				}
			}
			throw new DockerManagerException("Failed to retrieve token from:" + this.registryRealmURL);
		} catch (HttpClientException | URISyntaxException e) {
			throw new DockerManagerException("Failed to connect to: " + this.registryRealmURL);
		}
	}

	/**
	 * Uses basic crednetials to gain a basic auth token.
	 * 
	 * @return String token
	 * @throws DockerManagerException
	 */
	public String retrieveBasicToken() throws DockerManagerException {
		try {
			
			ICredentials creds = getCreds();
			if (creds instanceof ICredentialsUsernamePassword) {

				String user = ((ICredentialsUsername) creds).getUsername();
				String password = ((ICredentialsUsernamePassword) creds).getPassword();

				this.client.setAuthorisation(user, password);
				this.client.build();

				return generateDockerRegistryAuthStructure(user, password);
			}

			if (creds instanceof ICredentialsUsernameToken) {
				throw new DockerManagerException("Username tokens are not yet supported");
			}

			if (creds instanceof ICredentialsUsername) {
				throw new DockerManagerException("Username credentials are not yet supported");
			}

			if (creds instanceof ICredentialsToken) {
				throw new DockerManagerException("Tokens are not yet supported");
			}
			throw new DockerManagerException("Couldnt generate token");
		} catch (DockerManagerException | CredentialsException e) {
			throw new DockerManagerException("Couldnt locate credentials to generate token", e);
		}
	}


	/**
	 * Retrieves credentials from the credential store with a given key
	 * 
	 * @return ICredentials creds
	 * @throws ConfigurationPropertyStoreException
	 * @throws CredentialsException
	 */
	public ICredentials getCreds() throws DockerManagerException, CredentialsException {
		String credKey = DockerRegistryCredentials.get(this);
		return credService.getCredentials(credKey);
	}

	/**
	 * Returns boolean for if a auth realm can be found and identified.
	 * 
	 * @param image
	 * @return
	 * @throws DockerManagerException
	 */
	boolean retrieveRealm(DockerImageImpl image) throws DockerManagerException {
		String path = "/v2/" + getPrefix() + image.getImageName() + "/manifests/" + image.getTag();

		try {
			HttpClientResponse<JsonObject> response = this.client.getJson(path);
			if (response.getStatusCode() == (HttpStatus.SC_OK)) {
				return false;
			}
			if (response.getStatusCode() == (HttpStatus.SC_UNAUTHORIZED)) {
				Map<String, String> headers = response.getheaders();
				for (String key : headers.keySet()) {
					if (key.equalsIgnoreCase("WWW-Authenticate")) {
						this.registryRealmType = parseAuthRealmType(headers.get(key));
						if (registryRealmType.equals("")) {
							throw new DockerManagerException("Registry location '" + path + "' not found. Check to ensure image registry path is correct.");
						}
						if (!"BASIC realm".equalsIgnoreCase(this.registryRealmType)){
							this.registryRealmURL = parseAuthRealmURL(headers.get(key));
						}
						return true;
					}
				}
			}
			throw new DockerManagerException("Failed to authenticate, and authentication is required.");
		} catch (HttpClientException | MalformedURLException | JsonSyntaxException e) {
			throw new DockerManagerException("Failed to connect to registry.", e);
		}
	}

	/**
	 * Parses the realm type from the WWW-Authenticate header
	 * 
	 * @param header
	 * @return String realmType
	 */
	private String parseAuthRealmType(String header) {
		return header.split("=")[0];
	}

	/**
	 * Extracts the realm URL and builds the  full URL to authenticate with the correct scope
	 * 
	 * @param header
	 * @return URL authURL
	 * @throws MalformedURLException
	 */
	private URL parseAuthRealmURL(String header) throws MalformedURLException {
		String manString = header.replaceAll("\"", "");
		String[] components = (manString.substring(manString.indexOf("=")+1).split(","));
		
		StringBuilder builder = new StringBuilder();
		builder.append(components[0]);
		builder.append("?");

		for(int i=1;i<components.length;i++) {
			builder.append(components[i]);
			if (i!=components.length-1) {
				builder.append("&");
			}
		}

		return new URL(builder.toString());
	}

	/**
	 * Encodes credentials into a Base64 auth structure.
	 * 
	 * @param user
	 * @param password
	 * @return
	 */
	private String generateDockerRegistryAuthStructure(String user, String password) {
		JsonObject creds = new JsonObject();
		creds.addProperty("username", user);
		creds.addProperty("password", password);

		return Base64.getEncoder().encodeToString(creds.toString().getBytes());
	}
    
	/**
	 * Returns the host of the registry
	 * @return String
	 */
    public String getHost() {
		if (registryUrl.getPort() != -1) {
			return this.registryUrl.getHost() + ":" + this.registryUrl.getPort();
		} else {
			return this.registryUrl.getHost();
        }
	}   
    
    /**
     * Returns the prefix of the registry
     * @return String
     */
    public String getPrefix() {
    	return this.prefix;
    }
	
	/**
	 * Returns the auth token
	 * 
	 * @return String
	 */
	public String getAuthToken() {
		return this.authToken;
	}

	/**
	 * Returns id of the registry for properties
	 * @return
	 */
	public String getId() {
		return this.registryId;
	}
}