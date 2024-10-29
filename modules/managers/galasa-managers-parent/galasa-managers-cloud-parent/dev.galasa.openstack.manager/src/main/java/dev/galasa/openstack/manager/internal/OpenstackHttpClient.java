/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openstack.manager.internal;

import java.time.ZonedDateTime;
import java.util.ArrayList;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGson;
import dev.galasa.openstack.manager.OpenstackManagerException;
import dev.galasa.openstack.manager.internal.json.Api;
import dev.galasa.openstack.manager.internal.json.Auth;
import dev.galasa.openstack.manager.internal.json.AuthTokenResponse;
import dev.galasa.openstack.manager.internal.json.AuthTokens;
import dev.galasa.openstack.manager.internal.json.Domain;
import dev.galasa.openstack.manager.internal.json.Endpoint;
import dev.galasa.openstack.manager.internal.json.Flavor;
import dev.galasa.openstack.manager.internal.json.Flavors;
import dev.galasa.openstack.manager.internal.json.Floatingip;
import dev.galasa.openstack.manager.internal.json.FloatingipRequestResponse;
import dev.galasa.openstack.manager.internal.json.Floatingips;
import dev.galasa.openstack.manager.internal.json.Identity;
import dev.galasa.openstack.manager.internal.json.Image;
import dev.galasa.openstack.manager.internal.json.Images;
import dev.galasa.openstack.manager.internal.json.Network;
import dev.galasa.openstack.manager.internal.json.Networks;
import dev.galasa.openstack.manager.internal.json.Password;
import dev.galasa.openstack.manager.internal.json.Port;
import dev.galasa.openstack.manager.internal.json.PortsResponse;
import dev.galasa.openstack.manager.internal.json.Project;
import dev.galasa.openstack.manager.internal.json.Scope;
import dev.galasa.openstack.manager.internal.json.Server;
import dev.galasa.openstack.manager.internal.json.ServerRequest;
import dev.galasa.openstack.manager.internal.json.ServerResponse;
import dev.galasa.openstack.manager.internal.json.ServersResponse;
import dev.galasa.openstack.manager.internal.json.User;
import dev.galasa.openstack.manager.internal.properties.OpenStackCredentialsId;
import dev.galasa.openstack.manager.internal.properties.OpenStackDomainName;
import dev.galasa.openstack.manager.internal.properties.OpenStackIdentityUri;
import dev.galasa.openstack.manager.internal.properties.OpenStackProjectName;

public class OpenstackHttpClient {

    private final static Log          logger = LogFactory.getLog(OpenstackHttpClient.class);

    private final IFramework          framework;

    private final CloseableHttpClient httpClient;
    private OpenstackToken            openstackToken;

    private String                    openstackImageUri;
    private String                    openstackComputeUri;
    private String                    openstackNetworkUri;

    private GalasaGson                      gson   = new GalasaGson();

    protected OpenstackHttpClient(IFramework framework) throws ConfigurationPropertyStoreException {
        this.framework = framework;
        this.httpClient = HttpClients.createDefault();

    }

    protected void checkToken() throws OpenstackManagerException {
        if (openstackToken == null || !openstackToken.isOk()) {
            this.openstackToken = null;
            if (!connectToOpenstack()) {
                throw new OpenstackManagerException("Unable to re-authenticate with the OpenStack server");
            }
        }
    }

    protected boolean connectToOpenstack() throws OpenstackManagerException {
        if (this.openstackToken != null) {
            return true;
        }

        try {
            String identityEndpoint = OpenStackIdentityUri.get();
            String domain = OpenStackDomainName.get();
            String project = OpenStackProjectName.get();

            if (identityEndpoint == null) {
                logger.warn("Openstack is not available due to identity property missing in CPS");
                return false;
            }

            if (domain == null) {
                logger.warn("Openstack is not available due to domain property is missing in CPS");
                return false;
            }

            if (project == null) {
                logger.warn("Openstack is not available due to project property is missing in CPS");
                return false;
            }

            String credentialsId = OpenStackCredentialsId.get();

            ICredentials credentials = null;
            try {
                credentials = this.framework.getCredentialsService().getCredentials(credentialsId);
            } catch (Exception e) {
                logger.warn("OpenStack is not available due to missing credentials " + credentialsId);
                return false;
            }
            
            if (credentials == null) {
                logger.warn("OpenStack credentials are missing");
                return false;
            }

            if (!(credentials instanceof ICredentialsUsernamePassword)) {
                logger.warn("OpenStack credentials are not a username/password");
                return false;
            }

            ICredentialsUsernamePassword usernamePassword = (ICredentialsUsernamePassword) credentials;


            AuthTokens authTokens = new AuthTokens();
            authTokens.auth = new Auth();
            authTokens.auth.identity = new Identity();
            authTokens.auth.identity.methods = new ArrayList<>();
            authTokens.auth.identity.methods.add("password");
            authTokens.auth.identity.password = new Password();
            authTokens.auth.identity.password.user = new User();
            authTokens.auth.identity.password.user.name = usernamePassword.getUsername();
            authTokens.auth.identity.password.user.password = usernamePassword.getPassword();
            authTokens.auth.identity.password.user.domain = new Domain();
            authTokens.auth.identity.password.user.domain.name = domain;
            authTokens.auth.scope = new Scope();
            authTokens.auth.scope.project = new Project();
            authTokens.auth.scope.project.name = project;
            authTokens.auth.scope.project.domain = new Domain();
            authTokens.auth.scope.project.domain.name = domain;

            String content = gson.toJson(authTokens);

            HttpPost post = new HttpPost(identityEndpoint + "/auth/tokens");
            StringEntity entity = new StringEntity(content, ContentType.APPLICATION_JSON);
            post.setEntity(entity);

            try (CloseableHttpResponse response = this.httpClient.execute(post)) {
                StatusLine status = response.getStatusLine();
                HttpEntity responseEntity = response.getEntity();
                String responseString = EntityUtils.toString(responseEntity);
                if (status.getStatusCode() != HttpStatus.SC_CREATED) {
                    logger.warn("OpenStack is not available due to identity responding with " + status);
                    return false;
                }

                AuthTokenResponse tokenResponse = gson.fromJson(responseString, AuthTokenResponse.class);
                Header tokenHeader = response.getFirstHeader("X-Subject-Token");
                if (tokenHeader == null) {
                    logger.warn("OpenStack is not available due to missing X-Subject-Token");
                    return false;
                }

                this.openstackImageUri = null;

                if (tokenResponse.token != null && tokenResponse.token.catalog != null) {
                    for (Api api : tokenResponse.token.catalog) {
                        if ("image".equals(api.type)) {
                            if (api.endpoints != null) {
                                for (Endpoint endpoint : api.endpoints) {
                                    if ("public".equals(endpoint.endpoint_interface)) {
                                        this.openstackImageUri = endpoint.url;
                                    }
                                }
                            }
                        } else if ("compute".equals(api.type)) {
                            if (api.endpoints != null) {
                                for (Endpoint endpoint : api.endpoints) {
                                    if ("public".equals(endpoint.endpoint_interface)) {
                                        this.openstackComputeUri = endpoint.url;
                                    }
                                }
                            }
                        } else if ("network".equals(api.type)) {
                            if (api.endpoints != null) {
                                for (Endpoint endpoint : api.endpoints) {
                                    if ("public".equals(endpoint.endpoint_interface)) {
                                        this.openstackNetworkUri = endpoint.url;
                                    }
                                }
                            }
                        }

                    }
                }

                if (this.openstackImageUri == null) {
                    logger.info("OpenStack is not available as some APIs are missing");
                    return false;
                }

                String tokenString = tokenHeader.getValue();
                ZonedDateTime zdt = ZonedDateTime.parse(tokenResponse.token.expires_at);

                this.openstackToken = new OpenstackToken(tokenString, zdt.toInstant());
                
                logger.debug("Connected to the OpenStack server");

                return true;
            }
        } catch (Exception e) {
            logger.warn("OpenStack is not available due to " + e.getMessage()); // not reporting full stacktrace to keep
                                                                                // log compact, as this could be
                                                                                // expected
            return false;
        }
    }

    public Server findServerByName(@NotNull String serverName) throws OpenstackManagerException {
        try {
            checkToken();

            // *** Retrieve a list of the servers available and select one

            HttpGet get = new HttpGet(this.openstackComputeUri + "/servers");
            get.addHeader(this.openstackToken.getHeader());

            try (CloseableHttpResponse response = httpClient.execute(get)) {
                StatusLine status = response.getStatusLine();
                String entity = EntityUtils.toString(response.getEntity());

                if (status.getStatusCode() != HttpStatus.SC_OK) {
                    throw new OpenstackManagerException("OpenStack list servers failed - " + status);
                }

                ServersResponse servers = this.gson.fromJson(entity, ServersResponse.class);
                if (servers != null && servers.servers != null) {
                    for (Server server : servers.servers) {
                        if (serverName.equals(server.name)) {
                            return server;
                        }
                    }
                }
            }
            return null;
        } catch (OpenstackManagerException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenstackManagerException("Unable to list servers ", e);
        }
    }

    public void deleteServer(Server server) throws OpenstackManagerException {
        if (server.id == null) {
            return;
        }

        try {
            checkToken();

            // *** Delete the server

            HttpDelete delete = new HttpDelete(this.openstackComputeUri + "/servers/" + server.id);
            delete.addHeader(this.openstackToken.getHeader());

            try (CloseableHttpResponse response = httpClient.execute(delete)) {
                StatusLine status = response.getStatusLine();
                EntityUtils.consume(response.getEntity());

                if (status.getStatusCode() != HttpStatus.SC_NO_CONTENT) {
                    throw new OpenstackManagerException("OpenStack delete server failed - " + status);
                }
            }
            return;
        } catch (OpenstackManagerException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenstackManagerException("Unable to delete server ", e);
        }
    }

    public Server getServer(String id) throws OpenstackManagerException {
        if (id == null) {
            return null;
        }

        try {
            checkToken();

            // *** Get the server

            HttpGet get = new HttpGet(this.openstackComputeUri + "/servers/" + id);
            get.addHeader(this.openstackToken.getHeader());

            try (CloseableHttpResponse response = httpClient.execute(get)) {
                StatusLine status = response.getStatusLine();
                String entity = EntityUtils.toString(response.getEntity());

                if (status.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                    return null;
                }

                if (status.getStatusCode() != HttpStatus.SC_OK) {
                    throw new OpenstackManagerException("OpenStack list servers failed - " + status);
                }

                ServerResponse server = this.gson.fromJson(entity, ServerResponse.class);
                if (server != null && server.server != null) {
                    return server.server;
                }
            }
            return null;
        } catch (OpenstackManagerException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenstackManagerException("Unable to list servers ", e);
        }
    }

    public Server createServer(@NotNull ServerRequest serverRequest) throws OpenstackManagerException {
        try {
            checkToken();

            HttpPost get = new HttpPost(this.openstackComputeUri + "/servers");
            get.addHeader(this.openstackToken.getHeader());
            get.setEntity(new StringEntity(gson.toJson(serverRequest)));

            try (CloseableHttpResponse response = this.httpClient.execute(get)) {
                StatusLine status = response.getStatusLine();
                String entity = EntityUtils.toString(response.getEntity());

                if (status.getStatusCode() != HttpStatus.SC_ACCEPTED) {
                    throw new OpenstackManagerException("OpenStack create server failed - " + status + "\n" + entity);
                }

                ServerResponse serverResponse = gson.fromJson(entity, ServerResponse.class);
                if (serverResponse.server == null) {
                    throw new OpenstackManagerException(
                            "Unexpected response from create server, server is missing:-\n" + entity);
                }

                if (serverResponse.server.id == null) {
                    throw new OpenstackManagerException("OpenStack did not return a server id");
                }

                if (serverResponse.server.adminPass == null) {
                    throw new OpenstackManagerException("OpenStack did not return a password");
                }

                return serverResponse.server;
            }
        } catch (OpenstackManagerException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenstackManagerException("Unable to create server ", e);
        }
    }

    public Floatingip findFloatingIpByName(String fipName) throws OpenstackManagerException {
        try {
            checkToken();

            // *** Retrieve a list of the floating ips allocated to the project

            HttpGet get = new HttpGet(this.openstackNetworkUri + "/v2.0/floatingips");
            get.addHeader(this.openstackToken.getHeader());

            try (CloseableHttpResponse response = httpClient.execute(get)) {
                StatusLine status = response.getStatusLine();
                String entity = EntityUtils.toString(response.getEntity());

                if (status.getStatusCode() != HttpStatus.SC_OK) {
                    throw new OpenstackManagerException("OpenStack list floating ips failed - " + status);
                }

                Floatingips fips = this.gson.fromJson(entity, Floatingips.class);
                if (fips != null && fips.floatingips != null) {
                    for (Floatingip fip : fips.floatingips) {
                        if (fipName.equals(fip.floating_ip_address)) {
                            return fip;
                        }
                    }
                }
            }
            return null;
        } catch (OpenstackManagerException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenstackManagerException("Unable to list floating ips ", e);
        }
    }

    public void deleteFloatingIp(Floatingip floatingip) throws OpenstackManagerException {
        if (floatingip.id == null) {
            return;
        }

        try {
            checkToken();

            // *** Delete floating ip from project

            HttpDelete delete = new HttpDelete(this.openstackNetworkUri + "/v2.0/floatingips/" + floatingip.id);
            delete.addHeader(this.openstackToken.getHeader());

            try (CloseableHttpResponse response = httpClient.execute(delete)) {
                StatusLine status = response.getStatusLine();
                EntityUtils.consume(response.getEntity());

                if (status.getStatusCode() != HttpStatus.SC_NO_CONTENT) {
                    throw new OpenstackManagerException("OpenStack delete floatingip failed - " + status);
                }
            }
            return;
        } catch (OpenstackManagerException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenstackManagerException("Unable to floatingip server ", e);
        }
    }

    public Floatingip getFloatingIp(String id) throws OpenstackManagerException {
        if (id == null) {
            return null;
        }

        try {
            checkToken();

            // *** Retrieve the floating ip

            HttpGet get = new HttpGet(this.openstackNetworkUri + "/v2.0/floatingips/" + id);
            get.addHeader(this.openstackToken.getHeader());

            try (CloseableHttpResponse response = httpClient.execute(get)) {
                StatusLine status = response.getStatusLine();
                String entity = EntityUtils.toString(response.getEntity());

                if (status.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                    return null;
                }

                if (status.getStatusCode() != HttpStatus.SC_OK) {
                    throw new OpenstackManagerException("OpenStack get floatingip failed - " + status);
                }

                FloatingipRequestResponse fipResponse = this.gson.fromJson(entity, FloatingipRequestResponse.class);
                if (fipResponse != null && fipResponse.floatingip != null) {
                    return fipResponse.floatingip;
                }
            }
            return null;
        } catch (OpenstackManagerException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenstackManagerException("Unable to get floatingip ", e);
        }
    }

    public Port retrievePort(@NotNull String deviceId) throws OpenstackManagerException {
        try {
            checkToken();

            // *** Retrieve all the ports and extract the correct one

            HttpGet get = new HttpGet(this.openstackNetworkUri + "/v2.0/ports");
            get.addHeader(this.openstackToken.getHeader());

            try (CloseableHttpResponse response = httpClient.execute(get)) {
                StatusLine status = response.getStatusLine();
                String entity = EntityUtils.toString(response.getEntity());

                if (status.getStatusCode() != HttpStatus.SC_OK) {
                    throw new OpenstackManagerException("OpenStack list port failed - " + status);
                }

                PortsResponse portsResponse = this.gson.fromJson(entity, PortsResponse.class);
                if (portsResponse != null && portsResponse.ports != null) {
                    for (Port port : portsResponse.ports) {
                        if (deviceId.equals(port.device_id)) {
                            return port;
                        }
                    }
                }
            }
            return null;
        } catch (OpenstackManagerException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenstackManagerException("Unable to retrieve the server port", e);
        }
    }

    public String retrieveServerPassword(@NotNull Server server) throws OpenstackManagerException {
        try {
            checkToken();

            // *** Retrieve all the ports and extract the correct one

            HttpGet get = new HttpGet(this.openstackComputeUri + "/servers/" + server.id + "/os-server-password");
            get.addHeader(this.openstackToken.getHeader());

            try (CloseableHttpResponse response = httpClient.execute(get)) {
                StatusLine status = response.getStatusLine();
                if (status.getStatusCode() != HttpStatus.SC_OK) {
                    throw new OpenstackManagerException("OpenStack list os password failed - " + status);
                }

            }
            return null;
        } catch (OpenstackManagerException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenstackManagerException("Unable to retrieve the server os password", e);
        }
    }

    protected String getImageId(@NotNull String image) throws OpenstackManagerException {
        try {
            checkToken();

            /* Attempt to retrieve the image ID we want from Openstack using the image name */
            String uri = this.openstackImageUri + "/v2/images?name=" + image;
            logger.trace("Attempting to get the image " + image + " from " + uri);
            HttpGet get = new HttpGet(uri);
            get.addHeader(this.openstackToken.getHeader());

            try (CloseableHttpResponse response = httpClient.execute(get)) {
                StatusLine status = response.getStatusLine();
                String entity = EntityUtils.toString(response.getEntity());

                if (status.getStatusCode() != HttpStatus.SC_OK) {
                    throw new OpenstackManagerException("OpenStack list image failed - " + status);
                }

                /* Even though we are searching by image name, the JSON returned is still an array of images */
                Images images = gson.fromJson(entity, Images.class);
                if (images != null & images.images != null) {
                    for (Image i :images.images){
                        if (i.name != null && i.name != ""){
                            if (image.equals(i.name)) {
                                logger.trace("Image " + image + " found");
                                return i.id;
                            }
                        }
                    }
                } 
            }
            logger.trace("Image " + image + " wasn't found in Openstack");
            return null;
        } catch (OpenstackManagerException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenstackManagerException("Unable to list image " + image, e);
        }
    }

    protected String getFlavourId(@NotNull String flavour) throws OpenstackManagerException {
        try {
            checkToken();

            // *** Retrieve a list of the flavours

            HttpGet get = new HttpGet(this.openstackComputeUri + "/flavors");
            get.addHeader(this.openstackToken.getHeader());

            try (CloseableHttpResponse response = httpClient.execute(get)) {
                StatusLine status = response.getStatusLine();
                String entity = EntityUtils.toString(response.getEntity());

                if (status.getStatusCode() != HttpStatus.SC_OK) {
                    throw new OpenstackManagerException("OpenStack list flavour failed - " + status);
                }

                Flavors flavours = gson.fromJson(entity, Flavors.class);
                if (flavours != null && flavours.flavors != null) {
                    for (Flavor f : flavours.flavors) {
                        if (f.name != null) {
                            if (flavour.equals(f.name)) {
                                return f.id;
                            }
                        }
                    }
                }
            }
            return null;
        } catch (OpenstackManagerException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenstackManagerException("Unable to list flavour " + flavour, e);
        }
    }

    public Floatingip allocateFloatingip(Port port, Network network) throws OpenstackManagerException {
        try {
            checkToken();

            Floatingip fip = new Floatingip();
            fip.port_id = port.id;
            fip.floating_network_id = network.id;
            fip.description = "galasa_run=" + this.framework.getTestRunName();

            FloatingipRequestResponse fipRequest = new FloatingipRequestResponse();
            fipRequest.floatingip = fip;

            // *** Allocate a floating ip

            HttpPost post = new HttpPost(this.openstackNetworkUri + "/v2.0/floatingips");
            post.addHeader(this.openstackToken.getHeader());
            post.setEntity(new StringEntity(this.gson.toJson(fipRequest), ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(post)) {
                StatusLine status = response.getStatusLine();
                String entity = EntityUtils.toString(response.getEntity());

                if (status.getStatusCode() != HttpStatus.SC_CREATED) {
                    throw new OpenstackManagerException("OpenStack create floating ip failed - " + status);
                }

                FloatingipRequestResponse fipResponse = this.gson.fromJson(entity, FloatingipRequestResponse.class);
                if (fipResponse != null && fipResponse.floatingip != null) {
                    return fipResponse.floatingip;
                }
            }
            return null;
        } catch (OpenstackManagerException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenstackManagerException("Unable to list floating ips ", e);
        }
    }

    public Network findExternalNetwork(String externalNetwork) throws OpenstackManagerException {

        try {
            checkToken();

            // *** Retrieve a list of the networks available and select one

            HttpGet get = new HttpGet(this.openstackNetworkUri + "/v2.0/networks");
            get.addHeader(this.openstackToken.getHeader());

            try (CloseableHttpResponse response = httpClient.execute(get)) {
                StatusLine status = response.getStatusLine();
                String entity = EntityUtils.toString(response.getEntity());

                if (status.getStatusCode() != HttpStatus.SC_OK) {
                    throw new OpenstackManagerException("OpenStack list networks failed - " + status);
                }

                Networks networks = this.gson.fromJson(entity, Networks.class);
                if (networks != null && networks.networks != null) {
                    for (Network network : networks.networks) {
                        if (externalNetwork != null && externalNetwork.equals(network.name)) {
                            return network;
                        }
                    }
                }

            }
            return null;
        } catch (OpenstackManagerException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenstackManagerException("Unable to list networks ", e);
        }
    }

}
