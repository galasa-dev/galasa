/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile.zosmf.manager.internal;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
import dev.galasa.zosmf.spi.IZosmfManagerSpi;

/**
 * Implementation of {@link IZosFileHandler} using zOS/MF
 *
 */
public class ZosmfZosFileHandlerImpl implements IZosFileHandler {

    private List<ZosmfZosDatasetImpl> zosDatasets = new ArrayList<>();
    private List<ZosmfZosVSAMDatasetImpl> zosVsamDatasets = new ArrayList<>();
    private List<ZosmfZosUNIXFileImpl> zosUnixFiles = new ArrayList<>();
    private String fieldName;
    
	private ZosmfZosFileManagerImpl zosFileManager;
	public ZosmfZosFileManagerImpl getZosFileManager() {
		return zosFileManager;
	}
	public IZosManagerSpi getZosManager() {
		return zosFileManager.getZosManager();
	}

	public IZosmfManagerSpi getZosmfManager() {
		return zosFileManager.getZosmfManager();
	}
    
    private static final Log logger = LogFactory.getLog(ZosmfZosFileHandlerImpl.class);

    public ZosmfZosFileHandlerImpl(ZosmfZosFileManagerImpl zosFileManager) {
        this(zosFileManager, "INTERNAL");
    }

    public ZosmfZosFileHandlerImpl(ZosmfZosFileManagerImpl zosFileManager, String fieldName) {
    	this.zosFileManager = zosFileManager;
        this.fieldName = fieldName;
    }

    @Override
    public IZosDataset newDataset(String dsname, IZosImage image) throws ZosDatasetException {
        ZosmfZosDatasetImpl zosDataset = new ZosmfZosDatasetImpl(this, image, dsname);
        zosDatasets.add(zosDataset);
        return zosDataset;
    }

    @Override
    public IZosUNIXFile newUNIXFile(String fullFilePath, IZosImage image) throws ZosUNIXFileException {
        ZosmfZosUNIXFileImpl zosUnixFile = new ZosmfZosUNIXFileImpl(this, image, fullFilePath);
        zosUnixFiles.add(zosUnixFile);
        return zosUnixFile;
    }

    @Override
    public IZosVSAMDataset newVSAMDataset(String dsname, IZosImage image) throws ZosVSAMDatasetException {
        ZosmfZosVSAMDatasetImpl zosVsamDataset = new ZosmfZosVSAMDatasetImpl(this, image, dsname);
        this.zosVsamDatasets.add(zosVsamDataset);
        return zosVsamDataset;
    }
    
    @Override
	public List<String> listDatasets(String prefix, IZosImage image) throws ZosDatasetException {
    	ZosmfZosDatasetImpl zosDatasetPrefix = new ZosmfZosDatasetImpl(this, image, prefix);
    	return ((ZosmfZosDatasetImpl) zosDatasetPrefix).listDatasets();
	}
    
	public void cleanup() throws ZosFileManagerException {
        cleanupDatasets();
        cleanupVsamDatasets();
        cleanupUnixFiles();
    }
    
    public void cleanupDatasets() throws ZosFileManagerException {
        Iterator<ZosmfZosDatasetImpl> datasetIterator = this.zosDatasets.iterator();
        while (datasetIterator.hasNext()) {
            ZosmfZosDatasetImpl zosDataset = datasetIterator.next();
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
        Iterator<ZosmfZosVSAMDatasetImpl> vsamDatasetIterator = this.zosVsamDatasets.iterator();
        while (vsamDatasetIterator.hasNext()) {
            ZosmfZosVSAMDatasetImpl zosVsamDataset = vsamDatasetIterator.next();
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
	        Iterator<ZosmfZosUNIXFileImpl> unixFileIterator = this.zosUnixFiles.iterator();
	        while (unixFileIterator.hasNext()) {
	            ZosmfZosUNIXFileImpl zosUnixFile = unixFileIterator.next();
				if (zosUnixFile.created() && !zosUnixFile.deleted() && zosUnixFile.exists()) {
					if (zosUnixFile.shouldArchive()) {
						zosUnixFile.archiveContent();
					}
				}
	        }
	        unixFileIterator = this.zosUnixFiles.iterator();
	        while (unixFileIterator.hasNext()) {
	            ZosmfZosUNIXFileImpl zosUnixFile = unixFileIterator.next();
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
    
	public Path getArtifactsRoot() {
		return this.zosFileManager.getArtifactsRoot();
	}
}
