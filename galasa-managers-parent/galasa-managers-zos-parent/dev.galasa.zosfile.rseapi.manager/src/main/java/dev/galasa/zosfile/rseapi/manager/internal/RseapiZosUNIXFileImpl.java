/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile.rseapi.manager.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosrseapi.IRseapi.RseapiRequestType;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.IRseapiRestApiProcessor;
import dev.galasa.zosrseapi.RseapiException;
import dev.galasa.zosrseapi.RseapiManagerException;

public class RseapiZosUNIXFileImpl implements IZosUNIXFile {
    
    IRseapiRestApiProcessor rseapiApiProcessor;

    private Path testMethodArchiveFolder;

	private RseapiZosFileHandlerImpl zosFileHandler;
	public RseapiZosFileHandlerImpl getZosFileHandler() {
		return zosFileHandler;
	}

    private IZosImage image;

    private static final String SLASH = "/";
    private static final String COMMA = ",";
    private static final String RESTFILES_FILE_PATH = SLASH + "rseapi" + SLASH + "api" + SLASH + "v1" + SLASH + "unixfiles";
    private static final String RESTFILES_FILE_PATH_RAW_CONTENT = SLASH + "rawContent";
    private static final String PATH_EQUALS = "?path=";

    private String unixPath;
    private String fileName;
    private String directoryPath;
    private UNIXFileType fileType;
    private String createMode;
    private UNIXFileDataType dataType;
	private Set<PosixFilePermission> filePermissions;
	private int fileSize = -1;
	private String lastModified;
	private String user;
	private String group;

    private boolean pathCreated;
    private String createdPath;
    private boolean deleted;

    private boolean shouldArchive = false;

    private boolean shouldCleanup = true;
    
	private static final String PROP_PERMISSIONS_SYMBOLIC = "permissionsSymbolic";
	private static final String PROP_SIZE = "size";
	private static final String PROP_LAST_MODIFIED = "lastModified";
	private static final String PROP_TYPE = "type";
	private static final String PROP_NAME = "name";
	private static final String PROP_FILE_OWNER = "fileOwner";
	private static final String PROP_ENCODING = "encoding";
	private static final String PROP_GROUP = "group";
    private static final String PROP_CHILDREN = "children";
    private static final String PROP_PERMISSIONS = "permissions";
    private static final String PROP_CONTENT = "content";
    
    private static final String HEADER_CONVERT = "convert";

    private static final String LOG_UNIX_PATH = "UNIX path ";
    private static final String LOG_DOES_NOT_EXIST = " does not exist";
    private static final String LOG_ARCHIVED_TO = " archived to ";
    private static final String LOG_INVALID_REQUETS = "Invalid request, ";
    private static final String LOG_UNABLE_TO_LIST_UNIX_PATH = "Unable to list UNIX path ";

    private static final Log logger = LogFactory.getLog(RseapiZosUNIXFileImpl.class);

	public RseapiZosUNIXFileImpl(RseapiZosFileHandlerImpl zosFileHandler, IZosImage image, String unixPath) throws ZosUNIXFileException {
        if (!unixPath.startsWith(SLASH)) {
            throw new ZosUNIXFileException(LOG_UNIX_PATH + "must be absolute not be relative");
        }        
        this.image = image;
        this.unixPath = FilenameUtils.normalize(unixPath, true);
        this.zosFileHandler = zosFileHandler;
        this.testMethodArchiveFolder = this.zosFileHandler.getZosFileManager().getUnixPathCurrentTestMethodArchiveFolder();
        splitUnixPath();
        
        try {
            this.rseapiApiProcessor = this.zosFileHandler.getZosFileManager().getRseapiManager().newRseapiRestApiProcessor(this.image, this.zosFileHandler.getZosManager().getZosFilePropertyFileRestrictToImage(image.getImageID()));
            this.createMode = this.zosFileHandler.getZosManager().getZosFilePropertyUnixFilePermissions(this.image.getImageID());
        } catch (ZosFileManagerException | RseapiManagerException e) {
            throw new ZosUNIXFileException(e);
        }
    }

    @Override
    public IZosUNIXFile create() throws ZosUNIXFileException {
        return create(PosixFilePermissions.fromString(this.createMode));
    }
    
    @Override
	public IZosUNIXFile create(Set<PosixFilePermission> accessPermissions) throws ZosUNIXFileException {
        if (exists()) {
            throw new ZosUNIXFileException(LOG_UNIX_PATH + quoted(this.unixPath) + " already exists" + logOnImage());
        }
        String[] directoryPathParts = this.directoryPath.substring(1).split(SLASH);
        StringBuilder path = new StringBuilder();
        path.append(SLASH);
        for (String part : directoryPathParts) {
            path.append(part);
            if (!exists(path.toString())) {
                createPath(path.toString(), UNIXFileType.DIRECTORY, accessPermissions);
                if (this.createdPath == null) {
                    this.createdPath = path.toString() + SLASH;
                }
            }
            path.append(SLASH);
        }
        if (this.fileName != null) {
            createPath(this.unixPath, this.fileType, accessPermissions);
        }
        
        if (exists()) {
            logger.info(LOG_UNIX_PATH + quoted(this.unixPath) + " created" + logOnImage());
            this.pathCreated = true;
        } else {
            logger.info(LOG_UNIX_PATH + quoted(this.unixPath) + " not created" + logOnImage());
        }
        return this;
	}

	@Override
    public boolean delete() throws ZosUNIXFileException {
        delete(this.unixPath, false);
        return this.deleted;
    }
    
    @Override
    public boolean directoryDeleteNonEmpty() throws ZosUNIXFileException {
        delete(this.unixPath, true);
        return this.deleted;
    }

    @Override
    public boolean exists() throws ZosUNIXFileException {
        return exists(this.unixPath);
    }
    
    @Override
    public void storeText(String content) throws ZosUNIXFileException {
        if (!exists()) {
            throw new ZosUNIXFileException(LOG_UNIX_PATH + quoted(this.unixPath) + LOG_DOES_NOT_EXIST + logOnImage());
        }
        if (isDirectory()) {
            throw new ZosUNIXFileException(LOG_INVALID_REQUETS + quoted(this.unixPath) + " is a directory");
        }
        setDataType(UNIXFileDataType.TEXT);
        String urlPath = RESTFILES_FILE_PATH + this.unixPath;
        RseapiRequestType requestType = RseapiRequestType.PUT_JSON;
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty(PROP_CONTENT, content);
    	Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_CONVERT, "true");        
    
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(requestType, urlPath, headers, requestBody, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosUNIXFileException(e);
        }
        
        if (response.getStatusCode() != HttpStatus.SC_OK) {
            // Error case
        	String displayMessage = this.zosFileHandler.buildErrorString("writing to " + quoted(this.unixPath), response); 
            logger.error(displayMessage);
            throw new ZosUNIXFileException(displayMessage);
        }
    
        logger.trace(LOG_UNIX_PATH + quoted(this.directoryPath) + " updated" + logOnImage());
    }

    @Override
	public void storeBinary(byte[] content) throws ZosUNIXFileException {
        if (!exists()) {
            throw new ZosUNIXFileException(LOG_UNIX_PATH + quoted(this.unixPath) + LOG_DOES_NOT_EXIST + logOnImage());
        }
        if (isDirectory()) {
            throw new ZosUNIXFileException(LOG_INVALID_REQUETS + quoted(this.unixPath) + " is a directory");
        }
        setDataType(UNIXFileDataType.BINARY);
        String urlPath = RESTFILES_FILE_PATH + this.unixPath + RESTFILES_FILE_PATH_RAW_CONTENT;
        RseapiRequestType requestType = RseapiRequestType.PUT_BINARY;
        Object requestBody = content;
        Map<String, String> headers = new HashMap<>();
    	headers.put(HEADER_CONVERT, "false");
    
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(requestType, urlPath, headers, requestBody, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosUNIXFileException(e);
        }
        
        if (response.getStatusCode() != HttpStatus.SC_OK) {
            // Error case
        	String displayMessage = this.zosFileHandler.buildErrorString("writing to " + quoted(this.unixPath), response); 
            logger.error(displayMessage);
            throw new ZosUNIXFileException(displayMessage);
        }
    
        logger.trace(LOG_UNIX_PATH + quoted(this.directoryPath) + " updated" + logOnImage());
	}

	@Override
    public String retrieveAsText() throws ZosUNIXFileException {
        if (!exists()) {
            throw new ZosUNIXFileException(LOG_UNIX_PATH + quoted(this.unixPath) + LOG_DOES_NOT_EXIST + logOnImage());
        }
        if (isDirectory()) {
            throw new ZosUNIXFileException(LOG_INVALID_REQUETS + quoted(this.unixPath) + " is a directory");
        }
        setDataType(UNIXFileDataType.TEXT);
        return (String) retrieve(this.unixPath);
    }

    @Override
	public byte[] retrieveAsBinary() throws ZosUNIXFileException {
        if (!exists()) {
            throw new ZosUNIXFileException(LOG_UNIX_PATH + quoted(this.unixPath) + LOG_DOES_NOT_EXIST + logOnImage());
        }
        if (isDirectory()) {
            throw new ZosUNIXFileException(LOG_INVALID_REQUETS + quoted(this.unixPath) + " is a directory");
        }
        setDataType(UNIXFileDataType.BINARY);
        return (byte[]) retrieve(this.unixPath);
	}

	@Override
    public void saveToResultsArchive(String rasPath) throws ZosUNIXFileException {
        saveToResultsArchive(this.unixPath, rasPath);
    }
    
    @Override
    public boolean isDirectory() throws ZosUNIXFileException {
        return isDirectory(this.unixPath);
    }

    @Override
    public SortedMap<String, IZosUNIXFile> directoryList() throws ZosUNIXFileException {
        return listDirectory(this.unixPath, false);
    }

    @Override
    public SortedMap<String, IZosUNIXFile> directoryListRecursive() throws ZosUNIXFileException {
        return listDirectory(this.unixPath, true);
    }

    @Override
    public void setDataType(UNIXFileDataType dataType) {
        this.dataType = dataType;
    }

    @Override
	public void setAccessPermissions(Set<PosixFilePermission> accessPermissions, boolean recursive) throws ZosUNIXFileException {
    	String command;
    	if (recursive) {
    		command = "chmod -R ";
    	} else {
    		command = "chmod ";
    	}
    	command = command + IZosUNIXFile.posixFilePermissionsToOctal(accessPermissions) + " " + this.unixPath;
    	RseapiZosUnixCommand zosUnixCommand = new RseapiZosUnixCommand(this.zosFileHandler);
    	JsonObject responseBody;
		try {
			responseBody = zosUnixCommand.execute(this.rseapiApiProcessor, command);
		} catch (ZosFileManagerException e) {
			throw new ZosUNIXFileException("Unable to set zOS UNIX file access permissions of " + this.unixPath, e);
		}
    	JsonObject output = responseBody.getAsJsonObject("output");
    	if (output !=  null && output.get("stderr") != null) {
			String stderr = output.get("stderr").getAsString();
			String[] stderrArray = stderr.split("\n");
			if (!(stderrArray.length == 1 && stderrArray[0].isEmpty())) {
				boolean notPermitted = false;
				logger.warn("Messages issued to stderr:");
				for (String element : stderrArray) {
					if (!element.isEmpty()) {
						logger.warn(element);
					}
					if (element.contains("EDC5139I")) {
						notPermitted = true;
					}
				}
				if (notPermitted) {
					throw new ZosUNIXFileException("Unable to change file access permissions of " + LOG_UNIX_PATH + quoted(this.unixPath) + ".\ndetails:\n" + stderr);
				}
			}
        }
	}

	@Override
    public UNIXFileType getFileType() {
    	return this.fileType;
    }

	@Override
	public UNIXFileDataType getDataType() {
	    if (this.dataType == null) {
	        return UNIXFileDataType.TEXT;
	    }
	    return this.dataType;
	}

	@Override
	public Set<PosixFilePermission> getFilePermissions() throws ZosUNIXFileException {
		retrieveAttributes();
		return this.filePermissions;
	}

	@Override
	public int getSize() throws ZosUNIXFileException {
		retrieveAttributes();
		return this.fileSize;
	}

	@Override
	public String getLastModified() throws ZosUNIXFileException {
		retrieveAttributes();
		return this.lastModified;
	}

	@Override
	public String getUser() throws ZosUNIXFileException {
		retrieveAttributes();
		return this.user;
	}

	@Override
	public String getGroup() throws ZosUNIXFileException {
		retrieveAttributes();
		return this.group;
	}

	@Override
	public void retrieveAttributes() throws ZosUNIXFileException {
		getAttributes(this.unixPath);
	}

	@Override
    public String getUnixPath() {
        return this.unixPath;
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public String getDirectoryPath() {
        return this.directoryPath;
    }
    
    @Override 
    public String getAttributesAsString() throws ZosUNIXFileException {
        return getAttributesAsString(this.unixPath);
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
    
    protected String getAttributesAsString(String path) throws ZosUNIXFileException {
    	JsonObject responseBody = getAttributes(path);
        StringBuilder attributes = new StringBuilder();
        attributes.append("Name=");
        attributes.append(path);
        attributes.append(COMMA);
        attributes.append("Type=");
        String typeValue = emptyStringWhenNull(responseBody, PROP_TYPE);
        attributes.append(typeValue);
        attributes.append(COMMA);
        if (typeValue.equalsIgnoreCase(UNIXFileType.DIRECTORY.toString())) {
            attributes.append("IsEmpty=");
            JsonArray children = responseBody.getAsJsonArray(PROP_CHILDREN);
            attributes.append(children == null? "true" : "false");
            attributes.append(COMMA);            	
        }
        attributes.append("Mode=");
        attributes.append(emptyStringWhenNull(responseBody, PROP_PERMISSIONS_SYMBOLIC));
        attributes.append(COMMA);
        attributes.append("Size=");
        attributes.append(emptyStringWhenNull(responseBody, PROP_SIZE));
        attributes.append(COMMA);
        attributes.append("User=");
        attributes.append(emptyStringWhenNull(responseBody, PROP_FILE_OWNER));
        attributes.append(COMMA);
        attributes.append("Group=");
        attributes.append(emptyStringWhenNull(responseBody, PROP_GROUP));
        attributes.append(COMMA);
        attributes.append("Modified=");
        attributes.append(emptyStringWhenNull(responseBody, PROP_LAST_MODIFIED));
        attributes.append(COMMA);
        attributes.append("Encoding=");
        attributes.append(emptyStringWhenNull(responseBody, PROP_ENCODING));
        return attributes.toString();
    }
        
    protected JsonObject getAttributes(String path) throws ZosUNIXFileException {
        if (path.endsWith(SLASH)) {
            path = path.substring(0, path.length()-1);
        }
        if (!exists(path)) {
            throw new ZosUNIXFileException(LOG_UNIX_PATH + quoted(path) + LOG_DOES_NOT_EXIST + logOnImage());
        }
        
        Map<String, String> headers = new HashMap<>();
        String urlPath = RESTFILES_FILE_PATH + PATH_EQUALS + path;
        
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.GET, urlPath, headers, null, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosUNIXFileException(e);
        }
            
        JsonObject responseBody;
        try {
            responseBody = response.getJsonContent();
        } catch (RseapiException e) {
            throw new ZosUNIXFileException("Unable to list " + LOG_UNIX_PATH + quoted(path) + logOnImage(), e);
        }
        
        logger.trace(responseBody);
        if (response.getStatusCode() == HttpStatus.SC_OK) {
        	if (path.equals(this.unixPath)) {
        		setAttributeValues(responseBody);
        	}
        	return responseBody;            
        } else {
            // Error case
            String displayMessage = this.zosFileHandler.buildErrorString("getting attributes " + quoted(path), response);
            logger.error(displayMessage);
            throw new ZosUNIXFileException(displayMessage);
        }
    }

    protected void setAttributeValues(JsonObject responseBody) {
		JsonElement element = responseBody.get(PROP_PERMISSIONS_SYMBOLIC);
        if (element != null) {
        	this.filePermissions = PosixFilePermissions.fromString(element.getAsString().substring(1));
        	this.fileType = determineType(element.getAsString());
        }
    	element = responseBody.get(PROP_SIZE);
        if (element != null) {
        	this.fileSize = element.getAsInt();
        }
        element = responseBody.get(PROP_FILE_OWNER);
        if (element != null) {
        	this.user = element.getAsString();
        }
        element = responseBody.get(PROP_GROUP);
        if (element != null) {
        	this.group = element.getAsString();
        }
        element = responseBody.get(PROP_LAST_MODIFIED);
        if (element != null) {
        	this.lastModified = element.getAsString();
        }
	}


	protected boolean createPath(String path, UNIXFileType fileType, Set<PosixFilePermission> accessPermissions) throws ZosUNIXFileException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty(PROP_TYPE, fileType.toString().toLowerCase());
        requestBody.addProperty(PROP_PERMISSIONS, IZosUNIXFile.posixFilePermissionsToSymbolicNotation(accessPermissions));
        
        String urlPath = RESTFILES_FILE_PATH + SLASH + path;
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.POST_JSON, urlPath, null, requestBody, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosUNIXFileException(e);
        }

        if (response.getStatusCode() != HttpStatus.SC_CREATED) {            
            // Error case
            String displayMessage = this.zosFileHandler.buildErrorString("creating path " + quoted(path), response); 
            logger.error(displayMessage);
            throw new ZosUNIXFileException(displayMessage);
        }
        return true;
    }


    protected void delete(String path, boolean recursive) throws ZosUNIXFileException {
        if (!exists(path)) {
            throw new ZosUNIXFileException(LOG_UNIX_PATH + quoted(path) + LOG_DOES_NOT_EXIST + logOnImage());
        }
        Map<String, String> headers = new HashMap<>();
        String attributes = getAttributesAsString(path);
        boolean isDirectory = attributes.contains("Type=" + UNIXFileType.DIRECTORY.toString().toUpperCase());
        if (recursive) {
            if (!isDirectory) {
                throw new ZosUNIXFileException(LOG_INVALID_REQUETS + LOG_UNIX_PATH + quoted(path) + " is not a directory");
            }
        } else {
        	if (isDirectory && attributes.contains("IsEmpty=false")) {
                throw new ZosUNIXFileException(LOG_INVALID_REQUETS + LOG_UNIX_PATH + quoted(path) + " is a directory and is not empty. Use the directoryDeleteNonEmpty() method");
            }
        }
        String urlPath = RESTFILES_FILE_PATH + path;
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.DELETE, urlPath, headers, null, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosUNIXFileException(e);
        }

        if (response.getStatusCode() != HttpStatus.SC_NO_CONTENT) {            
            // Error case
            String displayMessage = this.zosFileHandler.buildErrorString("creating path " + quoted(path), response); 
            logger.error(displayMessage);
            throw new ZosUNIXFileException(displayMessage);
        }
        
        if (exists(path)) {
            logger.info(LOG_UNIX_PATH + quoted(path) + " not deleted" + logOnImage());
            this.deleted = false;
        } else {
            logger.info(LOG_UNIX_PATH + quoted(path) + " deleted" + logOnImage());
            this.deleted = true;
        }
    }


    protected boolean exists(String path) throws ZosUNIXFileException {
        if (path.endsWith(SLASH)) {
            path = path.substring(0, path.length()-1);
        }
        String urlPath = RESTFILES_FILE_PATH + SLASH + PATH_EQUALS + path;
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.GET, urlPath, null, null, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosUNIXFileException(e);
        }
        
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            logger.trace(LOG_UNIX_PATH + quoted(path) + " exists" + logOnImage());
            return true;
        } else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            logger.trace(LOG_UNIX_PATH + quoted(path) + LOG_DOES_NOT_EXIST + logOnImage());
            return false;
        } else {
        	String displayMessage = this.zosFileHandler.buildErrorString("listing path " + quoted(path), response); 
            logger.error(displayMessage);
            throw new ZosUNIXFileException(displayMessage);
        }
    }


    protected Object retrieve(String path) throws ZosUNIXFileException {
    	String urlPath;
        Map<String, String> headers = new HashMap<>();
        boolean convert;
        if (getDataType().equals(UNIXFileDataType.TEXT)) {
        	urlPath = RESTFILES_FILE_PATH + path;
        	convert = true;
        } else {        	
        	urlPath = RESTFILES_FILE_PATH + path + RESTFILES_FILE_PATH_RAW_CONTENT;
        	convert = false;
        }
    	headers.put(HEADER_CONVERT, String.valueOf(convert));
        
        
        IRseapiResponse response;
        try {
			response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.GET, urlPath, headers, null, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, convert);
        } catch (RseapiException e) {
            throw new ZosUNIXFileException(e);
        }

        if (response.getStatusCode() != HttpStatus.SC_OK) {            
            // Error case
            String displayMessage = this.zosFileHandler.buildErrorString("retrieve content " + quoted(path), response); 
            logger.error(displayMessage);
            throw new ZosUNIXFileException(displayMessage);
        }

        Object content = "";
        Object responseBody;
        try {
        	if (getDataType().equals(UNIXFileDataType.TEXT)) {
        		responseBody = response.getJsonContent();            
        		logger.trace(responseBody);
        		if (((JsonObject) responseBody).get(PROP_CONTENT) != null) {
        			content = ((JsonObject) responseBody).get(PROP_CONTENT).getAsString();
        		}
        	} else {
        		content = IOUtils.toByteArray((InputStream) response.getContent());
        	}
        } catch (RseapiException | IOException e) {
        	throw new ZosUNIXFileException("Unable to retrieve content of " + quoted(path) + logOnImage(), e);
        }
    
        logger.trace("Content of " + LOG_UNIX_PATH + quoted(path) + " retrieved from  image " + this.image.getImageID());
        return content;
    }


    protected void saveToResultsArchive(String path, String rasPath) throws ZosUNIXFileException {
        if (!exists(path)) {
            throw new ZosUNIXFileException(LOG_UNIX_PATH + quoted(path) + LOG_DOES_NOT_EXIST + logOnImage());
        }
        if (isDirectory(path)) {
            Map<String, IZosUNIXFile> paths = listDirectory(path, true);
            for (Map.Entry<String, IZosUNIXFile> entry : paths.entrySet()) {
            	IZosUNIXFile entryUnixFile = entry.getValue();
	            String entryPath = entryUnixFile.getUnixPath();
	            if (!entryPath.contains("~")) {
	                String directoryName = entryPath.substring(path.length());
	                UNIXFileType entryFileType = entryUnixFile.getFileType();
	                if (entryFileType.equals(UNIXFileType.FILE)) {
	                	String fileName = entry.getValue().getFileName();
	                	if (directoryName.contains(SLASH)) {
	                		directoryName = SLASH + directoryName.substring(0,directoryName.length()-fileName.length()-1);
	                	} else {
	                		directoryName = SLASH;
	                	}
	                    String archiveLocation = storeArtifact(rasPath + directoryName, retrieve(entryPath), false, fileName);
	                    logger.info(quoted(entryPath) + LOG_ARCHIVED_TO + archiveLocation);
	                } else if (entryFileType.equals(UNIXFileType.DIRECTORY)) {
	                    String archiveLocation = storeArtifact(rasPath, null, true, directoryName);
	                    logger.info(quoted(entryPath) + LOG_ARCHIVED_TO + archiveLocation);
	                }
            	}
            }
        } else {
            String archiveLocation;
            if (getDataType().equals(UNIXFileDataType.TEXT)) {
            	archiveLocation = storeArtifact(rasPath, retrieveAsText(), false, this.fileName);
            } else {
            	archiveLocation = storeArtifact(rasPath, retrieveAsBinary(), false, this.fileName);
            }
            logger.info(quoted(this.unixPath) + LOG_ARCHIVED_TO + archiveLocation);
        }
    }


    protected boolean isDirectory(String path) throws ZosUNIXFileException {
        if (path.equals(this.unixPath) && !exists(path)) {
            return this.fileType.equals(UNIXFileType.DIRECTORY);
        }
        return getAttributesAsString(path).contains("Type=" + UNIXFileType.DIRECTORY.toString().toUpperCase());
    }


    protected SortedMap<String, IZosUNIXFile> listDirectory(String path, boolean recursive) throws ZosUNIXFileException {
        if (!isDirectory(path)) {
            throw new ZosUNIXFileException(LOG_INVALID_REQUETS + quoted(path) + " is not a directory");
        }

        path.replaceAll("/[\\/\\/]+", "/").replaceAll("\\/$", "");
        Map<String, String> headers = new HashMap<>();
        String urlPath = RESTFILES_FILE_PATH + PATH_EQUALS + path;
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.GET, urlPath, headers, null, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosUNIXFileException(e);
        }
            
        JsonObject responseBody;
        try {
            responseBody = response.getJsonContent();
        } catch (RseapiException e) {
            throw new ZosUNIXFileException(LOG_UNABLE_TO_LIST_UNIX_PATH + quoted(path) + logOnImage(), e);
        }
        
        logger.trace(responseBody);
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            return getPaths(path, responseBody, recursive);
        } else {
            // Error case
        	String displayMessage = this.zosFileHandler.buildErrorString("listing path " + quoted(path), response);
            logger.error(displayMessage);
            throw new ZosUNIXFileException(displayMessage);
        }
    }

    protected SortedMap<String, IZosUNIXFile> getPaths(String root, JsonObject responseBody, boolean recursive) throws ZosUNIXFileException {
    	if (!root.endsWith(SLASH)) {
    		root = root + SLASH;
    	}
		SortedMap<String, IZosUNIXFile> paths = new TreeMap<>();
		JsonArray children = responseBody.getAsJsonArray(PROP_CHILDREN);
		if (children != null) {
			for (JsonElement childElement : children) {
				JsonObject child = childElement.getAsJsonObject();
				String path = root + child.get(PROP_NAME).getAsString();
				if (!(path.endsWith("/.") || path.endsWith("/.."))) {
				   	IZosUNIXFile unixFile = newUnixFile(path);
					paths.put(path, unixFile);
					if (recursive && unixFile.getFileType().equals(UNIXFileType.DIRECTORY)) {
						paths.putAll(listDirectory(path, recursive));
					}
				}
			}
		}
        return paths;
    } 

    protected IZosUNIXFile newUnixFile(String path) throws ZosUNIXFileException {
    	RseapiZosUNIXFileImpl unixFile = new RseapiZosUNIXFileImpl(this.zosFileHandler, this.image, path);
    	unixFile.setAttributeValues(unixFile.getAttributes(path));
    	return unixFile;
	}

	protected UNIXFileType determineType(String mode) {
        String typeChar = mode.substring(0, 1);
        switch(typeChar) {
            case "-": return UNIXFileType.FILE;
            case "c": return UNIXFileType.CHARACTER;
            case "d": return UNIXFileType.DIRECTORY;
            case "e": return UNIXFileType.EXTLINK;
            case "l": return UNIXFileType.SYMBLINK;
            case "p": return UNIXFileType.FIFO;
            case "s": return UNIXFileType.SOCKET;
            default: return UNIXFileType.UNKNOWN;
        }
    }
    
    protected String storeArtifact(String rasPath, Object content, boolean directory, String artifactPath) throws ZosUNIXFileException {
        Path rasArtifactPath;
		try {
            if (directory) {
            	rasArtifactPath = this.zosFileHandler.getArtifactsRoot().resolve(StringUtils.stripStart(rasPath, SLASH)).resolve(StringUtils.stripStart(artifactPath, SLASH));
                Files.createDirectories(rasArtifactPath);
            } else {
            	String uniqueArtifactPath = this.zosFileHandler.getZosManager().buildUniquePathName(this.zosFileHandler.getArtifactsRoot().resolve(StringUtils.stripStart(rasPath, SLASH)), StringUtils.stripStart(artifactPath, SLASH));
            	rasArtifactPath = this.zosFileHandler.getArtifactsRoot().resolve(rasPath).resolve(uniqueArtifactPath);
                Files.createFile(rasArtifactPath, ResultArchiveStoreContentType.TEXT);
                if (content instanceof String) {
                    Files.write(rasArtifactPath, ((String) content).getBytes()); 
                } else if (content instanceof byte[]) {
                    Files.write(rasArtifactPath, (byte[]) content);
                } else {
                    throw new ZosUNIXFileException("Unable to store artifact. Invalid content object type: " + content.getClass().getName());
                }
            }
        } catch (IOException e) {
            throw new ZosUNIXFileException("Unable to store artifact", e);
        }
        return rasArtifactPath.toString();
    }
    
    protected void splitUnixPath() {
        if (this.unixPath.endsWith(SLASH)) {
            this.fileName = null;
            this.directoryPath = this.unixPath;
            this.fileType = UNIXFileType.DIRECTORY;
        } else {
            int index = this.unixPath.lastIndexOf('/');
            this.fileName = this.unixPath.substring(++index);
            this.directoryPath = this.unixPath.substring(0,index-1);
            this.fileType = UNIXFileType.FILE;
        }
    }

    protected String emptyStringWhenNull(JsonObject jsonElement, String property) {
        JsonElement element = jsonElement.get(property);
        if (element == null) {
            return "";
        }
        return element.getAsString();
    }

    protected String quoted(String name) {
        return "'" + name + "'";
    }

    protected String logOnImage() {
        return " on image " + this.image.getImageID();
    }
    
    @Override
    public String toString() {
        return this.unixPath;
    }

    public boolean created() {
        return this.pathCreated;
    }
    
    public void cleanCreatedPath() {
        try {
            if (this.createdPath != null && exists(this.createdPath)) {
            	if (this.shouldArchive()) {
            		cleanCreatedPathStore();
            	}
                cleanCreatedDelete();
            }
        } catch (ZosUNIXFileException e) {
            logger.error(e);
        }
    }
    
    protected void cleanCreatedPathStore() {
        try {
            saveToResultsArchive(this.createdPath);
        } catch (ZosUNIXFileException e) {
            logger.error(e);
        }
    }
    
    protected void cleanCreatedDelete() {
        try {
            delete(this.createdPath, true);
        } catch (ZosUNIXFileException e) {
            logger.error(e);
        }
    }

    public boolean deleted() {
        return this.deleted;
    }
    
    protected void archiveContent() throws ZosUNIXFileException {
    	if (shouldArchive()) {
    		Path rasPath = this.testMethodArchiveFolder.resolve(this.zosFileHandler.getZosManager().buildUniquePathName(testMethodArchiveFolder, this.unixPath.substring(1)));
            saveToResultsArchive(rasPath.toString());
        }
    }
}
