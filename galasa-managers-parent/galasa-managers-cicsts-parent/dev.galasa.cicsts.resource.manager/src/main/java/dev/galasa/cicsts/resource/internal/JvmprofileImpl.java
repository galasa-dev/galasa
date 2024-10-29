/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.resource.internal;

import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.cicsts.cicsresource.CicsJvmserverResourceException;
import dev.galasa.cicsts.cicsresource.IJvmprofile;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.IZosUNIXFile.UNIXFileDataType;
import dev.galasa.zosfile.ZosUNIXFileException;

public class JvmprofileImpl implements IJvmprofile {
    
    private static final Log logger = LogFactory.getLog(JvmprofileImpl.class);

    private IZosFileHandler zosFileHandler;
    private IZosImage zosImage;
    
    private IZosUNIXFile profileUnixFile;
    private String profileName;
    private Map<String, String> profileMap;
    private String jvmProfileDir;

    private static final String COMMA_SYBMOL = ",";
    private static final String HASH_SYBMOL = "#";
    private static final String PLUS_SYBMOL = "+";
    private static final String MINUS_SYBMOL = "-";
    private static final String EQUALS_SYBMOL = "=";
    private static final String COLON_SYBMOL = ":";
    private static final String BACK_SLASH_SYBMOL = "\\";
    private static final String SLASH_SYBMOL = "/";
    private static final String NEW_LINE = "\n";
    private static final String GCTHREADS = "-Xgcthreads";
    private static final String XMS = "-Xms";
    private static final String XMSO = "-Xmso";
    private static final String XMX = "-Xmx";
    private static final String XSCMX = "-Xscmx";
    private static final String MINUS_D = "-D";
    
    protected static final String OPTION_CLASSPATH_PREFIX = "CLASSPATH_PREFIX";
    protected static final String OPTION_CLASSPATH_SUFFIX = "CLASSPATH_SUFFIX";
    protected static final String OPTION_JAVA_PIPELINE = "JAVA_PIPELINE";
    protected static final String OPTION_SECURITY_TOKEN_SERVICE = "SECURITY_TOKEN_SERVICE";
    protected static final String OPTION_WLP_INSTALL_DIR = "WLP_INSTALL_DIR";
    protected static final String OPTION_WLP_USER_DIR = "WLP_USER_DIR";
    protected static final String OPTION_WLP_OUTPUT_DIR = "WLP_OUTPUT_DIR";
    protected static final String OPTION_WLP_SERVER_NAME = "-Dcom.ibm.cics.jvmserver.wlp.server.name";
    protected static final String OPTION_WLP_SERVER_HTTP_PORT = "-Dcom.ibm.cics.jvmserver.wlp.server.http.port";
    protected static final String OPTION_WLP_SERVER_HTTPS_PORT = "-Dcom.ibm.cics.jvmserver.wlp.server.https.port";
    protected static final String OPTION_WLP_SERVER_HOST = "-Dcom.ibm.cics.jvmserver.wlp.server.host";
    protected static final String OPTION_WLP_AUTOCONFIGURE = "-Dcom.ibm.cics.jvmserver.wlp.autoconfigure";
    protected static final String OPTION_WLP_WAB_ENABLED = "-Dcom.ibm.cics.jvmserver.wlp.wab";

    protected static final String OPTION_ZCEE_INSTALL_DIR = "ZCEE_INSTALL_DIR";
    
    public JvmprofileImpl(IZosFileHandler zosFileHandler, IZosImage zosImage, String jvmprofileName) {
        this.zosFileHandler = zosFileHandler;
        this.zosImage = zosImage;
        this.profileName = jvmprofileName;
        this.profileMap = new HashMap<>();
    }

    public JvmprofileImpl(IZosFileHandler zosFileHandler, IZosImage zosImage, String jvmprofileName, Map<String, String> content) {
        this.zosFileHandler = zosFileHandler;
        this.zosImage = zosImage;
        this.profileName = jvmprofileName;
        this.profileMap = content;
    }

    public JvmprofileImpl(IZosFileHandler zosFileHandler, IZosImage zosImage, String jvmprofileName, String content) {
        this.zosFileHandler = zosFileHandler;
        this.zosImage = zosImage;
        this.profileName = jvmprofileName;
        parseContent(content);
    }

    protected void parseContent(String content) {
        String continuationKey = null;
        this.profileMap = new LinkedHashMap<>();
        String[] contentArray = content.split("\\r?\\n");
        for (int i = 0; i < contentArray.length; i++) {
            String line = contentArray[i];
            List<String> option = parseLine(line);
            if (!option.isEmpty()) {
                String optionKey = option.get(0);
                String optionValue = option.size() >= 2? option.get(1): null;
                if (optionKey.startsWith(PLUS_SYBMOL)) {
                    optionKey = optionKey.substring(1);
                    if (profileMap.containsKey(optionKey) && optionValue != null) {
                        setProfileValue(optionKey, getProfileValue(optionKey) + COMMA_SYBMOL + optionValue);
                    } else {
                        setProfileValue(optionKey, optionValue);
                    }
                } else {
                    if (continuationKey != null) {
                        optionValue = getProfileValue(continuationKey) + optionKey;
                        optionKey = continuationKey;
                    }
                    if (optionValue != null && optionValue.endsWith(BACK_SLASH_SYBMOL)) {
                        continuationKey = optionKey;
                        optionValue = optionValue.substring(0, optionValue.length()-1);
                    } else {
                        continuationKey = null;
                    }
                    setProfileValue(optionKey, optionValue);
                }
            }
        }
    }

    protected List<String> parseLine(String line) {
        List<String> option = new ArrayList<>();
        line = line.trim();
        if (!line.startsWith(HASH_SYBMOL) && !line.isEmpty()) {
            if (line.startsWith(XMSO)) {
                option.add(line.substring(0, XMSO.length()));
                option.add(line.substring(XMSO.length()));
            } else if (line.startsWith(XMS)) {
                option.add(line.substring(0, XMS.length()));
                option.add(line.substring(XMS.length()));
            } else if (line.startsWith(XMX)) {
                option.add(line.substring(0, XMX.length()));
                option.add(line.substring(XMX.length()));
            } else if (line.startsWith(XSCMX)) {
                option.add(line.substring(0, XSCMX.length()));
                option.add(line.substring(XSCMX.length()));
            } else if (line.startsWith(GCTHREADS)) {
                option.add(line.substring(0, GCTHREADS.length()));
                option.add(line.substring(GCTHREADS.length()));
            } else if (line.startsWith(MINUS_SYBMOL) && !line.startsWith(MINUS_D)) {
                if (line.contains(COLON_SYBMOL)) {
                    String[] lineArray = line.split(COLON_SYBMOL);
                    if (lineArray.length >= 2) {
                        option.add(lineArray[0] + COLON_SYBMOL);
                        StringBuilder value = new StringBuilder();
                        for (int i = 1; i < lineArray.length; i++) {
                            value.append(lineArray[1]);
                            value.append(COLON_SYBMOL);
                        }
                        if (!line.endsWith(COLON_SYBMOL)) {
                            value.delete(value.length()-1, value.length());
                        }
                        option.add(value.toString());
                    } 
                }                
            } else {
                String[] lineArray = line.split(EQUALS_SYBMOL);
                option.add(lineArray[0]);
                if (lineArray.length >= 2) {
                    option.add(lineArray[1]);
                }
            }
        }
        
        // Strip trailing comments
        if (option.size() == 2) {
            String optionValue = option.get(1);
            int posComment = StringUtils.ordinalIndexOf(optionValue, " #", 1);
            int posQuote1 = StringUtils.ordinalIndexOf(optionValue, "\"", 1);
            int posQuote2 = StringUtils.ordinalIndexOf(optionValue, "\"", 2);
            if ((posComment > -1 && posQuote1 < 0) || (posComment > -1 && posQuote1  < posComment && posQuote2 > -1)) {
                option.set(1, optionValue.substring(0, posComment).trim());
            }
        }
        return option;
    }


    @Override
    public void setProfileName(String name) throws CicsJvmserverResourceException {
        this.profileName = name;
        resetUnixFile();
    }

    @Override
    public void setJvmProfileDir(String jvmProfileDir) throws CicsJvmserverResourceException {
        this.jvmProfileDir = jvmProfileDir;
        resetUnixFile();
    }

    private void resetUnixFile() throws CicsJvmserverResourceException {
        try {
            this.profileUnixFile = this.zosFileHandler.newUNIXFile(this.getJvmProfileDir() + "/" + this.getProfileName(), this.zosImage);
        } catch (ZosUNIXFileException e) {
            throw new CicsJvmserverResourceException("Problem setting JVM profile zOS UNIX file", e);
        }
    }

    @Override
    public IZosUNIXFile getProfile() {
        return this.profileUnixFile;
    }

    @Override
    public String getProfileName() {
        return this.profileName;
    }

    @Override
    public void setProfileValue(String key, String value) {
        this.profileMap.put(key, value);
    }

    @Override
    public void appendProfileValue(String key, String value) {
        this.profileMap.put(key, getProfileValue(key) + COMMA_SYBMOL + value);
    }

    @Override
    public String getProfileValue(String key) {
        return this.profileMap.get(key);
    }

    @Override
    public void removeProfileValue(String key) {
        this.profileMap.remove(key);

    }

    @Override
    public boolean containsOption(String option) {
        return this.profileMap.containsKey(option);
    }

    @Override
    public Map<String, String> getProfileMap() {
        return this.profileMap;
    }

    @Override
    public String getProfileString() {
        StringBuilder content = new StringBuilder();
        for (Entry<String, String> entry : this.profileMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value == null) {
                value = "";  
            }
            if (key.endsWith(":") || key.equals(XMSO) || key.equals(XMS) || key.equals(XMX) || key.equals(XSCMX) || key.equals(GCTHREADS)) {
                content.append(key);
                content.append(value);
                content.append(NEW_LINE);
            } else if (!value.isEmpty()) {
                content.append(key);
                content.append(EQUALS_SYBMOL);
                content.append(value);
                content.append(NEW_LINE);
            }
        }
        return content.toString();
    }

    @Override
    public String getJvmProfileDir() {
        return this.jvmProfileDir;
    }

    @Override
    public void printProfile() {
        logger.info("JVM profile " + this.profileName + " parsed content:\n" + getProfileString());
    }

    @Override
    public void build() throws CicsJvmserverResourceException {
        try {
            if (this.profileUnixFile == null || !this.profileUnixFile.getFileName().equals(getProfileFileName())) {
                if (getJvmProfileDir() == null) {
                    throw new CicsJvmserverResourceException("Unable to build JVM profile - JVM profile directory not supplied");
                }
                this.profileUnixFile = this.zosFileHandler.newUNIXFile(getJvmProfileDir() + SLASH_SYBMOL + getProfileFileName(), zosImage);
                this.profileUnixFile.setDataType(UNIXFileDataType.TEXT);
            }
            if (!this.profileUnixFile.exists()) {
                this.profileUnixFile.setShouldCleanup(false);
                this.profileUnixFile.create(PosixFilePermissions.fromString("rwxrwxrwx"));
            }
            this.profileUnixFile.storeText(getProfileString());
        } catch (ZosUNIXFileException e) {
            throw new CicsJvmserverResourceException("Problem creating JVM profile on zOS UNIX file system", e);
        }
    }

    @Override
    public void delete() throws CicsJvmserverResourceException {
        try {
            if (this.profileUnixFile != null && this.profileUnixFile.exists()) {
                this.profileUnixFile.delete();
            }
        } catch (ZosUNIXFileException e) {
            throw new CicsJvmserverResourceException("Problem deleteing the JVM profile from zOS UNIX file system", e);
        }
    }

    @Override
    public void saveToResultsArchive(String rasPath) throws CicsJvmserverResourceException {
        if (this.profileUnixFile != null) {
            try {
                this.profileUnixFile.saveToResultsArchive(rasPath);
            } catch (ZosUNIXFileException e) {
                throw new CicsJvmserverResourceException("Problem saving the JVM profile from zOS UNIX file system", e);
            }
        }
    }

    private String getProfileFileName() {
        return this.profileName + ".jvmprofile";
    }

    @Override
    public void setWlpInstallDir(String wlpInstallDir) throws CicsJvmserverResourceException {
        setProfileValue(OPTION_WLP_INSTALL_DIR, wlpInstallDir);

    }

    @Override
    public void setWlpUserDir(String wlpUserDir) throws CicsJvmserverResourceException {
        setProfileValue(OPTION_WLP_USER_DIR, wlpUserDir);
    }

    @Override
    public void setWlpOutputDir(String wlpOutputDir) throws CicsJvmserverResourceException {
        setProfileValue(OPTION_WLP_OUTPUT_DIR, wlpOutputDir);
    }

    @Override
    public void setWlpServerName(String serverName) throws CicsJvmserverResourceException {
        setProfileValue(OPTION_WLP_SERVER_NAME, serverName);
    }

    @Override
    public void setZosConnectInstallDir() throws CicsJvmserverResourceException {
        try {
            setZosConnectInstallDir(this.zosImage.getZosConnectInstallDir());
        } catch (ZosManagerException e) {
            throw new CicsJvmserverResourceException("Unable to get zOS Connetct install directory", e);
        }
    }

    @Override
    public void setZosConnectInstallDir(String zOSConnectInstallDir) throws CicsJvmserverResourceException {
        setProfileValue(OPTION_ZCEE_INSTALL_DIR, zOSConnectInstallDir);
    }

    @Override
    public void setWlpAutoconfigure(boolean autoconfigure) throws CicsJvmserverResourceException {
        setProfileValue(OPTION_WLP_AUTOCONFIGURE, String.valueOf(autoconfigure));
    }

    @Override
    public void setWlpServerHost(String hostname) throws CicsJvmserverResourceException {
        setProfileValue(OPTION_WLP_SERVER_HOST, hostname);
    }

    @Override
    public void setWlpServerHttpPort(int httpPort) throws CicsJvmserverResourceException {
        setProfileValue(OPTION_WLP_SERVER_HTTP_PORT, Integer.toString(httpPort));
    }

    @Override
    public void setWlpServerHttpsPort(int httpsPort) throws CicsJvmserverResourceException {
        setProfileValue(OPTION_WLP_SERVER_HTTPS_PORT, Integer.toString(httpsPort));
    }

    @Override
    public void setWlpServerWabEnabled(boolean wabEnabled) throws CicsJvmserverResourceException {
        setProfileValue(OPTION_WLP_WAB_ENABLED, String.valueOf(wabEnabled));
    }

    @Override
    public String getWlpInstallDir() throws CicsJvmserverResourceException {
        return getProfileValue(OPTION_WLP_INSTALL_DIR);
    }

    @Override
    public String getWlpUserDir() {
        return getProfileValue(OPTION_WLP_USER_DIR);
    }

    @Override
    public String getWlpOutputDir() {
        return getProfileValue(OPTION_WLP_OUTPUT_DIR);
    }

    @Override
    public String getWlpServerName() {
        return getProfileValue(OPTION_WLP_SERVER_NAME);
    }

    @Override
    public String getWlpAutoconfigure() {
        return getProfileValue(OPTION_WLP_AUTOCONFIGURE);
    }

    @Override
    public String getWlpServerHost() {
        return getProfileValue(OPTION_WLP_SERVER_HOST);
    }

    @Override
    public String getWlpServerHttpPort() {
        return getProfileValue(OPTION_WLP_SERVER_HTTP_PORT);
    }

    @Override
    public String getWlpServerHttpsPort() {
        return getProfileValue(OPTION_WLP_SERVER_HTTPS_PORT);
    }

    @Override
    public boolean getWlpServerWabEnabled() {
        return Boolean.valueOf(getProfileValue(OPTION_WLP_WAB_ENABLED));
    }

    @Override
    public String toString() {
        return "[JVM profile] " + getProfileName();
    }

	protected void cleanup() {		
		try {
			delete();
		} catch (CicsJvmserverResourceException e) {
			logger.error("Problem in cleanup phase", e);
		}
	}
}
