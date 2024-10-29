/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.galasaecosystem.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.artifact.IBundleResources;
import dev.galasa.framework.spi.InsufficientResourcesAvailableException;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;
import dev.galasa.galasaecosystem.IsolationInstallation;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.java.IJavaInstallation;
import dev.galasa.linux.ILinuxImage;
import dev.galasa.linux.LinuxManagerException;

public class LocalLinuxEcosystemImpl extends LocalEcosystemImpl {

    private final Log                        logger = LogFactory.getLog(getClass());

    private final static Pattern processPattern = Pattern.compile("^\\QPROCESS=\\E(\\d+)$", Pattern.MULTILINE);
    private final static Pattern runnamePattern = Pattern.compile("Allocated Run Name (\\w+) to this run");

    private final ILinuxImage linuxImage;

    private int internalRunNumber = 0;
    private int saveSimPlatformNumber = 0;

    private Path scriptFile;

    private final ArrayList<Path> runFiles = new ArrayList<Path>();
    private final HashMap<Path, String> runNameFiles = new HashMap<>();

    public LocalLinuxEcosystemImpl(GalasaEcosystemManagerImpl manager, String tag,
            ILinuxImage linuxImage, IJavaInstallation javaInstallation, 
            IsolationInstallation isolationInstallation,
            boolean startSimPlatform,
            String defaultZosImage) throws LinuxManagerException, InsufficientResourcesAvailableException, GalasaEcosystemManagerException {
        super(manager, tag, javaInstallation, isolationInstallation, startSimPlatform, defaultZosImage);

        this.linuxImage = linuxImage;
    }

    @Override
    public void build() throws GalasaEcosystemManagerException {
        try {
            logger.debug("Building a Galasa Local Ecosystem on Linux image " + this.linuxImage.getImageID());

            Path home = this.linuxImage.getHome();
            Path runHome = this.linuxImage.getRunDirectory();
            build(runHome, home);
            this.scriptFile = getGalasaConfigDirectory().resolve("galasaboot.sh");
        } catch (Exception e) {
            throw new GalasaEcosystemManagerException("Problem building the Local Ecosystem on Linux", e); 
        }

    }

    @Override
    public void stop() {
        // At the moment, nothing to stop
    }

    @Override
    public void discard() {
        // Will let the Linux manager discard to home dir, but we need to save all the data from the remote ecosystem

        super.discard();

        for(Path consoleFile : this.runFiles) {
            saveConsoleLog(consoleFile);
        }
        
        try {
            stopSimPlatform();
        } catch (GalasaEcosystemManagerException e) {
            logger.warn("Problem stopping SimPlatform during discard",e);
        }

    }

    private void saveConsoleLog(Path consoleFile) {
        String runName = this.runNameFiles.get(consoleFile);

        if (!Files.exists(consoleFile)) {
            return;
        }

        if (runName == null) {
            runName = "unknown";
        }

        Path saRoot = getEcosystemManager().getFramework().getResultArchiveStore().getStoredArtifactsRoot();
        Path saRunConsoleFile = saRoot.resolve("ecosystem").resolve("runs").resolve(runName).resolve(consoleFile.getFileName().toString());

        try {
            Files.copy(consoleFile, saRunConsoleFile);
        } catch (IOException e) {
            logger.warn("Failed to copy the console log from " + consoleFile.toString());
        }
    }



    @Override
    public String submitRun(String runType, String requestor, String groupName, @NotNull String bundleName,
            @NotNull String testName, String mavenRepository, String obr, String stream, Properties overrides)
                    throws GalasaEcosystemManagerException {
        try {
            this.internalRunNumber++;

            Path overridesFile = null;

            // Create the overrides file if provided
            if (overrides != null && !overrides.isEmpty()) {
                overridesFile = this.linuxImage.getRunDirectory().resolve("run" + this.internalRunNumber + ".overrides");
                overrides.store(Files.newOutputStream(overridesFile, StandardOpenOption.CREATE_NEW), "Galasa Ecosystem Manager");
                this.runFiles.add(overridesFile);
            }

            // allocate the console log file

            Path consoleFile = this.linuxImage.getRunDirectory().resolve("run" + this.internalRunNumber + ".console");
            this.runFiles.add(consoleFile);
            
            // do we have a stream
            
            String streamObr  = null;
            String streamRepo = null;
            
            if (stream != null) {
                streamObr = getCpsProperty("framework.test.stream." + stream + ".obr");
                streamRepo = getCpsProperty("framework.test.stream." + stream + ".repo");
                
                if (streamObr == null) {
                    throw new GalasaEcosystemManagerException("Stream " + stream + " has been requested but the obr property is missing");
                }
                if (streamRepo == null) {
                    throw new GalasaEcosystemManagerException("Stream " + stream + " has been requested but the repo property is missing");
                }
            }

            // Build the command script
            
            StringBuilder runCommand = new StringBuilder();
            runCommand.append("#!/bin/bash\nnohup ");
            runCommand.append(getJavaInstallation().getJavaCommand());
            runCommand.append(" -jar ");
            runCommand.append(getBootJar().toString());
            runCommand.append(" --bootstrap file:");
            runCommand.append(getBootstrapFile().toString());
            if (streamRepo != null) {
                runCommand.append(" --remotemaven ");
                runCommand.append(removeHttps(streamRepo));
            }
            if (streamObr != null) {
                runCommand.append(" --obr ");
                runCommand.append(streamObr);
            }
            runCommand.append(" --remotemaven ");
            runCommand.append(removeHttps(getMavenRepo().toString()));
            runCommand.append(" --localmaven file:");
            runCommand.append(getMavenLocal().toString());
            runCommand.append(" --obr  mvn:dev.galasa/dev.galasa.uber.obr/");
            runCommand.append(getMavenVersion());
            runCommand.append("/obr ");
            runCommand.append(" --trace ");
            if (overridesFile != null) {
                runCommand.append(" --overrides ");
                runCommand.append(overridesFile.toString());
            }
            runCommand.append(" --test ");
            runCommand.append(bundleName);
            runCommand.append("/");
            runCommand.append(testName);
            runCommand.append(" > ");
            runCommand.append(consoleFile.toString());
            runCommand.append(" &\necho PROCESS=$!\nsleep 2");


            Files.write(this.scriptFile, runCommand.toString().getBytes(StandardCharsets.UTF_8));
            String response = getCommandShell().issueCommand("sh " + this.scriptFile.toString());

            Matcher matcher = processPattern.matcher(response);
            if (!matcher.find()) {
                throw new GalasaEcosystemManagerException("Unexpected response for the run.sh script:-\n" + response);
            }

            String runName = null;
            Instant expire = Instant.now().plus(2, ChronoUnit.MINUTES);
            while(expire.isAfter(Instant.now())) {
                Thread.sleep(2000);

                String consoleContents = new String(Files.readAllBytes(consoleFile), StandardCharsets.UTF_8);
                if (consoleContents.contains("Exiting launcher due to exception")) {
                    logger.error("Run terminatated early");

                    if (consoleContents.contains("java.net.SocketException: Network is unreachable (connect failed)") || 
                            consoleContents.contains("java.net.UnknownHostException")) {
                        logger.error("Network blip, marking as resource unavailable");
                        saveConsoleLog(consoleFile);
                        this.runFiles.remove(consoleFile);
                        throw new ResourceUnavailableException("Unable to complete installation of ecosystem, due to network blip");
                    }

                    break;
                }

                matcher = runnamePattern.matcher(consoleContents);
                if (matcher.find()) {
                    runName = matcher.group(1);
                    break;
                }
            }
            if (runName == null) {
                saveConsoleLog(consoleFile);
                this.runFiles.remove(consoleFile);
                throw new GalasaEcosystemManagerException("Unable to locate the assigned run name to the submitted run");
            }

            LocalRun localRun = new LocalRun(bundleName, testName, groupName, runName, consoleFile, overridesFile);
            addLocalRun(localRun);
            this.runNameFiles.put(consoleFile, runName);
            if (overridesFile != null) {
                this.runNameFiles.put(overridesFile, runName);
            }

            logger.info("Submitted test run with run name of " + runName);

            return runName;
        } catch(Exception e) {
            throw new GalasaEcosystemManagerException("Failed to submit run to local ecosystem", e);
        }

    }
    
    
    // TODO - Hacky to get round cacerts issue in java manager, ie functionality not there yet
    private String removeHttps(String url) {
        if (url.startsWith("https://cicscit.hursley.ibm.com")) {
            return url.replace("https://cicscit.hursley.ibm.com", "http://cicscit.hursley.ibm.com");
        }
        if (url.startsWith("https://nexus.cics-ts.hur.hdclab.intranet.ibm.com/")) {
            return url.replace("https://nexus.cics-ts.hur.hdclab.intranet.ibm.com/", "http://nexus.cics-ts.hur.hdclab.intranet.ibm.com:81/");
        }
        
        return url;
    }

    @Override
    public ICommandShell getCommandShell() throws GalasaEcosystemManagerException {
        try {
            return this.linuxImage.getCommandShell();
        } catch (LinuxManagerException e) {
            throw new GalasaEcosystemManagerException("Problem obtaining command shell", e);
        }
    }

    @Override
    public void startSimPlatform() throws GalasaEcosystemManagerException {
        SimPlatformInstance simPlatformInstance = getSimPlatformInstance();
        if (simPlatformInstance != null) {
            throw new GalasaEcosystemManagerException("SimPlatform is already started");
        }

        try {
            // copy our script file for running simplatform
            this.scriptFile = getGalasaConfigDirectory().resolve("simplatform.sh");
            saveSimPlatformNumber++;
            Path consoleFile = getRunHome().resolve("simplatform" + saveSimPlatformNumber + ".console");
            IBundleResources bundleResources = getEcosystemManager().getArtifactManager().getBundleResources(getClass());

            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("JAVA_CMD", getJavaInstallation().getJavaCommand());
            parameters.put("SIMPLATFORM_JAR", getSimplatformJar().toString());
            parameters.put("SIMPLATFORM_CONSOLE", consoleFile.toString());
            InputStream is = bundleResources.retrieveSkeletonFile("local/simplatform.sh", parameters);
            Files.copy(is, this.scriptFile);

            getCommandShell().issueCommand("chmod +x " + this.scriptFile.toString());

            String response = getCommandShell().issueCommand(this.scriptFile.toString());

            Matcher matcher = processPattern.matcher(response);
            if (!matcher.find()) {
                throw new GalasaEcosystemManagerException("Unexpected response for the simplatform.sh script:-\n" + response);
            }

            int processNumber = Integer.parseInt(matcher.group(1));

            SimPlatformInstance instance = new SimPlatformInstance(processNumber, consoleFile, this.linuxImage.getIpHost());
            setSimPlatformInstance(instance);

            logger.info("SimPlatform started as process " + processNumber);
        } catch(Exception e) {
            throw new GalasaEcosystemManagerException("Problem starting the SimPlatform process",e);
        }
    }

    @Override
    public void stopSimPlatform() throws GalasaEcosystemManagerException {
        SimPlatformInstance simPlatformInstance = getSimPlatformInstance();
        if (simPlatformInstance == null) {
            return;
        }

        try {
            getCommandShell().issueCommand("kill " + simPlatformInstance.getProcessNumber());
        } catch (Exception e) {
            throw new GalasaEcosystemManagerException("Problem stopping SimPlatform", e);
        }
        
        Path saRoot = getEcosystemManager().getFramework().getResultArchiveStore().getStoredArtifactsRoot();
        Path saConsoleFile = saRoot.resolve("ecosystem").resolve(simPlatformInstance.getConsoleFile().getFileName().toString());

        try {
            Files.copy(simPlatformInstance.getConsoleFile(), saConsoleFile);
        } catch (IOException e) {
            logger.warn("Failed to copy the console log from " + simPlatformInstance.getConsoleFile().toString());
        }
        
        setSimPlatformInstance(null);
    }

}
