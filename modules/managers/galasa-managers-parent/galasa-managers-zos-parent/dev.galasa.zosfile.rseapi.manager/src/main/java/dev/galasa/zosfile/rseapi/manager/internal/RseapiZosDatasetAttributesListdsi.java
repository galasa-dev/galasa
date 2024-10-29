/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile.rseapi.manager.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.FrameworkUtil;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.IZosDataset.DSType;
import dev.galasa.zosfile.IZosDataset.DatasetOrganization;
import dev.galasa.zosfile.IZosDataset.RecordFormat;
import dev.galasa.zosfile.IZosDataset.SpaceUnit;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosrseapi.IRseapiRestApiProcessor;

public class RseapiZosDatasetAttributesListdsi {
	private RseapiZosFileHandlerImpl zosFileHandler;
	private IRseapiRestApiProcessor rseapiApiProcessor;
    private IZosImage image;
    private String execDatasetName;
    private RseapiZosDatasetImpl execDataset;
    private boolean initialised;
    
    private static final String LISTDSI_EXEC_NAME = "LISTDSI";

    private static final String PROP_LISTDSIRC    = "listdsirc";
    private static final String PROP_SYSDSNAME    = "sysdsname";    
    private static final String PROP_SYSVOLUME    = "sysvolume";    
    private static final String PROP_SYSUNIT      = "sysunit";        
    private static final String PROP_SYSDSORG     = "sysdsorg";      
    private static final String PROP_SYSRECFM     = "sysrecfm";      
    private static final String PROP_SYSLRECL     = "syslrecl";      
    private static final String PROP_SYSBLKSIZE   = "sysblksize";  
    private static final String PROP_SYSKEYLEN    = "syskeylen";    
    private static final String PROP_SYSALLOC     = "sysalloc";      
    private static final String PROP_SYSUSED      = "sysused";
    private static final String PROP_SYSUSEDPAGES = "sysusedpages";
    private static final String PROP_SYSPRIMARY   = "sysprimary";  
    private static final String PROP_SYSSECONDS   = "sysseconds";  
    private static final String PROP_SYSUNITS     = "sysunits";      
    private static final String PROP_SYSEXTENTS   = "sysextents";  
    private static final String PROP_SYSCREATE    = "syscreate";    
    private static final String PROP_SYSREFDATE   = "sysrefdate";  
    private static final String PROP_SYSEXDATE    = "sysexdate";    
    private static final String PROP_SYSPASSWORD  = "syspassword"; //Not a password but a pointer to a password //pragma: allowlist secret
    private static final String PROP_SYSRACFA     = "sysracfa";      
    private static final String PROP_SYSUPDATED   = "sysupdated";  
    private static final String PROP_SYSTRKSCYL   = "systrkscyl";  
    private static final String PROP_SYSBLKSTRK   = "sysblkstrk";  
    private static final String PROP_SYSADIRBLK   = "sysadirblk";  
    private static final String PROP_SYSUDIRBLK   = "sysudirblk";  
    private static final String PROP_SYSMEMBERS   = "sysmembers";         
    private static final String PROP_SYSREASON    = "sysreason";
    private static final String PROP_SYSMSGLVL1   = "sysmsglvl1";  
    private static final String PROP_SYSMSGLVL2   = "sysmsglvl2";  
    private static final String PROP_SYSDSSMS     = "sysdssms";  
    private static final String PROP_SYSDATACLASS = "sysdataclass";  
    private static final String PROP_SYSSTORCLASS = "sysstorclass";  
    private static final String PROP_SYSMGMTCLASS = "sysmgmtclass";

    private static final String PROP_VOLUME_SERIAL = "volumeSerial";     
    private static final String PROP_UNIT = "unit";
    private static final String PROP_DATASET_ORGANIZATION = "dataSetOrganization";      
    private static final String PROP_ALLOCATION_UNIT = "allocationUnit";    
    private static final String PROP_PRIMARY = "primary";    
    private static final String PROP_SECONDARY = "secondary";  
    private static final String PROP_DIRECTORY_BLOCKS = "directoryBlocks";     
    private static final String PROP_RECORD_FORMAT = "recordFormat";      
    private static final String PROP_BLOCK_SIZE = "blockSize";    
    private static final String PROP_RECORD_LENGTH = "recordLength";      
    private static final String PROP_STOR_CLASS = "storClass";
    private static final String PROP_MGMT_CLASS = "mgmtClass";
    private static final String PROP_DATA_CLASS = "dataClass";
    private static final String PROP_DSN_TYPE = "dsnType";
    private static final String PROP_NAME = "name";
    private static final String PROP_EXTENTS = "extents";
    private static final String PROP_USED = "used";
    private static final String PROP_CREATION_DATE = "creationDate";
    private static final String PROP_REFERENCE_DATE = "referenceDate";
    private static final String PROP_EXPIRY_DATE = "expiryDate";
    
    private static final String PROP_OUTPUT = "output";
    private static final String PROP_STDOUT = "stdout";
    private static final String PROP_STDERR = "stderr";
    
    private static final Log logger = LogFactory.getLog(RseapiZosDatasetAttributesListdsi.class);
    
    public RseapiZosDatasetAttributesListdsi(RseapiZosFileHandlerImpl zosFileHandler, IRseapiRestApiProcessor rseapiApiProcessor, IZosImage image) {
        this.zosFileHandler = zosFileHandler;
        this.rseapiApiProcessor = rseapiApiProcessor;
        this.image = image;
	}

	protected void initialise() throws ZosDatasetException {
        try {
            this.execDatasetName = this.zosFileHandler.getZosFileManager().getRunDatasetHLQ(this.image) + "." + this.zosFileHandler.getZosFileManager().getRunId() + ".EXEC";
            createExecDataset();
            initialised = true;
        } catch (ZosFileManagerException e) {
            throw new ZosDatasetException("Unable to create LISTDSI EXEC command", e);
        }
    }

    public JsonObject get(String dsname) throws ZosDatasetException {
        if (!initialised) {
            initialise();
        }
        JsonObject listdsiJson = execListdsi(dsname);
        
        if (listdsiJson.get(PROP_LISTDSIRC) == null) {
            throw new ZosDatasetException("Invalid JSON object returend from LISTDSI:\n" + listdsiJson);
        }
        
        JsonObject datasetAttributes = new JsonObject();
        
        String value = listdsiJson.get(PROP_LISTDSIRC).getAsString();
        if (NumberUtils.isCreatable(value)) {
            datasetAttributes.addProperty(PROP_LISTDSIRC, Integer.parseInt(value));
        }
        
        value = listdsiJson.get(PROP_SYSREASON).getAsString();
        if (NumberUtils.isCreatable(value)) {
            datasetAttributes.addProperty(PROP_SYSREASON, Integer.parseInt(value));
        }
        
        value = listdsiJson.get(PROP_SYSMSGLVL1).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_SYSMSGLVL1, value);
        }
        
        value = listdsiJson.get(PROP_SYSMSGLVL2).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_SYSMSGLVL2, value);
        }
        
        value = listdsiJson.get(PROP_SYSDSNAME).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_NAME, value);
        }
        
        value = listdsiJson.get(PROP_SYSVOLUME).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_VOLUME_SERIAL, value);
        }       
        
        value = listdsiJson.get(PROP_SYSUNIT).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_UNIT, value);
        } 
        
        value = listdsiJson.get(PROP_SYSDSORG).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_DATASET_ORGANIZATION, value);
        }       
        
        value = listdsiJson.get(PROP_SYSUNITS).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_ALLOCATION_UNIT, value);
        }
        
        value = listdsiJson.get(PROP_SYSCREATE).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_CREATION_DATE, formatDate(value));
        }
        
        value = listdsiJson.get(PROP_SYSPRIMARY).getAsString();
        if (NumberUtils.isCreatable(value)) {
            datasetAttributes.addProperty(PROP_PRIMARY, Integer.parseInt(value));
        }
        
        value = listdsiJson.get(PROP_SYSSECONDS).getAsString();
        if (NumberUtils.isCreatable(value)) {
            datasetAttributes.addProperty(PROP_SECONDARY, Integer.parseInt(value));
        }
        
        value = listdsiJson.get(PROP_SYSADIRBLK).getAsString();
        if (NumberUtils.isCreatable(value)) {
            datasetAttributes.addProperty(PROP_DIRECTORY_BLOCKS, Integer.parseInt(value));
        }
        
        value = listdsiJson.get(PROP_SYSRECFM).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_RECORD_FORMAT, value);
        }
        
        value = listdsiJson.get(PROP_SYSBLKSIZE).getAsString();
        if (NumberUtils.isCreatable(value)) {
            datasetAttributes.addProperty(PROP_BLOCK_SIZE, Integer.parseInt(value));
        }
        
        value = listdsiJson.get(PROP_SYSLRECL).getAsString();
        if (NumberUtils.isCreatable(value)) {
            datasetAttributes.addProperty(PROP_RECORD_LENGTH, Integer.parseInt(value));
        }
        
        value = listdsiJson.get(PROP_SYSSTORCLASS).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_STOR_CLASS, value);
        }
        
        value = listdsiJson.get(PROP_SYSMGMTCLASS).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_MGMT_CLASS, value);
        }
        
        value = listdsiJson.get(PROP_SYSDATACLASS).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_DATA_CLASS, value);
        }
        
        value = listdsiJson.get(PROP_SYSDSSMS).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_DSN_TYPE, value);
        }
        
        value = listdsiJson.get(PROP_SYSUSED).getAsString();
        if (NumberUtils.isCreatable(value)) {
            datasetAttributes.addProperty(PROP_USED, Integer.parseInt(value));
        } else {
            value = listdsiJson.get(PROP_SYSUSEDPAGES).getAsString();
            if (NumberUtils.isCreatable(value)) {
                datasetAttributes.addProperty(PROP_USED, Integer.parseInt(value));
            }    
        }
        
        value = listdsiJson.get(PROP_SYSEXTENTS).getAsString();
        if (NumberUtils.isCreatable(value)) {
            datasetAttributes.addProperty(PROP_EXTENTS, Integer.parseInt(value));
        }
        
        value = listdsiJson.get(PROP_SYSEXDATE).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_EXPIRY_DATE, formatDate(value));
        }
        
        value = listdsiJson.get(PROP_SYSREFDATE).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_REFERENCE_DATE, formatDate(value));
        }
        
        // Not currently used
        value = listdsiJson.get(PROP_SYSKEYLEN).getAsString();
        if (NumberUtils.isCreatable(value)) {
            datasetAttributes.addProperty(PROP_SYSKEYLEN, Integer.parseInt(value));
        }

        value = listdsiJson.get(PROP_SYSALLOC).getAsString();
        if (NumberUtils.isCreatable(value)) {
            datasetAttributes.addProperty(PROP_SYSALLOC, Integer.parseInt(value));
        }

        value = listdsiJson.get(PROP_SYSPASSWORD).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_SYSPASSWORD, value);
        }

        value = listdsiJson.get(PROP_SYSRACFA).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_SYSRACFA, value);
        }

        value = listdsiJson.get(PROP_SYSUPDATED).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_SYSUPDATED, value);
        }

        value = listdsiJson.get(PROP_SYSTRKSCYL).getAsString();
        if (NumberUtils.isCreatable(value)) {
            datasetAttributes.addProperty(PROP_SYSTRKSCYL, Integer.parseInt(value));
        }

        value = listdsiJson.get(PROP_SYSBLKSTRK).getAsString();
        if (NumberUtils.isCreatable(value)) {
            datasetAttributes.addProperty(PROP_SYSBLKSTRK, Integer.parseInt(value));
        }

        value = listdsiJson.get(PROP_SYSUDIRBLK).getAsString();
        if (NumberUtils.isCreatable(value)) {
            datasetAttributes.addProperty(PROP_SYSUDIRBLK, Integer.parseInt(value));
        }

        value = listdsiJson.get(PROP_SYSMEMBERS).getAsString();
        if (NumberUtils.isCreatable(value)) {
            datasetAttributes.addProperty(PROP_SYSMEMBERS, Integer.parseInt(value));
        }

        return datasetAttributes;
    }
    
    protected String formatDate(String value) {
        String date = "***None***";
        try {
            date = LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy/DDD")).format(DateTimeFormatter.ofPattern("yyy/MM/dd"));
        } catch (Exception e) {
            // NOOP
        }
        return date;
    }

    protected JsonObject execListdsi(String dsname) throws ZosDatasetException {
		String tsocmd = "EXEC '" + execDatasetName  + "(" + LISTDSI_EXEC_NAME  + ")' '" + dsname + "'";
        String command = "tsocmd \"" + tsocmd + "\";echo RC=$?";

    	RseapiZosUnixCommand zosUnixCommand = new RseapiZosUnixCommand(this.zosFileHandler);
    	JsonObject responseBody;
		try {
			responseBody = zosUnixCommand.execute(rseapiApiProcessor, command);
		} catch (ZosFileManagerException e) {
			throw new ZosDatasetException(e);
		}
        
        logger.trace(responseBody);
    	String errorMessage = "Unable to get data set attibutes. Response body:\n" + responseBody;
        if (!getOutputProperty(responseBody, PROP_STDOUT).equals("RC=0")) {
            logger.error(errorMessage);
            throw new ZosDatasetException(errorMessage);
        }

        if (!getOutputProperty(responseBody, PROP_STDERR).trim().equals(tsocmd)) {
        	errorMessage = "Unable to get data set attibutes - stdout should contain the issued command. Response body:\n" + responseBody;
            logger.error(errorMessage);
            throw new ZosDatasetException(errorMessage);
        }        
        
        String json = execDataset.memberRetrieveAsText("JSON");
        logger.debug("LISTDSI JSON:\n" + json);
        
        return new JsonParser().parse(json).getAsJsonObject();
    }

    protected String getOutputProperty(JsonObject responseBody, String memberName) {
    	String propertyValue = "UNKNOWN";
    	JsonObject output = responseBody.getAsJsonObject(PROP_OUTPUT);
    	if (output !=  null && output.get(memberName) != null) {
			propertyValue = output.get(memberName).getAsString();
    	}
    	return propertyValue;
	}

	protected void createExecDataset() throws ZosDatasetException {
        this.execDataset = (RseapiZosDatasetImpl) this.zosFileHandler.newDataset(this.execDatasetName, this.image);
        if (!this.execDataset.exists()) {
            execDataset.setDatasetOrganization(DatasetOrganization.PARTITIONED);
            execDataset.setDatasetType(DSType.LIBRARY);
            execDataset.setDirectoryBlocks(1);
            execDataset.setRecordFormat(RecordFormat.VARIABLE_BLOCKED);
            execDataset.setRecordlength(255);
            execDataset.setBlockSize(32720);
            execDataset.setSpace(SpaceUnit.TRACKS, 1, 1);
            execDataset.create();
            execDataset.setShouldArchive(false);
        }
        if (!this.execDataset.memberExists(LISTDSI_EXEC_NAME)) {
            execDataset.memberStoreText(LISTDSI_EXEC_NAME, getExecResource());
        }
    }

    protected String getExecResource() throws ZosDatasetException {
        try {            
            URL url = FrameworkUtil.getBundle(this.getClass()).getResource("resources/" + LISTDSI_EXEC_NAME + ".rexx");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
            StringBuilder execContent = new StringBuilder();
            while(bufferedReader.ready()) {
                execContent.append(bufferedReader.readLine());
                execContent.append("\n");
            }
            bufferedReader.close();
            return execContent.toString();
        } catch (IOException e) {
            throw new ZosDatasetException("Unable to get LISTDSI EXEC resource from Manager Bundle", e);
        }
    }

}
