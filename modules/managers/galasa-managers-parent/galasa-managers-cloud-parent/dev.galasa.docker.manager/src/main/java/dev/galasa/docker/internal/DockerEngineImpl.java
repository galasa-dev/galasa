/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import javax.validation.constraints.NotNull;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import com.google.gson.JsonObject;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsKeyStore;
import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.DockerNotFoundException;
import dev.galasa.docker.DockerProvisionException;
import dev.galasa.docker.IDockerEngine;
import dev.galasa.docker.internal.properties.DockerDSEEngine;
import dev.galasa.docker.internal.properties.DockerEngine;
import dev.galasa.docker.internal.properties.DockerEngineCredentials;
import dev.galasa.docker.internal.properties.DockerEnginePort;
import dev.galasa.docker.internal.properties.DockerEngines;
import dev.galasa.docker.internal.properties.DockerRegistry;
import dev.galasa.docker.internal.properties.DockerRegistryBusyboxImage;
import dev.galasa.docker.internal.properties.DockerSlots;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.HttpFileResponse;
import dev.galasa.http.IHttpClient;

public class DockerEngineImpl implements IDockerEngine {
	private IFramework framework;
	private DockerManagerImpl dockerManager;
	private final IHttpClient dockerEngineClient;
	private final IDynamicStatusStoreService dss;

	private URI uri;
	private SSLContext sslContext;

	private String dockerEngineId;
	private String dockerEngineTag;
	private String dockerVersion;
	private String apiVersion;

	private static final Log logger = LogFactory.getLog(DockerEngine.class);

	/**
	 * Docker Engine Implementation. This provides all the docker engine API calls
	 * to perform docker commands on a specified engine running docker.
	 * 
	 * @param framework
	 * @param dockerManager
	 * @param dockerEngineTag
	 * @param dss
	 * @throws DockerProvisionException
	 */
	public DockerEngineImpl(IFramework framework, DockerManagerImpl dockerManager, String dockerEngineTag,
			IDynamicStatusStoreService dss) throws DockerProvisionException {
		this.framework = framework;
		this.dockerManager = dockerManager;
		this.dockerEngineTag = dockerEngineTag;
		this.dss = dss;

		this.dockerEngineClient = dockerManager.getHttpManager().newHttpClient();
		try {

			// Get the DSE image
			this.dockerEngineId = DockerDSEEngine.get(this);
			if (this.dockerEngineId != null) {
				initDseEngine();
			} else {
				// Only works on "default" cluster currently
				initClusterEngine();
			}

			if (this.uri.toString() == null) {
				throw new DockerProvisionException("Could not locate a availabe engine");
			}
			
			String credentialsId = DockerEngineCredentials.get(this);
			if (credentialsId != null) {
				configureHttps(credentialsId);
				// Explicitly build the HTTP client after SSL configuration
				dockerEngineClient.build();
			} else {
				logger.debug("No credentials configured for Docker engine " + dockerEngineTag + ", using HTTP");
			}

		} catch (Exception e) {
			throw new DockerProvisionException("Unable to instantiate Docker Engine", e);
		}

	}

	// Setup the client for the selected Engine
	private void initDseEngine() throws DockerManagerException, URISyntaxException {
		String engine = DockerEngine.get(this);
		String port = DockerEnginePort.get(this);

		if (!engine.startsWith("http://") && !engine.startsWith("https://")) {
			// If no scheme, default to http
			engine = "http://" + engine;
		}

		this.uri = new URI(engine + ":" + port);
		this.dockerEngineClient.setURI(this.uri);
		logger.info("Docker DSE Engine is set to " + this.dockerEngineId);
	}

	// Locate a free engine from a group of engines
	private void initClusterEngine() throws DockerManagerException, DynamicStatusStoreException, URISyntaxException,
			DockerProvisionException {
		// Get available Engines
		String[] engines = DockerEngines.get(this).split(",");

		// Select an engine and check slot availabilty
		for (String engineId : engines) {
			this.dockerEngineId = engineId;
			String engine = DockerEngine.get(this);
			String port = DockerEnginePort.get(this);
			int slotLimit = Integer.parseInt(DockerSlots.get(this));

			if (!engine.startsWith("http://") && !engine.startsWith("https://")) {
				// If no scheme, default to http
				engine = "http://" + engine;
			}

			// Quick check to see if there is an engine with a free slot. This does not allocate slot
			String currentSlots = dss.get("engine." + engineId + ".current.slots");
			if (currentSlots == null) {
				currentSlots = "0";
			}
			int currentSlotsI = Integer.parseInt(currentSlots);

			if (currentSlotsI < slotLimit) {
				if (engine != null && port != null) {
					this.uri = new URI(engine + ":" + port);
					this.dockerEngineClient.setURI(this.uri);
					logger.info("Docker Engine is set to " + engineId);
					return;
				}
			}
			logger.info("Engine " + engineId + " has no free slots. Checking to see if another engine is available.");
		}
		throw new DockerProvisionException("No Engines are free");
	}

	/**
	 * Configure HTTPS connection when credentials are specified in CPS.
	 *
	 * This method retrieves the KeyStore from the Credentials Store
	 * and configures the HTTP client for mutual TLS authentication.
	 * HTTPS is automatically enabled when credentials are configured.
	 *
	 * @param credentialsId the ID of the credentials containing the KeyStore
	 * @throws DockerProvisionException if HTTPS configuration fails
	 */
	private void configureHttps(String credentialsId) throws DockerProvisionException {
		try {
			logger.info("Configuring HTTPS for Docker engine " + dockerEngineTag + " using credentials: " + credentialsId);
			logger.debug("Docker engine URI before HTTPS configuration: " + this.uri);

			ICredentialsKeyStore keyStoreCreds = retrieveKeyStoreCredentials(credentialsId);
			configureClientSsl(keyStoreCreds);
			
			// Always use HTTPS when credentials are configured
			this.uri = convertUriToHttps(this.uri);
			dockerEngineClient.setURI(this.uri);
			logger.info("Docker engine " + dockerEngineTag + " configured for HTTPS: " + this.uri);
			
		} catch (CredentialsException | HttpClientException | URISyntaxException e) {
			throw new DockerProvisionException("Failed to configure HTTPS for Docker engine " + dockerEngineTag, e);
		}
	}

	/**
	 * Retrieve and validate KeyStore credentials from the Credentials Store.
	 *
	 * @param credentialsId the ID of the credentials to retrieve
	 * @return the KeyStore credentials
	 * @throws DockerProvisionException if credentials are not found or invalid
	 * @throws CredentialsException if there's an error retrieving credentials
	 */
	private ICredentialsKeyStore retrieveKeyStoreCredentials(String credentialsId)
			throws DockerProvisionException, CredentialsException {
		ICredentials credentials = framework.getCredentialsService().getCredentials(credentialsId);
		
		if (credentials == null) {
			throw new DockerProvisionException("Credentials '" + credentialsId + "' not found in Credentials Store");
		}
		
		if (!(credentials instanceof ICredentialsKeyStore)) {
			throw new DockerProvisionException(
				"Credentials '" + credentialsId + "' must be of type KeyStore for Docker HTTPS. " +
				"Found type: " + credentials.getClass().getSimpleName());
		}
		
		return (ICredentialsKeyStore) credentials;
	}

	/**
	 * Configure the HTTP client with SSL context using the provided KeyStore credentials.
	 *
	 * This method creates an SSLContext that is used by both the HTTP Manager client
	 * and raw HttpURLConnection instances (e.g., in DockerExecImpl).
	 *
	 * @param keyStoreCreds the KeyStore credentials containing the certificate and key
	 * @throws HttpClientException if SSL configuration fails
	 */
	private void configureClientSsl(ICredentialsKeyStore keyStoreCreds) throws HttpClientException {
		try {
			KeyStore keyStore = keyStoreCreds.getKeyStore();
			String password = keyStoreCreds.getKeyStorePassword();
			
			// Create KeyManagerFactory for client authentication
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, password.toCharArray());
			
			// Create TrustManagerFactory for server certificate validation
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(keyStore);
			
			// Create and initialize SSLContext
			this.sslContext = SSLContext.getInstance("TLS");
			this.sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			
			// Configure HTTP client with SSL context
			// Use the same KeyStore for both client auth and server trust
			dockerEngineClient.setupClientAuth(keyStore, password);
			
		} catch (Exception e) {
			throw new HttpClientException("Failed to configure SSL context", e);
		}
	}

	/**
	 * Convert a URI to use HTTPS scheme while preserving all other components.
	 *
	 * @param currentUri the current URI
	 * @return a new URI with HTTPS scheme
	 * @throws URISyntaxException if URI construction fails
	 */
	private URI convertUriToHttps(URI currentUri) throws URISyntaxException {
		return new URI("https",
					  currentUri.getUserInfo(),
					  currentUri.getHost(),
					  currentUri.getPort(),
					  currentUri.getPath(),
					  currentUri.getQuery(),
					  currentUri.getFragment());
	}

	public String getEngineTag() {
		return this.dockerEngineTag;
	}

	public String getEngineId() {
		return this.dockerEngineId;
	}

	/**
	 * Checks the docker engine is contactable.
	 * 
	 * @throws DockerProvisionException
	 */
	public void checkEngine() throws DockerProvisionException {
		try {
			JsonObject jsonVersion = getJson("/version");

			if (jsonVersion != null) {
				dockerVersion = jsonVersion.get("Version").getAsString();
				apiVersion = jsonVersion.get("ApiVersion").getAsString();
			}

			logger.info("Docker engine is running, version: " + dockerVersion + ", apiVersion: " + apiVersion);
		} catch (DockerManagerException e) {
			throw new DockerProvisionException("Unable to validate Docker engine connectivity.", e);
		}
	}

	/**
	 * Retrieve docker logs from a docker container from the url
	 * 
	 * @param path
	 * @return
	 * @throws DockerManagerException
	 */
	public String getLog(String path) throws DockerManagerException {
		try {
			HttpClientResponse<String> response = dockerEngineClient.getText(path);

			String repsString = response.getContent();

			switch (response.getStatusCode()) {
			case HttpStatus.SC_OK:
				return repsString;
			case HttpStatus.SC_NOT_FOUND:
				return null;
			}

			logger.error(
					"Get Log failed to Docker engine - " + response.getStatusLine().toString() + "\n" + repsString);
			throw new DockerManagerException(
					"Log Get failed to Docker engine - " + response.getStatusLine().toString());
		} catch (Exception e) {
			throw new DockerManagerException("Get Log failed to Docker engine", e);
		}
	}

	/**
	 * Pull docker image from registries
	 * 
	 * @param fullName
	 * @return String repsonse
	 * @throws DockerManagerException
	 */
	public String pullImage(@NotNull String fullName) throws DockerManagerException {
		return postString("/images/create?fromImage=" + fullName, "");
	}

	public String pullImage(@NotNull String fullName, String registryToken) throws DockerManagerException {

		dockerEngineClient.addCommonHeader("X-Registry-Auth", registryToken);
		return pullImage(fullName);
	}

	public byte[] buildImage(String imageName, Path dockerfile) throws DockerManagerException, IOException {
		return postBinary("/build?t="+imageName, Files.readAllBytes(dockerfile));
	}

	public byte[] postBinary(String path, byte[] data) throws DockerManagerException {
		try {
			HttpClientResponse<byte[]> resp = dockerEngineClient.postBinary(path, data);
			byte[] response = resp.getContent();

			switch (resp.getStatusCode()) {
			case HttpStatus.SC_OK:
			case HttpStatus.SC_CREATED:
				return response;
			case HttpStatus.SC_NO_CONTENT:
				return null;
			case HttpStatus.SC_NOT_FOUND:
				throw new DockerNotFoundException("Docker API post returned 'not found': " + response.toString());
			}

			logger.error("Post failed to Docker engine - " + resp.getStatusLine());
			logger.error(resp.getStatusMessage());
			throw new DockerManagerException("Post failed to Docker engine - " + resp.getStatusLine());
		} catch (Exception e) {
			throw new DockerManagerException("Post failed to Docker engine", e);
		}
	}

	/**
	 * Retrieves the image information
	 * 
	 * @param imageName
	 * @return JsonObject
	 * @throws DockerManagerException
	 */
	public JsonObject getImage(@NotNull String imageName) throws DockerManagerException {
		return getJson("/images/" + imageName + "/json");
	}

	public JsonObject getVolume(String volumeName) throws DockerManagerException {
		return getJson("/volumes/" + volumeName);
	}

	public String deleteVolume(String volumeName) throws DockerManagerException {
		return deleteString("/volumes/" + volumeName);
	}

	/**
	 * Create a volume with a defined name. The volume will not be tied to the test as a resource to be cleaned up
	 * at the end of test. Instead it will be monitored and cleaned up from a user defined CPS property.
	 * 
	 * @param volumeName
	 * @return
	 * @throws DockerManagerException
	 */
	public JsonObject createVolume(String volumeName) throws DockerManagerException {
		JsonObject data = new JsonObject();
		if (!"".equals(volumeName)) {
			data.addProperty("Name", volumeName);
		}
		
		JsonObject labels = new JsonObject();
		labels.addProperty("GALASA", "GALASA");
		labels.addProperty("RUN_ID", framework.getTestRunName());

		data.add("Labels", labels);

		return postJson("/volumes/create", data);
	}

	/**
	 * Retrieves container information from the container id
	 * 
	 * @param containerId
	 * @return JsonObject
	 * @throws DockerManagerException
	 */
	public JsonObject getContainer(@NotNull String containerId) throws DockerManagerException {
		return getJson("/containers/" + containerId + "/json");
	}

	/**
	 * Creates a container from a image name
	 * 
	 * @param imageName
	 * @param imageData
	 * @return JsonObject
	 * @throws DockerManagerException
	 */
	public JsonObject createContainer(@NotNull String imageName, JsonObject imageData) throws DockerManagerException {
		return postJson("/containers/create?name=" + imageName, imageData);
	}

	/**
	 * Kills a container from it's container id
	 * 
	 * @param containerId
	 * @return String response
	 * @throws DockerManagerException
	 */
	public String killContainer(@NotNull String containerId) throws DockerManagerException {
		return postString("/containers/" + containerId + "/kill", "");
	}

	/**
	 * Deletes a container from the docker engine using container id.
	 * 
	 * @param containerId
	 * @return String response
	 * @throws DockerManagerException
	 */
	public String deleteContainer(@NotNull String containerId) throws DockerManagerException {
		return deleteString("/containers/" + containerId + "?force=true&v=true");
	}

	/**
	 * Starts a docker container on the docker engine from the container id.
	 * 
	 * @param containerId
	 * @return
	 * @throws DockerManagerException
	 */
	public String startContainer(@NotNull String containerId) throws DockerManagerException {
		return postString("/containers/" + containerId + "/start", "");
	}

	/**
	 * Sends commands through to a docker container running on the docker engine
	 * using the container id.
	 * 
	 * @param containerId
	 * @param commandData
	 * @return
	 * @throws DockerManagerException
	 */
	public JsonObject sendExecCommands(@NotNull String containerId, JsonObject commandData)
			throws DockerManagerException {
		return postJson("/containers/" + containerId + "/exec", commandData);
	}

	/**
	 * Returns the state of exec commands being performed on a docker container
	 * using container id.
	 * 
	 * @param containerId
	 * @return
	 * @throws DockerManagerException
	 */
	public JsonObject getExecInfo(@NotNull String containerId) throws DockerManagerException {
		return getJson("/exec/" + containerId + "/json");
	}

	/**
	 * Returns docker engine host
	 * 
	 * @return String
	 */
	public String getHost() {
		return this.uri.getHost();
	}
	
	public String getBusybox() throws DockerManagerException {
		String[] dockerRegistries = DockerRegistry.get();
		String busyboxImageName = DockerRegistryBusyboxImage.get(dockerRegistries);
		DockerImageImpl image = new DockerImageImpl(framework, dockerManager, this, busyboxImageName);
		image.locateImage();
		return image.getFullName();
	}

	/**
	 * returns the docker engine URI
	 * 
	 * @return URI
	 * @throws URISyntaxException
	 */
	public URI getURI() throws URISyntaxException {
		return this.uri;
	}

	/**
	 * Get the SSL context for HTTPS connections.
	 *
	 * This SSL context is used by both the HTTP Manager and raw HttpURLConnection
	 * instances to ensure consistent TLS configuration across all Docker API calls.
	 *
	 * @return the SSL context, or null if HTTPS is not configured
	 */
	public SSLContext getSslContext() {
		return this.sslContext;
	}

	/**
	 * Issues a HTTP DELETE command to the specified path
	 * 
	 * @param path
	 * @return String response
	 * @throws DockerManagerException
	 */
	private String deleteString(String path) throws DockerManagerException {
		try {
			HttpClientResponse<String> response = dockerEngineClient.deleteText(path);
			String resp = response.getContent();

			switch (response.getStatusCode()) {
			case HttpStatus.SC_OK:
			case HttpStatus.SC_CREATED:
				return resp;
			case HttpStatus.SC_NO_CONTENT:
			case HttpStatus.SC_NOT_FOUND:
				return null;
			}

			logger.error("Delete failed to Docker engine - " + resp);
			throw new DockerManagerException("Delete failed to Docker engine - " + resp);
		} catch (Exception e) {
			dockerEngineClient.close();
			throw new DockerManagerException("Delete failed to Docker engine", e);
		}
	}

	/**
	 * Performs a HTTP GET to the docker engine to a specified path
	 * 
	 * @param path
	 * @return JsonObject
	 * @throws DockerManagerException
	 */
	private JsonObject getJson(String path) throws DockerManagerException {
		try {
			HttpClientResponse<JsonObject> response = dockerEngineClient.getJson(path);

			JsonObject jsonResponse = response.getContent();

			switch (response.getStatusCode()) {
			case HttpStatus.SC_OK:
				return jsonResponse;
			case HttpStatus.SC_NOT_FOUND:
				return null;
			}
			logger.error("Get failed to Docker engine - " + response.getStatusLine().toString() + "\n"
					+ jsonResponse.getAsString());
			throw new DockerManagerException("Get failed to Docker engine - " + response.getStatusLine().toString());
		} catch (Exception e) {
			throw new DockerManagerException("Failed to get from Docker engine: ", e);
		}
	}

	/**
	 * Performs a HTTP POST to the docker engine to a specified path with a json
	 * body.
	 * 
	 * @param path
	 * @param data
	 * @return JsonObject
	 * @throws DockerManagerException
	 */
	private JsonObject postJson(String path, JsonObject data) throws DockerManagerException {
		try {
			HttpClientResponse<JsonObject> json = dockerEngineClient.postJson(path, data);
			JsonObject response = json.getContent();

			switch (json.getStatusCode()) {
			case HttpStatus.SC_OK:
			case HttpStatus.SC_CREATED:
				return response;
			case HttpStatus.SC_NO_CONTENT:
				return null;
			case HttpStatus.SC_NOT_FOUND:
				throw new DockerNotFoundException("Docker API post returned 'not found': " + response.toString());
			}

			logger.error("Post failed to Docker engine - " + response.getAsString());
			throw new DockerManagerException("Post failed to Docker engine - " + response.getAsString());
		} catch (Exception e) {
			throw new DockerManagerException("Post failed to Docker engine", e);
		}
	}

	/**
	 * Performs a HTTP POST to the docker engine to a specified path with a text
	 * body.
	 * 
	 * @param path
	 * @param data
	 * @return
	 * @throws DockerManagerException
	 */
	private String postString(String path, String data) throws DockerManagerException {
		try {
			logger.debug("Posting: " + data + "to the endpoint: " + path);
			HttpClientResponse<String> response = dockerEngineClient.postText(path, data);
			String resp = response.getContent();

			switch (response.getStatusCode()) {
			case HttpStatus.SC_OK:
			case HttpStatus.SC_CREATED:
				return resp;
			case HttpStatus.SC_NO_CONTENT:
				return null;
			case HttpStatus.SC_NOT_FOUND:
				throw new DockerNotFoundException("Docker API post returned 'not found':" + resp.toString());
			}

			logger.error("Post failed to docker engine - " + resp);
			throw new DockerManagerException("Post failed to Docker engine - " + resp);
		} catch (Exception e) {
			dockerEngineClient.close();
			throw new DockerManagerException("Post failed to Docker engine", e);
		}
	}

	/**
	 * Can send a file onto a container running on the docker engine
	 * @param container
	 * @param file
	 * @param location
	 */
	public void sendArchiveFile(DockerContainerImpl container, InputStream file, String location) {
		String path = "/containers/" + container.getContainerId() + "/archive?path=" + location;

		dockerEngineClient.putFile(path, file);
	}

	/**
	 * Returns the contents of a file on a container running in the docker engine
	 * 
	 * @param container
	 * @param filePath
	 * @return String
	 * @throws HttpClientException
	 */
	public InputStream getArchiveFile(DockerContainerImpl container, String filePath) throws DockerManagerException {
		String path = "/containers/" + container.getContainerId() + "/archive?path=" + filePath;

		try {
			HttpFileResponse response = dockerEngineClient.getFileStream(path);
			InputStream in = response.getContent();
			
			TarArchiveInputStream tais = new TarArchiveInputStream(in);
			ArchiveEntry ae = tais.getNextEntry();
			if (ae == null) {
				tais.close();
				in.close();
				throw new DockerManagerException("Could not find entry in returned archive file");
			}

			return tais;		
		} catch (HttpClientException |IOException e) {
			logger.error("Failed to read returned output", e);
			throw new DockerManagerException("Could not find entry in returned archive file");
		}
	}
}