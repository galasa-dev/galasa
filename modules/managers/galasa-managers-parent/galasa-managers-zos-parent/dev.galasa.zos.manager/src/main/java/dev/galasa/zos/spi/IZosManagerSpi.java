/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.spi;

import java.nio.file.Path;
import java.util.HashMap;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;
import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.IZosManager;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosbatch.spi.IZosBatchJobOutputSpi;
import dev.galasa.zosconsole.ZosConsoleManagerException;
import dev.galasa.zosfile.ZosFileManagerException;

public interface IZosManagerSpi extends IZosManager {
    
    /**
     * Returns a zOS Image for the specified tag, if necessary provisions it
     * @param tag the tag of the image
     * @return and image, never null
     * @throws ZosManagerException if the tag is missing
     */
    @NotNull
    IZosImage provisionImageForTag(@NotNull String tag) throws ZosManagerException;

    /**
     * Returns a zOS Image for the specified tag
     * @param tag the tag of the image
     * @return and image, never null
     * @throws ZosManagerException if the tag is missing
     */
    @NotNull
    IZosImage getImageForTag(@NotNull String tag) throws ZosManagerException;

    /**
     * Returns a zOS Image for the specified image ID
     * @param imageId the ID of the image
     * @return the image, never null
     * @throws ZosManagerException
     */
    @NotNull
    IZosImage getImage(String imageId) throws ZosManagerException;

    /**
     * Returns a zOS Image for the specified image that may not have been provisioned so far
     * @param imageId the ID of the image
     * @return the image, never null
     * @throws ZosManagerException if there is no image defined
     */
    @NotNull
    IZosImage getUnmanagedImage(String imageId) throws ZosManagerException;

    /**
     * Returns the data set HLQ(s) for temporary data sets for the specified image
     * @param image
     * @return the image, never null
     * @throws ZosManagerException
     */
    @NotNull
    String getRunDatasetHLQ(@NotNull IZosImage image) throws ZosManagerException;

    /**
     * Returns the zOS UNIX path prefix for temporary file for the specified image
     * @param image
     * @return the image, never null
     * @throws ZosManagerException
     */
    @NotNull
    String getRunUNIXPathPrefix(@NotNull IZosImage image) throws ZosManagerException;
    
    /**
     * Provides other managers to the zOS Batch {@code zosbatch.batchjob.[imageid].restrict.to.image} property
     * @param imageId
     * @return
     * @throws ZosBatchManagerException
     */
	boolean getZosBatchPropertyBatchRestrictToImage(String imageId) throws ZosBatchManagerException;

	/**
	 * Provides other managers to the zOS Batch {@code zosbatch.batchjob.[imageid].use.sysaff} property
	 * @param imageId
	 * @return
	 * @throws ZosBatchManagerException
	 */
	boolean getZosBatchPropertyUseSysaff(String imageId) throws ZosBatchManagerException;
	
	/**
	 * Provides other managers to the zOS Batch {@code zosbatch.batchjob.[imageid].timeout} property
	 * @param imageId
	 * @return
	 * @throws ZosBatchManagerException
	 */
	int getZosBatchPropertyJobWaitTimeout(String imageId) throws ZosBatchManagerException;

	/**
	 * Provides other managers to the zOS Batch {@code zosbatch.batchjob.[imageid].truncate.jcl.records} property
	 * @param imageId
	 * @return
	 * @throws ZosBatchManagerException
	 */
	boolean getZosBatchPropertyTruncateJCLRecords(String imageId) throws ZosBatchManagerException;

	/**
	 * Provides other managers a {@link IZosBatchJobname} with a prefix defined by the zOS Batch {@code zosbatch.jobname.[imageid].prefix} property
	 * @param image
	 * @return
	 * @throws ZosBatchException
	 */
	IZosBatchJobname newZosBatchJobname(IZosImage image) throws ZosBatchException;

	/**
	 * Provides other managers a {@link IZosBatchJobname} with a the supplied name
	 * @param name
	 * @return
	 * @throws ZosBatchException
	 */
	IZosBatchJobname newZosBatchJobname(String name) throws ZosBatchException;

	/**
	 * Create a new batch job output object
	 * @param batchJob
	 * @param name
	 * @param jobid
	 * @return
	 */
	IZosBatchJobOutputSpi newZosBatchJobOutput(IZosBatchJob batchJob, String name, String jobid);
    
    /**
     * Create a new zOS Batch job spool file object  
     * @param batchJob
     * @param jobname
     * @param jobid
     * @param stepname
     * @param procstep
     * @param ddname
     * @param id
     * @param records
     * @return
     * @throws ZosBatchException
     */
    public IZosBatchJobOutputSpoolFile newZosBatchJobOutputSpoolFile(IZosBatchJob batchJob, String jobname, String jobid, String stepname, String procstep, String ddname, String id, String records) throws ZosBatchException;

	/**
	 * Build a unique results archive artifact name 
	 * @param artifactPath
	 * @param name
	 * @return
	 */
	String buildUniquePathName(Path artifactPath, String name);

	/**
	 * Store an artifact in the results archive on behalf of another manager
	 * @param artifactPath
	 * @param content
	 * @param type
	 */
	void storeArtifact(Path artifactPath, String content, ResultArchiveStoreContentType type) throws ZosManagerException;

	/**
	 * Create an empty dirictory in the results archive on behalf of another manager
	 * @param artifactPath
	 * @throws ZosManagerException
	 */
	void createArtifactDirectory(Path artifactPath) throws ZosManagerException;
	
    /**
     * Provides other managers to the zOS File {@code zosfile.unix.[imageid].directory.list.max.items} property
     * @param imageId
     * @return
     * @throws ZosFileManagerException
     */
	int getZosFilePropertyDirectoryListMaxItems(String imageId) throws ZosFileManagerException;
	
    /**
     * Provides other managers to the zOS File {@code zosfile.batchjob.[imageid].restrict.to.image} property
     * @param imageId
     * @return
     * @throws ZosFileManagerException
     */
	boolean getZosFilePropertyFileRestrictToImage(String imageId) throws ZosFileManagerException;
	
    /**
     * Provides other managers to the zOS File {@code zosfile.[imageid].unix.file.permission} property
     * @param imageId
     * @return
     * @throws ZosFileManagerException
     */
	String getZosFilePropertyUnixFilePermissions(String imageId) throws ZosFileManagerException;
	
    /**
     * Provides other managers to the zOS Console {@code zosconsole.console.[imageid].restrict.to.image} property
     * @param imageId
     * @return
     * @throws ZosConsoleManagerException
     */
	boolean getZosConsolePropertyConsoleRestrictToImage(String imageId) throws ZosConsoleManagerException;
	
	/**
	 * Returns the credentials on the specified image id
	 * @param credentialsId
	 * @param imageId
	 * @return
	 * @throws ZosManagerException
	 */
	ICredentials getCredentials(String credentialsId, String imageId) throws ZosManagerException;
	
	/**
	 * Returns the provisioned z/OS ports which are tagged with a string value
	 * @return Map of tags to ports
	 */
	HashMap<String, String> getTaggedPorts();
}
