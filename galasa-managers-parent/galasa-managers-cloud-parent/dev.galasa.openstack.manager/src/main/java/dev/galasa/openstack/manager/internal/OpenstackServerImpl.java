/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openstack.manager.internal;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ICredentials;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DssDelete;
import dev.galasa.framework.spi.DssSwap;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreMatchException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.openstack.manager.OpenstackManagerException;
import dev.galasa.openstack.manager.internal.json.Floatingip;
import dev.galasa.openstack.manager.internal.json.Network;
import dev.galasa.openstack.manager.internal.json.Port;
import dev.galasa.openstack.manager.internal.json.Server;
import dev.galasa.openstack.manager.internal.json.ServerRequest;
import dev.galasa.openstack.manager.internal.properties.BuildTimeout;
import dev.galasa.openstack.manager.internal.properties.OpenStackNetworkName;

public abstract class OpenstackServerImpl {

    private final static Log logger = LogFactory.getLog(OpenstackServerImpl.class);
    
    private final String osType;
    public final OpenstackManagerImpl manager;
    private final OpenstackHttpClient openstackHttpClient;
    public final String               instanceName;
    public final String               image;
    public final String               tag;
    
    private String                    id;

    private String                    hostname;

    private Server                    openstackServer;
    private Port                      openstackPort;
    private Floatingip                openstackFloatingip;

    private OpenstackIpHost           ipHost;

    private ICommandShell             commandShell;

    protected OpenstackServerImpl(@NotNull String osType, @NotNull OpenstackManagerImpl manager,
            @NotNull OpenstackHttpClient openstackHttpClient, @NotNull String instanceName, @NotNull String image,
            @NotNull String tag) {
        this.osType = osType;
        this.manager = manager;
        this.openstackHttpClient = openstackHttpClient;
        this.instanceName = instanceName;
        this.image = image;
        this.tag = tag;
    }

    public static void deleteServerByName(String serverName, String runName, IDynamicStatusStoreService dss,
            OpenstackHttpClient openstackHttpClient) throws OpenstackManagerException {

        // *** Need to locate the id of the server before we can delete it

        Server server = openstackHttpClient.findServerByName(serverName);

        // *** Now delete the server, doesn't matter if the id is null as we want the
        // DSS stuff deleted anyway

        deleteServer(server, serverName, runName, dss, openstackHttpClient);
    }

    public static void deleteServer(Server server, String serverName, String runName, IDynamicStatusStoreService dss,
            OpenstackHttpClient openstackHttpClient) throws OpenstackManagerException {

        if (server != null && server.id != null) {
            openstackHttpClient.deleteServer(server);

            Instant expire = Instant.now();
            expire = expire.plus(1, ChronoUnit.MINUTES); // TODO cps
            boolean deleted = false;
            while (expire.compareTo(Instant.now()) >= 0) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Wait for server delete interrupted", e);
                    return;
                }
                Server deletingServer = openstackHttpClient.getServer(server.id);
                if (deletingServer == null) {
                    deleted = true;
                    break;
                }
            }

            if (!deleted) {
                logger.warn("Failed to delete an OpenStack compute server in time - " + serverName + "/" + server.id);
                return;
            }

            logger.info("Successfully deleted OpenStack compute server " + serverName + "/" + server.id);
        }

        try {
            freeServerFromDss(serverName, runName, dss);
        } catch (Exception e) {
            logger.error("Cleanup of DSS failed", e);
        }
    }

    private static void freeServerFromDss(String serverName, String runName, IDynamicStatusStoreService dss)
            throws DynamicStatusStoreException, InterruptedException {
        String currentInstances = dss.get("server.current.compute.instances");

        int usedInstances = 0;
        if (currentInstances != null) {
            usedInstances = Integer.parseInt(currentInstances);
        }
        usedInstances--;
        if (usedInstances < 0) {
            usedInstances = 0;
        }

        // *** Remove the userview set
        // TODO create userview set

        // *** Remove the control set
        DssSwap slotNumber = new DssSwap("server.current.compute.instances", currentInstances, Integer.toString(usedInstances));

        DssDelete computeName   = new DssDelete(serverName, null);
        DssDelete runAllocation = new DssDelete("run." + runName + "." + serverName, null);
        
        try {
            dss.performActions(slotNumber, computeName, runAllocation);
        } catch(DynamicStatusStoreMatchException e) {
            //*** collision on either the slot increment or the instance name,  so simply retry
            Thread.sleep(200 + new SecureRandom().nextInt(200)); // *** To avoid race conditions
            freeServerFromDss(serverName, runName, dss);
            return;
        }
    }

    public static void deleteFloatingIpByName(String fipName, String runName, IDynamicStatusStoreService dss,
            OpenstackHttpClient openstackHttpClient) throws OpenstackManagerException {

        // *** Need to locate the id of the floatingup before we can delete it

        Floatingip fip = openstackHttpClient.findFloatingIpByName(fipName);

        // *** Now delete the floatingip, doesn't matter if the id is null as we want
        // the DSS stuff deleted anyway

        deleteFloatingIp(fip, fipName, runName, dss, openstackHttpClient);
    }

    public static void deleteFloatingIp(Floatingip floatingip, String fipName, String runName,
            IDynamicStatusStoreService dss, OpenstackHttpClient openstackHttpClient) throws OpenstackManagerException {

        if (floatingip != null && floatingip.id != null) {
            openstackHttpClient.deleteFloatingIp(floatingip);

            Instant expire = Instant.now();
            expire = expire.plus(1, ChronoUnit.MINUTES); // TODO cps
            boolean deleted = false;
            while (expire.compareTo(Instant.now()) >= 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Wait for server delete interrupted", e);
                    return;
                }
                Floatingip deletingFip = openstackHttpClient.getFloatingIp(floatingip.id);
                if (deletingFip == null) {
                    deleted = true;
                    break;
                }
            }

            if (!deleted) {
                logger.warn("Failed to delete an OpenStack floatingip in time - " + fipName + "/" + floatingip.id);
                return;
            }

            logger.info("Successfully deleted OpenStack floatingip " + fipName + "/" + floatingip.id);
        }

        try {
            freeFloatingipFromDss(fipName, runName, dss);
        } catch (Exception e) {
            logger.error("Cleanup of DSS failed", e);
        }
    }

    private static void freeFloatingipFromDss(String fipName, String runName, IDynamicStatusStoreService dss)
            throws DynamicStatusStoreException, InterruptedException {
        // *** Remove the userview set
        // TODO create userview set

        // *** Remove the control set
        String fipSub = fipName.replaceAll("\\.", "_");
        String prefix = "floatingip." + fipSub;

        // *** Clear the DSS for this run completely
        HashSet<String> deleteProperties = new HashSet<>();
        deleteProperties.add("run." + runName + "." + prefix);
        deleteProperties.add(prefix);
        dss.delete(deleteProperties);
    }

    protected static void registerFloatingIp(IDynamicStatusStoreService dss, String runName, Floatingip floatingIp)
            throws DynamicStatusStoreException {

        String fip = floatingIp.floating_ip_address.replaceAll("\\.", "_");

        String prefix = "floatingip." + fip;

        HashMap<String, String> fipProperties = new HashMap<>();
        fipProperties.put("run." + runName + "." + prefix, "active");
        fipProperties.put(prefix, runName);
        dss.put(fipProperties);
    }
    
    public void discard() {
        try {
            // *** delete the instance in Openstack
            if (this.openstackServer != null) {
                try {
                    deleteServer(this.openstackServer, this.openstackServer.name,
                            this.manager.getFramework().getTestRunName(), this.manager.getDSS(),
                            this.openstackHttpClient);
                } catch (Exception e) {
                    logger.warn("Failed to delete the server", e);
                }
            }

            // *** Delete the Floating IP
            if (this.openstackFloatingip != null) {
                try {
                    deleteFloatingIp(this.openstackFloatingip, this.openstackFloatingip.floating_ip_address,
                            this.manager.getFramework().getTestRunName(), this.manager.getDSS(),
                            this.openstackHttpClient);
                } catch (Exception e) {
                    logger.warn("Failed to delete the floating ip", e);
                }
            }
            logger.info("OpenStack " + this.osType + " instance " + this.instanceName + " for tag " + tag + " has been discarded");
        } catch (Exception e) {
            logger.warn("Unable to discard OpenStack " + this.osType + " instance " + this.instanceName, e);
        }
    }
    
    protected void createServer(ServerRequest serverRequest) throws OpenstackManagerException {
        try {
            this.openstackServer = this.openstackHttpClient.createServer(serverRequest);
            this.id = this.openstackServer.id;

            Instant expire = Instant.now();
            expire = expire.plus(BuildTimeout.get(), ChronoUnit.MINUTES);
            Instant poll = Instant.now().plus(30, ChronoUnit.SECONDS);

            String serverJson = "";
            String state = null;
            boolean up = false;
            while (expire.compareTo(Instant.now()) > 0) {
                Thread.sleep(5000);

                Server checkServer = this.openstackHttpClient.getServer(this.id);
                if (checkServer != null) {
                    serverJson = this.manager.getGson().toJson(checkServer);
                    if (checkServer.power_state != null) {
                        if (checkServer.power_state == 1) {
                            logger.info("OpenStack " + this.osType + " instance " + this.instanceName
                                    + " has been built and is running, compute server id = " + this.openstackServer.id);
                            this.openstackServer = checkServer;
                            up = true;
                            break;
                        }
                        state = checkServer.task_state;
                    }
                }

                if (logger.isTraceEnabled()) {
                    logger.trace("Still waiting for OpenStack " + this.osType + " instance " + this.instanceName + " to be built, task="
                            + state);
                } else {
                    if (Instant.now().isAfter(poll)) {
                        logger.debug("Still waiting for OpenStack " + this.osType + " instance " + this.instanceName + " to be built, task="
                                + state);
                        poll = Instant.now().plus(30, ChronoUnit.SECONDS);
                    }
                }
            }

            if (!up) {
                throw new OpenstackManagerException(
                        "OpenStack failed to build the server in time, last response was:-\n" + serverJson);
            }

            // *** Get the network port details
            this.openstackPort = this.openstackHttpClient.retrievePort(this.openstackServer.id);
            if (this.openstackPort == null) {
                throw new OpenstackManagerException("OpenStack did not allocate a port for this instance");
            }

            // *** Locate the external network
            String networkName = OpenStackNetworkName.get();
            if (networkName == null) {
                throw new OpenstackManagerException("The external network name to allocate a floatingip on was not provided in the CPS");
            }
            Network network = this.openstackHttpClient.findExternalNetwork(networkName);
            if (network == null) {
                throw new OpenstackManagerException("Unable to select an external network to allocate a floatingip on");
            }

            // *** Assign a floating IPv4 address
            this.openstackFloatingip = this.openstackHttpClient.allocateFloatingip(this.openstackPort, network);
            logger.info("OpenStack " + this.osType + " Server " + this.instanceName + " assigned IP address "
                    + this.openstackFloatingip.floating_ip_address);

            // *** Create the DSS properties to manager the Floating IP Address
            registerFloatingIp(this.manager.getDSS(), this.manager.getFramework().getTestRunName(),
                    this.openstackFloatingip);

            // *** Default hostname to the floatingip
            this.hostname = this.openstackFloatingip.floating_ip_address;

            // *** Create the IPHost
            this.ipHost = new OpenstackIpHost(this.hostname, getServerCredentials());

            // *** Create the Commandshell
            this.commandShell = this.manager.getIpNetworkManager().getCommandShell(this.ipHost,
                    this.ipHost.getDefaultCredentials());
        } catch (OpenstackManagerException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenstackManagerException("Unable to start OpenStack " + this.osType + " server", e);
        }
    }
    
    public @NotNull IIpHost getIpHost() {
        return this.ipHost;
    }
    
    public String getImage() {
        return this.image;
    }
    
    protected OpenstackHttpClient getOpenstackHttpClient() {
        return this.openstackHttpClient;
    }
    
    protected ICommandShell getServerCommandShell() {
        return this.commandShell;
    }
    
    protected abstract void build() throws OpenstackManagerException, ConfigurationPropertyStoreException;

    protected abstract ICredentials getServerCredentials() throws OpenstackManagerException;

}
