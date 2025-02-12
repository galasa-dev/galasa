/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.ivts.zos;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.Test;
import dev.galasa.artifact.BundleResources;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.core.manager.CoreManager;
import dev.galasa.core.manager.CoreManagerException;
import dev.galasa.core.manager.ICoreManager;
import dev.galasa.core.manager.Logger;
import dev.galasa.core.manager.TestProperty;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosImage;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.IZosUNIXFile.UNIXFileDataType;
import dev.galasa.zosfile.IZosUNIXFile.UNIXFileType;
import dev.galasa.zosfile.ZosFileHandler;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.ZosUNIXCommand;
import dev.galasa.zosunixcommand.ZosUNIXCommandException;

@Test
public class ZosManagerFileIVT {
    
    private final String IMG_TAG = "PRIMARY";
    
    @Logger
    public Log logger;
    
    @ZosImage(imageTag =  IMG_TAG)
    public IZosImage imagePrimary;
        
    @ZosFileHandler
    public IZosFileHandler fileHandler;

    @BundleResources
    public IBundleResources resources; 
    
    @CoreManager
    public ICoreManager coreManager;
    
    @ZosUNIXCommand(imageTag = IMG_TAG)
    public IZosUNIXCommand zosUNIXCommand;
    
    @TestProperty(prefix = "IVT.RUN",suffix = "NAME", required = false)
    public String providedRunName;
    
    private String runName  = new String();
    
    private String userName = new String();

    @Test
    public void preFlightTests() throws Exception {
        // Ensure we have the resources we need for testing
        assertThat(imagePrimary).isNotNull();
        assertThat(fileHandler).isNotNull();
        assertThat(coreManager).isNotNull();
        assertThat(resources).isNotNull();
        assertThat(logger).isNotNull();
        assertThat(imagePrimary.getDefaultCredentials()).isNotNull();
        userName = ((ICredentialsUsernamePassword)imagePrimary.getDefaultCredentials()).getUsername().toLowerCase();
        runName = coreManager.getRunName();
        logger.info("Using Run ID of: " + runName);
    }
    
    
    @Test
    public void unixFileCreate() throws ZosUNIXFileException, ZosUNIXCommandException, CoreManagerException {
        // Tests file creation using ZosFileHandler and UNIX File(s)
        // Establish file name and location
        String filePath = "/u/" + userName + "/GalasaTests/fileTest/" + runName + "/createMe";
        IZosUNIXFile unixFile = fileHandler.newUNIXFile(filePath, imagePrimary);

        String commandTestExist = "test -f " + filePath + " && echo \"File Exists\"";
        
        // Check file doesn't exist
        assertThat(unixFile.exists()).isFalse(); // Using fileManager
        assertThat(zosUNIXCommand.issueCommand(commandTestExist)).isEqualTo(""); // Using commandManager
        
        // Create File
        unixFile.create();
        
        // Check file was created
        assertThat(unixFile.exists()).isTrue(); // Using fileManager
        assertThat(zosUNIXCommand.issueCommand(commandTestExist))
            .isEqualToIgnoringWhitespace("File Exists"); // Using commandManager
    }
    
    @Test
    public void unixFileCreateWithPermissions() throws ZosUNIXFileException, ZosUNIXCommandException, CoreManagerException {
        // Tests file creation (with Specified Access Permissions) using ZosFileHandler and UNIX File(s)
        // Establish file name and location
        String fileName = "createMeWithPermissions";
        String filePath = "/u/" + userName + "/GalasaTests/fileTest/" + runName + "/" + fileName;
        IZosUNIXFile unixFile = fileHandler.newUNIXFile(filePath, imagePrimary);

        String commandTestExist = "test -f " + filePath + " && echo \"File Exists\"";
        String commandCheckPermissions = "ls -l " + filePath + " | grep " + fileName;
        
        // Check file doesn't exist
        assertThat(unixFile.exists()).isFalse(); // Using fileManager
        assertThat(zosUNIXCommand.issueCommand(commandTestExist)).isEqualTo(""); // Using commandManager
        
        // Create File With Permissions
        unixFile.create(PosixFilePermissions.fromString("rwxrwxrwx"));
        
        // Check file was created
        assertThat(unixFile.exists()).isTrue(); // Using fileManager
        assertThat(zosUNIXCommand.issueCommand(commandTestExist))
            .isEqualToIgnoringWhitespace("File Exists"); // Using commandManager
        
        // Check file has relevant permissions
        assertThat(unixFile.getAttributesAsString()).contains("Mode=-rwxrwxrwx"); // Using fileManager
        Set<PosixFilePermission> permissions = unixFile.getFilePermissions();
        assertThat(permissions).contains(PosixFilePermission.OWNER_READ);
        assertThat(permissions).contains(PosixFilePermission.OWNER_WRITE);
        assertThat(permissions).contains(PosixFilePermission.OWNER_EXECUTE);
        assertThat(permissions).contains(PosixFilePermission.GROUP_READ);
        assertThat(permissions).contains(PosixFilePermission.GROUP_WRITE);
        assertThat(permissions).contains(PosixFilePermission.GROUP_EXECUTE);
        assertThat(permissions).contains(PosixFilePermission.OTHERS_READ);
        assertThat(permissions).contains(PosixFilePermission.OTHERS_WRITE);
        assertThat(permissions).contains(PosixFilePermission.OTHERS_EXECUTE);
        assertThat(zosUNIXCommand.issueCommand(commandCheckPermissions))
            .startsWith("-rwxrwxrwx"); // Using commandManager
    }
    
    @Test
    public void unixFileDelete() throws ZosUNIXFileException, ZosUNIXCommandException, CoreManagerException {
        // Tests file deletion using ZosFileHandler and UNIX File(s)
        // Establish file name and location
        String filePath = "/u/" + userName + "/GalasaTests/fileTest/" + runName + "/deleteMe";
        IZosUNIXFile unixFile = fileHandler.newUNIXFile(filePath, imagePrimary);

        String commandTestExist = "test -f " + filePath + " && echo \"File Exists\"";
        
        // Create File
        unixFile.create();
        
        // Check file was created
        assertThat(unixFile.exists()).isTrue(); // Using fileManager
        assertThat(zosUNIXCommand.issueCommand(commandTestExist))
            .isEqualToIgnoringWhitespace("File Exists"); // Using commandManager
        
        // Delete File
        unixFile.delete();
        
        // Check file doesn't exist / was deleted
        assertThat(unixFile.exists()).isFalse(); // Using fileManager
        assertThat(zosUNIXCommand.issueCommand(commandTestExist)).isEqualTo(""); // Using commandManager
    }
    
    @Test
    public void unixFileDeleteNonEmptyDirectory() throws ZosUNIXFileException, ZosUNIXCommandException, CoreManagerException {
        // Tests directory deletion using ZosFileHandler and UNIX File(s)
        // Establish file name and location
        String filePath = "/u/" + userName + "/GalasaTests/fileTest/" + runName 
            + "/deleteThisDir/ThisToo";
        String deletePath = filePath.substring(0, filePath.indexOf("ThisToo"));
        IZosUNIXFile unixFile = fileHandler.newUNIXFile(filePath, imagePrimary);
        IZosUNIXFile unixDirectoryToDelete = fileHandler.newUNIXFile(deletePath, imagePrimary);
        
        String commandTestFileExists = "test -f " + filePath + " && echo \"File Exists\"";
        String commandTestDirExists = "test -f " + deletePath + " && echo \"File Exists\"";
        
        // Create File
        unixFile.create();
        
        // Check file was created
        assertThat(unixFile.exists()).isTrue(); // Using fileManager
        assertThat(zosUNIXCommand.issueCommand(commandTestFileExists))
            .isEqualToIgnoringWhitespace("File Exists"); // Using commandManager
        
        // Delete Directory
        unixDirectoryToDelete.directoryDeleteNonEmpty();
        
        // Check that the directory doesn't exist / was deleted
        assertThat(unixFile.exists()).isFalse(); // Using fileManager
        assertThat(unixDirectoryToDelete.exists()).isFalse(); // Using fileManager
        assertThat(zosUNIXCommand.issueCommand(commandTestFileExists)).isEqualTo(""); // Using commandManager
        assertThat(zosUNIXCommand.issueCommand(commandTestDirExists)).isEqualTo(""); // Using commandManager
    }
    
    @Test
    public void unixFileListDirectories() throws ZosUNIXFileException, ZosUNIXCommandException, CoreManagerException {
        // Tests directory listing using ZosFileHandler and UNIX File(s)
        // Establish file name and location
        String dirPath = "/u/" + userName + "/GalasaTests/fileTest/" + runName + "/testDir/";
        IZosUNIXFile unixFile = fileHandler.newUNIXFile(dirPath, imagePrimary);
        
        String commandCreateDirectories = "mkdir -p " + dirPath + "dirFoo";
        String commandCreateFiles = "touch " + dirPath + "fileBar";
        
        // Create Directory / File
        assertThat(zosUNIXCommand.issueCommand(commandCreateDirectories)).isEmpty();
        assertThat(zosUNIXCommand.issueCommand(commandCreateFiles)).isEmpty();

        // List files
        SortedMap<String, IZosUNIXFile> fileMap = unixFile.directoryList();
        assertThat(fileMap.containsKey(dirPath + "dirFoo"));
        assertThat(fileMap.containsKey(dirPath + "fileBar"));
        assertThat(fileMap.get(dirPath + "dirFoo").isDirectory()).isTrue();
        assertThat(fileMap.get(dirPath + "dirFoo").getUnixPath()).isEqualTo(dirPath + "dirFoo");
        assertThat(fileMap.get(dirPath + "fileBar").isDirectory()).isFalse();
        assertThat(fileMap.get(dirPath + "fileBar").getUnixPath()).isEqualTo(dirPath + "fileBar");
    }
    
    @Test
    public void unixFileListDirectoriesRecursively() throws ZosUNIXFileException, ZosUNIXCommandException, CoreManagerException {
        // Tests recursive directory listing using ZosFileHandler and UNIX File(s)
        // Establish file name and location
        String dirPath = "/u/" + userName + "/GalasaTests/fileTest/" + runName + "/testRecDir/";
        IZosUNIXFile unixFile = fileHandler.newUNIXFile(dirPath, imagePrimary);
        
        String commandCreateDirectories = "mkdir -p " + dirPath + "dirFoo " + dirPath + "dirBar";
        String commandCreateFiles = "touch " + dirPath + "dirFoo/fileFoo "  + dirPath + "dirBar/fileBar" ;
        
        // Create Directory / File
        zosUNIXCommand.issueCommand(commandCreateDirectories);
        zosUNIXCommand.issueCommand(commandCreateFiles);
                
        // List files
        SortedMap<String, IZosUNIXFile> fileMap = unixFile.directoryListRecursive();
        
        assertThat(fileMap.containsKey(dirPath + "dirFoo")).isTrue();
        assertThat(fileMap.containsKey(dirPath + "dirFoo/fileFoo")).isTrue();
        assertThat(fileMap.containsKey(dirPath + "dirBar")).isTrue();
        assertThat(fileMap.containsKey(dirPath + "dirBar/fileBar")).isTrue();
        
        assertThat(fileMap.get(dirPath + "dirFoo").getUnixPath()).isEqualTo(dirPath + "dirFoo");
        assertThat(fileMap.get(dirPath + "dirFoo/fileFoo").getUnixPath()).isEqualTo(dirPath + "dirFoo/fileFoo");
        assertThat(fileMap.get(dirPath + "dirBar").getUnixPath()).isEqualTo(dirPath + "dirBar");
        assertThat(fileMap.get(dirPath + "dirBar/fileBar").getUnixPath()).isEqualTo(dirPath + "dirBar/fileBar");
    }
    
    @Test
    public void unixFileGetAttributes() throws ZosUNIXFileException, ZosUNIXCommandException, CoreManagerException {
        // Tests reading file attributes using ZosFileHandler and UNIX File(s)
        // Establish file name and location
        String filePath = "/u/" + userName + "/GalasaTests/fileTest/" + runName + "/attributes";
        IZosUNIXFile unixFile = fileHandler.newUNIXFile(filePath, imagePrimary);
        
        // Create File With Permissions (to ensure permissions are known)
        unixFile.create(PosixFilePermissions.fromString("rwxrwxrwx"));
        
        // Test Attributes
        String attributes = unixFile.getAttributesAsString();
        assertThat(attributes).isNotEmpty();
        assertThat(attributes).contains("Name=" + filePath);
        assertThat(attributes).containsIgnoringCase("Type=file");
        assertThat(attributes).contains("Mode=-rwxrwxrwx");
        assertThat(attributes).contains("Size=0");
        assertThat(attributes).containsIgnoringCase("User=" + userName);
    }
    
    @Test
    public void unixFileDataTypeIsText() throws ZosUNIXFileException, ZosUNIXCommandException, CoreManagerException {
        // Tests file type using ZosFileHandler and UNIX File(s)
        // Establish file name and location
        String filePath = "/u/" + userName + "/GalasaTests/fileTest/" + runName + "/textFile";
        IZosUNIXFile unixFile = fileHandler.newUNIXFile(filePath, imagePrimary);
        
        // Create File
        unixFile.create();
        unixFile.storeText("Hello World"); // Text
        
        // Test file type
        assertThat(unixFile.getDataType()).isEqualTo(UNIXFileDataType.TEXT);
    }
    
    @Test
    public void unixFileGetDirectoryPath() throws ZosUNIXFileException, ZosUNIXCommandException, CoreManagerException {
        // Tests directory path using ZosFileHandler and UNIX File(s)
        // Establish file name and location
        String filePath = "/u/" + userName + "/GalasaTests/fileTest/" + runName + "/childFile";
        IZosUNIXFile unixFile = fileHandler.newUNIXFile(filePath, imagePrimary);
        
        // Create File
        unixFile.create();
        
        // Test Directory Path
        assertThat(unixFile.getDirectoryPath()).isEqualTo(filePath.substring(0, filePath.indexOf("/childFile")));
    }
    
    @Test
    public void unixFileGetFileName() throws ZosUNIXFileException, ZosUNIXCommandException, CoreManagerException {
        // Tests file name retrieval using ZosFileHandler and UNIX File(s)
        // Establish file name and location
        String filePath = "/u/" + userName + "/GalasaTests/fileTest/" + runName + "/uniqueFileName";
        IZosUNIXFile unixFile = fileHandler.newUNIXFile(filePath, imagePrimary);
        
        // Create File
        unixFile.create();
        
        // Test file name
        assertThat(unixFile.getFileName()).isEqualTo("uniqueFileName");
    }
    
    // // @Test // Fails because getFileType doesn't return the type from the machine
    public void unixFileGetFileDirType() throws ZosUNIXFileException, ZosUNIXCommandException, CoreManagerException {
        // Tests file/directory type retrieval using ZosFileHandler and UNIX File(s)
        // Establish file name and location
        String dirPath = "/u/" + userName + "/GalasaTests/fileTest/" + runName;
        String filePath = dirPath + "/aFile";
        
        IZosUNIXFile unixDir = fileHandler.newUNIXFile(dirPath, imagePrimary);
        IZosUNIXFile unixFile = fileHandler.newUNIXFile(filePath, imagePrimary);
        
        // Create File
        if (!unixDir.exists()) {
            unixDir.create();
        }
        unixFile.create();
        
        // Test file type
        assertThat(zosUNIXCommand.issueCommand("ls -ld " + dirPath))
            .startsWith("d");
        assertThat(zosUNIXCommand.issueCommand("ls -l " + dirPath + " | grep aFile"))
            .startsWith("-");
        assertThat(unixDir.getFileType()).isEqualTo(UNIXFileType.DIRECTORY);
        assertThat(unixFile.getFileType()).isEqualTo(UNIXFileType.FILE);
    }
    
    @Test
    public void unixFileGetGroup() throws ZosUNIXFileException, ZosUNIXCommandException, CoreManagerException {
        // Tests group using ZosFileHandler and UNIX File(s)
        // Establish file name and location
        String filePath = "/u/" + userName + "/GalasaTests/fileTest/" + runName + "/groupFile";
        IZosUNIXFile unixFile = fileHandler.newUNIXFile(filePath, imagePrimary);
        
        // Create File
        unixFile.create();
        
        String machineGroupId = zosUNIXCommand.issueCommand("ls -ld " + filePath + " | awk '{print $4}'");
        assertThat(machineGroupId).isNotEmpty();
        
        // Test file type
        assertThat(unixFile.getGroup()).isEqualToIgnoringWhitespace(machineGroupId);
    }
    
    //@Test - Fails on RSE because time zone is set to EST
    public void unixFileGetLastModified() throws ZosUNIXFileException, ZosUNIXCommandException, CoreManagerException, ParseException {
        // Tests group using ZosFileHandler and UNIX File(s)
        // Establish file name and location
        String filePath = "/u/" + userName + "/GalasaTests/fileTest/" + runName + "/datedFile";
        IZosUNIXFile unixFile = fileHandler.newUNIXFile(filePath, imagePrimary);

        // Create File
        unixFile.create();
        
        // Get TimeZone Shortcode for Z/OS TZ env var
        // zosUNIXCommand does not set a TimeZone when issuing a command
        // If we don't do this, the test will fail unless the machine running tests is in GMT+0
        DateTimeFormatter zoneFormatter = DateTimeFormatter.ofPattern("zzz", Locale.forLanguageTag("en"));
        final ZonedDateTime timestamp = ZonedDateTime.of(LocalDateTime.now(), Calendar.getInstance().getTimeZone().toZoneId());
        String timeZoneFormatted = Calendar.getInstance().getTimeZone().getDisplayName(false, TimeZone.SHORT) 
                + Integer.toString((Calendar.getInstance().getTimeZone().getRawOffset() / 60 / 1000))
                + timestamp.format(zoneFormatter);
        String regexTimeZone = "[A-Z]{3,5}\\-{0,1}\\d[A-Z]{3,5}";
        Pattern patternTimeZone = Pattern.compile(regexTimeZone, Pattern.MULTILINE);
        assertThat(timeZoneFormatted).containsPattern(patternTimeZone);
        
        // Set timezone, then fetch modified time/date using zosUNIXCommand
        // Then regex for month/date/time (e.g. "Mar 23 12:34")
        String machineFileModified = zosUNIXCommand
            .issueCommand("TZ=" + timeZoneFormatted + "; ls -ld " + filePath);
        assertThat(machineFileModified).isNotEmpty();
        final String regexTimeFromLS = "[\\-rwdx]{10}\\s+\\d\\s[a-zA-Z0-9]+\\s+[a-zA-Z0-9]+\\s+0\\s([a-zA-Z]{3})\\s+(\\d{1,2})\\s(\\d{2}:\\d{2})";
        final Pattern patternTimeFromLS = Pattern.compile(regexTimeFromLS, Pattern.MULTILINE);
        Matcher matcher = patternTimeFromLS.matcher(machineFileModified);
        assertThat(matcher.find()).isTrue();
        String month = matcher.group(1);
        String day = matcher.group(2);
        String time = matcher.group(3);
        
        // Convert scraped date from machine into usable epoch date
        SimpleDateFormat lsDateFormat = new SimpleDateFormat("yyyy MMM dd HH:mm");
        String dateToBeParsed = Integer.toString(Calendar.getInstance().get(Calendar.YEAR)) + " " + month + " " + day + " " + time;
        Date machineDate = lsDateFormat.parse(dateToBeParsed);
        
        // Get date from getLastModified (method being tested) and convert to a comparable format
        // (Ignore seconds because scraped date/time doesn't have seconds)
        String actualModified = unixFile.getLastModified().replaceAll("T", " ");
        SimpleDateFormat methodDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm"); 
        Date methodDate = methodDateFormat.parse(actualModified.substring(0, actualModified.lastIndexOf(":")));
        
        // Test file modification date
        assertThat(methodDate).isEqualTo(machineDate);
    }
    
    // // @Test // Fails because getSize doesn't return the size from the machine
    public void unixFileGetSize() throws ZosUNIXFileException, ZosUNIXCommandException, CoreManagerException {
        // Tests group using ZosFileHandler and UNIX File(s)
        // Establish file name and location
        String filePath = "/u/" + userName + "/GalasaTests/fileTest/" + runName + "/sizedFile";
        IZosUNIXFile unixFile = fileHandler.newUNIXFile(filePath, imagePrimary);
        
        // Create File
        unixFile.create();
        unixFile.storeText("This file will be more than one byte.");
        
        int size = unixFile.getSize();
        
        assertThat(size).isGreaterThan(0);
    }
}
