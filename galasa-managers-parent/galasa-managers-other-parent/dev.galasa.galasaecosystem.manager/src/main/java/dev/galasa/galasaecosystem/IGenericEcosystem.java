/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.galasaecosystem;

import java.util.Properties;

import javax.validation.constraints.NotNull;

import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;

/**
 * Generic Ecosystem TPI
 * 
 * Provides a means to manipulate the ecosystem
 *  
 *  
 *
 */
public interface IGenericEcosystem {
           
    /**
     * @param endpoint {@link EcosystemEndpoint} Which endpoint is required
     * @return Object of the endpoint, never null, URL, URI or InetSocketAddress
     * @throws GalasaEcosystemManagerException If the endpoint is unsupported
     */
    public @NotNull Object getEndpoint(@NotNull EcosystemEndpoint endpoint) throws GalasaEcosystemManagerException;
    
    /**
     * Retrieve a CPS property from the host ecosystem
     * 
     * @param namespace The namespace of the CPS
     * @return the value of the property or null if not found
     * @throws GalasaEcosystemManagerException if there is a problem accessing the CPS
     */
    public String getHostCpsProperty(@NotNull String namespace, @NotNull String prefix, @NotNull String suffix, String... infixes) throws GalasaEcosystemManagerException;
 
    /**
     * Retrieve a CPS property
     * 
     * @param property The property to retrieve
     * @return the value of the property or null if not found
     * @throws GalasaEcosystemManagerException if there is a problem accessing the CPS
     */
    public String getCpsProperty(@NotNull String property) throws GalasaEcosystemManagerException;
    
    /**
     * Set a CPS property
     * 
     * @param property The property to set
     * @param value The value to set, null means delete
     * @throws GalasaEcosystemManagerException if there is a problem accessing the CPS
     */
    public void setCpsProperty(@NotNull String property, String value)  throws GalasaEcosystemManagerException;
    
    /**
     * Retrieve a DSS property
     * 
     * @param property The property to retrieve
     * @return the value of the property or null if not found
     * @throws GalasaEcosystemManagerException if there is a problem accessing the DSS
     */
    public String getDssProperty(@NotNull String property) throws GalasaEcosystemManagerException;
    
    /**
     * Set a DSS property
     * 
     * @param property The property to set
     * @param value The value to set, null means delete
     * @throws GalasaEcosystemManagerException if there is a problem accessing the DSS
     */
    public void setDssProperty(@NotNull String property, String value)  throws GalasaEcosystemManagerException;
    
    /**
     * Retrieve a CREDS property
     * 
     * @param property The property to retrieve
     * @return the value of the property or null if not found
     * @throws GalasaEcosystemManagerException if there is a problem accessing the CREDS
     */
    public String getCredsProperty(@NotNull String property) throws GalasaEcosystemManagerException;
    
    /**
     * Set a CREDS property
     * 
     * @param property The property to set
     * @param value The value to set, null means delete
     * @throws GalasaEcosystemManagerException if there is a problem accessing the CREDS
     */
    public void setCredsProperty(@NotNull String property, String value)  throws GalasaEcosystemManagerException;
    
    public String submitRun(String runType,
                          String requestor,
                          String groupName,
                          @NotNull String bundleName,
                          @NotNull String testName,
                          String mavenRepository,
                          String obr,
                          String stream,
                          Properties overrides) throws GalasaEcosystemManagerException;
    
    public JsonObject waitForRun(String run) throws GalasaEcosystemManagerException;
    public JsonObject waitForRun(String run, int minutes) throws GalasaEcosystemManagerException;

    public void addZosImageToCpsAsDefault(@NotNull IZosImage image) throws GalasaEcosystemManagerException;
    public void addZosImageToCps(@NotNull IZosImage image) throws GalasaEcosystemManagerException;
    public void setZosImageDseTag(@NotNull String tag, @NotNull IZosImage image) throws GalasaEcosystemManagerException;
    public void setZosClusterImages(@NotNull String cluserId, @NotNull IZosImage... images)  throws GalasaEcosystemManagerException;
}