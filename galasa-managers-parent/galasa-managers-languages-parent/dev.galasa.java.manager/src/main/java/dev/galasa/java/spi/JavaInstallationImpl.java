/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.java.spi;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;

import dev.galasa.CpuArchitecture;
import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.OperatingSystem;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.java.IJavaInstallation;
import dev.galasa.java.JavaManagerException;
import dev.galasa.java.JavaType;
import dev.galasa.java.JavaVersion;
import dev.galasa.java.internal.JavaManagerImpl;
import dev.galasa.java.internal.properties.CodeCoverageSaveCredentials;
import dev.galasa.java.internal.properties.CodeCoverageSaveLocation;
import dev.galasa.java.internal.properties.DefaultVersion;
import dev.galasa.java.internal.properties.DownloadLocation;
import dev.galasa.java.internal.properties.JacocoAgentLocation;
import dev.galasa.java.internal.properties.UseCodeCoverage;

public abstract class JavaInstallationImpl implements IJavaInstallation {

    private final static Log                   logger       = LogFactory.getLog(JavaInstallationImpl.class);

    private final JavaManagerImpl javaManager;
    private final JavaType        javaType; 
    private final OperatingSystem operatingSystem;
    private final CpuArchitecture cpuArchitecture;
    private final JavaVersion     javaVersion;
    private final String          javaJvm;
    private final String          javaTag; 

    private Path archive;
    private Path agent;
    private String downloadLocation;
    
    private static final Pattern patternNumber = Pattern.compile("(\\d+)\\Q.exec\\E$");

    public JavaInstallationImpl(IJavaManagerSpi javaManager, 
            JavaType javaType, 
            OperatingSystem operatingSystem, 
            CpuArchitecture cpuArchitecture,
            JavaVersion     javaVersion,
            String javaJvm, 
            String javaTag) throws JavaManagerException {
        this.javaManager     = (JavaManagerImpl)javaManager;
        this.javaType        = javaType;
        this.operatingSystem = operatingSystem;
        this.cpuArchitecture = cpuArchitecture;
        this.javaJvm         = javaJvm;
        this.javaTag         = javaTag;

        this.javaManager.registerJavaInstallationForTag(javaTag, this);

        if (javaVersion == JavaVersion.vDefault) {
            this.javaVersion = DefaultVersion.get();
            logger.info("Using default Java version " + this.javaVersion + " for installation tag " + this.javaTag);
        } else {
            this.javaVersion     = javaVersion;
        }
    }

    @Override
    public Path retrieveArchive() throws JavaManagerException, ResourceUnavailableException {
        if (archive != null) {
            return archive;
        }

        String downloadLocation = getDownloadLocation();
        if (downloadLocation.startsWith("http:") || downloadLocation.startsWith("https:")) {
            return downloadHttp(downloadLocation);
        } else {
            throw new JavaManagerException("Unsupported archive location " + downloadLocation);
        }
    }

    private Path downloadHttp(String downloadLocation) throws JavaManagerException, ResourceUnavailableException {

        logger.trace("Retrieving Java archive from " + downloadLocation);

        URI uri;
        try {
            uri = new URI(downloadLocation);
        } catch (URISyntaxException e) {
            throw new JavaManagerException("Invalid Java archive download location", e);
        }

        IHttpManagerSpi httpManager = this.javaManager.getHttpManager();
        IHttpClient client = httpManager.newHttpClient();
        client.setURI(uri);

        try (CloseableHttpResponse response = client.getFile(uri.getPath())) {

            Path archive = Files.createTempFile("galasa.java.", ".archive");
            archive.toFile().deleteOnExit();

            HttpEntity entity = response.getEntity();

            Files.copy(entity.getContent(), archive, StandardCopyOption.REPLACE_EXISTING);

            this.archive = archive;

            return archive;
        } catch (ConnectionClosedException e) {
            logger.error("Transfer connection closed early, usually caused by network instability, marking as resource unavailable so can try again later",e);
            throw new ResourceUnavailableException("Network error downloading Java archive from " + uri.toString());
        } catch (Exception e) {
            throw new JavaManagerException("Unable to download Java archive " + downloadLocation, e);
        }
    }

    @Override
    public Path retrieveJacocoAgent() throws JavaManagerException {

        String downloadLocation = JacocoAgentLocation.get();
        if (downloadLocation == null) {
            throw new JavaManagerException("The location of the Jacoco Agent has not been provided");
        }

        logger.trace("Retrieving Jacoco Agent from " + downloadLocation);

        URI uri;
        try {
            uri = new URI(downloadLocation);
        } catch (URISyntaxException e) {
            throw new JavaManagerException("Invalid Java archive download location", e);
        }

        IHttpManagerSpi httpManager = this.javaManager.getHttpManager();
        IHttpClient client = httpManager.newHttpClient();
        client.setURI(uri);

        try (CloseableHttpResponse response = client.getFile(uri.getPath())) {

            Path agent = Files.createTempFile("galasa.jacoco.", ".agent");
            agent.toFile().deleteOnExit();

            HttpEntity entity = response.getEntity();

            Files.copy(entity.getContent(), agent, StandardCopyOption.REPLACE_EXISTING);

            this.agent = agent;

            return agent;
        } catch (Exception e) {
            throw new JavaManagerException("Unable to download jacoco agent " + downloadLocation, e);
        }
    }

    @Override
    public String getArchiveFilename() throws JavaManagerException {
        String downloadLocation = getDownloadLocation();

        URL url;
        try {
            url = new URL(downloadLocation);
        } catch (MalformedURLException e) {
            throw new JavaManagerException("Invalid Java archive download location", e);
        }

        String fileName = url.getPath();
        fileName = fileName.replaceAll("\\\\", "/");
        int pos = fileName.lastIndexOf("/");
        if (pos < 0) {
            return fileName;
        }

        return fileName.substring(pos + 1);
    }

    private String getDownloadLocation() throws JavaManagerException {
        if (this.downloadLocation != null) {
            return this.downloadLocation;
        }

        this.downloadLocation = DownloadLocation.get(this.javaType, this.operatingSystem, this.cpuArchitecture, this.javaVersion, this.javaJvm);
        if (this.downloadLocation == null) {
            throw new JavaManagerException("Unable to determine location of Java archive for type=" 
                    + this.javaType
                    + ", os=" 
                    + this.operatingSystem
                    + ", cpu="
                    + this.cpuArchitecture
                    + ", version="
                    + this.javaVersion
                    + ", jvm="
                    + this.javaJvm);
        }


        if (this.downloadLocation.startsWith("http:") || this.downloadLocation.startsWith("https:")) {
            return this.downloadLocation;
        } else {
            throw new JavaManagerException("Unsupported archive location " + this.downloadLocation);
        }
    }

    protected void discard() {
        if (this.archive != null) {
            try {
                Files.delete(archive);
            } catch(Exception e) {
                logger.trace("Failed to delete downloaded Java archive at " + this.archive);
            }
        }

        if (this.agent != null) {
            try {
                Files.delete(agent);
            } catch(Exception e) {
                logger.trace("Failed to delete downloaded Jacoco Agent at " + this.archive);
            }
        }
    }

    public String getTag() {
        return this.javaTag;
    }

    protected boolean isCodeCoverageRequested() throws JavaManagerException {
        return UseCodeCoverage.get();
    }

    public void saveCodeCoverageExecs(List<Path> execPaths) throws JavaManagerException {
        if (!UseCodeCoverage.get()) {
            return;
        }
        
        if (execPaths.isEmpty()) {
            return;
        }

        String saveLocation = CodeCoverageSaveLocation.get();
        String saveCredsId  = CodeCoverageSaveCredentials.get();

        if (saveLocation == null) {
            return;
        }
        
        ICredentialsUsernamePassword creds = null;
        if (saveCredsId != null) {
            ICredentials tempCreds;
            try {
                tempCreds = this.javaManager.getFramework().getCredentialsService().getCredentials(saveCredsId);
            } catch (CredentialsException e) {
                throw new JavaManagerException("Problem accessing credentials", e);
            }
            if (tempCreds == null) {
                throw new JavaManagerException("Failed to retrieve credentials for ID " + saveCredsId);
            }
            if (!(tempCreds instanceof ICredentialsUsernamePassword)) {
                throw new JavaManagerException("credentials for ID " + saveCredsId + " are not username/password type");
            }
            
            creds = (ICredentialsUsernamePassword) tempCreds;
        }

        // First determine where we are going to save them

        IRun testRun = this.javaManager.getFramework().getTestRun();
        String testName = testRun.getTest();

        String location = CodeCoverageSaveLocation.get() + "/" + testName + ".zip";

        if (location.startsWith("http:") || location.startsWith("https:")) {
            System.out.println(location);
        } else {
            throw new JavaManagerException("Unable to save code coverage execs as location is unsupported " + location);
        }

        // Create a zip locally
        
        String className = testRun.getTestClassName();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ZipOutputStream zos = new ZipOutputStream(baos);
            for(Path exec : execPaths) {
                String fileName = exec.getFileName().toString();
                
                // extract the rolling number if there is one
                Matcher matcher = patternNumber.matcher(fileName);
                if (matcher.find()) {
                    fileName = className + matcher.group(1) + ".exec";
                }
                
                ZipEntry entry = new ZipEntry(fileName);
                zos.putNextEntry(entry);
                Files.copy(exec, zos);
                zos.closeEntry();
            }
            zos.close();
        } catch(Exception e) {
            throw new JavaManagerException("Unable to create jacoco execs zip", e);
        }
        
        // Send the file to the target location
        
        try {
            logger.debug("Saving jacoco exec zip to " + location);
            
            URI uriLocation = new URI(location); 
            IHttpClient client = this.javaManager.getHttpManager().newHttpClient();
            client.setURI(uriLocation);
            
            if (creds != null) {
                client.setAuthorisation(creds.getUsername(), creds.getPassword());
            }
            
            String locationPath = uriLocation.getPath();
            
            HttpClientResponse<byte[]> response = client.putBinary(locationPath, baos.toByteArray());
            
            if (response.getStatusCode() != 200 && response.getStatusCode() != 201) {
                throw new JavaManagerException("Error response from save jacoco location - " + response.getStatusLine());
            }
            
            logger.info("Jacoco exec zip file has been saved at " + location);
        } catch(Exception e) {
            throw new JavaManagerException("Unable to send jacoco zip to save location", e);
        }
    }
}
