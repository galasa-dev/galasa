/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.java.ubuntu.spi;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.CpuArchitecture;
import dev.galasa.OperatingSystem;
import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.SetContentType;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.java.JavaManagerException;
import dev.galasa.java.JavaType;
import dev.galasa.java.JavaVersion;
import dev.galasa.java.spi.JavaInstallationImpl;
import dev.galasa.java.ubuntu.IJavaUbuntuInstallation;
import dev.galasa.java.ubuntu.JavaUbuntuManagerException;
import dev.galasa.java.ubuntu.internal.JavaUbuntuManagerImpl;
import dev.galasa.linux.ILinuxImage;
import dev.galasa.linux.spi.ILinuxManagerSpi;

public class JavaUbuntuInstallationImpl extends JavaInstallationImpl implements IJavaUbuntuInstallation {

    private final static Log                   logger       = LogFactory.getLog(JavaUbuntuInstallationImpl.class);

    private final JavaUbuntuManagerImpl javaUbuntuManager;

    private final String javaDirectoryName;
    private Path javaHome;
    private Path jacocoAgent;

    private static final Pattern findHome = Pattern.compile("^(\\.\\/){0,1}(([a-zA-Z0-9\\-\\+\\._/ \\\\]*)?/)?\\Qbin/java\\E$", Pattern.MULTILINE);

    private final ArrayList<Path> jacocoExecs = new ArrayList<>();
    private int execFileNumber;

    private final String imageTag;
    private ILinuxImage image;
    private Path home;
    private Path runHome;

    public JavaUbuntuInstallationImpl(JavaUbuntuManagerImpl javaUbuntuManager, 
            JavaType javaType, 
            JavaVersion javaVersion, 
            String javaJvm, 
            String javaTag, 
            String imageTag) throws JavaManagerException {
        super(javaUbuntuManager.getJavaManager(),
                javaType,
                OperatingSystem.linux,
                CpuArchitecture.x64,
                javaVersion, 
                javaJvm, 
                javaTag);

        this.imageTag = imageTag;
        this.javaUbuntuManager = javaUbuntuManager;

        this.javaDirectoryName = javaType.name() 
                + "_" 
                + javaVersion.name()
                + "_"
                + javaJvm;

    }

    public void build() throws JavaManagerException, ResourceUnavailableException {
        try {
            ILinuxManagerSpi linuxManager = this.javaUbuntuManager.getLinuxManager();

            this.image = linuxManager.getImageForTag(this.imageTag);
            this.home = this.image.getHome();
            this.runHome = this.image.getRunDirectory();
        } catch(Exception e) {
            throw new JavaManagerException("Unable to determine home and run home directories", e);
        }

        buildJavaHome();
        buildJacocoAgent();
    }

    private void buildJavaHome() throws JavaManagerException, ResourceUnavailableException {
        String actualFilename = getArchiveFilename();

        try {
            Path managerHome  = home.resolve(JavaUbuntuManagerImpl.NAMESPACE);
            Path javasHome    = managerHome.resolve("javas");
            Path archivesHome = this.image.getArchivesDirectory().resolve(JavaUbuntuManagerImpl.NAMESPACE);         


            // for DSE and speed reasons, check to see if we need to install the 
            // Java installation.

            if (Files.exists(javasHome)) {
                Path possibleJavaHome = javasHome.resolve(this.javaDirectoryName);
                if (Files.exists(possibleJavaHome)) {
                    this.javaHome = possibleJavaHome;
                    ICommandShell commandShell = image.getCommandShell();
                    String response = commandShell.issueCommand(this.javaHome.resolve("bin/java").toString() + " -version");
                    logger.info("Java installation for tag " + getTag() + " is preinstalled at " + this.javaHome + " with version information:-\n" + response);
                    return;
                }
            }


            // not preinstalled,  so check if the archive is available
            Path remoteArchive = null;

            if (Files.exists(archivesHome)) {
                Path possibleArchive = archivesHome.resolve(actualFilename);
                if (Files.exists(possibleArchive)) {
                    remoteArchive = possibleArchive;
                    logger.trace("Java archive for tag " + getTag() + " is located at " + possibleArchive);
                }
            }

            // Not installed and no archive,  need to download
            if (remoteArchive == null) {
                logger.debug("Java archive for tag " + getTag() + " is being retrieved");
                Path localArchive = retrieveArchive();
                logger.trace("Java archive for tag " + getTag() + " retrieved");

                // Make sure runhome is there, // TODO move to the linux manager
                if (!Files.exists(runHome)) {
                    Files.createDirectory(runHome);
                }


                remoteArchive = runHome.resolve(actualFilename);
                Files.copy(localArchive, remoteArchive);
                logger.debug("Java archive for tag " + getTag() + " has been transferred to the linux image");
            }


            // Install it into the run directory
            Path possibleJavaHomes = runHome.resolve(JavaUbuntuManagerImpl.NAMESPACE + "/javas");
            Files.createDirectories(possibleJavaHomes);
            Path tempExtractDirectory = runHome.resolve(JavaUbuntuManagerImpl.NAMESPACE + "/extract/" + this.javaDirectoryName);
            Files.createDirectories(tempExtractDirectory);


            ICommandShell commandShell = image.getCommandShell();
            if (actualFilename.endsWith(".tgz") || actualFilename.endsWith(".tar.gz")) {
                logger.debug("Extracting Java archive for tag " + getTag());
                String response = commandShell.issueCommand("cd " + tempExtractDirectory + ";tar -xvzf " + remoteArchive + ";echo response=$?");

                if (!response.contains("response=0")) {
                    throw new JavaManagerException("Extract of the Java archive failed\n" + response);
                }


                // Check to see if the java home directory is in a sub folder of the archive
                Matcher matcher = findHome.matcher(response);
                if (!matcher.find()) {
                    throw new JavaManagerException("Unable to locate Java home in the archive");
                }

                Path targetHome = possibleJavaHomes.resolve(this.javaDirectoryName);
                Path extractedHome = tempExtractDirectory;

                String prefixDirectories = matcher.group(3);
                if (prefixDirectories != null && !prefixDirectories.isEmpty()) {
                    if ("/".equals(prefixDirectories)) {
                        throw new JavaManagerException("Unexpected found Java home in the root directory of the archive, ie prefixed /");
                    }
                    extractedHome = tempExtractDirectory.resolve(prefixDirectories);
                }

                response = commandShell.issueCommand("mv " + extractedHome + " " + targetHome + ";echo response=$?");
                if (!response.contains("response=0")) {
                    throw new JavaManagerException("Move of the extracted Java home failed\n" + response);
                }

                this.javaHome = targetHome;

                response = commandShell.issueCommand(this.javaHome.resolve("bin/java").toString() + " -version");
                logger.info("Java installation for tag " + getTag() + " has been installed at " + this.javaHome + " with version information:-\n" + response);

            } else {
                throw new JavaUbuntuManagerException("Unable to support uncompressing of archive, unknown type");
            }
        } catch(Exception e) {
            throw new JavaUbuntuManagerException("Problem installing Java on server", e);
        }
    }

    private void buildJacocoAgent() throws JavaUbuntuManagerException {
        try {
            if (isCodeCoverageRequested()) {
                Path managerHome  = home.resolve(JavaUbuntuManagerImpl.NAMESPACE);
                Path javasHome    = managerHome.resolve("javas");
                Path possibleJacoco = javasHome.resolve("jacocoagent.jar");

                if (Files.exists(possibleJacoco)) {
                    this.jacocoAgent = possibleJacoco;
                    logger.info("Jacoco agent is preinstalled at " + this.jacocoAgent);
                } else {
                    Path localJacoco = retrieveJacocoAgent();

                    if (!Files.exists(runHome)) {
                        Files.createDirectory(runHome);
                    }

                    this.jacocoAgent = runHome.resolve("jacocoagent.jar");
                    Files.copy(localJacoco, this.jacocoAgent);
                    logger.info("Jacoco agent has been copied to " + this.jacocoAgent);
                }
            }
        } catch(Exception e) {
            throw new JavaUbuntuManagerException("Problem installing Java on server", e);
        }
    }

    public void discard() {
        // save any jacoco exec files we may have

        Path saRoot = this.javaUbuntuManager.getFramework().getResultArchiveStore().getStoredArtifactsRoot();        
        Path jacocoDir = saRoot.resolve("java").resolve("jacoco");

        ArrayList<Path> savePaths = new ArrayList<>();
        
        for(Path exec : this.jacocoExecs) {
            Path targetExec = jacocoDir.resolve(exec.getFileName().toString());

            try {
                if (Files.exists(exec)) {
                    ResultArchiveStoreContentType type = ResultArchiveStoreContentType.BINARY;
                    Files.copy(exec, Files.newOutputStream(targetExec, StandardOpenOption.CREATE_NEW, new SetContentType(type)));
                    savePaths.add(exec);
                }
            } catch(Exception e) {
                logger.warn("Failed to save jacoco exec file " + exec,e);
            }
        }

        // Save them to a target location if requested

        try {
            saveCodeCoverageExecs(savePaths);
        } catch(Exception e) {
            logger.warn("Failed to save jacoco execs to save location",e);
        }

        super.discard();
    }

    @Override
    public String getJavaCommand() throws JavaManagerException {
        if (isCodeCoverageRequested()) {
            this.execFileNumber++;

            String testClassName = this.javaUbuntuManager.getTestClassName();

            Path execFile = this.runHome.resolve("jacoco" + this.execFileNumber + ".exec");

            jacocoExecs.add(execFile);

            StringBuilder sb = new StringBuilder();
            sb.append(this.javaHome.resolve("bin/java").toString());
            sb.append(" ");
            sb.append("-javaagent:");
            sb.append(this.jacocoAgent.toString());
            sb.append("=destfile=");
            sb.append(execFile.toString());
            sb.append(",sessionid=");
            sb.append(this.javaUbuntuManager.getFramework().getTestRunName());
            sb.append("_");
            sb.append(testClassName);
            sb.append(Integer.toString(this.execFileNumber));

            return sb.toString();
        } else {
            return this.javaHome.resolve("bin/java").toString();
        }
    }

    @Override
    public String getJavaHome() {
        return this.javaHome.toString();
    }

}
