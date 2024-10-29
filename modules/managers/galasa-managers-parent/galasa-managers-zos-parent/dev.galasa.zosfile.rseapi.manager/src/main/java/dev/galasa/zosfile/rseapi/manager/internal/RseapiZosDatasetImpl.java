/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile.rseapi.manager.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosfile.IZosDataset;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosrseapi.IRseapi.RseapiRequestType;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.IRseapiRestApiProcessor;
import dev.galasa.zosrseapi.RseapiException;
import dev.galasa.zosrseapi.RseapiManagerException;

public class RseapiZosDatasetImpl implements IZosDataset {
    
    private IRseapiRestApiProcessor rseapiApiProcessor;

    // zOS Image
    private IZosImage image;

    private static final String SLASH = "/";
    private static final String COMMA = ",";
    private static final String RESTFILES_DATASET_PATH = SLASH + "rseapi" + SLASH + "api" + SLASH + "v1" + SLASH + "datasets";
    private static final String RESTFILES_DATASET_PATH_CONTENT = SLASH + "content";
    private static final String RESTFILES_DATASET_PATH_RAW_CONTENT = SLASH + "rawContent";
    private static final String RESTFILES_DATASET_PATH_MEMBERS = SLASH + "members";
    private static final String RESTFILES_DATASET_LIST = SLASH + "list";
    
    // data set and member names
    private String dsname;
    private String memberName;

	private boolean datasetCreated = false;
    private boolean convert = true;
    
    private Collection<String> datasetMembers;

    // parameters
    private String volser = null;
    private String unit = null;
    private DatasetOrganization dsorg = null;
    private SpaceUnit alcunit = null;
    private int primary = -1;
    private int secondary = -1;
    private int dirblk = -1;
    private int avgblk = -1;
    private RecordFormat recfm = null;
    private int blksize = -1;
    private int lrecl = -1;
    private String storeclass = null;
    private String mgntclass = null;
    private String dataclass = null;
    private DSType dstype = null;
    private int extents = -1;
    private int used = -1;
    private String createDate = null;
    private String referencedDate = null;
    private String expirationDate = null;

    private DatasetDataType dataType = DatasetDataType.TEXT;

    private boolean shouldArchive = false;

    private boolean shouldCleanup = true;

    private RseapiZosDatasetAttributesListdsi rseapiZosDatasetAttributesListdsi;

    private Path testMethodArchiveFolder;

    private RseapiZosFileHandlerImpl zosFileHandler;

	public RseapiZosFileHandlerImpl getZosFileHandler() {
        return zosFileHandler;
    }

    private static final String PROP_NAME = "name";
    private static final String PROP_VOLUME_SERIAL = "volumeSerial";     
    private static final String PROP_UNIT = "unit";
    private static final String PROP_DATASET_ORGANIZATION = "dataSetOrganization";
    private static final String PROP_ALLOCATION_UNIT = "allocationUnit";
    private static final String PROP_PRIMARY = "primary";
    private static final String PROP_SECONDARY = "secondary";
    private static final String PROP_DIRECTORY_BLOCKS = "directoryBlocks";
    private static final String PROP_AVERAGE_BLOCK = "averageBlock";
    private static final String PROP_RECORD_FORMAT = "recordFormat";
    private static final String PROP_BLOCK_SIZE = "blockSize";
    private static final String PROP_RECORD_LENGTH = "recordLength";
    private static final String PROP_DSN_TYPE = "dsnType";
	private static final String PROP_DATA_SET_ORGANIZATION = "dataSetOrganization";     
    private static final String PROP_STOR_CLASS = "storageClass";
    private static final String PROP_MGMT_CLASS = "managementClass";
    private static final String PROP_DATA_CLASS = "dataClass";
	private static final String PROP_EXTENTS = "extents";
	private static final String PROP_CREATION_DATE = "creationDate";
	private static final String PROP_REFERENCE_DATE = "referenceDate";
	private static final String PROP_EXPIRY_DATE = "expiryDate";
	private static final String PROP_USED = "used";
	
	private static final String PROP_RECORDS = "records";
    private static final String PROP_ITEMS = "items";
    
    private static final String PROP_LISTDSIRC    = "listdsirc";
    private static final String PROP_SYSREASON    = "sysreason";
    private static final String PROP_SYSMSGLVL1   = "sysmsglvl1";  
    private static final String PROP_SYSMSGLVL2   = "sysmsglvl2";
    
    private static final String LOG_DATA_SET = "Data set ";
    private static final String LOG_MEMBER = "Member ";
    private static final String LOG_DOES_NOT_EXIST = " does not exist";
    private static final String LOG_ARCHIVED_TO = " archived to ";
    private static final String LOG_NOT_PDS = " is not a partitioned data set";
    private static final String LOG_CONTENT_MUST_NOT_BE_NULL = "content must not be null";
    private static final String LOG_MEMBER_NAME_MUST_NOT_BE_NULL = "member name must not be null";

    public static final Log logger = LogFactory.getLog(RseapiZosDatasetImpl.class);

	private static final String BINARY_HEADER = "binary";

    public RseapiZosDatasetImpl(RseapiZosFileHandlerImpl zosFileHandler, IZosImage image, String dsname) throws ZosDatasetException {
        this.zosFileHandler = zosFileHandler;
        this.image = image;
        splitDSN(dsname);
        this.testMethodArchiveFolder = this.zosFileHandler.getZosFileManager().getDatasetCurrentTestMethodArchiveFolder();
        
        try {
            this.rseapiApiProcessor = this.zosFileHandler.getRseapiManager().newRseapiRestApiProcessor(this.image, this.zosFileHandler.getZosManager().getZosFilePropertyFileRestrictToImage(image.getImageID()));
        } catch (ZosFileManagerException | RseapiManagerException e) {
            throw new ZosDatasetException(e);
        }
    }

    @Override
    public IZosDataset create() throws ZosDatasetException {
        if (exists()) {
            throw new ZosDatasetException(LOG_DATA_SET + quoted(this.dsname) + " already exists" + logOnImage());
        }
        
        JsonObject requestBody = new JsonObject();
        requestBody = addPropertyWhenSet(requestBody, PROP_NAME, this.dsname);
        requestBody = addPropertyWhenSet(requestBody, PROP_VOLUME_SERIAL, this.volser);
        requestBody = addPropertyWhenSet(requestBody, PROP_DATASET_ORGANIZATION, this.dsorg);
        requestBody = addPropertyWhenSet(requestBody, PROP_ALLOCATION_UNIT, this.alcunit != null? alcunit.name().replaceFirst("S$",""): null);
        requestBody = addPropertyWhenSet(requestBody, PROP_PRIMARY, this.primary);
        requestBody = addPropertyWhenSet(requestBody, PROP_SECONDARY, this.secondary);
        // RSE API bug requires dir > 0 for PDSE (LIBRARY)
        requestBody = addPropertyWhenSet(requestBody, PROP_DIRECTORY_BLOCKS, (this.dstype == DSType.LIBRARY || this.dstype == DSType.PDSE) && this.dirblk == -1? 1 : this.dirblk);
        requestBody = addPropertyWhenSet(requestBody, PROP_AVERAGE_BLOCK, this.avgblk);
        requestBody = addPropertyWhenSet(requestBody, PROP_RECORD_FORMAT, this.recfm);
        requestBody = addPropertyWhenSet(requestBody, PROP_BLOCK_SIZE, this.blksize);
        requestBody = addPropertyWhenSet(requestBody, PROP_RECORD_LENGTH, this.lrecl);
        requestBody = addPropertyWhenSet(requestBody, PROP_DSN_TYPE, this.dstype == DSType.PDSE? DSType.LIBRARY : this.dstype);
        requestBody = addPropertyWhenSet(requestBody, PROP_STOR_CLASS, this.storeclass);
        requestBody = addPropertyWhenSet(requestBody, PROP_MGMT_CLASS, this.mgntclass);
        requestBody = addPropertyWhenSet(requestBody, PROP_DATA_CLASS, this.dataclass);
        
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.POST_JSON, RESTFILES_DATASET_PATH, null, requestBody, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, this.convert);
        } catch (RseapiException e) {
            throw new ZosDatasetException(e);
        }

        if (response.getStatusCode() != HttpStatus.SC_CREATED) {
        	// Error case
            String displayMessage = this.zosFileHandler.buildErrorString("Create data set", response); 
            logger.error(displayMessage);
            throw new ZosDatasetException(displayMessage);
        }
        
        if (exists()) {
            logger.info(LOG_DATA_SET + quoted(this.dsname) + " created" + logOnImage());
            this.datasetCreated = true;
        } else {
            logger.warn(LOG_DATA_SET + quoted(this.dsname) + " not created" + logOnImage());
        }
        return this;
    }

    @Override
    public boolean delete() throws ZosDatasetException {
        if (!exists()) {
            throw new ZosDatasetException(quoted(this.dsname) + LOG_DOES_NOT_EXIST + logOnImage());
        }
        
        delete(this.dsname);
        
        if (exists()) {
            logger.info(LOG_DATA_SET + quoted(this.dsname) + " not deleted" + logOnImage());
            return false;
        } else {
            logger.info(LOG_DATA_SET + quoted(this.dsname) + " deleted" + logOnImage());
            return true;
        }
    }

    @Override
    public boolean exists() throws ZosDatasetException {
        String urlPath = RESTFILES_DATASET_PATH + SLASH + this.dsname;
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.GET, urlPath, null, null, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosDatasetException(e);
        }

        if (response.getStatusCode() != HttpStatus.SC_OK) {
        	// Error case
            String displayMessage = this.zosFileHandler.buildErrorString("List data set", response); 
            logger.error(displayMessage);
            throw new ZosDatasetException(displayMessage);
        }
        
        JsonObject responseBody;
        try {
            responseBody = response.getJsonContent();
        } catch (RseapiException e) {
            throw new ZosDatasetException("Unable to list data set " + quoted(this.dsname) + logOnImage(), e);
        }
        
        logger.trace(responseBody);
        JsonArray items = responseBody.getAsJsonArray(PROP_ITEMS);
        if (items != null && items.size() > 0) {
	        JsonObject item = items.get(0).getAsJsonObject();
	        String dsnameValue = item.get(PROP_NAME).getAsString();
	        if (this.dsname.equals(dsnameValue)) {
	            logger.trace(LOG_DATA_SET + quoted(this.dsname) + " exists" + logOnImage());
	            return true;
	        }
        }
    
        logger.trace(LOG_DATA_SET + quoted(this.dsname) + LOG_DOES_NOT_EXIST + logOnImage());
        return false;
    }

    @Override
    public void storeText(@NotNull String content) throws ZosDatasetException {
    	Objects.requireNonNull(content, LOG_CONTENT_MUST_NOT_BE_NULL);
        if (isPDS()) {
            throw new ZosDatasetException(LOG_DATA_SET + quoted(this.dsname) + " is a partitioned data set. Use memberStore(String memberName, String content) method instead");
        }
        storeText(content, null, this.convert);
    }

    @Override
    public void storeBinary(@NotNull byte[] content) throws ZosDatasetException {
    	Objects.requireNonNull(content, LOG_CONTENT_MUST_NOT_BE_NULL);
        if (isPDS()) {
            throw new ZosDatasetException(LOG_DATA_SET + quoted(this.dsname) + " is a partitioned data set. Use memberStore(String memberName, String content) method instead");
        }
        storeBinary(content, null, this.convert);
    }

    @Override
    public String retrieveAsText() throws ZosDatasetException {
        if (isPDS()) {
            throw new ZosDatasetException(LOG_DATA_SET + quoted(this.dsname) + " is a partitioned data set. Use retrieve(String memberName) method instead");
        }
        Object content = retrieve(null);
        if (content instanceof byte[]) {
            return new String((byte[]) content);
        } else if (content instanceof InputStream) {
            return new String(inputStreamToByteArray((InputStream) content));
        }
        return (String) content;
    }

    @Override
    public byte[] retrieveAsBinary() throws ZosDatasetException {
        if (isPDS()) {
            throw new ZosDatasetException(LOG_DATA_SET + quoted(this.dsname) + " is a partitioned data set. Use retrieve(String memberName) method instead");
        }
        Object content = retrieve(null);
        if (content instanceof String) {
            return ((String) content).getBytes();
        } else if (content instanceof InputStream) {
            return inputStreamToByteArray((InputStream) content);
        }
        return (byte[]) content;
    }
    
    @Override
    public void saveToResultsArchive(String rasPath) throws ZosDatasetException {
        try {
            if (exists()) {
                if (isPDS()) {
                    savePDSToResultsArchive(rasPath);
                } else {
                    Path artifactPath = this.zosFileHandler.getArtifactsRoot().resolve(rasPath);
            		logger.info("Archiving " + quoted(this.dsname) + " to " + artifactPath.toString());
                    try {
                    	if (this.dataType.equals(DatasetDataType.TEXT)) {
                    		this.zosFileHandler.getZosManager().storeArtifact(artifactPath, retrieveAsText(), ResultArchiveStoreContentType.TEXT);
                    	} else  {
                    		this.zosFileHandler.getZosManager().storeArtifact(artifactPath, new String(retrieveAsBinary()), ResultArchiveStoreContentType.TEXT);
                    	}
        			} catch (ZosManagerException e) {
        				throw new ZosDatasetException(e);
        			}
                }
            }
        } catch (ZosFileManagerException e) {
            logger.error("Unable to save data set to archive", e);
        }
    }

    @Override
    public boolean isPDS() throws ZosDatasetException {
        return emptyStringWhenNull(getAttibutes(), PROP_DATA_SET_ORGANIZATION).startsWith("PO");
    }

    @Override
    public void memberCreate(@NotNull String memberName) throws ZosDatasetException {
    	Objects.requireNonNull(memberName, LOG_MEMBER_NAME_MUST_NOT_BE_NULL);
        if (!isPDS()) {
            throw new ZosDatasetException(LOG_DATA_SET + quoted(this.dsname) + LOG_NOT_PDS);
        }
        String emptyFileName;
        IZosUNIXFile emptyFile;
		try {
			emptyFileName = this.image.getRunTemporaryUNIXPath() + SLASH + "/emptyFile";
			emptyFile = zosFileHandler.getZosFileManager().newZosFileHandler().newUNIXFile(emptyFileName, image);
			emptyFile.setShouldArchive(false);
			if (!emptyFile.exists()) {
				emptyFile.create();
			}			
		} catch (ZosManagerException e) {
			throw new ZosDatasetException("Unable to create member " + memberName, e);
		}
    	String command = "> " + emptyFileName + ";cp " + emptyFileName + " \"//'" + joinDSN(memberName) +"'\"";
    	RseapiZosUnixCommand zosUnixCommand = new RseapiZosUnixCommand(this.zosFileHandler);
    	try {
			zosUnixCommand.execute(rseapiApiProcessor, command);
		} catch (ZosFileManagerException e) {
			throw new ZosDatasetException(e);
		}
        if (memberExists(memberName)) {
            logger.info(LOG_MEMBER + memberName + " created in " + LOG_DATA_SET + quoted(this.dsname) + logOnImage());
        } else {
            logger.info(LOG_MEMBER + memberName + " not created in " + LOG_DATA_SET + quoted(this.dsname) + logOnImage());
        }
    }

    @Override
    public void memberDelete(@NotNull String memberName) throws ZosDatasetException {
    	Objects.requireNonNull(memberName, LOG_MEMBER_NAME_MUST_NOT_BE_NULL);
        if (!isPDS()) {
            throw new ZosDatasetException(LOG_DATA_SET + quoted(this.dsname) + LOG_NOT_PDS);
        }
        if (!exists()) {
            logger.info(LOG_DATA_SET + quoted(this.dsname) + LOG_DOES_NOT_EXIST + logOnImage());
            return;
        }

        if (!memberExists(memberName)) {
            logger.info(LOG_MEMBER + memberName + LOG_DOES_NOT_EXIST + "in " + LOG_DATA_SET + quoted(this.dsname) + logOnImage());
            return;
        }
        
        delete(joinDSN(memberName));
        
        if (memberExists(memberName)) {
            logger.info(LOG_MEMBER + memberName + " not deleted from data set " + quoted(this.dsname) + logOnImage());
        } else {
            logger.info(LOG_MEMBER + memberName + " deleted from data set " + quoted(this.dsname) + logOnImage());
        }
    }

    @Override
    public boolean memberExists(@NotNull String memberName) throws ZosDatasetException {
    	Objects.requireNonNull(memberName, LOG_MEMBER_NAME_MUST_NOT_BE_NULL);
        if (!isPDS()) {
            throw new ZosDatasetException(LOG_DATA_SET + quoted(this.dsname) + LOG_NOT_PDS);
        }
        memberList();
        if (datasetMembers.contains(memberName)) {
        	logger.trace("Data set member " + quoted(joinDSN(memberName)) + " exists" + logOnImage());
        	return true;
        }
    
        logger.trace("Data set member " + quoted(joinDSN(memberName)) + LOG_DOES_NOT_EXIST + logOnImage());
        return false;
    }

    @Override
    public void memberStoreText(@NotNull String memberName, @NotNull String content) throws ZosDatasetException {
    	Objects.requireNonNull(memberName, LOG_MEMBER_NAME_MUST_NOT_BE_NULL);
    	Objects.requireNonNull(content, LOG_CONTENT_MUST_NOT_BE_NULL);
        if (!isPDS()) {
            throw new ZosDatasetException(LOG_DATA_SET + quoted(this.dsname) + LOG_NOT_PDS);
        }
        storeText(content, memberName, true);
    }

    @Override
    public void memberStoreBinary(@NotNull String memberName, @NotNull byte[] content) throws ZosDatasetException {
    	Objects.requireNonNull(memberName, LOG_MEMBER_NAME_MUST_NOT_BE_NULL);
    	Objects.requireNonNull(content, LOG_CONTENT_MUST_NOT_BE_NULL);
        if (!isPDS()) {
            throw new ZosDatasetException(LOG_DATA_SET + quoted(this.dsname) + LOG_NOT_PDS);
        }
        storeBinary(content, memberName, false);
    }

    @Override
    public String memberRetrieveAsText(@NotNull String memberName) throws ZosDatasetException {
    	Objects.requireNonNull(memberName, LOG_MEMBER_NAME_MUST_NOT_BE_NULL);
        if (!isPDS()) {
            throw new ZosDatasetException(LOG_DATA_SET + quoted(this.dsname) + LOG_NOT_PDS);
        }
        Object content = retrieve(memberName);
        if (content instanceof byte[]) {
            return new String((byte[]) content);
        } else if (content instanceof InputStream) {
            return new String(inputStreamToByteArray((InputStream) content));
        }
        return (String) content;
    }

    @Override
    public byte[] memberRetrieveAsBinary(@NotNull String memberName) throws ZosDatasetException {
    	Objects.requireNonNull(memberName, LOG_MEMBER_NAME_MUST_NOT_BE_NULL);
        if (!isPDS()) {
            throw new ZosDatasetException(LOG_DATA_SET + quoted(this.dsname) + LOG_NOT_PDS);
        }
        Object content = retrieve(memberName);
        if (content instanceof String) {
            return ((String) content).getBytes();
        } else if (content instanceof InputStream) {
            return inputStreamToByteArray((InputStream) content);
        }
        return (byte[]) content;
    }

    @Override
    public Collection<String> memberList() throws ZosDatasetException {
        if (!isPDS()) {
            throw new ZosDatasetException(LOG_DATA_SET + quoted(this.dsname) + LOG_NOT_PDS);
        }
        this.datasetMembers = new ArrayList<>();
        
        String urlPath = RESTFILES_DATASET_PATH + SLASH + this.dsname + RESTFILES_DATASET_PATH_MEMBERS;
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.GET, urlPath, null, null, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosDatasetException(e);
        }
        
        if (response.getStatusCode() != HttpStatus.SC_OK && response.getStatusCode() != HttpStatus.SC_NOT_FOUND) {
        	// Error case
            String displayMessage = this.zosFileHandler.buildErrorString("List data set members", response); 
            logger.error(displayMessage);
            throw new ZosDatasetException(displayMessage);
        }
        
        if (response.getStatusCode() != HttpStatus.SC_NOT_FOUND) {        
	        JsonObject responseBody;
	        try {
	            responseBody = response.getJsonContent();
	        } catch (RseapiException e) {
	            throw new ZosDatasetException("Unable to retrieve member list of data set " + quoted(this.dsname) + logOnImage(), e);
	        }
	        
	        logger.trace(responseBody);
	        JsonArray items = responseBody.getAsJsonArray(PROP_ITEMS);
	        for (JsonElement member : items) {
	        	datasetMembers.add(member.getAsString());
	        }
        }
        
        logger.trace("List of members of data set " + quoted(this.dsname) + "  retrieved from  image " + this.image.getImageID());

        return this.datasetMembers;
    }

    @Override
    public void memberSaveToResultsArchive(@NotNull String memberName, String rasPath) throws ZosDatasetException {
    	Objects.requireNonNull(memberName, LOG_MEMBER_NAME_MUST_NOT_BE_NULL);
        if (!isPDS()) {
            throw new ZosDatasetException(LOG_DATA_SET + quoted(this.dsname) + LOG_NOT_PDS);
        }
        try {
            try {
            	Path artifactPath = this.zosFileHandler.getArtifactsRoot().resolve(rasPath);
        		logger.info("Archiving " + quoted(this.dsname) + " to " + artifactPath.toString());
                if (this.dataType.equals(DatasetDataType.TEXT)) {
            		this.zosFileHandler.getZosManager().storeArtifact(artifactPath, memberRetrieveAsText(memberName), ResultArchiveStoreContentType.TEXT);
            	} else  {
            		this.zosFileHandler.getZosManager().storeArtifact(artifactPath, new String(memberRetrieveAsBinary(memberName)), ResultArchiveStoreContentType.TEXT);
            	}
			} catch (ZosManagerException e) {
				throw new ZosDatasetException(e);
			}
        } catch (ZosFileManagerException e) {
            logger.error("Unable to save data set member to archive", e);
        }
    }

    @Override
	public String getMemberName() {
		return this.memberName;
	}

	@Override
    public void setDataType(DatasetDataType dataType) {
        String dType = dataType.toString();
        if (BINARY_HEADER.equals(dType)){
            this.convert = false;
        }
        logger.info("Data type set to " + dType);
        this.dataType = dataType;
    }

    @Override
    public void setUnit(String unit) {
        this.unit = unit;       
    }

    @Override
    public void setVolumes(String volumes) {
        this.volser = volumes;
    }

    @Override
    public void setDatasetOrganization(DatasetOrganization organization) {
        this.dsorg = organization;
    }

    @Override
    public void setSpace(SpaceUnit spaceUnit, int primaryExtents, int secondaryExtents) {
        // Singular value rather than plural
        this.alcunit = spaceUnit;
        this.primary = primaryExtents;
        this.secondary = secondaryExtents;
    }

    @Override
    public void setDirectoryBlocks(int directoryBlocks) {
        this.dirblk = directoryBlocks;
    }

    @Override
    public void setRecordFormat(RecordFormat recordFormat) {
        this.recfm = recordFormat;
    }

    @Override
    public void setBlockSize(int blockSize) {
        this.blksize = blockSize;
    }

    @Override
    public void setRecordlength(int recordlength) {
        this.lrecl = recordlength;
    }

    @Override
    public void setManagementClass(String managementClass) {
        this.mgntclass = managementClass;
    }

    @Override
    public void setStorageClass(String storageClass) {
        this.storeclass = storageClass;
    }

    @Override
    public void setDataClass(String dataClass) {
        this.dataclass = dataClass;
    }

    @Override
    public void setDatasetType(DSType dsType) {
        this.dstype = dsType;
    }

    @Override
    public String getName() {
        return this.dsname;
    }

    @Override
    public DatasetDataType getDataType() {
        return this.dataType;
    }

    @Override
    public String getUnit() {
        return this.unit;
    }

    @Override
    public String getVolumes() {
        return this.volser;
    }

    @Override
    public DatasetOrganization getDatasetOrganization() {
        return this.dsorg;
    }

    @Override
    public SpaceUnit getSpaceUnit() {
        return this.alcunit;
    }

    @Override
    public int getPrimaryExtents() {
        return this.primary;
    }

    @Override
    public int getSecondaryExtents() {
        return this.secondary;
    }

    @Override
    public int getDirectoryBlocks() {
        return this.dirblk;
    }

    @Override
    public RecordFormat getRecordFormat() {
        return this.recfm;
    }

    @Override
    public int getBlockSize() {
        return this.blksize;
    }

    @Override
    public int getRecordlength() {
        return this.lrecl;
    }

    @Override
    public String getManagementClass() {
        return this.mgntclass;
    }

    @Override
    public String getStorageClass() {
        return this.storeclass;
    }

    @Override
    public String getDataClass() {
        return this.dataclass;
    }

    @Override
    public DSType getDatasetType() {
        return dstype;
    }
    
    @Override
    public int getExtents() {
        return this.extents;
    }
    
    @Override
    public int getUsed() {
        return this.used;
    }

    @Override
    public String getReferencedDate() {
        return this.referencedDate;
    }

    @Override
    public String getExpirationDate() {
        return this.expirationDate;
    }

    @Override
    public String getCreateDate() {
        return this.createDate;
    }

    @Override
    public void retrieveAttibutes() throws ZosDatasetException {
        if (this.rseapiZosDatasetAttributesListdsi == null) {
            this.rseapiZosDatasetAttributesListdsi = new RseapiZosDatasetAttributesListdsi(this.zosFileHandler, this.rseapiApiProcessor, this.image);
        }
        JsonObject datasteAttributes = rseapiZosDatasetAttributesListdsi.get(this.dsname);
        
        int listdsiRc = datasteAttributes.get(PROP_LISTDSIRC).getAsInt();
        JsonElement value;
        if (listdsiRc != 0) {
            value = datasteAttributes.get(PROP_SYSREASON);
            int sysreason = -1;
            if (value != null) {
                sysreason = value.getAsInt();
            }
            String sysmsglvl1 = emptyStringWhenNull(datasteAttributes, PROP_SYSMSGLVL1);
            String sysmsglvl2 = emptyStringWhenNull(datasteAttributes, PROP_SYSMSGLVL2);
            String message;
            if (listdsiRc == 4) {
                message = "Unable to get full attributes for data set " + quoted(this.dsname) + ". LISTDSI RC=" + listdsiRc +
                        "\nSYSREASON=" + sysreason +
                        "\nSYSMSGLVL1: " + sysmsglvl1 +
                        "\nSYSMSGLVL2: " + sysmsglvl2;
                logger.warn(message);
            } else {
                message = "Unable to get attributes for data set " + quoted(this.dsname) + ". LISTDSI RC=" + listdsiRc + 
                        "\nSYSREASON=" + sysreason + 
                        "\nSYSMSGLVL1: " + sysmsglvl1 + 
                        "\nSYSMSGLVL2: " + sysmsglvl2;
                throw new ZosDatasetException(message);
            }
        }
        
        setAttributes(datasteAttributes);
    }

    @Override
    public String getAttibutesAsString() throws ZosDatasetException {
        if (!exists()) {
            throw new ZosDatasetException(LOG_DATA_SET + quoted(this.dsname) + LOG_DOES_NOT_EXIST + logOnImage());
        }
        StringBuilder attributes = new StringBuilder();
        JsonObject jsonObject = getAttibutes();
        attributes.append("Data Set Name=");
        attributes.append(emptyStringWhenNull(jsonObject, PROP_NAME));
        attributes.append(COMMA);
        attributes.append("Volume serial=");
        attributes.append(emptyStringWhenNull(jsonObject, PROP_VOLUME_SERIAL));
        attributes.append(COMMA);
        attributes.append("Organization=");
        attributes.append(emptyStringWhenNull(jsonObject, PROP_DATA_SET_ORGANIZATION));
        attributes.append(COMMA);
        attributes.append("Record format=");
        attributes.append(emptyStringWhenNull(jsonObject, PROP_RECORD_FORMAT));
        attributes.append(COMMA);
        attributes.append("Record length=");
        attributes.append(emptyStringWhenNull(jsonObject, PROP_RECORD_LENGTH));
        attributes.append(COMMA);
        attributes.append("Block size=");
        attributes.append(emptyStringWhenNull(jsonObject, PROP_BLOCK_SIZE));
        attributes.append(COMMA);
        attributes.append("Data set type=");
        attributes.append(emptyStringWhenNull(jsonObject, PROP_DSN_TYPE));
        attributes.append(COMMA);
        attributes.append("Allocated extents=");
        attributes.append(emptyStringWhenNull(jsonObject, PROP_EXTENTS));
        attributes.append(COMMA);
        if (emptyStringWhenNull(jsonObject, PROP_DATA_SET_ORGANIZATION).startsWith("PO")) {
            attributes.append("PDS=true");
            attributes.append(COMMA);
        } else {
            attributes.append("PDS=false");
            attributes.append(COMMA);
        }
        attributes.append("Creation date=");
        attributes.append(emptyStringWhenNull(jsonObject, PROP_CREATION_DATE));
        attributes.append(COMMA);
        attributes.append("Referenced date=");
        attributes.append(emptyStringWhenNull(jsonObject, PROP_REFERENCE_DATE));

        return attributes.toString();
    }

    @Override
	public void setShouldArchive(boolean shouldArchive) {
		this.shouldArchive = shouldArchive;
	}

	@Override
	public boolean shouldArchive() {
		return this.shouldArchive;
	}

    @Override
	public void setShouldCleanup(boolean shouldCleanup) {
		this.shouldCleanup = shouldCleanup;
	}

	@Override
	public boolean shouldCleanup() {
		return this.shouldCleanup;
	}

    protected JsonObject getAttibutes() throws ZosDatasetException {
        if (!exists()) {
            throw new ZosDatasetException(LOG_DATA_SET + quoted(this.dsname) + LOG_DOES_NOT_EXIST + logOnImage());
        }
        
        String urlPath = RESTFILES_DATASET_PATH + SLASH + this.dsname;
        IRseapiResponse response;
        try {
        	response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.GET, urlPath, null, null, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosDatasetException(e);
        }

        if (response.getStatusCode() != HttpStatus.SC_OK) {
        	// Error case
            String displayMessage = this.zosFileHandler.buildErrorString("list data set", response); 
            logger.error(displayMessage);
            throw new ZosDatasetException(displayMessage);
        }
        
        JsonObject responseBody;
        try {
            responseBody = response.getJsonContent();
        } catch (RseapiException e) {
            throw new ZosDatasetException("Unable list to attibutes of data set " + quoted(this.dsname) + logOnImage(), e);
        }
        
        logger.trace(responseBody);
        JsonObject attributes;
        JsonArray items = responseBody.getAsJsonArray(PROP_ITEMS);
        if (items != null && items.size() > 0) {
	        attributes = items.get(0).getAsJsonObject();
	        JsonElement dsnameElement = attributes.get(PROP_NAME);
	        if (dsnameElement != null && this.dsname.equals(dsnameElement.getAsString())) {
		        logger.trace("Attibutes of data set " + quoted(this.dsname) + "  retrieved from  image " + this.image.getImageID());	            
		        return attributes;
	        }
        }
        throw new ZosDatasetException("Unable to retrieve attibutes of data set " + quoted(this.dsname) + logOnImage());
    }
    
    protected void setAttributes(JsonObject datasteAttributes) {
        JsonElement value;
        value = datasteAttributes.get(PROP_VOLUME_SERIAL);
        if (value != null) {
            setVolumes(value.getAsString());
        }
        
        value = datasteAttributes.get(PROP_UNIT);
        if (value != null) {
            this.setUnit(value.getAsString());
        }
        
        value = datasteAttributes.get(PROP_DATA_SET_ORGANIZATION);
        if (value != null) {
            setDatasetOrganization(DatasetOrganization.valueOfLabel(value.getAsString()));
        }
        
        value = datasteAttributes.get(PROP_ALLOCATION_UNIT);
        if (value != null) {
            this.alcunit = SpaceUnit.valueOf(value.getAsString() + "S");
        }
        
        value = datasteAttributes.get(PROP_PRIMARY);
        if (value != null) {
            this.primary = value.getAsInt();
        }
        
        value = datasteAttributes.get(PROP_SECONDARY);
        if (value != null) {
            this.secondary = value.getAsInt();
        }
        
        value = datasteAttributes.get(PROP_DIRECTORY_BLOCKS);
        if (value != null) {
            this.dirblk = value.getAsInt();
        }
        value = datasteAttributes.get(PROP_BLOCK_SIZE);
        if (value != null) {
            setBlockSize(value.getAsInt());
        }
    
        value = datasteAttributes.get(PROP_RECORD_FORMAT);
        if (value != null) {
            setRecordFormat(RecordFormat.valueOfLabel(value.getAsString()));
        }
        
        value = datasteAttributes.get(PROP_RECORD_LENGTH);
        if (value != null) {
            setRecordlength(value.getAsInt());
        }
        
        value = datasteAttributes.get(PROP_DATA_CLASS);
        if (value != null) {
        	setDataClass(value.getAsString());
        }
        
        value = datasteAttributes.get(PROP_STOR_CLASS);
        if (value != null) {
        	setStorageClass(value.getAsString());
        }
        
        value = datasteAttributes.get(PROP_MGMT_CLASS);
        if (value != null) {
        	setManagementClass(value.getAsString());
        }
        
        value = datasteAttributes.get(PROP_DSN_TYPE);
        if (value != null) {
        	if (value.getAsString().contains(DSType.LIBRARY.toString())) {
        		setDatasetType(DSType.LIBRARY);
        	} else {
        		setDatasetType(DSType.valueOfLabel(value.getAsString()));
        	}
        }
        
        value = datasteAttributes.get(PROP_USED);
        if (value != null) {
            this.used = value.getAsInt();
        }
        
        value = datasteAttributes.get(PROP_EXTENTS);
        if (value != null) {
            this.extents = value.getAsInt();
        }
        
        value = datasteAttributes.get(PROP_CREATION_DATE);
        if (value != null) {
            this.createDate = value.getAsString();
        }
        
        value = datasteAttributes.get(PROP_REFERENCE_DATE);
        if (value != null) {
            this.referencedDate = value.getAsString();
        }
        
        value = datasteAttributes.get(PROP_EXPIRY_DATE);
        if (value != null) {
            this.expirationDate = value.getAsString();
        }
    }

    protected Object retrieve(String memberName) throws ZosDatasetException {
    	Map<String, String> headers = new HashMap<>();
    	String dType = this.dataType.toString();
    	String urlPath = RESTFILES_DATASET_PATH + SLASH + joinDSN(memberName) + RESTFILES_DATASET_PATH_CONTENT;
    	IRseapiResponse response;
    	if (BINARY_HEADER.equals(dType)) {
            this.convert = false;
        }
        try {
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.GET, urlPath, headers, null, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, this.convert);
        } catch (RseapiException e) {
            throw new ZosDatasetException(e);
        }
        
        if (response.getStatusCode() != HttpStatus.SC_OK) {
        	// Error case
            String displayMessage = this.zosFileHandler.buildErrorString("retrieve content of data set", response); 
            logger.error(displayMessage);
            throw new ZosDatasetException(displayMessage);
        }
        
        JsonObject responseBody;
        try {
            responseBody = response.getJsonContent();
        } catch (RseapiException e) {
            throw new ZosDatasetException("Unable to retrieve content of data set " + quoted(this.dsname) + logOnImage(), e);
        }
  
        Object content = "";
        JsonElement records = responseBody.get(PROP_RECORDS);
        if (records != null) {
        	content = records.getAsString();
        }
    
        logger.trace("Content of data set " + quoted(joinDSN(memberName)) + " retrieved from  image " + this.image.getImageID());
        return content;
    }

    protected void delete(String name) throws ZosDatasetException {
	    String urlPath = RESTFILES_DATASET_PATH + SLASH + name;
	    IRseapiResponse response;
	    try {
	        response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.DELETE, urlPath, null, null, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, this.convert);
	    } catch (RseapiException e) {
	        throw new ZosDatasetException(e);
	    }
	    
	    if (response.getStatusCode() != HttpStatus.SC_NO_CONTENT) {
	    	// Error case
	        String displayMessage = this.zosFileHandler.buildErrorString("delete " + name, response); 
	        logger.error(displayMessage);
	        throw new ZosDatasetException(displayMessage);
	    }
	}

	protected byte[] inputStreamToByteArray(InputStream in) throws ZosDatasetException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int count;

        try {
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
        } catch(IOException e){
            throw new ZosDatasetException("Failed to collect binary", e);
        }

        return out.toByteArray();
    }
    
    protected void storeText(String content, String memberName, boolean convert) throws ZosDatasetException {
        if (!exists()) {
            throw new ZosDatasetException(LOG_DATA_SET + quoted(this.dsname) + LOG_DOES_NOT_EXIST + logOnImage());
        }
        
        JsonObject requestBody = new JsonObject();
        requestBody = addPropertyWhenSet(requestBody, PROP_RECORDS, content);
    
        String urlPath = RESTFILES_DATASET_PATH + SLASH + joinDSN(memberName) + RESTFILES_DATASET_PATH_CONTENT;
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.PUT_JSON, urlPath, null, requestBody, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, convert);
        } catch (RseapiException e) {
            throw new ZosDatasetException(e);
        }
        
        if (response.getStatusCode() != HttpStatus.SC_OK && response.getStatusCode() != HttpStatus.SC_CREATED) {
            // Error case
            String displayMessage = this.zosFileHandler.buildErrorString("writing to data set", response); 
            logger.error(displayMessage);
            throw new ZosDatasetException(displayMessage);
        }
    
        logger.trace(LOG_DATA_SET + quoted(joinDSN(memberName)) + " updated" + logOnImage());
    }

    protected void savePDSToResultsArchive(String rasPath) throws ZosFileManagerException {
    	Path artifactPath = this.zosFileHandler.getArtifactsRoot().resolve(rasPath);
        try {
        	zosFileHandler.getZosManager().createArtifactDirectory(artifactPath);
            Collection<String> memberList = memberList();
            Iterator<String> memberListIterator = memberList.iterator();
        
        	while (memberListIterator.hasNext()) {
        		String memberName = memberListIterator.next();
        		String fileName = zosFileHandler.getZosManager().buildUniquePathName(artifactPath, memberName);
        		if (this.dataType.equals(DatasetDataType.TEXT)) {
        			zosFileHandler.getZosManager().storeArtifact(artifactPath.resolve(fileName), memberRetrieveAsText(memberName), ResultArchiveStoreContentType.TEXT);
        		} else {
        			zosFileHandler.getZosManager().storeArtifact(artifactPath.resolve(fileName), new String(memberRetrieveAsBinary(memberName)), ResultArchiveStoreContentType.TEXT);
        		}
            	logger.info(quoted(joinDSN(memberName)) + LOG_ARCHIVED_TO + artifactPath.resolve(fileName));
        	}
		} catch (ZosManagerException e) {
			throw new ZosDatasetException(e);
		}
    }

    protected void storeBinary(byte[] content, String memberName, boolean convert) throws ZosDatasetException {
        if (!exists()) {
            throw new ZosDatasetException(LOG_DATA_SET + quoted(this.dsname) + LOG_DOES_NOT_EXIST + logOnImage());
        }
        Map<String, String> headers = new HashMap<>();
        headers.put(BINARY_HEADER, "true");
    
        String urlPath = RESTFILES_DATASET_PATH + SLASH + joinDSN(memberName) + RESTFILES_DATASET_PATH_RAW_CONTENT;
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.PUT_TEXT, urlPath, headers, new String(content), RseapiZosFileHandlerImpl.VALID_STATUS_CODES, convert);
        } catch (RseapiException e) {
            throw new ZosDatasetException(e);
        }
        
        if (response.getStatusCode() != HttpStatus.SC_OK && response.getStatusCode() != HttpStatus.SC_CREATED) {
            // Error case
            String displayMessage = this.zosFileHandler.buildErrorString("write to data set", response); 
            logger.error(displayMessage);
            throw new ZosDatasetException(displayMessage);
        }
    
        logger.trace(LOG_DATA_SET + quoted(joinDSN(memberName)) + " updated" + logOnImage());
    }

    protected String emptyStringWhenNull(JsonObject jsonElement, String property) {
        JsonElement element = jsonElement.get(property);
        if (element == null) {
            return "";
        }
        return element.getAsString();
    }

    protected JsonObject addPropertyWhenSet(JsonObject requestBody, String property, Object value) throws ZosDatasetException {
        if (value != null) {
            if (value instanceof String) {
                requestBody.addProperty(property, (String) value);
            } else if (value instanceof Integer) {
                if ((int) value >= 0) {
                    requestBody.addProperty(property, (Integer) value);
                }                
            } else {
                try {
                    requestBody.addProperty(property, value.toString());
                } catch (Exception e) {
                    throw new ZosDatasetException("Invlaid type of " + quoted(value.getClass().getName()) + " for property " + quoted(property) + logOnImage(), e);
                }
            }
        }
        return requestBody;
    }
    
    /**
     * Infer the data set and member names from the full DSN
     * @param fullName 
     */
    protected void splitDSN(String fullName) {
        if (fullName.matches(".*\\(.*\\)")) {
            this.dsname = fullName.substring(0, fullName.indexOf('(')).trim();
            this.memberName = fullName.substring(fullName.indexOf("(")+1, fullName.indexOf(")"));
        } else {
            this.dsname = fullName;
        }
    }

    /**
     * Infer the full DSN name from data set and member names 
     */
    private String joinDSN(String memberName) {
        if (memberName == null) {
            return this.dsname;
        }
        return this.dsname + "(" + memberName + ")";
    }
    
    private String quoted(String name) {
        return "\"" + name + "\"";
    }

    private String logOnImage() {
        return " on image " + this.image.getImageID();
    }
    
    @Override
    public String toString() {
        return this.dsname;
    }

    public boolean created() {
        return this.datasetCreated;
    }

    public IRseapiRestApiProcessor getRseapiApiProcessor() {
        return this.rseapiApiProcessor;
    }
    
    protected void archiveContent() throws ZosDatasetException {
    	if (shouldArchive()) {
    		Path rasPath = this.testMethodArchiveFolder.resolve(this.zosFileHandler.getZosManager().buildUniquePathName(testMethodArchiveFolder, this.dsname));
            saveToResultsArchive(rasPath.toString());
        }
    }

	protected List<String> listDatasets() throws ZosDatasetException {
		List<String> datasetList = new ArrayList<>();
		
		if (!this.dsname.endsWith("*")) {
			this.dsname = this.dsname + "*";
		}
        
        String urlPath = RESTFILES_DATASET_PATH + SLASH + this.dsname + RESTFILES_DATASET_LIST;
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.GET, urlPath, null, null, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosDatasetException(e);
        }
        
        if (response.getStatusCode() != HttpStatus.SC_OK && response.getStatusCode() != HttpStatus.SC_NOT_FOUND) {
        	// Error case
            String displayMessage = this.zosFileHandler.buildErrorString("List data sets", response); 
            logger.error(displayMessage);
            throw new ZosDatasetException(displayMessage);
        }
        
        if (response.getStatusCode() != HttpStatus.SC_NOT_FOUND) {        
	        JsonObject responseBody;
	        try {
	            responseBody = response.getJsonContent();
	        } catch (RseapiException e) {
	            throw new ZosDatasetException("Unable to retrieve list of data sets " + quoted(this.dsname) + logOnImage(), e);
	        }
	        
	        logger.trace(responseBody);
	        
	        JsonArray jsonArray;
            try {
            	JsonObject jsonObject = response.getJsonContent();
            	jsonArray = jsonObject.get("items").getAsJsonArray();
            } catch (RseapiException e) {
                throw new ZosDatasetException(e);
            }
            for (JsonElement jsonElement : jsonArray) {
                JsonObject item = jsonElement.getAsJsonObject();
                String name = item.get("name").getAsString();
                datasetList.add(name);
            }
        }
        
        logger.trace("List of members of data set " + quoted(this.dsname) + "  retrieved from  image " + this.image.getImageID());

        return datasetList;
	}
}
