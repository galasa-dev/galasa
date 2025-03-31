/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.maven.repository.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

import dev.galasa.framework.maven.repository.spi.IMavenRepository;

@Component(service = { URLStreamHandlerService.class }, property = { URLConstants.URL_HANDLER_PROTOCOL + "=mvn" })
public class GalasaMavenUrlHandlerService extends AbstractURLStreamHandlerService {

    private static final Log               logger                = LogFactory
            .getLog(GalasaMavenUrlHandlerService.class);
    private static final DateTimeFormatter dtf                   = DateTimeFormatter.ofPattern("uuuuMMddHHmmss");

    @Reference
    private IMavenRepository               galasaRepository;

    @Override
    public URLConnection openConnection(URL arg0) throws IOException {

        String[] parts = arg0.getPath().split("/");
        if (parts.length != 4) {
            throw new MalformedURLException("Must have 4 parts in the maven artifact reference - " + arg0);
        }

        String groupId = parts[0].trim();
        String artifactId = parts[1].trim();
        String version = parts[2].trim();
        String packaging = parts[3].trim();

        if (groupId.isEmpty()) {
            throw new MalformedURLException("groupId is missing - " + arg0);
        }
        if (artifactId.isEmpty()) {
            throw new MalformedURLException("artifactId is missing - " + arg0);
        }
        if (version.isEmpty()) {
            throw new MalformedURLException("version is missing - " + arg0);
        }
        if (packaging.isEmpty()) {
            throw new MalformedURLException("packaging is missing - " + arg0);
        }

        URL result = fetchArtifact(groupId, artifactId, version, packaging);
        if (result == null) {
            throw new IOException("Unable to locate maven artifact " + arg0);
        }

        return result.openConnection();
    }

    private URL fetchArtifact(String groupid, String artifactid, String version,
            String type) throws IOException {
        logger.trace("Resolving maven artifact " + groupid + ":" + artifactid + ":" + version + ":" + type);

        if (groupid.equals("dev.galasa") && type.equals("obr") && version.equals("LATEST")) {
            String latestVersion = resolveLatest(groupid, artifactid, type);
            if (latestVersion == null) {
                return null;
            }
            logger.trace("Maven artifact " + groupid + ":" + artifactid + ":" + version + ":" + type + " resolved to " + groupid + ":" + artifactid + ":" + latestVersion + ":" + type);
            version = latestVersion;
        }

        URL localRepository = galasaRepository.getLocalRepository();
        logger.trace("Checking local repository " + localRepository.toExternalForm());

        // *** Check the local repository first, if the file exists
        URL localFile;
        Path pathLocalFile;
        try {
            localFile = buildArtifactUrl(localRepository, groupid, artifactid, version, buildArtifactFilename(artifactid, version, type));
            pathLocalFile = Paths.get(localFile.toURI());
            logger.trace("Looking for file " + pathLocalFile.toFile().getAbsolutePath());
            if (pathLocalFile.toFile().exists()) {
                logger.trace("Found in local repository at " + localFile.toExternalForm());
                // Don't use the a local SNAPSHOT. It will be checked to see if it's the latest later on
                if (!(groupid.equals("dev.galasa") && version.endsWith("-SNAPSHOT"))) {
                    return localFile;
                }
            } else {
                localFile = null;
            }
        } catch (Exception e) {
            throw new IOException("Problem with local maven repository");
        }

        if (galasaRepository.getRemoteRepositories() == null) {
            return null;
        }

        Files.createDirectories(pathLocalFile.getParent());

        if (version.endsWith("-SNAPSHOT")) {
            URL remoteArtifact = fetchSnapshotArtifact(pathLocalFile, groupid, artifactid, version, type);
            return remoteArtifact == null ? localFile : remoteArtifact;
        } else {
            return fetchReleaseArtifact(pathLocalFile, groupid, artifactid, version, type);
        }
    }

    private String resolveLatest(String groupid, String artifactid, String type) throws IOException {
        Path tempMetadata = null;
        for (URL remoteRepository : galasaRepository.getRemoteRepositories()) {
            tempMetadata = getTempMetadata(remoteRepository, groupid, artifactid, null);
            if (tempMetadata != null) {
                break;
            }
        }
        String resolvedVersion = null;
        if (tempMetadata != null) {        
            try {
                MetadataXpp3Reader reader = new MetadataXpp3Reader();
                Metadata metadata = reader.read(Files.newInputStream(tempMetadata));
    
                Versioning versioning = metadata.getVersioning();
                if (versioning != null) {
                    String latest = versioning.getLatest();
                    if (latest != null) {
                        resolvedVersion = latest;
                    } else {
                        List<String> versions = versioning.getVersions();
                        if (!versions.isEmpty()) {
                            resolvedVersion = Collections.max(versions);
                        }
                    }
                }
            } catch (XmlPullParserException e) {
            } finally {
                Files.delete(tempMetadata);
            }
        }
        
        if (resolvedVersion != null) {
            logger.debug("Version 'LATEST' resolved to " + resolvedVersion);
        } else {
            logger.error("Unable to resolve vesion 'LATEST'");
        }
        
        return resolvedVersion;
    }

    private URL fetchSnapshotArtifact(Path localArtifact, String groupid, String artifactid, String version,
            String type) throws IOException {
        Path localTimestamp = localArtifact.resolveSibling(localArtifact.getFileName().toString() + ".lastupdated");

        long lastupdated = -1;

        // *** Get the local last updated timestamp if it exists
        if (localTimestamp.toFile().exists() && localArtifact.toFile().exists()) {
            try {
                lastupdated = Long.parseLong(new String(Files.readAllBytes(localTimestamp), "utf-8"));
            } catch (Exception e) {
                // NOP
            }
        }

        // *** has it been updated today
        LocalDate today = LocalDate.now();
        long startOfDay = today.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        if (lastupdated >= startOfDay) {
            logger.info("Downloaded snapshot is still valid");
            return localArtifact.toUri().toURL();
        }

        logger.debug("Looking for updated snapshot of " + groupid + ":" + artifactid + ":" + version + ":" + type);

        for (URL remoteRepository : galasaRepository.getRemoteRepositories()) {
            URL found = retrieveSnapshot(remoteRepository, lastupdated, localArtifact, localTimestamp, groupid,
                    artifactid, version, type);
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    private URL retrieveSnapshot(URL repository, long lastupdated, Path localArtifact, Path localTimestamp,
            String groupid, String artifactid, String version, String type) throws IOException {

        Path tempMetadata = getTempMetadata(repository, groupid, artifactid, version);
        if (tempMetadata == null) {
            return null;
        }

        String snapshotSuffix = null;
        long updatedTime = 0;
        try {
            MetadataXpp3Reader reader = new MetadataXpp3Reader();
            Metadata metadata = reader.read(Files.newInputStream(tempMetadata));

            Versioning versioning = metadata.getVersioning();
            if (versioning != null) {
                String sUpdatedtime = versioning.getLastUpdated();
                LocalDateTime updated = LocalDateTime.parse(sUpdatedtime, dtf);
                updatedTime = updated.toEpochSecond(ZoneOffset.UTC);

                if (updatedTime < lastupdated) {
                    logger.debug("Snapshot is up to date");
                    return localArtifact.toUri().toURL();
                }

                Snapshot snapshot = versioning.getSnapshot();
                if (snapshot == null) {
                    return null;
                }

                if (snapshot.isLocalCopy()) {
                    snapshotSuffix = version;
                } else {
                    snapshotSuffix = version.replace("SNAPSHOT",
                            snapshot.getTimestamp() + "-" + snapshot.getBuildNumber());
                }
            }
        } catch (XmlPullParserException e) {
            return null;
        } finally {
            Files.delete(tempMetadata);
        }

        URL urlRemoteFile = buildArtifactUrl(repository, groupid, artifactid, version,
                buildArtifactFilename(artifactid, snapshotSuffix, type));
        logger.debug("Attempting to download " + urlRemoteFile);

        URLConnection connection = urlRemoteFile.openConnection();
        connection.setConnectTimeout(300000);
        connection.setReadTimeout(300000);
        connection.setDoOutput(false);
        connection.connect();

        try {
            Files.copy(connection.getInputStream(), localArtifact, StandardCopyOption.REPLACE_EXISTING);
        } catch (FileNotFoundException e) {
            return null;
        }

        logger.trace("Snapshot artifact downloaded from " + urlRemoteFile);

        Files.write(localTimestamp, Long.toString(Instant.now().getEpochSecond()).getBytes());

        return localArtifact.toUri().toURL();
    }

    private Path getTempMetadata(URL repository, String groupid, String artifactid, String version) throws IOException {
      Path tempMetadata = Files.createTempFile("metadata", ".xml");
      try {
          URL urlRemoteFile = buildArtifactUrl(repository, groupid, artifactid, version, "maven-metadata.xml");
          logger.debug("Attempting to download " + urlRemoteFile);

          URLConnection connection = urlRemoteFile.openConnection();
          connection.setConnectTimeout(300000);
          connection.setReadTimeout(300000);
          connection.setDoOutput(false);
          connection.connect();
          Files.copy(connection.getInputStream(), tempMetadata, StandardCopyOption.REPLACE_EXISTING);
      } catch (FileNotFoundException e) {
          Files.delete(tempMetadata);
          return null;
      }
      return tempMetadata;
    }

    private URL fetchReleaseArtifact(Path localArtifact, String groupid, String artifactid, String version, String type)
            throws IOException {
        if (localArtifact.toFile().exists()) {
            logger.trace("Release artifact already in Galasa repository");
            return localArtifact.toUri().toURL();
        }

        for (URL remoteRepository : galasaRepository.getRemoteRepositories()) {
            if (getArtifact(remoteRepository, localArtifact, groupid, artifactid, version)) {
                return localArtifact.toUri().toURL();
            }
        }

        return null;
    }

    private static final int FIVE_MINUTES_IN_MS_UNITS = 3000000;

    private boolean getArtifact(URL repository, Path localArtifact, String groupid, String artifactid,
            String version) throws IOException {
        logger.debug("Checking " + repository);

        // *** Read the artifact
        URL urlRemoteFile = buildArtifactUrl(repository, groupid, artifactid, version,
                localArtifact.getFileName().toString());
        int connectionTimeoutMilliSecs = FIVE_MINUTES_IN_MS_UNITS; 
        int readTimeoutMilliSecs = FIVE_MINUTES_IN_MS_UNITS;
        logger.debug("Attempting to download " + urlRemoteFile+ 
                    " with connection timeout of "+Integer.toString(connectionTimeoutMilliSecs)+"ms "+
                    "and read timeout of "+Integer.toString(readTimeoutMilliSecs)+"ms "
                    );
        URLConnection connection = urlRemoteFile.openConnection();
        connection.setDoOutput(false);

        connection.setConnectTimeout(connectionTimeoutMilliSecs);
        connection.setReadTimeout(readTimeoutMilliSecs);

        try {
            logger.debug("Connecting now...");
            connection.connect();
            logger.debug("Connected OK...");
            Files.copy(connection.getInputStream(), localArtifact, StandardCopyOption.REPLACE_EXISTING);
        } catch (FileNotFoundException e) {
            logger.trace("Release artifact "+ urlRemoteFile+" failed to download. File not found." );
            return false;
        } catch (Exception e) {
            // Re-throw any exception after tracing it.
            logger.trace("Release artifact "+ urlRemoteFile+" failed to download.",e );
            throw e ; 
        }

        logger.trace("Release artifact downloaded from " + urlRemoteFile);

        return true;
    }

    public URL buildArtifactUrl(URL repository, String groupid, String artifactid, String version,
            String filename) throws IOException {
        String groupidDirectory = groupid.replaceAll("\\.", "/");

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(repository.toExternalForm());

        appendSlashIfNotEndingInSlash(stringBuilder);

        stringBuilder.append(groupidDirectory);
        appendSlashIfNotEndingInSlash(stringBuilder);
        stringBuilder.append(artifactid);
        appendSlashIfNotEndingInSlash(stringBuilder);
        if (version != null) {
            stringBuilder.append(version);
        }
        appendSlashIfNotEndingInSlash(stringBuilder);
        stringBuilder.append(filename);

        // *** Read the artifact
        return new URL(stringBuilder.toString());
    }

    /**
     * Sometimes the obr location URL or other parts of the artifact location 
     * may have a trailing slash already, in which case don't add another slash.
     */
    private void appendSlashIfNotEndingInSlash(StringBuilder stringBuilder) {
        if( ! stringBuilder.toString().endsWith("/") ) {
            stringBuilder.append("/");
        }
    }

    private static String buildArtifactFilename(String artifactid, String version, String filename) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(artifactid);
        stringBuilder.append("-");
        stringBuilder.append(version);
        stringBuilder.append(".");
        stringBuilder.append(filename);

        return stringBuilder.toString();
    }
}