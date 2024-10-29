/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile;

import java.util.List;

import dev.galasa.zos.IZosImage;

/**
 * <p>
 * Tester facing interface for zOS file management.
 * </p>
 * 
 * <p>
 * Provides 3 types of files:
 * <ul>
 * <li>IUNIXFile</li>
 * <li>IDataset</li>
 * <li>IVSAMDataset</li>
 * </ul>
 * Typical usage would be to instantiate one of these using the newXXX(...)
 * methods (e.g.: {@link #newDataset(String, IZosImage)}), then to call methods on that
 * object to configure it, and then to call methods on this manager to
 * manipulate it.
 * </p>
 * 
 */
public interface IZosFileHandler {

    /**
     * Instantiate a new {@link IZosDataset}, which can represent either an
     * existing dataset, or one to be created.  Member name will be ignored
     * 
     * @param dsname
     * @param image
     * @return
     * @throws ZosDatasetException 
     */
    public IZosDataset newDataset(String dsname, IZosImage image) throws ZosDatasetException;

    /**
     * Instantiate a new {@link IZosUNIXFile}, which can represent either an
     * existing UNIX file, or directory, or one to be created. <br />
     * <br />
     * N.B. If a directory is to be represented, fullFilePath must end with a
     * "/"
     * 
     * @param fullFilePath
     * @return
     * @throws ZosUNIXFileException 
     */
    public IZosUNIXFile newUNIXFile(String fullFilePath, IZosImage image) throws ZosUNIXFileException;

    /**
     * Instantiate a new {@link IZosVSAMDataset} object with the given name. The
     * object returned is a 'blank-slate' and will require configuring before it
     * can be defined or stored.
     * 
     * @param dsname
     * @return
     * @throws ZosVSAMDatasetException 
     */
    public IZosVSAMDataset newVSAMDataset(String dsname, IZosImage image) throws ZosVSAMDatasetException;

    /**
     * Return a {@link List} of data set name starting with the supplied prefix
     * 
     * @param prefix
     * @param image
     * @return
     * @throws ZosDatasetException 
     */
    public List<String> listDatasets(String prefix, IZosImage image) throws ZosDatasetException;
}
