/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile.zosmf.manager.internal;

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
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.ZosUNIXCommandException;

public class ZosmfZosDatasetAttributesListdsi {
    
	private ZosmfZosFileManagerImpl zosFileManager;
	private ZosmfZosFileHandlerImpl zosFileHandler;
    private IZosUNIXCommand unixCommand;
    private IZosImage image;
    private String execDatasetName;
    private ZosmfZosDatasetImpl execDataset;
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

    private static final String PROP_VOLSER = "volser";     
    private static final String PROP_UNIT = "unit";       
    private static final String PROP_DSORG = "dsorg";      
    private static final String PROP_ALCUNIT = "alcunit";    
    private static final String PROP_PRIMARY = "primary";    
    private static final String PROP_SECONDARY = "secondary";  
    private static final String PROP_DIRBLK = "dirblk";     
    private static final String PROP_RECFM = "recfm";      
    private static final String PROP_BLKSIZE = "blksize";    
    private static final String PROP_LRECL = "lrecl";      
    private static final String PROP_STORECLASS = "storeclass";
    private static final String PROP_MGNTCLASS = "mgntclass";
    private static final String PROP_DATACLASS = "dataclass";
    private static final String PROP_DSNTYPE = "dsntype";
    private static final String PROP_DSNAME = "dsname";
    private static final String PROP_EXTX = "extx";
    private static final String PROP_USED = "used";
    private static final String PROP_CDATE = "cdate";
    private static final String PROP_RDATE = "rdate";
    private static final String PROP_EDATE = "edate";
    
    private static final Log logger = LogFactory.getLog(ZosmfZosDatasetAttributesListdsi.class);
    
    public ZosmfZosDatasetAttributesListdsi(ZosmfZosFileManagerImpl zosFileManager, IZosImage image) {
    	this.zosFileManager = zosFileManager;
        this.image = image;
    }
    
    protected void initialise() throws ZosDatasetException {
        try {
            if (zosFileManager.getZosUnixCommandManager() == null) {
                throw new ZosDatasetException("zosFileManager zosUnixCommandManager is null");
            }
            this.unixCommand = zosFileManager.getZosUnixCommandManager().getZosUNIXCommand(image);
            this.zosFileHandler = (ZosmfZosFileHandlerImpl) zosFileManager.newZosFileHandler();
            this.execDatasetName = zosFileManager.getRunDatasetHLQ(this.image) + "." + zosFileManager.getRunId() + ".EXEC";
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
            datasetAttributes.addProperty(PROP_DSNAME, value);
        }
        
        value = listdsiJson.get(PROP_SYSVOLUME).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_VOLSER, value);
        }       
        
        value = listdsiJson.get(PROP_SYSUNIT).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_UNIT, value);
        } 
        
        value = listdsiJson.get(PROP_SYSDSORG).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_DSORG, value);
        }       
        
        value = listdsiJson.get(PROP_SYSUNITS).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_ALCUNIT, value);
        }
        
        value = listdsiJson.get(PROP_SYSCREATE).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_CDATE, formatDate(value));
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
            datasetAttributes.addProperty(PROP_DIRBLK, Integer.parseInt(value));
        }
        
        value = listdsiJson.get(PROP_SYSRECFM).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_RECFM, value);
        }
        
        value = listdsiJson.get(PROP_SYSBLKSIZE).getAsString();
        if (NumberUtils.isCreatable(value)) {
            datasetAttributes.addProperty(PROP_BLKSIZE, Integer.parseInt(value));
        }
        
        value = listdsiJson.get(PROP_SYSLRECL).getAsString();
        if (NumberUtils.isCreatable(value)) {
            datasetAttributes.addProperty(PROP_LRECL, Integer.parseInt(value));
        }
        
        value = listdsiJson.get(PROP_SYSSTORCLASS).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_STORECLASS, value);
        }
        
        value = listdsiJson.get(PROP_SYSMGMTCLASS).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_MGNTCLASS, value);
        }
        
        value = listdsiJson.get(PROP_SYSDATACLASS).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_DATACLASS, value);
        }
        
        value = listdsiJson.get(PROP_SYSDSSMS).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_DSNTYPE, value);
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
            datasetAttributes.addProperty(PROP_EXTX, Integer.parseInt(value));
        }
        
        value = listdsiJson.get(PROP_SYSEXDATE).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_EDATE, formatDate(value));
        }
        
        value = listdsiJson.get(PROP_SYSREFDATE).getAsString();
        if (!value.isEmpty()) {
            datasetAttributes.addProperty(PROP_RDATE, formatDate(value));
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
        String command = "tsocmd \"EXEC '" + execDatasetName + "(" + LISTDSI_EXEC_NAME + ")' '" + dsname + "'\" 2>/dev/null;echo RC=$?";
        String tsocmdRc;
        try {
            tsocmdRc = this.unixCommand.issueCommand(command);
        } catch (ZosUNIXCommandException e) {
            throw new ZosDatasetException("Problem issuing zOS UNIX command", e);
        }
        if (!tsocmdRc.startsWith("RC=0")) {
            throw new ZosDatasetException("Unable to get data set attibutes: " + tsocmdRc);
        }
        String json = execDataset.memberRetrieveAsText("JSON");
        logger.debug("LISTDSI JSON:\n" + json);
        return new JsonParser().parse(json).getAsJsonObject();
    }

    protected void createExecDataset() throws ZosDatasetException {
        this.execDataset = (ZosmfZosDatasetImpl) this.zosFileHandler.newDataset(this.execDatasetName, this.image);
        if (!this.execDataset.exists()) {
            execDataset.setDatasetOrganization(DatasetOrganization.PARTITIONED);
            execDataset.setDatasetType(DSType.LIBRARY);
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
