/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile.rseapi.manager.internal;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosfile.IZosDataset;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.IZosVSAMDataset;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosfile.ZosVSAMDatasetException;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.RseapiException;
import dev.galasa.zosrseapi.spi.IRseapiManagerSpi;

/**
 * Implementation of {@link IZosFileHandler} using zOS/MF
 *
 */
public class RseapiZosFileHandlerImpl implements IZosFileHandler {

    private List<RseapiZosDatasetImpl> zosDatasets = new ArrayList<>();
    private List<RseapiZosVSAMDatasetImpl> zosVsamDatasets = new ArrayList<>();
    private List<RseapiZosUNIXFileImpl> zosUnixFiles = new ArrayList<>();
    private String fieldName;
    
	private RseapiZosFileManagerImpl zosFileManager;
	public RseapiZosFileManagerImpl getZosFileManager() {
		return zosFileManager;
	}
	public IZosManagerSpi getZosManager() {
		return zosFileManager.getZosManager();
	}

	public IRseapiManagerSpi getRseapiManager() {
		return zosFileManager.getRseapiManager();
	}

    protected static final List<Integer> VALID_STATUS_CODES = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK,
                                                                                         HttpStatus.SC_CREATED,
                                                                                         HttpStatus.SC_NO_CONTENT,
                                                                                         HttpStatus.SC_BAD_REQUEST,
                                                                                         HttpStatus.SC_UNAUTHORIZED,
                                                                                         HttpStatus.SC_FORBIDDEN,
                                                                                         HttpStatus.SC_NOT_FOUND,
                                                                                         HttpStatus.SC_INTERNAL_SERVER_ERROR
                                                                                         ));
    private static final Log logger = LogFactory.getLog(RseapiZosFileHandlerImpl.class);
    
    public RseapiZosFileHandlerImpl(RseapiZosFileManagerImpl zosFileManager) {
        this(zosFileManager, "INTERNAL");
    }

    public RseapiZosFileHandlerImpl(RseapiZosFileManagerImpl zosFileManager, String fieldName) {
    	this.zosFileManager = zosFileManager;
        this.fieldName = fieldName;
    }

    @Override
    public IZosDataset newDataset(String dsname, IZosImage image) throws ZosDatasetException {
        RseapiZosDatasetImpl zosDataset = new RseapiZosDatasetImpl(this, image, dsname);
        zosDatasets.add(zosDataset);
        return zosDataset;
    }

    @Override
    public IZosUNIXFile newUNIXFile(String fullFilePath, IZosImage image) throws ZosUNIXFileException {
        RseapiZosUNIXFileImpl zosUnixFile = new RseapiZosUNIXFileImpl(this, image, fullFilePath);
        zosUnixFiles.add(zosUnixFile);
        return zosUnixFile;
    }

    @Override
    public IZosVSAMDataset newVSAMDataset(String dsname, IZosImage image) throws ZosVSAMDatasetException {
        RseapiZosVSAMDatasetImpl zosVsamDataset = new RseapiZosVSAMDatasetImpl(this, image, dsname);
        this.zosVsamDatasets.add(zosVsamDataset);
        return zosVsamDataset;
    }
    
    @Override
	public List<String> listDatasets(String prefix, IZosImage image) throws ZosDatasetException {
    	RseapiZosDatasetImpl zosDatasetPrefix = new RseapiZosDatasetImpl(this, image, prefix);
    	return ((RseapiZosDatasetImpl) zosDatasetPrefix).listDatasets();
	}
    
	public void cleanup() throws ZosFileManagerException {
        cleanupDatasets();
        cleanupVsamDatasets();
        cleanupUnixFiles();
    }
    
    public void cleanupDatasets() throws ZosFileManagerException {
        Iterator<RseapiZosDatasetImpl> datasetIterator = this.zosDatasets.iterator();
        while (datasetIterator.hasNext()) {
            RseapiZosDatasetImpl zosDataset = datasetIterator.next();
            try {
	            if (zosDataset.created() && zosDataset.exists()) {
	                if (zosDataset.shouldArchive()) {
	                    zosDataset.archiveContent();
	                }
	                if (zosDataset.shouldCleanup()) {
	                	zosDataset.delete();
	                }
	            }
			} catch (ZosDatasetException e) {
				logger.error("Problem in data set cleanup phase", e);
			}
        }
    }

    public void cleanupVsamDatasets() throws ZosFileManagerException {
        Iterator<RseapiZosVSAMDatasetImpl> vsamDatasetIterator = this.zosVsamDatasets.iterator();
        while (vsamDatasetIterator.hasNext()) {
            RseapiZosVSAMDatasetImpl zosVsamDataset = vsamDatasetIterator.next();
            try {
	            if (zosVsamDataset.created() && zosVsamDataset.exists()) {
	            	if (zosVsamDataset.shouldArchive()) {
	            		zosVsamDataset.archiveContent();
	            	}
	                if (zosVsamDataset.shouldCleanup()) {
	                	zosVsamDataset.delete();
	                }
	            }
			} catch (ZosVSAMDatasetException e) {
				logger.error("Problem in VSAM data set cleanup phase", e);
			}
            vsamDatasetIterator.remove();
        }
    }

    public void cleanupUnixFiles() throws ZosFileManagerException {
        try {
	        Iterator<RseapiZosUNIXFileImpl> unixFileIterator = this.zosUnixFiles.iterator();
	        while (unixFileIterator.hasNext()) {
	            RseapiZosUNIXFileImpl zosUnixFile = unixFileIterator.next();
				if (zosUnixFile.created() && !zosUnixFile.deleted() && zosUnixFile.exists()) {
					if (zosUnixFile.shouldArchive()) {
						zosUnixFile.archiveContent();
					}
				}
	        }
	        unixFileIterator = this.zosUnixFiles.iterator();
	        while (unixFileIterator.hasNext()) {
	            RseapiZosUNIXFileImpl zosUnixFile = unixFileIterator.next();
				if (zosUnixFile.created() && !zosUnixFile.deleted() && zosUnixFile.exists()) {
					if (zosUnixFile.shouldCleanup()) {
						if (zosUnixFile.isDirectory()) {
							zosUnixFile.directoryDeleteNonEmpty();
						} else {
		                	zosUnixFile.delete();
		                }
				        zosUnixFile.cleanCreatedPath();
		            }
		        }
	            unixFileIterator.remove();
	        }			
		} catch (ZosUNIXFileException e) {
			logger.error("Problem in UNIX file cleanup phase", e);
		}
    }

    @Override
    public String toString() {
        return this.fieldName;
    }
    
	public String buildErrorString(String action, IRseapiResponse response) {
		String message = "";
		try {
			Object content = response.getContent();
			if (content != null) {
				RseapiZosDatasetImpl.logger.trace(content);
				if (content instanceof JsonObject) {
					message = "\nstatus: " + ((JsonObject) content).get("status").getAsString() + "\n" + "message: " + ((JsonObject) content).get("message").getAsString(); 
				} else if (content instanceof String) {
					message = " response body:\n" + content;
				}
			}
		} catch (RseapiException e) {
			// NOP
		}
	    return "Error " + action + ", HTTP Status Code " + response.getStatusCode() + " : " + response.getStatusLine() + message;
	}
    
	public Path getArtifactsRoot() {
		return this.zosFileManager.getArtifactsRoot();
	}
}
