/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.ivts.zos;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.logging.Log;

import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.Test;
import dev.galasa.artifact.BundleResources;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.core.manager.CoreManager;
import dev.galasa.core.manager.ICoreManager;
import dev.galasa.core.manager.Logger;
import dev.galasa.core.manager.TestProperty;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosImage;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.ZosBatch;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.IZosVSAMDataset;
import dev.galasa.zosfile.IZosVSAMDataset.VSAMSpaceUnit;
import dev.galasa.zosfile.ZosFileHandler;
import dev.galasa.zosfile.ZosVSAMDatasetException;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.ZosUNIXCommand;

@Test
public class ZosManagerFileVSAMIVT {
    
    private final String IMG_TAG = "PRIMARY";
    
    @Logger
    public Log logger;
    
    @ZosImage(imageTag =  IMG_TAG)
    public IZosImage imagePrimary;
        
    @ZosFileHandler
    public IZosFileHandler fileHandler;
    
    @ZosBatch(imageTag = "PRIMARY")
    public IZosBatch batch;

    @BundleResources
    public IBundleResources resources; 
    
    @CoreManager
    public ICoreManager coreManager;
    
    @ZosUNIXCommand(imageTag = IMG_TAG)
    public IZosUNIXCommand zosUNIXCommand;
    
    @TestProperty(prefix = "IVT.RUN",suffix = "NAME", required = false)
    public String providedRunName;
    
    private String runName  = new String();

    @Test
    public void preFlightTests() throws Exception {
        // Ensure we have the resources we need for testing
        assertThat(imagePrimary).isNotNull();
        assertThat(fileHandler).isNotNull();
        assertThat(coreManager).isNotNull();
        assertThat(resources).isNotNull();
        assertThat(logger).isNotNull();
        assertThat(imagePrimary.getDefaultCredentials()).isNotNull();
        runName = coreManager.getRunName();
        logger.info("Using Run ID of: " + runName);
    }
    
    /**
     * Very Basic test method to create and delete a KSDS file
     * Using IDCAMS to check our working
     * @throws ZosVSAMDatasetException
     * @throws ZosBatchException
     * @throws TestBundleResourceException
     * @throws IOException
     */
    @Test
    public void basicKSDSDefine() throws ZosVSAMDatasetException, ZosBatchException, TestBundleResourceException, IOException {
    	String DSName = "CTS.GALASA." + runName + ".KSDS";
    	IZosVSAMDataset vsamDataSet = fileHandler.newVSAMDataset(DSName, imagePrimary);
    	vsamDataSet.setSpace(VSAMSpaceUnit.CYLINDERS, 1, 1);
    	vsamDataSet.setRecordSize(50, 101);

        if (checkThatPDSExists(DSName)) {
            logger.info("Dataset " + DSName + " already exists. Deleting...");
            vsamDataSet.delete();
            logger.info("Dataset " + DSName + " deleted OK.");
        }
        assertThat(checkThatPDSExists(DSName)).isFalse();
        
    	vsamDataSet.create();
    	
    	assertThat(checkThatPDSExists(DSName)).isTrue();
    	assertThat(vsamDataSet.exists()).isTrue();
    	
    	vsamDataSet.delete();
    	
    	assertThat(checkThatPDSExists(DSName)).isFalse();
    	assertThat(vsamDataSet.exists()).isFalse();
    }
    
    /**
     * Run some JCL to check that a PDS has been created
     * This is used to test that zosFile is working correctly
     * @param dataset The name of the dataset that we want to test
     * @return a boolean to signify that the dataset exists (or not)
     * @throws TestBundleResourceException
     * @throws IOException
     * @throws ZosBatchException
     */
    private boolean checkThatPDSExists(String dataset) throws TestBundleResourceException, IOException, ZosBatchException {
    	HashMap<String,Object> parms = new HashMap<>();
    	parms.put("DATASET", dataset);
    	String jcl = resources.retrieveSkeletonFileAsString("/jcl/PDSCheck.jcl", parms);
    	return(batch.submitJob(jcl, null).waitForJob() == 0);
    }
}
