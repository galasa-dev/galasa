/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosliberty.internal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dev.galasa.artifact.IArtifactManager;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJob.JobStatus;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosconsole.IZosConsole;
import dev.galasa.zosconsole.ZosConsoleException;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.IZosUNIXFile.UNIXFileDataType;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosliberty.IZosLibertyServer;
import dev.galasa.zosliberty.IZosLibertyServerLog;
import dev.galasa.zosliberty.IZosLibertyServerLogs;
import dev.galasa.zosliberty.IZosLibertyServerXml;
import dev.galasa.zosliberty.ZosLibertyManagerException;
import dev.galasa.zosliberty.ZosLibertyServerException;
import dev.galasa.zosliberty.internal.properties.DefaultTimeout;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.ZosUNIXCommandException;

public class ZosLibertyServerImpl implements IZosLibertyServer {

    private static final Log logger = LogFactory.getLog(ZosLibertyServerImpl.class);
    
    private ZosLibertyImpl zosLiberty;
    private ZosLibertyManagerImpl zosLibertyManager;
    private IZosFileHandler zosFileHandler;
    private IZosBatch zosBatch;
    private IZosConsole zosConsole;
    private IZosUNIXCommand zosUNIXCommand;
    private IArtifactManager artifactManager;
    private IZosImage zosImage;
    private IZosUNIXFile wlpInstallDir;
    private IZosUNIXFile wlpUserDir;
    private IZosUNIXFile wlpOutputDir;
    private IZosUNIXFile dropinsDir;
    private IZosUNIXFile javaHome;
    private IZosUNIXFile logsDir;
    private String serverName = "defaultServer";
    private IZosLibertyServerXml serverXml;
    private IZosLibertyServerLogs libertyServerLogs;
    private IZosBatchJob zosLibertySeverJob;
    private IZosUNIXFile tmpWorkDirDir;
    private IZosUNIXFile sharedAppDir;
    private IZosUNIXFile serverConfigDir;
    private IZosUNIXFile serverOutputDir;
    private IZosUNIXFile sharedConfigDir;
    private IZosUNIXFile sharedResourcesDir;
    private IZosUNIXFile serverResourcesDir;
    private IZosUNIXFile serverSecurityDir;
	private int defaultTimeout = -1;
    
    private static final String SLASH_SYBMOL = "/";
    private static final String SEMI_COLON_SYMBOL = ";";

    private static final String APP_STARTED_MESSAGE_ID = "CWWKZ0001I";
    private static final String APP_STOPPED_MESSAGE_ID = "CWWKZ0009I";
    private static final String SERVER_STARTED_MESSAGE_ID = "CWWKF0011I";
    private static final String SERVER_STOPPED_MESSAGE_ID = "CWWKE0036I";
    
    public ZosLibertyServerImpl(ZosLibertyImpl zosLiberty, IZosImage zosImage, String wlpInstallDir, String wlpUserDir, String wlpOutputDir) throws ZosLibertyServerException {
        this.zosLiberty = zosLiberty;
        try {
            this.zosLibertyManager = this.zosLiberty.getZosLibertyManager();
        } catch (ZosLibertyManagerException e) {
            throw new ZosLibertyServerException("Unable to get zOS Liberty Manager", e);
        }
        try {
            this.zosFileHandler = this.zosLibertyManager.getZosFileHandler();
        } catch (ZosLibertyManagerException e) {
            throw new ZosLibertyServerException("Unable to get zOS File Handler", e);
        }
        this.zosUNIXCommand = this.zosLibertyManager.getZosUNIXCommand(zosImage);
        this.zosBatch = this.zosLibertyManager.getZosBatch(zosImage);
        try {
            this.zosConsole = this.zosLibertyManager.getZosConsole(zosImage);
        } catch (ZosLibertyManagerException e) {
            throw new ZosLibertyServerException("Unable to get zOS Console", e);
        }
        this.artifactManager = this.zosLibertyManager.getArtifactManager();
        this.zosImage = zosImage;
        if (wlpInstallDir == null) {
            getWlpInstallDir();
        } else {
            setWlpInstallDir(wlpInstallDir);
        }
        if (wlpUserDir == null) {
            getWlpUserDir();
        } else {
            setWlpUserDir(wlpUserDir);
        }
        if (wlpOutputDir == null) {
            getWlpOutputDir();
        } else {
            setWlpOutputDir(wlpOutputDir);
        }
        this.serverXml = new ZosLibertyServerXmlImpl(getServerXmlUnixFile());
    }
    
    @Override
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public void setWlpInstallDir(String wlpInstallDir) throws ZosLibertyServerException {
        try {
            this.wlpInstallDir = this.zosFileHandler.newUNIXFile(toDirectory(wlpInstallDir), zosImage);
        } catch (ZosUNIXFileException e) {
            throw new ZosLibertyServerException("Unable to create WLP install directory zOS UNIX file object", e);
        }
    }

    @Override
    public void setWlpUserDir(String wlpUserDir) throws ZosLibertyServerException {
        try {
            this.wlpUserDir = this.zosFileHandler.newUNIXFile(toDirectory(wlpUserDir), getZosImage());
        } catch (ZosUNIXFileException e) {
            throw new ZosLibertyServerException("Unable to create WLP user directory zOS UNIX file object", e);
        }
    }

    @Override
    public void setWlpOutputDir(String wlpOutputDir) throws ZosLibertyServerException {
        try {
            this.wlpOutputDir = this.zosFileHandler.newUNIXFile(toDirectory(wlpOutputDir), getZosImage());
        } catch (ZosUNIXFileException e) {
            throw new ZosLibertyServerException("Unable to create WLP output directory zOS UNIX file object", e);
        }
    }

    @Override
    public void setJavaHome(String javaHome) throws ZosLibertyServerException {
        try {
            this.javaHome = this.zosFileHandler.newUNIXFile(toDirectory(javaHome), getZosImage());
        } catch (ZosUNIXFileException e) {
            throw new ZosLibertyServerException("Unable to create Java Home zOS UNIX file object", e);
        }
    }

    @Override
    public String getVersion() throws ZosLibertyServerException {
        try {
            UnixCommandResponse commandResponse = issueLibertyCommand("productInfo version", false);
            int rc = commandResponse.getRc();
            if (rc != 0) {
                throw new ZosLibertyServerException("Invalid return code from the productInfo script: RC=" + rc);
            }
            Pattern pattern = Pattern.compile(".*Product version:\\s*([^\\n\\r]*)", Pattern.MULTILINE);
            String response = commandResponse.getResponse();
            Matcher matcher = pattern.matcher(response);
            if (matcher.find() && matcher.groupCount() > 0) {
                return matcher.group(1);
            } else {
                throw new ZosLibertyServerException("Unable to get Liberty version from the productInfo script:\n" + response);
            }
        } catch (ZosLibertyServerException e) {
            throw new ZosLibertyServerException("Unable to get Liberty version", e);
        }
    }
    
    @Override
    public void build() throws ZosLibertyServerException {
        if (getServerXml() != null) {
            getServerXml().store();
        }
    }

    @Override
    public String getServerName() {
        if (this.serverName == null) {
            this.serverName = "defaultServer";
        }
        return this.serverName;
    }

    @Override
    public IZosUNIXFile getWlpInstallDir() throws ZosLibertyServerException {
        if (this.wlpInstallDir == null) {
            try {
                if (getZosImage().getLibertyInstallDir() == null) {
                    throw new ZosLibertyServerException("The Liberty install directory not been set and a value has not been supplied in the CPS for zOS image " + getZosImage().getImageID());
                }
                setWlpInstallDir(getZosImage().getLibertyInstallDir());
            } catch (ZosManagerException e) {
                throw new ZosLibertyServerException("Unable to get Liberty install directory", e);
            }
        }
        return this.wlpInstallDir;
    }

    @Override
    public IZosUNIXFile getWlpUserDir() throws ZosLibertyServerException {
        if (this.wlpUserDir == null) {
            try {
                IZosUNIXFile runTemporaryUNIXPath = this.zosFileHandler.newUNIXFile(getZosImage().getRunTemporaryUNIXPath(), getZosImage());
                if (!runTemporaryUNIXPath.exists()) {
                    runTemporaryUNIXPath.create(PosixFilePermissions.fromString("rwxrwxrwx"));
                    runTemporaryUNIXPath.setShouldCleanup(true);
                }
                StringBuilder path = new StringBuilder();
                path.append(runTemporaryUNIXPath);
                path.append(SLASH_SYBMOL);
                path.append("wlp");
                path.append(SLASH_SYBMOL);
                path.append("usr");
                path.append(SLASH_SYBMOL);
                setWlpUserDir(path.toString());
            } catch (ZosManagerException e) {
                throw new ZosLibertyServerException("Unable to get Liberty user directory", e);
            }
        }
        return this.wlpUserDir;
    }

    @Override
    public IZosUNIXFile getWlpOutputDir() throws ZosLibertyServerException {
        if (this.wlpOutputDir == null) {
            setWlpOutputDir(getServerConfigDir().getUnixPath());
        }
        return this.wlpOutputDir;
    }
    
    @Override
    public IZosUNIXFile getSharedAppDir() throws ZosLibertyServerException {
        if (this.sharedAppDir == null) {
            try {
                this.sharedAppDir = this.zosFileHandler.newUNIXFile(getWlpUserDir().getUnixPath() + "shared/apps/", getZosImage());
            } catch (ZosManagerException e) {
                throw new ZosLibertyServerException("Unable to get shared app directory", e);
            }
        }
        return this.sharedAppDir;
    }

    @Override
    public IZosUNIXFile getServerConfigDir() throws ZosLibertyServerException {
        if (this.serverConfigDir == null) {
            try {
                this.serverConfigDir = this.zosFileHandler.newUNIXFile(getWlpUserDir().getUnixPath() + "servers/" + getServerName() + SLASH_SYBMOL, getZosImage());
            } catch (ZosManagerException e) {
                throw new ZosLibertyServerException("Unable to get server config directory", e);
            }
        }
        return this.serverConfigDir;
    }

    @Override
    public IZosUNIXFile getServerOutputDir() throws ZosLibertyServerException {
        if (this.serverOutputDir == null) {
            try {
                this.serverOutputDir = this.zosFileHandler.newUNIXFile(getServerConfigDir().getUnixPath(), getZosImage());
            } catch (ZosManagerException e) {
                throw new ZosLibertyServerException("Unable to get server output directory", e);
            }
        }
        return this.serverOutputDir;
    }

    @Override
    public IZosUNIXFile getSharedConfigDir() throws ZosLibertyServerException {
        if (this.sharedConfigDir == null) {
            try {
                this.sharedConfigDir = this.zosFileHandler.newUNIXFile(getWlpUserDir().getUnixPath() + "shared/config/", getZosImage());
            } catch (ZosManagerException e) {
                throw new ZosLibertyServerException("Unable to get server output directory", e);
            }
        }
        return this.sharedConfigDir;
    }

    @Override
    public IZosUNIXFile getSharedResourcesDir() throws ZosLibertyServerException {
        if (this.sharedResourcesDir == null) {
            try {
                this.sharedResourcesDir = this.zosFileHandler.newUNIXFile(getWlpUserDir().getUnixPath() + "shared/resources/", getZosImage());
            } catch (ZosManagerException e) {
                throw new ZosLibertyServerException("Unable to get server output directory", e);
            }
        }
        return this.sharedResourcesDir;
    }

    public IZosUNIXFile getResourcesDir() throws ZosLibertyServerException {
        if (this.serverResourcesDir == null) {
            try {
                this.serverResourcesDir = this.zosFileHandler.newUNIXFile(getServerConfigDir() + "resources/", getZosImage());
            } catch (ZosManagerException e) {
                throw new ZosLibertyServerException("Unable to get server resources directory", e);
            }
        }
        return this.serverResourcesDir;
    }

    public IZosUNIXFile getServerSecurityResourcesDir() throws ZosLibertyServerException {
        if (this.serverSecurityDir == null) {
            try {
                this.serverSecurityDir = this.zosFileHandler.newUNIXFile(getResourcesDir() + "security/", getZosImage());
            } catch (ZosManagerException e) {
                throw new ZosLibertyServerException("Unable to get server security directory", e);
            }
        }
        return this.serverSecurityDir;
    }

    @Override
    public IZosUNIXFile getLogsDirectory() throws ZosLibertyServerException {
        if (this.logsDir == null) {
            try {
                this.logsDir = this.zosFileHandler.newUNIXFile(getWlpOutputDir().getUnixPath() + SLASH_SYBMOL + "logs/", getZosImage());
            } catch (ZosUNIXFileException e) {
                throw new ZosLibertyServerException("Unable to get logs directory", e);
            }
    
        }
        return this.logsDir;
    }

    @Override
    public IZosUNIXFile getDropinsDir() throws ZosLibertyServerException {
        if (this.dropinsDir == null) {
            try {
                this.dropinsDir = this.zosFileHandler.newUNIXFile(getWlpOutputDir().getUnixPath() + SLASH_SYBMOL + "dropins/", getZosImage());
            } catch (ZosUNIXFileException e) {
                throw new ZosLibertyServerException("Unable to get dropins directory", e);
            }
        }
        return this.dropinsDir;
    }

    @Override
    public IZosUNIXFile getJavaHome() throws ZosLibertyServerException {
        if (this.javaHome == null) {
            try {
                if (getZosImage().getJavaHome() == null) {
                    throw new ZosLibertyServerException("The Java Home directory not been set and a value has not been supplied in the CPS for zOS image " + getZosImage().getImageID());
                }
                setJavaHome(getZosImage().getJavaHome());
            } catch (ZosManagerException e) {
                throw new ZosLibertyServerException("Unable to get Java Home directory", e);
            }
        }
        return this.javaHome;
    }

    @Override
    public IZosImage getZosImage() {
        return this.zosImage;
    }

    @Override
    public void setServerXml(IZosLibertyServerXml serverXml) throws ZosLibertyServerException {
        this.serverXml = serverXml;
    }

    @Override
    public IZosLibertyServerXml getServerXml() throws ZosLibertyServerException {
        return this.serverXml;
    }

    @Override
    public IZosLibertyServerXml loadServerXmlFromFileSystem() throws ZosLibertyServerException {
        getServerXml().loadFromFileSystem();
        return getServerXml();
    }

    @Override
    public IZosLibertyServerLogs getLogs() throws ZosLibertyServerException {
        if (this.libertyServerLogs == null) {
            try {
                this.libertyServerLogs = new ZosLibertyServerLogsImpl(getLogsDirectory(), this.zosLibertyManager);
            } catch (ZosLibertyServerException e) {
                throw new ZosLibertyServerException("Unable to get server logs", e);
            }
        }
        return this.libertyServerLogs;
    }

    @Override
    public void clearLogs() throws ZosLibertyServerException {        
        getLogs().delete();
    }

    @Override
    public void checkpointLogs() throws ZosLibertyServerException {
        IZosLibertyServerLog log = getLogs().getMessagesLog();
        if (log != null) {
            log.checkpoint();
        }
        log = getLogs().getTraceLog();
        if (log != null) {
            log.checkpoint();
        }
    }

    @Override
    public String retrieveMessagesLog() throws ZosLibertyServerException {
        try {
            return new String(retrieveLog(getLogs().getTraceLog()), StandardCharsets.UTF_8);
        } catch (ZosLibertyServerException e) {
            throw new ZosLibertyServerException("Unable to get messages.log", e);
        }
    }

    @Override
    public String retrieveTraceLog() throws ZosLibertyServerException {
        try {
            return new String(retrieveLog(getLogs().getTraceLog()), StandardCharsets.UTF_8);
        } catch (ZosLibertyServerException e) {
            throw new ZosLibertyServerException("Unable to get trace.log", e);
        }
    }

    public byte[] retrieveLog(IZosLibertyServerLog log) throws ZosLibertyServerException {
        try {
            if (log !=  null && log.getZosUNIXFile().exists()) {
                return log.getZosUNIXFile().retrieveAsBinary();
            } else {
                return new byte[0];
            }
        } catch (ZosUNIXFileException e) {
            throw new ZosLibertyServerException("Unable to get log file", e);
        }
    }

    @Override
    public String retrieveMessagesLogSinceCheckpoint() throws ZosLibertyServerException {
        try {
            IZosLibertyServerLog messagesLog = getLogs().getMessagesLog();
            InputStream content = new ByteArrayInputStream(retrieveLog(messagesLog));
            long skipped = content.skip(messagesLog .getCheckpoint());
            if (skipped != messagesLog.getCheckpoint()) {
                throw new IOException("Failed to skip " + messagesLog.getCheckpoint() + " bytes. Actual bytes skipped " + skipped);
            }                
            return IOUtils.toString(content, StandardCharsets.UTF_8);
        } catch (ZosLibertyServerException | IOException e) {
            throw new ZosLibertyServerException("Unable to get messages.log since checkpoint", e);
        }
    }

    @Override
    public String retrieveTraceLogSinceCheckpoint() throws ZosLibertyServerException {
        try {
            IZosLibertyServerLog traceLog = getLogs().getTraceLog();
            InputStream content = new ByteArrayInputStream(retrieveLog(traceLog));
            long skipped = content.skip(traceLog .getCheckpoint());
            if (skipped != traceLog.getCheckpoint()) {
                throw new IOException("Failed to skip " + traceLog.getCheckpoint() + " bytes. Actual bytes skipped " + skipped);
            }                
            return IOUtils.toString(content, StandardCharsets.UTF_8);
        } catch (ZosLibertyServerException | IOException e) {
            throw new ZosLibertyServerException("Unable to get trace.log since checkpoint", e);
        }
    }

    @Override
    public int create() throws ZosLibertyServerException {
        try {
            return issueLibertyCommand("server create " + getServerName(), false).getRc();
        } catch (ZosLibertyServerException e) {
            throw new ZosLibertyServerException("Unable to get create Liberty server", e);
        }
    }

    @Override
    public int start() throws ZosLibertyServerException {
        try {
            return issueLibertyCommand("server start " + getServerName(), false).getRc();
        } catch (ZosLibertyServerException e) {
            throw new ZosLibertyServerException("Unable to start Liberty server", e);
        }
    }

    @Override
    public int run() throws ZosLibertyServerException {
        try {
            this.zosLibertySeverJob = this.zosBatch.submitJob(buildServerJcl(), null);
            if (waitForStart() != 0) {
                if (this.zosLibertySeverJob.getStatus() != JobStatus.ACTIVE) {
                    throw new ZosLibertyServerException("Liberty server batch job ended " + this.zosLibertySeverJob.getRetcode());
                }
            }
            return status();
        } catch (ZosBatchException | ZosLibertyServerException e) {
            throw new ZosLibertyServerException("Unable to start Liberty server", e);
        }
    }

    @Override
    public int waitForStart() throws ZosLibertyServerException {
        return waitForStart(getDefaultTimeout());
    }

    @Override
    public int waitForStart(int timeout) throws ZosLibertyServerException {
        logger.trace("Waiting " + timeout + "second(s) for Liberty server " +  getServerName() + " to start");
	    
        LocalDateTime timeoutTime = LocalDateTime.now().plusSeconds(timeout);
	    while (LocalDateTime.now().isBefore(timeoutTime)) {
            if (this.zosLibertySeverJob == null || this.zosLibertySeverJob.getStatus() == JobStatus.ACTIVE) {
                if (status() == 0) {
                    return 0;
                }
            } else {
                JobStatus jobStatus = this.zosLibertySeverJob.getStatus();
                if (jobStatus == JobStatus.OUTPUT ||
                    jobStatus == JobStatus.NOTFOUND ||
                    jobStatus == JobStatus.UNKNOWN) {
                    return status();
                }
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new ZosLibertyServerException("Interrupted during wait", e);
            }
        }
        return status();
    }

    @Override
    public boolean waitForStartMessage() throws ZosLibertyServerException {
        return waitForStartMessage(getDefaultTimeout());
    }

    @Override
    public boolean waitForStartMessage(int timeout) throws ZosLibertyServerException {
        Pattern searchPattern = Pattern.compile(SERVER_STARTED_MESSAGE_ID);
        IZosLibertyServerLog messagesLog = getLogs().getMessagesLog();
        String result;
        if (messagesLog.getCheckpoint() > -1) {
            result = messagesLog.waitForPatternSinceCheckpoint(searchPattern, timeout * 1000);
        } else {
            result = messagesLog.waitForPattern(searchPattern, timeout * 1000);
        }
        if (result != null && result.equals(SERVER_STARTED_MESSAGE_ID)) {
            return true;
        }
        return false;
    }

    @Override
    public int stop() throws ZosLibertyServerException {
        try {
            if (this.zosLibertySeverJob != null) {
                return stopJob();
            } else {
                return issueLibertyCommand("server stop " + getServerName(), false).getRc();
            }
        } catch (ZosLibertyServerException e) {
            throw new ZosLibertyServerException("Problem stopping Liberty server", e);
        }
    }

    @Override
    public int waitForStop() throws ZosLibertyServerException {
        return waitForStop(10000);
    }

    @Override
    public int waitForStop(int timeout) throws ZosLibertyServerException {
        if (this.zosLibertySeverJob != null) {
            try {
                this.zosLibertySeverJob.waitForJob();
            } catch (ZosBatchException e) {
                throw new ZosLibertyServerException("Problem waiting for Liberty server batch job to complete", e);
            }
        } else {
            logger.trace("Waiting " + timeout + " second(s) for Liberty server " +  getServerName() + " to stop");
            LocalDateTime timeoutTime = LocalDateTime.now().plusSeconds(timeout);
    	    while (LocalDateTime.now().isBefore(timeoutTime)) {
                if (status() == 1) {
                    return 1;
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new ZosLibertyServerException("Interrupted during wait", e);
                }
            }
        }
        return status();
    }

    @Override
    public boolean waitForStopMessage() throws ZosLibertyServerException {
        return waitForStopMessage(getDefaultTimeout());
    }

    @Override
    public boolean waitForStopMessage(int timeout) throws ZosLibertyServerException {
        Pattern searchPattern = Pattern.compile(SERVER_STOPPED_MESSAGE_ID);
        IZosLibertyServerLog messagesLog = getLogs().getMessagesLog();
        String result;
        if (messagesLog.getCheckpoint() > -1) {
            result = messagesLog.waitForPatternSinceCheckpoint(searchPattern, timeout * 1000);
        } else {
            result = messagesLog.waitForPattern(searchPattern, timeout * 1000);
        }
        if (result != null && result.equals(SERVER_STOPPED_MESSAGE_ID)) {
            return true;
        }
        return false;
    }

    @Override
    public int status() throws ZosLibertyServerException {
        try {
            return issueLibertyCommand("server status " + getServerName(), false).getRc();
        } catch (ZosLibertyServerException e) {
            throw new ZosLibertyServerException("Unable to get Liberty server status", e);
        }
    }
    
    @Override
    public void delete() throws ZosLibertyServerException {
        try {
            if (getWlpOutputDir().exists()) {
                getWlpOutputDir().directoryDeleteNonEmpty();
            }
        } catch (ZosUNIXFileException | ZosLibertyServerException e) {
            logger.info("Problem deleting Liberty output directory", e);
        }
        try {
            if (getWlpUserDir().exists()) {
                getWlpUserDir().directoryDeleteNonEmpty();
            }
        } catch (ZosUNIXFileException | ZosLibertyServerException e) {
            logger.info("Problem deleting Liberty user directory", e);
        }
    }
    
    @Override
    public List<String> listFeatures() throws ZosLibertyServerException {
    	List<String> features = new ArrayList<>();
    	List<Element> featureManagerElements = loadServerXmlFromFileSystem().getElements("featureManager");
    	for (Element featureManagerElement : featureManagerElements) {
    		NodeList featureNodes = featureManagerElement.getChildNodes();
    		for (int i = 0; i < featureNodes.getLength(); i++) {
    			String feature = featureNodes.item(i).getTextContent();
                if (feature != null && !feature.isEmpty() && !feature.startsWith("\n")) {
                	features.add(featureNodes.item(i).getTextContent());
                }                
            }
        }
    	return features;
    }
    
    @Override
	public void addFeature(String feature) throws ZosLibertyServerException {
    	List<Element> featureManagerElements = loadServerXmlFromFileSystem().getElements("featureManager");
    	getServerXml().addTextContextElement(featureManagerElements.get(0), "feature", feature);
        getServerXml().store();
	}

	@Override
	public void removeFeature(String feature) throws ZosLibertyServerException {
    	List<Element> featureManagerElements = loadServerXmlFromFileSystem().getElements("featureManager");
    	getServerXml().removeTextContextElement(featureManagerElements.get(0), "feature", feature);
    	getServerXml().store();
	}

	@Override
    public void setDefaultHttpEndpoint(String host, int httpPort, int httpsPort) throws ZosLibertyServerException {
        loadServerXmlFromFileSystem().removeElementsById("httpEndpoint", "defaultHttpEndpoint");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("id", "defaultHttpEndpoint");
        if (host != null) {
            attributes.put("host", host);
        }
        if (httpPort > 0) {
            attributes.put("httpPort", String.valueOf(httpPort));
        }
        if (httpsPort > 0) {
            attributes.put("httpsPort", String.valueOf(httpsPort));
        }
        getServerXml().addElement("httpEndpoint", attributes);
        getServerXml().store();
    }
    
    @Override
    public void deployApplication(Class<?> testClass, String path, String targetLocation, ApplicationType type, String name, String contextRoot) throws ZosLibertyServerException {
        try {
            if (targetLocation == null) {
                targetLocation = "${shared.app.dir}";
            }
            String parsedLocation = resolveLocation(targetLocation);
            copyApplicationToZosUnix(path, testClass, parsedLocation);
            if (!targetLocation.endsWith(getFileNameFromPath(path))) {
                targetLocation = targetLocation + SLASH_SYBMOL + getFileNameFromPath(path);
            }
            Map<String, String> attributes = new HashMap<>();
            attributes.put("id", name);
            attributes.put("name", name);
            attributes.put("type", type.toString());
            attributes.put("location", targetLocation);
            if (contextRoot != null) {
                attributes.put("context-root", contextRoot);            
            }
            loadServerXmlFromFileSystem().addElement("application", attributes);
            getServerXml().store();
        } catch (ZosLibertyServerException e) {
            throw new ZosLibertyServerException("Problem deploying application", e);
        }
    }
    
    private String getFileNameFromPath(String path) {
        return new File(path).getName();
    }

    private String resolveLocation(String location) throws ZosLibertyServerException {
        String parsedLocation = location;
        try {
            parsedLocation = parsedLocation.replace("${shared.app.dir}", getSharedAppDir().getUnixPath());
            parsedLocation = parsedLocation.replace("${server.config.dir}", getServerConfigDir().getUnixPath());
            parsedLocation = parsedLocation.replace("${server.output.dir}", getServerOutputDir().getUnixPath());
            parsedLocation = parsedLocation.replace("${shared.config.dir}", getSharedConfigDir().getUnixPath());
            parsedLocation = parsedLocation.replace("${shared.resource.dir}", getSharedResourcesDir().getUnixPath());
            parsedLocation = parsedLocation.replace("${wlp.user.dir}", getWlpUserDir().getUnixPath());
        } catch (ZosLibertyServerException e) {
            throw new ZosLibertyServerException("Problem parsing location String '" + location + "'", e);
        }
        return parsedLocation;
    }

    @Override
    public void deployApplicationToDropins(Class<?> testClass, String path) throws ZosLibertyServerException {
        try {
            copyApplicationToZosUnix(path, testClass, getDropinsDir().getUnixPath());
        } catch (ZosLibertyServerException e) {
            throw new ZosLibertyServerException("Problem deploying application to dropins", e);
        }
    }
    
    private void copyApplicationToZosUnix(String path, Class<?> testClass, String location) throws ZosLibertyServerException {
        IBundleResources resources = this.artifactManager.getBundleResources(testClass);
        InputStream application;
        try {
            application = resources.retrieveFile(path);
            if (application == null) {
            	throw new ZosLibertyServerException("Application not found in path '" + path + "' in test bundle");
            }
        } catch (TestBundleResourceException e) {
            throw new ZosLibertyServerException("Problem getting resource '" + path + "' from test bundle", e);
        }
        try {
            String fileName = new File(path).getName();
            IZosUNIXFile applicationFile = this.zosFileHandler.newUNIXFile(getTempWorkDir().getUnixPath() + fileName, getZosImage());
            applicationFile.setDataType(UNIXFileDataType.BINARY);
            if (!applicationFile.exists()) {
                applicationFile.create();
            }
            applicationFile.storeBinary(IOUtils.toByteArray(application));
            IZosUNIXFile locationFile = this.zosFileHandler.newUNIXFile(location + SLASH_SYBMOL, getZosImage());
            if (!locationFile.exists()) {
                locationFile.create();
            }
            String command = "cp " + getTempWorkDir().getUnixPath() + fileName + " " + location;
            UnixCommandResponse response = issueUnixCommand(command);
            if (response.getRc() != 0) {
                throw new ZosLibertyServerException("Problem copying application file to '" + location + "'");
            }
        } catch (ZosUNIXFileException | IOException e) {
            throw new ZosLibertyServerException("Problem deploying application to '" + location + "'", e);
        }
    }

    @Override
    public void removeApplication(String name) throws ZosLibertyServerException {
        try {
            List<Element> applicationElements = loadServerXmlFromFileSystem().getElements("application");
            for (Element applicationElement : applicationElements) {
                String applicationName = applicationElement.getAttribute("name");
                if (name == null || (applicationName != null && applicationName.equals(name))) {
                    String applicationLocation = applicationElement.getAttribute("location");
                    if (applicationLocation != null) {
                        IZosUNIXFile application = this.zosFileHandler.newUNIXFile(resolveLocation(applicationLocation), getZosImage());
                        if (application.exists()) {
                            applicationElement.getParentNode().removeChild(applicationElement);
                            getServerXml().store();
                            application.delete();
                        } else {
                            throw new ZosLibertyServerException("Application file " + name + " does not exist in " + application.getDirectoryPath());
                        }
                    }
                }
            }
        } catch (ZosUNIXFileException e) {
            throw new ZosLibertyServerException("Unable to remove application", e);
        }
    }

    @Override
    public void removeApplicationFromDropins(String fileName) throws ZosLibertyServerException {
        if (fileName != null) {
            try {
                IZosUNIXFile application = this.zosFileHandler.newUNIXFile(getDropinsDir().getUnixPath() + SLASH_SYBMOL + fileName, getZosImage());
                if (application.exists()) {
                    application.delete();
                } else {
                    throw new ZosLibertyServerException("File " + fileName + " does not exist in dropins directory " + getDropinsDir());
                }
            } catch (ZosUNIXFileException | ZosLibertyServerException e) {
                throw new ZosLibertyServerException("Unable to remove application file " + fileName + " from dropins directory", e);
            }
        } else {
            try {
                for (Entry<String, IZosUNIXFile> entry :getDropinsDir().directoryListRecursive().entrySet()) {
                    if (entry.getValue().isDirectory())
                        if (entry.getValue().exists()) {
                            entry.getValue().directoryDeleteNonEmpty();
                    } else {
                        if (entry.getValue().exists()) {
                            entry.getValue().delete();
                        }
                    }
                }
            } catch (ZosUNIXFileException | ZosLibertyServerException e) {
                throw new ZosLibertyServerException("Unable to all remove applications from dropins directory", e);
            }
        }
    }

    @Override
    public boolean waitForApplicationStart(String name) throws ZosLibertyServerException {
        return waitForApplicationStart(name, getDefaultTimeout());
    }

    @Override
    public boolean waitForApplicationStart(String name, int timeout) throws ZosLibertyServerException {
        Pattern searchPattern = Pattern.compile(APP_STARTED_MESSAGE_ID + ":\\s.*\\s" + name + "\\s.*");
        IZosLibertyServerLog messagesLog = getLogs().getMessagesLog();
        String result;
        if (messagesLog.getCheckpoint() > -1) {
            result = messagesLog.waitForPatternSinceCheckpoint(searchPattern, timeout * 1000);
        } else {
            result = messagesLog.waitForPattern(searchPattern, timeout * 1000);
        }
        if (result != null && result.startsWith(APP_STARTED_MESSAGE_ID)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean waitForApplicationStop(String name) throws ZosLibertyServerException {
        return waitForApplicationStop(name, getDefaultTimeout());
    }

    @Override
    public boolean waitForApplicationStop(String name, int timeout) throws ZosLibertyServerException {
        Pattern searchPattern = Pattern.compile(APP_STOPPED_MESSAGE_ID + ":\\s.*\\s" + name + "\\s.*");
        IZosLibertyServerLog messagesLog = getLogs().getMessagesLog();
        String result;
        if (messagesLog.getCheckpoint() > -1) {
            result = messagesLog.waitForPatternSinceCheckpoint(searchPattern, timeout * 1000);
        } else {
            result = messagesLog.waitForPattern(searchPattern, timeout * 1000);
        }
        if (result != null && result.startsWith(APP_STOPPED_MESSAGE_ID)) {
            return true;
        }
        return false;
    }
    
    @Override
    public IZosUNIXFile getKeystoreFile() throws ZosLibertyServerException {
        try {
            // Get the PCKS12 keystore
            IZosUNIXFile keystore = this.zosFileHandler.newUNIXFile(getResourcesDir() +"/security/key.p12", getZosImage());
            if (!keystore.exists()) {
                // Get the JKS keystore
                keystore = this.zosFileHandler.newUNIXFile(getResourcesDir() +"/security/key.jks", getZosImage());
                if (!keystore.exists()) {
                    logger.warn("Unable to locate either JKS or PKCS12 keystore.");
                    return null;
                }
            }
            return keystore;
        } catch(ZosUNIXFileException e) {
            throw new ZosLibertyServerException("Problem getting keystore file", e);
        }
    }

    @Override
    public String securityUtilityEncode(String password) throws ZosLibertyServerException {
        try {
            return issueLibertyCommand("securityUtility encode " + password, false).getResponse();
        } catch (ZosLibertyServerException e) {
            throw new ZosLibertyServerException("Problem encoding passord", e);
        }
    }

    @Override
    public IZosUNIXFile securityUtilityGenerateKeystore(String password) throws ZosLibertyServerException {
        // Delete existing keystore files
        try {
            for(Entry<String, IZosUNIXFile> entry : getServerSecurityResourcesDir().directoryList().entrySet()) {
                if (entry.getValue().getFileName().startsWith("key.")) {
                    logger.info("Deleting existing key file: " + entry.getValue().getUnixPath());
                    entry.getValue().delete();
                }
            }
        } catch(ZosUNIXFileException | ZosLibertyServerException e) {
            throw new ZosLibertyServerException("Problem deleting existing keystore file", e);
        }
        
        
        try {
        	if (issueLibertyCommand("securityUtility createSSLCertificate --server=" + getServerName() + " --password=" + password + ";echo RC=$?", false).getRc() != 0) {
        		throw new ZosLibertyServerException("Non zero rc from securityUtility"); 
        	}
            return getKeystoreFile();
        } catch (ZosLibertyServerException e) {
            throw new ZosLibertyServerException("Problem generating keystore", e);
        }
    }
    
    @Override
    public String toString() {
        return "[zOS Liberty Server] " + this.serverName;
    }

    @Override
    public void saveToResultsArchive() throws ZosLibertyServerException {
        saveToResultsArchive(getDefaultRasPath());
    }

    @Override
    public void saveToResultsArchive(String rasPath) throws ZosLibertyServerException {
        if (this.zosLibertySeverJob != null) {
            rasPath = archiveJob(rasPath);
        }
        getLogs().saveToResultsArchive(rasPath);
        try {
            if (getServerXml().getAsZosUNIXFile().exists()) {
                getServerXml().saveToResultsArchive(rasPath);
            }
        } catch (ZosUNIXFileException | ZosLibertyServerException e) {
            throw new ZosLibertyServerException("Unable to save server.xml zOS UNIX file", e);
        }
    }

    private int stopJob() throws ZosLibertyServerException {
        int rc = -1;
        try {
            this.zosConsole.issueCommand("RO " + this.zosImage.getImageID() + ",STOP " + this.zosLibertySeverJob.getJobname().getName());
            rc  = waitForStop();
        } catch (ZosConsoleException | ZosLibertyServerException e) {
            logger.error("Problem stopping Liberty server job", e);
        }
        try {
            saveToResultsArchive(getDefaultRasPath());
            this.zosLibertySeverJob.purge();
            this.zosLibertySeverJob = null;
        } catch (ZosBatchException e) {
            logger.error("Problem during Liberty server job clean up", e);
        }
        return rc;
    }

    private String archiveJob(String rasPath) throws ZosLibertyServerException {
        try {
            rasPath = rasPath + SLASH_SYBMOL +
                      "serverBatchJobs" + SLASH_SYBMOL + 
                      this.zosLibertySeverJob.getJobname().getName() + "_" + 
                      this.zosLibertySeverJob.getJobId() + "_" + 
                      this.zosLibertySeverJob.getRetcode().replace(" ", "-").replace("????", "UNKNOWN");
            this.zosLibertySeverJob.saveOutputToResultsArchive(rasPath);
            return rasPath;
        } catch (ZosBatchException e) {
            throw new ZosLibertyServerException("Problem during archive of Liberty server batch job", e);
        }
    }

    private IZosUNIXFile getServerXmlUnixFile() throws ZosLibertyServerException {
        IZosUNIXFile serverXmlUnixFile;
        try {
            serverXmlUnixFile = this.zosFileHandler.newUNIXFile(getServerConfigDir() + "server.xml", zosImage);
        } catch (ZosUNIXFileException e) {
            throw new ZosLibertyServerException("Unable to create server.xml object", e);
        }
        return serverXmlUnixFile;
    }

    private UnixCommandResponse issueLibertyCommand(String command, boolean outputDir) throws ZosLibertyServerException {
        try {
            command = serverEnv(outputDir) + getWlpInstallDir().getUnixPath() + "bin/" + command;
            return issueUnixCommand(command);
        } catch (ZosLibertyServerException e) {
            throw new ZosLibertyServerException("Problem issuing Liberty command", e);
        }
    }

    private UnixCommandResponse issueUnixCommand(String command) throws ZosLibertyServerException {
        try {
            command = command + ";echo RC=$?";
            return new UnixCommandResponse(this.zosUNIXCommand.issueCommand(command));
        } catch (ZosUNIXCommandException e) {
            throw new ZosLibertyServerException("Problem issuing zOS UNIX command", e);
        }
    }

    private String serverEnv(boolean outputDir) throws ZosLibertyServerException {
        StringBuilder envs = new StringBuilder();
        envs.append("export JAVA_HOME=");
        envs.append(getJavaHome());
        envs.append(SEMI_COLON_SYMBOL);
        envs.append("export WLP_USER_DIR=");
        envs.append(getWlpUserDir());
        envs.append(SEMI_COLON_SYMBOL);
        if (outputDir) {
            envs.append("export WLP_OUTPUT_DIR=");
            envs.append(getWlpOutputDir());
            envs.append(SEMI_COLON_SYMBOL);
        }
        return envs.toString();
    }

    private String buildServerJcl() throws ZosLibertyServerException {
        return "// SET NAME='" + getServerName() + "'\n"
             + "// SET INSDIR='" + getWlpInstallDir() + "'\n"
             + "// SET USRDIR='" + getWlpUserDir() + "'\n"
             + "//SERVER  EXEC PGM=BPXBATSL,REGION=0M,TIME=NOLIMIT,\n"
             + "//  PARM='PGM &INSDIR./lib/native/zos/s390x/bbgzsrv &NAME'\n"
             + "//WLPUDIR  DD PATH='&USRDIR.'\n"
             + "//STDENV   DD *\n"
             + "JAVA_HOME=" + getJavaHome() + "\n"
             + "//STDOUT   DD SYSOUT=*\n"
             + "//STDERR   DD SYSOUT=*\n";
    }

    private String toDirectory(String dir) {
        return dir.endsWith(SLASH_SYBMOL)? dir: dir + SLASH_SYBMOL;
    }

    private int getDefaultTimeout() throws ZosLibertyServerException {
		if (this.defaultTimeout == -1) {
			try {
				this.defaultTimeout = DefaultTimeout.get(getZosImage());
			} catch (ZosLibertyManagerException e) {
				throw new ZosLibertyServerException("Unable to get default timeout", e);
			}
		}
		return this.defaultTimeout;
    }

    private String getDefaultRasPath() {
        return this.zosLibertyManager.getCurrentTestMethodArchiveFolder().toString();
    }
    
    private IZosUNIXFile getTempWorkDir() throws ZosLibertyServerException {
        if (this.tmpWorkDirDir == null) {
            try {
                this.tmpWorkDirDir = this.zosFileHandler.newUNIXFile(getZosImage().getRunTemporaryUNIXPath() + "tmp/", getZosImage());
            } catch (ZosManagerException e) {
                throw new ZosLibertyServerException("Unable to get temporary work directory", e);
            }
        }
        return this.tmpWorkDirDir;
    }

    class UnixCommandResponse {
        private String response;
        private int rc;

        UnixCommandResponse(String commandResponse) throws ZosLibertyServerException {
            int lastPos = commandResponse.lastIndexOf("RC=");
            if (lastPos >= 0) {
                this.response = commandResponse.substring(0, lastPos);
                try {
                    this.rc = Integer.parseInt(commandResponse.substring(lastPos+3).trim());
                } catch (NumberFormatException e) {
                    throw new ZosLibertyServerException("Problem getting return code from zOS UNIX command (" + commandResponse.substring(lastPos).trim() + ")", e);
                }
            } else {
                throw new ZosLibertyServerException("Problem getting return code from zOS UNIX command");
            }
        }
        
        int getRc() {
            return this.rc;
        }
        
        String getResponse() {
            return this.response;
        }
    }
}
