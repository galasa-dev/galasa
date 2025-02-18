/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts;

import javax.validation.constraints.NotNull;

import dev.galasa.ProductVersion;
import dev.galasa.cicsts.cicsresource.ICicsResource;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosfile.IZosUNIXFile;

public interface ICicsRegion {

    /***
     * Retrieve the CICS TS Region tag
     * @return the tag of the CICS TS Region
     */
    String getTag();

    /***
     * Retrieve the CICS TS Region applid
     * @return the applid of the CICS TS Region
     * @throws CicstsManagerException If the applid is not available
     */
    String getApplid();
    
    /***
     * Retrieve the CICS TS Region sysid
     * @return the sysid of the CICS TS Region
     * @throws CicstsManagerException If the applid is not available
     */
    String getSysid();

    /***
     * Retrieve the CICS TS Region version
     * @return the version of the CICS TS Region
     * @throws CicstsManagerException If the version is not available
     */
    ProductVersion getVersion() throws CicstsManagerException;
    
    /***
     * Retrieve the zOS Image the CICS TS region resides on
     * @return the zOS Image the CICS TS region resides on
     */
    IZosImage getZosImage();

    /**
     * Describes the type of CICS region
     * 
     * @return The type of CICS Region
     */
    MasType getMasType();
    
    //TODO
    ICemt cemt() throws CicstsManagerException;    
    ICeda ceda() throws CicstsManagerException;    
    ICeci ceci() throws CicstsManagerException;
    
    /**
     * Provides a CICS resource instance that can then be used to create a specific CICS resource 
     * @return a {@link ICicsResource} instance associated with this CICS region
     * @throws CicstsManagerException
     */
    public ICicsResource cicsResource() throws CicstsManagerException;
    
    void startup() throws CicstsManagerException;
    void shutdown() throws CicstsManagerException;
    
    boolean isProvisionStart();

    public String getUssHome() throws CicstsManagerException;

    public String getJvmProfileDir() throws CicstsManagerException;
    
	public String getJavaHome() throws CicstsManagerException;

    /**
     * Return the CICS region {@link IZosBatchJob}
     * @return the CICS region job
     * @throws CicstsManagerException
     */
	public IZosBatchJob getRegionJob() throws CicstsManagerException;
	
	/**
	 * 
	 * @return the Run Temporary UNIX Directory for this CICS Region
	 * @throws CicstsManagerException
	 */
	public IZosUNIXFile getRunTemporaryUNIXDirectory() throws CicstsManagerException;
	
	
	/**
	 * This method allows a SIT parameter to be altered,  but only if the provisioning tool allows it.
	 * The CICS TS Region must be down before this method is called.
	 * 
	 * @param sitParam - The SIT parameter to alter
	 * @param sitValue - The value to set,  null = delete parameter
	 * @throws CicstsManagerException - If the provisioning tool does not allow SIT modification or the CICS Regions is still up
	 */
	public void alterSit(@NotNull String sitParam, String sitValue) throws CicstsManagerException;

	/**
	 * This method removes a SIT parameter from CICS runtime JCL provided the provisioning tool allows.
	 * The CICS TS Region must be down before this method is called.
	 * 
	 * @param sitParam
	 * @throws CicstsManagerException
	 */
	public void removeSit(@NotNull String sitParam) throws CicstsManagerException;

    /**
     * Allows a testcase to get a specific property about the region.
     * 
     * The list of properties supported will depend upon how the region was 
     * deployed/provisioned.
     * 
     * By default, no properties are supported, but each implementation of this
     * interface is free to provide tests with whatever values they wish to 
     * reflect this particular CICS region.
     * 
     * @param propertyName The name of the property for which the caller wishes to 
     * get a value.
     * 
     * @return A string value for the requested property, or null if that property
     * value is not available.
     * 
     * @throws CicstsManagerException
     */
    public default String getRegionProperty( String propertyName ) throws CicstsManagerException {
        return null;
    }
}