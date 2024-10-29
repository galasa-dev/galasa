/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.resource.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.cicsts.CicstsHashMap;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.cicsresource.CicsJvmserverResourceException;
import dev.galasa.cicsts.cicsresource.CicsResourceManagerException;
import dev.galasa.cicsts.cicsresource.CicsResourceStatus;
import dev.galasa.cicsts.cicsresource.IJvmprofile;
import dev.galasa.cicsts.cicsresource.IJvmserver;
import dev.galasa.cicsts.cicsresource.IJvmserverLog;
import dev.galasa.cicsts.resource.internal.properties.DefaultResourceTimeout;
import dev.galasa.textscan.ILogScanner;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.IZosUNIXFile.UNIXFileDataType;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosliberty.IZosLiberty;
import dev.galasa.zosliberty.IZosLibertyServer;
import dev.galasa.zosliberty.ZosLibertyServerException;

public class JvmserverImpl implements IJvmserver {

    private static final Log logger = LogFactory.getLog(JvmserverImpl.class);

    private CicsResourceManagerImpl cicsResourceManager;
    private IZosFileHandler zosFileHandler;
    private IZosLiberty zosLiberty;

    private boolean shouldArchive = true;
    private boolean shouldCleanup = true;

    private ICicsRegion cicsRegion;
    private ICicsTerminal cicsTerminal;
    private String cicsApplid;
    private IZosImage cicsZosImage;
    private IZosUNIXFile runTemporaryUNIXPath;
    private ICredentials cicsZosImageDefaultCredentials;
    private String cicsZosImageDefaultCredentialsUserid;
    private String cicsConfigroot;
    private String cicsJvmprofileDir;
    private String cicsUsshome;
    private String cicsRegionHomeDirectory;
    private String cicsRegionJobname;
    private String cicsRegionUserid;

    private String resourceDefinitionName;
    private String resourceDefinitionGroup;
    private String resourceDefinitionDescription;
    private CicsResourceStatus resourceDefinitionStatus = CicsResourceStatus.ENABLED;
    private String resourceDefinitionJvmprofile;
    private JvmserverType jvmserverType = JvmserverType.UNKNOWN;
    private String resourceDefinitionLerunopts;
    private int resourceDefinitionThreadlimit = 15;

    private IJvmprofile jvmprofile;

    private String defaultWorkingDirectoryValue;
    private String defaultJavaHomeValue;
    private String defaultWlpInstallDirValue;
    private String defaultWlpUserDirValue;
    private String defaultWlpOutputDirValue;

    // These values dependent on content of JVM profile
    private IZosUNIXFile workingDirectory;
    private IZosUNIXFile diagnosticsDirectory;
    private IZosUNIXFile javaHome;
    private IZosUNIXFile logsDirectory;
    private IJvmserverLog jvmLogLog;
    private IJvmserverLog stdOutLog;
    private IJvmserverLog stdErrLog;
    private IJvmserverLog jvmTraceLog;

    private IZosLibertyServer zosLibertyServer;
    private String wlpInstallDir;
    private String wlpUserDir;
    private String wlpOutputDir;

    private int defaultTimeout = -1;

    private static final String SLASH_SYBMOL = "/";
    private static final String SYMBOL_APPLID = "&APPLID;";
    private static final String SYMBOL_CONFIGROOT = "&CONFIGROOT;";
    private static final String SYMBOL_JVMSERVER = "&JVMSERVER;";
    private static final String SYMBOL_USSHOME = "&USSHOME;";
    private static final String SYMBOL_DATE = "&DATE;";
    private static final String SYMBOL_TIME = "&TIME;";

    private static final String OPTION_JAVA_HOME = "JAVA_HOME";
    private static final String OPTION_WORK_DIR = "WORK_DIR";

    private static final String RESOURCE_TYPE_JVMSERVER = "JVMSERVER";

    private static final String LOG_CEMT_PARAMETER = "Log";
    private static final String LOG_PROFILE_OPTION = "JVMLOG";
    private static final String LOG_FILE_SUFFIX = "dfhjvmlog";
    private static final String STDOUT_CEMT_PARAMETER = "Stdout";
    private static final String STDOUT_PROFILE_OPTION = "STDOUT";
    private static final String STDOUT_FILE_SUFFIX = "dfhjvmout";
    private static final String STDERR_CEMT_PARAMETER = "Stderr";
    private static final String STDERR_PROFILE_OPTION = "STDERR";
    private static final String STDERR_FILE_SUFFIX = "dfhjvmerr";
    private static final String TRACE_CEMT_PARAMETER = "Trace";
    private static final String TRACE_PROFILE_OPTION = "JVMTRACE";
    private static final String TRACE_FILE_SUFFIX = "dfhjvmtrc";

    public JvmserverImpl(CicsResourceManagerImpl cicsResourceManagerImpl, ICicsRegion cicsRegion, ICicsTerminal cicsTerminal, String name, String group) throws CicsJvmserverResourceException {
        this.cicsResourceManager = cicsResourceManagerImpl;
        this.cicsResourceManager.registerJvmserver(this);
        this.cicsRegion = cicsRegion;
        this.cicsZosImage = cicsRegion.getZosImage();

        try {
            this.zosFileHandler = this.cicsResourceManager.getZosFileHandler();
        } catch (CicsResourceManagerException e) {
            throw new CicsJvmserverResourceException("Unable to get zOS File Handler", e);
        }

        setRunTemporaryUNIXPath();
        this.cicsTerminal = cicsTerminal;
        this.resourceDefinitionName = name;
        this.resourceDefinitionGroup = group;
    }

    public JvmserverImpl(CicsResourceManagerImpl cicsResourceManagerImpl, ICicsRegion cicsRegion, ICicsTerminal cicsTerminal, String name, String group, String jvmprofileName, JvmserverType jvmserverType) throws CicsJvmserverResourceException {
        this(cicsResourceManagerImpl, cicsRegion, cicsTerminal, name, group);
        this.resourceDefinitionJvmprofile = jvmprofileName;
        this.jvmserverType = jvmserverType;
        this.jvmprofile = newJvmprofileFromCicsSuppliedProfile(jvmprofileName);

        try {
            this.javaHome = this.zosFileHandler.newUNIXFile(getDefaultJavaHomeValue(), cicsZosImage);
        } catch (ZosUNIXFileException e) {
            throw new CicsJvmserverResourceException("Unable to set JAVA_HOME", e);
        }

        this.jvmprofile.setProfileValue(OPTION_JAVA_HOME, this.javaHome.getUnixPath());

        try {
            this.workingDirectory = this.zosFileHandler.newUNIXFile(getDefaultWorkingDirectoryValue(), cicsZosImage);
        } catch (ZosUNIXFileException e) {
            throw new CicsJvmserverResourceException("Unable to set WORK_DIR", e);
        }
        this.jvmprofile.setProfileValue(OPTION_WORK_DIR, this.workingDirectory.getUnixPath());
        this.jvmprofile.setJvmProfileDir(getJvmProfileDir());
        this.jvmprofile.printProfile();
        if (isLiberty()) {
            jvmprofile.setWlpInstallDir(getDefaultWlpInstallDirValue());
            this.wlpInstallDir = absolutePath(parseJvmprofileSymbols(this.jvmprofile.getWlpInstallDir()));
            this.jvmprofile.setWlpUserDir(getDefaultWlpUserDirValue());
            this.wlpUserDir = absolutePath(parseJvmprofileSymbols(this.jvmprofile.getWlpUserDir()));
            if (this.jvmprofile.getWlpOutputDir() != null) {
                this.wlpOutputDir = absolutePath(parseJvmprofileSymbols(this.jvmprofile.getWlpOutputDir()));
            }
            this.zosLibertyServer = newZosLibertyServerFromCicsSuppliedServerXml();
        }
    }



    public JvmserverImpl(CicsResourceManagerImpl cicsResourceManagerImpl, ICicsRegion cicsRegion, ICicsTerminal cicsTerminal, String name, String group, IJvmprofile jvmprofile) throws CicsJvmserverResourceException {
        this(cicsResourceManagerImpl, cicsRegion, cicsTerminal, name, group);
        this.jvmprofile = jvmprofile;
        this.resourceDefinitionJvmprofile = jvmprofile.getProfileName();
        this.jvmserverType = determineJvmserverType();
    }

    public JvmserverImpl(CicsResourceManagerImpl cicsResourceManagerImpl, ICicsRegion cicsRegion, ICicsTerminal cicsTerminal, String name, String group, IJvmprofile jvmprofile, IZosLibertyServer libertyServer) throws CicsJvmserverResourceException {
        this(cicsResourceManagerImpl, cicsRegion, cicsTerminal, name, group);
        this.jvmprofile = jvmprofile;
        this.resourceDefinitionJvmprofile = jvmprofile.getProfileName();
        this.jvmserverType = JvmserverType.LIBERTY;
        this.zosLibertyServer = libertyServer;
    }

    private void setRunTemporaryUNIXPath() throws CicsJvmserverResourceException {
        try {
            this.runTemporaryUNIXPath = this.zosFileHandler.newUNIXFile(cicsRegion.getRunTemporaryUNIXDirectory().getUnixPath() + SLASH_SYBMOL, this.cicsZosImage);
        } catch (ZosManagerException | CicstsManagerException e) {
            throw new CicsJvmserverResourceException("Unable to get Run Temporary UNIX directory for image" + this.cicsZosImage.getImageID(), e);
        }
    }

    protected ILogScanner newLogScanner() throws CicsResourceManagerException {
        return this.cicsResourceManager.getLogScanner();
    }

    protected boolean isLiberty() {
        return this.jvmserverType == JvmserverType.LIBERTY;
    }

    protected IZosLibertyServer newZosLibertyServerFromCicsSuppliedServerXml() throws CicsJvmserverResourceException {
        IZosLibertyServer libertyServer;
        try {
            libertyServer = getLiberty().newZosLibertyServer(this.cicsZosImage, this.wlpInstallDir, this.wlpUserDir, this.wlpOutputDir);
            StringBuilder path = new StringBuilder();
            path.append(getUsshome());
            path.append(SLASH_SYBMOL);
            path.append("etc");
            path.append(SLASH_SYBMOL);
            path.append("wlp");
            path.append(SLASH_SYBMOL);
            path.append("extensions");
            path.append(SLASH_SYBMOL);
            path.append("cicsts");
            path.append(SLASH_SYBMOL);
            path.append("templates");
            path.append(SLASH_SYBMOL);
            path.append("servers");
            path.append(SLASH_SYBMOL);
            path.append("defaultServer");
            path.append(SLASH_SYBMOL);
            path.append("server.xml");
            IZosUNIXFile cicsSuppliedserverXml = this.zosFileHandler.newUNIXFile(path.toString(), this.cicsZosImage);
            cicsSuppliedserverXml.setDataType(UNIXFileDataType.BINARY);
            byte[] content = cicsSuppliedserverXml.retrieveAsBinary();
            libertyServer.getServerXml().setFromString(new String(content, StandardCharsets.UTF_8));
        } catch (ZosLibertyServerException | ZosUNIXFileException e) {
            throw new CicsJvmserverResourceException("Unable to create new zOS Liberty Server for " + RESOURCE_TYPE_JVMSERVER + " " + this.getName(), e);
        }
        return libertyServer;
    }

    protected IZosLiberty getLiberty() throws CicsJvmserverResourceException {
        if (this.zosLiberty == null) {
            try {
                this.zosLiberty = cicsResourceManager.getZosLiberty();
            } catch (CicsResourceManagerException e) {
                throw new CicsJvmserverResourceException("Unable to get zOS Liberty", e);
            }
        }
        return this.zosLiberty;
    }

    protected String absolutePath(String path) throws CicsJvmserverResourceException {
        if (!path.startsWith(SLASH_SYBMOL)) {
            path = getHomeDirectory() + path;
        }
        if (!path.endsWith(SLASH_SYBMOL)) {
            path = path + SLASH_SYBMOL;
        }
        return path;
    }

    protected String getDefaultWorkingDirectoryValue() throws CicsJvmserverResourceException {
        if (this.defaultWorkingDirectoryValue == null) {
            this.defaultWorkingDirectoryValue = this.runTemporaryUNIXPath.getUnixPath().substring(0, this.runTemporaryUNIXPath.getUnixPath().length()-getApplid().length()-1);
        }
        return this.defaultWorkingDirectoryValue;
    }

    protected IZosUNIXFile getDefaultLogsDiretory() throws CicsJvmserverResourceException {
        if (this.logsDirectory == null) {
            try {
                this.logsDirectory = this.zosFileHandler.newUNIXFile(getDefaultWorkingDirectoryValue() + SLASH_SYBMOL + this.getApplid() + SLASH_SYBMOL + getName() + SLASH_SYBMOL, cicsZosImage);
            } catch (CicsJvmserverResourceException | ZosUNIXFileException e) {
                throw new CicsJvmserverResourceException("Unable to get default logs directory", e);
            }
        }
        return this.logsDirectory;
    }

    protected NavigableMap<String, IZosUNIXFile> decendingDirectoryList(IZosUNIXFile directory) throws CicsJvmserverResourceException {
        SortedMap<String, IZosUNIXFile> directoryList = new TreeMap<>();
        try {
            if (directory.exists()) {

            }
            directoryList = directory.directoryList();
        } catch (ZosUNIXFileException e) {
            throw new CicsJvmserverResourceException("Unable to list directory", e);
        }
        return ((TreeMap<String, IZosUNIXFile>) directoryList).descendingMap();
    }

    protected String getDefaultJavaHomeValue() throws CicsJvmserverResourceException {
        if (this.defaultJavaHomeValue == null) {
            try {
                this.defaultJavaHomeValue = this.cicsRegion.getJavaHome();
            } catch (CicstsManagerException e) {
                throw new CicsJvmserverResourceException("Unable to get default Java Home", e);
            }
        }
        return this.defaultJavaHomeValue;
    }

    protected String getDefaultWlpInstallDirValue() {
        if (this.defaultWlpInstallDirValue == null) {
            this.defaultWlpInstallDirValue = "&USSHOME;/wlp";
        }
        return this.defaultWlpInstallDirValue;
    }

    protected String getDefaultWlpUserDirValue() throws CicsJvmserverResourceException {
        if (this.defaultWlpUserDirValue == null) {
            this.defaultWlpUserDirValue =  getDefaultWorkingDirectoryValue() + SLASH_SYBMOL + "./&APPLID;/&JVMSERVER;/wlp/usr";
        }
        return this.defaultWlpUserDirValue;
    }

    protected String getDefaultWlpOutputDirValue() throws CicsJvmserverResourceException {
        if (this.defaultWlpOutputDirValue == null) {
            this.defaultWlpOutputDirValue =  getDefaultWlpUserDirValue() + SLASH_SYBMOL + getName();
        }
        return this.defaultWlpOutputDirValue;
    }

    protected IJvmprofile newJvmprofileFromCicsSuppliedProfile(String name) throws CicsJvmserverResourceException {
        String cicsSuppliedProfileFileName = this.jvmserverType.getCicsSuppliedProfile();
        if (this.jvmserverType == JvmserverType.UNKNOWN) {
            throw new CicsJvmserverResourceException("Unknown JVM server type " + jvmserverType);
        }
        IZosUNIXFile cicsSuppliedProfile = getCicsSuppliedProfile(cicsSuppliedProfileFileName);
        try {
            cicsSuppliedProfile.setDataType(UNIXFileDataType.TEXT);
            String content = cicsSuppliedProfile.retrieveAsText();
            return new JvmprofileImpl(this.zosFileHandler, this.cicsZosImage, name, content);
        } catch (ZosUNIXFileException e) {
            throw new CicsJvmserverResourceException("Problem creating JVM profile from template " + cicsSuppliedProfile.getUnixPath(), e);
        }
    }

    protected IZosUNIXFile getCicsSuppliedProfile(String cicsSuppliedProfileFileName) throws CicsJvmserverResourceException {
        String templatePath = getUsshome() + SLASH_SYBMOL + "JVMProfiles" + SLASH_SYBMOL + cicsSuppliedProfileFileName;
        IZosUNIXFile cicsSuppliedProfile;
        try {
            cicsSuppliedProfile = this.zosFileHandler.newUNIXFile(templatePath, this.cicsZosImage);
        } catch (ZosUNIXFileException e) {
            throw new CicsJvmserverResourceException("Unable to get CICS supplied JVM profile " +  templatePath, e);
        }
        return cicsSuppliedProfile;
    }

    protected JvmserverType determineJvmserverType() {
        if (this.jvmprofile.containsOption(JvmprofileImpl.OPTION_CLASSPATH_PREFIX)) {
            return JvmserverType.LIBERTY;
        } else if (this.jvmprofile.containsOption(JvmprofileImpl.OPTION_SECURITY_TOKEN_SERVICE)) {
            return JvmserverType.STS;
        } else if (this.jvmprofile.containsOption(JvmprofileImpl.OPTION_JAVA_PIPELINE)) {
            return JvmserverType.AXIS2;
        } else if (this.jvmprofile.containsOption(JvmprofileImpl.OPTION_CLASSPATH_PREFIX) || this.jvmprofile.containsOption(JvmprofileImpl.OPTION_CLASSPATH_SUFFIX)) {
            return JvmserverType.CLASSPATH;
        } else if (JvmserverType.CMCI.getCicsSuppliedProfile().startsWith(this.jvmprofile.getProfileName())) {
            return JvmserverType.CMCI;
        } else  if (this.jvmprofile.getProfileMap().isEmpty()) {
            return JvmserverType.OSGI;
        }
        return JvmserverType.UNKNOWN;
    }

    protected String getHomeDirectory() throws CicsJvmserverResourceException {
        if (this.cicsRegionHomeDirectory == null) {
            this.cicsRegionHomeDirectory = SLASH_SYBMOL + "u" + SLASH_SYBMOL + getCicsRegionUserid().toLowerCase() + SLASH_SYBMOL;
        }
        return this.cicsRegionHomeDirectory;
    }

    protected String getCicsZosImageDefaultCredentialsUserid() throws CicsJvmserverResourceException {
        if (this.cicsZosImageDefaultCredentialsUserid == null) {
            this.cicsZosImageDefaultCredentialsUserid = ((ICredentialsUsernamePassword) getCicsZosImageDefaultCredentials()).getUsername();
        }
        return this.cicsZosImageDefaultCredentialsUserid;
    }


    protected ICredentials getCicsZosImageDefaultCredentials() throws CicsJvmserverResourceException {
        if (this.cicsZosImageDefaultCredentials == null) {
            try {
                this.cicsZosImageDefaultCredentials = this.cicsZosImage.getDefaultCredentials();
            } catch (ZosManagerException e) {
                throw new CicsJvmserverResourceException("Problem getting CICS region zOS image default credentials", e);
            }
        }
        return this.cicsZosImageDefaultCredentials;
    }

    @Override
    public void setDefinitionDescriptionAttribute(String value) {
        this.resourceDefinitionDescription = value;
    }

    @Override
    public void setDefinitionStatusAttribute(CicsResourceStatus value) {
        this.resourceDefinitionStatus = value;
    }

    @Override
    public void setResourceDefinitionLerunoptsAttribute(String lerunopts) {
        this.resourceDefinitionLerunopts = lerunopts;
    }

    @Override
    public void setResourceDefinitionThreadlimitAttribute(int threadlimit) {
        this.resourceDefinitionThreadlimit = threadlimit;
    }

    @Override
    public String getResourceDefinitionNameAttribute() {
        return this.resourceDefinitionName;
    }

    @Override
    public String getResourceDefinitionGroupAttribute() {
        return this.resourceDefinitionGroup;
    }

    @Override
    public String getResourceDefinitionDescriptionAttribute() {
        return this.resourceDefinitionDescription;
    }

    @Override
    public CicsResourceStatus getResourceDefinitionStatusAttribute() {
        return this.resourceDefinitionStatus;
    }

    @Override
    public String getResourceDefinitionJvmprofileAttribute() {
        return this.resourceDefinitionJvmprofile;
    }

    @Override
    public String getResourceDefinitionLerunoptsAttribute() {
        return this.resourceDefinitionLerunopts;
    }

    @Override
    public int getResourceDefinitionThreadlimitAttribute() {
        return this.resourceDefinitionThreadlimit;
    }

    @Override
    public void buildResourceDefinition() throws CicsJvmserverResourceException {
        try {
            if (resourceDefined()) {
                throw new CicsJvmserverResourceException(RESOURCE_TYPE_JVMSERVER + " " + getName() + " already exists");
            }
            this.cicsRegion.ceda().createResource(this.cicsTerminal, RESOURCE_TYPE_JVMSERVER, getName(), this.resourceDefinitionGroup, buildResourceParameters());
            //TODO: Messages???
            if (!resourceDefined()) {
                throw new CicsJvmserverResourceException("Failed to define " + RESOURCE_TYPE_JVMSERVER + " resource definition");
            }
        } catch (CicstsManagerException e) {
            throw new CicsJvmserverResourceException("Unable to build " + RESOURCE_TYPE_JVMSERVER + " resource definition", e);
        }
    }

    @Override
    public void buildInstallResourceDefinition() throws CicsJvmserverResourceException {
        buildResourceDefinition();
        installResourceDefinition();
    }

    @Override
    public void installResourceDefinition() throws CicsJvmserverResourceException {
        try {
            if (resourceInstalled()) {
                throw new CicsJvmserverResourceException(RESOURCE_TYPE_JVMSERVER + " " + getName() + " already installed");
            }
            this.cicsRegion.ceda().installResource(this.cicsTerminal, RESOURCE_TYPE_JVMSERVER, getName(), this.resourceDefinitionGroup);
            if (!resourceInstalled()) {
                throw new CicsJvmserverResourceException("Failed to install " + RESOURCE_TYPE_JVMSERVER + " resource definition");
            }
        } catch (CicstsManagerException e) {
            throw new CicsJvmserverResourceException("Unable to install " + RESOURCE_TYPE_JVMSERVER + " resource definition", e);
        }
    }

    @Override
    public boolean resourceDefined() throws CicsJvmserverResourceException {
        try {
            return this.cicsRegion.ceda().resourceExists(this.cicsTerminal, RESOURCE_TYPE_JVMSERVER, getName(), resourceDefinitionGroup);
        } catch (CicstsManagerException e) {
            throw new CicsJvmserverResourceException("Unable to display " + RESOURCE_TYPE_JVMSERVER + " resource definition", e);
        }
    }

    @Override
    public boolean resourceInstalled() throws CicsJvmserverResourceException {
        try {
            return this.cicsRegion.cemt().inquireResource(this.cicsTerminal, RESOURCE_TYPE_JVMSERVER, getName()) != null;
        } catch (CicstsManagerException e) {
            throw new CicsJvmserverResourceException("Unable to inquire " + RESOURCE_TYPE_JVMSERVER + "", e);
        }
    }

    @Override
    public void enable() throws CicsJvmserverResourceException {
        try {
            // Reset logs etc as the names will have changed and there may be JVM profile updates
            resetSavedValues();
            if (!resourceInstalled()) {
                throw new CicsJvmserverResourceException(RESOURCE_TYPE_JVMSERVER + " " + getName() + " does not exist");
            }
            this.cicsRegion.cemt().setResource(this.cicsTerminal, RESOURCE_TYPE_JVMSERVER, getName(), "ENABLED");
        } catch (CicstsManagerException e) {
            throw new CicsJvmserverResourceException("Problem enabling " + RESOURCE_TYPE_JVMSERVER + " " + getName(), e);
        }
    }

    private void resetSavedValues() {
        this.workingDirectory = null;
        this.diagnosticsDirectory = null;
        this.javaHome = null;
        this.logsDirectory = null;
        this.jvmLogLog = null;
        this.stdOutLog = null;
        this.stdErrLog = null;
        this.jvmTraceLog = null;
        this.wlpInstallDir = null;
        this.wlpUserDir = null;
        this.wlpOutputDir = null;
    }

    @Override
    public boolean waitForEnable() throws CicsJvmserverResourceException {
        return waitForEnable(getDefaultTimeout());
    }

    @Override
    public boolean waitForEnable(int timeout) throws CicsJvmserverResourceException {
        logger.trace("Waiting " + timeout + " second(s) for " + RESOURCE_TYPE_JVMSERVER + " " +  getName() + " to be enabled");
        LocalDateTime timeoutTime = LocalDateTime.now().plusSeconds(timeout);
	    while (LocalDateTime.now().isBefore(timeoutTime)) {
            if (isEnabled()) {
                return true;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new CicsJvmserverResourceException("Interrupted during wait", e);
            }
        }
        return isEnabled();
    }

    @Override
    public boolean isEnabled() throws CicsJvmserverResourceException {
        if (!resourceInstalled()) {
            return false;
        }
        boolean enabled = cemtInquire().isParameterEquals("enablestatus", CicsResourceStatus.ENABLED.toString());
        if (enabled) {
            logger.trace(RESOURCE_TYPE_JVMSERVER + " " +  getName() + " is enabled");
        } else {
            logger.trace(RESOURCE_TYPE_JVMSERVER + " " +  getName() + " is NOT enabled");
        }
        return enabled;
    }

    @Override
    public boolean disable() throws CicsJvmserverResourceException {
        return disable(PurgeType.PHASEOUT, getDefaultTimeout());
    }

    @Override
    public boolean disable(PurgeType purgeType) throws CicsJvmserverResourceException {
        return disable(purgeType, getDefaultTimeout());
    }

    @Override
    public boolean disable(PurgeType purgeType, int timeout) throws CicsJvmserverResourceException {
        try {
            if (!resourceInstalled()) {
                throw new CicsJvmserverResourceException(RESOURCE_TYPE_JVMSERVER + " " + getName() + " does not exist");
            }
            this.cicsRegion.cemt().setResource(this.cicsTerminal, RESOURCE_TYPE_JVMSERVER, getName(), "DISABLED " + purgeType);
        } catch (CicstsManagerException e) {
            throw new CicsJvmserverResourceException("Problem disabling " + RESOURCE_TYPE_JVMSERVER + " " + getName(), e);
        }
        return waitForDisable(timeout);
    }

    @Override
    public PurgeType disableWithEscalate() throws CicsJvmserverResourceException {
        return disableWithEscalate(getDefaultTimeout());
    }

    @Override
    public PurgeType disableWithEscalate(int steptimeout) throws CicsJvmserverResourceException {
        PurgeType purgeType = PurgeType.PHASEOUT;
        if (!disable(purgeType, steptimeout)) {
            purgeType = PurgeType.PURGE;
            if (!disable(purgeType, steptimeout)) {
                purgeType = PurgeType.FORCEPURGE;
                if (!disable(purgeType, steptimeout)) {
                    purgeType = PurgeType.KILL;
                    if (!disable(purgeType, steptimeout)) {
                        purgeType = PurgeType.FAILED;
                    }
                }
            }
        }

        return purgeType;
    }

    @Override
    public boolean waitForDisable() throws CicsJvmserverResourceException {
        return waitForDisable(getDefaultTimeout());
    }

    @Override
    public boolean waitForDisable(int timeout) throws CicsJvmserverResourceException {
        logger.trace("Waiting " + timeout + " second(s) for " + RESOURCE_TYPE_JVMSERVER + " " +  getName() + " to be disabled");
        LocalDateTime timeoutTime = LocalDateTime.now().plusSeconds(timeout);
	    while (LocalDateTime.now().isBefore(timeoutTime)) {
            if (!isEnabled()) {
                return true;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new CicsJvmserverResourceException("Interrupted during wait", e);
            }
        }
        if (isEnabled()) {
            throw new CicsJvmserverResourceException(RESOURCE_TYPE_JVMSERVER + " " + getName() + " not disabled in " + timeout + " second(s)");
        }
        return true;
    }

    @Override
    public void delete() throws CicsJvmserverResourceException {
        delete(false);
    }

    @Override
    public void delete(boolean ignoreErrors) throws CicsJvmserverResourceException {
        try {
            if (resourceDefined()) {
                this.cicsRegion.ceda().deleteResource(this.cicsTerminal, RESOURCE_TYPE_JVMSERVER, getName(), resourceDefinitionGroup);
            }
        } catch (CicstsManagerException e) {
            String message = "Problem deleteing " + RESOURCE_TYPE_JVMSERVER + " " + getName();
            if (ignoreErrors) {
                logger.warn(message + " - " + e.getMessage());
            } else {
                throw new CicsJvmserverResourceException(message, e);
            }
        }
        if (this.jvmprofile != null) {
            try {
                this.jvmprofile.delete();
            } catch (CicstsManagerException e) {
                String message = "Problem deleteing JVM profile for " + RESOURCE_TYPE_JVMSERVER + " " + getName();
                if (ignoreErrors) {
                    logger.warn(message + " - " + e.getMessage());
                } else {
                    throw new CicsJvmserverResourceException(message, e);
                }
            }
        }
        if (isLiberty()) {
        	try {
				getLibertyServer().delete();
			} catch (ZosLibertyServerException e) {
				String message = "Problem deleteing Liberty server for " + RESOURCE_TYPE_JVMSERVER + " " + getName();
	            if (ignoreErrors) {
	                logger.warn(message + " - " + e.getMessage());
	            } else {
	                throw new CicsJvmserverResourceException(message, e);
	            }
			}
        }
        try {
            clearJvmLogs();
        } catch (CicstsManagerException e) {
            String message = "Problem deleteing logs for " + RESOURCE_TYPE_JVMSERVER + " " + getName();
            if (ignoreErrors) {
                logger.warn(message + " - " + e.getMessage());
            } else {
                throw new CicsJvmserverResourceException(message, e);
            }
        }
        try {
            if (getDefaultLogsDiretory() != null && getDefaultLogsDiretory().exists()) {
                getDefaultLogsDiretory().directoryDeleteNonEmpty();
            }
        } catch (CicsJvmserverResourceException | ZosUNIXFileException e) {
            String message = "Problem deleteing logs directory for " + RESOURCE_TYPE_JVMSERVER + " " + getName();
            if (ignoreErrors) {
                logger.warn(message + " - " + e.getMessage());
            } else {
                throw new CicsJvmserverResourceException(message, e);
            }
        }
    }

    @Override
    public void discard() throws CicsJvmserverResourceException {
        try {
            if (resourceInstalled()) {
                this.cicsRegion.cemt().discardResource(cicsTerminal, RESOURCE_TYPE_JVMSERVER, getName());
            }
        } catch (CicstsManagerException e) {
            throw new CicsJvmserverResourceException("Problem discarding " + RESOURCE_TYPE_JVMSERVER + " " + getName(), e);
        }
    }

    @Override
    public void disableDiscardDelete(boolean ignoreErrors) throws CicsJvmserverResourceException {
        disable();
        discard();
        delete(ignoreErrors);
    }

    @Override
    public void setThreadLimit(int threadlimit) throws CicsJvmserverResourceException {
        try {
            if (!resourceInstalled()) {
                throw new CicsJvmserverResourceException(RESOURCE_TYPE_JVMSERVER + " " + getName() + " does not exist");
            }
            this.cicsRegion.cemt().setResource(this.cicsTerminal, RESOURCE_TYPE_JVMSERVER, getName(), "THREADLIMIT(" + threadlimit + ")");
        } catch (CicstsManagerException e) {
            throw new CicsJvmserverResourceException("Problem setting THREADLIMIT for " + RESOURCE_TYPE_JVMSERVER + " " + getName(), e);
        }
    }

    @Override
    public int getThreadLimit() throws CicsJvmserverResourceException {
        CicstsHashMap cemtMap;
        try {
             cemtMap = cemtInquire();
        } catch (CicsJvmserverResourceException e) {
            throw new CicsJvmserverResourceException("Problem getting THREADLIMIT for " + RESOURCE_TYPE_JVMSERVER + " " + getName(), e);
        }
        return Integer.valueOf(cemtMap.get("Threadlimit"));
    }

    @Override
    public int getThreadCount() throws CicsJvmserverResourceException {
        CicstsHashMap cemtMap;
        try {
             cemtMap = cemtInquire();
        } catch (CicsJvmserverResourceException e) {
            throw new CicsJvmserverResourceException("Problem getting THREADCOUNT for " + RESOURCE_TYPE_JVMSERVER + " " + getName(), e);
        }
        return Integer.valueOf(cemtMap.get("Threadcount"));
    }

    @Override
    public void build() throws CicsJvmserverResourceException {
        try {
            this.jvmprofile.build();
            if (isLiberty()) {
                this.zosLibertyServer.build();
            }
            buildResourceDefinition();
            installResourceDefinition();
        } catch (CicsJvmserverResourceException | ZosLibertyServerException e) {
            throw new CicsJvmserverResourceException("Problem building " + RESOURCE_TYPE_JVMSERVER + " " + getName(), e);
        }
    }

    @Override
    public IJvmprofile getJvmprofile() {
        return this.jvmprofile;
    }

    @Override
    public void buildProfile() throws CicsJvmserverResourceException {
        this.jvmprofile.build();
    }

    @Override
    public boolean isProfileBuilt() {
        return false;
    }

    @Override
    public void setLibertyServer(IZosLibertyServer zosLibertyServer) {
        this.zosLibertyServer = zosLibertyServer;
    }

    @Override
    public String getName() {
        return this.resourceDefinitionName;
    }

    @Override
    public IZosLibertyServer getLibertyServer() {
        return this.zosLibertyServer;
    }

    @Override
    public IZosUNIXFile getJavaHome() throws CicsJvmserverResourceException {
        if (this.javaHome == null) {
            String javaHomeValue = this.jvmprofile.getProfileValue(OPTION_JAVA_HOME);
            if (javaHomeValue == null) {
                javaHomeValue = getDefaultJavaHomeValue();
                this.jvmprofile.setProfileValue(OPTION_JAVA_HOME, javaHomeValue);
            }
            try {
                this.javaHome = this.zosFileHandler.newUNIXFile(javaHomeValue, this.cicsZosImage);
            } catch (ZosUNIXFileException e) {
                throw new CicsJvmserverResourceException("Problem getting Java home", e);
            }
        }
        return this.javaHome;
    }

    @Override
    public IZosUNIXFile getWorkingDirectory() throws CicsJvmserverResourceException {
        if (this.workingDirectory == null) {
            String workDirValue = this.jvmprofile.getProfileValue(OPTION_WORK_DIR);
            if (workDirValue == null) {
                workDirValue = getDefaultWorkingDirectoryValue();
                this.jvmprofile.setProfileValue(OPTION_WORK_DIR, workDirValue);
            }
            if (!workDirValue.startsWith(SLASH_SYBMOL)) {
                workDirValue = getHomeDirectory() + workDirValue;
            }
            try {
                this.workingDirectory = this.zosFileHandler.newUNIXFile(workDirValue, this.cicsZosImage);
            } catch (ZosUNIXFileException e) {
                throw new CicsJvmserverResourceException("Problem getting working directory", e);
            }
        }
        return this.workingDirectory;
    }

    @Override
    public IJvmserverLog getJvmLog() throws CicsJvmserverResourceException {
        if (this.jvmLogLog == null) {
            this.jvmLogLog = getLog(LOG_CEMT_PARAMETER, LOG_PROFILE_OPTION, LOG_FILE_SUFFIX);
        }
        return this.jvmLogLog;
    }

    @Override
    public IJvmserverLog getStdOut() throws CicsJvmserverResourceException {
        if (this.stdOutLog == null) {
            this.stdOutLog = getLog(STDOUT_CEMT_PARAMETER, STDOUT_PROFILE_OPTION, STDOUT_FILE_SUFFIX);
        }
        return this.stdOutLog;
    }

    @Override
    public IJvmserverLog getStdErr() throws CicsJvmserverResourceException {
        if (this.stdErrLog == null) {
            this.stdErrLog = getLog(STDERR_CEMT_PARAMETER, STDERR_PROFILE_OPTION, STDERR_FILE_SUFFIX);
        }
        return this.stdErrLog;
    }

    @Override
    public IJvmserverLog getJvmTrace() throws CicsJvmserverResourceException {
        if (this.jvmTraceLog == null) {
            this.jvmTraceLog = getLog(TRACE_CEMT_PARAMETER, TRACE_PROFILE_OPTION, TRACE_FILE_SUFFIX);
        }
        return this.jvmTraceLog;
    }

    @Override
    public void checkpointLogs() throws CicsJvmserverResourceException {
        getJvmLog().checkpoint();
        getStdOut().checkpoint();
        getStdErr().checkpoint();
        getJvmTrace().checkpoint();
    }

    @Override
    public List<IZosUNIXFile> getJavaLogs() throws CicsJvmserverResourceException {
        List <IZosUNIXFile> javaLogs = new ArrayList<>();
        try {
            if (getWorkingDirectory().exists()) {
                SortedMap<String, IZosUNIXFile> directoryList = getWorkingDirectory().directoryList();
                for (Entry<String, IZosUNIXFile> entry : directoryList.entrySet()) {
                    if (entry.getKey().matches(".*/Snap.*\\.trc$") ||
                        entry.getKey().matches(".*/javacore.*\\.txt$")) {
                        javaLogs.add(entry.getValue());
                    } else if(entry.getKey().matches(".*/jitdump.*\\.dmp$")) {
                        entry.getValue().setDataType(UNIXFileDataType.BINARY);
                        javaLogs.add(entry.getValue());
                    }
                }
            }
        } catch (ZosUNIXFileException | CicsJvmserverResourceException e) {
            throw new CicsJvmserverResourceException("Unable to list working directory", e);
        }
        return javaLogs;
    }

    @Override
    public void saveToResultsArchive() throws CicsJvmserverResourceException {
        saveToResultsArchive(getDefaultRasPath());
    }

    @Override
    public void saveToResultsArchive(String rasPath) throws CicsJvmserverResourceException {
        getJvmprofile().saveToResultsArchive(rasPath);

        getJvmLog().saveToResultsArchive(rasPath);
        getStdOut().saveToResultsArchive(rasPath);
        getStdErr().saveToResultsArchive(rasPath);
        getJvmTrace().saveToResultsArchive(rasPath);

        saveDiagnosticsToResultsArchive(rasPath);
        saveJavaLogsToResultsArchive(rasPath);
        if (isLiberty()) {
            try {
                this.zosLibertyServer.saveToResultsArchive(rasPath + SLASH_SYBMOL + "libertyServers" + SLASH_SYBMOL + this.zosLibertyServer.getServerName() + SLASH_SYBMOL);
            } catch (ZosLibertyServerException e) {
                throw new CicsJvmserverResourceException("Unable to store the content of the Liberty server logs and configuration to the Results Archive Store", e);
            }
        }
    }

    @Override
    public void clearJvmLogs() throws CicsJvmserverResourceException {
        if (this.jvmLogLog != null) {
            this.jvmLogLog.delete();
        }
        if (this.stdOutLog != null) {
            this.stdOutLog.delete();
        }
        if (this.stdErrLog != null) {
            this.stdErrLog.delete();
        }
        if (this.jvmTraceLog != null) {
            this.jvmTraceLog.delete();
        }
        deleteDiagnostics();
        deleteJavaLogs();
        if (isLiberty()) {
            try {
                this.zosLibertyServer.clearLogs();
            } catch (ZosLibertyServerException e) {
                throw new CicsJvmserverResourceException("Unable to clear the Liberty server logs", e);
            }
        }
    }

    @Override
    public String toString() {
        return "[JVM server] " + getName();
    }

    protected String getDefaultRasPath() {
        return this.cicsResourceManager.getCurrentTestMethodArchiveFolder().toString() + SLASH_SYBMOL + getApplid() + SLASH_SYBMOL + getName() + SLASH_SYBMOL;
    }

    protected int getDefaultTimeout() throws CicsJvmserverResourceException {
        if (this.defaultTimeout == -1) {
            try {
                this.defaultTimeout = DefaultResourceTimeout.get(this.cicsZosImage);
            } catch (CicsResourceManagerException e) {
                throw new CicsJvmserverResourceException("Problem creating getting default resource timeout", e);
            }
        }
        return this.defaultTimeout;
    }

    protected IJvmserverLog getLog(String cemtParameter, String jvmprofileOption, String fileSuffix) throws CicsJvmserverResourceException {
        String logName = getLogName(cemtParameter, jvmprofileOption, fileSuffix);
        try {
            if (isDdname(logName)) {
                return new JvmserverLogImpl(this.cicsRegion.getRegionJob().getSpoolFile(getDdname(logName)), newLogScanner());
            } else {
                return new JvmserverLogImpl(this.zosFileHandler.newUNIXFile(logName, this.cicsZosImage), newLogScanner());
            }
        } catch (ZosBatchException | ZosUNIXFileException | CicstsManagerException e) {
            throw new CicsJvmserverResourceException("Problem creating IJvmserverLog object for " + cemtParameter, e);
        }

    }

    protected String getDdname(String logName) {
        return logName.substring(5);
    }

    protected String getLogName(String cemtParameter, String jvmprofileOption, String fileSuffix) throws CicsJvmserverResourceException {
        // First try CEMT
        if (resourceInstalled()) {
            CicstsHashMap cemtMap = cemtInquire();
            if (cemtMap != null && cemtMap.containsKey(cemtParameter)) {
                return cemtMap.get(cemtParameter);
            }
        }

        // Try jvmprofile
        if (this.jvmprofile.containsOption(jvmprofileOption)) {
            String value = this.jvmprofile.getProfileValue(jvmprofileOption);
            if (value != null) {
                String parsedValue = parseJvmprofileSymbols(value);
                if (!parsedValue .contains(SYMBOL_DATE) && !parsedValue.contains(SYMBOL_TIME)) {
                    return parsedValue;
                }
            }
        }

        // Look on the file system
        for(Map.Entry<String, IZosUNIXFile> entry : decendingDirectoryList(getDefaultLogsDiretory()).entrySet()) {
            if (entry.getKey().endsWith("." + fileSuffix)) {
                return entry.getValue().getUnixPath();
            }
        }

        throw new CicsJvmserverResourceException("Unable to establish JVM server " + cemtParameter + " log file");

    }

    protected boolean isDdname(String logName) {
        return logName.startsWith("//DD:");
    }

    protected void getSystemValues() throws CicsJvmserverResourceException {
        try {
            this.cicsRegion.ceci().startCECISession(this.cicsTerminal);
            this.cicsRegion.ceci().issueCommand(this.cicsTerminal, "INQUIRE SYSTEM JOBNAME(&JOBNAME)", false).getResponseOutputValues();
            this.cicsRegionJobname = this.cicsRegion.ceci().retrieveVariableText(this.cicsTerminal, "&JOBNAME").trim();
            this.cicsRegion.ceci().issueCommand(this.cicsTerminal, "INQUIRE SYSTEM REGIONUSERID(&USER)", false).getResponseOutputValues();
            this.cicsRegionUserid = this.cicsRegion.ceci().retrieveVariableText(this.cicsTerminal, "&USER").trim();
        } catch (CicstsManagerException e) {
            throw new CicsJvmserverResourceException("Unable to retrieve CICS region system values", e);
        }
    }

    protected String getCicsJobname() throws CicsJvmserverResourceException {
        if (this.cicsRegionJobname == null) {
            getSystemValues();
        }
        return this.cicsRegionJobname;
    }

    protected String getCicsRegionUserid() throws CicsJvmserverResourceException {
        if (this.cicsRegionUserid == null) {
            getSystemValues();
        }
        return this.cicsRegionUserid;
    }

    protected String buildResourceParameters() {
        StringBuilder resourceParameters = new StringBuilder();
        appendNotNull(resourceParameters, "DESCRIPTION", getResourceDefinitionDescriptionAttribute());
        appendNotNull(resourceParameters, "STATUS", getResourceDefinitionStatusAttribute().toString());
        appendNotNull(resourceParameters, "JVMPROFILE", getResourceDefinitionJvmprofileAttribute());
        appendNotNull(resourceParameters, "LERUNOPTS", getResourceDefinitionLerunoptsAttribute());
        appendNotNull(resourceParameters, "THREADLIMIT", Integer.toString(getResourceDefinitionThreadlimitAttribute()));
        return resourceParameters.toString();
    }

    protected StringBuilder appendNotNull(StringBuilder resourceParameters, String attribute, String value) {
        if (value != null && !value.isEmpty()) {
            resourceParameters.append(attribute);
            resourceParameters.append(" (");
            resourceParameters.append(value);
            resourceParameters.append(") ");
        }
        return resourceParameters;
    }

    protected CicstsHashMap cemtInquire() throws CicsJvmserverResourceException {
        if (!resourceInstalled()) {
            throw new CicsJvmserverResourceException(RESOURCE_TYPE_JVMSERVER + " " + getName() + " does not exist");
        }
        CicstsHashMap cemtMap;
        try {
            cemtMap = this.cicsRegion.cemt().inquireResource(this.cicsTerminal, RESOURCE_TYPE_JVMSERVER, getName());
        } catch (CicstsManagerException e) {
            throw new CicsJvmserverResourceException("Problem inquiring " + RESOURCE_TYPE_JVMSERVER + " " + getName(), e);
        }
        return cemtMap;
    }

    protected String parseJvmprofileSymbols(String value) throws CicsJvmserverResourceException {
        if (value.contains(SYMBOL_DATE) || value.contains(SYMBOL_TIME)) {
            throw new CicsJvmserverResourceException("JVM profile options \"" + SYMBOL_DATE + "\" and \"" + SYMBOL_TIME + "\" not supported by JVM server Manager");
        }
        String parsedValue = StringUtils.replace(value, SYMBOL_APPLID, getApplid());
        parsedValue = StringUtils.replace(parsedValue, SYMBOL_USSHOME, getUsshome());
        parsedValue = StringUtils.replace(parsedValue, SYMBOL_CONFIGROOT, getUsshome());
        parsedValue = StringUtils.replace(parsedValue, SYMBOL_JVMSERVER, getName());
        parsedValue = StringUtils.replace(parsedValue, SYMBOL_USSHOME, getUsshome());

        return parsedValue;
    }

    protected String getUsshome() throws CicsJvmserverResourceException {
        if (this.cicsUsshome == null) {
            try {
                this.cicsUsshome = this.cicsRegion.getUssHome();
            } catch (CicstsManagerException e) {
                throw new CicsJvmserverResourceException("Unable to get value of USSHOME", e);
            }
        }
        return this.cicsUsshome;
    }

    protected String getApplid() {
        if (this.cicsApplid == null) {
            this.cicsApplid = this.cicsRegion.getApplid();
        }
        return this.cicsApplid;
    }

    protected String getJvmProfileDir() throws CicsJvmserverResourceException {
        if (this.cicsJvmprofileDir == null) {
            try {
                this.cicsJvmprofileDir = this.cicsRegion.getJvmProfileDir();
            } catch (CicstsManagerException e) {
                throw new CicsJvmserverResourceException("Unable to get value of JVMPROFILEDIR", e);
            }
        }
        return this.cicsJvmprofileDir;
    }

    protected String getConfigRoot() throws CicsJvmserverResourceException {
        if (this.cicsConfigroot == null) {
            //TOODO: get CONFIGROOT
            this.cicsConfigroot = getJvmProfileDir();
        }
        return this.cicsConfigroot;
    }

    protected IZosUNIXFile getDiagnosticsDirectory() throws CicsJvmserverResourceException {
        if (this.diagnosticsDirectory == null) {
            try {
                this.diagnosticsDirectory = this.zosFileHandler.newUNIXFile(getWorkingDirectory() + SLASH_SYBMOL + "diagnostics" + SLASH_SYBMOL + getApplid() + SLASH_SYBMOL + getName() + SLASH_SYBMOL, cicsZosImage);
            } catch (ZosUNIXFileException | CicsJvmserverResourceException e) {
                throw new CicsJvmserverResourceException("Unable to get diagnostics directory", e);
            }
        }
        return this.diagnosticsDirectory;
    }

    protected void saveDiagnosticsToResultsArchive(String rasPath) throws CicsJvmserverResourceException {
        try {
            if (getDiagnosticsDirectory() != null && getDiagnosticsDirectory().exists()) {
                String diagnostics = "diagnostics";
                if (!rasPath.endsWith(SLASH_SYBMOL)) {
                    diagnostics = SLASH_SYBMOL + diagnostics;
                }
                getDiagnosticsDirectory().saveToResultsArchive(rasPath + diagnostics);
            }
        } catch (ZosUNIXFileException e) {
            throw new CicsJvmserverResourceException("Unable to archive diagnostics directory", e);
        }
    }

    protected void saveJavaLogsToResultsArchive(String rasPath) throws CicsJvmserverResourceException {
        for (IZosUNIXFile file : getJavaLogs()) {
            try {
                file.saveToResultsArchive(rasPath);
            } catch (ZosUNIXFileException e) {
                throw new CicsJvmserverResourceException("Unable to archive Java log file", e);
            }
        }
    }

    protected void deleteDiagnostics() throws CicsJvmserverResourceException {
        try {
            this.zosFileHandler.newUNIXFile(getDiagnosticsDirectory().getUnixPath() + "..", this.cicsZosImage).directoryDeleteNonEmpty();
        } catch (ZosUNIXFileException e) {
            throw new CicsJvmserverResourceException("Problem deleting JVM server diagnostics directory", e);
        }
    }

    protected void deleteJavaLogs() throws CicsJvmserverResourceException {
        for (IZosUNIXFile file : getJavaLogs()) {
            try {
                file.delete();
            } catch (ZosUNIXFileException e) {
                throw new CicsJvmserverResourceException("Problem deleting JVM server Java logs", e);
            }
        }
    }

    protected void cleanup() {
        if (shouldArchive()) {
            try {
                saveToResultsArchive();
            } catch (CicsJvmserverResourceException e) {
                logger.error("Problem in cleanup phase", e);
            }
        }
        if (shouldCleanup()) {
            try {
                if (!resourceInstalled()) {
                    logger.info(RESOURCE_TYPE_JVMSERVER + " " + getName() + " has not been installed");
                } else {
                    try {
                        disableWithEscalate();
                    } catch (CicsJvmserverResourceException e) {
                        logger.error("Problem in cleanup phase", e);
                    }
                    try {
                        discard();
                    } catch (CicsJvmserverResourceException e) {
                        logger.error("Problem in cleanup phase", e);
                    }
                }
            } catch (CicstsManagerException e) {
                logger.error("Problem in cleanup phase", e);
            }
            try {
                delete(true);
            } catch (CicsJvmserverResourceException e) {
                logger.error("Problem in cleanup phase", e);
            }
        }
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
}
