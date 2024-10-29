/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile;

import java.util.Collection;

import javax.validation.constraints.NotNull;

/**
 * Representation of a non-VSAM data set
 */
public interface IZosDataset {
    
    /**
     * Enumeration of data set types:
     * <li>{@link #HFS}</li>
     * <li>{@link #PDS}</li>
     * <li>{@link #PDSE}</li>
     * <li>{@link #LIBRARY}</li>
     * <li>{@link #LARGE}</li>
     * <li>{@link #BASIC}</li>
     * <li>{@link #EXTREQ}</li>
     * <li>{@link #EXTPREF}</li>
     */
    public enum DSType {

        HFS("HFS"),
        PDS("PDS"),
        PDSE("PDSE"),
        LIBRARY("LIBRARY"),
        LARGE("LARGE"),
        BASIC("BASIC"),
        EXTREQ("EXTREQ"),
        EXTPREF("EXTPREF");
        
        private String type;
        
        DSType(String type) {
            this.type = type;
        }
        
        @Override
        public String toString() {
            return type;
        }
        
        public static DSType valueOfLabel(String label) {
            for (DSType element : values()) {
                if (element.type.equals(label)) {
                    return element;
                }
            }
            return null;
        }
    }
    
    /**
     * Enumeration of record format options:
     * <li>{@link #BLOCK}</li>
     * <li>{@link #FIXED}</li>
     * <li>{@link #FIXED_BLOCKED}</li>
     * <li>{@link #VARIABLE}</li>
     * <li>{@link #VARIABLE_BLOCKED}</li>
     * <li>{@link #UNDEFINED}</li>
     */
    public enum RecordFormat {
        
        BLOCK("B"),
        FIXED("F"),
        FIXED_BLOCKED("FB"),
        VARIABLE("V"),
        VARIABLE_BLOCKED("VB"),
        UNDEFINED("U");
        
        private String format;
        
        RecordFormat(String format) {
            this.format = format;
        }
        
        @Override
        public String toString() {
            return format;
        }
        
        public static RecordFormat valueOfLabel(String label) {
            for (RecordFormat element : values()) {
                if (element.format.equals(label)) {
                    return element;
                }
            }
            return null;
        }
    }
    
    /**
     * Enumeration of data set organization options:
     * <li>{@link #PARTITIONED}</li>
     * <li>{@link #SEQUENTIAL}</li>
     */
    public enum DatasetOrganization {
        
        PARTITIONED("PO"),
        SEQUENTIAL("PS");
        
        private String format;
        
        DatasetOrganization(String format) {
            this.format = format;
        }
        
        @Override
        public String toString() {
            return format;
        }
        
        public static DatasetOrganization valueOfLabel(String label) {
            for (DatasetOrganization element : values()) {
                if (element.format.equals(label)) {
                    return element;
                }
            }
            return null;
        }
    }
    
    /**
     * Enumeration of space units for data set allocation:
     * <li>{@link #TRACKS}</li>
     * <li>{@link #CYLINDERS}</li>
     */
    public enum SpaceUnit {
        /**
         * Allocate data set in tracks
         */
        TRACKS("TRK"),
        /**
         * Allocate data set in cylinders
         */
        CYLINDERS("CYL");
        
        private String unit;
        
        SpaceUnit(String spaceUnit) {
            this.unit = spaceUnit;
        }
        
        @Override
        public String toString() {
            return unit;
        }
        
        public static SpaceUnit valueOfLabel(String label) {
            for (SpaceUnit element : values()) {
                if (element.unit.equals(label)) {
                    return element;
                }
            }
            return null;
        }
    }
    
    /**
     * Enumeration of data type for store and retrieve of data set content:
     * <li>{@link #TEXT}</li>
     * <li>{@link #BINARY}</li>
     * <li>{@link #RECORD}</li>
     */
    public enum DatasetDataType {
        /**
         * Content is between ISO8859-1 on the client and EBCDIC on the host
         */
        TEXT("text"),
        /**
         * No data conversion is performed
         */
        BINARY("binary"),
        /**
         * No data conversion is performed. Each logical record is preceded by the 4-byte record length of the record that follows
         */
        RECORD("record");
        
        private String dataType;
        
        DatasetDataType(String dataType) {
            this.dataType = dataType;
        }
        
        @Override
        public String toString() {
            return dataType;
        }
        
        public static DatasetDataType valueOfLabel(String label) {
            for (DatasetDataType element : values()) {
                if (element.dataType.equals(label)) {
                    return element;
                }
            }
            return null;
        }
    }
    
    /**
     * Allocate the physical data set on the zOS image. Will be deleted at test method end
     * @return
     * @throws ZosDatasetException 
     */
    public IZosDataset create() throws ZosDatasetException;

    /**
     * Delete the data set on the zOS image.
     * @return deleted
     * @throws ZosDatasetException
     */
    public boolean delete() throws ZosDatasetException;

    /**
     * Returns true if the data set exists on the zOS image
     * @return 
     * @throws ZosDatasetException
     */
    public boolean exists() throws ZosDatasetException;

    /**
     * Write content to the data set in Text mode 
     * <p>See {@link #setDataType(DatasetDataType)}
     * @param content
     * @throws ZosDatasetException
     */
    public void storeText(@NotNull String content) throws ZosDatasetException;

    /**
     * Write content to the data set in Binary mode 
     * <p>See {@link #setDataType(DatasetDataType)}
     * @param content
     * @throws ZosDatasetException
     */
    public void storeBinary(@NotNull byte[] content) throws ZosDatasetException;
    
    /**
     * Retrieve content of the data set in Text mode
     * <p>See {@link #setDataType(DatasetDataType)}
     * @return data set content
     * @throws ZosDatasetException
     */
    public String retrieveAsText() throws ZosDatasetException;

    /**
     * Retrieve content of the data set in Binary mode
     * <p>See {@link #setDataType(DatasetDataType)}
     * @return data set content
     * @throws ZosDatasetException
     */
    public byte[] retrieveAsBinary() throws ZosDatasetException;

    /**
     * Store the content of the data set to the Results Archive Store
     * @param rasPath path in Results Archive Store
     * @throws ZosDatasetException
     */
    public void saveToResultsArchive(String rasPath) throws ZosDatasetException;
    
    /**
     * Returns true if the data set exists and is a partitioned data set
     * @return
     * @throws ZosDatasetException
     */
    public boolean isPDS() throws ZosDatasetException;
    
    /**
     * Returns the member name if supplied and is a partitioned data set
     * @return
     * @throws ZosDatasetException
     */
    public String getMemberName();

    /**
     * Create an empty member in the partitioned data set
     * @param memberName
     * @throws ZosDatasetException
     */
    public void memberCreate(@NotNull String memberName) throws ZosDatasetException;

    /**
     * Delete a member from the partitioned data set
     * @param memberName
     * @throws ZosDatasetException
     */
    public void memberDelete(@NotNull String memberName) throws ZosDatasetException;
    
    /**
     * Return true if the named member exists in the partitioned data set
     * @param memberName
     * @return
     * @throws ZosDatasetException
     */
    public boolean memberExists(@NotNull String memberName) throws ZosDatasetException;
    
    /**
     * Write content to the partitioned data set member in Text mode
     * @param memberName
     * @param content
     * @throws ZosDatasetException
     */
    public void memberStoreText(@NotNull String memberName, @NotNull String content) throws ZosDatasetException;

    /**
     * Write content to the partitioned data set member in Binary mode
     * @param memberName
     * @param content
     * @throws ZosDatasetException
     */
    public void memberStoreBinary(@NotNull String memberName, @NotNull byte[] content) throws ZosDatasetException;
    
    /**
     * Retrieve content from the partitioned data set member in Text mode
     * @param memberName
     * @return
     * @throws ZosDatasetException
     */
    public String memberRetrieveAsText(@NotNull String memberName) throws ZosDatasetException;

    /**
     * Retrieve content from the partitioned data set member in Binary mode
     * @param memberName
     * @return
     * @throws ZosDatasetException
     */
    public  byte[] memberRetrieveAsBinary(@NotNull String memberName) throws ZosDatasetException;

    /**
     * List the members of the partitioned data set
     * @return
     * @throws ZosDatasetException
     */
    public Collection<String> memberList() throws ZosDatasetException;

    /**
     * Store the content of the partitioned data set member to the Results Archive Store
     * @param memberName
     * @param rasPath path in Results Archive Store
     * @throws ZosDatasetException
     */
    public void memberSaveToResultsArchive(@NotNull String memberName, String rasPath) throws ZosDatasetException;
    
    /**
     * Set the data type ({@link DatasetDataType}) for store and retrieve of the data set content
     * @param dataType
     */
    public void setDataType(DatasetDataType dataType);

    /**
     * Set the     Volume serial(s) of the data set
     * @param volumes
     */
    public void setVolumes(String volumes);

    /**
     * Set the unit name of the data set
     * @param unit
     */
    public void setUnit(String unit);

    /**
     * Set the organization ({@link DatasetOrganization}) of the data set
     * @param organization
     */
    public void setDatasetOrganization(DatasetOrganization organization);

    /**
     * Set the {@link SpaceUnit} for data set, and specify how many 
     * primary and secondary extents to allocate.
     * 
     * @param spaceUnit
     * @param primaryExtents
     * @param secondaryExtents
     */
    public void setSpace(SpaceUnit spaceUnit, int primaryExtents, int secondaryExtents);

    /**
     * Set the number of directory blocks
     * @param directoryBlocks
     */
    public void setDirectoryBlocks(int directoryBlocks);

    /**
     * Set the {@link RecordFormat} for the data set
     * 
     * @param recordFormat
     */
    public void setRecordFormat(RecordFormat recordFormat);

    /**
     * Set the block size for the data set
     * 
     * @param blockSize
     */
    public void setBlockSize(int blockSize);

    /**
     * Set the record length for the data set
     * 
     * @param recordlength
     */
    public void setRecordlength(int recordlength);
    
    /**
     * Set the management class of the data set
     * @param managementClass
     */
    public void setManagementClass(String managementClass);
    
    /**
     * Set the storage class of the data set
     * @param storageClass
     */
    public void setStorageClass(String storageClass);
    
    /**
     * Set the data class of the data set
     * @param dataClass
     */
    public void setDataClass(String dataClass);

    /**
     * Set the {@link DSType} for the data set
     * 
     * @param dsType
     */
    public void setDatasetType(DSType dsType);
    
    /**
     * Return the name of the data set 
     * @return
     */
    public String getName();
    
    /**
     * Return the data type ({@link DatasetDataType}) for store and retrieve of the data set content
     * @return
     */
    public DatasetDataType getDataType();
    
    /**
     * Return the Volume serial(s) of the data set
     * @return
     */
    public String getVolumes();

    /**
     * Return the unit name of the data set
     * @return
     */
    public String getUnit();

    /**
     * Return the organization ({@link DatasetOrganization}) of the data set
     * @return
     */
    public DatasetOrganization getDatasetOrganization();

    /**
     * Return the allocation space unit of the data set
     * @return
     */
    public SpaceUnit getSpaceUnit();

    /**
     * Return primary allocation extents of the data set
     * @return
     */
    public int getPrimaryExtents();

    /**
     * Return secondary allocation extents of the data set
     * @return
     */
    public int getSecondaryExtents();

    /**
     * Return directory blocks of the partitioned data set
     * @return
     */
    public int getDirectoryBlocks();

    /**
     * Return the record format of the data set
     * @return
     */
    public RecordFormat getRecordFormat();

    /**
     * Return block size of the data set
     * @return
     */
    public int getBlockSize();

    /**
     * Return the record length of the data set
     * @return
     */
    public int getRecordlength();

    /**
     * Return the management class of the data set
     * @return
     */
    public String getManagementClass();

    /**
     * Return the storage class of the data set
     * @return
     */
    public String getStorageClass();

    /**
     * Return the data class of the data set
     * @return
     */
    public String getDataClass();

    /**
     * Get the {@link DSType} for the data set
     * 
     * @return
     */
    public DSType getDatasetType();
    
    /**
     * Get the number of extents of the data set
     * @return
     */
    public int getExtents();

    /**
     * Get the number of used extents of the data set
     * @return
     */
    public int getUsed();

    /**
     * Get the data set creation date
     * @return
     */
    public String getCreateDate();

    /**
     * Get the data set referenced date
     * @return
     */
    public String getReferencedDate();

    /**
     * Get the data set expiration date
     * @return
     */
    public String getExpirationDate();

    /**
     * Retrieve the attributes of an existing data set to make the values available in the getter methods
     * @throws ZosDatasetException
     */
    public void retrieveAttibutes() throws ZosDatasetException;

    /**
     * Return the attributes of the data set as a {@link String}<br> 
     * The format of the String is defined by the implementation
     * @return
     * @throws @ZosDatasetException
     */
    public String getAttibutesAsString() throws ZosDatasetException;

    /**
     * Set flag to control if the content of the data set should be automatically stored to the test output at test end. Defaults to false
     */    
    public void setShouldArchive(boolean shouldArchive);

    /**
     * Return flag that controls if the content of the data set should be automatically stored to the test output at test end
     */    
    public boolean shouldArchive();

    /**
     * Set flag to control if the data set should be automatically deleted from zOS at test end. Defaults to true
     */    
    public void setShouldCleanup(boolean shouldCleanup);

    /**
     * Return flag that controls if the data set should be automatically deleted from zOS at test end
     */    
    public boolean shouldCleanup();
}
