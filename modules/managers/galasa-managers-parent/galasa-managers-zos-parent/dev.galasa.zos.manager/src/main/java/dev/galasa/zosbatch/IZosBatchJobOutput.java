/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch;

import java.util.List;

/**
 * Represents a zOS Batch Job output
 *
 */
public interface IZosBatchJobOutput extends Iterable<IZosBatchJobOutputSpoolFile> {

    /**
     * Returns the zOS batch jobname
     * 
     * @return jobname
     * @throws ZosBatchException
     */
    public String getJobname() throws ZosBatchException;

    /**
     * Returns the zOS batch jobid
     * 
     * @return jobid
     * @throws ZosBatchException
     */
    public String getJobid() throws ZosBatchException;

    /**
     * Returns the zOS batch job spool files
     * 
     * @return An {@link List} of {@link IZosBatchJobOutputSpoolFile}
     */
    public List<IZosBatchJobOutputSpoolFile> getSpoolFiles();

    /**
     * Returns the zOS batch job spool files as a {@link List} of spool files
     * 
     * @return a {@Link List} of spool files 
     */
    public List<String> toList();

    /**
     * Returns the number of {@link IZosBatchJobOutputSpoolFile} elements in this {@link IZosBatchJobOutput}.  If this list contains
     * more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of elements
     */
    public int size();

    /**
     * Returns <tt>true</tt> if this {@link IZosBatchJobOutputSpoolFile} contains no {@link IZosBatchJobOutput}
     *
     * @return <tt>true</tt> if empty
     */
    public boolean isEmpty();
}
