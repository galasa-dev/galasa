/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile;

import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import java.util.SortedMap;

import javax.validation.constraints.NotNull;

import dev.galasa.zosfile.IZosDataset.DatasetDataType;

/**
 * Representation of a UNIX file or directory.
 *
 */
public interface IZosUNIXFile {
    
    /**
     * Enumeration of zOS UNIX file types:
     * <li>{@link #FILE}</li>
     * <li>{@link #CHARACTER}</li>
     * <li>{@link #DIRECTORY}</li>
     * <li>{@link #EXTLINK}</li>
     * <li>{@link #SYMBLINK}</li>
     * <li>{@link #FIFO}</li>
     * <li>{@link #SOCKET}</li>
     * <li>{@link #UNKNOWN}</li>
     */
    public enum UNIXFileType {
        
        FILE("file"),
        CHARACTER("character"),
        DIRECTORY("directory"),
        EXTLINK("extlink"),
        SYMBLINK("symblink"),
        FIFO("FIFO"),
        SOCKET("socket"),
        UNKNOWN("UNKNOWN");
        
        private String fileType;
        
        UNIXFileType(String dataType) {
            this.fileType = dataType;
        }
        
        @Override
        public String toString() {
            return fileType;
        }
    }
    
    /**
     * Enumeration of data type for store and retrieve of data set content:
     * <li>{@link #TEXT}</li>
     * <li>{@link #BINARY}</li>
     */
    public enum UNIXFileDataType {
        /**
         * Content is between ISO8859-1 on the client and EBCDIC on the host
         */
        TEXT("text"),
        /**
         * No data conversion is performed
         */
        BINARY("binary");
        
        private String dataType;
        
        UNIXFileDataType(String dataType) {
            this.dataType = dataType;
        }
        
        @Override
        public String toString() {
            return dataType;
        }
    }
    
    /**
     * Create the zOS UNIX file or directory
     * @return
     * @throws ZosUNIXFileException 
     */
    public IZosUNIXFile create() throws ZosUNIXFileException;
    
    /**
     * Create the zOS UNIX file or directory with the supplied Access Permissions 
     * @param accessPermissions the access permissions, e.g.<br>
     * {@code PosixFilePermissions.fromString("rwxrwxrwx")}
     * @return
     * @throws ZosUNIXFileException 
     */
    public IZosUNIXFile create(Set<PosixFilePermission> accessPermissions) throws ZosUNIXFileException;

    /**
     * Delete the zOS UNIX file or directory from the zOS image. Attempting to delete a non-empty directory will throw {@link ZosUNIXFileException}
     * @return deleted
     * @throws ZosUNIXFileException
     */
    public boolean delete() throws ZosUNIXFileException;
    
    /**
     * Recursively delete the zOS UNIX directory and its contents from the zOS image
     * @return deleted
     * @throws ZosUNIXFileException
     */
    public boolean directoryDeleteNonEmpty() throws ZosUNIXFileException;

    /**
     * Return true if the zOS UNIX exists on the zOS image
     * @return
     * @throws ZosUNIXFileException
     */
    public boolean exists() throws ZosUNIXFileException;

    /**
     * Write the content to the zOS UNIX file on the zOS image in Text mode
     * <p>See {@link #setDataType(UNIXFileDataType)}
     */
    public void storeText(String content) throws ZosUNIXFileException;

    /**
     * Write content to the zOS UNIX file on the zOS image in Binary mode 
     * <p>See {@link #setDataType(UNIXFileDataType)}
     * @param content
     * @throws ZosUNIXFileException
     */
    public void storeBinary(@NotNull byte[] content) throws ZosUNIXFileException;

    /**
     * Retrieve the content of the zOS UNIX file from the zOS image in Text mode
     * <p>See {@link #setDataType(UNIXFileDataType)}
     * @throws ZosUNIXFileException
     */
    public String retrieveAsText() throws ZosUNIXFileException;

    /**
     * Retrieve content of the zOS UNIX file from the zOS image in Binary mode
     * <p>See {@link #setDataType(UNIXFileDataType)}
     * @return data set content
     * @throws ZosUNIXFileException
     */
    public byte[] retrieveAsBinary() throws ZosUNIXFileException;

    /**
     * Recursively store the content of the zOS UNIX file or directory to the Results Archive Store
     * @param rasPath path in Results Archive Store
     * @throws ZosUNIXFileException
     */
    public void saveToResultsArchive(String rasPath) throws ZosUNIXFileException;
    
    /**
     * Return true if this object represents a zOS UNIX directory
     * @return
     * @throws ZosUNIXFileException
     */
    public boolean isDirectory() throws ZosUNIXFileException;
    
    /**
     * Returns sorted {@link SortedMap} the zOS UNIX files and directories in this zOS UNIX directory
     * @return
     * @throws ZosUNIXFileException
     */
    public SortedMap<String, IZosUNIXFile> directoryList() throws ZosUNIXFileException;
    
    /**
     * Returns recursive sorted {@link SortedMap} the zOS UNIX files and directories in this zOS UNIX directory
     * @return
     * @throws ZosUNIXFileException
     */
    public SortedMap<String, IZosUNIXFile> directoryListRecursive() throws ZosUNIXFileException;
    
    /**
     * Set the data type ({@link UNIXFileDataType}) for store and retrieve of the zOS UNIX file content
     * @param dataType
     */
    public void setDataType(UNIXFileDataType dataType);
    
    /**
     * Change the Access Permissions of the zOS UNIX file  
     * @param accessPermissions the access permissions, e.g.<br>
     * {@code PosixFilePermissions.fromString("rwxrwxrwx")}
     * @param recursive change the access permissions recursively
     * @throws ZosUNIXFileException
     */
    public void setAccessPermissions(Set<PosixFilePermission> accessPermissions, boolean recursive) throws ZosUNIXFileException;
    
    /**
     * Return the zOS UNIX file type ({@link UNIXFileType})
     */
    public UNIXFileType getFileType();
    
    /**
     * Return the data type ({@link UNIXFileDataType}) for store and retrieve of the zOS UNIX file content
     */
    public UNIXFileDataType getDataType();
    
    /**
     * Return the path of the zOS UNIX file or directory
     */
    public String getUnixPath();
    
    /**
     * Return the file name of the zOS UNIX file, or null if this object represents a directory 
     * @return
     */
    public String getFileName();

    /**
     * Return the full directory path for the zOS UNIX file or directory
     * @return
     */
    public String getDirectoryPath() throws ZosUNIXFileException;
    
    public Set<PosixFilePermission> getFilePermissions() throws ZosUNIXFileException;
    
    public int getSize() throws ZosUNIXFileException;
    
    public String getLastModified() throws ZosUNIXFileException;
    
    public String getUser() throws ZosUNIXFileException;
    
    public String getGroup() throws ZosUNIXFileException;

    /**
     * Retrieve the attributes of an existing data set to make the values available in the getter methods
     * @throws ZosUNIXFileException
     */
    public void retrieveAttributes() throws ZosUNIXFileException;

    /**
     * Return the attributes of the zOS UNIX file or directory as a {@link String} 
     * @return
     * @throws ZosUNIXFileException
     */
    public String getAttributesAsString() throws ZosUNIXFileException;

    /**
     * Set flag to control if the content of the zOS UNIX path should be stored to the test output. Defaults to false
     */    
    public void setShouldArchive(boolean shouldArchive);

    /**
     * Return flag that controls if the content of the zOS UNIX path should be stored to the test output
     */    
    public boolean shouldArchive();

    /**
     * Set flag to control if the zOS UNIX path should be automatically deleted from zOS at test end. Defaults to true
     */    
    public void setShouldCleanup(boolean shouldCleanup);

    /**
     * Return flag that controls if the zOS UNIX path should be automatically deleted from zOS at test end
     */    
    public boolean shouldCleanup();
    
	/**
	 * Convert  {@link Set}&lt;{@link PosixFilePermission}&gt; to Symbolic Notation (e.g. rwxwrxrwx)
	 * @param accessPermissions
	 * @return a {@link String} containing the file permissions in Symbolic Notation
	 */	
    public static String posixFilePermissionsToSymbolicNotation(Set<PosixFilePermission> accessPermissions) {
		StringBuilder permissions = new StringBuilder();
		permissions.append("---------");
		for (PosixFilePermission posixFilePermission : accessPermissions) {
			switch (posixFilePermission) {
			case OWNER_READ:
				permissions.replace(0, 1, "r");
				break;
			case OWNER_WRITE:
				permissions.replace(1, 2, "w");
				break;
			case OWNER_EXECUTE:
				permissions.replace(2, 3, "x");
				break;
			case GROUP_READ:
				permissions.replace(3, 4, "r");
				break;
			case GROUP_WRITE:
				permissions.replace(4, 5, "w");
				break;
			case GROUP_EXECUTE:
				permissions.replace(5, 6, "x");
				break;
			case OTHERS_READ:
				permissions.replace(6, 7, "r");
				break;
			case OTHERS_WRITE:
				permissions.replace(7, 8, "w");
				break;
			case OTHERS_EXECUTE:
				permissions.replace(8, 9, "x");
				break;
			default:
				break;
			}
		}
		return permissions.toString();
	}

    /**
	 * Convert  {@link Set}&lt;{@link PosixFilePermission}&gt; to Numeric Notation (e.g. 777)
	 * @param accessPermissions
	 * @return a {@link String} containing the file permissions in Numeric Notation
	 */	
    public static String posixFilePermissionsToOctal(Set<PosixFilePermission> accessPermissions) {
		int permissions = 0;
		for (PosixFilePermission posixFilePermission : accessPermissions) {
			switch (posixFilePermission) {
			case OWNER_READ:
				permissions = permissions + 400;
				break;
			case OWNER_WRITE:
				permissions = permissions + 200;
				break;
			case OWNER_EXECUTE:
				permissions = permissions + 100;
				break;
			case GROUP_READ:
				permissions = permissions + 40;
				break;
			case GROUP_WRITE:
				permissions = permissions + 20;
				break;
			case GROUP_EXECUTE:
				permissions = permissions + 10;
				break;
			case OTHERS_READ:
				permissions = permissions + 4;
				break;
			case OTHERS_WRITE:
				permissions = permissions + 2;
				break;
			case OTHERS_EXECUTE:
				permissions = permissions + 1;
				break;
			default:
				break;
			}
		}
		return String.format("%03d", permissions);
	}
}
