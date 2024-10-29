/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.plugin.common.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import dev.galasa.plugin.common.AuthenticationService;
import dev.galasa.plugin.common.PluginCommonFactory;
import dev.galasa.plugin.common.BootstrapLoader;
import dev.galasa.plugin.common.ErrorRaiser;
import dev.galasa.plugin.common.TestCatalogArtifact;
import dev.galasa.plugin.common.UrlCalculator;
import dev.galasa.plugin.common.WrappedLog;

public class TestCatalogArtifactDeployer<Ex extends Exception> {

    private WrappedLog log ;
    private ErrorRaiser<Ex> errorRaiser ;
    private BootstrapLoader<Ex> bootstrapLoader ;
    private UrlCalculator<Ex> urlCalculator ;
    private GalasaRestApiMetadataImpl restApiMetadata;
    private PluginCommonFactory<Ex> pluginCommonFactory;

    public TestCatalogArtifactDeployer(
        WrappedLog log, 
        ErrorRaiser<Ex> errorRaiser, 
        PluginCommonFactory<Ex> pluginCommonFactory
    ) {
        this.log = log ;
        this.errorRaiser = errorRaiser ;
        this.bootstrapLoader = pluginCommonFactory.newBootstrapLoader(log,errorRaiser);
        this.urlCalculator = pluginCommonFactory.newUrlCalculator(errorRaiser);
        this.restApiMetadata = new GalasaRestApiMetadataImpl();
        this.pluginCommonFactory = pluginCommonFactory;
    }

    public void deployToServer(URL bootstrapUrl, String testStream, String galasaAccessToken, TestCatalogArtifact<Ex> testCatalogArtifact ) throws Ex {

        this.log.warn("This task/goal is deprecated and will be removed in future versions of Galasa."+
        " Consider using the galasactl tool to set your test stream 'location' URL to refer to the location of your test "+
        "catalog where it is located in your published maven repository.");

        Properties bootstrapProperties = bootstrapLoader.getBootstrapProperties(bootstrapUrl);

        String apiServerUrl = urlCalculator.calculateApiServerUrl(bootstrapProperties, bootstrapUrl);

        URL testcatalogUrl = urlCalculator.calculateTestCatalogUrl(apiServerUrl, testStream);

        String jwt = null ;
        // For now, if no galasa token is supplied, that's ok. It's optional.   
        // If no galasa access token supplied by the user, the jwt will stay as null.
        if ( (galasaAccessToken!=null) && (!galasaAccessToken.isEmpty()) ) {
            jwt = getAuthenticatedJwt(this.pluginCommonFactory, galasaAccessToken, apiServerUrl) ;
        }

        publishTestCatalogToGalasaServer(testcatalogUrl,jwt, testCatalogArtifact);
    }

    private void publishTestCatalogToGalasaServer(URL testCatalogUrl, String jwt, TestCatalogArtifact<Ex> testCatalogArtifact) throws Ex {
 
        HttpURLConnection conn = null ;
        try {
            conn = (HttpURLConnection) testCatalogUrl.openConnection();
        } catch (IOException ioEx) {
            this.errorRaiser.raiseError(ioEx,"Problem publishing the test catalog. Could not open URL connection to the Galasa server.");
        }

        if (conn==null) {
            this.errorRaiser.raiseError("Deploy to Test Catalog Store failed. Could not open a URL connection to the Galasa server.");
        } else {
            try {
                postTestCatalogToGalasaServer(conn, testCatalogUrl, jwt, testCatalogArtifact);
            } finally {
                conn.disconnect();
            }
            
            // The following should probably be an 'info' message, but if you warn in the log, then it gets displayed 
            // on the user console, so this important event is more visible this way.
            this.log.warn("Test Catalog successfully deployed to " + testCatalogUrl.toString());

        }
    }

    private void postTestCatalogToGalasaServer( 
        HttpURLConnection conn, 
        URL testCatalogUrl,     
        String jwt, 
        TestCatalogArtifact<Ex> testCatalogArtifact
    ) throws Ex {

        int rc = 0 ;
        String response = "";
        String message = "";
        
        try {
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("PUT");
            conn.addRequestProperty("Content-Type", "application/json");
            conn.addRequestProperty("Accept", "application/json");

            // Only add the jwt header if we have a jwt value.
            if (jwt == null) {
                this.log.info("Not sending a JWT bearer token to the server, as the galasa.token property was not supplied.");
            } else {
                conn.addRequestProperty("Authorization", "Bearer "+jwt);
            }

            conn.addRequestProperty("ClientApiVersion",restApiMetadata.getGalasaRestApiVersion()); // The version of the API we coded this against.

            testCatalogArtifact.transferTo(conn.getOutputStream());
            
            rc = conn.getResponseCode();
            message = conn.getResponseMessage();

            InputStream is = null;
            if (rc != HttpURLConnection.HTTP_OK) {
                is = conn.getInputStream();
            } else {
                is = conn.getErrorStream();
            }

            if (is != null) {
                response = IOUtils.toString(is, "utf-8");
            }
        } catch(IOException ioEx) {
            this.errorRaiser.raiseError(ioEx, "Problem publishing the test catalog. Problem dealing with response from Galasa server.");
        } 

        if (rc != HttpURLConnection.HTTP_OK) {
            this.log.error("Deploy to Test Catalog Store failed:-");
            this.log.error(Integer.toString(rc) + " - " + message);
            if (!response.isEmpty()) {
                this.log.error(response);
                errorRaiser.raiseError("Failed to deploy the test catalog. The server did not reply with OK (200)");
            }
        }
    }

    /**
     * Swap the galasa token for a JWT which we can use to talk to the API server on the ecosystem.
     * @return A JWT string (Java Web Token).
     * @throws MojoExecutionException
     */
    private String getAuthenticatedJwt(PluginCommonFactory<Ex> authFactory, String galasaAccessToken, String apiServerUrlString) throws Ex {
        String jwt = null ;
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            URL apiServerUrl = new URL(apiServerUrlString);
            AuthenticationService authTokenService = authFactory.newAuthenticationService(apiServerUrl,galasaAccessToken,httpClient);
            this.log.info("Turning the galasa access token into a JWT");
            jwt = authTokenService.getJWT();
            this.log.info("Java Web Token (JWT) obtained from the galasa ecosystem OK.");
        } catch( Exception ex) {
            this.errorRaiser.raiseError(ex,"Failure when exchanging the galasa access token with a JWT");
        } 
        return jwt;
    }
}
